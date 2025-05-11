package org.openhab.binding.jellyfin.internal.api.version.current;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openhab.binding.jellyfin.internal.api.version.ApiClient;
import org.openhab.binding.jellyfin.internal.api.version.current.model.ClientLogDocumentResponseDto;
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
public class ClientLogApi {
    private ApiClient apiClient;

    public ClientLogApi() {
        this(new ApiClient());
    }

    @Autowired
    public ClientLogApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Upload a document.
     * 
     * <p>
     * <b>200</b> - Document saved.
     * <p>
     * <b>403</b> - Event logging disabled.
     * <p>
     * <b>413</b> - Upload size too large.
     * <p>
     * <b>401</b> - Unauthorized
     * 
     * @param body The body parameter
     * @return ClientLogDocumentResponseDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec logFileRequestCreation(File body) throws WebClientResponseException {
        Object postBody = body;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { "text/plain" };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<ClientLogDocumentResponseDto> localVarReturnType = new ParameterizedTypeReference<ClientLogDocumentResponseDto>() {
        };
        return apiClient.invokeAPI("/ClientLog/Document", HttpMethod.POST, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Upload a document.
     * 
     * <p>
     * <b>200</b> - Document saved.
     * <p>
     * <b>403</b> - Event logging disabled.
     * <p>
     * <b>413</b> - Upload size too large.
     * <p>
     * <b>401</b> - Unauthorized
     * 
     * @param body The body parameter
     * @return ClientLogDocumentResponseDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ClientLogDocumentResponseDto> logFile(File body) throws WebClientResponseException {
        ParameterizedTypeReference<ClientLogDocumentResponseDto> localVarReturnType = new ParameterizedTypeReference<ClientLogDocumentResponseDto>() {
        };
        return logFileRequestCreation(body).bodyToMono(localVarReturnType);
    }

    /**
     * Upload a document.
     * 
     * <p>
     * <b>200</b> - Document saved.
     * <p>
     * <b>403</b> - Event logging disabled.
     * <p>
     * <b>413</b> - Upload size too large.
     * <p>
     * <b>401</b> - Unauthorized
     * 
     * @param body The body parameter
     * @return ResponseEntity&lt;ClientLogDocumentResponseDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<ClientLogDocumentResponseDto>> logFileWithHttpInfo(File body)
            throws WebClientResponseException {
        ParameterizedTypeReference<ClientLogDocumentResponseDto> localVarReturnType = new ParameterizedTypeReference<ClientLogDocumentResponseDto>() {
        };
        return logFileRequestCreation(body).toEntity(localVarReturnType);
    }

    /**
     * Upload a document.
     * 
     * <p>
     * <b>200</b> - Document saved.
     * <p>
     * <b>403</b> - Event logging disabled.
     * <p>
     * <b>413</b> - Upload size too large.
     * <p>
     * <b>401</b> - Unauthorized
     * 
     * @param body The body parameter
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec logFileWithResponseSpec(File body) throws WebClientResponseException {
        return logFileRequestCreation(body);
    }
}
