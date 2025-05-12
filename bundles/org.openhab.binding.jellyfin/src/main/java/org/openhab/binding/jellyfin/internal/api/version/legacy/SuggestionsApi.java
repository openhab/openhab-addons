package org.openhab.binding.jellyfin.internal.api.version.legacy;

import org.openhab.binding.jellyfin.internal.api.version.ApiClient;

import org.openhab.binding.jellyfin.internal.api.version.legacy.model.BaseItemDtoQueryResult;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.BaseItemKind;
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
public class SuggestionsApi {
    private ApiClient apiClient;

    public SuggestionsApi() {
        this(new ApiClient());
    }

    @Autowired
    public SuggestionsApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    
    /**
     * Gets suggestions.
     * 
     * <p><b>200</b> - Suggestions returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId The user id.
     * @param mediaType The media types.
     * @param type The type.
     * @param startIndex Optional. The start index.
     * @param limit Optional. The limit.
     * @param enableTotalRecordCount Whether to enable the total record count.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getSuggestionsRequestCreation(UUID userId, List<String> mediaType, List<BaseItemKind> type, Integer startIndex, Integer limit, Boolean enableTotalRecordCount) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'userId' is set
        if (userId == null) {
            throw new WebClientResponseException("Missing the required parameter 'userId' when calling getSuggestions", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("userId", userId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "mediaType", mediaType));
        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "type", type));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "startIndex", startIndex));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "limit", limit));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableTotalRecordCount", enableTotalRecordCount));
        
        final String[] localVarAccepts = { 
            "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {};
        return apiClient.invokeAPI("/Users/{userId}/Suggestions", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets suggestions.
     * 
     * <p><b>200</b> - Suggestions returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId The user id.
     * @param mediaType The media types.
     * @param type The type.
     * @param startIndex Optional. The start index.
     * @param limit Optional. The limit.
     * @param enableTotalRecordCount Whether to enable the total record count.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<BaseItemDtoQueryResult> getSuggestions(UUID userId, List<String> mediaType, List<BaseItemKind> type, Integer startIndex, Integer limit, Boolean enableTotalRecordCount) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {};
        return getSuggestionsRequestCreation(userId, mediaType, type, startIndex, limit, enableTotalRecordCount).bodyToMono(localVarReturnType);
    }

    /**
     * Gets suggestions.
     * 
     * <p><b>200</b> - Suggestions returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId The user id.
     * @param mediaType The media types.
     * @param type The type.
     * @param startIndex Optional. The start index.
     * @param limit Optional. The limit.
     * @param enableTotalRecordCount Whether to enable the total record count.
     * @return ResponseEntity&lt;BaseItemDtoQueryResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<BaseItemDtoQueryResult>> getSuggestionsWithHttpInfo(UUID userId, List<String> mediaType, List<BaseItemKind> type, Integer startIndex, Integer limit, Boolean enableTotalRecordCount) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {};
        return getSuggestionsRequestCreation(userId, mediaType, type, startIndex, limit, enableTotalRecordCount).toEntity(localVarReturnType);
    }

    /**
     * Gets suggestions.
     * 
     * <p><b>200</b> - Suggestions returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId The user id.
     * @param mediaType The media types.
     * @param type The type.
     * @param startIndex Optional. The start index.
     * @param limit Optional. The limit.
     * @param enableTotalRecordCount Whether to enable the total record count.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getSuggestionsWithResponseSpec(UUID userId, List<String> mediaType, List<BaseItemKind> type, Integer startIndex, Integer limit, Boolean enableTotalRecordCount) throws WebClientResponseException {
        return getSuggestionsRequestCreation(userId, mediaType, type, startIndex, limit, enableTotalRecordCount);
    }
}
