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
package org.openhab.binding.jellyfin.internal.thirdparty.api.current;

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

import org.openhab.binding.jellyfin.internal.thirdparty.api.ApiClient;
import org.openhab.binding.jellyfin.internal.thirdparty.api.ApiException;
import org.openhab.binding.jellyfin.internal.thirdparty.api.ApiResponse;
import org.openhab.binding.jellyfin.internal.thirdparty.api.Configuration;
import org.openhab.binding.jellyfin.internal.thirdparty.api.Pair;
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.BaseItemKind;
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.MediaType;
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.QueryFilters;
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.QueryFiltersLegacy;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class FilterApi {
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
    private final Consumer<HttpResponse<InputStream>> memberVarAsyncResponseInterceptor;

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
        InputStream responseBody = ApiClient.getResponseBody(response);
        String body = null;
        try {
            body = responseBody == null ? null : new String(responseBody.readAllBytes());
        } finally {
            if (responseBody != null) {
                responseBody.close();
            }
        }
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
    public File downloadFileFromResponse(HttpResponse<InputStream> response, InputStream responseBody)
            throws ApiException {
        if (responseBody == null) {
            throw new ApiException(new IOException("Response body is empty"));
        }
        try {
            File file = prepareDownloadFile(response);
            java.nio.file.Files.copy(responseBody, file.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
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
    public QueryFilters getQueryFilters(@org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable UUID parentId,
            @org.eclipse.jdt.annotation.Nullable List<BaseItemKind> includeItemTypes,
            @org.eclipse.jdt.annotation.Nullable Boolean isAiring, @org.eclipse.jdt.annotation.Nullable Boolean isMovie,
            @org.eclipse.jdt.annotation.Nullable Boolean isSports, @org.eclipse.jdt.annotation.Nullable Boolean isKids,
            @org.eclipse.jdt.annotation.Nullable Boolean isNews, @org.eclipse.jdt.annotation.Nullable Boolean isSeries,
            @org.eclipse.jdt.annotation.Nullable Boolean recursive) throws ApiException {
        return getQueryFilters(userId, parentId, includeItemTypes, isAiring, isMovie, isSports, isKids, isNews,
                isSeries, recursive, null);
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
     * @param headers Optional headers to include in the request
     * @return QueryFilters
     * @throws ApiException if fails to make API call
     */
    public QueryFilters getQueryFilters(@org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable UUID parentId,
            @org.eclipse.jdt.annotation.Nullable List<BaseItemKind> includeItemTypes,
            @org.eclipse.jdt.annotation.Nullable Boolean isAiring, @org.eclipse.jdt.annotation.Nullable Boolean isMovie,
            @org.eclipse.jdt.annotation.Nullable Boolean isSports, @org.eclipse.jdt.annotation.Nullable Boolean isKids,
            @org.eclipse.jdt.annotation.Nullable Boolean isNews, @org.eclipse.jdt.annotation.Nullable Boolean isSeries,
            @org.eclipse.jdt.annotation.Nullable Boolean recursive, Map<String, String> headers) throws ApiException {
        ApiResponse<QueryFilters> localVarResponse = getQueryFiltersWithHttpInfo(userId, parentId, includeItemTypes,
                isAiring, isMovie, isSports, isKids, isNews, isSeries, recursive, headers);
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
    public ApiResponse<QueryFilters> getQueryFiltersWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable UUID parentId,
            @org.eclipse.jdt.annotation.Nullable List<BaseItemKind> includeItemTypes,
            @org.eclipse.jdt.annotation.Nullable Boolean isAiring, @org.eclipse.jdt.annotation.Nullable Boolean isMovie,
            @org.eclipse.jdt.annotation.Nullable Boolean isSports, @org.eclipse.jdt.annotation.Nullable Boolean isKids,
            @org.eclipse.jdt.annotation.Nullable Boolean isNews, @org.eclipse.jdt.annotation.Nullable Boolean isSeries,
            @org.eclipse.jdt.annotation.Nullable Boolean recursive) throws ApiException {
        return getQueryFiltersWithHttpInfo(userId, parentId, includeItemTypes, isAiring, isMovie, isSports, isKids,
                isNews, isSeries, recursive, null);
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
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;QueryFilters&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<QueryFilters> getQueryFiltersWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable UUID parentId,
            @org.eclipse.jdt.annotation.Nullable List<BaseItemKind> includeItemTypes,
            @org.eclipse.jdt.annotation.Nullable Boolean isAiring, @org.eclipse.jdt.annotation.Nullable Boolean isMovie,
            @org.eclipse.jdt.annotation.Nullable Boolean isSports, @org.eclipse.jdt.annotation.Nullable Boolean isKids,
            @org.eclipse.jdt.annotation.Nullable Boolean isNews, @org.eclipse.jdt.annotation.Nullable Boolean isSeries,
            @org.eclipse.jdt.annotation.Nullable Boolean recursive, Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getQueryFiltersRequestBuilder(userId, parentId, includeItemTypes,
                isAiring, isMovie, isSports, isKids, isNews, isSeries, recursive, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getQueryFilters", localVarResponse);
                }
                localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
                if (localVarResponseBody == null) {
                    return new ApiResponse<QueryFilters>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponseBody.readAllBytes());
                QueryFilters responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<QueryFilters>() {
                        });

                return new ApiResponse<QueryFilters>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                        responseValue);
            } finally {
                if (localVarResponseBody != null) {
                    localVarResponseBody.close();
                }
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder getQueryFiltersRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable UUID parentId,
            @org.eclipse.jdt.annotation.Nullable List<BaseItemKind> includeItemTypes,
            @org.eclipse.jdt.annotation.Nullable Boolean isAiring, @org.eclipse.jdt.annotation.Nullable Boolean isMovie,
            @org.eclipse.jdt.annotation.Nullable Boolean isSports, @org.eclipse.jdt.annotation.Nullable Boolean isKids,
            @org.eclipse.jdt.annotation.Nullable Boolean isNews, @org.eclipse.jdt.annotation.Nullable Boolean isSeries,
            @org.eclipse.jdt.annotation.Nullable Boolean recursive, Map<String, String> headers) throws ApiException {

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
    public QueryFiltersLegacy getQueryFiltersLegacy(@org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable UUID parentId,
            @org.eclipse.jdt.annotation.Nullable List<BaseItemKind> includeItemTypes,
            @org.eclipse.jdt.annotation.Nullable List<MediaType> mediaTypes) throws ApiException {
        return getQueryFiltersLegacy(userId, parentId, includeItemTypes, mediaTypes, null);
    }

    /**
     * Gets legacy query filters.
     * 
     * @param userId Optional. User id. (optional)
     * @param parentId Optional. Parent id. (optional)
     * @param includeItemTypes Optional. If specified, results will be filtered based on item type. This allows
     *            multiple, comma delimited. (optional)
     * @param mediaTypes Optional. Filter by MediaType. Allows multiple, comma delimited. (optional)
     * @param headers Optional headers to include in the request
     * @return QueryFiltersLegacy
     * @throws ApiException if fails to make API call
     */
    public QueryFiltersLegacy getQueryFiltersLegacy(@org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable UUID parentId,
            @org.eclipse.jdt.annotation.Nullable List<BaseItemKind> includeItemTypes,
            @org.eclipse.jdt.annotation.Nullable List<MediaType> mediaTypes, Map<String, String> headers)
            throws ApiException {
        ApiResponse<QueryFiltersLegacy> localVarResponse = getQueryFiltersLegacyWithHttpInfo(userId, parentId,
                includeItemTypes, mediaTypes, headers);
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
            @org.eclipse.jdt.annotation.Nullable UUID userId, @org.eclipse.jdt.annotation.Nullable UUID parentId,
            @org.eclipse.jdt.annotation.Nullable List<BaseItemKind> includeItemTypes,
            @org.eclipse.jdt.annotation.Nullable List<MediaType> mediaTypes) throws ApiException {
        return getQueryFiltersLegacyWithHttpInfo(userId, parentId, includeItemTypes, mediaTypes, null);
    }

    /**
     * Gets legacy query filters.
     * 
     * @param userId Optional. User id. (optional)
     * @param parentId Optional. Parent id. (optional)
     * @param includeItemTypes Optional. If specified, results will be filtered based on item type. This allows
     *            multiple, comma delimited. (optional)
     * @param mediaTypes Optional. Filter by MediaType. Allows multiple, comma delimited. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;QueryFiltersLegacy&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<QueryFiltersLegacy> getQueryFiltersLegacyWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable UUID userId, @org.eclipse.jdt.annotation.Nullable UUID parentId,
            @org.eclipse.jdt.annotation.Nullable List<BaseItemKind> includeItemTypes,
            @org.eclipse.jdt.annotation.Nullable List<MediaType> mediaTypes, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getQueryFiltersLegacyRequestBuilder(userId, parentId,
                includeItemTypes, mediaTypes, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getQueryFiltersLegacy", localVarResponse);
                }
                localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
                if (localVarResponseBody == null) {
                    return new ApiResponse<QueryFiltersLegacy>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponseBody.readAllBytes());
                QueryFiltersLegacy responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<QueryFiltersLegacy>() {
                        });

                return new ApiResponse<QueryFiltersLegacy>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(), responseValue);
            } finally {
                if (localVarResponseBody != null) {
                    localVarResponseBody.close();
                }
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder getQueryFiltersLegacyRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable UUID parentId,
            @org.eclipse.jdt.annotation.Nullable List<BaseItemKind> includeItemTypes,
            @org.eclipse.jdt.annotation.Nullable List<MediaType> mediaTypes, Map<String, String> headers)
            throws ApiException {

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
