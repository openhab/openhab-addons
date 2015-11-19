/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
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
    public final static ThingTypeUID THING_COMMAND = new ThingTypeUID(BINDING_ID, "command");

    // List of all Channel ids
    public final static String OUTPUT = "output";
    public final static String EXIT = "exit";
    public final static String RUNNING = "running";
    public final static String EXECUTE = "execute";
    public final static String LAST_EXECUTION = "lastexecution";

}
