package com.cresensolutions.userservice.util;

import java.time.OffsetDateTime;
import java.time.ZoneId;

public final class DateTimeUtil {

    private static final ZoneId IST_ZONE = ZoneId.of("Asia/Kolkata");

    private DateTimeUtil() {
    }

    public static OffsetDateTime nowInIst() {
        return OffsetDateTime.now(IST_ZONE);
    }
}
