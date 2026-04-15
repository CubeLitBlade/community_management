import { useCallback, useEffect, useMemo, useState, type ReactNode } from 'react';
import type { Profile } from '../types/Account';
import apiClient, { AUTH_UNAUTHORIZED_EVENT, refreshCsrfToken } from '../api/apiClient';
import { AccountContext, type AccountContextValue } from './AccountContext';

export const AccountProvider = ({ children }: { children: ReactNode }) => {
  const [profile, setProfile] = useState<Profile | null>(null);
  const [isLoading, setLoading] = useState(true);

  const clearAccountSession = useCallback(() => {
    setProfile(null);
  }, []);

  const logout = useCallback(async () => {
    try {
      await refreshCsrfToken();
      await apiClient.post('/auth/logout');
    } catch {
      // Always clear local session even if remote logout fails.
    } finally {
      clearAccountSession();
      window.location.reload();
    }
  }, [clearAccountSession]);

  const fetchAccount = useCallback(async () => {
    setLoading(true);
    try {
      const response = await apiClient.get<Profile>('/account/me');
      setProfile(response.data);
    } catch {
      clearAccountSession();
    } finally {
      setLoading(false);
    }
  }, [clearAccountSession]);

  useEffect(() => {
    const bootstrapAccount = async () => {
      try {
        await refreshCsrfToken();
      } catch {
        // The app can still function for read-only flows even if CSRF bootstrap fails temporarily.
      }

      await fetchAccount();
    };

    void bootstrapAccount();
  }, [fetchAccount]);

  useEffect(() => {
    const handleUnauthorized = () => {
      clearAccountSession();
    };

    window.addEventListener(AUTH_UNAUTHORIZED_EVENT, handleUnauthorized);
    return () => {
      window.removeEventListener(AUTH_UNAUTHORIZED_EVENT, handleUnauthorized);
    };
  }, [clearAccountSession]);

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
