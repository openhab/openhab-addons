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
package org.openhab.binding.innogysmarthome.internal.client.exception;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Thrown, if the innogy SmartHome controller (SHC) is offline.
 *
 * @author Oliver Kuhl - Initial contribution
 *
 */
@NonNullByDefault
public class ControllerOfflineException extends IOException {

    private static final long serialVersionUID = 1L;

    public ControllerOfflineException() {
    }

    public ControllerOfflineException(String message) {
        super(message);
    }
}
