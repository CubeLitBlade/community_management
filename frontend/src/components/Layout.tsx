import {
  AppItem,
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
} from '@fluentui/react-components';

import {
  bundleIcon,
  CalendarMultiple20Filled,
  CalendarMultiple20Regular,
  CommentBadge20Filled,
  CommentBadge20Regular,
  Home20Filled,
  Home20Regular,
  LayerDiagonalPerson20Filled,
  LayerDiagonalPerson20Regular,
  MailInboxAll20Filled,
  MailInboxAll20Regular,
  PersonCircle32Regular,
  Settings20Filled,
  Settings20Regular,
  SlideTextSparkle20Filled,
  SlideTextSparkle20Regular,
  ThumbLike20Filled,
  ThumbLike20Regular,
} from '@fluentui/react-icons';

import { useState, type ComponentProps } from 'react';
import { useLocation, useNavigate, Outlet } from 'react-router';
import useAuth from '../hooks/useAuth';

const useStyles = makeStyles({
  root: {
    overflow: 'hidden',
    display: 'flex',
    height: '100vh',
  },
  nav: {
    minWidth: '260px',
  },
  content: {
    flex: '1',
    padding: '16px',
    display: 'grid',
    justifyContent: 'flex-start',
    alignItems: 'flex-start',
    overflowY: 'auto',
  },
  navBody: {
    display: 'flex',
    flexDirection: 'column',
    height: '100%',
    justifyContent: 'space-between',
  },
});

const Home = bundleIcon(Home20Filled, Home20Regular);
const SlideTextSparkle = bundleIcon(SlideTextSparkle20Filled, SlideTextSparkle20Regular);
const CalendarMultiple = bundleIcon(CalendarMultiple20Filled, CalendarMultiple20Regular);
const CommentBadge = bundleIcon(CommentBadge20Filled, CommentBadge20Regular);
const ThumbLike = bundleIcon(ThumbLike20Filled, ThumbLike20Regular);
const MailInBoxAll = bundleIcon(MailInboxAll20Filled, MailInboxAll20Regular);
const LayerDiagonalPerson = bundleIcon(LayerDiagonalPerson20Filled, LayerDiagonalPerson20Regular);
const Settings = bundleIcon(Settings20Filled, Settings20Regular);

type NavSelectHandler = NonNullable<ComponentProps<typeof NavDrawer>['onNavItemSelect']>;

export default function Layout() {
  const [isOpen, setIsOpen] = useState(true);
  const styles = useStyles();
  const location = useLocation();
  const navigate = useNavigate();
  const { authStatus } = useAuth();

  const handleAuthClick = () => {
    if (authStatus === 'checking') {
      return;
    }

    navigate(authStatus === 'authenticated' ? '/' : '/auth/login'); // TODO: navigate to "/profile" if authenticated
  };

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
            <AppItem
              icon={<PersonCircle32Regular />}
              as="a"
              href="/auth/login"
              onClick={(e) => {
                e.preventDefault();
                handleAuthClick();
              }}
            >
              {authStatus === 'authenticated' ? '个人中心' : '登录/注册'}{' '}
              {/* TODO:  display nickname here*/}
            </AppItem>
            <NavItem icon={<Home />} value="/">
              首页
            </NavItem>
            <NavSectionHeader>社区</NavSectionHeader>
            <NavItem icon={<SlideTextSparkle />} value="/feed" disabled>
              新鲜事
            </NavItem>
            <NavCategory value="/activities">
              <NavCategoryItem icon={<CalendarMultiple />}>活动</NavCategoryItem>
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
            <NavItem icon={<CommentBadge />} value="/replies" disabled>
              回复我的
            </NavItem>
            <NavItem icon={<ThumbLike />} value="/likes" disabled>
              收到喜欢
            </NavItem>
            <NavItem icon={<MailInBoxAll />} value="/notifications" disabled>
              通知
            </NavItem>
            <NavSectionHeader>管理</NavSectionHeader>
            <NavItem icon={<LayerDiagonalPerson />} value="/management/accounts" disabled>
              用户管理
            </NavItem>
          </div>
          <div>
            <NavDivider />
            <NavItem icon={<Settings />} value="/settings" disabled>
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
