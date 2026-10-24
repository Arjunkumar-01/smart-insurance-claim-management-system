import {
  Alert,
  Box,
  Button,
  Divider,
  IconButton,
  InputAdornment,
  Link as MuiLink,
  Paper,
  TextField,
  Typography,
} from "@mui/material";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";
import { ApiError } from "../../../api/api-error";
import { useAuth } from "../../../auth/useAuth";
import { getLandingPath } from "../../../types/roles";
import { loginSchema, type LoginFormValues } from "../schemas/auth-schemas";

export function LoginPage() {
  const { isAuthenticated, login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [showPassword, setShowPassword] = useState(false);
  const [submitError, setSubmitError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const message = (location.state as { message?: string } | null)?.message;
  const {
    register,
    handleSubmit,
    setError,
    formState: { errors },
  } = useForm<LoginFormValues>({ resolver: zodResolver(loginSchema) });

  useEffect(() => {
    if (isAuthenticated) navigate("/app", { replace: true });
  }, [isAuthenticated, navigate]);

  const onSubmit = async (values: LoginFormValues) => {
    setSubmitError(null);
    setIsSubmitting(true);
    try {
      const session = await login(values);
      navigate(getLandingPath(session.user.roles), { replace: true });
    } catch (error) {
      if (error instanceof ApiError) {
        Object.entries(error.fieldErrors).forEach(([field, message]) => {
          if (field === "email" || field === "password")
            setError(field, { message });
        });
        setSubmitError(error.message);
      } else {
        setSubmitError("Unable to sign in. Please try again.");
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <Box
      sx={{
        minHeight: "100vh",
        display: "grid",
        gridTemplateColumns: {
          xs: "1fr",
          md: "minmax(360px, 0.9fr) minmax(460px, 1.1fr)",
        },
        bgcolor: "background.default",
      }}
    >
      <Box
        sx={{
          display: { xs: "none", md: "flex" },
          position: "relative",
          overflow: "hidden",
          bgcolor: "#071A33",
          color: "#FFFFFF",
          p: 6,
          alignItems: "flex-end",
        }}
      >
        <Box sx={{ position: "relative", maxWidth: 420 }}>
          <Typography
            variant="overline"
            sx={{ color: "#06B6D4", letterSpacing: 0 }}
          >
            SICMS
          </Typography>
          <Typography
            variant="h1"
            sx={{
              mt: 1,
              color: "#FFFFFF",
              fontSize: { xs: "2.2rem", lg: "4rem" },
            }}
          >
            Claims operations, clearly structured.
          </Typography>
          <Typography sx={{ mt: 2, color: "#94A3B8", maxWidth: 360 }}>
            A secure workspace for the people and processes behind every
            insurance claim.
          </Typography>
        </Box>
      </Box>
      <Box sx={{ display: "grid", placeItems: "center", p: { xs: 3, sm: 6 } }}>
        <Paper
          component="main"
          elevation={0}
          sx={{
            width: "100%",
            maxWidth: 480,
            p: { xs: 3, sm: 5 },
            border: 1,
            borderColor: "divider",
          }}
        >
          <Typography variant="overline" color="primary.main">
            Secure access
          </Typography>
          <Typography variant="h2" sx={{ mt: 1, fontSize: "2rem" }}>
            Welcome back
          </Typography>
          <Typography color="text.secondary" sx={{ mt: 1 }}>
            Sign in to continue to SICMS.
          </Typography>
          {message ? (
            <Alert severity="success" sx={{ mt: 3 }}>
              {message}
            </Alert>
          ) : null}
          {submitError ? (
            <Alert severity="error" role="alert" sx={{ mt: 3 }}>
              {submitError}
            </Alert>
          ) : null}
          <Box
            component="form"
            onSubmit={handleSubmit(onSubmit)}
            noValidate
            sx={{ mt: 3, display: "grid", gap: 2 }}
          >
            <TextField
              label="Email"
              type="email"
              autoComplete="email"
              {...register("email")}
              error={Boolean(errors.email)}
              helperText={errors.email?.message}
            />
            <TextField
              label="Password"
              type={showPassword ? "text" : "password"}
              autoComplete="current-password"
              {...register("password")}
              error={Boolean(errors.password)}
              helperText={errors.password?.message}
              InputProps={{
                endAdornment: (
                  <InputAdornment position="end">
                    <IconButton
                      type="button"
                      onClick={() => setShowPassword((visible) => !visible)}
                      aria-label={
                        showPassword ? "Hide password" : "Show password"
                      }
                      edge="end"
                    >
                      {showPassword ? "Hide" : "Show"}
                    </IconButton>
                  </InputAdornment>
                ),
              }}
            />
            <Button
              type="submit"
              variant="contained"
              size="large"
              disabled={isSubmitting}
            >
              {isSubmitting ? "Signing in..." : "Sign in"}
            </Button>
          </Box>
          <Divider sx={{ my: 3 }} />
          <Typography variant="body2" color="text.secondary">
            New customer?{" "}
            <MuiLink component={Link} to="/register">
              Create an account
            </MuiLink>
          </Typography>
        </Paper>
      </Box>
    </Box>
  );
}
