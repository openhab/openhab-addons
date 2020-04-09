/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.smhi.internal;

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link SmhiConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Anders Alfredsson - Initial contribution
 */
public class SmhiConfiguration {
    public double latitude;
    public double longitude;
    @Nullable
    public List<Integer> hourlyForecasts;
    @Nullable
    public List<Integer> dailyForecasts;
}
