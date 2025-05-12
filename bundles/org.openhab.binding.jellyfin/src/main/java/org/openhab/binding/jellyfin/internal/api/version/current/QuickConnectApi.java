package org.openhab.binding.jellyfin.internal.api.version.current;

import org.openhab.binding.jellyfin.internal.api.version.ApiClient;

import org.openhab.binding.jellyfin.internal.api.version.current.model.ProblemDetails;
import org.openhab.binding.jellyfin.internal.api.version.current.model.QuickConnectResult;
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
public class QuickConnectApi {
    private ApiClient apiClient;

    public QuickConnectApi() {
        this(new ApiClient());
    }

    @Autowired
    public QuickConnectApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    
    /**
     * Authorizes a pending quick connect request.
     * 
     * <p><b>200</b> - Quick connect result authorized successfully.
     * <p><b>403</b> - Unknown user id.
     * <p><b>401</b> - Unauthorized
     * @param code Quick connect code to authorize.
     * @param userId The user the authorize. Access to the requested user is required.
     * @return Boolean
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec authorizeQuickConnectRequestCreation(String code, UUID userId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'code' is set
        if (code == null) {
            throw new WebClientResponseException("Missing the required parameter 'code' when calling authorizeQuickConnect", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "code", code));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "userId", userId));
        
        final String[] localVarAccepts = { 
            "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Boolean> localVarReturnType = new ParameterizedTypeReference<Boolean>() {};
        return apiClient.invokeAPI("/QuickConnect/Authorize", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Authorizes a pending quick connect request.
     * 
     * <p><b>200</b> - Quick connect result authorized successfully.
     * <p><b>403</b> - Unknown user id.
     * <p><b>401</b> - Unauthorized
     * @param code Quick connect code to authorize.
     * @param userId The user the authorize. Access to the requested user is required.
     * @return Boolean
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Boolean> authorizeQuickConnect(String code, UUID userId) throws WebClientResponseException {
        ParameterizedTypeReference<Boolean> localVarReturnType = new ParameterizedTypeReference<Boolean>() {};
        return authorizeQuickConnectRequestCreation(code, userId).bodyToMono(localVarReturnType);
    }

    /**
     * Authorizes a pending quick connect request.
     * 
     * <p><b>200</b> - Quick connect result authorized successfully.
     * <p><b>403</b> - Unknown user id.
     * <p><b>401</b> - Unauthorized
     * @param code Quick connect code to authorize.
     * @param userId The user the authorize. Access to the requested user is required.
     * @return ResponseEntity&lt;Boolean&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Boolean>> authorizeQuickConnectWithHttpInfo(String code, UUID userId) throws WebClientResponseException {
        ParameterizedTypeReference<Boolean> localVarReturnType = new ParameterizedTypeReference<Boolean>() {};
        return authorizeQuickConnectRequestCreation(code, userId).toEntity(localVarReturnType);
    }

    /**
     * Authorizes a pending quick connect request.
     * 
     * <p><b>200</b> - Quick connect result authorized successfully.
     * <p><b>403</b> - Unknown user id.
     * <p><b>401</b> - Unauthorized
     * @param code Quick connect code to authorize.
     * @param userId The user the authorize. Access to the requested user is required.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec authorizeQuickConnectWithResponseSpec(String code, UUID userId) throws WebClientResponseException {
        return authorizeQuickConnectRequestCreation(code, userId);
    }

    /**
     * Gets the current quick connect state.
     * 
     * <p><b>200</b> - Quick connect state returned.
     * @return Boolean
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getQuickConnectEnabledRequestCreation() throws WebClientResponseException {
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

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<Boolean> localVarReturnType = new ParameterizedTypeReference<Boolean>() {};
        return apiClient.invokeAPI("/QuickConnect/Enabled", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets the current quick connect state.
     * 
     * <p><b>200</b> - Quick connect state returned.
     * @return Boolean
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Boolean> getQuickConnectEnabled() throws WebClientResponseException {
        ParameterizedTypeReference<Boolean> localVarReturnType = new ParameterizedTypeReference<Boolean>() {};
        return getQuickConnectEnabledRequestCreation().bodyToMono(localVarReturnType);
    }

    /**
     * Gets the current quick connect state.
     * 
     * <p><b>200</b> - Quick connect state returned.
     * @return ResponseEntity&lt;Boolean&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Boolean>> getQuickConnectEnabledWithHttpInfo() throws WebClientResponseException {
        ParameterizedTypeReference<Boolean> localVarReturnType = new ParameterizedTypeReference<Boolean>() {};
        return getQuickConnectEnabledRequestCreation().toEntity(localVarReturnType);
    }

    /**
     * Gets the current quick connect state.
     * 
     * <p><b>200</b> - Quick connect state returned.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getQuickConnectEnabledWithResponseSpec() throws WebClientResponseException {
        return getQuickConnectEnabledRequestCreation();
    }

    /**
     * Attempts to retrieve authentication information.
     * 
     * <p><b>200</b> - Quick connect result returned.
     * <p><b>404</b> - Unknown quick connect secret.
     * @param secret Secret previously returned from the Initiate endpoint.
     * @return QuickConnectResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getQuickConnectStateRequestCreation(String secret) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'secret' is set
        if (secret == null) {
            throw new WebClientResponseException("Missing the required parameter 'secret' when calling getQuickConnectState", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "secret", secret));
        
        final String[] localVarAccepts = { 
            "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<QuickConnectResult> localVarReturnType = new ParameterizedTypeReference<QuickConnectResult>() {};
        return apiClient.invokeAPI("/QuickConnect/Connect", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Attempts to retrieve authentication information.
     * 
     * <p><b>200</b> - Quick connect result returned.
     * <p><b>404</b> - Unknown quick connect secret.
     * @param secret Secret previously returned from the Initiate endpoint.
     * @return QuickConnectResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<QuickConnectResult> getQuickConnectState(String secret) throws WebClientResponseException {
        ParameterizedTypeReference<QuickConnectResult> localVarReturnType = new ParameterizedTypeReference<QuickConnectResult>() {};
        return getQuickConnectStateRequestCreation(secret).bodyToMono(localVarReturnType);
    }

    /**
     * Attempts to retrieve authentication information.
     * 
     * <p><b>200</b> - Quick connect result returned.
     * <p><b>404</b> - Unknown quick connect secret.
     * @param secret Secret previously returned from the Initiate endpoint.
     * @return ResponseEntity&lt;QuickConnectResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<QuickConnectResult>> getQuickConnectStateWithHttpInfo(String secret) throws WebClientResponseException {
        ParameterizedTypeReference<QuickConnectResult> localVarReturnType = new ParameterizedTypeReference<QuickConnectResult>() {};
        return getQuickConnectStateRequestCreation(secret).toEntity(localVarReturnType);
    }

    /**
     * Attempts to retrieve authentication information.
     * 
     * <p><b>200</b> - Quick connect result returned.
     * <p><b>404</b> - Unknown quick connect secret.
     * @param secret Secret previously returned from the Initiate endpoint.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getQuickConnectStateWithResponseSpec(String secret) throws WebClientResponseException {
        return getQuickConnectStateRequestCreation(secret);
    }

    /**
     * Initiate a new quick connect request.
     * 
     * <p><b>200</b> - Quick connect request successfully created.
     * <p><b>401</b> - Quick connect is not active on this server.
     * @return QuickConnectResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec initiateQuickConnectRequestCreation() throws WebClientResponseException {
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

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<QuickConnectResult> localVarReturnType = new ParameterizedTypeReference<QuickConnectResult>() {};
        return apiClient.invokeAPI("/QuickConnect/Initiate", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Initiate a new quick connect request.
     * 
     * <p><b>200</b> - Quick connect request successfully created.
     * <p><b>401</b> - Quick connect is not active on this server.
     * @return QuickConnectResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<QuickConnectResult> initiateQuickConnect() throws WebClientResponseException {
        ParameterizedTypeReference<QuickConnectResult> localVarReturnType = new ParameterizedTypeReference<QuickConnectResult>() {};
        return initiateQuickConnectRequestCreation().bodyToMono(localVarReturnType);
    }

    /**
     * Initiate a new quick connect request.
     * 
     * <p><b>200</b> - Quick connect request successfully created.
     * <p><b>401</b> - Quick connect is not active on this server.
     * @return ResponseEntity&lt;QuickConnectResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<QuickConnectResult>> initiateQuickConnectWithHttpInfo() throws WebClientResponseException {
        ParameterizedTypeReference<QuickConnectResult> localVarReturnType = new ParameterizedTypeReference<QuickConnectResult>() {};
        return initiateQuickConnectRequestCreation().toEntity(localVarReturnType);
    }

    /**
     * Initiate a new quick connect request.
     * 
     * <p><b>200</b> - Quick connect request successfully created.
     * <p><b>401</b> - Quick connect is not active on this server.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec initiateQuickConnectWithResponseSpec() throws WebClientResponseException {
        return initiateQuickConnectRequestCreation();
    }
}
