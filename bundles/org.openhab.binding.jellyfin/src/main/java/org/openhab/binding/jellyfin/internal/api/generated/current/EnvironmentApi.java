package org.openhab.binding.jellyfin.internal.api.generated.current;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javax.ws.rs.core.GenericType;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;
import org.openhab.binding.jellyfin.internal.api.generated.ApiException;
import org.openhab.binding.jellyfin.internal.api.generated.ApiResponse;
import org.openhab.binding.jellyfin.internal.api.generated.Configuration;
import org.openhab.binding.jellyfin.internal.api.generated.Pair;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.DefaultDirectoryBrowserInfoDto;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.FileSystemEntryInfo;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.ValidatePathDto;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class EnvironmentApi {
    private ApiClient apiClient;

    public EnvironmentApi() {
        this(Configuration.getDefaultApiClient());
    }

    public EnvironmentApi(ApiClient apiClient) {
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
     * Get Default directory browser.
     * 
     * @return DefaultDirectoryBrowserInfoDto
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
     *                        <td>Default directory browser returned.</td>
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
    public DefaultDirectoryBrowserInfoDto getDefaultDirectoryBrowser() throws ApiException {
        return getDefaultDirectoryBrowserWithHttpInfo().getData();
    }

    /**
     * Get Default directory browser.
     * 
     * @return ApiResponse&lt;DefaultDirectoryBrowserInfoDto&gt;
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
     *                        <td>Default directory browser returned.</td>
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
    public ApiResponse<DefaultDirectoryBrowserInfoDto> getDefaultDirectoryBrowserWithHttpInfo() throws ApiException {
        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<DefaultDirectoryBrowserInfoDto> localVarReturnType = new GenericType<DefaultDirectoryBrowserInfoDto>() {
        };
        return apiClient.invokeAPI("EnvironmentApi.getDefaultDirectoryBrowser", "/Environment/DefaultDirectoryBrowser",
                "GET", new ArrayList<>(), null, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(),
                localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType, false);
    }

    /**
     * Gets the contents of a given directory in the file system.
     * 
     * @param path The path. (required)
     * @param includeFiles An optional filter to include or exclude files from the results. true/false. (optional,
     *            default to false)
     * @param includeDirectories An optional filter to include or exclude folders from the results. true/false.
     *            (optional, default to false)
     * @return List&lt;FileSystemEntryInfo&gt;
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
     *                        <td>Directory contents returned.</td>
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
    public List<FileSystemEntryInfo> getDirectoryContents(@org.eclipse.jdt.annotation.Nullable String path,
            @org.eclipse.jdt.annotation.NonNull Boolean includeFiles,
            @org.eclipse.jdt.annotation.NonNull Boolean includeDirectories) throws ApiException {
        return getDirectoryContentsWithHttpInfo(path, includeFiles, includeDirectories).getData();
    }

    /**
     * Gets the contents of a given directory in the file system.
     * 
     * @param path The path. (required)
     * @param includeFiles An optional filter to include or exclude files from the results. true/false. (optional,
     *            default to false)
     * @param includeDirectories An optional filter to include or exclude folders from the results. true/false.
     *            (optional, default to false)
     * @return ApiResponse&lt;List&lt;FileSystemEntryInfo&gt;&gt;
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
     *                        <td>Directory contents returned.</td>
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
    public ApiResponse<List<FileSystemEntryInfo>> getDirectoryContentsWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable String path, @org.eclipse.jdt.annotation.NonNull Boolean includeFiles,
            @org.eclipse.jdt.annotation.NonNull Boolean includeDirectories) throws ApiException {
        // Check required parameters
        if (path == null) {
            throw new ApiException(400, "Missing the required parameter 'path' when calling getDirectoryContents");
        }

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "path", path));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "includeFiles", includeFiles));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "includeDirectories", includeDirectories));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<List<FileSystemEntryInfo>> localVarReturnType = new GenericType<List<FileSystemEntryInfo>>() {
        };
        return apiClient.invokeAPI("EnvironmentApi.getDirectoryContents", "/Environment/DirectoryContents", "GET",
                localVarQueryParams, null, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(),
                localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType, false);
    }

    /**
     * Gets available drives from the server&#39;s file system.
     * 
     * @return List&lt;FileSystemEntryInfo&gt;
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
     *                        <td>List of entries returned.</td>
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
    public List<FileSystemEntryInfo> getDrives() throws ApiException {
        return getDrivesWithHttpInfo().getData();
    }

    /**
     * Gets available drives from the server&#39;s file system.
     * 
     * @return ApiResponse&lt;List&lt;FileSystemEntryInfo&gt;&gt;
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
     *                        <td>List of entries returned.</td>
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
    public ApiResponse<List<FileSystemEntryInfo>> getDrivesWithHttpInfo() throws ApiException {
        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<List<FileSystemEntryInfo>> localVarReturnType = new GenericType<List<FileSystemEntryInfo>>() {
        };
        return apiClient.invokeAPI("EnvironmentApi.getDrives", "/Environment/Drives", "GET", new ArrayList<>(), null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, localVarReturnType, false);
    }

    /**
     * Gets network paths.
     * 
     * @return List&lt;FileSystemEntryInfo&gt;
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
     *                        <td>Empty array returned.</td>
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
     * @deprecated
     */
    @Deprecated
    public List<FileSystemEntryInfo> getNetworkShares() throws ApiException {
        return getNetworkSharesWithHttpInfo().getData();
    }

    /**
     * Gets network paths.
     * 
     * @return ApiResponse&lt;List&lt;FileSystemEntryInfo&gt;&gt;
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
     *                        <td>Empty array returned.</td>
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
     * @deprecated
     */
    @Deprecated
    public ApiResponse<List<FileSystemEntryInfo>> getNetworkSharesWithHttpInfo() throws ApiException {
        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<List<FileSystemEntryInfo>> localVarReturnType = new GenericType<List<FileSystemEntryInfo>>() {
        };
        return apiClient.invokeAPI("EnvironmentApi.getNetworkShares", "/Environment/NetworkShares", "GET",
                new ArrayList<>(), null, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(),
                localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType, false);
    }

    /**
     * Gets the parent path of a given path.
     * 
     * @param path The path. (required)
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
     *                        <td>Success</td>
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
    public String getParentPath(@org.eclipse.jdt.annotation.Nullable String path) throws ApiException {
        return getParentPathWithHttpInfo(path).getData();
    }

    /**
     * Gets the parent path of a given path.
     * 
     * @param path The path. (required)
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
     *                        <td>Success</td>
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
    public ApiResponse<String> getParentPathWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String path)
            throws ApiException {
        // Check required parameters
        if (path == null) {
            throw new ApiException(400, "Missing the required parameter 'path' when calling getParentPath");
        }

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "path", path));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<String> localVarReturnType = new GenericType<String>() {
        };
        return apiClient.invokeAPI("EnvironmentApi.getParentPath", "/Environment/ParentPath", "GET",
                localVarQueryParams, null, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(),
                localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType, false);
    }

    /**
     * Validates path.
     * 
     * @param validatePathDto Validate request object. (required)
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
     *                        <td>Path validated.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Path not found.</td>
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
    public void validatePath(@org.eclipse.jdt.annotation.Nullable ValidatePathDto validatePathDto) throws ApiException {
        validatePathWithHttpInfo(validatePathDto);
    }

    /**
     * Validates path.
     * 
     * @param validatePathDto Validate request object. (required)
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
     *                        <td>Path validated.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Path not found.</td>
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
    public ApiResponse<Void> validatePathWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable ValidatePathDto validatePathDto) throws ApiException {
        // Check required parameters
        if (validatePathDto == null) {
            throw new ApiException(400, "Missing the required parameter 'validatePathDto' when calling validatePath");
        }

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType("application/json", "text/json",
                "application/*+json");
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        return apiClient.invokeAPI("EnvironmentApi.validatePath", "/Environment/ValidatePath", "POST",
                new ArrayList<>(), validatePathDto, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(),
                localVarAccept, localVarContentType, localVarAuthNames, null, false);
    }
}
