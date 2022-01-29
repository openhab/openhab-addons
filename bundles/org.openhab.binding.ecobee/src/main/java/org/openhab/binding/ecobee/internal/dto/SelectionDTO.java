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
package org.openhab.binding.ecobee.internal.dto;

import java.util.Set;

/**
 * The {@link SelectionDTO} defines the resources and information to return
 * as part of a response. The selection is required in all requests however
 * meaning of some selection fields is only meaningful to certain types of requests.
 *
 * The selectionType parameter defines the type of selection to perform. The selectionMatch
 * specifies the matching criteria for the type specified.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class SelectionDTO {

    /*
     * The type of match data supplied: Values: thermostats, registered, managementSet.
     */
    public SelectionType selectionType;

    /*
     * The match data based on selectionType (e.g. a comma-separated list of thermostat
     * idendifiers in the case of a selectionType of thermostats)
     */
    public String selectionMatch;

    /*
     * Include the thermostat's unacknowledged alert objects. If not specified, defaults to false.
     */
    public Boolean includeAlerts;

    /*
     * Include the audio configuration for the selected Thermostat(s). If not specified, defaults to false.
     */
    public Boolean includeAudio;

    /*
     * Include the thermostat device configuration objects. If not specified, defaults to false.
     */
    public Boolean includeDevice;

    /*
     * Include the electricity readings object. If not specified, defaults to false.
     */
    public Boolean includeElectricity;

    /*
     * Include the energy configuration for the selected Thermostat(s). If not specified, defaults to false.
     */
    public Boolean includeEnergy;

    /*
     * Include the current thermostat equipment status information. If not specified, defaults to false.
     */
    public Boolean includeEquipmentStatus;

    /*
     * Include the thermostat calendar events objects. If not specified, defaults to false.
     */
    public Boolean includeEvents;

    /*
     * Include the extended thermostat runtime object. If not specified, defaults to false.
     */
    public Boolean includeExtendedRuntime;

    /*
     * Include the current thermostat house details object. If not specified, defaults to false.
     */
    public Boolean includeHouseDetails;

    /*
     * Include the thermostat location object. If not specified, defaults to false.
     */
    public Boolean includeLocation;

    /*
     * Include the thermostat management company object. If not specified, defaults to false.
     */
    public Boolean includeManagement;

    /*
     * Include the current thermostat alert and reminders settings. If not specified, defaults to false.
     */
    public Boolean includeNotificationSettings;

    /*
     * Include the current thermostat OemCfg object. If not specified, defaults to false.
     */
    public Boolean includeOemCfg;

    /*
     * Include the current thermostat privacy settings. Note: access to this object is restricted
     * to callers with implict authentication, setting this value to true without proper
     * credentials will result in an authentication exception.
     */
    public Boolean includePrivacy;

    /*
     * Include the thermostat program object. If not specified, defaults to false.
     */
    public Boolean includeProgram;

    /*
     * Include the thermostat reminder object. If not specified, defaults to false.
     */
    public Boolean includeReminders;

    /*
     * Include the thermostat runtime object. If not specified, defaults to false.
     */
    public Boolean includeRuntime;

    /*
     * Include the current securitySettings object for the selected Thermostat(s). If not specified, defaults to false.
     */
    public Boolean includeSecuritySettings;

    /*
     * Include the list of current thermostatRemoteSensor objects for the selected Thermostat(s).
     * If not specified, defaults to false.
     */
    public Boolean includeSensors;

    /*
     * Include the thermostat settings object. If not specified, defaults to false.
     */
    public Boolean includeSettings;

    /*
     * Include the thermostat technician object. If not specified, defaults to false.
     */
    public Boolean includeTechnician;

    /*
     * Include the thermostat utility company object. If not specified, defaults to false.
     */
    public Boolean includeUtility;

    /*
     * Include the current firmware version the Thermostat is running. If not specified, defaults to false.
     */
    public Boolean includeVersion;

    /*
     * Include the current thermostat weather forecast object. If not specified, defaults to false.
     */
    public Boolean includeWeather;

    public SelectionDTO() {
        selectionType = SelectionType.REGISTERED;
    }

    public void setThermostats(Set<String> thermostatIds) {
        if (thermostatIds == null || thermostatIds.isEmpty()) {
            selectionType = SelectionType.REGISTERED;
            selectionMatch = "";
        } else {
            selectionType = SelectionType.THERMOSTATS;
            selectionMatch = String.join(",", thermostatIds);
        }
    }

    public void setSelectionType(SelectionType selectionType) {
        this.selectionType = selectionType;
    }

    /**
     * Merge this selection object with the one passed in as a parameter.
     *
     * @param selection
     * @return A SelectionDTO object representing the merged selection objects
     */
    public SelectionDTO mergeSelection(SelectionDTO selection) {
        // Always get alerts, equipmentStatus, events, program, runtime, and sensors
        this.includeAlerts = Boolean.TRUE;
        this.includeEquipmentStatus = Boolean.TRUE;
        this.includeEvents = Boolean.TRUE;
        this.includeProgram = Boolean.TRUE;
        this.includeRuntime = Boolean.TRUE;
        this.includeSensors = Boolean.TRUE;

        this.includeAudio = Boolean.TRUE.equals(selection.includeAudio) ? Boolean.TRUE : includeAudio;
        this.includeDevice = Boolean.TRUE.equals(selection.includeDevice) ? Boolean.TRUE : includeDevice;
        this.includeElectricity = Boolean.TRUE.equals(selection.includeElectricity) ? Boolean.TRUE : includeElectricity;
        this.includeEnergy = Boolean.TRUE.equals(selection.includeEnergy) ? Boolean.TRUE : includeEnergy;
        this.includeExtendedRuntime = Boolean.TRUE.equals(selection.includeExtendedRuntime) ? Boolean.TRUE
                : includeExtendedRuntime;
        this.includeHouseDetails = Boolean.TRUE.equals(selection.includeHouseDetails) ? Boolean.TRUE
                : includeHouseDetails;
        this.includeLocation = Boolean.TRUE.equals(selection.includeLocation) ? Boolean.TRUE : includeLocation;
        this.includeManagement = Boolean.TRUE.equals(selection.includeManagement) ? Boolean.TRUE : includeManagement;
        this.includeNotificationSettings = Boolean.TRUE.equals(selection.includeNotificationSettings) ? Boolean.TRUE
                : includeNotificationSettings;
        this.includeOemCfg = Boolean.TRUE.equals(selection.includeOemCfg) ? Boolean.TRUE : includeOemCfg;
        this.includePrivacy = Boolean.TRUE.equals(selection.includePrivacy) ? Boolean.TRUE : includePrivacy;
        this.includeReminders = Boolean.TRUE.equals(selection.includeReminders) ? Boolean.TRUE : includeReminders;
        this.includeSecuritySettings = Boolean.TRUE.equals(selection.includeSecuritySettings) ? Boolean.TRUE
                : includeSecuritySettings;
        this.includeSettings = Boolean.TRUE.equals(selection.includeSettings) ? Boolean.TRUE : includeSettings;
        this.includeTechnician = Boolean.TRUE.equals(selection.includeTechnician) ? Boolean.TRUE : includeTechnician;
        this.includeUtility = Boolean.TRUE.equals(selection.includeUtility) ? Boolean.TRUE : includeUtility;
        this.includeVersion = Boolean.TRUE.equals(selection.includeVersion) ? Boolean.TRUE : includeVersion;
        this.includeWeather = Boolean.TRUE.equals(selection.includeWeather) ? Boolean.TRUE : includeWeather;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("selectionType=").append(selectionType).append(",");
        sb.append("selectionMatch=").append(selectionMatch).append(",");
        sb.append("includeAlerts=").append(includeAlerts).append(",");
        sb.append("includeAudio=").append(includeAudio).append(",");
        sb.append("includeDevice=").append(includeDevice).append(",");
        sb.append("includeElectricity=").append(includeElectricity).append(",");
        sb.append("includeEnergy=").append(includeEnergy).append(",");
        sb.append("includeEquipmentStatus=").append(includeEquipmentStatus).append(",");
        sb.append("includeExtendedRuntime=").append(includeExtendedRuntime).append(",");
        sb.append("includeHouseDetails=").append(includeHouseDetails).append(",");
        sb.append("includeLocation=").append(includeLocation).append(",");
        sb.append("includeManagement=").append(includeManagement).append(",");
        sb.append("includeNotificationSettings=").append(includeNotificationSettings).append(",");
        sb.append("includeOemCfg=").append(includeOemCfg).append(",");
        sb.append("includePrivacy=").append(includePrivacy).append(",");
        sb.append("includeProgram=").append(includeProgram).append(",");
        sb.append("includeReminders=").append(includeReminders).append(",");
        sb.append("includeRuntime=").append(includeRuntime).append(",");
        sb.append("includeSecuritySettings=").append(includeSecuritySettings).append(",");
        sb.append("includeSensors=").append(includeSensors).append(",");
        sb.append("includeSettings=").append(includeSettings).append(",");
        sb.append("includeTechnician=").append(includeTechnician).append(",");
        sb.append("includeUtility=").append(includeUtility).append(",");
        sb.append("includeVersion=").append(includeVersion).append(",");
        sb.append("includeWeather=").append(includeWeather);
        return sb.toString();
    }
}
