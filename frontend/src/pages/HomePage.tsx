import {
  Avatar,
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
import useFeedPosts from '../hooks/useFeedPosts';

const useStyles = makeStyles({
  page: {
    width: 'min(100%, 70rem)',
    margin: '0 auto',
    padding: `${tokens.spacingVerticalL} ${tokens.spacingHorizontalL}`,
    display: 'grid',
    gap: tokens.spacingVerticalL,
  },
  hero: {
    display: 'grid',
    gap: tokens.spacingVerticalS,
  },
  muted: {
    color: tokens.colorNeutralForeground2,
  },
  board: {
    display: 'grid',
    gridTemplateColumns: 'minmax(0, 2fr) minmax(0, 1fr)',
    gap: tokens.spacingHorizontalL,
    alignItems: 'start',
    '@media (max-width: 980px)': {
      gridTemplateColumns: '1fr',
    },
  },
  rail: {
    display: 'grid',
    gap: tokens.spacingVerticalL,
  },
  surface: {
    display: 'flex',
    flexDirection: 'column',
    minHeight: '14rem',
  },
  section: {
    padding: tokens.spacingHorizontalL,
    display: 'grid',
    gap: tokens.spacingVerticalM,
  },
  streamList: {
    display: 'grid',
    gap: tokens.spacingVerticalS,
  },
  streamItemButton: {
    justifyContent: 'flex-start',
    textAlign: 'left',
    height: 'auto',
    whiteSpace: 'normal',
    padding: `${tokens.spacingVerticalM} ${tokens.spacingHorizontalM}`,
  },
  streamItem: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalXS,
  },
  row: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    gap: tokens.spacingHorizontalS,
  },
  rowStart: {
    display: 'flex',
    alignItems: 'center',
    gap: tokens.spacingHorizontalS,
  },
  streamMeta: {
    color: tokens.colorNeutralForeground2,
    lineHeight: tokens.lineHeightBase300,
    whiteSpace: 'pre-wrap',
  },
  loading: {
    display: 'flex',
    alignItems: 'center',
    gap: tokens.spacingHorizontalS,
    minHeight: '5rem',
  },
  railActions: {
    display: 'grid',
    gap: tokens.spacingVerticalS,
  },
});

const postDateFormatter = new Intl.DateTimeFormat('zh-CN', {
  year: 'numeric',
  month: '2-digit',
  day: '2-digit',
  hour: '2-digit',
  minute: '2-digit',
});

function formatPostTime(value: string) {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return '';
  return postDateFormatter.format(date);
}

export default function HomePage() {
  const styles = useStyles();
  const navigate = useNavigate();
  const { posts, isInitialLoading } = useFeedPosts();

  const displayPosts = posts.slice(0, 3);

  return (
    <div className={styles.page}>
      <div className={styles.hero}>
        <Title2>社区首页</Title2>
        <Body1 className={styles.muted}>按信息流组织内容：新鲜事、活动预告、通知公告。</Body1>
      </div>

      <div className={styles.board}>
        <Card appearance="filled-alternative" className={styles.surface}>
          <CardHeader
            header={<Subtitle2>新鲜事</Subtitle2>}
            description={<Caption1 className={styles.muted}>最新 3 条社区动态</Caption1>}
            action={
              <Button appearance="subtle" size="small" onClick={() => navigate('/feed')}>
                更多
              </Button>
            }
          />
          <Divider />
          <div className={styles.section}>
            {isInitialLoading ? (
              <div className={styles.loading}>
                <Spinner size="small" />
                <Caption1 className={styles.muted}>正在加载动态</Caption1>
              </div>
            ) : displayPosts.length > 0 ? (
              <div className={styles.streamList}>
                {displayPosts.map((post) => {
                  const authorNickname = post.authorNickname?.trim() || '匿名';
                  const hasTitle = Boolean(post.title?.trim());
                  const contentPreview =
                    post.content.length > 60 ? `${post.content.substring(0, 60)}...` : post.content;

                  return (
                    <Button
                      key={post.id}
                      appearance="subtle"
                      className={styles.streamItemButton}
                      onClick={() => navigate(`/posts/${post.id}`)}
                    >
                      <div className={styles.streamItem}>
                        <div className={styles.row}>
                          <div className={styles.rowStart}>
                            <Avatar name={authorNickname} size={24} />
                            <Body1Strong>{authorNickname}</Body1Strong>
                          </div>
                          <Caption1 className={styles.muted}>
                            {formatPostTime(post.createdAt)}
                          </Caption1>
                        </div>
                        {hasTitle ? <Body1Strong>{post.title}</Body1Strong> : null}
                        <Body1 className={styles.streamMeta}>{contentPreview}</Body1>
                      </div>
                    </Button>
                  );
                })}
              </div>
            ) : (
              <Caption1 className={styles.muted}>暂无新鲜事，来发布第一条动态吧。</Caption1>
            )}
            <div className={styles.railActions}>
              <Button appearance="secondary" onClick={() => navigate('/feed')}>
                去发布新鲜事
              </Button>
            </div>
          </div>
        </Card>

        <div className={styles.rail}>
          <Card className={styles.surface}>
            <CardHeader
              header={<Subtitle2>活动预告</Subtitle2>}
              description={<Caption1 className={styles.muted}>近期活动与招募信息</Caption1>}
            />
            <Divider />
            <div className={styles.section}>
              <Caption1 className={styles.muted}>
                活动流区域预留中，可展示热门活动、即将截止报名和我参与的活动。
              </Caption1>
              <div className={styles.railActions}>
                <Button appearance="primary" onClick={() => navigate('/activities/plaza')}>
                  浏览活动广场
                </Button>
              </div>
            </div>
          </Card>

          <Card className={styles.surface}>
            <CardHeader
              header={<Subtitle2>通知公告</Subtitle2>}
              description={
                <Caption1 className={styles.muted}>系统通知、活动提醒与社区公告</Caption1>
              }
            />
            <Divider />
            <div className={styles.section}>
              <Caption1 className={styles.muted}>
                通知流区域预留中，可展示未读通知、公告置顶和任务提醒。
              </Caption1>
              <div className={styles.railActions}>
                <Button appearance="secondary" onClick={() => navigate('/notifications')}>
                  查看通知中心
                </Button>
              </div>
            </div>
          </Card>
        </div>
      </div>
    </div>
  );
}
