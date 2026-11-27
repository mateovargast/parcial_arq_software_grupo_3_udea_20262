import { useState } from 'react';
import { NavLink, Outlet, useLocation } from 'react-router-dom';
import {
  AppBar, Avatar, Box, Chip, Divider, Drawer, IconButton, List, ListItemButton,
  ListItemIcon, ListItemText, Toolbar, Tooltip, Typography, useMediaQuery,
} from '@mui/material';
import { useTheme } from '@mui/material/styles';
import DashboardRoundedIcon from '@mui/icons-material/DashboardRounded';
import Inventory2OutlinedIcon from '@mui/icons-material/Inventory2Outlined';
import StorefrontOutlinedIcon from '@mui/icons-material/StorefrontOutlined';
import WidgetsOutlinedIcon from '@mui/icons-material/WidgetsOutlined';
import AddBoxOutlinedIcon from '@mui/icons-material/AddBoxOutlined';
import MenuRoundedIcon from '@mui/icons-material/MenuRounded';
import WarehouseOutlinedIcon from '@mui/icons-material/WarehouseOutlined';

const drawerWidth = 252;

const navigation = [
  { label: 'Resumen', path: '/', icon: <DashboardRoundedIcon /> },
  { label: 'Almacenes', path: '/warehouses', icon: <StorefrontOutlinedIcon /> },
  { label: 'Inventario', path: '/inventory', icon: <Inventory2OutlinedIcon /> },
  { label: 'Productos', path: '/products', icon: <WidgetsOutlinedIcon /> },
  { label: 'Registrar productos', path: '/products/new', icon: <AddBoxOutlinedIcon /> },
];

function titleForPath(path: string): string {
  if (path.includes('/inventory')) return 'Inventario';
  if (path.includes('/warehouses')) return 'Almacenes';
  if (path.includes('/products/new')) return 'Registro de productos';
  if (path.includes('/products/')) return 'Detalle del producto';
  if (path.includes('/products')) return 'Productos';
  return 'Resumen general';
}

export function AppShell() {
  const [mobileOpen, setMobileOpen] = useState(false);
  const theme = useTheme();
  const isDesktop = useMediaQuery(theme.breakpoints.up('md'));
  const location = useLocation();

  const drawer = (
    <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column', color: '#e8eff4' }}>
      <Box sx={{ px: 2.5, py: 2.75, display: 'flex', alignItems: 'center', gap: 1.5 }}>
        <Avatar variant="rounded" sx={{ bgcolor: '#22a884', color: '#10293d', width: 40, height: 40 }}>
          <WarehouseOutlinedIcon />
        </Avatar>
        <Box>
          <Typography fontWeight={750} letterSpacing="-.02em">Bodega</Typography>
          <Typography variant="caption" sx={{ color: '#a8bac7' }}>CONTROL DE INVENTARIO</Typography>
        </Box>
      </Box>
      <Divider sx={{ borderColor: 'rgba(255,255,255,.1)', mx: 2 }} />
      <Typography variant="overline" sx={{ px: 3, pt: 3, pb: 1, color: '#8298a7', letterSpacing: '.12em' }}>
        ESPACIO DE TRABAJO
      </Typography>
      <List sx={{ px: 1.5, pt: 0 }}>
        {navigation.map((item) => (
          <ListItemButton
            key={item.path}
            component={NavLink}
            to={item.path}
            onClick={() => setMobileOpen(false)}
            end={item.path === '/'}
            sx={{
              my: 0.4,
              minHeight: 46,
              borderRadius: 2,
              color: '#b8c6cf',
              '& .MuiListItemIcon-root': { color: 'inherit', minWidth: 40 },
              '&.active': { color: '#fff', bgcolor: 'rgba(34,168,132,.18)' },
              '&.active .MuiListItemIcon-root': { color: '#55d1ac' },
              '&:hover': { bgcolor: 'rgba(255,255,255,.07)', color: '#fff' },
            }}
          >
            <ListItemIcon>{item.icon}</ListItemIcon>
            <ListItemText primary={item.label} primaryTypographyProps={{ fontSize: 14, fontWeight: 600 }} />
          </ListItemButton>
        ))}
      </List>
      <Box sx={{ mt: 'auto', p: 2 }}>
        <Box sx={{ p: 1.75, borderRadius: 2, bgcolor: 'rgba(255,255,255,.06)' }}>
          <Typography variant="caption" sx={{ color: '#98abb8' }}>API</Typography>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mt: 0.5 }}>
            <Box sx={{ width: 7, height: 7, borderRadius: '50%', bgcolor: '#e1a34b' }} />
            <Typography variant="body2" fontWeight={600}>Estado por verificar</Typography>
          </Box>
        </Box>
      </Box>
    </Box>
  );

  return (
    <Box sx={{ minHeight: '100vh', display: 'flex' }}>
      <AppBar
        position="fixed"
        elevation={0}
        sx={{
          width: { md: `calc(100% - ${drawerWidth}px)` },
          ml: { md: `${drawerWidth}px` },
          bgcolor: 'rgba(255,255,255,.92)',
          color: 'text.primary',
          borderBottom: '1px solid #e8edf1',
          backdropFilter: 'blur(10px)',
        }}
      >
        <Toolbar sx={{ minHeight: '72px !important', px: { xs: 2, md: 4 } }}>
          {!isDesktop && (
            <IconButton onClick={() => setMobileOpen(true)} edge="start" sx={{ mr: 1.5 }} aria-label="Abrir navegación">
              <MenuRoundedIcon />
            </IconButton>
          )}
          <Box sx={{ flexGrow: 1 }}>
            <Typography variant="overline" color="text.secondary" sx={{ lineHeight: 1.2 }}>
              OPERACIONES · INVENTARIO
            </Typography>
            <Typography variant="h6" sx={{ lineHeight: 1.35 }}>{titleForPath(location.pathname)}</Typography>
          </Box>
          <Tooltip title="Datos consultados desde la API configurada">
            <Chip
              size="small"
              label="API · v1"
              variant="outlined"
              sx={{ borderColor: '#c7e8dd', color: '#21866c', bgcolor: '#f1fbf7', fontWeight: 650 }}
            />
          </Tooltip>
          <Avatar sx={{ ml: 2, width: 34, height: 34, bgcolor: '#e8f0f5', color: '#183b56', fontSize: 13, fontWeight: 700 }}>
            U
          </Avatar>
        </Toolbar>
      </AppBar>

      <Box component="nav" sx={{ width: { md: drawerWidth }, flexShrink: { md: 0 } }}>
        <Drawer
          variant={isDesktop ? 'permanent' : 'temporary'}
          open={isDesktop || mobileOpen}
          onClose={() => setMobileOpen(false)}
          ModalProps={{ keepMounted: true }}
          sx={{
            '& .MuiDrawer-paper': {
              width: drawerWidth,
              boxSizing: 'border-box',
              bgcolor: '#101b2d',
              border: 0,
            },
          }}
        >
          {drawer}
        </Drawer>
      </Box>
      <Box component="main" sx={{ flexGrow: 1, minWidth: 0, px: { xs: 2, sm: 3, lg: 4 }, pb: 5, pt: '96px' }}>
        <Outlet />
      </Box>
    </Box>
  );
}
