import {
  Badge,
  Body1,
  Button,
  Dropdown,
  Field,
  Option,
  OptionGroup,
  Subtitle2,
  Title2,
  makeStyles,
  tokens,
} from '@fluentui/react-components';
import useTheme from '../hooks/useTheme';
import type { ThemeAppearance } from '../context/ThemeContext';

const useStyles = makeStyles({
  page: {
    width: 'min(100%, 40rem)',
    margin: '0 auto',
    padding: `${tokens.spacingVerticalXXL} ${tokens.spacingHorizontalL}`,
    display: 'grid',
    gap: tokens.spacingVerticalXL,
  },
  section: {
    display: 'grid',
    gap: tokens.spacingVerticalM,
  },
  field: {
    maxWidth: '24rem',
  },
  preview: {
    maxWidth: '32rem',
    padding: tokens.spacingHorizontalL,
    borderRadius: tokens.borderRadiusXLarge,
    backgroundColor: tokens.colorNeutralBackground2,
    border: `1px solid ${tokens.colorNeutralStroke2}`,
    display: 'grid',
    gap: tokens.spacingVerticalM,
  },
  previewHeader: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: tokens.spacingHorizontalM,
  },
  previewBody: {
    display: 'grid',
    gap: tokens.spacingVerticalS,
  },
  previewActions: {
    display: 'flex',
    alignItems: 'center',
    gap: tokens.spacingHorizontalS,
  },
});

const appearanceLabels: Record<ThemeAppearance, string> = {
  light: '浅色',
  dark: '深色',
  contrast: '高对比',
};

const optionLabels: Record<string, string> = {
  'web-light': 'Web',
  'web-dark': 'Web',
  'teams-light': 'Teams',
  'teams-dark': 'Teams',
  'teams-light-v2': 'Teams v2',
  'teams-dark-v2': 'Teams v2',
  'teams-high-contrast': 'High Contrast',
};

export default function SettingsPage() {
  const styles = useStyles();
  const { preference, setPreference, themeOptions } = useTheme();

  const appearanceOrder: ThemeAppearance[] = ['light', 'dark', 'contrast'];
  const selectedLabel =
    preference === 'system' ? '跟随系统' : (optionLabels[preference] ?? '跟随系统');

  return (
    <div className={styles.page}>
      <Title2>设置</Title2>
      <section className={styles.section}>
        <Subtitle2>外观</Subtitle2>
        <Field label="主题" className={styles.field}>
          <Dropdown
            value={selectedLabel}
            selectedOptions={[preference]}
            onOptionSelect={(_, data) => {
              if (data.optionValue) {
                setPreference(data.optionValue as typeof preference);
              }
            }}
          >
            <OptionGroup label="系统">
              <Option value="system">跟随系统</Option>
            </OptionGroup>
            {appearanceOrder.map((appearance) => {
              const groupOptions = themeOptions.filter(
                (option) => option.appearance === appearance,
              );

              if (groupOptions.length === 0) {
                return null;
              }

              return (
                <OptionGroup key={appearance} label={appearanceLabels[appearance]}>
                  {groupOptions.map((option) => (
                    <Option key={option.value} value={option.value}>
                      {optionLabels[option.value]}
                    </Option>
                  ))}
                </OptionGroup>
              );
            })}
          </Dropdown>
        </Field>
        <div className={styles.preview}>
          <div className={styles.previewHeader}>
            <Subtitle2>预览</Subtitle2>
            <Badge appearance="filled" color="brand">
              社区
            </Badge>
          </div>
          <div className={styles.previewBody}>
            <Body1>这是一条文本。</Body1>
          </div>
          <div className={styles.previewActions}>
            <Button appearance="primary" size="small">
              主要按钮
            </Button>
            <Button appearance="secondary" size="small">
              次要按钮
            </Button>
          </div>
        </div>
      </section>
    </div>
  );
}
