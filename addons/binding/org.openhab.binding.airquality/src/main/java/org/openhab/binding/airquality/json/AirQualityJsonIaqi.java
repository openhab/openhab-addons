/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.airquality.json;

import java.math.BigDecimal;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link AirQualityJsonIaqi} is responsible for storing
 * "iaqi" node from the waqi.org JSON response
 * It contains information on air pollution particles
 * as well as some basic weather metrics.
 *
 * @author Kuba Wolanin - Initial contribution
 * @author Łukasz Dywicki - Initial contribution
 */
public class AirQualityJsonIaqi {

    private AirQualityValue<BigDecimal> pm25;
    private AirQualityValue<BigDecimal> pm10;
    private AirQualityValue<BigDecimal> o3;
    private AirQualityValue<BigDecimal> no2;
    private AirQualityValue<BigDecimal> co;
    private AirQualityValue<BigDecimal> t;

    @SerializedName("p")
    private AirQualityValue<BigDecimal> pressure;
    private AirQualityValue<BigDecimal> h;

    @SerializedName("w")
    private AirQualityValue<BigDecimal> wind;

    public AirQualityJsonIaqi() {
    }

    public BigDecimal getPm25() {
        return pm25.getValue();
    }

    public BigDecimal getPm10() {
        return pm10.getValue();
    }

    public BigDecimal getO3() {
        return o3.getValue();
    }

    public BigDecimal getNo2() {
        return no2.getValue();
    }

    public BigDecimal getCo() {
        return co.getValue();
    }

    public BigDecimal getT() {
        return t.getValue();
    }

    public BigDecimal getP() {
        return pressure.getValue();
    }

    public BigDecimal getH() {
        return h.getValue();
    }

    public BigDecimal getW() {
        return wind.getValue();
    }

}
