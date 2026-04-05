import axios, { AxiosError } from 'axios';
import { BizError, type ProblemDetail } from '../types/Error';

const apiClient = axios.create({
  baseURL: '/api',
  timeout: 5000,
});

apiClient.interceptors.request.use(
  (config) => {
    const accessToken = localStorage.getItem('accessToken');

    if (accessToken) {
      config.headers.Authorization = `Bearer ${accessToken}`;
    }

    return config;
  },
  (error) => {
    return Promise.reject(error);
  },
);

apiClient.interceptors.response.use(
  (response) => {
    return response;
  },
  (error: AxiosError<ProblemDetail>) => {
    if (error.response) {
      if (error.response.status === 401) {
        localStorage.removeItem('accessToken');
      }

      const response = error.response.data;

      return Promise.reject(new BizError(response));
    }
    return Promise.reject(error);
  },
);

export default apiClient;
