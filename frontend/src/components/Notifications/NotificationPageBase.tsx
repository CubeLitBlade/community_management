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
import {
  filterNotifications,
  toNotificationItem,
  type NotificationItem,
  type NotificationScope,
  type PageKind,
} from './notificationPresentation';

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
