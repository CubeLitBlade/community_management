import {
  Badge,
  Button,
  Card,
  CardFooter,
  CardHeader,
  Field,
  Image,
  Input,
  Spinner,
  Text,
  Title3,
  makeStyles,
  tokens,
  useId,
} from '@fluentui/react-components';
import {
  ArrowResetRegular,
  ShieldKeyhole20Regular,
} from '@fluentui/react-icons';
import { useEffect } from 'react';
import { useNavigate } from 'react-router';
import BgLogin from '../assets/bg-login.jpg';
import useAuth from '../hooks/useAuth';
import useChangePassword from '../hooks/useChangePassword';

const FIELD_MESSAGE_PLACEHOLDER = '\u00A0';
const CARD_MAX_WIDTH = '30rem';
const BRAND_LOGO_SIZE = '3rem';

const useStyles = makeStyles({
  root: {
    minHeight: '100dvh',
    backgroundImage: `linear-gradient( rgba(8, 27, 53, 0.72), rgba(12, 21, 38, 0.78) ), url(${BgLogin})`,
    backgroundSize: 'cover',
    backgroundPosition: 'center',
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    padding: `${tokens.spacingVerticalXL} ${tokens.spacingHorizontalL}`,
    '@media (max-width: 480px)': {
      padding: tokens.spacingHorizontalM,
    },
  },
  card: {
    width: '100%',
    maxWidth: CARD_MAX_WIDTH,
    backgroundColor: tokens.colorNeutralBackground1,
    borderRadius: tokens.borderRadiusXLarge,
    boxShadow: tokens.shadow16,
    overflow: 'hidden',
    border: `1px solid ${tokens.colorNeutralStroke1}`,
  },
  logoContainer: {
    display: 'flex',
    justifyContent: 'center',
    padding: `${tokens.spacingVerticalL} 0 ${tokens.spacingVerticalS}`,
  },
  logo: {
    width: BRAND_LOGO_SIZE,
    height: BRAND_LOGO_SIZE,
    paddingTop: `${tokens.spacingHorizontalSNudge}`,
  },
  header: {
    padding: `0 ${tokens.spacingHorizontalXXXL} ${tokens.spacingVerticalS}`,
  },
  titleRow: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: tokens.spacingHorizontalM,
    flexWrap: 'wrap',
  },
  title: {
    color: tokens.colorNeutralForeground1,
  },
  subtitle: {
    color: tokens.colorNeutralForeground2,
    marginTop: tokens.spacingVerticalXS,
    lineHeight: tokens.lineHeightBase400,
  },
  heroPanel: {
    margin: `0 ${tokens.spacingHorizontalXXXL} ${tokens.spacingVerticalL}`,
    padding: `${tokens.spacingVerticalM} ${tokens.spacingHorizontalL}`,
    borderRadius: tokens.borderRadiusLarge,
    backgroundColor: tokens.colorNeutralBackground2,
    border: `1px solid ${tokens.colorNeutralStroke2}`,
    display: 'grid',
    gap: tokens.spacingVerticalXS,
  },
  heroEyebrow: {
    color: tokens.colorNeutralForeground3,
  },
  form: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalL,
    padding: `0 ${tokens.spacingHorizontalXXXL} ${tokens.spacingVerticalXL}`,
  },
  inputGroup: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalL,
  },
  input: {
    width: '100%',
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
  submitButton: {
    width: '100%',
    marginTop: tokens.spacingVerticalM,
  },
  footer: {
    display: 'flex',
    justifyContent: 'center',
    width: '100%',
    padding: `0 ${tokens.spacingHorizontalXL} ${tokens.spacingVerticalXL}`,
  },
  footerLink: {
    color: tokens.colorBrandForeground1,
    textDecoration: 'none',
    minWidth: 'fit-content',
  },
  cardFooter: {
    justifyContent: 'center',
  },
});

export default function ChangePasswordPage() {
  const styles = useStyles();
  const navigate = useNavigate();
  const { profile, isLoading, logout } = useAuth();
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
  const currentPasswordId = useId('current-password');
  const newPasswordId = useId('new-password');

  useEffect(() => {
    if (isLoading) {
      return;
    }

    if (!profile) {
      navigate('/auth/login', { replace: true });
      return;
    }

    if (!profile.mustChangePassword) {
      navigate('/', { replace: true });
    }
  }, [isLoading, navigate, profile]);

  if (isLoading || !profile || !profile.mustChangePassword) {
    return (
      <div className={styles.root}>
        <Spinner label="正在检查账户状态..." />
      </div>
    );
  }

  return (
    <div className={styles.root}>
      <Card className={styles.card}>
        <div className={styles.logoContainer}>
          <Image
            className={styles.logo}
            src="https://raw.githubusercontent.com/microsoft/fluentui-system-icons/refs/heads/main/assets/People%20Community/SVG/ic_fluent_people_community_48_color.svg"
            alt="社区图标"
          />
        </div>
        <CardHeader
          className={styles.header}
          header={
            <div className={styles.titleRow}>
              <Title3 className={styles.title}>更新密码</Title3>
              <Badge appearance="tint" color="danger">
                必须完成
              </Badge>
            </div>
          }
          description={
            <Text className={styles.subtitle}>
              为了保护账户安全，首次登录或重置密码后，需要先设置一个新密码。
            </Text>
          }
        />
        <div className={styles.heroPanel}>
          <Text size={200} weight="medium" className={styles.heroEyebrow}>
            完成后即可继续访问系统
          </Text>
          <Text>此步骤仅更新你的登录密码，不会影响账号资料与其余设置。</Text>
        </div>
        <form
          className={styles.form}
          onSubmit={async (event) => {
            const result = await handleSubmit(event);
            if (result === 'success') {
              navigate('/', { replace: true });
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
                type="password"
                id={currentPasswordId}
                className={styles.input}
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
                type="password"
                id={newPasswordId}
                className={styles.input}
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
          <Button
            type="submit"
            appearance="primary"
            className={styles.submitButton}
            disabled={isSubmitting}
            icon={isSubmitting ? <Spinner size="tiny" /> : <ArrowResetRegular />}
          >
            {isSubmitting ? '提交中' : '更新密码'}
          </Button>
        </form>
        <CardFooter className={styles.cardFooter}>
          <div className={styles.footer}>
            <Button appearance="subtle" className={styles.footerLink} onClick={() => void logout()}>
              退出当前账户
            </Button>
          </div>
        </CardFooter>
      </Card>
    </div>
  );
}
