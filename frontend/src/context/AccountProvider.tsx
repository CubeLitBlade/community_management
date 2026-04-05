import { useCallback, useEffect, useMemo, useState, type ReactNode } from 'react';
import type { Profile } from '../types/Account';
import apiClient from '../api/apiClient';
import { AccountContext, type AccountContextValue } from './AccountContext';

export const AccountProvider = ({ children }: { children: ReactNode }) => {
  const [profile, setProfile] = useState<Profile | null>(null);
  const [isLoading, setLoading] = useState(true);

  const logout = useCallback(() => {
    localStorage.removeItem('accessToken');
    setProfile(null);
  }, []);

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
        logout();
      } finally {
        setLoading(false);
      }
    }
  }, [logout]);

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
