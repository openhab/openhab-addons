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
package org.openhab.binding.sonos.internal;

/**
 * The {@link SonosZoneGroup} is data structure to describe
 * state of a Zone Player
 * 
 * @author Karel Goderis - Initial contribution
 */
public class SonosZonePlayerState {

    public String transportState;
    public String volume;
    public String relTime;
    public SonosEntry entry;
    public long track;
}
