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
package org.openhab.binding.nikobus.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;

/**
 * The {@link NikobusBaseThingHandler} class defines utility logic to be consumed by Nikobus thing(s).
 *
 * @author Boris Krivonog - Initial contribution
 */
@NonNullByDefault
abstract class NikobusBaseThingHandler extends BaseThingHandler {

    protected NikobusBaseThingHandler(Thing thing) {
        super(thing);
    }

    protected @Nullable NikobusPcLinkHandler getPcLink() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            return (NikobusPcLinkHandler) bridge.getHandler();
        }
        return null;
    }

    protected String getAddress() {
        return thing.getUID().getId();
    }
}
