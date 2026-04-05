export interface ProblemDetail {
  status: number;
  title: string;
  detail: string;
  code: ProblemCode;
  instance: string;
}

export type ProblemCode =
  | 'INVALID_CREDENTIALS'
  | 'ACCOUNT_ARCHIVED'
  | 'ACCOUNT_SUSPENDED'
  | 'USERNAME_ALREADY_EXISTS';

export class BizError extends Error {
  public readonly detail: ProblemDetail;

  constructor(problemDetail: ProblemDetail) {
    super(problemDetail.detail || '请求失败');
    this.name = 'BizError';
    this.detail = problemDetail;

    Object.setPrototypeOf(this, BizError.prototype);
  }
}
