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
package org.openhab.binding.loxone.internal.core;

import java.util.Map;
import com.google.gson.JsonElement;

/**
 * A structure of JSON file http://miniserver/data/LoxAPP3.json used for parsing it with Gson library.
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
public class LxJsonApp3 {

    public LxJsonInfo msInfo;
    public Map<String, LxJsonControl> controls;
    public Map<String, LxJsonRoom> rooms;
    public Map<String, LxJsonCat> cats;

    public class LxJsonInfo {
        public String serialNr;
        public String location;
        public String roomTitle;
        public String catTitle;
        public String msName;
        public String projectName;
        public String remoteUrl;
        public String swVersion;
        public String macAddress;
    }

    public class LxJsonRoom {
        public String uuid;
        public String name;
    }

    public class LxJsonCat {
        public String uuid;
        public String name;
        public String type;
    }

    public class LxJsonControl {

        public class LxJsonDetails {
            public class LxJsonText {
                public String off;
                public String on;
            }

            public LxJsonText text;
            public String format;
            public int movementScene;
            public String allOff;
            public Map<String, String> outputs;
        }

        public String uuidAction;
        public String name;
        public String type;
        public String room;
        public String cat;
        public LxJsonDetails details;
        public Map<String, JsonElement> states;
        public Map<String, LxJsonControl> subControls;
    }
}
