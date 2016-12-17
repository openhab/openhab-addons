/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.upb.internal.converter;

import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.upb.internal.UPBMessage;

/**
 * Provides conversion between a {@link UPBMessage} and a {@link State} for a single channel.
 *
 * @author Chris Van Orman
 * @since 2.0.0
 *
 */
interface StateChannelConverter {

    /**
     * Converts a {@link UPBMessage} to a {@link State}.
     *
     * @param message the message to convert
     * @return the equivalent State or null if one does not exist
     */
    State convert(UPBMessage message);
}
