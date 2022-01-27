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
 * The {@link CommunityChargeCycleEntry} Data Transfer Object
 *
 * @author Bernd Weymann - Initial contribution
 */
public class CommunityChargeCycleEntry {
    public float communityAverage;// ": 194.21,
    public float communityHigh;// ": 270,
    public float userAverage;// ": 57.3,
    public float userHigh;// ": 185.48,
    public float userCurrentChargeCycle;// ": 68
}
