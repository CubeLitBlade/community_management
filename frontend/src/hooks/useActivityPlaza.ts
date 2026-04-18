import { useCallback, useEffect, useMemo, useState } from 'react';
import apiClient, { refreshCsrfToken } from '../api/apiClient';
import type { ActivityListResponse, ActivityView, CreateActivityRequest } from '../types/Activity';

function sortActivities(activities: ActivityView[]) {
  return [...activities].sort((left, right) => {
    return new Date(left.startTime).getTime() - new Date(right.startTime).getTime();
  });
}

export default function useActivityPlaza() {
  const [activities, setActivities] = useState<ActivityView[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState('');
  const [isCreating, setIsCreating] = useState(false);
  const [createErrorMessage, setCreateErrorMessage] = useState('');

  const refresh = useCallback(async () => {
    setIsLoading(true);
    setErrorMessage('');
    try {
      const response = await apiClient.get<ActivityListResponse>('/activities');
      setActivities(sortActivities(response.data.activities));
    } catch {
      setActivities([]);
      setErrorMessage('加载活动失败，请稍后重试。');
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    void refresh();
  }, [refresh]);

  const createActivity = useCallback(
    async (request: CreateActivityRequest) => {
      setIsCreating(true);
      setCreateErrorMessage('');
      try {
        await refreshCsrfToken();
        const response = await apiClient.post('/activities', request);
        const location = response.headers.location as string | undefined;
        await refresh();
        return location ?? null;
      } catch {
        setCreateErrorMessage('创建活动失败，请稍后重试。');
        return null;
      } finally {
        setIsCreating(false);
      }
    },
    [refresh],
  );

  return useMemo(
    () => ({
      activities,
      isLoading,
      errorMessage,
      isCreating,
      createErrorMessage,
      refresh,
      createActivity,
    }),
    [activities, isLoading, errorMessage, isCreating, createErrorMessage, refresh, createActivity],
  );
}
