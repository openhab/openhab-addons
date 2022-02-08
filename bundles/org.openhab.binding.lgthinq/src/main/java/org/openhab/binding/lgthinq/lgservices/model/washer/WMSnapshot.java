/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.lgthinq.lgservices.model.washer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lgthinq.lgservices.model.DevicePowerState;
import org.openhab.binding.lgthinq.lgservices.model.Snapshot;

/**
 * The {@link WMSnapshot}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class WMSnapshot implements Snapshot {
    private DevicePowerState powerState = DevicePowerState.DV_POWER_UNK;
    private String course = "";

    @Override
    public DevicePowerState getPowerStatus() {
        return powerState;
    }

    @Override
    public void setPowerStatus(DevicePowerState value) {
        this.powerState = value;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    @Override
    public boolean isOnline() {
        return false;
    }

    @Override
    public void setOnline(boolean online) {
    }
}
