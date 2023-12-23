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
package org.openhab.binding.intellicenter2.internal.protocol;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Valdis Rigdon - Initial contribution
 */
@NonNullByDefault
public class ICResponse {

    private String messageID = "";
    private String command = "";
    private @Nullable String description;
    private String response = "";

    private @Nullable List<ResponseObject> objectList;
    private @Nullable List<ResponseObject> answer;

    public ICResponse() {
    }

    @NonNull
    public String getMessageID() {
        return messageID;
    }

    public String getCommand() {
        return command;
    }

    public List<ResponseObject> getObjectList() {
        if (objectList == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(objectList);
    }

    @Nullable
    public String getDescription() {
        return description;
    }

    public List<ResponseObject> getAnswer() {
        if (answer == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(answer);
    }

    /**
     * HTTP response code for the response.
     * 
     * @return
     */
    public String getResponse() {
        return response;
    }

    @Override
    public String toString() {
        return ICProtocol.GSON.toJson(this);
    }
}
