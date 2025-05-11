package org.openhab.binding.jellyfin.internal.api.version.legacy;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.openhab.binding.jellyfin.internal.api.version.ApiClient;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.BaseItemKind;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.ClientCapabilitiesDto;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.GeneralCommand;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.GeneralCommandType;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.MessageCommand;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.NameIdPair;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.PlayCommand;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.PlaystateCommand;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.SessionInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.12.0")
public class SessionApi {
    private ApiClient apiClient;

    public SessionApi() {
        this(new ApiClient());
    }

    @Autowired
    public SessionApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Adds an additional user to a session.
     * 
     * <p>
     * <b>204</b> - User added to session.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param sessionId The session id.
     * @param userId The user id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec addUserToSessionRequestCreation(String sessionId, UUID userId)
            throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'sessionId' is set
        if (sessionId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'sessionId' when calling addUserToSession",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'userId' is set
        if (userId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'userId' when calling addUserToSession",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("sessionId", sessionId);
        pathParams.put("userId", userId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = {};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return apiClient.invokeAPI("/Sessions/{sessionId}/User/{userId}", HttpMethod.POST, pathParams, queryParams,
                postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType,
                localVarAuthNames, localVarReturnType);
    }

    /**
     * Adds an additional user to a session.
     * 
     * <p>
     * <b>204</b> - User added to session.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param sessionId The session id.
     * @param userId The user id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> addUserToSession(String sessionId, UUID userId) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return addUserToSessionRequestCreation(sessionId, userId).bodyToMono(localVarReturnType);
    }

    /**
     * Adds an additional user to a session.
     * 
     * <p>
     * <b>204</b> - User added to session.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param sessionId The session id.
     * @param userId The user id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> addUserToSessionWithHttpInfo(String sessionId, UUID userId)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return addUserToSessionRequestCreation(sessionId, userId).toEntity(localVarReturnType);
    }

    /**
     * Adds an additional user to a session.
     * 
     * <p>
     * <b>204</b> - User added to session.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param sessionId The session id.
     * @param userId The user id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec addUserToSessionWithResponseSpec(String sessionId, UUID userId)
            throws WebClientResponseException {
        return addUserToSessionRequestCreation(sessionId, userId);
    }

    /**
     * Instructs a session to browse to an item or view.
     * 
     * <p>
     * <b>204</b> - Instruction sent to session.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param sessionId The session Id.
     * @param itemType The type of item to browse to.
     * @param itemId The Id of the item.
     * @param itemName The name of the item.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec displayContentRequestCreation(String sessionId, BaseItemKind itemType, String itemId,
            String itemName) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'sessionId' is set
        if (sessionId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'sessionId' when calling displayContent",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'itemType' is set
        if (itemType == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'itemType' when calling displayContent",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException("Missing the required parameter 'itemId' when calling displayContent",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'itemName' is set
        if (itemName == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'itemName' when calling displayContent",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("sessionId", sessionId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "itemType", itemType));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "itemId", itemId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "itemName", itemName));

        final String[] localVarAccepts = {};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return apiClient.invokeAPI("/Sessions/{sessionId}/Viewing", HttpMethod.POST, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Instructs a session to browse to an item or view.
     * 
     * <p>
     * <b>204</b> - Instruction sent to session.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param sessionId The session Id.
     * @param itemType The type of item to browse to.
     * @param itemId The Id of the item.
     * @param itemName The name of the item.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> displayContent(String sessionId, BaseItemKind itemType, String itemId, String itemName)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return displayContentRequestCreation(sessionId, itemType, itemId, itemName).bodyToMono(localVarReturnType);
    }

    /**
     * Instructs a session to browse to an item or view.
     * 
     * <p>
     * <b>204</b> - Instruction sent to session.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param sessionId The session Id.
     * @param itemType The type of item to browse to.
     * @param itemId The Id of the item.
     * @param itemName The name of the item.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> displayContentWithHttpInfo(String sessionId, BaseItemKind itemType, String itemId,
            String itemName) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return displayContentRequestCreation(sessionId, itemType, itemId, itemName).toEntity(localVarReturnType);
    }

    /**
     * Instructs a session to browse to an item or view.
     * 
     * <p>
     * <b>204</b> - Instruction sent to session.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param sessionId The session Id.
     * @param itemType The type of item to browse to.
     * @param itemId The Id of the item.
     * @param itemName The name of the item.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec displayContentWithResponseSpec(String sessionId, BaseItemKind itemType, String itemId,
            String itemName) throws WebClientResponseException {
        return displayContentRequestCreation(sessionId, itemType, itemId, itemName);
    }

    /**
     * Get all auth providers.
     * 
     * <p>
     * <b>200</b> - Auth providers retrieved.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return List&lt;NameIdPair&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getAuthProvidersRequestCreation() throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<NameIdPair> localVarReturnType = new ParameterizedTypeReference<NameIdPair>() {
        };
        return apiClient.invokeAPI("/Auth/Providers", HttpMethod.GET, pathParams, queryParams, postBody, headerParams,
                cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get all auth providers.
     * 
     * <p>
     * <b>200</b> - Auth providers retrieved.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return List&lt;NameIdPair&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Flux<NameIdPair> getAuthProviders() throws WebClientResponseException {
        ParameterizedTypeReference<NameIdPair> localVarReturnType = new ParameterizedTypeReference<NameIdPair>() {
        };
        return getAuthProvidersRequestCreation().bodyToFlux(localVarReturnType);
    }

    /**
     * Get all auth providers.
     * 
     * <p>
     * <b>200</b> - Auth providers retrieved.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return ResponseEntity&lt;List&lt;NameIdPair&gt;&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<List<NameIdPair>>> getAuthProvidersWithHttpInfo() throws WebClientResponseException {
        ParameterizedTypeReference<NameIdPair> localVarReturnType = new ParameterizedTypeReference<NameIdPair>() {
        };
        return getAuthProvidersRequestCreation().toEntityList(localVarReturnType);
    }

    /**
     * Get all auth providers.
     * 
     * <p>
     * <b>200</b> - Auth providers retrieved.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getAuthProvidersWithResponseSpec() throws WebClientResponseException {
        return getAuthProvidersRequestCreation();
    }

    /**
     * Get all password reset providers.
     * 
     * <p>
     * <b>200</b> - Password reset providers retrieved.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return List&lt;NameIdPair&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getPasswordResetProvidersRequestCreation() throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<NameIdPair> localVarReturnType = new ParameterizedTypeReference<NameIdPair>() {
        };
        return apiClient.invokeAPI("/Auth/PasswordResetProviders", HttpMethod.GET, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Get all password reset providers.
     * 
     * <p>
     * <b>200</b> - Password reset providers retrieved.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return List&lt;NameIdPair&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Flux<NameIdPair> getPasswordResetProviders() throws WebClientResponseException {
        ParameterizedTypeReference<NameIdPair> localVarReturnType = new ParameterizedTypeReference<NameIdPair>() {
        };
        return getPasswordResetProvidersRequestCreation().bodyToFlux(localVarReturnType);
    }

    /**
     * Get all password reset providers.
     * 
     * <p>
     * <b>200</b> - Password reset providers retrieved.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return ResponseEntity&lt;List&lt;NameIdPair&gt;&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<List<NameIdPair>>> getPasswordResetProvidersWithHttpInfo()
            throws WebClientResponseException {
        ParameterizedTypeReference<NameIdPair> localVarReturnType = new ParameterizedTypeReference<NameIdPair>() {
        };
        return getPasswordResetProvidersRequestCreation().toEntityList(localVarReturnType);
    }

    /**
     * Get all password reset providers.
     * 
     * <p>
     * <b>200</b> - Password reset providers retrieved.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getPasswordResetProvidersWithResponseSpec() throws WebClientResponseException {
        return getPasswordResetProvidersRequestCreation();
    }

    /**
     * Gets a list of sessions.
     * 
     * <p>
     * <b>200</b> - List of sessions returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param controllableByUserId Filter by sessions that a given user is allowed to remote control.
     * @param deviceId Filter by device Id.
     * @param activeWithinSeconds Optional. Filter by sessions that were active in the last n seconds.
     * @return List&lt;SessionInfo&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getSessionsRequestCreation(UUID controllableByUserId, String deviceId,
            Integer activeWithinSeconds) throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "controllableByUserId", controllableByUserId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "deviceId", deviceId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "activeWithinSeconds", activeWithinSeconds));

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<SessionInfo> localVarReturnType = new ParameterizedTypeReference<SessionInfo>() {
        };
        return apiClient.invokeAPI("/Sessions", HttpMethod.GET, pathParams, queryParams, postBody, headerParams,
                cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets a list of sessions.
     * 
     * <p>
     * <b>200</b> - List of sessions returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param controllableByUserId Filter by sessions that a given user is allowed to remote control.
     * @param deviceId Filter by device Id.
     * @param activeWithinSeconds Optional. Filter by sessions that were active in the last n seconds.
     * @return List&lt;SessionInfo&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Flux<SessionInfo> getSessions(UUID controllableByUserId, String deviceId, Integer activeWithinSeconds)
            throws WebClientResponseException {
        ParameterizedTypeReference<SessionInfo> localVarReturnType = new ParameterizedTypeReference<SessionInfo>() {
        };
        return getSessionsRequestCreation(controllableByUserId, deviceId, activeWithinSeconds)
                .bodyToFlux(localVarReturnType);
    }

    /**
     * Gets a list of sessions.
     * 
     * <p>
     * <b>200</b> - List of sessions returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param controllableByUserId Filter by sessions that a given user is allowed to remote control.
     * @param deviceId Filter by device Id.
     * @param activeWithinSeconds Optional. Filter by sessions that were active in the last n seconds.
     * @return ResponseEntity&lt;List&lt;SessionInfo&gt;&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<List<SessionInfo>>> getSessionsWithHttpInfo(UUID controllableByUserId, String deviceId,
            Integer activeWithinSeconds) throws WebClientResponseException {
        ParameterizedTypeReference<SessionInfo> localVarReturnType = new ParameterizedTypeReference<SessionInfo>() {
        };
        return getSessionsRequestCreation(controllableByUserId, deviceId, activeWithinSeconds)
                .toEntityList(localVarReturnType);
    }

    /**
     * Gets a list of sessions.
     * 
     * <p>
     * <b>200</b> - List of sessions returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param controllableByUserId Filter by sessions that a given user is allowed to remote control.
     * @param deviceId Filter by device Id.
     * @param activeWithinSeconds Optional. Filter by sessions that were active in the last n seconds.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getSessionsWithResponseSpec(UUID controllableByUserId, String deviceId,
            Integer activeWithinSeconds) throws WebClientResponseException {
        return getSessionsRequestCreation(controllableByUserId, deviceId, activeWithinSeconds);
    }

    /**
     * Instructs a session to play an item.
     * 
     * <p>
     * <b>204</b> - Instruction sent to session.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param sessionId The session id.
     * @param playCommand The type of play command to issue (PlayNow, PlayNext, PlayLast). Clients who have not yet
     *            implemented play next and play last may play now.
     * @param itemIds The ids of the items to play, comma delimited.
     * @param startPositionTicks The starting position of the first item.
     * @param mediaSourceId Optional. The media source id.
     * @param audioStreamIndex Optional. The index of the audio stream to play.
     * @param subtitleStreamIndex Optional. The index of the subtitle stream to play.
     * @param startIndex Optional. The start index.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec playRequestCreation(String sessionId, PlayCommand playCommand, List<UUID> itemIds,
            Long startPositionTicks, String mediaSourceId, Integer audioStreamIndex, Integer subtitleStreamIndex,
            Integer startIndex) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'sessionId' is set
        if (sessionId == null) {
            throw new WebClientResponseException("Missing the required parameter 'sessionId' when calling play",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'playCommand' is set
        if (playCommand == null) {
            throw new WebClientResponseException("Missing the required parameter 'playCommand' when calling play",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'itemIds' is set
        if (itemIds == null) {
            throw new WebClientResponseException("Missing the required parameter 'itemIds' when calling play",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("sessionId", sessionId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "playCommand", playCommand));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "itemIds", itemIds));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "startPositionTicks", startPositionTicks));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "mediaSourceId", mediaSourceId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "audioStreamIndex", audioStreamIndex));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "subtitleStreamIndex", subtitleStreamIndex));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "startIndex", startIndex));

        final String[] localVarAccepts = {};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return apiClient.invokeAPI("/Sessions/{sessionId}/Playing", HttpMethod.POST, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Instructs a session to play an item.
     * 
     * <p>
     * <b>204</b> - Instruction sent to session.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param sessionId The session id.
     * @param playCommand The type of play command to issue (PlayNow, PlayNext, PlayLast). Clients who have not yet
     *            implemented play next and play last may play now.
     * @param itemIds The ids of the items to play, comma delimited.
     * @param startPositionTicks The starting position of the first item.
     * @param mediaSourceId Optional. The media source id.
     * @param audioStreamIndex Optional. The index of the audio stream to play.
     * @param subtitleStreamIndex Optional. The index of the subtitle stream to play.
     * @param startIndex Optional. The start index.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> play(String sessionId, PlayCommand playCommand, List<UUID> itemIds, Long startPositionTicks,
            String mediaSourceId, Integer audioStreamIndex, Integer subtitleStreamIndex, Integer startIndex)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return playRequestCreation(sessionId, playCommand, itemIds, startPositionTicks, mediaSourceId, audioStreamIndex,
                subtitleStreamIndex, startIndex).bodyToMono(localVarReturnType);
    }

    /**
     * Instructs a session to play an item.
     * 
     * <p>
     * <b>204</b> - Instruction sent to session.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param sessionId The session id.
     * @param playCommand The type of play command to issue (PlayNow, PlayNext, PlayLast). Clients who have not yet
     *            implemented play next and play last may play now.
     * @param itemIds The ids of the items to play, comma delimited.
     * @param startPositionTicks The starting position of the first item.
     * @param mediaSourceId Optional. The media source id.
     * @param audioStreamIndex Optional. The index of the audio stream to play.
     * @param subtitleStreamIndex Optional. The index of the subtitle stream to play.
     * @param startIndex Optional. The start index.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> playWithHttpInfo(String sessionId, PlayCommand playCommand, List<UUID> itemIds,
            Long startPositionTicks, String mediaSourceId, Integer audioStreamIndex, Integer subtitleStreamIndex,
            Integer startIndex) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return playRequestCreation(sessionId, playCommand, itemIds, startPositionTicks, mediaSourceId, audioStreamIndex,
                subtitleStreamIndex, startIndex).toEntity(localVarReturnType);
    }

    /**
     * Instructs a session to play an item.
     * 
     * <p>
     * <b>204</b> - Instruction sent to session.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param sessionId The session id.
     * @param playCommand The type of play command to issue (PlayNow, PlayNext, PlayLast). Clients who have not yet
     *            implemented play next and play last may play now.
     * @param itemIds The ids of the items to play, comma delimited.
     * @param startPositionTicks The starting position of the first item.
     * @param mediaSourceId Optional. The media source id.
     * @param audioStreamIndex Optional. The index of the audio stream to play.
     * @param subtitleStreamIndex Optional. The index of the subtitle stream to play.
     * @param startIndex Optional. The start index.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec playWithResponseSpec(String sessionId, PlayCommand playCommand, List<UUID> itemIds,
            Long startPositionTicks, String mediaSourceId, Integer audioStreamIndex, Integer subtitleStreamIndex,
            Integer startIndex) throws WebClientResponseException {
        return playRequestCreation(sessionId, playCommand, itemIds, startPositionTicks, mediaSourceId, audioStreamIndex,
                subtitleStreamIndex, startIndex);
    }

    /**
     * Updates capabilities for a device.
     * 
     * <p>
     * <b>204</b> - Capabilities posted.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param id The session id.
     * @param playableMediaTypes A list of playable media types, comma delimited. Audio, Video, Book, Photo.
     * @param supportedCommands A list of supported remote control commands, comma delimited.
     * @param supportsMediaControl Determines whether media can be played remotely..
     * @param supportsSync Determines whether sync is supported.
     * @param supportsPersistentIdentifier Determines whether the device supports a unique identifier.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec postCapabilitiesRequestCreation(String id, List<String> playableMediaTypes,
            List<GeneralCommandType> supportedCommands, Boolean supportsMediaControl, Boolean supportsSync,
            Boolean supportsPersistentIdentifier) throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "id", id));
        queryParams.putAll(
                apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)),
                        "playableMediaTypes", playableMediaTypes));
        queryParams.putAll(
                apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)),
                        "supportedCommands", supportedCommands));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "supportsMediaControl", supportsMediaControl));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "supportsSync", supportsSync));
        queryParams.putAll(
                apiClient.parameterToMultiValueMap(null, "supportsPersistentIdentifier", supportsPersistentIdentifier));

        final String[] localVarAccepts = {};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return apiClient.invokeAPI("/Sessions/Capabilities", HttpMethod.POST, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Updates capabilities for a device.
     * 
     * <p>
     * <b>204</b> - Capabilities posted.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param id The session id.
     * @param playableMediaTypes A list of playable media types, comma delimited. Audio, Video, Book, Photo.
     * @param supportedCommands A list of supported remote control commands, comma delimited.
     * @param supportsMediaControl Determines whether media can be played remotely..
     * @param supportsSync Determines whether sync is supported.
     * @param supportsPersistentIdentifier Determines whether the device supports a unique identifier.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> postCapabilities(String id, List<String> playableMediaTypes,
            List<GeneralCommandType> supportedCommands, Boolean supportsMediaControl, Boolean supportsSync,
            Boolean supportsPersistentIdentifier) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return postCapabilitiesRequestCreation(id, playableMediaTypes, supportedCommands, supportsMediaControl,
                supportsSync, supportsPersistentIdentifier).bodyToMono(localVarReturnType);
    }

    /**
     * Updates capabilities for a device.
     * 
     * <p>
     * <b>204</b> - Capabilities posted.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param id The session id.
     * @param playableMediaTypes A list of playable media types, comma delimited. Audio, Video, Book, Photo.
     * @param supportedCommands A list of supported remote control commands, comma delimited.
     * @param supportsMediaControl Determines whether media can be played remotely..
     * @param supportsSync Determines whether sync is supported.
     * @param supportsPersistentIdentifier Determines whether the device supports a unique identifier.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> postCapabilitiesWithHttpInfo(String id, List<String> playableMediaTypes,
            List<GeneralCommandType> supportedCommands, Boolean supportsMediaControl, Boolean supportsSync,
            Boolean supportsPersistentIdentifier) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return postCapabilitiesRequestCreation(id, playableMediaTypes, supportedCommands, supportsMediaControl,
                supportsSync, supportsPersistentIdentifier).toEntity(localVarReturnType);
    }

    /**
     * Updates capabilities for a device.
     * 
     * <p>
     * <b>204</b> - Capabilities posted.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param id The session id.
     * @param playableMediaTypes A list of playable media types, comma delimited. Audio, Video, Book, Photo.
     * @param supportedCommands A list of supported remote control commands, comma delimited.
     * @param supportsMediaControl Determines whether media can be played remotely..
     * @param supportsSync Determines whether sync is supported.
     * @param supportsPersistentIdentifier Determines whether the device supports a unique identifier.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec postCapabilitiesWithResponseSpec(String id, List<String> playableMediaTypes,
            List<GeneralCommandType> supportedCommands, Boolean supportsMediaControl, Boolean supportsSync,
            Boolean supportsPersistentIdentifier) throws WebClientResponseException {
        return postCapabilitiesRequestCreation(id, playableMediaTypes, supportedCommands, supportsMediaControl,
                supportsSync, supportsPersistentIdentifier);
    }

    /**
     * Updates capabilities for a device.
     * 
     * <p>
     * <b>204</b> - Capabilities updated.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param clientCapabilitiesDto The MediaBrowser.Model.Session.ClientCapabilities.
     * @param id The session id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec postFullCapabilitiesRequestCreation(ClientCapabilitiesDto clientCapabilitiesDto, String id)
            throws WebClientResponseException {
        Object postBody = clientCapabilitiesDto;
        // verify the required parameter 'clientCapabilitiesDto' is set
        if (clientCapabilitiesDto == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'clientCapabilitiesDto' when calling postFullCapabilities",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "id", id));

        final String[] localVarAccepts = {};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { "application/json", "text/json", "application/*+json" };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return apiClient.invokeAPI("/Sessions/Capabilities/Full", HttpMethod.POST, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Updates capabilities for a device.
     * 
     * <p>
     * <b>204</b> - Capabilities updated.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param clientCapabilitiesDto The MediaBrowser.Model.Session.ClientCapabilities.
     * @param id The session id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> postFullCapabilities(ClientCapabilitiesDto clientCapabilitiesDto, String id)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return postFullCapabilitiesRequestCreation(clientCapabilitiesDto, id).bodyToMono(localVarReturnType);
    }

    /**
     * Updates capabilities for a device.
     * 
     * <p>
     * <b>204</b> - Capabilities updated.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param clientCapabilitiesDto The MediaBrowser.Model.Session.ClientCapabilities.
     * @param id The session id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> postFullCapabilitiesWithHttpInfo(ClientCapabilitiesDto clientCapabilitiesDto,
            String id) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return postFullCapabilitiesRequestCreation(clientCapabilitiesDto, id).toEntity(localVarReturnType);
    }

    /**
     * Updates capabilities for a device.
     * 
     * <p>
     * <b>204</b> - Capabilities updated.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param clientCapabilitiesDto The MediaBrowser.Model.Session.ClientCapabilities.
     * @param id The session id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec postFullCapabilitiesWithResponseSpec(ClientCapabilitiesDto clientCapabilitiesDto, String id)
            throws WebClientResponseException {
        return postFullCapabilitiesRequestCreation(clientCapabilitiesDto, id);
    }

    /**
     * Removes an additional user from a session.
     * 
     * <p>
     * <b>204</b> - User removed from session.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param sessionId The session id.
     * @param userId The user id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec removeUserFromSessionRequestCreation(String sessionId, UUID userId)
            throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'sessionId' is set
        if (sessionId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'sessionId' when calling removeUserFromSession",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'userId' is set
        if (userId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'userId' when calling removeUserFromSession",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("sessionId", sessionId);
        pathParams.put("userId", userId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = {};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return apiClient.invokeAPI("/Sessions/{sessionId}/User/{userId}", HttpMethod.DELETE, pathParams, queryParams,
                postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType,
                localVarAuthNames, localVarReturnType);
    }

    /**
     * Removes an additional user from a session.
     * 
     * <p>
     * <b>204</b> - User removed from session.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param sessionId The session id.
     * @param userId The user id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> removeUserFromSession(String sessionId, UUID userId) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return removeUserFromSessionRequestCreation(sessionId, userId).bodyToMono(localVarReturnType);
    }

    /**
     * Removes an additional user from a session.
     * 
     * <p>
     * <b>204</b> - User removed from session.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param sessionId The session id.
     * @param userId The user id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> removeUserFromSessionWithHttpInfo(String sessionId, UUID userId)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return removeUserFromSessionRequestCreation(sessionId, userId).toEntity(localVarReturnType);
    }

    /**
     * Removes an additional user from a session.
     * 
     * <p>
     * <b>204</b> - User removed from session.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param sessionId The session id.
     * @param userId The user id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec removeUserFromSessionWithResponseSpec(String sessionId, UUID userId)
            throws WebClientResponseException {
        return removeUserFromSessionRequestCreation(sessionId, userId);
    }

    /**
     * Reports that a session has ended.
     * 
     * <p>
     * <b>204</b> - Session end reported to server.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec reportSessionEndedRequestCreation() throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = {};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return apiClient.invokeAPI("/Sessions/Logout", HttpMethod.POST, pathParams, queryParams, postBody, headerParams,
                cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Reports that a session has ended.
     * 
     * <p>
     * <b>204</b> - Session end reported to server.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> reportSessionEnded() throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return reportSessionEndedRequestCreation().bodyToMono(localVarReturnType);
    }

    /**
     * Reports that a session has ended.
     * 
     * <p>
     * <b>204</b> - Session end reported to server.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> reportSessionEndedWithHttpInfo() throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return reportSessionEndedRequestCreation().toEntity(localVarReturnType);
    }

    /**
     * Reports that a session has ended.
     * 
     * <p>
     * <b>204</b> - Session end reported to server.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec reportSessionEndedWithResponseSpec() throws WebClientResponseException {
        return reportSessionEndedRequestCreation();
    }

    /**
     * Reports that a session is viewing an item.
     * 
     * <p>
     * <b>204</b> - Session reported to server.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId The item id.
     * @param sessionId The session id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec reportViewingRequestCreation(String itemId, String sessionId)
            throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException("Missing the required parameter 'itemId' when calling reportViewing",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "sessionId", sessionId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "itemId", itemId));

        final String[] localVarAccepts = {};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return apiClient.invokeAPI("/Sessions/Viewing", HttpMethod.POST, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Reports that a session is viewing an item.
     * 
     * <p>
     * <b>204</b> - Session reported to server.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId The item id.
     * @param sessionId The session id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> reportViewing(String itemId, String sessionId) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return reportViewingRequestCreation(itemId, sessionId).bodyToMono(localVarReturnType);
    }

    /**
     * Reports that a session is viewing an item.
     * 
     * <p>
     * <b>204</b> - Session reported to server.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId The item id.
     * @param sessionId The session id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> reportViewingWithHttpInfo(String itemId, String sessionId)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return reportViewingRequestCreation(itemId, sessionId).toEntity(localVarReturnType);
    }

    /**
     * Reports that a session is viewing an item.
     * 
     * <p>
     * <b>204</b> - Session reported to server.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId The item id.
     * @param sessionId The session id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec reportViewingWithResponseSpec(String itemId, String sessionId)
            throws WebClientResponseException {
        return reportViewingRequestCreation(itemId, sessionId);
    }

    /**
     * Issues a full general command to a client.
     * 
     * <p>
     * <b>204</b> - Full general command sent to session.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param sessionId The session id.
     * @param generalCommand The MediaBrowser.Model.Session.GeneralCommand.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec sendFullGeneralCommandRequestCreation(String sessionId, GeneralCommand generalCommand)
            throws WebClientResponseException {
        Object postBody = generalCommand;
        // verify the required parameter 'sessionId' is set
        if (sessionId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'sessionId' when calling sendFullGeneralCommand",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'generalCommand' is set
        if (generalCommand == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'generalCommand' when calling sendFullGeneralCommand",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("sessionId", sessionId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = {};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { "application/json", "text/json", "application/*+json" };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return apiClient.invokeAPI("/Sessions/{sessionId}/Command", HttpMethod.POST, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Issues a full general command to a client.
     * 
     * <p>
     * <b>204</b> - Full general command sent to session.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param sessionId The session id.
     * @param generalCommand The MediaBrowser.Model.Session.GeneralCommand.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> sendFullGeneralCommand(String sessionId, GeneralCommand generalCommand)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return sendFullGeneralCommandRequestCreation(sessionId, generalCommand).bodyToMono(localVarReturnType);
    }

    /**
     * Issues a full general command to a client.
     * 
     * <p>
     * <b>204</b> - Full general command sent to session.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param sessionId The session id.
     * @param generalCommand The MediaBrowser.Model.Session.GeneralCommand.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> sendFullGeneralCommandWithHttpInfo(String sessionId,
            GeneralCommand generalCommand) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return sendFullGeneralCommandRequestCreation(sessionId, generalCommand).toEntity(localVarReturnType);
    }

    /**
     * Issues a full general command to a client.
     * 
     * <p>
     * <b>204</b> - Full general command sent to session.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param sessionId The session id.
     * @param generalCommand The MediaBrowser.Model.Session.GeneralCommand.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec sendFullGeneralCommandWithResponseSpec(String sessionId, GeneralCommand generalCommand)
            throws WebClientResponseException {
        return sendFullGeneralCommandRequestCreation(sessionId, generalCommand);
    }

    /**
     * Issues a general command to a client.
     * 
     * <p>
     * <b>204</b> - General command sent to session.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param sessionId The session id.
     * @param command The command to send.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec sendGeneralCommandRequestCreation(String sessionId, GeneralCommandType command)
            throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'sessionId' is set
        if (sessionId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'sessionId' when calling sendGeneralCommand",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'command' is set
        if (command == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'command' when calling sendGeneralCommand",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("sessionId", sessionId);
        pathParams.put("command", command);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = {};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return apiClient.invokeAPI("/Sessions/{sessionId}/Command/{command}", HttpMethod.POST, pathParams, queryParams,
                postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType,
                localVarAuthNames, localVarReturnType);
    }

    /**
     * Issues a general command to a client.
     * 
     * <p>
     * <b>204</b> - General command sent to session.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param sessionId The session id.
     * @param command The command to send.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> sendGeneralCommand(String sessionId, GeneralCommandType command)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return sendGeneralCommandRequestCreation(sessionId, command).bodyToMono(localVarReturnType);
    }

    /**
     * Issues a general command to a client.
     * 
     * <p>
     * <b>204</b> - General command sent to session.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param sessionId The session id.
     * @param command The command to send.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> sendGeneralCommandWithHttpInfo(String sessionId, GeneralCommandType command)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return sendGeneralCommandRequestCreation(sessionId, command).toEntity(localVarReturnType);
    }

    /**
     * Issues a general command to a client.
     * 
     * <p>
     * <b>204</b> - General command sent to session.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param sessionId The session id.
     * @param command The command to send.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec sendGeneralCommandWithResponseSpec(String sessionId, GeneralCommandType command)
            throws WebClientResponseException {
        return sendGeneralCommandRequestCreation(sessionId, command);
    }

    /**
     * Issues a command to a client to display a message to the user.
     * 
     * <p>
     * <b>204</b> - Message sent.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param sessionId The session id.
     * @param messageCommand The MediaBrowser.Model.Session.MessageCommand object containing Header, Message Text, and
     *            TimeoutMs.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec sendMessageCommandRequestCreation(String sessionId, MessageCommand messageCommand)
            throws WebClientResponseException {
        Object postBody = messageCommand;
        // verify the required parameter 'sessionId' is set
        if (sessionId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'sessionId' when calling sendMessageCommand",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'messageCommand' is set
        if (messageCommand == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'messageCommand' when calling sendMessageCommand",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("sessionId", sessionId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = {};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { "application/json", "text/json", "application/*+json" };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return apiClient.invokeAPI("/Sessions/{sessionId}/Message", HttpMethod.POST, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Issues a command to a client to display a message to the user.
     * 
     * <p>
     * <b>204</b> - Message sent.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param sessionId The session id.
     * @param messageCommand The MediaBrowser.Model.Session.MessageCommand object containing Header, Message Text, and
     *            TimeoutMs.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> sendMessageCommand(String sessionId, MessageCommand messageCommand)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return sendMessageCommandRequestCreation(sessionId, messageCommand).bodyToMono(localVarReturnType);
    }

    /**
     * Issues a command to a client to display a message to the user.
     * 
     * <p>
     * <b>204</b> - Message sent.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param sessionId The session id.
     * @param messageCommand The MediaBrowser.Model.Session.MessageCommand object containing Header, Message Text, and
     *            TimeoutMs.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> sendMessageCommandWithHttpInfo(String sessionId, MessageCommand messageCommand)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return sendMessageCommandRequestCreation(sessionId, messageCommand).toEntity(localVarReturnType);
    }

    /**
     * Issues a command to a client to display a message to the user.
     * 
     * <p>
     * <b>204</b> - Message sent.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param sessionId The session id.
     * @param messageCommand The MediaBrowser.Model.Session.MessageCommand object containing Header, Message Text, and
     *            TimeoutMs.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec sendMessageCommandWithResponseSpec(String sessionId, MessageCommand messageCommand)
            throws WebClientResponseException {
        return sendMessageCommandRequestCreation(sessionId, messageCommand);
    }

    /**
     * Issues a playstate command to a client.
     * 
     * <p>
     * <b>204</b> - Playstate command sent to session.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param sessionId The session id.
     * @param command The MediaBrowser.Model.Session.PlaystateCommand.
     * @param seekPositionTicks The optional position ticks.
     * @param controllingUserId The optional controlling user id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec sendPlaystateCommandRequestCreation(String sessionId, PlaystateCommand command,
            Long seekPositionTicks, String controllingUserId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'sessionId' is set
        if (sessionId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'sessionId' when calling sendPlaystateCommand",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'command' is set
        if (command == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'command' when calling sendPlaystateCommand",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("sessionId", sessionId);
        pathParams.put("command", command);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "seekPositionTicks", seekPositionTicks));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "controllingUserId", controllingUserId));

        final String[] localVarAccepts = {};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return apiClient.invokeAPI("/Sessions/{sessionId}/Playing/{command}", HttpMethod.POST, pathParams, queryParams,
                postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType,
                localVarAuthNames, localVarReturnType);
    }

    /**
     * Issues a playstate command to a client.
     * 
     * <p>
     * <b>204</b> - Playstate command sent to session.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param sessionId The session id.
     * @param command The MediaBrowser.Model.Session.PlaystateCommand.
     * @param seekPositionTicks The optional position ticks.
     * @param controllingUserId The optional controlling user id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> sendPlaystateCommand(String sessionId, PlaystateCommand command, Long seekPositionTicks,
            String controllingUserId) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return sendPlaystateCommandRequestCreation(sessionId, command, seekPositionTicks, controllingUserId)
                .bodyToMono(localVarReturnType);
    }

    /**
     * Issues a playstate command to a client.
     * 
     * <p>
     * <b>204</b> - Playstate command sent to session.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param sessionId The session id.
     * @param command The MediaBrowser.Model.Session.PlaystateCommand.
     * @param seekPositionTicks The optional position ticks.
     * @param controllingUserId The optional controlling user id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> sendPlaystateCommandWithHttpInfo(String sessionId, PlaystateCommand command,
            Long seekPositionTicks, String controllingUserId) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return sendPlaystateCommandRequestCreation(sessionId, command, seekPositionTicks, controllingUserId)
                .toEntity(localVarReturnType);
    }

    /**
     * Issues a playstate command to a client.
     * 
     * <p>
     * <b>204</b> - Playstate command sent to session.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param sessionId The session id.
     * @param command The MediaBrowser.Model.Session.PlaystateCommand.
     * @param seekPositionTicks The optional position ticks.
     * @param controllingUserId The optional controlling user id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec sendPlaystateCommandWithResponseSpec(String sessionId, PlaystateCommand command,
            Long seekPositionTicks, String controllingUserId) throws WebClientResponseException {
        return sendPlaystateCommandRequestCreation(sessionId, command, seekPositionTicks, controllingUserId);
    }

    /**
     * Issues a system command to a client.
     * 
     * <p>
     * <b>204</b> - System command sent to session.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param sessionId The session id.
     * @param command The command to send.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec sendSystemCommandRequestCreation(String sessionId, GeneralCommandType command)
            throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'sessionId' is set
        if (sessionId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'sessionId' when calling sendSystemCommand",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'command' is set
        if (command == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'command' when calling sendSystemCommand",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("sessionId", sessionId);
        pathParams.put("command", command);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = {};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return apiClient.invokeAPI("/Sessions/{sessionId}/System/{command}", HttpMethod.POST, pathParams, queryParams,
                postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType,
                localVarAuthNames, localVarReturnType);
    }

    /**
     * Issues a system command to a client.
     * 
     * <p>
     * <b>204</b> - System command sent to session.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param sessionId The session id.
     * @param command The command to send.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> sendSystemCommand(String sessionId, GeneralCommandType command)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return sendSystemCommandRequestCreation(sessionId, command).bodyToMono(localVarReturnType);
    }

    /**
     * Issues a system command to a client.
     * 
     * <p>
     * <b>204</b> - System command sent to session.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param sessionId The session id.
     * @param command The command to send.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> sendSystemCommandWithHttpInfo(String sessionId, GeneralCommandType command)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return sendSystemCommandRequestCreation(sessionId, command).toEntity(localVarReturnType);
    }

    /**
     * Issues a system command to a client.
     * 
     * <p>
     * <b>204</b> - System command sent to session.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param sessionId The session id.
     * @param command The command to send.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec sendSystemCommandWithResponseSpec(String sessionId, GeneralCommandType command)
            throws WebClientResponseException {
        return sendSystemCommandRequestCreation(sessionId, command);
    }
}
