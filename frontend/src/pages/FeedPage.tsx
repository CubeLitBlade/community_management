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
  Field,
  Input,
  Link,
  Menu,
  MenuItem,
  MenuList,
  MenuPopover,
  MenuTrigger,
  Persona,
  Spinner,
  Subtitle2,
  Textarea,
  ToggleButton,
  Title2,
  makeStyles,
  tokens,
} from '@fluentui/react-components';
import { DeleteRegular, MoreHorizontalRegular, SlideTextEditRegular } from '@fluentui/react-icons';
import { useEffect, useRef, useState, type SubmitEvent } from 'react';
import { useNavigate } from 'react-router';
import useAccount from '../hooks/useAccount';
import useComments from '../hooks/useComments';
import useFeedPosts from '../hooks/useFeedPosts';
import type { PostReactionView } from '../types/Post';

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
  postFooter: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    gap: tokens.spacingHorizontalM,
  },
  footerMeta: {
    display: 'flex',
    alignItems: 'center',
    gap: tokens.spacingHorizontalM,
    flexWrap: 'wrap',
    minWidth: 0,
  },
  reactionList: {
    display: 'flex',
    alignItems: 'center',
    gap: tokens.spacingHorizontalS,
    flexWrap: 'wrap',
  },
  reactionButton: {
    minWidth: '3.25rem',
  },
  reactionButtonContent: {
    display: 'inline-flex',
    alignItems: 'center',
    gap: '0.35rem',
  },
  reactionIcon: {
    fontSize: tokens.fontSizeBase300,
    lineHeight: 1,
  },
  reactionCount: {
    minWidth: '1.5ch',
    textAlign: 'center',
    fontVariantNumeric: 'tabular-nums',
  },
  commentSection: {
    display: 'grid',
    gap: tokens.spacingVerticalM,
  },
  commentHeader: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    gap: tokens.spacingHorizontalM,
    flexWrap: 'wrap',
  },
  commentList: {
    display: 'grid',
    gap: tokens.spacingVerticalM,
  },
  commentItem: {
    display: 'grid',
    gap: tokens.spacingVerticalS,
    padding: tokens.spacingHorizontalM,
    borderRadius: tokens.borderRadiusLarge,
    backgroundColor: tokens.colorNeutralBackground2,
  },
  commentContent: {
    whiteSpace: 'pre-wrap',
    lineHeight: tokens.lineHeightBase300,
  },
  commentMeta: {
    display: 'flex',
    alignItems: 'center',
    gap: tokens.spacingHorizontalS,
    flexWrap: 'wrap',
  },
  emptyCommentState: {
    color: tokens.colorNeutralForeground3,
  },
  previewActions: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    gap: tokens.spacingHorizontalM,
    flexWrap: 'wrap',
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

function getPostTimeLabel(createdAt: string, updatedAt?: string | null) {
  const normalizedUpdatedAt = updatedAt?.trim() || '';
  if (normalizedUpdatedAt) {
    const formattedUpdatedAt = formatPostTime(normalizedUpdatedAt);
    if (formattedUpdatedAt) {
      return `编辑于 ${formattedUpdatedAt}`;
    }
  }

  const formattedCreatedAt = formatPostTime(createdAt);
  return formattedCreatedAt ? `发布于 ${formattedCreatedAt}` : '';
}

function getReactionLabel(reactionType: string) {
  switch (reactionType) {
    case 'like':
      return '点赞';
    case 'love':
      return '爱心';
    case 'laugh':
      return '好笑';
    case 'sad':
      return '难过';
    default:
      return reactionType;
  }
}

function getReactionIcon(reactionType: string) {
  switch (reactionType) {
    case 'like':
      return '👍';
    case 'love':
      return '❤️';
    case 'laugh':
      return '😄';
    case 'sad':
      return '😢';
    default:
      return '•';
  }
}

const REACTION_OPTIONS = ['like', 'love', 'laugh', 'sad'] as const;

function sortReactions(reactions?: PostReactionView[]) {
  if (!reactions?.length) {
    return [];
  }

  return [...reactions].sort((left, right) => {
    if (right.count !== left.count) {
      return right.count - left.count;
    }

    return left.reactionType.localeCompare(right.reactionType);
  });
}

export default function FeedPage() {
  const styles = useStyles();
  const navigate = useNavigate();
  const { profile } = useAccount();
  const {
    commentsByPostId,
    loadingPosts,
    errorMessage: commentErrorMessage,
    loadComments,
  } = useComments();
  const {
    posts,
    hasMore,
    isInitialLoading,
    isLoadingMore,
    errorMessage,
    publishErrorMessage,
    editErrorMessage,
    deleteErrorMessage,
    reactionErrorMessage,
    isPublishing,
    updatingPostId,
    deletingPostId,
    loadMore,
    publishPost,
    editPost,
    deletePost,
    setReaction,
    refresh,
  } = useFeedPosts();
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [editTitle, setEditTitle] = useState('');
  const [editContent, setEditContent] = useState('');
  const [showEditTitle, setShowEditTitle] = useState(true);
  const [editPostId, setEditPostId] = useState<number | null>(null);
  const [confirmDeletePostId, setConfirmDeletePostId] = useState<number | null>(null);
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

  useEffect(() => {
    posts.forEach((post) => {
      if (!commentsByPostId[post.id] && !loadingPosts[post.id]) {
        void loadComments(post.id);
      }
    });
  }, [commentsByPostId, loadComments, loadingPosts, posts]);

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

  const handleConfirmDelete = async () => {
    if (confirmDeletePostId === null) {
      return;
    }

    const success = await deletePost(confirmDeletePostId);
    if (success) {
      setConfirmDeletePostId(null);
    }
  };

  const openEditDialog = (postId: number, currentTitle: string | null, currentContent: string) => {
    const hasTitle = currentTitle !== null;
    setShowEditTitle(hasTitle);
    setEditTitle(currentTitle ?? '');
    setEditContent(currentContent);
    setEditPostId(postId);
  };

  const handleConfirmEdit = async () => {
    if (editPostId === null) {
      return;
    }

    const normalizedContent = editContent.trim();
    if (!normalizedContent) {
      return;
    }

    const success = await editPost(editPostId, {
      title: showEditTitle ? editTitle.trim() || null : null,
      content: normalizedContent,
    });

    if (success) {
      setEditPostId(null);
    }
  };

  const isAdmin = profile?.role === 'admin' || profile?.role === 'owner';
  const profileId = profile ? Number(profile.id) : Number.NaN;
  const handleReactionClick = async (
    postId: number,
    currentReaction: string | null,
    nextReaction: string,
  ) => {
    if (!profile) {
      navigate('/auth/login');
      return;
    }

    const targetReaction = currentReaction === nextReaction ? null : nextReaction;
    await setReaction(postId, targetReaction);
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

      {deleteErrorMessage ? (
        <Caption1 className={styles.muted}>{deleteErrorMessage}</Caption1>
      ) : null}

      {editErrorMessage ? <Caption1 className={styles.muted}>{editErrorMessage}</Caption1> : null}

      {reactionErrorMessage ? (
        <Caption1 className={styles.muted}>{reactionErrorMessage}</Caption1>
      ) : null}

      {commentErrorMessage ? (
        <Caption1 className={styles.muted}>{commentErrorMessage}</Caption1>
      ) : null}

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
          const hasTitle = post.title?.trim() != '';
          const authorNickname = post.authorNickname?.trim() || '已注销用户';
          const authorUsername = post.authorUsername?.trim() || '';
          const reactions = sortReactions(post.reactions);
          const reactionCountByType = new Map(
            reactions.map((reaction) => [reaction.reactionType, reaction.count]),
          );
          const viewerReaction = post.viewerReaction ?? null;
          const isAuthor = !Number.isNaN(profileId) && profileId === post.authorId;
          const canEdit = isAuthor;
          const canDelete = Boolean(profile) && (isAdmin || isAuthor);
          const canManagePost = canEdit || canDelete;

          return (
            <Card key={post.id}>
              <div className={styles.cardBody}>
                <CardHeader
                  image={
                    <Persona
                      name={authorNickname}
                      secondaryText={'@' + authorUsername}
                      size="small"
                      textAlignment="center"
                    />
                  }
                  action={
                    <Caption1 className={styles.muted}>
                      {getPostTimeLabel(post.createdAt, post.updatedAt)}
                    </Caption1>
                  }
                />
                <Divider />
                <div className={styles.postBody}>
                  {hasTitle ? <Subtitle2>{post.title}</Subtitle2> : null}
                  <Body1 className={styles.postText}>{post.content}</Body1>
                </div>
                <Divider />
                <CardFooter className={styles.postFooter}>
                  <div className={styles.footerMeta}>
                    <Caption1 className={styles.muted}>社区动态</Caption1>
                    <div className={styles.reactionList} aria-label="帖子互动统计">
                      {REACTION_OPTIONS.map((reactionType) => {
                        const reactionCount = reactionCountByType.get(reactionType) ?? 0;
                        const isSelected = viewerReaction === reactionType;

                        return (
                          <ToggleButton
                            key={reactionType}
                            size="small"
                            shape="circular"
                            checked={isSelected}
                            className={styles.reactionButton}
                            title={`${getReactionLabel(reactionType)} ${reactionCount}`}
                            onClick={() =>
                              void handleReactionClick(post.id, viewerReaction, reactionType)
                            }
                            isAccessible
                          >
                            <span className={styles.reactionButtonContent}>
                              <span className={styles.reactionIcon} aria-hidden="true">
                                {getReactionIcon(reactionType)}
                              </span>
                              <span className={styles.reactionCount}>{reactionCount}</span>
                            </span>
                          </ToggleButton>
                        );
                      })}
                    </div>
                  </div>
                  {canManagePost ? (
                    <Menu>
                      <MenuTrigger disableButtonEnhancement>
                        <Button
                          appearance="subtle"
                          icon={<MoreHorizontalRegular />}
                          aria-label="更多操作"
                          disabled={deletingPostId === post.id || updatingPostId === post.id}
                        />
                      </MenuTrigger>
                      <MenuPopover>
                        <MenuList>
                          {canEdit ? (
                            <MenuItem
                              icon={<SlideTextEditRegular />}
                              onClick={() => openEditDialog(post.id, post.title, post.content)}
                              disabled={updatingPostId === post.id}
                            >
                              {updatingPostId === post.id ? '编辑中' : '编辑'}
                            </MenuItem>
                          ) : null}
                          <MenuItem
                            icon={<DeleteRegular />}
                            onClick={() => setConfirmDeletePostId(post.id)}
                            disabled={deletingPostId === post.id}
                          >
                            {deletingPostId === post.id ? '删除中' : '删除'}
                          </MenuItem>
                        </MenuList>
                      </MenuPopover>
                    </Menu>
                  ) : null}
                </CardFooter>
                <Divider />
                <div className={styles.commentSection}>
                  <div className={styles.commentHeader}>
                    <Subtitle2>评论</Subtitle2>
                    <Caption1 className={styles.muted}>
                      {(commentsByPostId[post.id] ?? []).length} 条主评论
                    </Caption1>
                  </div>
                  {loadingPosts[post.id] && !(commentsByPostId[post.id] ?? []).length ? (
                    <Spinner size="tiny" label="加载评论" />
                  ) : null}
                  {(commentsByPostId[post.id] ?? []).length > 0 ? (
                    <div className={styles.commentList}>
                      {(commentsByPostId[post.id] ?? []).slice(0, 2).map((comment) => {
                        const authorNickname = comment.authorNickname || '已注销用户';
                        const authorUsername = comment.authorUsername?.trim() || '';

                        return (
                          <div key={comment.id} className={styles.commentItem}>
                            <div className={styles.commentMeta}>
                              <Body1Strong>{authorNickname}</Body1Strong>
                              {authorUsername ? (
                                <Caption1 className={styles.muted}>@{authorUsername}</Caption1>
                              ) : null}
                              <Caption1 className={styles.muted}>
                                {getPostTimeLabel(comment.createdAt, comment.updatedAt)}
                              </Caption1>
                            </div>
                            <Body1 className={styles.commentContent}>{comment.content}</Body1>
                          </div>
                        );
                      })}
                    </div>
                  ) : !loadingPosts[post.id] ? (
                    <Caption1 className={styles.emptyCommentState}>
                      还没有评论，来留下第一条吧。
                    </Caption1>
                  ) : null}
                  <div className={styles.previewActions}>
                    <Caption1 className={styles.muted}>
                      {profile ? '' : '登录后可在详情页参与评论。'}
                    </Caption1>
                    <Link
                      href={`/posts/${post.id}`}
                      onClick={(event) => {
                        event.preventDefault();
                        navigate(`/posts/${post.id}`);
                      }}
                    >
                      详情
                    </Link>
                  </div>
                </div>
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

      <Dialog
        open={editPostId !== null}
        onOpenChange={(_event, data) => {
          if (!data.open) {
            setEditPostId(null);
          }
        }}
      >
        <DialogSurface>
          <DialogBody>
            <DialogTitle>编辑帖子</DialogTitle>
            <DialogContent>
              <div className={styles.formFields}>
                {showEditTitle ? (
                  <Field label="标题">
                    <Input
                      value={editTitle}
                      onChange={(_event, data) => setEditTitle(data.value)}
                      placeholder="可编辑标题"
                    />
                  </Field>
                ) : null}
                <Field label="内容">
                  <Textarea
                    value={editContent}
                    onChange={(_event, data) => setEditContent(data.value)}
                    placeholder="编辑内容"
                    resize="vertical"
                  />
                </Field>
              </div>
            </DialogContent>
            <DialogActions>
              <Button appearance="secondary" onClick={() => setEditPostId(null)}>
                取消
              </Button>
              <Button
                appearance="primary"
                onClick={() => void handleConfirmEdit()}
                disabled={
                  editPostId === null || updatingPostId === editPostId || editContent.trim() === ''
                }
              >
                {updatingPostId === editPostId ? '编辑中' : '确认编辑'}
              </Button>
            </DialogActions>
          </DialogBody>
        </DialogSurface>
      </Dialog>

      <Dialog
        open={confirmDeletePostId !== null}
        onOpenChange={(_event, data) => {
          if (!data.open) {
            setConfirmDeletePostId(null);
          }
        }}
      >
        <DialogSurface>
          <DialogBody>
            <DialogTitle>确认删除</DialogTitle>
            <DialogContent>删除后无法恢复，确定要删除这条新鲜事吗？</DialogContent>
            <DialogActions>
              <Button appearance="secondary" onClick={() => setConfirmDeletePostId(null)}>
                取消
              </Button>
              <Button
                appearance="primary"
                onClick={() => void handleConfirmDelete()}
                disabled={confirmDeletePostId === null || deletingPostId === confirmDeletePostId}
              >
                {deletingPostId === confirmDeletePostId ? '删除中' : '确认删除'}
              </Button>
            </DialogActions>
          </DialogBody>
        </DialogSurface>
      </Dialog>
    </div>
  );
}
