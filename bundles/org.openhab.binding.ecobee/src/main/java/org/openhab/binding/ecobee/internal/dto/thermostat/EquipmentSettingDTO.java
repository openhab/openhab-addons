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

import org.openhab.binding.ecobee.internal.enums.EquipmentNotificationType;

/**
 * The {@link EquipmentSettingDTO} represents the alert/reminder type which is associated
 * with and dependent upon specific equipment controlled by the Thermostat. It is used
 * when getting/setting the Thermostat NotificationSettings object. Note: Only the notification
 * settings for the equipment/devices currently controlled by the Thermostat are returned during
 * GET request, and only those same settings can be updated using the POST request. The type
 * corresponds to the Alert.notificationType returned when alerts are also included in the
 * selection. See Alert for more information.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class EquipmentSettingDTO {

    /*
     * The date the filter was last changed for this equipment. String format: YYYY-MM-DD
     */
    public String filterLastChanged;

    /*
     * The value representing the life of the filter. This value is expressed in month or hour,
     * which is specified in the the filterLifeUnits property.
     */
    public Integer filterLife;

    /*
     * The units the filterLife field is measured in. Possible values are: month, hour. month
     * has a range of 1 - 12. hour has a range of 100 - 10000.
     */
    public String filterLifeUnits;

    /*
     * The date the reminder will be triggered. This is a read-only field and cannot be modified
     * through the API. The value is calculated and set by the thermostat.
     */
    public String remindMeDate;

    /*
     * Boolean value representing whether or not alerts/reminders are enabled for this
     * notification type or not.
     */
    public Boolean enabled;

    /*
     * The type of notification. Possible values are: hvac, furnaceFilter, humidifierFilter,
     * dehumidifierFilter, ventilator, ac, airFilter, airCleaner, uvLamp
     */
    public EquipmentNotificationType type;

    /*
     * Boolean value representing whether or not alerts/reminders should be sent to the
     * technician/contractor assoicated with the thermostat.
     */
    public Boolean remindTechnician;
}
