package org.openhab.binding.jellyfin.internal.api.version.current;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.openhab.binding.jellyfin.internal.api.version.ApiClient;
import org.openhab.binding.jellyfin.internal.api.version.current.model.BaseItemDtoQueryResult;
import org.openhab.binding.jellyfin.internal.api.version.current.model.BaseItemKind;
import org.openhab.binding.jellyfin.internal.api.version.current.model.MediaType;
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
     * <p>
     * <b>200</b> - Suggestions returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param userId The user id.
     * @param mediaType The media types.
     * @param type The type.
     * @param startIndex Optional. The start index.
     * @param limit Optional. The limit.
     * @param enableTotalRecordCount Whether to enable the total record count.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getSuggestionsRequestCreation(UUID userId, List<MediaType> mediaType, List<BaseItemKind> type,
            Integer startIndex, Integer limit, Boolean enableTotalRecordCount) throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "userId", userId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "mediaType", mediaType));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "type", type));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "startIndex", startIndex));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "limit", limit));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableTotalRecordCount", enableTotalRecordCount));

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {
        };
        return apiClient.invokeAPI("/Items/Suggestions", HttpMethod.GET, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Gets suggestions.
     * 
     * <p>
     * <b>200</b> - Suggestions returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param userId The user id.
     * @param mediaType The media types.
     * @param type The type.
     * @param startIndex Optional. The start index.
     * @param limit Optional. The limit.
     * @param enableTotalRecordCount Whether to enable the total record count.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<BaseItemDtoQueryResult> getSuggestions(UUID userId, List<MediaType> mediaType, List<BaseItemKind> type,
            Integer startIndex, Integer limit, Boolean enableTotalRecordCount) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {
        };
        return getSuggestionsRequestCreation(userId, mediaType, type, startIndex, limit, enableTotalRecordCount)
                .bodyToMono(localVarReturnType);
    }

    /**
     * Gets suggestions.
     * 
     * <p>
     * <b>200</b> - Suggestions returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param userId The user id.
     * @param mediaType The media types.
     * @param type The type.
     * @param startIndex Optional. The start index.
     * @param limit Optional. The limit.
     * @param enableTotalRecordCount Whether to enable the total record count.
     * @return ResponseEntity&lt;BaseItemDtoQueryResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<BaseItemDtoQueryResult>> getSuggestionsWithHttpInfo(UUID userId,
            List<MediaType> mediaType, List<BaseItemKind> type, Integer startIndex, Integer limit,
            Boolean enableTotalRecordCount) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {
        };
        return getSuggestionsRequestCreation(userId, mediaType, type, startIndex, limit, enableTotalRecordCount)
                .toEntity(localVarReturnType);
    }

    /**
     * Gets suggestions.
     * 
     * <p>
     * <b>200</b> - Suggestions returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param userId The user id.
     * @param mediaType The media types.
     * @param type The type.
     * @param startIndex Optional. The start index.
     * @param limit Optional. The limit.
     * @param enableTotalRecordCount Whether to enable the total record count.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getSuggestionsWithResponseSpec(UUID userId, List<MediaType> mediaType, List<BaseItemKind> type,
            Integer startIndex, Integer limit, Boolean enableTotalRecordCount) throws WebClientResponseException {
        return getSuggestionsRequestCreation(userId, mediaType, type, startIndex, limit, enableTotalRecordCount);
    }
}
