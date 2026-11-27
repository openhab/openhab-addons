/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.qbus.internal.handler;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.qbus.internal.QbusBridgeHandler;
import org.openhab.binding.qbus.internal.protocol.QbusCommunication;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link QbusGlobalHandler} is used in other handlers, to share the functions.
 *
 * @author Koen Schockaert - Initial Contribution
 */

@NonNullByDefault
public abstract class QbusGlobalHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(QbusGlobalHandler.class);

    public QbusGlobalHandler(Thing thing) {
        super(thing);
    }

    /**
     * Get Bridge communication
     *
     * @param type
     * @param globalId
     * @return
     */
    public @Nullable QbusCommunication getCommunication(String type, @Nullable Integer globalId) {
        QbusBridgeHandler qBridgeHandler = null;
        if (globalId != null) {
            qBridgeHandler = getBridgeHandler(type, globalId);
        }

        if (qBridgeHandler == null) {
            if (globalId == null) {
                updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, "No global ID configured for " + type + ".");
            } else {
                logger.debug("Bridge handler unavailable for Thing {}", getThing().getUID());
            }
            return null;
        }
        return qBridgeHandler.getCommunication();
    }

    /**
     * Get the Bridge handler
     *
     * @param type
     * @param globalId
     * @return
     */
    public @Nullable QbusBridgeHandler getBridgeHandler(String type, @Nullable Integer globalId) {
        Bridge qBridge = getBridge();
        if (qBridge == null) {
            logger.debug("Bridge unavailable for Thing {} (type {}, global ID {})", getThing().getUID(), type,
                    globalId);
            return null;
        }
        return (QbusBridgeHandler) qBridge.getHandler();
    }

    /**
     *
     * @param qComm
     * @param type
     * @param globalId
     */
    public void restartCommunication(QbusCommunication qComm, String type, @Nullable Integer globalId) {
        try {
            qComm.restartCommunication();
        } catch (InterruptedException e) {
            String message = e.toString();
            updateStatus(ThingStatusDetail.COMMUNICATION_ERROR, message);
        } catch (IOException e) {
            String message = e.toString();
            updateStatus(ThingStatusDetail.COMMUNICATION_ERROR, message);
        }

        QbusBridgeHandler qBridgeHandler = getBridgeHandler(type, globalId);

        if (qBridgeHandler != null && qComm.communicationActive()) {
            qBridgeHandler.bridgeOnline();
        } else {
            thingOffline(ThingStatusDetail.COMMUNICATION_ERROR, "Communication socket error");
        }
    }

    /**
     * Put thing offline
     *
     * @param message
     */
    public void thingOffline(ThingStatusDetail detail, String message) {
        updateStatus(detail, message);
    }
}
