import { useCallback, useEffect, useMemo, useState } from 'react';
import apiClient from '../api/apiClient';
import type { Post, PublishPostRequest, RecentPostsResponse } from '../types/Post';

const PAGE_SIZE = 12;

function normalizePost(post: Post): Post {
  return {
    ...post,
    title: post.title?.trim() ? post.title.trim() : null,
  };
}

export default function useFeedPosts() {
  const [posts, setPosts] = useState<Post[]>([]);
  const [hasMore, setHasMore] = useState(true);
  const [isInitialLoading, setIsInitialLoading] = useState(true);
  const [isLoadingMore, setIsLoadingMore] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const [isPublishing, setIsPublishing] = useState(false);
  const [publishErrorMessage, setPublishErrorMessage] = useState('');

  const fetchRecentPosts = useCallback(
    async (lastId: number | null, mode: 'replace' | 'append') => {
      if (mode === 'append') {
        setIsLoadingMore(true);
      } else {
        setIsInitialLoading(true);
      }

      setErrorMessage('');

      try {
        const response = await apiClient.get<RecentPostsResponse>('/posts/recent', {
          params: {
            count: PAGE_SIZE,
            lastId,
          },
        });

        const nextPosts = response.data.items.map(normalizePost);

        setPosts((current) => (mode === 'replace' ? nextPosts : [...current, ...nextPosts]));
        setHasMore(response.data.hasMore);
      } catch {
        setErrorMessage('加载帖子失败，请稍后重试。');
        setHasMore(false);
        if (mode === 'replace') {
          setPosts([]);
        }
      } finally {
        setIsInitialLoading(false);
        setIsLoadingMore(false);
      }
    },
    [],
  );

  useEffect(() => {
    void fetchRecentPosts(null, 'replace');
  }, [fetchRecentPosts]);

  const loadMore = useCallback(async () => {
    if (!hasMore || isInitialLoading || isLoadingMore || errorMessage) {
      return;
    }

    const lastPost = posts[posts.length - 1];
    await fetchRecentPosts(lastPost ? lastPost.id : null, 'append');
  }, [fetchRecentPosts, hasMore, isInitialLoading, isLoadingMore, errorMessage, posts]);

  const refresh = useCallback(async () => {
    await fetchRecentPosts(null, 'replace');
  }, [fetchRecentPosts]);

  const publishPost = useCallback(
    async (request: PublishPostRequest) => {
      setIsPublishing(true);
      setPublishErrorMessage('');

      const normalizedTitle = request.title?.trim() ? request.title.trim() : null;

      try {
        await apiClient.post('/posts', {
          title: normalizedTitle,
          content: request.content.trim(),
        });

        await refresh();
        return true;
      } catch {
        setPublishErrorMessage('发布失败，请稍后重试。');
        return false;
      } finally {
        setIsPublishing(false);
      }
    },
    [refresh],
  );

  return useMemo(
    () => ({
      posts,
      hasMore,
      isInitialLoading,
      isLoadingMore,
      errorMessage,
      publishErrorMessage,
      isPublishing,
      loadMore,
      refresh,
      publishPost,
    }),
    [
      posts,
      hasMore,
      isInitialLoading,
      isLoadingMore,
      errorMessage,
      publishErrorMessage,
      isPublishing,
      loadMore,
      refresh,
      publishPost,
    ],
  );
}
