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
package org.openhab.binding.lgthinq.internal.type;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeProvider;

/**
 * The ThinqChannelTypeProvider interface.
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public interface ThinqChannelTypeProvider extends ChannelTypeProvider {
    public void addChannelType(final ChannelType channelType);
}
