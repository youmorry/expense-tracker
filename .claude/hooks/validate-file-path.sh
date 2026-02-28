#!/bin/bash
# PreToolUse hook: Block Edit/Write to sensitive file paths
# Protects credentials, secrets, and system files from modification

set -euo pipefail

INPUT=$(cat)
TOOL_NAME=$(echo "$INPUT" | jq -r '.tool_name // empty')

# Only inspect Edit and Write tool calls
if [[ "$TOOL_NAME" != "Edit" && "$TOOL_NAME" != "Write" ]]; then
  exit 0
fi

FILE_PATH=$(echo "$INPUT" | jq -r '.tool_input.file_path // empty')

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

# --- Sensitive file patterns ---
case "$FILE_PATH" in
  *.env|*.env.local|*.env.production|*.env.*.local)
    deny "Editing environment files (.env*) is blocked to protect secrets."
    ;;
  */.ssh/*|*/.aws/*|*/.gnupg/*)
    deny "Editing credential directories (.ssh, .aws, .gnupg) is blocked."
    ;;
  */credentials*|*/secrets*|*secret_key*|*private_key*)
    deny "Editing files matching credential/secret patterns is blocked."
    ;;
  /etc/*)
    deny "Editing system configuration files (/etc/) is blocked."
    ;;
esac

# File path is safe
exit 0
