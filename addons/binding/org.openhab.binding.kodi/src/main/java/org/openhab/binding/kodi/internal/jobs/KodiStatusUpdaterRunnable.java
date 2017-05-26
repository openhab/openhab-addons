/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.kodi.internal.jobs;

import org.openhab.binding.kodi.internal.KodiEventListener.KodiState;
import org.openhab.binding.kodi.internal.protocol.KodiConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link KodiStatusUpdaterRunnable} is responsible for updating the player
 * status while playing.
 *
 * @author Christoph Weitkamp - Added channels for opening PVR TV or Radio streams
 *
 */
public class KodiStatusUpdaterRunnable implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(KodiStatusUpdaterRunnable.class);

    private final KodiConnection connection;

    public KodiStatusUpdaterRunnable(final KodiConnection connection) {
        this.connection = connection;
    }

    @Override
    public void run() {
        if (this.connection.getState().equals(KodiState.Play)) {
            this.connection.updatePlayerStatus();
        }
    }

}
