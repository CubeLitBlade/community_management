import {
  Badge,
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
  Spinner,
  Subtitle2,
  Textarea,
  Title2,
  makeStyles,
  tokens,
} from '@fluentui/react-components';
import { useState } from 'react';
import { useNavigate } from 'react-router';
import useAuth from '../hooks/useAuth';
import usePendingActivities from '../hooks/usePendingActivities';
import { Checkmark12Regular, CheckmarkRegular } from '@fluentui/react-icons';

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
  statsRow: {
    display: 'grid',
    gridTemplateColumns: 'repeat(2, minmax(0, 1fr))',
    gap: tokens.spacingHorizontalM,
  },
  stat: {
    display: 'grid',
    gap: tokens.spacingVerticalXXS,
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
  statusCard: {
    padding: tokens.spacingHorizontalL,
    display: 'grid',
    gap: tokens.spacingVerticalS,
    borderLeft: `${tokens.strokeWidthThick} solid ${tokens.colorStatusDangerBorder1}`,
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
            <Title2>活动审核</Title2>
            <Caption1 className={styles.muted}>仅管理员或站点所有者可以访问此页面。</Caption1>
            <div className={styles.row}>
              <Button appearance="secondary" onClick={() => navigate('/activities/plaza')}>
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
        <Title2>活动审核</Title2>
        <Body1 className={styles.muted}>
          处理用户提交的待审核活动。通过后将立即公开，并向发起人发送通知。
        </Body1>
      </div>

      <div className={styles.dashboardRow}>
        <Card appearance="filled-alternative" className={styles.dashboardCard}>
          <div className={styles.cardBody}>
            <Subtitle2>审核概览</Subtitle2>
            <div className={styles.statsRow}>
              <div className={styles.stat}>
                <Body1Strong>{pendingCount}</Body1Strong>
                <Caption1 className={styles.muted}>个待审核活动</Caption1>
              </div>
              <div className={styles.stat}>
                <Body1Strong>{latestCreatedAt}</Body1Strong>
                <Caption1 className={styles.muted}>最近提交时间</Caption1>
              </div>
            </div>
          </div>
        </Card>

        <Card className={styles.dashboardCard}>
          <div className={styles.cardBody}>
            <Subtitle2>审核提示</Subtitle2>
            <div className={styles.tipList}>
              <Caption1 className={styles.muted}>1. 先检查时间顺序是否合理。</Caption1>
              <Caption1 className={styles.muted}>2. 再确认地点与描述是否足够清晰。</Caption1>
              <Caption1 className={styles.muted}>3. 驳回时填写具体原因，便于发起人修改。</Caption1>
            </div>
          </div>
        </Card>
      </div>

      <div className={styles.feedHeader}>
        <Subtitle2>待审核列表</Subtitle2>
        {isLoading ? <Spinner size="tiny" label="加载中" /> : null}
      </div>

      {actionErrorMessage ? (
        <Card className={styles.statusCard}>
          <Body1Strong>操作失败</Body1Strong>
          <Caption1 className={styles.muted}>{actionErrorMessage}</Caption1>
        </Card>
      ) : null}

      {errorMessage ? (
        <Card>
          <div className={styles.cardBody}>
            <Caption1>{errorMessage}</Caption1>
          </div>
        </Card>
      ) : isLoading ? (
        <Card>
          <div className={styles.cardBody}>
            <Caption1 className={styles.muted}>正在加载待审核活动</Caption1>
          </div>
        </Card>
      ) : activities.length === 0 ? (
        <Card>
          <div className={styles.cardBody}>
            <Body1Strong>当前没有待审核活动</Body1Strong>
            <Caption1 className={styles.muted}>
              新活动提交后会自动出现在这里，便于集中审核。
            </Caption1>
          </div>
        </Card>
      ) : (
        <div className={styles.list}>
          {activities.map((activity) => (
            <Card key={activity.id}>
              <CardHeader
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
                action={<Badge appearance="tint">待审核</Badge>}
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
                    appearance="primary"
                    icon={actioningId === activity.id ? <Spinner /> : <CheckmarkRegular />}
                    disabled={actioningId === activity.id}
                    onClick={() => void approve(activity.id)}
                  >
                    {actioningId === activity.id ? '处理中...' : '通过'}
                  </Button>
                  <Dialog
                    open={rejectTargetId === activity.id}
                    onOpenChange={(_, data) => setRejectTargetId(data.open ? activity.id : null)}
                  >
                    <DialogTrigger disableButtonEnhancement>
                      <Button appearance="secondary" disabled={actioningId === activity.id}>
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
                          <Button appearance="secondary" onClick={() => setRejectTargetId(null)}>
                            取消
                          </Button>
                          <Button
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
