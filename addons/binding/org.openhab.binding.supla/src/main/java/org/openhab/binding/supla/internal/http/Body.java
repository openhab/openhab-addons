/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.supla.internal.http;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

public abstract class Body {
    protected final Map<String, String> body;

    protected Body(Map<String, String> body) {
        if(body == null) {
            this.body = ImmutableMap.of();
        } else {
            this.body = ImmutableMap.copyOf(body);
        }
    }

    protected Body() {
        this(null);
    }

    public Map<String, String> getBody() {
        return body;
    }

    public abstract byte[] buildBytesToSend();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Body)) return false;

        Body body1 = (Body) o;

        return body.equals(body1.body);
    }

    @Override
    public int hashCode() {
        return body.hashCode();
    }

    @Override
    public String toString() {
        return "Body{" +
                "body=" + body +
                '}';
    }
}
