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
package org.openhab.binding.dbquery.internal;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.Channel;

/**
 * Abstract the action to get channels that need to be updated
 *
 * @author Joan Pujol - Initial contribution
 */
@NonNullByDefault
public interface ChannelsToUpdateQueryResult {
    List<Channel> getChannels();
}
