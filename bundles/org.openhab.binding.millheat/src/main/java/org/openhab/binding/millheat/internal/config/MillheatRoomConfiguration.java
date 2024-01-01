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
package org.openhab.binding.millheat.internal.config;

/**
 * The {@link MillheatRoomConfiguration} class contains room thing configuration parameters.
 *
 * @author Arne Seime - Initial contribution
 */
public class MillheatRoomConfiguration {
    /*
     * Room ID
     */
    public Long roomId;

    @Override
    public String toString() {
        return "MillheatRoomConfiguration [roomId=" + roomId + "]";
    }
}
