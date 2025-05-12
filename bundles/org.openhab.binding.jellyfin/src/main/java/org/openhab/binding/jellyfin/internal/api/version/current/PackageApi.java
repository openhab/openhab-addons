package org.openhab.binding.jellyfin.internal.api.version.current;

import org.openhab.binding.jellyfin.internal.api.version.ApiClient;

import org.openhab.binding.jellyfin.internal.api.version.current.model.PackageInfo;
import org.openhab.binding.jellyfin.internal.api.version.current.model.ProblemDetails;
import org.openhab.binding.jellyfin.internal.api.version.current.model.RepositoryInfo;
import java.util.UUID;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.12.0")
public class PackageApi {
    private ApiClient apiClient;

    public PackageApi() {
        this(new ApiClient());
    }

    @Autowired
    public PackageApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    
    /**
     * Cancels a package installation.
     * 
     * <p><b>204</b> - Installation cancelled.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param packageId Installation Id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec cancelPackageInstallationRequestCreation(UUID packageId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'packageId' is set
        if (packageId == null) {
            throw new WebClientResponseException("Missing the required parameter 'packageId' when calling cancelPackageInstallation", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("packageId", packageId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return apiClient.invokeAPI("/Packages/Installing/{packageId}", HttpMethod.DELETE, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Cancels a package installation.
     * 
     * <p><b>204</b> - Installation cancelled.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param packageId Installation Id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> cancelPackageInstallation(UUID packageId) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return cancelPackageInstallationRequestCreation(packageId).bodyToMono(localVarReturnType);
    }

    /**
     * Cancels a package installation.
     * 
     * <p><b>204</b> - Installation cancelled.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param packageId Installation Id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> cancelPackageInstallationWithHttpInfo(UUID packageId) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return cancelPackageInstallationRequestCreation(packageId).toEntity(localVarReturnType);
    }

    /**
     * Cancels a package installation.
     * 
     * <p><b>204</b> - Installation cancelled.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param packageId Installation Id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec cancelPackageInstallationWithResponseSpec(UUID packageId) throws WebClientResponseException {
        return cancelPackageInstallationRequestCreation(packageId);
    }

    /**
     * Gets a package by name or assembly GUID.
     * 
     * <p><b>200</b> - Package retrieved.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param name The name of the package.
     * @param assemblyGuid The GUID of the associated assembly.
     * @return PackageInfo
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getPackageInfoRequestCreation(String name, UUID assemblyGuid) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'name' is set
        if (name == null) {
            throw new WebClientResponseException("Missing the required parameter 'name' when calling getPackageInfo", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("name", name);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "assemblyGuid", assemblyGuid));
        
        final String[] localVarAccepts = { 
            "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<PackageInfo> localVarReturnType = new ParameterizedTypeReference<PackageInfo>() {};
        return apiClient.invokeAPI("/Packages/{name}", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets a package by name or assembly GUID.
     * 
     * <p><b>200</b> - Package retrieved.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param name The name of the package.
     * @param assemblyGuid The GUID of the associated assembly.
     * @return PackageInfo
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<PackageInfo> getPackageInfo(String name, UUID assemblyGuid) throws WebClientResponseException {
        ParameterizedTypeReference<PackageInfo> localVarReturnType = new ParameterizedTypeReference<PackageInfo>() {};
        return getPackageInfoRequestCreation(name, assemblyGuid).bodyToMono(localVarReturnType);
    }

    /**
     * Gets a package by name or assembly GUID.
     * 
     * <p><b>200</b> - Package retrieved.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param name The name of the package.
     * @param assemblyGuid The GUID of the associated assembly.
     * @return ResponseEntity&lt;PackageInfo&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<PackageInfo>> getPackageInfoWithHttpInfo(String name, UUID assemblyGuid) throws WebClientResponseException {
        ParameterizedTypeReference<PackageInfo> localVarReturnType = new ParameterizedTypeReference<PackageInfo>() {};
        return getPackageInfoRequestCreation(name, assemblyGuid).toEntity(localVarReturnType);
    }

    /**
     * Gets a package by name or assembly GUID.
     * 
     * <p><b>200</b> - Package retrieved.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param name The name of the package.
     * @param assemblyGuid The GUID of the associated assembly.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getPackageInfoWithResponseSpec(String name, UUID assemblyGuid) throws WebClientResponseException {
        return getPackageInfoRequestCreation(name, assemblyGuid);
    }

    /**
     * Gets available packages.
     * 
     * <p><b>200</b> - Available packages returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @return List&lt;PackageInfo&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getPackagesRequestCreation() throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<PackageInfo> localVarReturnType = new ParameterizedTypeReference<PackageInfo>() {};
        return apiClient.invokeAPI("/Packages", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets available packages.
     * 
     * <p><b>200</b> - Available packages returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @return List&lt;PackageInfo&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Flux<PackageInfo> getPackages() throws WebClientResponseException {
        ParameterizedTypeReference<PackageInfo> localVarReturnType = new ParameterizedTypeReference<PackageInfo>() {};
        return getPackagesRequestCreation().bodyToFlux(localVarReturnType);
    }

    /**
     * Gets available packages.
     * 
     * <p><b>200</b> - Available packages returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @return ResponseEntity&lt;List&lt;PackageInfo&gt;&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<List<PackageInfo>>> getPackagesWithHttpInfo() throws WebClientResponseException {
        ParameterizedTypeReference<PackageInfo> localVarReturnType = new ParameterizedTypeReference<PackageInfo>() {};
        return getPackagesRequestCreation().toEntityList(localVarReturnType);
    }

    /**
     * Gets available packages.
     * 
     * <p><b>200</b> - Available packages returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getPackagesWithResponseSpec() throws WebClientResponseException {
        return getPackagesRequestCreation();
    }

    /**
     * Gets all package repositories.
     * 
     * <p><b>200</b> - Package repositories returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @return List&lt;RepositoryInfo&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getRepositoriesRequestCreation() throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<RepositoryInfo> localVarReturnType = new ParameterizedTypeReference<RepositoryInfo>() {};
        return apiClient.invokeAPI("/Repositories", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets all package repositories.
     * 
     * <p><b>200</b> - Package repositories returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @return List&lt;RepositoryInfo&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Flux<RepositoryInfo> getRepositories() throws WebClientResponseException {
        ParameterizedTypeReference<RepositoryInfo> localVarReturnType = new ParameterizedTypeReference<RepositoryInfo>() {};
        return getRepositoriesRequestCreation().bodyToFlux(localVarReturnType);
    }

    /**
     * Gets all package repositories.
     * 
     * <p><b>200</b> - Package repositories returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @return ResponseEntity&lt;List&lt;RepositoryInfo&gt;&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<List<RepositoryInfo>>> getRepositoriesWithHttpInfo() throws WebClientResponseException {
        ParameterizedTypeReference<RepositoryInfo> localVarReturnType = new ParameterizedTypeReference<RepositoryInfo>() {};
        return getRepositoriesRequestCreation().toEntityList(localVarReturnType);
    }

    /**
     * Gets all package repositories.
     * 
     * <p><b>200</b> - Package repositories returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getRepositoriesWithResponseSpec() throws WebClientResponseException {
        return getRepositoriesRequestCreation();
    }

    /**
     * Installs a package.
     * 
     * <p><b>204</b> - Package found.
     * <p><b>404</b> - Package not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param name Package name.
     * @param assemblyGuid GUID of the associated assembly.
     * @param version Optional version. Defaults to latest version.
     * @param repositoryUrl Optional. Specify the repository to install from.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec installPackageRequestCreation(String name, UUID assemblyGuid, String version, String repositoryUrl) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'name' is set
        if (name == null) {
            throw new WebClientResponseException("Missing the required parameter 'name' when calling installPackage", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("name", name);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "assemblyGuid", assemblyGuid));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "version", version));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "repositoryUrl", repositoryUrl));
        
        final String[] localVarAccepts = { 
            "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return apiClient.invokeAPI("/Packages/Installed/{name}", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Installs a package.
     * 
     * <p><b>204</b> - Package found.
     * <p><b>404</b> - Package not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param name Package name.
     * @param assemblyGuid GUID of the associated assembly.
     * @param version Optional version. Defaults to latest version.
     * @param repositoryUrl Optional. Specify the repository to install from.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> installPackage(String name, UUID assemblyGuid, String version, String repositoryUrl) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return installPackageRequestCreation(name, assemblyGuid, version, repositoryUrl).bodyToMono(localVarReturnType);
    }

    /**
     * Installs a package.
     * 
     * <p><b>204</b> - Package found.
     * <p><b>404</b> - Package not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param name Package name.
     * @param assemblyGuid GUID of the associated assembly.
     * @param version Optional version. Defaults to latest version.
     * @param repositoryUrl Optional. Specify the repository to install from.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> installPackageWithHttpInfo(String name, UUID assemblyGuid, String version, String repositoryUrl) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return installPackageRequestCreation(name, assemblyGuid, version, repositoryUrl).toEntity(localVarReturnType);
    }

    /**
     * Installs a package.
     * 
     * <p><b>204</b> - Package found.
     * <p><b>404</b> - Package not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param name Package name.
     * @param assemblyGuid GUID of the associated assembly.
     * @param version Optional version. Defaults to latest version.
     * @param repositoryUrl Optional. Specify the repository to install from.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec installPackageWithResponseSpec(String name, UUID assemblyGuid, String version, String repositoryUrl) throws WebClientResponseException {
        return installPackageRequestCreation(name, assemblyGuid, version, repositoryUrl);
    }

    /**
     * Sets the enabled and existing package repositories.
     * 
     * <p><b>204</b> - Package repositories saved.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param repositoryInfo The list of package repositories.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec setRepositoriesRequestCreation(List<RepositoryInfo> repositoryInfo) throws WebClientResponseException {
        Object postBody = repositoryInfo;
        // verify the required parameter 'repositoryInfo' is set
        if (repositoryInfo == null) {
            throw new WebClientResponseException("Missing the required parameter 'repositoryInfo' when calling setRepositories", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { 
            "application/json", "text/json", "application/*+json"
        };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return apiClient.invokeAPI("/Repositories", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Sets the enabled and existing package repositories.
     * 
     * <p><b>204</b> - Package repositories saved.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param repositoryInfo The list of package repositories.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> setRepositories(List<RepositoryInfo> repositoryInfo) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return setRepositoriesRequestCreation(repositoryInfo).bodyToMono(localVarReturnType);
    }

    /**
     * Sets the enabled and existing package repositories.
     * 
     * <p><b>204</b> - Package repositories saved.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param repositoryInfo The list of package repositories.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> setRepositoriesWithHttpInfo(List<RepositoryInfo> repositoryInfo) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return setRepositoriesRequestCreation(repositoryInfo).toEntity(localVarReturnType);
    }

    /**
     * Sets the enabled and existing package repositories.
     * 
     * <p><b>204</b> - Package repositories saved.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param repositoryInfo The list of package repositories.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec setRepositoriesWithResponseSpec(List<RepositoryInfo> repositoryInfo) throws WebClientResponseException {
        return setRepositoriesRequestCreation(repositoryInfo);
    }
}
