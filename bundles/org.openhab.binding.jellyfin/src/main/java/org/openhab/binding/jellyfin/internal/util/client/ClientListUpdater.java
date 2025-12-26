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

import org.openhab.binding.jellyfin.internal.api.ApiClient;
import org.openhab.binding.jellyfin.internal.api.generated.current.SessionApi;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.SessionInfoDto;

/**
 * Utility for updating the Jellyfin client list based on active users.
 * 
 * <p>
 * This utility retrieves all active sessions from the Jellyfin server and filters
 * them to include only sessions belonging to the specified user IDs. By querying all
 * sessions in a single API call rather than per-user queries, this approach ensures
 * that all client devices are discovered reliably.
 *
 * @author Initial contribution by openHAB Community
 */
public final class ClientListUpdater {
    private ClientListUpdater() {
    }

    /**
     * Updates the given client map with sessions for the given user IDs.
     * 
     * <p>
     * This method retrieves all active sessions from the Jellyfin server using
     * {@code getSessions(null, ...)} and filters them to include only sessions
     * where the user ID matches one in the provided set. The client map is cleared
     * and repopulated with the filtered results.
     * 
     * <p>
     * <b>Implementation Note:</b> Using {@code null} for the userId parameter in
     * {@code getSessions()} retrieves all sessions, which is then filtered client-side.
     * This approach was found to be more reliable than querying sessions per user,
     * as per-user queries may not return all client devices in certain Jellyfin
     * configurations.
     *
     * @param apiClient the Jellyfin API client
     * @param userIds the set of user IDs to include
     * @param clientMap the map to update (will be cleared and repopulated)
     */
    public static void updateClients(ApiClient apiClient, Set<String> userIds, Map<String, SessionInfoDto> clientMap) {
        var sessionApi = new SessionApi(apiClient);
        clientMap.clear();
        try {
            List<SessionInfoDto> sessions = sessionApi.getSessions(null, null, null, null);
            for (SessionInfoDto session : sessions) {
                if (session.getUserId() != null && userIds.contains(session.getUserId().toString())) {
                    clientMap.put(session.getId(), session);
                }
            }
        } catch (Exception e) {
            // Log or handle as appropriate in the caller
        }
    }
}
