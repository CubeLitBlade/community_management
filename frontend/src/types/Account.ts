export type Role = 'user' | 'admin' | 'owner';
export type Status = 'normal' | 'suspended' | 'archived';

export type Profile = {
  id: string;
  username: string;
  nickname: string;
  role: Role;
  status: Status;
};

export type LoginRequest = {
  username: string;
  password: string;
};

export type LoginResponse = {
  accessToken: string;
};
