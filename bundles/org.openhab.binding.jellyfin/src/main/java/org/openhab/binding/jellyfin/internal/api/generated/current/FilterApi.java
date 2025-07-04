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
import org.openhab.binding.jellyfin.internal.api.generated.current.model.MediaType;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.QueryFilters;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.QueryFiltersLegacy;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class FilterApi {
    private final HttpClient memberVarHttpClient;
    private final ObjectMapper memberVarObjectMapper;
    private final String memberVarBaseUri;
    private final Consumer<HttpRequest.Builder> memberVarInterceptor;
    private final Duration memberVarReadTimeout;
    private final Consumer<HttpResponse<InputStream>> memberVarResponseInterceptor;
    private final Consumer<HttpResponse<String>> memberVarAsyncResponseInterceptor;

    public FilterApi() {
        this(Configuration.getDefaultApiClient());
    }

    public FilterApi(ApiClient apiClient) {
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
     * Gets query filters.
     * 
     * @param userId Optional. User id. (optional)
     * @param parentId Optional. Specify this to localize the search to a specific item or folder. Omit to use the root.
     *            (optional)
     * @param includeItemTypes Optional. If specified, results will be filtered based on item type. This allows
     *            multiple, comma delimited. (optional)
     * @param isAiring Optional. Is item airing. (optional)
     * @param isMovie Optional. Is item movie. (optional)
     * @param isSports Optional. Is item sports. (optional)
     * @param isKids Optional. Is item kids. (optional)
     * @param isNews Optional. Is item news. (optional)
     * @param isSeries Optional. Is item series. (optional)
     * @param recursive Optional. Search recursive. (optional)
     * @return QueryFilters
     * @throws ApiException if fails to make API call
     */
    public QueryFilters getQueryFilters(@org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull UUID parentId,
            @org.eclipse.jdt.annotation.NonNull List<BaseItemKind> includeItemTypes,
            @org.eclipse.jdt.annotation.NonNull Boolean isAiring, @org.eclipse.jdt.annotation.NonNull Boolean isMovie,
            @org.eclipse.jdt.annotation.NonNull Boolean isSports, @org.eclipse.jdt.annotation.NonNull Boolean isKids,
            @org.eclipse.jdt.annotation.NonNull Boolean isNews, @org.eclipse.jdt.annotation.NonNull Boolean isSeries,
            @org.eclipse.jdt.annotation.NonNull Boolean recursive) throws ApiException {
        ApiResponse<QueryFilters> localVarResponse = getQueryFiltersWithHttpInfo(userId, parentId, includeItemTypes,
                isAiring, isMovie, isSports, isKids, isNews, isSeries, recursive);
        return localVarResponse.getData();
    }

    /**
     * Gets query filters.
     * 
     * @param userId Optional. User id. (optional)
     * @param parentId Optional. Specify this to localize the search to a specific item or folder. Omit to use the root.
     *            (optional)
     * @param includeItemTypes Optional. If specified, results will be filtered based on item type. This allows
     *            multiple, comma delimited. (optional)
     * @param isAiring Optional. Is item airing. (optional)
     * @param isMovie Optional. Is item movie. (optional)
     * @param isSports Optional. Is item sports. (optional)
     * @param isKids Optional. Is item kids. (optional)
     * @param isNews Optional. Is item news. (optional)
     * @param isSeries Optional. Is item series. (optional)
     * @param recursive Optional. Search recursive. (optional)
     * @return ApiResponse&lt;QueryFilters&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<QueryFilters> getQueryFiltersWithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull UUID parentId,
            @org.eclipse.jdt.annotation.NonNull List<BaseItemKind> includeItemTypes,
            @org.eclipse.jdt.annotation.NonNull Boolean isAiring, @org.eclipse.jdt.annotation.NonNull Boolean isMovie,
            @org.eclipse.jdt.annotation.NonNull Boolean isSports, @org.eclipse.jdt.annotation.NonNull Boolean isKids,
            @org.eclipse.jdt.annotation.NonNull Boolean isNews, @org.eclipse.jdt.annotation.NonNull Boolean isSeries,
            @org.eclipse.jdt.annotation.NonNull Boolean recursive) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getQueryFiltersRequestBuilder(userId, parentId, includeItemTypes,
                isAiring, isMovie, isSports, isKids, isNews, isSeries, recursive);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getQueryFilters", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<QueryFilters>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<QueryFilters>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody, new TypeReference<QueryFilters>() {
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

    private HttpRequest.Builder getQueryFiltersRequestBuilder(@org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull UUID parentId,
            @org.eclipse.jdt.annotation.NonNull List<BaseItemKind> includeItemTypes,
            @org.eclipse.jdt.annotation.NonNull Boolean isAiring, @org.eclipse.jdt.annotation.NonNull Boolean isMovie,
            @org.eclipse.jdt.annotation.NonNull Boolean isSports, @org.eclipse.jdt.annotation.NonNull Boolean isKids,
            @org.eclipse.jdt.annotation.NonNull Boolean isNews, @org.eclipse.jdt.annotation.NonNull Boolean isSeries,
            @org.eclipse.jdt.annotation.NonNull Boolean recursive) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Items/Filters2";

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "userId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("userId", userId));
        localVarQueryParameterBaseName = "parentId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("parentId", parentId));
        localVarQueryParameterBaseName = "includeItemTypes";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "includeItemTypes", includeItemTypes));
        localVarQueryParameterBaseName = "isAiring";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("isAiring", isAiring));
        localVarQueryParameterBaseName = "isMovie";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("isMovie", isMovie));
        localVarQueryParameterBaseName = "isSports";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("isSports", isSports));
        localVarQueryParameterBaseName = "isKids";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("isKids", isKids));
        localVarQueryParameterBaseName = "isNews";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("isNews", isNews));
        localVarQueryParameterBaseName = "isSeries";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("isSeries", isSeries));
        localVarQueryParameterBaseName = "recursive";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("recursive", recursive));

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
     * Gets legacy query filters.
     * 
     * @param userId Optional. User id. (optional)
     * @param parentId Optional. Parent id. (optional)
     * @param includeItemTypes Optional. If specified, results will be filtered based on item type. This allows
     *            multiple, comma delimited. (optional)
     * @param mediaTypes Optional. Filter by MediaType. Allows multiple, comma delimited. (optional)
     * @return QueryFiltersLegacy
     * @throws ApiException if fails to make API call
     */
    public QueryFiltersLegacy getQueryFiltersLegacy(@org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull UUID parentId,
            @org.eclipse.jdt.annotation.NonNull List<BaseItemKind> includeItemTypes,
            @org.eclipse.jdt.annotation.NonNull List<MediaType> mediaTypes) throws ApiException {
        ApiResponse<QueryFiltersLegacy> localVarResponse = getQueryFiltersLegacyWithHttpInfo(userId, parentId,
                includeItemTypes, mediaTypes);
        return localVarResponse.getData();
    }

    /**
     * Gets legacy query filters.
     * 
     * @param userId Optional. User id. (optional)
     * @param parentId Optional. Parent id. (optional)
     * @param includeItemTypes Optional. If specified, results will be filtered based on item type. This allows
     *            multiple, comma delimited. (optional)
     * @param mediaTypes Optional. Filter by MediaType. Allows multiple, comma delimited. (optional)
     * @return ApiResponse&lt;QueryFiltersLegacy&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<QueryFiltersLegacy> getQueryFiltersLegacyWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull UUID userId, @org.eclipse.jdt.annotation.NonNull UUID parentId,
            @org.eclipse.jdt.annotation.NonNull List<BaseItemKind> includeItemTypes,
            @org.eclipse.jdt.annotation.NonNull List<MediaType> mediaTypes) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getQueryFiltersLegacyRequestBuilder(userId, parentId,
                includeItemTypes, mediaTypes);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getQueryFiltersLegacy", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<QueryFiltersLegacy>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<QueryFiltersLegacy>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody,
                                        new TypeReference<QueryFiltersLegacy>() {
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

    private HttpRequest.Builder getQueryFiltersLegacyRequestBuilder(@org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull UUID parentId,
            @org.eclipse.jdt.annotation.NonNull List<BaseItemKind> includeItemTypes,
            @org.eclipse.jdt.annotation.NonNull List<MediaType> mediaTypes) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Items/Filters";

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "userId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("userId", userId));
        localVarQueryParameterBaseName = "parentId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("parentId", parentId));
        localVarQueryParameterBaseName = "includeItemTypes";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "includeItemTypes", includeItemTypes));
        localVarQueryParameterBaseName = "mediaTypes";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "mediaTypes", mediaTypes));

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
}
