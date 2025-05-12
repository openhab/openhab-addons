package org.openhab.binding.jellyfin.internal.api.version.current;

import org.openhab.binding.jellyfin.internal.api.version.ApiClient;

import org.openhab.binding.jellyfin.internal.api.version.current.model.BaseItemKind;
import org.openhab.binding.jellyfin.internal.api.version.current.model.MediaType;
import org.openhab.binding.jellyfin.internal.api.version.current.model.QueryFilters;
import org.openhab.binding.jellyfin.internal.api.version.current.model.QueryFiltersLegacy;
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
public class FilterApi {
    private ApiClient apiClient;

    public FilterApi() {
        this(new ApiClient());
    }

    @Autowired
    public FilterApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    
    /**
     * Gets query filters.
     * 
     * <p><b>200</b> - Filters retrieved.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId Optional. User id.
     * @param parentId Optional. Specify this to localize the search to a specific item or folder. Omit to use the root.
     * @param includeItemTypes Optional. If specified, results will be filtered based on item type. This allows multiple, comma delimited.
     * @param isAiring Optional. Is item airing.
     * @param isMovie Optional. Is item movie.
     * @param isSports Optional. Is item sports.
     * @param isKids Optional. Is item kids.
     * @param isNews Optional. Is item news.
     * @param isSeries Optional. Is item series.
     * @param recursive Optional. Search recursive.
     * @return QueryFilters
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getQueryFiltersRequestCreation(UUID userId, UUID parentId, List<BaseItemKind> includeItemTypes, Boolean isAiring, Boolean isMovie, Boolean isSports, Boolean isKids, Boolean isNews, Boolean isSeries, Boolean recursive) throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "userId", userId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "parentId", parentId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "includeItemTypes", includeItemTypes));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isAiring", isAiring));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isMovie", isMovie));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isSports", isSports));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isKids", isKids));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isNews", isNews));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isSeries", isSeries));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "recursive", recursive));
        
        final String[] localVarAccepts = { 
            "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<QueryFilters> localVarReturnType = new ParameterizedTypeReference<QueryFilters>() {};
        return apiClient.invokeAPI("/Items/Filters2", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets query filters.
     * 
     * <p><b>200</b> - Filters retrieved.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId Optional. User id.
     * @param parentId Optional. Specify this to localize the search to a specific item or folder. Omit to use the root.
     * @param includeItemTypes Optional. If specified, results will be filtered based on item type. This allows multiple, comma delimited.
     * @param isAiring Optional. Is item airing.
     * @param isMovie Optional. Is item movie.
     * @param isSports Optional. Is item sports.
     * @param isKids Optional. Is item kids.
     * @param isNews Optional. Is item news.
     * @param isSeries Optional. Is item series.
     * @param recursive Optional. Search recursive.
     * @return QueryFilters
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<QueryFilters> getQueryFilters(UUID userId, UUID parentId, List<BaseItemKind> includeItemTypes, Boolean isAiring, Boolean isMovie, Boolean isSports, Boolean isKids, Boolean isNews, Boolean isSeries, Boolean recursive) throws WebClientResponseException {
        ParameterizedTypeReference<QueryFilters> localVarReturnType = new ParameterizedTypeReference<QueryFilters>() {};
        return getQueryFiltersRequestCreation(userId, parentId, includeItemTypes, isAiring, isMovie, isSports, isKids, isNews, isSeries, recursive).bodyToMono(localVarReturnType);
    }

    /**
     * Gets query filters.
     * 
     * <p><b>200</b> - Filters retrieved.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId Optional. User id.
     * @param parentId Optional. Specify this to localize the search to a specific item or folder. Omit to use the root.
     * @param includeItemTypes Optional. If specified, results will be filtered based on item type. This allows multiple, comma delimited.
     * @param isAiring Optional. Is item airing.
     * @param isMovie Optional. Is item movie.
     * @param isSports Optional. Is item sports.
     * @param isKids Optional. Is item kids.
     * @param isNews Optional. Is item news.
     * @param isSeries Optional. Is item series.
     * @param recursive Optional. Search recursive.
     * @return ResponseEntity&lt;QueryFilters&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<QueryFilters>> getQueryFiltersWithHttpInfo(UUID userId, UUID parentId, List<BaseItemKind> includeItemTypes, Boolean isAiring, Boolean isMovie, Boolean isSports, Boolean isKids, Boolean isNews, Boolean isSeries, Boolean recursive) throws WebClientResponseException {
        ParameterizedTypeReference<QueryFilters> localVarReturnType = new ParameterizedTypeReference<QueryFilters>() {};
        return getQueryFiltersRequestCreation(userId, parentId, includeItemTypes, isAiring, isMovie, isSports, isKids, isNews, isSeries, recursive).toEntity(localVarReturnType);
    }

    /**
     * Gets query filters.
     * 
     * <p><b>200</b> - Filters retrieved.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId Optional. User id.
     * @param parentId Optional. Specify this to localize the search to a specific item or folder. Omit to use the root.
     * @param includeItemTypes Optional. If specified, results will be filtered based on item type. This allows multiple, comma delimited.
     * @param isAiring Optional. Is item airing.
     * @param isMovie Optional. Is item movie.
     * @param isSports Optional. Is item sports.
     * @param isKids Optional. Is item kids.
     * @param isNews Optional. Is item news.
     * @param isSeries Optional. Is item series.
     * @param recursive Optional. Search recursive.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getQueryFiltersWithResponseSpec(UUID userId, UUID parentId, List<BaseItemKind> includeItemTypes, Boolean isAiring, Boolean isMovie, Boolean isSports, Boolean isKids, Boolean isNews, Boolean isSeries, Boolean recursive) throws WebClientResponseException {
        return getQueryFiltersRequestCreation(userId, parentId, includeItemTypes, isAiring, isMovie, isSports, isKids, isNews, isSeries, recursive);
    }

    /**
     * Gets legacy query filters.
     * 
     * <p><b>200</b> - Legacy filters retrieved.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId Optional. User id.
     * @param parentId Optional. Parent id.
     * @param includeItemTypes Optional. If specified, results will be filtered based on item type. This allows multiple, comma delimited.
     * @param mediaTypes Optional. Filter by MediaType. Allows multiple, comma delimited.
     * @return QueryFiltersLegacy
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getQueryFiltersLegacyRequestCreation(UUID userId, UUID parentId, List<BaseItemKind> includeItemTypes, List<MediaType> mediaTypes) throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "userId", userId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "parentId", parentId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "includeItemTypes", includeItemTypes));
        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "mediaTypes", mediaTypes));
        
        final String[] localVarAccepts = { 
            "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<QueryFiltersLegacy> localVarReturnType = new ParameterizedTypeReference<QueryFiltersLegacy>() {};
        return apiClient.invokeAPI("/Items/Filters", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets legacy query filters.
     * 
     * <p><b>200</b> - Legacy filters retrieved.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId Optional. User id.
     * @param parentId Optional. Parent id.
     * @param includeItemTypes Optional. If specified, results will be filtered based on item type. This allows multiple, comma delimited.
     * @param mediaTypes Optional. Filter by MediaType. Allows multiple, comma delimited.
     * @return QueryFiltersLegacy
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<QueryFiltersLegacy> getQueryFiltersLegacy(UUID userId, UUID parentId, List<BaseItemKind> includeItemTypes, List<MediaType> mediaTypes) throws WebClientResponseException {
        ParameterizedTypeReference<QueryFiltersLegacy> localVarReturnType = new ParameterizedTypeReference<QueryFiltersLegacy>() {};
        return getQueryFiltersLegacyRequestCreation(userId, parentId, includeItemTypes, mediaTypes).bodyToMono(localVarReturnType);
    }

    /**
     * Gets legacy query filters.
     * 
     * <p><b>200</b> - Legacy filters retrieved.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId Optional. User id.
     * @param parentId Optional. Parent id.
     * @param includeItemTypes Optional. If specified, results will be filtered based on item type. This allows multiple, comma delimited.
     * @param mediaTypes Optional. Filter by MediaType. Allows multiple, comma delimited.
     * @return ResponseEntity&lt;QueryFiltersLegacy&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<QueryFiltersLegacy>> getQueryFiltersLegacyWithHttpInfo(UUID userId, UUID parentId, List<BaseItemKind> includeItemTypes, List<MediaType> mediaTypes) throws WebClientResponseException {
        ParameterizedTypeReference<QueryFiltersLegacy> localVarReturnType = new ParameterizedTypeReference<QueryFiltersLegacy>() {};
        return getQueryFiltersLegacyRequestCreation(userId, parentId, includeItemTypes, mediaTypes).toEntity(localVarReturnType);
    }

    /**
     * Gets legacy query filters.
     * 
     * <p><b>200</b> - Legacy filters retrieved.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId Optional. User id.
     * @param parentId Optional. Parent id.
     * @param includeItemTypes Optional. If specified, results will be filtered based on item type. This allows multiple, comma delimited.
     * @param mediaTypes Optional. Filter by MediaType. Allows multiple, comma delimited.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getQueryFiltersLegacyWithResponseSpec(UUID userId, UUID parentId, List<BaseItemKind> includeItemTypes, List<MediaType> mediaTypes) throws WebClientResponseException {
        return getQueryFiltersLegacyRequestCreation(userId, parentId, includeItemTypes, mediaTypes);
    }
}
