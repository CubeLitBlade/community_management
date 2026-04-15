import { useCallback, useEffect, useState } from 'react';
import apiClient from '../api/apiClient';
import type { PostView } from '../types/Post';

export default function usePostDetail(postId: number | null) {
  const [post, setPost] = useState<PostView | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  const fetchPost = useCallback(async () => {
    if (postId === null || Number.isNaN(postId)) {
      setPost(null);
      setErrorMessage('帖子不存在。');
      return;
    }

    setIsLoading(true);
    setErrorMessage('');

    try {
      const response = await apiClient.get<PostView>(`/posts/${postId}`);
      setPost(response.data);
    } catch {
      setPost(null);
      setErrorMessage('加载帖子详情失败，请稍后重试。');
    } finally {
      setIsLoading(false);
    }
  }, [postId]);

  useEffect(() => {
    void fetchPost();
  }, [fetchPost]);

  return {
    post,
    isLoading,
    errorMessage,
    refresh: fetchPost,
    setPost,
  };
}
