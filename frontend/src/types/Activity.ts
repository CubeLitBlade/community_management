export type ActivityStatus = 'pending' | 'approved' | 'rejected' | 'archived';

export type ActivityView = {
  id: number;
  creatorAccountId: number;
  creatorDisplayName: string | null;
  title: string;
  description: string;
  location: string;
  registrationDeadline: string;
  startTime: string;
  endTime: string;
  status: ActivityStatus;
  participantCount: number;
  viewerRegistered: boolean;
  rejectionReason: string | null;
  approvedAt: string | null;
  rejectedAt: string | null;
  createdAt: string;
  updatedAt: string;
};

export type ActivityListResponse = {
  activities: ActivityView[];
};

export type MyActivitiesResponse = {
  created: ActivityView[];
  registered: ActivityView[];
};

export type CreateActivityRequest = {
  title: string;
  description: string;
  location: string;
  registrationDeadline: string;
  startTime: string;
  endTime: string;
};

export type RejectActivityRequest = {
  reason: string;
};
