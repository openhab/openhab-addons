/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.dirigera.internal.interfaces;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.OnOffType;

/**
 * {@link PowerListener} for notifications of device power events
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public interface PowerListener {

    /**
     * Informs if power state of device has changed.
     *
     * @param power new power state
     * @param requested flag showing if new power state was requested by OH user command or from outside (e.g wall
     *            mounted switch)
     */
    void powerChanged(OnOffType power, boolean requested);
}
