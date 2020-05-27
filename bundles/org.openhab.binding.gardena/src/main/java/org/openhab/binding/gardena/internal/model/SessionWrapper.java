/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.gardena.internal.model;

import com.google.gson.annotations.SerializedName;

/**
 * Session wrapper for valid Gardena JSON serialization.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class SessionWrapper {
    @SerializedName("data")
    private Session session;

    public SessionWrapper() {
    }

    public SessionWrapper(Session session) {
        this.session = session;
    }

    /**
     * Returns the session.
     */
    public Session getSession() {
        return session;
    }

    /**
     * Sets the session.
     */
    public void setSession(Session session) {
        this.session = session;
    }
}
