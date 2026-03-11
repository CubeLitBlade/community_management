package io.github.cubelitblade.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {
    private Long id;

    private String type;

    private String params;

    @Builder.Default
    private String status = "pending";

    private Integer retryCount;

    private String errorMsg;

    private LocalDateTime createdAt;

    private LocalDateTime nextRunAt;

    private LocalDateTime updatedAt;
}
