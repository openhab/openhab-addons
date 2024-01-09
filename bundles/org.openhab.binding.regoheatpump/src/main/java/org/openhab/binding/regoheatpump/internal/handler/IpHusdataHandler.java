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
package org.openhab.binding.regoheatpump.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.regoheatpump.internal.RegoHeatPumpBindingConstants;
import org.openhab.binding.regoheatpump.internal.protocol.IpRegoConnection;
import org.openhab.binding.regoheatpump.internal.protocol.RegoConnection;
import org.openhab.core.thing.Thing;

/**
 * The {@link IpHusdataHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Boris Krivonog - Initial contribution
 */
@NonNullByDefault
public class IpHusdataHandler extends HusdataHandler {
    public IpHusdataHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected RegoConnection createConnection() {
        String host = (String) getConfig().get(RegoHeatPumpBindingConstants.HOST_PARAMETER);
        Integer port = ((Number) getConfig().get(RegoHeatPumpBindingConstants.TCP_PORT_PARAMETER)).intValue();

        return new IpRegoConnection(host, port);
    }
}
