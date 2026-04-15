import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
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
    viewerReaction: post.viewerReaction ?? null,
  };
}

function mergeViewerReactions(posts: PostView[], overrides: Map<number, string | null>) {
  return posts.map((post) => {
    const hasOverride = overrides.has(post.id);

    return normalizePost({
      ...post,
      viewerReaction: hasOverride ? (overrides.get(post.id) ?? null) : post.viewerReaction ?? null,
    });
  });
}

function applyOptimisticReaction(
  posts: PostView[],
  postId: number,
  nextReactionType: string | null,
) {
  return posts.map((post) => {
    if (post.id !== postId) {
      return post;
    }

    const currentReactionType = post.viewerReaction ?? null;
    const reactionCountByType = new Map(
      (post.reactions ?? []).map((reaction) => [reaction.reactionType, reaction.count] as const),
    );

    if (currentReactionType) {
      const currentCount = reactionCountByType.get(currentReactionType) ?? 0;
      if (currentCount <= 1) {
        reactionCountByType.delete(currentReactionType);
      } else {
        reactionCountByType.set(currentReactionType, currentCount - 1);
      }
    }

    if (nextReactionType) {
      reactionCountByType.set(nextReactionType, (reactionCountByType.get(nextReactionType) ?? 0) + 1);
    }

    return {
      ...post,
      viewerReaction: nextReactionType,
      reactions: Array.from(reactionCountByType.entries()).map(([reactionType, count]) => ({
        reactionType,
        count,
      })),
    };
  });
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
  const [reactionErrorMessage, setReactionErrorMessage] = useState('');
  const reactionRequestVersionRef = useRef(new Map<number, number>());
  const sessionReactionOverridesRef = useRef(new Map<number, string | null>());

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

        const nextPosts = mergeViewerReactions(
          response.data.items,
          sessionReactionOverridesRef.current,
        );

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

  const setReaction = useCallback(
    async (postId: number, reactionType: string | null) => {
      setReactionErrorMessage('');
      let previousPosts: PostView[] = [];
      let previousViewerReaction: string | null = null;
      const currentVersion = (reactionRequestVersionRef.current.get(postId) ?? 0) + 1;
      reactionRequestVersionRef.current.set(postId, currentVersion);

      setPosts((current) => {
        previousPosts = current;
        previousViewerReaction =
          current.find((post) => post.id === postId)?.viewerReaction ?? null;
        sessionReactionOverridesRef.current.set(postId, reactionType);
        return applyOptimisticReaction(current, postId, reactionType);
      });

      try {
        const response = await apiClient.post('/reactions', {
          targetType: 'post',
          targetId: postId,
          reactionType,
        });

        if (response.status !== 201 && response.status !== 204) {
          throw new Error('Unexpected response status');
        }

        return true;
      } catch {
        if (reactionRequestVersionRef.current.get(postId) !== currentVersion) {
          return false;
        }

        if (previousViewerReaction === null) {
          sessionReactionOverridesRef.current.delete(postId);
        } else {
          sessionReactionOverridesRef.current.set(postId, previousViewerReaction);
        }
        setPosts(previousPosts);
        setReactionErrorMessage('互动失败，请稍后重试。');
        return false;
      }
    },
    [],
  );

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
      reactionErrorMessage,
      isPublishing,
      updatingPostId,
      deletingPostId,
      loadMore,
      refresh,
      publishPost,
      editPost,
      deletePost,
      setReaction,
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
      reactionErrorMessage,
      isPublishing,
      updatingPostId,
      deletingPostId,
      loadMore,
      refresh,
      publishPost,
      editPost,
      deletePost,
      setReaction,
    ],
  );
}
