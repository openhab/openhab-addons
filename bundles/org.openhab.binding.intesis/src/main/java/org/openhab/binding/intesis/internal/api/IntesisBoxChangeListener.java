/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.intesis.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.ThingStatus;

/**
 * The {@link IntesisBoxChangeListener} is in interface for an IntesisBox changed consumer
 *
 * @author Hans-Jörg Merk - Initial contribution
 */
@NonNullByDefault
public interface IntesisBoxChangeListener {
    /**
     * This method will be called in case a message was received.
     *
     */
    void messageReceived(String messageLine);

    /**
     * This method will be called in case the connection status has changed.
     *
     */
    void connectionStatusChanged(ThingStatus status, @Nullable String message);
}
