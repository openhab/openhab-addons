/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.tradfri.internal;

import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.eclipse.californium.scandium.DTLSConnector;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class wraps {@link CoapEndpoint} from californium for the sole purpose of adding some debug logging to it in
 * order to figure out when the endpoint is destroyed.
 * See https://github.com/eclipse/californium/pull/452#issuecomment-341703735
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
public class TradfriCoapEndpoint extends CoapEndpoint {

    private final Logger logger = LoggerFactory.getLogger(TradfriCoapEndpoint.class);

    public TradfriCoapEndpoint(DTLSConnector dtlsConnector, NetworkConfig standard) {
        super(dtlsConnector, standard);
    }

    @Override
    public synchronized void destroy() {
        if (logger.isDebugEnabled()) {
            logger.debug("Destroying CoAP endpoint.", new RuntimeException("Endpoint destroyed"));
        }
        super.destroy();
    }
}
