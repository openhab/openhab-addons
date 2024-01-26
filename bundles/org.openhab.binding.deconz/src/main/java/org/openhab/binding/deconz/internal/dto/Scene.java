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
import org.openhab.core.types.CommandOption;

/**
 * The {@link Scene} is send by the websocket connection as well as the Rest API.
 * It is part of a {@link GroupMessage}.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class Scene {
    public String id = "";
    public String name = "";

    public CommandOption toCommandOption() {
        return new CommandOption(name, name);
    }

    @Override
    public String toString() {
        return "Scene{" + "id='" + id + '\'' + ", name='" + name + '\'' + '}';
    }
}
