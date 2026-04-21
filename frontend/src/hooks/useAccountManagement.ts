import { useCallback, useEffect, useMemo, useState } from 'react';
import apiClient, { refreshCsrfToken } from '../api/apiClient';
import { BizError } from '../types/Error';
import type {
  ManagedAccount,
  ManagedAccountListResponse,
  ResetManagedAccountPasswordRequest,
} from '../types/Account';

function replaceAccount(
  accounts: ManagedAccount[],
  nextAccount: ManagedAccount,
): ManagedAccount[] {
  return accounts.map((account) => (account.id === nextAccount.id ? nextAccount : account));
}

function toActionErrorMessage(error: unknown, fallback: string) {
  if (!(error instanceof BizError)) {
    return fallback;
  }

  switch (error.detail.code) {
    case 'FORBIDDEN':
      return '你没有权限执行该操作。';
    case 'ACCOUNT_NOT_FOUND':
      return '目标账户不存在，列表可能已经过期。';
    case 'ACCOUNT_STATE_SUSPENDED':
      return '该账户已停用，当前无法继续操作。';
    case 'ACCOUNT_STATE_ARCHIVED':
      return '该账户已归档，当前无法继续操作。';
    case 'INPUT_PASSWORD_BLANK':
      return '新密码不能为空。';
    case 'INPUT_PASSWORD_BAD_LENGTH':
      return '新密码长度需为 6 到 20 位。';
    case 'INPUT_PASSWORD_BAD_FORMAT':
      return '新密码需同时包含字母和数字。';
    case 'INVALID_REQUEST':
      return error.detail.detail || fallback;
    default:
      return error.detail.detail || fallback;
  }
}

export default function useAccountManagement(enabled: boolean) {
  const [accounts, setAccounts] = useState<ManagedAccount[]>([]);
  const [isLoading, setIsLoading] = useState(enabled);
  const [errorMessage, setErrorMessage] = useState('');
  const [actioningId, setActioningId] = useState<number | null>(null);
  const [actionErrorMessage, setActionErrorMessage] = useState('');

  const refresh = useCallback(async () => {
    if (!enabled) {
      setAccounts([]);
      setIsLoading(false);
      setErrorMessage('');
      return;
    }

    setIsLoading(true);
    setErrorMessage('');
    try {
      const response = await apiClient.get<ManagedAccountListResponse>('/admin/accounts');
      setAccounts(response.data.accounts);
    } catch (error) {
      setAccounts([]);
      setErrorMessage(toActionErrorMessage(error, '加载用户列表失败，请稍后重试。'));
    } finally {
      setIsLoading(false);
    }
  }, [enabled]);

  useEffect(() => {
    void refresh();
  }, [refresh]);

  const resetPassword = useCallback(
    async (accountId: number, newPassword: string) => {
      setActioningId(accountId);
      setActionErrorMessage('');
      try {
        await refreshCsrfToken();
        const request: ResetManagedAccountPasswordRequest = { newPassword };
        const response = await apiClient.post<ManagedAccount>(
          `/admin/accounts/${accountId}/reset-password`,
          request,
        );
        setAccounts((current) => replaceAccount(current, response.data));
        return true;
      } catch (error) {
        setActionErrorMessage(toActionErrorMessage(error, '重设密码失败，请稍后重试。'));
        return false;
      } finally {
        setActioningId((current) => (current === accountId ? null : current));
      }
    },
    [],
  );

  const suspendAccount = useCallback(async (accountId: number) => {
    setActioningId(accountId);
    setActionErrorMessage('');
    try {
      await refreshCsrfToken();
      const response = await apiClient.post<ManagedAccount>(`/admin/accounts/${accountId}/suspend`);
      setAccounts((current) => replaceAccount(current, response.data));
      return true;
    } catch (error) {
      setActionErrorMessage(toActionErrorMessage(error, '停用账户失败，请稍后重试。'));
      return false;
    } finally {
      setActioningId((current) => (current === accountId ? null : current));
    }
  }, []);

  const promoteAccount = useCallback(async (accountId: number) => {
    setActioningId(accountId);
    setActionErrorMessage('');
    try {
      await refreshCsrfToken();
      const response = await apiClient.post<ManagedAccount>(`/admin/accounts/${accountId}/promote`);
      setAccounts((current) => replaceAccount(current, response.data));
      return true;
    } catch (error) {
      setActionErrorMessage(toActionErrorMessage(error, '提权失败，请稍后重试。'));
      return false;
    } finally {
      setActioningId((current) => (current === accountId ? null : current));
    }
  }, []);

  const reactivateAccount = useCallback(async (accountId: number) => {
    setActioningId(accountId);
    setActionErrorMessage('');
    try {
      await refreshCsrfToken();
      const response = await apiClient.post<ManagedAccount>(
        `/admin/accounts/${accountId}/reactivate`,
      );
      setAccounts((current) => replaceAccount(current, response.data));
      return true;
    } catch (error) {
      setActionErrorMessage(toActionErrorMessage(error, '启用账户失败，请稍后重试。'));
      return false;
    } finally {
      setActioningId((current) => (current === accountId ? null : current));
    }
  }, []);

  const archiveAccount = useCallback(async (accountId: number) => {
    setActioningId(accountId);
    setActionErrorMessage('');
    try {
      await refreshCsrfToken();
      const response = await apiClient.post<ManagedAccount>(`/admin/accounts/${accountId}/archive`);
      setAccounts((current) => replaceAccount(current, response.data));
      return true;
    } catch (error) {
      setActionErrorMessage(toActionErrorMessage(error, '归档账户失败，请稍后重试。'));
      return false;
    } finally {
      setActioningId((current) => (current === accountId ? null : current));
    }
  }, []);

  const demoteAccount = useCallback(async (accountId: number) => {
    setActioningId(accountId);
    setActionErrorMessage('');
    try {
      await refreshCsrfToken();
      const response = await apiClient.post<ManagedAccount>(`/admin/accounts/${accountId}/demote`);
      setAccounts((current) => replaceAccount(current, response.data));
      return true;
    } catch (error) {
      setActionErrorMessage(toActionErrorMessage(error, '降权失败，请稍后重试。'));
      return false;
    } finally {
      setActioningId((current) => (current === accountId ? null : current));
    }
  }, []);

  return useMemo(
    () => ({
      accounts,
      isLoading,
      errorMessage,
      actioningId,
      actionErrorMessage,
      refresh,
      resetPassword,
      suspendAccount,
      promoteAccount,
      reactivateAccount,
      archiveAccount,
      demoteAccount,
    }),
    [
      accounts,
      isLoading,
      errorMessage,
      actioningId,
      actionErrorMessage,
      refresh,
      resetPassword,
      suspendAccount,
      promoteAccount,
      reactivateAccount,
      archiveAccount,
      demoteAccount,
    ],
  );
}
