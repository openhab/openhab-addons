package org.openhab.binding.ems;

import static org.mockito.Mockito.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;

import org.junit.jupiter.api.Test;
import org.openhab.binding.ems.dto.OnecallWeather;
import org.openhab.binding.ems.internal.EMSConfiguration;
import org.openhab.binding.ems.internal.handler.EMSHandler;
import org.openhab.binding.ems.utils.Constants;
import org.openhab.binding.ems.utils.Formulas;
import org.openhab.core.library.types.PointType;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;

class TestFormulas {

    @Test
    void test() {
        int minuteGRanularity = 5;
        int month = 5;
        double totalProduction = 0;
        for (int i = 0; i < 24; i++) {
            for (int j = 0; j * minuteGRanularity < 60; j++) {
                double sunHeight = Formulas
                        .round(Formulas.sunPositionDIN(2021, month, 8, i, j * minuteGRanularity, 0, 50, 10, 2), 1);
                double airMass = Formulas.round(Formulas.airMass(sunHeight), 4);
                double intensity = Formulas.round(Formulas.solarIntensity(airMass), 1);
                double winkel = 90 - sunHeight + 15;
                double realIntensity = Math.sin(Math.toRadians(winkel));
                double production = Formulas
                        .round(9.75
                                * Formulas.getRadiationInfo(
                                        Formulas.getCalendar(2021, month, 8, i, j * minuteGRanularity), sunHeight, 50.0)
                                / 1000 * minuteGRanularity / 60, 3);
                if (sunHeight > 0) {
                    totalProduction += production;
                    System.out.println("Hour: " + i + " Height: " + sunHeight + " Air Mass: " + airMass + " Intensity: "
                            + intensity + " Winkel: " + winkel + " Production: " + production);
                    System.out.println("Rad: "
                            + Formulas.getRadiationInfo(Formulas.getCalendar(2021, 5, 8, i, 30), sunHeight, 50.0));
                }
            }
        }
        System.out.println(totalProduction);
    }

    @Test
    public void test2() {
        Calendar date = Calendar.getInstance();
        int minuteGranularity = 5;
        double totalProduction = 0;
        for (int i = 0; i < 24; i++) {
            for (int j = 0; j * minuteGranularity < 60; j++) {
                double sunHeight = Formulas
                        .round(Formulas.sunPositionDIN(date.get(Calendar.YEAR), date.get(Calendar.MONTH) + 1,
                                date.get(Calendar.DAY_OF_MONTH) + 1, i, j * minuteGranularity, 0, 50, 10, 2), 1);
                double production = Formulas
                        .round(9.75
                                * Formulas.getRadiationInfo(
                                        Formulas.getCalendar(date.get(Calendar.YEAR), date.get(Calendar.MONTH) + 1,
                                                date.get(Calendar.DAY_OF_MONTH) + 1, i, j * minuteGranularity),
                                        sunHeight, 50.0)
                                / 1000 * minuteGranularity / 60, 3);
                if (sunHeight > 0) {
                    totalProduction += production;
                }
            }
        }
        System.out.println(totalProduction);
    }

    @Test
    public void testWeather() {
        try {
            URL weatherApi = new URL(
                    "https://api.openweathermap.org/data/2.5/onecall?lat=51.44&lon=-10.0&appid=7c82a05c28361abc8ab90b9f0faf18fa");
            // URL weatherApi = new URL(
            // "https://api.openweathermap.org/data/2.5/weather?q=Wetzlar&appid=7c82a05c28361abc8ab90b9f0faf18fa");
            HttpURLConnection con = (HttpURLConnection) weatherApi.openConnection();
            con.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer content = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            System.out.println(content.toString());
            OnecallWeather ocw = Constants.GSON.fromJson(content.toString(), OnecallWeather.class);
            System.out.println(ocw.hourly.size());
            System.out.println(ocw.daily.size());
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void testTimeConversion() {
        long ut1 = Instant.now().getEpochSecond();
        System.out.println(ut1);

        System.out.println(Instant.ofEpochMilli(ut1));
        java.util.Date time = new java.util.Date(ut1);
        System.out.println(time);

        java.util.Date time2 = new java.util.Date(1620507600);
        System.out.println(time2);

        long epoch = Instant.now().toEpochMilli();
        System.out.println(epoch);
        epoch = (long) 1620507600 * 1000;
        System.out.println(epoch);

        LocalDate ld = Instant.ofEpochMilli(epoch).atZone(ZoneId.systemDefault()).toLocalDate();
        System.out.println(ld);

        LocalDateTime ldt = Instant.ofEpochMilli(epoch).atZone(ZoneId.systemDefault()).toLocalDateTime();
        System.out.println(ldt);

        java.util.Date time3 = new java.util.Date((long) 1620633600 * 1000);
        System.out.println(time3);
    }

    @Test
    public void testEMSHandler() {
        PointType pt = PointType.valueOf("50.55,8.43");
        Thing thing = mock(Thing.class);
        when(thing.getUID()).thenReturn(new ThingUID("testbinding", "test"));
        EMSHandler ems = new EMSHandler(thing, pt);
        EMSConfiguration c = new EMSConfiguration();
        c.owmApiKey = "7c82a05c28361abc8ab90b9f0faf18fa";
        ems.setConfiguration(c);
        ems.generatePrediction();
    }
}
