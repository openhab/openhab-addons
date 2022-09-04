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
package org.openhab.binding.hdpowerview.internal._v3;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.hdpowerview.internal.api.ShadePosition;

/**
 * Interface for receiving SSE events from Generation 3 hubs.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public interface SseSinkV3 {

    /**
     * Method that is called when a shade changes state.
     *
     * @param evt contents of json evt element.
     * @param shadeId the id of the shade that changed.
     * @param shadePosition the shade's new position.
     */
    public void sseShade(String evt, int shadeId, ShadePosition shadePosition);

    /**
     * Method that is called when a scene changes state.
     *
     * @param evt contents of json 'evt' element.
     * @param sceneId the id of the scene that changed.
     */
    public void sseScene(String evt, int sceneId);
}
