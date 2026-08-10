import { renderToStaticMarkup } from "react-dom/server";
import { describe, expect, it } from "vitest";

import { App } from "./App";

describe("App", () => {
  it("renders the frontend foundation status", () => {
    const markup = renderToStaticMarkup(<App />);

    expect(markup).toContain("AI Chat Console");
    expect(markup).toContain("Frontend foundation ready");
  });
});
