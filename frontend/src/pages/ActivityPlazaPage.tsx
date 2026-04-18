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
import { useMemo } from 'react';
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
    gap: tokens.spacingVerticalM,
  },
  statsRow: {
    display: 'grid',
    gridTemplateColumns: 'repeat(2, minmax(0, 1fr))',
    gap: tokens.spacingHorizontalM,
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
  activityFeed: {
    display: 'grid',
    gap: tokens.spacingVerticalL,
  },
  activityCard: {
    display: 'grid',
    gap: tokens.spacingVerticalM,
    padding: tokens.spacingHorizontalL,
  },
  meta: {
    color: tokens.colorNeutralForeground2,
    whiteSpace: 'pre-wrap',
    lineHeight: tokens.lineHeightBase300,
  },
  description: {
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

export default function ActivityPlazaPage() {
  const styles = useStyles();
  const navigate = useNavigate();
  const { profile } = useAuth();
  const { activities, isLoading, errorMessage } = useActivityPlaza();

  const nextActivity = activities[0] ?? null;
  const nextDeadlineLabel = useMemo(() => {
    return nextActivity ? formatTime(nextActivity.registrationDeadline) : '暂无活动';
  }, [nextActivity]);

  return (
    <div className={styles.page}>
      <div className={styles.hero}>
        <Title2>活动广场</Title2>
        <Body1 className={styles.muted}>浏览社区近期活动与报名安排。</Body1>
      </div>

      <div className={styles.dashboardRow}>
        <Card appearance="filled-alternative" className={styles.dashboardCard}>
          <div className={styles.cardBody}>
            <Subtitle2>活动概览</Subtitle2>
            <div className={styles.statsRow}>
              <div className={styles.stat}>
                <Body1Strong>{activities.length}</Body1Strong>
                <Caption1 className={styles.muted}>个已发布活动</Caption1>
              </div>
              <div className={styles.stat}>
                <Body1Strong>{nextDeadlineLabel}</Body1Strong>
                <Caption1 className={styles.muted}>最近报名截止</Caption1>
              </div>
            </div>
          </div>
        </Card>

        <Card className={styles.dashboardCard}>
          <div className={styles.cardBody}>
            <Subtitle2>下一场活动</Subtitle2>
            {nextActivity ? (
              <>
                <Body1Strong>{nextActivity.title}</Body1Strong>
                <Caption1 className={styles.meta}>
                  {nextActivity.location}
                  {'\n'}
                  开始时间 {formatTime(nextActivity.startTime)}
                </Caption1>
                <div className={styles.actionsEnd}>
                  <Button
                    appearance="secondary"
                    onClick={() => navigate(`/activities/${nextActivity.id}`)}
                  >
                    查看详情
                  </Button>
                </div>
              </>
            ) : (
              <Caption1 className={styles.muted}>近期暂无活动安排。</Caption1>
            )}
          </div>
        </Card>

        <Card className={styles.dashboardCard}>
          <div className={styles.cardBody}>
            <Subtitle2>快捷入口</Subtitle2>
            <Caption1 className={styles.muted}>创建新活动或查看自己发起的活动记录。</Caption1>
            <div className={styles.actionsRow}>
              <Button
                appearance="primary"
                onClick={() => navigate(profile ? '/activities/create' : '/auth/login')}
              >
                {profile ? '发起活动' : '登录后发起活动'}
              </Button>
              <Button appearance="secondary" onClick={() => navigate('/activities/about-me')}>
                查看我的活动
              </Button>
            </div>
          </div>
        </Card>
      </div>

      <div className={styles.feedHeader}>
        <Subtitle2>最新活动</Subtitle2>
        {isLoading ? <Spinner size="tiny" label="加载中" /> : null}
      </div>

      {errorMessage ? (
        <Card>
          <div className={styles.cardBody}>
            <Caption1>{errorMessage}</Caption1>
          </div>
        </Card>
      ) : null}

      {!isLoading && !errorMessage && activities.length === 0 ? (
        <Card>
          <div className={styles.cardBody}>
            <Caption1 className={styles.muted}>暂无已发布活动。</Caption1>
          </div>
        </Card>
      ) : null}

      {!errorMessage ? (
        <div className={styles.activityFeed}>
          {activities.map((activity) => (
            <Card key={activity.id}>
              <CardHeader
                header={<Body1Strong>{activity.title}</Body1Strong>}
                description={
                  <Caption1 className={styles.meta}>
                    {activity.creatorDisplayName || '匿名发起人'} · {activity.location}
                  </Caption1>
                }
                action={<Badge appearance="outline">已报名 {activity.participantCount} 人</Badge>}
              />
              <Divider />
              <div className={styles.activityCard}>
                <Body1 className={styles.description}>
                  {summarizeDescription(activity.description)}
                </Body1>
                <Caption1 className={styles.meta}>
                  报名截止 {formatTime(activity.registrationDeadline)}
                  {'\n'}
                  开始时间 {formatTime(activity.startTime)}
                  {'\n'}
                  结束时间 {formatTime(activity.endTime)}
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
      ) : null}
    </div>
  );
}
