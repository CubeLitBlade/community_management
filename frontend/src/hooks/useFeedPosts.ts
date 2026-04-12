import { useCallback, useEffect, useMemo, useState } from 'react';
import apiClient from '../api/apiClient';
import type {
  EditPostRequest,
  PostRecord,
  PostView,
  PublishPostRequest,
  RecentPostsResponse,
} from '../types/Post';

const PAGE_SIZE = 12;

function normalizePost(post: PostView): PostView {
  return {
    ...post,
    title: post.title?.trim() ? post.title.trim() : null,
  };
}

export default function useFeedPosts() {
  const [posts, setPosts] = useState<PostView[]>([]);
  const [hasMore, setHasMore] = useState(true);
  const [isInitialLoading, setIsInitialLoading] = useState(true);
  const [isLoadingMore, setIsLoadingMore] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const [isPublishing, setIsPublishing] = useState(false);
  const [publishErrorMessage, setPublishErrorMessage] = useState('');
  const [updatingPostId, setUpdatingPostId] = useState<number | null>(null);
  const [editErrorMessage, setEditErrorMessage] = useState('');
  const [deletingPostId, setDeletingPostId] = useState<number | null>(null);
  const [deleteErrorMessage, setDeleteErrorMessage] = useState('');

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

  const deletePost = useCallback(async (postId: number) => {
    setDeletingPostId(postId);
    setDeleteErrorMessage('');

    try {
      const response = await apiClient.delete(`/posts/${postId}`);
      if (response.status !== 204) {
        throw new Error('Unexpected response status');
      }

      setPosts((current) => current.filter((post) => post.id !== postId));
      return true;
    } catch {
      setDeleteErrorMessage('删除失败，请稍后重试。');
      return false;
    } finally {
      setDeletingPostId((current) => (current === postId ? null : current));
    }
  }, []);

  const editPost = useCallback(async (postId: number, request: EditPostRequest) => {
    setUpdatingPostId(postId);
    setEditErrorMessage('');

    const normalizedTitle = request.title?.trim() ? request.title.trim() : null;
    const normalizedContent = request.content.trim();

    try {
      const response = await apiClient.patch<PostRecord>(`/posts/${postId}`, {
        title: normalizedTitle,
        content: normalizedContent,
      });

      if (response.status !== 200) {
        throw new Error('Unexpected response status');
      }

      const updatedPost = response.data;

      setPosts((current) =>
        current.map((post) => {
          if (post.id !== postId) {
            return post;
          }

          return normalizePost({
            ...post,
            title: updatedPost.title,
            content: updatedPost.content,
            updatedAt: updatedPost.updatedAt,
          });
        }),
      );

      return true;
    } catch {
      setEditErrorMessage('修改失败，请稍后重试。');
      return false;
    } finally {
      setUpdatingPostId((current) => (current === postId ? null : current));
    }
  }, []);

  return useMemo(
    () => ({
      posts,
      hasMore,
      isInitialLoading,
      isLoadingMore,
      errorMessage,
      publishErrorMessage,
      editErrorMessage,
      deleteErrorMessage,
      isPublishing,
      updatingPostId,
      deletingPostId,
      loadMore,
      refresh,
      publishPost,
      editPost,
      deletePost,
    }),
    [
      posts,
      hasMore,
      isInitialLoading,
      isLoadingMore,
      errorMessage,
      publishErrorMessage,
      editErrorMessage,
      deleteErrorMessage,
      isPublishing,
      updatingPostId,
      deletingPostId,
      loadMore,
      refresh,
      publishPost,
      editPost,
      deletePost,
    ],
  );
}
