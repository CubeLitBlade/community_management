import {
  Body1,
  Body1Strong,
  Button,
  Caption1,
  Card,
  DataGrid,
  DataGridBody,
  DataGridCell,
  DataGridHeader,
  DataGridHeaderCell,
  DataGridRow,
  Dialog,
  DialogActions,
  DialogBody,
  DialogContent,
  DialogSurface,
  DialogTitle,
  Field,
  Input,
  MessageBar,
  MessageBarBody,
  MessageBarTitle,
  ProgressBar,
  Spinner,
  Subtitle2,
  Title2,
  createTableColumn,
  makeStyles,
  tokens,
} from '@fluentui/react-components';
import {
  KeyRegular,
  PersonArrowBackRegular,
  PersonAvailableRegular,
  PersonDeleteRegular,
  PersonProhibitedRegular,
  PersonRegular,
  ShieldKeyholeRegular,
  ShieldPersonAddRegular,
  ShieldProhibitedRegular,
  ShieldPersonRegular,
  ArrowSyncRegular,
  KeyResetRegular,
} from '@fluentui/react-icons';
import { useMemo, useState } from 'react';
import { useNavigate } from 'react-router';
import useAuth from '../hooks/useAuth';
import useAccountManagement from '../hooks/useAccountManagement';
import type { ManagedAccount, Role, Status } from '../types/Account';

const useStyles = makeStyles({
  page: {
    width: 'min(100%, 56rem)',
    margin: '0 auto',
    padding: `${tokens.spacingVerticalXXL} ${tokens.spacingHorizontalL}`,
    display: 'grid',
    gap: tokens.spacingVerticalXL,
  },
  hero: {
    display: 'grid',
    gap: tokens.spacingVerticalS,
  },
  heroHeader: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'start',
    gap: tokens.spacingHorizontalM,
    flexWrap: 'wrap',
  },
  muted: {
    color: tokens.colorNeutralForeground2,
  },
  list: {
    display: 'grid',
    gap: tokens.spacingVerticalL,
  },
  selectionBar: {
    display: 'grid',
    gap: tokens.spacingVerticalM,
    padding: tokens.spacingHorizontalL,
  },
  selectionSummary: {
    display: 'grid',
    gap: tokens.spacingVerticalXXS,
    minWidth: 0,
  },
  selectionActions: {
    display: 'flex',
    alignItems: 'center',
    gap: tokens.spacingHorizontalS,
    flexWrap: 'wrap',
  },
  tableCard: {
    overflow: 'hidden',
    boxShadow: tokens.shadow8,
  },
  listHeader: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'end',
    gap: tokens.spacingHorizontalM,
    flexWrap: 'wrap',
  },
  listHeaderText: {
    display: 'grid',
    gap: tokens.spacingVerticalXXS,
  },
  row: {
    display: 'flex',
    alignItems: 'center',
    gap: tokens.spacingHorizontalS,
    flexWrap: 'wrap',
  },
  cardBody: {
    display: 'grid',
    gap: tokens.spacingVerticalL,
    padding: tokens.spacingHorizontalL,
  },
  fallbackCard: {
    padding: tokens.spacingHorizontalXL,
    display: 'grid',
    gap: tokens.spacingVerticalM,
  },
  dialogContent: {
    display: 'grid',
    gap: tokens.spacingVerticalM,
  },
  tableScroll: {
    overflowX: 'auto',
  },
  dataGrid: {
    minWidth: '52rem',
  },
  headerCell: {
    color: tokens.colorNeutralForeground3,
    whiteSpace: 'nowrap',
  },
  selectionCell: {
    width: '3rem',
  },
  accountPrimary: {
    display: 'grid',
    gap: tokens.spacingVerticalXXS,
    minWidth: 0,
  },
  accountName: {
    color: tokens.colorNeutralForeground1,
    minWidth: 0,
    overflow: 'hidden',
    textOverflow: 'ellipsis',
    whiteSpace: 'nowrap',
  },
  accountSecondary: {
    color: tokens.colorNeutralForeground2,
    minWidth: 0,
    overflow: 'hidden',
    textOverflow: 'ellipsis',
    whiteSpace: 'nowrap',
  },
  metaCell: {
    display: 'flex',
    alignItems: 'center',
    gap: tokens.spacingHorizontalS,
    minWidth: '9rem',
    whiteSpace: 'nowrap',
  },
  metaIcon: {
    flexShrink: 0,
  },
  policyCell: {
    display: 'flex',
    alignItems: 'center',
    gap: tokens.spacingHorizontalS,
    minWidth: 0,
  },
  policyText: {
    color: tokens.colorNeutralForeground2,
    lineHeight: tokens.lineHeightBase300,
    minWidth: 0,
    overflow: 'hidden',
    textOverflow: 'ellipsis',
    whiteSpace: 'nowrap',
  },
  idText: {
    fontVariantNumeric: 'tabular-nums',
    whiteSpace: 'nowrap',
  },
  progress: {
    width: '100%',
  },
});

const roleLabels: Record<Role, string> = {
  user: '普通用户',
  admin: '社区管理员',
  owner: '系统管理员',
};

function getRoleIcon(role: Role) {
  switch (role) {
    case 'user':
      return <PersonRegular />;
    case 'admin':
      return <ShieldPersonRegular />;
    case 'owner':
      return <ShieldKeyholeRegular />;
  }
}

const statusLabels: Record<Status, string> = {
  normal: '正常',
  suspended: '已停用',
  archived: '已归档',
};

function getSatatusIcon(status: Status) {
  switch (status) {
    case 'normal':
      return <PersonAvailableRegular />;
    case 'suspended':
      return <PersonProhibitedRegular />;
    case 'archived':
      return <PersonDeleteRegular />;
  }
}

function canManage(currentRole: Role, currentId: number, target: ManagedAccount) {
  if (target.id === currentId || target.role === 'owner') {
    return false;
  }
  if (currentRole === 'owner') {
    return target.role === 'user' || target.role === 'admin';
  }
  return currentRole === 'admin' && target.role === 'user';
}

function canPromote(currentRole: Role, currentId: number, target: ManagedAccount) {
  return (
    currentRole === 'owner' &&
    target.id !== currentId &&
    target.role === 'user' &&
    target.status !== 'archived'
  );
}

function canDemote(currentRole: Role, currentId: number, target: ManagedAccount) {
  return (
    currentRole === 'owner' &&
    target.id !== currentId &&
    target.role === 'admin' &&
    target.status !== 'archived'
  );
}

function canReactivate(currentRole: Role, currentId: number, target: ManagedAccount) {
  return canManage(currentRole, currentId, target) && target.status === 'suspended';
}

function canArchive(currentRole: Role, currentId: number, target: ManagedAccount) {
  return canManage(currentRole, currentId, target) && target.status !== 'archived';
}

export default function AccountsManagementPage() {
  const styles = useStyles();
  const navigate = useNavigate();
  const { profile } = useAuth();
  const canModerate = profile?.role === 'admin' || profile?.role === 'owner';
  const {
    accounts,
    isLoading,
    errorMessage,
    actioningId,
    actionErrorMessage,
    refresh,
    resetPassword,
    suspendAccount,
    promoteAccount,
    reactivateAccount,
    archiveAccount,
    demoteAccount,
  } = useAccountManagement(canModerate);
  const [resetTarget, setResetTarget] = useState<ManagedAccount | null>(null);
  const [suspendTarget, setSuspendTarget] = useState<ManagedAccount | null>(null);
  const [promoteTarget, setPromoteTarget] = useState<ManagedAccount | null>(null);
  const [reactivateTarget, setReactivateTarget] = useState<ManagedAccount | null>(null);
  const [archiveTarget, setArchiveTarget] = useState<ManagedAccount | null>(null);
  const [demoteTarget, setDemoteTarget] = useState<ManagedAccount | null>(null);
  const [newPassword, setNewPassword] = useState('');
  const [selectedAccountId, setSelectedAccountId] = useState<number | null>(null);

  const currentAccountId = useMemo(() => Number(profile?.id ?? 0), [profile?.id]);
  const selectedAccount = useMemo(
    () => accounts.find((account) => account.id === selectedAccountId) ?? null,
    [accounts, selectedAccountId],
  );
  const allowManageSelected =
    selectedAccount && profile ? canManage(profile.role, currentAccountId, selectedAccount) : false;
  const allowPromoteSelected =
    selectedAccount && profile
      ? canPromote(profile.role, currentAccountId, selectedAccount)
      : false;
  const allowReactivateSelected =
    selectedAccount && profile
      ? canReactivate(profile.role, currentAccountId, selectedAccount)
      : false;
  const allowArchiveSelected =
    selectedAccount && profile
      ? canArchive(profile.role, currentAccountId, selectedAccount)
      : false;
  const allowDemoteSelected =
    selectedAccount && profile ? canDemote(profile.role, currentAccountId, selectedAccount) : false;
  const isSelectedActioning = selectedAccount ? actioningId === selectedAccount.id : false;
  const canToggleStatusSelected = allowManageSelected || allowReactivateSelected;
  const canToggleRoleSelected = allowPromoteSelected || allowDemoteSelected;

  const columns = useMemo(
    () => [
      createTableColumn<ManagedAccount>({
        columnId: 'account',
        renderHeaderCell: () => '账户',
        renderCell: (account) => (
          <div className={styles.accountPrimary}>
            <Body1Strong className={styles.accountName}>
              {account.nickname || account.username}
            </Body1Strong>
            <Caption1 className={styles.accountSecondary}>@{account.username}</Caption1>
          </div>
        ),
      }),
      createTableColumn<ManagedAccount>({
        columnId: 'role',
        renderHeaderCell: () => '角色',
        renderCell: (account) => (
          <div className={styles.metaCell}>
            <span className={styles.metaIcon}>{getRoleIcon(account.role)}</span>
            <Body1>{roleLabels[account.role]}</Body1>
          </div>
        ),
      }),
      createTableColumn<ManagedAccount>({
        columnId: 'status',
        renderHeaderCell: () => '状态',
        renderCell: (account) => (
          <div className={styles.metaCell}>
            <span className={styles.metaIcon}>{getSatatusIcon(account.status)}</span>
            <Body1>{statusLabels[account.status]}</Body1>
          </div>
        ),
      }),
      createTableColumn<ManagedAccount>({
        columnId: 'password-policy',
        renderHeaderCell: () => '密码策略',
        renderCell: (account) => (
          <div className={styles.policyCell}>
            <span className={styles.metaIcon}>
              {account.mustChangePassword ? <KeyResetRegular /> : <KeyRegular />}
            </span>
            <Body1 className={styles.policyText}>
              {account.mustChangePassword ? '下次登录需修改密码' : '正常'}
            </Body1>
          </div>
        ),
      }),
      createTableColumn<ManagedAccount>({
        columnId: 'id',
        renderHeaderCell: () => '账户 ID',
        renderCell: (account) => <Body1 className={styles.idText}>{account.id}</Body1>,
      }),
    ],
    [styles],
  );

  if (!canModerate || !profile) {
    return (
      <div className={styles.page}>
        <Card appearance="filled-alternative">
          <div className={styles.cardBody}>
            <Title2>用户管理</Title2>
            <Caption1 className={styles.muted}>仅社区管理员或系统管理员可以访问此页面。</Caption1>
            <div className={styles.row}>
              <Button type="button" appearance="secondary" onClick={() => navigate('/')}>
                返回首页
              </Button>
            </div>
          </div>
        </Card>
      </div>
    );
  }

  return (
    <div className={styles.page}>
      <div className={styles.hero}>
        <div className={styles.heroHeader}>
          <Title2>用户管理</Title2>
          <Button
            type="button"
            appearance="secondary"
            icon={<ArrowSyncRegular />}
            onClick={() => void refresh()}
            disabled={isLoading}
          >
            刷新列表
          </Button>
        </div>
        <Body1 className={styles.muted}>
          查看当前账户状态，并执行重设密码、停用、启用、归档、提权、降权六项管理操作。
        </Body1>
      </div>

      {isLoading ? <ProgressBar className={styles.progress} /> : null}

      {actionErrorMessage ? (
        <MessageBar intent="error" layout="multiline">
          <MessageBarBody>
            <MessageBarTitle>操作失败</MessageBarTitle>
            {actionErrorMessage}
          </MessageBarBody>
        </MessageBar>
      ) : null}

      {errorMessage ? (
        <MessageBar intent="error" layout="multiline">
          <MessageBarBody>
            <MessageBarTitle>加载失败</MessageBarTitle>
            {errorMessage}
          </MessageBarBody>
        </MessageBar>
      ) : accounts.length === 0 && !isLoading ? (
        <Card appearance="filled-alternative">
          <div className={styles.fallbackCard}>
            <Body1Strong>当前没有可展示的账户</Body1Strong>
            <Caption1 className={styles.muted}>账户创建后会出现在这里。</Caption1>
          </div>
        </Card>
      ) : (
        <div className={styles.list}>
          <div className={styles.listHeader}>
            <div className={styles.listHeaderText}>
              <Subtitle2>账户列表</Subtitle2>
              <Caption1 className={styles.muted}>
                选择一个账户后，在上方共享操作区执行管理动作。
              </Caption1>
            </div>
            <Caption1 className={styles.muted}>共 {accounts.length} 个账户</Caption1>
          </div>

          <Card appearance="filled-alternative">
            <div className={styles.selectionBar}>
              <div className={styles.selectionSummary}>
                <Body1Strong>
                  {selectedAccount
                    ? `当前选中：${selectedAccount.nickname || selectedAccount.username}`
                    : '尚未选择账户'}
                </Body1Strong>
                <Caption1 className={styles.muted}>
                  {selectedAccount
                    ? `@${selectedAccount.username} · ${roleLabels[selectedAccount.role]} · ${statusLabels[selectedAccount.status]}`
                    : '使用左侧单选按钮选择一个账户后再执行操作。'}
                </Caption1>
              </div>
              <div className={styles.selectionActions}>
                <Button
                  type="button"
                  appearance="secondary"
                  icon={isSelectedActioning ? <Spinner size="tiny" /> : <KeyRegular />}
                  disabled={!selectedAccount || !allowManageSelected || isSelectedActioning}
                  onClick={() => {
                    if (!selectedAccount) {
                      return;
                    }
                    setResetTarget(selectedAccount);
                    setNewPassword('');
                  }}
                >
                  重设密码
                </Button>
                <Button
                  type="button"
                  appearance="secondary"
                  icon={
                    isSelectedActioning ? (
                      <Spinner size="tiny" />
                    ) : selectedAccount?.status === 'suspended' ? (
                      <PersonArrowBackRegular />
                    ) : (
                      <PersonProhibitedRegular />
                    )
                  }
                  disabled={!selectedAccount || !canToggleStatusSelected || isSelectedActioning}
                  onClick={() => {
                    if (!selectedAccount) {
                      return;
                    }
                    if (selectedAccount.status === 'suspended') {
                      setReactivateTarget(selectedAccount);
                      return;
                    }
                    setSuspendTarget(selectedAccount);
                  }}
                >
                  {selectedAccount?.status === 'suspended' ? '重新启用' : '停用账户'}
                </Button>
                <Button
                  type="button"
                  appearance="secondary"
                  icon={isSelectedActioning ? <Spinner size="tiny" /> : <PersonDeleteRegular />}
                  disabled={!selectedAccount || !allowArchiveSelected || isSelectedActioning}
                  onClick={() => {
                    if (!selectedAccount) {
                      return;
                    }
                    setArchiveTarget(selectedAccount);
                  }}
                >
                  归档账户
                </Button>
                <Button
                  type="button"
                  appearance={selectedAccount?.role === 'user' ? 'primary' : 'secondary'}
                  icon={
                    isSelectedActioning ? (
                      <Spinner size="tiny" />
                    ) : selectedAccount?.role === 'admin' ? (
                      <ShieldProhibitedRegular />
                    ) : (
                      <ShieldPersonAddRegular />
                    )
                  }
                  disabled={!selectedAccount || !canToggleRoleSelected || isSelectedActioning}
                  onClick={() => {
                    if (!selectedAccount) {
                      return;
                    }
                    if (selectedAccount.role === 'admin') {
                      setDemoteTarget(selectedAccount);
                      return;
                    }
                    setPromoteTarget(selectedAccount);
                  }}
                >
                  {selectedAccount?.role === 'admin' ? '降权为普通用户' : '提权为社区管理员'}
                </Button>
              </div>
            </div>
          </Card>

          <Card className={styles.tableCard}>
            <div className={styles.tableScroll}>
              <DataGrid
                items={accounts}
                columns={columns}
                getRowId={(account) => account.id}
                selectionMode="single"
                selectedItems={
                  selectedAccountId === null ? new Set() : new Set([selectedAccountId])
                }
                onSelectionChange={(_, data) => {
                  const nextId = data.selectedItems.values().next().value;
                  setSelectedAccountId(typeof nextId === 'number' ? nextId : null);
                }}
                subtleSelection
                className={styles.dataGrid}
              >
                <DataGridHeader>
                  <DataGridRow
                    selectionCell={{
                      type: 'radio',
                      invisible: true,
                      className: styles.selectionCell,
                    }}
                  >
                    {({ renderHeaderCell }) => (
                      <DataGridHeaderCell className={styles.headerCell}>
                        {renderHeaderCell()}
                      </DataGridHeaderCell>
                    )}
                  </DataGridRow>
                </DataGridHeader>
                <DataGridBody<ManagedAccount>>
                  {({ item, rowId }) => (
                    <DataGridRow<ManagedAccount>
                      key={rowId}
                      selectionCell={{
                        type: 'radio',
                        'aria-label': `选择 @${item.username}`,
                        className: styles.selectionCell,
                      }}
                    >
                      {({ renderCell }) => <DataGridCell>{renderCell(item)}</DataGridCell>}
                    </DataGridRow>
                  )}
                </DataGridBody>
              </DataGrid>
            </div>
          </Card>

          <Dialog
            open={resetTarget !== null}
            onOpenChange={(_, data) => {
              if (!data.open) {
                setResetTarget(null);
                setNewPassword('');
              }
            }}
          >
            <DialogSurface>
              <DialogBody>
                <DialogTitle>重设密码</DialogTitle>
                <DialogContent className={styles.dialogContent}>
                  <Body1>
                    为 <strong>@{resetTarget?.username}</strong>{' '}
                    设置一个新密码。对方下次登录时必须修改密码。
                  </Body1>
                  <Field label="新密码" hint="密码需为 6 到 20 位，并同时包含字母和数字。">
                    <Input
                      type="password"
                      value={newPassword}
                      onChange={(_, data) => setNewPassword(data.value)}
                    />
                  </Field>
                </DialogContent>
                <DialogActions>
                  <Button
                    type="button"
                    appearance="secondary"
                    onClick={() => {
                      setResetTarget(null);
                      setNewPassword('');
                    }}
                  >
                    取消
                  </Button>
                  <Button
                    type="button"
                    appearance="primary"
                    disabled={!resetTarget || isSelectedActioning || newPassword.trim() === ''}
                    onClick={async () => {
                      if (!resetTarget) {
                        return;
                      }
                      const ok = await resetPassword(resetTarget.id, newPassword.trim());
                      if (ok) {
                        setResetTarget(null);
                        setNewPassword('');
                      }
                    }}
                  >
                    确认重设
                  </Button>
                </DialogActions>
              </DialogBody>
            </DialogSurface>
          </Dialog>

          <Dialog
            open={suspendTarget !== null}
            onOpenChange={(_, data) => setSuspendTarget(data.open ? suspendTarget : null)}
          >
            <DialogSurface>
              <DialogBody>
                <DialogTitle>停用账户</DialogTitle>
                <DialogContent className={styles.dialogContent}>
                  <Body1>
                    停用后，<strong>@{suspendTarget?.username}</strong> 将无法继续登录系统。
                  </Body1>
                </DialogContent>
                <DialogActions>
                  <Button
                    type="button"
                    appearance="secondary"
                    onClick={() => setSuspendTarget(null)}
                  >
                    取消
                  </Button>
                  <Button
                    type="button"
                    appearance="primary"
                    disabled={
                      !suspendTarget || isSelectedActioning || suspendTarget.status === 'suspended'
                    }
                    onClick={async () => {
                      if (!suspendTarget) {
                        return;
                      }
                      const ok = await suspendAccount(suspendTarget.id);
                      if (ok) {
                        setSuspendTarget(null);
                      }
                    }}
                  >
                    确认停用
                  </Button>
                </DialogActions>
              </DialogBody>
            </DialogSurface>
          </Dialog>

          <Dialog
            open={promoteTarget !== null}
            onOpenChange={(_, data) => setPromoteTarget(data.open ? promoteTarget : null)}
          >
            <DialogSurface>
              <DialogBody>
                <DialogTitle>确认提权</DialogTitle>
                <DialogContent className={styles.dialogContent}>
                  <Body1>
                    该操作会把 <strong>@{promoteTarget?.username}</strong>{' '}
                    从普通用户提升为社区管理员。
                  </Body1>
                </DialogContent>
                <DialogActions>
                  <Button
                    type="button"
                    appearance="secondary"
                    onClick={() => setPromoteTarget(null)}
                  >
                    取消
                  </Button>
                  <Button
                    type="button"
                    appearance="primary"
                    disabled={!promoteTarget || isSelectedActioning}
                    onClick={async () => {
                      if (!promoteTarget) {
                        return;
                      }
                      const ok = await promoteAccount(promoteTarget.id);
                      if (ok) {
                        setPromoteTarget(null);
                      }
                    }}
                  >
                    确认提权
                  </Button>
                </DialogActions>
              </DialogBody>
            </DialogSurface>
          </Dialog>

          <Dialog
            open={reactivateTarget !== null}
            onOpenChange={(_, data) => setReactivateTarget(data.open ? reactivateTarget : null)}
          >
            <DialogSurface>
              <DialogBody>
                <DialogTitle>重新启用账户</DialogTitle>
                <DialogContent className={styles.dialogContent}>
                  <Body1>
                    重新启用后，<strong>@{reactivateTarget?.username}</strong> 将恢复登录权限。
                  </Body1>
                </DialogContent>
                <DialogActions>
                  <Button
                    type="button"
                    appearance="secondary"
                    onClick={() => setReactivateTarget(null)}
                  >
                    取消
                  </Button>
                  <Button
                    type="button"
                    appearance="primary"
                    disabled={!reactivateTarget || isSelectedActioning}
                    onClick={async () => {
                      if (!reactivateTarget) {
                        return;
                      }
                      const ok = await reactivateAccount(reactivateTarget.id);
                      if (ok) {
                        setReactivateTarget(null);
                      }
                    }}
                  >
                    确认启用
                  </Button>
                </DialogActions>
              </DialogBody>
            </DialogSurface>
          </Dialog>

          <Dialog
            open={archiveTarget !== null}
            onOpenChange={(_, data) => setArchiveTarget(data.open ? archiveTarget : null)}
          >
            <DialogSurface>
              <DialogBody>
                <DialogTitle>归档账户</DialogTitle>
                <DialogContent className={styles.dialogContent}>
                  <Body1>
                    归档后，<strong>@{archiveTarget?.username}</strong> 将被标记为归档状态。
                  </Body1>
                </DialogContent>
                <DialogActions>
                  <Button
                    type="button"
                    appearance="secondary"
                    onClick={() => setArchiveTarget(null)}
                  >
                    取消
                  </Button>
                  <Button
                    type="button"
                    appearance="primary"
                    disabled={!archiveTarget || isSelectedActioning}
                    onClick={async () => {
                      if (!archiveTarget) {
                        return;
                      }
                      const ok = await archiveAccount(archiveTarget.id);
                      if (ok) {
                        setArchiveTarget(null);
                      }
                    }}
                  >
                    确认归档
                  </Button>
                </DialogActions>
              </DialogBody>
            </DialogSurface>
          </Dialog>

          <Dialog
            open={demoteTarget !== null}
            onOpenChange={(_, data) => setDemoteTarget(data.open ? demoteTarget : null)}
          >
            <DialogSurface>
              <DialogBody>
                <DialogTitle>确认降权</DialogTitle>
                <DialogContent className={styles.dialogContent}>
                  <Body1>
                    该操作会把 <strong>@{demoteTarget?.username}</strong> 从社区管理员降为普通用户。
                  </Body1>
                </DialogContent>
                <DialogActions>
                  <Button
                    type="button"
                    appearance="secondary"
                    onClick={() => setDemoteTarget(null)}
                  >
                    取消
                  </Button>
                  <Button
                    type="button"
                    appearance="primary"
                    disabled={!demoteTarget || isSelectedActioning}
                    onClick={async () => {
                      if (!demoteTarget) {
                        return;
                      }
                      const ok = await demoteAccount(demoteTarget.id);
                      if (ok) {
                        setDemoteTarget(null);
                      }
                    }}
                  >
                    确认降权
                  </Button>
                </DialogActions>
              </DialogBody>
            </DialogSurface>
          </Dialog>
        </div>
      )}
    </div>
  );
}
