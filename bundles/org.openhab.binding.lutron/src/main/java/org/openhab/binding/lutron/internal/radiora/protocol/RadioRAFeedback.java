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
package org.openhab.binding.lutron.internal.radiora.protocol;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Base class for Feedback from RadioRA
 *
 * @author Jeff Lauterbach - Initial Contribution
 *
 */
@NonNullByDefault
public class RadioRAFeedback {

    public String[] parse(String msg, int numParams) {
        String[] params = msg.split(",");
        if (params.length < numParams + 1) {
            throw new IllegalStateException("Invalid message format: " + msg);
        }

        return params;
    }
}
