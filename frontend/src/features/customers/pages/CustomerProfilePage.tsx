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
import { CustomerProfileForm } from "../components/CustomerProfileForm";
import { CustomerProfileSummary } from "../components/CustomerProfileSummary";
import type { CustomerProfileRequest } from "../types/customer-types";

export function CustomerProfilePage() {
  const queryClient = useQueryClient();
  const { notify } = useNotifications();
  const [editing, setEditing] = useState(false);
  const profileQuery = useQuery({
    queryKey: customerKeys.profile(),
    queryFn: customerApi.getProfile,
    retry: false,
  });
  const mutation = useMutation({
    mutationFn: (payload: CustomerProfileRequest) =>
      profileQuery.data
        ? customerApi.updateProfile(payload)
        : customerApi.createProfile(payload),
    onSuccess: async (profile) => {
      await queryClient.setQueryData(customerKeys.profile(), profile);
      setEditing(false);
      notify(
        profileQuery.data ? "Profile updated." : "Profile created.",
        "success",
      );
    },
    onError: async (error) => {
      if (error instanceof ApiError && error.kind === "conflict") {
        await queryClient.invalidateQueries({
          queryKey: customerKeys.profile(),
        });
      }
    },
  });

  if (profileQuery.isPending)
    return (
      <>
        <PageHeader
          eyebrow="Customer"
          title="Customer Profile"
          description="Manage your customer record securely."
        />
        <SkeletonState rows={6} />
      </>
    );
  if (
    profileQuery.error &&
    !(
      profileQuery.error instanceof ApiError &&
      profileQuery.error.kind === "not-found"
    )
  )
    return (
      <>
        <PageHeader
          eyebrow="Customer"
          title="Customer Profile"
          description="Manage your customer record securely."
        />
        <ErrorState
          message={
            profileQuery.error instanceof ApiError
              ? profileQuery.error.message
              : "Unable to load your profile."
          }
          onRetry={() => void profileQuery.refetch()}
        />
      </>
    );
  const profile = profileQuery.data;
  if (!profile || editing)
    return (
      <>
        <PageHeader
          eyebrow="Customer"
          title={
            profile ? "Edit Customer Profile" : "Complete your Customer Profile"
          }
          description="Keep your personal and contact information current."
        />
        {mutation.error instanceof ApiError &&
        mutation.error.kind === "conflict" ? (
          <Alert severity="warning" sx={{ mb: 3 }}>
            A profile already exists. Refreshing your saved profile.
          </Alert>
        ) : null}
        <Paper
          elevation={0}
          sx={{ p: { xs: 2.5, sm: 4 }, border: 1, borderColor: "divider" }}
        >
          <FormSection
            title="Personal and contact information"
            description="Name and email are managed by your account and are read-only here."
          >
            <CustomerProfileForm
              profile={profile}
              pending={mutation.isPending}
              onCancel={profile ? () => setEditing(false) : undefined}
              onSubmit={(values) => mutation.mutate(values)}
            />
          </FormSection>
        </Paper>
      </>
    );
  return (
    <>
      <PageHeader
        eyebrow="Customer"
        title="Customer Profile"
        description="Manage your customer record securely."
        actions={
          <Button variant="contained" onClick={() => setEditing(true)}>
            Edit profile
          </Button>
        }
      />
      <CustomerProfileSummary profile={profile} />
    </>
  );
}
