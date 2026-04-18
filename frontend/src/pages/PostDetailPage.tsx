import {
  Breadcrumb,
  BreadcrumbDivider,
  BreadcrumbItem,
  Body1,
  Body1Strong,
  Button,
  Caption1,
  Card,
  CardFooter,
  CardHeader,
  Divider,
  Persona,
  Spinner,
  Subtitle2,
  Textarea,
  ToggleButton,
  makeStyles,
  tokens,
} from '@fluentui/react-components';
import { useEffect, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router';
import useAccount from '../hooks/useAccount';
import useComments from '../hooks/useComments';
import usePostDetail from '../hooks/usePostDetail';
import type { CommentView } from '../types/Comment';
import type { PostReactionView } from '../types/Post';
import apiClient from '../api/apiClient';

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
  muted: {
    color: tokens.colorNeutralForeground2,
  },
  cardBody: {
    padding: tokens.spacingHorizontalL,
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
  commentComposer: {
    display: 'grid',
    gap: tokens.spacingVerticalS,
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
  commentActions: {
    display: 'flex',
    alignItems: 'center',
    gap: tokens.spacingHorizontalS,
    flexWrap: 'wrap',
  },
  textAction: {
    minWidth: 'fit-content',
    height: 'auto',
    padding: 0,
    color: tokens.colorBrandForeground1,
    fontWeight: tokens.fontWeightSemibold,
  },
  secondaryTextAction: {
    minWidth: 'fit-content',
    height: 'auto',
    padding: 0,
    color: tokens.colorNeutralForeground2,
  },
  nestedReplies: {
    display: 'grid',
    gap: tokens.spacingVerticalS,
    paddingLeft: tokens.spacingHorizontalL,
    borderLeft: `2px solid ${tokens.colorNeutralStroke2}`,
  },
  nestedRepliesHeader: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: tokens.spacingHorizontalS,
    flexWrap: 'wrap',
  },
  actionsEnd: {
    display: 'flex',
    justifyContent: 'flex-end',
    gap: tokens.spacingHorizontalM,
    flexWrap: 'wrap',
  },
  emptyCommentState: {
    color: tokens.colorNeutralForeground3,
  },
  composerHint: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    gap: tokens.spacingHorizontalS,
    flexWrap: 'wrap',
  },
});

const REACTION_OPTIONS = ['like', 'love', 'laugh', 'sad'] as const;

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

export default function PostDetailPage() {
  const styles = useStyles();
  const navigate = useNavigate();
  const params = useParams();
  const postId = params.postId ? Number(params.postId) : null;
  const { profile } = useAccount();
  const { post, isLoading, errorMessage, refresh, setPost } = usePostDetail(postId);
  const {
    commentsByPostId,
    repliesByCommentId,
    loadingPosts,
    loadingReplies,
    creatingTargetKey,
    errorMessage: commentErrorMessage,
    loadComments,
    loadReplies,
    createComment,
  } = useComments();
  const [commentDraft, setCommentDraft] = useState('');
  const [expandedReplies, setExpandedReplies] = useState<Record<number, boolean>>({});
  const [replyContext, setReplyContext] = useState<{
    mainCommentId: number;
    targetCommentId: number;
    displayName: string | null;
  } | null>(null);
  const [reactionErrorMessage, setReactionErrorMessage] = useState('');
  const composerRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    if (postId !== null && !Number.isNaN(postId)) {
      void loadComments(postId);
    }
  }, [loadComments, postId]);

  useEffect(() => {
    const comments =
      postId === null || Number.isNaN(postId) ? [] : (commentsByPostId[postId] ?? []);

    comments.forEach((comment) => {
      if (!repliesByCommentId[comment.id] && !loadingReplies[comment.id]) {
        void loadReplies(comment.id);
      }
    });
  }, [commentsByPostId, loadReplies, loadingReplies, postId, repliesByCommentId]);

  const handleReactionClick = async (currentReaction: string | null, nextReaction: string) => {
    if (!profile || !post) {
      navigate('/auth/login');
      return;
    }

    const targetReaction = currentReaction === nextReaction ? null : nextReaction;
    const previousReaction = post.viewerReaction ?? null;
    const reactionCountByType = new Map(
      (post.reactions ?? []).map((reaction) => [reaction.reactionType, reaction.count] as const),
    );

    if (previousReaction) {
      const currentCount = reactionCountByType.get(previousReaction) ?? 0;
      if (currentCount <= 1) {
        reactionCountByType.delete(previousReaction);
      } else {
        reactionCountByType.set(previousReaction, currentCount - 1);
      }
    }

    if (targetReaction) {
      reactionCountByType.set(targetReaction, (reactionCountByType.get(targetReaction) ?? 0) + 1);
    }

    setReactionErrorMessage('');
    setPost((current) =>
      current
        ? {
            ...current,
            viewerReaction: targetReaction,
            reactions: Array.from(reactionCountByType.entries()).map(([reactionType, count]) => ({
              reactionType,
              count,
            })),
          }
        : current,
    );

    try {
      const response = await apiClient.post('/reactions', {
        targetType: 'post',
        targetId: post.id,
        reactionType: targetReaction,
      });

      if (response.status !== 201 && response.status !== 204) {
        throw new Error('Unexpected response status');
      }
    } catch {
      setReactionErrorMessage('互动失败，请稍后重试。');
      await refresh();
    }
  };

  const handleCommentSubmit = async () => {
    if (!profile || postId === null || Number.isNaN(postId)) {
      navigate('/auth/login');
      return;
    }

    const normalizedContent = commentDraft.trim();
    if (!normalizedContent) {
      return;
    }

    const success = await createComment(
      replyContext
        ? {
            targetType: 'comment',
            targetId: replyContext.mainCommentId,
            parentId: replyContext.targetCommentId,
            content: normalizedContent,
          }
        : {
            targetType: 'post',
            targetId: postId,
            parentId: null,
            content: normalizedContent,
          },
    );

    if (success) {
      setCommentDraft('');
      setReplyContext(null);
    }
  };

  const toggleReplies = async (commentId: number) => {
    const nextExpanded = !expandedReplies[commentId];
    setExpandedReplies((current) => ({ ...current, [commentId]: nextExpanded }));

    if (nextExpanded && !repliesByCommentId[commentId] && !loadingReplies[commentId]) {
      await loadReplies(commentId);
    }
  };

  const openReplyEditorForComment = (mainComment: CommentView, target: CommentView) => {
    if (!profile) {
      navigate('/auth/login');
      return;
    }

    setExpandedReplies((current) => ({ ...current, [mainComment.id]: true }));
    setReplyContext({
      mainCommentId: mainComment.id,
      targetCommentId: target.id,
      displayName: target.authorUsername || target.authorNickname || null,
    });

    requestAnimationFrame(() => {
      composerRef.current?.scrollIntoView({ behavior: 'smooth', block: 'center' });
      const textarea = composerRef.current?.querySelector('textarea');
      if (textarea instanceof HTMLTextAreaElement) {
        textarea.focus();
      }
    });
  };

  const renderReplyItem = (mainComment: CommentView, reply: CommentView) => {
    const authorNickname = reply.authorNickname || '已注销用户';
    const authorUsername = reply.authorUsername?.trim() || '';
    const replyPrefix = reply.replyToUsername ? `回复 @${reply.replyToUsername}：` : '';

    return (
      <div key={reply.id} className={styles.commentItem}>
        <div className={styles.commentMeta}>
          <Body1Strong>{authorNickname}</Body1Strong>
          {authorUsername ? <Caption1 className={styles.muted}>@{authorUsername}</Caption1> : null}
          <Caption1 className={styles.muted}>
            {getPostTimeLabel(reply.createdAt, reply.updatedAt)}
          </Caption1>
        </div>
        <Body1 className={styles.commentContent}>
          {replyPrefix ? <Body1Strong>{replyPrefix}</Body1Strong> : null}
          {reply.content}
        </Body1>
        <div className={styles.commentActions}>
          <Button
            appearance="subtle"
            size="small"
            className={styles.textAction}
            onClick={() => openReplyEditorForComment(mainComment, reply)}
          >
            回复
          </Button>
        </div>
      </div>
    );
  };

  const renderCommentItem = (comment: CommentView) => {
    const authorNickname = comment.authorNickname || '已注销用户';
    const authorUsername = comment.authorUsername?.trim() || '';
    const replies = repliesByCommentId[comment.id] ?? [];
    const isRepliesExpanded = Boolean(expandedReplies[comment.id]);
    const replyActionLabel = replies.length > 0 ? `回复 (${replies.length})` : '回复';

    return (
      <div key={comment.id} className={styles.commentItem}>
        <div className={styles.commentMeta}>
          <Body1Strong>{authorNickname}</Body1Strong>
          {authorUsername ? <Caption1 className={styles.muted}>@{authorUsername}</Caption1> : null}
          <Caption1 className={styles.muted}>
            {getPostTimeLabel(comment.createdAt, comment.updatedAt)}
          </Caption1>
        </div>
        <Body1 className={styles.commentContent}>{comment.content}</Body1>
        <div className={styles.commentActions}>
          <Button
            appearance="subtle"
            size="small"
            className={styles.textAction}
            onClick={() => openReplyEditorForComment(comment, comment)}
          >
            {replyActionLabel}
          </Button>
        </div>
        {isRepliesExpanded && loadingReplies[comment.id] && replies.length === 0 ? (
          <div className={styles.nestedReplies}>
            <Spinner size="tiny" label="加载回复" />
          </div>
        ) : null}
        {isRepliesExpanded && replies.length > 0 ? (
          <div className={styles.nestedReplies}>
            <div className={styles.nestedRepliesHeader}>
              <Caption1 className={styles.muted}>{replies.length} 条回复</Caption1>
              <Button
                appearance="subtle"
                size="small"
                className={styles.secondaryTextAction}
                onClick={() => void toggleReplies(comment.id)}
                disabled={loadingReplies[comment.id]}
              >
                收起
              </Button>
            </div>
            {replies.map((reply) => renderReplyItem(comment, reply))}
          </div>
        ) : null}
      </div>
    );
  };

  if (isLoading) {
    return (
      <div className={styles.page}>
        <Spinner label="加载帖子详情" />
      </div>
    );
  }

  if (!post) {
    return (
      <div className={styles.page}>
        <Card>
          <div className={styles.cardBody}>
            <Body1 className={styles.muted}>{errorMessage || '帖子不存在。'}</Body1>
            <div className={styles.actionsEnd}>
              <Button appearance="secondary" onClick={() => navigate('/feed')}>
                返回信息流
              </Button>
            </div>
          </div>
        </Card>
      </div>
    );
  }

  const authorNickname = post.authorNickname?.trim() || '已注销用户';
  const authorUsername = post.authorUsername?.trim() || '';
  const hasTitle = post.title?.trim() !== '';
  const reactions = sortReactions(post.reactions);
  const reactionCountByType = new Map(
    reactions.map((reaction) => [reaction.reactionType, reaction.count]),
  );
  const viewerReaction = post.viewerReaction ?? null;
  const comments = postId === null || Number.isNaN(postId) ? [] : (commentsByPostId[postId] ?? []);

  return (
    <div className={styles.page}>
      <Breadcrumb className={styles.breadcrumb}>
        <BreadcrumbItem>
          <Button onClick={() => navigate('/feed')} appearance="subtle" size="small">
            新鲜事
          </Button>
        </BreadcrumbItem>
        <BreadcrumbDivider />
        <BreadcrumbItem>
          <Button className={styles.breadcrumbCurrent} appearance="subtle" size="small" disabled>
            帖子详情
          </Button>
        </BreadcrumbItem>
      </Breadcrumb>

      {reactionErrorMessage ? (
        <Caption1 className={styles.muted}>{reactionErrorMessage}</Caption1>
      ) : null}
      {commentErrorMessage ? (
        <Caption1 className={styles.muted}>{commentErrorMessage}</Caption1>
      ) : null}

      <Card>
        <div className={styles.cardBody}>
          <CardHeader
            image={
              <Persona
                name={authorNickname}
                secondaryText={authorUsername ? `@${authorUsername}` : undefined}
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
                      onClick={() => void handleReactionClick(viewerReaction, reactionType)}
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
          </CardFooter>
          <Divider />
          <div className={styles.commentSection} ref={composerRef}>
            <div className={styles.commentHeader}>
              <Subtitle2>评论</Subtitle2>
              <Caption1 className={styles.muted}>{comments.length} 条主评论</Caption1>
            </div>
            {profile ? (
              <div className={styles.commentComposer}>
                {replyContext ? (
                  <div className={styles.composerHint}>
                    <Caption1 className={styles.muted}>
                      正在回复 @{replyContext.displayName || '这条评论'}
                    </Caption1>
                    <Button
                      appearance="subtle"
                      size="small"
                      className={styles.secondaryTextAction}
                      onClick={() => setReplyContext(null)}
                    >
                      取消回复
                    </Button>
                  </div>
                ) : null}
                <Textarea
                  value={commentDraft}
                  onChange={(_event, data) => setCommentDraft(data.value)}
                  placeholder={replyContext ? '写下你的回复……' : '写下你的评论……'}
                  resize="vertical"
                />
                <div className={styles.actionsEnd}>
                  <Button
                    appearance="primary"
                    size="small"
                    disabled={
                      creatingTargetKey ===
                        (replyContext
                          ? `comment:${replyContext.mainCommentId}`
                          : `post:${post.id}`) || commentDraft.trim() === ''
                    }
                    onClick={() => void handleCommentSubmit()}
                  >
                    {creatingTargetKey ===
                    (replyContext ? `comment:${replyContext.mainCommentId}` : `post:${post.id}`)
                      ? '发送中'
                      : replyContext
                        ? '发送回复'
                        : '发表评论'}
                  </Button>
                </div>
              </div>
            ) : (
              <Caption1 className={styles.muted}>登录后即可参与评论和楼中楼回复。</Caption1>
            )}
            {loadingPosts[post.id] && comments.length === 0 ? (
              <Spinner size="tiny" label="加载评论" />
            ) : null}
            {comments.length > 0 ? (
              <div className={styles.commentList}>{comments.map(renderCommentItem)}</div>
            ) : !loadingPosts[post.id] ? (
              <Caption1 className={styles.emptyCommentState}>还没有评论，来留下第一条吧。</Caption1>
            ) : null}
          </div>
        </div>
      </Card>
    </div>
  );
}
