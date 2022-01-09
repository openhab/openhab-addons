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
package org.openhab.binding.bmwconnecteddrive.internal.dto.statistics;

/**
 * The {@link CommunityEletricDistanceEntry} Data Transfer Object
 *
 * @author Bernd Weymann - Initial contribution
 */
public class CommunityEletricDistanceEntry {
    public float communityLow;// ": 19,
    public float communityAverage;// ": 40850.56,
    public float communityHigh;// ": 193006,
    public float userTotal;// ": 16629.4
}
