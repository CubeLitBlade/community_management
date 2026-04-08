import {
  Card,
  CardPreview,
  CardFooter,
  Button,
  makeStyles,
  tokens,
  Image,
  Text,
  useId,
  Input,
  Title3,
  Spinner,
  Field,
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
    padding: tokens.spacingHorizontalXXL,
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
  credentialFields: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalL,
  },
  input: {
    width: '100%',
  },
  submitButton: {
    width: '100%',
    marginTop: tokens.spacingVerticalXL,
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
    errorMessage,
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
          />
        </div>
        <CardPreview>
          <Title3 className={styles.title}>登录</Title3>
          <Text className={styles.subtitle}>提供您的登录凭据。</Text>
          <form className={styles.form} onSubmit={handleLoginSubmit}>
            <div className={styles.inputGroup}>
              <Field
                validationState={errorMessage ? 'error' : 'none'}
                validationMessage={errorMessage || FIELD_MESSAGE_PLACEHOLDER}
              >
                <div className={styles.credentialFields}>
                  <Field label="用户名">
                    <Input
                      type="text"
                      id={usernameId}
                      className={styles.input}
                      value={username}
                      onChange={(e) => setUsername(e.target.value)}
                    />
                  </Field>
                  <Field label="密码">
                    <Input
                      type="password"
                      id={passwordId}
                      className={styles.input}
                      value={password}
                      onChange={(e) => setPassword(e.target.value)}
                    />
                  </Field>
                </div>
              </Field>
            </div>
            <Button
              type="submit"
              appearance="primary"
              className={styles.submitButton}
              disabledFocusable={isSubmitting}
              icon={isSubmitting ? <Spinner size="tiny" /> : null}
            >
              {isSubmitting ? '登录中' : '下一步'}
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
