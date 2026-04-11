import {
  Body1,
  Body1Strong,
  Button,
  Caption1,
  Card,
  CardHeader,
  Divider,
  Field,
  Input,
  Persona,
  Spinner,
  Subtitle2,
  Textarea,
  Title2,
  makeStyles,
  tokens,
} from '@fluentui/react-components';
import { useEffect, useRef, useState, type SubmitEvent } from 'react';
import { useNavigate } from 'react-router';
import useAccount from '../hooks/useAccount';
import useFeedPosts from '../hooks/useFeedPosts';

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
  cardBody: {
    padding: tokens.spacingHorizontalL,
    display: 'grid',
    gap: tokens.spacingVerticalL,
  },
  formFields: {
    display: 'grid',
    gap: tokens.spacingVerticalM,
  },
  row: {
    display: 'flex',
    gap: tokens.spacingHorizontalM,
    alignItems: 'center',
    flexWrap: 'wrap',
  },
  rowBetween: {
    display: 'flex',
    justifyContent: 'space-between',
    gap: tokens.spacingHorizontalM,
    alignItems: 'center',
  },
  postList: {
    display: 'grid',
    gap: tokens.spacingVerticalL,
  },
  postBody: {
    display: 'grid',
    gap: tokens.spacingVerticalS,
  },
  postText: {
    color: tokens.colorNeutralForeground1,
    lineHeight: tokens.lineHeightBase400,
    whiteSpace: 'pre-wrap',
  },
  actionsEnd: {
    display: 'flex',
    justifyContent: 'flex-end',
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

const postDateFormatter = new Intl.DateTimeFormat('zh-CN', {
  year: 'numeric',
  month: '2-digit',
  day: '2-digit',
  hour: '2-digit',
  minute: '2-digit',
});

function formatPostTime(value: string) {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return '';
  }

  return postDateFormatter.format(date);
}

export default function FeedPage() {
  const styles = useStyles();
  const navigate = useNavigate();
  const { profile } = useAccount();
  const {
    posts,
    hasMore,
    isInitialLoading,
    isLoadingMore,
    errorMessage,
    publishErrorMessage,
    isPublishing,
    loadMore,
    publishPost,
    refresh,
  } = useFeedPosts();
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const sentinelRef = useRef<HTMLDivElement | null>(null);

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

  const handleSubmit = async (event: SubmitEvent) => {
    event.preventDefault();

    const normalizedContent = content.trim();
    if (!normalizedContent) {
      return;
    }

    const success = await publishPost({
      title: title.trim() || null,
      content: normalizedContent,
    });

    if (success) {
      setTitle('');
      setContent('');
    }
  };

  return (
    <div className={styles.page}>
      <div className={styles.hero}>
        <Title2>新鲜事</Title2>
        <Body1 className={styles.muted}>分享精彩瞬间。</Body1>
      </div>

      {profile ? (
        <Card>
          <div className={styles.cardBody}>
            <CardHeader
              image={
                <Persona
                  name={profile.nickname}
                  secondaryText={`@${profile.username}`}
                  size="small"
                  textAlignment="center"
                />
              }
              header={<Body1Strong>发布新鲜事</Body1Strong>}
              description={
                <Caption1 className={styles.muted}>你的动态会展示在公开信息流中</Caption1>
              }
            />
            <Divider />
            <form className={styles.cardBody} onSubmit={handleSubmit}>
              <div className={styles.formFields}>
                <Field label="标题（可选）">
                  <Input
                    value={title}
                    onChange={(_event, data) => setTitle(data.value)}
                    placeholder="好的标题更容易受到关注哦"
                  />
                </Field>
                <Field label="内容">
                  <Textarea
                    value={content}
                    onChange={(_event, data) => setContent(data.value)}
                    placeholder="说点什么……"
                    resize="vertical"
                  />
                </Field>
              </div>
              {publishErrorMessage ? (
                <Caption1 className={styles.muted}>{publishErrorMessage}</Caption1>
              ) : null}
              <div className={styles.actionsEnd}>
                <Button
                  appearance="subtle"
                  type="button"
                  onClick={() => {
                    setTitle('');
                    setContent('');
                  }}
                >
                  清空
                </Button>
                <Button
                  type="submit"
                  appearance="primary"
                  disabled={isPublishing || content.trim() === ''}
                >
                  {isPublishing ? '发布中' : '发布'}
                </Button>
              </div>
            </form>
          </div>
        </Card>
      ) : (
        <Card>
          <div className={styles.cardBody}>
            <Subtitle2>登录后参与互动</Subtitle2>
            <Body1 className={styles.muted}>当前未登录，登录后即可分享新鲜事。</Body1>
            <div className={styles.row}>
              <Button appearance="primary" onClick={() => navigate('/auth/login')}>
                去登录
              </Button>
            </div>
          </div>
        </Card>
      )}

      <div className={styles.rowBetween}>
        <Subtitle2>最新</Subtitle2>
        {isInitialLoading ? <Spinner size="tiny" label="加载中" /> : null}
      </div>

      {errorMessage ? (
        <Card>
          <div className={styles.cardBody}>
            <Body1 className={styles.muted}>{errorMessage}</Body1>
            <div className={styles.row}>
              <Button appearance="primary" onClick={() => void refresh()}>
                重试
              </Button>
            </div>
          </div>
        </Card>
      ) : null}

      {!isInitialLoading && posts.length === 0 && !errorMessage ? (
        <Card>
          <div className={styles.cardBody}>
            <Body1 className={styles.muted}>还没有帖子。</Body1>
          </div>
        </Card>
      ) : null}

      <div className={styles.postList}>
        {posts.map((post) => {
          const hasTitle = Boolean(post.title?.trim());
          const authorNickname = post.authorNickname?.trim() || '作者昵称';

          return (
            <Card key={post.id}>
              <div className={styles.cardBody}>
                <CardHeader
                  image={
                    <Persona
                      name={authorNickname}
                      secondaryText={hasTitle ? '帖子作者' : '内容帖作者'}
                      size="small"
                      textAlignment="center"
                    />
                  }
                  action={
                    <Caption1 className={styles.muted}>{formatPostTime(post.createdAt)}</Caption1>
                  }
                />
                <Divider />
                <div className={styles.postBody}>
                  {hasTitle ? <Subtitle2>{post.title}</Subtitle2> : null}
                  <Body1 className={styles.postText}>{post.content}</Body1>
                </div>
                <Caption1 className={styles.muted}>社区动态</Caption1>
              </div>
            </Card>
          );
        })}
      </div>

      <div ref={sentinelRef} className={styles.loadMore}>
        {isLoadingMore ? <Spinner size="tiny" label="加载更多" /> : null}
        {!hasMore && posts.length > 0 ? (
          <Caption1 className={styles.muted}>没有更多内容了。</Caption1>
        ) : null}
      </div>
    </div>
  );
}
