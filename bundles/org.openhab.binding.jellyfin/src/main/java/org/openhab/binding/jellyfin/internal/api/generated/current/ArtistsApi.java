package org.openhab.binding.jellyfin.internal.api.generated.current;

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
import org.openhab.binding.jellyfin.internal.api.generated.current.model.BaseItemDto;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.BaseItemDtoQueryResult;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.BaseItemKind;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.ImageType;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.ItemFields;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.ItemFilter;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.ItemSortBy;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.MediaType;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.SortOrder;

public class ArtistsApi {
    private ApiClient apiClient;

    public ArtistsApi() {
        this(Configuration.getDefaultApiClient());
    }

    public ArtistsApi(ApiClient apiClient) {
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
     * Gets all album artists from a given item, folder, or the entire library.
     * 
     * @param minCommunityRating Optional filter by minimum community rating. (optional)
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param searchTerm Optional. Search term. (optional)
     * @param parentId Specify this to localize the search to a specific item or folder. Omit to use the root.
     *            (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. (optional)
     * @param excludeItemTypes Optional. If specified, results will be filtered out based on item type. This allows
     *            multiple, comma delimited. (optional)
     * @param includeItemTypes Optional. If specified, results will be filtered based on item type. This allows
     *            multiple, comma delimited. (optional)
     * @param filters Optional. Specify additional filters to apply. (optional)
     * @param isFavorite Optional filter by items that are marked as favorite, or not. (optional)
     * @param mediaTypes Optional filter by MediaType. Allows multiple, comma delimited. (optional)
     * @param genres Optional. If specified, results will be filtered based on genre. This allows multiple, pipe
     *            delimited. (optional)
     * @param genreIds Optional. If specified, results will be filtered based on genre id. This allows multiple, pipe
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
     *            person ids. (optional)
     * @param personTypes Optional. If specified, along with Person, results will be filtered to include only those
     *            containing the specified person and PersonType. Allows multiple, comma-delimited. (optional)
     * @param studios Optional. If specified, results will be filtered based on studio. This allows multiple, pipe
     *            delimited. (optional)
     * @param studioIds Optional. If specified, results will be filtered based on studio id. This allows multiple, pipe
     *            delimited. (optional)
     * @param userId User id. (optional)
     * @param nameStartsWithOrGreater Optional filter by items whose name is sorted equally or greater than a given
     *            input string. (optional)
     * @param nameStartsWith Optional filter by items whose name is sorted equally than a given input string. (optional)
     * @param nameLessThan Optional filter by items whose name is equally or lesser than a given input string.
     *            (optional)
     * @param sortBy Optional. Specify one or more sort orders, comma delimited. (optional)
     * @param sortOrder Sort Order - Ascending,Descending. (optional)
     * @param enableImages Optional, include image information in output. (optional, default to true)
     * @param enableTotalRecordCount Total record count. (optional, default to true)
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
     *                        <td>Album artists returned.</td>
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
    public BaseItemDtoQueryResult getAlbumArtists(@org.eclipse.jdt.annotation.NonNull Double minCommunityRating,
            @org.eclipse.jdt.annotation.NonNull Integer startIndex, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull String searchTerm, @org.eclipse.jdt.annotation.NonNull UUID parentId,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull List<BaseItemKind> excludeItemTypes,
            @org.eclipse.jdt.annotation.NonNull List<BaseItemKind> includeItemTypes,
            @org.eclipse.jdt.annotation.NonNull List<ItemFilter> filters,
            @org.eclipse.jdt.annotation.NonNull Boolean isFavorite,
            @org.eclipse.jdt.annotation.NonNull List<MediaType> mediaTypes,
            @org.eclipse.jdt.annotation.NonNull List<String> genres,
            @org.eclipse.jdt.annotation.NonNull List<UUID> genreIds,
            @org.eclipse.jdt.annotation.NonNull List<String> officialRatings,
            @org.eclipse.jdt.annotation.NonNull List<String> tags,
            @org.eclipse.jdt.annotation.NonNull List<Integer> years,
            @org.eclipse.jdt.annotation.NonNull Boolean enableUserData,
            @org.eclipse.jdt.annotation.NonNull Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.NonNull List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.NonNull String person, @org.eclipse.jdt.annotation.NonNull List<UUID> personIds,
            @org.eclipse.jdt.annotation.NonNull List<String> personTypes,
            @org.eclipse.jdt.annotation.NonNull List<String> studios,
            @org.eclipse.jdt.annotation.NonNull List<UUID> studioIds, @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull String nameStartsWithOrGreater,
            @org.eclipse.jdt.annotation.NonNull String nameStartsWith,
            @org.eclipse.jdt.annotation.NonNull String nameLessThan,
            @org.eclipse.jdt.annotation.NonNull List<ItemSortBy> sortBy,
            @org.eclipse.jdt.annotation.NonNull List<SortOrder> sortOrder,
            @org.eclipse.jdt.annotation.NonNull Boolean enableImages,
            @org.eclipse.jdt.annotation.NonNull Boolean enableTotalRecordCount) throws ApiException {
        return getAlbumArtistsWithHttpInfo(minCommunityRating, startIndex, limit, searchTerm, parentId, fields,
                excludeItemTypes, includeItemTypes, filters, isFavorite, mediaTypes, genres, genreIds, officialRatings,
                tags, years, enableUserData, imageTypeLimit, enableImageTypes, person, personIds, personTypes, studios,
                studioIds, userId, nameStartsWithOrGreater, nameStartsWith, nameLessThan, sortBy, sortOrder,
                enableImages, enableTotalRecordCount).getData();
    }

    /**
     * Gets all album artists from a given item, folder, or the entire library.
     * 
     * @param minCommunityRating Optional filter by minimum community rating. (optional)
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param searchTerm Optional. Search term. (optional)
     * @param parentId Specify this to localize the search to a specific item or folder. Omit to use the root.
     *            (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. (optional)
     * @param excludeItemTypes Optional. If specified, results will be filtered out based on item type. This allows
     *            multiple, comma delimited. (optional)
     * @param includeItemTypes Optional. If specified, results will be filtered based on item type. This allows
     *            multiple, comma delimited. (optional)
     * @param filters Optional. Specify additional filters to apply. (optional)
     * @param isFavorite Optional filter by items that are marked as favorite, or not. (optional)
     * @param mediaTypes Optional filter by MediaType. Allows multiple, comma delimited. (optional)
     * @param genres Optional. If specified, results will be filtered based on genre. This allows multiple, pipe
     *            delimited. (optional)
     * @param genreIds Optional. If specified, results will be filtered based on genre id. This allows multiple, pipe
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
     *            person ids. (optional)
     * @param personTypes Optional. If specified, along with Person, results will be filtered to include only those
     *            containing the specified person and PersonType. Allows multiple, comma-delimited. (optional)
     * @param studios Optional. If specified, results will be filtered based on studio. This allows multiple, pipe
     *            delimited. (optional)
     * @param studioIds Optional. If specified, results will be filtered based on studio id. This allows multiple, pipe
     *            delimited. (optional)
     * @param userId User id. (optional)
     * @param nameStartsWithOrGreater Optional filter by items whose name is sorted equally or greater than a given
     *            input string. (optional)
     * @param nameStartsWith Optional filter by items whose name is sorted equally than a given input string. (optional)
     * @param nameLessThan Optional filter by items whose name is equally or lesser than a given input string.
     *            (optional)
     * @param sortBy Optional. Specify one or more sort orders, comma delimited. (optional)
     * @param sortOrder Sort Order - Ascending,Descending. (optional)
     * @param enableImages Optional, include image information in output. (optional, default to true)
     * @param enableTotalRecordCount Total record count. (optional, default to true)
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
     *                        <td>Album artists returned.</td>
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
    public ApiResponse<BaseItemDtoQueryResult> getAlbumArtistsWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull Double minCommunityRating,
            @org.eclipse.jdt.annotation.NonNull Integer startIndex, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull String searchTerm, @org.eclipse.jdt.annotation.NonNull UUID parentId,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull List<BaseItemKind> excludeItemTypes,
            @org.eclipse.jdt.annotation.NonNull List<BaseItemKind> includeItemTypes,
            @org.eclipse.jdt.annotation.NonNull List<ItemFilter> filters,
            @org.eclipse.jdt.annotation.NonNull Boolean isFavorite,
            @org.eclipse.jdt.annotation.NonNull List<MediaType> mediaTypes,
            @org.eclipse.jdt.annotation.NonNull List<String> genres,
            @org.eclipse.jdt.annotation.NonNull List<UUID> genreIds,
            @org.eclipse.jdt.annotation.NonNull List<String> officialRatings,
            @org.eclipse.jdt.annotation.NonNull List<String> tags,
            @org.eclipse.jdt.annotation.NonNull List<Integer> years,
            @org.eclipse.jdt.annotation.NonNull Boolean enableUserData,
            @org.eclipse.jdt.annotation.NonNull Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.NonNull List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.NonNull String person, @org.eclipse.jdt.annotation.NonNull List<UUID> personIds,
            @org.eclipse.jdt.annotation.NonNull List<String> personTypes,
            @org.eclipse.jdt.annotation.NonNull List<String> studios,
            @org.eclipse.jdt.annotation.NonNull List<UUID> studioIds, @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull String nameStartsWithOrGreater,
            @org.eclipse.jdt.annotation.NonNull String nameStartsWith,
            @org.eclipse.jdt.annotation.NonNull String nameLessThan,
            @org.eclipse.jdt.annotation.NonNull List<ItemSortBy> sortBy,
            @org.eclipse.jdt.annotation.NonNull List<SortOrder> sortOrder,
            @org.eclipse.jdt.annotation.NonNull Boolean enableImages,
            @org.eclipse.jdt.annotation.NonNull Boolean enableTotalRecordCount) throws ApiException {
        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(
                apiClient.parameterToPairs("", "minCommunityRating", minCommunityRating));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "startIndex", startIndex));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "searchTerm", searchTerm));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "parentId", parentId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "fields", fields));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "excludeItemTypes", excludeItemTypes));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "includeItemTypes", includeItemTypes));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "filters", filters));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "isFavorite", isFavorite));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "mediaTypes", mediaTypes));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "genres", genres));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "genreIds", genreIds));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "officialRatings", officialRatings));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "tags", tags));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "years", years));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "enableUserData", enableUserData));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "imageTypeLimit", imageTypeLimit));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "enableImageTypes", enableImageTypes));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "person", person));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "personIds", personIds));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "personTypes", personTypes));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "studios", studios));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "studioIds", studioIds));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "userId", userId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "nameStartsWithOrGreater", nameStartsWithOrGreater));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "nameStartsWith", nameStartsWith));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "nameLessThan", nameLessThan));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "sortBy", sortBy));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "sortOrder", sortOrder));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "enableImages", enableImages));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "enableTotalRecordCount", enableTotalRecordCount));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<BaseItemDtoQueryResult> localVarReturnType = new GenericType<BaseItemDtoQueryResult>() {
        };
        return apiClient.invokeAPI("ArtistsApi.getAlbumArtists", "/Artists/AlbumArtists", "GET", localVarQueryParams,
                null, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, localVarReturnType, false);
    }

    /**
     * Gets an artist by name.
     * 
     * @param name Studio name. (required)
     * @param userId Optional. Filter by user id, and attach user data. (optional)
     * @return BaseItemDto
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
     *                        <td>Artist returned.</td>
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
    public BaseItemDto getArtistByName(@org.eclipse.jdt.annotation.Nullable String name,
            @org.eclipse.jdt.annotation.NonNull UUID userId) throws ApiException {
        return getArtistByNameWithHttpInfo(name, userId).getData();
    }

    /**
     * Gets an artist by name.
     * 
     * @param name Studio name. (required)
     * @param userId Optional. Filter by user id, and attach user data. (optional)
     * @return ApiResponse&lt;BaseItemDto&gt;
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
     *                        <td>Artist returned.</td>
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
    public ApiResponse<BaseItemDto> getArtistByNameWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String name,
            @org.eclipse.jdt.annotation.NonNull UUID userId) throws ApiException {
        // Check required parameters
        if (name == null) {
            throw new ApiException(400, "Missing the required parameter 'name' when calling getArtistByName");
        }

        // Path parameters
        String localVarPath = "/Artists/{name}".replaceAll("\\{name}", apiClient.escapeString(name.toString()));

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "userId", userId));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<BaseItemDto> localVarReturnType = new GenericType<BaseItemDto>() {
        };
        return apiClient.invokeAPI("ArtistsApi.getArtistByName", localVarPath, "GET", localVarQueryParams, null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, localVarReturnType, false);
    }

    /**
     * Gets all artists from a given item, folder, or the entire library.
     * 
     * @param minCommunityRating Optional filter by minimum community rating. (optional)
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param searchTerm Optional. Search term. (optional)
     * @param parentId Specify this to localize the search to a specific item or folder. Omit to use the root.
     *            (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. (optional)
     * @param excludeItemTypes Optional. If specified, results will be filtered out based on item type. This allows
     *            multiple, comma delimited. (optional)
     * @param includeItemTypes Optional. If specified, results will be filtered based on item type. This allows
     *            multiple, comma delimited. (optional)
     * @param filters Optional. Specify additional filters to apply. (optional)
     * @param isFavorite Optional filter by items that are marked as favorite, or not. (optional)
     * @param mediaTypes Optional filter by MediaType. Allows multiple, comma delimited. (optional)
     * @param genres Optional. If specified, results will be filtered based on genre. This allows multiple, pipe
     *            delimited. (optional)
     * @param genreIds Optional. If specified, results will be filtered based on genre id. This allows multiple, pipe
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
     *            person ids. (optional)
     * @param personTypes Optional. If specified, along with Person, results will be filtered to include only those
     *            containing the specified person and PersonType. Allows multiple, comma-delimited. (optional)
     * @param studios Optional. If specified, results will be filtered based on studio. This allows multiple, pipe
     *            delimited. (optional)
     * @param studioIds Optional. If specified, results will be filtered based on studio id. This allows multiple, pipe
     *            delimited. (optional)
     * @param userId User id. (optional)
     * @param nameStartsWithOrGreater Optional filter by items whose name is sorted equally or greater than a given
     *            input string. (optional)
     * @param nameStartsWith Optional filter by items whose name is sorted equally than a given input string. (optional)
     * @param nameLessThan Optional filter by items whose name is equally or lesser than a given input string.
     *            (optional)
     * @param sortBy Optional. Specify one or more sort orders, comma delimited. (optional)
     * @param sortOrder Sort Order - Ascending,Descending. (optional)
     * @param enableImages Optional, include image information in output. (optional, default to true)
     * @param enableTotalRecordCount Total record count. (optional, default to true)
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
     *                        <td>Artists returned.</td>
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
    public BaseItemDtoQueryResult getArtists(@org.eclipse.jdt.annotation.NonNull Double minCommunityRating,
            @org.eclipse.jdt.annotation.NonNull Integer startIndex, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull String searchTerm, @org.eclipse.jdt.annotation.NonNull UUID parentId,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull List<BaseItemKind> excludeItemTypes,
            @org.eclipse.jdt.annotation.NonNull List<BaseItemKind> includeItemTypes,
            @org.eclipse.jdt.annotation.NonNull List<ItemFilter> filters,
            @org.eclipse.jdt.annotation.NonNull Boolean isFavorite,
            @org.eclipse.jdt.annotation.NonNull List<MediaType> mediaTypes,
            @org.eclipse.jdt.annotation.NonNull List<String> genres,
            @org.eclipse.jdt.annotation.NonNull List<UUID> genreIds,
            @org.eclipse.jdt.annotation.NonNull List<String> officialRatings,
            @org.eclipse.jdt.annotation.NonNull List<String> tags,
            @org.eclipse.jdt.annotation.NonNull List<Integer> years,
            @org.eclipse.jdt.annotation.NonNull Boolean enableUserData,
            @org.eclipse.jdt.annotation.NonNull Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.NonNull List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.NonNull String person, @org.eclipse.jdt.annotation.NonNull List<UUID> personIds,
            @org.eclipse.jdt.annotation.NonNull List<String> personTypes,
            @org.eclipse.jdt.annotation.NonNull List<String> studios,
            @org.eclipse.jdt.annotation.NonNull List<UUID> studioIds, @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull String nameStartsWithOrGreater,
            @org.eclipse.jdt.annotation.NonNull String nameStartsWith,
            @org.eclipse.jdt.annotation.NonNull String nameLessThan,
            @org.eclipse.jdt.annotation.NonNull List<ItemSortBy> sortBy,
            @org.eclipse.jdt.annotation.NonNull List<SortOrder> sortOrder,
            @org.eclipse.jdt.annotation.NonNull Boolean enableImages,
            @org.eclipse.jdt.annotation.NonNull Boolean enableTotalRecordCount) throws ApiException {
        return getArtistsWithHttpInfo(minCommunityRating, startIndex, limit, searchTerm, parentId, fields,
                excludeItemTypes, includeItemTypes, filters, isFavorite, mediaTypes, genres, genreIds, officialRatings,
                tags, years, enableUserData, imageTypeLimit, enableImageTypes, person, personIds, personTypes, studios,
                studioIds, userId, nameStartsWithOrGreater, nameStartsWith, nameLessThan, sortBy, sortOrder,
                enableImages, enableTotalRecordCount).getData();
    }

    /**
     * Gets all artists from a given item, folder, or the entire library.
     * 
     * @param minCommunityRating Optional filter by minimum community rating. (optional)
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param searchTerm Optional. Search term. (optional)
     * @param parentId Specify this to localize the search to a specific item or folder. Omit to use the root.
     *            (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. (optional)
     * @param excludeItemTypes Optional. If specified, results will be filtered out based on item type. This allows
     *            multiple, comma delimited. (optional)
     * @param includeItemTypes Optional. If specified, results will be filtered based on item type. This allows
     *            multiple, comma delimited. (optional)
     * @param filters Optional. Specify additional filters to apply. (optional)
     * @param isFavorite Optional filter by items that are marked as favorite, or not. (optional)
     * @param mediaTypes Optional filter by MediaType. Allows multiple, comma delimited. (optional)
     * @param genres Optional. If specified, results will be filtered based on genre. This allows multiple, pipe
     *            delimited. (optional)
     * @param genreIds Optional. If specified, results will be filtered based on genre id. This allows multiple, pipe
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
     *            person ids. (optional)
     * @param personTypes Optional. If specified, along with Person, results will be filtered to include only those
     *            containing the specified person and PersonType. Allows multiple, comma-delimited. (optional)
     * @param studios Optional. If specified, results will be filtered based on studio. This allows multiple, pipe
     *            delimited. (optional)
     * @param studioIds Optional. If specified, results will be filtered based on studio id. This allows multiple, pipe
     *            delimited. (optional)
     * @param userId User id. (optional)
     * @param nameStartsWithOrGreater Optional filter by items whose name is sorted equally or greater than a given
     *            input string. (optional)
     * @param nameStartsWith Optional filter by items whose name is sorted equally than a given input string. (optional)
     * @param nameLessThan Optional filter by items whose name is equally or lesser than a given input string.
     *            (optional)
     * @param sortBy Optional. Specify one or more sort orders, comma delimited. (optional)
     * @param sortOrder Sort Order - Ascending,Descending. (optional)
     * @param enableImages Optional, include image information in output. (optional, default to true)
     * @param enableTotalRecordCount Total record count. (optional, default to true)
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
     *                        <td>Artists returned.</td>
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
    public ApiResponse<BaseItemDtoQueryResult> getArtistsWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull Double minCommunityRating,
            @org.eclipse.jdt.annotation.NonNull Integer startIndex, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull String searchTerm, @org.eclipse.jdt.annotation.NonNull UUID parentId,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull List<BaseItemKind> excludeItemTypes,
            @org.eclipse.jdt.annotation.NonNull List<BaseItemKind> includeItemTypes,
            @org.eclipse.jdt.annotation.NonNull List<ItemFilter> filters,
            @org.eclipse.jdt.annotation.NonNull Boolean isFavorite,
            @org.eclipse.jdt.annotation.NonNull List<MediaType> mediaTypes,
            @org.eclipse.jdt.annotation.NonNull List<String> genres,
            @org.eclipse.jdt.annotation.NonNull List<UUID> genreIds,
            @org.eclipse.jdt.annotation.NonNull List<String> officialRatings,
            @org.eclipse.jdt.annotation.NonNull List<String> tags,
            @org.eclipse.jdt.annotation.NonNull List<Integer> years,
            @org.eclipse.jdt.annotation.NonNull Boolean enableUserData,
            @org.eclipse.jdt.annotation.NonNull Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.NonNull List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.NonNull String person, @org.eclipse.jdt.annotation.NonNull List<UUID> personIds,
            @org.eclipse.jdt.annotation.NonNull List<String> personTypes,
            @org.eclipse.jdt.annotation.NonNull List<String> studios,
            @org.eclipse.jdt.annotation.NonNull List<UUID> studioIds, @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull String nameStartsWithOrGreater,
            @org.eclipse.jdt.annotation.NonNull String nameStartsWith,
            @org.eclipse.jdt.annotation.NonNull String nameLessThan,
            @org.eclipse.jdt.annotation.NonNull List<ItemSortBy> sortBy,
            @org.eclipse.jdt.annotation.NonNull List<SortOrder> sortOrder,
            @org.eclipse.jdt.annotation.NonNull Boolean enableImages,
            @org.eclipse.jdt.annotation.NonNull Boolean enableTotalRecordCount) throws ApiException {
        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(
                apiClient.parameterToPairs("", "minCommunityRating", minCommunityRating));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "startIndex", startIndex));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "searchTerm", searchTerm));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "parentId", parentId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "fields", fields));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "excludeItemTypes", excludeItemTypes));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "includeItemTypes", includeItemTypes));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "filters", filters));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "isFavorite", isFavorite));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "mediaTypes", mediaTypes));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "genres", genres));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "genreIds", genreIds));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "officialRatings", officialRatings));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "tags", tags));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "years", years));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "enableUserData", enableUserData));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "imageTypeLimit", imageTypeLimit));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "enableImageTypes", enableImageTypes));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "person", person));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "personIds", personIds));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "personTypes", personTypes));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "studios", studios));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "studioIds", studioIds));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "userId", userId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "nameStartsWithOrGreater", nameStartsWithOrGreater));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "nameStartsWith", nameStartsWith));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "nameLessThan", nameLessThan));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "sortBy", sortBy));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "sortOrder", sortOrder));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "enableImages", enableImages));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "enableTotalRecordCount", enableTotalRecordCount));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<BaseItemDtoQueryResult> localVarReturnType = new GenericType<BaseItemDtoQueryResult>() {
        };
        return apiClient.invokeAPI("ArtistsApi.getArtists", "/Artists", "GET", localVarQueryParams, null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, localVarReturnType, false);
    }
}
