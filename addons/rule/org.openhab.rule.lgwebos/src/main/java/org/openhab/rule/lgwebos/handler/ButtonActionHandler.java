/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.rule.lgwebos.handler;

import java.util.Map;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.handler.ActionHandler;
import org.eclipse.smarthome.automation.handler.BaseModuleHandler;
import org.openhab.binding.lgwebos.LGWebOS;
import org.openhab.binding.lgwebos.LGWebOS.Button;

public class ButtonActionHandler extends BaseModuleHandler<Action> implements ActionHandler {
    public static final String TYPE_ID = "lgwebos.ButtonAction";
    public static final String PARAM_THING_ID = "thingId";
    public static final String PARAM_BUTTON = "button";

    private LGWebOS api;

    public ButtonActionHandler(Action module, LGWebOS api) {
        super(module);
        this.api = api;
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> context) {
        String thingId = module.getConfiguration().get(PARAM_THING_ID).toString();
        Button button = Button.valueOf(module.getConfiguration().get(PARAM_BUTTON).toString());
        api.sendButton(thingId, button);
        return null;
    }

}
