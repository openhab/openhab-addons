/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import org.openhab.binding.yamahareceiver.internal.YamahaReceiverBindingConstants.Zone;
import org.openhab.binding.yamahareceiver.internal.config.YamahaZoneConfig;
import org.openhab.binding.yamahareceiver.internal.protocol.AbstractConnection;
import org.openhab.binding.yamahareceiver.internal.protocol.InputConverter;
import org.openhab.binding.yamahareceiver.internal.state.DeviceInformationState;
import org.openhab.binding.yamahareceiver.internal.state.ZoneControlStateListener;
import org.slf4j.LoggerFactory;

/**
 * Special case of {@link ZoneControlXML} that emulates Zone_2 for Yamaha HTR-xxx using Zone_B features.
 *
 * @author Tomasz Maruszak - Initial contribution.
 */
public class ZoneBControlXML extends ZoneControlXML {

    public ZoneBControlXML(AbstractConnection con, YamahaZoneConfig zoneSettings, ZoneControlStateListener observer,
            DeviceInformationState deviceInformationState, Supplier<InputConverter> inputConverterSupplier) {
        // Commands will need to be send to Main_Zone
        super(con, Zone.Main_Zone, zoneSettings, observer, deviceInformationState, inputConverterSupplier);

        this.logger = LoggerFactory.getLogger(ZoneBControlXML.class);
    }

    @Override
    public Zone getZone() {
        return Zone.Zone_2;
    }

    @Override
    protected void applyModelVariations() {
        super.applyModelVariations();

        // Apply custom templates for HTR-xxx
        this.power = new CommandTemplate("<Power_Control><Zone_B_Power>%s</Zone_B_Power></Power_Control>",
                "Power_Control/Zone_B_Power_Info");
        this.mute = new CommandTemplate("<Volume><Zone_B><Mute>%s</Mute></Zone_B></Volume>", "Volume/Zone_B/Mute");
        this.volume = new CommandTemplate(
                "<Volume><Zone_B><Lvl><Val>%d</Val><Exp>1</Exp><Unit>dB</Unit></Lvl></Zone_B></Volume>",
                "Volume/Zone_B/Lvl/Val");
    }
}
