import { createContext } from 'react';

export type AuthStatus = 'checking' | 'authenticated' | 'unauthenticated';

export type AuthContextValue = {
  authStatus: AuthStatus;
  isAuthenticated: boolean;
  refreshAuthStatus: () => Promise<AuthStatus>;
  markAuthenticated: () => void;
  markUnauthenticated: () => void;
};

export const AuthContext = createContext<AuthContextValue | undefined>(undefined);
