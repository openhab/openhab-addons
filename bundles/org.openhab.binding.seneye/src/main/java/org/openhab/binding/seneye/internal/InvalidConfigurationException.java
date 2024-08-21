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
package org.openhab.binding.seneye.internal;

/**
 * There was an error in the seneye configuration
 *
 * @author Niko Tanghe - Initial contribution
 */

public class InvalidConfigurationException extends Exception {
    private static final long serialVersionUID = -2894268584378662737L;

    public InvalidConfigurationException(String message) {
        super(message);
    }
}
