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
  MessageBar,
  MessageBarBody,
  MessageBarTitle,
  Menu,
  MenuItem,
  MenuList,
  MenuPopover,
  MenuTrigger,
  Persona,
  Popover,
  PopoverSurface,
  PopoverTrigger,
  Spinner,
  Subtitle2,
  Textarea,
  Title2,
  makeStyles,
  tokens,
} from '@fluentui/react-components';
import {
  CommentRegular,
  DeleteRegular,
  DismissRegular,
  MoreHorizontalRegular,
  SlideTextEditRegular,
  SlideTextTitleAddRegular,
  ThumbLikeRegular,
  WarningRegular,
} from '@fluentui/react-icons';
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
  cardBody: {
    padding: tokens.spacingHorizontalL,
    display: 'grid',
    gap: tokens.spacingVerticalL,
  },
  composerCard: {
    boxShadow: tokens.shadow16,
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
    alignItems: 'end',
    flexWrap: 'wrap',
  },
  feedHeaderText: {
    display: 'grid',
    gap: tokens.spacingVerticalXXS,
  },
  postList: {
    display: 'grid',
    gap: tokens.spacingVerticalL,
  },
  postCard: {
    boxShadow: tokens.shadow8,
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
    alignItems: 'center',
    gap: tokens.spacingHorizontalM,
    flexWrap: 'wrap',
  },
  composerFooter: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: tokens.spacingHorizontalM,
    flexWrap: 'wrap',
    padding: `${tokens.spacingVerticalS} ${tokens.spacingHorizontalL} ${tokens.spacingVerticalL}`,
  },
  composerActionHint: {
    textAlign: 'left',
    flex: '1 1 16rem',
  },
  loadMore: {
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    minHeight: '2rem',
  },
  postFooter: {
    display: 'grid',
    gridTemplateColumns: 'minmax(0, 1fr) auto',
    gap: tokens.spacingHorizontalM,
    alignItems: 'center',
    '@media (max-width: 720px)': {
      gridTemplateColumns: '1fr',
    },
  },
  footerPrimaryActions: {
    display: 'flex',
    alignItems: 'center',
    gap: tokens.spacingHorizontalM,
    flexWrap: 'wrap',
    minWidth: 0,
  },
  footerSecondaryActions: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'flex-end',
    gap: tokens.spacingHorizontalS,
    flexWrap: 'wrap',
  },
  reactionList: {
    display: 'flex',
    alignItems: 'center',
    gap: tokens.spacingHorizontalS,
    flexWrap: 'wrap',
    minWidth: 0,
  },
  reactionSummaryList: {
    display: 'flex',
    alignItems: 'center',
    gap: tokens.spacingHorizontalXS,
    flexWrap: 'wrap',
    minWidth: 0,
  },
  reactionSummaryItem: {
    display: 'inline-flex',
    alignItems: 'center',
    gap: '0.25rem',
    padding: `0 ${tokens.spacingHorizontalSNudge}`,
    borderRadius: tokens.borderRadiusCircular,
    backgroundColor: tokens.colorNeutralBackground2,
    color: tokens.colorNeutralForeground2,
  },
  reactionButton: {
    minWidth: '5.5rem',
  },
  reactionTray: {
    display: 'flex',
    alignItems: 'center',
    gap: tokens.spacingHorizontalXS,
    padding: tokens.spacingHorizontalXS,
  },
  reactionTrayButton: {
    minWidth: '2.5rem',
  },
  commentButton: {
    minWidth: '5rem',
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
    paddingTop: tokens.spacingVerticalS,
  },
  commentHeader: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    gap: tokens.spacingHorizontalM,
    flexWrap: 'wrap',
  },
  commentHeaderText: {
    display: 'grid',
    gap: tokens.spacingVerticalXXS,
  },
  commentHeaderMeta: {
    display: 'flex',
    alignItems: 'center',
    gap: tokens.spacingHorizontalS,
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
    boxShadow: tokens.shadow2,
  },
  commentCardHeader: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'start',
    gap: tokens.spacingHorizontalM,
    flexWrap: 'wrap',
  },
  commentAuthorBlock: {
    display: 'grid',
    gap: tokens.spacingVerticalXXS,
  },
  commentMetaBlock: {
    display: 'flex',
    alignItems: 'center',
    gap: tokens.spacingHorizontalS,
    flexWrap: 'wrap',
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
    justifyContent: 'flex-end',
    alignItems: 'center',
    gap: tokens.spacingHorizontalM,
    flexWrap: 'wrap',
  },
  statusCard: {
    boxShadow: tokens.shadow8,
  },
  emptyState: {
    display: 'grid',
    gap: tokens.spacingVerticalS,
    justifyItems: 'start',
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

function getReactionButtonIcon(reactionType: string | null) {
  if (!reactionType) {
    return <ThumbLikeRegular />;
  }

  return <span aria-hidden="true">{getReactionIcon(reactionType)}</span>;
}

function getCommentButtonLabel(commentCount: number) {
  return commentCount > 0 ? String(commentCount) : '评论';
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
  const [expandedCommentPostIds, setExpandedCommentPostIds] = useState<Set<number>>(new Set());
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
  const toggleCommentPreview = (postId: number) => {
    setExpandedCommentPostIds((current) => {
      const next = new Set(current);
      if (next.has(postId)) {
        next.delete(postId);
      } else {
        next.add(postId);
      }
      return next;
    });
  };
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
        <div className={styles.heroHeader}>
          <Title2>新鲜事</Title2>
        </div>
        <Body1 className={styles.muted}>分享社区里的近况、观点和瞬间，让讨论和互动自然发生。</Body1>
      </div>

      {profile ? (
        <Card className={styles.composerCard}>
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
              <Divider />
              <div className={styles.composerFooter}>
                <Caption1 className={`${styles.muted} ${styles.composerActionHint}`}>
                  你的动态会展示在公开信息流中。可以记录近况、分享观察，或抛出一个值得讨论的话题。
                </Caption1>
                <div className={styles.actionsEnd}>
                  <Button
                    appearance="subtle"
                    type="button"
                    icon={<DismissRegular />}
                    onClick={() => {
                      setTitle('');
                      setContent('');
                    }}
                  >
                    清空
                  </Button>
                  <Button
                    type="submit"
                    icon={<SlideTextTitleAddRegular />}
                    appearance="primary"
                    disabled={isPublishing || content.trim() === ''}
                  >
                    {isPublishing ? '发布中' : '发布'}
                  </Button>
                </div>
              </div>
            </form>
          </div>
        </Card>
      ) : (
        <Card appearance="filled-alternative" className={styles.composerCard}>
          <div className={styles.cardBody}>
            <Subtitle2>登录后参与互动</Subtitle2>
            <Body1 className={styles.muted}>
              当前未登录。登录后即可发布动态、表达回应，并在详情页参与评论讨论。
            </Body1>
            <div className={styles.row}>
              <Button appearance="primary" onClick={() => navigate('/auth/login')}>
                去登录
              </Button>
              <Button appearance="secondary" onClick={() => navigate('/')}>
                返回首页
              </Button>
            </div>
          </div>
        </Card>
      )}

      <div className={styles.rowBetween}>
        <div className={styles.feedHeaderText}>
          <Subtitle2>最新动态</Subtitle2>
          <Caption1 className={styles.muted}>按时间倒序展示社区里的最新帖子与互动预览。</Caption1>
        </div>
        {isInitialLoading ? <Spinner size="tiny" label="加载中" /> : null}
      </div>

      {deleteErrorMessage ? (
        <MessageBar intent="error">
          <MessageBarBody>
            <MessageBarTitle>删除失败</MessageBarTitle>
            {deleteErrorMessage}
          </MessageBarBody>
        </MessageBar>
      ) : null}

      {editErrorMessage ? (
        <MessageBar intent="error">
          <MessageBarBody>
            <MessageBarTitle>编辑失败</MessageBarTitle>
            {editErrorMessage}
          </MessageBarBody>
        </MessageBar>
      ) : null}

      {reactionErrorMessage ? (
        <MessageBar intent="warning">
          <MessageBarBody>
            <MessageBarTitle>互动未完成</MessageBarTitle>
            {reactionErrorMessage}
          </MessageBarBody>
        </MessageBar>
      ) : null}

      {commentErrorMessage ? (
        <MessageBar intent="warning">
          <MessageBarBody>
            <MessageBarTitle>评论加载受限</MessageBarTitle>
            {commentErrorMessage}
          </MessageBarBody>
        </MessageBar>
      ) : null}

      {errorMessage ? (
        <Card appearance="filled-alternative" className={styles.statusCard}>
          <div className={styles.cardBody}>
            <Subtitle2>动态加载失败</Subtitle2>
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
        <Card appearance="filled-alternative" className={styles.statusCard}>
          <div className={styles.cardBody}>
            <div className={styles.emptyState}>
              <Body1Strong>还没有帖子</Body1Strong>
              <Caption1 className={styles.muted}>
                第一条动态会出现在这里。可以先发一条近况、一个问题，或者一段值得讨论的内容。
              </Caption1>
            </div>
          </div>
        </Card>
      ) : null}

      <div className={styles.postList}>
        {posts.map((post) => {
          const hasTitle = post.title?.trim() != '';
          const authorNickname = post.authorNickname?.trim() || '已注销用户';
          const authorUsername = post.authorUsername?.trim() || '';
          const reactions = sortReactions(post.reactions);
          const visibleReactionSummaries = reactions.filter((reaction) => reaction.count > 0);
          const viewerReaction = post.viewerReaction ?? null;
          const commentCount = (commentsByPostId[post.id] ?? []).length;
          const isCommentPreviewExpanded = expandedCommentPostIds.has(post.id);
          const isAuthor = !Number.isNaN(profileId) && profileId === post.authorId;
          const canEdit = isAuthor;
          const canDelete = Boolean(profile) && (isAdmin || isAuthor);
          const canManagePost = canEdit || canDelete;

          return (
            <Card key={post.id} className={styles.postCard}>
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
                  <div className={styles.footerPrimaryActions}>
                    <div className={styles.reactionList} aria-label="帖子互动统计">
                      <Popover withArrow positioning="below-start">
                        <PopoverTrigger disableButtonEnhancement>
                          <Button
                            appearance="subtle"
                            icon={getReactionButtonIcon(viewerReaction)}
                            className={styles.reactionButton}
                            disabled={false}
                          >
                            回应
                          </Button>
                        </PopoverTrigger>
                        <PopoverSurface>
                          <div className={styles.reactionTray}>
                            {REACTION_OPTIONS.map((reactionType) => {
                              const isSelected = viewerReaction === reactionType;

                              return (
                                <Button
                                  key={reactionType}
                                  appearance={isSelected ? 'primary' : 'subtle'}
                                  size="small"
                                  shape="circular"
                                  className={styles.reactionTrayButton}
                                  aria-label={getReactionLabel(reactionType)}
                                  onClick={() =>
                                    void handleReactionClick(post.id, viewerReaction, reactionType)
                                  }
                                >
                                  <span className={styles.reactionIcon} aria-hidden="true">
                                    {getReactionIcon(reactionType)}
                                  </span>
                                </Button>
                              );
                            })}
                          </div>
                        </PopoverSurface>
                      </Popover>
                    </div>
                    {visibleReactionSummaries.length > 0 ? (
                      <div className={styles.reactionSummaryList}>
                        {visibleReactionSummaries.map((reaction) => (
                          <Caption1
                            key={reaction.reactionType}
                            className={styles.reactionSummaryItem}
                          >
                            <span aria-hidden="true">{getReactionIcon(reaction.reactionType)}</span>
                            <span>{reaction.count}</span>
                          </Caption1>
                        ))}
                      </div>
                    ) : null}
                  </div>
                  <div className={styles.footerSecondaryActions}>
                    <Button
                      appearance="subtle"
                      icon={<CommentRegular />}
                      className={styles.commentButton}
                      onClick={() => toggleCommentPreview(post.id)}
                    >
                      {getCommentButtonLabel(commentCount)}
                    </Button>
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
                          <MenuItem icon={<WarningRegular />}>举报</MenuItem>
                          {canEdit ? (
                            <MenuItem
                              icon={<SlideTextEditRegular />}
                              onClick={() => openEditDialog(post.id, post.title, post.content)}
                              disabled={updatingPostId === post.id}
                            >
                              {updatingPostId === post.id ? '编辑中' : '编辑'}
                            </MenuItem>
                          ) : null}
                          {canManagePost ? (
                            <MenuItem
                              icon={<DeleteRegular />}
                              onClick={() => setConfirmDeletePostId(post.id)}
                              disabled={deletingPostId === post.id}
                            >
                              {deletingPostId === post.id ? '删除中' : '删除'}
                            </MenuItem>
                          ) : null}
                        </MenuList>
                      </MenuPopover>
                    </Menu>
                  </div>
                </CardFooter>
                {isCommentPreviewExpanded ? (
                  <>
                    <Divider />
                    <div className={styles.commentSection}>
                      <div className={styles.commentHeader}>
                        <div className={styles.commentHeaderText}>
                          <Subtitle2>评论</Subtitle2>
                        </div>
                        <div className={styles.commentHeaderMeta}>
                          <Caption1 className={styles.muted}>{commentCount} 条主评论</Caption1>
                        </div>
                      </div>
                      {loadingPosts[post.id] && commentCount === 0 ? (
                        <Spinner size="tiny" label="加载评论" />
                      ) : null}
                      {commentCount > 0 ? (
                        <div className={styles.commentList}>
                          {(commentsByPostId[post.id] ?? []).slice(0, 2).map((comment) => {
                            const authorNickname = comment.authorNickname || '已注销用户';
                            const authorUsername = comment.authorUsername?.trim() || '';

                            return (
                              <div key={comment.id} className={styles.commentItem}>
                                <div className={styles.commentCardHeader}>
                                  <div className={styles.commentAuthorBlock}>
                                    <Body1Strong>{authorNickname}</Body1Strong>
                                    <div className={styles.commentMetaBlock}>
                                      {authorUsername ? (
                                        <Caption1 className={styles.muted}>
                                          @{authorUsername}
                                        </Caption1>
                                      ) : null}
                                      <Caption1 className={styles.muted}>主评论</Caption1>
                                    </div>
                                  </div>
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
                        <Button
                          appearance="subtle"
                          size="small"
                          onClick={() => navigate(`/posts/${post.id}`)}
                        >
                          查看详情
                        </Button>
                      </div>
                    </div>
                  </>
                ) : null}
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
