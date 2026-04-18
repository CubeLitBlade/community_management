import {
  Badge,
  Body1,
  Body1Strong,
  Button,
  Card,
  Caption1,
  InfoLabel,
  Skeleton,
  SkeletonItem,
  Subtitle2,
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
    alignItems: 'start',
    justifyContent: 'space-between',
    gap: tokens.spacingHorizontalM,
    flexWrap: 'wrap',
  },
  heroTitleBlock: {
    display: 'grid',
    gap: tokens.spacingVerticalS,
  },
  muted: {
    color: tokens.colorNeutralForeground2,
  },
  dashboardRow: {
    display: 'grid',
    gridTemplateColumns: 'repeat(2, minmax(0, 1fr))',
    gap: tokens.spacingHorizontalL,
    alignItems: 'start',
    '@media (max-width: 980px)': {
      gridTemplateColumns: '1fr',
    },
  },
  dashboardCard: {
    minHeight: '100%',
  },
  dashboardBody: {
    padding: tokens.spacingHorizontalL,
    display: 'grid',
    gap: tokens.spacingVerticalM,
  },
  dashboardInfoLabel: {
    fontSize: tokens.fontSizeBase400,
    lineHeight: tokens.lineHeightBase400,
    fontWeight: tokens.fontWeightSemibold,
  },
  dashboardMetricGrid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(2, minmax(0, 1fr))',
    gap: tokens.spacingHorizontalS,
    '@media (max-width: 720px)': {
      gridTemplateColumns: '1fr',
    },
  },
  dashboardMetricItem: {
    display: 'grid',
    gap: tokens.spacingVerticalXS,
    padding: `${tokens.spacingVerticalM} ${tokens.spacingHorizontalM}`,
    borderRadius: tokens.borderRadiusLarge,
    backgroundColor: tokens.colorNeutralBackground2,
    alignContent: 'start',
  },
  detailLabel: {
    color: tokens.colorNeutralForeground3,
  },
  dashboardDetailItem: {
    display: 'grid',
    gap: tokens.spacingVerticalXXS,
    padding: `${tokens.spacingVerticalS} ${tokens.spacingHorizontalM}`,
    borderRadius: tokens.borderRadiusLarge,
    backgroundColor: tokens.colorNeutralBackground2,
  },
  tipList: {
    display: 'grid',
    gap: tokens.spacingVerticalS,
  },
  sectionHeader: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    gap: tokens.spacingHorizontalM,
    flexWrap: 'wrap',
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
    position: 'relative',
    overflow: 'hidden',
    padding: tokens.spacingHorizontalXL,
    display: 'grid',
    gap: tokens.spacingVerticalXL,
    borderRadius: tokens.borderRadiusXLarge,
    backgroundColor: tokens.colorNeutralBackground1,
    backgroundImage: `
      radial-gradient(circle at top right, ${tokens.colorBrandBackground2} 0%, transparent 32%),
      linear-gradient(180deg, ${tokens.colorNeutralBackground1} 0%, ${tokens.colorNeutralBackground2} 100%)
    `,
    boxShadow: tokens.shadow16,
  },
  loginHero: {
    display: 'grid',
    gap: tokens.spacingVerticalM,
  },
  loginKicker: {
    color: tokens.colorBrandForeground2,
  },
  loginFeatureGrid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(3, minmax(0, 1fr))',
    gap: tokens.spacingHorizontalM,
    '@media (max-width: 900px)': {
      gridTemplateColumns: '1fr',
    },
  },
  loginFeature: {
    display: 'grid',
    gap: tokens.spacingVerticalXS,
    padding: `${tokens.spacingVerticalM} ${tokens.spacingHorizontalM}`,
    borderRadius: tokens.borderRadiusLarge,
    backgroundColor: tokens.colorNeutralBackground1,
    border: `1px solid ${tokens.colorNeutralStroke2}`,
    backdropFilter: 'blur(12px)',
  },
  loginActions: {
    display: 'flex',
    gap: tokens.spacingHorizontalM,
    flexWrap: 'wrap',
  },
  loginTitleBlock: {
    display: 'grid',
    gap: tokens.spacingVerticalS,
    maxWidth: '34rem',
  },
  statusCard: {
    padding: tokens.spacingHorizontalXL,
    borderRadius: tokens.borderRadiusXLarge,
    boxShadow: tokens.shadow16,
  },
  statusPanelCard: {
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

function getPageContent(kind: PageKind) {
  switch (kind) {
    case 'replies':
      return {
        metricLabel: '回复动态',
        secondaryMetricLabel: '等待处理',
        hintTitle: '使用方式',
        hintBody: '点击任一消息可直接进入原帖，继续查看上下文并参与讨论。',
        secondaryHintTitle: '典型内容',
        secondaryHintBody: '这里聚合别人对你帖子内容的评论回复，适合快速跟进讨论。',
        emptyTitle: '暂时还没有新的回复',
        emptyBody: '当有人回复你的帖子后，会自动出现在这里，方便你集中查看。',
        loginTitle: '登录后集中查看别人对你的回复',
        loginBody: '你可以快速回看讨论上下文、继续参与交流，并跟踪最近哪些帖子有新的互动。',
      };
    case 'likes':
      return {
        metricLabel: '互动反馈',
        secondaryMetricLabel: '尚未查看',
        hintTitle: '使用方式',
        hintBody: '这里展示别人对你内容的点赞和表态，帮助你快速判断哪些内容获得了反馈。',
        secondaryHintTitle: '典型内容',
        secondaryHintBody: '包括赞、喜欢、好笑、难过等互动信号，适合快速浏览近期回应。',
        emptyTitle: '暂时还没有新的互动反馈',
        emptyBody: '当别人对你的内容做出回应时，会集中显示在这里。',
        loginTitle: '登录后查看内容获得的回应',
        loginBody: '你可以快速了解哪些帖子得到了互动反馈，方便继续维护有讨论热度的内容。',
      };
    case 'notifications':
      return {
        metricLabel: '系统与活动提醒',
        secondaryMetricLabel: '尚未处理',
        hintTitle: '使用方式',
        hintBody: '活动提醒、系统通知和公告会统一进入这里，便于你集中处理后续事项。',
        secondaryHintTitle: '典型内容',
        secondaryHintBody: '适合查看活动更新、系统公告和任务提醒，避免错过后续安排。',
        emptyTitle: '当前没有新的通知',
        emptyBody: '新的系统提醒或活动通知到达后，会自动出现在这里。',
        loginTitle: '登录后查看系统提醒与社区通知',
        loginBody: '你可以在一个面板里集中处理活动更新、公告提醒和后续系统消息。',
      };
  }
}

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
  const pageContent = getPageContent(kind);
  const visibleUnreadCount = visibleItems.filter((item) => item.unread).length;

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
          <div className={styles.loginHero}>
            <Caption1 className={styles.loginKicker}>PERSONAL INBOX</Caption1>
            <div className={styles.loginTitleBlock}>
              <Title2>{pageContent.loginTitle}</Title2>
              <Body1 className={styles.muted}>{pageContent.loginBody}</Body1>
            </div>
            <div className={styles.loginActions}>
              <Button appearance="primary" onClick={() => navigate('/auth/login')}>
                立即登录
              </Button>
              <Button appearance="secondary" onClick={() => navigate('/feed')}>
                先去新鲜事
              </Button>
            </div>
          </div>
          <div className={styles.loginFeatureGrid}>
            <div className={styles.loginFeature}>
              <Caption1 className={styles.detailLabel}>集中查看</Caption1>
              <Body1Strong>{pageContent.metricLabel}</Body1Strong>
              <Caption1 className={styles.muted}>{pageContent.loginBody}</Caption1>
            </div>
            <div className={styles.loginFeature}>
              <Caption1 className={styles.detailLabel}>及时处理</Caption1>
              <Body1Strong>未读内容会单独标记</Body1Strong>
              <Caption1 className={styles.muted}>进入详情后会自动更新阅读状态，减少遗漏。</Caption1>
            </div>
            <div className={styles.loginFeature}>
              <Caption1 className={styles.detailLabel}>关联上下文</Caption1>
              <Body1Strong>从消息直达原文或活动</Body1Strong>
              <Caption1 className={styles.muted}>回复、互动和提醒都能回到对应内容继续处理。</Caption1>
            </div>
          </div>
        </Card>
      </div>
    );
  }

  return (
    <div className={styles.page}>
      <div className={styles.hero}>
        <div className={styles.heroRow}>
          <div className={styles.heroTitleBlock}>
            <Title2>{title}</Title2>
            <Body1 className={styles.muted}>{description}</Body1>
            {errorMessage ? <Caption1 className={styles.muted}>{errorMessage}</Caption1> : null}
          </div>
          {visibleUnreadCount > 0 ? (
            <Badge appearance="filled" color="danger">
              {visibleUnreadCount} 条未读
            </Badge>
          ) : (
            <Badge appearance="outline" color="subtle">
              已全部查看
            </Badge>
          )}
        </div>
      </div>

      <div className={styles.dashboardRow}>
        <Card appearance="filled-alternative" className={styles.dashboardCard}>
          <div className={styles.dashboardBody}>
            <InfoLabel
              label={{ className: styles.dashboardInfoLabel }}
              info="帮助你快速判断当前页面的通知规模和未读情况。"
            >
              当前概览
            </InfoLabel>
            <div className={styles.dashboardMetricGrid}>
              <div className={styles.dashboardMetricItem}>
                <Caption1 className={styles.detailLabel}>{pageContent.metricLabel}</Caption1>
                <Title2>{visibleItems.length}</Title2>
              </div>
              <div className={styles.dashboardMetricItem}>
                <Caption1 className={styles.detailLabel}>{pageContent.secondaryMetricLabel}</Caption1>
                <Title2>{visibleUnreadCount}</Title2>
              </div>
            </div>
          </div>
        </Card>

        <Card className={styles.dashboardCard}>
          <div className={styles.dashboardBody}>
            <InfoLabel
              label={{ className: styles.dashboardInfoLabel }}
              info="不同消息类型适合不同的处理方式，这里提供一个快速提示。"
            >
              查看建议
            </InfoLabel>
            <div className={styles.tipList}>
              <div className={styles.dashboardDetailItem}>
                <Caption1 className={styles.detailLabel}>{pageContent.hintTitle}</Caption1>
                <Caption1 className={styles.muted}>{pageContent.hintBody}</Caption1>
              </div>
              <div className={styles.dashboardDetailItem}>
                <Caption1 className={styles.detailLabel}>{pageContent.secondaryHintTitle}</Caption1>
                <Caption1 className={styles.muted}>{pageContent.secondaryHintBody}</Caption1>
              </div>
            </div>
          </div>
        </Card>
      </div>

      <div className={styles.sectionHeader}>
        <Subtitle2>消息列表</Subtitle2>
        {unreadCount > 0 ? (
          <Badge appearance="tint" color="danger">
            全部来源未读 {unreadCount}
          </Badge>
        ) : null}
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
              <Body1Strong>{pageContent.emptyTitle}</Body1Strong>
              <Caption1 className={styles.muted}>{pageContent.emptyBody}</Caption1>
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
