/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.dirigera.internal.interfaces;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.json.JSONObject;
import org.openhab.core.types.State;

/**
 * The {@link DirigeraAPI} Gateway interface
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public interface DirigeraAPI {
    public JSONObject readHome();

    public JSONObject readDevice(String deviceId);

    public JSONObject readScene(String deviceId);

    public void triggerScene(String sceneId, String trigger);

    public int sendPatch(String id, JSONObject attributes);

    public State getImage(String imageURL);
}
