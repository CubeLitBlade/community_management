import {
  Card,
  CardFooter,
  CardPreview,
  Button,
  Image,
  makeStyles,
  tokens,
} from '@fluentui/react-components';
import { ArrowLeftRegular } from '@fluentui/react-icons';
import BgLogin from '../assets/bg-login.jpg';
import useRegister from '../hooks/useRegister';
import { useNavigate } from 'react-router';
import StepCredentials from '../components/Register/StepCredentials';
import StepContactInfo from '../components/Register/StepContactInfo';

const CARD_MAX_WIDTH = '28rem';
const BRAND_LOGO_SIZE = '3rem';

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
  registerCard: {
    width: '100%',
    maxWidth: CARD_MAX_WIDTH,
    backgroundColor: tokens.colorNeutralBackground1,
    borderRadius: tokens.borderRadiusXLarge,
    boxShadow: tokens.shadow16,
    overflow: 'hidden',
    position: 'relative',
  },
  backButton: {
    position: 'absolute',
    top: tokens.spacingVerticalS,
    left: tokens.spacingHorizontalS,
    zIndex: 1,
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
  footer: {
    display: 'flex',
    justifyContent: 'center',
    width: '100%',
    padding: `0 ${tokens.spacingHorizontalXL} ${tokens.spacingVerticalXL}`,
    fontSize: tokens.fontSizeBase300,
    color: tokens.colorNeutralForeground2,
  },
  footerLink: {
    color: tokens.colorBrandForeground1,
    textDecoration: 'none',
    minWidth: 'fit-content',
  },
  cardFooter: {
    justifyContent: 'center',
  },
  footerPlaceholder: {
    width: '100%',
    padding: `0 ${tokens.spacingHorizontalXL} ${tokens.spacingVerticalL}`,
  },
});

export default function RegisterPage() {
  const styles = useStyles();
  const navigate = useNavigate();
  const {
    step,
    username,
    email,
    phone,
    password,
    setPassword,
    passwordMessage,
    handlePasswordBlur,
    confirmPassword,
    setConfirmPassword,
    passwordMismatchMessage,
    handleConfirmPasswordBlur,
    canProceedToStep2,
    canSubmit,
    submitErrorMessage,
    isSubmitting,
    handleNextStep,
    handlePrevStep,
    handleSubmit,
  } = useRegister();

  return (
    <div className={styles.root}>
      <Card className={styles.registerCard}>
        {step === 2 ? (
          <Button
            appearance="subtle"
            size="small"
            className={styles.backButton}
            icon={<ArrowLeftRegular />}
            onClick={handlePrevStep}
            aria-label="上一步"
            title="上一步"
          />
        ) : null}
        <div className={styles.logoContainer}>
          <Image
            className={styles.logo}
            src="https://raw.githubusercontent.com/microsoft/fluentui-system-icons/refs/heads/main/assets/People%20Community/SVG/ic_fluent_people_community_48_color.svg"
            alt="社区图标"
          />
        </div>
        <CardPreview>
          {step === 1 ? (
            <StepCredentials
              username={username}
              password={password}
              setPassword={setPassword}
              passwordMessage={passwordMessage}
              handlePasswordBlur={handlePasswordBlur}
              confirmPassword={confirmPassword}
              setConfirmPassword={setConfirmPassword}
              passwordMismatchMessage={passwordMismatchMessage}
              handleConfirmPasswordBlur={handleConfirmPasswordBlur}
              canProceed={canProceedToStep2}
              onNext={handleNextStep}
            />
          ) : (
            <StepContactInfo
              email={email}
              phone={phone}
              canSubmit={canSubmit}
              submitErrorMessage={submitErrorMessage}
              isSubmitting={isSubmitting}
              onSubmit={handleSubmit}
            />
          )}
        </CardPreview>
        <CardFooter className={styles.cardFooter}>
          {step === 1 ? (
            <div className={styles.footer}>
              <Button
                appearance="subtle"
                className={styles.footerLink}
                onClick={() => navigate('/auth/login')}
              >
                返回登录
              </Button>
            </div>
          ) : (
            <div className={styles.footerPlaceholder} aria-hidden="true" />
          )}
        </CardFooter>
      </Card>
    </div>
  );
}
