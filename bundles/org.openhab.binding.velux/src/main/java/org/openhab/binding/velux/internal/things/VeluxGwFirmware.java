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
package org.openhab.binding.velux.internal.things;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.velux.internal.VeluxBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <B>Velux</B> product representation.
 * <P>
 * Combined set of information describing a single Velux product.
 *
 * @author Guenther Schreiner - initial contribution.
 */
@NonNullByDefault
public class VeluxGwFirmware {
    private final Logger logger = LoggerFactory.getLogger(VeluxGwFirmware.class);

    // Class internal

    private String firmwareVersion = VeluxBindingConstants.UNKNOWN;

    // Constructor

    public VeluxGwFirmware(String firmwareVersion) {
        logger.trace("VeluxGwFirmware() created.");

        this.firmwareVersion = firmwareVersion;
    }

    public VeluxGwFirmware() {
        logger.trace("VeluxGwFirmware(dummy) created.");
    }

    // Class access methods

    public String getfirmwareVersion() {
        return this.firmwareVersion;
    }
}
