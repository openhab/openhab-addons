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
package org.openhab.binding.exec.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link ExecBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Karel Goderis - Initial contribution
 */
@NonNullByDefault
public class ExecBindingConstants {
    public static final String BINDING_ID = "exec";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_COMMAND = new ThingTypeUID(BINDING_ID, "command");

    // List of all Channel ids
    public static final String OUTPUT = "output";
    public static final String INPUT = "input";
    public static final String EXIT = "exit";
    public static final String RUN = "run";
    public static final String LAST_EXECUTION = "lastexecution";
}
