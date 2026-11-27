import { Card, CardContent, Stack, Typography } from '@mui/material';
import type { ReactNode } from 'react';

interface MetricCardProps {
  label: string;
  value: string;
  caption: string;
  icon: ReactNode;
  accent: string;
}

export function MetricCard({ label, value, caption, icon, accent }: MetricCardProps) {
  return (
    <Card sx={{ height: '100%' }}>
      <CardContent sx={{ p: 2.5 }}>
        <Stack direction="row" alignItems="flex-start" justifyContent="space-between">
          <Typography variant="body2" color="text.secondary" fontWeight={600}>{label}</Typography>
          <Stack alignItems="center" justifyContent="center" sx={{ width: 38, height: 38, borderRadius: 2, color: accent, bgcolor: `${accent}15` }}>
            {icon}
          </Stack>
        </Stack>
        <Typography variant="h4" sx={{ mt: 2, fontSize: 30 }}>{value}</Typography>
        <Typography variant="caption" color="text.secondary">{caption}</Typography>
      </CardContent>
    </Card>
  );
}
