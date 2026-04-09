package io.github.cubelitblade.common.exception;

import io.github.cubelitblade.event.exception.InvalidParameterException;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExceptionFactory {
  private final MessageSource messageSource;

  public InvalidParameterException onUnknownEventType(String passedEventType) {
    return new InvalidParameterException(
        messageSource.getMessage(
            "exception.invalid-parameter.unknown-event-type.title",
            new Object[] {passedEventType},
            Locale.getDefault()),
        messageSource.getMessage(
            "exception.invalid-parameter.unknown-event-type.detail", null, Locale.getDefault()));
  }
}
