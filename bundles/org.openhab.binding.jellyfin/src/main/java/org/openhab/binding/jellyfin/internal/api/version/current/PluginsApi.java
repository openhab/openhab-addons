package org.openhab.binding.jellyfin.internal.api.version.current;

import org.openhab.binding.jellyfin.internal.api.version.ApiClient;

import java.io.File;
import org.openhab.binding.jellyfin.internal.api.version.current.model.PluginInfo;
import org.openhab.binding.jellyfin.internal.api.version.current.model.ProblemDetails;
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
public class PluginsApi {
    private ApiClient apiClient;

    public PluginsApi() {
        this(new ApiClient());
    }

    @Autowired
    public PluginsApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    
    /**
     * Disable a plugin.
     * 
     * <p><b>204</b> - Plugin disabled.
     * <p><b>404</b> - Plugin not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param pluginId Plugin id.
     * @param version Plugin version.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec disablePluginRequestCreation(UUID pluginId, String version) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'pluginId' is set
        if (pluginId == null) {
            throw new WebClientResponseException("Missing the required parameter 'pluginId' when calling disablePlugin", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'version' is set
        if (version == null) {
            throw new WebClientResponseException("Missing the required parameter 'version' when calling disablePlugin", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("pluginId", pluginId);
        pathParams.put("version", version);

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

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return apiClient.invokeAPI("/Plugins/{pluginId}/{version}/Disable", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Disable a plugin.
     * 
     * <p><b>204</b> - Plugin disabled.
     * <p><b>404</b> - Plugin not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param pluginId Plugin id.
     * @param version Plugin version.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> disablePlugin(UUID pluginId, String version) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return disablePluginRequestCreation(pluginId, version).bodyToMono(localVarReturnType);
    }

    /**
     * Disable a plugin.
     * 
     * <p><b>204</b> - Plugin disabled.
     * <p><b>404</b> - Plugin not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param pluginId Plugin id.
     * @param version Plugin version.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> disablePluginWithHttpInfo(UUID pluginId, String version) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return disablePluginRequestCreation(pluginId, version).toEntity(localVarReturnType);
    }

    /**
     * Disable a plugin.
     * 
     * <p><b>204</b> - Plugin disabled.
     * <p><b>404</b> - Plugin not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param pluginId Plugin id.
     * @param version Plugin version.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec disablePluginWithResponseSpec(UUID pluginId, String version) throws WebClientResponseException {
        return disablePluginRequestCreation(pluginId, version);
    }

    /**
     * Enables a disabled plugin.
     * 
     * <p><b>204</b> - Plugin enabled.
     * <p><b>404</b> - Plugin not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param pluginId Plugin id.
     * @param version Plugin version.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec enablePluginRequestCreation(UUID pluginId, String version) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'pluginId' is set
        if (pluginId == null) {
            throw new WebClientResponseException("Missing the required parameter 'pluginId' when calling enablePlugin", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'version' is set
        if (version == null) {
            throw new WebClientResponseException("Missing the required parameter 'version' when calling enablePlugin", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("pluginId", pluginId);
        pathParams.put("version", version);

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

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return apiClient.invokeAPI("/Plugins/{pluginId}/{version}/Enable", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Enables a disabled plugin.
     * 
     * <p><b>204</b> - Plugin enabled.
     * <p><b>404</b> - Plugin not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param pluginId Plugin id.
     * @param version Plugin version.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> enablePlugin(UUID pluginId, String version) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return enablePluginRequestCreation(pluginId, version).bodyToMono(localVarReturnType);
    }

    /**
     * Enables a disabled plugin.
     * 
     * <p><b>204</b> - Plugin enabled.
     * <p><b>404</b> - Plugin not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param pluginId Plugin id.
     * @param version Plugin version.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> enablePluginWithHttpInfo(UUID pluginId, String version) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return enablePluginRequestCreation(pluginId, version).toEntity(localVarReturnType);
    }

    /**
     * Enables a disabled plugin.
     * 
     * <p><b>204</b> - Plugin enabled.
     * <p><b>404</b> - Plugin not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param pluginId Plugin id.
     * @param version Plugin version.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec enablePluginWithResponseSpec(UUID pluginId, String version) throws WebClientResponseException {
        return enablePluginRequestCreation(pluginId, version);
    }

    /**
     * Gets plugin configuration.
     * 
     * <p><b>200</b> - Plugin configuration returned.
     * <p><b>404</b> - Plugin not found or plugin configuration not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param pluginId Plugin id.
     * @return Object
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getPluginConfigurationRequestCreation(UUID pluginId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'pluginId' is set
        if (pluginId == null) {
            throw new WebClientResponseException("Missing the required parameter 'pluginId' when calling getPluginConfiguration", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("pluginId", pluginId);

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

        ParameterizedTypeReference<Object> localVarReturnType = new ParameterizedTypeReference<Object>() {};
        return apiClient.invokeAPI("/Plugins/{pluginId}/Configuration", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets plugin configuration.
     * 
     * <p><b>200</b> - Plugin configuration returned.
     * <p><b>404</b> - Plugin not found or plugin configuration not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param pluginId Plugin id.
     * @return Object
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Object> getPluginConfiguration(UUID pluginId) throws WebClientResponseException {
        ParameterizedTypeReference<Object> localVarReturnType = new ParameterizedTypeReference<Object>() {};
        return getPluginConfigurationRequestCreation(pluginId).bodyToMono(localVarReturnType);
    }

    /**
     * Gets plugin configuration.
     * 
     * <p><b>200</b> - Plugin configuration returned.
     * <p><b>404</b> - Plugin not found or plugin configuration not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param pluginId Plugin id.
     * @return ResponseEntity&lt;Object&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Object>> getPluginConfigurationWithHttpInfo(UUID pluginId) throws WebClientResponseException {
        ParameterizedTypeReference<Object> localVarReturnType = new ParameterizedTypeReference<Object>() {};
        return getPluginConfigurationRequestCreation(pluginId).toEntity(localVarReturnType);
    }

    /**
     * Gets plugin configuration.
     * 
     * <p><b>200</b> - Plugin configuration returned.
     * <p><b>404</b> - Plugin not found or plugin configuration not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param pluginId Plugin id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getPluginConfigurationWithResponseSpec(UUID pluginId) throws WebClientResponseException {
        return getPluginConfigurationRequestCreation(pluginId);
    }

    /**
     * Gets a plugin&#39;s image.
     * 
     * <p><b>200</b> - Plugin image returned.
     * <p><b>404</b> - Not Found
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param pluginId Plugin id.
     * @param version Plugin version.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getPluginImageRequestCreation(UUID pluginId, String version) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'pluginId' is set
        if (pluginId == null) {
            throw new WebClientResponseException("Missing the required parameter 'pluginId' when calling getPluginImage", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'version' is set
        if (version == null) {
            throw new WebClientResponseException("Missing the required parameter 'version' when calling getPluginImage", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("pluginId", pluginId);
        pathParams.put("version", version);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "image/*", "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return apiClient.invokeAPI("/Plugins/{pluginId}/{version}/Image", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets a plugin&#39;s image.
     * 
     * <p><b>200</b> - Plugin image returned.
     * <p><b>404</b> - Not Found
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param pluginId Plugin id.
     * @param version Plugin version.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<File> getPluginImage(UUID pluginId, String version) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return getPluginImageRequestCreation(pluginId, version).bodyToMono(localVarReturnType);
    }

    /**
     * Gets a plugin&#39;s image.
     * 
     * <p><b>200</b> - Plugin image returned.
     * <p><b>404</b> - Not Found
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param pluginId Plugin id.
     * @param version Plugin version.
     * @return ResponseEntity&lt;File&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<File>> getPluginImageWithHttpInfo(UUID pluginId, String version) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return getPluginImageRequestCreation(pluginId, version).toEntity(localVarReturnType);
    }

    /**
     * Gets a plugin&#39;s image.
     * 
     * <p><b>200</b> - Plugin image returned.
     * <p><b>404</b> - Not Found
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param pluginId Plugin id.
     * @param version Plugin version.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getPluginImageWithResponseSpec(UUID pluginId, String version) throws WebClientResponseException {
        return getPluginImageRequestCreation(pluginId, version);
    }

    /**
     * Gets a plugin&#39;s manifest.
     * 
     * <p><b>204</b> - Plugin manifest returned.
     * <p><b>404</b> - Plugin not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param pluginId Plugin id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getPluginManifestRequestCreation(UUID pluginId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'pluginId' is set
        if (pluginId == null) {
            throw new WebClientResponseException("Missing the required parameter 'pluginId' when calling getPluginManifest", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("pluginId", pluginId);

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

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return apiClient.invokeAPI("/Plugins/{pluginId}/Manifest", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets a plugin&#39;s manifest.
     * 
     * <p><b>204</b> - Plugin manifest returned.
     * <p><b>404</b> - Plugin not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param pluginId Plugin id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> getPluginManifest(UUID pluginId) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return getPluginManifestRequestCreation(pluginId).bodyToMono(localVarReturnType);
    }

    /**
     * Gets a plugin&#39;s manifest.
     * 
     * <p><b>204</b> - Plugin manifest returned.
     * <p><b>404</b> - Plugin not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param pluginId Plugin id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> getPluginManifestWithHttpInfo(UUID pluginId) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return getPluginManifestRequestCreation(pluginId).toEntity(localVarReturnType);
    }

    /**
     * Gets a plugin&#39;s manifest.
     * 
     * <p><b>204</b> - Plugin manifest returned.
     * <p><b>404</b> - Plugin not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param pluginId Plugin id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getPluginManifestWithResponseSpec(UUID pluginId) throws WebClientResponseException {
        return getPluginManifestRequestCreation(pluginId);
    }

    /**
     * Gets a list of currently installed plugins.
     * 
     * <p><b>200</b> - Installed plugins returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @return List&lt;PluginInfo&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getPluginsRequestCreation() throws WebClientResponseException {
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

        ParameterizedTypeReference<PluginInfo> localVarReturnType = new ParameterizedTypeReference<PluginInfo>() {};
        return apiClient.invokeAPI("/Plugins", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets a list of currently installed plugins.
     * 
     * <p><b>200</b> - Installed plugins returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @return List&lt;PluginInfo&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Flux<PluginInfo> getPlugins() throws WebClientResponseException {
        ParameterizedTypeReference<PluginInfo> localVarReturnType = new ParameterizedTypeReference<PluginInfo>() {};
        return getPluginsRequestCreation().bodyToFlux(localVarReturnType);
    }

    /**
     * Gets a list of currently installed plugins.
     * 
     * <p><b>200</b> - Installed plugins returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @return ResponseEntity&lt;List&lt;PluginInfo&gt;&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<List<PluginInfo>>> getPluginsWithHttpInfo() throws WebClientResponseException {
        ParameterizedTypeReference<PluginInfo> localVarReturnType = new ParameterizedTypeReference<PluginInfo>() {};
        return getPluginsRequestCreation().toEntityList(localVarReturnType);
    }

    /**
     * Gets a list of currently installed plugins.
     * 
     * <p><b>200</b> - Installed plugins returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getPluginsWithResponseSpec() throws WebClientResponseException {
        return getPluginsRequestCreation();
    }

    /**
     * Uninstalls a plugin.
     * 
     * <p><b>204</b> - Plugin uninstalled.
     * <p><b>404</b> - Plugin not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param pluginId Plugin id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     * @deprecated
     */
    @Deprecated
    private ResponseSpec uninstallPluginRequestCreation(UUID pluginId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'pluginId' is set
        if (pluginId == null) {
            throw new WebClientResponseException("Missing the required parameter 'pluginId' when calling uninstallPlugin", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("pluginId", pluginId);

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

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return apiClient.invokeAPI("/Plugins/{pluginId}", HttpMethod.DELETE, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Uninstalls a plugin.
     * 
     * <p><b>204</b> - Plugin uninstalled.
     * <p><b>404</b> - Plugin not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param pluginId Plugin id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> uninstallPlugin(UUID pluginId) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return uninstallPluginRequestCreation(pluginId).bodyToMono(localVarReturnType);
    }

    /**
     * Uninstalls a plugin.
     * 
     * <p><b>204</b> - Plugin uninstalled.
     * <p><b>404</b> - Plugin not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param pluginId Plugin id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> uninstallPluginWithHttpInfo(UUID pluginId) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return uninstallPluginRequestCreation(pluginId).toEntity(localVarReturnType);
    }

    /**
     * Uninstalls a plugin.
     * 
     * <p><b>204</b> - Plugin uninstalled.
     * <p><b>404</b> - Plugin not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param pluginId Plugin id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec uninstallPluginWithResponseSpec(UUID pluginId) throws WebClientResponseException {
        return uninstallPluginRequestCreation(pluginId);
    }

    /**
     * Uninstalls a plugin by version.
     * 
     * <p><b>204</b> - Plugin uninstalled.
     * <p><b>404</b> - Plugin not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param pluginId Plugin id.
     * @param version Plugin version.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec uninstallPluginByVersionRequestCreation(UUID pluginId, String version) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'pluginId' is set
        if (pluginId == null) {
            throw new WebClientResponseException("Missing the required parameter 'pluginId' when calling uninstallPluginByVersion", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'version' is set
        if (version == null) {
            throw new WebClientResponseException("Missing the required parameter 'version' when calling uninstallPluginByVersion", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("pluginId", pluginId);
        pathParams.put("version", version);

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

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return apiClient.invokeAPI("/Plugins/{pluginId}/{version}", HttpMethod.DELETE, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Uninstalls a plugin by version.
     * 
     * <p><b>204</b> - Plugin uninstalled.
     * <p><b>404</b> - Plugin not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param pluginId Plugin id.
     * @param version Plugin version.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> uninstallPluginByVersion(UUID pluginId, String version) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return uninstallPluginByVersionRequestCreation(pluginId, version).bodyToMono(localVarReturnType);
    }

    /**
     * Uninstalls a plugin by version.
     * 
     * <p><b>204</b> - Plugin uninstalled.
     * <p><b>404</b> - Plugin not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param pluginId Plugin id.
     * @param version Plugin version.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> uninstallPluginByVersionWithHttpInfo(UUID pluginId, String version) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return uninstallPluginByVersionRequestCreation(pluginId, version).toEntity(localVarReturnType);
    }

    /**
     * Uninstalls a plugin by version.
     * 
     * <p><b>204</b> - Plugin uninstalled.
     * <p><b>404</b> - Plugin not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param pluginId Plugin id.
     * @param version Plugin version.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec uninstallPluginByVersionWithResponseSpec(UUID pluginId, String version) throws WebClientResponseException {
        return uninstallPluginByVersionRequestCreation(pluginId, version);
    }

    /**
     * Updates plugin configuration.
     * Accepts plugin configuration as JSON body.
     * <p><b>204</b> - Plugin configuration updated.
     * <p><b>404</b> - Plugin not found or plugin does not have configuration.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param pluginId Plugin id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec updatePluginConfigurationRequestCreation(UUID pluginId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'pluginId' is set
        if (pluginId == null) {
            throw new WebClientResponseException("Missing the required parameter 'pluginId' when calling updatePluginConfiguration", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("pluginId", pluginId);

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

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return apiClient.invokeAPI("/Plugins/{pluginId}/Configuration", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Updates plugin configuration.
     * Accepts plugin configuration as JSON body.
     * <p><b>204</b> - Plugin configuration updated.
     * <p><b>404</b> - Plugin not found or plugin does not have configuration.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param pluginId Plugin id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> updatePluginConfiguration(UUID pluginId) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return updatePluginConfigurationRequestCreation(pluginId).bodyToMono(localVarReturnType);
    }

    /**
     * Updates plugin configuration.
     * Accepts plugin configuration as JSON body.
     * <p><b>204</b> - Plugin configuration updated.
     * <p><b>404</b> - Plugin not found or plugin does not have configuration.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param pluginId Plugin id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> updatePluginConfigurationWithHttpInfo(UUID pluginId) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return updatePluginConfigurationRequestCreation(pluginId).toEntity(localVarReturnType);
    }

    /**
     * Updates plugin configuration.
     * Accepts plugin configuration as JSON body.
     * <p><b>204</b> - Plugin configuration updated.
     * <p><b>404</b> - Plugin not found or plugin does not have configuration.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param pluginId Plugin id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec updatePluginConfigurationWithResponseSpec(UUID pluginId) throws WebClientResponseException {
        return updatePluginConfigurationRequestCreation(pluginId);
    }
}
