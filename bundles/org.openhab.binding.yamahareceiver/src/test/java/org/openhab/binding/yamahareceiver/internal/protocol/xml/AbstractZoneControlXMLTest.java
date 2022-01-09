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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.mockito.Mock;
import org.openhab.binding.yamahareceiver.internal.config.YamahaZoneConfig;
import org.openhab.binding.yamahareceiver.internal.protocol.InputConverter;
import org.openhab.binding.yamahareceiver.internal.state.DeviceInformationState;
import org.openhab.binding.yamahareceiver.internal.state.ZoneControlStateListener;

/**
 * Common zone test.
 *
 * @author Tomasz Maruszak - Initial contribution
 */
public abstract class AbstractZoneControlXMLTest extends AbstractXMLProtocolTest {

    protected DeviceInformationState deviceInformationState;

    protected @Mock InputConverter inputConverter;
    protected @Mock ZoneControlStateListener zoneControlStateListener;
    protected @Mock YamahaZoneConfig zoneConfig;

    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();

        deviceInformationState = new DeviceInformationState();

        when(zoneConfig.getVolumeDbMin()).thenReturn(-10f);
        when(zoneConfig.getVolumeDbMax()).thenReturn(10f);

        when(inputConverter.fromStateName(anyString())).thenAnswer(p -> p.getArgument(0));
        when(inputConverter.toCommandName(anyString())).thenAnswer(p -> p.getArgument(0));
    }
}
