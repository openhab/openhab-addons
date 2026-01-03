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
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.SearchHintResult;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class SearchApi {
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

    public SearchApi() {
        this(Configuration.getDefaultApiClient());
    }

    public SearchApi(ApiClient apiClient) {
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
     * Gets the search hint result.
     * 
     * @param searchTerm The search term to filter on. (required)
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param userId Optional. Supply a user id to search within a user&#39;s library or omit to search all. (optional)
     * @param includeItemTypes If specified, only results with the specified item types are returned. This allows
     *            multiple, comma delimited. (optional)
     * @param excludeItemTypes If specified, results with these item types are filtered out. This allows multiple, comma
     *            delimited. (optional)
     * @param mediaTypes If specified, only results with the specified media types are returned. This allows multiple,
     *            comma delimited. (optional)
     * @param parentId If specified, only children of the parent are returned. (optional)
     * @param isMovie Optional filter for movies. (optional)
     * @param isSeries Optional filter for series. (optional)
     * @param isNews Optional filter for news. (optional)
     * @param isKids Optional filter for kids. (optional)
     * @param isSports Optional filter for sports. (optional)
     * @param includePeople Optional filter whether to include people. (optional, default to true)
     * @param includeMedia Optional filter whether to include media. (optional, default to true)
     * @param includeGenres Optional filter whether to include genres. (optional, default to true)
     * @param includeStudios Optional filter whether to include studios. (optional, default to true)
     * @param includeArtists Optional filter whether to include artists. (optional, default to true)
     * @return SearchHintResult
     * @throws ApiException if fails to make API call
     */
    public SearchHintResult getSearchHints(@org.eclipse.jdt.annotation.NonNull String searchTerm,
            @org.eclipse.jdt.annotation.Nullable Integer startIndex, @org.eclipse.jdt.annotation.Nullable Integer limit,
            @org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable List<BaseItemKind> includeItemTypes,
            @org.eclipse.jdt.annotation.Nullable List<BaseItemKind> excludeItemTypes,
            @org.eclipse.jdt.annotation.Nullable List<MediaType> mediaTypes,
            @org.eclipse.jdt.annotation.Nullable UUID parentId, @org.eclipse.jdt.annotation.Nullable Boolean isMovie,
            @org.eclipse.jdt.annotation.Nullable Boolean isSeries, @org.eclipse.jdt.annotation.Nullable Boolean isNews,
            @org.eclipse.jdt.annotation.Nullable Boolean isKids, @org.eclipse.jdt.annotation.Nullable Boolean isSports,
            @org.eclipse.jdt.annotation.Nullable Boolean includePeople,
            @org.eclipse.jdt.annotation.Nullable Boolean includeMedia,
            @org.eclipse.jdt.annotation.Nullable Boolean includeGenres,
            @org.eclipse.jdt.annotation.Nullable Boolean includeStudios,
            @org.eclipse.jdt.annotation.Nullable Boolean includeArtists) throws ApiException {
        return getSearchHints(searchTerm, startIndex, limit, userId, includeItemTypes, excludeItemTypes, mediaTypes,
                parentId, isMovie, isSeries, isNews, isKids, isSports, includePeople, includeMedia, includeGenres,
                includeStudios, includeArtists, null);
    }

    /**
     * Gets the search hint result.
     * 
     * @param searchTerm The search term to filter on. (required)
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param userId Optional. Supply a user id to search within a user&#39;s library or omit to search all. (optional)
     * @param includeItemTypes If specified, only results with the specified item types are returned. This allows
     *            multiple, comma delimited. (optional)
     * @param excludeItemTypes If specified, results with these item types are filtered out. This allows multiple, comma
     *            delimited. (optional)
     * @param mediaTypes If specified, only results with the specified media types are returned. This allows multiple,
     *            comma delimited. (optional)
     * @param parentId If specified, only children of the parent are returned. (optional)
     * @param isMovie Optional filter for movies. (optional)
     * @param isSeries Optional filter for series. (optional)
     * @param isNews Optional filter for news. (optional)
     * @param isKids Optional filter for kids. (optional)
     * @param isSports Optional filter for sports. (optional)
     * @param includePeople Optional filter whether to include people. (optional, default to true)
     * @param includeMedia Optional filter whether to include media. (optional, default to true)
     * @param includeGenres Optional filter whether to include genres. (optional, default to true)
     * @param includeStudios Optional filter whether to include studios. (optional, default to true)
     * @param includeArtists Optional filter whether to include artists. (optional, default to true)
     * @param headers Optional headers to include in the request
     * @return SearchHintResult
     * @throws ApiException if fails to make API call
     */
    public SearchHintResult getSearchHints(@org.eclipse.jdt.annotation.NonNull String searchTerm,
            @org.eclipse.jdt.annotation.Nullable Integer startIndex, @org.eclipse.jdt.annotation.Nullable Integer limit,
            @org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable List<BaseItemKind> includeItemTypes,
            @org.eclipse.jdt.annotation.Nullable List<BaseItemKind> excludeItemTypes,
            @org.eclipse.jdt.annotation.Nullable List<MediaType> mediaTypes,
            @org.eclipse.jdt.annotation.Nullable UUID parentId, @org.eclipse.jdt.annotation.Nullable Boolean isMovie,
            @org.eclipse.jdt.annotation.Nullable Boolean isSeries, @org.eclipse.jdt.annotation.Nullable Boolean isNews,
            @org.eclipse.jdt.annotation.Nullable Boolean isKids, @org.eclipse.jdt.annotation.Nullable Boolean isSports,
            @org.eclipse.jdt.annotation.Nullable Boolean includePeople,
            @org.eclipse.jdt.annotation.Nullable Boolean includeMedia,
            @org.eclipse.jdt.annotation.Nullable Boolean includeGenres,
            @org.eclipse.jdt.annotation.Nullable Boolean includeStudios,
            @org.eclipse.jdt.annotation.Nullable Boolean includeArtists, Map<String, String> headers)
            throws ApiException {
        ApiResponse<SearchHintResult> localVarResponse = getSearchHintsWithHttpInfo(searchTerm, startIndex, limit,
                userId, includeItemTypes, excludeItemTypes, mediaTypes, parentId, isMovie, isSeries, isNews, isKids,
                isSports, includePeople, includeMedia, includeGenres, includeStudios, includeArtists, headers);
        return localVarResponse.getData();
    }

    /**
     * Gets the search hint result.
     * 
     * @param searchTerm The search term to filter on. (required)
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param userId Optional. Supply a user id to search within a user&#39;s library or omit to search all. (optional)
     * @param includeItemTypes If specified, only results with the specified item types are returned. This allows
     *            multiple, comma delimited. (optional)
     * @param excludeItemTypes If specified, results with these item types are filtered out. This allows multiple, comma
     *            delimited. (optional)
     * @param mediaTypes If specified, only results with the specified media types are returned. This allows multiple,
     *            comma delimited. (optional)
     * @param parentId If specified, only children of the parent are returned. (optional)
     * @param isMovie Optional filter for movies. (optional)
     * @param isSeries Optional filter for series. (optional)
     * @param isNews Optional filter for news. (optional)
     * @param isKids Optional filter for kids. (optional)
     * @param isSports Optional filter for sports. (optional)
     * @param includePeople Optional filter whether to include people. (optional, default to true)
     * @param includeMedia Optional filter whether to include media. (optional, default to true)
     * @param includeGenres Optional filter whether to include genres. (optional, default to true)
     * @param includeStudios Optional filter whether to include studios. (optional, default to true)
     * @param includeArtists Optional filter whether to include artists. (optional, default to true)
     * @return ApiResponse&lt;SearchHintResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<SearchHintResult> getSearchHintsWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull String searchTerm,
            @org.eclipse.jdt.annotation.Nullable Integer startIndex, @org.eclipse.jdt.annotation.Nullable Integer limit,
            @org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable List<BaseItemKind> includeItemTypes,
            @org.eclipse.jdt.annotation.Nullable List<BaseItemKind> excludeItemTypes,
            @org.eclipse.jdt.annotation.Nullable List<MediaType> mediaTypes,
            @org.eclipse.jdt.annotation.Nullable UUID parentId, @org.eclipse.jdt.annotation.Nullable Boolean isMovie,
            @org.eclipse.jdt.annotation.Nullable Boolean isSeries, @org.eclipse.jdt.annotation.Nullable Boolean isNews,
            @org.eclipse.jdt.annotation.Nullable Boolean isKids, @org.eclipse.jdt.annotation.Nullable Boolean isSports,
            @org.eclipse.jdt.annotation.Nullable Boolean includePeople,
            @org.eclipse.jdt.annotation.Nullable Boolean includeMedia,
            @org.eclipse.jdt.annotation.Nullable Boolean includeGenres,
            @org.eclipse.jdt.annotation.Nullable Boolean includeStudios,
            @org.eclipse.jdt.annotation.Nullable Boolean includeArtists) throws ApiException {
        return getSearchHintsWithHttpInfo(searchTerm, startIndex, limit, userId, includeItemTypes, excludeItemTypes,
                mediaTypes, parentId, isMovie, isSeries, isNews, isKids, isSports, includePeople, includeMedia,
                includeGenres, includeStudios, includeArtists, null);
    }

    /**
     * Gets the search hint result.
     * 
     * @param searchTerm The search term to filter on. (required)
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param userId Optional. Supply a user id to search within a user&#39;s library or omit to search all. (optional)
     * @param includeItemTypes If specified, only results with the specified item types are returned. This allows
     *            multiple, comma delimited. (optional)
     * @param excludeItemTypes If specified, results with these item types are filtered out. This allows multiple, comma
     *            delimited. (optional)
     * @param mediaTypes If specified, only results with the specified media types are returned. This allows multiple,
     *            comma delimited. (optional)
     * @param parentId If specified, only children of the parent are returned. (optional)
     * @param isMovie Optional filter for movies. (optional)
     * @param isSeries Optional filter for series. (optional)
     * @param isNews Optional filter for news. (optional)
     * @param isKids Optional filter for kids. (optional)
     * @param isSports Optional filter for sports. (optional)
     * @param includePeople Optional filter whether to include people. (optional, default to true)
     * @param includeMedia Optional filter whether to include media. (optional, default to true)
     * @param includeGenres Optional filter whether to include genres. (optional, default to true)
     * @param includeStudios Optional filter whether to include studios. (optional, default to true)
     * @param includeArtists Optional filter whether to include artists. (optional, default to true)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;SearchHintResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<SearchHintResult> getSearchHintsWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull String searchTerm,
            @org.eclipse.jdt.annotation.Nullable Integer startIndex, @org.eclipse.jdt.annotation.Nullable Integer limit,
            @org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable List<BaseItemKind> includeItemTypes,
            @org.eclipse.jdt.annotation.Nullable List<BaseItemKind> excludeItemTypes,
            @org.eclipse.jdt.annotation.Nullable List<MediaType> mediaTypes,
            @org.eclipse.jdt.annotation.Nullable UUID parentId, @org.eclipse.jdt.annotation.Nullable Boolean isMovie,
            @org.eclipse.jdt.annotation.Nullable Boolean isSeries, @org.eclipse.jdt.annotation.Nullable Boolean isNews,
            @org.eclipse.jdt.annotation.Nullable Boolean isKids, @org.eclipse.jdt.annotation.Nullable Boolean isSports,
            @org.eclipse.jdt.annotation.Nullable Boolean includePeople,
            @org.eclipse.jdt.annotation.Nullable Boolean includeMedia,
            @org.eclipse.jdt.annotation.Nullable Boolean includeGenres,
            @org.eclipse.jdt.annotation.Nullable Boolean includeStudios,
            @org.eclipse.jdt.annotation.Nullable Boolean includeArtists, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getSearchHintsRequestBuilder(searchTerm, startIndex, limit, userId,
                includeItemTypes, excludeItemTypes, mediaTypes, parentId, isMovie, isSeries, isNews, isKids, isSports,
                includePeople, includeMedia, includeGenres, includeStudios, includeArtists, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getSearchHints", localVarResponse);
                }
                localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
                if (localVarResponseBody == null) {
                    return new ApiResponse<SearchHintResult>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponseBody.readAllBytes());
                SearchHintResult responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<SearchHintResult>() {
                        });

                return new ApiResponse<SearchHintResult>(localVarResponse.statusCode(),
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

    private HttpRequest.Builder getSearchHintsRequestBuilder(@org.eclipse.jdt.annotation.NonNull String searchTerm,
            @org.eclipse.jdt.annotation.Nullable Integer startIndex, @org.eclipse.jdt.annotation.Nullable Integer limit,
            @org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable List<BaseItemKind> includeItemTypes,
            @org.eclipse.jdt.annotation.Nullable List<BaseItemKind> excludeItemTypes,
            @org.eclipse.jdt.annotation.Nullable List<MediaType> mediaTypes,
            @org.eclipse.jdt.annotation.Nullable UUID parentId, @org.eclipse.jdt.annotation.Nullable Boolean isMovie,
            @org.eclipse.jdt.annotation.Nullable Boolean isSeries, @org.eclipse.jdt.annotation.Nullable Boolean isNews,
            @org.eclipse.jdt.annotation.Nullable Boolean isKids, @org.eclipse.jdt.annotation.Nullable Boolean isSports,
            @org.eclipse.jdt.annotation.Nullable Boolean includePeople,
            @org.eclipse.jdt.annotation.Nullable Boolean includeMedia,
            @org.eclipse.jdt.annotation.Nullable Boolean includeGenres,
            @org.eclipse.jdt.annotation.Nullable Boolean includeStudios,
            @org.eclipse.jdt.annotation.Nullable Boolean includeArtists, Map<String, String> headers)
            throws ApiException {
        // verify the required parameter 'searchTerm' is set
        if (searchTerm == null) {
            throw new ApiException(400, "Missing the required parameter 'searchTerm' when calling getSearchHints");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Search/Hints";

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "startIndex";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("startIndex", startIndex));
        localVarQueryParameterBaseName = "limit";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("limit", limit));
        localVarQueryParameterBaseName = "userId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("userId", userId));
        localVarQueryParameterBaseName = "searchTerm";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("searchTerm", searchTerm));
        localVarQueryParameterBaseName = "includeItemTypes";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "includeItemTypes", includeItemTypes));
        localVarQueryParameterBaseName = "excludeItemTypes";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "excludeItemTypes", excludeItemTypes));
        localVarQueryParameterBaseName = "mediaTypes";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "mediaTypes", mediaTypes));
        localVarQueryParameterBaseName = "parentId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("parentId", parentId));
        localVarQueryParameterBaseName = "isMovie";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("isMovie", isMovie));
        localVarQueryParameterBaseName = "isSeries";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("isSeries", isSeries));
        localVarQueryParameterBaseName = "isNews";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("isNews", isNews));
        localVarQueryParameterBaseName = "isKids";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("isKids", isKids));
        localVarQueryParameterBaseName = "isSports";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("isSports", isSports));
        localVarQueryParameterBaseName = "includePeople";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("includePeople", includePeople));
        localVarQueryParameterBaseName = "includeMedia";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("includeMedia", includeMedia));
        localVarQueryParameterBaseName = "includeGenres";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("includeGenres", includeGenres));
        localVarQueryParameterBaseName = "includeStudios";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("includeStudios", includeStudios));
        localVarQueryParameterBaseName = "includeArtists";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("includeArtists", includeArtists));

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
