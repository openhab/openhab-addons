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

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class ItemLookupApi {
    private ApiClient apiClient;

    public ItemLookupApi() {
        this(Configuration.getDefaultApiClient());
    }

    public ItemLookupApi(ApiClient apiClient) {
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
     * Applies search criteria to an item and refreshes metadata.
     * 
     * @param itemId Item id. (required)
     * @param remoteSearchResult The remote search result. (required)
     * @param replaceAllImages Optional. Whether or not to replace all images. Default: True. (optional, default to
     *            true)
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
     *                        <td>204</td>
     *                        <td>Item metadata refreshed.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Item not found.</td>
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
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>204</td>
     *                        <td>Item metadata refreshed.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Item not found.</td>
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
    public ApiResponse<Void> applySearchCriteriaWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable RemoteSearchResult remoteSearchResult,
            @org.eclipse.jdt.annotation.NonNull Boolean replaceAllImages) throws ApiException {
        // Check required parameters
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling applySearchCriteria");
        }
        if (remoteSearchResult == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'remoteSearchResult' when calling applySearchCriteria");
        }

        // Path parameters
        String localVarPath = "/Items/RemoteSearch/Apply/{itemId}".replaceAll("\\{itemId}",
                apiClient.escapeString(itemId.toString()));

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(
                apiClient.parameterToPairs("", "replaceAllImages", replaceAllImages));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType("application/json", "text/json",
                "application/*+json");
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        return apiClient.invokeAPI("ItemLookupApi.applySearchCriteria", localVarPath, "POST", localVarQueryParams,
                remoteSearchResult, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, null, false);
    }

    /**
     * Get book remote search.
     * 
     * @param bookInfoRemoteSearchQuery Remote search query. (required)
     * @return List&lt;RemoteSearchResult&gt;
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
     *                        <td>Book remote search executed.</td>
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
    public List<RemoteSearchResult> getBookRemoteSearchResults(
            @org.eclipse.jdt.annotation.Nullable BookInfoRemoteSearchQuery bookInfoRemoteSearchQuery)
            throws ApiException {
        return getBookRemoteSearchResultsWithHttpInfo(bookInfoRemoteSearchQuery).getData();
    }

    /**
     * Get book remote search.
     * 
     * @param bookInfoRemoteSearchQuery Remote search query. (required)
     * @return ApiResponse&lt;List&lt;RemoteSearchResult&gt;&gt;
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
     *                        <td>Book remote search executed.</td>
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
    public ApiResponse<List<RemoteSearchResult>> getBookRemoteSearchResultsWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable BookInfoRemoteSearchQuery bookInfoRemoteSearchQuery)
            throws ApiException {
        // Check required parameters
        if (bookInfoRemoteSearchQuery == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'bookInfoRemoteSearchQuery' when calling getBookRemoteSearchResults");
        }

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType("application/json", "text/json",
                "application/*+json");
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<List<RemoteSearchResult>> localVarReturnType = new GenericType<List<RemoteSearchResult>>() {
        };
        return apiClient.invokeAPI("ItemLookupApi.getBookRemoteSearchResults", "/Items/RemoteSearch/Book", "POST",
                new ArrayList<>(), bookInfoRemoteSearchQuery, new LinkedHashMap<>(), new LinkedHashMap<>(),
                new LinkedHashMap<>(), localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType,
                false);
    }

    /**
     * Get box set remote search.
     * 
     * @param boxSetInfoRemoteSearchQuery Remote search query. (required)
     * @return List&lt;RemoteSearchResult&gt;
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
     *                        <td>Box set remote search executed.</td>
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
    public List<RemoteSearchResult> getBoxSetRemoteSearchResults(
            @org.eclipse.jdt.annotation.Nullable BoxSetInfoRemoteSearchQuery boxSetInfoRemoteSearchQuery)
            throws ApiException {
        return getBoxSetRemoteSearchResultsWithHttpInfo(boxSetInfoRemoteSearchQuery).getData();
    }

    /**
     * Get box set remote search.
     * 
     * @param boxSetInfoRemoteSearchQuery Remote search query. (required)
     * @return ApiResponse&lt;List&lt;RemoteSearchResult&gt;&gt;
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
     *                        <td>Box set remote search executed.</td>
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
    public ApiResponse<List<RemoteSearchResult>> getBoxSetRemoteSearchResultsWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable BoxSetInfoRemoteSearchQuery boxSetInfoRemoteSearchQuery)
            throws ApiException {
        // Check required parameters
        if (boxSetInfoRemoteSearchQuery == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'boxSetInfoRemoteSearchQuery' when calling getBoxSetRemoteSearchResults");
        }

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType("application/json", "text/json",
                "application/*+json");
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<List<RemoteSearchResult>> localVarReturnType = new GenericType<List<RemoteSearchResult>>() {
        };
        return apiClient.invokeAPI("ItemLookupApi.getBoxSetRemoteSearchResults", "/Items/RemoteSearch/BoxSet", "POST",
                new ArrayList<>(), boxSetInfoRemoteSearchQuery, new LinkedHashMap<>(), new LinkedHashMap<>(),
                new LinkedHashMap<>(), localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType,
                false);
    }

    /**
     * Get the item&#39;s external id info.
     * 
     * @param itemId Item id. (required)
     * @return List&lt;ExternalIdInfo&gt;
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
     *                        <td>External id info retrieved.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Item not found.</td>
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
    public List<ExternalIdInfo> getExternalIdInfos(@org.eclipse.jdt.annotation.Nullable UUID itemId)
            throws ApiException {
        return getExternalIdInfosWithHttpInfo(itemId).getData();
    }

    /**
     * Get the item&#39;s external id info.
     * 
     * @param itemId Item id. (required)
     * @return ApiResponse&lt;List&lt;ExternalIdInfo&gt;&gt;
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
     *                        <td>External id info retrieved.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Item not found.</td>
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
    public ApiResponse<List<ExternalIdInfo>> getExternalIdInfosWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable UUID itemId) throws ApiException {
        // Check required parameters
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling getExternalIdInfos");
        }

        // Path parameters
        String localVarPath = "/Items/{itemId}/ExternalIdInfos".replaceAll("\\{itemId}",
                apiClient.escapeString(itemId.toString()));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<List<ExternalIdInfo>> localVarReturnType = new GenericType<List<ExternalIdInfo>>() {
        };
        return apiClient.invokeAPI("ItemLookupApi.getExternalIdInfos", localVarPath, "GET", new ArrayList<>(), null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, localVarReturnType, false);
    }

    /**
     * Get movie remote search.
     * 
     * @param movieInfoRemoteSearchQuery Remote search query. (required)
     * @return List&lt;RemoteSearchResult&gt;
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
     *                        <td>Movie remote search executed.</td>
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
    public List<RemoteSearchResult> getMovieRemoteSearchResults(
            @org.eclipse.jdt.annotation.Nullable MovieInfoRemoteSearchQuery movieInfoRemoteSearchQuery)
            throws ApiException {
        return getMovieRemoteSearchResultsWithHttpInfo(movieInfoRemoteSearchQuery).getData();
    }

    /**
     * Get movie remote search.
     * 
     * @param movieInfoRemoteSearchQuery Remote search query. (required)
     * @return ApiResponse&lt;List&lt;RemoteSearchResult&gt;&gt;
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
     *                        <td>Movie remote search executed.</td>
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
    public ApiResponse<List<RemoteSearchResult>> getMovieRemoteSearchResultsWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable MovieInfoRemoteSearchQuery movieInfoRemoteSearchQuery)
            throws ApiException {
        // Check required parameters
        if (movieInfoRemoteSearchQuery == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'movieInfoRemoteSearchQuery' when calling getMovieRemoteSearchResults");
        }

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType("application/json", "text/json",
                "application/*+json");
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<List<RemoteSearchResult>> localVarReturnType = new GenericType<List<RemoteSearchResult>>() {
        };
        return apiClient.invokeAPI("ItemLookupApi.getMovieRemoteSearchResults", "/Items/RemoteSearch/Movie", "POST",
                new ArrayList<>(), movieInfoRemoteSearchQuery, new LinkedHashMap<>(), new LinkedHashMap<>(),
                new LinkedHashMap<>(), localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType,
                false);
    }

    /**
     * Get music album remote search.
     * 
     * @param albumInfoRemoteSearchQuery Remote search query. (required)
     * @return List&lt;RemoteSearchResult&gt;
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
     *                        <td>Music album remote search executed.</td>
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
    public List<RemoteSearchResult> getMusicAlbumRemoteSearchResults(
            @org.eclipse.jdt.annotation.Nullable AlbumInfoRemoteSearchQuery albumInfoRemoteSearchQuery)
            throws ApiException {
        return getMusicAlbumRemoteSearchResultsWithHttpInfo(albumInfoRemoteSearchQuery).getData();
    }

    /**
     * Get music album remote search.
     * 
     * @param albumInfoRemoteSearchQuery Remote search query. (required)
     * @return ApiResponse&lt;List&lt;RemoteSearchResult&gt;&gt;
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
     *                        <td>Music album remote search executed.</td>
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
    public ApiResponse<List<RemoteSearchResult>> getMusicAlbumRemoteSearchResultsWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable AlbumInfoRemoteSearchQuery albumInfoRemoteSearchQuery)
            throws ApiException {
        // Check required parameters
        if (albumInfoRemoteSearchQuery == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'albumInfoRemoteSearchQuery' when calling getMusicAlbumRemoteSearchResults");
        }

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType("application/json", "text/json",
                "application/*+json");
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<List<RemoteSearchResult>> localVarReturnType = new GenericType<List<RemoteSearchResult>>() {
        };
        return apiClient.invokeAPI("ItemLookupApi.getMusicAlbumRemoteSearchResults", "/Items/RemoteSearch/MusicAlbum",
                "POST", new ArrayList<>(), albumInfoRemoteSearchQuery, new LinkedHashMap<>(), new LinkedHashMap<>(),
                new LinkedHashMap<>(), localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType,
                false);
    }

    /**
     * Get music artist remote search.
     * 
     * @param artistInfoRemoteSearchQuery Remote search query. (required)
     * @return List&lt;RemoteSearchResult&gt;
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
     *                        <td>Music artist remote search executed.</td>
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
    public List<RemoteSearchResult> getMusicArtistRemoteSearchResults(
            @org.eclipse.jdt.annotation.Nullable ArtistInfoRemoteSearchQuery artistInfoRemoteSearchQuery)
            throws ApiException {
        return getMusicArtistRemoteSearchResultsWithHttpInfo(artistInfoRemoteSearchQuery).getData();
    }

    /**
     * Get music artist remote search.
     * 
     * @param artistInfoRemoteSearchQuery Remote search query. (required)
     * @return ApiResponse&lt;List&lt;RemoteSearchResult&gt;&gt;
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
     *                        <td>Music artist remote search executed.</td>
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
    public ApiResponse<List<RemoteSearchResult>> getMusicArtistRemoteSearchResultsWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable ArtistInfoRemoteSearchQuery artistInfoRemoteSearchQuery)
            throws ApiException {
        // Check required parameters
        if (artistInfoRemoteSearchQuery == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'artistInfoRemoteSearchQuery' when calling getMusicArtistRemoteSearchResults");
        }

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType("application/json", "text/json",
                "application/*+json");
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<List<RemoteSearchResult>> localVarReturnType = new GenericType<List<RemoteSearchResult>>() {
        };
        return apiClient.invokeAPI("ItemLookupApi.getMusicArtistRemoteSearchResults", "/Items/RemoteSearch/MusicArtist",
                "POST", new ArrayList<>(), artistInfoRemoteSearchQuery, new LinkedHashMap<>(), new LinkedHashMap<>(),
                new LinkedHashMap<>(), localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType,
                false);
    }

    /**
     * Get music video remote search.
     * 
     * @param musicVideoInfoRemoteSearchQuery Remote search query. (required)
     * @return List&lt;RemoteSearchResult&gt;
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
     *                        <td>Music video remote search executed.</td>
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
    public List<RemoteSearchResult> getMusicVideoRemoteSearchResults(
            @org.eclipse.jdt.annotation.Nullable MusicVideoInfoRemoteSearchQuery musicVideoInfoRemoteSearchQuery)
            throws ApiException {
        return getMusicVideoRemoteSearchResultsWithHttpInfo(musicVideoInfoRemoteSearchQuery).getData();
    }

    /**
     * Get music video remote search.
     * 
     * @param musicVideoInfoRemoteSearchQuery Remote search query. (required)
     * @return ApiResponse&lt;List&lt;RemoteSearchResult&gt;&gt;
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
     *                        <td>Music video remote search executed.</td>
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
    public ApiResponse<List<RemoteSearchResult>> getMusicVideoRemoteSearchResultsWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable MusicVideoInfoRemoteSearchQuery musicVideoInfoRemoteSearchQuery)
            throws ApiException {
        // Check required parameters
        if (musicVideoInfoRemoteSearchQuery == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'musicVideoInfoRemoteSearchQuery' when calling getMusicVideoRemoteSearchResults");
        }

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType("application/json", "text/json",
                "application/*+json");
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<List<RemoteSearchResult>> localVarReturnType = new GenericType<List<RemoteSearchResult>>() {
        };
        return apiClient.invokeAPI("ItemLookupApi.getMusicVideoRemoteSearchResults", "/Items/RemoteSearch/MusicVideo",
                "POST", new ArrayList<>(), musicVideoInfoRemoteSearchQuery, new LinkedHashMap<>(),
                new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType, false);
    }

    /**
     * Get person remote search.
     * 
     * @param personLookupInfoRemoteSearchQuery Remote search query. (required)
     * @return List&lt;RemoteSearchResult&gt;
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
     *                        <td>Person remote search executed.</td>
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
    public List<RemoteSearchResult> getPersonRemoteSearchResults(
            @org.eclipse.jdt.annotation.Nullable PersonLookupInfoRemoteSearchQuery personLookupInfoRemoteSearchQuery)
            throws ApiException {
        return getPersonRemoteSearchResultsWithHttpInfo(personLookupInfoRemoteSearchQuery).getData();
    }

    /**
     * Get person remote search.
     * 
     * @param personLookupInfoRemoteSearchQuery Remote search query. (required)
     * @return ApiResponse&lt;List&lt;RemoteSearchResult&gt;&gt;
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
     *                        <td>Person remote search executed.</td>
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
    public ApiResponse<List<RemoteSearchResult>> getPersonRemoteSearchResultsWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable PersonLookupInfoRemoteSearchQuery personLookupInfoRemoteSearchQuery)
            throws ApiException {
        // Check required parameters
        if (personLookupInfoRemoteSearchQuery == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'personLookupInfoRemoteSearchQuery' when calling getPersonRemoteSearchResults");
        }

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType("application/json", "text/json",
                "application/*+json");
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<List<RemoteSearchResult>> localVarReturnType = new GenericType<List<RemoteSearchResult>>() {
        };
        return apiClient.invokeAPI("ItemLookupApi.getPersonRemoteSearchResults", "/Items/RemoteSearch/Person", "POST",
                new ArrayList<>(), personLookupInfoRemoteSearchQuery, new LinkedHashMap<>(), new LinkedHashMap<>(),
                new LinkedHashMap<>(), localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType,
                false);
    }

    /**
     * Get series remote search.
     * 
     * @param seriesInfoRemoteSearchQuery Remote search query. (required)
     * @return List&lt;RemoteSearchResult&gt;
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
     *                        <td>Series remote search executed.</td>
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
    public List<RemoteSearchResult> getSeriesRemoteSearchResults(
            @org.eclipse.jdt.annotation.Nullable SeriesInfoRemoteSearchQuery seriesInfoRemoteSearchQuery)
            throws ApiException {
        return getSeriesRemoteSearchResultsWithHttpInfo(seriesInfoRemoteSearchQuery).getData();
    }

    /**
     * Get series remote search.
     * 
     * @param seriesInfoRemoteSearchQuery Remote search query. (required)
     * @return ApiResponse&lt;List&lt;RemoteSearchResult&gt;&gt;
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
     *                        <td>Series remote search executed.</td>
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
    public ApiResponse<List<RemoteSearchResult>> getSeriesRemoteSearchResultsWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable SeriesInfoRemoteSearchQuery seriesInfoRemoteSearchQuery)
            throws ApiException {
        // Check required parameters
        if (seriesInfoRemoteSearchQuery == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'seriesInfoRemoteSearchQuery' when calling getSeriesRemoteSearchResults");
        }

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType("application/json", "text/json",
                "application/*+json");
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<List<RemoteSearchResult>> localVarReturnType = new GenericType<List<RemoteSearchResult>>() {
        };
        return apiClient.invokeAPI("ItemLookupApi.getSeriesRemoteSearchResults", "/Items/RemoteSearch/Series", "POST",
                new ArrayList<>(), seriesInfoRemoteSearchQuery, new LinkedHashMap<>(), new LinkedHashMap<>(),
                new LinkedHashMap<>(), localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType,
                false);
    }

    /**
     * Get trailer remote search.
     * 
     * @param trailerInfoRemoteSearchQuery Remote search query. (required)
     * @return List&lt;RemoteSearchResult&gt;
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
     *                        <td>Trailer remote search executed.</td>
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
    public List<RemoteSearchResult> getTrailerRemoteSearchResults(
            @org.eclipse.jdt.annotation.Nullable TrailerInfoRemoteSearchQuery trailerInfoRemoteSearchQuery)
            throws ApiException {
        return getTrailerRemoteSearchResultsWithHttpInfo(trailerInfoRemoteSearchQuery).getData();
    }

    /**
     * Get trailer remote search.
     * 
     * @param trailerInfoRemoteSearchQuery Remote search query. (required)
     * @return ApiResponse&lt;List&lt;RemoteSearchResult&gt;&gt;
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
     *                        <td>Trailer remote search executed.</td>
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
    public ApiResponse<List<RemoteSearchResult>> getTrailerRemoteSearchResultsWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable TrailerInfoRemoteSearchQuery trailerInfoRemoteSearchQuery)
            throws ApiException {
        // Check required parameters
        if (trailerInfoRemoteSearchQuery == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'trailerInfoRemoteSearchQuery' when calling getTrailerRemoteSearchResults");
        }

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType("application/json", "text/json",
                "application/*+json");
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<List<RemoteSearchResult>> localVarReturnType = new GenericType<List<RemoteSearchResult>>() {
        };
        return apiClient.invokeAPI("ItemLookupApi.getTrailerRemoteSearchResults", "/Items/RemoteSearch/Trailer", "POST",
                new ArrayList<>(), trailerInfoRemoteSearchQuery, new LinkedHashMap<>(), new LinkedHashMap<>(),
                new LinkedHashMap<>(), localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType,
                false);
    }
}
