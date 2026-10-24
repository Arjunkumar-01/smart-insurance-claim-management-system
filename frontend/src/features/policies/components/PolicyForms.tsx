import { zodResolver } from "@hookform/resolvers/zod";
import { Box, Button, TextField } from "@mui/material";
import { useForm } from "react-hook-form";
import {
  cancellationSchema,
  nomineeSchema,
  renewalSchema,
} from "../schemas/policy-schemas";

export function NomineeForm({
  pending,
  onSubmit,
}: {
  pending?: boolean;
  onSubmit: (value: {
    name: string;
    relationship: string;
    contactNumber: string;
  }) => void;
}) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({ resolver: zodResolver(nomineeSchema) });
  return (
    <Box
      component="form"
      onSubmit={handleSubmit(onSubmit)}
      sx={{ display: "grid", gap: 2 }}
    >
      <TextField
        label="Name"
        {...register("name")}
        error={Boolean(errors.name)}
        helperText={errors.name?.message as string}
      />
      <TextField
        label="Relationship"
        {...register("relationship")}
        error={Boolean(errors.relationship)}
        helperText={errors.relationship?.message as string}
      />
      <TextField
        label="Contact number"
        {...register("contactNumber")}
        error={Boolean(errors.contactNumber)}
        helperText={errors.contactNumber?.message as string}
      />
      <Button type="submit" variant="contained" disabled={pending}>
        {pending ? "Saving..." : "Save nominee"}
      </Button>
    </Box>
  );
}
export function RenewalForm({
  pending,
  onSubmit,
  currentEndDate,
  newEndDate,
  renewalPremium,
}: {
  pending?: boolean;
  onSubmit: (value: Record<string, never>) => void;
  currentEndDate: string;
  newEndDate: string;
  renewalPremium: string;
}) {
  const { handleSubmit } = useForm<Record<string, never>>({
    resolver: zodResolver(renewalSchema),
  });
  return (
    <Box
      component="form"
      onSubmit={handleSubmit(onSubmit)}
      sx={{ display: "grid", gap: 2 }}
    >
      <Box
        sx={{
          display: "grid",
          gridTemplateColumns: { xs: "1fr", sm: "repeat(3, 1fr)" },
          gap: 2,
        }}
      >
        <Box>
          <strong>Current expiry</strong>
          <br />
          {currentEndDate}
        </Box>
        <Box>
          <strong>New expiry</strong>
          <br />
          {newEndDate}
        </Box>
        <Box>
          <strong>Renewal premium</strong>
          <br />
          {renewalPremium}
        </Box>
      </Box>
      <Button type="submit" variant="contained" disabled={pending}>
        {pending ? "Renewing..." : "Confirm renewal"}
      </Button>
    </Box>
  );
}
export function CancellationForm({
  pending,
  onSubmit,
}: {
  pending?: boolean;
  onSubmit: (value: { reason: string }) => void;
}) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({ resolver: zodResolver(cancellationSchema) });
  return (
    <Box
      component="form"
      onSubmit={handleSubmit(onSubmit)}
      sx={{ display: "grid", gap: 2 }}
    >
      <TextField
        label="Reason"
        multiline
        minRows={3}
        {...register("reason")}
        error={Boolean(errors.reason)}
        helperText={errors.reason?.message as string}
      />
      <Button
        type="submit"
        color="warning"
        variant="contained"
        disabled={pending}
      >
        {pending ? "Submitting..." : "Request cancellation"}
      </Button>
    </Box>
  );
}
