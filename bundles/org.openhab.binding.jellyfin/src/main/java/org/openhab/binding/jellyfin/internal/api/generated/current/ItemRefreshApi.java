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
import org.openhab.binding.jellyfin.internal.api.generated.current.model.MetadataRefreshMode;

import com.fasterxml.jackson.databind.ObjectMapper;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class ItemRefreshApi {
    private final HttpClient memberVarHttpClient;
    private final ObjectMapper memberVarObjectMapper;
    private final String memberVarBaseUri;
    private final Consumer<HttpRequest.Builder> memberVarInterceptor;
    private final Duration memberVarReadTimeout;
    private final Consumer<HttpResponse<InputStream>> memberVarResponseInterceptor;
    private final Consumer<HttpResponse<String>> memberVarAsyncResponseInterceptor;

    public ItemRefreshApi() {
        this(Configuration.getDefaultApiClient());
    }

    public ItemRefreshApi(ApiClient apiClient) {
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
     * Refreshes metadata for an item.
     * 
     * @param itemId Item id. (required)
     * @param metadataRefreshMode (Optional) Specifies the metadata refresh mode. (optional, default to None)
     * @param imageRefreshMode (Optional) Specifies the image refresh mode. (optional, default to None)
     * @param replaceAllMetadata (Optional) Determines if metadata should be replaced. Only applicable if mode is
     *            FullRefresh. (optional, default to false)
     * @param replaceAllImages (Optional) Determines if images should be replaced. Only applicable if mode is
     *            FullRefresh. (optional, default to false)
     * @param regenerateTrickplay (Optional) Determines if trickplay images should be replaced. Only applicable if mode
     *            is FullRefresh. (optional, default to false)
     * @throws ApiException if fails to make API call
     */
    public void refreshItem(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull MetadataRefreshMode metadataRefreshMode,
            @org.eclipse.jdt.annotation.NonNull MetadataRefreshMode imageRefreshMode,
            @org.eclipse.jdt.annotation.NonNull Boolean replaceAllMetadata,
            @org.eclipse.jdt.annotation.NonNull Boolean replaceAllImages,
            @org.eclipse.jdt.annotation.NonNull Boolean regenerateTrickplay) throws ApiException {
        refreshItemWithHttpInfo(itemId, metadataRefreshMode, imageRefreshMode, replaceAllMetadata, replaceAllImages,
                regenerateTrickplay);
    }

    /**
     * Refreshes metadata for an item.
     * 
     * @param itemId Item id. (required)
     * @param metadataRefreshMode (Optional) Specifies the metadata refresh mode. (optional, default to None)
     * @param imageRefreshMode (Optional) Specifies the image refresh mode. (optional, default to None)
     * @param replaceAllMetadata (Optional) Determines if metadata should be replaced. Only applicable if mode is
     *            FullRefresh. (optional, default to false)
     * @param replaceAllImages (Optional) Determines if images should be replaced. Only applicable if mode is
     *            FullRefresh. (optional, default to false)
     * @param regenerateTrickplay (Optional) Determines if trickplay images should be replaced. Only applicable if mode
     *            is FullRefresh. (optional, default to false)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> refreshItemWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull MetadataRefreshMode metadataRefreshMode,
            @org.eclipse.jdt.annotation.NonNull MetadataRefreshMode imageRefreshMode,
            @org.eclipse.jdt.annotation.NonNull Boolean replaceAllMetadata,
            @org.eclipse.jdt.annotation.NonNull Boolean replaceAllImages,
            @org.eclipse.jdt.annotation.NonNull Boolean regenerateTrickplay) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = refreshItemRequestBuilder(itemId, metadataRefreshMode,
                imageRefreshMode, replaceAllMetadata, replaceAllImages, regenerateTrickplay);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("refreshItem", localVarResponse);
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

    private HttpRequest.Builder refreshItemRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull MetadataRefreshMode metadataRefreshMode,
            @org.eclipse.jdt.annotation.NonNull MetadataRefreshMode imageRefreshMode,
            @org.eclipse.jdt.annotation.NonNull Boolean replaceAllMetadata,
            @org.eclipse.jdt.annotation.NonNull Boolean replaceAllImages,
            @org.eclipse.jdt.annotation.NonNull Boolean regenerateTrickplay) throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling refreshItem");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Items/{itemId}/Refresh".replace("{itemId}", ApiClient.urlEncode(itemId.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "metadataRefreshMode";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("metadataRefreshMode", metadataRefreshMode));
        localVarQueryParameterBaseName = "imageRefreshMode";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("imageRefreshMode", imageRefreshMode));
        localVarQueryParameterBaseName = "replaceAllMetadata";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("replaceAllMetadata", replaceAllMetadata));
        localVarQueryParameterBaseName = "replaceAllImages";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("replaceAllImages", replaceAllImages));
        localVarQueryParameterBaseName = "regenerateTrickplay";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("regenerateTrickplay", regenerateTrickplay));

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
