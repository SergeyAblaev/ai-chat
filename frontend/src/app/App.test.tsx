import { renderToStaticMarkup } from "react-dom/server";
import { describe, expect, it } from "vitest";

import { App } from "./App";

describe("App", () => {
  it("renders the secure backend connection screen", () => {
    const markup = renderToStaticMarkup(<App />);

    expect(markup).toContain("Connect to the backend");
    expect(markup).toContain('type="password"');
    expect(markup).toContain("Credentials stay in memory");
    expect(markup).toContain("Explore D05 demo");
  });
});
