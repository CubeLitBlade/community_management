export type CommentView = {
  id: number;
  targetType: 'post' | 'comment' | 'activity';
  targetId: number;
  accountId: number;
  authorUsername?: string | null;
  authorNickname?: string | null;
  parentId?: number | null;
  replyToAccountId?: number | null;
  replyToUsername?: string | null;
  replyToNickname?: string | null;
  content: string;
  createdAt: string;
  updatedAt: string;
};

export type CommentListResponse = {
  items: CommentView[];
};

export type CreateCommentRequest = {
  targetType: CommentView['targetType'];
  targetId: number;
  parentId: number | null;
  content: string;
};
