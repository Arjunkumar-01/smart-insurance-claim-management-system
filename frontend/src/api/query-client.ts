import { QueryClient } from "@tanstack/react-query";

export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: (failureCount, error: unknown) => {
        if (
          typeof error === "object" &&
          error !== null &&
          "response" in error
        ) {
          const status = (error as { response?: { status?: number } }).response
            ?.status;
          if (status && [400, 401, 403, 404, 409].includes(status))
            return false;
        }
        return failureCount < 1;
      },
      staleTime: 30_000,
      refetchOnWindowFocus: false,
    },
    mutations: { retry: false },
  },
});
