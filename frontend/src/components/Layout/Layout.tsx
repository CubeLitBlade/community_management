import {
  makeStyles,
  NavCategory,
  NavCategoryItem,
  NavDivider,
  NavDrawer,
  NavDrawerBody,
  NavItem,
  NavSectionHeader,
  NavSubItem,
  NavSubItemGroup,
  Toast,
  ToastBody,
  Toaster,
  ToastTitle,
  tokens,
  useToastController,
} from '@fluentui/react-components';

import { type ComponentProps, useEffect } from 'react';
import { useLocation, useNavigate, Outlet } from 'react-router';
import {
  CalendarMultipleIcon,
  CommentBadgeIcon,
  HomeIcon,
  LayerDiagonalPersonIcon,
  MailInboxAllIcon,
  SettingsIcon,
  SlideTextSparkleIcon,
  ThumbLikeIcon,
} from './icons';
import AccountNavItem from './AccountNavItem';
import useAuth from '../../hooks/useAuth';
import type { NotificationView } from '../../types/Notification';
import { toNotificationItem } from '../Notifications/notificationPresentation';

const NAV_WIDTH = '16.25rem';
const APP_TOASTER_ID = 'app-notifications';

function parseNotificationEvent(data: string) {
  try {
    return JSON.parse(data) as NotificationView;
  } catch {
    return null;
  }
}

const useStyles = makeStyles({
  root: {
    overflow: 'hidden',
    display: 'flex',
    height: '100dvh',
    backgroundColor: tokens.colorNeutralBackground2,
  },
  nav: {
    minWidth: NAV_WIDTH,
  },
  content: {
    flex: '1',
    minWidth: 0,
    display: 'flex',
    flexDirection: 'column',
    overflow: 'hidden',
    backgroundColor: tokens.colorNeutralBackground2,
  },
  contentBody: {
    flex: '1',
    overflowY: 'auto',
    padding: `${tokens.spacingVerticalL} ${tokens.spacingHorizontalXL}`,
    display: 'block',
  },
  navBody: {
    display: 'flex',
    flexDirection: 'column',
    height: '100%',
    justifyContent: 'space-between',
  },
});

type NavSelectHandler = NonNullable<ComponentProps<typeof NavDrawer>['onNavItemSelect']>;

export default function Layout() {
  const styles = useStyles();
  const location = useLocation();
  const navigate = useNavigate();
  const { profile, isLoading } = useAuth();
  const { dispatchToast } = useToastController(APP_TOASTER_ID);

  useEffect(() => {
    if (isLoading || !profile?.mustChangePassword) {
      return;
    }

    if (location.pathname !== '/account/change-password') {
      navigate('/account/change-password', { replace: true });
    }
  }, [isLoading, location.pathname, navigate, profile?.mustChangePassword]);

  useEffect(() => {
    if (isLoading || !profile) {
      return;
    }

    const eventSource = new EventSource('/api/notifications/stream', {
      withCredentials: true,
    });

    const handleNotification = (event: MessageEvent<string>) => {
      const notification = parseNotificationEvent(event.data);

      if (notification) {
        const item = toNotificationItem(notification);

        dispatchToast(
          <Toast>
            <ToastTitle>{item.title}</ToastTitle>
            <ToastBody>{item.summary}</ToastBody>
          </Toast>,
          {
            intent: 'info',
          },
        );
        return;
      }

      dispatchToast(
        <Toast>
          <ToastTitle>新通知</ToastTitle>
          <ToastBody>{event.data || '你收到了一条新的通知。'}</ToastBody>
        </Toast>,
        {
          intent: 'info',
        },
      );
    };

    eventSource.addEventListener('notification', handleNotification);

    return () => {
      eventSource.removeEventListener('notification', handleNotification);
      eventSource.close();
    };
  }, [dispatchToast, isLoading, profile]);

  const handleNavSelect: NavSelectHandler = (_event, data) => {
    if (!data.value) {
      return;
    }

    navigate(data.value);
  };

  return (
    <div className={styles.root}>
      <NavDrawer
        selectedValue={location.pathname}
        onNavItemSelect={handleNavSelect}
        open={true}
        type="inline"
        className={styles.nav}
      >
        <NavDrawerBody className={styles.navBody}>
          <div>
            <AccountNavItem profile={profile} isLoading={isLoading} />
            <NavItem icon={<HomeIcon />} value="/">
              首页
            </NavItem>
            <NavSectionHeader>社区</NavSectionHeader>
            <NavItem icon={<SlideTextSparkleIcon />} value="/feed">
              新鲜事
            </NavItem>
            <NavCategory value="/activities">
              <NavCategoryItem icon={<CalendarMultipleIcon />}>活动</NavCategoryItem>
              <NavSubItemGroup>
                <NavSubItem value="/activities/plaza" disabled>
                  广场
                </NavSubItem>
                <NavSubItem value="/activities/about-me" disabled>
                  我的
                </NavSubItem>
              </NavSubItemGroup>
            </NavCategory>
            <NavSectionHeader>与我相关</NavSectionHeader>
            <NavItem icon={<CommentBadgeIcon />} value="/replies">
              回复我的
            </NavItem>
            <NavItem icon={<ThumbLikeIcon />} value="/likes">
              收到回应
            </NavItem>
            <NavItem icon={<MailInboxAllIcon />} value="/notifications">
              通知
            </NavItem>
            <NavSectionHeader>管理</NavSectionHeader>
            <NavItem icon={<LayerDiagonalPersonIcon />} value="/management/accounts" disabled>
              用户管理
            </NavItem>
          </div>
          <div>
            <NavDivider />
            <NavItem icon={<SettingsIcon />} value="/settings">
              设置
            </NavItem>
          </div>
        </NavDrawerBody>
      </NavDrawer>
      <div className={styles.content}>
        <div className={styles.contentBody}>
          <Outlet />
        </div>
      </div>
      <Toaster toasterId={APP_TOASTER_ID} position="top-end" />
    </div>
  );
}
