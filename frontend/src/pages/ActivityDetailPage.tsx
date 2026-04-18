import {
  Breadcrumb,
  BreadcrumbDivider,
  BreadcrumbItem,
  Dialog,
  DialogActions,
  DialogBody,
  DialogContent,
  DialogSurface,
  DialogTitle,
  Body1,
  Body1Strong,
  Button,
  Caption1,
  Card,
  CardFooter,
  CardHeader,
  Divider,
  MessageBar,
  MessageBarBody,
  MessageBarTitle,
  Persona,
  Skeleton,
  SkeletonItem,
  Tag,
  makeStyles,
  tokens,
} from '@fluentui/react-components';
import {
  ArchiveRegular,
  Calendar28Regular,
  CheckmarkCircleRegular,
  ErrorCircleRegular,
  TimerRegular,
} from '@fluentui/react-icons';
import { useMemo, useState, useSyncExternalStore } from 'react';
import { useNavigate, useParams } from 'react-router';
import useAuth from '../hooks/useAuth';
import useActivityDetail from '../hooks/useActivityDetail';
import useActivityParticipants from '../hooks/useActivityParticipants';

const useStyles = makeStyles({
  page: {
    width: 'min(100%, 56rem)',
    margin: '0 auto',
    padding: `${tokens.spacingVerticalXXL} ${tokens.spacingHorizontalL}`,
    display: 'grid',
    gap: tokens.spacingVerticalXL,
  },
  breadcrumb: {
    alignItems: 'center',
  },
  breadcrumbCurrent: {
    color: tokens.colorNeutralForeground2,
    fontWeight: tokens.fontWeightSemibold,
  },
  cardBody: {
    padding: tokens.spacingHorizontalL,
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
  detailItem: {
    display: 'grid',
    gap: tokens.spacingVerticalXXS,
    padding: tokens.spacingHorizontalM,
    borderRadius: tokens.borderRadiusLarge,
    backgroundColor: tokens.colorNeutralBackground2,
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
  participantList: {
    display: 'grid',
    gap: tokens.spacingVerticalM,
  },
  participantItem: {
    padding: `${tokens.spacingVerticalS} 0`,
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

function getStatusIcon(status: string) {
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
      return undefined;
  }
}

let currentTimestamp = Date.now();
const nowListeners = new Set<() => void>();
let nowTimerId: number | null = null;

function emitCurrentTimestamp() {
  currentTimestamp = Date.now();
  nowListeners.forEach((listener) => listener());
}

function subscribeToCurrentTimestamp(listener: () => void) {
  nowListeners.add(listener);

  if (nowTimerId === null) {
    nowTimerId = window.setInterval(() => {
      emitCurrentTimestamp();
    }, 1000);
  }

  return () => {
    nowListeners.delete(listener);
    if (nowListeners.size === 0 && nowTimerId !== null) {
      window.clearInterval(nowTimerId);
      nowTimerId = null;
    }
  };
}

function getCurrentTimestampSnapshot() {
  return currentTimestamp;
}

function ActivityDetailSkeleton() {
  const styles = useStyles();

  return (
    <Card>
      <div className={styles.skeletonCard}>
        <div className={styles.skeletonHeader}>
          <Skeleton>
            <SkeletonItem shape="rectangle" size={24} style={{ width: '42%' }} />
          </Skeleton>
          <div className={styles.skeletonMetaRow}>
            <Skeleton>
              <SkeletonItem shape="rectangle" size={12} style={{ width: '7rem' }} />
            </Skeleton>
            <Skeleton>
              <SkeletonItem shape="rectangle" size={12} style={{ width: '8rem' }} />
            </Skeleton>
          </div>
        </div>
        <div className={styles.detailGrid}>
          {Array.from({ length: 5 }, (_, index) => (
            <Skeleton key={index}>
              <SkeletonItem shape="rectangle" size={52} />
            </Skeleton>
          ))}
        </div>
        <div className={styles.skeletonHeader}>
          <Skeleton>
            <SkeletonItem shape="rectangle" size={14} style={{ width: '100%' }} />
          </Skeleton>
          <Skeleton>
            <SkeletonItem shape="rectangle" size={14} style={{ width: '86%' }} />
          </Skeleton>
          <Skeleton>
            <SkeletonItem shape="rectangle" size={14} style={{ width: '72%' }} />
          </Skeleton>
        </div>
      </div>
    </Card>
  );
}

export default function ActivityDetailPage() {
  const styles = useStyles();
  const navigate = useNavigate();
  const { postId, activityId } = useParams();
  const resolvedId = activityId ?? postId;
  const currentTime = useSyncExternalStore(
    subscribeToCurrentTimestamp,
    getCurrentTimestampSnapshot,
  );
  const { profile } = useAuth();
  const [isParticipantsOpen, setIsParticipantsOpen] = useState(false);
  const {
    activity,
    isLoading,
    errorMessage,
    isSubmitting,
    submitErrorMessage,
    register,
    cancelRegistration,
  } = useActivityDetail(resolvedId);
  const {
    participants,
    isLoading: isParticipantsLoading,
    errorMessage: participantsErrorMessage,
    refresh: refreshParticipants,
  } = useActivityParticipants(resolvedId);

  const isDeadlinePassed = activity
    ? new Date(activity.registrationDeadline).getTime() <= currentTime
    : false;

  const canViewParticipants = useMemo(() => {
    if (!activity || !profile) {
      return false;
    }
    return (
      activity.creatorAccountId === Number(profile.id) ||
      profile.role === 'admin' ||
      profile.role === 'owner'
    );
  }, [activity, profile]);

  const isCreator = activity?.creatorAccountId === Number(profile?.id);

  return (
    <div className={styles.page}>
      <Breadcrumb className={styles.breadcrumb}>
        <BreadcrumbItem>
          <Button onClick={() => navigate('/activities/plaza')} appearance="subtle" size="small">
            活动广场
          </Button>
        </BreadcrumbItem>
        <BreadcrumbDivider />
        <BreadcrumbItem>
          <Button className={styles.breadcrumbCurrent} appearance="subtle" size="small" disabled>
            {activity?.title || '活动详情'}
          </Button>
        </BreadcrumbItem>
      </Breadcrumb>
      {isLoading ? (
        <ActivityDetailSkeleton />
      ) : errorMessage || !activity ? (
        <Card appearance="filled-alternative">
          <div className={styles.fallbackCard}>
            <Body1Strong>{errorMessage ? '加载活动详情失败' : '活动不存在'}</Body1Strong>
            <Caption1 className={styles.meta}>
              {errorMessage
                ? '活动信息暂时无法获取，你可以稍后重试，或先返回活动广场查看其他活动。'
                : '这个活动可能已下线，或链接已经失效。'}
            </Caption1>
            <div className={styles.fallbackActions}>
              <Button appearance="secondary" onClick={() => navigate('/activities/plaza')}>
                返回活动广场
              </Button>
            </div>
          </div>
        </Card>
      ) : (
        <Card>
          <CardHeader
            image={<Calendar28Regular />}
            header={<Body1>{activity.title}</Body1>}
            description={
              <div className={styles.headerMeta}>
                <Caption1 className={styles.meta}>
                  {activity.creatorDisplayName || '匿名发起人'}
                </Caption1>
                <Caption1 className={styles.meta}>创建于 {formatTime(activity.createdAt)}</Caption1>
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
                <Caption1 className={styles.detailLabel}>已报名人数</Caption1>
                <Body1>{activity.participantCount} 人</Body1>
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
            {activity.status === 'rejected' && activity.rejectionReason ? (
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
              {submitErrorMessage ? (
                <MessageBar intent="error" layout="multiline">
                  <MessageBarBody>
                    <MessageBarTitle>操作失败</MessageBarTitle>
                    {submitErrorMessage}
                  </MessageBarBody>
                </MessageBar>
              ) : null}
              {isCreator ? (
                <Caption1 className={styles.meta}>
                  这是你发起的活动，可在“我的活动”中查看审核状态。
                </Caption1>
              ) : null}
            </div>
            {profile ? (
              <div className={styles.row}>
                {canViewParticipants ? (
                  <Dialog
                    open={isParticipantsOpen}
                    onOpenChange={async (_, data) => {
                      setIsParticipantsOpen(data.open);
                      if (data.open) {
                        await refreshParticipants();
                      }
                    }}
                  >
                    <Button
                      appearance="secondary"
                      onClick={async () => {
                        setIsParticipantsOpen(true);
                        await refreshParticipants();
                      }}
                    >
                      查看报名成员
                    </Button>
                    <DialogSurface>
                      <DialogBody>
                        <DialogTitle>报名成员</DialogTitle>
                        <DialogContent>
                          {participantsErrorMessage ? (
                            <MessageBar intent="error" layout="multiline">
                              <MessageBarBody>
                                <MessageBarTitle>加载失败</MessageBarTitle>
                                {participantsErrorMessage}
                              </MessageBarBody>
                            </MessageBar>
                          ) : isParticipantsLoading ? (
                            <Caption1 className={styles.meta}>加载中...</Caption1>
                          ) : participants.length === 0 ? (
                            <Caption1 className={styles.meta}>当前还没有报名成员。</Caption1>
                          ) : (
                            <div className={styles.participantList}>
                              {participants.map((participant) => (
                                <div key={participant.accountId} className={styles.participantItem}>
                                  <Persona
                                    name={participant.displayName}
                                    secondaryText={`报名时间 ${formatTime(participant.registeredAt)}`}
                                  />
                                </div>
                              ))}
                            </div>
                          )}
                        </DialogContent>
                        <DialogActions>
                          <Button
                            appearance="secondary"
                            onClick={() => setIsParticipantsOpen(false)}
                          >
                            关闭
                          </Button>
                        </DialogActions>
                      </DialogBody>
                    </DialogSurface>
                  </Dialog>
                ) : null}
                {activity.status === 'approved' && !activity.viewerRegistered && !isCreator ? (
                  <Button
                    appearance="primary"
                    disabled={isSubmitting || isDeadlinePassed}
                    onClick={() => void register()}
                  >
                    {isDeadlinePassed ? '报名已截止' : isSubmitting ? '提交中...' : '立即报名'}
                  </Button>
                ) : null}
                {activity.status === 'approved' && activity.viewerRegistered ? (
                  <Button
                    appearance="secondary"
                    disabled={isSubmitting || isDeadlinePassed}
                    onClick={() => void cancelRegistration()}
                  >
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
          </CardFooter>
        </Card>
      )}
    </div>
  );
}
