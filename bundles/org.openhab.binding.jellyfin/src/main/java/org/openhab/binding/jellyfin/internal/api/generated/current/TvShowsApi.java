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
import java.time.OffsetDateTime;
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
import org.openhab.binding.jellyfin.internal.api.generated.current.model.ImageType;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.ItemFields;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.ItemSortBy;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class TvShowsApi {
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

    public TvShowsApi() {
        this(Configuration.getDefaultApiClient());
    }

    public TvShowsApi(ApiClient apiClient) {
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
     * Gets episodes for a tv season.
     * 
     * @param seriesId The series id. (required)
     * @param userId The user id. (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple,
     *            comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions,
     *            MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue,
     *            SortName, Studios, Taglines, TrailerUrls. (optional)
     * @param season Optional filter by season number. (optional)
     * @param seasonId Optional. Filter by season id. (optional)
     * @param isMissing Optional. Filter by items that are missing episodes or not. (optional)
     * @param adjacentTo Optional. Return items that are siblings of a supplied item. (optional)
     * @param startItemId Optional. Skip through the list until a given item is found. (optional)
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param enableImages Optional, include image information in output. (optional)
     * @param imageTypeLimit Optional, the max number of images to return, per image type. (optional)
     * @param enableImageTypes Optional. The image types to include in the output. (optional)
     * @param enableUserData Optional. Include user data. (optional)
     * @param sortBy Optional. Specify one or more sort orders, comma delimited. Options: Album, AlbumArtist, Artist,
     *            Budget, CommunityRating, CriticRating, DateCreated, DatePlayed, PlayCount, PremiereDate,
     *            ProductionYear, SortName, Random, Revenue, Runtime. (optional)
     * @return BaseItemDtoQueryResult
     * @throws ApiException if fails to make API call
     */
    public BaseItemDtoQueryResult getEpisodes(@org.eclipse.jdt.annotation.Nullable UUID seriesId,
            @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull Integer season, @org.eclipse.jdt.annotation.NonNull UUID seasonId,
            @org.eclipse.jdt.annotation.NonNull Boolean isMissing, @org.eclipse.jdt.annotation.NonNull UUID adjacentTo,
            @org.eclipse.jdt.annotation.NonNull UUID startItemId,
            @org.eclipse.jdt.annotation.NonNull Integer startIndex, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull Boolean enableImages,
            @org.eclipse.jdt.annotation.NonNull Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.NonNull List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.NonNull Boolean enableUserData,
            @org.eclipse.jdt.annotation.NonNull ItemSortBy sortBy) throws ApiException {
        return getEpisodes(seriesId, userId, fields, season, seasonId, isMissing, adjacentTo, startItemId, startIndex,
                limit, enableImages, imageTypeLimit, enableImageTypes, enableUserData, sortBy, null);
    }

    /**
     * Gets episodes for a tv season.
     * 
     * @param seriesId The series id. (required)
     * @param userId The user id. (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple,
     *            comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions,
     *            MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue,
     *            SortName, Studios, Taglines, TrailerUrls. (optional)
     * @param season Optional filter by season number. (optional)
     * @param seasonId Optional. Filter by season id. (optional)
     * @param isMissing Optional. Filter by items that are missing episodes or not. (optional)
     * @param adjacentTo Optional. Return items that are siblings of a supplied item. (optional)
     * @param startItemId Optional. Skip through the list until a given item is found. (optional)
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param enableImages Optional, include image information in output. (optional)
     * @param imageTypeLimit Optional, the max number of images to return, per image type. (optional)
     * @param enableImageTypes Optional. The image types to include in the output. (optional)
     * @param enableUserData Optional. Include user data. (optional)
     * @param sortBy Optional. Specify one or more sort orders, comma delimited. Options: Album, AlbumArtist, Artist,
     *            Budget, CommunityRating, CriticRating, DateCreated, DatePlayed, PlayCount, PremiereDate,
     *            ProductionYear, SortName, Random, Revenue, Runtime. (optional)
     * @param headers Optional headers to include in the request
     * @return BaseItemDtoQueryResult
     * @throws ApiException if fails to make API call
     */
    public BaseItemDtoQueryResult getEpisodes(@org.eclipse.jdt.annotation.Nullable UUID seriesId,
            @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull Integer season, @org.eclipse.jdt.annotation.NonNull UUID seasonId,
            @org.eclipse.jdt.annotation.NonNull Boolean isMissing, @org.eclipse.jdt.annotation.NonNull UUID adjacentTo,
            @org.eclipse.jdt.annotation.NonNull UUID startItemId,
            @org.eclipse.jdt.annotation.NonNull Integer startIndex, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull Boolean enableImages,
            @org.eclipse.jdt.annotation.NonNull Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.NonNull List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.NonNull Boolean enableUserData,
            @org.eclipse.jdt.annotation.NonNull ItemSortBy sortBy, Map<String, String> headers) throws ApiException {
        ApiResponse<BaseItemDtoQueryResult> localVarResponse = getEpisodesWithHttpInfo(seriesId, userId, fields, season,
                seasonId, isMissing, adjacentTo, startItemId, startIndex, limit, enableImages, imageTypeLimit,
                enableImageTypes, enableUserData, sortBy, headers);
        return localVarResponse.getData();
    }

    /**
     * Gets episodes for a tv season.
     * 
     * @param seriesId The series id. (required)
     * @param userId The user id. (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple,
     *            comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions,
     *            MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue,
     *            SortName, Studios, Taglines, TrailerUrls. (optional)
     * @param season Optional filter by season number. (optional)
     * @param seasonId Optional. Filter by season id. (optional)
     * @param isMissing Optional. Filter by items that are missing episodes or not. (optional)
     * @param adjacentTo Optional. Return items that are siblings of a supplied item. (optional)
     * @param startItemId Optional. Skip through the list until a given item is found. (optional)
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param enableImages Optional, include image information in output. (optional)
     * @param imageTypeLimit Optional, the max number of images to return, per image type. (optional)
     * @param enableImageTypes Optional. The image types to include in the output. (optional)
     * @param enableUserData Optional. Include user data. (optional)
     * @param sortBy Optional. Specify one or more sort orders, comma delimited. Options: Album, AlbumArtist, Artist,
     *            Budget, CommunityRating, CriticRating, DateCreated, DatePlayed, PlayCount, PremiereDate,
     *            ProductionYear, SortName, Random, Revenue, Runtime. (optional)
     * @return ApiResponse&lt;BaseItemDtoQueryResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<BaseItemDtoQueryResult> getEpisodesWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable UUID seriesId, @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull Integer season, @org.eclipse.jdt.annotation.NonNull UUID seasonId,
            @org.eclipse.jdt.annotation.NonNull Boolean isMissing, @org.eclipse.jdt.annotation.NonNull UUID adjacentTo,
            @org.eclipse.jdt.annotation.NonNull UUID startItemId,
            @org.eclipse.jdt.annotation.NonNull Integer startIndex, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull Boolean enableImages,
            @org.eclipse.jdt.annotation.NonNull Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.NonNull List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.NonNull Boolean enableUserData,
            @org.eclipse.jdt.annotation.NonNull ItemSortBy sortBy) throws ApiException {
        return getEpisodesWithHttpInfo(seriesId, userId, fields, season, seasonId, isMissing, adjacentTo, startItemId,
                startIndex, limit, enableImages, imageTypeLimit, enableImageTypes, enableUserData, sortBy, null);
    }

    /**
     * Gets episodes for a tv season.
     * 
     * @param seriesId The series id. (required)
     * @param userId The user id. (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple,
     *            comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions,
     *            MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue,
     *            SortName, Studios, Taglines, TrailerUrls. (optional)
     * @param season Optional filter by season number. (optional)
     * @param seasonId Optional. Filter by season id. (optional)
     * @param isMissing Optional. Filter by items that are missing episodes or not. (optional)
     * @param adjacentTo Optional. Return items that are siblings of a supplied item. (optional)
     * @param startItemId Optional. Skip through the list until a given item is found. (optional)
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param enableImages Optional, include image information in output. (optional)
     * @param imageTypeLimit Optional, the max number of images to return, per image type. (optional)
     * @param enableImageTypes Optional. The image types to include in the output. (optional)
     * @param enableUserData Optional. Include user data. (optional)
     * @param sortBy Optional. Specify one or more sort orders, comma delimited. Options: Album, AlbumArtist, Artist,
     *            Budget, CommunityRating, CriticRating, DateCreated, DatePlayed, PlayCount, PremiereDate,
     *            ProductionYear, SortName, Random, Revenue, Runtime. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;BaseItemDtoQueryResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<BaseItemDtoQueryResult> getEpisodesWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable UUID seriesId, @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull Integer season, @org.eclipse.jdt.annotation.NonNull UUID seasonId,
            @org.eclipse.jdt.annotation.NonNull Boolean isMissing, @org.eclipse.jdt.annotation.NonNull UUID adjacentTo,
            @org.eclipse.jdt.annotation.NonNull UUID startItemId,
            @org.eclipse.jdt.annotation.NonNull Integer startIndex, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull Boolean enableImages,
            @org.eclipse.jdt.annotation.NonNull Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.NonNull List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.NonNull Boolean enableUserData,
            @org.eclipse.jdt.annotation.NonNull ItemSortBy sortBy, Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getEpisodesRequestBuilder(seriesId, userId, fields, season,
                seasonId, isMissing, adjacentTo, startItemId, startIndex, limit, enableImages, imageTypeLimit,
                enableImageTypes, enableUserData, sortBy, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getEpisodes", localVarResponse);
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

    private HttpRequest.Builder getEpisodesRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID seriesId,
            @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull Integer season, @org.eclipse.jdt.annotation.NonNull UUID seasonId,
            @org.eclipse.jdt.annotation.NonNull Boolean isMissing, @org.eclipse.jdt.annotation.NonNull UUID adjacentTo,
            @org.eclipse.jdt.annotation.NonNull UUID startItemId,
            @org.eclipse.jdt.annotation.NonNull Integer startIndex, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull Boolean enableImages,
            @org.eclipse.jdt.annotation.NonNull Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.NonNull List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.NonNull Boolean enableUserData,
            @org.eclipse.jdt.annotation.NonNull ItemSortBy sortBy, Map<String, String> headers) throws ApiException {
        // verify the required parameter 'seriesId' is set
        if (seriesId == null) {
            throw new ApiException(400, "Missing the required parameter 'seriesId' when calling getEpisodes");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Shows/{seriesId}/Episodes".replace("{seriesId}",
                ApiClient.urlEncode(seriesId.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "userId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("userId", userId));
        localVarQueryParameterBaseName = "fields";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "fields", fields));
        localVarQueryParameterBaseName = "season";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("season", season));
        localVarQueryParameterBaseName = "seasonId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("seasonId", seasonId));
        localVarQueryParameterBaseName = "isMissing";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("isMissing", isMissing));
        localVarQueryParameterBaseName = "adjacentTo";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("adjacentTo", adjacentTo));
        localVarQueryParameterBaseName = "startItemId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("startItemId", startItemId));
        localVarQueryParameterBaseName = "startIndex";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("startIndex", startIndex));
        localVarQueryParameterBaseName = "limit";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("limit", limit));
        localVarQueryParameterBaseName = "enableImages";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("enableImages", enableImages));
        localVarQueryParameterBaseName = "imageTypeLimit";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("imageTypeLimit", imageTypeLimit));
        localVarQueryParameterBaseName = "enableImageTypes";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "enableImageTypes", enableImageTypes));
        localVarQueryParameterBaseName = "enableUserData";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("enableUserData", enableUserData));
        localVarQueryParameterBaseName = "sortBy";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("sortBy", sortBy));

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
     * Gets a list of next up episodes.
     * 
     * @param userId The user id of the user to get the next up episodes for. (optional)
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. (optional)
     * @param seriesId Optional. Filter by series id. (optional)
     * @param parentId Optional. Specify this to localize the search to a specific item or folder. Omit to use the root.
     *            (optional)
     * @param enableImages Optional. Include image information in output. (optional)
     * @param imageTypeLimit Optional. The max number of images to return, per image type. (optional)
     * @param enableImageTypes Optional. The image types to include in the output. (optional)
     * @param enableUserData Optional. Include user data. (optional)
     * @param nextUpDateCutoff Optional. Starting date of shows to show in Next Up section. (optional)
     * @param enableTotalRecordCount Whether to enable the total records count. Defaults to true. (optional, default to
     *            true)
     * @param disableFirstEpisode Whether to disable sending the first episode in a series as next up. (optional,
     *            default to false)
     * @param enableResumable Whether to include resumable episodes in next up results. (optional, default to true)
     * @param enableRewatching Whether to include watched episodes in next up results. (optional, default to false)
     * @return BaseItemDtoQueryResult
     * @throws ApiException if fails to make API call
     */
    public BaseItemDtoQueryResult getNextUp(@org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull Integer startIndex, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull UUID seriesId, @org.eclipse.jdt.annotation.NonNull UUID parentId,
            @org.eclipse.jdt.annotation.NonNull Boolean enableImages,
            @org.eclipse.jdt.annotation.NonNull Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.NonNull List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.NonNull Boolean enableUserData,
            @org.eclipse.jdt.annotation.NonNull OffsetDateTime nextUpDateCutoff,
            @org.eclipse.jdt.annotation.NonNull Boolean enableTotalRecordCount,
            @org.eclipse.jdt.annotation.NonNull Boolean disableFirstEpisode,
            @org.eclipse.jdt.annotation.NonNull Boolean enableResumable,
            @org.eclipse.jdt.annotation.NonNull Boolean enableRewatching) throws ApiException {
        return getNextUp(userId, startIndex, limit, fields, seriesId, parentId, enableImages, imageTypeLimit,
                enableImageTypes, enableUserData, nextUpDateCutoff, enableTotalRecordCount, disableFirstEpisode,
                enableResumable, enableRewatching, null);
    }

    /**
     * Gets a list of next up episodes.
     * 
     * @param userId The user id of the user to get the next up episodes for. (optional)
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. (optional)
     * @param seriesId Optional. Filter by series id. (optional)
     * @param parentId Optional. Specify this to localize the search to a specific item or folder. Omit to use the root.
     *            (optional)
     * @param enableImages Optional. Include image information in output. (optional)
     * @param imageTypeLimit Optional. The max number of images to return, per image type. (optional)
     * @param enableImageTypes Optional. The image types to include in the output. (optional)
     * @param enableUserData Optional. Include user data. (optional)
     * @param nextUpDateCutoff Optional. Starting date of shows to show in Next Up section. (optional)
     * @param enableTotalRecordCount Whether to enable the total records count. Defaults to true. (optional, default to
     *            true)
     * @param disableFirstEpisode Whether to disable sending the first episode in a series as next up. (optional,
     *            default to false)
     * @param enableResumable Whether to include resumable episodes in next up results. (optional, default to true)
     * @param enableRewatching Whether to include watched episodes in next up results. (optional, default to false)
     * @param headers Optional headers to include in the request
     * @return BaseItemDtoQueryResult
     * @throws ApiException if fails to make API call
     */
    public BaseItemDtoQueryResult getNextUp(@org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull Integer startIndex, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull UUID seriesId, @org.eclipse.jdt.annotation.NonNull UUID parentId,
            @org.eclipse.jdt.annotation.NonNull Boolean enableImages,
            @org.eclipse.jdt.annotation.NonNull Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.NonNull List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.NonNull Boolean enableUserData,
            @org.eclipse.jdt.annotation.NonNull OffsetDateTime nextUpDateCutoff,
            @org.eclipse.jdt.annotation.NonNull Boolean enableTotalRecordCount,
            @org.eclipse.jdt.annotation.NonNull Boolean disableFirstEpisode,
            @org.eclipse.jdt.annotation.NonNull Boolean enableResumable,
            @org.eclipse.jdt.annotation.NonNull Boolean enableRewatching, Map<String, String> headers)
            throws ApiException {
        ApiResponse<BaseItemDtoQueryResult> localVarResponse = getNextUpWithHttpInfo(userId, startIndex, limit, fields,
                seriesId, parentId, enableImages, imageTypeLimit, enableImageTypes, enableUserData, nextUpDateCutoff,
                enableTotalRecordCount, disableFirstEpisode, enableResumable, enableRewatching, headers);
        return localVarResponse.getData();
    }

    /**
     * Gets a list of next up episodes.
     * 
     * @param userId The user id of the user to get the next up episodes for. (optional)
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. (optional)
     * @param seriesId Optional. Filter by series id. (optional)
     * @param parentId Optional. Specify this to localize the search to a specific item or folder. Omit to use the root.
     *            (optional)
     * @param enableImages Optional. Include image information in output. (optional)
     * @param imageTypeLimit Optional. The max number of images to return, per image type. (optional)
     * @param enableImageTypes Optional. The image types to include in the output. (optional)
     * @param enableUserData Optional. Include user data. (optional)
     * @param nextUpDateCutoff Optional. Starting date of shows to show in Next Up section. (optional)
     * @param enableTotalRecordCount Whether to enable the total records count. Defaults to true. (optional, default to
     *            true)
     * @param disableFirstEpisode Whether to disable sending the first episode in a series as next up. (optional,
     *            default to false)
     * @param enableResumable Whether to include resumable episodes in next up results. (optional, default to true)
     * @param enableRewatching Whether to include watched episodes in next up results. (optional, default to false)
     * @return ApiResponse&lt;BaseItemDtoQueryResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<BaseItemDtoQueryResult> getNextUpWithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull Integer startIndex, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull UUID seriesId, @org.eclipse.jdt.annotation.NonNull UUID parentId,
            @org.eclipse.jdt.annotation.NonNull Boolean enableImages,
            @org.eclipse.jdt.annotation.NonNull Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.NonNull List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.NonNull Boolean enableUserData,
            @org.eclipse.jdt.annotation.NonNull OffsetDateTime nextUpDateCutoff,
            @org.eclipse.jdt.annotation.NonNull Boolean enableTotalRecordCount,
            @org.eclipse.jdt.annotation.NonNull Boolean disableFirstEpisode,
            @org.eclipse.jdt.annotation.NonNull Boolean enableResumable,
            @org.eclipse.jdt.annotation.NonNull Boolean enableRewatching) throws ApiException {
        return getNextUpWithHttpInfo(userId, startIndex, limit, fields, seriesId, parentId, enableImages,
                imageTypeLimit, enableImageTypes, enableUserData, nextUpDateCutoff, enableTotalRecordCount,
                disableFirstEpisode, enableResumable, enableRewatching, null);
    }

    /**
     * Gets a list of next up episodes.
     * 
     * @param userId The user id of the user to get the next up episodes for. (optional)
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. (optional)
     * @param seriesId Optional. Filter by series id. (optional)
     * @param parentId Optional. Specify this to localize the search to a specific item or folder. Omit to use the root.
     *            (optional)
     * @param enableImages Optional. Include image information in output. (optional)
     * @param imageTypeLimit Optional. The max number of images to return, per image type. (optional)
     * @param enableImageTypes Optional. The image types to include in the output. (optional)
     * @param enableUserData Optional. Include user data. (optional)
     * @param nextUpDateCutoff Optional. Starting date of shows to show in Next Up section. (optional)
     * @param enableTotalRecordCount Whether to enable the total records count. Defaults to true. (optional, default to
     *            true)
     * @param disableFirstEpisode Whether to disable sending the first episode in a series as next up. (optional,
     *            default to false)
     * @param enableResumable Whether to include resumable episodes in next up results. (optional, default to true)
     * @param enableRewatching Whether to include watched episodes in next up results. (optional, default to false)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;BaseItemDtoQueryResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<BaseItemDtoQueryResult> getNextUpWithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull Integer startIndex, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull UUID seriesId, @org.eclipse.jdt.annotation.NonNull UUID parentId,
            @org.eclipse.jdt.annotation.NonNull Boolean enableImages,
            @org.eclipse.jdt.annotation.NonNull Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.NonNull List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.NonNull Boolean enableUserData,
            @org.eclipse.jdt.annotation.NonNull OffsetDateTime nextUpDateCutoff,
            @org.eclipse.jdt.annotation.NonNull Boolean enableTotalRecordCount,
            @org.eclipse.jdt.annotation.NonNull Boolean disableFirstEpisode,
            @org.eclipse.jdt.annotation.NonNull Boolean enableResumable,
            @org.eclipse.jdt.annotation.NonNull Boolean enableRewatching, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getNextUpRequestBuilder(userId, startIndex, limit, fields,
                seriesId, parentId, enableImages, imageTypeLimit, enableImageTypes, enableUserData, nextUpDateCutoff,
                enableTotalRecordCount, disableFirstEpisode, enableResumable, enableRewatching, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getNextUp", localVarResponse);
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

    private HttpRequest.Builder getNextUpRequestBuilder(@org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull Integer startIndex, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull UUID seriesId, @org.eclipse.jdt.annotation.NonNull UUID parentId,
            @org.eclipse.jdt.annotation.NonNull Boolean enableImages,
            @org.eclipse.jdt.annotation.NonNull Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.NonNull List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.NonNull Boolean enableUserData,
            @org.eclipse.jdt.annotation.NonNull OffsetDateTime nextUpDateCutoff,
            @org.eclipse.jdt.annotation.NonNull Boolean enableTotalRecordCount,
            @org.eclipse.jdt.annotation.NonNull Boolean disableFirstEpisode,
            @org.eclipse.jdt.annotation.NonNull Boolean enableResumable,
            @org.eclipse.jdt.annotation.NonNull Boolean enableRewatching, Map<String, String> headers)
            throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Shows/NextUp";

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "userId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("userId", userId));
        localVarQueryParameterBaseName = "startIndex";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("startIndex", startIndex));
        localVarQueryParameterBaseName = "limit";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("limit", limit));
        localVarQueryParameterBaseName = "fields";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "fields", fields));
        localVarQueryParameterBaseName = "seriesId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("seriesId", seriesId));
        localVarQueryParameterBaseName = "parentId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("parentId", parentId));
        localVarQueryParameterBaseName = "enableImages";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("enableImages", enableImages));
        localVarQueryParameterBaseName = "imageTypeLimit";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("imageTypeLimit", imageTypeLimit));
        localVarQueryParameterBaseName = "enableImageTypes";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "enableImageTypes", enableImageTypes));
        localVarQueryParameterBaseName = "enableUserData";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("enableUserData", enableUserData));
        localVarQueryParameterBaseName = "nextUpDateCutoff";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("nextUpDateCutoff", nextUpDateCutoff));
        localVarQueryParameterBaseName = "enableTotalRecordCount";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("enableTotalRecordCount", enableTotalRecordCount));
        localVarQueryParameterBaseName = "disableFirstEpisode";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("disableFirstEpisode", disableFirstEpisode));
        localVarQueryParameterBaseName = "enableResumable";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("enableResumable", enableResumable));
        localVarQueryParameterBaseName = "enableRewatching";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("enableRewatching", enableRewatching));

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
     * Gets seasons for a tv series.
     * 
     * @param seriesId The series id. (required)
     * @param userId The user id. (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple,
     *            comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions,
     *            MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue,
     *            SortName, Studios, Taglines, TrailerUrls. (optional)
     * @param isSpecialSeason Optional. Filter by special season. (optional)
     * @param isMissing Optional. Filter by items that are missing episodes or not. (optional)
     * @param adjacentTo Optional. Return items that are siblings of a supplied item. (optional)
     * @param enableImages Optional. Include image information in output. (optional)
     * @param imageTypeLimit Optional. The max number of images to return, per image type. (optional)
     * @param enableImageTypes Optional. The image types to include in the output. (optional)
     * @param enableUserData Optional. Include user data. (optional)
     * @return BaseItemDtoQueryResult
     * @throws ApiException if fails to make API call
     */
    public BaseItemDtoQueryResult getSeasons(@org.eclipse.jdt.annotation.Nullable UUID seriesId,
            @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull Boolean isSpecialSeason,
            @org.eclipse.jdt.annotation.NonNull Boolean isMissing, @org.eclipse.jdt.annotation.NonNull UUID adjacentTo,
            @org.eclipse.jdt.annotation.NonNull Boolean enableImages,
            @org.eclipse.jdt.annotation.NonNull Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.NonNull List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.NonNull Boolean enableUserData) throws ApiException {
        return getSeasons(seriesId, userId, fields, isSpecialSeason, isMissing, adjacentTo, enableImages,
                imageTypeLimit, enableImageTypes, enableUserData, null);
    }

    /**
     * Gets seasons for a tv series.
     * 
     * @param seriesId The series id. (required)
     * @param userId The user id. (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple,
     *            comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions,
     *            MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue,
     *            SortName, Studios, Taglines, TrailerUrls. (optional)
     * @param isSpecialSeason Optional. Filter by special season. (optional)
     * @param isMissing Optional. Filter by items that are missing episodes or not. (optional)
     * @param adjacentTo Optional. Return items that are siblings of a supplied item. (optional)
     * @param enableImages Optional. Include image information in output. (optional)
     * @param imageTypeLimit Optional. The max number of images to return, per image type. (optional)
     * @param enableImageTypes Optional. The image types to include in the output. (optional)
     * @param enableUserData Optional. Include user data. (optional)
     * @param headers Optional headers to include in the request
     * @return BaseItemDtoQueryResult
     * @throws ApiException if fails to make API call
     */
    public BaseItemDtoQueryResult getSeasons(@org.eclipse.jdt.annotation.Nullable UUID seriesId,
            @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull Boolean isSpecialSeason,
            @org.eclipse.jdt.annotation.NonNull Boolean isMissing, @org.eclipse.jdt.annotation.NonNull UUID adjacentTo,
            @org.eclipse.jdt.annotation.NonNull Boolean enableImages,
            @org.eclipse.jdt.annotation.NonNull Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.NonNull List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.NonNull Boolean enableUserData, Map<String, String> headers)
            throws ApiException {
        ApiResponse<BaseItemDtoQueryResult> localVarResponse = getSeasonsWithHttpInfo(seriesId, userId, fields,
                isSpecialSeason, isMissing, adjacentTo, enableImages, imageTypeLimit, enableImageTypes, enableUserData,
                headers);
        return localVarResponse.getData();
    }

    /**
     * Gets seasons for a tv series.
     * 
     * @param seriesId The series id. (required)
     * @param userId The user id. (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple,
     *            comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions,
     *            MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue,
     *            SortName, Studios, Taglines, TrailerUrls. (optional)
     * @param isSpecialSeason Optional. Filter by special season. (optional)
     * @param isMissing Optional. Filter by items that are missing episodes or not. (optional)
     * @param adjacentTo Optional. Return items that are siblings of a supplied item. (optional)
     * @param enableImages Optional. Include image information in output. (optional)
     * @param imageTypeLimit Optional. The max number of images to return, per image type. (optional)
     * @param enableImageTypes Optional. The image types to include in the output. (optional)
     * @param enableUserData Optional. Include user data. (optional)
     * @return ApiResponse&lt;BaseItemDtoQueryResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<BaseItemDtoQueryResult> getSeasonsWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable UUID seriesId, @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull Boolean isSpecialSeason,
            @org.eclipse.jdt.annotation.NonNull Boolean isMissing, @org.eclipse.jdt.annotation.NonNull UUID adjacentTo,
            @org.eclipse.jdt.annotation.NonNull Boolean enableImages,
            @org.eclipse.jdt.annotation.NonNull Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.NonNull List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.NonNull Boolean enableUserData) throws ApiException {
        return getSeasonsWithHttpInfo(seriesId, userId, fields, isSpecialSeason, isMissing, adjacentTo, enableImages,
                imageTypeLimit, enableImageTypes, enableUserData, null);
    }

    /**
     * Gets seasons for a tv series.
     * 
     * @param seriesId The series id. (required)
     * @param userId The user id. (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple,
     *            comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions,
     *            MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue,
     *            SortName, Studios, Taglines, TrailerUrls. (optional)
     * @param isSpecialSeason Optional. Filter by special season. (optional)
     * @param isMissing Optional. Filter by items that are missing episodes or not. (optional)
     * @param adjacentTo Optional. Return items that are siblings of a supplied item. (optional)
     * @param enableImages Optional. Include image information in output. (optional)
     * @param imageTypeLimit Optional. The max number of images to return, per image type. (optional)
     * @param enableImageTypes Optional. The image types to include in the output. (optional)
     * @param enableUserData Optional. Include user data. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;BaseItemDtoQueryResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<BaseItemDtoQueryResult> getSeasonsWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable UUID seriesId, @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull Boolean isSpecialSeason,
            @org.eclipse.jdt.annotation.NonNull Boolean isMissing, @org.eclipse.jdt.annotation.NonNull UUID adjacentTo,
            @org.eclipse.jdt.annotation.NonNull Boolean enableImages,
            @org.eclipse.jdt.annotation.NonNull Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.NonNull List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.NonNull Boolean enableUserData, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getSeasonsRequestBuilder(seriesId, userId, fields, isSpecialSeason,
                isMissing, adjacentTo, enableImages, imageTypeLimit, enableImageTypes, enableUserData, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getSeasons", localVarResponse);
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

    private HttpRequest.Builder getSeasonsRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID seriesId,
            @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull Boolean isSpecialSeason,
            @org.eclipse.jdt.annotation.NonNull Boolean isMissing, @org.eclipse.jdt.annotation.NonNull UUID adjacentTo,
            @org.eclipse.jdt.annotation.NonNull Boolean enableImages,
            @org.eclipse.jdt.annotation.NonNull Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.NonNull List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.NonNull Boolean enableUserData, Map<String, String> headers)
            throws ApiException {
        // verify the required parameter 'seriesId' is set
        if (seriesId == null) {
            throw new ApiException(400, "Missing the required parameter 'seriesId' when calling getSeasons");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Shows/{seriesId}/Seasons".replace("{seriesId}",
                ApiClient.urlEncode(seriesId.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "userId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("userId", userId));
        localVarQueryParameterBaseName = "fields";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "fields", fields));
        localVarQueryParameterBaseName = "isSpecialSeason";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("isSpecialSeason", isSpecialSeason));
        localVarQueryParameterBaseName = "isMissing";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("isMissing", isMissing));
        localVarQueryParameterBaseName = "adjacentTo";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("adjacentTo", adjacentTo));
        localVarQueryParameterBaseName = "enableImages";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("enableImages", enableImages));
        localVarQueryParameterBaseName = "imageTypeLimit";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("imageTypeLimit", imageTypeLimit));
        localVarQueryParameterBaseName = "enableImageTypes";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "enableImageTypes", enableImageTypes));
        localVarQueryParameterBaseName = "enableUserData";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("enableUserData", enableUserData));

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
     * Gets a list of upcoming episodes.
     * 
     * @param userId The user id of the user to get the upcoming episodes for. (optional)
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. (optional)
     * @param parentId Optional. Specify this to localize the search to a specific item or folder. Omit to use the root.
     *            (optional)
     * @param enableImages Optional. Include image information in output. (optional)
     * @param imageTypeLimit Optional. The max number of images to return, per image type. (optional)
     * @param enableImageTypes Optional. The image types to include in the output. (optional)
     * @param enableUserData Optional. Include user data. (optional)
     * @return BaseItemDtoQueryResult
     * @throws ApiException if fails to make API call
     */
    public BaseItemDtoQueryResult getUpcomingEpisodes(@org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull Integer startIndex, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull UUID parentId, @org.eclipse.jdt.annotation.NonNull Boolean enableImages,
            @org.eclipse.jdt.annotation.NonNull Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.NonNull List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.NonNull Boolean enableUserData) throws ApiException {
        return getUpcomingEpisodes(userId, startIndex, limit, fields, parentId, enableImages, imageTypeLimit,
                enableImageTypes, enableUserData, null);
    }

    /**
     * Gets a list of upcoming episodes.
     * 
     * @param userId The user id of the user to get the upcoming episodes for. (optional)
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. (optional)
     * @param parentId Optional. Specify this to localize the search to a specific item or folder. Omit to use the root.
     *            (optional)
     * @param enableImages Optional. Include image information in output. (optional)
     * @param imageTypeLimit Optional. The max number of images to return, per image type. (optional)
     * @param enableImageTypes Optional. The image types to include in the output. (optional)
     * @param enableUserData Optional. Include user data. (optional)
     * @param headers Optional headers to include in the request
     * @return BaseItemDtoQueryResult
     * @throws ApiException if fails to make API call
     */
    public BaseItemDtoQueryResult getUpcomingEpisodes(@org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull Integer startIndex, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull UUID parentId, @org.eclipse.jdt.annotation.NonNull Boolean enableImages,
            @org.eclipse.jdt.annotation.NonNull Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.NonNull List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.NonNull Boolean enableUserData, Map<String, String> headers)
            throws ApiException {
        ApiResponse<BaseItemDtoQueryResult> localVarResponse = getUpcomingEpisodesWithHttpInfo(userId, startIndex,
                limit, fields, parentId, enableImages, imageTypeLimit, enableImageTypes, enableUserData, headers);
        return localVarResponse.getData();
    }

    /**
     * Gets a list of upcoming episodes.
     * 
     * @param userId The user id of the user to get the upcoming episodes for. (optional)
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. (optional)
     * @param parentId Optional. Specify this to localize the search to a specific item or folder. Omit to use the root.
     *            (optional)
     * @param enableImages Optional. Include image information in output. (optional)
     * @param imageTypeLimit Optional. The max number of images to return, per image type. (optional)
     * @param enableImageTypes Optional. The image types to include in the output. (optional)
     * @param enableUserData Optional. Include user data. (optional)
     * @return ApiResponse&lt;BaseItemDtoQueryResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<BaseItemDtoQueryResult> getUpcomingEpisodesWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull UUID userId, @org.eclipse.jdt.annotation.NonNull Integer startIndex,
            @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull UUID parentId, @org.eclipse.jdt.annotation.NonNull Boolean enableImages,
            @org.eclipse.jdt.annotation.NonNull Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.NonNull List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.NonNull Boolean enableUserData) throws ApiException {
        return getUpcomingEpisodesWithHttpInfo(userId, startIndex, limit, fields, parentId, enableImages,
                imageTypeLimit, enableImageTypes, enableUserData, null);
    }

    /**
     * Gets a list of upcoming episodes.
     * 
     * @param userId The user id of the user to get the upcoming episodes for. (optional)
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. (optional)
     * @param parentId Optional. Specify this to localize the search to a specific item or folder. Omit to use the root.
     *            (optional)
     * @param enableImages Optional. Include image information in output. (optional)
     * @param imageTypeLimit Optional. The max number of images to return, per image type. (optional)
     * @param enableImageTypes Optional. The image types to include in the output. (optional)
     * @param enableUserData Optional. Include user data. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;BaseItemDtoQueryResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<BaseItemDtoQueryResult> getUpcomingEpisodesWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull UUID userId, @org.eclipse.jdt.annotation.NonNull Integer startIndex,
            @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull UUID parentId, @org.eclipse.jdt.annotation.NonNull Boolean enableImages,
            @org.eclipse.jdt.annotation.NonNull Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.NonNull List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.NonNull Boolean enableUserData, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getUpcomingEpisodesRequestBuilder(userId, startIndex, limit,
                fields, parentId, enableImages, imageTypeLimit, enableImageTypes, enableUserData, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getUpcomingEpisodes", localVarResponse);
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

    private HttpRequest.Builder getUpcomingEpisodesRequestBuilder(@org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull Integer startIndex, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull UUID parentId, @org.eclipse.jdt.annotation.NonNull Boolean enableImages,
            @org.eclipse.jdt.annotation.NonNull Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.NonNull List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.NonNull Boolean enableUserData, Map<String, String> headers)
            throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Shows/Upcoming";

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "userId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("userId", userId));
        localVarQueryParameterBaseName = "startIndex";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("startIndex", startIndex));
        localVarQueryParameterBaseName = "limit";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("limit", limit));
        localVarQueryParameterBaseName = "fields";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "fields", fields));
        localVarQueryParameterBaseName = "parentId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("parentId", parentId));
        localVarQueryParameterBaseName = "enableImages";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("enableImages", enableImages));
        localVarQueryParameterBaseName = "imageTypeLimit";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("imageTypeLimit", imageTypeLimit));
        localVarQueryParameterBaseName = "enableImageTypes";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "enableImageTypes", enableImageTypes));
        localVarQueryParameterBaseName = "enableUserData";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("enableUserData", enableUserData));

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
