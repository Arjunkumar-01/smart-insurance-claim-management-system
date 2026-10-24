import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Alert, Button, Paper } from "@mui/material";
import { useState } from "react";
import { ApiError } from "../../../api/api-error";
import { ErrorState } from "../../../components/feedback/ErrorState";
import { SkeletonState } from "../../../components/feedback/SkeletonState";
import { FormSection } from "../../../components/layout/FormSection";
import { PageHeader } from "../../../components/layout/PageHeader";
import { useNotifications } from "../../../components/feedback/useNotifications";
import { customerApi, customerKeys } from "../api/customer-api";
import { CustomerKycForm } from "../components/CustomerKycForm";
import { CustomerKycSummary } from "../components/CustomerKycSummary";
import type { UpdateCustomerKycRequest } from "../types/customer-types";

export function CustomerKycPage() {
  const queryClient = useQueryClient();
  const { notify } = useNotifications();
  const [editing, setEditing] = useState(false);
  const kycQuery = useQuery({
    queryKey: customerKeys.kyc(),
    queryFn: customerApi.getKyc,
    retry: false,
  });
  const mutation = useMutation({
    mutationFn: (payload: UpdateCustomerKycRequest) =>
      customerApi.updateKyc(payload),
    onSuccess: async (kyc) => {
      await queryClient.setQueryData(customerKeys.kyc(), kyc);
      setEditing(false);
      notify(
        kycQuery.data
          ? "KYC information updated."
          : "KYC information submitted.",
        "success",
      );
    },
  });
  if (kycQuery.isPending)
    return (
      <>
        <PageHeader
          eyebrow="Customer compliance"
          title="KYC Verification"
          description="Submit identity information for your customer record."
        />
        <SkeletonState rows={3} />
      </>
    );
  if (
    kycQuery.error &&
    !(kycQuery.error instanceof ApiError && kycQuery.error.kind === "not-found")
  )
    return (
      <>
        <PageHeader
          eyebrow="Customer compliance"
          title="KYC Verification"
          description="Submit identity information for your customer record."
        />
        <ErrorState
          message={
            kycQuery.error instanceof ApiError
              ? kycQuery.error.message
              : "Unable to load your KYC information."
          }
          onRetry={() => void kycQuery.refetch()}
        />
      </>
    );
  const kyc = kycQuery.data;
  if (!kyc || editing)
    return (
      <>
        <PageHeader
          eyebrow="Customer compliance"
          title="KYC Verification"
          description="Identity status is controlled by the SICMS backend."
        />
        <Paper
          elevation={0}
          sx={{ p: { xs: 2.5, sm: 4 }, border: 1, borderColor: "divider" }}
        >
          <FormSection
            title="Identity information"
            description="The original identity number is submitted securely and is not retained in the browser."
          >
            <CustomerKycForm
              pending={mutation.isPending}
              onSubmit={(values) => mutation.mutate(values)}
            />
          </FormSection>
          {mutation.error ? (
            <Alert severity="error" sx={{ mt: 3 }}>
              {mutation.error instanceof ApiError
                ? mutation.error.message
                : "Unable to submit KYC information."}
            </Alert>
          ) : null}
        </Paper>
      </>
    );
  return (
    <>
      <PageHeader
        eyebrow="Customer compliance"
        title="KYC Verification"
        description="Review the identity information and server-controlled verification status."
        actions={
          <Button variant="contained" onClick={() => setEditing(true)}>
            Update KYC
          </Button>
        }
      />
      <CustomerKycSummary kyc={kyc} />
    </>
  );
}
