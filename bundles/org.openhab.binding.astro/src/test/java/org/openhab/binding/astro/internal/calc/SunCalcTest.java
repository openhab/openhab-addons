package org.openhab.binding.astro.internal.calc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openhab.binding.astro.internal.model.Sun;
import org.openhab.binding.astro.internal.model.SunPhaseName;

public class SunCalcTest {

    private SunCalc sunCalc;
    private final static Calendar FEB_27_2019 = new GregorianCalendar(2019, Calendar.FEBRUARY, 27);

    private final static double AMSTERDAM_LATITUDE = 52.367607;
    private final static double AMSTERDAM_LONGITUDE = 4.8978293;
    private final static Double AMSTERDAM_ALTITUDE = new Double(0.0);
    private final static int ACCURACY_IN_MILLIS = 3 * 60 * 1000;

    @Before
    public void init() {
        FEB_27_2019.setTimeZone(TimeZone.getTimeZone("Europe/Amsterdam"));
        sunCalc = new SunCalc();
    }

    @Test
    public void testGetSunInfoForOldDate() {
        Calendar calendar = new GregorianCalendar(2019, Calendar.FEBRUARY, 27);
        TimeZone.getAvailableIDs();
        calendar.setTimeZone(TimeZone.getTimeZone("Europe/Amsterdam"));

        Sun sun = sunCalc.getSunInfo(calendar, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, AMSTERDAM_ALTITUDE);

        assertNotNull(sun.getNight());

        assertNotNull(sun.getAstroDawn());
        assertNotNull(sun.getNauticDawn());
        assertNotNull(sun.getCivilDawn());

        assertNotNull(sun.getRise());

        assertNotNull(sun.getDaylight());
        assertNotNull(sun.getNoon());
        assertNotNull(sun.getSet());

        assertNotNull(sun.getCivilDusk());
        assertNotNull(sun.getNauticDusk());
        assertNotNull(sun.getAstroDusk());
        assertNotNull(sun.getNight());

        assertNotNull(sun.getMorningNight());
        assertNotNull(sun.getEveningNight());

        // for an old date the phase is always null
        assertNull(sun.getPhase().getName());
    }

    @Test
    public void testGetSunInfoForAstronomicalDawnAccuracy() {
        Sun sun = sunCalc.getSunInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, AMSTERDAM_ALTITUDE);

        // expected result from haevens-above.com is 27 Feb 2019 05:39 till 06:18
        assertEquals(new GregorianCalendar(2019, Calendar.FEBRUARY, 27, 5, 39).getTimeInMillis(),
                sun.getAstroDawn().getStart().getTimeInMillis(), ACCURACY_IN_MILLIS);
        assertEquals(new GregorianCalendar(2019, Calendar.FEBRUARY, 27, 6, 18).getTimeInMillis(),
                sun.getAstroDawn().getEnd().getTimeInMillis(), ACCURACY_IN_MILLIS);
    }

    @Test
    public void testGetSunInfoForNauticDawnAccuracy() {
        Sun sun = sunCalc.getSunInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, AMSTERDAM_ALTITUDE);

        // expected result from haevens-above.com is 27 Feb 2019 06:18 till 06:58
        assertEquals(new GregorianCalendar(2019, Calendar.FEBRUARY, 27, 6, 18).getTimeInMillis(),
                sun.getNauticDawn().getStart().getTimeInMillis(), ACCURACY_IN_MILLIS);
        assertEquals(new GregorianCalendar(2019, Calendar.FEBRUARY, 27, 6, 58).getTimeInMillis(),
                sun.getNauticDawn().getEnd().getTimeInMillis(), ACCURACY_IN_MILLIS);
    }

    @Test
    public void testGetSunInfoForCivilDawnAccuracy() {
        Sun sun = sunCalc.getSunInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, AMSTERDAM_ALTITUDE);

        // expected result from haevens-above.com is 27 Feb 2019 06:58 till 07:32
        assertEquals(new GregorianCalendar(2019, Calendar.FEBRUARY, 27, 6, 58).getTimeInMillis(),
                sun.getCivilDawn().getStart().getTimeInMillis(), ACCURACY_IN_MILLIS);
        assertEquals(new GregorianCalendar(2019, Calendar.FEBRUARY, 27, 7, 32).getTimeInMillis(),
                sun.getCivilDawn().getEnd().getTimeInMillis(), ACCURACY_IN_MILLIS);
    }

    @Test
    public void testGetSunInfoForRiseAccuracy() {
        Sun sun = sunCalc.getSunInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, AMSTERDAM_ALTITUDE);

        // expected result from haevens-above.com is 27 Feb 2019 07:32
        assertEquals(new GregorianCalendar(2019, Calendar.FEBRUARY, 27, 7, 32).getTimeInMillis(),
                sun.getRise().getStart().getTimeInMillis(), ACCURACY_IN_MILLIS);
    }

    @Test
    public void testGetSunInfoForSunNoonAccuracy() {
        Sun sun = sunCalc.getSunInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, AMSTERDAM_ALTITUDE);

        // expected result from haevens-above.com is 27 Feb 2019 12:54
        assertEquals(new GregorianCalendar(2019, Calendar.FEBRUARY, 27, 12, 54).getTimeInMillis(),
                sun.getNoon().getStart().getTimeInMillis(), ACCURACY_IN_MILLIS);
    }

    @Test
    public void testGetSunInfoForSetAccuracy() {
        Sun sun = sunCalc.getSunInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, AMSTERDAM_ALTITUDE);

        // expected result from haevens-above.com is 27 Feb 2019 18:15
        assertEquals(new GregorianCalendar(2019, Calendar.FEBRUARY, 27, 18, 15).getTimeInMillis(),
                sun.getSet().getStart().getTimeInMillis(), ACCURACY_IN_MILLIS);
    }

    @Test
    public void testGetSunInfoForCivilDuskAccuracy() {
        Sun sun = sunCalc.getSunInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, AMSTERDAM_ALTITUDE);

        // expected result from haevens-above.com is 27 Feb 2019 18:15 till 18:50
        assertEquals(new GregorianCalendar(2019, Calendar.FEBRUARY, 27, 18, 15).getTimeInMillis(),
                sun.getCivilDusk().getStart().getTimeInMillis(), ACCURACY_IN_MILLIS);
        assertEquals(new GregorianCalendar(2019, Calendar.FEBRUARY, 27, 18, 50).getTimeInMillis(),
                sun.getCivilDusk().getEnd().getTimeInMillis(), ACCURACY_IN_MILLIS);
    }

    @Test
    public void testGetSunInfoForNauticDuskAccuracy() {
        Sun sun = sunCalc.getSunInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, AMSTERDAM_ALTITUDE);

        // expected result from haevens-above.com is 27 Feb 2019 18:50 till 19:29
        assertEquals(new GregorianCalendar(2019, Calendar.FEBRUARY, 27, 18, 50).getTimeInMillis(),
                sun.getNauticDusk().getStart().getTimeInMillis(), ACCURACY_IN_MILLIS);
        assertEquals(new GregorianCalendar(2019, Calendar.FEBRUARY, 27, 19, 29).getTimeInMillis(),
                sun.getNauticDusk().getEnd().getTimeInMillis(), ACCURACY_IN_MILLIS);
    }

    @Test
    public void testGetSunInfoForAstronomicalDuskAccuracy() {
        Sun sun = sunCalc.getSunInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, AMSTERDAM_ALTITUDE);

        // expected result from haevens-above.com is 27 Feb 2019 19:29 till 20:09
        assertEquals(new GregorianCalendar(2019, Calendar.FEBRUARY, 27, 19, 29).getTimeInMillis(),
                sun.getAstroDusk().getStart().getTimeInMillis(), ACCURACY_IN_MILLIS);
        assertEquals(new GregorianCalendar(2019, Calendar.FEBRUARY, 27, 20, 9).getTimeInMillis(),
                sun.getAstroDusk().getEnd().getTimeInMillis(), ACCURACY_IN_MILLIS);
    }

    @Test
    @Ignore
    public void testRangesForCoherenceBetweenNightEndAndAstroDawnStart() {
        Sun sun = sunCalc.getSunInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, AMSTERDAM_ALTITUDE);

        assertEquals(sun.getAllRanges().get(SunPhaseName.NIGHT).getEnd(),
                sun.getAllRanges().get(SunPhaseName.ASTRO_DAWN).getStart());
    }

    @Test
    public void testRangesForCoherenceBetweenMorningNightEndAndAstroDawnStart() {
        Sun sun = sunCalc.getSunInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, AMSTERDAM_ALTITUDE);

        assertEquals(sun.getAllRanges().get(SunPhaseName.MORNING_NIGHT).getEnd(),
                sun.getAllRanges().get(SunPhaseName.ASTRO_DAWN).getStart());
    }

    @Test
    public void testRangesForCoherenceBetweenAstroDownEndAndNauticDawnStart() {
        Sun sun = sunCalc.getSunInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, AMSTERDAM_ALTITUDE);

        assertEquals(sun.getAllRanges().get(SunPhaseName.ASTRO_DAWN).getEnd(),
                sun.getAllRanges().get(SunPhaseName.NAUTIC_DAWN).getStart());
    }

    @Test
    public void testRangesForCoherenceBetweenNauticDawnEndAndCivilDawnStart() {
        Sun sun = sunCalc.getSunInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, AMSTERDAM_ALTITUDE);

        assertEquals(sun.getAllRanges().get(SunPhaseName.NAUTIC_DAWN).getEnd(),
                sun.getAllRanges().get(SunPhaseName.CIVIL_DAWN).getStart());
    }

    @Test
    public void testRangesForCoherenceBetweenCivilDawnEndAndSunRiseStart() {
        Sun sun = sunCalc.getSunInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, AMSTERDAM_ALTITUDE);

        assertEquals(sun.getAllRanges().get(SunPhaseName.CIVIL_DAWN).getEnd(),
                sun.getAllRanges().get(SunPhaseName.SUN_RISE).getStart());
    }

    @Test
    public void testRangesForCoherenceBetweenSunRiseEndAndDaylightStart() {
        Sun sun = sunCalc.getSunInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, AMSTERDAM_ALTITUDE);

        assertEquals(sun.getAllRanges().get(SunPhaseName.SUN_RISE).getEnd(),
                sun.getAllRanges().get(SunPhaseName.DAYLIGHT).getStart());
    }

    @Test
    public void testRangesForCoherenceBetweenDaylightEndAndSunSetStart() {
        Sun sun = sunCalc.getSunInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, AMSTERDAM_ALTITUDE);

        assertEquals(sun.getAllRanges().get(SunPhaseName.DAYLIGHT).getEnd(),
                sun.getAllRanges().get(SunPhaseName.SUN_SET).getStart());
    }

    @Test
    public void testRangesForCoherenceBetweenSunSetEndAndCivilDuskStart() {
        Sun sun = sunCalc.getSunInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, AMSTERDAM_ALTITUDE);

        assertEquals(sun.getAllRanges().get(SunPhaseName.SUN_SET).getEnd(),
                sun.getAllRanges().get(SunPhaseName.CIVIL_DUSK).getStart());
    }

    @Test
    public void testRangesForCoherenceBetweenCivilDuskEndAndNauticDuskStart() {
        Sun sun = sunCalc.getSunInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, AMSTERDAM_ALTITUDE);

        assertEquals(sun.getAllRanges().get(SunPhaseName.CIVIL_DUSK).getEnd(),
                sun.getAllRanges().get(SunPhaseName.NAUTIC_DUSK).getStart());
    }

    @Test
    public void testRangesForCoherenceBetweenNauticDuskEndAndAstroDuskStart() {
        Sun sun = sunCalc.getSunInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, AMSTERDAM_ALTITUDE);

        assertEquals(sun.getAllRanges().get(SunPhaseName.NAUTIC_DUSK).getEnd(),
                sun.getAllRanges().get(SunPhaseName.ASTRO_DUSK).getStart());
    }

    @Test
    public void testRangesForCoherenceBetweenAstroDuskEndAndNightStart() {
        Sun sun = sunCalc.getSunInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, AMSTERDAM_ALTITUDE);

        assertEquals(sun.getAllRanges().get(SunPhaseName.ASTRO_DUSK).getEnd(),
                sun.getAllRanges().get(SunPhaseName.NIGHT).getStart());
    }

    @Test
    public void testRangesForCoherenceBetweenAstroDuskEndAndEveningNightStart() {
        Sun sun = sunCalc.getSunInfo(FEB_27_2019, AMSTERDAM_LATITUDE, AMSTERDAM_LONGITUDE, AMSTERDAM_ALTITUDE);

        assertEquals(sun.getAllRanges().get(SunPhaseName.ASTRO_DUSK).getEnd(),
                sun.getAllRanges().get(SunPhaseName.EVENING_NIGHT).getStart());
    }
}
