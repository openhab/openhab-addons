/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.seneye.internal;

import org.eclipse.smarthome.core.library.types.DateTimeType;

/**
 * The Status of the seneye device
 *
 * @author Niko Tanghe - Initial contribution
 */

public class SeneyeStatus {
    public boolean disconnected;
    public String slide_serial;
    public String slide_expires;
    public boolean out_of_water;
    public boolean wrong_slide;
    public String last_experiment;

    public String getLast_experimentDate() {
        return getTickAsDate(last_experiment);
    }

    public String getSlide_expiresDate() {
        return getTickAsDate(slide_expires);
    }

    private String getTickAsDate(String tick) {
        String date = new java.text.SimpleDateFormat(DateTimeType.DATE_PATTERN_WITH_TZ_AND_MS_ISO)
                .format(new java.util.Date(Long.parseLong(tick) * 1000));
        return date;
    }
}
