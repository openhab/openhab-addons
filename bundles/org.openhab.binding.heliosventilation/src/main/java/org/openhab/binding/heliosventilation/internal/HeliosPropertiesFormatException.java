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
package org.openhab.binding.heliosventilation.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link HeliosPropertiesFormatException} class defines an exception to describe parsing format errors
 *
 * @author Raphael Mack - Initial contribution
 */
@NonNullByDefault
public class HeliosPropertiesFormatException extends Exception {
    private static final long serialVersionUID = 8051109351111509577L;
    private final String channelName;
    private final String fullSpec;
    private final String reason;

    public HeliosPropertiesFormatException(String reason, String channelName, String fullSpec) {
        this.channelName = channelName;
        this.fullSpec = fullSpec;
        this.reason = reason;
    }

    public String getChannelName() {
        return channelName;
    }

    public String getFullSpec() {
        return fullSpec;
    }

    @Override
    public @Nullable String getMessage() {
        return "Cannot parse '" + fullSpec + "' for datapoint '" + channelName + "': " + reason;
    }
}
