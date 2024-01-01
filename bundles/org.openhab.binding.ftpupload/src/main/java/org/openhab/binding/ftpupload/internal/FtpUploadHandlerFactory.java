/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.ftpupload.internal;

import static org.openhab.binding.ftpupload.internal.FtpUploadBindingConstants.THING_TYPE_IMAGERECEIVER;

import java.util.Dictionary;
import java.util.Set;

import org.apache.ftpserver.DataConnectionConfigurationFactory;
import org.apache.ftpserver.FtpServerConfigurationException;
import org.apache.ftpserver.ftplet.FtpException;
import org.openhab.binding.ftpupload.internal.ftp.FtpServer;
import org.openhab.binding.ftpupload.internal.handler.FtpUploadHandler;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
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
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.ftpupload")
public class FtpUploadHandlerFactory extends BaseThingHandlerFactory {
    private final Logger logger = LoggerFactory.getLogger(FtpUploadHandlerFactory.class);

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_IMAGERECEIVER);

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
        DataConnectionConfigurationFactory dataConnectionConfigurationFactory = new DataConnectionConfigurationFactory();

        int port = DEFAULT_PORT;
        int idleTimeout = DEFAULT_IDLE_TIMEOUT;

        if (properties.get("port") != null) {
            String strPort = properties.get("port").toString();
            if (!strPort.isEmpty()) {
                try {
                    port = Integer.valueOf(strPort);
                } catch (NumberFormatException e) {
                    logger.warn("Invalid port number '{}', using default port {}", strPort, port);
                }
            }
        }

        if (properties.get("idleTimeout") != null) {
            String strIdleTimeout = properties.get("idleTimeout").toString();
            if (!strIdleTimeout.isEmpty()) {
                try {
                    idleTimeout = Integer.valueOf(strIdleTimeout);
                } catch (NumberFormatException e) {
                    logger.warn("Invalid idle timeout '{}', using default timeout {}", strIdleTimeout, idleTimeout);
                }
            }
        }

        if (properties.get("passivePorts") != null) {
            String strPassivePorts = properties.get("passivePorts").toString();
            if (!strPassivePorts.isEmpty()) {
                try {
                    dataConnectionConfigurationFactory.setPassivePorts(strPassivePorts);
                } catch (IllegalArgumentException e) {
                    logger.warn("Invalid passive ports '{}' ({})", strPassivePorts, e.getMessage());
                }
            }
        }

        try {
            logger.debug("Starting FTP server, port={}, idleTimeout={}", port, idleTimeout);
            ftpServer.startServer(port, idleTimeout,
                    dataConnectionConfigurationFactory.createDataConnectionConfiguration());
        } catch (FtpException | FtpServerConfigurationException e) {
            logger.warn("FTP server starting failed, reason: {}", e.getMessage());
        }
    }

    private void stopFtpServer() {
        logger.debug("Stopping FTP server");
        ftpServer.stopServer();
    }
}
