/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.yamahareceiver.internal.protocol;

import org.openhab.binding.yamahareceiver.YamahaReceiverBindingConstants.Feature;
import org.openhab.binding.yamahareceiver.YamahaReceiverBindingConstants.Zone;
import org.openhab.binding.yamahareceiver.internal.config.YamahaBridgeConfiguration;
import org.openhab.binding.yamahareceiver.internal.config.YamahaZoneConfiguration;
import org.openhab.binding.yamahareceiver.internal.protocol.xml.*;
import org.openhab.binding.yamahareceiver.internal.state.*;

import java.util.function.Supplier;

/**
 * Factory to create a {@link AbstractConnection} connection object based on a feature test.
 * Also returns implementation objects for all the protocol interfaces.
 *
 * At the moment only the XML protocol is supported.
 *
 * @author David Graeff - Initial contribution
 * @author Tomasz Maruszak - Input mapping fix
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

    public static InputWithNavigationControl InputWithNavigationControl(AbstractConnection connection, NavigationControlState state,
            String inputID, NavigationControlStateListener observer) {
        if (connection instanceof XMLConnection) {
            return new InputWithNavigationControlXML(state, inputID, connection, observer);
        }
        return null;
    }

    public static SystemControl SystemControl(AbstractConnection connection, SystemControlStateListener listener) {
        if (connection instanceof XMLConnection) {
            return new SystemControlXML(connection, listener);
        }
        return null;
    }

    public static InputWithPlayControl InputWithPlayControl(AbstractConnection connection, String currentInputID,
                                                            PlayInfoStateListener listener, YamahaBridgeConfiguration settings) {
        if (connection instanceof XMLConnection) {
            return new InputWithPlayControlXML(currentInputID, connection, listener, settings);
        }
        return null;
    }

    public static InputWithPresetControl InputWithPresetControl(AbstractConnection connection, String currentInputID,
            PresetInfoStateListener listener) {
        if (connection instanceof XMLConnection) {
            return new InputWithPresetControlXML(currentInputID, connection, listener);
        }
        return null;
    }

    public static InputWithDabBandControl InputWithDabBandControl(String currentInputID, AbstractConnection connection,
                                                                  DabBandStateListener observerForBand,
                                                                  PresetInfoStateListener observerForPreset,
                                                                  PlayInfoStateListener observerForPlayInfo) {
        if (connection instanceof XMLConnection) {
            return new InputWithDabControlXML(currentInputID, connection, observerForBand, observerForPreset, observerForPlayInfo);
        }
        return null;
    }

    public static ZoneControl ZoneControl(AbstractConnection connection,
                                          YamahaZoneConfiguration zoneSettings,
                                          ZoneControlStateListener listener,
                                          Supplier<InputConverter> inputConverterSupplier,
                                          DeviceInformationState deviceInformationState) {
        if (connection instanceof XMLConnection) {
            if (isZoneB(zoneSettings.getZone(), deviceInformationState)) {
                return new ZoneBControlXML(connection, zoneSettings, listener, deviceInformationState, inputConverterSupplier);
            }
            return new ZoneControlXML(connection, zoneSettings.getZone(), zoneSettings, listener, deviceInformationState, inputConverterSupplier);
        }
        return null;
    }

    public static ZoneAvailableInputs ZoneAvailableInputs(AbstractConnection connection,
                                                          YamahaZoneConfiguration zoneSettings,
                                                          AvailableInputStateListener listener,
                                                          Supplier<InputConverter> inputConverterSupplier,
                                                          DeviceInformationState deviceInformationState) {
        if (connection instanceof XMLConnection) {
            if (isZoneB(zoneSettings.getZone(), deviceInformationState)) {
                return new ZoneBAvailableInputsXML(connection, listener, inputConverterSupplier);
            }
            return new ZoneAvailableInputsXML(connection, zoneSettings.getZone(), listener, inputConverterSupplier);
        }
        return null;
    }

    /**
     * Checks if the specified Zone_2 should be emulated using Zone_B feature.
     *
     * @param zone
     * @param deviceInformationState
     * @return
     */
    private static boolean isZoneB(Zone zone, DeviceInformationState deviceInformationState) {
        return Zone.Zone_2.equals(zone) && deviceInformationState.features.contains(Feature.ZONE_B);
    }

    public static DeviceInformation DeviceInformation(AbstractConnection connection, DeviceInformationState state) {
        if (connection instanceof XMLConnection) {
            return new DeviceInformationXML(connection, state);
        }
        return null;
    }

    public static InputConverter InputConverter(AbstractConnection connection, String setting) {
        if (connection instanceof XMLConnection) {
            return new InputConverterXML(connection, setting);
        }
        return null;
    }
}
