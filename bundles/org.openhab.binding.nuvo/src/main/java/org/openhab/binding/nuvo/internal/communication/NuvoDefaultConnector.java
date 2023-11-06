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
package org.openhab.binding.nuvo.internal.communication;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.nuvo.internal.NuvoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to create a default NuvoDefaultConnector before initialization is complete.
 *
 * @author Laurent Garnier - Initial contribution
 * @author Michael Lobstein - Adapted for the Nuvo binding
 */
@NonNullByDefault
public class NuvoDefaultConnector extends NuvoConnector {

    private final Logger logger = LoggerFactory.getLogger(NuvoDefaultConnector.class);

    @Override
    public void open() throws NuvoException {
        logger.warn("Nuvo binding incorrectly configured. Please configure for Serial or IP over serial connection");
        setConnected(false);
    }

    @Override
    public void close() {
        setConnected(false);
    }
}
