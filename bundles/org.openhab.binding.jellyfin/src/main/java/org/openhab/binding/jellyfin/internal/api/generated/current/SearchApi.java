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
import org.openhab.binding.jellyfin.internal.api.generated.current.model.SearchHintResult;

public class SearchApi {
    private ApiClient apiClient;

    public SearchApi() {
        this(Configuration.getDefaultApiClient());
    }

    public SearchApi(ApiClient apiClient) {
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
     *                        <td>Search hint returned.</td>
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
    public SearchHintResult getSearchHints(@org.eclipse.jdt.annotation.Nullable String searchTerm,
            @org.eclipse.jdt.annotation.NonNull Integer startIndex, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull List<BaseItemKind> includeItemTypes,
            @org.eclipse.jdt.annotation.NonNull List<BaseItemKind> excludeItemTypes,
            @org.eclipse.jdt.annotation.NonNull List<MediaType> mediaTypes,
            @org.eclipse.jdt.annotation.NonNull UUID parentId, @org.eclipse.jdt.annotation.NonNull Boolean isMovie,
            @org.eclipse.jdt.annotation.NonNull Boolean isSeries, @org.eclipse.jdt.annotation.NonNull Boolean isNews,
            @org.eclipse.jdt.annotation.NonNull Boolean isKids, @org.eclipse.jdt.annotation.NonNull Boolean isSports,
            @org.eclipse.jdt.annotation.NonNull Boolean includePeople,
            @org.eclipse.jdt.annotation.NonNull Boolean includeMedia,
            @org.eclipse.jdt.annotation.NonNull Boolean includeGenres,
            @org.eclipse.jdt.annotation.NonNull Boolean includeStudios,
            @org.eclipse.jdt.annotation.NonNull Boolean includeArtists) throws ApiException {
        return getSearchHintsWithHttpInfo(searchTerm, startIndex, limit, userId, includeItemTypes, excludeItemTypes,
                mediaTypes, parentId, isMovie, isSeries, isNews, isKids, isSports, includePeople, includeMedia,
                includeGenres, includeStudios, includeArtists).getData();
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
     *                        <td>Search hint returned.</td>
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
    public ApiResponse<SearchHintResult> getSearchHintsWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable String searchTerm,
            @org.eclipse.jdt.annotation.NonNull Integer startIndex, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull List<BaseItemKind> includeItemTypes,
            @org.eclipse.jdt.annotation.NonNull List<BaseItemKind> excludeItemTypes,
            @org.eclipse.jdt.annotation.NonNull List<MediaType> mediaTypes,
            @org.eclipse.jdt.annotation.NonNull UUID parentId, @org.eclipse.jdt.annotation.NonNull Boolean isMovie,
            @org.eclipse.jdt.annotation.NonNull Boolean isSeries, @org.eclipse.jdt.annotation.NonNull Boolean isNews,
            @org.eclipse.jdt.annotation.NonNull Boolean isKids, @org.eclipse.jdt.annotation.NonNull Boolean isSports,
            @org.eclipse.jdt.annotation.NonNull Boolean includePeople,
            @org.eclipse.jdt.annotation.NonNull Boolean includeMedia,
            @org.eclipse.jdt.annotation.NonNull Boolean includeGenres,
            @org.eclipse.jdt.annotation.NonNull Boolean includeStudios,
            @org.eclipse.jdt.annotation.NonNull Boolean includeArtists) throws ApiException {
        // Check required parameters
        if (searchTerm == null) {
            throw new ApiException(400, "Missing the required parameter 'searchTerm' when calling getSearchHints");
        }

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "startIndex", startIndex));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "userId", userId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "searchTerm", searchTerm));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "includeItemTypes", includeItemTypes));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "excludeItemTypes", excludeItemTypes));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "mediaTypes", mediaTypes));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "parentId", parentId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "isMovie", isMovie));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "isSeries", isSeries));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "isNews", isNews));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "isKids", isKids));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "isSports", isSports));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "includePeople", includePeople));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "includeMedia", includeMedia));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "includeGenres", includeGenres));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "includeStudios", includeStudios));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "includeArtists", includeArtists));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<SearchHintResult> localVarReturnType = new GenericType<SearchHintResult>() {
        };
        return apiClient.invokeAPI("SearchApi.getSearchHints", "/Search/Hints", "GET", localVarQueryParams, null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, localVarReturnType, false);
    }
}
