import {
  AppItem,
  Card,
  Popover,
  PopoverTrigger,
  PopoverSurface,
  Button,
  Body1Strong,
  Persona,
  Caption1,
  makeStyles,
  tokens,
  Avatar,
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
    padding: '0',
    backgroundColor: 'transparent',
    border: 'none',
    boxShadow: 'none',
  },
  panel: {
    width: AUTH_PANEL_WIDTH,
    overflow: 'hidden',
    borderRadius: tokens.borderRadiusXLarge,
    border: `1px solid ${tokens.colorNeutralStroke1}`,
    backgroundColor: tokens.colorNeutralBackground1,
    boxShadow: tokens.shadow8,
    padding: tokens.spacingHorizontalM,
  },
  topRow: {
    display: 'flex',
    alignItems: 'flex-start',
    justifyContent: 'space-between',
    gap: tokens.spacingHorizontalM,
  },
  topActions: {
    display: 'flex',
    flexDirection: 'column' as const,
    alignItems: 'flex-end',
    gap: tokens.spacingVerticalXS,
  },
  persona: {
    minWidth: 0,
  },
  statusBadge: {
    flexShrink: 0,
  },
  logoutButton: {
    minWidth: 'auto',
    paddingLeft: tokens.spacingHorizontalXS,
    paddingRight: tokens.spacingHorizontalXS,
  },
  statsRow: {
    marginTop: tokens.spacingVerticalM,
    display: 'grid',
    gridTemplateColumns: 'repeat(3, minmax(0, 1fr))',
    gap: tokens.spacingHorizontalSNudge,
  },
  statItem: {
    padding: `${tokens.spacingVerticalSNudge} ${tokens.spacingHorizontalSNudge}`,
    borderRadius: tokens.borderRadiusLarge,
    backgroundColor: tokens.colorNeutralBackground2,
    textAlign: 'center' as const,
    display: 'flex',
    flexDirection: 'column' as const,
    gap: tokens.spacingVerticalXXS,
  },
  statValue: {
    color: tokens.colorNeutralForeground1,
    lineHeight: tokens.lineHeightBase300,
  },
  statLabel: {
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
          <div className={classes.topRow}>
            <Persona
              className={classes.persona}
              textAlignment="center"
              name={profile.nickname}
              secondaryText={`@${profile.username}`}
              tertiaryText={roleLabel(profile.role)}
              presence={{ status: 'available' }}
              size="medium"
            />

            <div className={classes.topActions}>
              <Button
                appearance="transparent"
                size="small"
                className={classes.logoutButton}
                icon={<SignOutRegular />}
                onClick={() => {
                  setOpen(false);
                  onLogout();
                }}
              >
                注销
              </Button>
            </div>
          </div>

          <div className={classes.statsRow} aria-label="账户统计占位信息">
            <div className={classes.statItem}>
              <Caption1 className={classes.statLabel}>发帖</Caption1>
              <Body1Strong className={classes.statValue}>--</Body1Strong>
            </div>
            <div className={classes.statItem}>
              <Caption1 className={classes.statLabel}>活动</Caption1>
              <Body1Strong className={classes.statValue}>--</Body1Strong>
            </div>
            <div className={classes.statItem}>
              <Caption1 className={classes.statLabel}>获赞</Caption1>
              <Body1Strong className={classes.statValue}>--</Body1Strong>
            </div>
          </div>
        </Card>
      </PopoverSurface>
    </Popover>
  );
}
