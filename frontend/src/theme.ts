import { createTheme } from '@mui/material/styles';

export const theme = createTheme({
  palette: {
    mode: 'light',
    primary: { main: '#183b56', dark: '#10293d', light: '#e8f0f5' },
    secondary: { main: '#22a884' },
    background: { default: '#f4f6f8', paper: '#ffffff' },
    success: { main: '#21866c' },
    warning: { main: '#c27b15' },
    error: { main: '#c84d4d' },
    text: { primary: '#1e2936', secondary: '#687684' },
  },
  shape: { borderRadius: 12 },
  typography: {
    fontFamily: '"Inter", "Segoe UI", sans-serif',
    h4: { fontWeight: 700, letterSpacing: '-0.035em' },
    h5: { fontWeight: 700, letterSpacing: '-0.025em' },
    h6: { fontWeight: 650 },
    button: { fontWeight: 650, textTransform: 'none' },
  },
  components: {
    MuiCard: {
      styleOverrides: {
        root: {
          border: '1px solid #e8edf1',
          boxShadow: '0 6px 24px rgba(20, 43, 61, 0.045)',
        },
      },
    },
    MuiTableCell: {
      styleOverrides: {
        head: { color: '#687684', fontWeight: 700, backgroundColor: '#f8fafb' },
        root: { borderColor: '#edf0f2' },
      },
    },
  },
});
