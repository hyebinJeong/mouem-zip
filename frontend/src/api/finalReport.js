import api from '@/api/index.js';

export const getFinalReport = async (reportId) => {
  const res = await api.get(`/api/reports/${reportId}`, {});
  return res.data;
};

export const getFinalReportByUserAndRegistry = async (userId, registryId) => {
  const res = await api.get('/api/reports', {
    params: { userId, registryId },
  });
  return res.data;
};
