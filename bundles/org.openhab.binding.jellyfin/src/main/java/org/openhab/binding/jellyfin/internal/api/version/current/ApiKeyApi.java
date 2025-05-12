package org.openhab.binding.jellyfin.internal.api.version.current;

import org.openhab.binding.jellyfin.internal.api.version.ApiClient;

import org.openhab.binding.jellyfin.internal.api.version.current.model.AuthenticationInfoQueryResult;

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
public class ApiKeyApi {
    private ApiClient apiClient;

    public ApiKeyApi() {
        this(new ApiClient());
    }

    @Autowired
    public ApiKeyApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    
    /**
     * Create a new api key.
     * 
     * <p><b>204</b> - Api key created.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param app Name of the app using the authentication key.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec createKeyRequestCreation(String app) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'app' is set
        if (app == null) {
            throw new WebClientResponseException("Missing the required parameter 'app' when calling createKey", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "app", app));
        
        final String[] localVarAccepts = { };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return apiClient.invokeAPI("/Auth/Keys", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Create a new api key.
     * 
     * <p><b>204</b> - Api key created.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param app Name of the app using the authentication key.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> createKey(String app) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return createKeyRequestCreation(app).bodyToMono(localVarReturnType);
    }

    /**
     * Create a new api key.
     * 
     * <p><b>204</b> - Api key created.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param app Name of the app using the authentication key.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> createKeyWithHttpInfo(String app) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return createKeyRequestCreation(app).toEntity(localVarReturnType);
    }

    /**
     * Create a new api key.
     * 
     * <p><b>204</b> - Api key created.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param app Name of the app using the authentication key.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec createKeyWithResponseSpec(String app) throws WebClientResponseException {
        return createKeyRequestCreation(app);
    }

    /**
     * Get all keys.
     * 
     * <p><b>200</b> - Api keys retrieved.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @return AuthenticationInfoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getKeysRequestCreation() throws WebClientResponseException {
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

        ParameterizedTypeReference<AuthenticationInfoQueryResult> localVarReturnType = new ParameterizedTypeReference<AuthenticationInfoQueryResult>() {};
        return apiClient.invokeAPI("/Auth/Keys", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get all keys.
     * 
     * <p><b>200</b> - Api keys retrieved.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @return AuthenticationInfoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<AuthenticationInfoQueryResult> getKeys() throws WebClientResponseException {
        ParameterizedTypeReference<AuthenticationInfoQueryResult> localVarReturnType = new ParameterizedTypeReference<AuthenticationInfoQueryResult>() {};
        return getKeysRequestCreation().bodyToMono(localVarReturnType);
    }

    /**
     * Get all keys.
     * 
     * <p><b>200</b> - Api keys retrieved.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @return ResponseEntity&lt;AuthenticationInfoQueryResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<AuthenticationInfoQueryResult>> getKeysWithHttpInfo() throws WebClientResponseException {
        ParameterizedTypeReference<AuthenticationInfoQueryResult> localVarReturnType = new ParameterizedTypeReference<AuthenticationInfoQueryResult>() {};
        return getKeysRequestCreation().toEntity(localVarReturnType);
    }

    /**
     * Get all keys.
     * 
     * <p><b>200</b> - Api keys retrieved.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getKeysWithResponseSpec() throws WebClientResponseException {
        return getKeysRequestCreation();
    }

    /**
     * Remove an api key.
     * 
     * <p><b>204</b> - Api key deleted.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param key The access token to delete.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec revokeKeyRequestCreation(String key) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'key' is set
        if (key == null) {
            throw new WebClientResponseException("Missing the required parameter 'key' when calling revokeKey", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("key", key);

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
        return apiClient.invokeAPI("/Auth/Keys/{key}", HttpMethod.DELETE, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Remove an api key.
     * 
     * <p><b>204</b> - Api key deleted.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param key The access token to delete.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> revokeKey(String key) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return revokeKeyRequestCreation(key).bodyToMono(localVarReturnType);
    }

    /**
     * Remove an api key.
     * 
     * <p><b>204</b> - Api key deleted.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param key The access token to delete.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> revokeKeyWithHttpInfo(String key) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return revokeKeyRequestCreation(key).toEntity(localVarReturnType);
    }

    /**
     * Remove an api key.
     * 
     * <p><b>204</b> - Api key deleted.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param key The access token to delete.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec revokeKeyWithResponseSpec(String key) throws WebClientResponseException {
        return revokeKeyRequestCreation(key);
    }
}
