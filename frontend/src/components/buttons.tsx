import type { ButtonHTMLAttributes, ReactNode } from "react";
import styles from "./components.module.css";

type NativeButtonProps = Omit<ButtonHTMLAttributes<HTMLButtonElement>, "children">;

export interface GlassIconButtonProps extends NativeButtonProps {
  icon: ReactNode;
  label: string;
  size?: "sm" | "md";
  visualState?: "default" | "hover" | "pressed";
}

export function GlassIconButton({ icon, label, size = "md", visualState = "default", className = "", ...props }: GlassIconButtonProps) {
  return (
    <button
      {...props}
      aria-label={label}
      className={`${styles.buttonReset} ${styles.glassButton} ${className}`}
      data-size={size}
      data-state={visualState}
      type={props.type ?? "button"}
    >
      {icon}
    </button>
  );
}

export interface PrimaryGlassButtonProps extends NativeButtonProps {
  children: ReactNode;
  leadingIcon?: ReactNode;
}

export function PrimaryGlassButton({ children, leadingIcon, className = "", ...props }: PrimaryGlassButtonProps) {
  return (
    <button
      {...props}
      className={`${styles.buttonReset} ${styles.primaryGlassButton} ${className}`}
      type={props.type ?? "button"}
    >
      {leadingIcon}
      <span>{children}</span>
    </button>
  );
}

export interface PrimarySendButtonProps extends NativeButtonProps {
  icon: ReactNode;
  label: string;
  isLoading?: boolean;
}

export function PrimarySendButton({ icon, label, isLoading = false, className = "", disabled, ...props }: PrimarySendButtonProps) {
  return (
    <button
      {...props}
      aria-busy={isLoading}
      aria-label={label}
      className={`${styles.buttonReset} ${styles.sendButton} ${className}`}
      disabled={disabled || isLoading}
      type={props.type ?? "submit"}
    >
      {icon}
    </button>
  );
}
