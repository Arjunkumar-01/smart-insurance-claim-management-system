import { zodResolver } from "@hookform/resolvers/zod";
import { Box, Button, MenuItem, TextField } from "@mui/material";
import { useForm } from "react-hook-form";
import { claimSchema } from "../schemas/claim-schema";
import type { SubmitClaimRequest } from "../types/claim-types";
export function ClaimSubmissionForm({
  policies,
  defaultPolicyId,
  pending,
  onSubmit,
}: {
  policies: Array<{
    policyId: string;
    policyNumber: string;
    productName: string;
    coverageAmount: number;
    startDate: string;
    endDate: string;
  }>;
  defaultPolicyId?: string;
  pending?: boolean;
  onSubmit: (value: SubmitClaimRequest) => void;
}) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<SubmitClaimRequest>({
    resolver: zodResolver(claimSchema),
    defaultValues: { policyId: defaultPolicyId },
  });
  return (
    <Box
      component="form"
      onSubmit={handleSubmit(onSubmit)}
      sx={{ display: "grid", gap: 2, maxWidth: 760 }}
    >
      <TextField
        select
        label="Policy"
        {...register("policyId")}
        error={Boolean(errors.policyId)}
        helperText={errors.policyId?.message}
      >
        {policies.map((policy) => (
          <MenuItem value={policy.policyId} key={policy.policyId}>
            {policy.policyNumber} · {policy.productName}
          </MenuItem>
        ))}
      </TextField>
      <TextField
        select
        label="Claim type"
        defaultValue=""
        {...register("claimType")}
        error={Boolean(errors.claimType)}
        helperText={errors.claimType?.message}
      >
        <MenuItem value="HEALTH">Health</MenuItem>
        <MenuItem value="LIFE">Life</MenuItem>
        <MenuItem value="VEHICLE">Motor / Vehicle</MenuItem>
        <MenuItem value="TRAVEL">Travel</MenuItem>
      </TextField>
      <TextField
        label="Claim amount"
        type="number"
        inputProps={{ min: 0.01, step: "0.01" }}
        {...register("claimAmount")}
        error={Boolean(errors.claimAmount)}
        helperText={errors.claimAmount?.message}
      />
      <TextField
        label="Incident date"
        type="date"
        InputLabelProps={{ shrink: true }}
        {...register("incidentDate")}
        error={Boolean(errors.incidentDate)}
        helperText={errors.incidentDate?.message}
      />
      <TextField
        label="Description"
        multiline
        minRows={4}
        {...register("description")}
        error={Boolean(errors.description)}
        helperText={errors.description?.message}
      />
      <Button type="submit" variant="contained" disabled={pending}>
        {pending ? "Submitting..." : "Submit claim"}
      </Button>
    </Box>
  );
}
