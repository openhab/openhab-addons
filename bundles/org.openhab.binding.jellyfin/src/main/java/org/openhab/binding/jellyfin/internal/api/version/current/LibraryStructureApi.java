package org.openhab.binding.jellyfin.internal.api.version.current;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.openhab.binding.jellyfin.internal.api.version.ApiClient;
import org.openhab.binding.jellyfin.internal.api.version.current.model.AddVirtualFolderDto;
import org.openhab.binding.jellyfin.internal.api.version.current.model.CollectionTypeOptions;
import org.openhab.binding.jellyfin.internal.api.version.current.model.MediaPathDto;
import org.openhab.binding.jellyfin.internal.api.version.current.model.UpdateLibraryOptionsDto;
import org.openhab.binding.jellyfin.internal.api.version.current.model.UpdateMediaPathRequestDto;
import org.openhab.binding.jellyfin.internal.api.version.current.model.VirtualFolderInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.12.0")
public class LibraryStructureApi {
    private ApiClient apiClient;

    public LibraryStructureApi() {
        this(new ApiClient());
    }

    @Autowired
    public LibraryStructureApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Add a media path to a library.
     * 
     * <p>
     * <b>204</b> - Media path added.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param mediaPathDto The media path dto.
     * @param refreshLibrary Whether to refresh the library.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec addMediaPathRequestCreation(MediaPathDto mediaPathDto, Boolean refreshLibrary)
            throws WebClientResponseException {
        Object postBody = mediaPathDto;
        // verify the required parameter 'mediaPathDto' is set
        if (mediaPathDto == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'mediaPathDto' when calling addMediaPath",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "refreshLibrary", refreshLibrary));

        final String[] localVarAccepts = {};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { "application/json", "text/json", "application/*+json" };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return apiClient.invokeAPI("/Library/VirtualFolders/Paths", HttpMethod.POST, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Add a media path to a library.
     * 
     * <p>
     * <b>204</b> - Media path added.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param mediaPathDto The media path dto.
     * @param refreshLibrary Whether to refresh the library.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> addMediaPath(MediaPathDto mediaPathDto, Boolean refreshLibrary)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return addMediaPathRequestCreation(mediaPathDto, refreshLibrary).bodyToMono(localVarReturnType);
    }

    /**
     * Add a media path to a library.
     * 
     * <p>
     * <b>204</b> - Media path added.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param mediaPathDto The media path dto.
     * @param refreshLibrary Whether to refresh the library.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> addMediaPathWithHttpInfo(MediaPathDto mediaPathDto, Boolean refreshLibrary)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return addMediaPathRequestCreation(mediaPathDto, refreshLibrary).toEntity(localVarReturnType);
    }

    /**
     * Add a media path to a library.
     * 
     * <p>
     * <b>204</b> - Media path added.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param mediaPathDto The media path dto.
     * @param refreshLibrary Whether to refresh the library.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec addMediaPathWithResponseSpec(MediaPathDto mediaPathDto, Boolean refreshLibrary)
            throws WebClientResponseException {
        return addMediaPathRequestCreation(mediaPathDto, refreshLibrary);
    }

    /**
     * Adds a virtual folder.
     * 
     * <p>
     * <b>204</b> - Folder added.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param name The name of the virtual folder.
     * @param collectionType The type of the collection.
     * @param paths The paths of the virtual folder.
     * @param refreshLibrary Whether to refresh the library.
     * @param addVirtualFolderDto The library options.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec addVirtualFolderRequestCreation(String name, CollectionTypeOptions collectionType,
            List<String> paths, Boolean refreshLibrary, AddVirtualFolderDto addVirtualFolderDto)
            throws WebClientResponseException {
        Object postBody = addVirtualFolderDto;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "name", name));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "collectionType", collectionType));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "paths", paths));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "refreshLibrary", refreshLibrary));

        final String[] localVarAccepts = {};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { "application/json", "text/json", "application/*+json" };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return apiClient.invokeAPI("/Library/VirtualFolders", HttpMethod.POST, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Adds a virtual folder.
     * 
     * <p>
     * <b>204</b> - Folder added.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param name The name of the virtual folder.
     * @param collectionType The type of the collection.
     * @param paths The paths of the virtual folder.
     * @param refreshLibrary Whether to refresh the library.
     * @param addVirtualFolderDto The library options.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> addVirtualFolder(String name, CollectionTypeOptions collectionType, List<String> paths,
            Boolean refreshLibrary, AddVirtualFolderDto addVirtualFolderDto) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return addVirtualFolderRequestCreation(name, collectionType, paths, refreshLibrary, addVirtualFolderDto)
                .bodyToMono(localVarReturnType);
    }

    /**
     * Adds a virtual folder.
     * 
     * <p>
     * <b>204</b> - Folder added.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param name The name of the virtual folder.
     * @param collectionType The type of the collection.
     * @param paths The paths of the virtual folder.
     * @param refreshLibrary Whether to refresh the library.
     * @param addVirtualFolderDto The library options.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> addVirtualFolderWithHttpInfo(String name, CollectionTypeOptions collectionType,
            List<String> paths, Boolean refreshLibrary, AddVirtualFolderDto addVirtualFolderDto)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return addVirtualFolderRequestCreation(name, collectionType, paths, refreshLibrary, addVirtualFolderDto)
                .toEntity(localVarReturnType);
    }

    /**
     * Adds a virtual folder.
     * 
     * <p>
     * <b>204</b> - Folder added.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param name The name of the virtual folder.
     * @param collectionType The type of the collection.
     * @param paths The paths of the virtual folder.
     * @param refreshLibrary Whether to refresh the library.
     * @param addVirtualFolderDto The library options.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec addVirtualFolderWithResponseSpec(String name, CollectionTypeOptions collectionType,
            List<String> paths, Boolean refreshLibrary, AddVirtualFolderDto addVirtualFolderDto)
            throws WebClientResponseException {
        return addVirtualFolderRequestCreation(name, collectionType, paths, refreshLibrary, addVirtualFolderDto);
    }

    /**
     * Gets all virtual folders.
     * 
     * <p>
     * <b>200</b> - Virtual folders retrieved.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return List&lt;VirtualFolderInfo&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getVirtualFoldersRequestCreation() throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<VirtualFolderInfo> localVarReturnType = new ParameterizedTypeReference<VirtualFolderInfo>() {
        };
        return apiClient.invokeAPI("/Library/VirtualFolders", HttpMethod.GET, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Gets all virtual folders.
     * 
     * <p>
     * <b>200</b> - Virtual folders retrieved.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return List&lt;VirtualFolderInfo&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Flux<VirtualFolderInfo> getVirtualFolders() throws WebClientResponseException {
        ParameterizedTypeReference<VirtualFolderInfo> localVarReturnType = new ParameterizedTypeReference<VirtualFolderInfo>() {
        };
        return getVirtualFoldersRequestCreation().bodyToFlux(localVarReturnType);
    }

    /**
     * Gets all virtual folders.
     * 
     * <p>
     * <b>200</b> - Virtual folders retrieved.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return ResponseEntity&lt;List&lt;VirtualFolderInfo&gt;&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<List<VirtualFolderInfo>>> getVirtualFoldersWithHttpInfo()
            throws WebClientResponseException {
        ParameterizedTypeReference<VirtualFolderInfo> localVarReturnType = new ParameterizedTypeReference<VirtualFolderInfo>() {
        };
        return getVirtualFoldersRequestCreation().toEntityList(localVarReturnType);
    }

    /**
     * Gets all virtual folders.
     * 
     * <p>
     * <b>200</b> - Virtual folders retrieved.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getVirtualFoldersWithResponseSpec() throws WebClientResponseException {
        return getVirtualFoldersRequestCreation();
    }

    /**
     * Remove a media path.
     * 
     * <p>
     * <b>204</b> - Media path removed.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param name The name of the library.
     * @param path The path to remove.
     * @param refreshLibrary Whether to refresh the library.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec removeMediaPathRequestCreation(String name, String path, Boolean refreshLibrary)
            throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "name", name));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "path", path));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "refreshLibrary", refreshLibrary));

        final String[] localVarAccepts = {};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return apiClient.invokeAPI("/Library/VirtualFolders/Paths", HttpMethod.DELETE, pathParams, queryParams,
                postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType,
                localVarAuthNames, localVarReturnType);
    }

    /**
     * Remove a media path.
     * 
     * <p>
     * <b>204</b> - Media path removed.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param name The name of the library.
     * @param path The path to remove.
     * @param refreshLibrary Whether to refresh the library.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> removeMediaPath(String name, String path, Boolean refreshLibrary)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return removeMediaPathRequestCreation(name, path, refreshLibrary).bodyToMono(localVarReturnType);
    }

    /**
     * Remove a media path.
     * 
     * <p>
     * <b>204</b> - Media path removed.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param name The name of the library.
     * @param path The path to remove.
     * @param refreshLibrary Whether to refresh the library.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> removeMediaPathWithHttpInfo(String name, String path, Boolean refreshLibrary)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return removeMediaPathRequestCreation(name, path, refreshLibrary).toEntity(localVarReturnType);
    }

    /**
     * Remove a media path.
     * 
     * <p>
     * <b>204</b> - Media path removed.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param name The name of the library.
     * @param path The path to remove.
     * @param refreshLibrary Whether to refresh the library.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec removeMediaPathWithResponseSpec(String name, String path, Boolean refreshLibrary)
            throws WebClientResponseException {
        return removeMediaPathRequestCreation(name, path, refreshLibrary);
    }

    /**
     * Removes a virtual folder.
     * 
     * <p>
     * <b>204</b> - Folder removed.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param name The name of the folder.
     * @param refreshLibrary Whether to refresh the library.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec removeVirtualFolderRequestCreation(String name, Boolean refreshLibrary)
            throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "name", name));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "refreshLibrary", refreshLibrary));

        final String[] localVarAccepts = {};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return apiClient.invokeAPI("/Library/VirtualFolders", HttpMethod.DELETE, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Removes a virtual folder.
     * 
     * <p>
     * <b>204</b> - Folder removed.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param name The name of the folder.
     * @param refreshLibrary Whether to refresh the library.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> removeVirtualFolder(String name, Boolean refreshLibrary) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return removeVirtualFolderRequestCreation(name, refreshLibrary).bodyToMono(localVarReturnType);
    }

    /**
     * Removes a virtual folder.
     * 
     * <p>
     * <b>204</b> - Folder removed.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param name The name of the folder.
     * @param refreshLibrary Whether to refresh the library.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> removeVirtualFolderWithHttpInfo(String name, Boolean refreshLibrary)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return removeVirtualFolderRequestCreation(name, refreshLibrary).toEntity(localVarReturnType);
    }

    /**
     * Removes a virtual folder.
     * 
     * <p>
     * <b>204</b> - Folder removed.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param name The name of the folder.
     * @param refreshLibrary Whether to refresh the library.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec removeVirtualFolderWithResponseSpec(String name, Boolean refreshLibrary)
            throws WebClientResponseException {
        return removeVirtualFolderRequestCreation(name, refreshLibrary);
    }

    /**
     * Renames a virtual folder.
     * 
     * <p>
     * <b>204</b> - Folder renamed.
     * <p>
     * <b>404</b> - Library doesn&#39;t exist.
     * <p>
     * <b>409</b> - Library already exists.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param name The name of the virtual folder.
     * @param newName The new name.
     * @param refreshLibrary Whether to refresh the library.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec renameVirtualFolderRequestCreation(String name, String newName, Boolean refreshLibrary)
            throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "name", name));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "newName", newName));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "refreshLibrary", refreshLibrary));

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return apiClient.invokeAPI("/Library/VirtualFolders/Name", HttpMethod.POST, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Renames a virtual folder.
     * 
     * <p>
     * <b>204</b> - Folder renamed.
     * <p>
     * <b>404</b> - Library doesn&#39;t exist.
     * <p>
     * <b>409</b> - Library already exists.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param name The name of the virtual folder.
     * @param newName The new name.
     * @param refreshLibrary Whether to refresh the library.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> renameVirtualFolder(String name, String newName, Boolean refreshLibrary)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return renameVirtualFolderRequestCreation(name, newName, refreshLibrary).bodyToMono(localVarReturnType);
    }

    /**
     * Renames a virtual folder.
     * 
     * <p>
     * <b>204</b> - Folder renamed.
     * <p>
     * <b>404</b> - Library doesn&#39;t exist.
     * <p>
     * <b>409</b> - Library already exists.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param name The name of the virtual folder.
     * @param newName The new name.
     * @param refreshLibrary Whether to refresh the library.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> renameVirtualFolderWithHttpInfo(String name, String newName,
            Boolean refreshLibrary) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return renameVirtualFolderRequestCreation(name, newName, refreshLibrary).toEntity(localVarReturnType);
    }

    /**
     * Renames a virtual folder.
     * 
     * <p>
     * <b>204</b> - Folder renamed.
     * <p>
     * <b>404</b> - Library doesn&#39;t exist.
     * <p>
     * <b>409</b> - Library already exists.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param name The name of the virtual folder.
     * @param newName The new name.
     * @param refreshLibrary Whether to refresh the library.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec renameVirtualFolderWithResponseSpec(String name, String newName, Boolean refreshLibrary)
            throws WebClientResponseException {
        return renameVirtualFolderRequestCreation(name, newName, refreshLibrary);
    }

    /**
     * Update library options.
     * 
     * <p>
     * <b>204</b> - Library updated.
     * <p>
     * <b>404</b> - Item not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param updateLibraryOptionsDto The library name and options.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec updateLibraryOptionsRequestCreation(UpdateLibraryOptionsDto updateLibraryOptionsDto)
            throws WebClientResponseException {
        Object postBody = updateLibraryOptionsDto;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { "application/json", "text/json", "application/*+json" };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return apiClient.invokeAPI("/Library/VirtualFolders/LibraryOptions", HttpMethod.POST, pathParams, queryParams,
                postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType,
                localVarAuthNames, localVarReturnType);
    }

    /**
     * Update library options.
     * 
     * <p>
     * <b>204</b> - Library updated.
     * <p>
     * <b>404</b> - Item not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param updateLibraryOptionsDto The library name and options.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> updateLibraryOptions(UpdateLibraryOptionsDto updateLibraryOptionsDto)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return updateLibraryOptionsRequestCreation(updateLibraryOptionsDto).bodyToMono(localVarReturnType);
    }

    /**
     * Update library options.
     * 
     * <p>
     * <b>204</b> - Library updated.
     * <p>
     * <b>404</b> - Item not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param updateLibraryOptionsDto The library name and options.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> updateLibraryOptionsWithHttpInfo(UpdateLibraryOptionsDto updateLibraryOptionsDto)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return updateLibraryOptionsRequestCreation(updateLibraryOptionsDto).toEntity(localVarReturnType);
    }

    /**
     * Update library options.
     * 
     * <p>
     * <b>204</b> - Library updated.
     * <p>
     * <b>404</b> - Item not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param updateLibraryOptionsDto The library name and options.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec updateLibraryOptionsWithResponseSpec(UpdateLibraryOptionsDto updateLibraryOptionsDto)
            throws WebClientResponseException {
        return updateLibraryOptionsRequestCreation(updateLibraryOptionsDto);
    }

    /**
     * Updates a media path.
     * 
     * <p>
     * <b>204</b> - Media path updated.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param updateMediaPathRequestDto The name of the library and path infos.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec updateMediaPathRequestCreation(UpdateMediaPathRequestDto updateMediaPathRequestDto)
            throws WebClientResponseException {
        Object postBody = updateMediaPathRequestDto;
        // verify the required parameter 'updateMediaPathRequestDto' is set
        if (updateMediaPathRequestDto == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'updateMediaPathRequestDto' when calling updateMediaPath",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = {};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { "application/json", "text/json", "application/*+json" };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return apiClient.invokeAPI("/Library/VirtualFolders/Paths/Update", HttpMethod.POST, pathParams, queryParams,
                postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType,
                localVarAuthNames, localVarReturnType);
    }

    /**
     * Updates a media path.
     * 
     * <p>
     * <b>204</b> - Media path updated.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param updateMediaPathRequestDto The name of the library and path infos.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> updateMediaPath(UpdateMediaPathRequestDto updateMediaPathRequestDto)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return updateMediaPathRequestCreation(updateMediaPathRequestDto).bodyToMono(localVarReturnType);
    }

    /**
     * Updates a media path.
     * 
     * <p>
     * <b>204</b> - Media path updated.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param updateMediaPathRequestDto The name of the library and path infos.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> updateMediaPathWithHttpInfo(UpdateMediaPathRequestDto updateMediaPathRequestDto)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return updateMediaPathRequestCreation(updateMediaPathRequestDto).toEntity(localVarReturnType);
    }

    /**
     * Updates a media path.
     * 
     * <p>
     * <b>204</b> - Media path updated.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param updateMediaPathRequestDto The name of the library and path infos.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec updateMediaPathWithResponseSpec(UpdateMediaPathRequestDto updateMediaPathRequestDto)
            throws WebClientResponseException {
        return updateMediaPathRequestCreation(updateMediaPathRequestDto);
    }
}
