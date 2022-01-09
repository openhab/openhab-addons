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
