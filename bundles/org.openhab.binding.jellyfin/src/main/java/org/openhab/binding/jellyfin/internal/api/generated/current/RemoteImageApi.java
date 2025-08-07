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
import org.openhab.binding.jellyfin.internal.api.generated.current.model.ImageProviderInfo;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.ImageType;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.RemoteImageResult;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class RemoteImageApi {
    private ApiClient apiClient;

    public RemoteImageApi() {
        this(Configuration.getDefaultApiClient());
    }

    public RemoteImageApi(ApiClient apiClient) {
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
     * Downloads a remote image for an item.
     * 
     * @param itemId Item Id. (required)
     * @param type The image type. (required)
     * @param imageUrl The image url. (optional)
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
     *                        <td>Remote image downloaded.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Remote image not found.</td>
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
    public void downloadRemoteImage(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable ImageType type, @org.eclipse.jdt.annotation.NonNull String imageUrl)
            throws ApiException {
        downloadRemoteImageWithHttpInfo(itemId, type, imageUrl);
    }

    /**
     * Downloads a remote image for an item.
     * 
     * @param itemId Item Id. (required)
     * @param type The image type. (required)
     * @param imageUrl The image url. (optional)
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
     *                        <td>Remote image downloaded.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Remote image not found.</td>
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
    public ApiResponse<Void> downloadRemoteImageWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable ImageType type, @org.eclipse.jdt.annotation.NonNull String imageUrl)
            throws ApiException {
        // Check required parameters
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling downloadRemoteImage");
        }
        if (type == null) {
            throw new ApiException(400, "Missing the required parameter 'type' when calling downloadRemoteImage");
        }

        // Path parameters
        String localVarPath = "/Items/{itemId}/RemoteImages/Download".replaceAll("\\{itemId}",
                apiClient.escapeString(itemId.toString()));

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "type", type));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "imageUrl", imageUrl));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        return apiClient.invokeAPI("RemoteImageApi.downloadRemoteImage", localVarPath, "POST", localVarQueryParams,
                null, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, null, false);
    }

    /**
     * Gets available remote image providers for an item.
     * 
     * @param itemId Item Id. (required)
     * @return List&lt;ImageProviderInfo&gt;
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
     *                        <td>Returned remote image providers.</td>
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
    public List<ImageProviderInfo> getRemoteImageProviders(@org.eclipse.jdt.annotation.Nullable UUID itemId)
            throws ApiException {
        return getRemoteImageProvidersWithHttpInfo(itemId).getData();
    }

    /**
     * Gets available remote image providers for an item.
     * 
     * @param itemId Item Id. (required)
     * @return ApiResponse&lt;List&lt;ImageProviderInfo&gt;&gt;
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
     *                        <td>Returned remote image providers.</td>
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
    public ApiResponse<List<ImageProviderInfo>> getRemoteImageProvidersWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable UUID itemId) throws ApiException {
        // Check required parameters
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling getRemoteImageProviders");
        }

        // Path parameters
        String localVarPath = "/Items/{itemId}/RemoteImages/Providers".replaceAll("\\{itemId}",
                apiClient.escapeString(itemId.toString()));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<List<ImageProviderInfo>> localVarReturnType = new GenericType<List<ImageProviderInfo>>() {
        };
        return apiClient.invokeAPI("RemoteImageApi.getRemoteImageProviders", localVarPath, "GET", new ArrayList<>(),
                null, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, localVarReturnType, false);
    }

    /**
     * Gets available remote images for an item.
     * 
     * @param itemId Item Id. (required)
     * @param type The image type. (optional)
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param providerName Optional. The image provider to use. (optional)
     * @param includeAllLanguages Optional. Include all languages. (optional, default to false)
     * @return RemoteImageResult
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
     *                        <td>Remote Images returned.</td>
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
    public RemoteImageResult getRemoteImages(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull ImageType type, @org.eclipse.jdt.annotation.NonNull Integer startIndex,
            @org.eclipse.jdt.annotation.NonNull Integer limit, @org.eclipse.jdt.annotation.NonNull String providerName,
            @org.eclipse.jdt.annotation.NonNull Boolean includeAllLanguages) throws ApiException {
        return getRemoteImagesWithHttpInfo(itemId, type, startIndex, limit, providerName, includeAllLanguages)
                .getData();
    }

    /**
     * Gets available remote images for an item.
     * 
     * @param itemId Item Id. (required)
     * @param type The image type. (optional)
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param providerName Optional. The image provider to use. (optional)
     * @param includeAllLanguages Optional. Include all languages. (optional, default to false)
     * @return ApiResponse&lt;RemoteImageResult&gt;
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
     *                        <td>Remote Images returned.</td>
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
    public ApiResponse<RemoteImageResult> getRemoteImagesWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull ImageType type, @org.eclipse.jdt.annotation.NonNull Integer startIndex,
            @org.eclipse.jdt.annotation.NonNull Integer limit, @org.eclipse.jdt.annotation.NonNull String providerName,
            @org.eclipse.jdt.annotation.NonNull Boolean includeAllLanguages) throws ApiException {
        // Check required parameters
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling getRemoteImages");
        }

        // Path parameters
        String localVarPath = "/Items/{itemId}/RemoteImages".replaceAll("\\{itemId}",
                apiClient.escapeString(itemId.toString()));

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "type", type));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "startIndex", startIndex));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "providerName", providerName));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "includeAllLanguages", includeAllLanguages));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<RemoteImageResult> localVarReturnType = new GenericType<RemoteImageResult>() {
        };
        return apiClient.invokeAPI("RemoteImageApi.getRemoteImages", localVarPath, "GET", localVarQueryParams, null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, localVarReturnType, false);
    }
}
