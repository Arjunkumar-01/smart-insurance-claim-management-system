import { RouterProvider } from "react-router-dom";
import { AuthProvider } from "../auth/AuthProvider";
import { ErrorBoundary } from "../components/feedback/ErrorBoundary";
import { appRouter } from "./router";

export function App() {
  return (
    <ErrorBoundary>
      <AuthProvider>
        <RouterProvider router={appRouter} />
      </AuthProvider>
    </ErrorBoundary>
  );
}
