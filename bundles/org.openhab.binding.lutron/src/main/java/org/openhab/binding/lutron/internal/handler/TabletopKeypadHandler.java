/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.lutron.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lutron.internal.discovery.project.ComponentType;
import org.openhab.binding.lutron.internal.keypadconfig.KeypadConfigTabletopSeetouch;
import org.openhab.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler responsible for communicating with Lutron Tabletop seeTouch keypads used in RadioRA2 and Homeworks QS systems
 * (e.g. RR-T5RL, RR-T10RL, RR-T15RL, etc.)
 *
 * @author Bob Adair - Initial contribution
 */
@NonNullByDefault
public class TabletopKeypadHandler extends BaseKeypadHandler {

    private final Logger logger = LoggerFactory.getLogger(TabletopKeypadHandler.class);

    @Override
    protected void configureComponents(@Nullable String model) {
        String mod = model == null ? "Generic" : model;
        logger.debug("Configuring components for keypad model {}", model);

        switch (mod) {
            case "T5RL":
            case "T10RL":
            case "T15RL":
            case "T5CRL":
            case "T10CRL":
            case "T15CRL":
                buttonList = kp.getComponents(mod, ComponentType.BUTTON);
                ledList = kp.getComponents(mod, ComponentType.LED);
                break;

            default:
                logger.warn("No valid keypad model defined ({}). Assuming model T15RL.", mod);
                // fall through
            case "Generic":
                buttonList = kp.getComponents("Generic", ComponentType.BUTTON);
                ledList = kp.getComponents("Generic", ComponentType.LED);
                break;
        }
    }

    public TabletopKeypadHandler(Thing thing) {
        super(thing);
        kp = new KeypadConfigTabletopSeetouch();
    }
}
