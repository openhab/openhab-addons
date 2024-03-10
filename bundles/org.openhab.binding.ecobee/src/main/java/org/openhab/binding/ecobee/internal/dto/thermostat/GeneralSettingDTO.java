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

/**
 * The {@link GeneralSettingDTO} represent the General alert/reminder type. It is
 * used when getting/setting the Thermostat NotificationSettings object. The type
 * corresponds to the Alert.notificationType returned when alerts are included in
 * the selection. See Alert for more information.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class GeneralSettingDTO {

    /*
     * Boolean value representing whether or not alerts/reminders are enabled for this notification type or not.
     */
    public Boolean enabled;

    /*
     * The type of notification. Possible values are: temp
     */
    public String type;

    /*
     * Boolean value representing whether or not alerts/reminders should be sent to the
     * technician/contractor associated with the thermostat.
     */
    public Boolean remindTechnician;
}
