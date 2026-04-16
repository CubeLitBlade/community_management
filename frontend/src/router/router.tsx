import { createBrowserRouter, RouterProvider } from 'react-router';
import HomePage from '../pages/HomePage';
import FeedPage from '../pages/FeedPage';
import PostDetailPage from '../pages/PostDetailPage';
import LoginPage from '../pages/LoginPage';
import RegisterPage from '../pages/RegisterPage';
import SettingsPage from '../pages/SettingsPage';
import ChangePasswordPage from '../pages/ChangePasswordPage';
import SettingsChangePasswordPage from '../pages/SettingsChangePasswordPage';
import NotificationsPage from '../pages/NotificationsPage';
import RepliesPage from '../pages/RepliesPage';
import LikesPage from '../pages/LikesPage';
import Layout from '../components/Layout/Layout';

const router = createBrowserRouter([
  {
    path: '/',
    element: <Layout />,
    children: [
      {
        path: '/',
        element: <HomePage />,
      },
      {
        path: '/feed',
        element: <FeedPage />,
      },
      {
        path: '/posts/:postId',
        element: <PostDetailPage />,
      },
      {
        path: '/settings',
        element: <SettingsPage />,
      },
      {
        path: '/settings/password',
        element: <SettingsChangePasswordPage />,
      },
      {
        path: '/replies',
        element: <RepliesPage />,
      },
      {
        path: '/likes',
        element: <LikesPage />,
      },
      {
        path: '/notifications',
        element: <NotificationsPage />,
      },
    ],
  },
  {
    path: '/auth/login',
    element: <LoginPage />,
  },
  {
    path: '/auth/register',
    element: <RegisterPage />,
  },
  {
    path: '/account/change-password',
    element: <ChangePasswordPage />,
  },
]);

export default function App() {
  return <RouterProvider router={router} />;
}
