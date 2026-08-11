import type { ReactNode } from "react";
import styles from "./components.module.css";

export interface AppShellProps {
  children: ReactNode;
  details?: ReactNode;
  detailsState?: "visible" | "hidden";
  sidebar: ReactNode;
  sidebarState?: "expanded" | "collapsed";
  topBar: ReactNode;
}

export function AppShell({ children, details, detailsState = "visible", sidebar, sidebarState = "expanded", topBar }: AppShellProps) {
  return (
    <div className={styles.appShell} data-details={detailsState} data-sidebar={sidebarState}>
      <div className={styles.shellSidebar}>{sidebar}</div>
      <div className={styles.shellTopBar}>{topBar}</div>
      <main className={styles.shellMain}>{children}</main>
      {detailsState === "visible" && <div className={styles.shellDetails}>{details}</div>}
    </div>
  );
}
