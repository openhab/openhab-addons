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

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.Response;

/**
 * The {@link PlayerResponses} holds known responses for this API class
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class PlayerResponses {

    public static class PlayersResponse extends Response<List<Player>> {
    }

    public static class PlayerResponse extends Response<Player> {
    }

    public static class PlayerStatusResponse extends Response<PlayerStatus> {
    }

    public static class ConfigurationResponse extends Response<PlayerSystemConfiguration> {
    }

}
