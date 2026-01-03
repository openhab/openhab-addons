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
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.AlbumInfoRemoteSearchQuery;
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.ArtistInfoRemoteSearchQuery;
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.BookInfoRemoteSearchQuery;
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.BoxSetInfoRemoteSearchQuery;
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.ExternalIdInfo;
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.MovieInfoRemoteSearchQuery;
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.MusicVideoInfoRemoteSearchQuery;
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.PersonLookupInfoRemoteSearchQuery;
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.RemoteSearchResult;
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.SeriesInfoRemoteSearchQuery;
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.TrailerInfoRemoteSearchQuery;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class ItemLookupApi {
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

    public ItemLookupApi() {
        this(Configuration.getDefaultApiClient());
    }

    public ItemLookupApi(ApiClient apiClient) {
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
     * Applies search criteria to an item and refreshes metadata.
     * 
     * @param itemId Item id. (required)
     * @param remoteSearchResult The remote search result. (required)
     * @param replaceAllImages Optional. Whether or not to replace all images. Default: True. (optional, default to
     *            true)
     * @throws ApiException if fails to make API call
     */
    public void applySearchCriteria(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull RemoteSearchResult remoteSearchResult,
            @org.eclipse.jdt.annotation.Nullable Boolean replaceAllImages) throws ApiException {
        applySearchCriteria(itemId, remoteSearchResult, replaceAllImages, null);
    }

    /**
     * Applies search criteria to an item and refreshes metadata.
     * 
     * @param itemId Item id. (required)
     * @param remoteSearchResult The remote search result. (required)
     * @param replaceAllImages Optional. Whether or not to replace all images. Default: True. (optional, default to
     *            true)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void applySearchCriteria(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull RemoteSearchResult remoteSearchResult,
            @org.eclipse.jdt.annotation.Nullable Boolean replaceAllImages, Map<String, String> headers)
            throws ApiException {
        applySearchCriteriaWithHttpInfo(itemId, remoteSearchResult, replaceAllImages, headers);
    }

    /**
     * Applies search criteria to an item and refreshes metadata.
     * 
     * @param itemId Item id. (required)
     * @param remoteSearchResult The remote search result. (required)
     * @param replaceAllImages Optional. Whether or not to replace all images. Default: True. (optional, default to
     *            true)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> applySearchCriteriaWithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull RemoteSearchResult remoteSearchResult,
            @org.eclipse.jdt.annotation.Nullable Boolean replaceAllImages) throws ApiException {
        return applySearchCriteriaWithHttpInfo(itemId, remoteSearchResult, replaceAllImages, null);
    }

    /**
     * Applies search criteria to an item and refreshes metadata.
     * 
     * @param itemId Item id. (required)
     * @param remoteSearchResult The remote search result. (required)
     * @param replaceAllImages Optional. Whether or not to replace all images. Default: True. (optional, default to
     *            true)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> applySearchCriteriaWithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull RemoteSearchResult remoteSearchResult,
            @org.eclipse.jdt.annotation.Nullable Boolean replaceAllImages, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = applySearchCriteriaRequestBuilder(itemId, remoteSearchResult,
                replaceAllImages, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("applySearchCriteria", localVarResponse);
                }
                localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
                if (localVarResponseBody != null) {
                    localVarResponseBody.readAllBytes();
                }
                return new ApiResponse<>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
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

    private HttpRequest.Builder applySearchCriteriaRequestBuilder(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull RemoteSearchResult remoteSearchResult,
            @org.eclipse.jdt.annotation.Nullable Boolean replaceAllImages, Map<String, String> headers)
            throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling applySearchCriteria");
        }
        // verify the required parameter 'remoteSearchResult' is set
        if (remoteSearchResult == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'remoteSearchResult' when calling applySearchCriteria");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Items/RemoteSearch/Apply/{itemId}".replace("{itemId}",
                ApiClient.urlEncode(itemId.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "replaceAllImages";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("replaceAllImages", replaceAllImages));

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
        localVarRequestBuilder.header("Accept",
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(remoteSearchResult);
            localVarRequestBuilder.method("POST", HttpRequest.BodyPublishers.ofByteArray(localVarPostBody));
        } catch (IOException e) {
            throw new ApiException(e);
        }
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
     * Get book remote search.
     * 
     * @param bookInfoRemoteSearchQuery Remote search query. (required)
     * @return List&lt;RemoteSearchResult&gt;
     * @throws ApiException if fails to make API call
     */
    public List<RemoteSearchResult> getBookRemoteSearchResults(
            @org.eclipse.jdt.annotation.NonNull BookInfoRemoteSearchQuery bookInfoRemoteSearchQuery)
            throws ApiException {
        return getBookRemoteSearchResults(bookInfoRemoteSearchQuery, null);
    }

    /**
     * Get book remote search.
     * 
     * @param bookInfoRemoteSearchQuery Remote search query. (required)
     * @param headers Optional headers to include in the request
     * @return List&lt;RemoteSearchResult&gt;
     * @throws ApiException if fails to make API call
     */
    public List<RemoteSearchResult> getBookRemoteSearchResults(
            @org.eclipse.jdt.annotation.NonNull BookInfoRemoteSearchQuery bookInfoRemoteSearchQuery,
            Map<String, String> headers) throws ApiException {
        ApiResponse<List<RemoteSearchResult>> localVarResponse = getBookRemoteSearchResultsWithHttpInfo(
                bookInfoRemoteSearchQuery, headers);
        return localVarResponse.getData();
    }

    /**
     * Get book remote search.
     * 
     * @param bookInfoRemoteSearchQuery Remote search query. (required)
     * @return ApiResponse&lt;List&lt;RemoteSearchResult&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<RemoteSearchResult>> getBookRemoteSearchResultsWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull BookInfoRemoteSearchQuery bookInfoRemoteSearchQuery)
            throws ApiException {
        return getBookRemoteSearchResultsWithHttpInfo(bookInfoRemoteSearchQuery, null);
    }

    /**
     * Get book remote search.
     * 
     * @param bookInfoRemoteSearchQuery Remote search query. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;List&lt;RemoteSearchResult&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<RemoteSearchResult>> getBookRemoteSearchResultsWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull BookInfoRemoteSearchQuery bookInfoRemoteSearchQuery,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getBookRemoteSearchResultsRequestBuilder(bookInfoRemoteSearchQuery,
                headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getBookRemoteSearchResults", localVarResponse);
                }
                localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
                if (localVarResponseBody == null) {
                    return new ApiResponse<List<RemoteSearchResult>>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponseBody.readAllBytes());
                List<RemoteSearchResult> responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<List<RemoteSearchResult>>() {
                        });

                return new ApiResponse<List<RemoteSearchResult>>(localVarResponse.statusCode(),
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

    private HttpRequest.Builder getBookRemoteSearchResultsRequestBuilder(
            @org.eclipse.jdt.annotation.NonNull BookInfoRemoteSearchQuery bookInfoRemoteSearchQuery,
            Map<String, String> headers) throws ApiException {
        // verify the required parameter 'bookInfoRemoteSearchQuery' is set
        if (bookInfoRemoteSearchQuery == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'bookInfoRemoteSearchQuery' when calling getBookRemoteSearchResults");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Items/RemoteSearch/Book";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept",
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(bookInfoRemoteSearchQuery);
            localVarRequestBuilder.method("POST", HttpRequest.BodyPublishers.ofByteArray(localVarPostBody));
        } catch (IOException e) {
            throw new ApiException(e);
        }
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
     * Get box set remote search.
     * 
     * @param boxSetInfoRemoteSearchQuery Remote search query. (required)
     * @return List&lt;RemoteSearchResult&gt;
     * @throws ApiException if fails to make API call
     */
    public List<RemoteSearchResult> getBoxSetRemoteSearchResults(
            @org.eclipse.jdt.annotation.NonNull BoxSetInfoRemoteSearchQuery boxSetInfoRemoteSearchQuery)
            throws ApiException {
        return getBoxSetRemoteSearchResults(boxSetInfoRemoteSearchQuery, null);
    }

    /**
     * Get box set remote search.
     * 
     * @param boxSetInfoRemoteSearchQuery Remote search query. (required)
     * @param headers Optional headers to include in the request
     * @return List&lt;RemoteSearchResult&gt;
     * @throws ApiException if fails to make API call
     */
    public List<RemoteSearchResult> getBoxSetRemoteSearchResults(
            @org.eclipse.jdt.annotation.NonNull BoxSetInfoRemoteSearchQuery boxSetInfoRemoteSearchQuery,
            Map<String, String> headers) throws ApiException {
        ApiResponse<List<RemoteSearchResult>> localVarResponse = getBoxSetRemoteSearchResultsWithHttpInfo(
                boxSetInfoRemoteSearchQuery, headers);
        return localVarResponse.getData();
    }

    /**
     * Get box set remote search.
     * 
     * @param boxSetInfoRemoteSearchQuery Remote search query. (required)
     * @return ApiResponse&lt;List&lt;RemoteSearchResult&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<RemoteSearchResult>> getBoxSetRemoteSearchResultsWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull BoxSetInfoRemoteSearchQuery boxSetInfoRemoteSearchQuery)
            throws ApiException {
        return getBoxSetRemoteSearchResultsWithHttpInfo(boxSetInfoRemoteSearchQuery, null);
    }

    /**
     * Get box set remote search.
     * 
     * @param boxSetInfoRemoteSearchQuery Remote search query. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;List&lt;RemoteSearchResult&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<RemoteSearchResult>> getBoxSetRemoteSearchResultsWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull BoxSetInfoRemoteSearchQuery boxSetInfoRemoteSearchQuery,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getBoxSetRemoteSearchResultsRequestBuilder(
                boxSetInfoRemoteSearchQuery, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getBoxSetRemoteSearchResults", localVarResponse);
                }
                localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
                if (localVarResponseBody == null) {
                    return new ApiResponse<List<RemoteSearchResult>>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponseBody.readAllBytes());
                List<RemoteSearchResult> responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<List<RemoteSearchResult>>() {
                        });

                return new ApiResponse<List<RemoteSearchResult>>(localVarResponse.statusCode(),
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

    private HttpRequest.Builder getBoxSetRemoteSearchResultsRequestBuilder(
            @org.eclipse.jdt.annotation.NonNull BoxSetInfoRemoteSearchQuery boxSetInfoRemoteSearchQuery,
            Map<String, String> headers) throws ApiException {
        // verify the required parameter 'boxSetInfoRemoteSearchQuery' is set
        if (boxSetInfoRemoteSearchQuery == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'boxSetInfoRemoteSearchQuery' when calling getBoxSetRemoteSearchResults");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Items/RemoteSearch/BoxSet";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept",
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(boxSetInfoRemoteSearchQuery);
            localVarRequestBuilder.method("POST", HttpRequest.BodyPublishers.ofByteArray(localVarPostBody));
        } catch (IOException e) {
            throw new ApiException(e);
        }
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
     * Get the item&#39;s external id info.
     * 
     * @param itemId Item id. (required)
     * @return List&lt;ExternalIdInfo&gt;
     * @throws ApiException if fails to make API call
     */
    public List<ExternalIdInfo> getExternalIdInfos(@org.eclipse.jdt.annotation.NonNull UUID itemId)
            throws ApiException {
        return getExternalIdInfos(itemId, null);
    }

    /**
     * Get the item&#39;s external id info.
     * 
     * @param itemId Item id. (required)
     * @param headers Optional headers to include in the request
     * @return List&lt;ExternalIdInfo&gt;
     * @throws ApiException if fails to make API call
     */
    public List<ExternalIdInfo> getExternalIdInfos(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            Map<String, String> headers) throws ApiException {
        ApiResponse<List<ExternalIdInfo>> localVarResponse = getExternalIdInfosWithHttpInfo(itemId, headers);
        return localVarResponse.getData();
    }

    /**
     * Get the item&#39;s external id info.
     * 
     * @param itemId Item id. (required)
     * @return ApiResponse&lt;List&lt;ExternalIdInfo&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<ExternalIdInfo>> getExternalIdInfosWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull UUID itemId) throws ApiException {
        return getExternalIdInfosWithHttpInfo(itemId, null);
    }

    /**
     * Get the item&#39;s external id info.
     * 
     * @param itemId Item id. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;List&lt;ExternalIdInfo&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<ExternalIdInfo>> getExternalIdInfosWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull UUID itemId, Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getExternalIdInfosRequestBuilder(itemId, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getExternalIdInfos", localVarResponse);
                }
                localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
                if (localVarResponseBody == null) {
                    return new ApiResponse<List<ExternalIdInfo>>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponseBody.readAllBytes());
                List<ExternalIdInfo> responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<List<ExternalIdInfo>>() {
                        });

                return new ApiResponse<List<ExternalIdInfo>>(localVarResponse.statusCode(),
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

    private HttpRequest.Builder getExternalIdInfosRequestBuilder(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            Map<String, String> headers) throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling getExternalIdInfos");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Items/{itemId}/ExternalIdInfos".replace("{itemId}",
                ApiClient.urlEncode(itemId.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

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
     * Get movie remote search.
     * 
     * @param movieInfoRemoteSearchQuery Remote search query. (required)
     * @return List&lt;RemoteSearchResult&gt;
     * @throws ApiException if fails to make API call
     */
    public List<RemoteSearchResult> getMovieRemoteSearchResults(
            @org.eclipse.jdt.annotation.NonNull MovieInfoRemoteSearchQuery movieInfoRemoteSearchQuery)
            throws ApiException {
        return getMovieRemoteSearchResults(movieInfoRemoteSearchQuery, null);
    }

    /**
     * Get movie remote search.
     * 
     * @param movieInfoRemoteSearchQuery Remote search query. (required)
     * @param headers Optional headers to include in the request
     * @return List&lt;RemoteSearchResult&gt;
     * @throws ApiException if fails to make API call
     */
    public List<RemoteSearchResult> getMovieRemoteSearchResults(
            @org.eclipse.jdt.annotation.NonNull MovieInfoRemoteSearchQuery movieInfoRemoteSearchQuery,
            Map<String, String> headers) throws ApiException {
        ApiResponse<List<RemoteSearchResult>> localVarResponse = getMovieRemoteSearchResultsWithHttpInfo(
                movieInfoRemoteSearchQuery, headers);
        return localVarResponse.getData();
    }

    /**
     * Get movie remote search.
     * 
     * @param movieInfoRemoteSearchQuery Remote search query. (required)
     * @return ApiResponse&lt;List&lt;RemoteSearchResult&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<RemoteSearchResult>> getMovieRemoteSearchResultsWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull MovieInfoRemoteSearchQuery movieInfoRemoteSearchQuery)
            throws ApiException {
        return getMovieRemoteSearchResultsWithHttpInfo(movieInfoRemoteSearchQuery, null);
    }

    /**
     * Get movie remote search.
     * 
     * @param movieInfoRemoteSearchQuery Remote search query. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;List&lt;RemoteSearchResult&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<RemoteSearchResult>> getMovieRemoteSearchResultsWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull MovieInfoRemoteSearchQuery movieInfoRemoteSearchQuery,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getMovieRemoteSearchResultsRequestBuilder(
                movieInfoRemoteSearchQuery, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getMovieRemoteSearchResults", localVarResponse);
                }
                localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
                if (localVarResponseBody == null) {
                    return new ApiResponse<List<RemoteSearchResult>>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponseBody.readAllBytes());
                List<RemoteSearchResult> responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<List<RemoteSearchResult>>() {
                        });

                return new ApiResponse<List<RemoteSearchResult>>(localVarResponse.statusCode(),
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

    private HttpRequest.Builder getMovieRemoteSearchResultsRequestBuilder(
            @org.eclipse.jdt.annotation.NonNull MovieInfoRemoteSearchQuery movieInfoRemoteSearchQuery,
            Map<String, String> headers) throws ApiException {
        // verify the required parameter 'movieInfoRemoteSearchQuery' is set
        if (movieInfoRemoteSearchQuery == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'movieInfoRemoteSearchQuery' when calling getMovieRemoteSearchResults");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Items/RemoteSearch/Movie";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept",
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(movieInfoRemoteSearchQuery);
            localVarRequestBuilder.method("POST", HttpRequest.BodyPublishers.ofByteArray(localVarPostBody));
        } catch (IOException e) {
            throw new ApiException(e);
        }
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
     * Get music album remote search.
     * 
     * @param albumInfoRemoteSearchQuery Remote search query. (required)
     * @return List&lt;RemoteSearchResult&gt;
     * @throws ApiException if fails to make API call
     */
    public List<RemoteSearchResult> getMusicAlbumRemoteSearchResults(
            @org.eclipse.jdt.annotation.NonNull AlbumInfoRemoteSearchQuery albumInfoRemoteSearchQuery)
            throws ApiException {
        return getMusicAlbumRemoteSearchResults(albumInfoRemoteSearchQuery, null);
    }

    /**
     * Get music album remote search.
     * 
     * @param albumInfoRemoteSearchQuery Remote search query. (required)
     * @param headers Optional headers to include in the request
     * @return List&lt;RemoteSearchResult&gt;
     * @throws ApiException if fails to make API call
     */
    public List<RemoteSearchResult> getMusicAlbumRemoteSearchResults(
            @org.eclipse.jdt.annotation.NonNull AlbumInfoRemoteSearchQuery albumInfoRemoteSearchQuery,
            Map<String, String> headers) throws ApiException {
        ApiResponse<List<RemoteSearchResult>> localVarResponse = getMusicAlbumRemoteSearchResultsWithHttpInfo(
                albumInfoRemoteSearchQuery, headers);
        return localVarResponse.getData();
    }

    /**
     * Get music album remote search.
     * 
     * @param albumInfoRemoteSearchQuery Remote search query. (required)
     * @return ApiResponse&lt;List&lt;RemoteSearchResult&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<RemoteSearchResult>> getMusicAlbumRemoteSearchResultsWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull AlbumInfoRemoteSearchQuery albumInfoRemoteSearchQuery)
            throws ApiException {
        return getMusicAlbumRemoteSearchResultsWithHttpInfo(albumInfoRemoteSearchQuery, null);
    }

    /**
     * Get music album remote search.
     * 
     * @param albumInfoRemoteSearchQuery Remote search query. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;List&lt;RemoteSearchResult&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<RemoteSearchResult>> getMusicAlbumRemoteSearchResultsWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull AlbumInfoRemoteSearchQuery albumInfoRemoteSearchQuery,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getMusicAlbumRemoteSearchResultsRequestBuilder(
                albumInfoRemoteSearchQuery, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getMusicAlbumRemoteSearchResults", localVarResponse);
                }
                localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
                if (localVarResponseBody == null) {
                    return new ApiResponse<List<RemoteSearchResult>>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponseBody.readAllBytes());
                List<RemoteSearchResult> responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<List<RemoteSearchResult>>() {
                        });

                return new ApiResponse<List<RemoteSearchResult>>(localVarResponse.statusCode(),
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

    private HttpRequest.Builder getMusicAlbumRemoteSearchResultsRequestBuilder(
            @org.eclipse.jdt.annotation.NonNull AlbumInfoRemoteSearchQuery albumInfoRemoteSearchQuery,
            Map<String, String> headers) throws ApiException {
        // verify the required parameter 'albumInfoRemoteSearchQuery' is set
        if (albumInfoRemoteSearchQuery == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'albumInfoRemoteSearchQuery' when calling getMusicAlbumRemoteSearchResults");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Items/RemoteSearch/MusicAlbum";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept",
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(albumInfoRemoteSearchQuery);
            localVarRequestBuilder.method("POST", HttpRequest.BodyPublishers.ofByteArray(localVarPostBody));
        } catch (IOException e) {
            throw new ApiException(e);
        }
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
     * Get music artist remote search.
     * 
     * @param artistInfoRemoteSearchQuery Remote search query. (required)
     * @return List&lt;RemoteSearchResult&gt;
     * @throws ApiException if fails to make API call
     */
    public List<RemoteSearchResult> getMusicArtistRemoteSearchResults(
            @org.eclipse.jdt.annotation.NonNull ArtistInfoRemoteSearchQuery artistInfoRemoteSearchQuery)
            throws ApiException {
        return getMusicArtistRemoteSearchResults(artistInfoRemoteSearchQuery, null);
    }

    /**
     * Get music artist remote search.
     * 
     * @param artistInfoRemoteSearchQuery Remote search query. (required)
     * @param headers Optional headers to include in the request
     * @return List&lt;RemoteSearchResult&gt;
     * @throws ApiException if fails to make API call
     */
    public List<RemoteSearchResult> getMusicArtistRemoteSearchResults(
            @org.eclipse.jdt.annotation.NonNull ArtistInfoRemoteSearchQuery artistInfoRemoteSearchQuery,
            Map<String, String> headers) throws ApiException {
        ApiResponse<List<RemoteSearchResult>> localVarResponse = getMusicArtistRemoteSearchResultsWithHttpInfo(
                artistInfoRemoteSearchQuery, headers);
        return localVarResponse.getData();
    }

    /**
     * Get music artist remote search.
     * 
     * @param artistInfoRemoteSearchQuery Remote search query. (required)
     * @return ApiResponse&lt;List&lt;RemoteSearchResult&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<RemoteSearchResult>> getMusicArtistRemoteSearchResultsWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull ArtistInfoRemoteSearchQuery artistInfoRemoteSearchQuery)
            throws ApiException {
        return getMusicArtistRemoteSearchResultsWithHttpInfo(artistInfoRemoteSearchQuery, null);
    }

    /**
     * Get music artist remote search.
     * 
     * @param artistInfoRemoteSearchQuery Remote search query. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;List&lt;RemoteSearchResult&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<RemoteSearchResult>> getMusicArtistRemoteSearchResultsWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull ArtistInfoRemoteSearchQuery artistInfoRemoteSearchQuery,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getMusicArtistRemoteSearchResultsRequestBuilder(
                artistInfoRemoteSearchQuery, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getMusicArtistRemoteSearchResults", localVarResponse);
                }
                localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
                if (localVarResponseBody == null) {
                    return new ApiResponse<List<RemoteSearchResult>>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponseBody.readAllBytes());
                List<RemoteSearchResult> responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<List<RemoteSearchResult>>() {
                        });

                return new ApiResponse<List<RemoteSearchResult>>(localVarResponse.statusCode(),
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

    private HttpRequest.Builder getMusicArtistRemoteSearchResultsRequestBuilder(
            @org.eclipse.jdt.annotation.NonNull ArtistInfoRemoteSearchQuery artistInfoRemoteSearchQuery,
            Map<String, String> headers) throws ApiException {
        // verify the required parameter 'artistInfoRemoteSearchQuery' is set
        if (artistInfoRemoteSearchQuery == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'artistInfoRemoteSearchQuery' when calling getMusicArtistRemoteSearchResults");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Items/RemoteSearch/MusicArtist";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept",
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(artistInfoRemoteSearchQuery);
            localVarRequestBuilder.method("POST", HttpRequest.BodyPublishers.ofByteArray(localVarPostBody));
        } catch (IOException e) {
            throw new ApiException(e);
        }
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
     * Get music video remote search.
     * 
     * @param musicVideoInfoRemoteSearchQuery Remote search query. (required)
     * @return List&lt;RemoteSearchResult&gt;
     * @throws ApiException if fails to make API call
     */
    public List<RemoteSearchResult> getMusicVideoRemoteSearchResults(
            @org.eclipse.jdt.annotation.NonNull MusicVideoInfoRemoteSearchQuery musicVideoInfoRemoteSearchQuery)
            throws ApiException {
        return getMusicVideoRemoteSearchResults(musicVideoInfoRemoteSearchQuery, null);
    }

    /**
     * Get music video remote search.
     * 
     * @param musicVideoInfoRemoteSearchQuery Remote search query. (required)
     * @param headers Optional headers to include in the request
     * @return List&lt;RemoteSearchResult&gt;
     * @throws ApiException if fails to make API call
     */
    public List<RemoteSearchResult> getMusicVideoRemoteSearchResults(
            @org.eclipse.jdt.annotation.NonNull MusicVideoInfoRemoteSearchQuery musicVideoInfoRemoteSearchQuery,
            Map<String, String> headers) throws ApiException {
        ApiResponse<List<RemoteSearchResult>> localVarResponse = getMusicVideoRemoteSearchResultsWithHttpInfo(
                musicVideoInfoRemoteSearchQuery, headers);
        return localVarResponse.getData();
    }

    /**
     * Get music video remote search.
     * 
     * @param musicVideoInfoRemoteSearchQuery Remote search query. (required)
     * @return ApiResponse&lt;List&lt;RemoteSearchResult&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<RemoteSearchResult>> getMusicVideoRemoteSearchResultsWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull MusicVideoInfoRemoteSearchQuery musicVideoInfoRemoteSearchQuery)
            throws ApiException {
        return getMusicVideoRemoteSearchResultsWithHttpInfo(musicVideoInfoRemoteSearchQuery, null);
    }

    /**
     * Get music video remote search.
     * 
     * @param musicVideoInfoRemoteSearchQuery Remote search query. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;List&lt;RemoteSearchResult&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<RemoteSearchResult>> getMusicVideoRemoteSearchResultsWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull MusicVideoInfoRemoteSearchQuery musicVideoInfoRemoteSearchQuery,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getMusicVideoRemoteSearchResultsRequestBuilder(
                musicVideoInfoRemoteSearchQuery, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getMusicVideoRemoteSearchResults", localVarResponse);
                }
                localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
                if (localVarResponseBody == null) {
                    return new ApiResponse<List<RemoteSearchResult>>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponseBody.readAllBytes());
                List<RemoteSearchResult> responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<List<RemoteSearchResult>>() {
                        });

                return new ApiResponse<List<RemoteSearchResult>>(localVarResponse.statusCode(),
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

    private HttpRequest.Builder getMusicVideoRemoteSearchResultsRequestBuilder(
            @org.eclipse.jdt.annotation.NonNull MusicVideoInfoRemoteSearchQuery musicVideoInfoRemoteSearchQuery,
            Map<String, String> headers) throws ApiException {
        // verify the required parameter 'musicVideoInfoRemoteSearchQuery' is set
        if (musicVideoInfoRemoteSearchQuery == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'musicVideoInfoRemoteSearchQuery' when calling getMusicVideoRemoteSearchResults");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Items/RemoteSearch/MusicVideo";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept",
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(musicVideoInfoRemoteSearchQuery);
            localVarRequestBuilder.method("POST", HttpRequest.BodyPublishers.ofByteArray(localVarPostBody));
        } catch (IOException e) {
            throw new ApiException(e);
        }
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
     * Get person remote search.
     * 
     * @param personLookupInfoRemoteSearchQuery Remote search query. (required)
     * @return List&lt;RemoteSearchResult&gt;
     * @throws ApiException if fails to make API call
     */
    public List<RemoteSearchResult> getPersonRemoteSearchResults(
            @org.eclipse.jdt.annotation.NonNull PersonLookupInfoRemoteSearchQuery personLookupInfoRemoteSearchQuery)
            throws ApiException {
        return getPersonRemoteSearchResults(personLookupInfoRemoteSearchQuery, null);
    }

    /**
     * Get person remote search.
     * 
     * @param personLookupInfoRemoteSearchQuery Remote search query. (required)
     * @param headers Optional headers to include in the request
     * @return List&lt;RemoteSearchResult&gt;
     * @throws ApiException if fails to make API call
     */
    public List<RemoteSearchResult> getPersonRemoteSearchResults(
            @org.eclipse.jdt.annotation.NonNull PersonLookupInfoRemoteSearchQuery personLookupInfoRemoteSearchQuery,
            Map<String, String> headers) throws ApiException {
        ApiResponse<List<RemoteSearchResult>> localVarResponse = getPersonRemoteSearchResultsWithHttpInfo(
                personLookupInfoRemoteSearchQuery, headers);
        return localVarResponse.getData();
    }

    /**
     * Get person remote search.
     * 
     * @param personLookupInfoRemoteSearchQuery Remote search query. (required)
     * @return ApiResponse&lt;List&lt;RemoteSearchResult&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<RemoteSearchResult>> getPersonRemoteSearchResultsWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull PersonLookupInfoRemoteSearchQuery personLookupInfoRemoteSearchQuery)
            throws ApiException {
        return getPersonRemoteSearchResultsWithHttpInfo(personLookupInfoRemoteSearchQuery, null);
    }

    /**
     * Get person remote search.
     * 
     * @param personLookupInfoRemoteSearchQuery Remote search query. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;List&lt;RemoteSearchResult&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<RemoteSearchResult>> getPersonRemoteSearchResultsWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull PersonLookupInfoRemoteSearchQuery personLookupInfoRemoteSearchQuery,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getPersonRemoteSearchResultsRequestBuilder(
                personLookupInfoRemoteSearchQuery, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getPersonRemoteSearchResults", localVarResponse);
                }
                localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
                if (localVarResponseBody == null) {
                    return new ApiResponse<List<RemoteSearchResult>>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponseBody.readAllBytes());
                List<RemoteSearchResult> responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<List<RemoteSearchResult>>() {
                        });

                return new ApiResponse<List<RemoteSearchResult>>(localVarResponse.statusCode(),
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

    private HttpRequest.Builder getPersonRemoteSearchResultsRequestBuilder(
            @org.eclipse.jdt.annotation.NonNull PersonLookupInfoRemoteSearchQuery personLookupInfoRemoteSearchQuery,
            Map<String, String> headers) throws ApiException {
        // verify the required parameter 'personLookupInfoRemoteSearchQuery' is set
        if (personLookupInfoRemoteSearchQuery == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'personLookupInfoRemoteSearchQuery' when calling getPersonRemoteSearchResults");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Items/RemoteSearch/Person";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept",
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(personLookupInfoRemoteSearchQuery);
            localVarRequestBuilder.method("POST", HttpRequest.BodyPublishers.ofByteArray(localVarPostBody));
        } catch (IOException e) {
            throw new ApiException(e);
        }
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
     * Get series remote search.
     * 
     * @param seriesInfoRemoteSearchQuery Remote search query. (required)
     * @return List&lt;RemoteSearchResult&gt;
     * @throws ApiException if fails to make API call
     */
    public List<RemoteSearchResult> getSeriesRemoteSearchResults(
            @org.eclipse.jdt.annotation.NonNull SeriesInfoRemoteSearchQuery seriesInfoRemoteSearchQuery)
            throws ApiException {
        return getSeriesRemoteSearchResults(seriesInfoRemoteSearchQuery, null);
    }

    /**
     * Get series remote search.
     * 
     * @param seriesInfoRemoteSearchQuery Remote search query. (required)
     * @param headers Optional headers to include in the request
     * @return List&lt;RemoteSearchResult&gt;
     * @throws ApiException if fails to make API call
     */
    public List<RemoteSearchResult> getSeriesRemoteSearchResults(
            @org.eclipse.jdt.annotation.NonNull SeriesInfoRemoteSearchQuery seriesInfoRemoteSearchQuery,
            Map<String, String> headers) throws ApiException {
        ApiResponse<List<RemoteSearchResult>> localVarResponse = getSeriesRemoteSearchResultsWithHttpInfo(
                seriesInfoRemoteSearchQuery, headers);
        return localVarResponse.getData();
    }

    /**
     * Get series remote search.
     * 
     * @param seriesInfoRemoteSearchQuery Remote search query. (required)
     * @return ApiResponse&lt;List&lt;RemoteSearchResult&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<RemoteSearchResult>> getSeriesRemoteSearchResultsWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull SeriesInfoRemoteSearchQuery seriesInfoRemoteSearchQuery)
            throws ApiException {
        return getSeriesRemoteSearchResultsWithHttpInfo(seriesInfoRemoteSearchQuery, null);
    }

    /**
     * Get series remote search.
     * 
     * @param seriesInfoRemoteSearchQuery Remote search query. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;List&lt;RemoteSearchResult&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<RemoteSearchResult>> getSeriesRemoteSearchResultsWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull SeriesInfoRemoteSearchQuery seriesInfoRemoteSearchQuery,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getSeriesRemoteSearchResultsRequestBuilder(
                seriesInfoRemoteSearchQuery, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getSeriesRemoteSearchResults", localVarResponse);
                }
                localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
                if (localVarResponseBody == null) {
                    return new ApiResponse<List<RemoteSearchResult>>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponseBody.readAllBytes());
                List<RemoteSearchResult> responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<List<RemoteSearchResult>>() {
                        });

                return new ApiResponse<List<RemoteSearchResult>>(localVarResponse.statusCode(),
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

    private HttpRequest.Builder getSeriesRemoteSearchResultsRequestBuilder(
            @org.eclipse.jdt.annotation.NonNull SeriesInfoRemoteSearchQuery seriesInfoRemoteSearchQuery,
            Map<String, String> headers) throws ApiException {
        // verify the required parameter 'seriesInfoRemoteSearchQuery' is set
        if (seriesInfoRemoteSearchQuery == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'seriesInfoRemoteSearchQuery' when calling getSeriesRemoteSearchResults");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Items/RemoteSearch/Series";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept",
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(seriesInfoRemoteSearchQuery);
            localVarRequestBuilder.method("POST", HttpRequest.BodyPublishers.ofByteArray(localVarPostBody));
        } catch (IOException e) {
            throw new ApiException(e);
        }
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
     * Get trailer remote search.
     * 
     * @param trailerInfoRemoteSearchQuery Remote search query. (required)
     * @return List&lt;RemoteSearchResult&gt;
     * @throws ApiException if fails to make API call
     */
    public List<RemoteSearchResult> getTrailerRemoteSearchResults(
            @org.eclipse.jdt.annotation.NonNull TrailerInfoRemoteSearchQuery trailerInfoRemoteSearchQuery)
            throws ApiException {
        return getTrailerRemoteSearchResults(trailerInfoRemoteSearchQuery, null);
    }

    /**
     * Get trailer remote search.
     * 
     * @param trailerInfoRemoteSearchQuery Remote search query. (required)
     * @param headers Optional headers to include in the request
     * @return List&lt;RemoteSearchResult&gt;
     * @throws ApiException if fails to make API call
     */
    public List<RemoteSearchResult> getTrailerRemoteSearchResults(
            @org.eclipse.jdt.annotation.NonNull TrailerInfoRemoteSearchQuery trailerInfoRemoteSearchQuery,
            Map<String, String> headers) throws ApiException {
        ApiResponse<List<RemoteSearchResult>> localVarResponse = getTrailerRemoteSearchResultsWithHttpInfo(
                trailerInfoRemoteSearchQuery, headers);
        return localVarResponse.getData();
    }

    /**
     * Get trailer remote search.
     * 
     * @param trailerInfoRemoteSearchQuery Remote search query. (required)
     * @return ApiResponse&lt;List&lt;RemoteSearchResult&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<RemoteSearchResult>> getTrailerRemoteSearchResultsWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull TrailerInfoRemoteSearchQuery trailerInfoRemoteSearchQuery)
            throws ApiException {
        return getTrailerRemoteSearchResultsWithHttpInfo(trailerInfoRemoteSearchQuery, null);
    }

    /**
     * Get trailer remote search.
     * 
     * @param trailerInfoRemoteSearchQuery Remote search query. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;List&lt;RemoteSearchResult&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<RemoteSearchResult>> getTrailerRemoteSearchResultsWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull TrailerInfoRemoteSearchQuery trailerInfoRemoteSearchQuery,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getTrailerRemoteSearchResultsRequestBuilder(
                trailerInfoRemoteSearchQuery, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getTrailerRemoteSearchResults", localVarResponse);
                }
                localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
                if (localVarResponseBody == null) {
                    return new ApiResponse<List<RemoteSearchResult>>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponseBody.readAllBytes());
                List<RemoteSearchResult> responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<List<RemoteSearchResult>>() {
                        });

                return new ApiResponse<List<RemoteSearchResult>>(localVarResponse.statusCode(),
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

    private HttpRequest.Builder getTrailerRemoteSearchResultsRequestBuilder(
            @org.eclipse.jdt.annotation.NonNull TrailerInfoRemoteSearchQuery trailerInfoRemoteSearchQuery,
            Map<String, String> headers) throws ApiException {
        // verify the required parameter 'trailerInfoRemoteSearchQuery' is set
        if (trailerInfoRemoteSearchQuery == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'trailerInfoRemoteSearchQuery' when calling getTrailerRemoteSearchResults");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Items/RemoteSearch/Trailer";

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Content-Type", "application/json");
        localVarRequestBuilder.header("Accept",
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(trailerInfoRemoteSearchQuery);
            localVarRequestBuilder.method("POST", HttpRequest.BodyPublishers.ofByteArray(localVarPostBody));
        } catch (IOException e) {
            throw new ApiException(e);
        }
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
