/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.upb.handler;

import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.upb.UPBBindingConstants;

/**
 * Handles commands sent to a {@link UPBBindingConstants#THING_TYPE_DIMMER} or a
 * {@link UPBBindingConstants#THING_TYPE_SWITCH}.
 *
 * @author Chris Van Orman
 * @since 2.0.0
 */
public class DeviceHandler extends UPBBaseHandler {

    /**
     * Instantiates a new {@link DeviceHandler}.
     *
     * @param thing the thing that should be handled
     */
    public DeviceHandler(Thing thing) {
        super(thing);
    }
}
