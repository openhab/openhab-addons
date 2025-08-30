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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.UUID;
import java.util.function.Consumer;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;
import org.openhab.binding.jellyfin.internal.api.generated.ApiException;
import org.openhab.binding.jellyfin.internal.api.generated.ApiResponse;
import org.openhab.binding.jellyfin.internal.api.generated.Configuration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class VideoAttachmentsApi {
    private final HttpClient memberVarHttpClient;
    private final ObjectMapper memberVarObjectMapper;
    private final String memberVarBaseUri;
    private final Consumer<HttpRequest.Builder> memberVarInterceptor;
    private final Duration memberVarReadTimeout;
    private final Consumer<HttpResponse<InputStream>> memberVarResponseInterceptor;
    private final Consumer<HttpResponse<String>> memberVarAsyncResponseInterceptor;

    public VideoAttachmentsApi() {
        this(Configuration.getDefaultApiClient());
    }

    public VideoAttachmentsApi(ApiClient apiClient) {
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
     * Get video attachment.
     * 
     * @param videoId Video ID. (required)
     * @param mediaSourceId Media Source ID. (required)
     * @param index Attachment Index. (required)
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getAttachment(@org.eclipse.jdt.annotation.Nullable UUID videoId,
            @org.eclipse.jdt.annotation.Nullable String mediaSourceId,
            @org.eclipse.jdt.annotation.Nullable Integer index) throws ApiException {
        ApiResponse<File> localVarResponse = getAttachmentWithHttpInfo(videoId, mediaSourceId, index);
        return localVarResponse.getData();
    }

    /**
     * Get video attachment.
     * 
     * @param videoId Video ID. (required)
     * @param mediaSourceId Media Source ID. (required)
     * @param index Attachment Index. (required)
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getAttachmentWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID videoId,
            @org.eclipse.jdt.annotation.Nullable String mediaSourceId,
            @org.eclipse.jdt.annotation.Nullable Integer index) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getAttachmentRequestBuilder(videoId, mediaSourceId, index);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getAttachment", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody, new TypeReference<File>() {
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

    private HttpRequest.Builder getAttachmentRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID videoId,
            @org.eclipse.jdt.annotation.Nullable String mediaSourceId,
            @org.eclipse.jdt.annotation.Nullable Integer index) throws ApiException {
        // verify the required parameter 'videoId' is set
        if (videoId == null) {
            throw new ApiException(400, "Missing the required parameter 'videoId' when calling getAttachment");
        }
        // verify the required parameter 'mediaSourceId' is set
        if (mediaSourceId == null) {
            throw new ApiException(400, "Missing the required parameter 'mediaSourceId' when calling getAttachment");
        }
        // verify the required parameter 'index' is set
        if (index == null) {
            throw new ApiException(400, "Missing the required parameter 'index' when calling getAttachment");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Videos/{videoId}/{mediaSourceId}/Attachments/{index}"
                .replace("{videoId}", ApiClient.urlEncode(videoId.toString()))
                .replace("{mediaSourceId}", ApiClient.urlEncode(mediaSourceId.toString()))
                .replace("{index}", ApiClient.urlEncode(index.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Accept",
                "application/octet-stream, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase");

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
