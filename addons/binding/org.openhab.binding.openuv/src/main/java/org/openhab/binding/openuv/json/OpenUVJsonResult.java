/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.openuv.json;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;

/**
 * The {@link OpenUVJsonResult} is responsible for storing
 * the "result" node from the OpenUV JSON response
 *
 * s
 */
public class OpenUVJsonResult {

    private DecimalType uv;
    private ZonedDateTime uv_time;
    private DecimalType uv_max;
    private ZonedDateTime uv_max_time;
    private DecimalType ozone;
    private ZonedDateTime ozone_time;
    private OpenUVSafeExposureTime safe_exposure_time;

    public OpenUVJsonResult() {
    }

    public DecimalType getUv() {
        return uv;
    }

    public DecimalType getUvMax() {
        return uv_max;
    }

    public DecimalType getOzone() {
        return ozone;
    }

    public DateTimeType getUVTime() {
        return new DateTimeType(uv_time.withZoneSameInstant(ZoneId.systemDefault()));
    }

    public DateTimeType getUVMaxTime() {
        return new DateTimeType(getUVMaxTimeAsZDT());
    }

    public ZonedDateTime getUVMaxTimeAsZDT() {
        return uv_max_time.withZoneSameInstant(ZoneId.systemDefault());
    }

    public DateTimeType getOzoneTime() {
        return new DateTimeType(ozone_time.withZoneSameInstant(ZoneId.systemDefault()));
    }

    public OpenUVSafeExposureTime getSafeExposureTime() {
        return safe_exposure_time;
    }

}
