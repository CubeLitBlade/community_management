export type PrivateConversation = {
  contactAccountId: number;
  contactUsername: string;
  contactNickname: string | null;
  lastMessage: string;
  lastSenderAccountId: number;
  lastMessageAt: string;
  unreadCount: number;
};

export type PrivateConversationListResponse = {
  conversations: PrivateConversation[];
};

export type PrivateMessage = {
  id: number;
  senderAccountId: number;
  recipientAccountId: number;
  content: string;
  isRead: boolean;
  readAt: string | null;
  createdAt: string;
};

export type PrivateMessageListResponse = {
  messages: PrivateMessage[];
};

export type SendPrivateMessageRequest = {
  recipientAccountId: number;
  content: string;
};
