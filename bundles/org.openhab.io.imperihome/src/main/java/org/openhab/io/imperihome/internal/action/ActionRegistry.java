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
