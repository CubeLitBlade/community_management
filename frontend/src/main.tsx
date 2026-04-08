import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { FluentProvider, webDarkTheme, webLightTheme } from '@fluentui/react-components';
import './index.css';
import App from './App.tsx';
import { AccountProvider } from './context/AccountProvider.tsx';

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <FluentProvider theme={webDarkTheme}>
      <AccountProvider>
        <App />
      </AccountProvider>
    </FluentProvider>
  </StrictMode>,
);
