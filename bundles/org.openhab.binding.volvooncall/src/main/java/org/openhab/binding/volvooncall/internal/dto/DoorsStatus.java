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
package org.openhab.binding.volvooncall.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.OpenClosedType;

/**
 * The {@link DoorsStatus} is responsible for storing
 * informations returned by vehicle status rest answer
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class DoorsStatus {
    public @NonNullByDefault({}) OpenClosedType tailgateOpen;
    public @NonNullByDefault({}) OpenClosedType rearRightDoorOpen;
    public @NonNullByDefault({}) OpenClosedType rearLeftDoorOpen;
    public @NonNullByDefault({}) OpenClosedType frontRightDoorOpen;
    public @NonNullByDefault({}) OpenClosedType frontLeftDoorOpen;
    public @NonNullByDefault({}) OpenClosedType hoodOpen;
    /*
     * Currently unused in the binding, maybe interesting in the future
     * private ZonedDateTime timestamp;
     */
}
