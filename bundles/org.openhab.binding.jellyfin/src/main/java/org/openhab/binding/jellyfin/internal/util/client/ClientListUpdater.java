/**
 * Copyright (c) 2010-2025 openHAB.org and the openHAB project authors.
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openhab.binding.jellyfin.internal.util.client;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.openhab.binding.jellyfin.internal.api.ApiClient;
import org.openhab.binding.jellyfin.internal.api.generated.current.SessionApi;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.SessionInfoDto;

/**
 * Utility for updating the Jellyfin client list based on active users.
 *
 * @author Initial contribution by openHAB Community
 */
public final class ClientListUpdater {
    private ClientListUpdater() {
    }

    /**
     * Updates the given client map with sessions for the given user IDs.
     * Only sessions associated with the provided user IDs are included.
     *
     * @param apiClient the Jellyfin API client
     * @param userIds the set of user IDs to include
     * @param clientMap the map to update (will be cleared and repopulated)
     */
    public static void updateClients(ApiClient apiClient, Set<String> userIds, Map<String, SessionInfoDto> clientMap) {
        var sessionApi = new SessionApi(apiClient);
        clientMap.clear();
        for (String userId : userIds) {
            try {
                List<SessionInfoDto> sessions = sessionApi.getSessions(UUID.fromString(userId), null, null, null);
                for (SessionInfoDto session : sessions) {
                    if (userIds.contains(session.getUserId().toString())) {
                        clientMap.put(session.getId(), session);
                    }
                }
            } catch (Exception e) {
                // Log or handle as appropriate in the caller
            }
        }
    }
}
