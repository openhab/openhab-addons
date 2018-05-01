/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.yamahareceiver.internal.protocol.xml;

import org.openhab.binding.yamahareceiver.YamahaReceiverBindingConstants.Zone;
import org.openhab.binding.yamahareceiver.internal.protocol.AbstractConnection;
import org.openhab.binding.yamahareceiver.internal.protocol.InputConverter;
import org.openhab.binding.yamahareceiver.internal.config.YamahaZoneConfiguration;
import org.openhab.binding.yamahareceiver.internal.state.DeviceInformationState;
import org.openhab.binding.yamahareceiver.internal.state.ZoneControlStateListener;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

/**
 *
 * Special case of {@link ZoneControlXML} that emulates Zone_2 for Yamaha HTR-xxx using Zone_B features.
 *
 * @author Tomasz Maruszak - Initial contribution.
 */
public class ZoneBControlXML extends ZoneControlXML {

    public ZoneBControlXML(AbstractConnection xml,
                           YamahaZoneConfiguration zoneSettings,
                           ZoneControlStateListener observer,
                           DeviceInformationState deviceInformationState,
                           Supplier<InputConverter> inputConverterSupplier) {

        // Commands will need to be send to Main_Zone
        super(xml, Zone.Main_Zone, zoneSettings, observer, deviceInformationState, inputConverterSupplier);

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
        this.powerCmd = "<Power_Control><Zone_B_Power>%s</Zone_B_Power></Power_Control>";
        this.powerPath = "Power_Control/Zone_B_Power_Info";
        this.muteCmd = "<Volume><Zone_B><Mute>%s</Mute></Zone_B></Volume>";
        this.mutePath = "Volume/Zone_B/Mute";
        this.volumeCmd = "<Volume><Zone_B><Lvl><Val>%d</Val><Exp>1</Exp><Unit>dB</Unit></Lvl></Zone_B></Volume>";
        this.volumePath = "Volume/Zone_B/Lvl/Val";
    }
}
