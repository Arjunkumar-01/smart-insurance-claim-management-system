import { zodResolver } from "@hookform/resolvers/zod";
import {
  Box,
  Button,
  FormControl,
  FormControlLabel,
  Radio,
  RadioGroup,
  Typography,
} from "@mui/material";
import { useForm } from "react-hook-form";
import { paymentSchema } from "../schemas/payment-schema";
import type { PaymentMethod } from "../types/payment-types";

export function PaymentMethodForm({
  pending,
  onSubmit,
}: {
  pending?: boolean;
  onSubmit: (value: { paymentMethod: PaymentMethod }) => void;
}) {
  const { register, handleSubmit } = useForm<{ paymentMethod: PaymentMethod }>({
    resolver: zodResolver(paymentSchema),
    defaultValues: { paymentMethod: "CARD" },
  });
  return (
    <Box
      component="form"
      onSubmit={handleSubmit(onSubmit)}
      sx={{ display: "grid", gap: 2 }}
    >
      <Typography variant="subtitle2">Payment method</Typography>
      <FormControl>
        <RadioGroup defaultValue="CARD">
          <FormControlLabel
            value="CARD"
            control={<Radio {...register("paymentMethod")} />}
            label="Card (simulated)"
          />
          <FormControlLabel
            value="BANK_TRANSFER"
            control={<Radio {...register("paymentMethod")} />}
            label="Bank Transfer (simulated)"
          />
          <FormControlLabel
            value="UPI"
            control={<Radio {...register("paymentMethod")} />}
            label="UPI (simulated)"
          />
        </RadioGroup>
      </FormControl>
      <Button type="submit" variant="contained" disabled={pending}>
        {pending ? "Processing..." : "Complete payment"}
      </Button>
    </Box>
  );
}
