/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
 * {@link DirigeraAPI} high level interface to communicate with the gateway. These are comfort functions fitting to the
 * needs of the handlers. Each function is synchronized so no parallel calls will be established towards gateway.
 * Rationale:
 * Several times seen that gateway goes into a "quite mode" during monkey testing. It's still accepting commands but no
 * more updates were received.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public interface DirigeraAPI {

    /** JSON key for error flag, value shall be boolean */
    static final String HTTP_ERROR_FLAG = "http-error-flag";

    /** JSON key for error flag, value shall be int */
    static final String HTTP_ERROR_STATUS = "http-error-status";

    /** JSON key for error message, value shall be String */
    static final String HTTP_ERROR_MESSAGE = "http-error-message";

    /**
     * Read complete home model.
     *
     * @return JSONObject with data. In case of error the JSONObject is filled with error data
     */
    JSONObject readHome();

    /**
     * Read all data for one specific deviceId.
     *
     * @param deviceId to query
     * @return JSONObject with data. In case of error the JSONObject is filled with error data
     */
    JSONObject readDevice(String deviceId);

    /**
     * Read all data for one specific scene.
     *
     * @param sceneId to query
     * @return JSONObject with data. In case of error the JSONObject is filled with error data
     */
    JSONObject readScene(String sceneId);

    /**
     * Read all data for one specific scene.
     *
     * @param sceneId to query
     * @param trigger to send
     * @return JSONObject with data. In case of error the JSONObject is filled with error data
     */
    void triggerScene(String sceneId, String trigger);

    /**
     * Send attributes to a device
     *
     * @param deviceId to update
     * @param attributes to send
     * @return Integer of http response status
     */
    int sendAttributes(String deviceId, JSONObject attributes);

    /**
     * Send patch with other data than attributes to a device
     *
     * @param deviceId to update
     * @param data to send
     * @return Integer of http response status
     */
    int sendPatch(String deviceId, JSONObject data);

    /**
     * Creating a scene from scene template for a click pattern of a controller
     *
     * @param uuid of the scene to be created
     * @param clickPattern which shall trigger the scene
     * @param controllerId which delivering the clickPattern
     * @return String uuid of the created scene
     */
    String createScene(String uuid, String clickPattern, String controllerId);

    /**
     * Delete scene of given uuid
     *
     * @param uuid of the scene to be deleted
     */
    void deleteScene(String uuid);

    /**
     * Get image from an url.
     *
     * @return RawType in case of successful call, UndefType.UNDEF in case of error
     */
    State getImage(String imageURL);
}
