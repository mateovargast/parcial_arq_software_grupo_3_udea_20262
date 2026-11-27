import { Box, Typography } from '@mui/material';
import type { ReactNode } from 'react';

interface PageHeaderProps {
  title: string;
  description: string;
  action?: ReactNode;
}

export function PageHeader({ title, description, action }: PageHeaderProps) {
  return (
    <Box sx={{ display: 'flex', alignItems: { xs: 'flex-start', sm: 'center' }, justifyContent: 'space-between', gap: 2, mb: 3, flexWrap: 'wrap' }}>
      <Box>
        <Typography variant="h4" sx={{ fontSize: { xs: 27, md: 32 } }}>{title}</Typography>
        <Typography color="text.secondary" sx={{ mt: 0.6 }}>{description}</Typography>
      </Box>
      {action}
    </Box>
  );
}
