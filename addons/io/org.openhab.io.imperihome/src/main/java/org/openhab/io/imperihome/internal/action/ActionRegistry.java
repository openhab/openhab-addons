/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.imperihome.internal.action;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.core.events.EventPublisher;
import org.openhab.io.imperihome.internal.processor.DeviceRegistry;

/**
 * Action registry. Maps ImperiHome API action name to {@link Action} implementation.
 *
 * @author Pepijn de Geus - Initial contribution
 */
public class ActionRegistry {

    private final Map<String, Action> actions = new HashMap<>();

    public ActionRegistry(EventPublisher eventPublisher, DeviceRegistry deviceRegistry) {
        actions.put("setStatus", new SetStatusAction(eventPublisher));
        actions.put("setColor", new SetColorAction(eventPublisher));
        actions.put("setLevel", new SetLevelAction(eventPublisher));
        actions.put("setChoice", new SetChoiceAction(eventPublisher));
        actions.put("setMode", new SetModeAction(eventPublisher, deviceRegistry));
        actions.put("setSetPoint", new SetSetPointAction(eventPublisher));
        actions.put("launchScene", new LaunchSceneAction(eventPublisher));
        actions.put("stopShutter", new StopShutterAction(eventPublisher, deviceRegistry));
    }

    public Action get(String action) {
        return actions.get(action);
    }

}
