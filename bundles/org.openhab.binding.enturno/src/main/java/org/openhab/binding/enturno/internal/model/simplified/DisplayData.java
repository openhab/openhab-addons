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
package org.openhab.binding.enturno.internal.model.simplified;

import java.util.List;

/**
 * {@link DisplayData} is a Plain Old Java Objects class to wrap only needed data after processing API call results.
 *
 * @author Michal Kloc - Initial contribution
 */
public class DisplayData {
    public String stopPlaceId;

    public String stopName;

    public String transportMode;

    public String lineCode;

    public String frontText;

    public List<String> departures;

    public List<String> estimatedFlags;
}
