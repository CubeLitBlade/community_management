import { useNavigate } from 'react-router';
import { useState } from 'react';
import apiClient from '../api/apiClient';
import type { AccountAuthorizeRequest } from '../types/AccountAuthorizeRequest';
import type { AccountLoginResponse } from '../types/AccountLoginResponse';
import { BizError } from '../types/Error';
import useAuth from './useAuth';

type LoginSubmitEvent = React.FormEvent<HTMLFormElement>;

export default function useLogin() {
  const navigate = useNavigate();
  const { markAuthenticated } = useAuth();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [errorMessage, setErrorMessage] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleLoginSubmit = async (e: LoginSubmitEvent) => {
    e.preventDefault();

    if (username.trim() === '') {
      setErrorMessage('用户名不能为空。');
      return;
    }

    if (password.trim() === '') {
      setErrorMessage('密码不能为空。');
      return;
    }

    setIsSubmitting(true);
    setErrorMessage('');

    const result = await requestLogin(username, password);
    if (result) {
      markAuthenticated();
      navigate('/');
      return;
    }

    setIsSubmitting(false);
  };

  async function requestLogin(username: string, password: string): Promise<boolean> {
    const request: AccountAuthorizeRequest = {
      username,
      password,
    };

    try {
      const response: AccountLoginResponse = await apiClient.post('/auth/login', request);
      localStorage.setItem('accessToken', response.accessToken);
      return true;
    } catch (e) {
      if (e instanceof BizError) {
        switch (e.detail.code) {
          case 'INVALID_CREDENTIALS':
            setErrorMessage('用户名或密码错误');
            break;
          default:
            setErrorMessage('请重试。');
        }

        return false;
      }

      setErrorMessage('网络异常，请稍后重试。');
      return false;
    }
  }

  return {
    username,
    setUsername,
    password,
    setPassword,
    errorMessage,
    isSubmitting,
    handleLoginSubmit,
  };
}
