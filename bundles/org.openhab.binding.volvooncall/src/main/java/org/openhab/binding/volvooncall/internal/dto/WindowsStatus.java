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
 * The {@link WindowsStatus} is responsible for storing
 * Windows Status informations returned by vehicule status rest answer
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class WindowsStatus {
    public @NonNullByDefault({}) OpenClosedType frontLeftWindowOpen;
    public @NonNullByDefault({}) OpenClosedType frontRightWindowOpen;
    public @NonNullByDefault({}) OpenClosedType rearLeftWindowOpen;
    public @NonNullByDefault({}) OpenClosedType rearRightWindowOpen;
    /*
     * Currently unused in the binding, maybe interesting in the future
     * private ZonedDateTime timestamp;
     */
}
