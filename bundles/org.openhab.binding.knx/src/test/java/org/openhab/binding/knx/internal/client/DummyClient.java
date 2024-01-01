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
package org.openhab.binding.knx.internal.client;

import java.util.Collections;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.knx.internal.handler.KNXBridgeBaseThingHandler.CommandExtensionData;
import org.openhab.core.thing.ThingUID;

import tuwien.auto.calimero.KNXException;
import tuwien.auto.calimero.link.KNXNetworkLink;

/**
 * {@link AbstractKNXClient} implementation for test, using {@link DummyKNXNetworkLink}.
 *
 * @author Holger Friedrich - initial contribution and API.
 *
 */
@NonNullByDefault
public class DummyClient extends AbstractKNXClient {

    public DummyClient() {
        super(0, new ThingUID("dummy connection"), 0, 0, 0, null, new CommandExtensionData(Collections.emptyMap()),
                null);
    }

    @Override
    protected KNXNetworkLink establishConnection() throws KNXException, InterruptedException {
        return new DummyKNXNetworkLink();
    }
}
