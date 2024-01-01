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
package org.openhab.binding.resol.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.BaseThingHandler;

import de.resol.vbus.Packet;
import de.resol.vbus.Specification;
import de.resol.vbus.SpecificationFile.Language;

/**
 * The {@link ResolBaseThingHandler} class is a common ancestor for Resol thing handlers, capabale of handling vbus
 * packets
 *
 * @author Raphael Mack - Initial contribution
 */
@NonNullByDefault
public abstract class ResolBaseThingHandler extends BaseThingHandler {

    public ResolBaseThingHandler(Thing thing) {
        super(thing);
    }

    protected abstract void packetReceived(Specification spec, Language lang, Packet packet);
}
