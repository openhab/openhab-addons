/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.kostalpikoiqplenticore.internal.things;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.kostalpikoiqplenticore.internal.KostalPikoIqPlenticoreHandlerBase;
import org.openhab.binding.kostalpikoiqplenticore.internal.KostalPikoIqPlenticoreInverterTypes;

/**
 * The {@link KostalPikoIq85Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author René Stakemeier - Initial contribution
 */
@NonNullByDefault
public class KostalPikoIq85Handler extends KostalPikoIqPlenticoreHandlerBase {

    public KostalPikoIq85Handler(Thing thing) {
        super(thing);
    }

    @Override
    protected KostalPikoIqPlenticoreInverterTypes getInverterType() {
        return KostalPikoIqPlenticoreInverterTypes.PikoIq85;
    }

}
