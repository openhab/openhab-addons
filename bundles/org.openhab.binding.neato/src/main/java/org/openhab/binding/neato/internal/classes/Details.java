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
package org.openhab.binding.neato.internal.classes;

/**
 * The {@link Details} is the internal class for detailed information about the vacuum cleaner.
 *
 * @author Patrik Wimnell - Initial contribution
 */
public class Details {

    private Boolean isCharging;
    private Boolean isDocked;
    private Boolean isScheduleEnabled;
    private Boolean dockHasBeenSeen;
    private Integer charge;

    public Boolean getIsCharging() {
        return isCharging;
    }

    public void setIsCharging(Boolean isCharging) {
        this.isCharging = isCharging;
    }

    public Boolean getIsDocked() {
        return isDocked;
    }

    public void setIsDocked(Boolean isDocked) {
        this.isDocked = isDocked;
    }

    public Boolean getIsScheduleEnabled() {
        return isScheduleEnabled;
    }

    public void setIsScheduleEnabled(Boolean isScheduleEnabled) {
        this.isScheduleEnabled = isScheduleEnabled;
    }

    public Boolean getDockHasBeenSeen() {
        return dockHasBeenSeen;
    }

    public void setDockHasBeenSeen(Boolean dockHasBeenSeen) {
        this.dockHasBeenSeen = dockHasBeenSeen;
    }

    public Integer getCharge() {
        return charge;
    }

    public void setCharge(Integer charge) {
        this.charge = charge;
    }
}
