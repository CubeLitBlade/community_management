import {
  Button,
  Card,
  CardFooter,
  CardHeader,
  Field,
  Input,
  Spinner,
  Text,
  Title2,
  makeStyles,
  tokens,
  useId,
} from '@fluentui/react-components';
import { ArrowLeftRegular, ArrowResetRegular, ShieldKeyhole20Regular } from '@fluentui/react-icons';
import { useEffect } from 'react';
import { useNavigate } from 'react-router';
import useAuth from '../hooks/useAuth';
import useChangePassword from '../hooks/useChangePassword';

const FIELD_MESSAGE_PLACEHOLDER = '\u00A0';

const useStyles = makeStyles({
  page: {
    width: 'min(100%, 42rem)',
    margin: '0 auto',
    padding: `${tokens.spacingVerticalXXL} ${tokens.spacingHorizontalL}`,
    display: 'grid',
    gap: tokens.spacingVerticalXL,
  },
  header: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: tokens.spacingHorizontalM,
    flexWrap: 'wrap',
  },
  titleGroup: {
    display: 'grid',
    gap: tokens.spacingVerticalXS,
  },
  subtitle: {
    color: tokens.colorNeutralForeground2,
    lineHeight: tokens.lineHeightBase400,
  },
  card: {
    maxWidth: '34rem',
    padding: tokens.spacingHorizontalL,
    borderRadius: tokens.borderRadiusXLarge,
    backgroundColor: tokens.colorNeutralBackground1,
    border: `1px solid ${tokens.colorNeutralStroke1}`,
    boxShadow: tokens.shadow8,
    display: 'grid',
    gap: tokens.spacingVerticalL,
  },
  form: {
    display: 'grid',
    gap: tokens.spacingVerticalL,
  },
  inputGroup: {
    display: 'grid',
    gap: tokens.spacingVerticalL,
  },
  hint: {
    display: 'flex',
    alignItems: 'flex-start',
    gap: tokens.spacingHorizontalS,
    padding: `${tokens.spacingVerticalS} ${tokens.spacingHorizontalM}`,
    borderRadius: tokens.borderRadiusLarge,
    backgroundColor: tokens.colorNeutralBackground2,
    border: `1px solid ${tokens.colorNeutralStroke2}`,
    color: tokens.colorNeutralForeground3,
  },
  errorText: {
    color: tokens.colorPaletteRedForeground1,
  },
  actions: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: tokens.spacingHorizontalM,
    flexWrap: 'wrap',
  },
  footerNote: {
    color: tokens.colorNeutralForeground3,
  },
});

export default function SettingsChangePasswordPage() {
  const styles = useStyles();
  const navigate = useNavigate();
  const { profile, isLoading } = useAuth();
  const {
    currentPassword,
    setCurrentPassword,
    newPassword,
    setNewPassword,
    currentPasswordMessage,
    newPasswordMessage,
    submitErrorMessage,
    isSubmitting,
    handleSubmit,
  } = useChangePassword();
  const currentPasswordId = useId('settings-current-password');
  const newPasswordId = useId('settings-new-password');

  useEffect(() => {
    if (isLoading) {
      return;
    }

    if (!profile) {
      navigate('/auth/login', { replace: true });
    }
  }, [isLoading, navigate, profile]);

  if (isLoading || !profile) {
    return <Spinner label="正在检查账户状态..." />;
  }

  return (
    <div className={styles.page}>
      <div className={styles.header}>
        <div className={styles.titleGroup}>
          <Title2>修改密码</Title2>
          <Text className={styles.subtitle}>
            使用当前密码验证身份，然后为账户设置一个新的登录密码。
          </Text>
        </div>
        <Button appearance="subtle" icon={<ArrowLeftRegular />} onClick={() => navigate('/settings')}>
          返回设置
        </Button>
      </div>

      <Card className={styles.card}>
        <CardHeader
          header={<Text weight="semibold">密码安全</Text>}
          description={<Text className={styles.subtitle}>新密码会在保存后立即生效。</Text>}
        />
        <form
          className={styles.form}
          onSubmit={async (event) => {
            const result = await handleSubmit(event);
            if (result === 'success') {
              navigate('/settings', { replace: true });
            }
          }}
        >
          <div className={styles.inputGroup}>
            <Field
              label="当前密码"
              validationState={currentPasswordMessage ? 'error' : 'none'}
              validationMessage={currentPasswordMessage || FIELD_MESSAGE_PLACEHOLDER}
            >
              <Input
                id={currentPasswordId}
                type="password"
                value={currentPassword}
                autoComplete="current-password"
                onChange={(_, data) => setCurrentPassword(data.value)}
              />
            </Field>
            <Field
              label="新密码"
              validationState={newPasswordMessage ? 'error' : 'none'}
              validationMessage={newPasswordMessage || FIELD_MESSAGE_PLACEHOLDER}
            >
              <Input
                id={newPasswordId}
                type="password"
                value={newPassword}
                autoComplete="new-password"
                onChange={(_, data) => setNewPassword(data.value)}
              />
            </Field>
          </div>

          <div className={styles.hint}>
            <ShieldKeyhole20Regular />
            <Text>密码需为 6 到 20 位，并同时包含字母和数字。</Text>
          </div>

          {submitErrorMessage ? (
            <Text role="alert" className={styles.errorText}>
              {submitErrorMessage}
            </Text>
          ) : null}

          <CardFooter className={styles.actions}>
            <Text className={styles.footerNote}>修改成功后，新的密码会立即生效。</Text>
            <Button
              type="submit"
              appearance="primary"
              disabled={isSubmitting}
              icon={isSubmitting ? <Spinner size="tiny" /> : <ArrowResetRegular />}
            >
              {isSubmitting ? '提交中' : '保存新密码'}
            </Button>
          </CardFooter>
        </form>
      </Card>
    </div>
  );
}
