package org.openhab.binding.jellyfin.internal.api.version.current;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openhab.binding.jellyfin.internal.api.version.ApiClient;
import org.openhab.binding.jellyfin.internal.api.version.current.model.ConfigImageTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import reactor.core.publisher.Mono;

@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.12.0")
public class TmdbApi {
    private ApiClient apiClient;

    public TmdbApi() {
        this(new ApiClient());
    }

    @Autowired
    public TmdbApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Gets the TMDb image configuration options.
     * 
     * <p>
     * <b>200</b> - Success
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return ConfigImageTypes
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec tmdbClientConfigurationRequestCreation() throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { "application/json" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<ConfigImageTypes> localVarReturnType = new ParameterizedTypeReference<ConfigImageTypes>() {
        };
        return apiClient.invokeAPI("/Tmdb/ClientConfiguration", HttpMethod.GET, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Gets the TMDb image configuration options.
     * 
     * <p>
     * <b>200</b> - Success
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return ConfigImageTypes
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ConfigImageTypes> tmdbClientConfiguration() throws WebClientResponseException {
        ParameterizedTypeReference<ConfigImageTypes> localVarReturnType = new ParameterizedTypeReference<ConfigImageTypes>() {
        };
        return tmdbClientConfigurationRequestCreation().bodyToMono(localVarReturnType);
    }

    /**
     * Gets the TMDb image configuration options.
     * 
     * <p>
     * <b>200</b> - Success
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return ResponseEntity&lt;ConfigImageTypes&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<ConfigImageTypes>> tmdbClientConfigurationWithHttpInfo()
            throws WebClientResponseException {
        ParameterizedTypeReference<ConfigImageTypes> localVarReturnType = new ParameterizedTypeReference<ConfigImageTypes>() {
        };
        return tmdbClientConfigurationRequestCreation().toEntity(localVarReturnType);
    }

    /**
     * Gets the TMDb image configuration options.
     * 
     * <p>
     * <b>200</b> - Success
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec tmdbClientConfigurationWithResponseSpec() throws WebClientResponseException {
        return tmdbClientConfigurationRequestCreation();
    }
}
