package org.openhab.binding.jellyfin.internal.api.version.current;

import org.openhab.binding.jellyfin.internal.api.version.ApiClient;

import org.openhab.binding.jellyfin.internal.api.version.current.model.BaseItemKind;
import org.openhab.binding.jellyfin.internal.api.version.current.model.MediaType;
import org.openhab.binding.jellyfin.internal.api.version.current.model.SearchHintResult;
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
public class SearchApi {
    private ApiClient apiClient;

    public SearchApi() {
        this(new ApiClient());
    }

    @Autowired
    public SearchApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    
    /**
     * Gets the search hint result.
     * 
     * <p><b>200</b> - Search hint returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param searchTerm The search term to filter on.
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the results.
     * @param limit Optional. The maximum number of records to return.
     * @param userId Optional. Supply a user id to search within a user&#39;s library or omit to search all.
     * @param includeItemTypes If specified, only results with the specified item types are returned. This allows multiple, comma delimited.
     * @param excludeItemTypes If specified, results with these item types are filtered out. This allows multiple, comma delimited.
     * @param mediaTypes If specified, only results with the specified media types are returned. This allows multiple, comma delimited.
     * @param parentId If specified, only children of the parent are returned.
     * @param isMovie Optional filter for movies.
     * @param isSeries Optional filter for series.
     * @param isNews Optional filter for news.
     * @param isKids Optional filter for kids.
     * @param isSports Optional filter for sports.
     * @param includePeople Optional filter whether to include people.
     * @param includeMedia Optional filter whether to include media.
     * @param includeGenres Optional filter whether to include genres.
     * @param includeStudios Optional filter whether to include studios.
     * @param includeArtists Optional filter whether to include artists.
     * @return SearchHintResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getSearchHintsRequestCreation(String searchTerm, Integer startIndex, Integer limit, UUID userId, List<BaseItemKind> includeItemTypes, List<BaseItemKind> excludeItemTypes, List<MediaType> mediaTypes, UUID parentId, Boolean isMovie, Boolean isSeries, Boolean isNews, Boolean isKids, Boolean isSports, Boolean includePeople, Boolean includeMedia, Boolean includeGenres, Boolean includeStudios, Boolean includeArtists) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'searchTerm' is set
        if (searchTerm == null) {
            throw new WebClientResponseException("Missing the required parameter 'searchTerm' when calling getSearchHints", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "startIndex", startIndex));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "limit", limit));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "userId", userId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "searchTerm", searchTerm));
        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "includeItemTypes", includeItemTypes));
        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "excludeItemTypes", excludeItemTypes));
        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "mediaTypes", mediaTypes));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "parentId", parentId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isMovie", isMovie));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isSeries", isSeries));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isNews", isNews));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isKids", isKids));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isSports", isSports));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "includePeople", includePeople));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "includeMedia", includeMedia));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "includeGenres", includeGenres));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "includeStudios", includeStudios));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "includeArtists", includeArtists));
        
        final String[] localVarAccepts = { 
            "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<SearchHintResult> localVarReturnType = new ParameterizedTypeReference<SearchHintResult>() {};
        return apiClient.invokeAPI("/Search/Hints", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets the search hint result.
     * 
     * <p><b>200</b> - Search hint returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param searchTerm The search term to filter on.
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the results.
     * @param limit Optional. The maximum number of records to return.
     * @param userId Optional. Supply a user id to search within a user&#39;s library or omit to search all.
     * @param includeItemTypes If specified, only results with the specified item types are returned. This allows multiple, comma delimited.
     * @param excludeItemTypes If specified, results with these item types are filtered out. This allows multiple, comma delimited.
     * @param mediaTypes If specified, only results with the specified media types are returned. This allows multiple, comma delimited.
     * @param parentId If specified, only children of the parent are returned.
     * @param isMovie Optional filter for movies.
     * @param isSeries Optional filter for series.
     * @param isNews Optional filter for news.
     * @param isKids Optional filter for kids.
     * @param isSports Optional filter for sports.
     * @param includePeople Optional filter whether to include people.
     * @param includeMedia Optional filter whether to include media.
     * @param includeGenres Optional filter whether to include genres.
     * @param includeStudios Optional filter whether to include studios.
     * @param includeArtists Optional filter whether to include artists.
     * @return SearchHintResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<SearchHintResult> getSearchHints(String searchTerm, Integer startIndex, Integer limit, UUID userId, List<BaseItemKind> includeItemTypes, List<BaseItemKind> excludeItemTypes, List<MediaType> mediaTypes, UUID parentId, Boolean isMovie, Boolean isSeries, Boolean isNews, Boolean isKids, Boolean isSports, Boolean includePeople, Boolean includeMedia, Boolean includeGenres, Boolean includeStudios, Boolean includeArtists) throws WebClientResponseException {
        ParameterizedTypeReference<SearchHintResult> localVarReturnType = new ParameterizedTypeReference<SearchHintResult>() {};
        return getSearchHintsRequestCreation(searchTerm, startIndex, limit, userId, includeItemTypes, excludeItemTypes, mediaTypes, parentId, isMovie, isSeries, isNews, isKids, isSports, includePeople, includeMedia, includeGenres, includeStudios, includeArtists).bodyToMono(localVarReturnType);
    }

    /**
     * Gets the search hint result.
     * 
     * <p><b>200</b> - Search hint returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param searchTerm The search term to filter on.
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the results.
     * @param limit Optional. The maximum number of records to return.
     * @param userId Optional. Supply a user id to search within a user&#39;s library or omit to search all.
     * @param includeItemTypes If specified, only results with the specified item types are returned. This allows multiple, comma delimited.
     * @param excludeItemTypes If specified, results with these item types are filtered out. This allows multiple, comma delimited.
     * @param mediaTypes If specified, only results with the specified media types are returned. This allows multiple, comma delimited.
     * @param parentId If specified, only children of the parent are returned.
     * @param isMovie Optional filter for movies.
     * @param isSeries Optional filter for series.
     * @param isNews Optional filter for news.
     * @param isKids Optional filter for kids.
     * @param isSports Optional filter for sports.
     * @param includePeople Optional filter whether to include people.
     * @param includeMedia Optional filter whether to include media.
     * @param includeGenres Optional filter whether to include genres.
     * @param includeStudios Optional filter whether to include studios.
     * @param includeArtists Optional filter whether to include artists.
     * @return ResponseEntity&lt;SearchHintResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<SearchHintResult>> getSearchHintsWithHttpInfo(String searchTerm, Integer startIndex, Integer limit, UUID userId, List<BaseItemKind> includeItemTypes, List<BaseItemKind> excludeItemTypes, List<MediaType> mediaTypes, UUID parentId, Boolean isMovie, Boolean isSeries, Boolean isNews, Boolean isKids, Boolean isSports, Boolean includePeople, Boolean includeMedia, Boolean includeGenres, Boolean includeStudios, Boolean includeArtists) throws WebClientResponseException {
        ParameterizedTypeReference<SearchHintResult> localVarReturnType = new ParameterizedTypeReference<SearchHintResult>() {};
        return getSearchHintsRequestCreation(searchTerm, startIndex, limit, userId, includeItemTypes, excludeItemTypes, mediaTypes, parentId, isMovie, isSeries, isNews, isKids, isSports, includePeople, includeMedia, includeGenres, includeStudios, includeArtists).toEntity(localVarReturnType);
    }

    /**
     * Gets the search hint result.
     * 
     * <p><b>200</b> - Search hint returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param searchTerm The search term to filter on.
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the results.
     * @param limit Optional. The maximum number of records to return.
     * @param userId Optional. Supply a user id to search within a user&#39;s library or omit to search all.
     * @param includeItemTypes If specified, only results with the specified item types are returned. This allows multiple, comma delimited.
     * @param excludeItemTypes If specified, results with these item types are filtered out. This allows multiple, comma delimited.
     * @param mediaTypes If specified, only results with the specified media types are returned. This allows multiple, comma delimited.
     * @param parentId If specified, only children of the parent are returned.
     * @param isMovie Optional filter for movies.
     * @param isSeries Optional filter for series.
     * @param isNews Optional filter for news.
     * @param isKids Optional filter for kids.
     * @param isSports Optional filter for sports.
     * @param includePeople Optional filter whether to include people.
     * @param includeMedia Optional filter whether to include media.
     * @param includeGenres Optional filter whether to include genres.
     * @param includeStudios Optional filter whether to include studios.
     * @param includeArtists Optional filter whether to include artists.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getSearchHintsWithResponseSpec(String searchTerm, Integer startIndex, Integer limit, UUID userId, List<BaseItemKind> includeItemTypes, List<BaseItemKind> excludeItemTypes, List<MediaType> mediaTypes, UUID parentId, Boolean isMovie, Boolean isSeries, Boolean isNews, Boolean isKids, Boolean isSports, Boolean includePeople, Boolean includeMedia, Boolean includeGenres, Boolean includeStudios, Boolean includeArtists) throws WebClientResponseException {
        return getSearchHintsRequestCreation(searchTerm, startIndex, limit, userId, includeItemTypes, excludeItemTypes, mediaTypes, parentId, isMovie, isSeries, isNews, isKids, isSports, includePeople, includeMedia, includeGenres, includeStudios, includeArtists);
    }
}
