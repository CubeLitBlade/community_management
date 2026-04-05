import { Body1, Card, Title2, makeStyles, tokens } from '@fluentui/react-components';

const useStyles = makeStyles({
  root: {
    width: '100%',
    display: 'grid',
    gap: tokens.spacingVerticalL,
  },
  heroCard: {
    width: '100%',
    maxWidth: '52rem',
    padding: tokens.spacingHorizontalXXL,
    borderRadius: tokens.borderRadiusXLarge,
    backgroundColor: tokens.colorNeutralBackground1,
    boxShadow: tokens.shadow4,
    border: `1px solid ${tokens.colorNeutralStroke2}`,
  },
  title: {
    marginTop: '0',
    marginBottom: tokens.spacingVerticalS,
  },
  paragraph: {
    color: tokens.colorNeutralForeground2,
    lineHeight: tokens.lineHeightBase400,
  },
});

export default function HomePage() {
  const styles = useStyles();

  return (
    <div className={styles.root}>
      <Card className={styles.heroCard}>
        <Title2 className={styles.title}>社区主页</Title2>
        <Body1 className={styles.paragraph}>
          这里会展示社区动态、推荐活动和与你相关的更新。当前内容仍是占位，后续可直接替换为
          数据接口返回的卡片列表。
        </Body1>
      </Card>
    </div>
  );
}
