import axios, { AxiosError } from 'axios';
import { BizError, type ProblemDetail } from '../types/Error';

export const AUTH_UNAUTHORIZED_EVENT = 'app:auth-unauthorized';

const apiClient = axios.create({
  baseURL: '/api',
  timeout: 5000,
  withCredentials: true,
  xsrfCookieName: 'XSRF-TOKEN',
  xsrfHeaderName: 'X-XSRF-TOKEN',
});

function readCookie(name: string) {
  const cookies = document.cookie.split(';');
  for (const cookie of cookies) {
    const [rawKey, ...rawValueParts] = cookie.trim().split('=');
    if (rawKey === name) {
      return decodeURIComponent(rawValueParts.join('='));
    }
  }
  return null;
}

apiClient.interceptors.request.use((config) => {
  const method = config.method?.toLowerCase();
  const requiresCsrf =
    method === 'post' || method === 'put' || method === 'patch' || method === 'delete';

  if (requiresCsrf) {
    const csrfToken = readCookie('XSRF-TOKEN');
    if (csrfToken) {
      config.headers.set('X-XSRF-TOKEN', csrfToken);
    }
  }

  return config;
});

apiClient.interceptors.response.use(
  (response) => {
    return response;
  },
  (error: AxiosError<ProblemDetail>) => {
    if (error.response) {
      if (error.response.status === 401) {
        window.dispatchEvent(new Event(AUTH_UNAUTHORIZED_EVENT));
      }

      const response =
        error.response.data ??
        ({
          status: error.response.status,
          title: 'Request failed',
          detail: '请求失败',
          code: error.response.status === 401 ? 'UNAUTHORIZED' : 'INVALID_REQUEST',
        } satisfies ProblemDetail);

      return Promise.reject(new BizError(response));
    }
    return Promise.reject(error);
  },
);

export async function refreshCsrfToken() {
  await apiClient.get('/auth/csrf');
}

export default apiClient;
