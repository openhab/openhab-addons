/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.dscalarm.internal.config;

/**
 * Configuration class for the DSC Alarm Panel Thing.
 *
 * @author Russell Stephens - Initial contribution
 */

public class DSCAlarmPanelConfiguration {

    // Panel Thing constants
    public static final String USER_CODE = "userCode";
    public static final String SUPPRESS_ACK_MSGS = "suppressAcknowledgementMsgs";

    /**
     * The Panel User Code. Default is 1234;
     */
    public String userCode;

    /**
     * Suppress Acknowledgement messages when received
     */
    public boolean suppressAcknowledgementMsgs;
}
