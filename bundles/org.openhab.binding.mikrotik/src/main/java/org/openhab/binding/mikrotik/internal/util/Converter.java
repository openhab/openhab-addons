package org.openhab.binding.mikrotik.internal.util;

import org.apache.commons.lang.NotImplementedException;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.MutablePeriod;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@NonNullByDefault
public class Converter {
    private static final DateTimeFormatter routerosFormat = DateTimeFormat.forPattern("MMM/dd/YYYY kk:mm:ss");

    @Nullable
    public static DateTime fromRouterosTime(@Nullable String dateTimeString){
        if(dateTimeString == null) return null;
        String fixedTs = dateTimeString.substring(0, 1).toUpperCase() + dateTimeString.substring(1);
        return routerosFormat.parseDateTime(fixedTs);
    }

    @Nullable
    public static Period fromRouterosPeriod(@Nullable String durationString){
        if(durationString == null) return null;
        /*
            uptime = 6w6h31m31s
            uptime = 3d7h6m43s710ms
            uptime = 16h39m58s220ms
            uptime = 1h38m53s110ms
            uptime = 53m53s950ms
        */
        Pattern pat = Pattern.compile("(\\d+)([a-z]){1,3}");
        Matcher m = pat.matcher(durationString);
        MutablePeriod per = new MutablePeriod();
        while(m.find()) {
            int amount = Integer.parseInt(m.group(1));
            String period = m.group(2);
            switch (period){
                case "y":
                    per.addYears(amount);
                    break;
                case "w":
                    per.addWeeks(amount);
                    break;
                case "d":
                    per.addDays(amount);
                    break;
                case "h":
                    per.addHours(amount);
                    break;
                case "m":
                    per.addMinutes(amount);
                    break;
                case "s":
                    per.addSeconds(amount);
                    break;
                case "ms":
                    per.addMillis(amount);
                    break;
                default:
                    throw new NotImplementedException(String.format("Unable to parse duration %s - %s is unknown",
                            durationString, period));
            }
        }
        return per.toPeriod();
    }
}
