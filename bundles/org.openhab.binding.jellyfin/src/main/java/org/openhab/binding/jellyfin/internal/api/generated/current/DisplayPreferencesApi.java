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
import org.openhab.binding.jellyfin.internal.api.generated.current.model.DisplayPreferencesDto;

import jakarta.ws.rs.core.GenericType;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class DisplayPreferencesApi {
    private ApiClient apiClient;

    public DisplayPreferencesApi() {
        this(Configuration.getDefaultApiClient());
    }

    public DisplayPreferencesApi(ApiClient apiClient) {
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
     * Get Display Preferences.
     * 
     * @param displayPreferencesId Display preferences id. (required)
     * @param client Client. (required)
     * @param userId User id. (optional)
     * @return DisplayPreferencesDto
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
     *                        <td>Display preferences retrieved.</td>
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
    public DisplayPreferencesDto getDisplayPreferences(@org.eclipse.jdt.annotation.Nullable String displayPreferencesId,
            @org.eclipse.jdt.annotation.Nullable String client, @org.eclipse.jdt.annotation.NonNull UUID userId)
            throws ApiException {
        return getDisplayPreferencesWithHttpInfo(displayPreferencesId, client, userId).getData();
    }

    /**
     * Get Display Preferences.
     * 
     * @param displayPreferencesId Display preferences id. (required)
     * @param client Client. (required)
     * @param userId User id. (optional)
     * @return ApiResponse&lt;DisplayPreferencesDto&gt;
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
     *                        <td>Display preferences retrieved.</td>
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
    public ApiResponse<DisplayPreferencesDto> getDisplayPreferencesWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable String displayPreferencesId,
            @org.eclipse.jdt.annotation.Nullable String client, @org.eclipse.jdt.annotation.NonNull UUID userId)
            throws ApiException {
        // Check required parameters
        if (displayPreferencesId == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'displayPreferencesId' when calling getDisplayPreferences");
        }
        if (client == null) {
            throw new ApiException(400, "Missing the required parameter 'client' when calling getDisplayPreferences");
        }

        // Path parameters
        String localVarPath = "/DisplayPreferences/{displayPreferencesId}".replaceAll("\\{displayPreferencesId}",
                apiClient.escapeString(displayPreferencesId.toString()));

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "userId", userId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "client", client));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<DisplayPreferencesDto> localVarReturnType = new GenericType<DisplayPreferencesDto>() {
        };
        return apiClient.invokeAPI("DisplayPreferencesApi.getDisplayPreferences", localVarPath, "GET",
                localVarQueryParams, null, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(),
                localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType, false);
    }

    /**
     * Update Display Preferences.
     * 
     * @param displayPreferencesId Display preferences id. (required)
     * @param client Client. (required)
     * @param displayPreferencesDto New Display Preferences object. (required)
     * @param userId User Id. (optional)
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
     *                        <td>Display preferences updated.</td>
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
    public void updateDisplayPreferences(@org.eclipse.jdt.annotation.Nullable String displayPreferencesId,
            @org.eclipse.jdt.annotation.Nullable String client,
            @org.eclipse.jdt.annotation.Nullable DisplayPreferencesDto displayPreferencesDto,
            @org.eclipse.jdt.annotation.NonNull UUID userId) throws ApiException {
        updateDisplayPreferencesWithHttpInfo(displayPreferencesId, client, displayPreferencesDto, userId);
    }

    /**
     * Update Display Preferences.
     * 
     * @param displayPreferencesId Display preferences id. (required)
     * @param client Client. (required)
     * @param displayPreferencesDto New Display Preferences object. (required)
     * @param userId User Id. (optional)
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
     *                        <td>Display preferences updated.</td>
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
    public ApiResponse<Void> updateDisplayPreferencesWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable String displayPreferencesId,
            @org.eclipse.jdt.annotation.Nullable String client,
            @org.eclipse.jdt.annotation.Nullable DisplayPreferencesDto displayPreferencesDto,
            @org.eclipse.jdt.annotation.NonNull UUID userId) throws ApiException {
        // Check required parameters
        if (displayPreferencesId == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'displayPreferencesId' when calling updateDisplayPreferences");
        }
        if (client == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'client' when calling updateDisplayPreferences");
        }
        if (displayPreferencesDto == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'displayPreferencesDto' when calling updateDisplayPreferences");
        }

        // Path parameters
        String localVarPath = "/DisplayPreferences/{displayPreferencesId}".replaceAll("\\{displayPreferencesId}",
                apiClient.escapeString(displayPreferencesId.toString()));

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "userId", userId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "client", client));

        String localVarAccept = apiClient.selectHeaderAccept();
        String localVarContentType = apiClient.selectHeaderContentType("application/json", "text/json",
                "application/*+json");
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        return apiClient.invokeAPI("DisplayPreferencesApi.updateDisplayPreferences", localVarPath, "POST",
                localVarQueryParams, displayPreferencesDto, new LinkedHashMap<>(), new LinkedHashMap<>(),
                new LinkedHashMap<>(), localVarAccept, localVarContentType, localVarAuthNames, null, false);
    }
}
