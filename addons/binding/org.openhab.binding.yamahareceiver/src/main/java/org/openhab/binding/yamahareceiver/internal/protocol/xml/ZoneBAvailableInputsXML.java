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
import org.openhab.binding.yamahareceiver.internal.state.AvailableInputStateListener;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

/**
 * Special case of {@link ZoneAvailableInputsXML} that emulates Zone_2 for Yamaha HTR-xxx using Zone_B features.
 *
 * @author Tomasz Maruszak - Initial contribution.
 */
public class ZoneBAvailableInputsXML extends ZoneAvailableInputsXML {

    public ZoneBAvailableInputsXML(AbstractConnection con,
                                   AvailableInputStateListener observer,
                                   Supplier<InputConverter> inputConverterSupplier) {

        super(con, Zone.Main_Zone, observer, inputConverterSupplier);
        this.logger = LoggerFactory.getLogger(ZoneBAvailableInputsXML.class);
    }

    @Override
    public Zone getZone() {
        return Zone.Zone_2;
    }
}
