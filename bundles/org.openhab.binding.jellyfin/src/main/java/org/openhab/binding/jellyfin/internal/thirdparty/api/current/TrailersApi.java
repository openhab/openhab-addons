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
import java.time.OffsetDateTime;
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
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.BaseItemDtoQueryResult;
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.BaseItemKind;
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.ImageType;
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.ItemFields;
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.ItemFilter;
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.ItemSortBy;
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.LocationType;
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.MediaType;
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.SeriesStatus;
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.SortOrder;
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.VideoType;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class TrailersApi {
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

    public TrailersApi() {
        this(Configuration.getDefaultApiClient());
    }

    public TrailersApi(ApiClient apiClient) {
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
     * Finds movies and trailers similar to a given trailer.
     * 
     * @param userId The user id supplied as query parameter; this is required when not using an API key. (optional)
     * @param maxOfficialRating Optional filter by maximum official rating (PG, PG-13, TV-MA, etc). (optional)
     * @param hasThemeSong Optional filter by items with theme songs. (optional)
     * @param hasThemeVideo Optional filter by items with theme videos. (optional)
     * @param hasSubtitles Optional filter by items with subtitles. (optional)
     * @param hasSpecialFeature Optional filter by items with special features. (optional)
     * @param hasTrailer Optional filter by items with trailers. (optional)
     * @param adjacentTo Optional. Return items that are siblings of a supplied item. (optional)
     * @param parentIndexNumber Optional filter by parent index number. (optional)
     * @param hasParentalRating Optional filter by items that have or do not have a parental rating. (optional)
     * @param isHd Optional filter by items that are HD or not. (optional)
     * @param is4K Optional filter by items that are 4K or not. (optional)
     * @param locationTypes Optional. If specified, results will be filtered based on LocationType. This allows
     *            multiple, comma delimited. (optional)
     * @param excludeLocationTypes Optional. If specified, results will be filtered based on the LocationType. This
     *            allows multiple, comma delimited. (optional)
     * @param isMissing Optional filter by items that are missing episodes or not. (optional)
     * @param isUnaired Optional filter by items that are unaired episodes or not. (optional)
     * @param minCommunityRating Optional filter by minimum community rating. (optional)
     * @param minCriticRating Optional filter by minimum critic rating. (optional)
     * @param minPremiereDate Optional. The minimum premiere date. Format &#x3D; ISO. (optional)
     * @param minDateLastSaved Optional. The minimum last saved date. Format &#x3D; ISO. (optional)
     * @param minDateLastSavedForUser Optional. The minimum last saved date for the current user. Format &#x3D; ISO.
     *            (optional)
     * @param maxPremiereDate Optional. The maximum premiere date. Format &#x3D; ISO. (optional)
     * @param hasOverview Optional filter by items that have an overview or not. (optional)
     * @param hasImdbId Optional filter by items that have an IMDb id or not. (optional)
     * @param hasTmdbId Optional filter by items that have a TMDb id or not. (optional)
     * @param hasTvdbId Optional filter by items that have a TVDb id or not. (optional)
     * @param isMovie Optional filter for live tv movies. (optional)
     * @param isSeries Optional filter for live tv series. (optional)
     * @param isNews Optional filter for live tv news. (optional)
     * @param isKids Optional filter for live tv kids. (optional)
     * @param isSports Optional filter for live tv sports. (optional)
     * @param excludeItemIds Optional. If specified, results will be filtered by excluding item ids. This allows
     *            multiple, comma delimited. (optional)
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param recursive When searching within folders, this determines whether or not the search will be recursive.
     *            true/false. (optional)
     * @param searchTerm Optional. Filter based on a search term. (optional)
     * @param sortOrder Sort Order - Ascending, Descending. (optional)
     * @param parentId Specify this to localize the search to a specific item or folder. Omit to use the root.
     *            (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple,
     *            comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions,
     *            MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue,
     *            SortName, Studios, Taglines. (optional)
     * @param excludeItemTypes Optional. If specified, results will be filtered based on item type. This allows
     *            multiple, comma delimited. (optional)
     * @param filters Optional. Specify additional filters to apply. This allows multiple, comma delimited. Options:
     *            IsFolder, IsNotFolder, IsUnplayed, IsPlayed, IsFavorite, IsResumable, Likes, Dislikes. (optional)
     * @param isFavorite Optional filter by items that are marked as favorite, or not. (optional)
     * @param mediaTypes Optional filter by MediaType. Allows multiple, comma delimited. (optional)
     * @param imageTypes Optional. If specified, results will be filtered based on those containing image types. This
     *            allows multiple, comma delimited. (optional)
     * @param sortBy Optional. Specify one or more sort orders, comma delimited. Options: Album, AlbumArtist, Artist,
     *            Budget, CommunityRating, CriticRating, DateCreated, DatePlayed, PlayCount, PremiereDate,
     *            ProductionYear, SortName, Random, Revenue, Runtime. (optional)
     * @param isPlayed Optional filter by items that are played, or not. (optional)
     * @param genres Optional. If specified, results will be filtered based on genre. This allows multiple, pipe
     *            delimited. (optional)
     * @param officialRatings Optional. If specified, results will be filtered based on OfficialRating. This allows
     *            multiple, pipe delimited. (optional)
     * @param tags Optional. If specified, results will be filtered based on tag. This allows multiple, pipe delimited.
     *            (optional)
     * @param years Optional. If specified, results will be filtered based on production year. This allows multiple,
     *            comma delimited. (optional)
     * @param enableUserData Optional, include user data. (optional)
     * @param imageTypeLimit Optional, the max number of images to return, per image type. (optional)
     * @param enableImageTypes Optional. The image types to include in the output. (optional)
     * @param person Optional. If specified, results will be filtered to include only those containing the specified
     *            person. (optional)
     * @param personIds Optional. If specified, results will be filtered to include only those containing the specified
     *            person id. (optional)
     * @param personTypes Optional. If specified, along with Person, results will be filtered to include only those
     *            containing the specified person and PersonType. Allows multiple, comma-delimited. (optional)
     * @param studios Optional. If specified, results will be filtered based on studio. This allows multiple, pipe
     *            delimited. (optional)
     * @param artists Optional. If specified, results will be filtered based on artists. This allows multiple, pipe
     *            delimited. (optional)
     * @param excludeArtistIds Optional. If specified, results will be filtered based on artist id. This allows
     *            multiple, pipe delimited. (optional)
     * @param artistIds Optional. If specified, results will be filtered to include only those containing the specified
     *            artist id. (optional)
     * @param albumArtistIds Optional. If specified, results will be filtered to include only those containing the
     *            specified album artist id. (optional)
     * @param contributingArtistIds Optional. If specified, results will be filtered to include only those containing
     *            the specified contributing artist id. (optional)
     * @param albums Optional. If specified, results will be filtered based on album. This allows multiple, pipe
     *            delimited. (optional)
     * @param albumIds Optional. If specified, results will be filtered based on album id. This allows multiple, pipe
     *            delimited. (optional)
     * @param ids Optional. If specific items are needed, specify a list of item id&#39;s to retrieve. This allows
     *            multiple, comma delimited. (optional)
     * @param videoTypes Optional filter by VideoType (videofile, dvd, bluray, iso). Allows multiple, comma delimited.
     *            (optional)
     * @param minOfficialRating Optional filter by minimum official rating (PG, PG-13, TV-MA, etc). (optional)
     * @param isLocked Optional filter by items that are locked. (optional)
     * @param isPlaceHolder Optional filter by items that are placeholders. (optional)
     * @param hasOfficialRating Optional filter by items that have official ratings. (optional)
     * @param collapseBoxSetItems Whether or not to hide items behind their boxsets. (optional)
     * @param minWidth Optional. Filter by the minimum width of the item. (optional)
     * @param minHeight Optional. Filter by the minimum height of the item. (optional)
     * @param maxWidth Optional. Filter by the maximum width of the item. (optional)
     * @param maxHeight Optional. Filter by the maximum height of the item. (optional)
     * @param is3D Optional filter by items that are 3D, or not. (optional)
     * @param seriesStatus Optional filter by Series Status. Allows multiple, comma delimited. (optional)
     * @param nameStartsWithOrGreater Optional filter by items whose name is sorted equally or greater than a given
     *            input string. (optional)
     * @param nameStartsWith Optional filter by items whose name is sorted equally than a given input string. (optional)
     * @param nameLessThan Optional filter by items whose name is equally or lesser than a given input string.
     *            (optional)
     * @param studioIds Optional. If specified, results will be filtered based on studio id. This allows multiple, pipe
     *            delimited. (optional)
     * @param genreIds Optional. If specified, results will be filtered based on genre id. This allows multiple, pipe
     *            delimited. (optional)
     * @param enableTotalRecordCount Optional. Enable the total record count. (optional, default to true)
     * @param enableImages Optional, include image information in output. (optional, default to true)
     * @return BaseItemDtoQueryResult
     * @throws ApiException if fails to make API call
     */
    public BaseItemDtoQueryResult getTrailers(@org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable String maxOfficialRating,
            @org.eclipse.jdt.annotation.Nullable Boolean hasThemeSong,
            @org.eclipse.jdt.annotation.Nullable Boolean hasThemeVideo,
            @org.eclipse.jdt.annotation.Nullable Boolean hasSubtitles,
            @org.eclipse.jdt.annotation.Nullable Boolean hasSpecialFeature,
            @org.eclipse.jdt.annotation.Nullable Boolean hasTrailer,
            @org.eclipse.jdt.annotation.Nullable UUID adjacentTo,
            @org.eclipse.jdt.annotation.Nullable Integer parentIndexNumber,
            @org.eclipse.jdt.annotation.Nullable Boolean hasParentalRating,
            @org.eclipse.jdt.annotation.Nullable Boolean isHd, @org.eclipse.jdt.annotation.Nullable Boolean is4K,
            @org.eclipse.jdt.annotation.Nullable List<LocationType> locationTypes,
            @org.eclipse.jdt.annotation.Nullable List<LocationType> excludeLocationTypes,
            @org.eclipse.jdt.annotation.Nullable Boolean isMissing,
            @org.eclipse.jdt.annotation.Nullable Boolean isUnaired,
            @org.eclipse.jdt.annotation.Nullable Double minCommunityRating,
            @org.eclipse.jdt.annotation.Nullable Double minCriticRating,
            @org.eclipse.jdt.annotation.Nullable OffsetDateTime minPremiereDate,
            @org.eclipse.jdt.annotation.Nullable OffsetDateTime minDateLastSaved,
            @org.eclipse.jdt.annotation.Nullable OffsetDateTime minDateLastSavedForUser,
            @org.eclipse.jdt.annotation.Nullable OffsetDateTime maxPremiereDate,
            @org.eclipse.jdt.annotation.Nullable Boolean hasOverview,
            @org.eclipse.jdt.annotation.Nullable Boolean hasImdbId,
            @org.eclipse.jdt.annotation.Nullable Boolean hasTmdbId,
            @org.eclipse.jdt.annotation.Nullable Boolean hasTvdbId,
            @org.eclipse.jdt.annotation.Nullable Boolean isMovie, @org.eclipse.jdt.annotation.Nullable Boolean isSeries,
            @org.eclipse.jdt.annotation.Nullable Boolean isNews, @org.eclipse.jdt.annotation.Nullable Boolean isKids,
            @org.eclipse.jdt.annotation.Nullable Boolean isSports,
            @org.eclipse.jdt.annotation.Nullable List<UUID> excludeItemIds,
            @org.eclipse.jdt.annotation.Nullable Integer startIndex, @org.eclipse.jdt.annotation.Nullable Integer limit,
            @org.eclipse.jdt.annotation.Nullable Boolean recursive,
            @org.eclipse.jdt.annotation.Nullable String searchTerm,
            @org.eclipse.jdt.annotation.Nullable List<SortOrder> sortOrder,
            @org.eclipse.jdt.annotation.Nullable UUID parentId,
            @org.eclipse.jdt.annotation.Nullable List<ItemFields> fields,
            @org.eclipse.jdt.annotation.Nullable List<BaseItemKind> excludeItemTypes,
            @org.eclipse.jdt.annotation.Nullable List<ItemFilter> filters,
            @org.eclipse.jdt.annotation.Nullable Boolean isFavorite,
            @org.eclipse.jdt.annotation.Nullable List<MediaType> mediaTypes,
            @org.eclipse.jdt.annotation.Nullable List<ImageType> imageTypes,
            @org.eclipse.jdt.annotation.Nullable List<ItemSortBy> sortBy,
            @org.eclipse.jdt.annotation.Nullable Boolean isPlayed,
            @org.eclipse.jdt.annotation.Nullable List<String> genres,
            @org.eclipse.jdt.annotation.Nullable List<String> officialRatings,
            @org.eclipse.jdt.annotation.Nullable List<String> tags,
            @org.eclipse.jdt.annotation.Nullable List<Integer> years,
            @org.eclipse.jdt.annotation.Nullable Boolean enableUserData,
            @org.eclipse.jdt.annotation.Nullable Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.Nullable List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.Nullable String person,
            @org.eclipse.jdt.annotation.Nullable List<UUID> personIds,
            @org.eclipse.jdt.annotation.Nullable List<String> personTypes,
            @org.eclipse.jdt.annotation.Nullable List<String> studios,
            @org.eclipse.jdt.annotation.Nullable List<String> artists,
            @org.eclipse.jdt.annotation.Nullable List<UUID> excludeArtistIds,
            @org.eclipse.jdt.annotation.Nullable List<UUID> artistIds,
            @org.eclipse.jdt.annotation.Nullable List<UUID> albumArtistIds,
            @org.eclipse.jdt.annotation.Nullable List<UUID> contributingArtistIds,
            @org.eclipse.jdt.annotation.Nullable List<String> albums,
            @org.eclipse.jdt.annotation.Nullable List<UUID> albumIds,
            @org.eclipse.jdt.annotation.Nullable List<UUID> ids,
            @org.eclipse.jdt.annotation.Nullable List<VideoType> videoTypes,
            @org.eclipse.jdt.annotation.Nullable String minOfficialRating,
            @org.eclipse.jdt.annotation.Nullable Boolean isLocked,
            @org.eclipse.jdt.annotation.Nullable Boolean isPlaceHolder,
            @org.eclipse.jdt.annotation.Nullable Boolean hasOfficialRating,
            @org.eclipse.jdt.annotation.Nullable Boolean collapseBoxSetItems,
            @org.eclipse.jdt.annotation.Nullable Integer minWidth,
            @org.eclipse.jdt.annotation.Nullable Integer minHeight,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight, @org.eclipse.jdt.annotation.Nullable Boolean is3D,
            @org.eclipse.jdt.annotation.Nullable List<SeriesStatus> seriesStatus,
            @org.eclipse.jdt.annotation.Nullable String nameStartsWithOrGreater,
            @org.eclipse.jdt.annotation.Nullable String nameStartsWith,
            @org.eclipse.jdt.annotation.Nullable String nameLessThan,
            @org.eclipse.jdt.annotation.Nullable List<UUID> studioIds,
            @org.eclipse.jdt.annotation.Nullable List<UUID> genreIds,
            @org.eclipse.jdt.annotation.Nullable Boolean enableTotalRecordCount,
            @org.eclipse.jdt.annotation.Nullable Boolean enableImages) throws ApiException {
        return getTrailers(userId, maxOfficialRating, hasThemeSong, hasThemeVideo, hasSubtitles, hasSpecialFeature,
                hasTrailer, adjacentTo, parentIndexNumber, hasParentalRating, isHd, is4K, locationTypes,
                excludeLocationTypes, isMissing, isUnaired, minCommunityRating, minCriticRating, minPremiereDate,
                minDateLastSaved, minDateLastSavedForUser, maxPremiereDate, hasOverview, hasImdbId, hasTmdbId,
                hasTvdbId, isMovie, isSeries, isNews, isKids, isSports, excludeItemIds, startIndex, limit, recursive,
                searchTerm, sortOrder, parentId, fields, excludeItemTypes, filters, isFavorite, mediaTypes, imageTypes,
                sortBy, isPlayed, genres, officialRatings, tags, years, enableUserData, imageTypeLimit,
                enableImageTypes, person, personIds, personTypes, studios, artists, excludeArtistIds, artistIds,
                albumArtistIds, contributingArtistIds, albums, albumIds, ids, videoTypes, minOfficialRating, isLocked,
                isPlaceHolder, hasOfficialRating, collapseBoxSetItems, minWidth, minHeight, maxWidth, maxHeight, is3D,
                seriesStatus, nameStartsWithOrGreater, nameStartsWith, nameLessThan, studioIds, genreIds,
                enableTotalRecordCount, enableImages, null);
    }

    /**
     * Finds movies and trailers similar to a given trailer.
     * 
     * @param userId The user id supplied as query parameter; this is required when not using an API key. (optional)
     * @param maxOfficialRating Optional filter by maximum official rating (PG, PG-13, TV-MA, etc). (optional)
     * @param hasThemeSong Optional filter by items with theme songs. (optional)
     * @param hasThemeVideo Optional filter by items with theme videos. (optional)
     * @param hasSubtitles Optional filter by items with subtitles. (optional)
     * @param hasSpecialFeature Optional filter by items with special features. (optional)
     * @param hasTrailer Optional filter by items with trailers. (optional)
     * @param adjacentTo Optional. Return items that are siblings of a supplied item. (optional)
     * @param parentIndexNumber Optional filter by parent index number. (optional)
     * @param hasParentalRating Optional filter by items that have or do not have a parental rating. (optional)
     * @param isHd Optional filter by items that are HD or not. (optional)
     * @param is4K Optional filter by items that are 4K or not. (optional)
     * @param locationTypes Optional. If specified, results will be filtered based on LocationType. This allows
     *            multiple, comma delimited. (optional)
     * @param excludeLocationTypes Optional. If specified, results will be filtered based on the LocationType. This
     *            allows multiple, comma delimited. (optional)
     * @param isMissing Optional filter by items that are missing episodes or not. (optional)
     * @param isUnaired Optional filter by items that are unaired episodes or not. (optional)
     * @param minCommunityRating Optional filter by minimum community rating. (optional)
     * @param minCriticRating Optional filter by minimum critic rating. (optional)
     * @param minPremiereDate Optional. The minimum premiere date. Format &#x3D; ISO. (optional)
     * @param minDateLastSaved Optional. The minimum last saved date. Format &#x3D; ISO. (optional)
     * @param minDateLastSavedForUser Optional. The minimum last saved date for the current user. Format &#x3D; ISO.
     *            (optional)
     * @param maxPremiereDate Optional. The maximum premiere date. Format &#x3D; ISO. (optional)
     * @param hasOverview Optional filter by items that have an overview or not. (optional)
     * @param hasImdbId Optional filter by items that have an IMDb id or not. (optional)
     * @param hasTmdbId Optional filter by items that have a TMDb id or not. (optional)
     * @param hasTvdbId Optional filter by items that have a TVDb id or not. (optional)
     * @param isMovie Optional filter for live tv movies. (optional)
     * @param isSeries Optional filter for live tv series. (optional)
     * @param isNews Optional filter for live tv news. (optional)
     * @param isKids Optional filter for live tv kids. (optional)
     * @param isSports Optional filter for live tv sports. (optional)
     * @param excludeItemIds Optional. If specified, results will be filtered by excluding item ids. This allows
     *            multiple, comma delimited. (optional)
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param recursive When searching within folders, this determines whether or not the search will be recursive.
     *            true/false. (optional)
     * @param searchTerm Optional. Filter based on a search term. (optional)
     * @param sortOrder Sort Order - Ascending, Descending. (optional)
     * @param parentId Specify this to localize the search to a specific item or folder. Omit to use the root.
     *            (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple,
     *            comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions,
     *            MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue,
     *            SortName, Studios, Taglines. (optional)
     * @param excludeItemTypes Optional. If specified, results will be filtered based on item type. This allows
     *            multiple, comma delimited. (optional)
     * @param filters Optional. Specify additional filters to apply. This allows multiple, comma delimited. Options:
     *            IsFolder, IsNotFolder, IsUnplayed, IsPlayed, IsFavorite, IsResumable, Likes, Dislikes. (optional)
     * @param isFavorite Optional filter by items that are marked as favorite, or not. (optional)
     * @param mediaTypes Optional filter by MediaType. Allows multiple, comma delimited. (optional)
     * @param imageTypes Optional. If specified, results will be filtered based on those containing image types. This
     *            allows multiple, comma delimited. (optional)
     * @param sortBy Optional. Specify one or more sort orders, comma delimited. Options: Album, AlbumArtist, Artist,
     *            Budget, CommunityRating, CriticRating, DateCreated, DatePlayed, PlayCount, PremiereDate,
     *            ProductionYear, SortName, Random, Revenue, Runtime. (optional)
     * @param isPlayed Optional filter by items that are played, or not. (optional)
     * @param genres Optional. If specified, results will be filtered based on genre. This allows multiple, pipe
     *            delimited. (optional)
     * @param officialRatings Optional. If specified, results will be filtered based on OfficialRating. This allows
     *            multiple, pipe delimited. (optional)
     * @param tags Optional. If specified, results will be filtered based on tag. This allows multiple, pipe delimited.
     *            (optional)
     * @param years Optional. If specified, results will be filtered based on production year. This allows multiple,
     *            comma delimited. (optional)
     * @param enableUserData Optional, include user data. (optional)
     * @param imageTypeLimit Optional, the max number of images to return, per image type. (optional)
     * @param enableImageTypes Optional. The image types to include in the output. (optional)
     * @param person Optional. If specified, results will be filtered to include only those containing the specified
     *            person. (optional)
     * @param personIds Optional. If specified, results will be filtered to include only those containing the specified
     *            person id. (optional)
     * @param personTypes Optional. If specified, along with Person, results will be filtered to include only those
     *            containing the specified person and PersonType. Allows multiple, comma-delimited. (optional)
     * @param studios Optional. If specified, results will be filtered based on studio. This allows multiple, pipe
     *            delimited. (optional)
     * @param artists Optional. If specified, results will be filtered based on artists. This allows multiple, pipe
     *            delimited. (optional)
     * @param excludeArtistIds Optional. If specified, results will be filtered based on artist id. This allows
     *            multiple, pipe delimited. (optional)
     * @param artistIds Optional. If specified, results will be filtered to include only those containing the specified
     *            artist id. (optional)
     * @param albumArtistIds Optional. If specified, results will be filtered to include only those containing the
     *            specified album artist id. (optional)
     * @param contributingArtistIds Optional. If specified, results will be filtered to include only those containing
     *            the specified contributing artist id. (optional)
     * @param albums Optional. If specified, results will be filtered based on album. This allows multiple, pipe
     *            delimited. (optional)
     * @param albumIds Optional. If specified, results will be filtered based on album id. This allows multiple, pipe
     *            delimited. (optional)
     * @param ids Optional. If specific items are needed, specify a list of item id&#39;s to retrieve. This allows
     *            multiple, comma delimited. (optional)
     * @param videoTypes Optional filter by VideoType (videofile, dvd, bluray, iso). Allows multiple, comma delimited.
     *            (optional)
     * @param minOfficialRating Optional filter by minimum official rating (PG, PG-13, TV-MA, etc). (optional)
     * @param isLocked Optional filter by items that are locked. (optional)
     * @param isPlaceHolder Optional filter by items that are placeholders. (optional)
     * @param hasOfficialRating Optional filter by items that have official ratings. (optional)
     * @param collapseBoxSetItems Whether or not to hide items behind their boxsets. (optional)
     * @param minWidth Optional. Filter by the minimum width of the item. (optional)
     * @param minHeight Optional. Filter by the minimum height of the item. (optional)
     * @param maxWidth Optional. Filter by the maximum width of the item. (optional)
     * @param maxHeight Optional. Filter by the maximum height of the item. (optional)
     * @param is3D Optional filter by items that are 3D, or not. (optional)
     * @param seriesStatus Optional filter by Series Status. Allows multiple, comma delimited. (optional)
     * @param nameStartsWithOrGreater Optional filter by items whose name is sorted equally or greater than a given
     *            input string. (optional)
     * @param nameStartsWith Optional filter by items whose name is sorted equally than a given input string. (optional)
     * @param nameLessThan Optional filter by items whose name is equally or lesser than a given input string.
     *            (optional)
     * @param studioIds Optional. If specified, results will be filtered based on studio id. This allows multiple, pipe
     *            delimited. (optional)
     * @param genreIds Optional. If specified, results will be filtered based on genre id. This allows multiple, pipe
     *            delimited. (optional)
     * @param enableTotalRecordCount Optional. Enable the total record count. (optional, default to true)
     * @param enableImages Optional, include image information in output. (optional, default to true)
     * @param headers Optional headers to include in the request
     * @return BaseItemDtoQueryResult
     * @throws ApiException if fails to make API call
     */
    public BaseItemDtoQueryResult getTrailers(@org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable String maxOfficialRating,
            @org.eclipse.jdt.annotation.Nullable Boolean hasThemeSong,
            @org.eclipse.jdt.annotation.Nullable Boolean hasThemeVideo,
            @org.eclipse.jdt.annotation.Nullable Boolean hasSubtitles,
            @org.eclipse.jdt.annotation.Nullable Boolean hasSpecialFeature,
            @org.eclipse.jdt.annotation.Nullable Boolean hasTrailer,
            @org.eclipse.jdt.annotation.Nullable UUID adjacentTo,
            @org.eclipse.jdt.annotation.Nullable Integer parentIndexNumber,
            @org.eclipse.jdt.annotation.Nullable Boolean hasParentalRating,
            @org.eclipse.jdt.annotation.Nullable Boolean isHd, @org.eclipse.jdt.annotation.Nullable Boolean is4K,
            @org.eclipse.jdt.annotation.Nullable List<LocationType> locationTypes,
            @org.eclipse.jdt.annotation.Nullable List<LocationType> excludeLocationTypes,
            @org.eclipse.jdt.annotation.Nullable Boolean isMissing,
            @org.eclipse.jdt.annotation.Nullable Boolean isUnaired,
            @org.eclipse.jdt.annotation.Nullable Double minCommunityRating,
            @org.eclipse.jdt.annotation.Nullable Double minCriticRating,
            @org.eclipse.jdt.annotation.Nullable OffsetDateTime minPremiereDate,
            @org.eclipse.jdt.annotation.Nullable OffsetDateTime minDateLastSaved,
            @org.eclipse.jdt.annotation.Nullable OffsetDateTime minDateLastSavedForUser,
            @org.eclipse.jdt.annotation.Nullable OffsetDateTime maxPremiereDate,
            @org.eclipse.jdt.annotation.Nullable Boolean hasOverview,
            @org.eclipse.jdt.annotation.Nullable Boolean hasImdbId,
            @org.eclipse.jdt.annotation.Nullable Boolean hasTmdbId,
            @org.eclipse.jdt.annotation.Nullable Boolean hasTvdbId,
            @org.eclipse.jdt.annotation.Nullable Boolean isMovie, @org.eclipse.jdt.annotation.Nullable Boolean isSeries,
            @org.eclipse.jdt.annotation.Nullable Boolean isNews, @org.eclipse.jdt.annotation.Nullable Boolean isKids,
            @org.eclipse.jdt.annotation.Nullable Boolean isSports,
            @org.eclipse.jdt.annotation.Nullable List<UUID> excludeItemIds,
            @org.eclipse.jdt.annotation.Nullable Integer startIndex, @org.eclipse.jdt.annotation.Nullable Integer limit,
            @org.eclipse.jdt.annotation.Nullable Boolean recursive,
            @org.eclipse.jdt.annotation.Nullable String searchTerm,
            @org.eclipse.jdt.annotation.Nullable List<SortOrder> sortOrder,
            @org.eclipse.jdt.annotation.Nullable UUID parentId,
            @org.eclipse.jdt.annotation.Nullable List<ItemFields> fields,
            @org.eclipse.jdt.annotation.Nullable List<BaseItemKind> excludeItemTypes,
            @org.eclipse.jdt.annotation.Nullable List<ItemFilter> filters,
            @org.eclipse.jdt.annotation.Nullable Boolean isFavorite,
            @org.eclipse.jdt.annotation.Nullable List<MediaType> mediaTypes,
            @org.eclipse.jdt.annotation.Nullable List<ImageType> imageTypes,
            @org.eclipse.jdt.annotation.Nullable List<ItemSortBy> sortBy,
            @org.eclipse.jdt.annotation.Nullable Boolean isPlayed,
            @org.eclipse.jdt.annotation.Nullable List<String> genres,
            @org.eclipse.jdt.annotation.Nullable List<String> officialRatings,
            @org.eclipse.jdt.annotation.Nullable List<String> tags,
            @org.eclipse.jdt.annotation.Nullable List<Integer> years,
            @org.eclipse.jdt.annotation.Nullable Boolean enableUserData,
            @org.eclipse.jdt.annotation.Nullable Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.Nullable List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.Nullable String person,
            @org.eclipse.jdt.annotation.Nullable List<UUID> personIds,
            @org.eclipse.jdt.annotation.Nullable List<String> personTypes,
            @org.eclipse.jdt.annotation.Nullable List<String> studios,
            @org.eclipse.jdt.annotation.Nullable List<String> artists,
            @org.eclipse.jdt.annotation.Nullable List<UUID> excludeArtistIds,
            @org.eclipse.jdt.annotation.Nullable List<UUID> artistIds,
            @org.eclipse.jdt.annotation.Nullable List<UUID> albumArtistIds,
            @org.eclipse.jdt.annotation.Nullable List<UUID> contributingArtistIds,
            @org.eclipse.jdt.annotation.Nullable List<String> albums,
            @org.eclipse.jdt.annotation.Nullable List<UUID> albumIds,
            @org.eclipse.jdt.annotation.Nullable List<UUID> ids,
            @org.eclipse.jdt.annotation.Nullable List<VideoType> videoTypes,
            @org.eclipse.jdt.annotation.Nullable String minOfficialRating,
            @org.eclipse.jdt.annotation.Nullable Boolean isLocked,
            @org.eclipse.jdt.annotation.Nullable Boolean isPlaceHolder,
            @org.eclipse.jdt.annotation.Nullable Boolean hasOfficialRating,
            @org.eclipse.jdt.annotation.Nullable Boolean collapseBoxSetItems,
            @org.eclipse.jdt.annotation.Nullable Integer minWidth,
            @org.eclipse.jdt.annotation.Nullable Integer minHeight,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight, @org.eclipse.jdt.annotation.Nullable Boolean is3D,
            @org.eclipse.jdt.annotation.Nullable List<SeriesStatus> seriesStatus,
            @org.eclipse.jdt.annotation.Nullable String nameStartsWithOrGreater,
            @org.eclipse.jdt.annotation.Nullable String nameStartsWith,
            @org.eclipse.jdt.annotation.Nullable String nameLessThan,
            @org.eclipse.jdt.annotation.Nullable List<UUID> studioIds,
            @org.eclipse.jdt.annotation.Nullable List<UUID> genreIds,
            @org.eclipse.jdt.annotation.Nullable Boolean enableTotalRecordCount,
            @org.eclipse.jdt.annotation.Nullable Boolean enableImages, Map<String, String> headers)
            throws ApiException {
        ApiResponse<BaseItemDtoQueryResult> localVarResponse = getTrailersWithHttpInfo(userId, maxOfficialRating,
                hasThemeSong, hasThemeVideo, hasSubtitles, hasSpecialFeature, hasTrailer, adjacentTo, parentIndexNumber,
                hasParentalRating, isHd, is4K, locationTypes, excludeLocationTypes, isMissing, isUnaired,
                minCommunityRating, minCriticRating, minPremiereDate, minDateLastSaved, minDateLastSavedForUser,
                maxPremiereDate, hasOverview, hasImdbId, hasTmdbId, hasTvdbId, isMovie, isSeries, isNews, isKids,
                isSports, excludeItemIds, startIndex, limit, recursive, searchTerm, sortOrder, parentId, fields,
                excludeItemTypes, filters, isFavorite, mediaTypes, imageTypes, sortBy, isPlayed, genres,
                officialRatings, tags, years, enableUserData, imageTypeLimit, enableImageTypes, person, personIds,
                personTypes, studios, artists, excludeArtistIds, artistIds, albumArtistIds, contributingArtistIds,
                albums, albumIds, ids, videoTypes, minOfficialRating, isLocked, isPlaceHolder, hasOfficialRating,
                collapseBoxSetItems, minWidth, minHeight, maxWidth, maxHeight, is3D, seriesStatus,
                nameStartsWithOrGreater, nameStartsWith, nameLessThan, studioIds, genreIds, enableTotalRecordCount,
                enableImages, headers);
        return localVarResponse.getData();
    }

    /**
     * Finds movies and trailers similar to a given trailer.
     * 
     * @param userId The user id supplied as query parameter; this is required when not using an API key. (optional)
     * @param maxOfficialRating Optional filter by maximum official rating (PG, PG-13, TV-MA, etc). (optional)
     * @param hasThemeSong Optional filter by items with theme songs. (optional)
     * @param hasThemeVideo Optional filter by items with theme videos. (optional)
     * @param hasSubtitles Optional filter by items with subtitles. (optional)
     * @param hasSpecialFeature Optional filter by items with special features. (optional)
     * @param hasTrailer Optional filter by items with trailers. (optional)
     * @param adjacentTo Optional. Return items that are siblings of a supplied item. (optional)
     * @param parentIndexNumber Optional filter by parent index number. (optional)
     * @param hasParentalRating Optional filter by items that have or do not have a parental rating. (optional)
     * @param isHd Optional filter by items that are HD or not. (optional)
     * @param is4K Optional filter by items that are 4K or not. (optional)
     * @param locationTypes Optional. If specified, results will be filtered based on LocationType. This allows
     *            multiple, comma delimited. (optional)
     * @param excludeLocationTypes Optional. If specified, results will be filtered based on the LocationType. This
     *            allows multiple, comma delimited. (optional)
     * @param isMissing Optional filter by items that are missing episodes or not. (optional)
     * @param isUnaired Optional filter by items that are unaired episodes or not. (optional)
     * @param minCommunityRating Optional filter by minimum community rating. (optional)
     * @param minCriticRating Optional filter by minimum critic rating. (optional)
     * @param minPremiereDate Optional. The minimum premiere date. Format &#x3D; ISO. (optional)
     * @param minDateLastSaved Optional. The minimum last saved date. Format &#x3D; ISO. (optional)
     * @param minDateLastSavedForUser Optional. The minimum last saved date for the current user. Format &#x3D; ISO.
     *            (optional)
     * @param maxPremiereDate Optional. The maximum premiere date. Format &#x3D; ISO. (optional)
     * @param hasOverview Optional filter by items that have an overview or not. (optional)
     * @param hasImdbId Optional filter by items that have an IMDb id or not. (optional)
     * @param hasTmdbId Optional filter by items that have a TMDb id or not. (optional)
     * @param hasTvdbId Optional filter by items that have a TVDb id or not. (optional)
     * @param isMovie Optional filter for live tv movies. (optional)
     * @param isSeries Optional filter for live tv series. (optional)
     * @param isNews Optional filter for live tv news. (optional)
     * @param isKids Optional filter for live tv kids. (optional)
     * @param isSports Optional filter for live tv sports. (optional)
     * @param excludeItemIds Optional. If specified, results will be filtered by excluding item ids. This allows
     *            multiple, comma delimited. (optional)
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param recursive When searching within folders, this determines whether or not the search will be recursive.
     *            true/false. (optional)
     * @param searchTerm Optional. Filter based on a search term. (optional)
     * @param sortOrder Sort Order - Ascending, Descending. (optional)
     * @param parentId Specify this to localize the search to a specific item or folder. Omit to use the root.
     *            (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple,
     *            comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions,
     *            MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue,
     *            SortName, Studios, Taglines. (optional)
     * @param excludeItemTypes Optional. If specified, results will be filtered based on item type. This allows
     *            multiple, comma delimited. (optional)
     * @param filters Optional. Specify additional filters to apply. This allows multiple, comma delimited. Options:
     *            IsFolder, IsNotFolder, IsUnplayed, IsPlayed, IsFavorite, IsResumable, Likes, Dislikes. (optional)
     * @param isFavorite Optional filter by items that are marked as favorite, or not. (optional)
     * @param mediaTypes Optional filter by MediaType. Allows multiple, comma delimited. (optional)
     * @param imageTypes Optional. If specified, results will be filtered based on those containing image types. This
     *            allows multiple, comma delimited. (optional)
     * @param sortBy Optional. Specify one or more sort orders, comma delimited. Options: Album, AlbumArtist, Artist,
     *            Budget, CommunityRating, CriticRating, DateCreated, DatePlayed, PlayCount, PremiereDate,
     *            ProductionYear, SortName, Random, Revenue, Runtime. (optional)
     * @param isPlayed Optional filter by items that are played, or not. (optional)
     * @param genres Optional. If specified, results will be filtered based on genre. This allows multiple, pipe
     *            delimited. (optional)
     * @param officialRatings Optional. If specified, results will be filtered based on OfficialRating. This allows
     *            multiple, pipe delimited. (optional)
     * @param tags Optional. If specified, results will be filtered based on tag. This allows multiple, pipe delimited.
     *            (optional)
     * @param years Optional. If specified, results will be filtered based on production year. This allows multiple,
     *            comma delimited. (optional)
     * @param enableUserData Optional, include user data. (optional)
     * @param imageTypeLimit Optional, the max number of images to return, per image type. (optional)
     * @param enableImageTypes Optional. The image types to include in the output. (optional)
     * @param person Optional. If specified, results will be filtered to include only those containing the specified
     *            person. (optional)
     * @param personIds Optional. If specified, results will be filtered to include only those containing the specified
     *            person id. (optional)
     * @param personTypes Optional. If specified, along with Person, results will be filtered to include only those
     *            containing the specified person and PersonType. Allows multiple, comma-delimited. (optional)
     * @param studios Optional. If specified, results will be filtered based on studio. This allows multiple, pipe
     *            delimited. (optional)
     * @param artists Optional. If specified, results will be filtered based on artists. This allows multiple, pipe
     *            delimited. (optional)
     * @param excludeArtistIds Optional. If specified, results will be filtered based on artist id. This allows
     *            multiple, pipe delimited. (optional)
     * @param artistIds Optional. If specified, results will be filtered to include only those containing the specified
     *            artist id. (optional)
     * @param albumArtistIds Optional. If specified, results will be filtered to include only those containing the
     *            specified album artist id. (optional)
     * @param contributingArtistIds Optional. If specified, results will be filtered to include only those containing
     *            the specified contributing artist id. (optional)
     * @param albums Optional. If specified, results will be filtered based on album. This allows multiple, pipe
     *            delimited. (optional)
     * @param albumIds Optional. If specified, results will be filtered based on album id. This allows multiple, pipe
     *            delimited. (optional)
     * @param ids Optional. If specific items are needed, specify a list of item id&#39;s to retrieve. This allows
     *            multiple, comma delimited. (optional)
     * @param videoTypes Optional filter by VideoType (videofile, dvd, bluray, iso). Allows multiple, comma delimited.
     *            (optional)
     * @param minOfficialRating Optional filter by minimum official rating (PG, PG-13, TV-MA, etc). (optional)
     * @param isLocked Optional filter by items that are locked. (optional)
     * @param isPlaceHolder Optional filter by items that are placeholders. (optional)
     * @param hasOfficialRating Optional filter by items that have official ratings. (optional)
     * @param collapseBoxSetItems Whether or not to hide items behind their boxsets. (optional)
     * @param minWidth Optional. Filter by the minimum width of the item. (optional)
     * @param minHeight Optional. Filter by the minimum height of the item. (optional)
     * @param maxWidth Optional. Filter by the maximum width of the item. (optional)
     * @param maxHeight Optional. Filter by the maximum height of the item. (optional)
     * @param is3D Optional filter by items that are 3D, or not. (optional)
     * @param seriesStatus Optional filter by Series Status. Allows multiple, comma delimited. (optional)
     * @param nameStartsWithOrGreater Optional filter by items whose name is sorted equally or greater than a given
     *            input string. (optional)
     * @param nameStartsWith Optional filter by items whose name is sorted equally than a given input string. (optional)
     * @param nameLessThan Optional filter by items whose name is equally or lesser than a given input string.
     *            (optional)
     * @param studioIds Optional. If specified, results will be filtered based on studio id. This allows multiple, pipe
     *            delimited. (optional)
     * @param genreIds Optional. If specified, results will be filtered based on genre id. This allows multiple, pipe
     *            delimited. (optional)
     * @param enableTotalRecordCount Optional. Enable the total record count. (optional, default to true)
     * @param enableImages Optional, include image information in output. (optional, default to true)
     * @return ApiResponse&lt;BaseItemDtoQueryResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<BaseItemDtoQueryResult> getTrailersWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable String maxOfficialRating,
            @org.eclipse.jdt.annotation.Nullable Boolean hasThemeSong,
            @org.eclipse.jdt.annotation.Nullable Boolean hasThemeVideo,
            @org.eclipse.jdt.annotation.Nullable Boolean hasSubtitles,
            @org.eclipse.jdt.annotation.Nullable Boolean hasSpecialFeature,
            @org.eclipse.jdt.annotation.Nullable Boolean hasTrailer,
            @org.eclipse.jdt.annotation.Nullable UUID adjacentTo,
            @org.eclipse.jdt.annotation.Nullable Integer parentIndexNumber,
            @org.eclipse.jdt.annotation.Nullable Boolean hasParentalRating,
            @org.eclipse.jdt.annotation.Nullable Boolean isHd, @org.eclipse.jdt.annotation.Nullable Boolean is4K,
            @org.eclipse.jdt.annotation.Nullable List<LocationType> locationTypes,
            @org.eclipse.jdt.annotation.Nullable List<LocationType> excludeLocationTypes,
            @org.eclipse.jdt.annotation.Nullable Boolean isMissing,
            @org.eclipse.jdt.annotation.Nullable Boolean isUnaired,
            @org.eclipse.jdt.annotation.Nullable Double minCommunityRating,
            @org.eclipse.jdt.annotation.Nullable Double minCriticRating,
            @org.eclipse.jdt.annotation.Nullable OffsetDateTime minPremiereDate,
            @org.eclipse.jdt.annotation.Nullable OffsetDateTime minDateLastSaved,
            @org.eclipse.jdt.annotation.Nullable OffsetDateTime minDateLastSavedForUser,
            @org.eclipse.jdt.annotation.Nullable OffsetDateTime maxPremiereDate,
            @org.eclipse.jdt.annotation.Nullable Boolean hasOverview,
            @org.eclipse.jdt.annotation.Nullable Boolean hasImdbId,
            @org.eclipse.jdt.annotation.Nullable Boolean hasTmdbId,
            @org.eclipse.jdt.annotation.Nullable Boolean hasTvdbId,
            @org.eclipse.jdt.annotation.Nullable Boolean isMovie, @org.eclipse.jdt.annotation.Nullable Boolean isSeries,
            @org.eclipse.jdt.annotation.Nullable Boolean isNews, @org.eclipse.jdt.annotation.Nullable Boolean isKids,
            @org.eclipse.jdt.annotation.Nullable Boolean isSports,
            @org.eclipse.jdt.annotation.Nullable List<UUID> excludeItemIds,
            @org.eclipse.jdt.annotation.Nullable Integer startIndex, @org.eclipse.jdt.annotation.Nullable Integer limit,
            @org.eclipse.jdt.annotation.Nullable Boolean recursive,
            @org.eclipse.jdt.annotation.Nullable String searchTerm,
            @org.eclipse.jdt.annotation.Nullable List<SortOrder> sortOrder,
            @org.eclipse.jdt.annotation.Nullable UUID parentId,
            @org.eclipse.jdt.annotation.Nullable List<ItemFields> fields,
            @org.eclipse.jdt.annotation.Nullable List<BaseItemKind> excludeItemTypes,
            @org.eclipse.jdt.annotation.Nullable List<ItemFilter> filters,
            @org.eclipse.jdt.annotation.Nullable Boolean isFavorite,
            @org.eclipse.jdt.annotation.Nullable List<MediaType> mediaTypes,
            @org.eclipse.jdt.annotation.Nullable List<ImageType> imageTypes,
            @org.eclipse.jdt.annotation.Nullable List<ItemSortBy> sortBy,
            @org.eclipse.jdt.annotation.Nullable Boolean isPlayed,
            @org.eclipse.jdt.annotation.Nullable List<String> genres,
            @org.eclipse.jdt.annotation.Nullable List<String> officialRatings,
            @org.eclipse.jdt.annotation.Nullable List<String> tags,
            @org.eclipse.jdt.annotation.Nullable List<Integer> years,
            @org.eclipse.jdt.annotation.Nullable Boolean enableUserData,
            @org.eclipse.jdt.annotation.Nullable Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.Nullable List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.Nullable String person,
            @org.eclipse.jdt.annotation.Nullable List<UUID> personIds,
            @org.eclipse.jdt.annotation.Nullable List<String> personTypes,
            @org.eclipse.jdt.annotation.Nullable List<String> studios,
            @org.eclipse.jdt.annotation.Nullable List<String> artists,
            @org.eclipse.jdt.annotation.Nullable List<UUID> excludeArtistIds,
            @org.eclipse.jdt.annotation.Nullable List<UUID> artistIds,
            @org.eclipse.jdt.annotation.Nullable List<UUID> albumArtistIds,
            @org.eclipse.jdt.annotation.Nullable List<UUID> contributingArtistIds,
            @org.eclipse.jdt.annotation.Nullable List<String> albums,
            @org.eclipse.jdt.annotation.Nullable List<UUID> albumIds,
            @org.eclipse.jdt.annotation.Nullable List<UUID> ids,
            @org.eclipse.jdt.annotation.Nullable List<VideoType> videoTypes,
            @org.eclipse.jdt.annotation.Nullable String minOfficialRating,
            @org.eclipse.jdt.annotation.Nullable Boolean isLocked,
            @org.eclipse.jdt.annotation.Nullable Boolean isPlaceHolder,
            @org.eclipse.jdt.annotation.Nullable Boolean hasOfficialRating,
            @org.eclipse.jdt.annotation.Nullable Boolean collapseBoxSetItems,
            @org.eclipse.jdt.annotation.Nullable Integer minWidth,
            @org.eclipse.jdt.annotation.Nullable Integer minHeight,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight, @org.eclipse.jdt.annotation.Nullable Boolean is3D,
            @org.eclipse.jdt.annotation.Nullable List<SeriesStatus> seriesStatus,
            @org.eclipse.jdt.annotation.Nullable String nameStartsWithOrGreater,
            @org.eclipse.jdt.annotation.Nullable String nameStartsWith,
            @org.eclipse.jdt.annotation.Nullable String nameLessThan,
            @org.eclipse.jdt.annotation.Nullable List<UUID> studioIds,
            @org.eclipse.jdt.annotation.Nullable List<UUID> genreIds,
            @org.eclipse.jdt.annotation.Nullable Boolean enableTotalRecordCount,
            @org.eclipse.jdt.annotation.Nullable Boolean enableImages) throws ApiException {
        return getTrailersWithHttpInfo(userId, maxOfficialRating, hasThemeSong, hasThemeVideo, hasSubtitles,
                hasSpecialFeature, hasTrailer, adjacentTo, parentIndexNumber, hasParentalRating, isHd, is4K,
                locationTypes, excludeLocationTypes, isMissing, isUnaired, minCommunityRating, minCriticRating,
                minPremiereDate, minDateLastSaved, minDateLastSavedForUser, maxPremiereDate, hasOverview, hasImdbId,
                hasTmdbId, hasTvdbId, isMovie, isSeries, isNews, isKids, isSports, excludeItemIds, startIndex, limit,
                recursive, searchTerm, sortOrder, parentId, fields, excludeItemTypes, filters, isFavorite, mediaTypes,
                imageTypes, sortBy, isPlayed, genres, officialRatings, tags, years, enableUserData, imageTypeLimit,
                enableImageTypes, person, personIds, personTypes, studios, artists, excludeArtistIds, artistIds,
                albumArtistIds, contributingArtistIds, albums, albumIds, ids, videoTypes, minOfficialRating, isLocked,
                isPlaceHolder, hasOfficialRating, collapseBoxSetItems, minWidth, minHeight, maxWidth, maxHeight, is3D,
                seriesStatus, nameStartsWithOrGreater, nameStartsWith, nameLessThan, studioIds, genreIds,
                enableTotalRecordCount, enableImages, null);
    }

    /**
     * Finds movies and trailers similar to a given trailer.
     * 
     * @param userId The user id supplied as query parameter; this is required when not using an API key. (optional)
     * @param maxOfficialRating Optional filter by maximum official rating (PG, PG-13, TV-MA, etc). (optional)
     * @param hasThemeSong Optional filter by items with theme songs. (optional)
     * @param hasThemeVideo Optional filter by items with theme videos. (optional)
     * @param hasSubtitles Optional filter by items with subtitles. (optional)
     * @param hasSpecialFeature Optional filter by items with special features. (optional)
     * @param hasTrailer Optional filter by items with trailers. (optional)
     * @param adjacentTo Optional. Return items that are siblings of a supplied item. (optional)
     * @param parentIndexNumber Optional filter by parent index number. (optional)
     * @param hasParentalRating Optional filter by items that have or do not have a parental rating. (optional)
     * @param isHd Optional filter by items that are HD or not. (optional)
     * @param is4K Optional filter by items that are 4K or not. (optional)
     * @param locationTypes Optional. If specified, results will be filtered based on LocationType. This allows
     *            multiple, comma delimited. (optional)
     * @param excludeLocationTypes Optional. If specified, results will be filtered based on the LocationType. This
     *            allows multiple, comma delimited. (optional)
     * @param isMissing Optional filter by items that are missing episodes or not. (optional)
     * @param isUnaired Optional filter by items that are unaired episodes or not. (optional)
     * @param minCommunityRating Optional filter by minimum community rating. (optional)
     * @param minCriticRating Optional filter by minimum critic rating. (optional)
     * @param minPremiereDate Optional. The minimum premiere date. Format &#x3D; ISO. (optional)
     * @param minDateLastSaved Optional. The minimum last saved date. Format &#x3D; ISO. (optional)
     * @param minDateLastSavedForUser Optional. The minimum last saved date for the current user. Format &#x3D; ISO.
     *            (optional)
     * @param maxPremiereDate Optional. The maximum premiere date. Format &#x3D; ISO. (optional)
     * @param hasOverview Optional filter by items that have an overview or not. (optional)
     * @param hasImdbId Optional filter by items that have an IMDb id or not. (optional)
     * @param hasTmdbId Optional filter by items that have a TMDb id or not. (optional)
     * @param hasTvdbId Optional filter by items that have a TVDb id or not. (optional)
     * @param isMovie Optional filter for live tv movies. (optional)
     * @param isSeries Optional filter for live tv series. (optional)
     * @param isNews Optional filter for live tv news. (optional)
     * @param isKids Optional filter for live tv kids. (optional)
     * @param isSports Optional filter for live tv sports. (optional)
     * @param excludeItemIds Optional. If specified, results will be filtered by excluding item ids. This allows
     *            multiple, comma delimited. (optional)
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param recursive When searching within folders, this determines whether or not the search will be recursive.
     *            true/false. (optional)
     * @param searchTerm Optional. Filter based on a search term. (optional)
     * @param sortOrder Sort Order - Ascending, Descending. (optional)
     * @param parentId Specify this to localize the search to a specific item or folder. Omit to use the root.
     *            (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple,
     *            comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions,
     *            MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue,
     *            SortName, Studios, Taglines. (optional)
     * @param excludeItemTypes Optional. If specified, results will be filtered based on item type. This allows
     *            multiple, comma delimited. (optional)
     * @param filters Optional. Specify additional filters to apply. This allows multiple, comma delimited. Options:
     *            IsFolder, IsNotFolder, IsUnplayed, IsPlayed, IsFavorite, IsResumable, Likes, Dislikes. (optional)
     * @param isFavorite Optional filter by items that are marked as favorite, or not. (optional)
     * @param mediaTypes Optional filter by MediaType. Allows multiple, comma delimited. (optional)
     * @param imageTypes Optional. If specified, results will be filtered based on those containing image types. This
     *            allows multiple, comma delimited. (optional)
     * @param sortBy Optional. Specify one or more sort orders, comma delimited. Options: Album, AlbumArtist, Artist,
     *            Budget, CommunityRating, CriticRating, DateCreated, DatePlayed, PlayCount, PremiereDate,
     *            ProductionYear, SortName, Random, Revenue, Runtime. (optional)
     * @param isPlayed Optional filter by items that are played, or not. (optional)
     * @param genres Optional. If specified, results will be filtered based on genre. This allows multiple, pipe
     *            delimited. (optional)
     * @param officialRatings Optional. If specified, results will be filtered based on OfficialRating. This allows
     *            multiple, pipe delimited. (optional)
     * @param tags Optional. If specified, results will be filtered based on tag. This allows multiple, pipe delimited.
     *            (optional)
     * @param years Optional. If specified, results will be filtered based on production year. This allows multiple,
     *            comma delimited. (optional)
     * @param enableUserData Optional, include user data. (optional)
     * @param imageTypeLimit Optional, the max number of images to return, per image type. (optional)
     * @param enableImageTypes Optional. The image types to include in the output. (optional)
     * @param person Optional. If specified, results will be filtered to include only those containing the specified
     *            person. (optional)
     * @param personIds Optional. If specified, results will be filtered to include only those containing the specified
     *            person id. (optional)
     * @param personTypes Optional. If specified, along with Person, results will be filtered to include only those
     *            containing the specified person and PersonType. Allows multiple, comma-delimited. (optional)
     * @param studios Optional. If specified, results will be filtered based on studio. This allows multiple, pipe
     *            delimited. (optional)
     * @param artists Optional. If specified, results will be filtered based on artists. This allows multiple, pipe
     *            delimited. (optional)
     * @param excludeArtistIds Optional. If specified, results will be filtered based on artist id. This allows
     *            multiple, pipe delimited. (optional)
     * @param artistIds Optional. If specified, results will be filtered to include only those containing the specified
     *            artist id. (optional)
     * @param albumArtistIds Optional. If specified, results will be filtered to include only those containing the
     *            specified album artist id. (optional)
     * @param contributingArtistIds Optional. If specified, results will be filtered to include only those containing
     *            the specified contributing artist id. (optional)
     * @param albums Optional. If specified, results will be filtered based on album. This allows multiple, pipe
     *            delimited. (optional)
     * @param albumIds Optional. If specified, results will be filtered based on album id. This allows multiple, pipe
     *            delimited. (optional)
     * @param ids Optional. If specific items are needed, specify a list of item id&#39;s to retrieve. This allows
     *            multiple, comma delimited. (optional)
     * @param videoTypes Optional filter by VideoType (videofile, dvd, bluray, iso). Allows multiple, comma delimited.
     *            (optional)
     * @param minOfficialRating Optional filter by minimum official rating (PG, PG-13, TV-MA, etc). (optional)
     * @param isLocked Optional filter by items that are locked. (optional)
     * @param isPlaceHolder Optional filter by items that are placeholders. (optional)
     * @param hasOfficialRating Optional filter by items that have official ratings. (optional)
     * @param collapseBoxSetItems Whether or not to hide items behind their boxsets. (optional)
     * @param minWidth Optional. Filter by the minimum width of the item. (optional)
     * @param minHeight Optional. Filter by the minimum height of the item. (optional)
     * @param maxWidth Optional. Filter by the maximum width of the item. (optional)
     * @param maxHeight Optional. Filter by the maximum height of the item. (optional)
     * @param is3D Optional filter by items that are 3D, or not. (optional)
     * @param seriesStatus Optional filter by Series Status. Allows multiple, comma delimited. (optional)
     * @param nameStartsWithOrGreater Optional filter by items whose name is sorted equally or greater than a given
     *            input string. (optional)
     * @param nameStartsWith Optional filter by items whose name is sorted equally than a given input string. (optional)
     * @param nameLessThan Optional filter by items whose name is equally or lesser than a given input string.
     *            (optional)
     * @param studioIds Optional. If specified, results will be filtered based on studio id. This allows multiple, pipe
     *            delimited. (optional)
     * @param genreIds Optional. If specified, results will be filtered based on genre id. This allows multiple, pipe
     *            delimited. (optional)
     * @param enableTotalRecordCount Optional. Enable the total record count. (optional, default to true)
     * @param enableImages Optional, include image information in output. (optional, default to true)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;BaseItemDtoQueryResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<BaseItemDtoQueryResult> getTrailersWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable String maxOfficialRating,
            @org.eclipse.jdt.annotation.Nullable Boolean hasThemeSong,
            @org.eclipse.jdt.annotation.Nullable Boolean hasThemeVideo,
            @org.eclipse.jdt.annotation.Nullable Boolean hasSubtitles,
            @org.eclipse.jdt.annotation.Nullable Boolean hasSpecialFeature,
            @org.eclipse.jdt.annotation.Nullable Boolean hasTrailer,
            @org.eclipse.jdt.annotation.Nullable UUID adjacentTo,
            @org.eclipse.jdt.annotation.Nullable Integer parentIndexNumber,
            @org.eclipse.jdt.annotation.Nullable Boolean hasParentalRating,
            @org.eclipse.jdt.annotation.Nullable Boolean isHd, @org.eclipse.jdt.annotation.Nullable Boolean is4K,
            @org.eclipse.jdt.annotation.Nullable List<LocationType> locationTypes,
            @org.eclipse.jdt.annotation.Nullable List<LocationType> excludeLocationTypes,
            @org.eclipse.jdt.annotation.Nullable Boolean isMissing,
            @org.eclipse.jdt.annotation.Nullable Boolean isUnaired,
            @org.eclipse.jdt.annotation.Nullable Double minCommunityRating,
            @org.eclipse.jdt.annotation.Nullable Double minCriticRating,
            @org.eclipse.jdt.annotation.Nullable OffsetDateTime minPremiereDate,
            @org.eclipse.jdt.annotation.Nullable OffsetDateTime minDateLastSaved,
            @org.eclipse.jdt.annotation.Nullable OffsetDateTime minDateLastSavedForUser,
            @org.eclipse.jdt.annotation.Nullable OffsetDateTime maxPremiereDate,
            @org.eclipse.jdt.annotation.Nullable Boolean hasOverview,
            @org.eclipse.jdt.annotation.Nullable Boolean hasImdbId,
            @org.eclipse.jdt.annotation.Nullable Boolean hasTmdbId,
            @org.eclipse.jdt.annotation.Nullable Boolean hasTvdbId,
            @org.eclipse.jdt.annotation.Nullable Boolean isMovie, @org.eclipse.jdt.annotation.Nullable Boolean isSeries,
            @org.eclipse.jdt.annotation.Nullable Boolean isNews, @org.eclipse.jdt.annotation.Nullable Boolean isKids,
            @org.eclipse.jdt.annotation.Nullable Boolean isSports,
            @org.eclipse.jdt.annotation.Nullable List<UUID> excludeItemIds,
            @org.eclipse.jdt.annotation.Nullable Integer startIndex, @org.eclipse.jdt.annotation.Nullable Integer limit,
            @org.eclipse.jdt.annotation.Nullable Boolean recursive,
            @org.eclipse.jdt.annotation.Nullable String searchTerm,
            @org.eclipse.jdt.annotation.Nullable List<SortOrder> sortOrder,
            @org.eclipse.jdt.annotation.Nullable UUID parentId,
            @org.eclipse.jdt.annotation.Nullable List<ItemFields> fields,
            @org.eclipse.jdt.annotation.Nullable List<BaseItemKind> excludeItemTypes,
            @org.eclipse.jdt.annotation.Nullable List<ItemFilter> filters,
            @org.eclipse.jdt.annotation.Nullable Boolean isFavorite,
            @org.eclipse.jdt.annotation.Nullable List<MediaType> mediaTypes,
            @org.eclipse.jdt.annotation.Nullable List<ImageType> imageTypes,
            @org.eclipse.jdt.annotation.Nullable List<ItemSortBy> sortBy,
            @org.eclipse.jdt.annotation.Nullable Boolean isPlayed,
            @org.eclipse.jdt.annotation.Nullable List<String> genres,
            @org.eclipse.jdt.annotation.Nullable List<String> officialRatings,
            @org.eclipse.jdt.annotation.Nullable List<String> tags,
            @org.eclipse.jdt.annotation.Nullable List<Integer> years,
            @org.eclipse.jdt.annotation.Nullable Boolean enableUserData,
            @org.eclipse.jdt.annotation.Nullable Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.Nullable List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.Nullable String person,
            @org.eclipse.jdt.annotation.Nullable List<UUID> personIds,
            @org.eclipse.jdt.annotation.Nullable List<String> personTypes,
            @org.eclipse.jdt.annotation.Nullable List<String> studios,
            @org.eclipse.jdt.annotation.Nullable List<String> artists,
            @org.eclipse.jdt.annotation.Nullable List<UUID> excludeArtistIds,
            @org.eclipse.jdt.annotation.Nullable List<UUID> artistIds,
            @org.eclipse.jdt.annotation.Nullable List<UUID> albumArtistIds,
            @org.eclipse.jdt.annotation.Nullable List<UUID> contributingArtistIds,
            @org.eclipse.jdt.annotation.Nullable List<String> albums,
            @org.eclipse.jdt.annotation.Nullable List<UUID> albumIds,
            @org.eclipse.jdt.annotation.Nullable List<UUID> ids,
            @org.eclipse.jdt.annotation.Nullable List<VideoType> videoTypes,
            @org.eclipse.jdt.annotation.Nullable String minOfficialRating,
            @org.eclipse.jdt.annotation.Nullable Boolean isLocked,
            @org.eclipse.jdt.annotation.Nullable Boolean isPlaceHolder,
            @org.eclipse.jdt.annotation.Nullable Boolean hasOfficialRating,
            @org.eclipse.jdt.annotation.Nullable Boolean collapseBoxSetItems,
            @org.eclipse.jdt.annotation.Nullable Integer minWidth,
            @org.eclipse.jdt.annotation.Nullable Integer minHeight,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight, @org.eclipse.jdt.annotation.Nullable Boolean is3D,
            @org.eclipse.jdt.annotation.Nullable List<SeriesStatus> seriesStatus,
            @org.eclipse.jdt.annotation.Nullable String nameStartsWithOrGreater,
            @org.eclipse.jdt.annotation.Nullable String nameStartsWith,
            @org.eclipse.jdt.annotation.Nullable String nameLessThan,
            @org.eclipse.jdt.annotation.Nullable List<UUID> studioIds,
            @org.eclipse.jdt.annotation.Nullable List<UUID> genreIds,
            @org.eclipse.jdt.annotation.Nullable Boolean enableTotalRecordCount,
            @org.eclipse.jdt.annotation.Nullable Boolean enableImages, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getTrailersRequestBuilder(userId, maxOfficialRating, hasThemeSong,
                hasThemeVideo, hasSubtitles, hasSpecialFeature, hasTrailer, adjacentTo, parentIndexNumber,
                hasParentalRating, isHd, is4K, locationTypes, excludeLocationTypes, isMissing, isUnaired,
                minCommunityRating, minCriticRating, minPremiereDate, minDateLastSaved, minDateLastSavedForUser,
                maxPremiereDate, hasOverview, hasImdbId, hasTmdbId, hasTvdbId, isMovie, isSeries, isNews, isKids,
                isSports, excludeItemIds, startIndex, limit, recursive, searchTerm, sortOrder, parentId, fields,
                excludeItemTypes, filters, isFavorite, mediaTypes, imageTypes, sortBy, isPlayed, genres,
                officialRatings, tags, years, enableUserData, imageTypeLimit, enableImageTypes, person, personIds,
                personTypes, studios, artists, excludeArtistIds, artistIds, albumArtistIds, contributingArtistIds,
                albums, albumIds, ids, videoTypes, minOfficialRating, isLocked, isPlaceHolder, hasOfficialRating,
                collapseBoxSetItems, minWidth, minHeight, maxWidth, maxHeight, is3D, seriesStatus,
                nameStartsWithOrGreater, nameStartsWith, nameLessThan, studioIds, genreIds, enableTotalRecordCount,
                enableImages, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getTrailers", localVarResponse);
                }
                localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
                if (localVarResponseBody == null) {
                    return new ApiResponse<BaseItemDtoQueryResult>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponseBody.readAllBytes());
                BaseItemDtoQueryResult responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<BaseItemDtoQueryResult>() {
                        });

                return new ApiResponse<BaseItemDtoQueryResult>(localVarResponse.statusCode(),
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

    private HttpRequest.Builder getTrailersRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable String maxOfficialRating,
            @org.eclipse.jdt.annotation.Nullable Boolean hasThemeSong,
            @org.eclipse.jdt.annotation.Nullable Boolean hasThemeVideo,
            @org.eclipse.jdt.annotation.Nullable Boolean hasSubtitles,
            @org.eclipse.jdt.annotation.Nullable Boolean hasSpecialFeature,
            @org.eclipse.jdt.annotation.Nullable Boolean hasTrailer,
            @org.eclipse.jdt.annotation.Nullable UUID adjacentTo,
            @org.eclipse.jdt.annotation.Nullable Integer parentIndexNumber,
            @org.eclipse.jdt.annotation.Nullable Boolean hasParentalRating,
            @org.eclipse.jdt.annotation.Nullable Boolean isHd, @org.eclipse.jdt.annotation.Nullable Boolean is4K,
            @org.eclipse.jdt.annotation.Nullable List<LocationType> locationTypes,
            @org.eclipse.jdt.annotation.Nullable List<LocationType> excludeLocationTypes,
            @org.eclipse.jdt.annotation.Nullable Boolean isMissing,
            @org.eclipse.jdt.annotation.Nullable Boolean isUnaired,
            @org.eclipse.jdt.annotation.Nullable Double minCommunityRating,
            @org.eclipse.jdt.annotation.Nullable Double minCriticRating,
            @org.eclipse.jdt.annotation.Nullable OffsetDateTime minPremiereDate,
            @org.eclipse.jdt.annotation.Nullable OffsetDateTime minDateLastSaved,
            @org.eclipse.jdt.annotation.Nullable OffsetDateTime minDateLastSavedForUser,
            @org.eclipse.jdt.annotation.Nullable OffsetDateTime maxPremiereDate,
            @org.eclipse.jdt.annotation.Nullable Boolean hasOverview,
            @org.eclipse.jdt.annotation.Nullable Boolean hasImdbId,
            @org.eclipse.jdt.annotation.Nullable Boolean hasTmdbId,
            @org.eclipse.jdt.annotation.Nullable Boolean hasTvdbId,
            @org.eclipse.jdt.annotation.Nullable Boolean isMovie, @org.eclipse.jdt.annotation.Nullable Boolean isSeries,
            @org.eclipse.jdt.annotation.Nullable Boolean isNews, @org.eclipse.jdt.annotation.Nullable Boolean isKids,
            @org.eclipse.jdt.annotation.Nullable Boolean isSports,
            @org.eclipse.jdt.annotation.Nullable List<UUID> excludeItemIds,
            @org.eclipse.jdt.annotation.Nullable Integer startIndex, @org.eclipse.jdt.annotation.Nullable Integer limit,
            @org.eclipse.jdt.annotation.Nullable Boolean recursive,
            @org.eclipse.jdt.annotation.Nullable String searchTerm,
            @org.eclipse.jdt.annotation.Nullable List<SortOrder> sortOrder,
            @org.eclipse.jdt.annotation.Nullable UUID parentId,
            @org.eclipse.jdt.annotation.Nullable List<ItemFields> fields,
            @org.eclipse.jdt.annotation.Nullable List<BaseItemKind> excludeItemTypes,
            @org.eclipse.jdt.annotation.Nullable List<ItemFilter> filters,
            @org.eclipse.jdt.annotation.Nullable Boolean isFavorite,
            @org.eclipse.jdt.annotation.Nullable List<MediaType> mediaTypes,
            @org.eclipse.jdt.annotation.Nullable List<ImageType> imageTypes,
            @org.eclipse.jdt.annotation.Nullable List<ItemSortBy> sortBy,
            @org.eclipse.jdt.annotation.Nullable Boolean isPlayed,
            @org.eclipse.jdt.annotation.Nullable List<String> genres,
            @org.eclipse.jdt.annotation.Nullable List<String> officialRatings,
            @org.eclipse.jdt.annotation.Nullable List<String> tags,
            @org.eclipse.jdt.annotation.Nullable List<Integer> years,
            @org.eclipse.jdt.annotation.Nullable Boolean enableUserData,
            @org.eclipse.jdt.annotation.Nullable Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.Nullable List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.Nullable String person,
            @org.eclipse.jdt.annotation.Nullable List<UUID> personIds,
            @org.eclipse.jdt.annotation.Nullable List<String> personTypes,
            @org.eclipse.jdt.annotation.Nullable List<String> studios,
            @org.eclipse.jdt.annotation.Nullable List<String> artists,
            @org.eclipse.jdt.annotation.Nullable List<UUID> excludeArtistIds,
            @org.eclipse.jdt.annotation.Nullable List<UUID> artistIds,
            @org.eclipse.jdt.annotation.Nullable List<UUID> albumArtistIds,
            @org.eclipse.jdt.annotation.Nullable List<UUID> contributingArtistIds,
            @org.eclipse.jdt.annotation.Nullable List<String> albums,
            @org.eclipse.jdt.annotation.Nullable List<UUID> albumIds,
            @org.eclipse.jdt.annotation.Nullable List<UUID> ids,
            @org.eclipse.jdt.annotation.Nullable List<VideoType> videoTypes,
            @org.eclipse.jdt.annotation.Nullable String minOfficialRating,
            @org.eclipse.jdt.annotation.Nullable Boolean isLocked,
            @org.eclipse.jdt.annotation.Nullable Boolean isPlaceHolder,
            @org.eclipse.jdt.annotation.Nullable Boolean hasOfficialRating,
            @org.eclipse.jdt.annotation.Nullable Boolean collapseBoxSetItems,
            @org.eclipse.jdt.annotation.Nullable Integer minWidth,
            @org.eclipse.jdt.annotation.Nullable Integer minHeight,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight, @org.eclipse.jdt.annotation.Nullable Boolean is3D,
            @org.eclipse.jdt.annotation.Nullable List<SeriesStatus> seriesStatus,
            @org.eclipse.jdt.annotation.Nullable String nameStartsWithOrGreater,
            @org.eclipse.jdt.annotation.Nullable String nameStartsWith,
            @org.eclipse.jdt.annotation.Nullable String nameLessThan,
            @org.eclipse.jdt.annotation.Nullable List<UUID> studioIds,
            @org.eclipse.jdt.annotation.Nullable List<UUID> genreIds,
            @org.eclipse.jdt.annotation.Nullable Boolean enableTotalRecordCount,
            @org.eclipse.jdt.annotation.Nullable Boolean enableImages, Map<String, String> headers)
            throws ApiException {

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Trailers";

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "userId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("userId", userId));
        localVarQueryParameterBaseName = "maxOfficialRating";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxOfficialRating", maxOfficialRating));
        localVarQueryParameterBaseName = "hasThemeSong";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("hasThemeSong", hasThemeSong));
        localVarQueryParameterBaseName = "hasThemeVideo";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("hasThemeVideo", hasThemeVideo));
        localVarQueryParameterBaseName = "hasSubtitles";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("hasSubtitles", hasSubtitles));
        localVarQueryParameterBaseName = "hasSpecialFeature";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("hasSpecialFeature", hasSpecialFeature));
        localVarQueryParameterBaseName = "hasTrailer";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("hasTrailer", hasTrailer));
        localVarQueryParameterBaseName = "adjacentTo";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("adjacentTo", adjacentTo));
        localVarQueryParameterBaseName = "parentIndexNumber";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("parentIndexNumber", parentIndexNumber));
        localVarQueryParameterBaseName = "hasParentalRating";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("hasParentalRating", hasParentalRating));
        localVarQueryParameterBaseName = "isHd";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("isHd", isHd));
        localVarQueryParameterBaseName = "is4K";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("is4K", is4K));
        localVarQueryParameterBaseName = "locationTypes";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "locationTypes", locationTypes));
        localVarQueryParameterBaseName = "excludeLocationTypes";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "excludeLocationTypes", excludeLocationTypes));
        localVarQueryParameterBaseName = "isMissing";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("isMissing", isMissing));
        localVarQueryParameterBaseName = "isUnaired";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("isUnaired", isUnaired));
        localVarQueryParameterBaseName = "minCommunityRating";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("minCommunityRating", minCommunityRating));
        localVarQueryParameterBaseName = "minCriticRating";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("minCriticRating", minCriticRating));
        localVarQueryParameterBaseName = "minPremiereDate";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("minPremiereDate", minPremiereDate));
        localVarQueryParameterBaseName = "minDateLastSaved";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("minDateLastSaved", minDateLastSaved));
        localVarQueryParameterBaseName = "minDateLastSavedForUser";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("minDateLastSavedForUser", minDateLastSavedForUser));
        localVarQueryParameterBaseName = "maxPremiereDate";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxPremiereDate", maxPremiereDate));
        localVarQueryParameterBaseName = "hasOverview";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("hasOverview", hasOverview));
        localVarQueryParameterBaseName = "hasImdbId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("hasImdbId", hasImdbId));
        localVarQueryParameterBaseName = "hasTmdbId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("hasTmdbId", hasTmdbId));
        localVarQueryParameterBaseName = "hasTvdbId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("hasTvdbId", hasTvdbId));
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
        localVarQueryParameterBaseName = "excludeItemIds";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "excludeItemIds", excludeItemIds));
        localVarQueryParameterBaseName = "startIndex";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("startIndex", startIndex));
        localVarQueryParameterBaseName = "limit";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("limit", limit));
        localVarQueryParameterBaseName = "recursive";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("recursive", recursive));
        localVarQueryParameterBaseName = "searchTerm";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("searchTerm", searchTerm));
        localVarQueryParameterBaseName = "sortOrder";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "sortOrder", sortOrder));
        localVarQueryParameterBaseName = "parentId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("parentId", parentId));
        localVarQueryParameterBaseName = "fields";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "fields", fields));
        localVarQueryParameterBaseName = "excludeItemTypes";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "excludeItemTypes", excludeItemTypes));
        localVarQueryParameterBaseName = "filters";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "filters", filters));
        localVarQueryParameterBaseName = "isFavorite";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("isFavorite", isFavorite));
        localVarQueryParameterBaseName = "mediaTypes";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "mediaTypes", mediaTypes));
        localVarQueryParameterBaseName = "imageTypes";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "imageTypes", imageTypes));
        localVarQueryParameterBaseName = "sortBy";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "sortBy", sortBy));
        localVarQueryParameterBaseName = "isPlayed";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("isPlayed", isPlayed));
        localVarQueryParameterBaseName = "genres";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "genres", genres));
        localVarQueryParameterBaseName = "officialRatings";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "officialRatings", officialRatings));
        localVarQueryParameterBaseName = "tags";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "tags", tags));
        localVarQueryParameterBaseName = "years";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "years", years));
        localVarQueryParameterBaseName = "enableUserData";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("enableUserData", enableUserData));
        localVarQueryParameterBaseName = "imageTypeLimit";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("imageTypeLimit", imageTypeLimit));
        localVarQueryParameterBaseName = "enableImageTypes";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "enableImageTypes", enableImageTypes));
        localVarQueryParameterBaseName = "person";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("person", person));
        localVarQueryParameterBaseName = "personIds";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "personIds", personIds));
        localVarQueryParameterBaseName = "personTypes";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "personTypes", personTypes));
        localVarQueryParameterBaseName = "studios";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "studios", studios));
        localVarQueryParameterBaseName = "artists";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "artists", artists));
        localVarQueryParameterBaseName = "excludeArtistIds";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "excludeArtistIds", excludeArtistIds));
        localVarQueryParameterBaseName = "artistIds";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "artistIds", artistIds));
        localVarQueryParameterBaseName = "albumArtistIds";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "albumArtistIds", albumArtistIds));
        localVarQueryParameterBaseName = "contributingArtistIds";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "contributingArtistIds", contributingArtistIds));
        localVarQueryParameterBaseName = "albums";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "albums", albums));
        localVarQueryParameterBaseName = "albumIds";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "albumIds", albumIds));
        localVarQueryParameterBaseName = "ids";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "ids", ids));
        localVarQueryParameterBaseName = "videoTypes";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "videoTypes", videoTypes));
        localVarQueryParameterBaseName = "minOfficialRating";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("minOfficialRating", minOfficialRating));
        localVarQueryParameterBaseName = "isLocked";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("isLocked", isLocked));
        localVarQueryParameterBaseName = "isPlaceHolder";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("isPlaceHolder", isPlaceHolder));
        localVarQueryParameterBaseName = "hasOfficialRating";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("hasOfficialRating", hasOfficialRating));
        localVarQueryParameterBaseName = "collapseBoxSetItems";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("collapseBoxSetItems", collapseBoxSetItems));
        localVarQueryParameterBaseName = "minWidth";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("minWidth", minWidth));
        localVarQueryParameterBaseName = "minHeight";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("minHeight", minHeight));
        localVarQueryParameterBaseName = "maxWidth";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxWidth", maxWidth));
        localVarQueryParameterBaseName = "maxHeight";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxHeight", maxHeight));
        localVarQueryParameterBaseName = "is3D";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("is3D", is3D));
        localVarQueryParameterBaseName = "seriesStatus";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "seriesStatus", seriesStatus));
        localVarQueryParameterBaseName = "nameStartsWithOrGreater";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("nameStartsWithOrGreater", nameStartsWithOrGreater));
        localVarQueryParameterBaseName = "nameStartsWith";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("nameStartsWith", nameStartsWith));
        localVarQueryParameterBaseName = "nameLessThan";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("nameLessThan", nameLessThan));
        localVarQueryParameterBaseName = "studioIds";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "studioIds", studioIds));
        localVarQueryParameterBaseName = "genreIds";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "genreIds", genreIds));
        localVarQueryParameterBaseName = "enableTotalRecordCount";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("enableTotalRecordCount", enableTotalRecordCount));
        localVarQueryParameterBaseName = "enableImages";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("enableImages", enableImages));

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
