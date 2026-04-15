import { createContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import {
  teamsDarkTheme,
  teamsDarkV21Theme,
  teamsHighContrastTheme,
  teamsLightTheme,
  teamsLightV21Theme,
  webDarkTheme,
  webLightTheme,
  type Theme,
} from '@fluentui/react-components';

export type ThemeAppearance = 'light' | 'dark' | 'contrast';
export type ThemePreference =
  | 'system'
  | 'web-light'
  | 'web-dark'
  | 'teams-light'
  | 'teams-dark'
  | 'teams-light-v2'
  | 'teams-dark-v2'
  | 'teams-high-contrast';
export type ResolvedTheme = 'light' | 'dark';

export type ThemeOption = {
  value: Exclude<ThemePreference, 'system'>;
  label: string;
  description: string;
  appearance: ThemeAppearance;
  theme: Theme;
};

type ThemeContextValue = {
  preference: ThemePreference;
  resolvedTheme: ResolvedTheme;
  resolvedAppearance: ThemeAppearance;
  fluentTheme: Theme;
  activeThemeLabel: string;
  themeOptions: ThemeOption[];
  setPreference: (value: ThemePreference) => void;
};

const STORAGE_KEY = 'themePreference';

const themeOptions: ThemeOption[] = [
  {
    value: 'web-light',
    label: 'Web Light',
    description: 'Fluent 的默认浅色风格，清爽、中性，适合作为日常主界面。',
    appearance: 'light',
    theme: webLightTheme,
  },
  {
    value: 'teams-light',
    label: 'Teams Light',
    description: '更偏 Teams 风格的浅色主题，品牌感更强一些。',
    appearance: 'light',
    theme: teamsLightTheme,
  },
  {
    value: 'teams-light-v2',
    label: 'Teams Light V2',
    description: '新版 Teams 浅色主题，层级和对比更现代一些。',
    appearance: 'light',
    theme: teamsLightV21Theme,
  },
  {
    value: 'web-dark',
    label: 'Web Dark',
    description: 'Fluent 默认深色主题，视觉更稳，适合夜间使用。',
    appearance: 'dark',
    theme: webDarkTheme,
  },
  {
    value: 'teams-dark',
    label: 'Teams Dark',
    description: 'Teams 风格深色主题，对品牌色的使用更明显。',
    appearance: 'dark',
    theme: teamsDarkTheme,
  },
  {
    value: 'teams-dark-v2',
    label: 'Teams Dark V2',
    description: '新版 Teams 深色主题，适合想要更强界面对比的场景。',
    appearance: 'dark',
    theme: teamsDarkV21Theme,
  },
  {
    value: 'teams-high-contrast',
    label: 'High Contrast',
    description: '高对比主题，优先保证辨识度和可访问性。',
    appearance: 'contrast',
    theme: teamsHighContrastTheme,
  },
];

const selectableThemeValues = themeOptions.map((option) => option.value);

function getStoredPreference(): ThemePreference {
  if (typeof window === 'undefined') {
    return 'system';
  }

  const storedPreference = window.localStorage.getItem(STORAGE_KEY);
  if (storedPreference === 'system') {
    return storedPreference;
  }

  return selectableThemeValues.includes(storedPreference as ThemeOption['value'])
    ? (storedPreference as ThemePreference)
    : 'system';
}

function getSystemTheme(): ResolvedTheme {
  if (typeof window === 'undefined') {
    return 'light';
  }

  return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
}

function resolveThemePreference(preference: ThemePreference, systemTheme: ResolvedTheme) {
  if (preference === 'system') {
    return {
      appearance: systemTheme,
      label: `跟随系统 (${systemTheme === 'dark' ? '当前为深色' : '当前为浅色'})`,
      theme: systemTheme === 'dark' ? webDarkTheme : webLightTheme,
    };
  }

  const matchedOption = themeOptions.find((option) => option.value === preference) ?? themeOptions[0];

  return {
    appearance: matchedOption.appearance,
    label: matchedOption.label,
    theme: matchedOption.theme,
  };
}

export const ThemeContext = createContext<ThemeContextValue | undefined>(undefined);

export function ThemeProvider({ children }: { children: ReactNode }) {
  const [preference, setPreference] = useState<ThemePreference>(() => getStoredPreference());
  const [systemTheme, setSystemTheme] = useState<ResolvedTheme>(() => getSystemTheme());

  useEffect(() => {
    const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)');
    const updateSystemTheme = (event?: MediaQueryListEvent) => {
      setSystemTheme((event?.matches ?? mediaQuery.matches) ? 'dark' : 'light');
    };

    updateSystemTheme();
    mediaQuery.addEventListener('change', updateSystemTheme);

    return () => {
      mediaQuery.removeEventListener('change', updateSystemTheme);
    };
  }, []);

  useEffect(() => {
    window.localStorage.setItem(STORAGE_KEY, preference);
  }, [preference]);

  const resolvedConfig = resolveThemePreference(preference, systemTheme);
  const resolvedTheme: ResolvedTheme =
    resolvedConfig.appearance === 'dark' ? 'dark' : 'light';

  useEffect(() => {
    document.documentElement.dataset.theme = resolvedTheme;
  }, [resolvedTheme]);

  const value = useMemo<ThemeContextValue>(
    () => ({
      preference,
      resolvedTheme,
      resolvedAppearance: resolvedConfig.appearance,
      fluentTheme: resolvedConfig.theme,
      activeThemeLabel: resolvedConfig.label,
      themeOptions,
      setPreference,
    }),
    [preference, resolvedConfig, resolvedTheme],
  );

  return <ThemeContext.Provider value={value}>{children}</ThemeContext.Provider>;
}
