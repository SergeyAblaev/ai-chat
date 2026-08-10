import { ProjectStatus } from "../components/ProjectStatus/ProjectStatus";
import styles from "./App.module.css";

export function App() {
  return (
    <main className={styles.app}>
      <ProjectStatus />
    </main>
  );
}
