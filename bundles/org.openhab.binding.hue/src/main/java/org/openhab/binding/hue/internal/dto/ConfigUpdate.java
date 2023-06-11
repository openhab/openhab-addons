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
package org.openhab.binding.hue.internal.dto;

import static java.util.stream.Collectors.joining;

import java.util.ArrayList;

/**
 * Collection of updates
 *
 * @author Q42 - Initial contribution
 * @author Denis Dudnik - moved Jue library source code inside the smarthome Hue binding, minor code cleanup
 * @author Samuel Leisering - Added support for sensor API
 * @author Christoph Weitkamp - Added support for sensor API
 */
public class ConfigUpdate {

    public final ArrayList<Command> commands = new ArrayList<>();

    public ConfigUpdate() {
        super();
    }

    public boolean isEmpty() {
        return commands.isEmpty();
    }

    public String toJson() {
        return commands.stream().map(c -> c.toJson()).collect(joining(",", "{", "}"));
    }

    /**
     * Returns the message delay recommended by Philips
     * Regarding to this article: https://developers.meethue.com/documentation/hue-system-performance
     */
    public long getMessageDelay() {
        return commands.size() * 40L;
    }
}
