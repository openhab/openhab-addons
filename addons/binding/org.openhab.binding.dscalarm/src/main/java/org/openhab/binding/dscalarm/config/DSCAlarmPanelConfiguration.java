/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.dscalarm.config;

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
