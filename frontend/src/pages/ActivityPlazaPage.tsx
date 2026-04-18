import {
  Body1,
  Body1Strong,
  Button,
  Caption1,
  Card,
  CardFooter,
  CardHeader,
  Divider,
  InfoLabel,
  MessageBar,
  MessageBarBody,
  MessageBarTitle,
  ProgressBar,
  SearchBox,
  Spinner,
  Skeleton,
  SkeletonItem,
  Subtitle2,
  Subtitle2Stronger,
  Title2,
  makeStyles,
  tokens,
} from '@fluentui/react-components';
import {
  Calendar28Regular,
  CalendarAddRegular,
  PersonArrowLeftRegular,
} from '@fluentui/react-icons';
import { useEffect, useMemo, useRef } from 'react';
import { useNavigate } from 'react-router';
import useAuth from '../hooks/useAuth';
import useActivityPlaza from '../hooks/useActivityPlaza';

const useStyles = makeStyles({
  page: {
    width: 'min(100%, 56rem)',
    margin: '0 auto',
    padding: `${tokens.spacingVerticalXXL} ${tokens.spacingHorizontalL}`,
    display: 'grid',
    gap: tokens.spacingVerticalXL,
  },
  hero: {
    display: 'grid',
    gap: tokens.spacingVerticalS,
  },
  heroHeader: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'start',
    gap: tokens.spacingHorizontalM,
    flexWrap: 'wrap',
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
  cardBody: {
    padding: tokens.spacingHorizontalL,
    display: 'grid',
    gap: tokens.spacingVerticalM,
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
  feedHeader: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'end',
    gap: tokens.spacingHorizontalM,
    flexWrap: 'wrap',
  },
  feedHeaderText: {
    display: 'grid',
    gap: tokens.spacingVerticalXXS,
  },
  searchControls: {
    display: 'flex',
    alignItems: 'center',
    gap: tokens.spacingHorizontalM,
    flexWrap: 'wrap',
    width: 'min(100%, 28rem)',
  },
  searchField: {
    flex: '1 1 16rem',
  },
  activityFeed: {
    display: 'grid',
    gap: tokens.spacingVerticalL,
  },
  meta: {
    color: tokens.colorNeutralForeground2,
    whiteSpace: 'pre-wrap',
    lineHeight: tokens.lineHeightBase300,
  },
  row: {
    display: 'flex',
    gap: tokens.spacingHorizontalM,
    flexWrap: 'wrap',
  },
  actionsEnd: {
    display: 'flex',
    justifyContent: 'flex-end',
    gap: tokens.spacingHorizontalM,
    flexWrap: 'wrap',
  },
  actionsRow: {
    display: 'grid',
    gap: tokens.spacingVerticalS,
  },
  headerMeta: {
    display: 'flex',
    alignItems: 'center',
    gap: tokens.spacingHorizontalS,
    flexWrap: 'wrap',
  },
  detailGrid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(2, minmax(0, 1fr))',
    gap: `${tokens.spacingVerticalM} ${tokens.spacingHorizontalL}`,
    '@media (max-width: 720px)': {
      gridTemplateColumns: '1fr',
    },
  },
  dashboardDetailGrid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(2, minmax(0, 1fr))',
    gap: tokens.spacingHorizontalS,
    '@media (max-width: 720px)': {
      gridTemplateColumns: '1fr',
    },
  },
  detailItem: {
    display: 'grid',
    gap: tokens.spacingVerticalXXS,
    padding: tokens.spacingHorizontalM,
    borderRadius: tokens.borderRadiusLarge,
    backgroundColor: tokens.colorNeutralBackground2,
  },
  dashboardDetailItem: {
    display: 'grid',
    gap: tokens.spacingVerticalXXS,
    padding: `${tokens.spacingVerticalS} ${tokens.spacingHorizontalM}`,
    borderRadius: tokens.borderRadiusLarge,
    backgroundColor: tokens.colorNeutralBackground2,
  },
  dashboardTitleRow: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'start',
    gap: tokens.spacingHorizontalM,
    flexWrap: 'wrap',
  },
  dashboardSummary: {
    color: tokens.colorNeutralForeground2,
    whiteSpace: 'pre-wrap',
    lineHeight: tokens.lineHeightBase300,
  },
  dashboardMetricItem: {
    display: 'grid',
    gap: tokens.spacingVerticalXS,
    padding: `${tokens.spacingVerticalM} ${tokens.spacingHorizontalM}`,
    borderRadius: tokens.borderRadiusLarge,
    backgroundColor: tokens.colorNeutralBackground2,
    alignContent: 'start',
    alignItems: 'start',
  },
  dashboardEmptyState: {
    display: 'grid',
    justifyItems: 'center',
    textAlign: 'center',
    padding: `${tokens.spacingVerticalL} ${tokens.spacingHorizontalM}`,
  },
  detailLabel: {
    color: tokens.colorNeutralForeground3,
  },
  descriptionBlock: {
    display: 'grid',
    gap: tokens.spacingVerticalS,
  },
  description: {
    whiteSpace: 'pre-wrap',
    lineHeight: tokens.lineHeightBase400,
  },
  footer: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    gap: tokens.spacingHorizontalM,
    flexWrap: 'wrap',
    padding: `${tokens.spacingVerticalM} ${tokens.spacingHorizontalL} ${tokens.spacingVerticalL}`,
  },
  footerMeta: {
    display: 'grid',
    gap: tokens.spacingVerticalXXS,
  },
  progress: {
    width: '100%',
  },
  skeletonCard: {
    padding: tokens.spacingHorizontalL,
    display: 'grid',
    gap: tokens.spacingVerticalL,
  },
  skeletonHeader: {
    display: 'grid',
    gap: tokens.spacingVerticalS,
  },
  skeletonMetaRow: {
    display: 'flex',
    gap: tokens.spacingHorizontalM,
    flexWrap: 'wrap',
  },
  skeletonGrid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(2, minmax(0, 1fr))',
    gap: tokens.spacingHorizontalL,
    '@media (max-width: 720px)': {
      gridTemplateColumns: '1fr',
    },
  },
  fallbackCard: {
    padding: tokens.spacingHorizontalXL,
    display: 'grid',
    gap: tokens.spacingVerticalM,
  },
  fallbackActions: {
    display: 'flex',
    gap: tokens.spacingHorizontalM,
    flexWrap: 'wrap',
  },
  loadMore: {
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    minHeight: '2rem',
  },
});

const dateFormatter = new Intl.DateTimeFormat('zh-CN', {
  year: 'numeric',
  month: '2-digit',
  day: '2-digit',
  hour: '2-digit',
  minute: '2-digit',
});

function formatTime(value: string) {
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? '' : dateFormatter.format(date);
}

function summarizeDescription(value: string) {
  const text = value.trim();
  if (text.length <= 92) {
    return text;
  }
  return `${text.slice(0, 92)}...`;
}

function ActivityPlazaCardSkeleton() {
  const styles = useStyles();

  return (
    <Card>
      <div className={styles.skeletonCard}>
        <div className={styles.skeletonHeader}>
          <Skeleton>
            <SkeletonItem shape="rectangle" size={20} style={{ width: '48%' }} />
          </Skeleton>
          <div className={styles.skeletonMetaRow}>
            <Skeleton>
              <SkeletonItem shape="rectangle" size={12} style={{ width: '6rem' }} />
            </Skeleton>
            <Skeleton>
              <SkeletonItem shape="rectangle" size={12} style={{ width: '8rem' }} />
            </Skeleton>
          </div>
        </div>
        <div className={styles.skeletonGrid}>
          {Array.from({ length: 4 }, (_, index) => (
            <Skeleton key={index}>
              <SkeletonItem shape="rectangle" size={48} />
            </Skeleton>
          ))}
        </div>
        <div className={styles.skeletonHeader}>
          <Skeleton>
            <SkeletonItem shape="rectangle" size={14} style={{ width: '100%' }} />
          </Skeleton>
          <Skeleton>
            <SkeletonItem shape="rectangle" size={14} style={{ width: '74%' }} />
          </Skeleton>
        </div>
      </div>
    </Card>
  );
}

export default function ActivityPlazaPage() {
  const styles = useStyles();
  const navigate = useNavigate();
  const { profile } = useAuth();
  const {
    activities,
    isLoading,
    isSearching,
    isLoadingMore,
    hasMore,
    hasLoaded,
    errorMessage,
    searchKeyword,
    activeSearchKeyword,
    setSearchKeyword,
    loadMore,
  } = useActivityPlaza();
  const sentinelRef = useRef<HTMLDivElement | null>(null);
  const hasActiveSearch = activeSearchKeyword.length > 0;
  const showInitialLoading = isLoading && !hasLoaded;

  useEffect(() => {
    const sentinel = sentinelRef.current;
    if (!sentinel) {
      return;
    }

    const observer = new IntersectionObserver(
      (entries) => {
        if (entries.some((entry) => entry.isIntersecting)) {
          void loadMore();
        }
      },
      {
        rootMargin: '480px 0px',
      },
    );

    observer.observe(sentinel);

    return () => observer.disconnect();
  }, [loadMore]);

  const nextActivity = activities[0] ?? null;
  const nextDeadlineLabel = useMemo(() => {
    return nextActivity ? formatTime(nextActivity.registrationDeadline) : '暂无活动';
  }, [nextActivity]);

  return (
    <div className={styles.page}>
      <div className={styles.hero}>
        <div className={styles.heroHeader}>
          <Title2>活动广场</Title2>
          <Button
            appearance="primary"
            icon={profile ? <CalendarAddRegular /> : <PersonArrowLeftRegular />}
            onClick={() => navigate(profile ? '/activities/create' : '/auth/login')}
          >
            {profile ? '发起活动' : '登录后发起活动'}
          </Button>
        </div>
        <Body1 className={styles.muted}>浏览社区近期活动与报名安排。</Body1>
      </div>

      <div className={styles.dashboardRow}>
        <Card appearance="filled-alternative" className={styles.dashboardCard}>
          <div className={styles.dashboardBody}>
            <InfoLabel
              label={{ className: styles.dashboardInfoLabel }}
              info="先看最近报名截止，再决定当前的参与优先级。"
            >
              活动概览
            </InfoLabel>
            <div className={styles.dashboardDetailGrid}>
              <div className={styles.dashboardMetricItem}>
                <Caption1 className={styles.detailLabel}>已发布活动</Caption1>
                <Subtitle2Stronger>{activities.length}</Subtitle2Stronger>
              </div>
              <div className={styles.dashboardMetricItem}>
                <Caption1 className={styles.detailLabel}>最近报名截止</Caption1>
                <Subtitle2Stronger>{nextDeadlineLabel}</Subtitle2Stronger>
              </div>
            </div>
          </div>
        </Card>

        <Card className={styles.dashboardCard}>
          <div className={styles.dashboardBody}>
            <InfoLabel
              label={{ className: styles.dashboardInfoLabel }}
              info="优先关注最靠前的一场公开活动。"
            >
              下一场活动
            </InfoLabel>
            {nextActivity ? (
              <>
                <div className={styles.dashboardDetailItem}>
                  <Caption1 className={styles.detailLabel}>活动标题</Caption1>
                  <div className={styles.dashboardTitleRow}>
                    <Body1Strong>{nextActivity.title}</Body1Strong>
                    <Button
                      appearance="subtle"
                      onClick={() => navigate(`/activities/${nextActivity.id}`)}
                    >
                      查看详情
                    </Button>
                  </div>
                  <Caption1 className={styles.dashboardSummary}>
                    {nextActivity.location} · 开始时间 {formatTime(nextActivity.startTime)}
                  </Caption1>
                </div>
              </>
            ) : (
              <div className={styles.dashboardEmptyState}>
                <Subtitle2 className={styles.muted}>近期暂无活动安排。</Subtitle2>
              </div>
            )}
          </div>
        </Card>
      </div>

      <div className={styles.feedHeader}>
        <div className={styles.feedHeaderText}>
          <Subtitle2>最新活动</Subtitle2>
          <Caption1 className={styles.muted}>支持按标题、地点、活动描述搜索。</Caption1>
        </div>
        <div className={styles.searchControls}>
          <SearchBox
            className={styles.searchField}
            value={searchKeyword}
            placeholder="搜索标题、地点、描述"
            onChange={(_, data) => setSearchKeyword(data.value)}
          />
        </div>
      </div>
      {isLoading || isSearching ? <ProgressBar className={styles.progress} /> : null}

      {errorMessage ? (
        <MessageBar intent="error" layout="multiline">
          <MessageBarBody>
            <MessageBarTitle>加载失败</MessageBarTitle>
            {errorMessage}
          </MessageBarBody>
        </MessageBar>
      ) : null}

      {!showInitialLoading &&
      !isSearching &&
      !errorMessage &&
      activities.length === 0 &&
      !hasActiveSearch ? (
        <Card appearance="filled-alternative">
          <div className={styles.fallbackCard}>
            <Body1Strong>暂无已发布活动</Body1Strong>
            <Caption1 className={styles.muted}>
              新活动提交并通过审核后，会出现在这里供其他用户查看和报名。
            </Caption1>
            <div className={styles.fallbackActions}>
              <Button
                appearance="primary"
                icon={profile ? <CalendarAddRegular /> : <PersonArrowLeftRegular />}
                onClick={() => navigate(profile ? '/activities/create' : '/auth/login')}
              >
                {profile ? '发起活动' : '登录后发起活动'}
              </Button>
            </div>
          </div>
        </Card>
      ) : null}

      {!showInitialLoading &&
      !isSearching &&
      !errorMessage &&
      activities.length === 0 &&
      hasActiveSearch ? (
        <Card appearance="filled-alternative">
          <div className={styles.fallbackCard}>
            <Body1Strong>未找到匹配活动</Body1Strong>
            <Caption1 className={styles.muted}>
              没有找到和“{activeSearchKeyword}”相关的活动，试试其他关键词。
            </Caption1>
          </div>
        </Card>
      ) : null}

      {!errorMessage && showInitialLoading ? (
        <div className={styles.activityFeed}>
          <ActivityPlazaCardSkeleton />
          <ActivityPlazaCardSkeleton />
        </div>
      ) : null}

      {!errorMessage && !showInitialLoading && activities.length > 0 ? (
        <div className={styles.activityFeed}>
          {activities.map((activity) => (
            <Card key={activity.id}>
              <CardHeader
                image={<Calendar28Regular />}
                header={<Body1Strong>{activity.title}</Body1Strong>}
                description={
                  <div className={styles.headerMeta}>
                    <Caption1 className={styles.muted}>
                      {activity.creatorDisplayName || '匿名发起人'}
                    </Caption1>
                    <Caption1 className={styles.muted}>
                      创建于 {formatTime(activity.createdAt)}
                    </Caption1>
                  </div>
                }
              />
              <Divider />
              <div className={styles.cardBody}>
                <div className={styles.detailGrid}>
                  <div className={styles.detailItem}>
                    <Caption1 className={styles.detailLabel}>活动地点</Caption1>
                    <Body1>{activity.location}</Body1>
                  </div>
                  <div className={styles.detailItem}>
                    <Caption1 className={styles.detailLabel}>报名截止</Caption1>
                    <Body1>{formatTime(activity.registrationDeadline)}</Body1>
                  </div>
                  <div className={styles.detailItem}>
                    <Caption1 className={styles.detailLabel}>开始时间</Caption1>
                    <Body1>{formatTime(activity.startTime)}</Body1>
                  </div>
                  <div className={styles.detailItem}>
                    <Caption1 className={styles.detailLabel}>结束时间</Caption1>
                    <Body1>{formatTime(activity.endTime)}</Body1>
                  </div>
                </div>
                <div className={styles.descriptionBlock}>
                  <Caption1 className={styles.detailLabel}>活动描述</Caption1>
                  <Body1 className={styles.description}>
                    {summarizeDescription(activity.description)}
                  </Body1>
                </div>
              </div>
              <Divider />
              <CardFooter className={styles.footer}>
                <div className={styles.footerMeta}>
                  <Caption1 className={styles.muted}>
                    当前已报名 {activity.participantCount} 人
                  </Caption1>
                  <Caption1 className={styles.muted}>报名后可在“我的活动”中查看安排</Caption1>
                </div>
                <div className={styles.row}>
                  <Button
                    appearance="secondary"
                    onClick={() => navigate(`/activities/${activity.id}`)}
                  >
                    查看详情
                  </Button>
                </div>
              </CardFooter>
            </Card>
          ))}
        </div>
      ) : null}

      <div ref={sentinelRef} className={styles.loadMore}>
        {isLoadingMore ? <Spinner size="tiny" label="加载更多" /> : null}
        {!hasMore && activities.length > 0 ? (
          <Caption1 className={styles.muted}>没有更多活动了。</Caption1>
        ) : null}
      </div>
    </div>
  );
}
