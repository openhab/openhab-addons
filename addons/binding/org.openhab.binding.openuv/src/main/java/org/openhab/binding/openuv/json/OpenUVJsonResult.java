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
 * @author Gaël L'hopital - Initial contribution
 */
public class OpenUVJsonResult {

    private DecimalType uv;
    private ZonedDateTime uvTime;
    private DecimalType uvMax;
    private ZonedDateTime uvMaxTime;
    private DecimalType ozone;
    private ZonedDateTime ozoneTime;
    private OpenUVSafeExposureTime safeExposureTime;

    public OpenUVJsonResult() {
    }

    public DecimalType getUv() {
        return uv;
    }

    public DecimalType getUvMax() {
        return uvMax;
    }

    public DecimalType getOzone() {
        return ozone;
    }

    public DateTimeType getUVTime() {
        return new DateTimeType(uvTime.withZoneSameInstant(ZoneId.systemDefault()));
    }

    public DateTimeType getUVMaxTime() {
        return new DateTimeType(getUVMaxTimeAsZDT());
    }

    public ZonedDateTime getUVMaxTimeAsZDT() {
        return uvMaxTime.withZoneSameInstant(ZoneId.systemDefault());
    }

    public DateTimeType getOzoneTime() {
        return new DateTimeType(ozoneTime.withZoneSameInstant(ZoneId.systemDefault()));
    }

    public OpenUVSafeExposureTime getSafeExposureTime() {
        return safeExposureTime;
    }

}
