import { createBrowserRouter, RouterProvider } from 'react-router';
import HomePage from '../pages/HomePage';
import LoginPage from '../pages/LoginPage';
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
    ],
  },
  {
    path: '/auth/login',
    element: <LoginPage />,
  },
]);

export default function App() {
  return <RouterProvider router={router} />;
}
