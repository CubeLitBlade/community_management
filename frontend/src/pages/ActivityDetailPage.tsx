import {
  Badge,
  Body1,
  Body1Strong,
  Button,
  Caption1,
  Card,
  Spinner,
  Title2,
  makeStyles,
  tokens,
} from '@fluentui/react-components';
import { useMemo } from 'react';
import { useNavigate, useParams } from 'react-router';
import useAuth from '../hooks/useAuth';
import useActivityDetail from '../hooks/useActivityDetail';

const useStyles = makeStyles({
  page: {
    width: 'min(100%, 48rem)',
    margin: '0 auto',
    padding: `${tokens.spacingVerticalXL} ${tokens.spacingHorizontalL}`,
    display: 'grid',
    gap: tokens.spacingVerticalL,
  },
  section: {
    padding: tokens.spacingHorizontalL,
    display: 'grid',
    gap: tokens.spacingVerticalM,
  },
  meta: {
    color: tokens.colorNeutralForeground2,
    whiteSpace: 'pre-wrap',
  },
  actions: {
    display: 'flex',
    gap: tokens.spacingHorizontalM,
    flexWrap: 'wrap',
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

function getStatusLabel(status: string) {
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

export default function ActivityDetailPage() {
  const styles = useStyles();
  const navigate = useNavigate();
  const { postId, activityId } = useParams();
  const resolvedId = activityId ?? postId;
  const { profile } = useAuth();
  const { activity, isLoading, errorMessage, isSubmitting, submitErrorMessage, register, cancelRegistration } =
    useActivityDetail(resolvedId);

  const isDeadlinePassed = useMemo(() => {
    if (!activity) {
      return false;
    }
    return new Date(activity.registrationDeadline).getTime() <= Date.now();
  }, [activity]);

  return (
    <div className={styles.page}>
      <Button appearance="subtle" onClick={() => navigate(-1)}>
        返回
      </Button>
      {isLoading ? (
        <Spinner size="small" />
      ) : errorMessage || !activity ? (
        <Caption1>{errorMessage || '活动不存在。'}</Caption1>
      ) : (
        <Card>
          <div className={styles.section}>
            <div>
              <Title2>{activity.title}</Title2>
              <Caption1 className={styles.meta}>
                {activity.creatorDisplayName || '匿名发起人'} · {activity.location}
              </Caption1>
            </div>
            <div className={styles.actions}>
              <Badge appearance="filled">{getStatusLabel(activity.status)}</Badge>
              <Caption1 className={styles.meta}>已报名 {activity.participantCount} 人</Caption1>
            </div>
            <Body1>{activity.description}</Body1>
            <Caption1 className={styles.meta}>
              报名截止 {formatTime(activity.registrationDeadline)}
              {'\n'}
              开始时间 {formatTime(activity.startTime)}
              {'\n'}
              结束时间 {formatTime(activity.endTime)}
            </Caption1>
            {activity.status === 'rejected' && activity.rejectionReason ? (
              <Caption1 className={styles.meta}>驳回原因：{activity.rejectionReason}</Caption1>
            ) : null}
            {submitErrorMessage ? <Caption1>{submitErrorMessage}</Caption1> : null}
            {profile ? (
              <div className={styles.actions}>
                {activity.status === 'approved' && !activity.viewerRegistered ? (
                  <Button appearance="primary" disabled={isSubmitting || isDeadlinePassed} onClick={() => void register()}>
                    {isDeadlinePassed ? '报名已截止' : isSubmitting ? '提交中...' : '立即报名'}
                  </Button>
                ) : null}
                {activity.status === 'approved' && activity.viewerRegistered ? (
                  <Button appearance="secondary" disabled={isSubmitting || isDeadlinePassed} onClick={() => void cancelRegistration()}>
                    {isDeadlinePassed ? '已过截止时间' : isSubmitting ? '处理中...' : '取消报名'}
                  </Button>
                ) : null}
                <Button appearance="subtle" onClick={() => navigate('/activities/about-me')}>
                  查看我的活动
                </Button>
              </div>
            ) : (
              <Button appearance="primary" onClick={() => navigate('/auth/login')}>
                登录后报名
              </Button>
            )}
            {activity.creatorAccountId === Number(profile?.id) ? (
              <Body1Strong>这是你发起的活动，可在“我的活动”中查看审核状态。</Body1Strong>
            ) : null}
          </div>
        </Card>
      )}
    </div>
  );
}
