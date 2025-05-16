package org.openhab.binding.jellyfin.internal.api.version.current;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.openhab.binding.jellyfin.internal.api.version.ApiClient;
import org.openhab.binding.jellyfin.internal.api.version.current.model.MediaSegmentDtoQueryResult;
import org.openhab.binding.jellyfin.internal.api.version.current.model.MediaSegmentType;
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
public class MediaSegmentsApi {
    private ApiClient apiClient;

    public MediaSegmentsApi() {
        this(new ApiClient());
    }

    @Autowired
    public MediaSegmentsApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Gets all media segments based on an itemId.
     * 
     * <p>
     * <b>200</b> - Success
     * <p>
     * <b>404</b> - Not Found
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId The ItemId.
     * @param includeSegmentTypes Optional filter of requested segment types.
     * @return MediaSegmentDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getItemSegmentsRequestCreation(UUID itemId, List<MediaSegmentType> includeSegmentTypes)
            throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException("Missing the required parameter 'itemId' when calling getItemSegments",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("itemId", itemId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(
                apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)),
                        "includeSegmentTypes", includeSegmentTypes));

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<MediaSegmentDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<MediaSegmentDtoQueryResult>() {
        };
        return apiClient.invokeAPI("/MediaSegments/{itemId}", HttpMethod.GET, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Gets all media segments based on an itemId.
     * 
     * <p>
     * <b>200</b> - Success
     * <p>
     * <b>404</b> - Not Found
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId The ItemId.
     * @param includeSegmentTypes Optional filter of requested segment types.
     * @return MediaSegmentDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<MediaSegmentDtoQueryResult> getItemSegments(UUID itemId, List<MediaSegmentType> includeSegmentTypes)
            throws WebClientResponseException {
        ParameterizedTypeReference<MediaSegmentDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<MediaSegmentDtoQueryResult>() {
        };
        return getItemSegmentsRequestCreation(itemId, includeSegmentTypes).bodyToMono(localVarReturnType);
    }

    /**
     * Gets all media segments based on an itemId.
     * 
     * <p>
     * <b>200</b> - Success
     * <p>
     * <b>404</b> - Not Found
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId The ItemId.
     * @param includeSegmentTypes Optional filter of requested segment types.
     * @return ResponseEntity&lt;MediaSegmentDtoQueryResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<MediaSegmentDtoQueryResult>> getItemSegmentsWithHttpInfo(UUID itemId,
            List<MediaSegmentType> includeSegmentTypes) throws WebClientResponseException {
        ParameterizedTypeReference<MediaSegmentDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<MediaSegmentDtoQueryResult>() {
        };
        return getItemSegmentsRequestCreation(itemId, includeSegmentTypes).toEntity(localVarReturnType);
    }

    /**
     * Gets all media segments based on an itemId.
     * 
     * <p>
     * <b>200</b> - Success
     * <p>
     * <b>404</b> - Not Found
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId The ItemId.
     * @param includeSegmentTypes Optional filter of requested segment types.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getItemSegmentsWithResponseSpec(UUID itemId, List<MediaSegmentType> includeSegmentTypes)
            throws WebClientResponseException {
        return getItemSegmentsRequestCreation(itemId, includeSegmentTypes);
    }
}
