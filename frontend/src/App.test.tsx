import { render, screen } from "@testing-library/react";
import App from "./App";

describe("App", () => {
  it("アプリケーションのタイトルが表示される", () => {
    render(<App />);
    expect(screen.getByRole("heading", { name: "expense-tracker" })).toBeInTheDocument();
  });
});
