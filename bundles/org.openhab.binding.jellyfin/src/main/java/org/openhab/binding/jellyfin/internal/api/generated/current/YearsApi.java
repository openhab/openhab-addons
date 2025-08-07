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
import org.openhab.binding.jellyfin.internal.api.generated.current.model.ItemSortBy;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.MediaType;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.SortOrder;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class YearsApi {
    private ApiClient apiClient;

    public YearsApi() {
        this(Configuration.getDefaultApiClient());
    }

    public YearsApi(ApiClient apiClient) {
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
     * Gets a year.
     * 
     * @param year The year. (required)
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
     *                        <td>Year returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Year not found.</td>
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
    public BaseItemDto getYear(@org.eclipse.jdt.annotation.Nullable Integer year,
            @org.eclipse.jdt.annotation.NonNull UUID userId) throws ApiException {
        return getYearWithHttpInfo(year, userId).getData();
    }

    /**
     * Gets a year.
     * 
     * @param year The year. (required)
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
     *                        <td>Year returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Year not found.</td>
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
    public ApiResponse<BaseItemDto> getYearWithHttpInfo(@org.eclipse.jdt.annotation.Nullable Integer year,
            @org.eclipse.jdt.annotation.NonNull UUID userId) throws ApiException {
        // Check required parameters
        if (year == null) {
            throw new ApiException(400, "Missing the required parameter 'year' when calling getYear");
        }

        // Path parameters
        String localVarPath = "/Years/{year}".replaceAll("\\{year}", apiClient.escapeString(year.toString()));

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "userId", userId));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<BaseItemDto> localVarReturnType = new GenericType<BaseItemDto>() {
        };
        return apiClient.invokeAPI("YearsApi.getYear", localVarPath, "GET", localVarQueryParams, null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, localVarReturnType, false);
    }

    /**
     * Get years.
     * 
     * @param startIndex Skips over a given number of items within the results. Use for paging. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param sortOrder Sort Order - Ascending,Descending. (optional)
     * @param parentId Specify this to localize the search to a specific item or folder. Omit to use the root.
     *            (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. (optional)
     * @param excludeItemTypes Optional. If specified, results will be excluded based on item type. This allows
     *            multiple, comma delimited. (optional)
     * @param includeItemTypes Optional. If specified, results will be included based on item type. This allows
     *            multiple, comma delimited. (optional)
     * @param mediaTypes Optional. Filter by MediaType. Allows multiple, comma delimited. (optional)
     * @param sortBy Optional. Specify one or more sort orders, comma delimited. Options: Album, AlbumArtist, Artist,
     *            Budget, CommunityRating, CriticRating, DateCreated, DatePlayed, PlayCount, PremiereDate,
     *            ProductionYear, SortName, Random, Revenue, Runtime. (optional)
     * @param enableUserData Optional. Include user data. (optional)
     * @param imageTypeLimit Optional. The max number of images to return, per image type. (optional)
     * @param enableImageTypes Optional. The image types to include in the output. (optional)
     * @param userId User Id. (optional)
     * @param recursive Search recursively. (optional, default to true)
     * @param enableImages Optional. Include image information in output. (optional, default to true)
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
     *                        <td>Year query returned.</td>
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
    public BaseItemDtoQueryResult getYears(@org.eclipse.jdt.annotation.NonNull Integer startIndex,
            @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull List<SortOrder> sortOrder,
            @org.eclipse.jdt.annotation.NonNull UUID parentId,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull List<BaseItemKind> excludeItemTypes,
            @org.eclipse.jdt.annotation.NonNull List<BaseItemKind> includeItemTypes,
            @org.eclipse.jdt.annotation.NonNull List<MediaType> mediaTypes,
            @org.eclipse.jdt.annotation.NonNull List<ItemSortBy> sortBy,
            @org.eclipse.jdt.annotation.NonNull Boolean enableUserData,
            @org.eclipse.jdt.annotation.NonNull Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.NonNull List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.NonNull UUID userId, @org.eclipse.jdt.annotation.NonNull Boolean recursive,
            @org.eclipse.jdt.annotation.NonNull Boolean enableImages) throws ApiException {
        return getYearsWithHttpInfo(startIndex, limit, sortOrder, parentId, fields, excludeItemTypes, includeItemTypes,
                mediaTypes, sortBy, enableUserData, imageTypeLimit, enableImageTypes, userId, recursive, enableImages)
                .getData();
    }

    /**
     * Get years.
     * 
     * @param startIndex Skips over a given number of items within the results. Use for paging. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param sortOrder Sort Order - Ascending,Descending. (optional)
     * @param parentId Specify this to localize the search to a specific item or folder. Omit to use the root.
     *            (optional)
     * @param fields Optional. Specify additional fields of information to return in the output. (optional)
     * @param excludeItemTypes Optional. If specified, results will be excluded based on item type. This allows
     *            multiple, comma delimited. (optional)
     * @param includeItemTypes Optional. If specified, results will be included based on item type. This allows
     *            multiple, comma delimited. (optional)
     * @param mediaTypes Optional. Filter by MediaType. Allows multiple, comma delimited. (optional)
     * @param sortBy Optional. Specify one or more sort orders, comma delimited. Options: Album, AlbumArtist, Artist,
     *            Budget, CommunityRating, CriticRating, DateCreated, DatePlayed, PlayCount, PremiereDate,
     *            ProductionYear, SortName, Random, Revenue, Runtime. (optional)
     * @param enableUserData Optional. Include user data. (optional)
     * @param imageTypeLimit Optional. The max number of images to return, per image type. (optional)
     * @param enableImageTypes Optional. The image types to include in the output. (optional)
     * @param userId User Id. (optional)
     * @param recursive Search recursively. (optional, default to true)
     * @param enableImages Optional. Include image information in output. (optional, default to true)
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
     *                        <td>Year query returned.</td>
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
    public ApiResponse<BaseItemDtoQueryResult> getYearsWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull Integer startIndex, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull List<SortOrder> sortOrder,
            @org.eclipse.jdt.annotation.NonNull UUID parentId,
            @org.eclipse.jdt.annotation.NonNull List<ItemFields> fields,
            @org.eclipse.jdt.annotation.NonNull List<BaseItemKind> excludeItemTypes,
            @org.eclipse.jdt.annotation.NonNull List<BaseItemKind> includeItemTypes,
            @org.eclipse.jdt.annotation.NonNull List<MediaType> mediaTypes,
            @org.eclipse.jdt.annotation.NonNull List<ItemSortBy> sortBy,
            @org.eclipse.jdt.annotation.NonNull Boolean enableUserData,
            @org.eclipse.jdt.annotation.NonNull Integer imageTypeLimit,
            @org.eclipse.jdt.annotation.NonNull List<ImageType> enableImageTypes,
            @org.eclipse.jdt.annotation.NonNull UUID userId, @org.eclipse.jdt.annotation.NonNull Boolean recursive,
            @org.eclipse.jdt.annotation.NonNull Boolean enableImages) throws ApiException {
        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "startIndex", startIndex));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "sortOrder", sortOrder));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "parentId", parentId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "fields", fields));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "excludeItemTypes", excludeItemTypes));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "includeItemTypes", includeItemTypes));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "mediaTypes", mediaTypes));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "sortBy", sortBy));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "enableUserData", enableUserData));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "imageTypeLimit", imageTypeLimit));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "enableImageTypes", enableImageTypes));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "userId", userId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "recursive", recursive));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "enableImages", enableImages));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<BaseItemDtoQueryResult> localVarReturnType = new GenericType<BaseItemDtoQueryResult>() {
        };
        return apiClient.invokeAPI("YearsApi.getYears", "/Years", "GET", localVarQueryParams, null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, localVarReturnType, false);
    }
}
