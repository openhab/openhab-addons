package org.openhab.binding.jellyfin.internal.api.version.legacy;

import org.openhab.binding.jellyfin.internal.api.version.ApiClient;

import org.openhab.binding.jellyfin.internal.api.version.legacy.model.UtcTimeResponse;

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
public class TimeSyncApi {
    private ApiClient apiClient;

    public TimeSyncApi() {
        this(new ApiClient());
    }

    @Autowired
    public TimeSyncApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    
    /**
     * Gets the current UTC time.
     * 
     * <p><b>200</b> - Time returned.
     * @return UtcTimeResponse
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getUtcTimeRequestCreation() throws WebClientResponseException {
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

        ParameterizedTypeReference<UtcTimeResponse> localVarReturnType = new ParameterizedTypeReference<UtcTimeResponse>() {};
        return apiClient.invokeAPI("/GetUtcTime", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets the current UTC time.
     * 
     * <p><b>200</b> - Time returned.
     * @return UtcTimeResponse
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<UtcTimeResponse> getUtcTime() throws WebClientResponseException {
        ParameterizedTypeReference<UtcTimeResponse> localVarReturnType = new ParameterizedTypeReference<UtcTimeResponse>() {};
        return getUtcTimeRequestCreation().bodyToMono(localVarReturnType);
    }

    /**
     * Gets the current UTC time.
     * 
     * <p><b>200</b> - Time returned.
     * @return ResponseEntity&lt;UtcTimeResponse&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<UtcTimeResponse>> getUtcTimeWithHttpInfo() throws WebClientResponseException {
        ParameterizedTypeReference<UtcTimeResponse> localVarReturnType = new ParameterizedTypeReference<UtcTimeResponse>() {};
        return getUtcTimeRequestCreation().toEntity(localVarReturnType);
    }

    /**
     * Gets the current UTC time.
     * 
     * <p><b>200</b> - Time returned.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getUtcTimeWithResponseSpec() throws WebClientResponseException {
        return getUtcTimeRequestCreation();
    }
}
