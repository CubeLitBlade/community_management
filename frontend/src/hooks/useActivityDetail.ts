import { useCallback, useEffect, useMemo, useState } from 'react';
import apiClient, { refreshCsrfToken } from '../api/apiClient';
import type { ActivityView } from '../types/Activity';

export default function useActivityDetail(activityId: string | undefined) {
  const [activity, setActivity] = useState<ActivityView | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitErrorMessage, setSubmitErrorMessage] = useState('');

  const refresh = useCallback(async () => {
    if (!activityId) {
      setActivity(null);
      setErrorMessage('活动不存在。');
      setIsLoading(false);
      return;
    }

    setIsLoading(true);
    setErrorMessage('');
    try {
      const response = await apiClient.get<ActivityView>(`/activities/${activityId}`);
      setActivity(response.data);
    } catch {
      setActivity(null);
      setErrorMessage('加载活动详情失败，请稍后重试。');
    } finally {
      setIsLoading(false);
    }
  }, [activityId]);

  useEffect(() => {
    void refresh();
  }, [refresh]);

  const submitRegistration = useCallback(
    async (action: 'register' | 'cancel') => {
      if (!activityId) {
        return false;
      }

      setIsSubmitting(true);
      setSubmitErrorMessage('');

      try {
        await refreshCsrfToken();
        if (action === 'register') {
          await apiClient.post(`/activities/${activityId}/registrations`);
        } else {
          await apiClient.delete(`/activities/${activityId}/registrations/me`);
        }
        await refresh();
        return true;
      } catch {
        setSubmitErrorMessage(action === 'register' ? '报名失败，请稍后重试。' : '取消报名失败，请稍后重试。');
        return false;
      } finally {
        setIsSubmitting(false);
      }
    },
    [activityId, refresh],
  );

  return useMemo(
    () => ({
      activity,
      isLoading,
      errorMessage,
      isSubmitting,
      submitErrorMessage,
      refresh,
      register: () => submitRegistration('register'),
      cancelRegistration: () => submitRegistration('cancel'),
    }),
    [activity, isLoading, errorMessage, isSubmitting, submitErrorMessage, refresh, submitRegistration],
  );
}
