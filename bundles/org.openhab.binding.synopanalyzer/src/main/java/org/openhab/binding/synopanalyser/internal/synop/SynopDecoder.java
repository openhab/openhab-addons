package org.openhab.binding.synopanalyser.internal.synop;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.measure.Unit;
import javax.measure.quantity.Speed;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.unit.Units;

@NonNullByDefault
public class SynopDecoder {

    public Map<String, String> PrecipitationIndicator = Map.of("0", "Precipitation in groups 1 and 3", "1",
            "Precipitation reported in group 1 only", "2", "Precipitation reported in group 3 only", "3",
            "Precipitation omitted, no precipitation", "4", "Precipitation omitted, no observation");

    public Map<String, String> StationType = Map.of("1", "manned station -- weather group included", "2",
            "manned station -- omitted, no significant weather", "3",
            "manned station -- omitted, no weather observation", "4",
            "automated station -- weather group included (see automated weather codes 4677 and 4561)", "5",
            "automated station -- omitted, no significant weather", "6",
            "automated station -- omitted, no weather observation", "7",
            "automated station -- weather group included (see automated weather codes 4680 and 4531");

    public Map<String, String> CloudBase = Map.of("0", "0 to 50 m", "1", "50 to 100 m", "2", "100 to 200 m", "3",
            "200 to 300 m", "4", "300 to 600 m", "5", "600 to 1000 m", "6", "1000 to 1500 m", "7", "1500 to 2000 m",
            "8", "2000 to 2500 m", "9", "above 2500 m", "/", "unknown");

    public enum StationType {
        AA("Landstation (FM 12)"),
        BB("Seastation (FM 13)"),
        OO("Mobile landstation (FM 14)");

        public final String label;

        private StationType(String label) {
            this.label = label;
        }
    }

    public enum WindUnit {
        WU0(Units.METRE_PER_SECOND, false),
        WU1(Units.METRE_PER_SECOND, true),
        WU3(Units.KNOT, false),
        WU4(Units.KNOT, true);

        public final Unit<Speed> unit;
        public final boolean measured;

        private WindUnit(Unit<Speed> unit, boolean measured) {
            this.unit = unit;
            this.measured = measured;
        }
    }

    public enum WmoCountry {
        C06("BE"),
        C07("FR"),
        C10("DL");

        public final String label;

        private WmoCountry(String label) {
            this.label = label;
        }
    }

    // String SYNOP = "(?<section0>" + SECTION_0 + ")\\s(?<section1>" + SECTION_1 + ")";

    public class Section0Decoder {
        String SECTION_0 = "((?<datetime>\\d{12})\\s+)?(?<stationType>AA|BB|OO)XX\\s+(?<monthDay>\\d{2})(?<hour>\\d{2})(?<windUnit>\\d)\\s+(?<country>\\d{2})(?<stationId>\\d{3})";
        Pattern pSection0 = Pattern.compile(SECTION_0);
        @Nullable
        WindUnit windUnit;
        @Nullable
        WmoCountry country;
        @Nullable
        String stationId;
        @Nullable
        ZonedDateTime reportTime;

        Section0Decoder(String message) {
            Matcher m = pSection0.matcher(message);
            if (m.find()) {
                windUnit = WindUnit.valueOf("WU" + m.group("windUnit"));
                country = WmoCountry.valueOf("C" + m.group("country"));
                stationId = m.group("stationId");

                ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
                int monthDay = Integer.valueOf(m.group("monthDay"));
                int hour = Integer.valueOf(m.group("hour"));
                reportTime = ZonedDateTime.of(now.getYear(), now.getMonth().getValue(), monthDay, hour, 0, 0, 0,
                        ZoneOffset.UTC);
            }
        }
    }

    public class Section1Decoder {
        String SECTION_1 = ".*((?<iihVV>(\\d|\\/){5})\\s+(?<Nddff>(\\d|/){5})\\s+(00(?<fff>\\d{3})\\s+)?(1(?<tair>(\\d|/){4})\\s+)?(2(?<dewp>(\\d|/){4})\\s+)?(3(?<pbaro>(\\d\\d\\d\\d|\\d\\d\\d\\/))\\s+)?(4(?<pslv>(\\d\\d\\d\\d|\\d\\d\\d\\/))\\s+)?(5(?<appp>\\d{4})\\s+)?(6(?<RRRt>(\\d|/){3}\\d\\s+))?(7(?<wwWW>\\d{2}(\\d|/)(\\d|/))\\s+)?(8(?<NCCC>(\\d|/){4})\\s+)?(9(?<GGgg>\\d{4})\\s+)?)?";
        Pattern pSection0 = Pattern.compile(SECTION_1);

        Section1Decoder(String message) {
            Matcher m = pSection0.matcher(message);
            if (m.find()) {
                String iihVV = m.group("iihVV");
                String Nddff = m.group("Nddff");
            }
        }
    }

    @SuppressWarnings("unused")
    public SynopDecoder(String message) {
        Section0Decoder section0 = new Section0Decoder(message);
        Section1Decoder section1 = new Section1Decoder(message);
        /*
         * Pattern pSection0 = Pattern.compile(SYNOP);
         * Matcher m = pSection0.matcher(message);
         * while (m.find()) {
         * String s = m.group("section0");
         * StationType stationType = StationType.valueOf(m.group("stationType"));
         * WindUnit windUnit = WindUnit.valueOf("WU" + m.group("windUnit"));
         * WmoCountry country = WmoCountry.valueOf("C" + m.group("country"));
         * String stationId = m.group("stationId");
         *
         * ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
         * int monthDay = Integer.valueOf(m.group("monthDay"));
         * int hour = Integer.valueOf(m.group("hour"));
         * ZonedDateTime reportTime = ZonedDateTime.of(now.getYear(), now.getMonth().getValue(), monthDay, hour, 0, 0,
         * 0, ZoneOffset.UTC);
         *
         * s = m.group("section1");
         * // Pattern pSection1 = Pattern.compile(SECTION_1);
         * // m = pSection1.matcher(message);
         * while (m.find()) {
         * String iihVV = m.group("iihVV");
         * String Nddff = m.group("Nddff");
         * }
         * }
         */
    }

}
