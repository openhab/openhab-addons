package org.openhab.binding.jellyfin.internal.api.generated.current;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import javax.ws.rs.core.GenericType;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;
import org.openhab.binding.jellyfin.internal.api.generated.ApiException;
import org.openhab.binding.jellyfin.internal.api.generated.ApiResponse;
import org.openhab.binding.jellyfin.internal.api.generated.Configuration;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.BrandingOptions;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class BrandingApi {
    private ApiClient apiClient;

    public BrandingApi() {
        this(Configuration.getDefaultApiClient());
    }

    public BrandingApi(ApiClient apiClient) {
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
     * Gets branding css.
     * 
     * @return String
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
     *                        <td>Branding css returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>204</td>
     *                        <td>No branding css configured.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public String getBrandingCss() throws ApiException {
        return getBrandingCssWithHttpInfo().getData();
    }

    /**
     * Gets branding css.
     * 
     * @return ApiResponse&lt;String&gt;
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
     *                        <td>Branding css returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>204</td>
     *                        <td>No branding css configured.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<String> getBrandingCssWithHttpInfo() throws ApiException {
        String localVarAccept = apiClient.selectHeaderAccept("text/css", "application/json",
                "application/json; profile=CamelCase", "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        GenericType<String> localVarReturnType = new GenericType<String>() {
        };
        return apiClient.invokeAPI("BrandingApi.getBrandingCss", "/Branding/Css", "GET", new ArrayList<>(), null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, null, localVarReturnType, false);
    }

    /**
     * Gets branding css.
     * 
     * @return String
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
     *                        <td>Branding css returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>204</td>
     *                        <td>No branding css configured.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public String getBrandingCss2() throws ApiException {
        return getBrandingCss2WithHttpInfo().getData();
    }

    /**
     * Gets branding css.
     * 
     * @return ApiResponse&lt;String&gt;
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
     *                        <td>Branding css returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>204</td>
     *                        <td>No branding css configured.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<String> getBrandingCss2WithHttpInfo() throws ApiException {
        String localVarAccept = apiClient.selectHeaderAccept("text/css", "application/json",
                "application/json; profile=CamelCase", "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        GenericType<String> localVarReturnType = new GenericType<String>() {
        };
        return apiClient.invokeAPI("BrandingApi.getBrandingCss2", "/Branding/Css.css", "GET", new ArrayList<>(), null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, null, localVarReturnType, false);
    }

    /**
     * Gets branding configuration.
     * 
     * @return BrandingOptions
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
     *                        <td>Branding configuration returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public BrandingOptions getBrandingOptions() throws ApiException {
        return getBrandingOptionsWithHttpInfo().getData();
    }

    /**
     * Gets branding configuration.
     * 
     * @return ApiResponse&lt;BrandingOptions&gt;
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
     *                        <td>Branding configuration returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<BrandingOptions> getBrandingOptionsWithHttpInfo() throws ApiException {
        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        GenericType<BrandingOptions> localVarReturnType = new GenericType<BrandingOptions>() {
        };
        return apiClient.invokeAPI("BrandingApi.getBrandingOptions", "/Branding/Configuration", "GET",
                new ArrayList<>(), null, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(),
                localVarAccept, localVarContentType, null, localVarReturnType, false);
    }
}
