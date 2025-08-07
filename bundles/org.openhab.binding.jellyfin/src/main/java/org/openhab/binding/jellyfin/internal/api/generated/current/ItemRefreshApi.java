package org.openhab.binding.jellyfin.internal.api.generated.current;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;
import org.openhab.binding.jellyfin.internal.api.generated.ApiException;
import org.openhab.binding.jellyfin.internal.api.generated.ApiResponse;
import org.openhab.binding.jellyfin.internal.api.generated.Configuration;
import org.openhab.binding.jellyfin.internal.api.generated.Pair;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.MetadataRefreshMode;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class ItemRefreshApi {
    private ApiClient apiClient;

    public ItemRefreshApi() {
        this(Configuration.getDefaultApiClient());
    }

    public ItemRefreshApi(ApiClient apiClient) {
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
     * Refreshes metadata for an item.
     * 
     * @param itemId Item id. (required)
     * @param metadataRefreshMode (Optional) Specifies the metadata refresh mode. (optional, default to None)
     * @param imageRefreshMode (Optional) Specifies the image refresh mode. (optional, default to None)
     * @param replaceAllMetadata (Optional) Determines if metadata should be replaced. Only applicable if mode is
     *            FullRefresh. (optional, default to false)
     * @param replaceAllImages (Optional) Determines if images should be replaced. Only applicable if mode is
     *            FullRefresh. (optional, default to false)
     * @param regenerateTrickplay (Optional) Determines if trickplay images should be replaced. Only applicable if mode
     *            is FullRefresh. (optional, default to false)
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
     *                        <td>Item metadata refresh queued.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Item to refresh not found.</td>
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
    public void refreshItem(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull MetadataRefreshMode metadataRefreshMode,
            @org.eclipse.jdt.annotation.NonNull MetadataRefreshMode imageRefreshMode,
            @org.eclipse.jdt.annotation.NonNull Boolean replaceAllMetadata,
            @org.eclipse.jdt.annotation.NonNull Boolean replaceAllImages,
            @org.eclipse.jdt.annotation.NonNull Boolean regenerateTrickplay) throws ApiException {
        refreshItemWithHttpInfo(itemId, metadataRefreshMode, imageRefreshMode, replaceAllMetadata, replaceAllImages,
                regenerateTrickplay);
    }

    /**
     * Refreshes metadata for an item.
     * 
     * @param itemId Item id. (required)
     * @param metadataRefreshMode (Optional) Specifies the metadata refresh mode. (optional, default to None)
     * @param imageRefreshMode (Optional) Specifies the image refresh mode. (optional, default to None)
     * @param replaceAllMetadata (Optional) Determines if metadata should be replaced. Only applicable if mode is
     *            FullRefresh. (optional, default to false)
     * @param replaceAllImages (Optional) Determines if images should be replaced. Only applicable if mode is
     *            FullRefresh. (optional, default to false)
     * @param regenerateTrickplay (Optional) Determines if trickplay images should be replaced. Only applicable if mode
     *            is FullRefresh. (optional, default to false)
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
     *                        <td>Item metadata refresh queued.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Item to refresh not found.</td>
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
    public ApiResponse<Void> refreshItemWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull MetadataRefreshMode metadataRefreshMode,
            @org.eclipse.jdt.annotation.NonNull MetadataRefreshMode imageRefreshMode,
            @org.eclipse.jdt.annotation.NonNull Boolean replaceAllMetadata,
            @org.eclipse.jdt.annotation.NonNull Boolean replaceAllImages,
            @org.eclipse.jdt.annotation.NonNull Boolean regenerateTrickplay) throws ApiException {
        // Check required parameters
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling refreshItem");
        }

        // Path parameters
        String localVarPath = "/Items/{itemId}/Refresh".replaceAll("\\{itemId}",
                apiClient.escapeString(itemId.toString()));

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(
                apiClient.parameterToPairs("", "metadataRefreshMode", metadataRefreshMode));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "imageRefreshMode", imageRefreshMode));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "replaceAllMetadata", replaceAllMetadata));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "replaceAllImages", replaceAllImages));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "regenerateTrickplay", regenerateTrickplay));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        return apiClient.invokeAPI("ItemRefreshApi.refreshItem", localVarPath, "POST", localVarQueryParams, null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, null, false);
    }
}
