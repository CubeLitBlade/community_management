import {
  Body1,
  Body1Strong,
  Button,
  Caption1,
  Card,
  CardFooter,
  CardHeader,
  Divider,
  Dialog,
  DialogActions,
  DialogBody,
  DialogContent,
  DialogSurface,
  DialogTitle,
  DialogTrigger,
  Field,
  InfoLabel,
  MessageBar,
  MessageBarBody,
  MessageBarTitle,
  ProgressBar,
  Skeleton,
  SkeletonItem,
  Spinner,
  Subtitle2,
  Textarea,
  Title2,
  makeStyles,
  tokens,
  Tag,
} from '@fluentui/react-components';
import { useState } from 'react';
import { useNavigate } from 'react-router';
import useAuth from '../hooks/useAuth';
import usePendingActivities from '../hooks/usePendingActivities';
import {
  Calendar28Regular,
  CheckmarkRegular,
  DismissRegular,
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
    display: 'grid',
    gap: tokens.spacingVerticalL,
    padding: tokens.spacingHorizontalL,
  },
  dashboardBody: {
    display: 'grid',
    gap: tokens.spacingVerticalM,
    padding: tokens.spacingHorizontalL,
  },
  dashboardInfoLabel: {
    fontSize: tokens.fontSizeBase400,
    lineHeight: tokens.lineHeightBase400,
    fontWeight: tokens.fontWeightSemibold,
  },
  list: {
    display: 'grid',
    gap: tokens.spacingVerticalL,
  },
  feedHeader: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    gap: tokens.spacingHorizontalM,
    flexWrap: 'wrap',
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
  headerMeta: {
    display: 'flex',
    alignItems: 'center',
    gap: tokens.spacingHorizontalS,
    flexWrap: 'wrap',
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
  tipList: {
    display: 'grid',
    gap: tokens.spacingVerticalS,
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

function PendingActivityListSkeleton() {
  const styles = useStyles();

  return (
    <div className={styles.list}>
      {Array.from({ length: 2 }, (_, index) => (
        <Card key={index}>
          <div className={styles.skeletonCard}>
            <div className={styles.skeletonHeader}>
              <Skeleton>
                <SkeletonItem shape="rectangle" size={20} style={{ width: '52%' }} />
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
            <div className={styles.skeletonGrid}>
              {Array.from({ length: 4 }, (_, detailIndex) => (
                <Skeleton key={detailIndex}>
                  <SkeletonItem shape="rectangle" size={48} />
                </Skeleton>
              ))}
            </div>
            <div className={styles.skeletonHeader}>
              <Skeleton>
                <SkeletonItem shape="rectangle" size={14} style={{ width: '100%' }} />
              </Skeleton>
              <Skeleton>
                <SkeletonItem shape="rectangle" size={14} style={{ width: '82%' }} />
              </Skeleton>
            </div>
          </div>
        </Card>
      ))}
    </div>
  );
}

export default function PendingActivitiesPage() {
  const styles = useStyles();
  const navigate = useNavigate();
  const { profile } = useAuth();
  const canModerate = profile?.role === 'admin' || profile?.role === 'owner';
  const { activities, isLoading, errorMessage, actioningId, actionErrorMessage, approve, reject } =
    usePendingActivities(canModerate);
  const [rejectTargetId, setRejectTargetId] = useState<number | null>(null);
  const [reason, setReason] = useState('');
  const pendingCount = activities.length;
  const latestCreatedAt = activities[0] ? formatTime(activities[0].createdAt) : '暂无待审';

  if (!canModerate) {
    return (
      <div className={styles.page}>
        <Card appearance="filled-alternative">
          <div className={styles.cardBody}>
            <Title2>活动审批</Title2>
            <Caption1 className={styles.muted}>仅管理员或站点所有者可以访问此页面。</Caption1>
            <div className={styles.row}>
              <Button
                type="button"
                appearance="secondary"
                onClick={() => navigate('/activities/plaza')}
              >
                返回活动广场
              </Button>
            </div>
          </div>
        </Card>
      </div>
    );
  }

  return (
    <div className={styles.page}>
      <div className={styles.hero}>
        <Title2>活动审批</Title2>
        <Body1 className={styles.muted}>
          处理用户提交的待审批活动。通过后将立即公开，并向发起人发送通知。
        </Body1>
      </div>

      <div className={styles.dashboardRow}>
        <Card appearance="filled-alternative" className={styles.dashboardCard}>
          <div className={styles.dashboardBody}>
            <InfoLabel
              label={{ className: styles.dashboardInfoLabel }}
              info="优先处理最近提交且即将开始的活动。"
            >
              审核概览
            </InfoLabel>
            <div className={styles.dashboardDetailGrid}>
              <div className={styles.dashboardMetricItem}>
                <Caption1 className={styles.detailLabel}>待审核数量</Caption1>
                <Title2>{pendingCount}</Title2>
              </div>
              <div className={styles.dashboardMetricItem}>
                <Caption1 className={styles.detailLabel}>最近提交时间</Caption1>
                <Subtitle2>{latestCreatedAt}</Subtitle2>
              </div>
            </div>
          </div>
        </Card>

        <Card className={styles.dashboardCard}>
          <div className={styles.dashboardBody}>
            <InfoLabel
              label={{ className: styles.dashboardInfoLabel }}
              info="保持时间、地点、说明三项信息完整可读。"
            >
              审核提示
            </InfoLabel>
            <div className={styles.tipList}>
              <div className={styles.dashboardDetailItem}>
                <Caption1 className={styles.detailLabel}>时间与信息</Caption1>
                <Caption1 className={styles.muted}>
                  先看报名截止、开始结束顺序，再确认地点和描述是否清晰。
                </Caption1>
              </div>
              <div className={styles.dashboardDetailItem}>
                <Caption1 className={styles.detailLabel}>驳回反馈</Caption1>
                <Caption1 className={styles.muted}>写清修改项，便于发起人尽快重提。</Caption1>
              </div>
            </div>
          </div>
        </Card>
      </div>

      <div className={styles.feedHeader}>
        <Subtitle2>待审核列表</Subtitle2>
      </div>
      {isLoading ? <ProgressBar className={styles.progress} /> : null}

      {actionErrorMessage ? (
        <MessageBar intent="error" layout="multiline">
          <MessageBarBody>
            <MessageBarTitle>操作失败</MessageBarTitle>
            {actionErrorMessage}
          </MessageBarBody>
        </MessageBar>
      ) : null}

      {errorMessage ? (
        <MessageBar intent="error" layout="multiline">
          <MessageBarBody>
            <MessageBarTitle>加载失败</MessageBarTitle>
            {errorMessage}
          </MessageBarBody>
        </MessageBar>
      ) : isLoading ? (
        <PendingActivityListSkeleton />
      ) : activities.length === 0 ? (
        <Card appearance="filled-alternative">
          <div className={styles.fallbackCard}>
            <Body1Strong>当前没有待审核活动</Body1Strong>
            <Caption1 className={styles.muted}>
              新活动提交后会自动出现在这里，便于后续集中审核。
            </Caption1>
          </div>
        </Card>
      ) : (
        <div className={styles.list}>
          {activities.map((activity) => (
            // Example Card
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
                action={
                  <Tag shape="circular" icon={<TimerRegular />}>
                    待审核
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
                  <Caption1 className={styles.muted}>审核后立即同步活动状态</Caption1>
                  <Caption1 className={styles.muted}>驳回时建议写清楚需要修改的具体内容</Caption1>
                </div>
                <div className={styles.row}>
                  <Button
                    type="button"
                    appearance="primary"
                    icon={
                      actioningId === activity.id ? <Spinner size="tiny" /> : <CheckmarkRegular />
                    }
                    disabled={actioningId === activity.id}
                    onClick={(event) => {
                      event.preventDefault();
                      void approve(activity.id);
                    }}
                  >
                    {actioningId === activity.id ? '处理中...' : '通过'}
                  </Button>
                  <Dialog
                    open={rejectTargetId === activity.id}
                    onOpenChange={(_, data) => setRejectTargetId(data.open ? activity.id : null)}
                  >
                    <DialogTrigger disableButtonEnhancement>
                      <Button
                        type="button"
                        icon={<DismissRegular />}
                        appearance="secondary"
                        disabled={actioningId === activity.id}
                      >
                        驳回
                      </Button>
                    </DialogTrigger>
                    <DialogSurface>
                      <DialogBody>
                        <DialogTitle>驳回活动</DialogTitle>
                        <DialogContent>
                          <Field label="驳回原因">
                            <Textarea
                              value={reason}
                              onChange={(_, data) => setReason(data.value)}
                            />
                          </Field>
                        </DialogContent>
                        <DialogActions>
                          <Button
                            type="button"
                            appearance="secondary"
                            onClick={() => setRejectTargetId(null)}
                          >
                            取消
                          </Button>
                          <Button
                            type="button"
                            appearance="primary"
                            onClick={async () => {
                              const ok = await reject(activity.id, reason.trim());
                              if (ok) {
                                setReason('');
                                setRejectTargetId(null);
                              }
                            }}
                          >
                            确认驳回
                          </Button>
                        </DialogActions>
                      </DialogBody>
                    </DialogSurface>
                  </Dialog>
                  <Button
                    type="button"
                    appearance="subtle"
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
