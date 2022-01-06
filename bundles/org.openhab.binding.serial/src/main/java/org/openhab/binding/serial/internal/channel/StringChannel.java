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
package org.openhab.binding.serial.internal.channel;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.serial.internal.transform.ValueTransformationProvider;

/**
 * The {@link StringChannel} channel applies a format followed by a transform.
 *
 * @author Mike Major - Initial contribution
 */
@NonNullByDefault
public class StringChannel extends DeviceChannel {

    public StringChannel(final ValueTransformationProvider valueTransformationProvider, final ChannelConfig config) {
        super(valueTransformationProvider, config);
    }
}
