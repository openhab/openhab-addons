/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
 * The {@link NeatoState} is the internal class for state information from the vacuum cleaner.
 *
 * @author Patrik Wimnell - Initial contribution
 */
public class NeatoState {

    private Integer version;
    private String reqId;
    private String result;
    private String error;
    private RobotInfoData data;
    private Integer state;
    private Integer action;
    private Cleaning cleaning;
    private Details details;
    private AvailableCommands availableCommands;
    private AvailableServices availableServices;
    private Meta meta;

    public enum RobotState {
        INVALID(0),
        IDLE(1),
        BUSY(2),
        PAUSED(3),
        ERROR(4);

        private int value;

        private RobotState(int value) {
            this.value = value;
        }

        public static RobotState fromValue(int value) {
            for (RobotState s : RobotState.values()) {
                if (s.value == value) {
                    return s;
                }
            }
            return INVALID;
        }
    }

    public enum RobotAction {
        INVALID(0),
        HOUSE_CLEANING(1),
        SPOT_CLEANING(2),
        MANUAL_CLEANING(3),
        DOCKING(4),
        USER_MENU_ACTIVE(5),
        SUSPENDED_CLEANING(6),
        UPDATING(7),
        COPYING_LOGS(8),
        RECOVERING_LOCATION(9),
        IEC_TEST(10),
        MAP_CLEANING(11),
        EXPLORING_MAP(12),
        AQUIRING_MAP_IDS(13),
        CREATING_MAP(14),
        SUSPENDED_EXPLORATION(15);

        private int value;

        private RobotAction(int value) {
            this.value = value;
        }

        public static RobotAction fromValue(int value) {
            for (RobotAction a : RobotAction.values()) {
                if (a.value == value) {
                    return a;
                }
            }
            return INVALID;
        }
    }

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
        return error;
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

    public RobotState getRobotState() {
        return RobotState.fromValue(this.state);
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Integer getAction() {
        return action;
    }

    public RobotAction getRobotAction() {
        return RobotAction.fromValue(this.action);
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
