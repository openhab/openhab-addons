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
package org.openhab.binding.freeathomesystem.internal.type;

import org.openhab.core.thing.type.ChannelGroupType;
import org.openhab.core.thing.type.ChannelGroupTypeProvider;

/**
 *
 * @author Andras Uhrin - Initial contribution
 *
 */
public interface FreeAtHomeChannelGroupTypeProvider extends ChannelGroupTypeProvider {

    public void addChannelGroupType(ChannelGroupType channelGroupType);
}
