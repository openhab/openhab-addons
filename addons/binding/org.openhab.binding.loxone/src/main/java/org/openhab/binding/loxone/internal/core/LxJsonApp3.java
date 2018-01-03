/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.loxone.internal.core;

import java.util.Map;

import com.google.gson.JsonElement;

/**
 * A structure of JSON file http://miniserver/data/LoxAPP3.json used for parsing it with Gson library.
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
class LxJsonApp3 {

    LxJsonInfo msInfo;
    Map<String, LxJsonControl> controls;
    Map<String, LxJsonRoom> rooms;
    Map<String, LxJsonCat> cats;

    class LxJsonInfo {
        String serialNr;
        String location;
        String roomTitle;
        String catTitle;
        String msName;
        String projectName;
        String remoteUrl;
    }

    class LxJsonRoom {
        String uuid;
        String name;
    }

    class LxJsonCat {
        String uuid;
        String name;
        String type;
    }

    class LxJsonControl {

        class LxJsonDetails {
            class LxJsonText {
                String off;
                String on;
            }

            LxJsonText text;
            String format;
            int movementScene;
            String allOff;
            Map<String, String> outputs;
        }

        String uuidAction;
        String name;
        String type;
        String room;
        String cat;
        LxJsonDetails details;
        Map<String, JsonElement> states;
        Map<String, LxJsonControl> subControls;
    }
}
