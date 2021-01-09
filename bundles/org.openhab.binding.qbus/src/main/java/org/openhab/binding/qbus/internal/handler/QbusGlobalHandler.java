/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.qbus.internal.QbusBridgeHandler;
import org.openhab.binding.qbus.internal.protocol.QbusCommunication;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;

/**
 * The {@link QbusGlobalHandler} is used in other handlers, to share the functions.
 *
 * @author Koen Schockaert - Initial Contribution
 */

@NonNullByDefault
public abstract class QbusGlobalHandler extends BaseThingHandler {

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
    public @Nullable QbusCommunication getCommunication(String type, int globalId) {
        QbusBridgeHandler QBridgeHandler = getBridgeHandler(type, globalId);
        if (QBridgeHandler == null) {
            updateStatus(ThingStatus.UNINITIALIZED, ThingStatusDetail.BRIDGE_UNINITIALIZED,
                    "No bridge handler initialized for " + type + " with id " + globalId + ".");
            return null;
        }
        QbusCommunication QComm = QBridgeHandler.getCommunication();
        return QComm;
    }

    /**
     * Get the Bridge handler
     *
     * @param type
     * @param globalId
     * @return
     */
    public @Nullable QbusBridgeHandler getBridgeHandler(String type, int globalId) {
        Bridge QBridge = getBridge();
        if (QBridge == null) {
            updateStatus(ThingStatus.UNINITIALIZED, ThingStatusDetail.BRIDGE_UNINITIALIZED,
                    "No bridge initialized for " + type + " with ID " + globalId);
            return null;
        }
        QbusBridgeHandler QBridgeHandler = (QbusBridgeHandler) QBridge.getHandler();
        return QBridgeHandler;
    }

    /**
     *
     * @param QComm
     * @param type
     * @param globalId
     */
    public void restartCommunication(QbusCommunication QComm, String type, int globalId) {
        QComm.restartCommunication();

        if (!QComm.communicationActive()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Communication socket error");
            return;
        }

        QbusBridgeHandler QBridgeHandler = getBridgeHandler(type, globalId);
        if (QBridgeHandler != null) {
            QBridgeHandler.bridgeOnline();
        }
    }
}
