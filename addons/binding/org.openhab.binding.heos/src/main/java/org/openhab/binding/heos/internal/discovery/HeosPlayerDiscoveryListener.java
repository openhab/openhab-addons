/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.heos.internal.discovery;

import org.openhab.binding.heos.handler.HeosBridgeHandler;

/**
 * The {@link HeosPlayerDiscoveryListener } is an Event Listener
 * for the HEOS network. Handler which wants the get informed
 * if the player or groups within the HEOS network have changed has to
 * implement this class and register itself at the {@link HeosBridgeHandler}
 *
 * @author Johannes Einig - Initial contribution
 */
public interface HeosPlayerDiscoveryListener {
    void playerChanged();
}
