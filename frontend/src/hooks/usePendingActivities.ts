import { useCallback, useEffect, useMemo, useState } from 'react';
import apiClient, { refreshCsrfToken } from '../api/apiClient';
import { BizError } from '../types/Error';
import type { ActivityListResponse, ActivityView } from '../types/Activity';

function sortPending(activities: ActivityView[]) {
  return [...activities].sort((left, right) => {
    return new Date(left.createdAt).getTime() - new Date(right.createdAt).getTime();
  });
}

export default function usePendingActivities(enabled: boolean) {
  const [activities, setActivities] = useState<ActivityView[]>([]);
  const [isLoading, setIsLoading] = useState(enabled);
  const [errorMessage, setErrorMessage] = useState('');
  const [actioningId, setActioningId] = useState<number | null>(null);
  const [actionErrorMessage, setActionErrorMessage] = useState('');

  const refresh = useCallback(async () => {
    if (!enabled) {
      setActivities([]);
      setIsLoading(false);
      setErrorMessage('');
      return;
    }

    setIsLoading(true);
    setErrorMessage('');
    try {
      const response = await apiClient.get<ActivityListResponse>('/admin/activities/pending');
      setActivities(sortPending(response.data.activities));
    } catch {
      setActivities([]);
      setErrorMessage('加载待审核活动失败，请稍后重试。');
    } finally {
      setIsLoading(false);
    }
  }, [enabled]);

  useEffect(() => {
    void refresh();
  }, [refresh]);

  const approve = useCallback(
    async (activityId: number) => {
      setActioningId(activityId);
      setActionErrorMessage('');
      try {
        await refreshCsrfToken();
        await apiClient.post(`/admin/activities/${activityId}/approve`, {});
        await refresh();
        return true;
      } catch (error) {
        if (error instanceof BizError) {
          setActionErrorMessage(error.detail.detail || '审核通过失败，请稍后重试。');
        } else {
          setActionErrorMessage('审核通过失败，请稍后重试。');
        }
        return false;
      } finally {
        setActioningId((current) => (current === activityId ? null : current));
      }
    },
    [refresh],
  );

  const reject = useCallback(
    async (activityId: number, reason: string) => {
      setActioningId(activityId);
      setActionErrorMessage('');
      try {
        await refreshCsrfToken();
        await apiClient.post(`/admin/activities/${activityId}/reject`, { reason });
        await refresh();
        return true;
      } catch (error) {
        if (error instanceof BizError) {
          setActionErrorMessage(error.detail.detail || '驳回失败，请稍后重试。');
        } else {
          setActionErrorMessage('驳回失败，请稍后重试。');
        }
        return false;
      } finally {
        setActioningId((current) => (current === activityId ? null : current));
      }
    },
    [refresh],
  );

  return useMemo(
    () => ({
      activities,
      isLoading,
      errorMessage,
      actioningId,
      actionErrorMessage,
      refresh,
      approve,
      reject,
    }),
    [activities, isLoading, errorMessage, actioningId, actionErrorMessage, refresh, approve, reject],
  );
}
