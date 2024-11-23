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
 * The {@link AlertDTO} The Alert object represents an alert generated either
 * by a thermostat or user which requires user attention. It may be an error,
 * or a reminder for a filter change. Alerts may not be modified directly but
 * rather they must be acknowledged using the Acknowledge Function.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class AlertDTO {

    /*
     *
     */
    public String acknowledgeRef;

    /*
     *
     */
    public String date;

    /*
     *
     */
    public String time;

    /*
     *
     */
    public String severity;

    /*
     *
     */
    public String text;

    /*
     *
     */
    public Integer alertNumber;

    /*
     *
     */
    public String alertType;

    /*
     *
     */
    public Boolean isOperatorAlert;

    /*
     *
     */
    public String reminder;

    /*
     *
     */
    public Boolean showIdt;

    /*
     *
     */
    public Boolean showWeb;

    /*
     *
     */
    public Boolean sendEmail;

    /*
     *
     */
    public String acknowledgement;

    /*
     *
     */
    public Boolean remindMeLater;

    /*
     *
     */
    public String thermostatIdentifier;

    /*
     *
     */
    public String notificationType;
}
