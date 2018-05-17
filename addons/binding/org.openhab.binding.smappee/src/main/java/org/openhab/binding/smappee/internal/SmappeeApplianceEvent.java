/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smappee.internal;

import org.eclipse.smarthome.core.library.types.DateTimeType;

/**
 * What is the active power consumption of a specific appliance ?
 *
 * @author Niko Tanghe - Initial contribution
 */
public class SmappeeApplianceEvent {

    public double activePower;
    public String applianceId;
    public String timestamp;

    public String getTimestamp() {
        return getTickAsDate(timestamp);
    }

    private String getTickAsDate(String tick) {
        String date = new java.text.SimpleDateFormat(DateTimeType.DATE_PATTERN_WITH_TZ_AND_MS_ISO)
                .format(new java.util.Date(Long.parseLong(tick) * 1000));
        return date;
    }
}
