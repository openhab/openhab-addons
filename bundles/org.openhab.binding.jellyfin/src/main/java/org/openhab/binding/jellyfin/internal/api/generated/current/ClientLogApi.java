package org.openhab.binding.jellyfin.internal.api.generated.current;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import javax.ws.rs.core.GenericType;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;
import org.openhab.binding.jellyfin.internal.api.generated.ApiException;
import org.openhab.binding.jellyfin.internal.api.generated.ApiResponse;
import org.openhab.binding.jellyfin.internal.api.generated.Configuration;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.ClientLogDocumentResponseDto;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class ClientLogApi {
    private ApiClient apiClient;

    public ClientLogApi() {
        this(Configuration.getDefaultApiClient());
    }

    public ClientLogApi(ApiClient apiClient) {
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
     * Upload a document.
     * 
     * @param body (optional)
     * @return ClientLogDocumentResponseDto
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
     *                        <td>Document saved.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Event logging disabled.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>413</td>
     *                        <td>Upload size too large.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ClientLogDocumentResponseDto logFile(@org.eclipse.jdt.annotation.NonNull File body) throws ApiException {
        return logFileWithHttpInfo(body).getData();
    }

    /**
     * Upload a document.
     * 
     * @param body (optional)
     * @return ApiResponse&lt;ClientLogDocumentResponseDto&gt;
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
     *                        <td>Document saved.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Event logging disabled.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>413</td>
     *                        <td>Upload size too large.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<ClientLogDocumentResponseDto> logFileWithHttpInfo(@org.eclipse.jdt.annotation.NonNull File body)
            throws ApiException {
        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType("text/plain");
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<ClientLogDocumentResponseDto> localVarReturnType = new GenericType<ClientLogDocumentResponseDto>() {
        };
        return apiClient.invokeAPI("ClientLogApi.logFile", "/ClientLog/Document", "POST", new ArrayList<>(), body,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, localVarReturnType, false);
    }
}
