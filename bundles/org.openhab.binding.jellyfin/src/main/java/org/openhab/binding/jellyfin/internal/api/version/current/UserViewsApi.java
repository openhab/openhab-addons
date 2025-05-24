package org.openhab.binding.jellyfin.internal.api.version.current;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.openhab.binding.jellyfin.internal.api.version.ApiClient;
import org.openhab.binding.jellyfin.internal.api.version.current.model.BaseItemDtoQueryResult;
import org.openhab.binding.jellyfin.internal.api.version.current.model.CollectionType;
import org.openhab.binding.jellyfin.internal.api.version.current.model.SpecialViewOptionDto;
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

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.12.0")
public class UserViewsApi {
    private ApiClient apiClient;

    public UserViewsApi() {
        this(new ApiClient());
    }

    @Autowired
    public UserViewsApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Get user view grouping options.
     * 
     * <p>
     * <b>200</b> - User view grouping options returned.
     * <p>
     * <b>404</b> - User not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param userId User id.
     * @return List&lt;SpecialViewOptionDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getGroupingOptionsRequestCreation(UUID userId) throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "userId", userId));

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<SpecialViewOptionDto> localVarReturnType = new ParameterizedTypeReference<SpecialViewOptionDto>() {
        };
        return apiClient.invokeAPI("/UserViews/GroupingOptions", HttpMethod.GET, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Get user view grouping options.
     * 
     * <p>
     * <b>200</b> - User view grouping options returned.
     * <p>
     * <b>404</b> - User not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param userId User id.
     * @return List&lt;SpecialViewOptionDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Flux<SpecialViewOptionDto> getGroupingOptions(UUID userId) throws WebClientResponseException {
        ParameterizedTypeReference<SpecialViewOptionDto> localVarReturnType = new ParameterizedTypeReference<SpecialViewOptionDto>() {
        };
        return getGroupingOptionsRequestCreation(userId).bodyToFlux(localVarReturnType);
    }

    /**
     * Get user view grouping options.
     * 
     * <p>
     * <b>200</b> - User view grouping options returned.
     * <p>
     * <b>404</b> - User not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param userId User id.
     * @return ResponseEntity&lt;List&lt;SpecialViewOptionDto&gt;&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<List<SpecialViewOptionDto>>> getGroupingOptionsWithHttpInfo(UUID userId)
            throws WebClientResponseException {
        ParameterizedTypeReference<SpecialViewOptionDto> localVarReturnType = new ParameterizedTypeReference<SpecialViewOptionDto>() {
        };
        return getGroupingOptionsRequestCreation(userId).toEntityList(localVarReturnType);
    }

    /**
     * Get user view grouping options.
     * 
     * <p>
     * <b>200</b> - User view grouping options returned.
     * <p>
     * <b>404</b> - User not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param userId User id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getGroupingOptionsWithResponseSpec(UUID userId) throws WebClientResponseException {
        return getGroupingOptionsRequestCreation(userId);
    }

    /**
     * Get user views.
     * 
     * <p>
     * <b>200</b> - User views returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param userId User id.
     * @param includeExternalContent Whether or not to include external views such as channels or live tv.
     * @param presetViews Preset views.
     * @param includeHidden Whether or not to include hidden content.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getUserViewsRequestCreation(UUID userId, Boolean includeExternalContent,
            List<CollectionType> presetViews, Boolean includeHidden) throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "userId", userId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "includeExternalContent", includeExternalContent));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "presetViews", presetViews));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "includeHidden", includeHidden));

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {
        };
        return apiClient.invokeAPI("/UserViews", HttpMethod.GET, pathParams, queryParams, postBody, headerParams,
                cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get user views.
     * 
     * <p>
     * <b>200</b> - User views returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param userId User id.
     * @param includeExternalContent Whether or not to include external views such as channels or live tv.
     * @param presetViews Preset views.
     * @param includeHidden Whether or not to include hidden content.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<BaseItemDtoQueryResult> getUserViews(UUID userId, Boolean includeExternalContent,
            List<CollectionType> presetViews, Boolean includeHidden) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {
        };
        return getUserViewsRequestCreation(userId, includeExternalContent, presetViews, includeHidden)
                .bodyToMono(localVarReturnType);
    }

    /**
     * Get user views.
     * 
     * <p>
     * <b>200</b> - User views returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param userId User id.
     * @param includeExternalContent Whether or not to include external views such as channels or live tv.
     * @param presetViews Preset views.
     * @param includeHidden Whether or not to include hidden content.
     * @return ResponseEntity&lt;BaseItemDtoQueryResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<BaseItemDtoQueryResult>> getUserViewsWithHttpInfo(UUID userId,
            Boolean includeExternalContent, List<CollectionType> presetViews, Boolean includeHidden)
            throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {
        };
        return getUserViewsRequestCreation(userId, includeExternalContent, presetViews, includeHidden)
                .toEntity(localVarReturnType);
    }

    /**
     * Get user views.
     * 
     * <p>
     * <b>200</b> - User views returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param userId User id.
     * @param includeExternalContent Whether or not to include external views such as channels or live tv.
     * @param presetViews Preset views.
     * @param includeHidden Whether or not to include hidden content.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getUserViewsWithResponseSpec(UUID userId, Boolean includeExternalContent,
            List<CollectionType> presetViews, Boolean includeHidden) throws WebClientResponseException {
        return getUserViewsRequestCreation(userId, includeExternalContent, presetViews, includeHidden);
    }
}
