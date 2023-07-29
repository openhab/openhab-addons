/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.enocean.internal.messages.Response;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
@NonNullByDefault
public abstract class ResponseListenerIgnoringTimeouts<T extends @Nullable Response> extends ResponseListener<T> {

    @SuppressWarnings({ "unchecked", "null" })
    public ResponseListenerIgnoringTimeouts() {
        this.persistentClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass())
                .getActualTypeArguments()[0];
    }

    @Override
    public void responseTimeOut() {
    }
}
