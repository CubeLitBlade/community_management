import {
  Badge,
  Body1,
  Button,
  Card,
  CardHeader,
  Divider,
  Dropdown,
  Field,
  Option,
  OptionGroup,
  Subtitle2,
  Text,
  Title2,
  makeStyles,
  tokens,
} from '@fluentui/react-components';
import { ArrowExitRegular, KeyRegular } from '@fluentui/react-icons';
import { useNavigate } from 'react-router';
import useAuth from '../hooks/useAuth';
import useTheme from '../hooks/useTheme';
import type { ThemeAppearance } from '../context/ThemeContext';

const useStyles = makeStyles({
  page: {
    width: 'min(100%, 46rem)',
    margin: '0 auto',
    padding: `${tokens.spacingVerticalXXL} ${tokens.spacingHorizontalL}`,
    display: 'grid',
    gap: tokens.spacingVerticalXL,
  },
  section: {
    display: 'grid',
    gap: tokens.spacingVerticalM,
  },
  card: {
    maxWidth: '40rem',
    padding: tokens.spacingHorizontalL,
    borderRadius: tokens.borderRadiusXLarge,
    backgroundColor: tokens.colorNeutralBackground1,
    border: `1px solid ${tokens.colorNeutralStroke1}`,
    boxShadow: tokens.shadow8,
  },
  field: {
    maxWidth: '24rem',
  },
  preview: {
    maxWidth: '32rem',
    padding: `${tokens.spacingVerticalM} ${tokens.spacingHorizontalL}`,
    borderRadius: tokens.borderRadiusLarge,
    backgroundColor: tokens.colorNeutralBackground2,
    border: `1px solid ${tokens.colorNeutralStroke2}`,
    display: 'grid',
    gap: tokens.spacingVerticalS,
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
  accountActions: {
    display: 'grid',
    gap: tokens.spacingVerticalS,
  },
  actionRow: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: tokens.spacingHorizontalM,
    padding: `${tokens.spacingVerticalM} 0`,
    flexWrap: 'wrap',
  },
  actionMeta: {
    display: 'grid',
    gap: tokens.spacingVerticalXXS,
  },
  actionDescription: {
    color: tokens.colorNeutralForeground3,
  },
  sectionIntro: {
    color: tokens.colorNeutralForeground2,
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
  const navigate = useNavigate();
  const { profile, logout } = useAuth();
  const { preference, setPreference, themeOptions } = useTheme();

  const appearanceOrder: ThemeAppearance[] = ['light', 'dark', 'contrast'];
  const selectedLabel =
    preference === 'system' ? '跟随系统' : (optionLabels[preference] ?? '跟随系统');

  return (
    <div className={styles.page}>
      <div>
        <Title2>设置</Title2>
      </div>
      {profile ? (
        <section className={styles.section}>
          <Subtitle2>账户</Subtitle2>
          <Text className={styles.sectionIntro}>
            修改密码、结束当前会话，以及管理与你账户相关的安全操作。
          </Text>
          <Card className={styles.card}>
            <div className={styles.accountActions}>
              <CardHeader
                header={<Text weight="semibold">安全与会话</Text>}
                description={
                  <Text className={styles.actionDescription}>管理密码和当前账户会话。</Text>
                }
              />
              <div className={styles.actionRow}>
                <div className={styles.actionMeta}>
                  <Text weight="medium">修改密码</Text>
                  <Text className={styles.actionDescription}>
                    使用当前密码验证身份后，设置一个新的登录密码。
                  </Text>
                </div>
                <Button
                  appearance="primary"
                  icon={<KeyRegular />}
                  onClick={() => navigate('/settings/password')}
                >
                  修改密码
                </Button>
              </div>
              <Divider />
              <div className={styles.actionRow}>
                <div className={styles.actionMeta}>
                  <Text weight="medium">退出登录</Text>
                  <Text className={styles.actionDescription}>
                    在当前浏览器中结束登录状态，并返回登录页面。
                  </Text>
                </div>
                <Button
                  appearance="secondary"
                  icon={<ArrowExitRegular />}
                  onClick={() => void logout()}
                >
                  退出登录
                </Button>
              </div>
            </div>
          </Card>
        </section>
      ) : null}
      <section className={styles.section}>
        <Subtitle2>外观</Subtitle2>
        <Text className={styles.sectionIntro}>选择你偏好的主题风格，并实时查看组件外观效果。</Text>
        <Card className={styles.card}>
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
        </Card>
      </section>
    </div>
  );
}
