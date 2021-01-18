/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.bmwconnecteddrive.internal.dto.charge;

import org.openhab.binding.bmwconnecteddrive.internal.utils.ChargeProfileUtils;
import org.openhab.binding.bmwconnecteddrive.internal.utils.Constants;

/**
 * The {@link ChargingWindow} Data Transfer Object
 *
 * @author Bernd Weymann - Initial contribution
 * @author Norbert Truchsess - contributor
 */
public class ChargingWindow {
    public String startTime;// ":"11:00",
    public String endTime;// ":"17:00"}}

    public void completeChargingWindow() {
        if (startTime == null) {
            startTime = Constants.NULL_TIME;
        }
        if (endTime == null) {
            endTime = Constants.NULL_TIME;
        }
    }

    public void setStartMinute(int minute) {
        startTime = ChargeProfileUtils.withMinute(startTime, minute);
    }

    public void setStartHour(int hour) {
        startTime = ChargeProfileUtils.withHour(startTime, hour);
    }

    public void setEndMinute(int minute) {
        endTime = ChargeProfileUtils.withMinute(endTime, minute);
    }

    public void setEndHour(int hour) {
        endTime = ChargeProfileUtils.withHour(endTime, hour);
    }
}
