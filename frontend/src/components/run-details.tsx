import type { ReactNode } from "react";
import styles from "./components.module.css";

export type ProviderName = "OPENAI" | "ANTHROPIC" | "GEMINI";
export type ProviderStepStatus = "failed" | "success" | "skipped" | "running";

const providerLabels: Record<ProviderName, string> = {
  ANTHROPIC: "Anthropic",
  GEMINI: "Gemini",
  OPENAI: "OpenAI",
};

const statusLabels: Record<ProviderStepStatus, string> = {
  failed: "Failed",
  running: "Running",
  skipped: "Skipped",
  success: "Success",
};

export interface ProviderStepProps {
  attemptCount?: number;
  durationMs?: number;
  message?: ReactNode;
  provider: ProviderName;
  status: ProviderStepStatus;
}

export function ProviderStep({ attemptCount, durationMs, message, provider, status }: ProviderStepProps) {
  return (
    <article className={styles.providerStep} data-status={status}>
      <div className={styles.providerHeader}>
        <span className={styles.providerName}>{providerLabels[provider]}</span>
        <span className={styles.statusChip} data-status={status}>{statusLabels[status]}</span>
      </div>
      {(durationMs !== undefined || attemptCount !== undefined) && (
        <div className={styles.providerMeta}>
          {durationMs !== undefined && <span>{durationMs} ms</span>}
          {attemptCount !== undefined && <span>{attemptCount} {attemptCount === 1 ? "attempt" : "attempts"}</span>}
        </div>
      )}
      {message && <div className={styles.providerMessage}>{message}</div>}
    </article>
  );
}

export interface RunDetailItem {
  label: ReactNode;
  value: ReactNode;
}

export interface RunDetailsPanelProps {
  details?: readonly RunDetailItem[];
  steps: readonly ProviderStepProps[];
  title?: ReactNode;
}

export function RunDetailsPanel({ details = [], steps, title = "Run details" }: RunDetailsPanelProps) {
  return (
    <aside aria-label="Run details" className={styles.detailsPanel}>
      <h2 className={styles.detailsTitle}>{title}</h2>
      {details.length > 0 && (
        <dl className={styles.detailsMeta}>
          {details.map((detail, index) => (
            <div className={styles.detailsMetaRow} key={index}>
              <dt>{detail.label}</dt>
              <dd>{detail.value}</dd>
            </div>
          ))}
        </dl>
      )}
      <div className={styles.providerList}>
        {steps.map((step, index) => <ProviderStep {...step} key={`${step.provider}-${index}`} />)}
      </div>
    </aside>
  );
}
