import {
  Hamburger,
  makeStyles,
  NavCategory,
  NavCategoryItem,
  NavDivider,
  NavDrawer,
  NavDrawerBody,
  NavDrawerHeader,
  NavItem,
  NavSectionHeader,
  NavSubItem,
  NavSubItemGroup,
  Tooltip,
  tokens,
} from '@fluentui/react-components';

import { useState, type ComponentProps } from 'react';
import { useLocation, useNavigate, Outlet } from 'react-router';
import useAccount from '../../hooks/useAccount';
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

const NAV_WIDTH = '16.25rem';

const useStyles = makeStyles({
  root: {
    overflow: 'hidden',
    display: 'flex',
    height: '100dvh',
  },
  nav: {
    minWidth: NAV_WIDTH,
  },
  content: {
    flex: '1',
    padding: tokens.spacingHorizontalXL,
    display: 'grid',
    justifyContent: 'flex-start',
    alignItems: 'flex-start',
    overflowY: 'auto',
    backgroundColor: tokens.colorNeutralBackground2,
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
  const [isOpen, setIsOpen] = useState(true);
  const styles = useStyles();
  const location = useLocation();
  const navigate = useNavigate();
  const { profile, isLoading, logout } = useAccount();

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
        open={isOpen}
        type="inline"
        className={styles.nav}
      >
        <NavDrawerHeader>
          <Tooltip content="收起" relationship="label">
            <Hamburger onClick={() => setIsOpen(!isOpen)} />
          </Tooltip>
        </NavDrawerHeader>
        <NavDrawerBody className={styles.navBody}>
          <div>
            <AccountNavItem profile={profile} isLoading={isLoading} onLogout={logout} />
            <NavItem icon={<HomeIcon />} value="/">
              首页
            </NavItem>
            <NavSectionHeader>社区</NavSectionHeader>
            <NavItem icon={<SlideTextSparkleIcon />} value="/feed" disabled>
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
            <NavItem icon={<CommentBadgeIcon />} value="/replies" disabled>
              回复我的
            </NavItem>
            <NavItem icon={<ThumbLikeIcon />} value="/likes" disabled>
              收到喜欢
            </NavItem>
            <NavItem icon={<MailInboxAllIcon />} value="/notifications" disabled>
              通知
            </NavItem>
            <NavSectionHeader>管理</NavSectionHeader>
            <NavItem icon={<LayerDiagonalPersonIcon />} value="/management/accounts" disabled>
              用户管理
            </NavItem>
          </div>
          <div>
            <NavDivider />
            <NavItem icon={<SettingsIcon />} value="/settings" disabled>
              设置
            </NavItem>
          </div>
        </NavDrawerBody>
      </NavDrawer>
      <div className={styles.content}>
        <Outlet />
      </div>
    </div>
  );
}
