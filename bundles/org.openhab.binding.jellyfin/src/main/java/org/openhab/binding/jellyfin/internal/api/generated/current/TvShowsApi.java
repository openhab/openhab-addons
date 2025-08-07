package org.openhab.binding.jellyfin.internal.api.generated.current;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.core.GenericType;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;
import org.openhab.binding.jellyfin.internal.api.generated.ApiException;
import org.openhab.binding.jellyfin.internal.api.generated.ApiResponse;
import org.openhab.binding.jellyfin.internal.api.generated.Configuration;
import org.openhab.binding.jellyfin.internal.api.generated.Pair;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.BaseItemDtoQueryResult;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.ImageType;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.ItemFields;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.ItemSortBy;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class TvShowsApi {
    private ApiClient apiClient;

    public TvShowsApi() {
        this(Configuration.getDefaultApiClient());
    }

    public TvShowsApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Get the API client
     *
     * @return API client
     */
    public ApiClient getApiClient() {
        return apiClient;
    }

    /**
     * Set the API client
     *
     * @param apiClient an instance of API client
     */
    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
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
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Success</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Not Found</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
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
        return getEpisodesWithHttpInfo(seriesId, userId, fields, season, seasonId, isMissing, adjacentTo, startItemId,
                startIndex, limit, enableImages, imageTypeLimit, enableImageTypes, enableUserData, sortBy).getData();
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
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Success</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Not Found</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
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
        // Check required parameters
        if (seriesId == null) {
            throw new ApiException(400, "Missing the required parameter 'seriesId' when calling getEpisodes");
        }

        // Path parameters
        String localVarPath = "/Shows/{seriesId}/Episodes".replaceAll("\\{seriesId}",
                apiClient.escapeString(seriesId.toString()));

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "userId", userId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "fields", fields));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "season", season));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "seasonId", seasonId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "isMissing", isMissing));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "adjacentTo", adjacentTo));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "startItemId", startItemId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "startIndex", startIndex));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "enableImages", enableImages));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "imageTypeLimit", imageTypeLimit));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "enableImageTypes", enableImageTypes));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "enableUserData", enableUserData));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "sortBy", sortBy));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<BaseItemDtoQueryResult> localVarReturnType = new GenericType<BaseItemDtoQueryResult>() {
        };
        return apiClient.invokeAPI("TvShowsApi.getEpisodes", localVarPath, "GET", localVarQueryParams, null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, localVarReturnType, false);
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
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Success</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
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
        return getNextUpWithHttpInfo(userId, startIndex, limit, fields, seriesId, parentId, enableImages,
                imageTypeLimit, enableImageTypes, enableUserData, nextUpDateCutoff, enableTotalRecordCount,
                disableFirstEpisode, enableResumable, enableRewatching).getData();
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
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Success</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
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
        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "userId", userId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "startIndex", startIndex));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "fields", fields));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "seriesId", seriesId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "parentId", parentId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "enableImages", enableImages));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "imageTypeLimit", imageTypeLimit));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "enableImageTypes", enableImageTypes));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "enableUserData", enableUserData));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "nextUpDateCutoff", nextUpDateCutoff));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "enableTotalRecordCount", enableTotalRecordCount));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "disableFirstEpisode", disableFirstEpisode));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "enableResumable", enableResumable));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "enableRewatching", enableRewatching));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<BaseItemDtoQueryResult> localVarReturnType = new GenericType<BaseItemDtoQueryResult>() {
        };
        return apiClient.invokeAPI("TvShowsApi.getNextUp", "/Shows/NextUp", "GET", localVarQueryParams, null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, localVarReturnType, false);
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
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Success</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Not Found</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
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
        return getSeasonsWithHttpInfo(seriesId, userId, fields, isSpecialSeason, isMissing, adjacentTo, enableImages,
                imageTypeLimit, enableImageTypes, enableUserData).getData();
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
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Success</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Not Found</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
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
        // Check required parameters
        if (seriesId == null) {
            throw new ApiException(400, "Missing the required parameter 'seriesId' when calling getSeasons");
        }

        // Path parameters
        String localVarPath = "/Shows/{seriesId}/Seasons".replaceAll("\\{seriesId}",
                apiClient.escapeString(seriesId.toString()));

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "userId", userId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "fields", fields));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "isSpecialSeason", isSpecialSeason));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "isMissing", isMissing));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "adjacentTo", adjacentTo));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "enableImages", enableImages));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "imageTypeLimit", imageTypeLimit));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "enableImageTypes", enableImageTypes));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "enableUserData", enableUserData));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<BaseItemDtoQueryResult> localVarReturnType = new GenericType<BaseItemDtoQueryResult>() {
        };
        return apiClient.invokeAPI("TvShowsApi.getSeasons", localVarPath, "GET", localVarQueryParams, null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, localVarReturnType, false);
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
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Success</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public BaseItemDtoQueryResult getUpcomingEpisodes(@org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull Integer startIndex, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull UUID parentId, @org.eclipse.jdt.annotation.NonNull Boolean enableImages,
            @org.eclipse.jdt.annotation.NonNull Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.NonNull List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.NonNull Boolean enableUserData) throws ApiException {
        return getUpcomingEpisodesWithHttpInfo(userId, startIndex, limit, fields, parentId, enableImages,
                imageTypeLimit, enableImageTypes, enableUserData).getData();
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
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Success</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<BaseItemDtoQueryResult> getUpcomingEpisodesWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull UUID userId, @org.eclipse.jdt.annotation.NonNull Integer startIndex,
            @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull UUID parentId, @org.eclipse.jdt.annotation.NonNull Boolean enableImages,
            @org.eclipse.jdt.annotation.NonNull Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.NonNull List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.NonNull Boolean enableUserData) throws ApiException {
        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "userId", userId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "startIndex", startIndex));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "fields", fields));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "parentId", parentId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "enableImages", enableImages));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "imageTypeLimit", imageTypeLimit));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "enableImageTypes", enableImageTypes));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "enableUserData", enableUserData));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<BaseItemDtoQueryResult> localVarReturnType = new GenericType<BaseItemDtoQueryResult>() {
        };
        return apiClient.invokeAPI("TvShowsApi.getUpcomingEpisodes", "/Shows/Upcoming", "GET", localVarQueryParams,
                null, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, localVarReturnType, false);
    }
}
