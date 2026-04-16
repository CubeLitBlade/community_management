import NotificationPageBase from '../components/Notifications/NotificationPageBase';

export default function RepliesPage() {
  return (
    <NotificationPageBase
      title="回复我的"
      description="查看别人对你内容的回复。"
      scope="replies"
      kind="replies"
    />
  );
}
