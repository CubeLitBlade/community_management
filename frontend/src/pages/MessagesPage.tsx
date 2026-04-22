import {
  Avatar,
  Badge,
  Body1,
  Body1Strong,
  Button,
  Card,
  Caption1,
  Dialog,
  DialogSurface,
  Divider,
  Field,
  Input,
  ProgressBar,
  Skeleton,
  SkeletonItem,
  Textarea,
  Title2,
  Toolbar,
  ToolbarButton,
  makeStyles,
  tokens,
} from '@fluentui/react-components';
import { Chat, ChatMessage, ChatMyMessage } from '@fluentui-contrib/react-chat';
import { AddRegular, ArrowClockwiseRegular, SearchRegular, SendRegular } from '@fluentui/react-icons';
import { useCallback, useEffect, useMemo, useState } from 'react';
import type { FormEvent, KeyboardEvent } from 'react';
import { useNavigate } from 'react-router';
import apiClient, { refreshCsrfToken } from '../api/apiClient';
import useAuth from '../hooks/useAuth';
import type { ContactAccount, ContactAccountListResponse } from '../types/Account';
import type {
  PrivateConversation,
  PrivateConversationListResponse,
  PrivateMessage,
  PrivateMessageListResponse,
  SendPrivateMessageRequest,
} from '../types/Message';

const messageTimeFormatter = new Intl.DateTimeFormat('zh-CN', {
  month: '2-digit',
  day: '2-digit',
  hour: '2-digit',
  minute: '2-digit',
});

const useStyles = makeStyles({
  page: {
    width: 'min(100%, 72rem)',
    margin: '0 auto',
    padding: `${tokens.spacingVerticalXL} ${tokens.spacingHorizontalL}`,
    display: 'grid',
    gap: tokens.spacingVerticalXL,
  },
  hero: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'start',
    gap: tokens.spacingHorizontalL,
    flexWrap: 'wrap',
  },
  heroText: {
    display: 'grid',
    gap: tokens.spacingVerticalS,
  },
  muted: {
    color: tokens.colorNeutralForeground2,
  },
  shell: {
    display: 'grid',
    gridTemplateColumns: '20rem minmax(0, 1fr)',
    gap: tokens.spacingHorizontalL,
    alignItems: 'start',
    '@media (max-width: 920px)': {
      gridTemplateColumns: '1fr',
    },
  },
  panel: {
    overflow: 'hidden',
    borderRadius: tokens.borderRadiusXLarge,
    boxShadow: tokens.shadow8,
  },
  panelBody: {
    display: 'grid',
  },
  panelHeader: {
    padding: `${tokens.spacingVerticalS} ${tokens.spacingHorizontalM}`,
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    gap: tokens.spacingHorizontalS,
    borderBottom: `1px solid ${tokens.colorNeutralStroke2}`,
  },
  conversationButton: {
    justifyContent: 'flex-start',
    textAlign: 'left',
    whiteSpace: 'normal',
    height: 'auto',
    borderRadius: 0,
    padding: `${tokens.spacingVerticalM} ${tokens.spacingHorizontalL}`,
  },
  conversationContent: {
    width: '100%',
    display: 'grid',
    gap: tokens.spacingVerticalXS,
  },
  conversationRow: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    gap: tokens.spacingHorizontalS,
  },
  truncate: {
    overflow: 'hidden',
    textOverflow: 'ellipsis',
    whiteSpace: 'nowrap',
  },
  messageHeader: {
    padding: tokens.spacingHorizontalL,
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    gap: tokens.spacingHorizontalM,
    flexWrap: 'wrap',
  },
  messageList: {
    minHeight: '24rem',
    maxHeight: 'calc(100dvh - 24rem)',
    overflowY: 'auto',
    padding: tokens.spacingHorizontalL,
    display: 'grid',
    gap: tokens.spacingVerticalM,
    alignContent: 'start',
  },
  chat: {
    width: '100%',
  },
  composer: {
    padding: tokens.spacingHorizontalL,
    display: 'grid',
    gap: tokens.spacingVerticalM,
  },
  composerActions: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    gap: tokens.spacingHorizontalM,
    flexWrap: 'wrap',
  },
  statusPanel: {
    minHeight: '20rem',
    padding: tokens.spacingHorizontalXL,
    display: 'grid',
    gap: tokens.spacingVerticalM,
    justifyItems: 'start',
    alignContent: 'center',
  },
  skeletonStack: {
    padding: tokens.spacingHorizontalL,
    display: 'grid',
    gap: tokens.spacingVerticalL,
  },
  conversationSkeletonItem: {
    display: 'grid',
    gap: tokens.spacingVerticalS,
  },
  conversationSkeletonRow: {
    display: 'flex',
    justifyContent: 'space-between',
    gap: tokens.spacingHorizontalM,
  },
  messageSkeleton: {
    display: 'grid',
    gap: tokens.spacingVerticalL,
  },
  messageSkeletonMine: {
    justifyItems: 'end',
  },
  pageProgress: {
    width: '100%',
  },
  dialogSurface: {
    width: 'min(38rem, calc(100vw - 2rem))',
    maxWidth: 'min(38rem, calc(100vw - 2rem))',
    height: 'min(38rem, calc(100vh - 4rem))',
    maxHeight: 'calc(100vh - 4rem)',
  },
  dialogFrame: {
    height: '100%',
    display: 'grid',
    gridTemplateRows: 'auto minmax(0, 1fr) auto',
    gap: tokens.spacingVerticalM,
    minHeight: 0,
  },
  dialogHeader: {
    display: 'grid',
    gap: tokens.spacingVerticalXS,
  },
  dialogMain: {
    display: 'grid',
    gap: tokens.spacingVerticalM,
    minWidth: 0,
    minHeight: 0,
    overflow: 'hidden',
    gridTemplateRows: 'auto minmax(0, 1fr)',
  },
  dialogFooter: {
    display: 'flex',
    justifyContent: 'flex-end',
    gap: tokens.spacingHorizontalS,
  },
  contactResults: {
    minHeight: 0,
    maxHeight: '100%',
    overflowY: 'auto',
    display: 'grid',
    alignContent: 'start',
    gap: tokens.spacingVerticalXS,
    padding: `${tokens.spacingVerticalXS} 0`,
  },
  contactResultButton: {
    justifyContent: 'flex-start',
    textAlign: 'left',
    whiteSpace: 'normal',
    height: 'auto',
    padding: `${tokens.spacingVerticalS} ${tokens.spacingHorizontalM}`,
  },
  contactResultContent: {
    width: '100%',
    display: 'flex',
    alignItems: 'center',
    gap: tokens.spacingHorizontalM,
    minWidth: 0,
  },
  contactResultText: {
    display: 'grid',
    gap: tokens.spacingVerticalXXS,
    minWidth: 0,
  },
});

function ConversationListSkeleton() {
  const styles = useStyles();

  return (
    <div className={styles.skeletonStack}>
      {Array.from({ length: 4 }, (_, index) => (
        <div key={index} className={styles.conversationSkeletonItem}>
          <div className={styles.conversationSkeletonRow}>
            <Skeleton>
              <SkeletonItem shape="rectangle" size={16} style={{ width: '8rem' }} />
            </Skeleton>
            <Skeleton>
              <SkeletonItem shape="rectangle" size={12} style={{ width: '3rem' }} />
            </Skeleton>
          </div>
          <Skeleton>
            <SkeletonItem shape="rectangle" size={14} style={{ width: '85%' }} />
          </Skeleton>
          <Skeleton>
            <SkeletonItem shape="rectangle" size={12} style={{ width: '35%' }} />
          </Skeleton>
        </div>
      ))}
    </div>
  );
}

function MessageListSkeleton() {
  const styles = useStyles();

  return (
    <div className={styles.messageSkeleton}>
      {Array.from({ length: 4 }, (_, index) => {
        const mine = index % 2 === 1;
        return (
          <div key={index} className={mine ? styles.messageSkeletonMine : undefined}>
            <Skeleton>
              <SkeletonItem
                shape="rectangle"
                size={48}
                style={{ width: mine ? '14rem' : '18rem' }}
              />
            </Skeleton>
          </div>
        );
      })}
    </div>
  );
}

function displayName(account: Pick<ContactAccount, 'username' | 'nickname'>) {
  return account.nickname?.trim() || account.username;
}

function formatTime(value: string) {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return '';
  }
  return messageTimeFormatter.format(date);
}

export default function MessagesPage() {
  const styles = useStyles();
  const navigate = useNavigate();
  const { profile, isLoading: isAuthLoading } = useAuth();
  const [contacts, setContacts] = useState<ContactAccount[]>([]);
  const [conversations, setConversations] = useState<PrivateConversation[]>([]);
  const [messages, setMessages] = useState<PrivateMessage[]>([]);
  const [selectedContactId, setSelectedContactId] = useState<number | null>(null);
  const [contactQuery, setContactQuery] = useState('');
  const [isNewConversationOpen, setIsNewConversationOpen] = useState(false);
  const [content, setContent] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [isConversationLoading, setIsConversationLoading] = useState(false);
  const [isSending, setIsSending] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const [sendErrorMessage, setSendErrorMessage] = useState('');

  const contactsById = useMemo(() => {
    return new Map(contacts.map((contact) => [contact.id, contact]));
  }, [contacts]);

  const selectedContact = selectedContactId == null ? null : contactsById.get(selectedContactId);
  const selectedConversation = conversations.find(
    (conversation) => conversation.contactAccountId === selectedContactId,
  );
  const activeDisplayName =
    selectedContact != null
      ? displayName(selectedContact)
      : selectedConversation?.contactNickname || selectedConversation?.contactUsername || '';
  const activeUsername = selectedContact?.username || selectedConversation?.contactUsername || '';
  const filteredContacts = useMemo(() => {
    const normalizedQuery = contactQuery.trim().toLowerCase();
    if (!normalizedQuery) {
      return contacts;
    }

    return contacts.filter((contact) => {
      return (
        contact.username.toLowerCase().includes(normalizedQuery) ||
        (contact.nickname?.toLowerCase().includes(normalizedQuery) ?? false)
      );
    });
  }, [contactQuery, contacts]);
  const recentContacts = useMemo(() => {
    return conversations
      .map((conversation) => contactsById.get(conversation.contactAccountId))
      .filter((contact): contact is ContactAccount => contact != null)
      .slice(0, 8);
  }, [contactsById, conversations]);
  const dialogContacts = contactQuery.trim() ? filteredContacts.slice(0, 30) : recentContacts;

  const refreshConversations = useCallback(async () => {
    const response = await apiClient.get<PrivateConversationListResponse>(
      '/messages/conversations',
    );
    setConversations(response.data.conversations);
  }, []);

  useEffect(() => {
    if (isAuthLoading || !profile) {
      return;
    }

    let active = true;
    setIsLoading(true);
    setErrorMessage('');

    Promise.all([
      apiClient.get<ContactAccountListResponse>('/account/contacts'),
      apiClient.get<PrivateConversationListResponse>('/messages/conversations'),
    ])
      .then(([contactsResponse, conversationsResponse]) => {
        if (!active) {
          return;
        }
        setContacts(contactsResponse.data.accounts);
        setConversations(conversationsResponse.data.conversations);
      })
      .catch(() => {
        if (active) {
          setErrorMessage('加载私信失败，请稍后重试。');
        }
      })
      .finally(() => {
        if (active) {
          setIsLoading(false);
        }
      });

    return () => {
      active = false;
    };
  }, [isAuthLoading, profile]);

  useEffect(() => {
    if (selectedContactId == null) {
      setMessages([]);
      return;
    }

    let active = true;
    setIsConversationLoading(true);
    setSendErrorMessage('');

    apiClient
      .get<PrivateMessageListResponse>(`/messages/conversations/${selectedContactId}`)
      .then((response) => {
        if (active) {
          setMessages(response.data.messages);
          void refreshConversations();
        }
      })
      .catch(() => {
        if (active) {
          setSendErrorMessage('加载会话失败，请稍后重试。');
        }
      })
      .finally(() => {
        if (active) {
          setIsConversationLoading(false);
        }
      });

    return () => {
      active = false;
    };
  }, [refreshConversations, selectedContactId]);

  const openConversation = (contactAccountId: number) => {
    setSelectedContactId(contactAccountId);
    setIsNewConversationOpen(false);
    setContactQuery('');
  };

  const handleContactSearchKeyDown = (event: KeyboardEvent<HTMLInputElement>) => {
    if (event.key !== 'Enter' || dialogContacts.length === 0) {
      return;
    }

    event.preventDefault();
    openConversation(dialogContacts[0].id);
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (selectedContactId == null || !content.trim()) {
      return;
    }

    setIsSending(true);
    setSendErrorMessage('');

    try {
      await refreshCsrfToken();
      const request: SendPrivateMessageRequest = {
        recipientAccountId: selectedContactId,
        content,
      };
      await apiClient.post('/messages', request);
      setContent('');
      const [messagesResponse] = await Promise.all([
        apiClient.get<PrivateMessageListResponse>(`/messages/conversations/${selectedContactId}`),
        refreshConversations(),
      ]);
      setMessages(messagesResponse.data.messages);
    } catch {
      setSendErrorMessage('发送失败，请稍后重试。');
    } finally {
      setIsSending(false);
    }
  };

  if (isAuthLoading) {
    return (
      <div className={styles.page}>
        <Card className={styles.panel}>
          <ProgressBar className={styles.pageProgress} />
          <ConversationListSkeleton />
        </Card>
      </div>
    );
  }

  if (!profile) {
    return (
      <div className={styles.page}>
        <Card className={styles.panel}>
          <div className={styles.statusPanel}>
            <Title2>登录后查看私信</Title2>
            <Body1 className={styles.muted}>私信用于和社区成员进行非即时的一对一沟通。</Body1>
            <Button appearance="primary" onClick={() => navigate('/auth/login')}>
              立即登录
            </Button>
          </div>
        </Card>
      </div>
    );
  }

  return (
    <div className={styles.page}>
      <div className={styles.hero}>
        <div className={styles.heroText}>
          <Title2>私信</Title2>
          <Body1 className={styles.muted}>按联系人查看和发送站内私信。</Body1>
          {errorMessage ? <Caption1 className={styles.muted}>{errorMessage}</Caption1> : null}
        </div>
      </div>

      <div className={styles.shell}>
        <Card className={styles.panel}>
          <div className={styles.panelHeader}>
            <Body1Strong>会话</Body1Strong>
            <Toolbar size="small">
              <ToolbarButton
                aria-label="新建会话"
                icon={<AddRegular />}
                onClick={() => {
                  setContactQuery('');
                  setIsNewConversationOpen(true);
                }}
              />
              <ToolbarButton
                aria-label="刷新会话"
                icon={<ArrowClockwiseRegular />}
                onClick={() => void refreshConversations()}
              />
            </Toolbar>
          </div>
          {isLoading ? <ProgressBar className={styles.pageProgress} /> : null}
          <div className={styles.panelBody}>
            {isLoading ? (
              <ConversationListSkeleton />
            ) : conversations.length > 0 ? (
              conversations.map((conversation) => (
                <Button
                  key={conversation.contactAccountId}
                  appearance={
                    conversation.contactAccountId === selectedContactId ? 'secondary' : 'subtle'
                  }
                  className={styles.conversationButton}
                  onClick={() => setSelectedContactId(conversation.contactAccountId)}
                >
                  <div className={styles.conversationContent}>
                    <div className={styles.conversationRow}>
                      <Body1Strong>
                        {conversation.contactNickname || conversation.contactUsername}
                      </Body1Strong>
                      {conversation.unreadCount > 0 ? (
                        <Badge appearance="filled" color="danger">
                          {conversation.unreadCount}
                        </Badge>
                      ) : null}
                    </div>
                    <Caption1 className={`${styles.muted} ${styles.truncate}`}>
                      {conversation.lastSenderAccountId === Number(profile.id) ? '我：' : ''}
                      {conversation.lastMessage}
                    </Caption1>
                    <Caption1 className={styles.muted}>
                      {formatTime(conversation.lastMessageAt)}
                    </Caption1>
                  </div>
                </Button>
              ))
            ) : (
              <div className={styles.statusPanel}>
                <Body1Strong>暂无私信</Body1Strong>
                <Caption1 className={styles.muted}>选择联系人后即可发送第一条私信。</Caption1>
              </div>
            )}
          </div>
        </Card>

        <Card className={styles.panel}>
          {selectedContactId == null ? (
            <div className={styles.statusPanel}>
              <Body1Strong>选择一个会话</Body1Strong>
              <Caption1 className={styles.muted}>从左侧会话列表或联系人选择器开始。</Caption1>
            </div>
          ) : (
            <>
              <div className={styles.messageHeader}>
                <div>
                  <Body1Strong>{activeDisplayName}</Body1Strong>
                  <Caption1 className={styles.muted}> @{activeUsername}</Caption1>
                </div>
                {selectedConversation?.unreadCount ? (
                  <Badge appearance="tint" color="danger">
                    {selectedConversation.unreadCount} 条未读
                  </Badge>
                ) : null}
              </div>
              <Divider />
              {isConversationLoading ? <ProgressBar className={styles.pageProgress} /> : null}
              <div className={styles.messageList}>
                {isConversationLoading ? (
                  <MessageListSkeleton />
                ) : messages.length > 0 ? (
                  <Chat className={styles.chat}>
                    {messages.map((message) => {
                      const mine = message.senderAccountId === Number(profile.id);
                      const timestamp = formatTime(message.createdAt);

                      if (mine) {
                        return (
                          <ChatMyMessage
                            key={message.id}
                            timestamp={timestamp}
                            status={message.isRead ? 'read' : 'received'}
                          >
                            {message.content}
                          </ChatMyMessage>
                        );
                      }

                      return (
                        <ChatMessage
                          key={message.id}
                          author={activeDisplayName}
                          avatar={<Avatar name={activeDisplayName} size={28} />}
                          timestamp={timestamp}
                          persistentTimestamp
                        >
                          {message.content}
                        </ChatMessage>
                      );
                    })}
                  </Chat>
                ) : (
                  <div className={styles.statusPanel}>
                    <Body1Strong>还没有消息</Body1Strong>
                    <Caption1 className={styles.muted}>发送一条私信开始会话。</Caption1>
                  </div>
                )}
              </div>
              <Divider />
              <form className={styles.composer} onSubmit={handleSubmit}>
                <Field label="消息内容">
                  <Textarea
                    value={content}
                    onChange={(_, data) => setContent(data.value)}
                    resize="vertical"
                    placeholder="输入私信内容"
                    maxLength={1000}
                  />
                </Field>
                <div className={styles.composerActions}>
                  <Caption1 className={styles.muted}>
                    {sendErrorMessage || `${content.trim().length}/1000`}
                  </Caption1>
                  <Button
                    type="submit"
                    appearance="primary"
                    icon={<SendRegular />}
                    disabled={isSending || !content.trim()}
                  >
                    发送
                  </Button>
                </div>
              </form>
            </>
          )}
        </Card>
      </div>

      <Dialog
        open={isNewConversationOpen}
        onOpenChange={(_, data) => setIsNewConversationOpen(data.open)}
      >
        <DialogSurface className={styles.dialogSurface}>
          <div className={styles.dialogFrame}>
            <div className={styles.dialogHeader}>
              <Title2>新建会话</Title2>
            </div>
            <div className={styles.dialogMain}>
              <Field label="联系人">
                <Input
                  value={contactQuery}
                  contentBefore={<SearchRegular />}
                  placeholder="搜索姓名或用户名"
                  onChange={(_, data) => setContactQuery(data.value)}
                  onKeyDown={handleContactSearchKeyDown}
                />
              </Field>
              <div className={styles.contactResults}>
                {dialogContacts.length > 0 ? (
                  dialogContacts.map((contact) => (
                    <Button
                      key={contact.id}
                      appearance="subtle"
                      className={styles.contactResultButton}
                      onClick={() => openConversation(contact.id)}
                    >
                      <div className={styles.contactResultContent}>
                        <Avatar name={displayName(contact)} size={32} />
                        <div className={styles.contactResultText}>
                          <Body1Strong className={styles.truncate}>{displayName(contact)}</Body1Strong>
                          <Caption1 className={`${styles.muted} ${styles.truncate}`}>
                            @{contact.username}
                          </Caption1>
                        </div>
                      </div>
                    </Button>
                  ))
                ) : (
                  <Caption1 className={styles.muted}>
                    {contactQuery.trim() ? '没有匹配的联系人。' : '输入姓名或用户名搜索联系人。'}
                  </Caption1>
                )}
              </div>
            </div>
            <div className={styles.dialogFooter}>
              <Button appearance="secondary" onClick={() => setIsNewConversationOpen(false)}>
                取消
              </Button>
            </div>
          </div>
        </DialogSurface>
      </Dialog>
    </div>
  );
}
