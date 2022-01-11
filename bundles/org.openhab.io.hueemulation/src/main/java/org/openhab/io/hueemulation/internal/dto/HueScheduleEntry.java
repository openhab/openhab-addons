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
package org.openhab.io.hueemulation.internal.dto;

import org.openhab.io.hueemulation.internal.dto.changerequest.HueChangeScheduleEntry;
import org.openhab.io.hueemulation.internal.dto.changerequest.HueCommand;

/**
 * Hue API scan result object.
 * Enpoint: /api/{username}/lights/new
 *
 * @author David Graeff - Initial contribution
 */
public class HueScheduleEntry extends HueChangeScheduleEntry {
    /**
     * Assign default values to all fields that are otherwise nullable by the {@link HueChangeScheduleEntry}.
     */
    public HueScheduleEntry() {
        command = new HueCommand();
        name = "";
        description = "";
        localtime = "";
        status = "disabled";
        recycle = false;
        autodelete = true;
    }
}
