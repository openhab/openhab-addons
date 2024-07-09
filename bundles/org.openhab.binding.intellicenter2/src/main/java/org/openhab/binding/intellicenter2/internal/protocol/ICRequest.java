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

import static org.openhab.binding.intellicenter2.internal.protocol.Command.GET_QUERY;
import static org.openhab.binding.intellicenter2.internal.protocol.Command.RELEASE_PARAM_LIST;

import java.util.List;
import java.util.UUID;

import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Valdis Rigdon - Initial contribution
 */
public class ICRequest {

    private final String command;
    private @Nullable final String condition;

    private @Nullable final String queryName;
    private @Nullable final String arguments;

    private @Nullable List<RequestObject> objectList;
    private final String messageID;

    public static final ICRequest getQuery(String queryName) {
        return getQuery(queryName, "");
    }

    public static final ICRequest getQuery(String queryName, String arguments) {
        return new ICRequest(GET_QUERY, null, queryName, arguments);
    }

    public static final ICRequest getParamList(@Nullable String condition, RequestObject... objects) {
        return new ICRequest(Command.GET_PARAM_LIST, condition, null, null, objects);
    }

    public static final ICRequest requestParamList(RequestObject... objects) {
        return new ICRequest(Command.REQUEST_PARAM_LIST, null, null, null, objects);
    }

    public static final ICRequest setParamList(RequestObject... objects) {
        return new ICRequest(Command.SET_PARAM_LIST, null, null, null, objects);
    }

    public ICRequest(Command command, RequestObject... objects) {
        this(command, null, null, null, objects);
    }

    private ICRequest(Command command, @Nullable String condition, @Nullable String queryName,
            @Nullable String arguments, RequestObject... objects) {
        this.command = command.toString();
        this.condition = condition;
        this.queryName = queryName;
        this.arguments = arguments;
        if (objects.length > 0) {
            this.objectList = List.of(objects);
        }
        this.messageID = UUID.randomUUID().toString();
    }

    public static ICRequest releaseParamList(String objectName) {
        return new ICRequest(RELEASE_PARAM_LIST, new RequestObject(objectName));
    }

    public String getCommand() {
        return command;
    }

    public String getCondition() {
        return condition;
    }

    public List<RequestObject> getObjectList() {
        return objectList;
    }

    public String getMessageID() {
        return messageID;
    }
}
