/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.atv.internal.client.capability;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.atv.internal.client.dto.Playing;

/**
 * Listener interface for push updates.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public interface PushListener {

    /**
     * Informs about changes to what is currently playing.
     *
     * @param updater the push updater dispatching the update
     * @param playstatus new play status
     */
    void playstatusUpdate(PushUpdater updater, Playing playstatus);

    /**
     * Informs about an error when updating play status.
     *
     * @param updater the push updater dispatching the error
     * @param exception error that occurred
     */
    void playstatusError(PushUpdater updater, Exception exception);
}
