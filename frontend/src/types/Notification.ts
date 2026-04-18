export type NotificationType = 'post_comment' | 'post_reaction';

export const DEDICATED_NOTIFICATION_TYPES = [
  'system_announcement',
  'system_notice',
  'activity_reminder',
  'activity_update',
  'task_reminder',
] as const;

export type DedicatedNotificationType = (typeof DEDICATED_NOTIFICATION_TYPES)[number];

export type NotificationView = {
  id: number;
  recipientAccountId: number;
  actorAccountId: number;
  actorDisplayName: string | null;
  type: NotificationType | string;
  targetType: string;
  targetId: number;
  content: string;
  postTitle: string | null;
  postSummary: string | null;
  activityTitle: string | null;
  activitySummary: string | null;
  isRead: boolean;
  readAt: string | null;
  createdAt: string;
};

export type NotificationListResponse = {
  notifications: NotificationView[];
};

export type NotificationUnreadCountResponse = {
  count: number;
};
