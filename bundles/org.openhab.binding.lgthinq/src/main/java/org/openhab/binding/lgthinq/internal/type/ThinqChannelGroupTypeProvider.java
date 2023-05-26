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

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.type.ChannelGroupType;
import org.openhab.core.thing.type.ChannelGroupTypeProvider;

/**
 * The ThinqChannelGroupTypeProvider interface.
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public interface ThinqChannelGroupTypeProvider extends ChannelGroupTypeProvider {

    public void addChannelGroupType(ChannelGroupType channelGroupType);

    public void removeChannelGroupType(ChannelGroupType channelGroupType);

    public List<ChannelGroupType> internalGroupTypes();
}
