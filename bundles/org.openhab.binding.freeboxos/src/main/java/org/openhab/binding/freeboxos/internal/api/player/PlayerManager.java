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
package org.openhab.binding.freeboxos.internal.api.player;

import static org.openhab.binding.freeboxos.internal.api.ApiConstants.*;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.UriBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.freeboxos.internal.api.ApiConstants.Permission;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.Response;
import org.openhab.binding.freeboxos.internal.rest.FreeboxOsSession;
import org.openhab.binding.freeboxos.internal.rest.ListableRest;

/**
 * The {@link PlayerManager} is the Java class used to handle api requests related to player
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class PlayerManager extends ListableRest<Player, PlayerManager.PlayerResponse> {
    public static class PlayerResponse extends Response<Player> {
    }

    private static class PlayerStatusResponse extends Response<PlayerStatus> {
    }

    private static class ConfigurationResponse extends Response<PlayerSystemConfiguration> {
    }

    private final Map<Integer, String> subPaths = new HashMap<>();

    public PlayerManager(FreeboxOsSession session) throws FreeboxException {
        super(session, Permission.PLAYER, PlayerResponse.class, session.getUriBuilder().path(PLAYER_SUB_PATH));
        getDevices().stream().filter(Player::isApiAvailable)
                .forEach(player -> subPaths.put(player.getId(), player.baseUrl()));
    }

    public PlayerStatus getPlayerStatus(int id) throws FreeboxException {
        PlayerStatus statusResponse = getSingle(PlayerStatusResponse.class, subPaths.get(id), STATUS_SUB_PATH);
        if (statusResponse != null) {
            return statusResponse;
        }
        throw new FreeboxException("Player status is null");
    }

    // The player API does not allow to directly request a given player like others api parts
    @Override
    public Player getDevice(int id) throws FreeboxException {
        return getDevices().stream().filter(player -> player.getId() == id).findFirst().orElse(null);
    }

    public PlayerSystemConfiguration getConfig(int id) throws FreeboxException {
        PlayerSystemConfiguration response = getSingle(ConfigurationResponse.class, subPaths.get(id), SYSTEM_SUB_PATH);
        // Modification temporaire en attendant de revenir dessus quand tout aura été remis à plat.
        // Vérifier que ceci fonctionne.
        // SystemConfig response = get(ConfigurationResponse.class, subPaths.get(id), SYSTEM_SUB_PATH);
        if (response != null) {
            return response;
        }
        throw new FreeboxException("Player config is null");
    }

    public void sendKey(String ip, String code, String key, boolean longPress, int count) {
        UriBuilder uriBuilder = UriBuilder.fromPath("pub").scheme("http").host(ip).path("remote_control");
        uriBuilder.queryParam("code", code).queryParam("key", key);
        if (longPress) {
            uriBuilder.queryParam("long", true);
        }
        if (count > 1) {
            uriBuilder.queryParam("repeat", count);
        }
        try {
            session.execute(uriBuilder.build(), HttpMethod.GET, GenericResponse.class, null);
        } catch (FreeboxException ignore) {
            // This call does not return anything, we can safely ignore
        }
    }

    public void reboot(int id) throws FreeboxException {
        post(subPaths.get(id), SYSTEM_SUB_PATH, REBOOT_SUB_PATH);
    }
}
