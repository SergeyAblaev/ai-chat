/// <reference types="node" />

import { readFileSync } from "node:fs";
import { describe, expect, it } from "vitest";

const tokens = readFileSync(new URL("./tokens.css", import.meta.url), "utf8");

describe("design tokens", () => {
  it("contains every exported token category", () => {
    expect(tokens).toContain("--bg-canvas: #f7f8fa");
    expect(tokens).toContain("--space-16: 64px");
    expect(tokens).toContain("--radius-full: 999px");
    expect(tokens).toContain("--layout-content-max: 760px");
    expect(tokens).toContain("--font-size-display: 32px");
    expect(tokens).toContain("--shadow-glass:");
    expect(tokens).toContain("--blur-glass-floating-toolbar: 28px");
    expect(tokens).toContain("--glass-highlight-gradient:");
  });

  it("contains only the supported light color scheme", () => {
    expect(tokens).toContain("color-scheme: light");
    expect(tokens).not.toContain("prefers-color-scheme");
    expect(tokens).not.toContain("[data-theme=\"dark\"]");
  });
});
