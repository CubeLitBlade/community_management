import { useCallback, useMemo, useState } from 'react';
import apiClient, { refreshCsrfToken } from '../api/apiClient';
import type { CreateActivityRequest } from '../types/Activity';

export default function useCreateActivity() {
  const [isCreating, setIsCreating] = useState(false);
  const [createErrorMessage, setCreateErrorMessage] = useState('');

  const createActivity = useCallback(async (request: CreateActivityRequest) => {
    setIsCreating(true);
    setCreateErrorMessage('');
    try {
      await refreshCsrfToken();
      const response = await apiClient.post('/activities', request);
      const location = response.headers.location as string | undefined;
      return location ?? null;
    } catch {
      setCreateErrorMessage('创建活动失败，请稍后重试。');
      return null;
    } finally {
      setIsCreating(false);
    }
  }, []);

  return useMemo(
    () => ({
      isCreating,
      createErrorMessage,
      createActivity,
    }),
    [isCreating, createErrorMessage, createActivity],
  );
}
