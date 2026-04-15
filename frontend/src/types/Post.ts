export type PostRecord = {
  id: number;
  authorId: number;
  title: string | null;
  content: string;
  createdAt: string;
  updatedAt: string;
};

export type PostView = {
  id: number;
  authorId: number;
  authorNickname?: string | null;
  authorUsername?: string | null;
  title: string | null;
  content: string;
  reactions?: PostReactionView[];
  viewerReaction?: string | null;
  createdAt: string;
  updatedAt: string;
};

export type PostReactionView = {
  reactionType: string;
  count: number;
};

export type PublishPostRequest = {
  title: string | null;
  content: string;
};

export type RecentPostsResponse = {
  items: PostView[];
  hasMore: boolean;
};

export type EditPostRequest = {
  title: string | null;
  content: string;
};
