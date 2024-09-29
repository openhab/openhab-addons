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
package org.openhab.binding.sncf.internal.dto;

import java.util.List;

/**
 * The {@link PlacesNearby} holds a list or nearby places.
 *
 * @author Gaël L'hopital - Initial contribution
 */
public class PlacesNearby extends SncfAnswer {
    public List<PlaceNearby> placesNearby;
}
