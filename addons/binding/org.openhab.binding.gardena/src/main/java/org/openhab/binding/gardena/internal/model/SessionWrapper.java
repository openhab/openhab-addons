/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.gardena.internal.model;

import com.google.gson.annotations.SerializedName;

/**
 * Session wrapper for valid Gardena JSON serialization.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class SessionWrapper {
    @SerializedName(value = "sessions")
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
