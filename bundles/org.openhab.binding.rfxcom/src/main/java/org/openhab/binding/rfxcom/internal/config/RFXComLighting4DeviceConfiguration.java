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
package org.openhab.binding.rfxcom.internal.config;

/**
 * Configuration class for Lighting4 RFXCOM device.
 *
 * @author James Hewitt-Thomas - Initial contribution
 */
public class RFXComLighting4DeviceConfiguration extends RFXComGenericDeviceConfiguration {
    public static final String PULSE_LABEL = "pulse";
    public static final String ON_COMMAND_ID_LABEL = "onCommandId";
    public static final String OFF_COMMAND_ID_LABEL = "offCommandId";
    public static final String OPEN_COMMAND_ID_LABEL = "openCommandId";
    public static final String CLOSED_COMMAND_ID_LABEL = "closedCommandId";
    public Integer pulse;
    public Integer onCommandId;
    public Integer offCommandId;
    public Integer openCommandId;
    public Integer closedCommandId;
}
