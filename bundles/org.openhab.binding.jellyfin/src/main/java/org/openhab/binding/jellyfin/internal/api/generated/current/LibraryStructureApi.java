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
import org.openhab.binding.jellyfin.internal.api.generated.current.model.AddVirtualFolderDto;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.CollectionTypeOptions;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.MediaPathDto;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.UpdateLibraryOptionsDto;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.UpdateMediaPathRequestDto;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.VirtualFolderInfo;

public class LibraryStructureApi {
    private ApiClient apiClient;

    public LibraryStructureApi() {
        this(Configuration.getDefaultApiClient());
    }

    public LibraryStructureApi(ApiClient apiClient) {
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
     * Add a media path to a library.
     * 
     * @param mediaPathDto The media path dto. (required)
     * @param refreshLibrary Whether to refresh the library. (optional, default to false)
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
     *                        <td>Media path added.</td>
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
    public void addMediaPath(@org.eclipse.jdt.annotation.Nullable MediaPathDto mediaPathDto,
            @org.eclipse.jdt.annotation.NonNull Boolean refreshLibrary) throws ApiException {
        addMediaPathWithHttpInfo(mediaPathDto, refreshLibrary);
    }

    /**
     * Add a media path to a library.
     * 
     * @param mediaPathDto The media path dto. (required)
     * @param refreshLibrary Whether to refresh the library. (optional, default to false)
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
     *                        <td>Media path added.</td>
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
    public ApiResponse<Void> addMediaPathWithHttpInfo(@org.eclipse.jdt.annotation.Nullable MediaPathDto mediaPathDto,
            @org.eclipse.jdt.annotation.NonNull Boolean refreshLibrary) throws ApiException {
        // Check required parameters
        if (mediaPathDto == null) {
            throw new ApiException(400, "Missing the required parameter 'mediaPathDto' when calling addMediaPath");
        }

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(
                apiClient.parameterToPairs("", "refreshLibrary", refreshLibrary));

        String localVarAccept = apiClient.selectHeaderAccept();
        String localVarContentType = apiClient.selectHeaderContentType("application/json", "text/json",
                "application/*+json");
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        return apiClient.invokeAPI("LibraryStructureApi.addMediaPath", "/Library/VirtualFolders/Paths", "POST",
                localVarQueryParams, mediaPathDto, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(),
                localVarAccept, localVarContentType, localVarAuthNames, null, false);
    }

    /**
     * Adds a virtual folder.
     * 
     * @param name The name of the virtual folder. (optional)
     * @param collectionType The type of the collection. (optional)
     * @param paths The paths of the virtual folder. (optional)
     * @param refreshLibrary Whether to refresh the library. (optional, default to false)
     * @param addVirtualFolderDto The library options. (optional)
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
     *                        <td>Folder added.</td>
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
    public void addVirtualFolder(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull CollectionTypeOptions collectionType,
            @org.eclipse.jdt.annotation.NonNull List<String> paths,
            @org.eclipse.jdt.annotation.NonNull Boolean refreshLibrary,
            @org.eclipse.jdt.annotation.NonNull AddVirtualFolderDto addVirtualFolderDto) throws ApiException {
        addVirtualFolderWithHttpInfo(name, collectionType, paths, refreshLibrary, addVirtualFolderDto);
    }

    /**
     * Adds a virtual folder.
     * 
     * @param name The name of the virtual folder. (optional)
     * @param collectionType The type of the collection. (optional)
     * @param paths The paths of the virtual folder. (optional)
     * @param refreshLibrary Whether to refresh the library. (optional, default to false)
     * @param addVirtualFolderDto The library options. (optional)
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
     *                        <td>Folder added.</td>
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
    public ApiResponse<Void> addVirtualFolderWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull CollectionTypeOptions collectionType,
            @org.eclipse.jdt.annotation.NonNull List<String> paths,
            @org.eclipse.jdt.annotation.NonNull Boolean refreshLibrary,
            @org.eclipse.jdt.annotation.NonNull AddVirtualFolderDto addVirtualFolderDto) throws ApiException {
        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "name", name));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "collectionType", collectionType));
        localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "paths", paths));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "refreshLibrary", refreshLibrary));

        String localVarAccept = apiClient.selectHeaderAccept();
        String localVarContentType = apiClient.selectHeaderContentType("application/json", "text/json",
                "application/*+json");
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        return apiClient.invokeAPI("LibraryStructureApi.addVirtualFolder", "/Library/VirtualFolders", "POST",
                localVarQueryParams, addVirtualFolderDto, new LinkedHashMap<>(), new LinkedHashMap<>(),
                new LinkedHashMap<>(), localVarAccept, localVarContentType, localVarAuthNames, null, false);
    }

    /**
     * Gets all virtual folders.
     * 
     * @return List&lt;VirtualFolderInfo&gt;
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
     *                        <td>Virtual folders retrieved.</td>
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
    public List<VirtualFolderInfo> getVirtualFolders() throws ApiException {
        return getVirtualFoldersWithHttpInfo().getData();
    }

    /**
     * Gets all virtual folders.
     * 
     * @return ApiResponse&lt;List&lt;VirtualFolderInfo&gt;&gt;
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
     *                        <td>Virtual folders retrieved.</td>
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
    public ApiResponse<List<VirtualFolderInfo>> getVirtualFoldersWithHttpInfo() throws ApiException {
        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<List<VirtualFolderInfo>> localVarReturnType = new GenericType<List<VirtualFolderInfo>>() {
        };
        return apiClient.invokeAPI("LibraryStructureApi.getVirtualFolders", "/Library/VirtualFolders", "GET",
                new ArrayList<>(), null, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(),
                localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType, false);
    }

    /**
     * Remove a media path.
     * 
     * @param name The name of the library. (optional)
     * @param path The path to remove. (optional)
     * @param refreshLibrary Whether to refresh the library. (optional, default to false)
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
     *                        <td>Media path removed.</td>
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
    public void removeMediaPath(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull String path, @org.eclipse.jdt.annotation.NonNull Boolean refreshLibrary)
            throws ApiException {
        removeMediaPathWithHttpInfo(name, path, refreshLibrary);
    }

    /**
     * Remove a media path.
     * 
     * @param name The name of the library. (optional)
     * @param path The path to remove. (optional)
     * @param refreshLibrary Whether to refresh the library. (optional, default to false)
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
     *                        <td>Media path removed.</td>
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
    public ApiResponse<Void> removeMediaPathWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull String path, @org.eclipse.jdt.annotation.NonNull Boolean refreshLibrary)
            throws ApiException {
        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "name", name));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "path", path));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "refreshLibrary", refreshLibrary));

        String localVarAccept = apiClient.selectHeaderAccept();
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        return apiClient.invokeAPI("LibraryStructureApi.removeMediaPath", "/Library/VirtualFolders/Paths", "DELETE",
                localVarQueryParams, null, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(),
                localVarAccept, localVarContentType, localVarAuthNames, null, false);
    }

    /**
     * Removes a virtual folder.
     * 
     * @param name The name of the folder. (optional)
     * @param refreshLibrary Whether to refresh the library. (optional, default to false)
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
     *                        <td>Folder removed.</td>
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
    public void removeVirtualFolder(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull Boolean refreshLibrary) throws ApiException {
        removeVirtualFolderWithHttpInfo(name, refreshLibrary);
    }

    /**
     * Removes a virtual folder.
     * 
     * @param name The name of the folder. (optional)
     * @param refreshLibrary Whether to refresh the library. (optional, default to false)
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
     *                        <td>Folder removed.</td>
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
    public ApiResponse<Void> removeVirtualFolderWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull Boolean refreshLibrary) throws ApiException {
        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "name", name));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "refreshLibrary", refreshLibrary));

        String localVarAccept = apiClient.selectHeaderAccept();
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        return apiClient.invokeAPI("LibraryStructureApi.removeVirtualFolder", "/Library/VirtualFolders", "DELETE",
                localVarQueryParams, null, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(),
                localVarAccept, localVarContentType, localVarAuthNames, null, false);
    }

    /**
     * Renames a virtual folder.
     * 
     * @param name The name of the virtual folder. (optional)
     * @param newName The new name. (optional)
     * @param refreshLibrary Whether to refresh the library. (optional, default to false)
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
     *                        <td>Folder renamed.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Library doesn&#39;t exist.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>409</td>
     *                        <td>Library already exists.</td>
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
    public void renameVirtualFolder(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull String newName,
            @org.eclipse.jdt.annotation.NonNull Boolean refreshLibrary) throws ApiException {
        renameVirtualFolderWithHttpInfo(name, newName, refreshLibrary);
    }

    /**
     * Renames a virtual folder.
     * 
     * @param name The name of the virtual folder. (optional)
     * @param newName The new name. (optional)
     * @param refreshLibrary Whether to refresh the library. (optional, default to false)
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
     *                        <td>Folder renamed.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Library doesn&#39;t exist.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>409</td>
     *                        <td>Library already exists.</td>
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
    public ApiResponse<Void> renameVirtualFolderWithHttpInfo(@org.eclipse.jdt.annotation.NonNull String name,
            @org.eclipse.jdt.annotation.NonNull String newName,
            @org.eclipse.jdt.annotation.NonNull Boolean refreshLibrary) throws ApiException {
        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "name", name));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "newName", newName));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "refreshLibrary", refreshLibrary));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        return apiClient.invokeAPI("LibraryStructureApi.renameVirtualFolder", "/Library/VirtualFolders/Name", "POST",
                localVarQueryParams, null, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(),
                localVarAccept, localVarContentType, localVarAuthNames, null, false);
    }

    /**
     * Update library options.
     * 
     * @param updateLibraryOptionsDto The library name and options. (optional)
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
     *                        <td>Library updated.</td>
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
    public void updateLibraryOptions(
            @org.eclipse.jdt.annotation.NonNull UpdateLibraryOptionsDto updateLibraryOptionsDto) throws ApiException {
        updateLibraryOptionsWithHttpInfo(updateLibraryOptionsDto);
    }

    /**
     * Update library options.
     * 
     * @param updateLibraryOptionsDto The library name and options. (optional)
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
     *                        <td>Library updated.</td>
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
    public ApiResponse<Void> updateLibraryOptionsWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull UpdateLibraryOptionsDto updateLibraryOptionsDto) throws ApiException {
        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType("application/json", "text/json",
                "application/*+json");
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        return apiClient.invokeAPI("LibraryStructureApi.updateLibraryOptions", "/Library/VirtualFolders/LibraryOptions",
                "POST", new ArrayList<>(), updateLibraryOptionsDto, new LinkedHashMap<>(), new LinkedHashMap<>(),
                new LinkedHashMap<>(), localVarAccept, localVarContentType, localVarAuthNames, null, false);
    }

    /**
     * Updates a media path.
     * 
     * @param updateMediaPathRequestDto The name of the library and path infos. (required)
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
     *                        <td>Media path updated.</td>
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
    public void updateMediaPath(
            @org.eclipse.jdt.annotation.Nullable UpdateMediaPathRequestDto updateMediaPathRequestDto)
            throws ApiException {
        updateMediaPathWithHttpInfo(updateMediaPathRequestDto);
    }

    /**
     * Updates a media path.
     * 
     * @param updateMediaPathRequestDto The name of the library and path infos. (required)
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
     *                        <td>Media path updated.</td>
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
    public ApiResponse<Void> updateMediaPathWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable UpdateMediaPathRequestDto updateMediaPathRequestDto)
            throws ApiException {
        // Check required parameters
        if (updateMediaPathRequestDto == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'updateMediaPathRequestDto' when calling updateMediaPath");
        }

        String localVarAccept = apiClient.selectHeaderAccept();
        String localVarContentType = apiClient.selectHeaderContentType("application/json", "text/json",
                "application/*+json");
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        return apiClient.invokeAPI("LibraryStructureApi.updateMediaPath", "/Library/VirtualFolders/Paths/Update",
                "POST", new ArrayList<>(), updateMediaPathRequestDto, new LinkedHashMap<>(), new LinkedHashMap<>(),
                new LinkedHashMap<>(), localVarAccept, localVarContentType, localVarAuthNames, null, false);
    }
}
