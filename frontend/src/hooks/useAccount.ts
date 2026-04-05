import { useContext } from 'react';
import { AccountContext } from '../context/AccountContext';

export default function useAccount() {
  const context = useContext(AccountContext);
  if (!context) {
    throw new Error('"useAccount" must be used within "AccountProvider"');
  }

  return context;
}
