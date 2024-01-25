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
package org.openhab.binding.broadlink.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.broadlink.internal.BroadlinkRemoteDynamicCommandDescriptionProvider;
import org.openhab.core.thing.Thing;

/**
 * Extension for the RF part of an RM 4 Pro
 *
 * @author Anton Jansen
 */

@NonNullByDefault
public class BroadlinkRemoteModel4ProHandler extends BroadlinkRemoteModel4MiniHandler {

    public BroadlinkRemoteModel4ProHandler(Thing thing,
            BroadlinkRemoteDynamicCommandDescriptionProvider commandDescriptionProvider) {
        super(thing, commandDescriptionProvider);
        // TODO Auto-generated constructor stub
    }
}
