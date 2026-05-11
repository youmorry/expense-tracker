import "@testing-library/jest-dom/vitest";
import { afterAll, afterEach, beforeAll } from "vitest";
import { server } from "./mocks/server";

// jsdom では Pointer Events API が未実装のため、Radix UI 等が使うメソッドを polyfill する
// @see https://github.com/jsdom/jsdom/issues/3683
Element.prototype.hasPointerCapture = () => false;
Element.prototype.setPointerCapture = () => {};
Element.prototype.releasePointerCapture = () => {};
// jsdom は Element#scrollTo を実装しないため no-op で埋める
Element.prototype.scrollTo = () => {};

beforeAll(() => {
  server.listen({ onUnhandledRequest: "error" });
});
afterEach(() => {
  server.resetHandlers();
});
afterAll(() => {
  server.close();
});
