import { useNavigate } from 'react-router';
import { useState, type SubmitEvent } from 'react';
import apiClient from '../api/apiClient';
import type { LoginRequest, LoginResponse } from '../types/Account';
import { BizError } from '../types/Error';
import useAccount from './useAccount';

export default function useLogin() {
  const navigate = useNavigate();
  const { fetchAccount } = useAccount();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [errorMessage, setErrorMessage] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleLoginSubmit = async (e: SubmitEvent) => {
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
      await fetchAccount();
      navigate('/');
      return;
    }

    setIsSubmitting(false);
  };

  async function requestLogin(username: string, password: string): Promise<boolean> {
    const request: LoginRequest = {
      username,
      password,
    };

    try {
      const response = await apiClient.post<LoginResponse>('/auth/login', request);
      localStorage.setItem('accessToken', response.data.accessToken);
      return true;
    } catch (e) {
      if (e instanceof BizError) {
        switch (e.detail.code) {
          case 'LOGIN_FAILED_INVALID_CREDENTIALS':
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
