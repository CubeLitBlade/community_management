import { Suspense, lazy } from 'react';
import { Spinner, makeStyles, tokens } from '@fluentui/react-components';
import { createBrowserRouter, RouterProvider } from 'react-router';
import Layout from '../components/Layout/Layout';

const HomePage = lazy(() => import('../pages/HomePage'));
const FeedPage = lazy(() => import('../pages/FeedPage'));
const PostDetailPage = lazy(() => import('../pages/PostDetailPage'));
const LoginPage = lazy(() => import('../pages/LoginPage'));
const RegisterPage = lazy(() => import('../pages/RegisterPage'));
const SettingsPage = lazy(() => import('../pages/SettingsPage'));
const ChangePasswordPage = lazy(() => import('../pages/ChangePasswordPage'));
const SettingsChangePasswordPage = lazy(() => import('../pages/SettingsChangePasswordPage'));
const NotificationsPage = lazy(() => import('../pages/NotificationsPage'));
const RepliesPage = lazy(() => import('../pages/RepliesPage'));
const LikesPage = lazy(() => import('../pages/LikesPage'));
const MessagesPage = lazy(() => import('../pages/MessagesPage'));
const ActivityPlazaPage = lazy(() => import('../pages/ActivityPlazaPage'));
const CreateActivityPage = lazy(() => import('../pages/CreateActivityPage'));
const ActivityDetailPage = lazy(() => import('../pages/ActivityDetailPage'));
const MyActivitiesPage = lazy(() => import('../pages/MyActivitiesPage'));
const PendingActivitiesPage = lazy(() => import('../pages/PendingActivitiesPage'));
const AccountsManagementPage = lazy(() => import('../pages/AccountsManagementPage'));

const useStyles = makeStyles({
  fallback: {
    minHeight: '40vh',
    display: 'grid',
    placeItems: 'center',
    color: tokens.colorNeutralForeground2,
  },
});

function RouteFallback() {
  const styles = useStyles();

  return (
    <div className={styles.fallback}>
      <Spinner label="正在加载页面" />
    </div>
  );
}

function withSuspense(element: React.ReactNode) {
  return <Suspense fallback={<RouteFallback />}>{element}</Suspense>;
}

const router = createBrowserRouter([
  {
    path: '/',
    element: withSuspense(<Layout />),
    children: [
      {
        path: '/',
        element: withSuspense(<HomePage />),
      },
      {
        path: '/feed',
        element: withSuspense(<FeedPage />),
      },
      {
        path: '/posts/:postId',
        element: withSuspense(<PostDetailPage />),
      },
      {
        path: '/settings',
        element: withSuspense(<SettingsPage />),
      },
      {
        path: '/settings/password',
        element: withSuspense(<SettingsChangePasswordPage />),
      },
      {
        path: '/replies',
        element: withSuspense(<RepliesPage />),
      },
      {
        path: '/likes',
        element: withSuspense(<LikesPage />),
      },
      {
        path: '/messages',
        element: withSuspense(<MessagesPage />),
      },
      {
        path: '/notifications',
        element: withSuspense(<NotificationsPage />),
      },
      {
        path: '/activities/plaza',
        element: withSuspense(<ActivityPlazaPage />),
      },
      {
        path: '/activities/create',
        element: withSuspense(<CreateActivityPage />),
      },
      {
        path: '/activities/about-me',
        element: withSuspense(<MyActivitiesPage />),
      },
      {
        path: '/activities/review',
        element: withSuspense(<PendingActivitiesPage />),
      },
      {
        path: '/activities/:activityId',
        element: withSuspense(<ActivityDetailPage />),
      },
      {
        path: '/management/accounts',
        element: withSuspense(<AccountsManagementPage />),
      },
    ],
  },
  {
    path: '/auth/login',
    element: withSuspense(<LoginPage />),
  },
  {
    path: '/auth/register',
    element: withSuspense(<RegisterPage />),
  },
  {
    path: '/account/change-password',
    element: withSuspense(<ChangePasswordPage />),
  },
]);

export default function App() {
  return <RouterProvider router={router} />;
}
