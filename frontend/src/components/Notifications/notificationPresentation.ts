import { DEDICATED_NOTIFICATION_TYPES, type NotificationView } from '../../types/Notification';

export type NotificationScope = 'all' | 'replies' | 'reactions' | 'notifications';
export type PageKind = 'replies' | 'likes' | 'notifications';

export type NotificationItem = {
  id: number;
  title: string;
  summary: string;
  contextSummary?: string | null;
  time: string;
  href?: string;
  unread?: boolean;
};

const notificationTimeFormatter = new Intl.DateTimeFormat('zh-CN', {
  month: '2-digit',
  day: '2-digit',
  hour: '2-digit',
  minute: '2-digit',
});

function formatNotificationTime(value: string) {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return '';
  }

  return notificationTimeFormatter.format(date);
}

function getReactionLabel(value: string | null) {
  switch (value) {
    case 'like':
      return '赞了这条内容';
    case 'love':
      return '喜欢了这条内容';
    case 'laugh':
      return '觉得这条内容很好笑';
    case 'sad':
      return '表达了难过';
    default:
      return '回应了这条内容';
  }
}

function getReactionVerb(value: string | null) {
  switch (value) {
    case 'like':
      return ' 👍赞了';
    case 'love':
      return ' ❤喜欢了';
    case 'laugh':
      return ' 😂笑着回应了';
    case 'sad':
      return ' 😟难过地回应了';
    default:
      return ' 互动了';
  }
}

export function toNotificationItem(notification: NotificationView): NotificationItem {
  const actor = notification.actorDisplayName?.trim() || '有人';
  const postContext = notification.postTitle || notification.postSummary || null;

  switch (notification.type) {
    case 'post_comment':
      return {
        id: notification.id,
        title: `${actor} 回复了你的帖子`,
        summary: notification.content ?? '查看回复详情',
        contextSummary: postContext,
        time: formatNotificationTime(notification.createdAt),
        href: notification.targetType === 'post' ? `/posts/${notification.targetId}` : undefined,
        unread: !notification.isRead,
      };
    case 'post_reaction':
      return {
        id: notification.id,
        title: `${actor}${getReactionVerb(notification.content)}你的帖子`,
        summary: getReactionLabel(notification.content),
        contextSummary: postContext,
        time: formatNotificationTime(notification.createdAt),
        href: notification.targetType === 'post' ? `/posts/${notification.targetId}` : undefined,
        unread: !notification.isRead,
      };
    case 'system_announcement':
      return {
        id: notification.id,
        title: '系统公告',
        summary: notification.content ?? '查看通知详情',
        time: formatNotificationTime(notification.createdAt),
        unread: !notification.isRead,
      };
    case 'system_notice':
      return {
        id: notification.id,
        title: '系统通知',
        summary: notification.content ?? '查看通知详情',
        time: formatNotificationTime(notification.createdAt),
        unread: !notification.isRead,
      };
    case 'activity_reminder':
      return {
        id: notification.id,
        title: '活动提醒',
        summary: notification.content ?? '查看活动提醒',
        time: formatNotificationTime(notification.createdAt),
        unread: !notification.isRead,
      };
    case 'activity_update':
      return {
        id: notification.id,
        title: '活动更新',
        summary: notification.content ?? '查看活动更新',
        time: formatNotificationTime(notification.createdAt),
        unread: !notification.isRead,
      };
    case 'task_reminder':
      return {
        id: notification.id,
        title: '任务提醒',
        summary: notification.content ?? '查看任务提醒',
        time: formatNotificationTime(notification.createdAt),
        unread: !notification.isRead,
      };
    default:
      return {
        id: notification.id,
        title: '新通知',
        summary: notification.content ?? '查看通知详情',
        time: formatNotificationTime(notification.createdAt),
        unread: !notification.isRead,
      };
  }
}

export function filterNotifications(kind: PageKind, notifications: NotificationView[]) {
  switch (kind) {
    case 'replies':
      return notifications.filter((item) => item.type === 'post_comment');
    case 'likes':
      return notifications.filter((item) => item.type === 'post_reaction');
    case 'notifications':
      return notifications.filter((item) =>
        DEDICATED_NOTIFICATION_TYPES.includes(
          item.type as (typeof DEDICATED_NOTIFICATION_TYPES)[number],
        ),
      );
  }
}
