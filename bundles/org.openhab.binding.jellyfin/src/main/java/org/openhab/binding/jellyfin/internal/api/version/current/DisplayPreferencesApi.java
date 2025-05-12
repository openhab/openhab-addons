package org.openhab.binding.jellyfin.internal.api.version.current;

import org.openhab.binding.jellyfin.internal.api.version.ApiClient;

import org.openhab.binding.jellyfin.internal.api.version.current.model.DisplayPreferencesDto;
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
public class DisplayPreferencesApi {
    private ApiClient apiClient;

    public DisplayPreferencesApi() {
        this(new ApiClient());
    }

    @Autowired
    public DisplayPreferencesApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    
    /**
     * Get Display Preferences.
     * 
     * <p><b>200</b> - Display preferences retrieved.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param displayPreferencesId Display preferences id.
     * @param client Client.
     * @param userId User id.
     * @return DisplayPreferencesDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getDisplayPreferencesRequestCreation(String displayPreferencesId, String client, UUID userId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'displayPreferencesId' is set
        if (displayPreferencesId == null) {
            throw new WebClientResponseException("Missing the required parameter 'displayPreferencesId' when calling getDisplayPreferences", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'client' is set
        if (client == null) {
            throw new WebClientResponseException("Missing the required parameter 'client' when calling getDisplayPreferences", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("displayPreferencesId", displayPreferencesId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "userId", userId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "client", client));
        
        final String[] localVarAccepts = { 
            "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<DisplayPreferencesDto> localVarReturnType = new ParameterizedTypeReference<DisplayPreferencesDto>() {};
        return apiClient.invokeAPI("/DisplayPreferences/{displayPreferencesId}", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get Display Preferences.
     * 
     * <p><b>200</b> - Display preferences retrieved.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param displayPreferencesId Display preferences id.
     * @param client Client.
     * @param userId User id.
     * @return DisplayPreferencesDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<DisplayPreferencesDto> getDisplayPreferences(String displayPreferencesId, String client, UUID userId) throws WebClientResponseException {
        ParameterizedTypeReference<DisplayPreferencesDto> localVarReturnType = new ParameterizedTypeReference<DisplayPreferencesDto>() {};
        return getDisplayPreferencesRequestCreation(displayPreferencesId, client, userId).bodyToMono(localVarReturnType);
    }

    /**
     * Get Display Preferences.
     * 
     * <p><b>200</b> - Display preferences retrieved.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param displayPreferencesId Display preferences id.
     * @param client Client.
     * @param userId User id.
     * @return ResponseEntity&lt;DisplayPreferencesDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<DisplayPreferencesDto>> getDisplayPreferencesWithHttpInfo(String displayPreferencesId, String client, UUID userId) throws WebClientResponseException {
        ParameterizedTypeReference<DisplayPreferencesDto> localVarReturnType = new ParameterizedTypeReference<DisplayPreferencesDto>() {};
        return getDisplayPreferencesRequestCreation(displayPreferencesId, client, userId).toEntity(localVarReturnType);
    }

    /**
     * Get Display Preferences.
     * 
     * <p><b>200</b> - Display preferences retrieved.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param displayPreferencesId Display preferences id.
     * @param client Client.
     * @param userId User id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getDisplayPreferencesWithResponseSpec(String displayPreferencesId, String client, UUID userId) throws WebClientResponseException {
        return getDisplayPreferencesRequestCreation(displayPreferencesId, client, userId);
    }

    /**
     * Update Display Preferences.
     * 
     * <p><b>204</b> - Display preferences updated.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param displayPreferencesId Display preferences id.
     * @param client Client.
     * @param displayPreferencesDto New Display Preferences object.
     * @param userId User Id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec updateDisplayPreferencesRequestCreation(String displayPreferencesId, String client, DisplayPreferencesDto displayPreferencesDto, UUID userId) throws WebClientResponseException {
        Object postBody = displayPreferencesDto;
        // verify the required parameter 'displayPreferencesId' is set
        if (displayPreferencesId == null) {
            throw new WebClientResponseException("Missing the required parameter 'displayPreferencesId' when calling updateDisplayPreferences", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'client' is set
        if (client == null) {
            throw new WebClientResponseException("Missing the required parameter 'client' when calling updateDisplayPreferences", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'displayPreferencesDto' is set
        if (displayPreferencesDto == null) {
            throw new WebClientResponseException("Missing the required parameter 'displayPreferencesDto' when calling updateDisplayPreferences", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("displayPreferencesId", displayPreferencesId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "userId", userId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "client", client));
        
        final String[] localVarAccepts = { };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { 
            "application/json", "text/json", "application/*+json"
        };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return apiClient.invokeAPI("/DisplayPreferences/{displayPreferencesId}", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Update Display Preferences.
     * 
     * <p><b>204</b> - Display preferences updated.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param displayPreferencesId Display preferences id.
     * @param client Client.
     * @param displayPreferencesDto New Display Preferences object.
     * @param userId User Id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> updateDisplayPreferences(String displayPreferencesId, String client, DisplayPreferencesDto displayPreferencesDto, UUID userId) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return updateDisplayPreferencesRequestCreation(displayPreferencesId, client, displayPreferencesDto, userId).bodyToMono(localVarReturnType);
    }

    /**
     * Update Display Preferences.
     * 
     * <p><b>204</b> - Display preferences updated.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param displayPreferencesId Display preferences id.
     * @param client Client.
     * @param displayPreferencesDto New Display Preferences object.
     * @param userId User Id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> updateDisplayPreferencesWithHttpInfo(String displayPreferencesId, String client, DisplayPreferencesDto displayPreferencesDto, UUID userId) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return updateDisplayPreferencesRequestCreation(displayPreferencesId, client, displayPreferencesDto, userId).toEntity(localVarReturnType);
    }

    /**
     * Update Display Preferences.
     * 
     * <p><b>204</b> - Display preferences updated.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param displayPreferencesId Display preferences id.
     * @param client Client.
     * @param displayPreferencesDto New Display Preferences object.
     * @param userId User Id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec updateDisplayPreferencesWithResponseSpec(String displayPreferencesId, String client, DisplayPreferencesDto displayPreferencesDto, UUID userId) throws WebClientResponseException {
        return updateDisplayPreferencesRequestCreation(displayPreferencesId, client, displayPreferencesDto, userId);
    }
}
