import { Alert, Snackbar } from "@mui/material";
import { useCallback, useMemo, useState, type PropsWithChildren } from "react";
import { NotificationContext } from "./notification-context";

export type NotificationSeverity = "success" | "info" | "warning" | "error";
interface Notification {
  id: number;
  message: string;
  severity: NotificationSeverity;
}

export function NotificationProvider({ children }: PropsWithChildren) {
  const [notification, setNotification] = useState<Notification | null>(null);
  const notify = useCallback(
    (message: string, severity: NotificationSeverity = "info") =>
      setNotification({ id: Date.now(), message, severity }),
    [],
  );
  const value = useMemo(() => ({ notify }), [notify]);
  return (
    <NotificationContext.Provider value={value}>
      {children}
      {notification ? (
        <Snackbar
          open
          autoHideDuration={6000}
          onClose={() => setNotification(null)}
          anchorOrigin={{ vertical: "bottom", horizontal: "right" }}
        >
          <Alert
            onClose={() => setNotification(null)}
            severity={notification.severity}
            variant="filled"
            role="status"
            sx={{ width: "100%" }}
          >
            {notification.message}
          </Alert>
        </Snackbar>
      ) : null}
    </NotificationContext.Provider>
  );
}
