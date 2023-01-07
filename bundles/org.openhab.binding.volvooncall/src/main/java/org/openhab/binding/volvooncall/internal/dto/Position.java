/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

/**
 * The {@link Position} is responsible for storing
 * informations returned by vehicle position rest
 * answer
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class Position extends VocAnswer {
    public @NonNullByDefault({}) PositionData position;
    public @NonNullByDefault({}) PositionData calculatedPosition;
}
