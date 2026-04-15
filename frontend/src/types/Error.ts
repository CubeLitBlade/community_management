export interface ProblemDetail {
  status: number;
  title: string;
  detail: string;
  code: ProblemCode;
  instance?: string;
}

export type ProblemCode =
  | 'INVALID_REQUEST'
  | 'INVALID_TOKEN'
  | 'UNAUTHORIZED'
  | 'FORBIDDEN'
  | 'INPUT_USERNAME_BLANK'
  | 'INPUT_EMAIL_BLANK'
  | 'INPUT_PHONE_BLANK'
  | 'INPUT_PASSWORD_BLANK'
  | 'INPUT_USERNAME_BAD_LENGTH'
  | 'INPUT_PASSWORD_BAD_LENGTH'
  | 'INPUT_PASSWORD_BAD_FORMAT'
  | 'INPUT_EMAIL_BAD_FORMAT'
  | 'INPUT_PHONE_BAD_FORMAT'
  | 'INPUT_NO_CONTACT'
  | 'CONFLICT_USERNAME_EXISTS'
  | 'CONFLICT_EMAIL_EXISTS'
  | 'CONFLICT_PHONE_EXISTS'
  | 'LOGIN_FAILED_SUSPENDED'
  | 'LOGIN_FAILED_ARCHIVED'
  | 'LOGIN_FAILED_INVALID_CREDENTIALS'
  | 'ACCOUNT_STATE_SUSPENDED'
  | 'ACCOUNT_STATE_ARCHIVED'
  | 'ACCOUNT_NOT_FOUND'
  | 'POST_NOT_FOUND'
  | 'POST_FORBIDDEN';

export class BizError extends Error {
  public readonly detail: ProblemDetail;

  constructor(problemDetail: ProblemDetail) {
    super(problemDetail.detail || '请求失败');
    this.name = 'BizError';
    this.detail = problemDetail;

    Object.setPrototypeOf(this, BizError.prototype);
  }
}
