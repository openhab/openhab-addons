package org.openhab.binding.jellyfin.internal.api.generated.current;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javax.ws.rs.core.GenericType;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;
import org.openhab.binding.jellyfin.internal.api.generated.ApiException;
import org.openhab.binding.jellyfin.internal.api.generated.ApiResponse;
import org.openhab.binding.jellyfin.internal.api.generated.Configuration;
import org.openhab.binding.jellyfin.internal.api.generated.Pair;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.ActivityLogEntryQueryResult;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class ActivityLogApi {
    private ApiClient apiClient;

    public ActivityLogApi() {
        this(Configuration.getDefaultApiClient());
    }

    public ActivityLogApi(ApiClient apiClient) {
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
     * Gets activity log entries.
     * 
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param minDate Optional. The minimum date. Format &#x3D; ISO. (optional)
     * @param hasUserId Optional. Filter log entries if it has user id, or not. (optional)
     * @return ActivityLogEntryQueryResult
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
     *                        <td>Activity log returned.</td>
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
    public ActivityLogEntryQueryResult getLogEntries(@org.eclipse.jdt.annotation.NonNull Integer startIndex,
            @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull OffsetDateTime minDate,
            @org.eclipse.jdt.annotation.NonNull Boolean hasUserId) throws ApiException {
        return getLogEntriesWithHttpInfo(startIndex, limit, minDate, hasUserId).getData();
    }

    /**
     * Gets activity log entries.
     * 
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results. (optional)
     * @param limit Optional. The maximum number of records to return. (optional)
     * @param minDate Optional. The minimum date. Format &#x3D; ISO. (optional)
     * @param hasUserId Optional. Filter log entries if it has user id, or not. (optional)
     * @return ApiResponse&lt;ActivityLogEntryQueryResult&gt;
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
     *                        <td>Activity log returned.</td>
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
    public ApiResponse<ActivityLogEntryQueryResult> getLogEntriesWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull Integer startIndex, @org.eclipse.jdt.annotation.NonNull Integer limit,
            @org.eclipse.jdt.annotation.NonNull OffsetDateTime minDate,
            @org.eclipse.jdt.annotation.NonNull Boolean hasUserId) throws ApiException {
        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "startIndex", startIndex));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "minDate", minDate));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "hasUserId", hasUserId));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<ActivityLogEntryQueryResult> localVarReturnType = new GenericType<ActivityLogEntryQueryResult>() {
        };
        return apiClient.invokeAPI("ActivityLogApi.getLogEntries", "/System/ActivityLog/Entries", "GET",
                localVarQueryParams, null, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(),
                localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType, false);
    }
}
