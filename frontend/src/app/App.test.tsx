import { renderToStaticMarkup } from "react-dom/server";
import { describe, expect, it } from "vitest";

import { App } from "./App";

describe("App", () => {
  it("renders the D05 resilient fallback scenario", () => {
    const markup = renderToStaticMarkup(<App />);

    expect(markup).toContain("Resilient fallback");
    expect(markup).toContain("The CAP theorem says");
    expect(markup).toContain("Fallback");
    expect(markup).toContain("OpenAI");
    expect(markup).toContain("Anthropic");
    expect(markup).toContain("Gemini");
  });
});
