/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.samsungac.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 *
 * The {@link ChannelException} class is the Channel Exception class for the Samsung Digital Inverter
 *
 * @author Jan Grønlien - Initial contribution
 * @author Kai Kreuzer - Refactoring as preparation for openHAB contribution
 */

@NonNullByDefault
public class ChannelException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public ChannelException() {
    }

    public ChannelException(String message) {
        super(message);
    }
}
