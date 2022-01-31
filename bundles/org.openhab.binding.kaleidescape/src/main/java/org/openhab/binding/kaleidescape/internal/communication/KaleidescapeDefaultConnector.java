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
package org.openhab.binding.kaleidescape.internal.communication;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.kaleidescape.internal.KaleidescapeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to create a default Kaleidescape before initialization is complete.
 *
 * @author Laurent Garnier - Initial contribution
 * @author Michael Lobstein - Adapted for the Kaleidescape binding
 */
@NonNullByDefault
public class KaleidescapeDefaultConnector extends KaleidescapeConnector {
    private final Logger logger = LoggerFactory.getLogger(KaleidescapeDefaultConnector.class);

    @Override
    public void open() throws KaleidescapeException {
        logger.warn("Kaleidescape binding incorrectly configured. Please configure for IP or serial connection");
        setConnected(false);
    }

    @Override
    public void close() {
        setConnected(false);
    }

    @Override
    public void sendCommand(@Nullable String value) {
        logger.warn("Kaleidescape binding incorrectly configured. Please configure for IP or serial connection");
        setConnected(false);
    }
}
