/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.meteoblue.internal;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import javax.imageio.ImageIO;

import org.openhab.binding.meteoblue.internal.json.JsonDataDay;
import org.openhab.binding.meteoblue.internal.json.JsonMetadata;
import org.openhab.binding.meteoblue.internal.json.JsonUnits;
import org.openhab.core.OpenHAB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Model of forecast data.
 *
 * @author Chris Carman - Initial contribution
 */
public class Forecast {
    private final Logger logger = LoggerFactory.getLogger(Forecast.class);

    // metadata fields
    private Double latitude;
    private Double longitude;
    private Integer height;
    private String timeZoneAbbreviation;

    // units fields
    private String timeUnits;
    private String predictabilityUnits;
    private String precipitationProbabilityUnits;
    private String pressureUnits;
    private String relativeHumidityUnits;
    private String temperatureUnits;
    private String windDirectionUnits;
    private String precipitationUnits;
    private String windSpeedUnits;

    // data_day fields
    private Calendar forecastDate;
    private Integer pictocode;
    private Integer UVIndex;
    private Double minTemperature;
    private Double maxTemperature;
    private Double meanTemperature;
    private Double feltTemperatureMin;
    private Double feltTemperatureMax;
    private Integer windDirection;
    private Integer precipitationProbability;
    private String rainSpot;
    private Integer predictabilityClass;
    private Integer predictability;
    private Double precipitation;
    private Double snowFraction;
    private Integer minSeaLevelPressure;
    private Integer maxSeaLevelPressure;
    private Integer meanSeaLevelPressure;
    private Double minWindSpeed;
    private Double maxWindSpeed;
    private Double meanWindSpeed;
    private Integer relativeHumidityMin;
    private Integer relativeHumidityMax;
    private Integer relativeHumidityMean;
    private Double convectivePrecipitation;
    private Double precipitationHours;
    private Double humidityGreater90Hours;

    // derived fields
    private String cardinalWindDirection;
    private String iconName;
    private Image icon;
    private Image rainArea;
    private Double snowFall;

    public Forecast(int whichDay, JsonMetadata metadata, JsonUnits units, JsonDataDay dataDay)
            throws IllegalArgumentException {
        if (metadata == null) {
            throw new IllegalArgumentException("Received no metadata information.");
        }
        if (dataDay == null) {
            throw new IllegalArgumentException("Received no data_day information.");
        }
        if (whichDay > dataDay.getTime().length - 1) {
            throw new IndexOutOfBoundsException("No data received for day " + whichDay);
        }

        // extract the metadata fields
        latitude = metadata.getLatitude();
        longitude = metadata.getLongitude();
        height = metadata.getHeight();
        timeZoneAbbreviation = metadata.getTimeZoneAbbreviation();

        logger.trace("Metadata:");
        logger.trace("  Latitude: {}", latitude);
        logger.trace("  Longitude: {}", longitude);
        logger.trace("  Height: {}", height);
        logger.trace("  TZ Abbrev: {}", timeZoneAbbreviation);

        // extract the units fields
        timeUnits = units.getTime();
        predictabilityUnits = units.getPredictability();
        precipitationProbabilityUnits = units.getPrecipitationProbability();
        pressureUnits = units.getPressure();
        relativeHumidityUnits = units.getRelativeHumidity();
        temperatureUnits = units.getTemperature();
        windDirectionUnits = units.getWindDirection();
        precipitationUnits = units.getPrecipitation();
        windSpeedUnits = units.getWindSpeed();

        logger.trace("Units:");
        logger.trace("  Time: {}", timeUnits);
        logger.trace("  Predictability: {}", predictabilityUnits);
        logger.trace("  Precipitation Probability: {}", precipitationProbabilityUnits);
        logger.trace("  Pressure: {}", pressureUnits);
        logger.trace("  Relative Humidity: {}", relativeHumidityUnits);
        logger.trace("  Temperature: {}", temperatureUnits);
        logger.trace("  Wind Direction: {}", windDirectionUnits);
        logger.trace("  Precipitation: {}", precipitationUnits);
        logger.trace("  Wind Speed: {}", windSpeedUnits);

        // extract the data_day fields
        String timeString = dataDay.getTime()[whichDay];
        try {
            Calendar c = Calendar.getInstance(TimeZone.getTimeZone(timeZoneAbbreviation));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            c.setTime(formatter.parse(timeString));
            forecastDate = c;
        } catch (ParseException e) {
            logger.debug("Failed to parse the value '{}' as a date.", timeString);
        }

        pictocode = dataDay.getPictocode()[whichDay];
        iconName = getIconNameForPictocode(pictocode);
        icon = loadImageIcon(iconName);
        UVIndex = dataDay.getUVIndex()[whichDay];
        maxTemperature = dataDay.getTemperatureMax()[whichDay];
        minTemperature = dataDay.getTemperatureMin()[whichDay];
        meanTemperature = dataDay.getTemperatureMean()[whichDay];
        feltTemperatureMax = dataDay.getFeltTemperatureMax()[whichDay];
        feltTemperatureMin = dataDay.getFeltTemperatureMin()[whichDay];
        windDirection = dataDay.getWindDirection()[whichDay];
        cardinalWindDirection = getCardinalDirection(windDirection);
        precipitationProbability = dataDay.getPrecipitationProbability()[whichDay];
        rainSpot = dataDay.getRainspot()[whichDay];
        rainArea = generateRainAreaImage();
        predictabilityClass = dataDay.getPredictabilityClass()[whichDay];
        predictability = dataDay.getPredictability()[whichDay];
        precipitation = dataDay.getPrecipitation()[whichDay];
        snowFraction = dataDay.getSnowFraction()[whichDay];
        maxSeaLevelPressure = dataDay.getSeaLevelPressureMax()[whichDay];
        minSeaLevelPressure = dataDay.getSeaLevelPressureMin()[whichDay];
        meanSeaLevelPressure = dataDay.getSeaLevelPressureMean()[whichDay];
        maxWindSpeed = dataDay.getWindSpeedMax()[whichDay];
        meanWindSpeed = dataDay.getWindSpeedMean()[whichDay];
        minWindSpeed = dataDay.getWindSpeedMin()[whichDay];
        relativeHumidityMax = dataDay.getRelativeHumidityMax()[whichDay];
        relativeHumidityMin = dataDay.getRelativeHumidityMin()[whichDay];
        relativeHumidityMean = dataDay.getRelativeHumidityMean()[whichDay];
        convectivePrecipitation = dataDay.getConvectivePrecipitation()[whichDay];
        snowFall = snowFraction * precipitation;
        precipitationHours = dataDay.getPrecipitationHours()[whichDay];
        humidityGreater90Hours = dataDay.getHumidityGreater90Hours()[whichDay];

        logger.trace("DataDay:");
        logger.trace("  Time: {}", forecastDate);
        logger.trace("  Pictocode: {}", pictocode);
        logger.trace("  Icon: {}", icon);
        logger.trace("  UV Index: {}", UVIndex);
        logger.trace("  Max Temperature: {}", maxTemperature);
        logger.trace("  Min Temperature: {}", minTemperature);
        logger.trace("  Mean Temperature: {}", meanTemperature);
        logger.trace("  Felt Temperature Max: {}", feltTemperatureMax);
        logger.trace("  Felt Temperature Min: {}", feltTemperatureMin);
        logger.trace("  Wind Direction: {}", windDirection);
        logger.trace("  Precipitation Probability: {}", precipitationProbability);
        logger.trace("  Rainspot: {}", rainSpot);
        logger.trace("  Rain Area: {}", rainArea);
        logger.trace("  Predictability Class: {}", predictabilityClass);
        logger.trace("  Predictability: {}", predictability);
        logger.trace("  Precipitation: {}", precipitation);
        logger.trace("  Snow Fraction: {}", snowFraction);
        logger.trace("  Max Sea-level Pressure: {}", maxSeaLevelPressure);
        logger.trace("  Min Sea-level Pressure: {}", minSeaLevelPressure);
        logger.trace("  Mean Sea-level Pressure: {}", meanSeaLevelPressure);
        logger.trace("  Max Wind Speed: {}", maxWindSpeed);
        logger.trace("  Mean Wind Speed: {}", meanWindSpeed);
        logger.trace("  Min Wind Speed: {}", minWindSpeed);
        logger.trace("  Relative Humidity Max: {}", relativeHumidityMax);
        logger.trace("  Relative Humidity Min: {}", relativeHumidityMin);
        logger.trace("  Relative Humidity Mean: {}", relativeHumidityMean);
        logger.trace("  Convective Precipitation: {}", convectivePrecipitation);
        logger.trace("  Snowfall: {}", snowFall);
        logger.trace("  Precipitation Hours: {}", precipitationHours);
        logger.trace("  Humidity > 90 Hours: {}", humidityGreater90Hours);
    }

    // generic getter
    public Object getDatapoint(String datapointName) {
        if (datapointName.equals("condition")) {
            return String.valueOf(pictocode);
        }

        try {
            Field field = getClass().getDeclaredField(datapointName);
            field.setAccessible(true);
            return field.get(this);
        } catch (NoSuchFieldException e) {
            logger.warn("Unable to find a datapoint declared for the name '{}'", datapointName);
            return null;
        } catch (Exception e) {
            logger.warn("An unexpected error occurred while trying to access the datapoint '{}'", datapointName, e);
            return null;
        }
    }

    // metadata getters
    public Integer getHeight() {
        return height;
    }

    // units getters
    public String getTimeUnits() {
        return timeUnits;
    }

    public String getPredictabilityUnits() {
        return predictabilityUnits;
    }

    public String getPrecipitationProbabilityUnits() {
        return precipitationProbabilityUnits;
    }

    public String getPressureUnits() {
        return pressureUnits;
    }

    public String getRelativeHumidityUnits() {
        return relativeHumidityUnits;
    }

    public String getTemperatureUnits() {
        return temperatureUnits;
    }

    public String getWindDirectionUnits() {
        return windDirectionUnits;
    }

    public String getPrecipitationUnits() {
        return precipitationUnits;
    }

    public String getWindSpeedUnits() {
        return windSpeedUnits;
    }

    // data_day getters
    public Calendar getForecastDate() {
        return forecastDate;
    }

    public Integer getPictocode() {
        return pictocode;
    }

    public Integer getUVIndex() {
        return UVIndex;
    }

    public Double getTemperatureMin() {
        return minTemperature;
    }

    public Double getTemperatureMax() {
        return maxTemperature;
    }

    public Double getTemperatureMean() {
        return meanTemperature;
    }

    public Double getFeltTemperatureMin() {
        return feltTemperatureMin;
    }

    public Double feltTemperatureMax() {
        return feltTemperatureMax;
    }

    public Integer getWindDirection() {
        return windDirection;
    }

    public String getCardinalWindDirection() {
        return cardinalWindDirection;
    }

    public Integer getPrecipitationProbability() {
        return precipitationProbability;
    }

    public String getRainSpot() {
        return rainSpot;
    }

    public Image getRainArea() {
        return rainArea;
    }

    public Integer getPredictabilityClass() {
        return predictabilityClass;
    }

    public Integer getPredictability() {
        return predictability;
    }

    public Double getPrecipitation() {
        return precipitation;
    }

    public Double getSnowFraction() {
        return snowFraction;
    }

    public Integer getSeaLevelPressureMin() {
        return minSeaLevelPressure;
    }

    public Integer getSeaLevelPressureMax() {
        return maxSeaLevelPressure;
    }

    public Integer getSeaLevelPressureMean() {
        return meanSeaLevelPressure;
    }

    public Double getWindSpeedMin() {
        return minWindSpeed;
    }

    public Double getWindSpeedMax() {
        return maxWindSpeed;
    }

    public Double getWindSpeedMean() {
        return meanWindSpeed;
    }

    public Integer getRelativeHumidityMin() {
        return relativeHumidityMin;
    }

    public Integer getRelativeHumidityMax() {
        return relativeHumidityMax;
    }

    public Integer getRelativeHumidityMean() {
        return relativeHumidityMean;
    }

    public Double getConvectivePrecipitation() {
        return convectivePrecipitation;
    }

    public Double getPrecipitationHours() {
        return precipitationHours;
    }

    public Double getHumidityGreater90Hours() {
        return humidityGreater90Hours;
    }

    // derived getters
    private String getCardinalDirection(int degrees) {
        /*
         * 8 directions @ 45 deg. each, centered
         * N = 337.5-360,0-22.5
         * NE = 22.5-67.5
         * E = 67.5-112.5
         * SE = 112.5-157.5
         * S = 157.5-202.5
         * SW = 202.5-247.5
         * W = 247.5-292.5
         * NW = 292.5-337.5
         */

        if (degrees > 337 || degrees < 23) {
            return "N";
        }

        if (degrees > 22 && degrees < 68) {
            return "NE";
        }

        if (degrees > 67 && degrees < 113) {
            return "E";
        }

        if (degrees > 112 && degrees < 158) {
            return "SE";
        }

        if (degrees > 157 && degrees < 203) {
            return "S";
        }

        if (degrees > 202 && degrees < 248) {
            return "SW";
        }

        if (degrees > 247 && degrees < 293) {
            return "W";
        }

        return "NW";
    }

    public Double getSnowFall() {
        return snowFall;
    }

    public Image getIcon() {
        return icon;
    }

    private String getIconNameForPictocode(int which) {
        if (which < 1 || which > 17) {
            return "iday.png";
        }

        return "iday-" + which + ".png";
    }

    private Image loadImageIcon(String imageFileName) {
        BufferedImage buf = null;
        String configDirectory = OpenHAB.getConfigFolder();
        File dataFile = new File(new File(configDirectory, "icons/classic/"), imageFileName);
        if (!dataFile.exists()) {
            logger.debug("Image file '{}' does not exist. Unable to create imageIcon.", dataFile.getAbsolutePath());
            return null;
        }

        try {
            buf = ImageIO.read(dataFile);
            logger.trace("Returning image data: {}", buf);
            return buf;
        } catch (FileNotFoundException e) {
            logger.trace("Image file '{}' not found during read attempt", dataFile, e);
            return null;
        } catch (IOException e) {
            logger.debug("Failed to load image file '{}' for weather icon.", dataFile, e);
            return null;
        }
    }

    private Image generateRainAreaImage() {
        if (rainSpot == null) {
            logger.debug("No rainspot data exists. Can't generate rain area image.");
            return null;
        }

        // @formatter:off
        /*
         * Grid position ->           <- String position
         * [  0  1  2  3  4  5  6 ]   [ 42 43 44 45 46 47 48 ]
         * [  7  8  9 10 11 12 13 ]   [ 35 36 37 38 39 40 41 ]
         * [ 14 15 16 17 18 19 20 ]   [ 28 29 30 31 32 33 34 ]
         * [ 21 22 23 24 25 26 27 ]   [ 21 22 23 24 25 26 27 ]
         * [ 28 29 30 31 32 33 34 ]   [ 14 15 16 17 18 19 20 ]
         * [ 35 36 37 38 39 40 41 ]   [  7  8  9 10 11 12 13 ]
         * [ 42 43 44 45 46 47 48 ]   [  0  1  2  3  4  5  6 ]
         */
        // @formatter:on

        String s42 = rainSpot.substring(42);
        String s35 = rainSpot.substring(35, 42);
        String s28 = rainSpot.substring(28, 35);
        String s21 = rainSpot.substring(21, 28);
        String s14 = rainSpot.substring(14, 21);
        String s07 = rainSpot.substring(7, 14);
        String s00 = rainSpot.substring(0, 7);
        char[] values = new char[49];
        s00.getChars(0, s00.length(), values, 42);
        s07.getChars(0, s07.length(), values, 35);
        s14.getChars(0, s14.length(), values, 28);
        s21.getChars(0, s21.length(), values, 21);
        s28.getChars(0, s28.length(), values, 14);
        s35.getChars(0, s35.length(), values, 7);
        s42.getChars(0, s42.length(), values, 0);
        logger.trace("Final grid: {}", values);

        BufferedImage buf = new BufferedImage(350, 350, BufferedImage.TYPE_INT_RGB);

        int gridCell = 0;
        Color color;

        for (int y = 0; y < 7; y++) {
            for (int x = 0; x < 7; x++) {
                gridCell = y * 7 + x;
                color = getColorForIndex(values[gridCell]);
                writeGrid(gridCell, buf, color);
            }
        }

        return buf;
    }

    private void writeGrid(int cell, BufferedImage img, Color color) {
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        int p = (r << 16) | (g << 8) | b;

        int startX = cell % 7 * 50;
        int startY = cell / 7 * 50;
        for (int i = startY; i < startY + 50; i++) {
            for (int j = startX; j < startX + 50; j++) {
                img.setRGB(j, i, p);
            }
        }
    }

    private Color getColorForIndex(char c) {
        switch (c) {
            case '0':
                return Color.WHITE;
            case '1':
                return Color.GREEN;
            case '2':
                return Color.YELLOW;
            case '3':
                return Color.RED;
            case '9':
                return Color.decode("0x00FF00");
            default:
                return Color.BLACK;
        }
    }
}
