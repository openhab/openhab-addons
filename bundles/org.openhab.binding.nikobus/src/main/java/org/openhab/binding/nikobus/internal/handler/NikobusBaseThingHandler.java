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
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.openhab.binding.nikobus.internal.NikobusBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NikobusBaseThingHandler} class defines utility logic to be consumed by Nikobus thing(s).
 *
 * @author Boris Krivonog - Initial contribution
 * @author Wouter Denayer - support for module addresses as seen in the Niko PC tool
 */
@NonNullByDefault
abstract class NikobusBaseThingHandler extends BaseThingHandler {
    @Nullable
    protected String address;

    private final Logger logger = LoggerFactory.getLogger(NikobusBaseThingHandler.class);

    protected NikobusBaseThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        address = (String) getConfig().get(NikobusBindingConstants.CONFIG_ADDRESS);
        if (address == null) {
            String addressPC = (String) getConfig().get(NikobusBindingConstants.CONFIG_ADDRESS_PC);
            if (addressPC != null) {
                address = addressPC.substring(2, 4) + addressPC.substring(0, 2);
                logger.debug("address PC, address= '{}'", address);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                        "Address must be set!");
                return;
            }
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
