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
package org.openhab.binding.souliss.handler;

import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.souliss.handler.SoulissGenericTypical.typicalCommonMethods;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SoulissT57Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Tonino Fazio - Initial contribution
 */
public class SoulissT57Handler extends SoulissT5nHandler implements typicalCommonMethods {

    private Logger logger = LoggerFactory.getLogger(SoulissT57Handler.class);

    public SoulissT57Handler(Thing thing) {
        super(thing);
    }

}
