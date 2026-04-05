import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { FluentProvider, webDarkTheme } from '@fluentui/react-components';
import './index.css';
import App from './App.tsx';
import { AuthProvider } from './context/AuthProvider.tsx';

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <FluentProvider theme={webDarkTheme}>
      <AuthProvider>
        <App />
      </AuthProvider>
    </FluentProvider>
  </StrictMode>,
);
