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
package org.openhab.binding.livisismarthome.internal.client.exception;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Thrown, if the LIVISI SmartHome controller (SHC) is offline.
 *
 * @author Oliver Kuhl - Initial contribution
 *
 */
@NonNullByDefault
public class ControllerOfflineException extends IOException {

    private static final long serialVersionUID = 2851756294511651529L;

    public ControllerOfflineException(String message) {
        super(message);
    }
}
