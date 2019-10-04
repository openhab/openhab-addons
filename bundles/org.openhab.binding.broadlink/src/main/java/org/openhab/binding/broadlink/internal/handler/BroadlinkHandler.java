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
package org.openhab.binding.broadlink.internal.handler;

import com.github.mob41.blapi.BLDevice;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.common.ThreadPoolManager;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.openhab.binding.broadlink.internal.BroadlinkBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

/**
 * The {@link BroadlinkHandler} is the device handler class for a broadlink device.
 *
 * @author Florian Mueller - Initial contribution
 */
public abstract class BroadlinkHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(BroadlinkHandler.class);

    protected BLDevice blDevice;
    protected final ScheduledExecutorService scheduler = ThreadPoolManager.getScheduledPool("thingHandler");
    protected  @Nullable ScheduledFuture<?> scanJob;
    protected String host;
    protected String mac;
    protected String deviceDescription;

    /**
     * Creates a new instance of this class for the {@link Thing}.
     *
     * @param thing the thing that should be handled, not null
     */
    public BroadlinkHandler(Thing thing) {
        super(thing);
        deviceDescription = thing.getProperties().get(BroadlinkBindingConstants.DESCRIPTION);
        host = (String) thing.getConfiguration().get(BroadlinkBindingConstants.HOST);
        mac = (String) thing.getConfiguration().get(BroadlinkBindingConstants.MAC);
        logger.debug("Config properties: {}", thing.getConfiguration());
        logger.debug("host: {}, mac: {}, deviceDescription: {}", host, mac, deviceDescription);

    }

    protected void authenticate(){
        try {
            if (blDevice.auth()) {
                updateStatus(ThingStatus.ONLINE);
            }
        } catch (IOException e) {
            logger.error("Error while authenticating broadlink device {}", thing.getLabel(), e);
            updateStatus(ThingStatus.INITIALIZING);
        }
    }



    @Override
    public void dispose() {
        logger.debug("Disposing thing {}", getThing().getUID());
        scanJob.cancel(true);
        logger.debug("Thing {} disposed", getThing().getUID());
    }

}
