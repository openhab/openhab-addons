/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.openwebnet.internal;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ResponseHolder} class is defined to manage the access to the Response in a generic way.
 *
 * @author Antoine Laydier
 *
 */
@NonNullByDefault
class ResponseHolder<T> {
    private @NonNull T response;

    /**
     * Create the ResponseHolder
     *
     * @param initialValue initiale value of the response
     */
    ResponseHolder(@NonNull T initialValue) {
        response = initialValue;
    }

    /**
     * Set the content of the response
     *
     * @param response response to set
     * @param shouldNotify flag to indicate if the response need to be dispatched (use false, for a temporary response).
     */
    public synchronized void set(@NonNull T response, boolean shouldNotify) {
        this.response = response;
        if (shouldNotify) {
            this.notifyAll();
        }
    }

    /**
     * Get the content of the response after it has been notified
     *
     * @return the response
     * @throws InterruptedException
     */
    public synchronized @NonNull T get() throws InterruptedException {
        this.wait();
        return response;
    }

    /**
     * Get the content of the response directly if not the default value, or else wait until notification (to be used if
     * the notification may already been dispatched)
     *
     * @param defaultValue value indicating that no response has been provided
     * @return the response
     * @throws InterruptedException
     */
    public synchronized @NonNull T get(T defaultValue) throws InterruptedException {
        if (response.equals(defaultValue)) {
            this.wait();
        }
        return response;
    }

    /**
     * Get the current response without waiting
     *
     * @return the response
     */
    public synchronized @NonNull T pick() {
        return response;
    }
}
