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

package org.openhab.binding.octoprint.internal.services;

import org.openhab.core.types.State;

/**
 * The {@link PollRequestInformation}.TODO
 *
 * @author Jan Niklas Freisinger - Initial contribution
 */
public class PollRequestInformation {
    public final String channelUID;
    public final String route;
    public final String jsonKey;
    public final State type;

    PollRequestInformation(String channelUID, String route, String jsonKey, State type) {
        this.channelUID = channelUID;
        this.route = route;
        this.jsonKey = jsonKey;
        this.type = type;
    }
}
