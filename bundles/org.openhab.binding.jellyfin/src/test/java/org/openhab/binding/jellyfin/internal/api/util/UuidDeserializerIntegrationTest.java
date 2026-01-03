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
package org.openhab.binding.jellyfin.internal.api.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.openhab.binding.jellyfin.internal.api.ApiClient;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.UserDto;

import com.fasterxml.jackson.core.type.TypeReference;

/**
 * Integration test demonstrating the UUID deserialization fix with actual Jellyfin JSON data
 *
 * @author Patrik Gfeller - Initial contribution
 */
public class UuidDeserializerIntegrationTest {

    @Test
    public void testRealJellyfinUserResponseDeserialization() throws Exception {
        // This is the actual JSON response that was causing the deserialization error
        String jellyfinUsersResponse = """
                [
                    {
                        "Name": "admin",
                        "ServerId": "f7873c7a09f94f358321478d31cf3f97",
                        "Id": "05e66d53183c4be4986c18d7e12694be",
                        "HasPassword": true,
                        "HasConfiguredPassword": true,
                        "HasConfiguredEasyPassword": false,
                        "EnableAutoLogin": false,
                        "LastLoginDate": "2024-11-06T00:40:18.8487902Z",
                        "LastActivityDate": "2024-11-06T00:40:18.8487902Z",
                        "Configuration": {
                            "PlayDefaultAudioTrack": true,
                            "SubtitleLanguagePreference": "",
                            "DisplayMissingEpisodes": false,
                            "GroupedFolders": [],
                            "SubtitleMode": "Default",
                            "DisplayCollectionsView": false,
                            "EnableLocalPassword": false,
                            "OrderedViews": [],
                            "LatestItemsExcludes": [],
                            "MyMediaExcludes": [],
                            "HidePlayedInLatest": true,
                            "RememberAudioSelections": true,
                            "RememberSubtitleSelections": true,
                            "EnableNextEpisodeAutoPlay": true
                        },
                        "Policy": {
                            "IsAdministrator": true,
                            "IsHidden": true,
                            "EnableCollectionManagement": false,
                            "EnableSubtitleManagement": false,
                            "EnableLyricManagement": false,
                            "IsDisabled": false,
                            "BlockedTags": [],
                            "AllowedTags": [],
                            "EnableUserPreferenceAccess": true,
                            "AccessSchedules": [],
                            "BlockUnratedItems": [],
                            "EnableRemoteControlOfOtherUsers": true,
                            "EnableSharedDeviceControl": true,
                            "EnableRemoteAccess": true,
                            "EnableLiveTvManagement": true,
                            "EnableLiveTvAccess": true,
                            "EnableMediaPlayback": true,
                            "EnableAudioPlaybackTranscoding": true,
                            "EnableVideoPlaybackTranscoding": true,
                            "EnablePlaybackRemuxing": true,
                            "ForceRemoteSourceTranscoding": false,
                            "EnableContentDeletion": true,
                            "EnableContentDeletionFromFolders": [],
                            "EnableContentDownloading": true,
                            "EnableSyncTranscoding": true,
                            "EnableMediaConversion": true,
                            "EnabledDevices": [],
                            "EnableAllDevices": true,
                            "EnabledChannels": [],
                            "EnableAllChannels": true,
                            "EnabledFolders": [],
                            "EnableAllFolders": true,
                            "InvalidLoginAttemptCount": 0,
                            "LoginAttemptsBeforeLockout": -1,
                            "MaxActiveSessions": 0,
                            "EnablePublicSharing": true,
                            "BlockedMediaFolders": [],
                            "BlockedChannels": [],
                            "RemoteClientBitrateLimit": 0,
                            "AuthenticationProviderId": "Jellyfin.Server.Implementations.Users.DefaultAuthenticationProvider",
                            "PasswordResetProviderId": "Jellyfin.Server.Implementations.Users.DefaultPasswordResetProvider",
                            "SyncPlayAccess": "CreateAndJoinGroups"
                        }
                    },
                    {
                        "Name": "openhab",
                        "ServerId": "f7873c7a09f94f358321478d31cf3f97",
                        "Id": "cc89f0df805247a5aab1fe3d27b19183",
                        "HasPassword": true,
                        "HasConfiguredPassword": true,
                        "HasConfiguredEasyPassword": false,
                        "EnableAutoLogin": false,
                        "LastLoginDate": "2024-11-06T00:51:53.1062821Z",
                        "LastActivityDate": "2025-09-26T22:31:40.2025129Z",
                        "Configuration": {
                            "PlayDefaultAudioTrack": true,
                            "SubtitleLanguagePreference": "",
                            "DisplayMissingEpisodes": false,
                            "GroupedFolders": [],
                            "SubtitleMode": "Default",
                            "DisplayCollectionsView": false,
                            "EnableLocalPassword": false,
                            "OrderedViews": [],
                            "LatestItemsExcludes": [],
                            "MyMediaExcludes": [],
                            "HidePlayedInLatest": true,
                            "RememberAudioSelections": true,
                            "RememberSubtitleSelections": true,
                            "EnableNextEpisodeAutoPlay": true
                        },
                        "Policy": {
                            "IsAdministrator": false,
                            "IsHidden": true,
                            "EnableCollectionManagement": false,
                            "EnableSubtitleManagement": false,
                            "EnableLyricManagement": false,
                            "IsDisabled": false,
                            "BlockedTags": [],
                            "AllowedTags": [],
                            "EnableUserPreferenceAccess": true,
                            "AccessSchedules": [],
                            "BlockUnratedItems": [],
                            "EnableRemoteControlOfOtherUsers": true,
                            "EnableSharedDeviceControl": true,
                            "EnableRemoteAccess": true,
                            "EnableLiveTvManagement": true,
                            "EnableLiveTvAccess": true,
                            "EnableMediaPlayback": true,
                            "EnableAudioPlaybackTranscoding": true,
                            "EnableVideoPlaybackTranscoding": true,
                            "EnablePlaybackRemuxing": true,
                            "ForceRemoteSourceTranscoding": false,
                            "EnableContentDeletion": false,
                            "EnableContentDeletionFromFolders": [],
                            "EnableContentDownloading": true,
                            "EnableSyncTranscoding": true,
                            "EnableMediaConversion": true,
                            "EnabledDevices": [],
                            "EnableAllDevices": true,
                            "EnabledChannels": [],
                            "EnableAllChannels": false,
                            "EnabledFolders": [],
                            "EnableAllFolders": true,
                            "InvalidLoginAttemptCount": 0,
                            "LoginAttemptsBeforeLockout": -1,
                            "MaxActiveSessions": 0,
                            "EnablePublicSharing": true,
                            "BlockedMediaFolders": [],
                            "BlockedChannels": [],
                            "RemoteClientBitrateLimit": 0,
                            "AuthenticationProviderId": "Jellyfin.Server.Implementations.Users.DefaultAuthenticationProvider",
                            "PasswordResetProviderId": "Jellyfin.Server.Implementations.Users.DefaultPasswordResetProvider",
                            "SyncPlayAccess": "CreateAndJoinGroups"
                        }
                    }
                ]
                """;

        // Use the actual ApiClient ObjectMapper configuration (which now includes our custom UUID deserializer)
        var objectMapper = ApiClient.createDefaultObjectMapper();

        // This should now work without throwing the UUID deserialization error
        List<UserDto> users = objectMapper.readValue(jellyfinUsersResponse, new TypeReference<List<UserDto>>() {
        });

        assertNotNull(users);
        assertEquals(2, users.size());

        // Verify the first user
        UserDto adminUser = users.get(0);
        assertEquals("admin", adminUser.getName());
        assertEquals(UUID.fromString("05e66d53-183c-4be4-986c-18d7e12694be"), adminUser.getId());
        assertEquals("f7873c7a09f94f358321478d31cf3f97", adminUser.getServerId());

        // Verify the second user
        UserDto openhabUser = users.get(1);
        assertEquals("openhab", openhabUser.getName());
        assertEquals(UUID.fromString("cc89f0df-8052-47a5-aab1-fe3d27b19183"), openhabUser.getId());
        assertEquals("f7873c7a09f94f358321478d31cf3f97", openhabUser.getServerId());
    }
}
