/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.mqtt.generic.internal.generic;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A transformation for a {@link ChannelState}. It is applied for each received value on an MQTT topic.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public interface ChannelStateTransformation {

    /**
     * @param value The incoming value
     * @return The transformed value
     */
    public String processValue(String value);
}