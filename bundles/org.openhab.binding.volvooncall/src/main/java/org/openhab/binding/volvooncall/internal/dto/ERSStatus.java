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
package org.openhab.binding.volvooncall.internal.dto;

import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ERSStatus} is responsible for storing
 * ERS Status informations returned by vehicule status rest answer
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class ERSStatus {
    public @NonNullByDefault({}) String status;
    public @NonNullByDefault({}) ZonedDateTime timestamp;
    public @NonNullByDefault({}) String engineStartWarning;
    public @NonNullByDefault({}) ZonedDateTime engineStartWarningTimestamp;
}
