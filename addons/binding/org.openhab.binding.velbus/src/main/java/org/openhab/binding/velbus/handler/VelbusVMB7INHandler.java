/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.velbus.handler;

import static org.openhab.binding.velbus.VelbusBindingConstants.THING_TYPE_VMB7IN;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link VelbusVMB7INHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Cedric Boon - Initial contribution
 */
public class VelbusVMB7INHandler extends VelbusSensorHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = new HashSet<>(Arrays.asList(THING_TYPE_VMB7IN));

    public VelbusVMB7INHandler(Thing thing) {
        super(thing, 8, 0);
    }
}
