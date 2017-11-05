/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.neato.internal.classes;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * The {@link Details} is the internal class for detailed information about the vacuum cleaner.
 *
 * @author Patrik Wimnell - Initial contribution
 */
public class Details {

    @SerializedName("isCharging")
    @Expose
    private Boolean isCharging;
    @SerializedName("isDocked")
    @Expose
    private Boolean isDocked;
    @SerializedName("isScheduleEnabled")
    @Expose
    private Boolean isScheduleEnabled;
    @SerializedName("dockHasBeenSeen")
    @Expose
    private Boolean dockHasBeenSeen;
    @SerializedName("charge")
    @Expose
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
