/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.gardena.internal.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Represents a Gardena session.
 *
 * @author Gerhard Riegler - Initial contribution
 */
@JsonTypeInfo(include = As.WRAPPER_OBJECT, use = Id.NAME)
@JsonTypeName("sessions")
public class Session {
    private String token;
    private long created;

    @JsonProperty(value = "user_id")
    private String userId;

    public Session() {
        this.created = System.currentTimeMillis();
    }

    /**
     * Returns the token of the session.
     */
    public String getToken() {
        return token;
    }

    /**
     * Returns the userId of the session.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Returns the creation timestamp of the session.
     */
    public long getCreated() {
        return created;
    }

}
