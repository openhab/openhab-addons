package org.openhab.binding.jellyfin.internal.api.version.legacy;

import org.openhab.binding.jellyfin.internal.api.version.ApiClient;

import org.openhab.binding.jellyfin.internal.api.version.legacy.model.AlbumInfoRemoteSearchQuery;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.ArtistInfoRemoteSearchQuery;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.BookInfoRemoteSearchQuery;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.BoxSetInfoRemoteSearchQuery;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.ExternalIdInfo;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.MovieInfoRemoteSearchQuery;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.MusicVideoInfoRemoteSearchQuery;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.PersonLookupInfoRemoteSearchQuery;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.ProblemDetails;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.RemoteSearchResult;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.SeriesInfoRemoteSearchQuery;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.TrailerInfoRemoteSearchQuery;
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
public class ItemLookupApi {
    private ApiClient apiClient;

    public ItemLookupApi() {
        this(new ApiClient());
    }

    @Autowired
    public ItemLookupApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    
    /**
     * Applies search criteria to an item and refreshes metadata.
     * 
     * <p><b>204</b> - Item metadata refreshed.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId Item id.
     * @param remoteSearchResult The remote search result.
     * @param replaceAllImages Optional. Whether or not to replace all images. Default: True.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec applySearchCriteriaRequestCreation(UUID itemId, RemoteSearchResult remoteSearchResult, Boolean replaceAllImages) throws WebClientResponseException {
        Object postBody = remoteSearchResult;
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException("Missing the required parameter 'itemId' when calling applySearchCriteria", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'remoteSearchResult' is set
        if (remoteSearchResult == null) {
            throw new WebClientResponseException("Missing the required parameter 'remoteSearchResult' when calling applySearchCriteria", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("itemId", itemId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "replaceAllImages", replaceAllImages));
        
        final String[] localVarAccepts = { };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { 
            "application/json", "text/json", "application/*+json"
        };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return apiClient.invokeAPI("/Items/RemoteSearch/Apply/{itemId}", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Applies search criteria to an item and refreshes metadata.
     * 
     * <p><b>204</b> - Item metadata refreshed.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId Item id.
     * @param remoteSearchResult The remote search result.
     * @param replaceAllImages Optional. Whether or not to replace all images. Default: True.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> applySearchCriteria(UUID itemId, RemoteSearchResult remoteSearchResult, Boolean replaceAllImages) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return applySearchCriteriaRequestCreation(itemId, remoteSearchResult, replaceAllImages).bodyToMono(localVarReturnType);
    }

    /**
     * Applies search criteria to an item and refreshes metadata.
     * 
     * <p><b>204</b> - Item metadata refreshed.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId Item id.
     * @param remoteSearchResult The remote search result.
     * @param replaceAllImages Optional. Whether or not to replace all images. Default: True.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> applySearchCriteriaWithHttpInfo(UUID itemId, RemoteSearchResult remoteSearchResult, Boolean replaceAllImages) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return applySearchCriteriaRequestCreation(itemId, remoteSearchResult, replaceAllImages).toEntity(localVarReturnType);
    }

    /**
     * Applies search criteria to an item and refreshes metadata.
     * 
     * <p><b>204</b> - Item metadata refreshed.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId Item id.
     * @param remoteSearchResult The remote search result.
     * @param replaceAllImages Optional. Whether or not to replace all images. Default: True.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec applySearchCriteriaWithResponseSpec(UUID itemId, RemoteSearchResult remoteSearchResult, Boolean replaceAllImages) throws WebClientResponseException {
        return applySearchCriteriaRequestCreation(itemId, remoteSearchResult, replaceAllImages);
    }

    /**
     * Get book remote search.
     * 
     * <p><b>200</b> - Book remote search executed.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param bookInfoRemoteSearchQuery Remote search query.
     * @return List&lt;RemoteSearchResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getBookRemoteSearchResultsRequestCreation(BookInfoRemoteSearchQuery bookInfoRemoteSearchQuery) throws WebClientResponseException {
        Object postBody = bookInfoRemoteSearchQuery;
        // verify the required parameter 'bookInfoRemoteSearchQuery' is set
        if (bookInfoRemoteSearchQuery == null) {
            throw new WebClientResponseException("Missing the required parameter 'bookInfoRemoteSearchQuery' when calling getBookRemoteSearchResults", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
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
        final String[] localVarContentTypes = { 
            "application/json", "text/json", "application/*+json"
        };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<RemoteSearchResult> localVarReturnType = new ParameterizedTypeReference<RemoteSearchResult>() {};
        return apiClient.invokeAPI("/Items/RemoteSearch/Book", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get book remote search.
     * 
     * <p><b>200</b> - Book remote search executed.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param bookInfoRemoteSearchQuery Remote search query.
     * @return List&lt;RemoteSearchResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Flux<RemoteSearchResult> getBookRemoteSearchResults(BookInfoRemoteSearchQuery bookInfoRemoteSearchQuery) throws WebClientResponseException {
        ParameterizedTypeReference<RemoteSearchResult> localVarReturnType = new ParameterizedTypeReference<RemoteSearchResult>() {};
        return getBookRemoteSearchResultsRequestCreation(bookInfoRemoteSearchQuery).bodyToFlux(localVarReturnType);
    }

    /**
     * Get book remote search.
     * 
     * <p><b>200</b> - Book remote search executed.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param bookInfoRemoteSearchQuery Remote search query.
     * @return ResponseEntity&lt;List&lt;RemoteSearchResult&gt;&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<List<RemoteSearchResult>>> getBookRemoteSearchResultsWithHttpInfo(BookInfoRemoteSearchQuery bookInfoRemoteSearchQuery) throws WebClientResponseException {
        ParameterizedTypeReference<RemoteSearchResult> localVarReturnType = new ParameterizedTypeReference<RemoteSearchResult>() {};
        return getBookRemoteSearchResultsRequestCreation(bookInfoRemoteSearchQuery).toEntityList(localVarReturnType);
    }

    /**
     * Get book remote search.
     * 
     * <p><b>200</b> - Book remote search executed.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param bookInfoRemoteSearchQuery Remote search query.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getBookRemoteSearchResultsWithResponseSpec(BookInfoRemoteSearchQuery bookInfoRemoteSearchQuery) throws WebClientResponseException {
        return getBookRemoteSearchResultsRequestCreation(bookInfoRemoteSearchQuery);
    }

    /**
     * Get box set remote search.
     * 
     * <p><b>200</b> - Box set remote search executed.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param boxSetInfoRemoteSearchQuery Remote search query.
     * @return List&lt;RemoteSearchResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getBoxSetRemoteSearchResultsRequestCreation(BoxSetInfoRemoteSearchQuery boxSetInfoRemoteSearchQuery) throws WebClientResponseException {
        Object postBody = boxSetInfoRemoteSearchQuery;
        // verify the required parameter 'boxSetInfoRemoteSearchQuery' is set
        if (boxSetInfoRemoteSearchQuery == null) {
            throw new WebClientResponseException("Missing the required parameter 'boxSetInfoRemoteSearchQuery' when calling getBoxSetRemoteSearchResults", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
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
        final String[] localVarContentTypes = { 
            "application/json", "text/json", "application/*+json"
        };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<RemoteSearchResult> localVarReturnType = new ParameterizedTypeReference<RemoteSearchResult>() {};
        return apiClient.invokeAPI("/Items/RemoteSearch/BoxSet", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get box set remote search.
     * 
     * <p><b>200</b> - Box set remote search executed.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param boxSetInfoRemoteSearchQuery Remote search query.
     * @return List&lt;RemoteSearchResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Flux<RemoteSearchResult> getBoxSetRemoteSearchResults(BoxSetInfoRemoteSearchQuery boxSetInfoRemoteSearchQuery) throws WebClientResponseException {
        ParameterizedTypeReference<RemoteSearchResult> localVarReturnType = new ParameterizedTypeReference<RemoteSearchResult>() {};
        return getBoxSetRemoteSearchResultsRequestCreation(boxSetInfoRemoteSearchQuery).bodyToFlux(localVarReturnType);
    }

    /**
     * Get box set remote search.
     * 
     * <p><b>200</b> - Box set remote search executed.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param boxSetInfoRemoteSearchQuery Remote search query.
     * @return ResponseEntity&lt;List&lt;RemoteSearchResult&gt;&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<List<RemoteSearchResult>>> getBoxSetRemoteSearchResultsWithHttpInfo(BoxSetInfoRemoteSearchQuery boxSetInfoRemoteSearchQuery) throws WebClientResponseException {
        ParameterizedTypeReference<RemoteSearchResult> localVarReturnType = new ParameterizedTypeReference<RemoteSearchResult>() {};
        return getBoxSetRemoteSearchResultsRequestCreation(boxSetInfoRemoteSearchQuery).toEntityList(localVarReturnType);
    }

    /**
     * Get box set remote search.
     * 
     * <p><b>200</b> - Box set remote search executed.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param boxSetInfoRemoteSearchQuery Remote search query.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getBoxSetRemoteSearchResultsWithResponseSpec(BoxSetInfoRemoteSearchQuery boxSetInfoRemoteSearchQuery) throws WebClientResponseException {
        return getBoxSetRemoteSearchResultsRequestCreation(boxSetInfoRemoteSearchQuery);
    }

    /**
     * Get the item&#39;s external id info.
     * 
     * <p><b>200</b> - External id info retrieved.
     * <p><b>404</b> - Item not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId Item id.
     * @return List&lt;ExternalIdInfo&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getExternalIdInfosRequestCreation(UUID itemId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException("Missing the required parameter 'itemId' when calling getExternalIdInfos", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("itemId", itemId);

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

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<ExternalIdInfo> localVarReturnType = new ParameterizedTypeReference<ExternalIdInfo>() {};
        return apiClient.invokeAPI("/Items/{itemId}/ExternalIdInfos", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get the item&#39;s external id info.
     * 
     * <p><b>200</b> - External id info retrieved.
     * <p><b>404</b> - Item not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId Item id.
     * @return List&lt;ExternalIdInfo&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Flux<ExternalIdInfo> getExternalIdInfos(UUID itemId) throws WebClientResponseException {
        ParameterizedTypeReference<ExternalIdInfo> localVarReturnType = new ParameterizedTypeReference<ExternalIdInfo>() {};
        return getExternalIdInfosRequestCreation(itemId).bodyToFlux(localVarReturnType);
    }

    /**
     * Get the item&#39;s external id info.
     * 
     * <p><b>200</b> - External id info retrieved.
     * <p><b>404</b> - Item not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId Item id.
     * @return ResponseEntity&lt;List&lt;ExternalIdInfo&gt;&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<List<ExternalIdInfo>>> getExternalIdInfosWithHttpInfo(UUID itemId) throws WebClientResponseException {
        ParameterizedTypeReference<ExternalIdInfo> localVarReturnType = new ParameterizedTypeReference<ExternalIdInfo>() {};
        return getExternalIdInfosRequestCreation(itemId).toEntityList(localVarReturnType);
    }

    /**
     * Get the item&#39;s external id info.
     * 
     * <p><b>200</b> - External id info retrieved.
     * <p><b>404</b> - Item not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId Item id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getExternalIdInfosWithResponseSpec(UUID itemId) throws WebClientResponseException {
        return getExternalIdInfosRequestCreation(itemId);
    }

    /**
     * Get movie remote search.
     * 
     * <p><b>200</b> - Movie remote search executed.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param movieInfoRemoteSearchQuery Remote search query.
     * @return List&lt;RemoteSearchResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getMovieRemoteSearchResultsRequestCreation(MovieInfoRemoteSearchQuery movieInfoRemoteSearchQuery) throws WebClientResponseException {
        Object postBody = movieInfoRemoteSearchQuery;
        // verify the required parameter 'movieInfoRemoteSearchQuery' is set
        if (movieInfoRemoteSearchQuery == null) {
            throw new WebClientResponseException("Missing the required parameter 'movieInfoRemoteSearchQuery' when calling getMovieRemoteSearchResults", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
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
        final String[] localVarContentTypes = { 
            "application/json", "text/json", "application/*+json"
        };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<RemoteSearchResult> localVarReturnType = new ParameterizedTypeReference<RemoteSearchResult>() {};
        return apiClient.invokeAPI("/Items/RemoteSearch/Movie", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get movie remote search.
     * 
     * <p><b>200</b> - Movie remote search executed.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param movieInfoRemoteSearchQuery Remote search query.
     * @return List&lt;RemoteSearchResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Flux<RemoteSearchResult> getMovieRemoteSearchResults(MovieInfoRemoteSearchQuery movieInfoRemoteSearchQuery) throws WebClientResponseException {
        ParameterizedTypeReference<RemoteSearchResult> localVarReturnType = new ParameterizedTypeReference<RemoteSearchResult>() {};
        return getMovieRemoteSearchResultsRequestCreation(movieInfoRemoteSearchQuery).bodyToFlux(localVarReturnType);
    }

    /**
     * Get movie remote search.
     * 
     * <p><b>200</b> - Movie remote search executed.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param movieInfoRemoteSearchQuery Remote search query.
     * @return ResponseEntity&lt;List&lt;RemoteSearchResult&gt;&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<List<RemoteSearchResult>>> getMovieRemoteSearchResultsWithHttpInfo(MovieInfoRemoteSearchQuery movieInfoRemoteSearchQuery) throws WebClientResponseException {
        ParameterizedTypeReference<RemoteSearchResult> localVarReturnType = new ParameterizedTypeReference<RemoteSearchResult>() {};
        return getMovieRemoteSearchResultsRequestCreation(movieInfoRemoteSearchQuery).toEntityList(localVarReturnType);
    }

    /**
     * Get movie remote search.
     * 
     * <p><b>200</b> - Movie remote search executed.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param movieInfoRemoteSearchQuery Remote search query.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getMovieRemoteSearchResultsWithResponseSpec(MovieInfoRemoteSearchQuery movieInfoRemoteSearchQuery) throws WebClientResponseException {
        return getMovieRemoteSearchResultsRequestCreation(movieInfoRemoteSearchQuery);
    }

    /**
     * Get music album remote search.
     * 
     * <p><b>200</b> - Music album remote search executed.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param albumInfoRemoteSearchQuery Remote search query.
     * @return List&lt;RemoteSearchResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getMusicAlbumRemoteSearchResultsRequestCreation(AlbumInfoRemoteSearchQuery albumInfoRemoteSearchQuery) throws WebClientResponseException {
        Object postBody = albumInfoRemoteSearchQuery;
        // verify the required parameter 'albumInfoRemoteSearchQuery' is set
        if (albumInfoRemoteSearchQuery == null) {
            throw new WebClientResponseException("Missing the required parameter 'albumInfoRemoteSearchQuery' when calling getMusicAlbumRemoteSearchResults", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
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
        final String[] localVarContentTypes = { 
            "application/json", "text/json", "application/*+json"
        };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<RemoteSearchResult> localVarReturnType = new ParameterizedTypeReference<RemoteSearchResult>() {};
        return apiClient.invokeAPI("/Items/RemoteSearch/MusicAlbum", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get music album remote search.
     * 
     * <p><b>200</b> - Music album remote search executed.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param albumInfoRemoteSearchQuery Remote search query.
     * @return List&lt;RemoteSearchResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Flux<RemoteSearchResult> getMusicAlbumRemoteSearchResults(AlbumInfoRemoteSearchQuery albumInfoRemoteSearchQuery) throws WebClientResponseException {
        ParameterizedTypeReference<RemoteSearchResult> localVarReturnType = new ParameterizedTypeReference<RemoteSearchResult>() {};
        return getMusicAlbumRemoteSearchResultsRequestCreation(albumInfoRemoteSearchQuery).bodyToFlux(localVarReturnType);
    }

    /**
     * Get music album remote search.
     * 
     * <p><b>200</b> - Music album remote search executed.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param albumInfoRemoteSearchQuery Remote search query.
     * @return ResponseEntity&lt;List&lt;RemoteSearchResult&gt;&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<List<RemoteSearchResult>>> getMusicAlbumRemoteSearchResultsWithHttpInfo(AlbumInfoRemoteSearchQuery albumInfoRemoteSearchQuery) throws WebClientResponseException {
        ParameterizedTypeReference<RemoteSearchResult> localVarReturnType = new ParameterizedTypeReference<RemoteSearchResult>() {};
        return getMusicAlbumRemoteSearchResultsRequestCreation(albumInfoRemoteSearchQuery).toEntityList(localVarReturnType);
    }

    /**
     * Get music album remote search.
     * 
     * <p><b>200</b> - Music album remote search executed.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param albumInfoRemoteSearchQuery Remote search query.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getMusicAlbumRemoteSearchResultsWithResponseSpec(AlbumInfoRemoteSearchQuery albumInfoRemoteSearchQuery) throws WebClientResponseException {
        return getMusicAlbumRemoteSearchResultsRequestCreation(albumInfoRemoteSearchQuery);
    }

    /**
     * Get music artist remote search.
     * 
     * <p><b>200</b> - Music artist remote search executed.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param artistInfoRemoteSearchQuery Remote search query.
     * @return List&lt;RemoteSearchResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getMusicArtistRemoteSearchResultsRequestCreation(ArtistInfoRemoteSearchQuery artistInfoRemoteSearchQuery) throws WebClientResponseException {
        Object postBody = artistInfoRemoteSearchQuery;
        // verify the required parameter 'artistInfoRemoteSearchQuery' is set
        if (artistInfoRemoteSearchQuery == null) {
            throw new WebClientResponseException("Missing the required parameter 'artistInfoRemoteSearchQuery' when calling getMusicArtistRemoteSearchResults", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
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
        final String[] localVarContentTypes = { 
            "application/json", "text/json", "application/*+json"
        };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<RemoteSearchResult> localVarReturnType = new ParameterizedTypeReference<RemoteSearchResult>() {};
        return apiClient.invokeAPI("/Items/RemoteSearch/MusicArtist", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get music artist remote search.
     * 
     * <p><b>200</b> - Music artist remote search executed.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param artistInfoRemoteSearchQuery Remote search query.
     * @return List&lt;RemoteSearchResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Flux<RemoteSearchResult> getMusicArtistRemoteSearchResults(ArtistInfoRemoteSearchQuery artistInfoRemoteSearchQuery) throws WebClientResponseException {
        ParameterizedTypeReference<RemoteSearchResult> localVarReturnType = new ParameterizedTypeReference<RemoteSearchResult>() {};
        return getMusicArtistRemoteSearchResultsRequestCreation(artistInfoRemoteSearchQuery).bodyToFlux(localVarReturnType);
    }

    /**
     * Get music artist remote search.
     * 
     * <p><b>200</b> - Music artist remote search executed.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param artistInfoRemoteSearchQuery Remote search query.
     * @return ResponseEntity&lt;List&lt;RemoteSearchResult&gt;&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<List<RemoteSearchResult>>> getMusicArtistRemoteSearchResultsWithHttpInfo(ArtistInfoRemoteSearchQuery artistInfoRemoteSearchQuery) throws WebClientResponseException {
        ParameterizedTypeReference<RemoteSearchResult> localVarReturnType = new ParameterizedTypeReference<RemoteSearchResult>() {};
        return getMusicArtistRemoteSearchResultsRequestCreation(artistInfoRemoteSearchQuery).toEntityList(localVarReturnType);
    }

    /**
     * Get music artist remote search.
     * 
     * <p><b>200</b> - Music artist remote search executed.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param artistInfoRemoteSearchQuery Remote search query.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getMusicArtistRemoteSearchResultsWithResponseSpec(ArtistInfoRemoteSearchQuery artistInfoRemoteSearchQuery) throws WebClientResponseException {
        return getMusicArtistRemoteSearchResultsRequestCreation(artistInfoRemoteSearchQuery);
    }

    /**
     * Get music video remote search.
     * 
     * <p><b>200</b> - Music video remote search executed.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param musicVideoInfoRemoteSearchQuery Remote search query.
     * @return List&lt;RemoteSearchResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getMusicVideoRemoteSearchResultsRequestCreation(MusicVideoInfoRemoteSearchQuery musicVideoInfoRemoteSearchQuery) throws WebClientResponseException {
        Object postBody = musicVideoInfoRemoteSearchQuery;
        // verify the required parameter 'musicVideoInfoRemoteSearchQuery' is set
        if (musicVideoInfoRemoteSearchQuery == null) {
            throw new WebClientResponseException("Missing the required parameter 'musicVideoInfoRemoteSearchQuery' when calling getMusicVideoRemoteSearchResults", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
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
        final String[] localVarContentTypes = { 
            "application/json", "text/json", "application/*+json"
        };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<RemoteSearchResult> localVarReturnType = new ParameterizedTypeReference<RemoteSearchResult>() {};
        return apiClient.invokeAPI("/Items/RemoteSearch/MusicVideo", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get music video remote search.
     * 
     * <p><b>200</b> - Music video remote search executed.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param musicVideoInfoRemoteSearchQuery Remote search query.
     * @return List&lt;RemoteSearchResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Flux<RemoteSearchResult> getMusicVideoRemoteSearchResults(MusicVideoInfoRemoteSearchQuery musicVideoInfoRemoteSearchQuery) throws WebClientResponseException {
        ParameterizedTypeReference<RemoteSearchResult> localVarReturnType = new ParameterizedTypeReference<RemoteSearchResult>() {};
        return getMusicVideoRemoteSearchResultsRequestCreation(musicVideoInfoRemoteSearchQuery).bodyToFlux(localVarReturnType);
    }

    /**
     * Get music video remote search.
     * 
     * <p><b>200</b> - Music video remote search executed.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param musicVideoInfoRemoteSearchQuery Remote search query.
     * @return ResponseEntity&lt;List&lt;RemoteSearchResult&gt;&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<List<RemoteSearchResult>>> getMusicVideoRemoteSearchResultsWithHttpInfo(MusicVideoInfoRemoteSearchQuery musicVideoInfoRemoteSearchQuery) throws WebClientResponseException {
        ParameterizedTypeReference<RemoteSearchResult> localVarReturnType = new ParameterizedTypeReference<RemoteSearchResult>() {};
        return getMusicVideoRemoteSearchResultsRequestCreation(musicVideoInfoRemoteSearchQuery).toEntityList(localVarReturnType);
    }

    /**
     * Get music video remote search.
     * 
     * <p><b>200</b> - Music video remote search executed.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param musicVideoInfoRemoteSearchQuery Remote search query.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getMusicVideoRemoteSearchResultsWithResponseSpec(MusicVideoInfoRemoteSearchQuery musicVideoInfoRemoteSearchQuery) throws WebClientResponseException {
        return getMusicVideoRemoteSearchResultsRequestCreation(musicVideoInfoRemoteSearchQuery);
    }

    /**
     * Get person remote search.
     * 
     * <p><b>200</b> - Person remote search executed.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param personLookupInfoRemoteSearchQuery Remote search query.
     * @return List&lt;RemoteSearchResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getPersonRemoteSearchResultsRequestCreation(PersonLookupInfoRemoteSearchQuery personLookupInfoRemoteSearchQuery) throws WebClientResponseException {
        Object postBody = personLookupInfoRemoteSearchQuery;
        // verify the required parameter 'personLookupInfoRemoteSearchQuery' is set
        if (personLookupInfoRemoteSearchQuery == null) {
            throw new WebClientResponseException("Missing the required parameter 'personLookupInfoRemoteSearchQuery' when calling getPersonRemoteSearchResults", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
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
        final String[] localVarContentTypes = { 
            "application/json", "text/json", "application/*+json"
        };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<RemoteSearchResult> localVarReturnType = new ParameterizedTypeReference<RemoteSearchResult>() {};
        return apiClient.invokeAPI("/Items/RemoteSearch/Person", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get person remote search.
     * 
     * <p><b>200</b> - Person remote search executed.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param personLookupInfoRemoteSearchQuery Remote search query.
     * @return List&lt;RemoteSearchResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Flux<RemoteSearchResult> getPersonRemoteSearchResults(PersonLookupInfoRemoteSearchQuery personLookupInfoRemoteSearchQuery) throws WebClientResponseException {
        ParameterizedTypeReference<RemoteSearchResult> localVarReturnType = new ParameterizedTypeReference<RemoteSearchResult>() {};
        return getPersonRemoteSearchResultsRequestCreation(personLookupInfoRemoteSearchQuery).bodyToFlux(localVarReturnType);
    }

    /**
     * Get person remote search.
     * 
     * <p><b>200</b> - Person remote search executed.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param personLookupInfoRemoteSearchQuery Remote search query.
     * @return ResponseEntity&lt;List&lt;RemoteSearchResult&gt;&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<List<RemoteSearchResult>>> getPersonRemoteSearchResultsWithHttpInfo(PersonLookupInfoRemoteSearchQuery personLookupInfoRemoteSearchQuery) throws WebClientResponseException {
        ParameterizedTypeReference<RemoteSearchResult> localVarReturnType = new ParameterizedTypeReference<RemoteSearchResult>() {};
        return getPersonRemoteSearchResultsRequestCreation(personLookupInfoRemoteSearchQuery).toEntityList(localVarReturnType);
    }

    /**
     * Get person remote search.
     * 
     * <p><b>200</b> - Person remote search executed.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param personLookupInfoRemoteSearchQuery Remote search query.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getPersonRemoteSearchResultsWithResponseSpec(PersonLookupInfoRemoteSearchQuery personLookupInfoRemoteSearchQuery) throws WebClientResponseException {
        return getPersonRemoteSearchResultsRequestCreation(personLookupInfoRemoteSearchQuery);
    }

    /**
     * Get series remote search.
     * 
     * <p><b>200</b> - Series remote search executed.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param seriesInfoRemoteSearchQuery Remote search query.
     * @return List&lt;RemoteSearchResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getSeriesRemoteSearchResultsRequestCreation(SeriesInfoRemoteSearchQuery seriesInfoRemoteSearchQuery) throws WebClientResponseException {
        Object postBody = seriesInfoRemoteSearchQuery;
        // verify the required parameter 'seriesInfoRemoteSearchQuery' is set
        if (seriesInfoRemoteSearchQuery == null) {
            throw new WebClientResponseException("Missing the required parameter 'seriesInfoRemoteSearchQuery' when calling getSeriesRemoteSearchResults", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
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
        final String[] localVarContentTypes = { 
            "application/json", "text/json", "application/*+json"
        };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<RemoteSearchResult> localVarReturnType = new ParameterizedTypeReference<RemoteSearchResult>() {};
        return apiClient.invokeAPI("/Items/RemoteSearch/Series", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get series remote search.
     * 
     * <p><b>200</b> - Series remote search executed.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param seriesInfoRemoteSearchQuery Remote search query.
     * @return List&lt;RemoteSearchResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Flux<RemoteSearchResult> getSeriesRemoteSearchResults(SeriesInfoRemoteSearchQuery seriesInfoRemoteSearchQuery) throws WebClientResponseException {
        ParameterizedTypeReference<RemoteSearchResult> localVarReturnType = new ParameterizedTypeReference<RemoteSearchResult>() {};
        return getSeriesRemoteSearchResultsRequestCreation(seriesInfoRemoteSearchQuery).bodyToFlux(localVarReturnType);
    }

    /**
     * Get series remote search.
     * 
     * <p><b>200</b> - Series remote search executed.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param seriesInfoRemoteSearchQuery Remote search query.
     * @return ResponseEntity&lt;List&lt;RemoteSearchResult&gt;&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<List<RemoteSearchResult>>> getSeriesRemoteSearchResultsWithHttpInfo(SeriesInfoRemoteSearchQuery seriesInfoRemoteSearchQuery) throws WebClientResponseException {
        ParameterizedTypeReference<RemoteSearchResult> localVarReturnType = new ParameterizedTypeReference<RemoteSearchResult>() {};
        return getSeriesRemoteSearchResultsRequestCreation(seriesInfoRemoteSearchQuery).toEntityList(localVarReturnType);
    }

    /**
     * Get series remote search.
     * 
     * <p><b>200</b> - Series remote search executed.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param seriesInfoRemoteSearchQuery Remote search query.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getSeriesRemoteSearchResultsWithResponseSpec(SeriesInfoRemoteSearchQuery seriesInfoRemoteSearchQuery) throws WebClientResponseException {
        return getSeriesRemoteSearchResultsRequestCreation(seriesInfoRemoteSearchQuery);
    }

    /**
     * Get trailer remote search.
     * 
     * <p><b>200</b> - Trailer remote search executed.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param trailerInfoRemoteSearchQuery Remote search query.
     * @return List&lt;RemoteSearchResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getTrailerRemoteSearchResultsRequestCreation(TrailerInfoRemoteSearchQuery trailerInfoRemoteSearchQuery) throws WebClientResponseException {
        Object postBody = trailerInfoRemoteSearchQuery;
        // verify the required parameter 'trailerInfoRemoteSearchQuery' is set
        if (trailerInfoRemoteSearchQuery == null) {
            throw new WebClientResponseException("Missing the required parameter 'trailerInfoRemoteSearchQuery' when calling getTrailerRemoteSearchResults", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
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
        final String[] localVarContentTypes = { 
            "application/json", "text/json", "application/*+json"
        };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<RemoteSearchResult> localVarReturnType = new ParameterizedTypeReference<RemoteSearchResult>() {};
        return apiClient.invokeAPI("/Items/RemoteSearch/Trailer", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get trailer remote search.
     * 
     * <p><b>200</b> - Trailer remote search executed.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param trailerInfoRemoteSearchQuery Remote search query.
     * @return List&lt;RemoteSearchResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Flux<RemoteSearchResult> getTrailerRemoteSearchResults(TrailerInfoRemoteSearchQuery trailerInfoRemoteSearchQuery) throws WebClientResponseException {
        ParameterizedTypeReference<RemoteSearchResult> localVarReturnType = new ParameterizedTypeReference<RemoteSearchResult>() {};
        return getTrailerRemoteSearchResultsRequestCreation(trailerInfoRemoteSearchQuery).bodyToFlux(localVarReturnType);
    }

    /**
     * Get trailer remote search.
     * 
     * <p><b>200</b> - Trailer remote search executed.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param trailerInfoRemoteSearchQuery Remote search query.
     * @return ResponseEntity&lt;List&lt;RemoteSearchResult&gt;&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<List<RemoteSearchResult>>> getTrailerRemoteSearchResultsWithHttpInfo(TrailerInfoRemoteSearchQuery trailerInfoRemoteSearchQuery) throws WebClientResponseException {
        ParameterizedTypeReference<RemoteSearchResult> localVarReturnType = new ParameterizedTypeReference<RemoteSearchResult>() {};
        return getTrailerRemoteSearchResultsRequestCreation(trailerInfoRemoteSearchQuery).toEntityList(localVarReturnType);
    }

    /**
     * Get trailer remote search.
     * 
     * <p><b>200</b> - Trailer remote search executed.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param trailerInfoRemoteSearchQuery Remote search query.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getTrailerRemoteSearchResultsWithResponseSpec(TrailerInfoRemoteSearchQuery trailerInfoRemoteSearchQuery) throws WebClientResponseException {
        return getTrailerRemoteSearchResultsRequestCreation(trailerInfoRemoteSearchQuery);
    }
}
