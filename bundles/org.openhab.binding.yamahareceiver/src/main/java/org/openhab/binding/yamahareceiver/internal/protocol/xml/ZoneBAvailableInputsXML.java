/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import org.openhab.binding.yamahareceiver.internal.protocol.AbstractConnection;
import org.openhab.binding.yamahareceiver.internal.protocol.InputConverter;
import org.openhab.binding.yamahareceiver.internal.state.AvailableInputStateListener;
import org.slf4j.LoggerFactory;

/**
 * Special case of {@link ZoneAvailableInputsXML} that emulates Zone_2 for Yamaha HTR-xxx using Zone_B features.
 *
 * @author Tomasz Maruszak - Initial contribution.
 */
public class ZoneBAvailableInputsXML extends ZoneAvailableInputsXML {

    public ZoneBAvailableInputsXML(AbstractConnection con, AvailableInputStateListener observer,
            Supplier<InputConverter> inputConverterSupplier) {
        super(con, Zone.Main_Zone, observer, inputConverterSupplier);
        this.logger = LoggerFactory.getLogger(ZoneBAvailableInputsXML.class);
    }

    @Override
    public Zone getZone() {
        return Zone.Zone_2;
    }
}
