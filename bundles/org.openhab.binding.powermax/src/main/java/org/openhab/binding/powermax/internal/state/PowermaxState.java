/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.powermax.internal.message.PowermaxMessageConstants;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class to store the state of the alarm system
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
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
    public BooleanValue ringing = new BooleanValue(this, RINGING);
    public DateTimeValue ringingSince = new DateTimeValue(this, "_ringing_since");
    public StringValue statusStr = new StringValue(this, SYSTEM_STATUS);
    public StringValue armMode = new StringValue(this, "_arm_mode");
    public BooleanValue downloadSetupRequired = new BooleanValue(this, "_download_setup_required");
    public DateTimeValue lastKeepAlive = new DateTimeValue(this, "_last_keepalive");
    public DateTimeValue lastMessageTime = new DateTimeValue(this, LAST_MESSAGE_TIME);

    public DynamicValue<Boolean> isArmed = new DynamicValue<>(this, SYSTEM_ARMED, () -> {
        return isArmed();
    }, () -> {
        Boolean isArmed = isArmed();
        if (isArmed == null) {
            return UnDefType.NULL;
        }
        return OnOffType.from(isArmed);
    });

    public DynamicValue<String> panelMode = new DynamicValue<>(this, MODE, () -> {
        return getPanelMode();
    }, () -> {
        String mode = getPanelMode();
        if (mode == null) {
            return UnDefType.NULL;
        }
        return new StringType(mode);
    });

    public DynamicValue<String> shortArmMode = new DynamicValue<>(this, ARM_MODE, () -> {
        return getShortArmMode();
    }, () -> {
        String mode = getShortArmMode();
        if (mode == null) {
            return UnDefType.NULL;
        }
        return new StringType(mode);
    });

    public DynamicValue<String> activeAlerts = new DynamicValue<>(this, ACTIVE_ALERTS, () -> {
        return getActiveAlerts();
    }, () -> {
        return new StringType(getActiveAlerts());
    });

    public DynamicValue<Boolean> pgmStatus = new DynamicValue<>(this, PGM_STATUS, () -> {
        return getPGMX10DeviceStatus(0);
    }, () -> {
        Boolean status = getPGMX10DeviceStatus(0);
        if (status == null) {
            return UnDefType.NULL;
        }
        return OnOffType.from(status);
    });

    private PowermaxPanelSettings panelSettings;
    private PowermaxZoneState[] zones;
    private Boolean[] pgmX10DevicesStatus;
    private byte @Nullable [] updateSettings;
    private String @Nullable [] eventLog;
    private Map<Integer, Byte> updatedZoneNames;
    private Map<Integer, Integer> updatedZoneInfos;
    private List<PowermaxActiveAlert> activeAlertList;
    private List<PowermaxActiveAlert> activeAlertQueue;

    private enum PowermaxAlertAction {
        ADD,
        CLEAR,
        CLEAR_ALL
    }

    private class PowermaxActiveAlert {
        public final @Nullable PowermaxAlertAction action;
        public final int zone;
        public final int code;

        public PowermaxActiveAlert(@Nullable PowermaxAlertAction action, int zone, int code) {
            this.action = action;
            this.zone = zone;
            this.code = code;
        }
    }

    /**
     * Constructor (default values)
     */
    public PowermaxState(PowermaxPanelSettings panelSettings, TimeZoneProvider timeZoneProvider) {
        super(timeZoneProvider);
        this.panelSettings = panelSettings;

        zones = new PowermaxZoneState[panelSettings.getNbZones()];
        for (int i = 0; i < panelSettings.getNbZones(); i++) {
            zones[i] = new PowermaxZoneState(timeZoneProvider);
        }
        pgmX10DevicesStatus = new Boolean[panelSettings.getNbPGMX10Devices()];
        updatedZoneNames = new HashMap<>();
        updatedZoneInfos = new HashMap<>();
        activeAlertList = new ArrayList<>();
        activeAlertQueue = new ArrayList<>();

        // Most fields will get populated by the initial download, but we set
        // the ringing indicator in response to an alarm message. We have no
        // other way to know if the siren is ringing so we'll initialize it to
        // false.

        this.ringing.setValue(false);
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
    public @Nullable Boolean getPGMX10DeviceStatus(int device) {
        return ((device < 0) || (device >= pgmX10DevicesStatus.length)) ? null : pgmX10DevicesStatus[device];
    }

    /**
     * Set the status of a PGM or X10 device
     *
     * @param device the index of the PGM/X10 device (0 s for PGM; for X10 device is index 1)
     * @param status true or false
     */
    public void setPGMX10DeviceStatus(int device, @Nullable Boolean status) {
        if ((device >= 0) && (device < pgmX10DevicesStatus.length)) {
            this.pgmX10DevicesStatus[device] = status;
        }
    }

    /**
     * Get the raw buffer containing all the settings
     *
     * @return the raw buffer as a table of bytes
     */
    public byte @Nullable [] getUpdateSettings() {
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
        String @Nullable [] localEventLog = eventLog;
        return (localEventLog == null) ? 0 : localEventLog.length;
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
    public @Nullable String getEventLog(int index) {
        String @Nullable [] localEventLog = eventLog;
        return ((localEventLog == null) || (index < 1) || (index > getEventLogSize())) ? null
                : localEventLog[index - 1];
    }

    /**
     * Set one entry from the event logs
     *
     * @param index the entry index (1 for the most recent entry)
     * @param event the entry value (event)
     */
    public void setEventLog(int index, String event) {
        String @Nullable [] localEventLog = eventLog;
        if ((localEventLog != null) && (index >= 1) && (index <= getEventLogSize())) {
            localEventLog[index - 1] = event;
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

    // This is an attempt to add persistence to an otherwise (mostly) stateless class.
    // All of the other values are either present or null, and it's easy to build a
    // delta state based only on which values are non-null. But these system events
    // are different because each event can be set by one message and cleared by a
    // later message. So to preserve the semantics of the state class, we'll keep a
    // queue of incoming changes, and apply them only when the delta state is resolved.

    public boolean hasActiveAlertsQueued() {
        return !activeAlertQueue.isEmpty();
    }

    public String getActiveAlerts() {
        if (activeAlertList.isEmpty()) {
            return "None";
        }

        List<String> alerts = new ArrayList<>();

        activeAlertList.forEach(e -> {
            String message = PowermaxMessageConstants.getSystemEvent(e.code).toString();
            String alert = e.zone == 0 ? message
                    : String.format("%s (%s)", message, panelSettings.getZoneOrUserName(e.zone));

            alerts.add(alert);
        });

        return String.join(", ", alerts);
    }

    public void addActiveAlert(int zoneIdx, int code) {
        PowermaxActiveAlert alert = new PowermaxActiveAlert(PowermaxAlertAction.ADD, zoneIdx, code);
        activeAlertQueue.add(alert);
    }

    public void clearActiveAlert(int zoneIdx, int code) {
        PowermaxActiveAlert alert = new PowermaxActiveAlert(PowermaxAlertAction.CLEAR, zoneIdx, code);
        activeAlertQueue.add(alert);
    }

    public void clearAllActiveAlerts() {
        PowermaxActiveAlert alert = new PowermaxActiveAlert(PowermaxAlertAction.CLEAR_ALL, 0, 0);
        activeAlertQueue.add(alert);
    }

    public void resolveActiveAlerts(@Nullable PowermaxState previousState) {
        copyActiveAlertsFrom(previousState);

        activeAlertQueue.forEach(alert -> {
            if (alert.action == PowermaxAlertAction.CLEAR_ALL) {
                activeAlertList.clear();
            } else {
                activeAlertList.removeIf(e -> e.zone == alert.zone && e.code == alert.code);

                if (alert.action == PowermaxAlertAction.ADD) {
                    activeAlertList.add(new PowermaxActiveAlert(null, alert.zone, alert.code));
                }
            }
        });
    }

    private void copyActiveAlertsFrom(@Nullable PowermaxState state) {
        activeAlertList = new ArrayList<>();

        if (state != null) {
            state.activeAlertList.forEach(alert -> {
                activeAlertList.add(new PowermaxActiveAlert(null, alert.zone, alert.code));
            });
        }
    }

    /**
     * Get the panel mode
     *
     * @return either Download or Powerlink or Standard
     */
    public @Nullable String getPanelMode() {
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
    public @Nullable Boolean isArmed() {
        return isArmed(armMode.getValue());
    }

    /**
     * Get whether or not an arming mode is considered as armed
     *
     * @param armMode the arming mode
     *
     * @return true or false; null if mode is unexpected
     */
    private static @Nullable Boolean isArmed(@Nullable String armMode) {
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
    public @Nullable String getShortArmMode() {
        return getShortArmMode(armMode.getValue());
    }

    /**
     * Get the short name associated to an arming mode
     *
     * @param armMode the arming mode
     *
     * @return the short name or null if mode is unexpected
     */
    private static @Nullable String getShortArmMode(@Nullable String armMode) {
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
            Boolean status = getPGMX10DeviceStatus(i);
            if ((status != null) && status.equals(otherState.getPGMX10DeviceStatus(i))) {
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

        if (hasActiveAlertsQueued()) {
            resolveActiveAlerts(otherState);
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
            Boolean status = update.getPGMX10DeviceStatus(i);
            if (status != null) {
                setPGMX10DeviceStatus(i, status);
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
            String log = update.getEventLog(i);
            if (log != null) {
                setEventLog(i, log);
            }
        }

        if (update.hasActiveAlertsQueued()) {
            copyActiveAlertsFrom(update);
        }
    }

    @Override
    public String toString() {
        String str = "Bridge state:";

        for (Value<?> value : getValues()) {
            if (value.getValue() != null) {
                String channel = value.getChannel();
                String vStr = value.getValue().toString();
                String state = value.getState().toString();

                str += "\n - " + channel + " = " + vStr;
                if (!vStr.equals(state)) {
                    str += " (" + state + ")";
                }
            }
        }

        for (int i = 0; i < pgmX10DevicesStatus.length; i++) {
            Boolean status = getPGMX10DeviceStatus(i);
            if (status != null) {
                str += String.format("\n - %s status = %s", (i == 0) ? "PGM device" : String.format("X10 device %d", i),
                        status ? "ON" : "OFF");
            }
        }

        for (int i = 1; i <= zones.length; i++) {
            for (Value<?> value : zones[i - 1].getValues()) {
                if (value.getValue() != null) {
                    String channel = value.getChannel();
                    String vStr = value.getValue().toString();
                    String state = value.getState().toString();

                    str += String.format("\n - sensor zone %d %s = %s", i, channel, vStr);
                    if (!vStr.equals(state)) {
                        str += " (" + state + ")";
                    }
                }
            }
        }

        for (int i = 1; i <= getEventLogSize(); i++) {
            String log = getEventLog(i);
            if (log != null) {
                str += "\n - event log " + i + " = " + log;
            }
        }

        str += "\n - active alarms/alerts = " + getActiveAlerts();

        return str;
    }
}
