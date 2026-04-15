import { AppItem, Persona } from '@fluentui/react-components';
import { type MouseEvent } from 'react';
import { useNavigate } from 'react-router';
import type { Profile } from '../../types/Account';

type AccountNavItemProps = {
  profile: Profile | null;
  isLoading: boolean;
};

export default function AccountNavItem({ profile, isLoading }: AccountNavItemProps) {
  const navigate = useNavigate();

  const handleGuestClick = (e: MouseEvent<HTMLAnchorElement>) => {
    e.preventDefault();

    if (!isLoading) {
      navigate('/auth/login');
    }
  };

  if (!profile) {
    return (
      <AppItem as="a" onClick={handleGuestClick}>
        <Persona />
        登录/注册
      </AppItem>
    );
  }

  return (
    <AppItem
      as="a"
      onClick={(event: MouseEvent<HTMLAnchorElement>) => {
        event.preventDefault();
      }}
    >
      <Persona
        textAlignment="center"
        name={profile.nickname}
        secondaryText={'@' + profile.username}
        presence={{ status: 'available' }}
      />
    </AppItem>
  );
}
