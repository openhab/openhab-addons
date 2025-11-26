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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    /**
     * Utility class for extending HttpRequest.Builder functionality.
     */
    private static class HttpRequestBuilderExtensions {
        /**
         * Adds additional headers to the provided HttpRequest.Builder. Useful for adding method/endpoint specific
         * headers.
         *
         * @param builder the HttpRequest.Builder to which headers will be added
         * @param headers a map of header names and values to add; may be null
         * @return the same HttpRequest.Builder instance with the additional headers set
         */
        static HttpRequest.Builder withAdditionalHeaders(HttpRequest.Builder builder, Map<String, String> headers) {
            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    builder.header(entry.getKey(), entry.getValue());
                }
            }
            return builder;
        }
    }

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
     * Download file from the given response.
     *
     * @param response Response
     * @return File
     * @throws ApiException If fail to read file content from response and write to disk
     */
    public File downloadFileFromResponse(HttpResponse<InputStream> response) throws ApiException {
        try {
            File file = prepareDownloadFile(response);
            java.nio.file.Files.copy(response.body(), file.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            return file;
        } catch (IOException e) {
            throw new ApiException(e);
        }
    }

    /**
     * <p>
     * Prepare the file for download from the response.
     * </p>
     *
     * @param response a {@link java.net.http.HttpResponse} object.
     * @return a {@link java.io.File} object.
     * @throws java.io.IOException if any.
     */
    private File prepareDownloadFile(HttpResponse<InputStream> response) throws IOException {
        String filename = null;
        java.util.Optional<String> contentDisposition = response.headers().firstValue("Content-Disposition");
        if (contentDisposition.isPresent() && !"".equals(contentDisposition.get())) {
            // Get filename from the Content-Disposition header.
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("filename=['\"]?([^'\"\\s]+)['\"]?");
            java.util.regex.Matcher matcher = pattern.matcher(contentDisposition.get());
            if (matcher.find())
                filename = matcher.group(1);
        }
        File file = null;
        if (filename != null) {
            java.nio.file.Path tempDir = java.nio.file.Files.createTempDirectory("swagger-gen-native");
            java.nio.file.Path filePath = java.nio.file.Files.createFile(tempDir.resolve(filename));
            file = filePath.toFile();
            tempDir.toFile().deleteOnExit(); // best effort cleanup
            file.deleteOnExit(); // best effort cleanup
        } else {
            file = java.nio.file.Files.createTempFile("download-", "").toFile();
            file.deleteOnExit(); // best effort cleanup
        }
        return file;
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
        return getMovieRecommendations(userId, parentId, fields, categoryLimit, itemLimit, null);
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
     * @param headers Optional headers to include in the request
     * @return List&lt;RecommendationDto&gt;
     * @throws ApiException if fails to make API call
     */
    public List<RecommendationDto> getMovieRecommendations(@org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull UUID parentId,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull Integer categoryLimit,
            @org.eclipse.jdt.annotation.NonNull Integer itemLimit, Map<String, String> headers) throws ApiException {
        ApiResponse<List<RecommendationDto>> localVarResponse = getMovieRecommendationsWithHttpInfo(userId, parentId,
                fields, categoryLimit, itemLimit, headers);
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
        return getMovieRecommendationsWithHttpInfo(userId, parentId, fields, categoryLimit, itemLimit, null);
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
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;List&lt;RecommendationDto&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<RecommendationDto>> getMovieRecommendationsWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull UUID userId, @org.eclipse.jdt.annotation.NonNull UUID parentId,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull Integer categoryLimit,
            @org.eclipse.jdt.annotation.NonNull Integer itemLimit, Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getMovieRecommendationsRequestBuilder(userId, parentId, fields,
                categoryLimit, itemLimit, headers);
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
                List<RecommendationDto> responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<List<RecommendationDto>>() {
                        });

                localVarResponse.body().close();

                return new ApiResponse<List<RecommendationDto>>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(), responseValue);
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
            @org.eclipse.jdt.annotation.NonNull Integer itemLimit, Map<String, String> headers) throws ApiException {

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
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

        localVarRequestBuilder.method("GET", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        // Add custom headers if provided
        localVarRequestBuilder = HttpRequestBuilderExtensions.withAdditionalHeaders(localVarRequestBuilder, headers);
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }
}
