import { useCallback, useEffect, useMemo, useState } from 'react';
import apiClient from '../api/apiClient';
import type {
  NotificationListResponse,
  NotificationUnreadCountResponse,
  NotificationView,
} from '../types/Notification';

type NotificationScope = 'all' | 'replies' | 'reactions' | 'notifications';

function sortNotifications(items: NotificationView[]) {
  return [...items].sort((left, right) => {
    return new Date(right.createdAt).getTime() - new Date(left.createdAt).getTime();
  });
}

export default function useNotifications(enabled: boolean, scope: NotificationScope) {
  const [notifications, setNotifications] = useState<NotificationView[]>([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [isLoading, setIsLoading] = useState(false);
  const [markingIds, setMarkingIds] = useState<Set<number>>(new Set());
  const [errorMessage, setErrorMessage] = useState('');

  const fetchNotifications = useCallback(async () => {
    if (!enabled) {
      setNotifications([]);
      setUnreadCount(0);
      setErrorMessage('');
      setIsLoading(false);
      return;
    }

    setIsLoading(true);
    setErrorMessage('');

    try {
      const [notificationsResponse, unreadCountResponse] = await Promise.all([
        apiClient.get<NotificationListResponse>('/notifications', {
          params: { scope },
        }),
        apiClient.get<NotificationUnreadCountResponse>('/notifications/unread-count', {
          params: { scope },
        }),
      ]);

      setNotifications(sortNotifications(notificationsResponse.data.notifications));
      setUnreadCount(unreadCountResponse.data.count);
    } catch {
      setNotifications([]);
      setUnreadCount(0);
      setErrorMessage('加载通知失败，请稍后重试。');
    } finally {
      setIsLoading(false);
    }
  }, [enabled, scope]);

  useEffect(() => {
    void fetchNotifications();
  }, [fetchNotifications]);

  const replyNotifications = useMemo(
    () => notifications.filter((item) => item.type === 'post_comment'),
    [notifications],
  );
  const reactionNotifications = useMemo(
    () => notifications.filter((item) => item.type === 'post_reaction'),
    [notifications],
  );

  const markAsRead = useCallback(
    async (notificationId: number) => {
      if (!enabled) {
        return false;
      }

      const target = notifications.find((item) => item.id === notificationId);
      if (!target || target.isRead) {
        return true;
      }

      setMarkingIds((current) => new Set(current).add(notificationId));

      try {
        await apiClient.post(`/notifications/${notificationId}/read`);
        setNotifications((current) =>
          current.map((item) =>
            item.id === notificationId
              ? { ...item, isRead: true, readAt: new Date().toISOString() }
              : item,
          ),
        );
        setUnreadCount((current) => Math.max(0, current - 1));
        return true;
      } catch {
        setErrorMessage('更新通知状态失败，请稍后重试。');
        return false;
      } finally {
        setMarkingIds((current) => {
          const next = new Set(current);
          next.delete(notificationId);
          return next;
        });
      }
    },
    [enabled, notifications],
  );

  return {
    notifications,
    replyNotifications,
    reactionNotifications,
    unreadCount,
    isLoading,
    markingIds,
    errorMessage,
    refresh: fetchNotifications,
    markAsRead,
  };
}
