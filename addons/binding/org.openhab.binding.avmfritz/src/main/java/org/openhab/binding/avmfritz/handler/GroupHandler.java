/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.avmfritz.handler;

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
