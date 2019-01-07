/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.enocean.internal.transceiver;

import java.lang.reflect.ParameterizedType;

import org.openhab.binding.enocean.internal.EnOceanException;
import org.openhab.binding.enocean.internal.messages.Response;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
public abstract class ResponseListener<T extends Response> {

    protected Class<T> persistentClass;

    @SuppressWarnings("unchecked")
    public ResponseListener() {
        this.persistentClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass())
                .getActualTypeArguments()[0];
    }

    public abstract void responseReceived(T response);

    public abstract void responseTimeOut();

    public void handleResponse(Response response) throws EnOceanException {
        try {
            responseReceived(persistentClass.getConstructor(Response.class).newInstance(response));
        } catch (Exception e) {
            throw new EnOceanException(e.getMessage());
        }
    }
}