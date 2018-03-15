/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.knx.internal.client;

import tuwien.auto.calimero.KNXException;
import tuwien.auto.calimero.knxnetip.KNXnetIPConnection;
import tuwien.auto.calimero.link.KNXNetworkLinkIP;
import tuwien.auto.calimero.link.medium.KNXMediumSettings;

/**
 * Subclass of {@link KNXNetworkLinkIP} which exposes the protected constructor in order to work-around
 * https://github.com/calimero-project/calimero-core/issues/57
 *
 * @author Simon Kaufmann - initial contribution and API
 *
 */
public class CustomKNXNetworkLinkIP extends KNXNetworkLinkIP {

    public static final int TUNNELING = KNXNetworkLinkIP.TUNNELING;
    public static final int ROUTING = KNXNetworkLinkIP.ROUTING;

    CustomKNXNetworkLinkIP(final int serviceMode, KNXnetIPConnection conn, KNXMediumSettings settings)
            throws KNXException, InterruptedException {
        super(serviceMode, conn, settings);
    }

}
