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
  Skeleton,
  SkeletonItem,
  Subtitle2,
  Tag,
  Title2,
  makeStyles,
  tokens,
} from '@fluentui/react-components';
import { useNavigate } from 'react-router';
import useAuth from '../hooks/useAuth';
import useMyActivities from '../hooks/useMyActivities';
import type { ActivityView } from '../types/Activity';
import {
  ArchiveRegular,
  Calendar24Regular,
  CalendarAddRegular,
  CalendarRegular,
  CheckmarkCircleRegular,
  ErrorCircleRegular,
  TimerRegular,
} from '@fluentui/react-icons';

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
    gap: tokens.spacingVerticalL,
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
    alignItems: 'center',
    gap: tokens.spacingHorizontalM,
    flexWrap: 'wrap',
  },
  list: {
    display: 'grid',
    gap: tokens.spacingVerticalL,
  },
  row: {
    display: 'flex',
    gap: tokens.spacingHorizontalM,
    flexWrap: 'wrap',
  },
  meta: {
    color: tokens.colorNeutralForeground2,
    whiteSpace: 'pre-wrap',
    lineHeight: tokens.lineHeightBase300,
  },
  actionsRow: {
    display: 'grid',
    gap: tokens.spacingVerticalS,
  },
  tipList: {
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
  dashboardMetricGrid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(3, minmax(0, 1fr))',
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
  fallbackSurface: {
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
  fallbackHero: {
    display: 'grid',
    gap: tokens.spacingVerticalM,
  },
  fallbackKicker: {
    color: tokens.colorBrandForeground2,
  },
  fallbackTitleRow: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'start',
    gap: tokens.spacingHorizontalL,
    flexWrap: 'wrap',
  },
  fallbackTitleBlock: {
    display: 'grid',
    gap: tokens.spacingVerticalS,
    maxWidth: '34rem',
  },
  fallbackFeatureGrid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(3, minmax(0, 1fr))',
    gap: tokens.spacingHorizontalM,
    '@media (max-width: 900px)': {
      gridTemplateColumns: '1fr',
    },
  },
  fallbackFeature: {
    display: 'grid',
    gap: tokens.spacingVerticalXS,
    padding: `${tokens.spacingVerticalM} ${tokens.spacingHorizontalM}`,
    borderRadius: tokens.borderRadiusLarge,
    backgroundColor: tokens.colorNeutralBackground1,
    border: `1px solid ${tokens.colorNeutralStroke2}`,
    backdropFilter: 'blur(12px)',
  },
  fallbackActions: {
    display: 'flex',
    gap: tokens.spacingHorizontalM,
    flexWrap: 'wrap',
  },
});

const dateFormatter = new Intl.DateTimeFormat('zh-CN', {
  month: '2-digit',
  day: '2-digit',
  hour: '2-digit',
  minute: '2-digit',
});

function formatTime(value: string) {
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? '' : dateFormatter.format(date);
}

function getStatusLabel(status: ActivityView['status']) {
  switch (status) {
    case 'pending':
      return '待审核';
    case 'approved':
      return '已发布';
    case 'rejected':
      return '未通过';
    case 'archived':
      return '已归档';
    default:
      return status;
  }
}

function getStatusIcon(status: ActivityView['status']) {
  switch (status) {
    case 'pending':
      return <TimerRegular />;
    case 'approved':
      return <CheckmarkCircleRegular />;
    case 'rejected':
      return <ErrorCircleRegular />;
    case 'archived':
      return <ArchiveRegular />;
    default:
      return;
  }
}

function ActivityCardSkeleton() {
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
            <SkeletonItem shape="rectangle" size={14} style={{ width: '78%' }} />
          </Skeleton>
        </div>
      </div>
    </Card>
  );
}

export default function MyActivitiesPage() {
  const styles = useStyles();
  const navigate = useNavigate();
  const { profile } = useAuth();
  const { created, registered, isLoading, errorMessage } = useMyActivities(Boolean(profile));
  const pendingCreatedCount = created.filter((activity) => activity.status === 'pending').length;

  if (!profile) {
    return (
      <div className={styles.page}>
        <div className={styles.hero}>
          <Title2>我的活动</Title2>
          <Body1 className={styles.muted}>登录后查看我发起的活动和报名记录。</Body1>
        </div>
        <Card className={styles.fallbackSurface}>
          <div className={styles.fallbackHero}>
            <Caption1 className={styles.fallbackKicker}>PERSONAL ACTIVITY HUB</Caption1>
            <div className={styles.fallbackTitleRow}>
              <div className={styles.fallbackTitleBlock}>
                <Body1Strong>登录后解锁你的活动工作台</Body1Strong>
                <Body1 className={styles.muted}>
                  在这里统一查看自己发起的活动、报名进度与审核状态，重要安排会集中展示，不用再分散查找。
                </Body1>
              </div>
            </div>
            <div className={styles.fallbackActions}>
              <Button appearance="primary" onClick={() => navigate('/auth/login')}>
                立即登录
              </Button>
              <Button appearance="secondary" onClick={() => navigate('/activities/plaza')}>
                先去活动广场
              </Button>
            </div>
          </div>
          <div className={styles.fallbackFeatureGrid}>
            <div className={styles.fallbackFeature}>
              <Caption1 className={styles.detailLabel}>发起管理</Caption1>
              <Body1Strong>跟踪审核与发布状态</Body1Strong>
              <Caption1 className={styles.muted}>
                待审核、已发布、已归档都会在一个面板里汇总。
              </Caption1>
            </div>
            <div className={styles.fallbackFeature}>
              <Caption1 className={styles.detailLabel}>报名记录</Caption1>
              <Body1Strong>快速回看参与安排</Body1Strong>
              <Caption1 className={styles.muted}>已报名活动的时间、地点和详情会集中呈现。</Caption1>
            </div>
            <div className={styles.fallbackFeature}>
              <Caption1 className={styles.detailLabel}>下一步</Caption1>
              <Body1Strong>创建或加入感兴趣的活动</Body1Strong>
              <Caption1 className={styles.muted}>
                登录后可直接发起活动，也能继续浏览并报名公开活动。
              </Caption1>
            </div>
          </div>
        </Card>
      </div>
    );
  }

  return (
    <div className={styles.page}>
      <div className={styles.hero}>
        <div className={styles.heroHeader}>
          <Title2>我的活动</Title2>
          <Button
            appearance="primary"
            icon={<CalendarAddRegular />}
            onClick={() => navigate('/activities/create')}
          >
            发起活动
          </Button>
        </div>
        <Body1 className={styles.muted}>查看我发起的活动状态，以及我已经报名的活动安排。</Body1>
      </div>

      <div className={styles.dashboardRow}>
        <Card appearance="filled-alternative" className={styles.dashboardCard}>
          <div className={styles.dashboardBody}>
            <InfoLabel
              label={{ className: styles.dashboardInfoLabel }}
              info="从这里快速判断自己当前的发起与参与情况。"
            >
              活动概览
            </InfoLabel>
            <div className={styles.dashboardMetricGrid}>
              <div className={styles.dashboardMetricItem}>
                <Caption1 className={styles.detailLabel}>我发起的活动</Caption1>
                <Title2>{created.length}</Title2>
              </div>
              <div className={styles.dashboardMetricItem}>
                <Caption1 className={styles.detailLabel}>我报名的活动</Caption1>
                <Title2>{registered.length}</Title2>
              </div>
              <div className={styles.dashboardMetricItem}>
                <Caption1 className={styles.detailLabel}>待审核活动</Caption1>
                <Title2>{pendingCreatedCount}</Title2>
              </div>
            </div>
          </div>
        </Card>

        <Card className={styles.dashboardCard}>
          <div className={styles.dashboardBody}>
            <InfoLabel
              label={{ className: styles.dashboardInfoLabel }}
              info="不同状态决定活动是否已经公开或结束处理。"
            >
              状态说明
            </InfoLabel>
            <div className={styles.tipList}>
              <div className={styles.dashboardDetailItem}>
                <Caption1 className={styles.detailLabel}>审核中</Caption1>
                <Caption1 className={styles.muted}>等待管理员处理，暂不会显示在活动广场。</Caption1>
              </div>
              <div className={styles.dashboardDetailItem}>
                <Caption1 className={styles.detailLabel}>处理完成</Caption1>
                <Caption1 className={styles.muted}>
                  已发布表示公开可报名；未通过或已归档表示不再对外招募。
                </Caption1>
              </div>
            </div>
          </div>
        </Card>
      </div>

      <div className={styles.feedHeader}>
        <Subtitle2>我发起的活动</Subtitle2>
      </div>
      {isLoading ? <ProgressBar className={styles.progress} /> : null}

      {errorMessage ? (
        <MessageBar intent="error" layout="multiline">
          <MessageBarBody>
            <MessageBarTitle>加载失败</MessageBarTitle>
            {errorMessage}
          </MessageBarBody>
        </MessageBar>
      ) : isLoading ? (
        <div className={styles.list}>
          <ActivityCardSkeleton />
          <ActivityCardSkeleton />
        </div>
      ) : created.length === 0 ? (
        <Card appearance="filled-alternative">
          <div className={styles.fallbackCard}>
            <Body1Strong>还没有发起过活动</Body1Strong>
            <Caption1 className={styles.muted}>
              新活动提交后会先进入审核，通过后才会显示在活动广场。
            </Caption1>
            <div className={styles.fallbackActions}>
              <Button
                appearance="primary"
                icon={<CalendarAddRegular />}
                onClick={() => navigate('/activities/create')}
              >
                发起活动
              </Button>
            </div>
          </div>
        </Card>
      ) : (
        <div className={styles.list}>
          {created.map((activity) => (
            <Card key={activity.id}>
              <CardHeader
                image={<Calendar24Regular />}
                header={<Body1Strong>{activity.title}</Body1Strong>}
                description={
                  <div className={styles.headerMeta}>
                    <Caption1 className={styles.muted}>由你发起</Caption1>
                    <Caption1 className={styles.muted}>
                      创建于 {formatTime(activity.createdAt)}
                    </Caption1>
                  </div>
                }
                action={
                  <Tag shape="circular" icon={getStatusIcon(activity.status)}>
                    {getStatusLabel(activity.status)}
                  </Tag>
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
                  <Body1 className={styles.description}>{activity.description}</Body1>
                </div>
                {activity.rejectionReason ? (
                  <MessageBar intent="warning" layout="multiline">
                    <MessageBarBody>
                      <MessageBarTitle>驳回原因</MessageBarTitle>
                      {activity.rejectionReason}
                    </MessageBarBody>
                  </MessageBar>
                ) : null}
              </div>
              <Divider />
              <CardFooter className={styles.footer}>
                <div className={styles.footerMeta}>
                  <Caption1 className={styles.muted}>
                    当前已报名 {activity.participantCount} 人
                  </Caption1>
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
      )}

      <div className={styles.feedHeader}>
        <Subtitle2>我报名的活动</Subtitle2>
      </div>
      {!errorMessage && isLoading ? <ProgressBar className={styles.progress} /> : null}

      {errorMessage ? null : isLoading ? (
        <div className={styles.list}>
          <ActivityCardSkeleton />
        </div>
      ) : registered.length === 0 ? (
        <Card appearance="filled-alternative">
          <div className={styles.fallbackCard}>
            <Body1Strong>还没有报名任何活动</Body1Strong>
            <Caption1 className={styles.muted}>
              公开活动开放报名后，会出现在活动广场并同步到这里。
            </Caption1>
          </div>
        </Card>
      ) : (
        <div className={styles.list}>
          {registered.map((activity) => (
            <Card key={activity.id}>
              <CardHeader
                image={<CalendarRegular />}
                header={<Body1Strong>{activity.title}</Body1Strong>}
                description={
                  <div className={styles.headerMeta}>
                    <Caption1 className={styles.muted}>
                      {activity.creatorDisplayName || '匿名发起人'}
                    </Caption1>
                    <Caption1 className={styles.muted}>报名状态已确认</Caption1>
                  </div>
                }
                action={
                  <Tag shape="circular" icon={getStatusIcon(activity.status)}>
                    {getStatusLabel(activity.status)}
                  </Tag>
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
                  <Body1 className={styles.description}>{activity.description}</Body1>
                </div>
              </div>
              <Divider />
              <CardFooter className={styles.footer}>
                <div className={styles.footerMeta}>
                  <Caption1 className={styles.muted}>
                    当前已报名 {activity.participantCount} 人
                  </Caption1>
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
      )}
    </div>
  );
}
