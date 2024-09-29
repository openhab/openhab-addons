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
package org.openhab.binding.ecobee.internal.dto.thermostat;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

/**
 * The {@link ThermostatDTO} is the central piece of the ecobee API. All objects
 * relate in one way or another to a real thermostat. The thermostat object
 * and its component objects define the real thermostat device.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class ThermostatDTO {
    /*
     * The unique thermostat serial number.
     */
    public String identifier;

    /*
     * A user defined name for a thermostat.
     */
    public String name;

    /*
     * The current thermostat configuration revision.
     */
    public String thermostatRev;

    /*
     * Whether the user registered the thermostat.
     */
    public Boolean isRegistered;

    /*
     * The thermostat model number.
     *
     * Values: apolloSmart, apolloEms, idtSmart, idtEms, siSmart, siEms,
     * athenaSmart, athenaEms, corSmart, nikeSmart, nikeEms
     */
    public String modelNumber;

    /*
     * The thermostat brand.
     */
    public String brand;

    /*
     * The comma-separated list of the thermostat's additional features, if any.
     */
    public String features;

    /*
     * The last modified date time for the thermostat configuration.
     */
    public Instant lastModified;

    /*
     * The current time in the thermostat's time zone.
     */
    public LocalDateTime thermostatTime;

    /*
     * The current time in UTC.
     */
    public String utcTime;

    /*
     * The status of all equipment controlled by this Thermostat.
     * Only running equipment is listed in the CSV String.
     *
     * Values: heatPump, heatPump2, heatPump3, compCool1, compCool2,
     * auxHeat1, auxHeat2, auxHeat3, fan, humidifier, dehumidifier,
     * ventilator, economizer, compHotWater, auxHotWater.
     *
     * Note: If no equipment is currently running an empty String is returned.
     * If Settings.hasHeatPump is true, heatPump value will be returned for
     * heating, compCool for cooling, and auxHeat for aux heat.
     * If Settings.hasForcedAir or Settings.hasBoiler is true, auxHeat value
     * will be returned for heating and compCool for cooling (heatPump will
     * not show up for heating).
     */
    public String equipmentStatus;

    public List<AlertDTO> alerts;

    public AudioDTO audio;

    public List<DeviceDTO> devices;

    public ElectricityDTO electricity;

    public EnergyDTO energy;

    public List<EventDTO> events;

    public ExtendedRuntimeDTO extendedRuntime;

    public HouseDetailsDTO houseDetails;

    public LocationDTO location;

    public ManagementDTO management;

    public NotificationSettingsDTO notificationSettings;

    public OemCfgDTO oemCfg;

    public PrivacyDTO privacy;

    public ProgramDTO program;

    public List<ReminderDTO> reminders;

    public RuntimeDTO runtime;

    public SecuritySettingsDTO securitySettings;

    public List<RemoteSensorDTO> remoteSensors;

    public SettingsDTO settings;

    public TechnicianDTO technician;

    public UtilityDTO utility;

    public VersionDTO version;

    public WeatherDTO weather;
}
