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
package org.openhab.binding.lutron.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lutron.internal.discovery.project.ComponentType;
import org.openhab.binding.lutron.internal.keypadconfig.KeypadConfigSeetouch;
import org.openhab.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler responsible for communicating with Lutron seeTouch and Hybrid seeTouch keypads used in
 * RadioRA2 and Homeworks QS systems
 *
 * @author Bob Adair - Initial contribution
 */
@NonNullByDefault
public class KeypadHandler extends BaseKeypadHandler {

    private final Logger logger = LoggerFactory.getLogger(KeypadHandler.class);

    @Override
    protected void configureComponents(@Nullable String model) {
        String mod = model == null ? "Generic" : model;
        logger.debug("Configuring components for keypad model {}", model);

        switch (mod) {
            case "W1RLD":
            case "H1RLD":
            case "HN1RLD":
                buttonList = kp.getComponents("W1RLD", ComponentType.BUTTON);
                ledList = kp.getComponents("W1RLD", ComponentType.LED);
                break;
            case "W2RLD":
            case "H2RLD":
            case "HN2RLD":
                buttonList = kp.getComponents("W2RLD", ComponentType.BUTTON);
                ledList = kp.getComponents("W2RLD", ComponentType.LED);
                break;
            case "W3S":
            case "H3S":
            case "HN3S":
                buttonList = kp.getComponents("W3S", ComponentType.BUTTON);
                ledList = kp.getComponents("W3S", ComponentType.LED);

                break;
            case "W3BD":
                buttonList = kp.getComponents(mod, ComponentType.BUTTON);
                ledList = kp.getComponents(mod, ComponentType.LED);
                break;
            case "W3BRL":
                buttonList = kp.getComponents(mod, ComponentType.BUTTON);
                ledList = kp.getComponents(mod, ComponentType.LED);
                break;
            case "W3BSRL":
            case "H3BSRL":
            case "HN3BSRL":
                buttonList = kp.getComponents("W3BSRL", ComponentType.BUTTON);
                ledList = kp.getComponents("W3BSRL", ComponentType.LED);
                break;
            case "W4S":
            case "H4S":
            case "HN4S":
                buttonList = kp.getComponents("W4S", ComponentType.BUTTON);
                ledList = kp.getComponents("W4S", ComponentType.LED);
                break;
            case "W5BRL":
            case "H5BRL":
            case "HN5BRL":
            case "W5BRLIR":
                buttonList = kp.getComponents("W5BRL", ComponentType.BUTTON);
                ledList = kp.getComponents("W5BRL", ComponentType.LED);
                break;
            case "W6BRL":
            case "H6BRL":
            case "HN6BRL":
                buttonList = kp.getComponents("W6BRL", ComponentType.BUTTON);
                ledList = kp.getComponents("W6BRL", ComponentType.LED);
                break;
            case "W7B":
                buttonList = kp.getComponents(mod, ComponentType.BUTTON);
                ledList = kp.getComponents(mod, ComponentType.LED);
                break;
            default:
                logger.warn("No valid keypad model defined ({}). Assuming Generic model.", mod);
                // fall through
            case "Generic":
                buttonList = kp.getComponents("Generic", ComponentType.BUTTON);
                ledList = kp.getComponents("Generic", ComponentType.LED);
                break;
        }
    }

    public KeypadHandler(Thing thing) {
        super(thing);
        kp = new KeypadConfigSeetouch();
    }
}
