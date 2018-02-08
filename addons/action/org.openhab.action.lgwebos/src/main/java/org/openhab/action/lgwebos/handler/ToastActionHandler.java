/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.action.lgwebos.handler;

import java.io.IOException;
import java.util.Map;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.handler.ActionHandler;
import org.eclipse.smarthome.automation.handler.BaseModuleHandler;
import org.openhab.binding.lgwebos.LGWebOS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This action handler allows to send a toast message.
 *
 * @author Sebastian Prehn - initial contribution
 *
 */
public class ToastActionHandler extends BaseModuleHandler<Action> implements ActionHandler {
    public static final String TYPE_ID = "lgwebos.ToastAction";
    public static final String PARAM_THING_ID = "thingId";
    public static final String PARAM_MESSAGE = "message";
    private final Logger logger = LoggerFactory.getLogger(ToastActionHandler.class);
    private LGWebOS api;

    public ToastActionHandler(Action module, LGWebOS api) {
        super(module);
        this.api = api;
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> context) {
        String deviceId = module.getConfiguration().get(PARAM_THING_ID).toString();
        String message = module.getConfiguration().get(PARAM_MESSAGE).toString();
        try {
            api.showToast(deviceId, message);
        } catch (IOException e) {
            logger.error("Error sending toast: '{}'", e.getMessage());
        }
        return null;
    }

}
