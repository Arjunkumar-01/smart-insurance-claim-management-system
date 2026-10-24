import { createContext } from "react";
import type { NotificationSeverity } from "./NotificationProvider";

export interface NotificationContextValue {
  notify: (message: string, severity?: NotificationSeverity) => void;
}

export const NotificationContext = createContext<
  NotificationContextValue | undefined
>(undefined);
export {};
