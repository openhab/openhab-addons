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
package org.openhab.binding.jellyfin.internal.api.generated.current;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.function.Consumer;

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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class SessionApi {
    private final HttpClient memberVarHttpClient;
    private final ObjectMapper memberVarObjectMapper;
    private final String memberVarBaseUri;
    private final Consumer<HttpRequest.Builder> memberVarInterceptor;
    private final Duration memberVarReadTimeout;
    private final Consumer<HttpResponse<InputStream>> memberVarResponseInterceptor;
    private final Consumer<HttpResponse<String>> memberVarAsyncResponseInterceptor;

    public SessionApi() {
        this(Configuration.getDefaultApiClient());
    }

    public SessionApi(ApiClient apiClient) {
        memberVarHttpClient = apiClient.getHttpClient();
        memberVarObjectMapper = apiClient.getObjectMapper();
        memberVarBaseUri = apiClient.getBaseUri();
        memberVarInterceptor = apiClient.getRequestInterceptor();
        memberVarReadTimeout = apiClient.getReadTimeout();
        memberVarResponseInterceptor = apiClient.getResponseInterceptor();
        memberVarAsyncResponseInterceptor = apiClient.getAsyncResponseInterceptor();
    }

    protected ApiException getApiException(String operationId, HttpResponse<InputStream> response) throws IOException {
        String body = response.body() == null ? null : new String(response.body().readAllBytes());
        String message = formatExceptionMessage(operationId, response.statusCode(), body);
        return new ApiException(response.statusCode(), message, response.headers(), body);
    }

    private String formatExceptionMessage(String operationId, int statusCode, String body) {
        if (body == null || body.isEmpty()) {
            body = "[no body]";
        }
        return operationId + " call failed with: " + statusCode + " - " + body;
    }

    /**
     * Adds an additional user to a session.
     * 
     * @param sessionId The session id. (required)
     * @param userId The user id. (required)
     * @throws ApiException if fails to make API call
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
     */
    public ApiResponse<Void> addUserToSessionWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String sessionId,
            @org.eclipse.jdt.annotation.Nullable UUID userId) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = addUserToSessionRequestBuilder(sessionId, userId);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("addUserToSession", localVarResponse);
                }
                return new ApiResponse<>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
            } finally {
                // Drain the InputStream
                while (localVarResponse.body().read() != -1) {
                    // Ignore
                }
                localVarResponse.body().close();
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder addUserToSessionRequestBuilder(@org.eclipse.jdt.annotation.Nullable String sessionId,
            @org.eclipse.jdt.annotation.Nullable UUID userId) throws ApiException {
        // verify the required parameter 'sessionId' is set
        if (sessionId == null) {
            throw new ApiException(400, "Missing the required parameter 'sessionId' when calling addUserToSession");
        }
        // verify the required parameter 'userId' is set
        if (userId == null) {
            throw new ApiException(400, "Missing the required parameter 'userId' when calling addUserToSession");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Sessions/{sessionId}/User/{userId}"
                .replace("{sessionId}", ApiClient.urlEncode(sessionId.toString()))
                .replace("{userId}", ApiClient.urlEncode(userId.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Accept", "application/json");

        localVarRequestBuilder.method("POST", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    /**
     * Instructs a session to browse to an item or view.
     * 
     * @param sessionId The session Id. (required)
     * @param itemType The type of item to browse to. (required)
     * @param itemId The Id of the item. (required)
     * @param itemName The name of the item. (required)
     * @throws ApiException if fails to make API call
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
     */
    public ApiResponse<Void> displayContentWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String sessionId,
            @org.eclipse.jdt.annotation.Nullable BaseItemKind itemType,
            @org.eclipse.jdt.annotation.Nullable String itemId, @org.eclipse.jdt.annotation.Nullable String itemName)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = displayContentRequestBuilder(sessionId, itemType, itemId,
                itemName);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("displayContent", localVarResponse);
                }
                return new ApiResponse<>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
            } finally {
                // Drain the InputStream
                while (localVarResponse.body().read() != -1) {
                    // Ignore
                }
                localVarResponse.body().close();
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder displayContentRequestBuilder(@org.eclipse.jdt.annotation.Nullable String sessionId,
            @org.eclipse.jdt.annotation.Nullable BaseItemKind itemType,
            @org.eclipse.jdt.annotation.Nullable String itemId, @org.eclipse.jdt.annotation.Nullable String itemName)
            throws ApiException {
        // verify the required parameter 'sessionId' is set
        if (sessionId == null) {
            throw new ApiException(400, "Missing the required parameter 'sessionId' when calling displayContent");
        }
        // verify the required parameter 'itemType' is set
        if (itemType == null) {
            throw new ApiException(400, "Missing the required parameter 'itemType' when calling displayContent");
        }
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling displayContent");
        }
        // verify the required parameter 'itemName' is set
        if (itemName == null) {
            throw new ApiException(400, "Missing the required parameter 'itemName' when calling displayContent");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Sessions/{sessionId}/Viewing".replace("{sessionId}",
                ApiClient.urlEncode(sessionId.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "itemType";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("itemType", itemType));
        localVarQueryParameterBaseName = "itemId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("itemId", itemId));
        localVarQueryParameterBaseName = "itemName";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("itemName", itemName));

        if (!localVarQueryParams.isEmpty() || localVarQueryStringJoiner.length() != 0) {
            StringJoiner queryJoiner = new StringJoiner("&");
            localVarQueryParams.forEach(p -> queryJoiner.add(p.getName() + '=' + p.getValue()));
            if (localVarQueryStringJoiner.length() != 0) {
                queryJoiner.add(localVarQueryStringJoiner.toString());
            }
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath + '?' + queryJoiner.toString()));
        } else {
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));
        }

        localVarRequestBuilder.header("Accept", "application/json");

        localVarRequestBuilder.method("POST", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    /**
     * Get all auth providers.
     * 
     * @return List&lt;NameIdPair&gt;
     * @throws ApiException if fails to make API call
     */
    public List<NameIdPair> getAuthProviders() throws ApiException {
        ApiResponse<List<NameIdPair>> localVarResponse = getAuthProvidersWithHttpInfo();
        return localVarResponse.getData();
    }

    /**
     * Get all auth providers.
     * 
     * @return ApiResponse&lt;List&lt;NameIdPair&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<NameIdPair>> getAuthProvidersWithHttpInfo() throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getAuthProvidersRequestBuilder();
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getAuthProviders", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<List<NameIdPair>>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<List<NameIdPair>>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(), responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody, new TypeReference<List<NameIdPair>>() {
                                }));
            } finally {
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder getAuthProvidersRequestBuilder() throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Auth/Providers";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Accept",
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase");

        localVarRequestBuilder.method("GET", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    /**
     * Get all password reset providers.
     * 
     * @return List&lt;NameIdPair&gt;
     * @throws ApiException if fails to make API call
     */
    public List<NameIdPair> getPasswordResetProviders() throws ApiException {
        ApiResponse<List<NameIdPair>> localVarResponse = getPasswordResetProvidersWithHttpInfo();
        return localVarResponse.getData();
    }

    /**
     * Get all password reset providers.
     * 
     * @return ApiResponse&lt;List&lt;NameIdPair&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<NameIdPair>> getPasswordResetProvidersWithHttpInfo() throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getPasswordResetProvidersRequestBuilder();
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getPasswordResetProviders", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<List<NameIdPair>>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<List<NameIdPair>>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(), responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody, new TypeReference<List<NameIdPair>>() {
                                }));
            } finally {
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder getPasswordResetProvidersRequestBuilder() throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Auth/PasswordResetProviders";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Accept",
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase");

        localVarRequestBuilder.method("GET", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    /**
     * Gets a list of sessions.
     * 
     * @param controllableByUserId Filter by sessions that a given user is allowed to remote control. (optional)
     * @param deviceId Filter by device Id. (optional)
     * @param activeWithinSeconds Optional. Filter by sessions that were active in the last n seconds. (optional)
     * @return List&lt;SessionInfoDto&gt;
     * @throws ApiException if fails to make API call
     */
    public List<SessionInfoDto> getSessions(@org.eclipse.jdt.annotation.NonNull UUID controllableByUserId,
            @org.eclipse.jdt.annotation.NonNull String deviceId,
            @org.eclipse.jdt.annotation.NonNull Integer activeWithinSeconds) throws ApiException {
        ApiResponse<List<SessionInfoDto>> localVarResponse = getSessionsWithHttpInfo(controllableByUserId, deviceId,
                activeWithinSeconds);
        return localVarResponse.getData();
    }

    /**
     * Gets a list of sessions.
     * 
     * @param controllableByUserId Filter by sessions that a given user is allowed to remote control. (optional)
     * @param deviceId Filter by device Id. (optional)
     * @param activeWithinSeconds Optional. Filter by sessions that were active in the last n seconds. (optional)
     * @return ApiResponse&lt;List&lt;SessionInfoDto&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<SessionInfoDto>> getSessionsWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull UUID controllableByUserId,
            @org.eclipse.jdt.annotation.NonNull String deviceId,
            @org.eclipse.jdt.annotation.NonNull Integer activeWithinSeconds) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getSessionsRequestBuilder(controllableByUserId, deviceId,
                activeWithinSeconds);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getSessions", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<List<SessionInfoDto>>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<List<SessionInfoDto>>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody,
                                        new TypeReference<List<SessionInfoDto>>() {
                                        }));
            } finally {
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder getSessionsRequestBuilder(@org.eclipse.jdt.annotation.NonNull UUID controllableByUserId,
            @org.eclipse.jdt.annotation.NonNull String deviceId,
            @org.eclipse.jdt.annotation.NonNull Integer activeWithinSeconds) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Sessions";

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "controllableByUserId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("controllableByUserId", controllableByUserId));
        localVarQueryParameterBaseName = "deviceId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("deviceId", deviceId));
        localVarQueryParameterBaseName = "activeWithinSeconds";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("activeWithinSeconds", activeWithinSeconds));

        if (!localVarQueryParams.isEmpty() || localVarQueryStringJoiner.length() != 0) {
            StringJoiner queryJoiner = new StringJoiner("&");
            localVarQueryParams.forEach(p -> queryJoiner.add(p.getName() + '=' + p.getValue()));
            if (localVarQueryStringJoiner.length() != 0) {
                queryJoiner.add(localVarQueryStringJoiner.toString());
            }
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath + '?' + queryJoiner.toString()));
        } else {
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));
        }

        localVarRequestBuilder.header("Accept",
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase");

        localVarRequestBuilder.method("GET", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
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
     */
    public ApiResponse<Void> playWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String sessionId,
            @org.eclipse.jdt.annotation.Nullable PlayCommand playCommand,
            @org.eclipse.jdt.annotation.Nullable List<UUID> itemIds,
            @org.eclipse.jdt.annotation.NonNull Long startPositionTicks,
            @org.eclipse.jdt.annotation.NonNull String mediaSourceId,
            @org.eclipse.jdt.annotation.NonNull Integer audioStreamIndex,
            @org.eclipse.jdt.annotation.NonNull Integer subtitleStreamIndex,
            @org.eclipse.jdt.annotation.NonNull Integer startIndex) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = playRequestBuilder(sessionId, playCommand, itemIds,
                startPositionTicks, mediaSourceId, audioStreamIndex, subtitleStreamIndex, startIndex);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("play", localVarResponse);
                }
                return new ApiResponse<>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
            } finally {
                // Drain the InputStream
                while (localVarResponse.body().read() != -1) {
                    // Ignore
                }
                localVarResponse.body().close();
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder playRequestBuilder(@org.eclipse.jdt.annotation.Nullable String sessionId,
            @org.eclipse.jdt.annotation.Nullable PlayCommand playCommand,
            @org.eclipse.jdt.annotation.Nullable List<UUID> itemIds,
            @org.eclipse.jdt.annotation.NonNull Long startPositionTicks,
            @org.eclipse.jdt.annotation.NonNull String mediaSourceId,
            @org.eclipse.jdt.annotation.NonNull Integer audioStreamIndex,
            @org.eclipse.jdt.annotation.NonNull Integer subtitleStreamIndex,
            @org.eclipse.jdt.annotation.NonNull Integer startIndex) throws ApiException {
        // verify the required parameter 'sessionId' is set
        if (sessionId == null) {
            throw new ApiException(400, "Missing the required parameter 'sessionId' when calling play");
        }
        // verify the required parameter 'playCommand' is set
        if (playCommand == null) {
            throw new ApiException(400, "Missing the required parameter 'playCommand' when calling play");
        }
        // verify the required parameter 'itemIds' is set
        if (itemIds == null) {
            throw new ApiException(400, "Missing the required parameter 'itemIds' when calling play");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Sessions/{sessionId}/Playing".replace("{sessionId}",
                ApiClient.urlEncode(sessionId.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "playCommand";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("playCommand", playCommand));
        localVarQueryParameterBaseName = "itemIds";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "itemIds", itemIds));
        localVarQueryParameterBaseName = "startPositionTicks";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("startPositionTicks", startPositionTicks));
        localVarQueryParameterBaseName = "mediaSourceId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("mediaSourceId", mediaSourceId));
        localVarQueryParameterBaseName = "audioStreamIndex";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("audioStreamIndex", audioStreamIndex));
        localVarQueryParameterBaseName = "subtitleStreamIndex";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("subtitleStreamIndex", subtitleStreamIndex));
        localVarQueryParameterBaseName = "startIndex";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("startIndex", startIndex));

        if (!localVarQueryParams.isEmpty() || localVarQueryStringJoiner.length() != 0) {
            StringJoiner queryJoiner = new StringJoiner("&");
            localVarQueryParams.forEach(p -> queryJoiner.add(p.getName() + '=' + p.getValue()));
            if (localVarQueryStringJoiner.length() != 0) {
                queryJoiner.add(localVarQueryStringJoiner.toString());
            }
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath + '?' + queryJoiner.toString()));
        } else {
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));
        }

        localVarRequestBuilder.header("Accept", "application/json");

        localVarRequestBuilder.method("POST", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
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
     */
    public ApiResponse<Void> postCapabilitiesWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String id,
            @org.eclipse.jdt.annotation.NonNull List<MediaType> playableMediaTypes,
            @org.eclipse.jdt.annotation.NonNull List<GeneralCommandType> supportedCommands,
            @org.eclipse.jdt.annotation.NonNull Boolean supportsMediaControl,
            @org.eclipse.jdt.annotation.NonNull Boolean supportsPersistentIdentifier) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = postCapabilitiesRequestBuilder(id, playableMediaTypes,
                supportedCommands, supportsMediaControl, supportsPersistentIdentifier);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("postCapabilities", localVarResponse);
                }
                return new ApiResponse<>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
            } finally {
                // Drain the InputStream
                while (localVarResponse.body().read() != -1) {
                    // Ignore
                }
                localVarResponse.body().close();
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder postCapabilitiesRequestBuilder(@org.eclipse.jdt.annotation.NonNull String id,
            @org.eclipse.jdt.annotation.NonNull List<MediaType> playableMediaTypes,
            @org.eclipse.jdt.annotation.NonNull List<GeneralCommandType> supportedCommands,
            @org.eclipse.jdt.annotation.NonNull Boolean supportsMediaControl,
            @org.eclipse.jdt.annotation.NonNull Boolean supportsPersistentIdentifier) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Sessions/Capabilities";

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "id";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("id", id));
        localVarQueryParameterBaseName = "playableMediaTypes";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "playableMediaTypes", playableMediaTypes));
        localVarQueryParameterBaseName = "supportedCommands";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "supportedCommands", supportedCommands));
        localVarQueryParameterBaseName = "supportsMediaControl";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("supportsMediaControl", supportsMediaControl));
        localVarQueryParameterBaseName = "supportsPersistentIdentifier";
        localVarQueryParams
                .addAll(ApiClient.parameterToPairs("supportsPersistentIdentifier", supportsPersistentIdentifier));

        if (!localVarQueryParams.isEmpty() || localVarQueryStringJoiner.length() != 0) {
            StringJoiner queryJoiner = new StringJoiner("&");
            localVarQueryParams.forEach(p -> queryJoiner.add(p.getName() + '=' + p.getValue()));
            if (localVarQueryStringJoiner.length() != 0) {
                queryJoiner.add(localVarQueryStringJoiner.toString());
            }
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath + '?' + queryJoiner.toString()));
        } else {
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));
        }

        localVarRequestBuilder.header("Accept", "application/json");

        localVarRequestBuilder.method("POST", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    /**
     * Updates capabilities for a device.
     * 
     * @param clientCapabilitiesDto The MediaBrowser.Model.Session.ClientCapabilities. (required)
     * @param id The session id. (optional)
     * @throws ApiException if fails to make API call
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
     */
    public ApiResponse<Void> postFullCapabilitiesWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable ClientCapabilitiesDto clientCapabilitiesDto,
            @org.eclipse.jdt.annotation.NonNull String id) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = postFullCapabilitiesRequestBuilder(clientCapabilitiesDto, id);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("postFullCapabilities", localVarResponse);
                }
                return new ApiResponse<>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
            } finally {
                // Drain the InputStream
                while (localVarResponse.body().read() != -1) {
                    // Ignore
                }
                localVarResponse.body().close();
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder postFullCapabilitiesRequestBuilder(
            @org.eclipse.jdt.annotation.Nullable ClientCapabilitiesDto clientCapabilitiesDto,
            @org.eclipse.jdt.annotation.NonNull String id) throws ApiException {
        // verify the required parameter 'clientCapabilitiesDto' is set
        if (clientCapabilitiesDto == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'clientCapabilitiesDto' when calling postFullCapabilities");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Sessions/Capabilities/Full";

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "id";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("id", id));

        if (!localVarQueryParams.isEmpty() || localVarQueryStringJoiner.length() != 0) {
            StringJoiner queryJoiner = new StringJoiner("&");
            localVarQueryParams.forEach(p -> queryJoiner.add(p.getName() + '=' + p.getValue()));
            if (localVarQueryStringJoiner.length() != 0) {
                queryJoiner.add(localVarQueryStringJoiner.toString());
            }
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath + '?' + queryJoiner.toString()));
        } else {
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));
        }

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept", "application/json");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(clientCapabilitiesDto);
            localVarRequestBuilder.method("POST", HttpRequest.BodyPublishers.ofByteArray(localVarPostBody));
        } catch (IOException e) {
            throw new ApiException(e);
        }
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    /**
     * Removes an additional user from a session.
     * 
     * @param sessionId The session id. (required)
     * @param userId The user id. (required)
     * @throws ApiException if fails to make API call
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
     */
    public ApiResponse<Void> removeUserFromSessionWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String sessionId,
            @org.eclipse.jdt.annotation.Nullable UUID userId) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = removeUserFromSessionRequestBuilder(sessionId, userId);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("removeUserFromSession", localVarResponse);
                }
                return new ApiResponse<>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
            } finally {
                // Drain the InputStream
                while (localVarResponse.body().read() != -1) {
                    // Ignore
                }
                localVarResponse.body().close();
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder removeUserFromSessionRequestBuilder(
            @org.eclipse.jdt.annotation.Nullable String sessionId, @org.eclipse.jdt.annotation.Nullable UUID userId)
            throws ApiException {
        // verify the required parameter 'sessionId' is set
        if (sessionId == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'sessionId' when calling removeUserFromSession");
        }
        // verify the required parameter 'userId' is set
        if (userId == null) {
            throw new ApiException(400, "Missing the required parameter 'userId' when calling removeUserFromSession");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Sessions/{sessionId}/User/{userId}"
                .replace("{sessionId}", ApiClient.urlEncode(sessionId.toString()))
                .replace("{userId}", ApiClient.urlEncode(userId.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Accept", "application/json");

        localVarRequestBuilder.method("DELETE", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    /**
     * Reports that a session has ended.
     * 
     * @throws ApiException if fails to make API call
     */
    public void reportSessionEnded() throws ApiException {
        reportSessionEndedWithHttpInfo();
    }

    /**
     * Reports that a session has ended.
     * 
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> reportSessionEndedWithHttpInfo() throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = reportSessionEndedRequestBuilder();
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("reportSessionEnded", localVarResponse);
                }
                return new ApiResponse<>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
            } finally {
                // Drain the InputStream
                while (localVarResponse.body().read() != -1) {
                    // Ignore
                }
                localVarResponse.body().close();
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder reportSessionEndedRequestBuilder() throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Sessions/Logout";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Accept", "application/json");

        localVarRequestBuilder.method("POST", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    /**
     * Reports that a session is viewing an item.
     * 
     * @param itemId The item id. (required)
     * @param sessionId The session id. (optional)
     * @throws ApiException if fails to make API call
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
     */
    public ApiResponse<Void> reportViewingWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String itemId,
            @org.eclipse.jdt.annotation.NonNull String sessionId) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = reportViewingRequestBuilder(itemId, sessionId);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("reportViewing", localVarResponse);
                }
                return new ApiResponse<>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
            } finally {
                // Drain the InputStream
                while (localVarResponse.body().read() != -1) {
                    // Ignore
                }
                localVarResponse.body().close();
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder reportViewingRequestBuilder(@org.eclipse.jdt.annotation.Nullable String itemId,
            @org.eclipse.jdt.annotation.NonNull String sessionId) throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling reportViewing");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Sessions/Viewing";

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "sessionId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("sessionId", sessionId));
        localVarQueryParameterBaseName = "itemId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("itemId", itemId));

        if (!localVarQueryParams.isEmpty() || localVarQueryStringJoiner.length() != 0) {
            StringJoiner queryJoiner = new StringJoiner("&");
            localVarQueryParams.forEach(p -> queryJoiner.add(p.getName() + '=' + p.getValue()));
            if (localVarQueryStringJoiner.length() != 0) {
                queryJoiner.add(localVarQueryStringJoiner.toString());
            }
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath + '?' + queryJoiner.toString()));
        } else {
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));
        }

        localVarRequestBuilder.header("Accept", "application/json");

        localVarRequestBuilder.method("POST", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    /**
     * Issues a full general command to a client.
     * 
     * @param sessionId The session id. (required)
     * @param generalCommand The MediaBrowser.Model.Session.GeneralCommand. (required)
     * @throws ApiException if fails to make API call
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
     */
    public ApiResponse<Void> sendFullGeneralCommandWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String sessionId,
            @org.eclipse.jdt.annotation.Nullable GeneralCommand generalCommand) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = sendFullGeneralCommandRequestBuilder(sessionId, generalCommand);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("sendFullGeneralCommand", localVarResponse);
                }
                return new ApiResponse<>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
            } finally {
                // Drain the InputStream
                while (localVarResponse.body().read() != -1) {
                    // Ignore
                }
                localVarResponse.body().close();
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder sendFullGeneralCommandRequestBuilder(
            @org.eclipse.jdt.annotation.Nullable String sessionId,
            @org.eclipse.jdt.annotation.Nullable GeneralCommand generalCommand) throws ApiException {
        // verify the required parameter 'sessionId' is set
        if (sessionId == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'sessionId' when calling sendFullGeneralCommand");
        }
        // verify the required parameter 'generalCommand' is set
        if (generalCommand == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'generalCommand' when calling sendFullGeneralCommand");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Sessions/{sessionId}/Command".replace("{sessionId}",
                ApiClient.urlEncode(sessionId.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept", "application/json");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(generalCommand);
            localVarRequestBuilder.method("POST", HttpRequest.BodyPublishers.ofByteArray(localVarPostBody));
        } catch (IOException e) {
            throw new ApiException(e);
        }
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    /**
     * Issues a general command to a client.
     * 
     * @param sessionId The session id. (required)
     * @param command The command to send. (required)
     * @throws ApiException if fails to make API call
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
     */
    public ApiResponse<Void> sendGeneralCommandWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String sessionId,
            @org.eclipse.jdt.annotation.Nullable GeneralCommandType command) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = sendGeneralCommandRequestBuilder(sessionId, command);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("sendGeneralCommand", localVarResponse);
                }
                return new ApiResponse<>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
            } finally {
                // Drain the InputStream
                while (localVarResponse.body().read() != -1) {
                    // Ignore
                }
                localVarResponse.body().close();
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder sendGeneralCommandRequestBuilder(@org.eclipse.jdt.annotation.Nullable String sessionId,
            @org.eclipse.jdt.annotation.Nullable GeneralCommandType command) throws ApiException {
        // verify the required parameter 'sessionId' is set
        if (sessionId == null) {
            throw new ApiException(400, "Missing the required parameter 'sessionId' when calling sendGeneralCommand");
        }
        // verify the required parameter 'command' is set
        if (command == null) {
            throw new ApiException(400, "Missing the required parameter 'command' when calling sendGeneralCommand");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Sessions/{sessionId}/Command/{command}"
                .replace("{sessionId}", ApiClient.urlEncode(sessionId.toString()))
                .replace("{command}", ApiClient.urlEncode(command.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Accept", "application/json");

        localVarRequestBuilder.method("POST", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    /**
     * Issues a command to a client to display a message to the user.
     * 
     * @param sessionId The session id. (required)
     * @param messageCommand The MediaBrowser.Model.Session.MessageCommand object containing Header, Message Text, and
     *            TimeoutMs. (required)
     * @throws ApiException if fails to make API call
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
     */
    public ApiResponse<Void> sendMessageCommandWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String sessionId,
            @org.eclipse.jdt.annotation.Nullable MessageCommand messageCommand) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = sendMessageCommandRequestBuilder(sessionId, messageCommand);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("sendMessageCommand", localVarResponse);
                }
                return new ApiResponse<>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
            } finally {
                // Drain the InputStream
                while (localVarResponse.body().read() != -1) {
                    // Ignore
                }
                localVarResponse.body().close();
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder sendMessageCommandRequestBuilder(@org.eclipse.jdt.annotation.Nullable String sessionId,
            @org.eclipse.jdt.annotation.Nullable MessageCommand messageCommand) throws ApiException {
        // verify the required parameter 'sessionId' is set
        if (sessionId == null) {
            throw new ApiException(400, "Missing the required parameter 'sessionId' when calling sendMessageCommand");
        }
        // verify the required parameter 'messageCommand' is set
        if (messageCommand == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'messageCommand' when calling sendMessageCommand");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Sessions/{sessionId}/Message".replace("{sessionId}",
                ApiClient.urlEncode(sessionId.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept", "application/json");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(messageCommand);
            localVarRequestBuilder.method("POST", HttpRequest.BodyPublishers.ofByteArray(localVarPostBody));
        } catch (IOException e) {
            throw new ApiException(e);
        }
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    /**
     * Issues a playstate command to a client.
     * 
     * @param sessionId The session id. (required)
     * @param command The MediaBrowser.Model.Session.PlaystateCommand. (required)
     * @param seekPositionTicks The optional position ticks. (optional)
     * @param controllingUserId The optional controlling user id. (optional)
     * @throws ApiException if fails to make API call
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
     */
    public ApiResponse<Void> sendPlaystateCommandWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String sessionId,
            @org.eclipse.jdt.annotation.Nullable PlaystateCommand command,
            @org.eclipse.jdt.annotation.NonNull Long seekPositionTicks,
            @org.eclipse.jdt.annotation.NonNull String controllingUserId) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = sendPlaystateCommandRequestBuilder(sessionId, command,
                seekPositionTicks, controllingUserId);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("sendPlaystateCommand", localVarResponse);
                }
                return new ApiResponse<>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
            } finally {
                // Drain the InputStream
                while (localVarResponse.body().read() != -1) {
                    // Ignore
                }
                localVarResponse.body().close();
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder sendPlaystateCommandRequestBuilder(
            @org.eclipse.jdt.annotation.Nullable String sessionId,
            @org.eclipse.jdt.annotation.Nullable PlaystateCommand command,
            @org.eclipse.jdt.annotation.NonNull Long seekPositionTicks,
            @org.eclipse.jdt.annotation.NonNull String controllingUserId) throws ApiException {
        // verify the required parameter 'sessionId' is set
        if (sessionId == null) {
            throw new ApiException(400, "Missing the required parameter 'sessionId' when calling sendPlaystateCommand");
        }
        // verify the required parameter 'command' is set
        if (command == null) {
            throw new ApiException(400, "Missing the required parameter 'command' when calling sendPlaystateCommand");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Sessions/{sessionId}/Playing/{command}"
                .replace("{sessionId}", ApiClient.urlEncode(sessionId.toString()))
                .replace("{command}", ApiClient.urlEncode(command.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "seekPositionTicks";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("seekPositionTicks", seekPositionTicks));
        localVarQueryParameterBaseName = "controllingUserId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("controllingUserId", controllingUserId));

        if (!localVarQueryParams.isEmpty() || localVarQueryStringJoiner.length() != 0) {
            StringJoiner queryJoiner = new StringJoiner("&");
            localVarQueryParams.forEach(p -> queryJoiner.add(p.getName() + '=' + p.getValue()));
            if (localVarQueryStringJoiner.length() != 0) {
                queryJoiner.add(localVarQueryStringJoiner.toString());
            }
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath + '?' + queryJoiner.toString()));
        } else {
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));
        }

        localVarRequestBuilder.header("Accept", "application/json");

        localVarRequestBuilder.method("POST", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    /**
     * Issues a system command to a client.
     * 
     * @param sessionId The session id. (required)
     * @param command The command to send. (required)
     * @throws ApiException if fails to make API call
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
     */
    public ApiResponse<Void> sendSystemCommandWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String sessionId,
            @org.eclipse.jdt.annotation.Nullable GeneralCommandType command) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = sendSystemCommandRequestBuilder(sessionId, command);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("sendSystemCommand", localVarResponse);
                }
                return new ApiResponse<>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
            } finally {
                // Drain the InputStream
                while (localVarResponse.body().read() != -1) {
                    // Ignore
                }
                localVarResponse.body().close();
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder sendSystemCommandRequestBuilder(@org.eclipse.jdt.annotation.Nullable String sessionId,
            @org.eclipse.jdt.annotation.Nullable GeneralCommandType command) throws ApiException {
        // verify the required parameter 'sessionId' is set
        if (sessionId == null) {
            throw new ApiException(400, "Missing the required parameter 'sessionId' when calling sendSystemCommand");
        }
        // verify the required parameter 'command' is set
        if (command == null) {
            throw new ApiException(400, "Missing the required parameter 'command' when calling sendSystemCommand");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Sessions/{sessionId}/System/{command}"
                .replace("{sessionId}", ApiClient.urlEncode(sessionId.toString()))
                .replace("{command}", ApiClient.urlEncode(command.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Accept", "application/json");

        localVarRequestBuilder.method("POST", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }
}
