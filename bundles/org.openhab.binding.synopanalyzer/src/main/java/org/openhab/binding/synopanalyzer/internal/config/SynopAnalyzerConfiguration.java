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
package org.openhab.binding.synopanalyzer.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link SynopAnalyzerConfiguration} holds configuration informations needed for the Synop thing
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class SynopAnalyzerConfiguration {
    public static final String STATION_ID = "stationId";
    public long refreshInterval = 60;
    public int stationId;
}
