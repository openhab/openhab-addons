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
import org.openhab.binding.jellyfin.internal.api.generated.current.model.BaseItemDtoQueryResult;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.ChannelFeatures;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.ItemFields;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.ItemFilter;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.ItemSortBy;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.SortOrder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class ChannelsApi {
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

    public ChannelsApi() {
        this(Configuration.getDefaultApiClient());
    }

    public ChannelsApi(ApiClient apiClient) {
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
     * Get all channel features.
     * 
     * @return List&lt;ChannelFeatures&gt;
     * @throws ApiException if fails to make API call
     */
    public List<ChannelFeatures> getAllChannelFeatures() throws ApiException {
        return getAllChannelFeatures(null);
    }

    /**
     * Get all channel features.
     * 
     * @param headers Optional headers to include in the request
     * @return List&lt;ChannelFeatures&gt;
     * @throws ApiException if fails to make API call
     */
    public List<ChannelFeatures> getAllChannelFeatures(Map<String, String> headers) throws ApiException {
        ApiResponse<List<ChannelFeatures>> localVarResponse = getAllChannelFeaturesWithHttpInfo(headers);
        return localVarResponse.getData();
    }

    /**
     * Get all channel features.
     * 
     * @return ApiResponse&lt;List&lt;ChannelFeatures&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<ChannelFeatures>> getAllChannelFeaturesWithHttpInfo() throws ApiException {
        return getAllChannelFeaturesWithHttpInfo(null);
    }

    /**
     * Get all channel features.
     * 
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;List&lt;ChannelFeatures&gt;&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<List<ChannelFeatures>> getAllChannelFeaturesWithHttpInfo(Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getAllChannelFeaturesRequestBuilder(headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getAllChannelFeatures", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<List<ChannelFeatures>>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                List<ChannelFeatures> responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<List<ChannelFeatures>>() {
                        });

                localVarResponse.body().close();

                return new ApiResponse<List<ChannelFeatures>>(localVarResponse.statusCode(),
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

    private HttpRequest.Builder getAllChannelFeaturesRequestBuilder(Map<String, String> headers) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Channels/Features";

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
     * Get channel features.
     * 
     * @param channelId Channel id. (required)
     * @return ChannelFeatures
     * @throws ApiException if fails to make API call
     */
    public ChannelFeatures getChannelFeatures(@org.eclipse.jdt.annotation.Nullable UUID channelId) throws ApiException {
        return getChannelFeatures(channelId, null);
    }

    /**
     * Get channel features.
     * 
     * @param channelId Channel id. (required)
     * @param headers Optional headers to include in the request
     * @return ChannelFeatures
     * @throws ApiException if fails to make API call
     */
    public ChannelFeatures getChannelFeatures(@org.eclipse.jdt.annotation.Nullable UUID channelId,
            Map<String, String> headers) throws ApiException {
        ApiResponse<ChannelFeatures> localVarResponse = getChannelFeaturesWithHttpInfo(channelId, headers);
        return localVarResponse.getData();
    }

    /**
     * Get channel features.
     * 
     * @param channelId Channel id. (required)
     * @return ApiResponse&lt;ChannelFeatures&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<ChannelFeatures> getChannelFeaturesWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable UUID channelId) throws ApiException {
        return getChannelFeaturesWithHttpInfo(channelId, null);
    }

    /**
     * Get channel features.
     * 
     * @param channelId Channel id. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;ChannelFeatures&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<ChannelFeatures> getChannelFeaturesWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable UUID channelId, Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getChannelFeaturesRequestBuilder(channelId, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getChannelFeatures", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<ChannelFeatures>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                ChannelFeatures responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<ChannelFeatures>() {
                        });

                localVarResponse.body().close();

                return new ApiResponse<ChannelFeatures>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                        responseValue);
            } finally {
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder getChannelFeaturesRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID channelId,
            Map<String, String> headers) throws ApiException {
        // verify the required parameter 'channelId' is set
        if (channelId == null) {
            throw new ApiException(400, "Missing the required parameter 'channelId' when calling getChannelFeatures");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Channels/{channelId}/Features".replace("{channelId}",
                ApiClient.urlEncode(channelId.toString()));

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
     * Get channel items.
     * 
     * @param channelId Channel Id. (required)
     * @param folderId Optional. Folder Id. (optional)
     * @param userId Optional. User Id. (optional)
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param sortOrder Optional. Sort Order - Ascending,Descending. (optional)
     * @param filters Optional. Specify additional filters to apply. (optional)
     * @param sortBy Optional. Specify one or more sort orders, comma delimited. Options: Album, AlbumArtist, Artist,
     *            Budget, CommunityRating, CriticRating, DateCreated, DatePlayed, PlayCount, PremiereDate,
     *            ProductionYear, SortName, Random, Revenue, Runtime. (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. (optional)
     * @return BaseItemDtoQueryResult
     * @throws ApiException if fails to make API call
     */
    public BaseItemDtoQueryResult getChannelItems(@org.eclipse.jdt.annotation.Nullable UUID channelId,
            @org.eclipse.jdt.annotation.NonNull UUID folderId, @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull Integer startIndex, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull List<SortOrder> sortOrder,
            @org.eclipse.jdt.annotation.NonNull List<ItemFilter> filters,
            @org.eclipse.jdt.annotation.NonNull List<ItemSortBy> sortBy,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields) throws ApiException {
        return getChannelItems(channelId, folderId, userId, startIndex, limit, sortOrder, filters, sortBy, fields,
                null);
    }

    /**
     * Get channel items.
     * 
     * @param channelId Channel Id. (required)
     * @param folderId Optional. Folder Id. (optional)
     * @param userId Optional. User Id. (optional)
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param sortOrder Optional. Sort Order - Ascending,Descending. (optional)
     * @param filters Optional. Specify additional filters to apply. (optional)
     * @param sortBy Optional. Specify one or more sort orders, comma delimited. Options: Album, AlbumArtist, Artist,
     *            Budget, CommunityRating, CriticRating, DateCreated, DatePlayed, PlayCount, PremiereDate,
     *            ProductionYear, SortName, Random, Revenue, Runtime. (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. (optional)
     * @param headers Optional headers to include in the request
     * @return BaseItemDtoQueryResult
     * @throws ApiException if fails to make API call
     */
    public BaseItemDtoQueryResult getChannelItems(@org.eclipse.jdt.annotation.Nullable UUID channelId,
            @org.eclipse.jdt.annotation.NonNull UUID folderId, @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull Integer startIndex, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull List<SortOrder> sortOrder,
            @org.eclipse.jdt.annotation.NonNull List<ItemFilter> filters,
            @org.eclipse.jdt.annotation.NonNull List<ItemSortBy> sortBy,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields, Map<String, String> headers)
            throws ApiException {
        ApiResponse<BaseItemDtoQueryResult> localVarResponse = getChannelItemsWithHttpInfo(channelId, folderId, userId,
                startIndex, limit, sortOrder, filters, sortBy, fields, headers);
        return localVarResponse.getData();
    }

    /**
     * Get channel items.
     * 
     * @param channelId Channel Id. (required)
     * @param folderId Optional. Folder Id. (optional)
     * @param userId Optional. User Id. (optional)
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param sortOrder Optional. Sort Order - Ascending,Descending. (optional)
     * @param filters Optional. Specify additional filters to apply. (optional)
     * @param sortBy Optional. Specify one or more sort orders, comma delimited. Options: Album, AlbumArtist, Artist,
     *            Budget, CommunityRating, CriticRating, DateCreated, DatePlayed, PlayCount, PremiereDate,
     *            ProductionYear, SortName, Random, Revenue, Runtime. (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. (optional)
     * @return ApiResponse&lt;BaseItemDtoQueryResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<BaseItemDtoQueryResult> getChannelItemsWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable UUID channelId, @org.eclipse.jdt.annotation.NonNull UUID folderId,
            @org.eclipse.jdt.annotation.NonNull UUID userId, @org.eclipse.jdt.annotation.NonNull Integer startIndex,
            @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull List<SortOrder> sortOrder,
            @org.eclipse.jdt.annotation.NonNull List<ItemFilter> filters,
            @org.eclipse.jdt.annotation.NonNull List<ItemSortBy> sortBy,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields) throws ApiException {
        return getChannelItemsWithHttpInfo(channelId, folderId, userId, startIndex, limit, sortOrder, filters, sortBy,
                fields, null);
    }

    /**
     * Get channel items.
     * 
     * @param channelId Channel Id. (required)
     * @param folderId Optional. Folder Id. (optional)
     * @param userId Optional. User Id. (optional)
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param sortOrder Optional. Sort Order - Ascending,Descending. (optional)
     * @param filters Optional. Specify additional filters to apply. (optional)
     * @param sortBy Optional. Specify one or more sort orders, comma delimited. Options: Album, AlbumArtist, Artist,
     *            Budget, CommunityRating, CriticRating, DateCreated, DatePlayed, PlayCount, PremiereDate,
     *            ProductionYear, SortName, Random, Revenue, Runtime. (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;BaseItemDtoQueryResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<BaseItemDtoQueryResult> getChannelItemsWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable UUID channelId, @org.eclipse.jdt.annotation.NonNull UUID folderId,
            @org.eclipse.jdt.annotation.NonNull UUID userId, @org.eclipse.jdt.annotation.NonNull Integer startIndex,
            @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull List<SortOrder> sortOrder,
            @org.eclipse.jdt.annotation.NonNull List<ItemFilter> filters,
            @org.eclipse.jdt.annotation.NonNull List<ItemSortBy> sortBy,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getChannelItemsRequestBuilder(channelId, folderId, userId,
                startIndex, limit, sortOrder, filters, sortBy, fields, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getChannelItems", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<BaseItemDtoQueryResult>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                BaseItemDtoQueryResult responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<BaseItemDtoQueryResult>() {
                        });

                localVarResponse.body().close();

                return new ApiResponse<BaseItemDtoQueryResult>(localVarResponse.statusCode(),
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

    private HttpRequest.Builder getChannelItemsRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID channelId,
            @org.eclipse.jdt.annotation.NonNull UUID folderId, @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull Integer startIndex, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull List<SortOrder> sortOrder,
            @org.eclipse.jdt.annotation.NonNull List<ItemFilter> filters,
            @org.eclipse.jdt.annotation.NonNull List<ItemSortBy> sortBy,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields, Map<String, String> headers)
            throws ApiException {
        // verify the required parameter 'channelId' is set
        if (channelId == null) {
            throw new ApiException(400, "Missing the required parameter 'channelId' when calling getChannelItems");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Channels/{channelId}/Items".replace("{channelId}",
                ApiClient.urlEncode(channelId.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "folderId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("folderId", folderId));
        localVarQueryParameterBaseName = "userId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("userId", userId));
        localVarQueryParameterBaseName = "startIndex";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("startIndex", startIndex));
        localVarQueryParameterBaseName = "limit";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("limit", limit));
        localVarQueryParameterBaseName = "sortOrder";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "sortOrder", sortOrder));
        localVarQueryParameterBaseName = "filters";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "filters", filters));
        localVarQueryParameterBaseName = "sortBy";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "sortBy", sortBy));
        localVarQueryParameterBaseName = "fields";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "fields", fields));

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
     * Gets available channels.
     * 
     * @param userId User Id to filter by. Use System.Guid.Empty to not filter by user. (optional)
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param supportsLatestItems Optional. Filter by channels that support getting latest items. (optional)
     * @param supportsMediaDeletion Optional. Filter by channels that support media deletion. (optional)
     * @param isFavorite Optional. Filter by channels that are favorite. (optional)
     * @return BaseItemDtoQueryResult
     * @throws ApiException if fails to make API call
     */
    public BaseItemDtoQueryResult getChannels(@org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull Integer startIndex, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull Boolean supportsLatestItems,
            @org.eclipse.jdt.annotation.NonNull Boolean supportsMediaDeletion,
            @org.eclipse.jdt.annotation.NonNull Boolean isFavorite) throws ApiException {
        return getChannels(userId, startIndex, limit, supportsLatestItems, supportsMediaDeletion, isFavorite, null);
    }

    /**
     * Gets available channels.
     * 
     * @param userId User Id to filter by. Use System.Guid.Empty to not filter by user. (optional)
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param supportsLatestItems Optional. Filter by channels that support getting latest items. (optional)
     * @param supportsMediaDeletion Optional. Filter by channels that support media deletion. (optional)
     * @param isFavorite Optional. Filter by channels that are favorite. (optional)
     * @param headers Optional headers to include in the request
     * @return BaseItemDtoQueryResult
     * @throws ApiException if fails to make API call
     */
    public BaseItemDtoQueryResult getChannels(@org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull Integer startIndex, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull Boolean supportsLatestItems,
            @org.eclipse.jdt.annotation.NonNull Boolean supportsMediaDeletion,
            @org.eclipse.jdt.annotation.NonNull Boolean isFavorite, Map<String, String> headers) throws ApiException {
        ApiResponse<BaseItemDtoQueryResult> localVarResponse = getChannelsWithHttpInfo(userId, startIndex, limit,
                supportsLatestItems, supportsMediaDeletion, isFavorite, headers);
        return localVarResponse.getData();
    }

    /**
     * Gets available channels.
     * 
     * @param userId User Id to filter by. Use System.Guid.Empty to not filter by user. (optional)
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param supportsLatestItems Optional. Filter by channels that support getting latest items. (optional)
     * @param supportsMediaDeletion Optional. Filter by channels that support media deletion. (optional)
     * @param isFavorite Optional. Filter by channels that are favorite. (optional)
     * @return ApiResponse&lt;BaseItemDtoQueryResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<BaseItemDtoQueryResult> getChannelsWithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull Integer startIndex, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull Boolean supportsLatestItems,
            @org.eclipse.jdt.annotation.NonNull Boolean supportsMediaDeletion,
            @org.eclipse.jdt.annotation.NonNull Boolean isFavorite) throws ApiException {
        return getChannelsWithHttpInfo(userId, startIndex, limit, supportsLatestItems, supportsMediaDeletion,
                isFavorite, null);
    }

    /**
     * Gets available channels.
     * 
     * @param userId User Id to filter by. Use System.Guid.Empty to not filter by user. (optional)
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param supportsLatestItems Optional. Filter by channels that support getting latest items. (optional)
     * @param supportsMediaDeletion Optional. Filter by channels that support media deletion. (optional)
     * @param isFavorite Optional. Filter by channels that are favorite. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;BaseItemDtoQueryResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<BaseItemDtoQueryResult> getChannelsWithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull Integer startIndex, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull Boolean supportsLatestItems,
            @org.eclipse.jdt.annotation.NonNull Boolean supportsMediaDeletion,
            @org.eclipse.jdt.annotation.NonNull Boolean isFavorite, Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getChannelsRequestBuilder(userId, startIndex, limit,
                supportsLatestItems, supportsMediaDeletion, isFavorite, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getChannels", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<BaseItemDtoQueryResult>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                BaseItemDtoQueryResult responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<BaseItemDtoQueryResult>() {
                        });

                localVarResponse.body().close();

                return new ApiResponse<BaseItemDtoQueryResult>(localVarResponse.statusCode(),
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

    private HttpRequest.Builder getChannelsRequestBuilder(@org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull Integer startIndex, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull Boolean supportsLatestItems,
            @org.eclipse.jdt.annotation.NonNull Boolean supportsMediaDeletion,
            @org.eclipse.jdt.annotation.NonNull Boolean isFavorite, Map<String, String> headers) throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Channels";

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "userId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("userId", userId));
        localVarQueryParameterBaseName = "startIndex";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("startIndex", startIndex));
        localVarQueryParameterBaseName = "limit";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("limit", limit));
        localVarQueryParameterBaseName = "supportsLatestItems";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("supportsLatestItems", supportsLatestItems));
        localVarQueryParameterBaseName = "supportsMediaDeletion";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("supportsMediaDeletion", supportsMediaDeletion));
        localVarQueryParameterBaseName = "isFavorite";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("isFavorite", isFavorite));

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
     * Gets latest channel items.
     * 
     * @param userId Optional. User Id. (optional)
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param filters Optional. Specify additional filters to apply. (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. (optional)
     * @param channelIds Optional. Specify one or more channel id&#39;s, comma delimited. (optional)
     * @return BaseItemDtoQueryResult
     * @throws ApiException if fails to make API call
     */
    public BaseItemDtoQueryResult getLatestChannelItems(@org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull Integer startIndex, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull List<ItemFilter> filters,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull List<UUID> channelIds) throws ApiException {
        return getLatestChannelItems(userId, startIndex, limit, filters, fields, channelIds, null);
    }

    /**
     * Gets latest channel items.
     * 
     * @param userId Optional. User Id. (optional)
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param filters Optional. Specify additional filters to apply. (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. (optional)
     * @param channelIds Optional. Specify one or more channel id&#39;s, comma delimited. (optional)
     * @param headers Optional headers to include in the request
     * @return BaseItemDtoQueryResult
     * @throws ApiException if fails to make API call
     */
    public BaseItemDtoQueryResult getLatestChannelItems(@org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull Integer startIndex, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull List<ItemFilter> filters,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull List<UUID> channelIds, Map<String, String> headers)
            throws ApiException {
        ApiResponse<BaseItemDtoQueryResult> localVarResponse = getLatestChannelItemsWithHttpInfo(userId, startIndex,
                limit, filters, fields, channelIds, headers);
        return localVarResponse.getData();
    }

    /**
     * Gets latest channel items.
     * 
     * @param userId Optional. User Id. (optional)
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param filters Optional. Specify additional filters to apply. (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. (optional)
     * @param channelIds Optional. Specify one or more channel id&#39;s, comma delimited. (optional)
     * @return ApiResponse&lt;BaseItemDtoQueryResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<BaseItemDtoQueryResult> getLatestChannelItemsWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull UUID userId, @org.eclipse.jdt.annotation.NonNull Integer startIndex,
            @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull List<ItemFilter> filters,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull List<UUID> channelIds) throws ApiException {
        return getLatestChannelItemsWithHttpInfo(userId, startIndex, limit, filters, fields, channelIds, null);
    }

    /**
     * Gets latest channel items.
     * 
     * @param userId Optional. User Id. (optional)
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param filters Optional. Specify additional filters to apply. (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. (optional)
     * @param channelIds Optional. Specify one or more channel id&#39;s, comma delimited. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;BaseItemDtoQueryResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<BaseItemDtoQueryResult> getLatestChannelItemsWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull UUID userId, @org.eclipse.jdt.annotation.NonNull Integer startIndex,
            @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull List<ItemFilter> filters,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull List<UUID> channelIds, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getLatestChannelItemsRequestBuilder(userId, startIndex, limit,
                filters, fields, channelIds, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getLatestChannelItems", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<BaseItemDtoQueryResult>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                BaseItemDtoQueryResult responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<BaseItemDtoQueryResult>() {
                        });

                localVarResponse.body().close();

                return new ApiResponse<BaseItemDtoQueryResult>(localVarResponse.statusCode(),
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

    private HttpRequest.Builder getLatestChannelItemsRequestBuilder(@org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull Integer startIndex, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull List<ItemFilter> filters,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull List<UUID> channelIds, Map<String, String> headers)
            throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Channels/Items/Latest";

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "userId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("userId", userId));
        localVarQueryParameterBaseName = "startIndex";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("startIndex", startIndex));
        localVarQueryParameterBaseName = "limit";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("limit", limit));
        localVarQueryParameterBaseName = "filters";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "filters", filters));
        localVarQueryParameterBaseName = "fields";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "fields", fields));
        localVarQueryParameterBaseName = "channelIds";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "channelIds", channelIds));

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
