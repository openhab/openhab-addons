package org.openhab.binding.jellyfin.internal.api.generated.current;

import java.io.File;
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

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class TrickplayApi {
    private ApiClient apiClient;

    public TrickplayApi() {
        this(Configuration.getDefaultApiClient());
    }

    public TrickplayApi(ApiClient apiClient) {
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
     * Gets an image tiles playlist for trickplay.
     * 
     * @param itemId The item id. (required)
     * @param width The width of a single tile. (required)
     * @param mediaSourceId The media version id, if using an alternate version. (optional)
     * @return File
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
     *                        <td>Tiles playlist returned.</td>
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
    public File getTrickplayHlsPlaylist(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.NonNull UUID mediaSourceId)
            throws ApiException {
        return getTrickplayHlsPlaylistWithHttpInfo(itemId, width, mediaSourceId).getData();
    }

    /**
     * Gets an image tiles playlist for trickplay.
     * 
     * @param itemId The item id. (required)
     * @param width The width of a single tile. (required)
     * @param mediaSourceId The media version id, if using an alternate version. (optional)
     * @return ApiResponse&lt;File&gt;
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
     *                        <td>Tiles playlist returned.</td>
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
    public ApiResponse<File> getTrickplayHlsPlaylistWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.NonNull UUID mediaSourceId)
            throws ApiException {
        // Check required parameters
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling getTrickplayHlsPlaylist");
        }
        if (width == null) {
            throw new ApiException(400, "Missing the required parameter 'width' when calling getTrickplayHlsPlaylist");
        }

        // Path parameters
        String localVarPath = "/Videos/{itemId}/Trickplay/{width}/tiles.m3u8"
                .replaceAll("\\{itemId}", apiClient.escapeString(itemId.toString()))
                .replaceAll("\\{width}", apiClient.escapeString(width.toString()));

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(
                apiClient.parameterToPairs("", "mediaSourceId", mediaSourceId));

        String localVarAccept = apiClient.selectHeaderAccept("application/x-mpegURL", "application/json",
                "application/json; profile=CamelCase", "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<File> localVarReturnType = new GenericType<File>() {
        };
        return apiClient.invokeAPI("TrickplayApi.getTrickplayHlsPlaylist", localVarPath, "GET", localVarQueryParams,
                null, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, localVarReturnType, false);
    }

    /**
     * Gets a trickplay tile image.
     * 
     * @param itemId The item id. (required)
     * @param width The width of a single tile. (required)
     * @param index The index of the desired tile. (required)
     * @param mediaSourceId The media version id, if using an alternate version. (optional)
     * @return File
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
     *                        <td>Tile image not found at specified index.</td>
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
    public File getTrickplayTileImage(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer index,
            @org.eclipse.jdt.annotation.NonNull UUID mediaSourceId) throws ApiException {
        return getTrickplayTileImageWithHttpInfo(itemId, width, index, mediaSourceId).getData();
    }

    /**
     * Gets a trickplay tile image.
     * 
     * @param itemId The item id. (required)
     * @param width The width of a single tile. (required)
     * @param index The index of the desired tile. (required)
     * @param mediaSourceId The media version id, if using an alternate version. (optional)
     * @return ApiResponse&lt;File&gt;
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
     *                        <td>Tile image not found at specified index.</td>
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
    public ApiResponse<File> getTrickplayTileImageWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer index,
            @org.eclipse.jdt.annotation.NonNull UUID mediaSourceId) throws ApiException {
        // Check required parameters
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling getTrickplayTileImage");
        }
        if (width == null) {
            throw new ApiException(400, "Missing the required parameter 'width' when calling getTrickplayTileImage");
        }
        if (index == null) {
            throw new ApiException(400, "Missing the required parameter 'index' when calling getTrickplayTileImage");
        }

        // Path parameters
        String localVarPath = "/Videos/{itemId}/Trickplay/{width}/{index}.jpg"
                .replaceAll("\\{itemId}", apiClient.escapeString(itemId.toString()))
                .replaceAll("\\{width}", apiClient.escapeString(width.toString()))
                .replaceAll("\\{index}", apiClient.escapeString(index.toString()));

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(
                apiClient.parameterToPairs("", "mediaSourceId", mediaSourceId));

        String localVarAccept = apiClient.selectHeaderAccept("image/*", "application/json",
                "application/json; profile=CamelCase", "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<File> localVarReturnType = new GenericType<File>() {
        };
        return apiClient.invokeAPI("TrickplayApi.getTrickplayTileImage", localVarPath, "GET", localVarQueryParams, null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, localVarReturnType, false);
    }
}
