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
package org.openhab.binding.airq.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Exception for handling an empty response.
 *
 * @author Fabian Wolter - Initial contribution
 */
@NonNullByDefault
public class AirqEmptyResonseException extends AirqException {
    private static final long serialVersionUID = 1423144673651821622L;

    public AirqEmptyResonseException() {
        super("Device sent an empty response");
    }
}
