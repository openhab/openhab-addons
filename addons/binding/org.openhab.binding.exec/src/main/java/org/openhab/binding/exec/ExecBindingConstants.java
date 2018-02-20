/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.exec;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link ExecBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Karel Goderis - Initial contribution
 */
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
