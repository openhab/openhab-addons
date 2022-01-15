/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.loxone.internal.types;

import java.lang.reflect.Type;
import java.util.Map;

import org.openhab.binding.loxone.internal.LxServerHandlerApi;
import org.openhab.binding.loxone.internal.controls.LxControl;
import org.openhab.binding.loxone.internal.controls.LxControl.LxControlConfig;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

/**
 * A structure of JSON file http://miniserver/data/LoxAPP3.json used for parsing it with Gson library.
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
public class LxConfig {

    private Map<LxUuid, LxContainer> rooms;
    @SerializedName("cats")
    private Map<LxUuid, LxCategory> categories;
    public Map<LxUuid, LxControl> controls;

    public class LxServerInfo {
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

    public LxServerInfo msInfo;

    public void finalize(LxServerHandlerApi thingHandler) {
        rooms.values().removeIf(o -> (o == null || o.getUuid() == null));
        categories.values().removeIf(o -> (o == null || o.getUuid() == null));
        controls.values().removeIf(c -> c == null || c.isSecured());
        controls.values().forEach(c -> c.initialize(
                new LxControlConfig(thingHandler, rooms.get(c.getRoomUuid()), categories.get(c.getCategoryUuid()))));
    }

    public static <T> T deserializeObject(JsonObject parent, String name, Type type,
            JsonDeserializationContext context) {
        JsonElement element = parent.get(name);
        if (element != null) {
            return context.deserialize(element, type);
        }
        return null;
    }

    public static String deserializeString(JsonObject parent, String name) {
        JsonElement element = parent.get(name);
        if (element != null) {
            return element.getAsString();
        }
        return null;
    }
}
