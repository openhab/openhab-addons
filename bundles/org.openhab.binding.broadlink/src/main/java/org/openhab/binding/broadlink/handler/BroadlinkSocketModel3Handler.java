/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
import org.openhab.core.thing.Thing;
import org.slf4j.LoggerFactory;

/**
 * Smart power socket handler
 *
 * @author John Marshall/Cato Sognen - Initial contribution
 */
@NonNullByDefault
public class BroadlinkSocketModel3Handler extends BroadlinkSocketModel2Handler {

    public BroadlinkSocketModel3Handler(Thing thing) {
        super(thing, LoggerFactory.getLogger(BroadlinkSocketModel3Handler.class));
    }
}
