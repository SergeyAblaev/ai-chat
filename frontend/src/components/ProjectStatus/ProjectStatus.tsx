import styles from "./ProjectStatus.module.css";

export function ProjectStatus() {
  return (
    <section className={styles.card} aria-labelledby="project-status-title">
      <p className={styles.eyebrow}>AI Chat Console</p>
      <h1 id="project-status-title">Frontend foundation ready</h1>
      <p>
        React, TypeScript, Vite, tests, linting, and the local backend proxy are configured.
      </p>
    </section>
  );
}
