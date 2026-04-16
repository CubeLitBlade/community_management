import NotificationPageBase from '../components/Notifications/NotificationPageBase';

export default function NotificationsPage() {
  return (
    <NotificationPageBase
      title="通知"
      description="查看社区提醒和后续系统通知。"
      scope="notifications"
      kind="notifications"
    />
  );
}
