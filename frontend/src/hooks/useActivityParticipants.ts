import { useCallback, useMemo, useState } from 'react';
import apiClient from '../api/apiClient';
import { BizError } from '../types/Error';
import type { ActivityParticipantView, ActivityParticipantListResponse } from '../types/Activity';

export default function useActivityParticipants(activityId: string | undefined) {
  const [participants, setParticipants] = useState<ActivityParticipantView[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  const refresh = useCallback(async () => {
    if (!activityId) {
      setParticipants([]);
      setErrorMessage('活动不存在。');
      return false;
    }

    setIsLoading(true);
    setErrorMessage('');
    try {
      const response = await apiClient.get<ActivityParticipantListResponse>(
        `/activities/${activityId}/participants`,
      );
      setParticipants(response.data.participants);
      return true;
    } catch (error) {
      setParticipants([]);
      if (error instanceof BizError) {
        setErrorMessage(error.detail.detail || '加载报名成员失败，请稍后重试。');
      } else {
        setErrorMessage('加载报名成员失败，请稍后重试。');
      }
      return false;
    } finally {
      setIsLoading(false);
    }
  }, [activityId]);

  return useMemo(
    () => ({
      participants,
      isLoading,
      errorMessage,
      refresh,
    }),
    [participants, isLoading, errorMessage, refresh],
  );
}
