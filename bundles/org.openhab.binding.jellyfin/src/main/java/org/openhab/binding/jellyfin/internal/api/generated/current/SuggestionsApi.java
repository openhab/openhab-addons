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
import org.openhab.binding.jellyfin.internal.api.generated.current.model.BaseItemDtoQueryResult;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.BaseItemKind;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.MediaType;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class SuggestionsApi {
    private ApiClient apiClient;

    public SuggestionsApi() {
        this(Configuration.getDefaultApiClient());
    }

    public SuggestionsApi(ApiClient apiClient) {
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
     * Gets suggestions.
     * 
     * @param userId The user id. (optional)
     * @param mediaType The media types. (optional)
     * @param type The type. (optional)
     * @param startIndex Optional. The start index. (optional)
     * @param limit Optional. The limit. (optional)
     * @param enableTotalRecordCount Whether to enable the total record count. (optional, default to false)
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
     *                        <td>Suggestions returned.</td>
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
    public BaseItemDtoQueryResult getSuggestions(@org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull List<MediaType> mediaType,
            @org.eclipse.jdt.annotation.NonNull List<BaseItemKind> type,
            @org.eclipse.jdt.annotation.NonNull Integer startIndex, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull Boolean enableTotalRecordCount) throws ApiException {
        return getSuggestionsWithHttpInfo(userId, mediaType, type, startIndex, limit, enableTotalRecordCount).getData();
    }

    /**
     * Gets suggestions.
     * 
     * @param userId The user id. (optional)
     * @param mediaType The media types. (optional)
     * @param type The type. (optional)
     * @param startIndex Optional. The start index. (optional)
     * @param limit Optional. The limit. (optional)
     * @param enableTotalRecordCount Whether to enable the total record count. (optional, default to false)
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
     *                        <td>Suggestions returned.</td>
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
    public ApiResponse<BaseItemDtoQueryResult> getSuggestionsWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull List<MediaType> mediaType,
            @org.eclipse.jdt.annotation.NonNull List<BaseItemKind> type,
            @org.eclipse.jdt.annotation.NonNull Integer startIndex, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull Boolean enableTotalRecordCount) throws ApiException {
        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "userId", userId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "mediaType", mediaType));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "type", type));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "startIndex", startIndex));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "enableTotalRecordCount", enableTotalRecordCount));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<BaseItemDtoQueryResult> localVarReturnType = new GenericType<BaseItemDtoQueryResult>() {
        };
        return apiClient.invokeAPI("SuggestionsApi.getSuggestions", "/Items/Suggestions", "GET", localVarQueryParams,
                null, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, localVarReturnType, false);
    }
}
