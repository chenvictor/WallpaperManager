package cvic.wallpapermanager.utils;

import java.util.concurrent.TimeUnit;

public class TimeUtils {

    private static final int MINUTE_INDEX = 0;
    private static final int HOUR_INDEX = 1;
    private static final int DAY_INDEX = 2;

    public static TimeUnit getTimeUnit(int idx) {
        switch (idx) {
            case MINUTE_INDEX:
                return TimeUnit.MINUTES;
            case HOUR_INDEX:
                return TimeUnit.HOURS;
            case DAY_INDEX:
                return TimeUnit.DAYS;
        }
        throw new IllegalArgumentException("Index must in in range [0, 2]");
    }

}
