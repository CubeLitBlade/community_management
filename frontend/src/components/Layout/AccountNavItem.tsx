import {
  AppItem,
  Avatar,
  Body1Strong,
  Caption1,
  Card,
  CardHeader,
  Divider,
  MenuItem,
  MenuList,
  Popover,
  PopoverSurface,
  PopoverTrigger,
  makeStyles,
  tokens,
} from '@fluentui/react-components';
import { SignOutRegular } from '@fluentui/react-icons';
import { useState, type MouseEvent } from 'react';
import { useNavigate } from 'react-router';
import type { Profile } from '../../types/Account';

const AUTH_PANEL_WIDTH = '17.5rem';
const POPOVER_OFFSET = 8;
const POPOVER_CROSS_AXIS_OFFSET = 16;

type AccountNavItemProps = {
  profile: Profile | null;
  isLoading: boolean;
  onLogout: () => void;
};

const useClasses = makeStyles({
  surface: {
    padding: 0,
  },
  panel: {
    width: AUTH_PANEL_WIDTH,
    padding: 0,
  },
  header: {
    padding: tokens.spacingHorizontalM,
  },
  headerMeta: {
    display: 'grid',
    gap: tokens.spacingVerticalXXS,
  },
  menu: {
    padding: tokens.spacingHorizontalXS,
  },
  roleText: {
    color: tokens.colorNeutralForeground3,
  },
});

const roleLabel = (role: Profile['role']) => {
  return role === 'admin' ? '管理员' : role === 'owner' ? '站长' : '普通用户';
};

export default function AccountNavItem({ profile, isLoading, onLogout }: AccountNavItemProps) {
  const navigate = useNavigate();
  const classes = useClasses();

  const [open, setOpen] = useState(false);
  const isPopoverOpen = Boolean(profile) && open;

  const handleGuestClick = (e: MouseEvent<HTMLAnchorElement>) => {
    e.preventDefault();

    if (!isLoading) {
      navigate('/auth/login');
    }
  };

  if (!profile) {
    return (
      <AppItem as="a" icon={<Avatar />} onClick={handleGuestClick}>
        登录/注册
      </AppItem>
    );
  }

  return (
    <Popover
      open={isPopoverOpen}
      onOpenChange={(_, data) => {
        setOpen(data.open);
      }}
      positioning={{
        position: 'after',
        align: 'start',
        offset: { mainAxis: POPOVER_OFFSET, crossAxis: POPOVER_CROSS_AXIS_OFFSET },
      }}
    >
      <PopoverTrigger disableButtonEnhancement>
        <AppItem as="a" icon={<Avatar name={profile.nickname} />}>
          {profile.nickname}
        </AppItem>
      </PopoverTrigger>

      <PopoverSurface className={classes.surface}>
        <Card className={classes.panel}>
          <div className={classes.header}>
            <CardHeader
              image={<Avatar name={profile.nickname} size={36} />}
              header={<Body1Strong>{profile.nickname}</Body1Strong>}
              description={
                <div className={classes.headerMeta}>
                  <Caption1>@{profile.username}</Caption1>
                  <Caption1 className={classes.roleText}>{roleLabel(profile.role)}</Caption1>
                </div>
              }
            />
          </div>
          <Divider />
          <MenuList className={classes.menu}>
            <MenuItem
              onClick={() => {
                setOpen(false);
                navigate('/');
              }}
            >
              个人中心
            </MenuItem>
            <MenuItem
              icon={<SignOutRegular />}
              onClick={() => {
                setOpen(false);
                onLogout();
              }}
            >
              退出登录
            </MenuItem>
          </MenuList>
        </Card>
      </PopoverSurface>
    </Popover>
  );
}
