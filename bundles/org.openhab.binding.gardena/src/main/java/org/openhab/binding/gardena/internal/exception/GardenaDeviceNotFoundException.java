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
package org.openhab.binding.gardena.internal.exception;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Exception if a device is not found, this happens if a device is requested and the data from Gardena smart system has
 * not been loaded.
 *
 * @author Gerhard Riegler - Initial contribution
 */
@NonNullByDefault
public class GardenaDeviceNotFoundException extends GardenaException {

    private static final long serialVersionUID = 2704767320916725490L;

    public GardenaDeviceNotFoundException(String message) {
        super(message);
    }
}
