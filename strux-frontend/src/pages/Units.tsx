import React, { useState } from 'react';
import {
  Box,
  Typography,
  Button,
  Paper,
  Grid,
  Card,
  CardContent,
  Chip,
  TextField,
  InputAdornment,
  LinearProgress,
} from '@mui/material';
import {
  Add as AddIcon,
  Search as SearchIcon,
  Home as HomeIcon,
} from '@mui/icons-material';

interface Unit {
  id: string;
  unitNumber: string;
  project: string;
  type: string;
  status: string;
  saleStatus: string;
  floor: number;
  rooms: number;
  area: number;
  price: number;
  progress: number;
}

const Units: React.FC = () => {
  const [searchTerm, setSearchTerm] = useState('');

  const units: Unit[] = [
    {
      id: '1',
      unitNumber: 'A-101',
      project: 'Bahçeşehir Konut Projesi',
      type: 'APARTMENT',
      status: 'UNDER_CONSTRUCTION',
      saleStatus: 'SOLD',
      floor: 1,
      rooms: 3,
      area: 120,
      price: 2500000,
      progress: 75,
    },
    {
      id: '2',
      unitNumber: 'B-205',
      project: 'Maltepe Residence',
      type: 'APARTMENT',
      status: 'PLANNED',
      saleStatus: 'AVAILABLE',
      floor: 2,
      rooms: 4,
      area: 150,
      price: 3200000,
      progress: 0,
    },
  ];

  const getStatusColor = (status: string) => {
    const colors: any = {
      PLANNED: 'default',
      UNDER_CONSTRUCTION: 'warning',
      COMPLETED: 'success',
      DELIVERED: 'info',
    };
    return colors[status] || 'default';
  };

  const getSaleStatusColor = (status: string) => {
    const colors: any = {
      AVAILABLE: 'success',
      RESERVED: 'warning',
      SOLD: 'error',
      NOT_FOR_SALE: 'default',
    };
    return colors[status] || 'default';
  };

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 3 }}>
        <Typography variant="h4" gutterBottom sx={{ fontWeight: 600 }}>
          Birimler
        </Typography>
        <Button variant="contained" startIcon={<AddIcon />}>
          Yeni Birim
        </Button>
      </Box>

      <Paper sx={{ p: 2, mb: 3 }}>
        <TextField
          fullWidth
          placeholder="Birim ara..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          InputProps={{
            startAdornment: (
              <InputAdornment position="start">
                <SearchIcon />
              </InputAdornment>
            ),
          }}
        />
      </Paper>

      <Grid container spacing={3}>
        {units.map((unit) => (
          <Grid item xs={12} sm={6} md={4} key={unit.id}>
            <Card>
              <CardContent>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    <HomeIcon color="primary" />
                    <Typography variant="h6">{unit.unitNumber}</Typography>
                  </Box>
                  <Chip
                    label={unit.saleStatus}
                    color={getSaleStatusColor(unit.saleStatus)}
                    size="small"
                  />
                </Box>

                <Typography variant="body2" color="text.secondary" gutterBottom>
                  {unit.project}
                </Typography>

                <Box sx={{ my: 2 }}>
                  <Typography variant="body2" color="text.secondary">
                    Kat: {unit.floor} | Oda: {unit.rooms}+1 | {unit.area}m²
                  </Typography>
                  <Typography variant="h6" sx={{ mt: 1 }}>
                    {unit.price.toLocaleString('tr-TR')} ₺
                  </Typography>
                </Box>

                <Chip
                  label={unit.status}
                  color={getStatusColor(unit.status)}
                  size="small"
                  sx={{ mb: 1 }}
                />

                <Box sx={{ mt: 2 }}>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                    <Typography variant="caption">İlerleme</Typography>
                    <Typography variant="caption">{unit.progress}%</Typography>
                  </Box>
                  <LinearProgress
                    variant="determinate"
                    value={unit.progress}
                    sx={{ height: 8, borderRadius: 4 }}
                  />
                </Box>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>
    </Box>
  );
};

export default Units;
