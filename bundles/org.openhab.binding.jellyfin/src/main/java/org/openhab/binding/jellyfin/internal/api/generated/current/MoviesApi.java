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
import org.openhab.binding.jellyfin.internal.api.generated.current.model.ItemFields;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.RecommendationDto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class MoviesApi {
    private final HttpClient memberVarHttpClient;
    private final ObjectMapper memberVarObjectMapper;
    private final String memberVarBaseUri;
    private final Consumer<HttpRequest.Builder> memberVarInterceptor;
    private final Duration memberVarReadTimeout;
    private final Consumer<HttpResponse<InputStream>> memberVarResponseInterceptor;
    private final Consumer<HttpResponse<String>> memberVarAsyncResponseInterceptor;

    public MoviesApi() {
        this(Configuration.getDefaultApiClient());
    }

    public MoviesApi(ApiClient apiClient) {
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
     * Gets movie recommendations.
     * 
     * @param userId Optional. Filter by user id, and attach user data. (optional)
     * @param parentId Specify this to localize the search to a specific item or folder. Omit to use the root.
     *            (optional)
     * @param fields Optional. The fields to return. (optional)
     * @param categoryLimit The max number of categories to return. (optional, default to 5)
     * @param itemLimit The max number of items to return per category. (optional, default to 8)
     * @return List&lt;RecommendationDto&gt;
     * @throws ApiException if fails to make API call
     */
    public List<RecommendationDto> getMovieRecommendations(@org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull UUID parentId,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull Integer categoryLimit,
            @org.eclipse.jdt.annotation.NonNull Integer itemLimit) throws ApiException {
        ApiResponse<List<RecommendationDto>> localVarResponse = getMovieRecommendationsWithHttpInfo(userId, parentId,
                fields, categoryLimit, itemLimit);
        return localVarResponse.getData();
    }

    /**
     * Gets movie recommendations.
     * 
     * @param userId Optional. Filter by user id, and attach user data. (optional)
     * @param parentId Specify this to localize the search to a specific item or folder. Omit to use the root.
     *            (optional)
     * @param fields Optional. The fields to return. (optional)
     * @param categoryLimit The max number of categories to return. (optional, default to 5)
     * @param itemLimit The max number of items to return per category. (optional, default to 8)
     * @return ApiResponse&lt;List&lt;RecommendationDto&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<RecommendationDto>> getMovieRecommendationsWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull UUID userId, @org.eclipse.jdt.annotation.NonNull UUID parentId,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull Integer categoryLimit,
            @org.eclipse.jdt.annotation.NonNull Integer itemLimit) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getMovieRecommendationsRequestBuilder(userId, parentId, fields,
                categoryLimit, itemLimit);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getMovieRecommendations", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<List<RecommendationDto>>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<List<RecommendationDto>>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody,
                                        new TypeReference<List<RecommendationDto>>() {
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

    private HttpRequest.Builder getMovieRecommendationsRequestBuilder(@org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull UUID parentId,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull Integer categoryLimit,
            @org.eclipse.jdt.annotation.NonNull Integer itemLimit) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Movies/Recommendations";

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "userId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("userId", userId));
        localVarQueryParameterBaseName = "parentId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("parentId", parentId));
        localVarQueryParameterBaseName = "fields";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "fields", fields));
        localVarQueryParameterBaseName = "categoryLimit";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("categoryLimit", categoryLimit));
        localVarQueryParameterBaseName = "itemLimit";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("itemLimit", itemLimit));

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
