/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.enocean.internal.transceiver;

import java.lang.reflect.ParameterizedType;

import org.openhab.binding.enocean.internal.messages.Response;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
public abstract class ResponseListenerIgnoringTimeouts<T extends Response> extends ResponseListener<T> {

    @SuppressWarnings("unchecked")
    public ResponseListenerIgnoringTimeouts() {
        this.persistentClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass())
                .getActualTypeArguments()[0];
    }

    @Override
    public void responseTimeOut() {

    }
}
