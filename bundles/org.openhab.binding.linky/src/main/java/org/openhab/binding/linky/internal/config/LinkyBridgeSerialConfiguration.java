/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.linky.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link LinkyBridgeSerialConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Nicolas SIBERIL - Initial contribution
 * @author Laurent Arnal - Refactor to integrate into Linky Binding
 */
@NonNullByDefault
public class LinkyBridgeSerialConfiguration extends LinkyBridgeConfiguration {

    public String serialport = "";
    public String ticMode = "";
    public boolean verifyChecksum = true;
    public boolean autoRepairInvalidADPSgroupLine = true;

    @Override
    public boolean seemsValid() {
        return !serialport.isBlank() && !ticMode.isBlank();
    }
}
