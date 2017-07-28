/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.supla.internal.api;

import org.openhab.binding.supla.internal.supla.entities.SuplaChannel;
import org.openhab.binding.supla.internal.supla.entities.SuplaChannelStatus;

import java.util.Optional;

/**
 * ChannelManager allows to change state of channels on Supla Cloud
 *
 * @author Martin Grzeslowski - Initial contribution
 */
public interface ChannelManager {
    boolean turnOn(SuplaChannel channel);

    boolean turnOff(SuplaChannel channel);

    Optional<SuplaChannelStatus> obtainChannelStatus(SuplaChannel channel);
}
