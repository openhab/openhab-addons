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
package org.openhab.binding.mpd.internal.handler;

import java.util.EventListener;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mpd.internal.protocol.MPDConnection;
import org.openhab.binding.mpd.internal.protocol.MPDSong;
import org.openhab.binding.mpd.internal.protocol.MPDStatus;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;

/**
 * Interface which has to be implemented by a class in order to get
 * updates from a {@link MPDConnection}
 *
 * @author Stefan Röllin - Initial contribution
 */
@NonNullByDefault
public interface MPDEventListener extends EventListener {

    void updateMPDSong(MPDSong song);

    void updateMPDStatus(MPDStatus status);

    void updateThingStatus(ThingStatus status, ThingStatusDetail statusDetail, @Nullable String description);
}
