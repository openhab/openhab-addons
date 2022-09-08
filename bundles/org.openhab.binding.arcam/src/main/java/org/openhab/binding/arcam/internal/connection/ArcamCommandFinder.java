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
package org.openhab.binding.arcam.internal.connection;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ArcamCommandFinder} class provides lookup methods, used to lookup ArcamCommandCode's from the device
 * specific lists.
 *
 * @author Joep Admiraal - Initial contribution
 */
@NonNullByDefault
public class ArcamCommandFinder {
    private final Logger logger = LoggerFactory.getLogger(ArcamCommandFinder.class);

    public byte[] getCommandDataFromCode(ArcamCommandCode code, List<ArcamCommand> list) {
        for (ArcamCommand command : list) {
            if (command.code.equals(code)) {
                return command.data.clone();
            }
        }

        logger.trace("Could not find ArcamCommand data for code: {}.", code);
        return new byte[] {};
    }
}
