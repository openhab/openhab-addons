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
import org.openhab.binding.jellyfin.internal.api.generated.current.model.BaseItemKind;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.MediaType;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.QueryFilters;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.QueryFiltersLegacy;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class FilterApi {
    private ApiClient apiClient;

    public FilterApi() {
        this(Configuration.getDefaultApiClient());
    }

    public FilterApi(ApiClient apiClient) {
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
     *                        <td>Filters retrieved.</td>
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
    public QueryFilters getQueryFilters(@org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull UUID parentId,
            @org.eclipse.jdt.annotation.NonNull List<BaseItemKind> includeItemTypes,
            @org.eclipse.jdt.annotation.NonNull Boolean isAiring, @org.eclipse.jdt.annotation.NonNull Boolean isMovie,
            @org.eclipse.jdt.annotation.NonNull Boolean isSports, @org.eclipse.jdt.annotation.NonNull Boolean isKids,
            @org.eclipse.jdt.annotation.NonNull Boolean isNews, @org.eclipse.jdt.annotation.NonNull Boolean isSeries,
            @org.eclipse.jdt.annotation.NonNull Boolean recursive) throws ApiException {
        return getQueryFiltersWithHttpInfo(userId, parentId, includeItemTypes, isAiring, isMovie, isSports, isKids,
                isNews, isSeries, recursive).getData();
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
     *                        <td>Filters retrieved.</td>
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
    public ApiResponse<QueryFilters> getQueryFiltersWithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull UUID parentId,
            @org.eclipse.jdt.annotation.NonNull List<BaseItemKind> includeItemTypes,
            @org.eclipse.jdt.annotation.NonNull Boolean isAiring, @org.eclipse.jdt.annotation.NonNull Boolean isMovie,
            @org.eclipse.jdt.annotation.NonNull Boolean isSports, @org.eclipse.jdt.annotation.NonNull Boolean isKids,
            @org.eclipse.jdt.annotation.NonNull Boolean isNews, @org.eclipse.jdt.annotation.NonNull Boolean isSeries,
            @org.eclipse.jdt.annotation.NonNull Boolean recursive) throws ApiException {
        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "userId", userId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "parentId", parentId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "includeItemTypes", includeItemTypes));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "isAiring", isAiring));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "isMovie", isMovie));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "isSports", isSports));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "isKids", isKids));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "isNews", isNews));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "isSeries", isSeries));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "recursive", recursive));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<QueryFilters> localVarReturnType = new GenericType<QueryFilters>() {
        };
        return apiClient.invokeAPI("FilterApi.getQueryFilters", "/Items/Filters2", "GET", localVarQueryParams, null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, localVarReturnType, false);
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
     *                        <td>Legacy filters retrieved.</td>
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
    public QueryFiltersLegacy getQueryFiltersLegacy(@org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull UUID parentId,
            @org.eclipse.jdt.annotation.NonNull List<BaseItemKind> includeItemTypes,
            @org.eclipse.jdt.annotation.NonNull List<MediaType> mediaTypes) throws ApiException {
        return getQueryFiltersLegacyWithHttpInfo(userId, parentId, includeItemTypes, mediaTypes).getData();
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
     *                        <td>Legacy filters retrieved.</td>
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
    public ApiResponse<QueryFiltersLegacy> getQueryFiltersLegacyWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull UUID userId, @org.eclipse.jdt.annotation.NonNull UUID parentId,
            @org.eclipse.jdt.annotation.NonNull List<BaseItemKind> includeItemTypes,
            @org.eclipse.jdt.annotation.NonNull List<MediaType> mediaTypes) throws ApiException {
        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "userId", userId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "parentId", parentId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "includeItemTypes", includeItemTypes));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "mediaTypes", mediaTypes));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<QueryFiltersLegacy> localVarReturnType = new GenericType<QueryFiltersLegacy>() {
        };
        return apiClient.invokeAPI("FilterApi.getQueryFiltersLegacy", "/Items/Filters", "GET", localVarQueryParams,
                null, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, localVarReturnType, false);
    }
}
