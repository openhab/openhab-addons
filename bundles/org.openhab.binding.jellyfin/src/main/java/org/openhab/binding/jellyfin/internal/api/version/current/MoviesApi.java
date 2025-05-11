package org.openhab.binding.jellyfin.internal.api.version.current;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.openhab.binding.jellyfin.internal.api.version.ApiClient;
import org.openhab.binding.jellyfin.internal.api.version.current.model.ItemFields;
import org.openhab.binding.jellyfin.internal.api.version.current.model.RecommendationDto;
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
public class MoviesApi {
    private ApiClient apiClient;

    public MoviesApi() {
        this(new ApiClient());
    }

    @Autowired
    public MoviesApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Gets movie recommendations.
     * 
     * <p>
     * <b>200</b> - Movie recommendations returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param userId Optional. Filter by user id, and attach user data.
     * @param parentId Specify this to localize the search to a specific item or folder. Omit to use the root.
     * @param fields Optional. The fields to return.
     * @param categoryLimit The max number of categories to return.
     * @param itemLimit The max number of items to return per category.
     * @return List&lt;RecommendationDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getMovieRecommendationsRequestCreation(UUID userId, UUID parentId, List<ItemFields> fields,
            Integer categoryLimit, Integer itemLimit) throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "userId", userId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "parentId", parentId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "fields", fields));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "categoryLimit", categoryLimit));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "itemLimit", itemLimit));

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<RecommendationDto> localVarReturnType = new ParameterizedTypeReference<RecommendationDto>() {
        };
        return apiClient.invokeAPI("/Movies/Recommendations", HttpMethod.GET, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Gets movie recommendations.
     * 
     * <p>
     * <b>200</b> - Movie recommendations returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param userId Optional. Filter by user id, and attach user data.
     * @param parentId Specify this to localize the search to a specific item or folder. Omit to use the root.
     * @param fields Optional. The fields to return.
     * @param categoryLimit The max number of categories to return.
     * @param itemLimit The max number of items to return per category.
     * @return List&lt;RecommendationDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Flux<RecommendationDto> getMovieRecommendations(UUID userId, UUID parentId, List<ItemFields> fields,
            Integer categoryLimit, Integer itemLimit) throws WebClientResponseException {
        ParameterizedTypeReference<RecommendationDto> localVarReturnType = new ParameterizedTypeReference<RecommendationDto>() {
        };
        return getMovieRecommendationsRequestCreation(userId, parentId, fields, categoryLimit, itemLimit)
                .bodyToFlux(localVarReturnType);
    }

    /**
     * Gets movie recommendations.
     * 
     * <p>
     * <b>200</b> - Movie recommendations returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param userId Optional. Filter by user id, and attach user data.
     * @param parentId Specify this to localize the search to a specific item or folder. Omit to use the root.
     * @param fields Optional. The fields to return.
     * @param categoryLimit The max number of categories to return.
     * @param itemLimit The max number of items to return per category.
     * @return ResponseEntity&lt;List&lt;RecommendationDto&gt;&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<List<RecommendationDto>>> getMovieRecommendationsWithHttpInfo(UUID userId, UUID parentId,
            List<ItemFields> fields, Integer categoryLimit, Integer itemLimit) throws WebClientResponseException {
        ParameterizedTypeReference<RecommendationDto> localVarReturnType = new ParameterizedTypeReference<RecommendationDto>() {
        };
        return getMovieRecommendationsRequestCreation(userId, parentId, fields, categoryLimit, itemLimit)
                .toEntityList(localVarReturnType);
    }

    /**
     * Gets movie recommendations.
     * 
     * <p>
     * <b>200</b> - Movie recommendations returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param userId Optional. Filter by user id, and attach user data.
     * @param parentId Specify this to localize the search to a specific item or folder. Omit to use the root.
     * @param fields Optional. The fields to return.
     * @param categoryLimit The max number of categories to return.
     * @param itemLimit The max number of items to return per category.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getMovieRecommendationsWithResponseSpec(UUID userId, UUID parentId, List<ItemFields> fields,
            Integer categoryLimit, Integer itemLimit) throws WebClientResponseException {
        return getMovieRecommendationsRequestCreation(userId, parentId, fields, categoryLimit, itemLimit);
    }
}
