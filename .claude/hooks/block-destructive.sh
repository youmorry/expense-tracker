#!/bin/bash
# PreToolUse hook: Block destructive Bash commands
# Exit 0 + JSON with deny = block the command
# Exit 0 without output    = allow the command

set -euo pipefail

INPUT=$(cat)
TOOL_NAME=$(echo "$INPUT" | jq -r '.tool_name // empty')

# Only inspect Bash tool calls
if [[ "$TOOL_NAME" != "Bash" ]]; then
  exit 0
fi

COMMAND=$(echo "$INPUT" | jq -r '.tool_input.command // empty')

deny() {
  local reason="$1"
  jq -n --arg reason "$reason" '{
    hookSpecificOutput: {
      hookEventName: "PreToolUse",
      permissionDecision: "deny",
      permissionDecisionReason: $reason
    }
  }'
  exit 0
}

# --- Destructive file operations ---
if echo "$COMMAND" | grep -qE '^\s*rm\s+.*-[a-zA-Z]*r[a-zA-Z]*f|^\s*rm\s+.*-[a-zA-Z]*f[a-zA-Z]*r|^\s*rm\s+-rf'; then
  deny "Recursive force delete (rm -rf) is blocked. Please specify files explicitly."
fi

if echo "$COMMAND" | grep -qE '^\s*mkfs|^\s*dd\s.*\bof='; then
  deny "Disk formatting / overwriting commands are blocked."
fi

# --- Git destructive operations ---
if echo "$COMMAND" | grep -qE 'git\s+push\s+.*--force|git\s+push\s+-f\b'; then
  deny "Force push is blocked. Use --force-with-lease instead if necessary."
fi

if echo "$COMMAND" | grep -qE 'git\s+reset\s+--hard'; then
  deny "git reset --hard is blocked. Use git stash or git reset --soft instead."
fi

if echo "$COMMAND" | grep -qE 'git\s+clean\s+.*-[a-zA-Z]*f'; then
  deny "git clean -f is blocked. Untracked files should be removed manually."
fi

# --- Secrets exfiltration ---
if echo "$COMMAND" | grep -qE 'curl.*\|\s*sh|wget.*\|\s*sh|curl.*\|\s*bash|wget.*\|\s*bash'; then
  deny "Piping remote content to shell is blocked."
fi

if echo "$COMMAND" | grep -qE '>\s*/etc/|>\s*~/.ssh/|>\s*~/.aws/'; then
  deny "Writing to system/credentials directories is blocked."
fi

# --- Environment variable leaks ---
if echo "$COMMAND" | grep -qE '\bprintenv\b|\benv\b\s*$|set\s*$' && ! echo "$COMMAND" | grep -qE 'set\s+-'; then
  deny "Dumping all environment variables is blocked."
fi

# Command is safe
exit 0
