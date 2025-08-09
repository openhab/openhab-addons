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
import org.openhab.binding.jellyfin.internal.api.generated.current.model.QuickConnectResult;

public class QuickConnectApi {
    private ApiClient apiClient;

    public QuickConnectApi() {
        this(Configuration.getDefaultApiClient());
    }

    public QuickConnectApi(ApiClient apiClient) {
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
     * Authorizes a pending quick connect request.
     * 
     * @param code Quick connect code to authorize. (required)
     * @param userId The user the authorize. Access to the requested user is required. (optional)
     * @return Boolean
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
     *                        <td>Quick connect result authorized successfully.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Unknown user id.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public Boolean authorizeQuickConnect(@org.eclipse.jdt.annotation.Nullable String code,
            @org.eclipse.jdt.annotation.NonNull UUID userId) throws ApiException {
        return authorizeQuickConnectWithHttpInfo(code, userId).getData();
    }

    /**
     * Authorizes a pending quick connect request.
     * 
     * @param code Quick connect code to authorize. (required)
     * @param userId The user the authorize. Access to the requested user is required. (optional)
     * @return ApiResponse&lt;Boolean&gt;
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
     *                        <td>Quick connect result authorized successfully.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Unknown user id.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<Boolean> authorizeQuickConnectWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String code,
            @org.eclipse.jdt.annotation.NonNull UUID userId) throws ApiException {
        // Check required parameters
        if (code == null) {
            throw new ApiException(400, "Missing the required parameter 'code' when calling authorizeQuickConnect");
        }

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "code", code));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "userId", userId));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<Boolean> localVarReturnType = new GenericType<Boolean>() {
        };
        return apiClient.invokeAPI("QuickConnectApi.authorizeQuickConnect", "/QuickConnect/Authorize", "POST",
                localVarQueryParams, null, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(),
                localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType, false);
    }

    /**
     * Gets the current quick connect state.
     * 
     * @return Boolean
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
     *                        <td>Quick connect state returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public Boolean getQuickConnectEnabled() throws ApiException {
        return getQuickConnectEnabledWithHttpInfo().getData();
    }

    /**
     * Gets the current quick connect state.
     * 
     * @return ApiResponse&lt;Boolean&gt;
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
     *                        <td>Quick connect state returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<Boolean> getQuickConnectEnabledWithHttpInfo() throws ApiException {
        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        GenericType<Boolean> localVarReturnType = new GenericType<Boolean>() {
        };
        return apiClient.invokeAPI("QuickConnectApi.getQuickConnectEnabled", "/QuickConnect/Enabled", "GET",
                new ArrayList<>(), null, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(),
                localVarAccept, localVarContentType, null, localVarReturnType, false);
    }

    /**
     * Attempts to retrieve authentication information.
     * 
     * @param secret Secret previously returned from the Initiate endpoint. (required)
     * @return QuickConnectResult
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
     *                        <td>Quick connect result returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Unknown quick connect secret.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public QuickConnectResult getQuickConnectState(@org.eclipse.jdt.annotation.Nullable String secret)
            throws ApiException {
        return getQuickConnectStateWithHttpInfo(secret).getData();
    }

    /**
     * Attempts to retrieve authentication information.
     * 
     * @param secret Secret previously returned from the Initiate endpoint. (required)
     * @return ApiResponse&lt;QuickConnectResult&gt;
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
     *                        <td>Quick connect result returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Unknown quick connect secret.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<QuickConnectResult> getQuickConnectStateWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable String secret) throws ApiException {
        // Check required parameters
        if (secret == null) {
            throw new ApiException(400, "Missing the required parameter 'secret' when calling getQuickConnectState");
        }

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "secret", secret));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        GenericType<QuickConnectResult> localVarReturnType = new GenericType<QuickConnectResult>() {
        };
        return apiClient.invokeAPI("QuickConnectApi.getQuickConnectState", "/QuickConnect/Connect", "GET",
                localVarQueryParams, null, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(),
                localVarAccept, localVarContentType, null, localVarReturnType, false);
    }

    /**
     * Initiate a new quick connect request.
     * 
     * @return QuickConnectResult
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
     *                        <td>Quick connect request successfully created.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Quick connect is not active on this server.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public QuickConnectResult initiateQuickConnect() throws ApiException {
        return initiateQuickConnectWithHttpInfo().getData();
    }

    /**
     * Initiate a new quick connect request.
     * 
     * @return ApiResponse&lt;QuickConnectResult&gt;
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
     *                        <td>Quick connect request successfully created.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Quick connect is not active on this server.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<QuickConnectResult> initiateQuickConnectWithHttpInfo() throws ApiException {
        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        GenericType<QuickConnectResult> localVarReturnType = new GenericType<QuickConnectResult>() {
        };
        return apiClient.invokeAPI("QuickConnectApi.initiateQuickConnect", "/QuickConnect/Initiate", "POST",
                new ArrayList<>(), null, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(),
                localVarAccept, localVarContentType, null, localVarReturnType, false);
    }
}
