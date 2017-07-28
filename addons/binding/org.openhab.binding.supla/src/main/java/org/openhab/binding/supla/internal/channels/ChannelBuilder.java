/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.supla.internal.channels;

import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.supla.internal.supla.entities.SuplaChannel;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * @author Martin Grzeslowski - Initial contribution
 */
public interface ChannelBuilder {
    Map<Channel, SuplaChannel> buildChannels(ThingUID thing, Collection<SuplaChannel> channel);

    Optional<Map.Entry<Channel, SuplaChannel>> buildChannel(ThingUID thing, SuplaChannel channel);
}
