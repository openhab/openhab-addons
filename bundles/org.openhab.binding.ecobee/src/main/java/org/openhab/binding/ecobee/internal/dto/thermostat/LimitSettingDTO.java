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
package org.openhab.binding.ecobee.internal.dto.thermostat;

import org.openhab.binding.ecobee.internal.enums.LimitNotificationType;

/**
 * The {@link LimitSettingDTO} represents the alert/reminder type which is associated
 * specific values, such as highHeat or lowHumidity. It is used when getting/setting
 * the Thermostat NotificationSettings object. The type corresponds to the
 * Alert.notificationType returned when alerts are also included in the selection.
 * See Alert for more information.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class LimitSettingDTO {

    /*
     * The value of the limit to set. For temperatures the value is expressed as
     * degrees Fahrenheit, multipled by 10. For humidity values are expressed as
     * a percentage from 5 to 95. See here for more information.
     */

    public Integer limit;

    /*
     * Boolean value representing whether or not alerts/reminders are enabled for
     * this notification type or not.
     */
    public Boolean enabled;

    /*
     * The type of notification. Possible values are: lowTemp, highTemp, lowHumidity,
     * highHumidity, auxHeat, auxOutdoor
     */
    public LimitNotificationType type;

    /*
     * Boolean value representing whether or not alerts/reminders should be sent to
     * the technician/contractor associated with the thermostat.
     */
    public Boolean remindTechnician;
}
