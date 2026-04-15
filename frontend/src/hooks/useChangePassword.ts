import { useState, type FormEvent } from 'react';
import { BizError } from '../types/Error';
import useAuth from './useAuth';

type SubmitResult = 'success' | 'logged_out' | null;

export default function useChangePassword() {
  const { changePassword, logout } = useAuth();
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [currentPasswordMessage, setCurrentPasswordMessage] = useState('');
  const [newPasswordMessage, setNewPasswordMessage] = useState('');
  const [submitErrorMessage, setSubmitErrorMessage] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleCurrentPasswordChange = (value: string) => {
    setCurrentPassword(value);
    setCurrentPasswordMessage('');
    setSubmitErrorMessage('');
  };

  const handleNewPasswordChange = (value: string) => {
    setNewPassword(value);
    setNewPasswordMessage('');
    setSubmitErrorMessage('');
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>): Promise<SubmitResult> => {
    event.preventDefault();

    if (currentPassword.trim() === '') {
      setCurrentPasswordMessage('当前密码不能为空。');
      setNewPasswordMessage('');
      setSubmitErrorMessage('');
      return null;
    }

    if (newPassword.trim() === '') {
      setCurrentPasswordMessage('');
      setNewPasswordMessage('新密码不能为空。');
      setSubmitErrorMessage('');
      return null;
    }

    setIsSubmitting(true);
    setCurrentPasswordMessage('');
    setNewPasswordMessage('');
    setSubmitErrorMessage('');

    try {
      await changePassword(currentPassword, newPassword);
      return 'success';
    } catch (error) {
      if (error instanceof BizError) {
        switch (error.detail.code) {
          case 'INPUT_PASSWORD_BLANK':
            setNewPasswordMessage('新密码不能为空。');
            break;
          case 'INPUT_PASSWORD_BAD_LENGTH':
            setNewPasswordMessage('密码长度需为 6 到 20 位。');
            break;
          case 'INPUT_PASSWORD_BAD_FORMAT':
            setNewPasswordMessage('密码必须包含字母和数字。');
            break;
          case 'LOGIN_FAILED_INVALID_CREDENTIALS':
            setCurrentPasswordMessage('当前密码不正确。');
            break;
          case 'UNAUTHORIZED':
          case 'INVALID_TOKEN':
            await logout();
            return 'logged_out';
          default:
            setSubmitErrorMessage('修改失败，请稍后重试。');
        }
      } else {
        setSubmitErrorMessage('网络异常，请稍后重试。');
      }

      return null;
    } finally {
      setIsSubmitting(false);
    }
  };

  return {
    currentPassword,
    setCurrentPassword: handleCurrentPasswordChange,
    newPassword,
    setNewPassword: handleNewPasswordChange,
    currentPasswordMessage,
    newPasswordMessage,
    submitErrorMessage,
    isSubmitting,
    handleSubmit,
  };
}
