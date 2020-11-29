/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.freeboxos.internal.api.player;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.ListResponse;
import org.openhab.binding.freeboxos.internal.api.Response;
import org.openhab.binding.freeboxos.internal.api.RestManager;
import org.openhab.binding.freeboxos.internal.api.login.Session.Permission;
import org.openhab.binding.freeboxos.internal.api.system.DeviceConfig;
import org.openhab.binding.freeboxos.internal.handler.ApiHandler;

/**
 * The {@link PlayerManager} is the Java class used to handle api requests
 * related to player
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class PlayerManager extends RestManager {
    private static String PLAYER_URL = "player/";

    public static Permission associatedPermission() {
        return Permission.PLAYER;
    }

    private final String baseUrl;
    private final String apiVersion;

    public PlayerManager(ApiHandler apiHandler, String baseUrl, String apiVersion) {
        super(apiHandler);
        this.baseUrl = baseUrl;
        this.apiVersion = apiVersion;
    }

    private String buildSubPath(int playerId, String path) {
        return String.format(PLAYER_URL + "%d%sv%s/%s", playerId, baseUrl, apiVersion, path);
    }

    public List<Player> getPlayers() throws FreeboxException {
        return apiHandler.getList(PLAYER_URL, PlayersResponse.class, true);
    }

    public PlayerStatus getPlayerStatus(int id) throws FreeboxException {
        return apiHandler.get(buildSubPath(id, "status"), PlayerResponse.class, true);
    }

    public DeviceConfig getConfig(int id) throws FreeboxException {
        return apiHandler.get(buildSubPath(id, "system"), PlayerConfigurationResponse.class, true);
    }

    public void reboot(int id) throws FreeboxException {
        apiHandler.post(buildSubPath(id, "system/reboot"), null);
    }

    // Response classes and validity evaluations
    private static class PlayersResponse extends ListResponse<Player> {
    }

    private static class PlayerResponse extends Response<PlayerStatus> {
    }

    private static class PlayerConfigurationResponse extends Response<DeviceConfig> {
    }
}
