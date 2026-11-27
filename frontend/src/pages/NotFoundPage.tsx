import { Button, Stack, Typography } from '@mui/material';
import { Link as RouterLink } from 'react-router-dom';

export function NotFoundPage() {
  return (
    <Stack alignItems="center" spacing={2} sx={{ py: 12, textAlign: 'center' }}>
      <Typography variant="h3" fontWeight={750}>404</Typography>
      <Typography color="text.secondary">No encontramos la página solicitada.</Typography>
      <Button component={RouterLink} to="/" variant="contained">Ir al resumen</Button>
    </Stack>
  );
}
