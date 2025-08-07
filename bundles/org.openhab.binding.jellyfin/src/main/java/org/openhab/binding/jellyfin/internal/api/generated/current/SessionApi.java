package org.openhab.binding.jellyfin.internal.api.generated.current;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.core.GenericType;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;
import org.openhab.binding.jellyfin.internal.api.generated.ApiException;
import org.openhab.binding.jellyfin.internal.api.generated.ApiResponse;
import org.openhab.binding.jellyfin.internal.api.generated.Configuration;
import org.openhab.binding.jellyfin.internal.api.generated.Pair;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.BaseItemKind;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.ClientCapabilitiesDto;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.GeneralCommand;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.GeneralCommandType;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.MediaType;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.MessageCommand;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.NameIdPair;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.PlayCommand;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.PlaystateCommand;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.SessionInfoDto;

public class SessionApi {
    private ApiClient apiClient;

    public SessionApi() {
        this(Configuration.getDefaultApiClient());
    }

    public SessionApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Get the API client
     *
     * @return API client
     */
    public ApiClient getApiClient() {
        return apiClient;
    }

    /**
     * Set the API client
     *
     * @param apiClient an instance of API client
     */
    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Adds an additional user to a session.
     * 
     * @param sessionId The session id. (required)
     * @param userId The user id. (required)
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>204</td>
     *                        <td>User added to session.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public void addUserToSession(@org.eclipse.jdt.annotation.Nullable String sessionId,
            @org.eclipse.jdt.annotation.Nullable UUID userId) throws ApiException {
        addUserToSessionWithHttpInfo(sessionId, userId);
    }

    /**
     * Adds an additional user to a session.
     * 
     * @param sessionId The session id. (required)
     * @param userId The user id. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>204</td>
     *                        <td>User added to session.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<Void> addUserToSessionWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String sessionId,
            @org.eclipse.jdt.annotation.Nullable UUID userId) throws ApiException {
        // Check required parameters
        if (sessionId == null) {
            throw new ApiException(400, "Missing the required parameter 'sessionId' when calling addUserToSession");
        }
        if (userId == null) {
            throw new ApiException(400, "Missing the required parameter 'userId' when calling addUserToSession");
        }

        // Path parameters
        String localVarPath = "/Sessions/{sessionId}/User/{userId}"
                .replaceAll("\\{sessionId}", apiClient.escapeString(sessionId.toString()))
                .replaceAll("\\{userId}", apiClient.escapeString(userId.toString()));

        String localVarAccept = apiClient.selectHeaderAccept();
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        return apiClient.invokeAPI("SessionApi.addUserToSession", localVarPath, "POST", new ArrayList<>(), null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, null, false);
    }

    /**
     * Instructs a session to browse to an item or view.
     * 
     * @param sessionId The session Id. (required)
     * @param itemType The type of item to browse to. (required)
     * @param itemId The Id of the item. (required)
     * @param itemName The name of the item. (required)
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>204</td>
     *                        <td>Instruction sent to session.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public void displayContent(@org.eclipse.jdt.annotation.Nullable String sessionId,
            @org.eclipse.jdt.annotation.Nullable BaseItemKind itemType,
            @org.eclipse.jdt.annotation.Nullable String itemId, @org.eclipse.jdt.annotation.Nullable String itemName)
            throws ApiException {
        displayContentWithHttpInfo(sessionId, itemType, itemId, itemName);
    }

    /**
     * Instructs a session to browse to an item or view.
     * 
     * @param sessionId The session Id. (required)
     * @param itemType The type of item to browse to. (required)
     * @param itemId The Id of the item. (required)
     * @param itemName The name of the item. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>204</td>
     *                        <td>Instruction sent to session.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<Void> displayContentWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String sessionId,
            @org.eclipse.jdt.annotation.Nullable BaseItemKind itemType,
            @org.eclipse.jdt.annotation.Nullable String itemId, @org.eclipse.jdt.annotation.Nullable String itemName)
            throws ApiException {
        // Check required parameters
        if (sessionId == null) {
            throw new ApiException(400, "Missing the required parameter 'sessionId' when calling displayContent");
        }
        if (itemType == null) {
            throw new ApiException(400, "Missing the required parameter 'itemType' when calling displayContent");
        }
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling displayContent");
        }
        if (itemName == null) {
            throw new ApiException(400, "Missing the required parameter 'itemName' when calling displayContent");
        }

        // Path parameters
        String localVarPath = "/Sessions/{sessionId}/Viewing".replaceAll("\\{sessionId}",
                apiClient.escapeString(sessionId.toString()));

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "itemType", itemType));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "itemId", itemId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "itemName", itemName));

        String localVarAccept = apiClient.selectHeaderAccept();
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        return apiClient.invokeAPI("SessionApi.displayContent", localVarPath, "POST", localVarQueryParams, null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, null, false);
    }

    /**
     * Get all auth providers.
     * 
     * @return List&lt;NameIdPair&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Auth providers retrieved.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public List<NameIdPair> getAuthProviders() throws ApiException {
        return getAuthProvidersWithHttpInfo().getData();
    }

    /**
     * Get all auth providers.
     * 
     * @return ApiResponse&lt;List&lt;NameIdPair&gt;&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Auth providers retrieved.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<List<NameIdPair>> getAuthProvidersWithHttpInfo() throws ApiException {
        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<List<NameIdPair>> localVarReturnType = new GenericType<List<NameIdPair>>() {
        };
        return apiClient.invokeAPI("SessionApi.getAuthProviders", "/Auth/Providers", "GET", new ArrayList<>(), null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, localVarReturnType, false);
    }

    /**
     * Get all password reset providers.
     * 
     * @return List&lt;NameIdPair&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Password reset providers retrieved.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public List<NameIdPair> getPasswordResetProviders() throws ApiException {
        return getPasswordResetProvidersWithHttpInfo().getData();
    }

    /**
     * Get all password reset providers.
     * 
     * @return ApiResponse&lt;List&lt;NameIdPair&gt;&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Password reset providers retrieved.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<List<NameIdPair>> getPasswordResetProvidersWithHttpInfo() throws ApiException {
        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<List<NameIdPair>> localVarReturnType = new GenericType<List<NameIdPair>>() {
        };
        return apiClient.invokeAPI("SessionApi.getPasswordResetProviders", "/Auth/PasswordResetProviders", "GET",
                new ArrayList<>(), null, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(),
                localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType, false);
    }

    /**
     * Gets a list of sessions.
     * 
     * @param controllableByUserId Filter by sessions that a given user is allowed to remote control. (optional)
     * @param deviceId Filter by device Id. (optional)
     * @param activeWithinSeconds Optional. Filter by sessions that were active in the last n seconds. (optional)
     * @return List&lt;SessionInfoDto&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>List of sessions returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public List<SessionInfoDto> getSessions(@org.eclipse.jdt.annotation.NonNull UUID controllableByUserId,
            @org.eclipse.jdt.annotation.NonNull String deviceId,
            @org.eclipse.jdt.annotation.NonNull Integer activeWithinSeconds) throws ApiException {
        return getSessionsWithHttpInfo(controllableByUserId, deviceId, activeWithinSeconds).getData();
    }

    /**
     * Gets a list of sessions.
     * 
     * @param controllableByUserId Filter by sessions that a given user is allowed to remote control. (optional)
     * @param deviceId Filter by device Id. (optional)
     * @param activeWithinSeconds Optional. Filter by sessions that were active in the last n seconds. (optional)
     * @return ApiResponse&lt;List&lt;SessionInfoDto&gt;&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>List of sessions returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<List<SessionInfoDto>> getSessionsWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull UUID controllableByUserId,
            @org.eclipse.jdt.annotation.NonNull String deviceId,
            @org.eclipse.jdt.annotation.NonNull Integer activeWithinSeconds) throws ApiException {
        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(
                apiClient.parameterToPairs("", "controllableByUserId", controllableByUserId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "deviceId", deviceId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "activeWithinSeconds", activeWithinSeconds));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<List<SessionInfoDto>> localVarReturnType = new GenericType<List<SessionInfoDto>>() {
        };
        return apiClient.invokeAPI("SessionApi.getSessions", "/Sessions", "GET", localVarQueryParams, null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, localVarReturnType, false);
    }

    /**
     * Instructs a session to play an item.
     * 
     * @param sessionId The session id. (required)
     * @param playCommand The type of play command to issue (PlayNow, PlayNext, PlayLast). Clients who have not yet
     *            implemented play next and play last may play now. (required)
     * @param itemIds The ids of the items to play, comma delimited. (required)
     * @param startPositionTicks The starting position of the first item. (optional)
     * @param mediaSourceId Optional. The media source id. (optional)
     * @param audioStreamIndex Optional. The index of the audio stream to play. (optional)
     * @param subtitleStreamIndex Optional. The index of the subtitle stream to play. (optional)
     * @param startIndex Optional. The start index. (optional)
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>204</td>
     *                        <td>Instruction sent to session.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public void play(@org.eclipse.jdt.annotation.Nullable String sessionId,
            @org.eclipse.jdt.annotation.Nullable PlayCommand playCommand,
            @org.eclipse.jdt.annotation.Nullable List<UUID> itemIds,
            @org.eclipse.jdt.annotation.NonNull Long startPositionTicks,
            @org.eclipse.jdt.annotation.NonNull String mediaSourceId,
            @org.eclipse.jdt.annotation.NonNull Integer audioStreamIndex,
            @org.eclipse.jdt.annotation.NonNull Integer subtitleStreamIndex,
            @org.eclipse.jdt.annotation.NonNull Integer startIndex) throws ApiException {
        playWithHttpInfo(sessionId, playCommand, itemIds, startPositionTicks, mediaSourceId, audioStreamIndex,
                subtitleStreamIndex, startIndex);
    }

    /**
     * Instructs a session to play an item.
     * 
     * @param sessionId The session id. (required)
     * @param playCommand The type of play command to issue (PlayNow, PlayNext, PlayLast). Clients who have not yet
     *            implemented play next and play last may play now. (required)
     * @param itemIds The ids of the items to play, comma delimited. (required)
     * @param startPositionTicks The starting position of the first item. (optional)
     * @param mediaSourceId Optional. The media source id. (optional)
     * @param audioStreamIndex Optional. The index of the audio stream to play. (optional)
     * @param subtitleStreamIndex Optional. The index of the subtitle stream to play. (optional)
     * @param startIndex Optional. The start index. (optional)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>204</td>
     *                        <td>Instruction sent to session.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<Void> playWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String sessionId,
            @org.eclipse.jdt.annotation.Nullable PlayCommand playCommand,
            @org.eclipse.jdt.annotation.Nullable List<UUID> itemIds,
            @org.eclipse.jdt.annotation.NonNull Long startPositionTicks,
            @org.eclipse.jdt.annotation.NonNull String mediaSourceId,
            @org.eclipse.jdt.annotation.NonNull Integer audioStreamIndex,
            @org.eclipse.jdt.annotation.NonNull Integer subtitleStreamIndex,
            @org.eclipse.jdt.annotation.NonNull Integer startIndex) throws ApiException {
        // Check required parameters
        if (sessionId == null) {
            throw new ApiException(400, "Missing the required parameter 'sessionId' when calling play");
        }
        if (playCommand == null) {
            throw new ApiException(400, "Missing the required parameter 'playCommand' when calling play");
        }
        if (itemIds == null) {
            throw new ApiException(400, "Missing the required parameter 'itemIds' when calling play");
        }

        // Path parameters
        String localVarPath = "/Sessions/{sessionId}/Playing".replaceAll("\\{sessionId}",
                apiClient.escapeString(sessionId.toString()));

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "playCommand", playCommand));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "itemIds", itemIds));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "startPositionTicks", startPositionTicks));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "mediaSourceId", mediaSourceId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "audioStreamIndex", audioStreamIndex));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "subtitleStreamIndex", subtitleStreamIndex));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "startIndex", startIndex));

        String localVarAccept = apiClient.selectHeaderAccept();
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        return apiClient.invokeAPI("SessionApi.play", localVarPath, "POST", localVarQueryParams, null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, null, false);
    }

    /**
     * Updates capabilities for a device.
     * 
     * @param id The session id. (optional)
     * @param playableMediaTypes A list of playable media types, comma delimited. Audio, Video, Book, Photo. (optional)
     * @param supportedCommands A list of supported remote control commands, comma delimited. (optional)
     * @param supportsMediaControl Determines whether media can be played remotely.. (optional, default to false)
     * @param supportsPersistentIdentifier Determines whether the device supports a unique identifier. (optional,
     *            default to true)
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>204</td>
     *                        <td>Capabilities posted.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public void postCapabilities(@org.eclipse.jdt.annotation.NonNull String id,
            @org.eclipse.jdt.annotation.NonNull List<MediaType> playableMediaTypes,
            @org.eclipse.jdt.annotation.NonNull List<GeneralCommandType> supportedCommands,
            @org.eclipse.jdt.annotation.NonNull Boolean supportsMediaControl,
            @org.eclipse.jdt.annotation.NonNull Boolean supportsPersistentIdentifier) throws ApiException {
        postCapabilitiesWithHttpInfo(id, playableMediaTypes, supportedCommands, supportsMediaControl,
                supportsPersistentIdentifier);
    }

    /**
     * Updates capabilities for a device.
     * 
     * @param id The session id. (optional)
     * @param playableMediaTypes A list of playable media types, comma delimited. Audio, Video, Book, Photo. (optional)
     * @param supportedCommands A list of supported remote control commands, comma delimited. (optional)
     * @param supportsMediaControl Determines whether media can be played remotely.. (optional, default to false)
     * @param supportsPersistentIdentifier Determines whether the device supports a unique identifier. (optional,
     *            default to true)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>204</td>
     *                        <td>Capabilities posted.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<Void> postCapabilitiesWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String id,
            @org.eclipse.jdt.annotation.NonNull List<MediaType> playableMediaTypes,
            @org.eclipse.jdt.annotation.NonNull List<GeneralCommandType> supportedCommands,
            @org.eclipse.jdt.annotation.NonNull Boolean supportsMediaControl,
            @org.eclipse.jdt.annotation.NonNull Boolean supportsPersistentIdentifier) throws ApiException {
        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "id", id));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "playableMediaTypes", playableMediaTypes));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "supportedCommands", supportedCommands));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "supportsMediaControl", supportsMediaControl));
        localVarQueryParams
                .addAll(apiClient.parameterToPairs("", "supportsPersistentIdentifier", supportsPersistentIdentifier));

        String localVarAccept = apiClient.selectHeaderAccept();
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        return apiClient.invokeAPI("SessionApi.postCapabilities", "/Sessions/Capabilities", "POST", localVarQueryParams,
                null, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, null, false);
    }

    /**
     * Updates capabilities for a device.
     * 
     * @param clientCapabilitiesDto The MediaBrowser.Model.Session.ClientCapabilities. (required)
     * @param id The session id. (optional)
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>204</td>
     *                        <td>Capabilities updated.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public void postFullCapabilities(@org.eclipse.jdt.annotation.Nullable ClientCapabilitiesDto clientCapabilitiesDto,
            @org.eclipse.jdt.annotation.NonNull String id) throws ApiException {
        postFullCapabilitiesWithHttpInfo(clientCapabilitiesDto, id);
    }

    /**
     * Updates capabilities for a device.
     * 
     * @param clientCapabilitiesDto The MediaBrowser.Model.Session.ClientCapabilities. (required)
     * @param id The session id. (optional)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>204</td>
     *                        <td>Capabilities updated.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<Void> postFullCapabilitiesWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable ClientCapabilitiesDto clientCapabilitiesDto,
            @org.eclipse.jdt.annotation.NonNull String id) throws ApiException {
        // Check required parameters
        if (clientCapabilitiesDto == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'clientCapabilitiesDto' when calling postFullCapabilities");
        }

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "id", id));

        String localVarAccept = apiClient.selectHeaderAccept();
        String localVarContentType = apiClient.selectHeaderContentType("application/json", "text/json",
                "application/*+json");
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        return apiClient.invokeAPI("SessionApi.postFullCapabilities", "/Sessions/Capabilities/Full", "POST",
                localVarQueryParams, clientCapabilitiesDto, new LinkedHashMap<>(), new LinkedHashMap<>(),
                new LinkedHashMap<>(), localVarAccept, localVarContentType, localVarAuthNames, null, false);
    }

    /**
     * Removes an additional user from a session.
     * 
     * @param sessionId The session id. (required)
     * @param userId The user id. (required)
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>204</td>
     *                        <td>User removed from session.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public void removeUserFromSession(@org.eclipse.jdt.annotation.Nullable String sessionId,
            @org.eclipse.jdt.annotation.Nullable UUID userId) throws ApiException {
        removeUserFromSessionWithHttpInfo(sessionId, userId);
    }

    /**
     * Removes an additional user from a session.
     * 
     * @param sessionId The session id. (required)
     * @param userId The user id. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>204</td>
     *                        <td>User removed from session.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<Void> removeUserFromSessionWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String sessionId,
            @org.eclipse.jdt.annotation.Nullable UUID userId) throws ApiException {
        // Check required parameters
        if (sessionId == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'sessionId' when calling removeUserFromSession");
        }
        if (userId == null) {
            throw new ApiException(400, "Missing the required parameter 'userId' when calling removeUserFromSession");
        }

        // Path parameters
        String localVarPath = "/Sessions/{sessionId}/User/{userId}"
                .replaceAll("\\{sessionId}", apiClient.escapeString(sessionId.toString()))
                .replaceAll("\\{userId}", apiClient.escapeString(userId.toString()));

        String localVarAccept = apiClient.selectHeaderAccept();
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        return apiClient.invokeAPI("SessionApi.removeUserFromSession", localVarPath, "DELETE", new ArrayList<>(), null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, null, false);
    }

    /**
     * Reports that a session has ended.
     * 
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>204</td>
     *                        <td>Session end reported to server.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public void reportSessionEnded() throws ApiException {
        reportSessionEndedWithHttpInfo();
    }

    /**
     * Reports that a session has ended.
     * 
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>204</td>
     *                        <td>Session end reported to server.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<Void> reportSessionEndedWithHttpInfo() throws ApiException {
        String localVarAccept = apiClient.selectHeaderAccept();
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        return apiClient.invokeAPI("SessionApi.reportSessionEnded", "/Sessions/Logout", "POST", new ArrayList<>(), null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, null, false);
    }

    /**
     * Reports that a session is viewing an item.
     * 
     * @param itemId The item id. (required)
     * @param sessionId The session id. (optional)
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>204</td>
     *                        <td>Session reported to server.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public void reportViewing(@org.eclipse.jdt.annotation.Nullable String itemId,
            @org.eclipse.jdt.annotation.NonNull String sessionId) throws ApiException {
        reportViewingWithHttpInfo(itemId, sessionId);
    }

    /**
     * Reports that a session is viewing an item.
     * 
     * @param itemId The item id. (required)
     * @param sessionId The session id. (optional)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>204</td>
     *                        <td>Session reported to server.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<Void> reportViewingWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String itemId,
            @org.eclipse.jdt.annotation.NonNull String sessionId) throws ApiException {
        // Check required parameters
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling reportViewing");
        }

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "sessionId", sessionId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "itemId", itemId));

        String localVarAccept = apiClient.selectHeaderAccept();
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        return apiClient.invokeAPI("SessionApi.reportViewing", "/Sessions/Viewing", "POST", localVarQueryParams, null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, null, false);
    }

    /**
     * Issues a full general command to a client.
     * 
     * @param sessionId The session id. (required)
     * @param generalCommand The MediaBrowser.Model.Session.GeneralCommand. (required)
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>204</td>
     *                        <td>Full general command sent to session.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public void sendFullGeneralCommand(@org.eclipse.jdt.annotation.Nullable String sessionId,
            @org.eclipse.jdt.annotation.Nullable GeneralCommand generalCommand) throws ApiException {
        sendFullGeneralCommandWithHttpInfo(sessionId, generalCommand);
    }

    /**
     * Issues a full general command to a client.
     * 
     * @param sessionId The session id. (required)
     * @param generalCommand The MediaBrowser.Model.Session.GeneralCommand. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>204</td>
     *                        <td>Full general command sent to session.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<Void> sendFullGeneralCommandWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String sessionId,
            @org.eclipse.jdt.annotation.Nullable GeneralCommand generalCommand) throws ApiException {
        // Check required parameters
        if (sessionId == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'sessionId' when calling sendFullGeneralCommand");
        }
        if (generalCommand == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'generalCommand' when calling sendFullGeneralCommand");
        }

        // Path parameters
        String localVarPath = "/Sessions/{sessionId}/Command".replaceAll("\\{sessionId}",
                apiClient.escapeString(sessionId.toString()));

        String localVarAccept = apiClient.selectHeaderAccept();
        String localVarContentType = apiClient.selectHeaderContentType("application/json", "text/json",
                "application/*+json");
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        return apiClient.invokeAPI("SessionApi.sendFullGeneralCommand", localVarPath, "POST", new ArrayList<>(),
                generalCommand, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, null, false);
    }

    /**
     * Issues a general command to a client.
     * 
     * @param sessionId The session id. (required)
     * @param command The command to send. (required)
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>204</td>
     *                        <td>General command sent to session.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public void sendGeneralCommand(@org.eclipse.jdt.annotation.Nullable String sessionId,
            @org.eclipse.jdt.annotation.Nullable GeneralCommandType command) throws ApiException {
        sendGeneralCommandWithHttpInfo(sessionId, command);
    }

    /**
     * Issues a general command to a client.
     * 
     * @param sessionId The session id. (required)
     * @param command The command to send. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>204</td>
     *                        <td>General command sent to session.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<Void> sendGeneralCommandWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String sessionId,
            @org.eclipse.jdt.annotation.Nullable GeneralCommandType command) throws ApiException {
        // Check required parameters
        if (sessionId == null) {
            throw new ApiException(400, "Missing the required parameter 'sessionId' when calling sendGeneralCommand");
        }
        if (command == null) {
            throw new ApiException(400, "Missing the required parameter 'command' when calling sendGeneralCommand");
        }

        // Path parameters
        String localVarPath = "/Sessions/{sessionId}/Command/{command}"
                .replaceAll("\\{sessionId}", apiClient.escapeString(sessionId.toString()))
                .replaceAll("\\{command}", apiClient.escapeString(command.toString()));

        String localVarAccept = apiClient.selectHeaderAccept();
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        return apiClient.invokeAPI("SessionApi.sendGeneralCommand", localVarPath, "POST", new ArrayList<>(), null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, null, false);
    }

    /**
     * Issues a command to a client to display a message to the user.
     * 
     * @param sessionId The session id. (required)
     * @param messageCommand The MediaBrowser.Model.Session.MessageCommand object containing Header, Message Text, and
     *            TimeoutMs. (required)
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>204</td>
     *                        <td>Message sent.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public void sendMessageCommand(@org.eclipse.jdt.annotation.Nullable String sessionId,
            @org.eclipse.jdt.annotation.Nullable MessageCommand messageCommand) throws ApiException {
        sendMessageCommandWithHttpInfo(sessionId, messageCommand);
    }

    /**
     * Issues a command to a client to display a message to the user.
     * 
     * @param sessionId The session id. (required)
     * @param messageCommand The MediaBrowser.Model.Session.MessageCommand object containing Header, Message Text, and
     *            TimeoutMs. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>204</td>
     *                        <td>Message sent.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<Void> sendMessageCommandWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String sessionId,
            @org.eclipse.jdt.annotation.Nullable MessageCommand messageCommand) throws ApiException {
        // Check required parameters
        if (sessionId == null) {
            throw new ApiException(400, "Missing the required parameter 'sessionId' when calling sendMessageCommand");
        }
        if (messageCommand == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'messageCommand' when calling sendMessageCommand");
        }

        // Path parameters
        String localVarPath = "/Sessions/{sessionId}/Message".replaceAll("\\{sessionId}",
                apiClient.escapeString(sessionId.toString()));

        String localVarAccept = apiClient.selectHeaderAccept();
        String localVarContentType = apiClient.selectHeaderContentType("application/json", "text/json",
                "application/*+json");
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        return apiClient.invokeAPI("SessionApi.sendMessageCommand", localVarPath, "POST", new ArrayList<>(),
                messageCommand, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, null, false);
    }

    /**
     * Issues a playstate command to a client.
     * 
     * @param sessionId The session id. (required)
     * @param command The MediaBrowser.Model.Session.PlaystateCommand. (required)
     * @param seekPositionTicks The optional position ticks. (optional)
     * @param controllingUserId The optional controlling user id. (optional)
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>204</td>
     *                        <td>Playstate command sent to session.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public void sendPlaystateCommand(@org.eclipse.jdt.annotation.Nullable String sessionId,
            @org.eclipse.jdt.annotation.Nullable PlaystateCommand command,
            @org.eclipse.jdt.annotation.NonNull Long seekPositionTicks,
            @org.eclipse.jdt.annotation.NonNull String controllingUserId) throws ApiException {
        sendPlaystateCommandWithHttpInfo(sessionId, command, seekPositionTicks, controllingUserId);
    }

    /**
     * Issues a playstate command to a client.
     * 
     * @param sessionId The session id. (required)
     * @param command The MediaBrowser.Model.Session.PlaystateCommand. (required)
     * @param seekPositionTicks The optional position ticks. (optional)
     * @param controllingUserId The optional controlling user id. (optional)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>204</td>
     *                        <td>Playstate command sent to session.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<Void> sendPlaystateCommandWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String sessionId,
            @org.eclipse.jdt.annotation.Nullable PlaystateCommand command,
            @org.eclipse.jdt.annotation.NonNull Long seekPositionTicks,
            @org.eclipse.jdt.annotation.NonNull String controllingUserId) throws ApiException {
        // Check required parameters
        if (sessionId == null) {
            throw new ApiException(400, "Missing the required parameter 'sessionId' when calling sendPlaystateCommand");
        }
        if (command == null) {
            throw new ApiException(400, "Missing the required parameter 'command' when calling sendPlaystateCommand");
        }

        // Path parameters
        String localVarPath = "/Sessions/{sessionId}/Playing/{command}"
                .replaceAll("\\{sessionId}", apiClient.escapeString(sessionId.toString()))
                .replaceAll("\\{command}", apiClient.escapeString(command.toString()));

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(
                apiClient.parameterToPairs("", "seekPositionTicks", seekPositionTicks));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "controllingUserId", controllingUserId));

        String localVarAccept = apiClient.selectHeaderAccept();
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        return apiClient.invokeAPI("SessionApi.sendPlaystateCommand", localVarPath, "POST", localVarQueryParams, null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, null, false);
    }

    /**
     * Issues a system command to a client.
     * 
     * @param sessionId The session id. (required)
     * @param command The command to send. (required)
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>204</td>
     *                        <td>System command sent to session.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public void sendSystemCommand(@org.eclipse.jdt.annotation.Nullable String sessionId,
            @org.eclipse.jdt.annotation.Nullable GeneralCommandType command) throws ApiException {
        sendSystemCommandWithHttpInfo(sessionId, command);
    }

    /**
     * Issues a system command to a client.
     * 
     * @param sessionId The session id. (required)
     * @param command The command to send. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>204</td>
     *                        <td>System command sent to session.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<Void> sendSystemCommandWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String sessionId,
            @org.eclipse.jdt.annotation.Nullable GeneralCommandType command) throws ApiException {
        // Check required parameters
        if (sessionId == null) {
            throw new ApiException(400, "Missing the required parameter 'sessionId' when calling sendSystemCommand");
        }
        if (command == null) {
            throw new ApiException(400, "Missing the required parameter 'command' when calling sendSystemCommand");
        }

        // Path parameters
        String localVarPath = "/Sessions/{sessionId}/System/{command}"
                .replaceAll("\\{sessionId}", apiClient.escapeString(sessionId.toString()))
                .replaceAll("\\{command}", apiClient.escapeString(command.toString()));

        String localVarAccept = apiClient.selectHeaderAccept();
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        return apiClient.invokeAPI("SessionApi.sendSystemCommand", localVarPath, "POST", new ArrayList<>(), null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, null, false);
    }
}
