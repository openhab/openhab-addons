/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import org.openhab.binding.nikobus.internal.NikobusBindingConstants;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;

/**
 * The {@link NikobusBaseThingHandler} class defines utility logic to be consumed by Nikobus thing(s).
 *
 * @author Boris Krivonog - Initial contribution
 */
@NonNullByDefault
abstract class NikobusBaseThingHandler extends BaseThingHandler {
    private @Nullable String address;

    protected NikobusBaseThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        address = (String) getConfig().get(NikobusBindingConstants.CONFIG_ADDRESS);
        if (address == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, "Address must be set!");
            return;
        }

        updateStatus(ThingStatus.UNKNOWN);
    }

    protected @Nullable NikobusPcLinkHandler getPcLink() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            return (NikobusPcLinkHandler) bridge.getHandler();
        }
        return null;
    }

    protected String getAddress() {
        String address = this.address;
        if (address == null) {
            throw new IllegalStateException();
        }
        return address;
    }
}
