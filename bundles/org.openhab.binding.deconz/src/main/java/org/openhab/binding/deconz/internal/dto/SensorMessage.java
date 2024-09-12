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
package org.openhab.binding.deconz.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The REST interface and websocket connection are using the same fields.
 * The REST data contains more descriptive info like the manufacturer and name.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class SensorMessage extends DeconzBaseMessage {
    public String type = "";

    public @Nullable SensorConfig config;
    public @Nullable SensorState state;

    @Override
    public String toString() {
        return "SensorMessage{" + "type='" + type + '\'' + ", config=" + config + ", state=" + state + ", e='" + e
                + '\'' + ", r='" + r + '\'' + ", t='" + t + '\'' + ", id='" + id + '\'' + ", manufacturername='"
                + manufacturername + '\'' + ", modelid='" + modelid + '\'' + ", name='" + name + '\'' + ", swversion='"
                + swversion + '\'' + ", ep='" + ep + '\'' + ", lastseen='" + lastseen + '\'' + ", uniqueid='" + uniqueid
                + '\'' + '}';
    }
}
