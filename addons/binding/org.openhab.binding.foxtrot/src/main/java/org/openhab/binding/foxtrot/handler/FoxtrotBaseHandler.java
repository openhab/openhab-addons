/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.foxtrot.handler;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.openhab.binding.foxtrot.internal.CommandExecutor;
import org.openhab.binding.foxtrot.internal.Refreshable;

/**
 * FoxtrotBaseHandler.
 *
 * @author Radovan Sninsky
 * @since 2018-04-03 18:13
 */
public abstract class FoxtrotBaseHandler extends BaseThingHandler implements Refreshable {

    FoxtrotBridgeHandler foxtrotBridgeHandler;
    CommandExecutor commandExecutor;

    FoxtrotBaseHandler(Thing thing) {
        super(thing);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void initialize() {
        if (getBridge() != null) {
            foxtrotBridgeHandler = (FoxtrotBridgeHandler) getBridge().getHandler();
            commandExecutor = foxtrotBridgeHandler.getCommandExecutor();
        }
    }
}
