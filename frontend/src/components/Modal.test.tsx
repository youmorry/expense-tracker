import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";

import { Modal } from "@/components/Modal";

describe("Modal", () => {
  it("renders title and children when open", () => {
    render(
      <Modal open={true} onClose={vi.fn()} title="Test Modal">
        <p>Modal content</p>
      </Modal>,
    );

    expect(screen.getByText("Test Modal")).toBeInTheDocument();
    expect(screen.getByText("Modal content")).toBeInTheDocument();
  });

  it("renders nothing when closed", () => {
    render(
      <Modal open={false} onClose={vi.fn()} title="Hidden Modal">
        <p>Hidden content</p>
      </Modal>,
    );

    expect(screen.queryByText("Hidden Modal")).not.toBeInTheDocument();
    expect(screen.queryByText("Hidden content")).not.toBeInTheDocument();
  });

  it("renders description when provided", () => {
    render(
      <Modal open={true} onClose={vi.fn()} title="Title" description="Some description">
        <p>Content</p>
      </Modal>,
    );

    expect(screen.getByText("Some description")).toBeInTheDocument();
  });

  it("calls onClose when close button is clicked", async () => {
    const user = userEvent.setup();
    const onClose = vi.fn();

    render(
      <Modal open={true} onClose={onClose} title="Closable Modal">
        <p>Content</p>
      </Modal>,
    );

    await user.click(screen.getByRole("button", { name: /close/i }));

    expect(onClose).toHaveBeenCalledOnce();
  });

  it("calls onClose when overlay is clicked", async () => {
    const user = userEvent.setup();
    const onClose = vi.fn();

    render(
      <Modal open={true} onClose={onClose} title="Overlay Modal">
        <p>Content</p>
      </Modal>,
    );

    const dialogElement = screen.getByRole("dialog");
    const overlay = dialogElement.parentElement?.querySelector("[data-slot='dialog-overlay']");
    if (!overlay) throw new Error("Overlay not found");
    await user.click(overlay);

    expect(onClose).toHaveBeenCalledOnce();
  });

  it("calls onClose when Escape key is pressed", async () => {
    const user = userEvent.setup();
    const onClose = vi.fn();

    render(
      <Modal open={true} onClose={onClose} title="Escape Modal">
        <p>Content</p>
      </Modal>,
    );

    await user.keyboard("{Escape}");

    expect(onClose).toHaveBeenCalledOnce();
  });
});
