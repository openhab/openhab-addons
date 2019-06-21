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
package org.openhab.binding.homeconnect.internal.logger;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.event.Level;

/**
 *
 * Log entry.
 *
 * @author Jonas Brüstel - Initial Contribution
 */
@NonNullByDefault
public class Log {

    private long created;
    private String className;
    private Type type;
    private Level level;
    private @Nullable String message;
    private @Nullable String haId;
    private @Nullable String label;
    private @Nullable List<String> details;
    private @Nullable Request request;
    private @Nullable Response response;

    public Log(long created, String className, Type type, Level level, @Nullable String message, @Nullable String haId,
            @Nullable String label, @Nullable List<String> details, @Nullable Request request,
            @Nullable Response response) {
        this.created = created;
        this.className = className;
        this.type = type;
        this.level = level;
        this.message = message;
        this.haId = haId;
        this.label = label;
        this.details = details;
        this.request = request;
        this.response = response;
    }

    public long getCreated() {
        return created;
    }

    public String getClassName() {
        return className;
    }

    public Type getType() {
        return type;
    }

    public Level getLevel() {
        return level;
    }

    public @Nullable String getMessage() {
        return message;
    }

    public @Nullable String getHaId() {
        return haId;
    }

    public @Nullable String getLabel() {
        return label;
    }

    public @Nullable List<String> getDetails() {
        return details;
    }

    public @Nullable Request getRequest() {
        return request;
    }

    public @Nullable Response getResponse() {
        return response;
    }

    @Override
    public String toString() {
        return "Log [created=" + created + ", className=" + className + ", type=" + type + ", level=" + level
                + ", message=" + message + ", haId=" + haId + ", label=" + label + ", details=" + details + ", request="
                + request + ", response=" + response + "]";
    }
}
