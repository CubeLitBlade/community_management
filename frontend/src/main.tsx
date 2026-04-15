import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { FluentProvider } from '@fluentui/react-components';
import './index.css';
import App from './App.tsx';
import { AccountProvider } from './context/AccountProvider.tsx';
import { ThemeProvider } from './context/ThemeContext.tsx';
import useTheme from './hooks/useTheme.ts';

function RootProviders() {
  const { fluentTheme } = useTheme();

  return (
    <FluentProvider theme={fluentTheme}>
      <AccountProvider>
        <App />
      </AccountProvider>
    </FluentProvider>
  );
}

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <ThemeProvider>
      <RootProviders />
    </ThemeProvider>
  </StrictMode>,
);
