import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import apiClient, { refreshCsrfToken } from '../api/apiClient';
import type { ActivityView, CreateActivityRequest, RecentActivitiesResponse } from '../types/Activity';

const SEARCH_DEBOUNCE_MS = 300;
const PAGE_SIZE = 12;

function sortActivities(activities: ActivityView[]) {
  return [...activities].sort((left, right) => {
    return new Date(left.startTime).getTime() - new Date(right.startTime).getTime();
  });
}

function mergeActivities(current: ActivityView[], incoming: ActivityView[]) {
  const merged = new Map<number, ActivityView>();
  current.forEach((activity) => merged.set(activity.id, activity));
  incoming.forEach((activity) => merged.set(activity.id, activity));
  return sortActivities(Array.from(merged.values()));
}

export default function useActivityPlaza() {
  const [activities, setActivities] = useState<ActivityView[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isSearching, setIsSearching] = useState(false);
  const [isLoadingMore, setIsLoadingMore] = useState(false);
  const [hasMore, setHasMore] = useState(true);
  const [hasLoaded, setHasLoaded] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const [isCreating, setIsCreating] = useState(false);
  const [createErrorMessage, setCreateErrorMessage] = useState('');
  const [searchKeyword, setSearchKeyword] = useState('');
  const [activeSearchKeyword, setActiveSearchKeyword] = useState('');
  const requestSequenceRef = useRef(0);
  const nextLastIdRef = useRef<number | null>(null);
  const hasRequestedRef = useRef(false);

  const fetchActivities = useCallback(
    async (keyword: string, lastId: number | null, mode: 'replace' | 'append') => {
      const requestSequence = ++requestSequenceRef.current;

      if (mode === 'append') {
        setIsLoadingMore(true);
      } else if (hasRequestedRef.current) {
        setIsSearching(true);
      } else {
        setIsLoading(true);
      }

      setErrorMessage('');

      try {
        const response = await apiClient.get<RecentActivitiesResponse>('/activities', {
          params: {
            count: PAGE_SIZE,
            lastId,
            keyword: keyword || undefined,
          },
        });
        if (requestSequence !== requestSequenceRef.current) {
          return;
        }

        const nextActivities = response.data.items;
        const sortedIncoming = sortActivities(nextActivities);

        if (mode === 'replace') {
          setActivities(sortedIncoming);
        } else {
          setActivities((current) => mergeActivities(current, sortedIncoming));
        }

        if (nextActivities.length > 0) {
          nextLastIdRef.current = nextActivities.reduce(
            (minId, activity) => (activity.id < minId ? activity.id : minId),
            nextActivities[0].id,
          );
        }
        setHasMore(response.data.hasMore);
      } catch {
        if (requestSequence !== requestSequenceRef.current) {
          return;
        }
        if (mode === 'replace') {
          setActivities([]);
          nextLastIdRef.current = null;
        }
        setHasMore(false);
        setErrorMessage('加载活动失败，请稍后重试。');
      }

      if (requestSequence !== requestSequenceRef.current) {
        return;
      }
      setHasLoaded(true);
      setIsLoading(false);
      setIsSearching(false);
      setIsLoadingMore(false);
    },
    [],
  );

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
    nextLastIdRef.current = null;
    setHasMore(true);
    void fetchActivities(activeSearchKeyword, null, 'replace');
    hasRequestedRef.current = true;
  }, [activeSearchKeyword, fetchActivities]);

  const refresh = useCallback(async () => {
    nextLastIdRef.current = null;
    setHasMore(true);
    await fetchActivities(activeSearchKeyword, null, 'replace');
  }, [activeSearchKeyword, fetchActivities]);

  const loadMore = useCallback(async () => {
    if (!hasMore || isLoading || isSearching || isLoadingMore || errorMessage) {
      return;
    }

    await fetchActivities(activeSearchKeyword, nextLastIdRef.current, 'append');
  }, [activeSearchKeyword, errorMessage, fetchActivities, hasMore, isLoading, isLoadingMore, isSearching]);

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
      isLoadingMore,
      hasMore,
      hasLoaded,
      errorMessage,
      isCreating,
      createErrorMessage,
      searchKeyword,
      activeSearchKeyword,
      setSearchKeyword,
      refresh,
      loadMore,
      createActivity,
    }),
    [
      activities,
      isLoading,
      isSearching,
      isLoadingMore,
      hasMore,
      hasLoaded,
      errorMessage,
      isCreating,
      createErrorMessage,
      searchKeyword,
      activeSearchKeyword,
      refresh,
      loadMore,
      createActivity,
    ],
  );
}
