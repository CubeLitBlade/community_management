import { useCallback, useEffect, useMemo, useState, type ReactNode } from 'react';
import type { Profile } from '../types/Account';
import apiClient from '../api/apiClient';
import { AccountContext, type AccountContextValue } from './AccountContext';

export const AccountProvider = ({ children }: { children: ReactNode }) => {
  const [profile, setProfile] = useState<Profile | null>(null);
  const [isLoading, setLoading] = useState(true);

  const clearAccountSession = useCallback(() => {
    localStorage.removeItem('accessToken');
    setProfile(null);
  }, []);

  const logout = useCallback(async () => {
    try {
      await apiClient.post('/auth/logout');
    } catch {
      // Always clear local session even if remote logout fails.
    } finally {
      clearAccountSession();
    }
  }, [clearAccountSession]);

  const fetchAccount = useCallback(async () => {
    const accessToken = localStorage.getItem('accessToken');

    if (!accessToken) {
      setProfile(null);
      setLoading(false);
    } else {
      setLoading(true);
      try {
        const response = await apiClient.get<Profile>('/account/me');
        setProfile(response.data);
      } catch {
        clearAccountSession();
      } finally {
        setLoading(false);
      }
    }
  }, [clearAccountSession]);

  useEffect(() => {
    void fetchAccount();
  }, [fetchAccount]);

  const value = useMemo<AccountContextValue>(
    () => ({
      profile,
      isLoading,
      fetchAccount,
      logout,
    }),
    [profile, isLoading, fetchAccount, logout],
  );

  return <AccountContext.Provider value={value}>{children}</AccountContext.Provider>;
};
