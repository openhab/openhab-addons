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
package org.openhab.binding.deconz.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.deconz.internal.types.ResourceType;

/**
 * The REST interface and websocket connection are using the same fields.
 * The REST data contains more descriptive info like the manufacturer and name.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class DeconzBaseMessage {
    // For websocket change events
    public String e = ""; // "changed", "scene-called"
    public ResourceType r = ResourceType.UNKNOWN; // "sensors"
    public String t = ""; // "event"
    public String id = ""; // "3"

    // for scene-recall
    public String gid = "";
    public String scid = "";

    // for rest API
    public String manufacturername = "";
    public String modelid = "";
    public String name = "";
    public String swversion = "";

    /** the API endpoint **/
    public String ep = "";

    /** device last seen */
    public @Nullable String lastseen;

    // websocket and rest api
    public String uniqueid = ""; // "00:0b:57:ff:fe:94:6b:dd-01-1000"
}
