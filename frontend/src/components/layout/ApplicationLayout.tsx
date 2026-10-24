import {
  Box,
  Button,
  Container,
  Divider,
  Drawer,
  IconButton,
  List,
  ListItemButton,
  ListItemText,
  Menu,
  MenuItem,
  Toolbar,
  Typography,
} from "@mui/material";
import { NavLink, Outlet, useLocation, useNavigate } from "react-router-dom";
import { useState, type KeyboardEvent } from "react";
import { useAuth } from "../../auth/useAuth";
import { getNavigationItems, roleLabels } from "../../types/navigation";
import { WorkspaceBreadcrumbs } from "./WorkspaceBreadcrumbs";

function NavigationContent({ onNavigate }: { onNavigate?: () => void }) {
  const { user } = useAuth();
  const items = getNavigationItems(user?.roles ?? []);
  const location = useLocation();
  return (
    <>
      <Typography
        variant="caption"
        sx={{
          color: "#94A3B8",
          textTransform: "uppercase",
          letterSpacing: 0,
        }}
      >
        Workspace
      </Typography>
      <List disablePadding sx={{ mt: 1 }}>
        {items.map((item) => (
          <ListItemButton
            key={item.id}
            component={NavLink}
            to={item.route}
            onClick={onNavigate}
            aria-current={location.pathname === item.route ? "page" : undefined}
            sx={{
              color: "#FFFFFF",
              borderRadius: 1,
              px: 1.5,
              "&:hover": { bgcolor: "#08204D" },
              "&.active": { bgcolor: "#2563EB" },
            }}
          >
            <ListItemText
              primary={item.label}
              primaryTypographyProps={{ fontWeight: 700 }}
            />
          </ListItemButton>
        ))}
      </List>
    </>
  );
}

export function ApplicationLayout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [mobileOpen, setMobileOpen] = useState(false);
  const [menuAnchor, setMenuAnchor] = useState<null | HTMLElement>(null);
  const handleLogout = () => {
    setMenuAnchor(null);
    logout();
    navigate("/login", { replace: true });
  };
  return (
    <Box
      sx={{
        minHeight: "100vh",
        display: "flex",
        flexDirection: { xs: "column", sm: "row" },
        bgcolor: "background.default",
      }}
    >
      <Box
        component="aside"
        aria-label="SICMS workspace navigation"
        sx={{
          width: { xs: "100%", sm: 248 },
          minHeight: { xs: "auto", sm: "100vh" },
          flexShrink: 0,
          bgcolor: "#071A33",
          color: "#FFFFFF",
          display: { xs: "none", sm: "flex" },
          flexDirection: "column",
          px: { xs: 2.5, sm: 3 },
          py: { xs: 2, sm: 3 },
        }}
      >
        <Typography variant="h6" sx={{ fontWeight: 800, letterSpacing: 0 }}>
          SICMS
        </Typography>
        <Typography variant="body2" sx={{ mt: 0.75, color: "#94A3B8" }}>
          {user?.roles.map((role) => roleLabels[role]).join(" / ")}
        </Typography>
        <Divider sx={{ my: 3, borderColor: "#E2E8F0" }} />
        <NavigationContent />
      </Box>
      <Box
        sx={{ minWidth: 0, flex: 1, display: "flex", flexDirection: "column" }}
      >
        <Box
          component="header"
          sx={{
            bgcolor: "background.paper",
            borderBottom: 1,
            borderColor: "divider",
          }}
        >
          <Container maxWidth="xl">
            <Toolbar
              disableGutters
              sx={{
                minHeight: "56px !important",
                justifyContent: "space-between",
                gap: 2,
              }}
            >
              <IconButton
                aria-label="Open workspace navigation"
                onClick={() => setMobileOpen(true)}
                sx={{
                  display: { xs: "inline-flex", sm: "none" },
                  color: "primary.main",
                }}
              >
                ☰
              </IconButton>
              <Box sx={{ minWidth: 0, flex: 1 }}>
                <Typography variant="body2" color="text.secondary">
                  Authenticated workspace
                </Typography>
              </Box>
              <Button
                onClick={(event) => setMenuAnchor(event.currentTarget)}
                aria-haspopup="menu"
                aria-expanded={Boolean(menuAnchor)}
                sx={{ color: "primary.main", whiteSpace: "nowrap" }}
              >
                {user?.firstName} {user?.lastName}
              </Button>
              <Menu
                anchorEl={menuAnchor}
                open={Boolean(menuAnchor)}
                onClose={() => setMenuAnchor(null)}
              >
                <MenuItem disabled>{user?.email}</MenuItem>
                <MenuItem disabled>
                  {user?.roles.map((role) => roleLabels[role]).join(", ")}
                </MenuItem>
                <MenuItem onClick={handleLogout}>Log out</MenuItem>
              </Menu>
            </Toolbar>
          </Container>
        </Box>
        <Box component="main" sx={{ flex: 1, py: { xs: 4, sm: 6 } }}>
          <Container maxWidth="xl">
            <WorkspaceBreadcrumbs />
            <Outlet />
          </Container>
        </Box>
      </Box>
      <Drawer
        anchor="left"
        open={mobileOpen}
        onClose={() => setMobileOpen(false)}
        ModalProps={{ keepMounted: true }}
      >
        <Box
          role="dialog"
          aria-label="Workspace navigation"
          sx={{
            width: 260,
            height: "100%",
            bgcolor: "#071A33",
            color: "#FFFFFF",
            p: 3,
          }}
          onKeyDown={(event: KeyboardEvent<HTMLDivElement>) => {
            if (event.key === "Escape") setMobileOpen(false);
          }}
        >
          <Box
            sx={{
              display: "flex",
              justifyContent: "space-between",
              alignItems: "center",
            }}
          >
            <Typography variant="h6" sx={{ fontWeight: 800, letterSpacing: 0 }}>
              SICMS
            </Typography>
            <IconButton
              aria-label="Close workspace navigation"
              onClick={() => setMobileOpen(false)}
              sx={{ color: "#FFFFFF" }}
            >
              ×
            </IconButton>
          </Box>
          <Divider sx={{ my: 3, borderColor: "#E2E8F0" }} />
          <NavigationContent onNavigate={() => setMobileOpen(false)} />
        </Box>
      </Drawer>
    </Box>
  );
}
