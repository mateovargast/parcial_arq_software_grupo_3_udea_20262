import { Alert, Box, Button, Skeleton, Stack, Typography } from '@mui/material';
import InboxOutlinedIcon from '@mui/icons-material/InboxOutlined';
import type { ReactNode } from 'react';

interface RequestStateProps {
  loading: boolean;
  error?: string | null;
  empty?: boolean;
  emptyTitle?: string;
  emptyDescription?: string;
  onRetry?: () => void;
  children: ReactNode;
}

export function RequestState({
  loading,
  error,
  empty,
  emptyTitle = 'Aún no hay información',
  emptyDescription = 'Cuando haya datos disponibles, aparecerán aquí.',
  onRetry,
  children,
}: RequestStateProps) {
  if (loading) {
    return (
      <Stack spacing={1.5} sx={{ py: 1 }}>
        <Skeleton variant="rounded" height={56} />
        <Skeleton variant="rounded" height={220} />
      </Stack>
    );
  }
  if (error) {
    return (
      <Alert
        severity="error"
        action={onRetry ? <Button color="inherit" size="small" onClick={onRetry}>Reintentar</Button> : undefined}
      >
        {error}
      </Alert>
    );
  }
  if (empty) {
    return (
      <Box sx={{ py: 7, textAlign: 'center', color: 'text.secondary' }}>
        <InboxOutlinedIcon sx={{ fontSize: 38, color: '#9aa8b2' }} />
        <Typography variant="h6" color="text.primary" sx={{ mt: 1 }}>{emptyTitle}</Typography>
        <Typography sx={{ mt: 0.5 }}>{emptyDescription}</Typography>
      </Box>
    );
  }
  return children;
}
