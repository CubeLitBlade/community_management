import { useCallback, useMemo } from 'react';
import apiClient from '../api/apiClient';
import type { LoginRequest, LoginResponse } from '../types/Account';
import useAccount from './useAccount';

export default function useAuth() {
  const { profile, isLoading, fetchAccount, logout } = useAccount();

  const login = useCallback(
    async (username: string, password: string) => {
      const request: LoginRequest = {
        username,
        password,
      };

      const response = await apiClient.post<LoginResponse>('/auth/login', request);
      localStorage.setItem('accessToken', response.data.accessToken);
      await fetchAccount();
      return response.data;
    },
    [fetchAccount],
  );

  const changePassword = useCallback(
    async (currentPassword: string, newPassword: string) => {
      await apiClient.post('/account/change-password', {
        currentPassword,
        newPassword,
      });
      await fetchAccount();
    },
    [fetchAccount],
  );

  const isAuthenticated = useMemo(() => profile !== null, [profile]);

  return {
    profile,
    isLoading,
    isAuthenticated,
    login,
    changePassword,
    logout,
    fetchAccount,
  };
}
