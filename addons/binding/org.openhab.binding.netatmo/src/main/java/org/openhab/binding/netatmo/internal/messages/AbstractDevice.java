/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.internal.messages;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.openhab.binding.netatmo.handler.AbstractEquipment.MeasureValueMap;

/**
 * Java Bean to represent a JSON common response elements of both modules and a
 * devices
 *
 * @author Gaël L'hopital
 */
@JsonAutoDetect(fieldVisibility = Visibility.ANY)
@JsonIgnoreProperties(ignoreUnknown = false)
public abstract class AbstractDevice extends AbstractMessage {

    protected String _id;
    protected Integer firmware;
    protected String module_name;
    protected Boolean public_ext_data;
    protected String type;
    protected List<String> data_type;

    // protected MeasureValueMap measures;

    public Calendar timeStamp;

    public String getId() {
        return this._id;
    }

    public Integer getFirmware() {
        return this.firmware;
    }

    public String getModuleName() {
        return this.module_name;
    }

    public Boolean isPublicData() {
        return this.public_ext_data;
    }

    public String getType() {
        return this.type;
    }

    public List<String> getMeasurements() {
        return this.data_type;
    }

    public Calendar gettimestamp() {
        return timeStamp;
    }

    public BigDecimal getComputed(String chanel, MeasureValueMap measures) {
        BigDecimal result = null;

        if (chanel.equals("humidex")) {
            BigDecimal temperature = measures.get("temperature");
            BigDecimal humidity = measures.get("humidity");
            if ((temperature != null) && (humidity != null)) {
                result = new BigDecimal(getHumidex(temperature.doubleValue(), humidity.doubleValue()));
            }
        } else if (chanel.equals("dewpoint")) {
            BigDecimal temperature = measures.get("temperature");
            BigDecimal humidity = measures.get("humidity");
            if ((temperature != null) && (humidity != null)) {
                result = new BigDecimal(getDewPoint(temperature.doubleValue(), humidity.doubleValue()));
            }
        } else if (chanel.equals("dewpointdepression")) {
            BigDecimal temperature = measures.get("temperature");
            BigDecimal dewpoint = getComputed("dewpoint", measures);
            if ((temperature != null) && (dewpoint != null)) {
                result = new BigDecimal(getDewPointDep(temperature.doubleValue(), dewpoint.doubleValue()));
            }
        } else if (chanel.equals("heatindex")) {
            BigDecimal temperature = measures.get("temperature");
            BigDecimal humidity = measures.get("humidity");
            if ((temperature != null) && (humidity != null)) {
                result = new BigDecimal(getHeatIndex(temperature.doubleValue(), humidity.doubleValue()));
            }
        }
        return result;
    }

    private static double getHeatIndex(double temperature, double r) {
        double t = (9 / 5) * temperature + 32;
        double hi = 16.923 + (1.85212 * Math.pow(10, -1) * t) + (5.37941 * r) - (1.00254 * Math.pow(10, -1) * t * r)
                + (9.41695 * Math.pow(10, -3) * Math.pow(t, 2)) + (7.28898 * Math.pow(10, -3) * Math.pow(r, 2))
                + (3.45372 * Math.pow(10, -4) * Math.pow(t, 2) * r) - (8.14971 * Math.pow(10, -4) * t * Math.pow(r, 2))
                + (1.02102 * Math.pow(10, -5) * Math.pow(t, 2) * Math.pow(r, 2))
                - (3.8646 * Math.pow(10, -5) * Math.pow(t, 3)) + (2.91583 * Math.pow(10, -5) * Math.pow(r, 3))
                + (1.42721 * Math.pow(10, -6) * Math.pow(t, 3) * r) + (1.97483 * Math.pow(10, -7) * t * Math.pow(r, 3))
                - (2.18429 * Math.pow(10, -8) * Math.pow(t, 3) * Math.pow(r, 2))
                + (8.43296 * Math.pow(10, -10) * Math.pow(t, 2) * Math.pow(r, 3))
                - (4.81975 * Math.pow(10, -11) * Math.pow(t, 3) * Math.pow(r, 3));
        hi = (5.0 / 9.0) * (hi - 32);
        return hi;
    }

    private static double getDewPointDep(double temperature, double dewpoint) {
        return temperature - dewpoint;
    }

    /**
     * Compute the Dewpoint temperature given temperature and hygrometry
     * valid up to 60 degrees, from
     * http://en.wikipedia.org/wiki/Dew_point#Calculating_the_dew_point
     *
     * @param temperature in (°C)
     * @param hygro relative level (%)
     * @return dewpoint temperature
     */
    public static double getDewPoint(double temperature, double humidity) {
        double a = 17.271, b = 237.2;
        double gamma = ((a * temperature) / (b + temperature)) + Math.log(humidity / 100.0);
        return b * gamma / (a - gamma);
    }

    /**
     * Compute the Humidex index given temperature and hygrometry
     *
     *
     * @param temperature in (°C)
     * @param hygro relative level (%)
     * @return Humidex index value
     */
    public static double getHumidex(double temperature, double hygro) {
        double result = 6.112 * Math.pow(10, 7.5 * temperature / (237.7 + temperature)) * hygro / 100;
        result = temperature + 0.555555556 * (result - 10);
        return result;
    }

    @Override
    public String toString() {
        final ToStringBuilder builder = createToStringBuilder();
        builder.appendSuper(super.toString());

        builder.append("id", getId());
        builder.append("firmware", getFirmware());
        builder.append("moduleName", getModuleName());
        builder.append("publicData", isPublicData());
        builder.append("type", getType());

        return builder.toString();
    }
}
