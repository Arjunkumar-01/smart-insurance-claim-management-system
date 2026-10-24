import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Alert } from "@mui/material";
import { useNavigate, useSearchParams } from "react-router-dom";
import { PageHeader } from "../../../components/layout/PageHeader";
import { SkeletonState } from "../../../components/feedback/SkeletonState";
import { ErrorState } from "../../../components/feedback/ErrorState";
import { useNotifications } from "../../../components/feedback/useNotifications";
import { policyApi, policyKeys } from "../../policies/api/policy-api";
import { claimApi, claimKeys } from "../api/claim-api";
import { ClaimSubmissionForm } from "../components/ClaimSubmissionForm";
import type { SubmitClaimRequest } from "../types/claim-types";
export function ClaimSubmitPage() {
  const [params] = useSearchParams();
  const navigate = useNavigate();
  const client = useQueryClient();
  const { notify } = useNotifications();
  const policies = useQuery({
    queryKey: policyKeys.mine(0),
    queryFn: () => policyApi.mine(0),
  });
  const mutation = useMutation({
    mutationFn: (payload: SubmitClaimRequest) => claimApi.submit(payload),
    onSuccess: async (claim) => {
      await client.invalidateQueries({ queryKey: claimKeys.all });
      notify("Claim submitted.", "success");
      navigate(`/app/claims/${claim.claimId}`);
    },
  });
  if (policies.isPending) return <SkeletonState rows={4} />;
  if (policies.error || !policies.data)
    return <ErrorState message="Unable to load eligible policies." />;
  const eligible = policies.data.policies.filter(
    (policy) => policy.status === "ACTIVE",
  );
  return (
    <>
      <PageHeader
        eyebrow="Claims"
        title="Submit Claim"
        description="Submit a claim against an active policy."
      />
      {!eligible.length ? (
        <Alert severity="info">
          No active policies are available for claim submission.
        </Alert>
      ) : (
        <>
          {mutation.error ? (
            <Alert severity="error" sx={{ mb: 2 }}>
              The claim could not be submitted.
            </Alert>
          ) : null}
          <ClaimSubmissionForm
            policies={eligible}
            defaultPolicyId={params.get("policyId") || undefined}
            pending={mutation.isPending}
            onSubmit={(value) => mutation.mutate(value)}
          />
        </>
      )}
    </>
  );
}
