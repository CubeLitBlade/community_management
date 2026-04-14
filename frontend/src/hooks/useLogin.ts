import { useNavigate } from 'react-router';
import { useState, type FormEvent } from 'react';
import { BizError } from '../types/Error';
import useAuth from './useAuth';

export default function useLogin() {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [usernameMessage, setUsernameMessage] = useState('');
  const [passwordMessage, setPasswordMessage] = useState('');
  const [submitErrorMessage, setSubmitErrorMessage] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleUsernameChange = (value: string) => {
    setUsername(value);
    setUsernameMessage('');
    setSubmitErrorMessage('');
  };

  const handlePasswordChange = (value: string) => {
    setPassword(value);
    setPasswordMessage('');
    setSubmitErrorMessage('');
  };

  const handleLoginSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();

    if (username.trim() === '') {
      setUsernameMessage('用户名不能为空。');
      setPasswordMessage('');
      setSubmitErrorMessage('');
      return;
    }

    if (password.trim() === '') {
      setPasswordMessage('密码不能为空。');
      setUsernameMessage('');
      setSubmitErrorMessage('');
      return;
    }

    setIsSubmitting(true);
    setUsernameMessage('');
    setPasswordMessage('');
    setSubmitErrorMessage('');

    const result = await requestLogin(username, password);
    if (result) {
      navigate('/');
      return;
    }

    setIsSubmitting(false);
  };

  async function requestLogin(username: string, password: string): Promise<boolean> {
    try {
      await login(username, password);
      return true;
    } catch (e) {
      if (e instanceof BizError) {
        switch (e.detail.code) {
          case 'LOGIN_FAILED_INVALID_CREDENTIALS':
            setSubmitErrorMessage('用户名或密码错误。');
            break;
          case 'LOGIN_FAILED_SUSPENDED':
            setSubmitErrorMessage('账号已被停用，请联系管理员。');
            break;
          case 'LOGIN_FAILED_ARCHIVED':
            setSubmitErrorMessage('账号已归档，无法登录。');
            break;
          default:
            setSubmitErrorMessage('请重试。');
        }

        return false;
      }

      setSubmitErrorMessage('网络异常，请稍后重试。');
      return false;
    }
  }

  return {
    username,
    setUsername: handleUsernameChange,
    password,
    setPassword: handlePasswordChange,
    usernameMessage,
    passwordMessage,
    submitErrorMessage,
    isSubmitting,
    handleLoginSubmit,
  };
}
