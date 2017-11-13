/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.loxone.internal.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.openhab.binding.loxone.internal.core.LxJsonApp3.LxJsonControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A factory of controls of Loxone Miniserver.
 * It creates various types of control objects based on control type received from Miniserver.
 *
 * @author Pawel Pieczul
 *
 */
class LxControlFactory {
    static {
        controls = new HashMap<>();
        addType(LxControlDimmer.class);
        addType(LxControlInfoOnlyAnalog.class);
        addType(LxControlInfoOnlyDigital.class);
        addType(LxControlJalousie.class);
        addType(LxControlLightController.class);
        addType(LxControlLightControllerV2.class);
        addType(LxControlPushbutton.class);
        addType(LxControlRadio.class);
        addType(LxControlSwitch.class);
        addType(LxControlTextState.class);
        addType(LxControlTimedSwitch.class);
    }

    private static Map<String, Class<?>> controls;

    /**
     * Create a {@link LxControl} object for a control received from the Miniserver
     *
     * @param client
     *            websocket client to facilitate communication with Miniserver
     * @param uuid
     *            UUID of the control to be created
     * @param json
     *            JSON describing the control as received from the Miniserver
     * @param room
     *            Room that this control belongs to
     * @param category
     *            Category that this control belongs to
     * @return
     *         created control object or null if error
     */
    static LxControl createControl(LxWsClient client, LxUuid uuid, LxJsonControl json, LxContainer room,
            LxCategory category) {
        if (json == null || json.type == null || json.name == null) {
            return null;
        }
        String type = json.type.toLowerCase();
        Class<?> c = controls.get(type);
        if (c != null) {
            try {
                Constructor<?> constructor = c.getDeclaredConstructor(LxWsClient.class, LxUuid.class,
                        LxJsonControl.class, LxContainer.class, LxCategory.class);
                Object control = constructor.newInstance(client, uuid, json, room, category);
                if (control instanceof LxControl) {
                    return (LxControl) control;
                }
                getLogger().debug("Unexpected object constructed: {}", control.getClass().getSimpleName());
            } catch (NoSuchMethodException | SecurityException | InvocationTargetException | IllegalAccessException
                    | InstantiationException | IllegalArgumentException e) {
                getLogger().debug("Failed to construct control object {}: {}", c.getSimpleName(), e.getMessage());
            }
        } else {
            getLogger().debug("No registered control class for {}, uuid = {}", type, json.uuidAction);
        }
        return null;
    }

    private static void addType(Class<?> c) {
        try {
            String name = (String) c.getDeclaredField("TYPE_NAME").get(null);
            controls.put(name, c);
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
            getLogger().debug("Error registering control class {}: {}", c.getSimpleName(), e.getMessage());
        }
    }

    private static Logger getLogger() {
        return LoggerFactory.getLogger(LxControlFactory.class);
    }
}
