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
package org.openhab.binding.sony.internal.transports;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebEvent;

/**
 * The {@link SonyTransportListener} allows listeners to receive events from a {@link SonyTransport}
 * 
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public interface SonyTransportListener {
    /**
     * Triggered when an event is received from the underlying transport
     *
     * @param event a non-null event
     */
    void onEvent(ScalarWebEvent event);

    /**
     * Triggered when an error occurs during communication
     *
     * @param t the non-null throwable
     */
    void onError(Throwable t);
}
