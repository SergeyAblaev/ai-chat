import type { ComponentProps, KeyboardEvent, ReactNode } from "react";
import { PrimarySendButton } from "./buttons";
import styles from "./components.module.css";

export interface PromptComposerProps {
  disabled?: boolean;
  leadingActions?: ReactNode;
  onChange: (value: string) => void;
  onSubmit: () => void;
  placeholder?: string;
  sendIcon: ReactNode;
  sending?: boolean;
  value: string;
}

export function PromptComposer({
  disabled = false,
  leadingActions,
  onChange,
  onSubmit,
  placeholder = "Ask anything…",
  sendIcon,
  sending = false,
  value,
}: PromptComposerProps) {
  const canSubmit = value.trim().length > 0 && !disabled && !sending;

  const submit = () => {
    if (canSubmit) onSubmit();
  };

  const handleSubmit: ComponentProps<"form">["onSubmit"] = (event) => {
    event.preventDefault();
    submit();
  };

  const handleKeyDown = (event: KeyboardEvent<HTMLTextAreaElement>) => {
    if (event.key === "Enter" && !event.shiftKey && !event.nativeEvent.isComposing) {
      event.preventDefault();
      submit();
    }
  };

  return (
    <form className={styles.composer} onSubmit={handleSubmit}>
      {leadingActions}
      <textarea
        aria-label="Message"
        className={styles.composerInput}
        disabled={disabled}
        onChange={(event) => onChange(event.target.value)}
        onKeyDown={handleKeyDown}
        placeholder={placeholder}
        rows={2}
        value={value}
      />
      <PrimarySendButton disabled={!canSubmit} icon={sendIcon} isLoading={sending} label="Send message" />
    </form>
  );
}
