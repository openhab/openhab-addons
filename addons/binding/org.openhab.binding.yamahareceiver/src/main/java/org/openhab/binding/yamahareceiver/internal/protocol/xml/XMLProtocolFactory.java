/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.yamahareceiver.internal.protocol.xml;

import org.openhab.binding.yamahareceiver.YamahaReceiverBindingConstants;
import org.openhab.binding.yamahareceiver.internal.config.YamahaBridgeConfig;
import org.openhab.binding.yamahareceiver.internal.config.YamahaZoneConfig;
import org.openhab.binding.yamahareceiver.internal.protocol.*;
import org.openhab.binding.yamahareceiver.internal.state.*;

import java.util.function.Supplier;

/**
 * Implementation of {@link ProtocolFactory} for XML protocol.
 *
 * @author Tomasz Maruszak - Initial contribution.
 */
public class XMLProtocolFactory implements ProtocolFactory {
    public void createConnection(String host, ConnectionStateListener connectionStateListener) {
        connectionStateListener.connectionEstablished(new XMLConnection(host));
    }

    public SystemControl SystemControl(AbstractConnection connection,
                                       SystemControlStateListener listener,
                                       DeviceInformationState deviceInformationState) {

        return new SystemControlXML(connection, listener, deviceInformationState);
    }

    public InputWithPlayControl InputWithPlayControl(AbstractConnection connection,
                                                     String currentInputID,
                                                     PlayInfoStateListener listener,
                                                     YamahaBridgeConfig bridgeConfig,
                                                     DeviceInformationState deviceInformationState) {

        return new InputWithPlayControlXML(currentInputID, connection, listener, bridgeConfig, deviceInformationState);
    }

    public InputWithPresetControl InputWithPresetControl(AbstractConnection connection,
                                                         String currentInputID,
                                                         PresetInfoStateListener listener,
                                                         DeviceInformationState deviceInformationState) {

        return new InputWithPresetControlXML(currentInputID, connection, listener, deviceInformationState);
    }

    public InputWithTunerBandControl InputWithDabBandControl(String currentInputID,
                                                             AbstractConnection connection,
                                                             DabBandStateListener observerForBand,
                                                             PresetInfoStateListener observerForPreset,
                                                             PlayInfoStateListener observerForPlayInfo,
                                                             DeviceInformationState deviceInformationState) {

        return new InputWithTunerDABControlXML(currentInputID, connection, observerForBand, observerForPreset, observerForPlayInfo, deviceInformationState);
    }

    public InputWithNavigationControl InputWithNavigationControl(AbstractConnection connection,
                                                                 NavigationControlState state,
                                                                 String inputID,
                                                                 NavigationControlStateListener observer,
                                                                 DeviceInformationState deviceInformationState) {

        return new InputWithNavigationControlXML(state, inputID, connection, observer, deviceInformationState);
    }

    public ZoneControl ZoneControl(AbstractConnection connection,
                                   YamahaZoneConfig zoneSettings,
                                   ZoneControlStateListener listener,
                                   Supplier<InputConverter> inputConverterSupplier,
                                   DeviceInformationState deviceInformationState) {

        if (isZoneB(zoneSettings.getZone(), deviceInformationState)) {
            return new ZoneBControlXML(connection, zoneSettings, listener, deviceInformationState, inputConverterSupplier);
        }
        return new ZoneControlXML(connection, zoneSettings.getZone(), zoneSettings, listener, deviceInformationState, inputConverterSupplier);
    }

    public ZoneAvailableInputs ZoneAvailableInputs(AbstractConnection connection,
                                                   YamahaZoneConfig zoneSettings,
                                                   AvailableInputStateListener listener,
                                                   Supplier<InputConverter> inputConverterSupplier,
                                                   DeviceInformationState deviceInformationState) {

        if (isZoneB(zoneSettings.getZone(), deviceInformationState)) {
            return new ZoneBAvailableInputsXML(connection, listener, inputConverterSupplier);
        }
        return new ZoneAvailableInputsXML(connection, zoneSettings.getZone(), listener, inputConverterSupplier);
    }

    /**
     * Checks if the specified Zone_2 should be emulated using Zone_B feature.
     *
     * @param zone
     * @param deviceInformationState
     * @return
     */
    private boolean isZoneB(YamahaReceiverBindingConstants.Zone zone, DeviceInformationState deviceInformationState) {
        return YamahaReceiverBindingConstants.Zone.Zone_2.equals(zone) && deviceInformationState.features.contains(YamahaReceiverBindingConstants.Feature.ZONE_B);
    }

    public DeviceInformation DeviceInformation(AbstractConnection connection,
                                               DeviceInformationState state) {
        return new DeviceInformationXML(connection, state);
    }

    public InputConverter InputConverter(AbstractConnection connection,
                                         String setting) {

        return new InputConverterXML(connection, setting);
    }

}
