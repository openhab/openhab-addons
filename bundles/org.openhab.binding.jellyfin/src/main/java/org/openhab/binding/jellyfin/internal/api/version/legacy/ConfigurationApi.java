package org.openhab.binding.jellyfin.internal.api.version.legacy;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openhab.binding.jellyfin.internal.api.version.ApiClient;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.MediaEncoderPathDto;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.MetadataOptions;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.ServerConfiguration;
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

import reactor.core.publisher.Mono;

@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.12.0")
public class ConfigurationApi {
    private ApiClient apiClient;

    public ConfigurationApi() {
        this(new ApiClient());
    }

    @Autowired
    public ConfigurationApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Gets application configuration.
     * 
     * <p>
     * <b>200</b> - Application configuration returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return ServerConfiguration
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getConfigurationRequestCreation() throws WebClientResponseException {
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

        ParameterizedTypeReference<ServerConfiguration> localVarReturnType = new ParameterizedTypeReference<ServerConfiguration>() {
        };
        return apiClient.invokeAPI("/System/Configuration", HttpMethod.GET, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Gets application configuration.
     * 
     * <p>
     * <b>200</b> - Application configuration returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return ServerConfiguration
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ServerConfiguration> getConfiguration() throws WebClientResponseException {
        ParameterizedTypeReference<ServerConfiguration> localVarReturnType = new ParameterizedTypeReference<ServerConfiguration>() {
        };
        return getConfigurationRequestCreation().bodyToMono(localVarReturnType);
    }

    /**
     * Gets application configuration.
     * 
     * <p>
     * <b>200</b> - Application configuration returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return ResponseEntity&lt;ServerConfiguration&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<ServerConfiguration>> getConfigurationWithHttpInfo() throws WebClientResponseException {
        ParameterizedTypeReference<ServerConfiguration> localVarReturnType = new ParameterizedTypeReference<ServerConfiguration>() {
        };
        return getConfigurationRequestCreation().toEntity(localVarReturnType);
    }

    /**
     * Gets application configuration.
     * 
     * <p>
     * <b>200</b> - Application configuration returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getConfigurationWithResponseSpec() throws WebClientResponseException {
        return getConfigurationRequestCreation();
    }

    /**
     * Gets a default MetadataOptions object.
     * 
     * <p>
     * <b>200</b> - Metadata options returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return MetadataOptions
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getDefaultMetadataOptionsRequestCreation() throws WebClientResponseException {
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

        ParameterizedTypeReference<MetadataOptions> localVarReturnType = new ParameterizedTypeReference<MetadataOptions>() {
        };
        return apiClient.invokeAPI("/System/Configuration/MetadataOptions/Default", HttpMethod.GET, pathParams,
                queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType,
                localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets a default MetadataOptions object.
     * 
     * <p>
     * <b>200</b> - Metadata options returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return MetadataOptions
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<MetadataOptions> getDefaultMetadataOptions() throws WebClientResponseException {
        ParameterizedTypeReference<MetadataOptions> localVarReturnType = new ParameterizedTypeReference<MetadataOptions>() {
        };
        return getDefaultMetadataOptionsRequestCreation().bodyToMono(localVarReturnType);
    }

    /**
     * Gets a default MetadataOptions object.
     * 
     * <p>
     * <b>200</b> - Metadata options returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return ResponseEntity&lt;MetadataOptions&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<MetadataOptions>> getDefaultMetadataOptionsWithHttpInfo()
            throws WebClientResponseException {
        ParameterizedTypeReference<MetadataOptions> localVarReturnType = new ParameterizedTypeReference<MetadataOptions>() {
        };
        return getDefaultMetadataOptionsRequestCreation().toEntity(localVarReturnType);
    }

    /**
     * Gets a default MetadataOptions object.
     * 
     * <p>
     * <b>200</b> - Metadata options returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getDefaultMetadataOptionsWithResponseSpec() throws WebClientResponseException {
        return getDefaultMetadataOptionsRequestCreation();
    }

    /**
     * Gets a named configuration.
     * 
     * <p>
     * <b>200</b> - Configuration returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param key Configuration key.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getNamedConfigurationRequestCreation(String key) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'key' is set
        if (key == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'key' when calling getNamedConfiguration",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("key", key);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { "application/json" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return apiClient.invokeAPI("/System/Configuration/{key}", HttpMethod.GET, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Gets a named configuration.
     * 
     * <p>
     * <b>200</b> - Configuration returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param key Configuration key.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<File> getNamedConfiguration(String key) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return getNamedConfigurationRequestCreation(key).bodyToMono(localVarReturnType);
    }

    /**
     * Gets a named configuration.
     * 
     * <p>
     * <b>200</b> - Configuration returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param key Configuration key.
     * @return ResponseEntity&lt;File&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<File>> getNamedConfigurationWithHttpInfo(String key) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return getNamedConfigurationRequestCreation(key).toEntity(localVarReturnType);
    }

    /**
     * Gets a named configuration.
     * 
     * <p>
     * <b>200</b> - Configuration returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param key Configuration key.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getNamedConfigurationWithResponseSpec(String key) throws WebClientResponseException {
        return getNamedConfigurationRequestCreation(key);
    }

    /**
     * Updates application configuration.
     * 
     * <p>
     * <b>204</b> - Configuration updated.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param serverConfiguration Configuration.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec updateConfigurationRequestCreation(ServerConfiguration serverConfiguration)
            throws WebClientResponseException {
        Object postBody = serverConfiguration;
        // verify the required parameter 'serverConfiguration' is set
        if (serverConfiguration == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'serverConfiguration' when calling updateConfiguration",
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
        return apiClient.invokeAPI("/System/Configuration", HttpMethod.POST, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Updates application configuration.
     * 
     * <p>
     * <b>204</b> - Configuration updated.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param serverConfiguration Configuration.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> updateConfiguration(ServerConfiguration serverConfiguration) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return updateConfigurationRequestCreation(serverConfiguration).bodyToMono(localVarReturnType);
    }

    /**
     * Updates application configuration.
     * 
     * <p>
     * <b>204</b> - Configuration updated.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param serverConfiguration Configuration.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> updateConfigurationWithHttpInfo(ServerConfiguration serverConfiguration)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return updateConfigurationRequestCreation(serverConfiguration).toEntity(localVarReturnType);
    }

    /**
     * Updates application configuration.
     * 
     * <p>
     * <b>204</b> - Configuration updated.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param serverConfiguration Configuration.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec updateConfigurationWithResponseSpec(ServerConfiguration serverConfiguration)
            throws WebClientResponseException {
        return updateConfigurationRequestCreation(serverConfiguration);
    }

    /**
     * Updates the path to the media encoder.
     * 
     * <p>
     * <b>204</b> - Media encoder path updated.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param mediaEncoderPathDto Media encoder path form body.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     * @deprecated
     */
    @Deprecated
    private ResponseSpec updateMediaEncoderPathRequestCreation(MediaEncoderPathDto mediaEncoderPathDto)
            throws WebClientResponseException {
        Object postBody = mediaEncoderPathDto;
        // verify the required parameter 'mediaEncoderPathDto' is set
        if (mediaEncoderPathDto == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'mediaEncoderPathDto' when calling updateMediaEncoderPath",
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
        return apiClient.invokeAPI("/System/MediaEncoder/Path", HttpMethod.POST, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Updates the path to the media encoder.
     * 
     * <p>
     * <b>204</b> - Media encoder path updated.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param mediaEncoderPathDto Media encoder path form body.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> updateMediaEncoderPath(MediaEncoderPathDto mediaEncoderPathDto)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return updateMediaEncoderPathRequestCreation(mediaEncoderPathDto).bodyToMono(localVarReturnType);
    }

    /**
     * Updates the path to the media encoder.
     * 
     * <p>
     * <b>204</b> - Media encoder path updated.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param mediaEncoderPathDto Media encoder path form body.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> updateMediaEncoderPathWithHttpInfo(MediaEncoderPathDto mediaEncoderPathDto)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return updateMediaEncoderPathRequestCreation(mediaEncoderPathDto).toEntity(localVarReturnType);
    }

    /**
     * Updates the path to the media encoder.
     * 
     * <p>
     * <b>204</b> - Media encoder path updated.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param mediaEncoderPathDto Media encoder path form body.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec updateMediaEncoderPathWithResponseSpec(MediaEncoderPathDto mediaEncoderPathDto)
            throws WebClientResponseException {
        return updateMediaEncoderPathRequestCreation(mediaEncoderPathDto);
    }

    /**
     * Updates named configuration.
     * 
     * <p>
     * <b>204</b> - Named configuration updated.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param key Configuration key.
     * @param body Configuration.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec updateNamedConfigurationRequestCreation(String key, Object body)
            throws WebClientResponseException {
        Object postBody = body;
        // verify the required parameter 'key' is set
        if (key == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'key' when calling updateNamedConfiguration",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'body' is set
        if (body == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'body' when calling updateNamedConfiguration",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("key", key);

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
        return apiClient.invokeAPI("/System/Configuration/{key}", HttpMethod.POST, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Updates named configuration.
     * 
     * <p>
     * <b>204</b> - Named configuration updated.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param key Configuration key.
     * @param body Configuration.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> updateNamedConfiguration(String key, Object body) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return updateNamedConfigurationRequestCreation(key, body).bodyToMono(localVarReturnType);
    }

    /**
     * Updates named configuration.
     * 
     * <p>
     * <b>204</b> - Named configuration updated.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param key Configuration key.
     * @param body Configuration.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> updateNamedConfigurationWithHttpInfo(String key, Object body)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return updateNamedConfigurationRequestCreation(key, body).toEntity(localVarReturnType);
    }

    /**
     * Updates named configuration.
     * 
     * <p>
     * <b>204</b> - Named configuration updated.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param key Configuration key.
     * @param body Configuration.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec updateNamedConfigurationWithResponseSpec(String key, Object body)
            throws WebClientResponseException {
        return updateNamedConfigurationRequestCreation(key, body);
    }
}
