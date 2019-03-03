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
package org.openhab.binding.avmfritz.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.core.thing.Bridge;
import org.openhab.binding.avmfritz.internal.AVMFritzDynamicStateDescriptionProvider;

/**
 * Handler for a FRITZ!Box device. Handles polling of values from AHA devices.
 *
 * @author Robert Bausdorf - Initial contribution
 * @author Christoph Weitkamp - Added support for groups
 */
@NonNullByDefault
public class BoxHandler extends AVMFritzBaseBridgeHandler {

    /**
     * Constructor
     *
     * @param bridge Bridge object representing a FRITZ!Box
     */
    public BoxHandler(Bridge bridge, HttpClient httpClient,
            AVMFritzDynamicStateDescriptionProvider stateDescriptionProvider) {
        super(bridge, httpClient, stateDescriptionProvider);
    }
}
