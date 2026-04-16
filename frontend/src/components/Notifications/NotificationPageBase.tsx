import {
  Badge,
  Body1,
  Body1Strong,
  Button,
  Card,
  Caption1,
  Skeleton,
  SkeletonItem,
  Title2,
  makeStyles,
  tokens,
} from '@fluentui/react-components';
import { useMemo } from 'react';
import { useNavigate } from 'react-router';
import useAuth from '../../hooks/useAuth';
import useNotifications from '../../hooks/useNotifications';
import { DEDICATED_NOTIFICATION_TYPES, type NotificationView } from '../../types/Notification';

type NotificationScope = 'all' | 'replies' | 'reactions' | 'notifications';
type PageKind = 'replies' | 'likes' | 'notifications';

type NotificationItem = {
  id: number;
  title: string;
  summary: string;
  contextSummary?: string | null;
  time: string;
  href?: string;
  unread?: boolean;
};

const useStyles = makeStyles({
  page: {
    width: 'min(100%, 62rem)',
    margin: '0 auto',
    padding: `${tokens.spacingVerticalXL} ${tokens.spacingHorizontalL}`,
    display: 'grid',
    gap: tokens.spacingVerticalXL,
  },
  hero: {
    display: 'grid',
    gap: tokens.spacingVerticalS,
  },
  heroRow: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: tokens.spacingHorizontalM,
    flexWrap: 'wrap',
  },
  muted: {
    color: tokens.colorNeutralForeground2,
  },
  listCard: {
    overflow: 'hidden',
    borderRadius: tokens.borderRadiusXLarge,
    boxShadow: tokens.shadow8,
  },
  listBody: {
    display: 'grid',
  },
  statusPanel: {
    padding: `${tokens.spacingVerticalXL} ${tokens.spacingHorizontalL}`,
    display: 'grid',
    gap: tokens.spacingVerticalM,
    justifyItems: 'start',
  },
  skeletonPanel: {
    padding: `${tokens.spacingVerticalL} ${tokens.spacingHorizontalL}`,
    display: 'grid',
    gap: tokens.spacingVerticalL,
  },
  skeletonItem: {
    display: 'grid',
    gap: tokens.spacingVerticalS,
  },
  skeletonRow: {
    display: 'flex',
    justifyContent: 'space-between',
    gap: tokens.spacingHorizontalM,
    alignItems: 'center',
  },
  itemButton: {
    justifyContent: 'flex-start',
    textAlign: 'left',
    whiteSpace: 'normal',
    height: 'auto',
    borderRadius: 0,
    padding: `${tokens.spacingVerticalL} ${tokens.spacingHorizontalL}`,
  },
  itemContent: {
    width: '100%',
    display: 'grid',
    gap: tokens.spacingVerticalS,
  },
  itemRow: {
    display: 'flex',
    justifyContent: 'space-between',
    gap: tokens.spacingHorizontalM,
    alignItems: 'center',
    flexWrap: 'wrap',
  },
  itemMeta: {
    display: 'flex',
    alignItems: 'center',
    gap: tokens.spacingHorizontalS,
    flexWrap: 'wrap',
  },
  itemSummary: {
    color: tokens.colorNeutralForeground2,
    lineHeight: tokens.lineHeightBase300,
  },
  itemContext: {
    color: tokens.colorNeutralForeground3,
    lineHeight: tokens.lineHeightBase200,
  },
  unreadDot: {
    width: '0.6rem',
    height: '0.6rem',
    borderRadius: tokens.borderRadiusCircular,
    backgroundColor: tokens.colorPaletteBerryForeground2,
    boxShadow: `0 0 0 4px ${tokens.colorPaletteBerryBackground2}`,
    flexShrink: 0,
  },
  loginCard: {
    padding: tokens.spacingHorizontalXL,
    display: 'grid',
    gap: tokens.spacingVerticalM,
    maxWidth: '34rem',
    margin: '0 auto',
    borderRadius: tokens.borderRadiusXLarge,
    boxShadow: tokens.shadow16,
  },
  statusCard: {
    padding: tokens.spacingHorizontalXL,
    display: 'grid',
    gap: tokens.spacingVerticalM,
    borderRadius: tokens.borderRadiusXLarge,
    boxShadow: tokens.shadow8,
  },
});

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

function toNotificationItem(notification: NotificationView): NotificationItem {
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

function filterNotifications(kind: PageKind, notifications: NotificationView[]) {
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

type NotificationPageBaseProps = {
  title: string;
  description: string;
  scope: NotificationScope;
  kind: PageKind;
};

function NotificationListSkeleton() {
  const styles = useStyles();

  return (
    <div className={styles.skeletonPanel}>
      {Array.from({ length: 1 }, (_, index) => (
        <div key={index} className={styles.skeletonItem}>
          <div className={styles.skeletonRow}>
            <Skeleton>
              <SkeletonItem shape="rectangle" size={16} style={{ width: '16rem' }} />
            </Skeleton>
            <Skeleton>
              <SkeletonItem shape="rectangle" size={12} style={{ width: '4.5rem' }} />
            </Skeleton>
          </div>
          <Skeleton>
            <SkeletonItem shape="rectangle" size={14} style={{ width: '70%' }} />
          </Skeleton>
          <Skeleton>
            <SkeletonItem shape="rectangle" size={12} style={{ width: '48%' }} />
          </Skeleton>
        </div>
      ))}
    </div>
  );
}

export default function NotificationPageBase({
  title,
  description,
  scope,
  kind,
}: NotificationPageBaseProps) {
  const styles = useStyles();
  const navigate = useNavigate();
  const { profile, isLoading } = useAuth();
  const {
    notifications,
    unreadCount,
    isLoading: isNotificationsLoading,
    markingIds,
    errorMessage,
    refresh,
    markAsRead,
  } = useNotifications(Boolean(profile), scope);

  const visibleItems = useMemo(
    () => filterNotifications(kind, notifications).map(toNotificationItem),
    [kind, notifications],
  );

  const handleNotificationClick = async (item: NotificationItem) => {
    if (!item.href) {
      return;
    }

    const success = await markAsRead(item.id);
    if (success) {
      navigate(item.href);
    }
  };

  if (isLoading) {
    return (
      <div className={styles.page}>
        <Card className={styles.statusCard}>
          <Title2>正在加载</Title2>
        </Card>
      </div>
    );
  }

  if (!profile) {
    return (
      <div className={styles.page}>
        <Card className={styles.loginCard}>
          <Badge appearance="filled" color="brand">
            仅登录后可见
          </Badge>
          <Title2>{title}</Title2>
          <div>
            <Button appearance="primary" onClick={() => navigate('/auth/login')}>
              前往登录
            </Button>
          </div>
        </Card>
      </div>
    );
  }

  return (
    <div className={styles.page}>
      <div className={styles.hero}>
        <div className={styles.heroRow}>
          <Title2>{title}</Title2>
          {unreadCount > 0 ? (
            <Badge appearance="filled" color="danger">
              {unreadCount} 条未读
            </Badge>
          ) : (
            <Badge appearance="outline" color="subtle">
              已全部查看
            </Badge>
          )}
        </div>
        <Body1 className={styles.muted}>{description}</Body1>
        {errorMessage ? <Caption1 className={styles.muted}>{errorMessage}</Caption1> : null}
      </div>

      <Card className={styles.listCard}>
        <div className={styles.listBody}>
          {isNotificationsLoading ? (
            <NotificationListSkeleton />
          ) : visibleItems.length > 0 ? (
            visibleItems.map((item) => (
              <Button
                key={item.id}
                appearance="subtle"
                className={styles.itemButton}
                onClick={item.href ? () => void handleNotificationClick(item) : undefined}
                disabled={markingIds.has(item.id)}
              >
                <div className={styles.itemContent}>
                  <div className={styles.itemRow}>
                    <div className={styles.itemMeta}>
                      {item.unread ? (
                        <span className={styles.unreadDot} aria-hidden="true" />
                      ) : null}
                      <Body1Strong>{item.title}</Body1Strong>
                    </div>
                    <Caption1 className={styles.muted}>{item.time}</Caption1>
                  </div>
                  <Body1 className={styles.itemSummary}>{item.summary}</Body1>
                  {item.contextSummary ? (
                    <Caption1 className={styles.itemContext}>{item.contextSummary}</Caption1>
                  ) : null}
                </div>
              </Button>
            ))
          ) : (
            <div className={styles.statusPanel}>
              <Body1 className={styles.muted}>暂无内容。</Body1>
              {errorMessage ? (
                <div>
                  <Button appearance="secondary" onClick={() => void refresh()}>
                    重试
                  </Button>
                </div>
              ) : null}
            </div>
          )}
        </div>
      </Card>
    </div>
  );
}
