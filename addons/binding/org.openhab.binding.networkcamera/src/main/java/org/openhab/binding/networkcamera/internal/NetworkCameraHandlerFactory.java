/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.networkcamera.internal;

import static org.openhab.binding.networkcamera.NetworkCameraBindingConstants.THING_TYPE_SAMPLE;

import java.util.Collections;
import java.util.Dictionary;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.networkcamera.handler.NetworkCameraHandler;
import org.openhab.binding.networkcamera.internal.ftp.FtpServer;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NetworkCameraHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class NetworkCameraHandlerFactory extends BaseThingHandlerFactory {

    private Logger logger = LoggerFactory.getLogger(NetworkCameraHandlerFactory.class);

    private final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_SAMPLE);

    private final int DEFAULT_PORT = 2121;
    private final int DEFAULT_IDLE_TIMEOUT = 60;

    private FtpServer ftpServer = new FtpServer();

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_SAMPLE)) {
            return new NetworkCameraHandler(thing, ftpServer);
        }

        return null;
    }

    @Override
    protected synchronized void activate(ComponentContext componentContext) {
        super.activate(componentContext);
        modified(componentContext);
    }

    @Override
    protected synchronized void deactivate(ComponentContext componentContext) {
        ftpServer.stopServer();
        super.deactivate(componentContext);
    }

    protected synchronized void modified(ComponentContext componentContext) {
        ftpServer.stopServer();

        Dictionary<String, Object> properties = componentContext.getProperties();

        int port = DEFAULT_PORT;
        int idleTimeout = DEFAULT_IDLE_TIMEOUT;

        String strPort = (String) properties.get("port");
        String strIdleTimeout = (String) componentContext.getProperties().get("idleTimeout");

        if (StringUtils.isNotEmpty(strPort)) {
            port = Integer.valueOf(strPort);
        }

        if (StringUtils.isNotEmpty(strPort)) {
            idleTimeout = Integer.valueOf(strIdleTimeout);
        }

        try {
            ftpServer.startServer(port, idleTimeout);
        } catch (Exception e) {
            logger.error("FTP server starting failed, reason: " + e.getMessage());
        }
    }
}
