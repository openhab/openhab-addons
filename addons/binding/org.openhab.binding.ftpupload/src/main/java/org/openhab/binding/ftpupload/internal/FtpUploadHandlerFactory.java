/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ftpupload.internal;

import static org.openhab.binding.ftpupload.FtpUploadBindingConstants.THING_TYPE_IMAGERECEIVER;

import java.util.Collections;
import java.util.Dictionary;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.ftpserver.FtpServerConfigurationException;
import org.apache.ftpserver.ftplet.FtpException;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.ftpupload.handler.FtpUploadHandler;
import org.openhab.binding.ftpupload.internal.ftp.FtpServer;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FtpUploadHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Pauli Anttila - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, immediate = true, configurationPid = "binding.ftpupload")
public class FtpUploadHandlerFactory extends BaseThingHandlerFactory {
    private final Logger logger = LoggerFactory.getLogger(FtpUploadHandlerFactory.class);

    private final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_IMAGERECEIVER);

    private final int DEFAULT_PORT = 2121;
    private final int DEFAULT_IDLE_TIMEOUT = 60;

    private FtpServer ftpServer;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_IMAGERECEIVER)) {
            if (ftpServer.getStartUpErrorReason() != null) {
                thing.setStatusInfo(new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        ftpServer.getStartUpErrorReason()));
            }
            return new FtpUploadHandler(thing, ftpServer);
        }

        return null;
    }

    @Override
    protected synchronized void activate(ComponentContext componentContext) {
        super.activate(componentContext);
        ftpServer = new FtpServer();
        modified(componentContext);
    }

    @Override
    protected synchronized void deactivate(ComponentContext componentContext) {
        stopFtpServer();
        ftpServer = null;
        super.deactivate(componentContext);
    }

    protected synchronized void modified(ComponentContext componentContext) {
        stopFtpServer();
        Dictionary<String, Object> properties = componentContext.getProperties();

        int port = DEFAULT_PORT;
        int idleTimeout = DEFAULT_IDLE_TIMEOUT;

        if (properties.get("port") != null) {
            String strPort = properties.get("port").toString();
            if (StringUtils.isNotEmpty(strPort)) {
                try {
                    port = Integer.valueOf(strPort);
                } catch (NumberFormatException e) {
                    logger.warn("Invalid port number '{}', using default port {}", strPort, port);
                }
            }
        }

        if (properties.get("idleTimeout") != null) {
            String strIdleTimeout = properties.get("idleTimeout").toString();
            if (StringUtils.isNotEmpty(strIdleTimeout)) {
                try {
                    idleTimeout = Integer.valueOf(strIdleTimeout);
                } catch (NumberFormatException e) {
                    logger.warn("Invalid idle timeout '{}', using default timeout {}", strIdleTimeout, idleTimeout);
                }
            }
        }

        try {
            logger.info("Starting FTP server, port={}, idleTimeout={}", port, idleTimeout);
            ftpServer.startServer(port, idleTimeout);
        } catch (FtpException | FtpServerConfigurationException e) {
            logger.warn("FTP server starting failed, reason: {}", e.getMessage());
        }
    }

    private void stopFtpServer() {
        logger.info("Stopping FTP server");
        ftpServer.stopServer();
    }
}
