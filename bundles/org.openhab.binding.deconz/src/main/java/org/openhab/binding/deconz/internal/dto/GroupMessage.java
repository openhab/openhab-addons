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

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.deconz.internal.types.GroupType;

/**
 * The REST interface and websocket connection are using the same fields.
 * The REST data contains more descriptive info like the manufacturer and name.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class GroupMessage extends DeconzBaseMessage {
    public @Nullable GroupAction action;
    public List<String> devicemembership = List.of();
    public @Nullable Boolean hidden;
    public List<String> lights = List.of();
    public List<String> lightsequence = List.of();
    public List<String> multideviceids = List.of();
    public List<Scene> scenes = List.of();
    public @Nullable GroupState state;
    public @Nullable GroupType type;

    @Override
    public String toString() {
        return "GroupMessage{" + "e='" + e + '\'' + ", r=" + r + ", t='" + t + '\'' + ", id='" + id + '\''
                + ", manufacturername='" + manufacturername + '\'' + ", modelid='" + modelid + '\'' + ", name='" + name
                + '\'' + ", swversion='" + swversion + '\'' + ", ep='" + ep + '\'' + ", lastseen='" + lastseen + '\''
                + ", uniqueid='" + uniqueid + '\'' + ", action=" + action + ", devicemembership=" + devicemembership
                + ", hidden=" + hidden + ", lights=" + lights + ", lightsequence=" + lightsequence + ", multideviceids="
                + multideviceids + ", scenes=" + scenes + ", state=" + state + ", type=" + type + '}';
    }
}
