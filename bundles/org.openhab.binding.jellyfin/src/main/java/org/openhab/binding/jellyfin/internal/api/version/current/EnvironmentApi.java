package org.openhab.binding.jellyfin.internal.api.version.current;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openhab.binding.jellyfin.internal.api.version.ApiClient;
import org.openhab.binding.jellyfin.internal.api.version.current.model.DefaultDirectoryBrowserInfoDto;
import org.openhab.binding.jellyfin.internal.api.version.current.model.FileSystemEntryInfo;
import org.openhab.binding.jellyfin.internal.api.version.current.model.ValidatePathDto;
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
public class EnvironmentApi {
    private ApiClient apiClient;

    public EnvironmentApi() {
        this(new ApiClient());
    }

    @Autowired
    public EnvironmentApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Get Default directory browser.
     * 
     * <p>
     * <b>200</b> - Default directory browser returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return DefaultDirectoryBrowserInfoDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getDefaultDirectoryBrowserRequestCreation() throws WebClientResponseException {
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

        ParameterizedTypeReference<DefaultDirectoryBrowserInfoDto> localVarReturnType = new ParameterizedTypeReference<DefaultDirectoryBrowserInfoDto>() {
        };
        return apiClient.invokeAPI("/Environment/DefaultDirectoryBrowser", HttpMethod.GET, pathParams, queryParams,
                postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType,
                localVarAuthNames, localVarReturnType);
    }

    /**
     * Get Default directory browser.
     * 
     * <p>
     * <b>200</b> - Default directory browser returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return DefaultDirectoryBrowserInfoDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<DefaultDirectoryBrowserInfoDto> getDefaultDirectoryBrowser() throws WebClientResponseException {
        ParameterizedTypeReference<DefaultDirectoryBrowserInfoDto> localVarReturnType = new ParameterizedTypeReference<DefaultDirectoryBrowserInfoDto>() {
        };
        return getDefaultDirectoryBrowserRequestCreation().bodyToMono(localVarReturnType);
    }

    /**
     * Get Default directory browser.
     * 
     * <p>
     * <b>200</b> - Default directory browser returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return ResponseEntity&lt;DefaultDirectoryBrowserInfoDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<DefaultDirectoryBrowserInfoDto>> getDefaultDirectoryBrowserWithHttpInfo()
            throws WebClientResponseException {
        ParameterizedTypeReference<DefaultDirectoryBrowserInfoDto> localVarReturnType = new ParameterizedTypeReference<DefaultDirectoryBrowserInfoDto>() {
        };
        return getDefaultDirectoryBrowserRequestCreation().toEntity(localVarReturnType);
    }

    /**
     * Get Default directory browser.
     * 
     * <p>
     * <b>200</b> - Default directory browser returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getDefaultDirectoryBrowserWithResponseSpec() throws WebClientResponseException {
        return getDefaultDirectoryBrowserRequestCreation();
    }

    /**
     * Gets the contents of a given directory in the file system.
     * 
     * <p>
     * <b>200</b> - Directory contents returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param path The path.
     * @param includeFiles An optional filter to include or exclude files from the results. true/false.
     * @param includeDirectories An optional filter to include or exclude folders from the results. true/false.
     * @return List&lt;FileSystemEntryInfo&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getDirectoryContentsRequestCreation(String path, Boolean includeFiles,
            Boolean includeDirectories) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'path' is set
        if (path == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'path' when calling getDirectoryContents",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "path", path));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "includeFiles", includeFiles));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "includeDirectories", includeDirectories));

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<FileSystemEntryInfo> localVarReturnType = new ParameterizedTypeReference<FileSystemEntryInfo>() {
        };
        return apiClient.invokeAPI("/Environment/DirectoryContents", HttpMethod.GET, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Gets the contents of a given directory in the file system.
     * 
     * <p>
     * <b>200</b> - Directory contents returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param path The path.
     * @param includeFiles An optional filter to include or exclude files from the results. true/false.
     * @param includeDirectories An optional filter to include or exclude folders from the results. true/false.
     * @return List&lt;FileSystemEntryInfo&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Flux<FileSystemEntryInfo> getDirectoryContents(String path, Boolean includeFiles, Boolean includeDirectories)
            throws WebClientResponseException {
        ParameterizedTypeReference<FileSystemEntryInfo> localVarReturnType = new ParameterizedTypeReference<FileSystemEntryInfo>() {
        };
        return getDirectoryContentsRequestCreation(path, includeFiles, includeDirectories)
                .bodyToFlux(localVarReturnType);
    }

    /**
     * Gets the contents of a given directory in the file system.
     * 
     * <p>
     * <b>200</b> - Directory contents returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param path The path.
     * @param includeFiles An optional filter to include or exclude files from the results. true/false.
     * @param includeDirectories An optional filter to include or exclude folders from the results. true/false.
     * @return ResponseEntity&lt;List&lt;FileSystemEntryInfo&gt;&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<List<FileSystemEntryInfo>>> getDirectoryContentsWithHttpInfo(String path,
            Boolean includeFiles, Boolean includeDirectories) throws WebClientResponseException {
        ParameterizedTypeReference<FileSystemEntryInfo> localVarReturnType = new ParameterizedTypeReference<FileSystemEntryInfo>() {
        };
        return getDirectoryContentsRequestCreation(path, includeFiles, includeDirectories)
                .toEntityList(localVarReturnType);
    }

    /**
     * Gets the contents of a given directory in the file system.
     * 
     * <p>
     * <b>200</b> - Directory contents returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param path The path.
     * @param includeFiles An optional filter to include or exclude files from the results. true/false.
     * @param includeDirectories An optional filter to include or exclude folders from the results. true/false.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getDirectoryContentsWithResponseSpec(String path, Boolean includeFiles,
            Boolean includeDirectories) throws WebClientResponseException {
        return getDirectoryContentsRequestCreation(path, includeFiles, includeDirectories);
    }

    /**
     * Gets available drives from the server&#39;s file system.
     * 
     * <p>
     * <b>200</b> - List of entries returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return List&lt;FileSystemEntryInfo&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getDrivesRequestCreation() throws WebClientResponseException {
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

        ParameterizedTypeReference<FileSystemEntryInfo> localVarReturnType = new ParameterizedTypeReference<FileSystemEntryInfo>() {
        };
        return apiClient.invokeAPI("/Environment/Drives", HttpMethod.GET, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Gets available drives from the server&#39;s file system.
     * 
     * <p>
     * <b>200</b> - List of entries returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return List&lt;FileSystemEntryInfo&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Flux<FileSystemEntryInfo> getDrives() throws WebClientResponseException {
        ParameterizedTypeReference<FileSystemEntryInfo> localVarReturnType = new ParameterizedTypeReference<FileSystemEntryInfo>() {
        };
        return getDrivesRequestCreation().bodyToFlux(localVarReturnType);
    }

    /**
     * Gets available drives from the server&#39;s file system.
     * 
     * <p>
     * <b>200</b> - List of entries returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return ResponseEntity&lt;List&lt;FileSystemEntryInfo&gt;&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<List<FileSystemEntryInfo>>> getDrivesWithHttpInfo() throws WebClientResponseException {
        ParameterizedTypeReference<FileSystemEntryInfo> localVarReturnType = new ParameterizedTypeReference<FileSystemEntryInfo>() {
        };
        return getDrivesRequestCreation().toEntityList(localVarReturnType);
    }

    /**
     * Gets available drives from the server&#39;s file system.
     * 
     * <p>
     * <b>200</b> - List of entries returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getDrivesWithResponseSpec() throws WebClientResponseException {
        return getDrivesRequestCreation();
    }

    /**
     * Gets network paths.
     * 
     * <p>
     * <b>200</b> - Empty array returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return List&lt;FileSystemEntryInfo&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     * @deprecated
     */
    @Deprecated
    private ResponseSpec getNetworkSharesRequestCreation() throws WebClientResponseException {
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

        ParameterizedTypeReference<FileSystemEntryInfo> localVarReturnType = new ParameterizedTypeReference<FileSystemEntryInfo>() {
        };
        return apiClient.invokeAPI("/Environment/NetworkShares", HttpMethod.GET, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Gets network paths.
     * 
     * <p>
     * <b>200</b> - Empty array returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return List&lt;FileSystemEntryInfo&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Flux<FileSystemEntryInfo> getNetworkShares() throws WebClientResponseException {
        ParameterizedTypeReference<FileSystemEntryInfo> localVarReturnType = new ParameterizedTypeReference<FileSystemEntryInfo>() {
        };
        return getNetworkSharesRequestCreation().bodyToFlux(localVarReturnType);
    }

    /**
     * Gets network paths.
     * 
     * <p>
     * <b>200</b> - Empty array returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return ResponseEntity&lt;List&lt;FileSystemEntryInfo&gt;&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<List<FileSystemEntryInfo>>> getNetworkSharesWithHttpInfo()
            throws WebClientResponseException {
        ParameterizedTypeReference<FileSystemEntryInfo> localVarReturnType = new ParameterizedTypeReference<FileSystemEntryInfo>() {
        };
        return getNetworkSharesRequestCreation().toEntityList(localVarReturnType);
    }

    /**
     * Gets network paths.
     * 
     * <p>
     * <b>200</b> - Empty array returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getNetworkSharesWithResponseSpec() throws WebClientResponseException {
        return getNetworkSharesRequestCreation();
    }

    /**
     * Gets the parent path of a given path.
     * 
     * <p>
     * <b>200</b> - Success
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param path The path.
     * @return String
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getParentPathRequestCreation(String path) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'path' is set
        if (path == null) {
            throw new WebClientResponseException("Missing the required parameter 'path' when calling getParentPath",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "path", path));

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<String> localVarReturnType = new ParameterizedTypeReference<String>() {
        };
        return apiClient.invokeAPI("/Environment/ParentPath", HttpMethod.GET, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Gets the parent path of a given path.
     * 
     * <p>
     * <b>200</b> - Success
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param path The path.
     * @return String
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<String> getParentPath(String path) throws WebClientResponseException {
        ParameterizedTypeReference<String> localVarReturnType = new ParameterizedTypeReference<String>() {
        };
        return getParentPathRequestCreation(path).bodyToMono(localVarReturnType);
    }

    /**
     * Gets the parent path of a given path.
     * 
     * <p>
     * <b>200</b> - Success
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param path The path.
     * @return ResponseEntity&lt;String&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<String>> getParentPathWithHttpInfo(String path) throws WebClientResponseException {
        ParameterizedTypeReference<String> localVarReturnType = new ParameterizedTypeReference<String>() {
        };
        return getParentPathRequestCreation(path).toEntity(localVarReturnType);
    }

    /**
     * Gets the parent path of a given path.
     * 
     * <p>
     * <b>200</b> - Success
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param path The path.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getParentPathWithResponseSpec(String path) throws WebClientResponseException {
        return getParentPathRequestCreation(path);
    }

    /**
     * Validates path.
     * 
     * <p>
     * <b>204</b> - Path validated.
     * <p>
     * <b>404</b> - Path not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param validatePathDto Validate request object.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec validatePathRequestCreation(ValidatePathDto validatePathDto)
            throws WebClientResponseException {
        Object postBody = validatePathDto;
        // verify the required parameter 'validatePathDto' is set
        if (validatePathDto == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'validatePathDto' when calling validatePath",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
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
        return apiClient.invokeAPI("/Environment/ValidatePath", HttpMethod.POST, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Validates path.
     * 
     * <p>
     * <b>204</b> - Path validated.
     * <p>
     * <b>404</b> - Path not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param validatePathDto Validate request object.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> validatePath(ValidatePathDto validatePathDto) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return validatePathRequestCreation(validatePathDto).bodyToMono(localVarReturnType);
    }

    /**
     * Validates path.
     * 
     * <p>
     * <b>204</b> - Path validated.
     * <p>
     * <b>404</b> - Path not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param validatePathDto Validate request object.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> validatePathWithHttpInfo(ValidatePathDto validatePathDto)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return validatePathRequestCreation(validatePathDto).toEntity(localVarReturnType);
    }

    /**
     * Validates path.
     * 
     * <p>
     * <b>204</b> - Path validated.
     * <p>
     * <b>404</b> - Path not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param validatePathDto Validate request object.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec validatePathWithResponseSpec(ValidatePathDto validatePathDto)
            throws WebClientResponseException {
        return validatePathRequestCreation(validatePathDto);
    }
}
