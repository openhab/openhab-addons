/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.myuplink.internal.discovery;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.myuplink.internal.MyUplinkBindingConstants;
import org.openhab.binding.myuplink.internal.handler.MyUplinkAccountHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * this class will handle discovery of wallboxes and circuits within the site configured.
 *
 * @author Alexander Friese - initial contribution
 *
 */
@NonNullByDefault
public class MyUplinkDiscoveryService extends AbstractDiscoveryService implements ThingHandlerService {

    private final Logger logger = LoggerFactory.getLogger(MyUplinkDiscoveryService.class);
    private @NonNullByDefault({}) MyUplinkAccountHandler bridgeHandler;

    public MyUplinkDiscoveryService() throws IllegalArgumentException {
        super(MyUplinkBindingConstants.SUPPORTED_THING_TYPES_UIDS, 300, false);
    }

    @Override
    protected void startScan() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'startScan'");
    }

    @Override
    public void setThingHandler(ThingHandler handler) {
        if (handler instanceof MyUplinkAccountHandler accountHandler) {
            this.bridgeHandler = accountHandler;
            this.bridgeHandler.setDiscoveryService(this);
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return bridgeHandler;
    }

    // method is defined in both implemented interface and inherited class, thus we must define a behaviour here.
    @Override
    public void deactivate() {
        super.deactivate();
    }
}
