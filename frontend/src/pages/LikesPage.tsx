import NotificationPageBase from '../components/Notifications/NotificationPageBase';

export default function LikesPage() {
  return (
    <NotificationPageBase
      title="收到回应"
      description="查看别人给你内容的回应和互动反馈。"
      scope="reactions"
      kind="likes"
    />
  );
}
