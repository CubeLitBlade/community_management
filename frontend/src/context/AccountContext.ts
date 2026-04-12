import { createContext } from 'react';
import type { Profile } from '../types/Account';

export type AccountContextValue = {
  profile: Profile | null;
  isLoading: boolean;
  fetchAccount: () => Promise<void>;
  logout: () => Promise<void>;
};

export const AccountContext = createContext<AccountContextValue | undefined>(undefined);
