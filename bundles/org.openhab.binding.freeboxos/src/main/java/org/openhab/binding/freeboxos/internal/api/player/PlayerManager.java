/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.FreeboxOsSession;
import org.openhab.binding.freeboxos.internal.api.ListResponse;
import org.openhab.binding.freeboxos.internal.api.Response;
import org.openhab.binding.freeboxos.internal.api.RestManager;
import org.openhab.binding.freeboxos.internal.api.login.Session.Permission;
import org.openhab.binding.freeboxos.internal.api.system.DeviceConfig;

/**
 * The {@link PlayerManager} is the Java class used to handle api requests
 * related to player
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class PlayerManager extends RestManager {
    private static String PLAYER_URL = "player";

    private final Map<Integer, String> subPaths = new HashMap<>();

    public PlayerManager(FreeboxOsSession session) throws FreeboxException {
        super(PLAYER_URL, session, Permission.PLAYER);
        getPlayers().forEach(player -> {
            subPaths.put(player.getId(), player.baseUrl());
        });
    }

    public List<Player> getPlayers() throws FreeboxException {
        return getList(PlayersResponse.class, true);
    }

    public PlayerStatus getPlayerStatus(int id) throws FreeboxException {
        return get(subPaths.get(id) + "status", PlayerResponse.class, true);
    }

    public DeviceConfig getConfig(int id) throws FreeboxException {
        return get(subPaths.get(id) + "system", PlayerConfigurationResponse.class, true);
    }

    public void reboot(int id) throws FreeboxException {
        post(subPaths.get(id) + "system/reboot");
    }

    // Response classes and validity evaluations
    private static class PlayersResponse extends ListResponse<Player> {
    }

    private static class PlayerResponse extends Response<PlayerStatus> {
    }

    private static class PlayerConfigurationResponse extends Response<DeviceConfig> {
    }
}
