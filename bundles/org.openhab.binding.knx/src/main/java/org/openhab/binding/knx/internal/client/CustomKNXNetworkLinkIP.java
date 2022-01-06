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
