package com.eva.evagita.event;

import com.eva.evagita.model.NotificationType;

public record TaskEvent(
        NotificationType type,
        Long userId,
        Long taskId,
        String taskTitle
) {
}
