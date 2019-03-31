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
import org.eclipse.smarthome.core.thing.Thing;

/**
 * Handler for a FRITZ! group. Handles commands, which are sent to one of the channels.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class GroupHandler extends AVMFritzBaseThingHandler {

    /**
     * Constructor
     *
     * @param thing Thing object representing a FRITZ! group
     */
    public GroupHandler(Thing thing) {
        super(thing);
    }
}
