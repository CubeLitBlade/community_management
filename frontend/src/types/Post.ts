export type Post = {
  id: number;
  authorNickname?: string | null;
  authorUsername?: string | null;
  title: string | null;
  content: string;
  createdAt: string;
  updatedAt: string;
};

export type PublishPostRequest = {
  title: string | null;
  content: string;
};

export type RecentPostsResponse = {
  items: Post[];
  hasMore: boolean;
};
