import {
  Badge,
  Body1,
  Body1Strong,
  Button,
  Caption1,
  Card,
  CardHeader,
  Divider,
  Spinner,
  Subtitle2,
  Title2,
  makeStyles,
  tokens,
} from '@fluentui/react-components';
import { useNavigate } from 'react-router';
import useAuth from '../hooks/useAuth';
import useMyActivities from '../hooks/useMyActivities';
import type { ActivityView } from '../types/Activity';

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
  muted: {
    color: tokens.colorNeutralForeground2,
  },
  dashboardRow: {
    display: 'grid',
    gridTemplateColumns: 'repeat(3, minmax(0, 1fr))',
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
  statsRow: {
    display: 'grid',
    gridTemplateColumns: 'repeat(3, minmax(0, 1fr))',
    gap: tokens.spacingHorizontalM,
    '@media (max-width: 640px)': {
      gridTemplateColumns: '1fr',
    },
  },
  stat: {
    display: 'grid',
    gap: tokens.spacingVerticalXXS,
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
  meta: {
    color: tokens.colorNeutralForeground2,
    whiteSpace: 'pre-wrap',
    lineHeight: tokens.lineHeightBase300,
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
        <Card>
          <div className={styles.cardBody}>
            <Button appearance="primary" onClick={() => navigate('/auth/login')}>
              登录后查看
            </Button>
          </div>
        </Card>
      </div>
    );
  }

  return (
    <div className={styles.page}>
      <div className={styles.hero}>
        <Title2>我的活动</Title2>
        <Body1 className={styles.muted}>查看我发起的活动状态，以及我已经报名的活动安排。</Body1>
      </div>

      <div className={styles.dashboardRow}>
        <Card appearance="filled-alternative" className={styles.dashboardCard}>
          <div className={styles.cardBody}>
            <Subtitle2>活动概览</Subtitle2>
            <div className={styles.statsRow}>
              <div className={styles.stat}>
                <Body1Strong>{created.length}</Body1Strong>
                <Caption1 className={styles.muted}>个我发起的活动</Caption1>
              </div>
              <div className={styles.stat}>
                <Body1Strong>{registered.length}</Body1Strong>
                <Caption1 className={styles.muted}>个我报名的活动</Caption1>
              </div>
              <div className={styles.stat}>
                <Body1Strong>{pendingCreatedCount}</Body1Strong>
                <Caption1 className={styles.muted}>个待审核活动</Caption1>
              </div>
            </div>
          </div>
        </Card>

        <Card className={styles.dashboardCard}>
          <div className={styles.cardBody}>
            <Subtitle2>快捷入口</Subtitle2>
            <Caption1 className={styles.muted}>切换到活动广场或继续发起新活动。</Caption1>
            <div className={styles.actionsRow}>
              <Button appearance="primary" onClick={() => navigate('/activities/plaza')}>
                前往活动广场
              </Button>
              <Button appearance="secondary" onClick={() => navigate('/activities/create')}>
                发起活动
              </Button>
            </div>
          </div>
        </Card>

        <Card className={styles.dashboardCard}>
          <div className={styles.cardBody}>
            <Subtitle2>状态说明</Subtitle2>
            <Caption1 className={styles.muted}>待审核表示等待管理员处理，已发布表示活动已经公开。</Caption1>
          </div>
        </Card>
      </div>

      <div className={styles.feedHeader}>
        <Subtitle2>我发起的活动</Subtitle2>
        {isLoading ? <Spinner size="tiny" label="加载中" /> : null}
      </div>

      {errorMessage ? (
        <Card>
          <div className={styles.cardBody}>
            <Caption1>{errorMessage}</Caption1>
          </div>
        </Card>
      ) : created.length === 0 ? (
        <Card>
          <div className={styles.cardBody}>
            <Caption1 className={styles.muted}>还没有发起过活动。</Caption1>
          </div>
        </Card>
      ) : (
        <div className={styles.list}>
          {created.map((activity) => (
            <Card key={activity.id}>
              <CardHeader
                header={<Body1Strong>{activity.title}</Body1Strong>}
                description={<Caption1 className={styles.meta}>活动开始 {formatTime(activity.startTime)}</Caption1>}
                action={<Badge appearance="outline">{getStatusLabel(activity.status)}</Badge>}
              />
              <Divider />
              <div className={styles.cardBody}>
                <Caption1 className={styles.meta}>
                  {activity.location}
                  {activity.rejectionReason ? `\n驳回原因：${activity.rejectionReason}` : ''}
                </Caption1>
                <div className={styles.actionsEnd}>
                  <Button appearance="secondary" onClick={() => navigate(`/activities/${activity.id}`)}>
                    查看详情
                  </Button>
                </div>
              </div>
            </Card>
          ))}
        </div>
      )}

      <div className={styles.feedHeader}>
        <Subtitle2>我报名的活动</Subtitle2>
      </div>

      {errorMessage ? null : registered.length === 0 ? (
        <Card>
          <div className={styles.cardBody}>
            <Caption1 className={styles.muted}>还没有报名任何活动。</Caption1>
          </div>
        </Card>
      ) : (
        <div className={styles.list}>
          {registered.map((activity) => (
            <Card key={activity.id}>
              <CardHeader
                header={<Body1Strong>{activity.title}</Body1Strong>}
                description={<Caption1 className={styles.meta}>{activity.location}</Caption1>}
              />
              <Divider />
              <div className={styles.cardBody}>
                <Caption1 className={styles.meta}>活动开始 {formatTime(activity.startTime)}</Caption1>
                <div className={styles.actionsEnd}>
                  <Button appearance="secondary" onClick={() => navigate(`/activities/${activity.id}`)}>
                    查看详情
                  </Button>
                </div>
              </div>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
