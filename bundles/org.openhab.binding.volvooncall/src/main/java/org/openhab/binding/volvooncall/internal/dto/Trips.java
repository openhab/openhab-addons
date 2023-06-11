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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link Trips} is responsible for storing
 * trip informations returned by trip rest answer
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class Trips extends VocAnswer {
    public List<Trip> trips = new ArrayList<>();
}
