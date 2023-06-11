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
package org.openhab.binding.pioneeravr.internal.handler;

import org.openhab.binding.pioneeravr.internal.PioneerAvrBindingConstants;
import org.openhab.binding.pioneeravr.internal.protocol.avr.AvrConnection;
import org.openhab.binding.pioneeravr.internal.protocol.ip.IpAvrConnection;
import org.openhab.core.thing.Thing;

/**
 * An handler of an AVR connected through an IP connection.
 *
 * @author Antoine Besnard - Initial contribution
 */
public class IpAvrHandler extends AbstractAvrHandler {

    public IpAvrHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected AvrConnection createConnection() {
        String host = (String) this.getConfig().get(PioneerAvrBindingConstants.HOST_PARAMETER);
        Integer tcpPort = ((Number) this.getConfig().get(PioneerAvrBindingConstants.TCP_PORT_PARAMETER)).intValue();

        return new IpAvrConnection(host, tcpPort);
    }
}
