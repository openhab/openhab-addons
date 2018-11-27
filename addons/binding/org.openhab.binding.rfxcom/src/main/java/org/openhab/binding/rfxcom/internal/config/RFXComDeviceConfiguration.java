/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rfxcom.internal.config;

/**
 * Configuration class for {@link RfxcomBinding} device.
 *
 * @author Pauli Anttila - Initial contribution
 */

public class RFXComDeviceConfiguration {
    public static final String DEVICE_ID_LABEL = "deviceId";
    public static final String SUB_TYPE_LABEL = "subType";
    public static final String PULSE_LABEL = "pulse";
    public static final String ON_COMMAND_ID_LABEL = "onCommandId";
    public static final String OFF_COMMAND_ID_LABEL = "offCommandId";

    public String deviceId;
    public String subType;
    public Integer pulse;
    public Integer onCommandId;
    public Integer offCommandId;
}
