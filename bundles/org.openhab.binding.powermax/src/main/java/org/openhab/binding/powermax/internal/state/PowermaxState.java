/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.powermax.internal.state;

import static org.openhab.binding.powermax.internal.PowermaxBindingConstants.*;

import java.util.HashMap;
import java.util.Map;

import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class to store the state of the alarm system
 *
 * @author Laurent Garnier - Initial contribution
 */
public class PowermaxState extends PowermaxStateContainer {

    private final Logger logger = LoggerFactory.getLogger(PowermaxState.class);

    // For values that are mapped to channels, use a channel name constant from
    // PowermaxBindingConstants. For values used internally but not mapped to
    // channels, use a unique name starting with "_".

    public BooleanValue powerlinkMode = new BooleanValue(this, "_powerlink_mode");
    public BooleanValue downloadMode = new BooleanValue(this, "_download_mode");
    public BooleanValue ready = new BooleanValue(this, READY);
    public BooleanValue bypass = new BooleanValue(this, WITH_ZONES_BYPASSED);
    public BooleanValue alarmActive = new BooleanValue(this, ALARM_ACTIVE);
    public BooleanValue trouble = new BooleanValue(this, TROUBLE);
    public BooleanValue alertInMemory = new BooleanValue(this, ALERT_IN_MEMORY);
    public StringValue statusStr = new StringValue(this, SYSTEM_STATUS);
    public StringValue armMode = new StringValue(this, "_arm_mode");
    public BooleanValue downloadSetupRequired = new BooleanValue(this, "_download_setup_required");
    public DateTimeValue lastKeepAlive = new DateTimeValue(this, "_last_keepalive");
    public DateTimeValue lastMessageReceived = new DateTimeValue(this, "_last_message_received");
    public StringValue panelStatus = new StringValue(this, "_panel_status");
    public StringValue alarmType = new StringValue(this, "_alarm_type");
    public StringValue troubleType = new StringValue(this, "_trouble_type");

    public DynamicValue<Boolean> isArmed = new DynamicValue<>(this, SYSTEM_ARMED, () -> {
        return isArmed();
    }, () -> {
        return isArmed() ? OnOffType.ON : OnOffType.OFF;
    });

    public DynamicValue<String> panelMode = new DynamicValue<>(this, MODE, () -> {
        return getPanelMode();
    }, () -> {
        return new StringType(getPanelMode());
    });

    public DynamicValue<String> shortArmMode = new DynamicValue<>(this, ARM_MODE, () -> {
        return getShortArmMode();
    }, () -> {
        return new StringType(getShortArmMode());
    });

    public DynamicValue<Boolean> pgmStatus = new DynamicValue<>(this, PGM_STATUS, () -> {
        return getPGMX10DeviceStatus(0);
    }, () -> {
        return getPGMX10DeviceStatus(0) ? OnOffType.ON : OnOffType.OFF;
    });

    private PowermaxZoneState[] zones;
    private Boolean[] pgmX10DevicesStatus;
    private byte[] updateSettings;
    private String[] eventLog;
    private Map<Integer, Byte> updatedZoneNames;
    private Map<Integer, Integer> updatedZoneInfos;

    /**
     * Constructor (default values)
     */
    public PowermaxState(PowermaxPanelSettings panelSettings, TimeZoneProvider timeZoneProvider) {
        super(timeZoneProvider);

        zones = new PowermaxZoneState[panelSettings.getNbZones()];
        for (int i = 0; i < panelSettings.getNbZones(); i++) {
            zones[i] = new PowermaxZoneState(timeZoneProvider);
        }
        pgmX10DevicesStatus = new Boolean[panelSettings.getNbPGMX10Devices()];
        updatedZoneNames = new HashMap<>();
        updatedZoneInfos = new HashMap<>();
    }

    /**
     * Return the PowermaxZoneState object for a given zone. If the zone number is
     * out of range, returns a dummy PowermaxZoneState object that won't be
     * persisted. The return value is never null, so it's safe to chain method
     * calls.
     *
     * @param zone the index of the zone (first zone is index 1)
     * @return the zone state object (or a dummy zone state)
     */
    public PowermaxZoneState getZone(int zone) {
        if ((zone < 1) || (zone > zones.length)) {
            logger.warn("Received update for invalid zone {}", zone);
            return new PowermaxZoneState(timeZoneProvider);
        } else {
            return zones[zone - 1];
        }
    }

    /**
     * Get the status of a PGM or X10 device
     *
     * @param device the index of the PGM/X10 device (0 s for PGM; for X10 device is index 1)
     *
     * @return the status (true or false)
     */
    public Boolean getPGMX10DeviceStatus(int device) {
        return ((device < 0) || (device >= pgmX10DevicesStatus.length)) ? null : pgmX10DevicesStatus[device];
    }

    /**
     * Set the status of a PGM or X10 device
     *
     * @param device the index of the PGM/X10 device (0 s for PGM; for X10 device is index 1)
     * @param status true or false
     */
    public void setPGMX10DeviceStatus(int device, Boolean status) {
        if ((device >= 0) && (device < pgmX10DevicesStatus.length)) {
            this.pgmX10DevicesStatus[device] = status;
        }
    }

    /**
     * Get the raw buffer containing all the settings
     *
     * @return the raw buffer as a table of bytes
     */
    public byte[] getUpdateSettings() {
        return updateSettings;
    }

    /**
     * Set the raw buffer containing all the settings
     *
     * @param updateSettings the raw buffer as a table of bytes
     */
    public void setUpdateSettings(byte[] updateSettings) {
        this.updateSettings = updateSettings;
    }

    /**
     * Get the number of entries in the event log
     *
     * @return the number of entries
     */
    public int getEventLogSize() {
        return (eventLog == null) ? 0 : eventLog.length;
    }

    /**
     * Set the number of entries in the event log
     *
     * @param size the number of entries
     */
    public void setEventLogSize(int size) {
        eventLog = new String[size];
    }

    /**
     * Get one entry from the event logs
     *
     * @param index the entry index (1 for the most recent entry)
     *
     * @return the entry value (event)
     */
    public String getEventLog(int index) {
        return ((index < 1) || (index > getEventLogSize())) ? null : eventLog[index - 1];
    }

    /**
     * Set one entry from the event logs
     *
     * @param index the entry index (1 for the most recent entry)
     * @param event the entry value (event)
     */
    public void setEventLog(int index, String event) {
        if ((index >= 1) && (index <= getEventLogSize())) {
            this.eventLog[index - 1] = event;
        }
    }

    public Map<Integer, Byte> getUpdatedZoneNames() {
        return updatedZoneNames;
    }

    public void updateZoneName(int zoneIdx, byte zoneNameIdx) {
        this.updatedZoneNames.put(zoneIdx, zoneNameIdx);
    }

    public Map<Integer, Integer> getUpdatedZoneInfos() {
        return updatedZoneInfos;
    }

    public void updateZoneInfo(int zoneIdx, int zoneInfo) {
        this.updatedZoneInfos.put(zoneIdx, zoneInfo);
    }

    /**
     * Get the panel mode
     *
     * @return either Download or Powerlink or Standard
     */
    public String getPanelMode() {
        String mode = null;
        if (Boolean.TRUE.equals(downloadMode.getValue())) {
            mode = "Download";
        } else if (Boolean.TRUE.equals(powerlinkMode.getValue())) {
            mode = "Powerlink";
        } else if (Boolean.FALSE.equals(powerlinkMode.getValue())) {
            mode = "Standard";
        }
        return mode;
    }

    /**
     * Get whether or not the current arming mode is considered as armed
     *
     * @return true or false
     */
    public Boolean isArmed() {
        return isArmed(armMode.getValue());
    }

    /**
     * Get whether or not an arming mode is considered as armed
     *
     * @param armMode the arming mode
     *
     * @return true or false; null if mode is unexpected
     */
    private static Boolean isArmed(String armMode) {
        Boolean result = null;
        if (armMode != null) {
            try {
                PowermaxArmMode mode = PowermaxArmMode.fromName(armMode);
                result = mode.isArmed();
            } catch (IllegalArgumentException e) {
                result = Boolean.FALSE;
            }
        }
        return result;
    }

    /**
     * Get the short description associated to the current arming mode
     *
     * @return the short description
     */
    public String getShortArmMode() {
        return getShortArmMode(armMode.getValue());
    }

    /**
     * Get the short name associated to an arming mode
     *
     * @param armMode the arming mode
     *
     * @return the short name or null if mode is unexpected
     */
    private static String getShortArmMode(String armMode) {
        String result = null;
        if (armMode != null) {
            try {
                PowermaxArmMode mode = PowermaxArmMode.fromName(armMode);
                result = mode.getShortName();
            } catch (IllegalArgumentException e) {
                result = armMode;
            }
        }
        return result;
    }

    /**
     * Keep only data that are different from another state and reset all others data to undefined
     *
     * @param otherState the other state
     */
    public void keepOnlyDifferencesWith(PowermaxState otherState) {
        for (int zone = 1; zone <= zones.length; zone++) {
            PowermaxZoneState thisZone = getZone(zone);
            PowermaxZoneState otherZone = otherState.getZone(zone);

            for (int i = 0; i < thisZone.getValues().size(); i++) {
                Value<?> thisValue = thisZone.getValues().get(i);
                Value<?> otherValue = otherZone.getValues().get(i);

                if ((thisValue.getValue() != null) && thisValue.getValue().equals(otherValue.getValue())) {
                    thisValue.setValue(null);
                }
            }
        }

        for (int i = 0; i < pgmX10DevicesStatus.length; i++) {
            if ((getPGMX10DeviceStatus(i) != null)
                    && getPGMX10DeviceStatus(i).equals(otherState.getPGMX10DeviceStatus(i))) {
                setPGMX10DeviceStatus(i, null);
            }
        }

        for (int i = 0; i < getValues().size(); i++) {
            Value<?> thisValue = getValues().get(i);
            Value<?> otherValue = otherState.getValues().get(i);

            if ((thisValue.getValue() != null) && thisValue.getValue().equals(otherValue.getValue())) {
                thisValue.setValue(null);
            }
        }
    }

    /**
     * Update (override) the current state data from another state, ignoring in this other state
     * the undefined data
     *
     * @param update the other state to consider for the update
     */
    public void merge(PowermaxState update) {
        for (int zone = 1; zone <= zones.length; zone++) {
            PowermaxZoneState thisZone = getZone(zone);
            PowermaxZoneState otherZone = update.getZone(zone);

            for (int i = 0; i < thisZone.getValues().size(); i++) {
                Value<?> thisValue = thisZone.getValues().get(i);
                Value<?> otherValue = otherZone.getValues().get(i);

                if (otherValue.getValue() != null) {
                    thisValue.setValueUnsafe(otherValue.getValue());
                }
            }
        }

        for (int i = 0; i < pgmX10DevicesStatus.length; i++) {
            if (update.getPGMX10DeviceStatus(i) != null) {
                setPGMX10DeviceStatus(i, update.getPGMX10DeviceStatus(i));
            }
        }

        for (int i = 0; i < getValues().size(); i++) {
            Value<?> thisValue = getValues().get(i);
            Value<?> otherValue = update.getValues().get(i);

            if (otherValue.getValue() != null) {
                thisValue.setValueUnsafe(otherValue.getValue());
            }
        }

        if (update.getEventLogSize() > getEventLogSize()) {
            setEventLogSize(update.getEventLogSize());
        }
        for (int i = 1; i <= getEventLogSize(); i++) {
            if (update.getEventLog(i) != null) {
                setEventLog(i, update.getEventLog(i));
            }
        }
    }

    @Override
    public String toString() {
        String str = "Bridge state:";

        for (Value<?> value : getValues()) {
            if ((value.getChannel() != null) && (value.getValue() != null)) {
                String channel = value.getChannel();
                String v_str = value.getValue().toString();
                String state = value.getState().toString();

                str += "\n - " + channel + " = " + v_str;
                if (!v_str.equals(state)) {
                    str += " (" + state + ")";
                }
            }
        }

        for (int i = 0; i < pgmX10DevicesStatus.length; i++) {
            if (getPGMX10DeviceStatus(i) != null) {
                str += String.format("\n - %s status = %s", (i == 0) ? "PGM device" : String.format("X10 device %d", i),
                        getPGMX10DeviceStatus(i) ? "ON" : "OFF");
            }
        }

        for (int i = 1; i <= zones.length; i++) {
            for (Value<?> value : zones[i - 1].getValues()) {
                if ((value.getChannel() != null) && (value.getValue() != null)) {
                    String channel = value.getChannel();
                    String v_str = value.getValue().toString();
                    String state = value.getState().toString();

                    str += String.format("\n - sensor zone %d %s = %s", i, channel, v_str);
                    if (!v_str.equals(state)) {
                        str += " (" + state + ")";
                    }
                }
            }
        }

        for (int i = 1; i <= getEventLogSize(); i++) {
            if (getEventLog(i) != null) {
                str += "\n - event log " + i + " = " + getEventLog(i);
            }
        }

        return str;
    }
}
