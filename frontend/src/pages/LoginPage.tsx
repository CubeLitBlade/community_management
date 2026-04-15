import {
  Button,
  Card,
  CardFooter,
  CardPreview,
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
import { useNavigate } from 'react-router';
import BgLogin from '../assets/bg-login.jpg';
import useLogin from '../hooks/useLogin';

const CARD_MAX_WIDTH = '28rem';
const BRAND_LOGO_SIZE = '3rem';
const FIELD_MESSAGE_PLACEHOLDER = '\u00A0';

const useStyles = makeStyles({
  root: {
    minHeight: '100dvh',
    backgroundImage: `linear-gradient( rgba(0, 0, 0, 0.5), rgba(0, 0, 0, 0.5) ), url(${BgLogin})`,
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
  loginCard: {
    width: '100%',
    maxWidth: CARD_MAX_WIDTH,
    backgroundColor: tokens.colorNeutralBackground1,
    borderRadius: tokens.borderRadiusXLarge,
    boxShadow: tokens.shadow16,
    overflow: 'hidden',
  },
  logoContainer: {
    display: 'flex',
    justifyContent: 'center',
    padding: `${tokens.spacingVerticalSNudge} 0`,
  },
  logo: {
    width: BRAND_LOGO_SIZE,
    height: BRAND_LOGO_SIZE,
    paddingTop: `${tokens.spacingHorizontalSNudge}`,
  },
  title: {
    textAlign: 'center',
    marginBottom: tokens.spacingVerticalS,
    color: tokens.colorNeutralForeground1,
  },
  subtitle: {
    textAlign: 'center',
    color: tokens.colorNeutralForeground2,
    marginBottom: tokens.spacingVerticalXL,
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
    fontSize: tokens.fontSizeBase300,
    color: tokens.colorNeutralForeground2,
  },
  cardFooter: {
    justifyContent: 'center',
  },
  footerLink: {
    color: tokens.colorBrandForeground1,
    textDecoration: 'none',
    minWidth: 'fit-content',
  },
});

export default function LoginPage() {
  const styles = useStyles();
  const navigate = useNavigate();
  const {
    username,
    setUsername,
    password,
    setPassword,
    usernameMessage,
    passwordMessage,
    submitErrorMessage,
    isSubmitting,
    handleLoginSubmit,
  } = useLogin();

  const usernameId = useId('username');
  const passwordId = useId('password');

  return (
    <div className={styles.root}>
      <Card className={styles.loginCard}>
        <div className={styles.logoContainer}>
          <Image
            className={styles.logo}
            src="https://raw.githubusercontent.com/microsoft/fluentui-system-icons/refs/heads/main/assets/People%20Community/SVG/ic_fluent_people_community_48_color.svg"
            alt="社区图标"
          />
        </div>
        <CardPreview>
          <Title3 className={styles.title}>登录</Title3>
          <Text className={styles.subtitle}>提供您的登录凭据。</Text>
          <form
            className={styles.form}
            onSubmit={async (event) => {
              const targetPath = await handleLoginSubmit(event);
              if (targetPath) {
                navigate(targetPath, { replace: true });
              }
            }}
          >
            <div className={styles.inputGroup}>
              <Field
                label="用户名"
                validationState={usernameMessage ? 'error' : 'none'}
                validationMessage={usernameMessage || FIELD_MESSAGE_PLACEHOLDER}
              >
                <Input
                  type="text"
                  id={usernameId}
                  className={styles.input}
                  value={username}
                  autoComplete="username"
                  onChange={(e) => setUsername(e.target.value)}
                />
              </Field>
              <Field
                label="密码"
                validationState={passwordMessage ? 'error' : 'none'}
                validationMessage={passwordMessage || FIELD_MESSAGE_PLACEHOLDER}
              >
                <Input
                  type="password"
                  id={passwordId}
                  className={styles.input}
                  value={password}
                  autoComplete="current-password"
                  onChange={(e) => setPassword(e.target.value)}
                />
              </Field>
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
              icon={isSubmitting ? <Spinner size="tiny" /> : null}
            >
              {isSubmitting ? '登录中' : '登录'}
            </Button>
          </form>
        </CardPreview>
        <CardFooter className={styles.cardFooter}>
          <div className={styles.footer}>
            <Button
              appearance="subtle"
              className={styles.footerLink}
              onClick={() => navigate('/auth/register')}
            >
              创建账户
            </Button>
          </div>
        </CardFooter>
      </Card>
    </div>
  );
}
