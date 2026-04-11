import {
  Button,
  Field,
  Input,
  Text,
  Title3,
  makeStyles,
  tokens,
  useId,
} from '@fluentui/react-components';
import { Spinner } from '@fluentui/react-components';
import type { FormEvent } from 'react';

const FIELD_MESSAGE_PLACEHOLDER = '\u00A0';

const useStyles = makeStyles({
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
  inputGroup: { display: 'flex', flexDirection: 'column', gap: tokens.spacingVerticalS },
  input: { width: '100%' },
  submitButton: { width: '100%', marginTop: tokens.spacingVerticalXXL },
});

interface StepCredentialsProps {
  username: {
    value: string;
    onChange: (val: string) => void;
    onBlur: () => void;
    checkState: string;
    checkMessage: string;
    isChecking: boolean;
  };
  password: string;
  setPassword: (val: string) => void;
  passwordMessage: string;
  handlePasswordBlur: () => void;
  confirmPassword: string;
  setConfirmPassword: (val: string) => void;
  passwordMismatchMessage: string;
  handleConfirmPasswordBlur: () => void;
  canProceed: boolean;
  onNext: (e: FormEvent<HTMLFormElement>) => void;
}

export default function StepCredentials({
  username,
  password,
  setPassword,
  passwordMessage,
  handlePasswordBlur,
  confirmPassword,
  setConfirmPassword,
  passwordMismatchMessage,
  handleConfirmPasswordBlur,
  canProceed,
  onNext,
}: StepCredentialsProps) {
  const styles = useStyles();
  const usernameId = useId('username');
  const passwordId = useId('password');
  const confirmPasswordId = useId('confirm-password');

  const usernameValidationState =
    username.checkState === 'unavailable' || username.checkState === 'error'
      ? 'error'
      : username.checkState === 'available'
        ? 'success'
        : 'none';
  const passwordValidationState = passwordMessage === '' ? 'none' : 'error';
  const confirmPwdValidationState = passwordMismatchMessage === '' ? 'none' : 'error';

  return (
    <>
      <Title3 className={styles.title}>注册</Title3>
      <Text className={styles.subtitle}>填写你的用户名和密码。</Text>
      <form className={styles.form} onSubmit={onNext}>
        <div className={styles.inputGroup}>
          <Field
            label="用户名"
            validationState={usernameValidationState}
            validationMessage={username.checkMessage || FIELD_MESSAGE_PLACEHOLDER}
          >
            <Input
              type="text"
              id={usernameId}
              className={styles.input}
              maxLength={20}
              value={username.value}
              autoComplete="username"
              onChange={(e) => username.onChange(e.target.value)}
              onBlur={username.onBlur}
            />
          </Field>
        </div>
        <div className={styles.inputGroup}>
          <Field
            label="密码"
            validationState={passwordValidationState}
            validationMessage={passwordMessage || FIELD_MESSAGE_PLACEHOLDER}
          >
            <Input
              type="password"
              id={passwordId}
              className={styles.input}
              maxLength={20}
              value={password}
              autoComplete="new-password"
              onChange={(e) => setPassword(e.target.value)}
              onBlur={handlePasswordBlur}
            />
          </Field>
          <Field
            label="确认密码"
            validationState={confirmPwdValidationState}
            validationMessage={passwordMismatchMessage || FIELD_MESSAGE_PLACEHOLDER}
          >
            <Input
              type="password"
              id={confirmPasswordId}
              className={styles.input}
              value={confirmPassword}
              autoComplete="new-password"
              onChange={(e) => setConfirmPassword(e.target.value)}
              onBlur={handleConfirmPasswordBlur}
            />
          </Field>
        </div>
        <Button
          type="submit"
          appearance="primary"
          className={styles.submitButton}
          disabled={!canProceed}
          disabledFocusable={username.isChecking}
          icon={username.isChecking ? <Spinner size="tiny" /> : null}
        >
          下一步
        </Button>
      </form>
    </>
  );
}
