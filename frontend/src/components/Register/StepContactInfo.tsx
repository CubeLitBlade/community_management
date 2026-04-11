import {
  Button,
  Field,
  Input,
  Text,
  Title3,
  makeStyles,
  tokens,
  useId,
  Spinner,
} from '@fluentui/react-components';
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
  inputGroup: { display: 'flex', flexDirection: 'column', gap: tokens.spacingVerticalL },
  input: { width: '100%' },
  submitButton: { width: '100%', marginTop: tokens.spacingVerticalXXL },
});

interface StepContactInfoProps {
  email: {
    value: string;
    onChange: (val: string) => void;
    onBlur: () => void;
    checkState: string;
    checkMessage: string;
    isChecking: boolean;
  };
  phone: {
    value: string;
    onChange: (val: string) => void;
    onBlur: () => void;
    checkState: string;
    checkMessage: string;
    isChecking: boolean;
  };
  canSubmit: boolean;
  submitErrorMessage: string;
  isSubmitting: boolean;
  onSubmit: (e: FormEvent<HTMLFormElement>) => void;
}

export default function StepContactInfo({
  email,
  phone,
  canSubmit,
  submitErrorMessage,
  isSubmitting,
  onSubmit,
}: StepContactInfoProps) {
  const styles = useStyles();
  const emailId = useId('email');
  const phoneId = useId('phone');

  const emailState =
    email.checkState === 'unavailable' || email.checkState === 'error'
      ? 'error'
      : email.checkState === 'available'
        ? 'success'
        : 'none';
  const phoneState =
    phone.checkState === 'unavailable' || phone.checkState === 'error'
      ? 'error'
      : phone.checkState === 'available'
        ? 'success'
        : 'none';
  const isAnyChecking = email.isChecking || phone.isChecking;

  return (
    <>
      <Title3 className={styles.title}>完善信息</Title3>
      <Text className={styles.subtitle}>请提供手机号或邮箱，至少填写一项。</Text>
      <form className={styles.form} onSubmit={onSubmit}>
        {submitErrorMessage ? (
          <Text role="alert" style={{ color: tokens.colorPaletteRedForeground1 }}>
            {submitErrorMessage}
          </Text>
        ) : null}
        <div className={styles.inputGroup}>
          <Field
            label="邮箱"
            validationState={emailState}
            validationMessage={email.checkMessage || FIELD_MESSAGE_PLACEHOLDER}
          >
            <Input
              type="email"
              id={emailId}
              className={styles.input}
              value={email.value}
              autoComplete="email"
              onChange={(e) => email.onChange(e.target.value)}
              onBlur={email.onBlur}
            />
          </Field>
          <Field
            label="手机号"
            validationState={phoneState}
            validationMessage={phone.checkMessage || FIELD_MESSAGE_PLACEHOLDER}
          >
            <Input
              type="tel"
              id={phoneId}
              className={styles.input}
              maxLength={12}
              value={phone.value}
              autoComplete="tel"
              onChange={(e) => phone.onChange(e.target.value)}
              onBlur={phone.onBlur}
            />
          </Field>
        </div>
        <Button
          type="submit"
          appearance="primary"
          className={styles.submitButton}
          disabled={!canSubmit}
          disabledFocusable={isAnyChecking || isSubmitting}
          icon={isAnyChecking || isSubmitting ? <Spinner size="tiny" /> : null}
        >
          {isSubmitting ? '注册中' : '完成注册'}
        </Button>
      </form>
    </>
  );
}
