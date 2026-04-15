import type { ProblemCode } from './Error';

export type Role = 'user' | 'admin' | 'owner';
export type Status = 'normal' | 'suspended' | 'archived';

export type Profile = {
  id: string;
  username: string;
  nickname: string;
  role: Role;
  status: Status;
  mustChangePassword: boolean;
};

export type LoginRequest = {
  username: string;
  password: string;
};

export type LoginResponse = {
  mustChangePassword: boolean;
};

export type FieldsCheckRequest = {
  username: string | null;
  email: string | null;
  phone: string | null;
};

export type FieldsCheckResponse = {
  available: boolean;
  reasons: ProblemCode[];
};

export type RegisterRequest = {
  username: string;
  password: string;
  email: string | null;
  phone: string | null;
};
