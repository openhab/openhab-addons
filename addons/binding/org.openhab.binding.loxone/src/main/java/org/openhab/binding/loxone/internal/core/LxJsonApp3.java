/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.loxone.internal.core;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.JsonElement;

/**
 * A structure of JSON file http://miniserver/data/LoxAPP3.json used for parsing it with Gson library.
 * All fields of this class can be null, because they are based on a data received from the remote server.
 * Only map keys and values we assume are non-null, once the map element is created.
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
@NonNullByDefault
class LxJsonApp3 {

    @Nullable
    LxJsonInfo msInfo;
    @Nullable
    Map<String, LxJsonControl> controls;
    @Nullable
    Map<String, LxJsonRoom> rooms;
    @Nullable
    Map<String, LxJsonCat> cats;

    class LxJsonInfo {
        @Nullable
        String serialNr;
        @Nullable
        String location;
        @Nullable
        String roomTitle;
        @Nullable
        String catTitle;
        @Nullable
        String msName;
        @Nullable
        String projectName;
        @Nullable
        String remoteUrl;
    }

    class LxJsonRoom {
        @Nullable
        String uuid;
        @Nullable
        String name;
    }

    class LxJsonCat {
        @Nullable
        String uuid;
        @Nullable
        String name;
        @Nullable
        String type;
    }

    class LxJsonControl {

        class LxJsonDetails {
            class LxJsonText {
                @Nullable
                String off;
                @Nullable
                String on;
            }

            @Nullable
            LxJsonText text;
            @Nullable
            String format;
            @Nullable
            Integer movementScene;
            @Nullable
            String allOff;
            @Nullable
            Map<String, String> outputs;
        }

        @Nullable
        String uuidAction;
        @Nullable
        String name;
        @Nullable
        String type;
        @Nullable
        String room;
        @Nullable
        String cat;
        @Nullable
        LxJsonDetails details;
        @Nullable
        Map<String, JsonElement> states;
        @Nullable
        Map<String, LxJsonControl> subControls;
    }
}
