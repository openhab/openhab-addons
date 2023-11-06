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
package org.openhab.binding.seneye.internal;

import org.openhab.core.library.types.DateTimeType;

/**
 * The Status of the seneye device
 *
 * @author Niko Tanghe - Initial contribution
 */

public class SeneyeStatus {
    public String disconnected;
    public String slide_serial;
    public String slide_expires;
    public String out_of_water;
    public String wrong_slide;
    public String last_experiment;

    public String getLast_experimentDate() {
        return getTickAsDate(last_experiment);
    }

    public String getSlide_expiresDate() {
        return getTickAsDate(slide_expires);
    }

    private String getTickAsDate(String tick) {
        return new java.text.SimpleDateFormat(DateTimeType.DATE_PATTERN_WITH_TZ_AND_MS_ISO)
                .format(new java.util.Date(Long.parseLong(tick) * 1000));
    }

    public String getWrong_slideString() {
        return wrong_slide;
    }

    public String getSlide_serialString() {
        return slide_serial;
    }

    public String getOut_of_waterString() {
        return out_of_water;
    }

    public String getDisconnectedString() {
        return disconnected;
    }
}
