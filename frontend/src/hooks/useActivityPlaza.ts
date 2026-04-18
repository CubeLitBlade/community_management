import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import apiClient, { refreshCsrfToken } from '../api/apiClient';
import type { ActivityListResponse, ActivityView, CreateActivityRequest } from '../types/Activity';

const SEARCH_DEBOUNCE_MS = 300;

function sortActivities(activities: ActivityView[]) {
  return [...activities].sort((left, right) => {
    return new Date(left.startTime).getTime() - new Date(right.startTime).getTime();
  });
}

export default function useActivityPlaza() {
  const [activities, setActivities] = useState<ActivityView[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isSearching, setIsSearching] = useState(false);
  const [hasLoaded, setHasLoaded] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const [isCreating, setIsCreating] = useState(false);
  const [createErrorMessage, setCreateErrorMessage] = useState('');
  const [searchKeyword, setSearchKeyword] = useState('');
  const [activeSearchKeyword, setActiveSearchKeyword] = useState('');
  const requestSequenceRef = useRef(0);
  const hasRequestedRef = useRef(false);

  const fetchActivities = useCallback(async (keyword: string, preserveActivities: boolean) => {
    const requestSequence = ++requestSequenceRef.current;
    if (preserveActivities) {
      setIsSearching(true);
    } else {
      setIsLoading(true);
    }
    setErrorMessage('');

    try {
      const response = await apiClient.get<ActivityListResponse>('/activities', {
        params: keyword ? { keyword } : undefined,
      });
      if (requestSequence !== requestSequenceRef.current) {
        return;
      }
      setActivities(sortActivities(response.data.activities));
    } catch {
      if (requestSequence !== requestSequenceRef.current) {
        return;
      }
      if (!preserveActivities) {
        setActivities([]);
      }
      setErrorMessage('加载活动失败，请稍后重试。');
    }

    if (requestSequence !== requestSequenceRef.current) {
      return;
    }
    setHasLoaded(true);
    setIsLoading(false);
    setIsSearching(false);
  }, []);

  useEffect(() => {
    const normalizedKeyword = searchKeyword.trim();
    const timeoutId = window.setTimeout(() => {
      setActiveSearchKeyword((current) =>
        current === normalizedKeyword ? current : normalizedKeyword,
      );
    }, SEARCH_DEBOUNCE_MS);

    return () => window.clearTimeout(timeoutId);
  }, [searchKeyword]);

  useEffect(() => {
    void fetchActivities(activeSearchKeyword, hasRequestedRef.current);
    hasRequestedRef.current = true;
  }, [activeSearchKeyword, fetchActivities]);

  const refresh = useCallback(async () => {
    await fetchActivities(activeSearchKeyword, hasRequestedRef.current);
  }, [activeSearchKeyword, fetchActivities]);

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
      isSearching,
      hasLoaded,
      errorMessage,
      isCreating,
      createErrorMessage,
      searchKeyword,
      activeSearchKeyword,
      setSearchKeyword,
      refresh,
      createActivity,
    }),
    [
      activities,
      isLoading,
      isSearching,
      hasLoaded,
      errorMessage,
      isCreating,
      createErrorMessage,
      searchKeyword,
      activeSearchKeyword,
      refresh,
      createActivity,
    ],
  );
}
