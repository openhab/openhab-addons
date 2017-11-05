/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.neato.internal.classes;

import static org.openhab.binding.neato.NeatoBindingConstants.*;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * The {@link NeatoState} is the internal class for state information from the vacuum cleaner.
 *
 * @author Patrik Wimnell - Initial contribution
 */
public class NeatoState {

    @SerializedName("version")
    @Expose
    private Integer version;
    @SerializedName("reqId")
    @Expose
    private String reqId;
    @SerializedName("result")
    @Expose
    private String result;
    @SerializedName("error")
    @Expose
    private String error;
    @SerializedName("data")
    @Expose
    private RobotInfoData data;
    @SerializedName("state")
    @Expose
    private Integer state;
    @SerializedName("action")
    @Expose
    private Integer action;
    @SerializedName("cleaning")
    @Expose
    private Cleaning cleaning;
    @SerializedName("details")
    @Expose
    private Details details;
    @SerializedName("availableCommands")
    @Expose
    private AvailableCommands availableCommands;
    @SerializedName("availableServices")
    @Expose
    private AvailableServices availableServices;
    @SerializedName("meta")
    @Expose
    private Meta meta;

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getReqId() {
        return reqId;
    }

    public void setReqId(String reqId) {
        this.reqId = reqId;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getError() {

        if (!error.equalsIgnoreCase("ui_alert_invalid")) {
            return error;
        } else {
            return "";
        }
    }

    public void setError(String error) {
        this.error = error;
    }

    public RobotInfoData getData() {
        return data;
    }

    public void setData(RobotInfoData data) {
        this.data = data;
    }

    public Integer getState() {
        return state;
    }

    public String getStateString() {
        switch (this.state) {
            case NEATO_STATE_INVALID:
                return "INVALID";

            case NEATO_STATE_IDLE:
                return "IDLE";

            case NEATO_STATE_BUSY:
                return "BUSY";

            case NEATO_STATE_PAUSED:
                return "PAUSED";

            case NEATO_STATE_ERROR:
                return "ERROR";

        }
        return "NONE";
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Integer getAction() {
        return action;
    }

    public String getActionString() {

        if (this.state == NEATO_STATE_IDLE || this.state == NEATO_STATE_ERROR) {
            return "";
        }

        switch (this.action) {
            case NEATO_ACTION_INVALID:
                return "INVALID";
            case NEATO_ACTION_HOUSECLEANING:
                return "HOUSE CLEANING";
            case NEATO_ACTION_SPOTCLEANING:
                return "SPOT CLEANING";
            case NEATO_ACTION_MANUALCLEANING:
                return "MANUAL CLEANING";
            case NEATO_ACTION_DOCKING:
                return "DOCKING";
            case NEATO_ACTION_USERMENUACTIVE:
                return "USER MENU ACTIVE";
            case NEATO_ACTION_SUSPENDEDCLEANING:
                return "SUSPENDED CLEANING";
            case NEATO_ACTION_UPDATING:
                return "UPDATING";
            case NEATO_ACTION_COPYINGLOGS:
                return "COPYING LOGS";
            case NEATO_ACTION_RECOVERINGLOCATION:
                return "RECOVERING LOCATION";
            case NEATO_ACTION_IECTEST:
                return "IEC TEST";
        }
        return "";
    }

    public void setAction(Integer action) {
        this.action = action;
    }

    public Cleaning getCleaning() {
        return cleaning;
    }

    public void setCleaning(Cleaning cleaning) {
        this.cleaning = cleaning;
    }

    public Details getDetails() {
        return details;
    }

    public void setDetails(Details details) {
        this.details = details;
    }

    public AvailableCommands getAvailableCommands() {
        return availableCommands;
    }

    public void setAvailableCommands(AvailableCommands availableCommands) {
        this.availableCommands = availableCommands;
    }

    public AvailableServices getAvailableServices() {
        return availableServices;
    }

    public void setAvailableServices(AvailableServices availableServices) {
        this.availableServices = availableServices;
    }

    public Meta getMeta() {
        return meta;
    }

    public void setMeta(Meta meta) {
        this.meta = meta;
    }
}
