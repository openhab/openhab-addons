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
package org.openhab.binding.pjlinkdevice.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Exception thrown whenever the thing configuration is invalid
 *
 * @author Nils Schnabel - Initial contribution
 */
@NonNullByDefault
public class ConfigurationException extends Exception {
    private static final long serialVersionUID = -3319800607314286998L;

    public ConfigurationException(String string) {
        super(string);
    }

    public ConfigurationException(Throwable cause) {
        super(cause);
    }
}
