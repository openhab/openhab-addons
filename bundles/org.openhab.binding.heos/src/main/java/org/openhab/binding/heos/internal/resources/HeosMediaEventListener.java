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
package org.openhab.binding.heos.internal.resources;

import java.util.EventListener;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.heos.internal.api.HeosEventController;
import org.openhab.binding.heos.internal.handler.HeosBridgeHandler;
import org.openhab.binding.heos.internal.json.payload.Media;

/**
 * The {@link HeosMediaEventListener } is a dedicated Event Listener
 * for the HEOS media events. Handler which wants the get informed
 * by an HEOS media event via the {@link HeosEventController} has to
 * implement this class and register itself either at the
 * {@link HeosEventController} or at {@link HeosBridgeHandler}
 *
 * @author Martin van Wingerden - Initial contribution
 */
@NonNullByDefault
public interface HeosMediaEventListener extends EventListener {

    void playerMediaChangeEvent(String pid, Media media);
}
