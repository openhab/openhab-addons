/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.yamahareceiver.internal.protocol;

import org.openhab.binding.yamahareceiver.YamahaReceiverBindingConstants.Zone;
import org.openhab.binding.yamahareceiver.handler.YamahaZoneThingHandler;
import org.openhab.binding.yamahareceiver.internal.protocol.xml.DeviceInformationXML;
import org.openhab.binding.yamahareceiver.internal.protocol.xml.InputWithNavigationControlXML;
import org.openhab.binding.yamahareceiver.internal.protocol.xml.InputWithPlayControlXML;
import org.openhab.binding.yamahareceiver.internal.protocol.xml.InputWithPresetControlXML;
import org.openhab.binding.yamahareceiver.internal.protocol.xml.SystemControlXML;
import org.openhab.binding.yamahareceiver.internal.protocol.xml.XMLConnection;
import org.openhab.binding.yamahareceiver.internal.protocol.xml.ZoneAvailableInputsXML;
import org.openhab.binding.yamahareceiver.internal.protocol.xml.ZoneControlXML;
import org.openhab.binding.yamahareceiver.internal.state.DeviceInformationState;
import org.openhab.binding.yamahareceiver.internal.state.NavigationControlState;
import org.openhab.binding.yamahareceiver.internal.state.NavigationControlStateListener;
import org.openhab.binding.yamahareceiver.internal.state.SystemControlState;

/**
 * Factory to create a {@link AbstractConnection} connection object based on a feature test.
 * Also returns implementation objects for all the protocol interfaces.
 *
 * At the moment only the XML protocol is supported.
 *
 * @author David Graeff - Initial contribution
 *
 */
public class ProtocolFactory {
    /**
     * Asynchronous method to create and return a connection object. Depending
     * on the feature test it might be either a {@link XMLConnection} or a JsonConnection.
     *
     * @param host The host name
     * @param connectionStateListener
     */
    public static void createConnection(String host, ConnectionStateListener connectionStateListener) {
        connectionStateListener.connectionEstablished(new XMLConnection(host));
    }

    public static InputWithNavigationControl InputWithNavigationControl(NavigationControlState state, String inputID,
            AbstractConnection connection, NavigationControlStateListener observer) {
        if (connection instanceof XMLConnection) {
            return new InputWithNavigationControlXML(state, inputID, connection, observer);
        }
        return null;
    }

    public static SystemControl SystemControl(AbstractConnection connection, SystemControlState state) {
        if (connection instanceof XMLConnection) {
            return new SystemControlXML(connection, state);
        }
        return null;
    }

    public static InputWithPlayControl InputWithPlayControl(String currentInputID, AbstractConnection connection,
            YamahaZoneThingHandler yamahaZoneThingHandler) {
        if (connection instanceof XMLConnection) {
            return new InputWithPlayControlXML(currentInputID, connection, yamahaZoneThingHandler);
        }
        return null;
    }

    public static InputWithPresetControl InputWithPresetControl(String currentInputID, AbstractConnection connection,
            YamahaZoneThingHandler yamahaZoneThingHandler) {
        if (connection instanceof XMLConnection) {
            return new InputWithPresetControlXML(currentInputID, connection, yamahaZoneThingHandler);
        }
        return null;
    }

    public static ZoneControl ZoneControl(AbstractConnection connection, Zone zone,
            YamahaZoneThingHandler yamahaZoneThingHandler) {
        if (connection instanceof XMLConnection) {
            return new ZoneControlXML(connection, zone, yamahaZoneThingHandler);
        }
        return null;
    }

    public static ZoneAvailableInputs ZoneAvailableInputs(AbstractConnection connection, Zone zone,
            YamahaZoneThingHandler yamahaZoneThingHandler) {
        if (connection instanceof XMLConnection) {
            return new ZoneAvailableInputsXML(connection, zone, yamahaZoneThingHandler);
        }
        return null;
    }

    public static DeviceInformation DeviceInformation(AbstractConnection connection, DeviceInformationState state) {
        if (connection instanceof XMLConnection) {
            return new DeviceInformationXML(connection, state);
        }
        return null;
    }
}
