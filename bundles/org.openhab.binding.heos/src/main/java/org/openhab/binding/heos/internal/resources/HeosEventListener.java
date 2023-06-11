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
package org.openhab.binding.heos.internal.resources;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.heos.internal.api.HeosEventController;
import org.openhab.binding.heos.internal.exception.HeosFunctionalException;
import org.openhab.binding.heos.internal.json.dto.HeosEventObject;
import org.openhab.binding.heos.internal.json.dto.HeosResponseObject;

/**
 * The {@link HeosEventListener } is an Event Listener
 * for the HEOS network. Handler which wants the get informed
 * by an HEOS event via the {@link HeosEventController} has to
 * implement this class and register itself at the {@link HeosEventController}
 *
 * @author Johannes Einig - Initial contribution
 * @author Martin van Wingerden - change handling of stop/pause depending on playing item type
 */
@NonNullByDefault
public interface HeosEventListener extends HeosMediaEventListener {

    void playerStateChangeEvent(HeosEventObject eventObject);

    void playerStateChangeEvent(HeosResponseObject<?> responseObject) throws HeosFunctionalException;

    void bridgeChangeEvent(String event, boolean success, Object command);
}
