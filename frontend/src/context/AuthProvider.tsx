import { useCallback, useEffect, useMemo, useState, type ReactNode } from 'react';
import apiClient from '../api/apiClient';
import { AuthContext, type AuthContextValue, type AuthStatus } from './AuthContext';

async function validateAccessToken(): Promise<boolean> {
  const accessToken = localStorage.getItem('accessToken');
  if (!accessToken) {
    return false;
  }

  try {
    // Uncomment after "auth/validate" complete
    // await apiClient.get('/auth/validate');
    return true;
  } catch {
    return false;
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [authStatus, setAuthStatus] = useState<AuthStatus>('checking');

  const refreshAuthStatus = useCallback(async (): Promise<AuthStatus> => {
    const isTokenValid = await validateAccessToken();
    const nextStatus: AuthStatus = isTokenValid ? 'authenticated' : 'unauthenticated';

    if (!isTokenValid) {
      localStorage.removeItem('accessToken');
    }

    setAuthStatus(nextStatus);
    return nextStatus;
  }, []);

  const markAuthenticated = useCallback(() => {
    setAuthStatus('authenticated');
  }, []);

  const markUnauthenticated = useCallback(() => {
    localStorage.removeItem('accessToken');
    setAuthStatus('unauthenticated');
  }, []);

  useEffect(() => {
    let active = true;

    const initializeAuthStatus = async () => {
      const isTokenValid = await validateAccessToken();
      if (!active) {
        return;
      }

      const nextStatus: AuthStatus = isTokenValid ? 'authenticated' : 'unauthenticated';
      if (!isTokenValid) {
        localStorage.removeItem('accessToken');
      }

      setAuthStatus(nextStatus);
    };

    void initializeAuthStatus();

    return () => {
      active = false;
    };
  }, []);

  const value = useMemo<AuthContextValue>(
    () => ({
      authStatus,
      isAuthenticated: authStatus === 'authenticated',
      refreshAuthStatus,
      markAuthenticated,
      markUnauthenticated,
    }),
    [authStatus, refreshAuthStatus, markAuthenticated, markUnauthenticated],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
