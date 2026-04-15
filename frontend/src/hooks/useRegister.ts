import { useEffect, useRef, useState, type FormEvent } from 'react';
import { useNavigate } from 'react-router';
import apiClient, { refreshCsrfToken } from '../api/apiClient';
import type { FieldsCheckRequest, FieldsCheckResponse, RegisterRequest } from '../types/Account';
import { BizError, type ProblemCode } from '../types/Error';

const CHECK_DEBOUNCE_MS = 500;
const PASSWORD_MIN_LENGTH = 6;
const PASSWORD_MAX_LENGTH = 20;
const USERNAME_MAX_LENGTH = 20;
const PHONE_PATTERN = /^\+?[0-9]{11}$/;
const PASSWORD_PATTERN = /^(?=.*[A-Za-z])(?=.*\d).{6,20}$/;

type FieldCheckState = 'idle' | 'available' | 'unavailable' | 'error';

function useFieldCheck(fieldKey: 'username' | 'email' | 'phone') {
  const allowEmpty = fieldKey !== 'username';
  const blankCodeByField: Record<'username' | 'email' | 'phone', ProblemCode> = {
    username: 'INPUT_USERNAME_BLANK',
    email: 'INPUT_EMAIL_BLANK',
    phone: 'INPUT_PHONE_BLANK',
  };
  const conflictCodeByField: Record<'username' | 'email' | 'phone', ProblemCode> = {
    username: 'CONFLICT_USERNAME_EXISTS',
    email: 'CONFLICT_EMAIL_EXISTS',
    phone: 'CONFLICT_PHONE_EXISTS',
  };
  const invalidCodesByField: Record<'username' | 'email' | 'phone', ProblemCode[]> = {
    username: ['INPUT_USERNAME_BAD_LENGTH'],
    email: ['INPUT_EMAIL_BAD_FORMAT'],
    phone: ['INPUT_PHONE_BAD_FORMAT'],
  };

  const getLocalIssue = (val: string): ProblemCode | null => {
    if (fieldKey === 'username') {
      if (val.length > USERNAME_MAX_LENGTH) return 'INPUT_USERNAME_BAD_LENGTH';
      return null;
    }

    if (fieldKey === 'phone') {
      if (!PHONE_PATTERN.test(val)) return 'INPUT_PHONE_BAD_FORMAT';
      return null;
    }

    return null;
  };
  const [value, setValue] = useState('');
  const [checkState, setCheckState] = useState<FieldCheckState>('idle');
  const [checkMessage, setCheckMessage] = useState('');
  const [isBlurred, setIsBlurred] = useState(false);
  const [isChecking, setIsChecking] = useState(false);

  const latestRequestId = useRef(0);
  const timeoutRef = useRef<number | null>(null);
  const pendingRef = useRef<string | null>(null);
  const inFlightRef = useRef<string | null>(null);
  const lastValidatedRef = useRef<string | null>(null);

  const normalizedValue = value.trim();

  useEffect(() => {
    return () => {
      if (timeoutRef.current !== null) window.clearTimeout(timeoutRef.current);
    };
  }, []);

  const getMessage = (reasons: ProblemCode[]) => {
    const label = fieldKey === 'email' ? '邮箱' : fieldKey === 'phone' ? '手机号' : '用户名';
    const blankCode = blankCodeByField[fieldKey];
    const conflictCode = conflictCodeByField[fieldKey];
    const invalidCodes = invalidCodesByField[fieldKey];

    if (reasons.includes(blankCode)) return `${label}不能为空。`;
    if (invalidCodes.some((code) => reasons.includes(code))) return `${label}格式不正确。`;
    if (reasons.includes(conflictCode)) return `${label}已存在。`;
    return `${label}不可用，请重试。`;
  };

  const cancelValidation = () => {
    if (timeoutRef.current !== null) window.clearTimeout(timeoutRef.current);
    timeoutRef.current = null;
    pendingRef.current = null;
    inFlightRef.current = null;
    latestRequestId.current += 1;
    setIsChecking(false);
  };

  const validate = async (val: string) => {
    const requestId = ++latestRequestId.current;
    inFlightRef.current = val;
    const request: FieldsCheckRequest = {
      username: null,
      email: null,
      phone: null,
      [fieldKey]: val,
    };

    try {
      await refreshCsrfToken();
      const response = await apiClient.post<FieldsCheckResponse>('/auth/register/check', request);
      if (requestId !== latestRequestId.current) return;
      lastValidatedRef.current = val;
      if (response.data.available) {
        setCheckState('available');
        setCheckMessage(
          `${fieldKey === 'username' ? '用户名' : fieldKey === 'email' ? '邮箱' : '手机号'}可用。`,
        );
      } else {
        setCheckState('unavailable');
        setCheckMessage(getMessage(response.data.reasons));
      }
    } catch {
      if (requestId !== latestRequestId.current) return;
      lastValidatedRef.current = val;
      setCheckState('error');
      setCheckMessage('校验失败，请稍后重试。');
    } finally {
      if (requestId === latestRequestId.current) {
        inFlightRef.current = null;
        setIsChecking(false);
      }
    }
  };

  const scheduleValidation = (val: string) => {
    const hasValidated =
      lastValidatedRef.current === val &&
      (checkState === 'available' || checkState === 'unavailable' || checkState === 'error');
    if (hasValidated || pendingRef.current === val || inFlightRef.current === val) return;

    if (timeoutRef.current !== null) window.clearTimeout(timeoutRef.current);
    pendingRef.current = val;
    setIsChecking(true);

    timeoutRef.current = window.setTimeout(() => {
      pendingRef.current = null;
      void validate(val);
    }, CHECK_DEBOUNCE_MS);
  };

  const applyLocalIssue = (issue: ProblemCode | null) => {
    if (issue === null) return false;
    cancelValidation();
    lastValidatedRef.current = null;
    setCheckState('unavailable');
    setCheckMessage(getMessage([issue]));
    return true;
  };

  const handleChange = (val: string) => {
    setValue(val);

    if (!isBlurred) return;

    const normalized = val.trim();
    if (normalized === '') {
      cancelValidation();
      lastValidatedRef.current = null;
      if (allowEmpty) {
        setCheckState('idle');
        setCheckMessage('');
      } else {
        applyLocalIssue(blankCodeByField[fieldKey]);
      }
      return;
    }

    if (applyLocalIssue(getLocalIssue(normalized))) return;
    scheduleValidation(normalized);
  };

  const handleBlur = () => {
    setIsBlurred(true);
    if (normalizedValue === '') {
      cancelValidation();
      lastValidatedRef.current = null;
      if (allowEmpty) {
        setCheckState('idle');
        setCheckMessage('');
      } else {
        applyLocalIssue(blankCodeByField[fieldKey]);
      }
      return;
    }

    if (applyLocalIssue(getLocalIssue(normalizedValue))) return;
    scheduleValidation(normalizedValue);
  };

  return {
    value,
    onChange: handleChange,
    onBlur: handleBlur,
    isBlurred,
    checkState,
    checkMessage,
    isChecking,
    isAvailable: checkState === 'available',
    normalizedValue,
  };
}

export default function useRegister() {
  const navigate = useNavigate();
  const [step, setStep] = useState<1 | 2>(1);
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [isPasswordBlurred, setIsPasswordBlurred] = useState(false);
  const [isConfirmPasswordBlurred, setIsConfirmPasswordBlurred] = useState(false);
  const [submitErrorMessage, setSubmitErrorMessage] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const username = useFieldCheck('username');
  const email = useFieldCheck('email');
  const phone = useFieldCheck('phone');

  const passwordsMatch =
    password.trim() !== '' && confirmPassword.trim() !== '' && password === confirmPassword;
  const passwordMismatchMessage =
    isConfirmPasswordBlurred &&
    password.trim() !== '' &&
    confirmPassword.trim() !== '' &&
    !passwordsMatch
      ? '两次输入的密码不一致。'
      : '';

  const passwordIssue: ProblemCode | null = (() => {
    if (password.trim() === '') return 'INPUT_PASSWORD_BLANK';
    if (password.length < PASSWORD_MIN_LENGTH || password.length > PASSWORD_MAX_LENGTH) {
      return 'INPUT_PASSWORD_BAD_LENGTH';
    }
    if (!PASSWORD_PATTERN.test(password)) return 'INPUT_PASSWORD_BAD_FORMAT';
    return null;
  })();

  const passwordMessage =
    isPasswordBlurred && passwordIssue
      ? passwordIssue === 'INPUT_PASSWORD_BLANK'
        ? '密码不能为空。'
        : passwordIssue === 'INPUT_PASSWORD_BAD_LENGTH'
          ? '密码长度需为6到20位。'
          : '密码必须同时包含字母和数字。'
      : '';

  const isStep1Valid =
    username.normalizedValue !== '' &&
    username.isAvailable &&
    passwordIssue === null &&
    confirmPassword.trim() !== '' &&
    passwordsMatch;

  const canProceedToStep2 =
    username.normalizedValue !== '' &&
    username.isAvailable &&
    password.trim() !== '' &&
    confirmPassword.trim() !== '';

  const hasAnyContact = email.normalizedValue !== '' || phone.normalizedValue !== '';
  const hasAvailableContact =
    (email.normalizedValue !== '' && email.isAvailable) ||
    (phone.normalizedValue !== '' && phone.isAvailable);
  const hasConfirmedUnavailableContact =
    (email.normalizedValue !== '' && email.isBlurred && email.checkState === 'unavailable') ||
    (phone.normalizedValue !== '' && phone.isBlurred && phone.checkState === 'unavailable');

  const canSubmit =
    !isSubmitting &&
    hasAnyContact &&
    (hasAvailableContact || !email.isBlurred || !phone.isBlurred) &&
    !hasConfirmedUnavailableContact;

  const handleNextStep = (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setIsPasswordBlurred(true);
    setIsConfirmPasswordBlurred(true);
    if (isStep1Valid) setStep(2);
  };

  const handlePrevStep = () => {
    setStep(1);
  };

  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (!canSubmit) return;

    setIsSubmitting(true);
    setSubmitErrorMessage('');
    const request: RegisterRequest = {
      username: username.normalizedValue,
      password: password,
      email: email.normalizedValue || null,
      phone: phone.normalizedValue || null,
    };

    try {
      await refreshCsrfToken();
      await apiClient.post('/auth/register', request);
      navigate('/auth/login', { replace: true });
    } catch (e) {
      if (e instanceof BizError) {
        switch (e.detail.code) {
          case 'CONFLICT_USERNAME_EXISTS':
            setSubmitErrorMessage('用户名已存在。');
            break;
          case 'CONFLICT_EMAIL_EXISTS':
            setSubmitErrorMessage('邮箱已存在。');
            break;
          case 'CONFLICT_PHONE_EXISTS':
            setSubmitErrorMessage('手机号已存在。');
            break;
          case 'INPUT_NO_CONTACT':
            setSubmitErrorMessage('请至少填写邮箱或手机号。');
            break;
          case 'INPUT_EMAIL_BAD_FORMAT':
            setSubmitErrorMessage('邮箱格式不正确。');
            break;
          case 'INPUT_PHONE_BAD_FORMAT':
            setSubmitErrorMessage('手机号格式不正确。');
            break;
          case 'INPUT_USERNAME_BAD_LENGTH':
            setSubmitErrorMessage('用户名长度不能超过20位。');
            break;
          case 'INPUT_PASSWORD_BAD_LENGTH':
            setSubmitErrorMessage('密码长度需为6到20位。');
            break;
          case 'INPUT_PASSWORD_BAD_FORMAT':
            setSubmitErrorMessage('密码必须同时包含字母和数字。');
            break;
          default:
            setSubmitErrorMessage('注册失败，请稍后重试。');
        }
      } else {
        setSubmitErrorMessage('网络异常，请稍后重试。');
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  return {
    step,
    username,
    email,
    phone,
    password,
    setPassword,
    passwordMessage,
    handlePasswordBlur: () => setIsPasswordBlurred(true),
    confirmPassword,
    setConfirmPassword,
    passwordMismatchMessage,
    isConfirmPasswordBlurred,
    handleConfirmPasswordBlur: () => setIsConfirmPasswordBlurred(true),
    canProceedToStep2,
    canSubmit,
    submitErrorMessage,
    isSubmitting,
    handleNextStep,
    handlePrevStep,
    handleSubmit,
  };
}
