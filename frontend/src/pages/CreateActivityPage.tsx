import * as React from 'react';
import {
  Body1,
  Button,
  Caption1,
  Card,
  CardHeader,
  Field,
  Input,
  Subtitle2,
  Textarea,
  Title2,
  makeStyles,
  tokens,
} from '@fluentui/react-components';
import { DatePicker } from '@fluentui/react-datepicker-compat';
import { TimePicker, formatDateToTimeString } from '@fluentui/react-timepicker-compat';
import { useNavigate } from 'react-router';
import useAuth from '../hooks/useAuth';
import useCreateActivity from '../hooks/useCreateActivity';

const useStyles = makeStyles({
  page: {
    width: 'min(100%, 64rem)',
    margin: '0 auto',
    padding: `${tokens.spacingVerticalXL} ${tokens.spacingHorizontalL}`,
    display: 'grid',
    gap: tokens.spacingVerticalL,
  },
  hero: {
    display: 'grid',
    gap: tokens.spacingVerticalS,
  },
  board: {
    display: 'grid',
    gridTemplateColumns: 'minmax(0, 1.45fr) minmax(16rem, 0.8fr)',
    gap: tokens.spacingHorizontalL,
    alignItems: 'start',
    '@media (max-width: 960px)': {
      gridTemplateColumns: '1fr',
    },
  },
  formCardBody: {
    padding: tokens.spacingHorizontalXL,
    display: 'grid',
    gap: tokens.spacingVerticalL,
  },
  formGrid: {
    display: 'grid',
    gap: tokens.spacingVerticalM,
  },
  dateTimeGrid: {
    display: 'grid',
    gridTemplateColumns: 'minmax(11rem, 16rem) minmax(8.5rem, 10rem)',
    gap: tokens.spacingHorizontalM,
    justifyContent: 'start',
    alignItems: 'start',
    '@media (max-width: 640px)': {
      gridTemplateColumns: '1fr',
    },
  },
  pickerCell: {
    minWidth: 0,
    width: '100%',
  },
  datePickerControl: {
    width: '100%',
    minWidth: 0,
    maxWidth: '100%',
  },
  aside: {
    display: 'grid',
    gap: tokens.spacingVerticalL,
  },
  asideBody: {
    padding: tokens.spacingHorizontalL,
    display: 'grid',
    gap: tokens.spacingVerticalM,
  },
  muted: {
    color: tokens.colorNeutralForeground2,
    whiteSpace: 'pre-wrap',
    lineHeight: tokens.lineHeightBase300,
  },
  actions: {
    display: 'flex',
    justifyContent: 'space-between',
    gap: tokens.spacingHorizontalM,
    flexWrap: 'wrap',
  },
});

type FormState = {
  title: string;
  description: string;
  location: string;
  registrationDate: Date | null;
  registrationTime: Date | null;
  registrationTimeText: string;
  startDate: Date | null;
  startTime: Date | null;
  startTimeText: string;
  endDate: Date | null;
  endTime: Date | null;
  endTimeText: string;
};

function normalizeToFiveMinutes(value: Date) {
  const normalized = new Date(value);
  normalized.setSeconds(0, 0);
  normalized.setMinutes(Math.floor(normalized.getMinutes() / 5) * 5);
  return normalized;
}

function buildDefaultForm(): FormState {
  const now = normalizeToFiveMinutes(new Date());
  const deadline = normalizeToFiveMinutes(new Date(now.getTime() + 24 * 60 * 60 * 1000));
  const start = normalizeToFiveMinutes(new Date(now.getTime() + 48 * 60 * 60 * 1000));
  const end = normalizeToFiveMinutes(new Date(now.getTime() + 50 * 60 * 60 * 1000));

  return {
    title: '',
    description: '',
    location: '',
    registrationDate: deadline,
    registrationTime: deadline,
    registrationTimeText: formatDateToTimeString(deadline),
    startDate: start,
    startTime: start,
    startTimeText: formatDateToTimeString(start),
    endDate: end,
    endTime: end,
    endTimeText: formatDateToTimeString(end),
  };
}

function mergeDateAndTime(date: Date | null, time: Date | null) {
  if (!date || !time) {
    return null;
  }

  const combined = new Date(date);
  combined.setHours(time.getHours(), time.getMinutes(), 0, 0);
  return combined;
}

function combineDateTime(date: Date | null, time: Date | null) {
  const combined = mergeDateAndTime(date, time);
  return combined ? combined.toISOString() : '';
}

function syncDatePart(nextDate: Date | null, currentTime: Date | null) {
  if (!nextDate) {
    return null;
  }

  const synced = new Date(nextDate);
  if (currentTime) {
    synced.setHours(currentTime.getHours(), currentTime.getMinutes(), 0, 0);
  } else {
    synced.setHours(0, 0, 0, 0);
  }
  return synced;
}

function syncTimePart(nextTime: Date | null, currentDate: Date | null) {
  if (!nextTime) {
    return null;
  }

  const synced = currentDate ? new Date(currentDate) : new Date(nextTime);
  synced.setHours(nextTime.getHours(), nextTime.getMinutes(), 0, 0);
  return synced;
}

function formatDate(value?: Date) {
  if (!value) {
    return '';
  }
  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  }).format(value);
}

export default function CreateActivityPage() {
  const styles = useStyles();
  const navigate = useNavigate();
  const { profile, isLoading } = useAuth();
  const { isCreating, createErrorMessage, createActivity } = useCreateActivity();
  const [form, setForm] = React.useState<FormState>(buildDefaultForm);

  React.useEffect(() => {
    if (!isLoading && !profile) {
      navigate('/auth/login', { replace: true });
    }
  }, [isLoading, navigate, profile]);

  const updateForm = <Key extends keyof FormState>(key: Key, value: FormState[Key]) => {
    setForm((current) => ({ ...current, [key]: value }));
  };

  const updateDateTimeDate = (
    dateKey: 'registrationDate' | 'startDate' | 'endDate',
    timeKey: 'registrationTime' | 'startTime' | 'endTime',
    nextDate: Date | null | undefined,
  ) => {
    const normalizedDate = nextDate ?? null;
    setForm((current) => ({
      ...current,
      [dateKey]: normalizedDate,
      [timeKey]: syncDatePart(normalizedDate, current[timeKey]),
    }));
  };

  const updateDateTimeTime = (
    dateKey: 'registrationDate' | 'startDate' | 'endDate',
    timeKey: 'registrationTime' | 'startTime' | 'endTime',
    textKey: 'registrationTimeText' | 'startTimeText' | 'endTimeText',
    nextTime: Date | null,
    nextText: string,
  ) => {
    setForm((current) => ({
      ...current,
      [timeKey]: syncTimePart(nextTime, current[dateKey]),
      [textKey]: nextText,
    }));
  };

  const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    const registrationDeadline = combineDateTime(form.registrationDate, form.registrationTime);
    const startTime = combineDateTime(form.startDate, form.startTime);
    const endTime = combineDateTime(form.endDate, form.endTime);

    const location = await createActivity({
      title: form.title.trim(),
      description: form.description.trim(),
      location: form.location.trim(),
      registrationDeadline,
      startTime,
      endTime,
    });

    if (!location) {
      return;
    }

    const match = location.match(/\/(\d+)$/);
    if (match) {
      navigate(`/activities/${match[1]}`);
      return;
    }

    navigate('/activities/about-me');
  };

  return (
    <div className={styles.page}>
      <div className={styles.hero}>
        <Title2>发起活动</Title2>
        <Body1>填写活动信息，提交后进入审核流程。通过审核后，活动才会出现在活动广场。</Body1>
      </div>

      <div className={styles.board}>
        <Card appearance="filled-alternative">
          <CardHeader
            header={<Subtitle2>活动信息</Subtitle2>}
            description={<Caption1>填写标题、时间与地点，确保审核时信息完整可读</Caption1>}
          />
          <div className={styles.formCardBody}>
            <form className={styles.formGrid} onSubmit={handleSubmit}>
              <Field label="活动标题">
                <Input value={form.title} onChange={(_, data) => updateForm('title', data.value)} />
              </Field>

              <Field label="活动简介">
                <Textarea
                  resize="vertical"
                  value={form.description}
                  onChange={(_, data) => updateForm('description', data.value)}
                />
              </Field>

              <Field label="活动地点">
                <Input
                  value={form.location}
                  onChange={(_, data) => updateForm('location', data.value)}
                />
              </Field>

              <Field label="报名截止">
                <div className={styles.dateTimeGrid}>
                  <div className={styles.pickerCell}>
                    <DatePicker
                      className={styles.datePickerControl}
                      value={form.registrationDate}
                      onSelectDate={(date) =>
                        updateDateTimeDate('registrationDate', 'registrationTime', date)
                      }
                      formatDate={formatDate}
                      placeholder="选择日期"
                    />
                  </div>
                  <div className={styles.pickerCell}>
                    <TimePicker
                      freeform
                      dateAnchor={form.registrationDate ?? undefined}
                      selectedTime={form.registrationTime}
                      onTimeChange={(_, data) =>
                        updateDateTimeTime(
                          'registrationDate',
                          'registrationTime',
                          'registrationTimeText',
                          data.selectedTime,
                          data.selectedTimeText ?? '',
                        )
                      }
                      value={form.registrationTimeText}
                      onInput={(event) =>
                        updateForm('registrationTimeText', event.currentTarget.value)
                      }
                      increment={5}
                      placeholder="选择时间"
                    />
                  </div>
                </div>
              </Field>

              <Field label="开始时间">
                <div className={styles.dateTimeGrid}>
                  <div className={styles.pickerCell}>
                    <DatePicker
                      className={styles.datePickerControl}
                      value={form.startDate}
                      onSelectDate={(date) => updateDateTimeDate('startDate', 'startTime', date)}
                      formatDate={formatDate}
                      placeholder="选择日期"
                    />
                  </div>
                  <div className={styles.pickerCell}>
                    <TimePicker
                      freeform
                      dateAnchor={form.startDate ?? undefined}
                      selectedTime={form.startTime}
                      onTimeChange={(_, data) =>
                        updateDateTimeTime(
                          'startDate',
                          'startTime',
                          'startTimeText',
                          data.selectedTime,
                          data.selectedTimeText ?? '',
                        )
                      }
                      value={form.startTimeText}
                      onInput={(event) => updateForm('startTimeText', event.currentTarget.value)}
                      increment={5}
                      placeholder="选择时间"
                    />
                  </div>
                </div>
              </Field>

              <Field label="结束时间">
                <div className={styles.dateTimeGrid}>
                  <div className={styles.pickerCell}>
                    <DatePicker
                      className={styles.datePickerControl}
                      value={form.endDate}
                      onSelectDate={(date) => updateDateTimeDate('endDate', 'endTime', date)}
                      formatDate={formatDate}
                      placeholder="选择日期"
                    />
                  </div>
                  <div className={styles.pickerCell}>
                    <TimePicker
                      freeform
                      dateAnchor={form.endDate ?? undefined}
                      selectedTime={form.endTime}
                      onTimeChange={(_, data) =>
                        updateDateTimeTime(
                          'endDate',
                          'endTime',
                          'endTimeText',
                          data.selectedTime,
                          data.selectedTimeText ?? '',
                        )
                      }
                      value={form.endTimeText}
                      onInput={(event) => updateForm('endTimeText', event.currentTarget.value)}
                      increment={5}
                      placeholder="选择时间"
                    />
                  </div>
                </div>
              </Field>

              {createErrorMessage ? <Caption1>{createErrorMessage}</Caption1> : null}

              <div className={styles.actions}>
                <Button appearance="secondary" onClick={() => navigate('/activities/plaza')}>
                  返回广场
                </Button>
                <Button appearance="primary" type="submit" disabled={isCreating}>
                  {isCreating ? '提交中...' : '提交审核'}
                </Button>
              </div>
            </form>
          </div>
        </Card>

        <div className={styles.aside}>
          <Card>
            <CardHeader
              header={<Subtitle2>填写建议</Subtitle2>}
              description={<Caption1>审核更关注信息是否清晰完整</Caption1>}
            />
            <div className={styles.asideBody}>
              <Caption1 className={styles.muted}>
                标题直接说明活动主题，例如“周末社区羽毛球局”。
                {'\n'}
                地点尽量写到楼栋、场馆或集合点。
                {'\n'}
                简介建议说明对象、流程和需要准备的物品。
              </Caption1>
            </div>
          </Card>

          <Card>
            <CardHeader
              header={<Subtitle2>发布后</Subtitle2>}
              description={<Caption1>活动创建后的后续流程</Caption1>}
            />
            <div className={styles.asideBody}>
              <Caption1 className={styles.muted}>
                提交后状态为“待审核”。
                {'\n'}
                管理员通过后，活动会出现在广场。
                {'\n'}
                审核结果和活动提醒会进入通知中心。
              </Caption1>
              <Button appearance="secondary" onClick={() => navigate('/activities/about-me')}>
                查看我的活动
              </Button>
            </div>
          </Card>
        </div>
      </div>
    </div>
  );
}
