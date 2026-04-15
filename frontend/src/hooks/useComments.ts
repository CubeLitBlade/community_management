import { useCallback, useState } from 'react';
import apiClient from '../api/apiClient';
import type { CommentListResponse, CommentView, CreateCommentRequest } from '../types/Comment';

type CommentsByPostId = Record<number, CommentView[]>;
type RepliesByCommentId = Record<number, CommentView[]>;
type LoadingFlags = Record<number, boolean>;

function normalizeComments(items: CommentView[]) {
  return items.map((item) => ({
    ...item,
    authorNickname: item.authorNickname?.trim() || null,
    authorUsername: item.authorUsername?.trim() || null,
    replyToUsername: item.replyToUsername?.trim() || null,
    replyToNickname: item.replyToNickname?.trim() || null,
  }));
}

export default function useComments() {
  const [commentsByPostId, setCommentsByPostId] = useState<CommentsByPostId>({});
  const [repliesByCommentId, setRepliesByCommentId] = useState<RepliesByCommentId>({});
  const [loadingPosts, setLoadingPosts] = useState<LoadingFlags>({});
  const [loadingReplies, setLoadingReplies] = useState<LoadingFlags>({});
  const [creatingTargetKey, setCreatingTargetKey] = useState<string | null>(null);
  const [errorMessage, setErrorMessage] = useState('');

  const loadComments = useCallback(async (postId: number) => {
    setLoadingPosts((current) => ({ ...current, [postId]: true }));
    setErrorMessage('');

    try {
      const response = await apiClient.get<CommentListResponse>('/comments', {
        params: {
          targetType: 'post',
          targetId: postId,
        },
      });

      setCommentsByPostId((current) => ({
        ...current,
        [postId]: normalizeComments(response.data.items),
      }));
    } catch {
      setErrorMessage('加载评论失败，请稍后重试。');
    } finally {
      setLoadingPosts((current) => ({ ...current, [postId]: false }));
    }
  }, []);

  const loadReplies = useCallback(async (commentId: number) => {
    setLoadingReplies((current) => ({ ...current, [commentId]: true }));
    setErrorMessage('');

    try {
      const response = await apiClient.get<CommentListResponse>('/comments', {
        params: {
          targetType: 'comment',
          targetId: commentId,
        },
      });

      setRepliesByCommentId((current) => ({
        ...current,
        [commentId]: normalizeComments(response.data.items),
      }));
    } catch {
      setErrorMessage('加载回复失败，请稍后重试。');
    } finally {
      setLoadingReplies((current) => ({ ...current, [commentId]: false }));
    }
  }, []);

  const createComment = useCallback(
    async (request: CreateCommentRequest) => {
      setCreatingTargetKey(`${request.targetType}:${request.targetId}`);
      setErrorMessage('');

      try {
        const response = await apiClient.post('/comments', {
          targetType: request.targetType,
          targetId: request.targetId,
          parentId: request.parentId,
          content: request.content.trim(),
        });

        if (response.status !== 201) {
          throw new Error('Unexpected response status');
        }

        if (request.targetType === 'post') {
          await loadComments(request.targetId);
        } else {
          await loadReplies(request.targetId);
        }

        return true;
      } catch {
        setErrorMessage('发表评论失败，请稍后重试。');
        return false;
      } finally {
        setCreatingTargetKey(null);
      }
    },
    [loadComments, loadReplies],
  );

  return {
    commentsByPostId,
    repliesByCommentId,
    loadingPosts,
    loadingReplies,
    creatingTargetKey,
    errorMessage,
    loadComments,
    loadReplies,
    createComment,
  };
}
