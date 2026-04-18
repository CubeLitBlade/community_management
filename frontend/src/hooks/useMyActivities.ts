import { useCallback, useEffect, useMemo, useState } from 'react';
import apiClient from '../api/apiClient';
import type { ActivityView, MyActivitiesResponse } from '../types/Activity';

function sortByTime(activities: ActivityView[], key: 'createdAt' | 'startTime') {
  return [...activities].sort((left, right) => {
    return new Date(right[key]).getTime() - new Date(left[key]).getTime();
  });
}

export default function useMyActivities(enabled: boolean) {
  const [created, setCreated] = useState<ActivityView[]>([]);
  const [registered, setRegistered] = useState<ActivityView[]>([]);
  const [isLoading, setIsLoading] = useState(enabled);
  const [errorMessage, setErrorMessage] = useState('');

  const refresh = useCallback(async () => {
    if (!enabled) {
      setCreated([]);
      setRegistered([]);
      setErrorMessage('');
      setIsLoading(false);
      return;
    }

    setIsLoading(true);
    setErrorMessage('');
    try {
      const response = await apiClient.get<MyActivitiesResponse>('/activities/mine');
      setCreated(sortByTime(response.data.created, 'createdAt'));
      setRegistered(sortByTime(response.data.registered, 'startTime'));
    } catch {
      setCreated([]);
      setRegistered([]);
      setErrorMessage('加载我的活动失败，请稍后重试。');
    } finally {
      setIsLoading(false);
    }
  }, [enabled]);

  useEffect(() => {
    void refresh();
  }, [refresh]);

  return useMemo(
    () => ({
      created,
      registered,
      isLoading,
      errorMessage,
      refresh,
    }),
    [created, registered, isLoading, errorMessage, refresh],
  );
}
