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
import org.openhab.binding.jellyfin.internal.api.generated.current.model.AlbumInfoRemoteSearchQuery;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.ArtistInfoRemoteSearchQuery;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.BookInfoRemoteSearchQuery;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.BoxSetInfoRemoteSearchQuery;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.ExternalIdInfo;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.MovieInfoRemoteSearchQuery;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.MusicVideoInfoRemoteSearchQuery;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.PersonLookupInfoRemoteSearchQuery;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.RemoteSearchResult;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.SeriesInfoRemoteSearchQuery;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.TrailerInfoRemoteSearchQuery;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class ItemLookupApi {
    private final HttpClient memberVarHttpClient;
    private final ObjectMapper memberVarObjectMapper;
    private final String memberVarBaseUri;
    private final Consumer<HttpRequest.Builder> memberVarInterceptor;
    private final Duration memberVarReadTimeout;
    private final Consumer<HttpResponse<InputStream>> memberVarResponseInterceptor;
    private final Consumer<HttpResponse<String>> memberVarAsyncResponseInterceptor;

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
     * Applies search criteria to an item and refreshes metadata.
     * 
     * @param itemId Item id. (required)
     * @param remoteSearchResult The remote search result. (required)
     * @param replaceAllImages Optional. Whether or not to replace all images. Default: True. (optional, default to
     *            true)
     * @throws ApiException if fails to make API call
     */
    public void applySearchCriteria(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable RemoteSearchResult remoteSearchResult,
            @org.eclipse.jdt.annotation.NonNull Boolean replaceAllImages) throws ApiException {
        applySearchCriteriaWithHttpInfo(itemId, remoteSearchResult, replaceAllImages);
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
    public ApiResponse<Void> applySearchCriteriaWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable RemoteSearchResult remoteSearchResult,
            @org.eclipse.jdt.annotation.NonNull Boolean replaceAllImages) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = applySearchCriteriaRequestBuilder(itemId, remoteSearchResult,
                replaceAllImages);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("applySearchCriteria", localVarResponse);
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

    private HttpRequest.Builder applySearchCriteriaRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable RemoteSearchResult remoteSearchResult,
            @org.eclipse.jdt.annotation.NonNull Boolean replaceAllImages) throws ApiException {
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
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(remoteSearchResult);
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
     * Get book remote search.
     * 
     * @param bookInfoRemoteSearchQuery Remote search query. (required)
     * @return List&lt;RemoteSearchResult&gt;
     * @throws ApiException if fails to make API call
     */
    public List<RemoteSearchResult> getBookRemoteSearchResults(
            @org.eclipse.jdt.annotation.Nullable BookInfoRemoteSearchQuery bookInfoRemoteSearchQuery)
            throws ApiException {
        ApiResponse<List<RemoteSearchResult>> localVarResponse = getBookRemoteSearchResultsWithHttpInfo(
                bookInfoRemoteSearchQuery);
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
            @org.eclipse.jdt.annotation.Nullable BookInfoRemoteSearchQuery bookInfoRemoteSearchQuery)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getBookRemoteSearchResultsRequestBuilder(
                bookInfoRemoteSearchQuery);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getBookRemoteSearchResults", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<List<RemoteSearchResult>>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<List<RemoteSearchResult>>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody,
                                        new TypeReference<List<RemoteSearchResult>>() {
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

    private HttpRequest.Builder getBookRemoteSearchResultsRequestBuilder(
            @org.eclipse.jdt.annotation.Nullable BookInfoRemoteSearchQuery bookInfoRemoteSearchQuery)
            throws ApiException {
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
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(bookInfoRemoteSearchQuery);
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
     * Get box set remote search.
     * 
     * @param boxSetInfoRemoteSearchQuery Remote search query. (required)
     * @return List&lt;RemoteSearchResult&gt;
     * @throws ApiException if fails to make API call
     */
    public List<RemoteSearchResult> getBoxSetRemoteSearchResults(
            @org.eclipse.jdt.annotation.Nullable BoxSetInfoRemoteSearchQuery boxSetInfoRemoteSearchQuery)
            throws ApiException {
        ApiResponse<List<RemoteSearchResult>> localVarResponse = getBoxSetRemoteSearchResultsWithHttpInfo(
                boxSetInfoRemoteSearchQuery);
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
            @org.eclipse.jdt.annotation.Nullable BoxSetInfoRemoteSearchQuery boxSetInfoRemoteSearchQuery)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getBoxSetRemoteSearchResultsRequestBuilder(
                boxSetInfoRemoteSearchQuery);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getBoxSetRemoteSearchResults", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<List<RemoteSearchResult>>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<List<RemoteSearchResult>>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody,
                                        new TypeReference<List<RemoteSearchResult>>() {
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

    private HttpRequest.Builder getBoxSetRemoteSearchResultsRequestBuilder(
            @org.eclipse.jdt.annotation.Nullable BoxSetInfoRemoteSearchQuery boxSetInfoRemoteSearchQuery)
            throws ApiException {
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
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(boxSetInfoRemoteSearchQuery);
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
     * Get the item&#39;s external id info.
     * 
     * @param itemId Item id. (required)
     * @return List&lt;ExternalIdInfo&gt;
     * @throws ApiException if fails to make API call
     */
    public List<ExternalIdInfo> getExternalIdInfos(@org.eclipse.jdt.annotation.Nullable UUID itemId)
            throws ApiException {
        ApiResponse<List<ExternalIdInfo>> localVarResponse = getExternalIdInfosWithHttpInfo(itemId);
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
            @org.eclipse.jdt.annotation.Nullable UUID itemId) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getExternalIdInfosRequestBuilder(itemId);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getExternalIdInfos", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<List<ExternalIdInfo>>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<List<ExternalIdInfo>>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody,
                                        new TypeReference<List<ExternalIdInfo>>() {
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

    private HttpRequest.Builder getExternalIdInfosRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID itemId)
            throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling getExternalIdInfos");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Items/{itemId}/ExternalIdInfos".replace("{itemId}",
                ApiClient.urlEncode(itemId.toString()));

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
     * Get movie remote search.
     * 
     * @param movieInfoRemoteSearchQuery Remote search query. (required)
     * @return List&lt;RemoteSearchResult&gt;
     * @throws ApiException if fails to make API call
     */
    public List<RemoteSearchResult> getMovieRemoteSearchResults(
            @org.eclipse.jdt.annotation.Nullable MovieInfoRemoteSearchQuery movieInfoRemoteSearchQuery)
            throws ApiException {
        ApiResponse<List<RemoteSearchResult>> localVarResponse = getMovieRemoteSearchResultsWithHttpInfo(
                movieInfoRemoteSearchQuery);
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
            @org.eclipse.jdt.annotation.Nullable MovieInfoRemoteSearchQuery movieInfoRemoteSearchQuery)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getMovieRemoteSearchResultsRequestBuilder(
                movieInfoRemoteSearchQuery);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getMovieRemoteSearchResults", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<List<RemoteSearchResult>>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<List<RemoteSearchResult>>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody,
                                        new TypeReference<List<RemoteSearchResult>>() {
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

    private HttpRequest.Builder getMovieRemoteSearchResultsRequestBuilder(
            @org.eclipse.jdt.annotation.Nullable MovieInfoRemoteSearchQuery movieInfoRemoteSearchQuery)
            throws ApiException {
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
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(movieInfoRemoteSearchQuery);
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
     * Get music album remote search.
     * 
     * @param albumInfoRemoteSearchQuery Remote search query. (required)
     * @return List&lt;RemoteSearchResult&gt;
     * @throws ApiException if fails to make API call
     */
    public List<RemoteSearchResult> getMusicAlbumRemoteSearchResults(
            @org.eclipse.jdt.annotation.Nullable AlbumInfoRemoteSearchQuery albumInfoRemoteSearchQuery)
            throws ApiException {
        ApiResponse<List<RemoteSearchResult>> localVarResponse = getMusicAlbumRemoteSearchResultsWithHttpInfo(
                albumInfoRemoteSearchQuery);
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
            @org.eclipse.jdt.annotation.Nullable AlbumInfoRemoteSearchQuery albumInfoRemoteSearchQuery)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getMusicAlbumRemoteSearchResultsRequestBuilder(
                albumInfoRemoteSearchQuery);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getMusicAlbumRemoteSearchResults", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<List<RemoteSearchResult>>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<List<RemoteSearchResult>>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody,
                                        new TypeReference<List<RemoteSearchResult>>() {
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

    private HttpRequest.Builder getMusicAlbumRemoteSearchResultsRequestBuilder(
            @org.eclipse.jdt.annotation.Nullable AlbumInfoRemoteSearchQuery albumInfoRemoteSearchQuery)
            throws ApiException {
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
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(albumInfoRemoteSearchQuery);
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
     * Get music artist remote search.
     * 
     * @param artistInfoRemoteSearchQuery Remote search query. (required)
     * @return List&lt;RemoteSearchResult&gt;
     * @throws ApiException if fails to make API call
     */
    public List<RemoteSearchResult> getMusicArtistRemoteSearchResults(
            @org.eclipse.jdt.annotation.Nullable ArtistInfoRemoteSearchQuery artistInfoRemoteSearchQuery)
            throws ApiException {
        ApiResponse<List<RemoteSearchResult>> localVarResponse = getMusicArtistRemoteSearchResultsWithHttpInfo(
                artistInfoRemoteSearchQuery);
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
            @org.eclipse.jdt.annotation.Nullable ArtistInfoRemoteSearchQuery artistInfoRemoteSearchQuery)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getMusicArtistRemoteSearchResultsRequestBuilder(
                artistInfoRemoteSearchQuery);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getMusicArtistRemoteSearchResults", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<List<RemoteSearchResult>>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<List<RemoteSearchResult>>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody,
                                        new TypeReference<List<RemoteSearchResult>>() {
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

    private HttpRequest.Builder getMusicArtistRemoteSearchResultsRequestBuilder(
            @org.eclipse.jdt.annotation.Nullable ArtistInfoRemoteSearchQuery artistInfoRemoteSearchQuery)
            throws ApiException {
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
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(artistInfoRemoteSearchQuery);
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
     * Get music video remote search.
     * 
     * @param musicVideoInfoRemoteSearchQuery Remote search query. (required)
     * @return List&lt;RemoteSearchResult&gt;
     * @throws ApiException if fails to make API call
     */
    public List<RemoteSearchResult> getMusicVideoRemoteSearchResults(
            @org.eclipse.jdt.annotation.Nullable MusicVideoInfoRemoteSearchQuery musicVideoInfoRemoteSearchQuery)
            throws ApiException {
        ApiResponse<List<RemoteSearchResult>> localVarResponse = getMusicVideoRemoteSearchResultsWithHttpInfo(
                musicVideoInfoRemoteSearchQuery);
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
            @org.eclipse.jdt.annotation.Nullable MusicVideoInfoRemoteSearchQuery musicVideoInfoRemoteSearchQuery)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getMusicVideoRemoteSearchResultsRequestBuilder(
                musicVideoInfoRemoteSearchQuery);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getMusicVideoRemoteSearchResults", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<List<RemoteSearchResult>>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<List<RemoteSearchResult>>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody,
                                        new TypeReference<List<RemoteSearchResult>>() {
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

    private HttpRequest.Builder getMusicVideoRemoteSearchResultsRequestBuilder(
            @org.eclipse.jdt.annotation.Nullable MusicVideoInfoRemoteSearchQuery musicVideoInfoRemoteSearchQuery)
            throws ApiException {
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
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(musicVideoInfoRemoteSearchQuery);
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
     * Get person remote search.
     * 
     * @param personLookupInfoRemoteSearchQuery Remote search query. (required)
     * @return List&lt;RemoteSearchResult&gt;
     * @throws ApiException if fails to make API call
     */
    public List<RemoteSearchResult> getPersonRemoteSearchResults(
            @org.eclipse.jdt.annotation.Nullable PersonLookupInfoRemoteSearchQuery personLookupInfoRemoteSearchQuery)
            throws ApiException {
        ApiResponse<List<RemoteSearchResult>> localVarResponse = getPersonRemoteSearchResultsWithHttpInfo(
                personLookupInfoRemoteSearchQuery);
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
            @org.eclipse.jdt.annotation.Nullable PersonLookupInfoRemoteSearchQuery personLookupInfoRemoteSearchQuery)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getPersonRemoteSearchResultsRequestBuilder(
                personLookupInfoRemoteSearchQuery);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getPersonRemoteSearchResults", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<List<RemoteSearchResult>>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<List<RemoteSearchResult>>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody,
                                        new TypeReference<List<RemoteSearchResult>>() {
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

    private HttpRequest.Builder getPersonRemoteSearchResultsRequestBuilder(
            @org.eclipse.jdt.annotation.Nullable PersonLookupInfoRemoteSearchQuery personLookupInfoRemoteSearchQuery)
            throws ApiException {
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
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(personLookupInfoRemoteSearchQuery);
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
     * Get series remote search.
     * 
     * @param seriesInfoRemoteSearchQuery Remote search query. (required)
     * @return List&lt;RemoteSearchResult&gt;
     * @throws ApiException if fails to make API call
     */
    public List<RemoteSearchResult> getSeriesRemoteSearchResults(
            @org.eclipse.jdt.annotation.Nullable SeriesInfoRemoteSearchQuery seriesInfoRemoteSearchQuery)
            throws ApiException {
        ApiResponse<List<RemoteSearchResult>> localVarResponse = getSeriesRemoteSearchResultsWithHttpInfo(
                seriesInfoRemoteSearchQuery);
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
            @org.eclipse.jdt.annotation.Nullable SeriesInfoRemoteSearchQuery seriesInfoRemoteSearchQuery)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getSeriesRemoteSearchResultsRequestBuilder(
                seriesInfoRemoteSearchQuery);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getSeriesRemoteSearchResults", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<List<RemoteSearchResult>>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<List<RemoteSearchResult>>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody,
                                        new TypeReference<List<RemoteSearchResult>>() {
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

    private HttpRequest.Builder getSeriesRemoteSearchResultsRequestBuilder(
            @org.eclipse.jdt.annotation.Nullable SeriesInfoRemoteSearchQuery seriesInfoRemoteSearchQuery)
            throws ApiException {
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
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(seriesInfoRemoteSearchQuery);
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
     * Get trailer remote search.
     * 
     * @param trailerInfoRemoteSearchQuery Remote search query. (required)
     * @return List&lt;RemoteSearchResult&gt;
     * @throws ApiException if fails to make API call
     */
    public List<RemoteSearchResult> getTrailerRemoteSearchResults(
            @org.eclipse.jdt.annotation.Nullable TrailerInfoRemoteSearchQuery trailerInfoRemoteSearchQuery)
            throws ApiException {
        ApiResponse<List<RemoteSearchResult>> localVarResponse = getTrailerRemoteSearchResultsWithHttpInfo(
                trailerInfoRemoteSearchQuery);
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
            @org.eclipse.jdt.annotation.Nullable TrailerInfoRemoteSearchQuery trailerInfoRemoteSearchQuery)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getTrailerRemoteSearchResultsRequestBuilder(
                trailerInfoRemoteSearchQuery);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getTrailerRemoteSearchResults", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<List<RemoteSearchResult>>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<List<RemoteSearchResult>>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody,
                                        new TypeReference<List<RemoteSearchResult>>() {
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

    private HttpRequest.Builder getTrailerRemoteSearchResultsRequestBuilder(
            @org.eclipse.jdt.annotation.Nullable TrailerInfoRemoteSearchQuery trailerInfoRemoteSearchQuery)
            throws ApiException {
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
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase");

        try {
            byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(trailerInfoRemoteSearchQuery);
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
}
