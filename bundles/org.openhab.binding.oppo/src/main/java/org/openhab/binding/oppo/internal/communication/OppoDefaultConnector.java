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
package org.openhab.binding.oppo.internal.communication;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.oppo.internal.OppoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to create a default MonopriceAudioConnector before initialization is complete.
 *
 * @author Laurent Garnier - Initial contribution
 * @author Michael Lobstein - Adapted for the Oppo binding
 */
@NonNullByDefault
public class OppoDefaultConnector extends OppoConnector {
    private final Logger logger = LoggerFactory.getLogger(OppoDefaultConnector.class);

    @Override
    public void open() throws OppoException {
        logger.warn("Oppo binding incorrectly configured. Please configure for Serial or IP connection");
        setConnected(false);
    }

    @Override
    public void close() {
        setConnected(false);
    }
}
