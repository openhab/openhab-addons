/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.yamahareceiver.internal.protocol.xml;

import java.util.function.Supplier;

import org.openhab.binding.yamahareceiver.internal.YamahaReceiverBindingConstants;
import org.openhab.binding.yamahareceiver.internal.config.YamahaBridgeConfig;
import org.openhab.binding.yamahareceiver.internal.config.YamahaZoneConfig;
import org.openhab.binding.yamahareceiver.internal.protocol.AbstractConnection;
import org.openhab.binding.yamahareceiver.internal.protocol.ConnectionStateListener;
import org.openhab.binding.yamahareceiver.internal.protocol.DeviceInformation;
import org.openhab.binding.yamahareceiver.internal.protocol.InputConverter;
import org.openhab.binding.yamahareceiver.internal.protocol.InputWithNavigationControl;
import org.openhab.binding.yamahareceiver.internal.protocol.InputWithPlayControl;
import org.openhab.binding.yamahareceiver.internal.protocol.InputWithPresetControl;
import org.openhab.binding.yamahareceiver.internal.protocol.InputWithTunerBandControl;
import org.openhab.binding.yamahareceiver.internal.protocol.ProtocolFactory;
import org.openhab.binding.yamahareceiver.internal.protocol.SystemControl;
import org.openhab.binding.yamahareceiver.internal.protocol.ZoneAvailableInputs;
import org.openhab.binding.yamahareceiver.internal.protocol.ZoneControl;
import org.openhab.binding.yamahareceiver.internal.state.AvailableInputStateListener;
import org.openhab.binding.yamahareceiver.internal.state.DabBandStateListener;
import org.openhab.binding.yamahareceiver.internal.state.DeviceInformationState;
import org.openhab.binding.yamahareceiver.internal.state.NavigationControlState;
import org.openhab.binding.yamahareceiver.internal.state.NavigationControlStateListener;
import org.openhab.binding.yamahareceiver.internal.state.PlayInfoStateListener;
import org.openhab.binding.yamahareceiver.internal.state.PresetInfoStateListener;
import org.openhab.binding.yamahareceiver.internal.state.SystemControlStateListener;
import org.openhab.binding.yamahareceiver.internal.state.ZoneControlStateListener;

/**
 * Implementation of {@link ProtocolFactory} for XML protocol.
 *
 * @author Tomasz Maruszak - Initial contribution.
 */
public class XMLProtocolFactory implements ProtocolFactory {
    @Override
    public void createConnection(String host, ConnectionStateListener connectionStateListener) {
        connectionStateListener.onConnectionCreated(new XMLConnection(host));
    }

    @Override
    public SystemControl SystemControl(AbstractConnection connection, SystemControlStateListener listener,
            DeviceInformationState deviceInformationState) {
        return new SystemControlXML(connection, listener, deviceInformationState);
    }

    @Override
    public InputWithPlayControl InputWithPlayControl(AbstractConnection connection, String currentInputID,
            PlayInfoStateListener listener, YamahaBridgeConfig bridgeConfig,
            DeviceInformationState deviceInformationState) {
        return new InputWithPlayControlXML(currentInputID, connection, listener, bridgeConfig, deviceInformationState);
    }

    @Override
    public InputWithPresetControl InputWithPresetControl(AbstractConnection connection, String currentInputID,
            PresetInfoStateListener listener, DeviceInformationState deviceInformationState) {
        return new InputWithPresetControlXML(currentInputID, connection, listener, deviceInformationState);
    }

    @Override
    public InputWithTunerBandControl InputWithDabBandControl(String currentInputID, AbstractConnection connection,
            DabBandStateListener observerForBand, PresetInfoStateListener observerForPreset,
            PlayInfoStateListener observerForPlayInfo, DeviceInformationState deviceInformationState) {
        return new InputWithTunerDABControlXML(currentInputID, connection, observerForBand, observerForPreset,
                observerForPlayInfo, deviceInformationState);
    }

    @Override
    public InputWithNavigationControl InputWithNavigationControl(AbstractConnection connection,
            NavigationControlState state, String inputID, NavigationControlStateListener observer,
            DeviceInformationState deviceInformationState) {
        return new InputWithNavigationControlXML(state, inputID, connection, observer, deviceInformationState);
    }

    @Override
    public ZoneControl ZoneControl(AbstractConnection connection, YamahaZoneConfig zoneSettings,
            ZoneControlStateListener listener, Supplier<InputConverter> inputConverterSupplier,
            DeviceInformationState deviceInformationState) {
        if (isZoneB(zoneSettings.getZone(), deviceInformationState)) {
            return new ZoneBControlXML(connection, zoneSettings, listener, deviceInformationState,
                    inputConverterSupplier);
        }
        return new ZoneControlXML(connection, zoneSettings.getZone(), zoneSettings, listener, deviceInformationState,
                inputConverterSupplier);
    }

    @Override
    public ZoneAvailableInputs ZoneAvailableInputs(AbstractConnection connection, YamahaZoneConfig zoneSettings,
            AvailableInputStateListener listener, Supplier<InputConverter> inputConverterSupplier,
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
        return YamahaReceiverBindingConstants.Zone.Zone_2.equals(zone)
                && deviceInformationState.features.contains(YamahaReceiverBindingConstants.Feature.ZONE_B);
    }

    @Override
    public DeviceInformation DeviceInformation(AbstractConnection connection, DeviceInformationState state) {
        return new DeviceInformationXML(connection, state);
    }

    @Override
    public InputConverter InputConverter(AbstractConnection connection, String setting) {
        return new InputConverterXML(connection, setting);
    }
}
