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
package org.openhab.io.hueemulation.internal.dto.changerequest;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Commands are http request data (method+url+body) used by schedules and rules.
 * <p>
 * Note: Rules use shorter address variants without the "/api/username" parts, which
 * makes them no valid relative urls!
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class HueCommand {
    public String address = "";
    public String method = "";
    public String body = "";

    public boolean isValid() {
        return !address.isEmpty() && !method.isEmpty() && !body.isEmpty();
    }

    public HueCommand() {
    }

    public HueCommand(String address, String method, String body) {
        this.address = address;
        this.method = method;
        this.body = body;
    }
}
