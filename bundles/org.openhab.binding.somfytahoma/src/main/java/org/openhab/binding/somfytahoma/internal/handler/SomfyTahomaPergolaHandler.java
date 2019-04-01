/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.somfytahoma.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.Thing;

import static org.openhab.binding.somfytahoma.internal.SomfyTahomaBindingConstants.*;

import java.util.HashMap;

/**
 * The {@link SomfyTahomaPergolaHandler} is responsible for handling commands,
 * which are sent to one of the channels of the pergola thing.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class SomfyTahomaPergolaHandler extends SomfyTahomaAwningHandler {

    public SomfyTahomaPergolaHandler(Thing thing) {
        super(thing);
        stateNames = new HashMap<String, String>() {{
            put(CONTROL, "core:TargetClosureState");
        }};
    }
}
