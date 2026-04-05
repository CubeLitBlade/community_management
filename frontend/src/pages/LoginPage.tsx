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
  Label,
  Spinner,
} from '@fluentui/react-components';
import BgLogin from '../assets/bg-login.jpg';
import useLogin from '../hooks/useLogin';

const useStyles = makeStyles({
  root: {
    height: '100vh',
    backgroundImage: `linear-gradient(
      rgba(0, 0, 0, 0.5), 
      rgba(0, 0, 0, 0.5)
    ), url(${BgLogin})`,
    backgroundSize: 'cover',
    backgroundPosition: 'center',
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    padding: '20px',
  },
  loginCard: {
    width: '100%',
    maxWidth: '400px',
    backgroundColor: tokens.colorNeutralBackground1,
    borderRadius: tokens.borderRadiusXLarge,
    boxShadow: tokens.shadow16,
    overflow: 'hidden',
  },
  logoContainer: {
    display: 'flex',
    justifyContent: 'center',
    padding: '6px 0',
  },
  logo: {
    width: '48px',
    height: '48px',
    paddingTop: `${tokens.spacingHorizontalSNudge}`,
  },
  title: {
    textAlign: 'center',
    marginBottom: '8px',
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
    gap: tokens.spacingVerticalM,
    padding: `0 ${tokens.spacingHorizontalXL}`,
  },
  inputGroup: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalM,
  },
  input: {
    width: '100%',
  },
  submitButton: {
    width: '100%',
    marginTop: tokens.spacingVerticalL,
  },
  footer: {
    display: 'flex',
    justifyContent: 'center',
    width: '100%',
    padding: `0 ${tokens.spacingHorizontalXL} ${tokens.spacingVerticalXL}`,
    fontSize: '14px',
    color: tokens.colorNeutralForeground2,
  },
  formTip: {
    color: `${tokens.colorStatusDangerForeground1}`,
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
              <Input
                type="text"
                id={usernameId}
                className={styles.input}
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                placeholder="用户名"
              />

              <Input
                type="password"
                id={passwordId}
                className={styles.input}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="密码"
              />

              <Label className={styles.formTip}>{errorMessage}</Label>
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
            <Button appearance="subtle" size="small" className={styles.footerLink}>
              创建账户
            </Button>
          </div>
        </CardFooter>
      </Card>
    </div>
  );
}
