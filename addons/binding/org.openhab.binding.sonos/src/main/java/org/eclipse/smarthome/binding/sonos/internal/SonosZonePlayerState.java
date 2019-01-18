/**
 * Copyright (c) 2014,2019 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.binding.sonos.internal;

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
