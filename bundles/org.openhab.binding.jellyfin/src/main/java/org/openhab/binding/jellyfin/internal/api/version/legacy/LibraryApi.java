package org.openhab.binding.jellyfin.internal.api.version.legacy;

import org.openhab.binding.jellyfin.internal.api.version.ApiClient;

import org.openhab.binding.jellyfin.internal.api.version.legacy.model.AllThemeMediaResult;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.BaseItemDto;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.BaseItemDtoQueryResult;
import java.io.File;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.ItemCounts;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.ItemFields;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.LibraryOptionsResultDto;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.MediaUpdateInfoDto;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.ProblemDetails;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.ThemeMediaResult;
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
public class LibraryApi {
    private ApiClient apiClient;

    public LibraryApi() {
        this(new ApiClient());
    }

    @Autowired
    public LibraryApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    
    /**
     * Deletes an item from the library and filesystem.
     * 
     * <p><b>204</b> - Item deleted.
     * <p><b>401</b> - Unauthorized access.
     * <p><b>403</b> - Forbidden
     * @param itemId The item id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec deleteItemRequestCreation(UUID itemId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException("Missing the required parameter 'itemId' when calling deleteItem", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
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

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return apiClient.invokeAPI("/Items/{itemId}", HttpMethod.DELETE, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Deletes an item from the library and filesystem.
     * 
     * <p><b>204</b> - Item deleted.
     * <p><b>401</b> - Unauthorized access.
     * <p><b>403</b> - Forbidden
     * @param itemId The item id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> deleteItem(UUID itemId) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return deleteItemRequestCreation(itemId).bodyToMono(localVarReturnType);
    }

    /**
     * Deletes an item from the library and filesystem.
     * 
     * <p><b>204</b> - Item deleted.
     * <p><b>401</b> - Unauthorized access.
     * <p><b>403</b> - Forbidden
     * @param itemId The item id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> deleteItemWithHttpInfo(UUID itemId) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return deleteItemRequestCreation(itemId).toEntity(localVarReturnType);
    }

    /**
     * Deletes an item from the library and filesystem.
     * 
     * <p><b>204</b> - Item deleted.
     * <p><b>401</b> - Unauthorized access.
     * <p><b>403</b> - Forbidden
     * @param itemId The item id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec deleteItemWithResponseSpec(UUID itemId) throws WebClientResponseException {
        return deleteItemRequestCreation(itemId);
    }

    /**
     * Deletes items from the library and filesystem.
     * 
     * <p><b>204</b> - Items deleted.
     * <p><b>401</b> - Unauthorized access.
     * <p><b>403</b> - Forbidden
     * @param ids The item ids.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec deleteItemsRequestCreation(List<UUID> ids) throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "ids", ids));
        
        final String[] localVarAccepts = { 
            "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return apiClient.invokeAPI("/Items", HttpMethod.DELETE, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Deletes items from the library and filesystem.
     * 
     * <p><b>204</b> - Items deleted.
     * <p><b>401</b> - Unauthorized access.
     * <p><b>403</b> - Forbidden
     * @param ids The item ids.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> deleteItems(List<UUID> ids) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return deleteItemsRequestCreation(ids).bodyToMono(localVarReturnType);
    }

    /**
     * Deletes items from the library and filesystem.
     * 
     * <p><b>204</b> - Items deleted.
     * <p><b>401</b> - Unauthorized access.
     * <p><b>403</b> - Forbidden
     * @param ids The item ids.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> deleteItemsWithHttpInfo(List<UUID> ids) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return deleteItemsRequestCreation(ids).toEntity(localVarReturnType);
    }

    /**
     * Deletes items from the library and filesystem.
     * 
     * <p><b>204</b> - Items deleted.
     * <p><b>401</b> - Unauthorized access.
     * <p><b>403</b> - Forbidden
     * @param ids The item ids.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec deleteItemsWithResponseSpec(List<UUID> ids) throws WebClientResponseException {
        return deleteItemsRequestCreation(ids);
    }

    /**
     * Gets all parents of an item.
     * 
     * <p><b>200</b> - Item parents returned.
     * <p><b>404</b> - Item not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId The item id.
     * @param userId Optional. Filter by user id, and attach user data.
     * @return List&lt;BaseItemDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getAncestorsRequestCreation(UUID itemId, UUID userId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException("Missing the required parameter 'itemId' when calling getAncestors", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("itemId", itemId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "userId", userId));
        
        final String[] localVarAccepts = { 
            "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<BaseItemDto> localVarReturnType = new ParameterizedTypeReference<BaseItemDto>() {};
        return apiClient.invokeAPI("/Items/{itemId}/Ancestors", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets all parents of an item.
     * 
     * <p><b>200</b> - Item parents returned.
     * <p><b>404</b> - Item not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId The item id.
     * @param userId Optional. Filter by user id, and attach user data.
     * @return List&lt;BaseItemDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Flux<BaseItemDto> getAncestors(UUID itemId, UUID userId) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDto> localVarReturnType = new ParameterizedTypeReference<BaseItemDto>() {};
        return getAncestorsRequestCreation(itemId, userId).bodyToFlux(localVarReturnType);
    }

    /**
     * Gets all parents of an item.
     * 
     * <p><b>200</b> - Item parents returned.
     * <p><b>404</b> - Item not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId The item id.
     * @param userId Optional. Filter by user id, and attach user data.
     * @return ResponseEntity&lt;List&lt;BaseItemDto&gt;&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<List<BaseItemDto>>> getAncestorsWithHttpInfo(UUID itemId, UUID userId) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDto> localVarReturnType = new ParameterizedTypeReference<BaseItemDto>() {};
        return getAncestorsRequestCreation(itemId, userId).toEntityList(localVarReturnType);
    }

    /**
     * Gets all parents of an item.
     * 
     * <p><b>200</b> - Item parents returned.
     * <p><b>404</b> - Item not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId The item id.
     * @param userId Optional. Filter by user id, and attach user data.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getAncestorsWithResponseSpec(UUID itemId, UUID userId) throws WebClientResponseException {
        return getAncestorsRequestCreation(itemId, userId);
    }

    /**
     * Gets critic review for an item.
     * 
     * <p><b>200</b> - Critic reviews returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId The itemId parameter
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     * @deprecated
     */
    @Deprecated
    private ResponseSpec getCriticReviewsRequestCreation(String itemId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException("Missing the required parameter 'itemId' when calling getCriticReviews", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
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

        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {};
        return apiClient.invokeAPI("/Items/{itemId}/CriticReviews", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets critic review for an item.
     * 
     * <p><b>200</b> - Critic reviews returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId The itemId parameter
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<BaseItemDtoQueryResult> getCriticReviews(String itemId) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {};
        return getCriticReviewsRequestCreation(itemId).bodyToMono(localVarReturnType);
    }

    /**
     * Gets critic review for an item.
     * 
     * <p><b>200</b> - Critic reviews returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId The itemId parameter
     * @return ResponseEntity&lt;BaseItemDtoQueryResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<BaseItemDtoQueryResult>> getCriticReviewsWithHttpInfo(String itemId) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {};
        return getCriticReviewsRequestCreation(itemId).toEntity(localVarReturnType);
    }

    /**
     * Gets critic review for an item.
     * 
     * <p><b>200</b> - Critic reviews returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId The itemId parameter
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getCriticReviewsWithResponseSpec(String itemId) throws WebClientResponseException {
        return getCriticReviewsRequestCreation(itemId);
    }

    /**
     * Downloads item media.
     * 
     * <p><b>200</b> - Media downloaded.
     * <p><b>404</b> - Item not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId The item id.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getDownloadRequestCreation(UUID itemId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException("Missing the required parameter 'itemId' when calling getDownload", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("itemId", itemId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "video/*", "audio/*", "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return apiClient.invokeAPI("/Items/{itemId}/Download", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Downloads item media.
     * 
     * <p><b>200</b> - Media downloaded.
     * <p><b>404</b> - Item not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId The item id.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<File> getDownload(UUID itemId) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return getDownloadRequestCreation(itemId).bodyToMono(localVarReturnType);
    }

    /**
     * Downloads item media.
     * 
     * <p><b>200</b> - Media downloaded.
     * <p><b>404</b> - Item not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId The item id.
     * @return ResponseEntity&lt;File&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<File>> getDownloadWithHttpInfo(UUID itemId) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return getDownloadRequestCreation(itemId).toEntity(localVarReturnType);
    }

    /**
     * Downloads item media.
     * 
     * <p><b>200</b> - Media downloaded.
     * <p><b>404</b> - Item not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId The item id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getDownloadWithResponseSpec(UUID itemId) throws WebClientResponseException {
        return getDownloadRequestCreation(itemId);
    }

    /**
     * Get the original file of an item.
     * 
     * <p><b>200</b> - File stream returned.
     * <p><b>404</b> - Item not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId The item id.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getFileRequestCreation(UUID itemId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException("Missing the required parameter 'itemId' when calling getFile", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("itemId", itemId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "video/*", "audio/*", "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return apiClient.invokeAPI("/Items/{itemId}/File", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get the original file of an item.
     * 
     * <p><b>200</b> - File stream returned.
     * <p><b>404</b> - Item not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId The item id.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<File> getFile(UUID itemId) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return getFileRequestCreation(itemId).bodyToMono(localVarReturnType);
    }

    /**
     * Get the original file of an item.
     * 
     * <p><b>200</b> - File stream returned.
     * <p><b>404</b> - Item not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId The item id.
     * @return ResponseEntity&lt;File&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<File>> getFileWithHttpInfo(UUID itemId) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return getFileRequestCreation(itemId).toEntity(localVarReturnType);
    }

    /**
     * Get the original file of an item.
     * 
     * <p><b>200</b> - File stream returned.
     * <p><b>404</b> - Item not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId The item id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getFileWithResponseSpec(UUID itemId) throws WebClientResponseException {
        return getFileRequestCreation(itemId);
    }

    /**
     * Get item counts.
     * 
     * <p><b>200</b> - Item counts returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId Optional. Get counts from a specific user&#39;s library.
     * @param isFavorite Optional. Get counts of favorite items.
     * @return ItemCounts
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getItemCountsRequestCreation(UUID userId, Boolean isFavorite) throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "userId", userId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isFavorite", isFavorite));
        
        final String[] localVarAccepts = { 
            "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<ItemCounts> localVarReturnType = new ParameterizedTypeReference<ItemCounts>() {};
        return apiClient.invokeAPI("/Items/Counts", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get item counts.
     * 
     * <p><b>200</b> - Item counts returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId Optional. Get counts from a specific user&#39;s library.
     * @param isFavorite Optional. Get counts of favorite items.
     * @return ItemCounts
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ItemCounts> getItemCounts(UUID userId, Boolean isFavorite) throws WebClientResponseException {
        ParameterizedTypeReference<ItemCounts> localVarReturnType = new ParameterizedTypeReference<ItemCounts>() {};
        return getItemCountsRequestCreation(userId, isFavorite).bodyToMono(localVarReturnType);
    }

    /**
     * Get item counts.
     * 
     * <p><b>200</b> - Item counts returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId Optional. Get counts from a specific user&#39;s library.
     * @param isFavorite Optional. Get counts of favorite items.
     * @return ResponseEntity&lt;ItemCounts&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<ItemCounts>> getItemCountsWithHttpInfo(UUID userId, Boolean isFavorite) throws WebClientResponseException {
        ParameterizedTypeReference<ItemCounts> localVarReturnType = new ParameterizedTypeReference<ItemCounts>() {};
        return getItemCountsRequestCreation(userId, isFavorite).toEntity(localVarReturnType);
    }

    /**
     * Get item counts.
     * 
     * <p><b>200</b> - Item counts returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId Optional. Get counts from a specific user&#39;s library.
     * @param isFavorite Optional. Get counts of favorite items.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getItemCountsWithResponseSpec(UUID userId, Boolean isFavorite) throws WebClientResponseException {
        return getItemCountsRequestCreation(userId, isFavorite);
    }

    /**
     * Gets the library options info.
     * 
     * <p><b>200</b> - Library options info returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param libraryContentType Library content type.
     * @param isNewLibrary Whether this is a new library.
     * @return LibraryOptionsResultDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getLibraryOptionsInfoRequestCreation(String libraryContentType, Boolean isNewLibrary) throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "libraryContentType", libraryContentType));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isNewLibrary", isNewLibrary));
        
        final String[] localVarAccepts = { 
            "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<LibraryOptionsResultDto> localVarReturnType = new ParameterizedTypeReference<LibraryOptionsResultDto>() {};
        return apiClient.invokeAPI("/Libraries/AvailableOptions", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets the library options info.
     * 
     * <p><b>200</b> - Library options info returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param libraryContentType Library content type.
     * @param isNewLibrary Whether this is a new library.
     * @return LibraryOptionsResultDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<LibraryOptionsResultDto> getLibraryOptionsInfo(String libraryContentType, Boolean isNewLibrary) throws WebClientResponseException {
        ParameterizedTypeReference<LibraryOptionsResultDto> localVarReturnType = new ParameterizedTypeReference<LibraryOptionsResultDto>() {};
        return getLibraryOptionsInfoRequestCreation(libraryContentType, isNewLibrary).bodyToMono(localVarReturnType);
    }

    /**
     * Gets the library options info.
     * 
     * <p><b>200</b> - Library options info returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param libraryContentType Library content type.
     * @param isNewLibrary Whether this is a new library.
     * @return ResponseEntity&lt;LibraryOptionsResultDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<LibraryOptionsResultDto>> getLibraryOptionsInfoWithHttpInfo(String libraryContentType, Boolean isNewLibrary) throws WebClientResponseException {
        ParameterizedTypeReference<LibraryOptionsResultDto> localVarReturnType = new ParameterizedTypeReference<LibraryOptionsResultDto>() {};
        return getLibraryOptionsInfoRequestCreation(libraryContentType, isNewLibrary).toEntity(localVarReturnType);
    }

    /**
     * Gets the library options info.
     * 
     * <p><b>200</b> - Library options info returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param libraryContentType Library content type.
     * @param isNewLibrary Whether this is a new library.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getLibraryOptionsInfoWithResponseSpec(String libraryContentType, Boolean isNewLibrary) throws WebClientResponseException {
        return getLibraryOptionsInfoRequestCreation(libraryContentType, isNewLibrary);
    }

    /**
     * Gets all user media folders.
     * 
     * <p><b>200</b> - Media folders returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param isHidden Optional. Filter by folders that are marked hidden, or not.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getMediaFoldersRequestCreation(Boolean isHidden) throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isHidden", isHidden));
        
        final String[] localVarAccepts = { 
            "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {};
        return apiClient.invokeAPI("/Library/MediaFolders", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets all user media folders.
     * 
     * <p><b>200</b> - Media folders returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param isHidden Optional. Filter by folders that are marked hidden, or not.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<BaseItemDtoQueryResult> getMediaFolders(Boolean isHidden) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {};
        return getMediaFoldersRequestCreation(isHidden).bodyToMono(localVarReturnType);
    }

    /**
     * Gets all user media folders.
     * 
     * <p><b>200</b> - Media folders returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param isHidden Optional. Filter by folders that are marked hidden, or not.
     * @return ResponseEntity&lt;BaseItemDtoQueryResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<BaseItemDtoQueryResult>> getMediaFoldersWithHttpInfo(Boolean isHidden) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {};
        return getMediaFoldersRequestCreation(isHidden).toEntity(localVarReturnType);
    }

    /**
     * Gets all user media folders.
     * 
     * <p><b>200</b> - Media folders returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param isHidden Optional. Filter by folders that are marked hidden, or not.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getMediaFoldersWithResponseSpec(Boolean isHidden) throws WebClientResponseException {
        return getMediaFoldersRequestCreation(isHidden);
    }

    /**
     * Gets a list of physical paths from virtual folders.
     * 
     * <p><b>200</b> - Physical paths returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @return List&lt;String&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getPhysicalPathsRequestCreation() throws WebClientResponseException {
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

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<List<String>> localVarReturnType = new ParameterizedTypeReference<List<String>>() {};
        return apiClient.invokeAPI("/Library/PhysicalPaths", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets a list of physical paths from virtual folders.
     * 
     * <p><b>200</b> - Physical paths returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @return List&lt;String&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<List<String>> getPhysicalPaths() throws WebClientResponseException {
        ParameterizedTypeReference<List<String>> localVarReturnType = new ParameterizedTypeReference<List<String>>() {};
        return getPhysicalPathsRequestCreation().bodyToMono(localVarReturnType);
    }

    /**
     * Gets a list of physical paths from virtual folders.
     * 
     * <p><b>200</b> - Physical paths returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @return ResponseEntity&lt;List&lt;String&gt;&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<List<String>>> getPhysicalPathsWithHttpInfo() throws WebClientResponseException {
        ParameterizedTypeReference<List<String>> localVarReturnType = new ParameterizedTypeReference<List<String>>() {};
        return getPhysicalPathsRequestCreation().toEntity(localVarReturnType);
    }

    /**
     * Gets a list of physical paths from virtual folders.
     * 
     * <p><b>200</b> - Physical paths returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getPhysicalPathsWithResponseSpec() throws WebClientResponseException {
        return getPhysicalPathsRequestCreation();
    }

    /**
     * Gets similar items.
     * 
     * <p><b>200</b> - Similar items returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId The item id.
     * @param excludeArtistIds Exclude artist ids.
     * @param userId Optional. Filter by user id, and attach user data.
     * @param limit Optional. The maximum number of records to return.
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple, comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions, MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue, SortName, Studios, Taglines, TrailerUrls.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getSimilarAlbumsRequestCreation(UUID itemId, List<UUID> excludeArtistIds, UUID userId, Integer limit, List<ItemFields> fields) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException("Missing the required parameter 'itemId' when calling getSimilarAlbums", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("itemId", itemId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "excludeArtistIds", excludeArtistIds));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "userId", userId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "limit", limit));
        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "fields", fields));
        
        final String[] localVarAccepts = { 
            "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {};
        return apiClient.invokeAPI("/Albums/{itemId}/Similar", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets similar items.
     * 
     * <p><b>200</b> - Similar items returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId The item id.
     * @param excludeArtistIds Exclude artist ids.
     * @param userId Optional. Filter by user id, and attach user data.
     * @param limit Optional. The maximum number of records to return.
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple, comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions, MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue, SortName, Studios, Taglines, TrailerUrls.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<BaseItemDtoQueryResult> getSimilarAlbums(UUID itemId, List<UUID> excludeArtistIds, UUID userId, Integer limit, List<ItemFields> fields) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {};
        return getSimilarAlbumsRequestCreation(itemId, excludeArtistIds, userId, limit, fields).bodyToMono(localVarReturnType);
    }

    /**
     * Gets similar items.
     * 
     * <p><b>200</b> - Similar items returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId The item id.
     * @param excludeArtistIds Exclude artist ids.
     * @param userId Optional. Filter by user id, and attach user data.
     * @param limit Optional. The maximum number of records to return.
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple, comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions, MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue, SortName, Studios, Taglines, TrailerUrls.
     * @return ResponseEntity&lt;BaseItemDtoQueryResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<BaseItemDtoQueryResult>> getSimilarAlbumsWithHttpInfo(UUID itemId, List<UUID> excludeArtistIds, UUID userId, Integer limit, List<ItemFields> fields) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {};
        return getSimilarAlbumsRequestCreation(itemId, excludeArtistIds, userId, limit, fields).toEntity(localVarReturnType);
    }

    /**
     * Gets similar items.
     * 
     * <p><b>200</b> - Similar items returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId The item id.
     * @param excludeArtistIds Exclude artist ids.
     * @param userId Optional. Filter by user id, and attach user data.
     * @param limit Optional. The maximum number of records to return.
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple, comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions, MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue, SortName, Studios, Taglines, TrailerUrls.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getSimilarAlbumsWithResponseSpec(UUID itemId, List<UUID> excludeArtistIds, UUID userId, Integer limit, List<ItemFields> fields) throws WebClientResponseException {
        return getSimilarAlbumsRequestCreation(itemId, excludeArtistIds, userId, limit, fields);
    }

    /**
     * Gets similar items.
     * 
     * <p><b>200</b> - Similar items returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId The item id.
     * @param excludeArtistIds Exclude artist ids.
     * @param userId Optional. Filter by user id, and attach user data.
     * @param limit Optional. The maximum number of records to return.
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple, comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions, MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue, SortName, Studios, Taglines, TrailerUrls.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getSimilarArtistsRequestCreation(UUID itemId, List<UUID> excludeArtistIds, UUID userId, Integer limit, List<ItemFields> fields) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException("Missing the required parameter 'itemId' when calling getSimilarArtists", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("itemId", itemId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "excludeArtistIds", excludeArtistIds));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "userId", userId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "limit", limit));
        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "fields", fields));
        
        final String[] localVarAccepts = { 
            "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {};
        return apiClient.invokeAPI("/Artists/{itemId}/Similar", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets similar items.
     * 
     * <p><b>200</b> - Similar items returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId The item id.
     * @param excludeArtistIds Exclude artist ids.
     * @param userId Optional. Filter by user id, and attach user data.
     * @param limit Optional. The maximum number of records to return.
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple, comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions, MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue, SortName, Studios, Taglines, TrailerUrls.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<BaseItemDtoQueryResult> getSimilarArtists(UUID itemId, List<UUID> excludeArtistIds, UUID userId, Integer limit, List<ItemFields> fields) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {};
        return getSimilarArtistsRequestCreation(itemId, excludeArtistIds, userId, limit, fields).bodyToMono(localVarReturnType);
    }

    /**
     * Gets similar items.
     * 
     * <p><b>200</b> - Similar items returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId The item id.
     * @param excludeArtistIds Exclude artist ids.
     * @param userId Optional. Filter by user id, and attach user data.
     * @param limit Optional. The maximum number of records to return.
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple, comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions, MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue, SortName, Studios, Taglines, TrailerUrls.
     * @return ResponseEntity&lt;BaseItemDtoQueryResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<BaseItemDtoQueryResult>> getSimilarArtistsWithHttpInfo(UUID itemId, List<UUID> excludeArtistIds, UUID userId, Integer limit, List<ItemFields> fields) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {};
        return getSimilarArtistsRequestCreation(itemId, excludeArtistIds, userId, limit, fields).toEntity(localVarReturnType);
    }

    /**
     * Gets similar items.
     * 
     * <p><b>200</b> - Similar items returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId The item id.
     * @param excludeArtistIds Exclude artist ids.
     * @param userId Optional. Filter by user id, and attach user data.
     * @param limit Optional. The maximum number of records to return.
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple, comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions, MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue, SortName, Studios, Taglines, TrailerUrls.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getSimilarArtistsWithResponseSpec(UUID itemId, List<UUID> excludeArtistIds, UUID userId, Integer limit, List<ItemFields> fields) throws WebClientResponseException {
        return getSimilarArtistsRequestCreation(itemId, excludeArtistIds, userId, limit, fields);
    }

    /**
     * Gets similar items.
     * 
     * <p><b>200</b> - Similar items returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId The item id.
     * @param excludeArtistIds Exclude artist ids.
     * @param userId Optional. Filter by user id, and attach user data.
     * @param limit Optional. The maximum number of records to return.
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple, comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions, MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue, SortName, Studios, Taglines, TrailerUrls.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getSimilarItemsRequestCreation(UUID itemId, List<UUID> excludeArtistIds, UUID userId, Integer limit, List<ItemFields> fields) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException("Missing the required parameter 'itemId' when calling getSimilarItems", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("itemId", itemId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "excludeArtistIds", excludeArtistIds));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "userId", userId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "limit", limit));
        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "fields", fields));
        
        final String[] localVarAccepts = { 
            "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {};
        return apiClient.invokeAPI("/Items/{itemId}/Similar", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets similar items.
     * 
     * <p><b>200</b> - Similar items returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId The item id.
     * @param excludeArtistIds Exclude artist ids.
     * @param userId Optional. Filter by user id, and attach user data.
     * @param limit Optional. The maximum number of records to return.
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple, comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions, MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue, SortName, Studios, Taglines, TrailerUrls.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<BaseItemDtoQueryResult> getSimilarItems(UUID itemId, List<UUID> excludeArtistIds, UUID userId, Integer limit, List<ItemFields> fields) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {};
        return getSimilarItemsRequestCreation(itemId, excludeArtistIds, userId, limit, fields).bodyToMono(localVarReturnType);
    }

    /**
     * Gets similar items.
     * 
     * <p><b>200</b> - Similar items returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId The item id.
     * @param excludeArtistIds Exclude artist ids.
     * @param userId Optional. Filter by user id, and attach user data.
     * @param limit Optional. The maximum number of records to return.
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple, comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions, MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue, SortName, Studios, Taglines, TrailerUrls.
     * @return ResponseEntity&lt;BaseItemDtoQueryResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<BaseItemDtoQueryResult>> getSimilarItemsWithHttpInfo(UUID itemId, List<UUID> excludeArtistIds, UUID userId, Integer limit, List<ItemFields> fields) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {};
        return getSimilarItemsRequestCreation(itemId, excludeArtistIds, userId, limit, fields).toEntity(localVarReturnType);
    }

    /**
     * Gets similar items.
     * 
     * <p><b>200</b> - Similar items returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId The item id.
     * @param excludeArtistIds Exclude artist ids.
     * @param userId Optional. Filter by user id, and attach user data.
     * @param limit Optional. The maximum number of records to return.
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple, comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions, MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue, SortName, Studios, Taglines, TrailerUrls.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getSimilarItemsWithResponseSpec(UUID itemId, List<UUID> excludeArtistIds, UUID userId, Integer limit, List<ItemFields> fields) throws WebClientResponseException {
        return getSimilarItemsRequestCreation(itemId, excludeArtistIds, userId, limit, fields);
    }

    /**
     * Gets similar items.
     * 
     * <p><b>200</b> - Similar items returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId The item id.
     * @param excludeArtistIds Exclude artist ids.
     * @param userId Optional. Filter by user id, and attach user data.
     * @param limit Optional. The maximum number of records to return.
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple, comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions, MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue, SortName, Studios, Taglines, TrailerUrls.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getSimilarMoviesRequestCreation(UUID itemId, List<UUID> excludeArtistIds, UUID userId, Integer limit, List<ItemFields> fields) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException("Missing the required parameter 'itemId' when calling getSimilarMovies", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("itemId", itemId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "excludeArtistIds", excludeArtistIds));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "userId", userId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "limit", limit));
        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "fields", fields));
        
        final String[] localVarAccepts = { 
            "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {};
        return apiClient.invokeAPI("/Movies/{itemId}/Similar", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets similar items.
     * 
     * <p><b>200</b> - Similar items returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId The item id.
     * @param excludeArtistIds Exclude artist ids.
     * @param userId Optional. Filter by user id, and attach user data.
     * @param limit Optional. The maximum number of records to return.
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple, comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions, MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue, SortName, Studios, Taglines, TrailerUrls.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<BaseItemDtoQueryResult> getSimilarMovies(UUID itemId, List<UUID> excludeArtistIds, UUID userId, Integer limit, List<ItemFields> fields) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {};
        return getSimilarMoviesRequestCreation(itemId, excludeArtistIds, userId, limit, fields).bodyToMono(localVarReturnType);
    }

    /**
     * Gets similar items.
     * 
     * <p><b>200</b> - Similar items returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId The item id.
     * @param excludeArtistIds Exclude artist ids.
     * @param userId Optional. Filter by user id, and attach user data.
     * @param limit Optional. The maximum number of records to return.
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple, comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions, MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue, SortName, Studios, Taglines, TrailerUrls.
     * @return ResponseEntity&lt;BaseItemDtoQueryResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<BaseItemDtoQueryResult>> getSimilarMoviesWithHttpInfo(UUID itemId, List<UUID> excludeArtistIds, UUID userId, Integer limit, List<ItemFields> fields) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {};
        return getSimilarMoviesRequestCreation(itemId, excludeArtistIds, userId, limit, fields).toEntity(localVarReturnType);
    }

    /**
     * Gets similar items.
     * 
     * <p><b>200</b> - Similar items returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId The item id.
     * @param excludeArtistIds Exclude artist ids.
     * @param userId Optional. Filter by user id, and attach user data.
     * @param limit Optional. The maximum number of records to return.
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple, comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions, MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue, SortName, Studios, Taglines, TrailerUrls.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getSimilarMoviesWithResponseSpec(UUID itemId, List<UUID> excludeArtistIds, UUID userId, Integer limit, List<ItemFields> fields) throws WebClientResponseException {
        return getSimilarMoviesRequestCreation(itemId, excludeArtistIds, userId, limit, fields);
    }

    /**
     * Gets similar items.
     * 
     * <p><b>200</b> - Similar items returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId The item id.
     * @param excludeArtistIds Exclude artist ids.
     * @param userId Optional. Filter by user id, and attach user data.
     * @param limit Optional. The maximum number of records to return.
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple, comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions, MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue, SortName, Studios, Taglines, TrailerUrls.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getSimilarShowsRequestCreation(UUID itemId, List<UUID> excludeArtistIds, UUID userId, Integer limit, List<ItemFields> fields) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException("Missing the required parameter 'itemId' when calling getSimilarShows", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("itemId", itemId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "excludeArtistIds", excludeArtistIds));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "userId", userId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "limit", limit));
        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "fields", fields));
        
        final String[] localVarAccepts = { 
            "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {};
        return apiClient.invokeAPI("/Shows/{itemId}/Similar", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets similar items.
     * 
     * <p><b>200</b> - Similar items returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId The item id.
     * @param excludeArtistIds Exclude artist ids.
     * @param userId Optional. Filter by user id, and attach user data.
     * @param limit Optional. The maximum number of records to return.
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple, comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions, MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue, SortName, Studios, Taglines, TrailerUrls.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<BaseItemDtoQueryResult> getSimilarShows(UUID itemId, List<UUID> excludeArtistIds, UUID userId, Integer limit, List<ItemFields> fields) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {};
        return getSimilarShowsRequestCreation(itemId, excludeArtistIds, userId, limit, fields).bodyToMono(localVarReturnType);
    }

    /**
     * Gets similar items.
     * 
     * <p><b>200</b> - Similar items returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId The item id.
     * @param excludeArtistIds Exclude artist ids.
     * @param userId Optional. Filter by user id, and attach user data.
     * @param limit Optional. The maximum number of records to return.
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple, comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions, MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue, SortName, Studios, Taglines, TrailerUrls.
     * @return ResponseEntity&lt;BaseItemDtoQueryResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<BaseItemDtoQueryResult>> getSimilarShowsWithHttpInfo(UUID itemId, List<UUID> excludeArtistIds, UUID userId, Integer limit, List<ItemFields> fields) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {};
        return getSimilarShowsRequestCreation(itemId, excludeArtistIds, userId, limit, fields).toEntity(localVarReturnType);
    }

    /**
     * Gets similar items.
     * 
     * <p><b>200</b> - Similar items returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId The item id.
     * @param excludeArtistIds Exclude artist ids.
     * @param userId Optional. Filter by user id, and attach user data.
     * @param limit Optional. The maximum number of records to return.
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple, comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions, MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue, SortName, Studios, Taglines, TrailerUrls.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getSimilarShowsWithResponseSpec(UUID itemId, List<UUID> excludeArtistIds, UUID userId, Integer limit, List<ItemFields> fields) throws WebClientResponseException {
        return getSimilarShowsRequestCreation(itemId, excludeArtistIds, userId, limit, fields);
    }

    /**
     * Gets similar items.
     * 
     * <p><b>200</b> - Similar items returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId The item id.
     * @param excludeArtistIds Exclude artist ids.
     * @param userId Optional. Filter by user id, and attach user data.
     * @param limit Optional. The maximum number of records to return.
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple, comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions, MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue, SortName, Studios, Taglines, TrailerUrls.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getSimilarTrailersRequestCreation(UUID itemId, List<UUID> excludeArtistIds, UUID userId, Integer limit, List<ItemFields> fields) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException("Missing the required parameter 'itemId' when calling getSimilarTrailers", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("itemId", itemId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "excludeArtistIds", excludeArtistIds));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "userId", userId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "limit", limit));
        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "fields", fields));
        
        final String[] localVarAccepts = { 
            "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {};
        return apiClient.invokeAPI("/Trailers/{itemId}/Similar", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets similar items.
     * 
     * <p><b>200</b> - Similar items returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId The item id.
     * @param excludeArtistIds Exclude artist ids.
     * @param userId Optional. Filter by user id, and attach user data.
     * @param limit Optional. The maximum number of records to return.
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple, comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions, MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue, SortName, Studios, Taglines, TrailerUrls.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<BaseItemDtoQueryResult> getSimilarTrailers(UUID itemId, List<UUID> excludeArtistIds, UUID userId, Integer limit, List<ItemFields> fields) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {};
        return getSimilarTrailersRequestCreation(itemId, excludeArtistIds, userId, limit, fields).bodyToMono(localVarReturnType);
    }

    /**
     * Gets similar items.
     * 
     * <p><b>200</b> - Similar items returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId The item id.
     * @param excludeArtistIds Exclude artist ids.
     * @param userId Optional. Filter by user id, and attach user data.
     * @param limit Optional. The maximum number of records to return.
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple, comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions, MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue, SortName, Studios, Taglines, TrailerUrls.
     * @return ResponseEntity&lt;BaseItemDtoQueryResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<BaseItemDtoQueryResult>> getSimilarTrailersWithHttpInfo(UUID itemId, List<UUID> excludeArtistIds, UUID userId, Integer limit, List<ItemFields> fields) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {};
        return getSimilarTrailersRequestCreation(itemId, excludeArtistIds, userId, limit, fields).toEntity(localVarReturnType);
    }

    /**
     * Gets similar items.
     * 
     * <p><b>200</b> - Similar items returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId The item id.
     * @param excludeArtistIds Exclude artist ids.
     * @param userId Optional. Filter by user id, and attach user data.
     * @param limit Optional. The maximum number of records to return.
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple, comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions, MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue, SortName, Studios, Taglines, TrailerUrls.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getSimilarTrailersWithResponseSpec(UUID itemId, List<UUID> excludeArtistIds, UUID userId, Integer limit, List<ItemFields> fields) throws WebClientResponseException {
        return getSimilarTrailersRequestCreation(itemId, excludeArtistIds, userId, limit, fields);
    }

    /**
     * Get theme songs and videos for an item.
     * 
     * <p><b>200</b> - Theme songs and videos returned.
     * <p><b>404</b> - Item not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId The item id.
     * @param userId Optional. Filter by user id, and attach user data.
     * @param inheritFromParent Optional. Determines whether or not parent items should be searched for theme media.
     * @return AllThemeMediaResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getThemeMediaRequestCreation(UUID itemId, UUID userId, Boolean inheritFromParent) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException("Missing the required parameter 'itemId' when calling getThemeMedia", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("itemId", itemId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "userId", userId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "inheritFromParent", inheritFromParent));
        
        final String[] localVarAccepts = { 
            "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<AllThemeMediaResult> localVarReturnType = new ParameterizedTypeReference<AllThemeMediaResult>() {};
        return apiClient.invokeAPI("/Items/{itemId}/ThemeMedia", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get theme songs and videos for an item.
     * 
     * <p><b>200</b> - Theme songs and videos returned.
     * <p><b>404</b> - Item not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId The item id.
     * @param userId Optional. Filter by user id, and attach user data.
     * @param inheritFromParent Optional. Determines whether or not parent items should be searched for theme media.
     * @return AllThemeMediaResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<AllThemeMediaResult> getThemeMedia(UUID itemId, UUID userId, Boolean inheritFromParent) throws WebClientResponseException {
        ParameterizedTypeReference<AllThemeMediaResult> localVarReturnType = new ParameterizedTypeReference<AllThemeMediaResult>() {};
        return getThemeMediaRequestCreation(itemId, userId, inheritFromParent).bodyToMono(localVarReturnType);
    }

    /**
     * Get theme songs and videos for an item.
     * 
     * <p><b>200</b> - Theme songs and videos returned.
     * <p><b>404</b> - Item not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId The item id.
     * @param userId Optional. Filter by user id, and attach user data.
     * @param inheritFromParent Optional. Determines whether or not parent items should be searched for theme media.
     * @return ResponseEntity&lt;AllThemeMediaResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<AllThemeMediaResult>> getThemeMediaWithHttpInfo(UUID itemId, UUID userId, Boolean inheritFromParent) throws WebClientResponseException {
        ParameterizedTypeReference<AllThemeMediaResult> localVarReturnType = new ParameterizedTypeReference<AllThemeMediaResult>() {};
        return getThemeMediaRequestCreation(itemId, userId, inheritFromParent).toEntity(localVarReturnType);
    }

    /**
     * Get theme songs and videos for an item.
     * 
     * <p><b>200</b> - Theme songs and videos returned.
     * <p><b>404</b> - Item not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId The item id.
     * @param userId Optional. Filter by user id, and attach user data.
     * @param inheritFromParent Optional. Determines whether or not parent items should be searched for theme media.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getThemeMediaWithResponseSpec(UUID itemId, UUID userId, Boolean inheritFromParent) throws WebClientResponseException {
        return getThemeMediaRequestCreation(itemId, userId, inheritFromParent);
    }

    /**
     * Get theme songs for an item.
     * 
     * <p><b>200</b> - Theme songs returned.
     * <p><b>404</b> - Item not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId The item id.
     * @param userId Optional. Filter by user id, and attach user data.
     * @param inheritFromParent Optional. Determines whether or not parent items should be searched for theme media.
     * @return ThemeMediaResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getThemeSongsRequestCreation(UUID itemId, UUID userId, Boolean inheritFromParent) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException("Missing the required parameter 'itemId' when calling getThemeSongs", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("itemId", itemId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "userId", userId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "inheritFromParent", inheritFromParent));
        
        final String[] localVarAccepts = { 
            "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<ThemeMediaResult> localVarReturnType = new ParameterizedTypeReference<ThemeMediaResult>() {};
        return apiClient.invokeAPI("/Items/{itemId}/ThemeSongs", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get theme songs for an item.
     * 
     * <p><b>200</b> - Theme songs returned.
     * <p><b>404</b> - Item not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId The item id.
     * @param userId Optional. Filter by user id, and attach user data.
     * @param inheritFromParent Optional. Determines whether or not parent items should be searched for theme media.
     * @return ThemeMediaResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ThemeMediaResult> getThemeSongs(UUID itemId, UUID userId, Boolean inheritFromParent) throws WebClientResponseException {
        ParameterizedTypeReference<ThemeMediaResult> localVarReturnType = new ParameterizedTypeReference<ThemeMediaResult>() {};
        return getThemeSongsRequestCreation(itemId, userId, inheritFromParent).bodyToMono(localVarReturnType);
    }

    /**
     * Get theme songs for an item.
     * 
     * <p><b>200</b> - Theme songs returned.
     * <p><b>404</b> - Item not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId The item id.
     * @param userId Optional. Filter by user id, and attach user data.
     * @param inheritFromParent Optional. Determines whether or not parent items should be searched for theme media.
     * @return ResponseEntity&lt;ThemeMediaResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<ThemeMediaResult>> getThemeSongsWithHttpInfo(UUID itemId, UUID userId, Boolean inheritFromParent) throws WebClientResponseException {
        ParameterizedTypeReference<ThemeMediaResult> localVarReturnType = new ParameterizedTypeReference<ThemeMediaResult>() {};
        return getThemeSongsRequestCreation(itemId, userId, inheritFromParent).toEntity(localVarReturnType);
    }

    /**
     * Get theme songs for an item.
     * 
     * <p><b>200</b> - Theme songs returned.
     * <p><b>404</b> - Item not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId The item id.
     * @param userId Optional. Filter by user id, and attach user data.
     * @param inheritFromParent Optional. Determines whether or not parent items should be searched for theme media.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getThemeSongsWithResponseSpec(UUID itemId, UUID userId, Boolean inheritFromParent) throws WebClientResponseException {
        return getThemeSongsRequestCreation(itemId, userId, inheritFromParent);
    }

    /**
     * Get theme videos for an item.
     * 
     * <p><b>200</b> - Theme videos returned.
     * <p><b>404</b> - Item not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId The item id.
     * @param userId Optional. Filter by user id, and attach user data.
     * @param inheritFromParent Optional. Determines whether or not parent items should be searched for theme media.
     * @return ThemeMediaResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getThemeVideosRequestCreation(UUID itemId, UUID userId, Boolean inheritFromParent) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException("Missing the required parameter 'itemId' when calling getThemeVideos", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("itemId", itemId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "userId", userId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "inheritFromParent", inheritFromParent));
        
        final String[] localVarAccepts = { 
            "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<ThemeMediaResult> localVarReturnType = new ParameterizedTypeReference<ThemeMediaResult>() {};
        return apiClient.invokeAPI("/Items/{itemId}/ThemeVideos", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get theme videos for an item.
     * 
     * <p><b>200</b> - Theme videos returned.
     * <p><b>404</b> - Item not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId The item id.
     * @param userId Optional. Filter by user id, and attach user data.
     * @param inheritFromParent Optional. Determines whether or not parent items should be searched for theme media.
     * @return ThemeMediaResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ThemeMediaResult> getThemeVideos(UUID itemId, UUID userId, Boolean inheritFromParent) throws WebClientResponseException {
        ParameterizedTypeReference<ThemeMediaResult> localVarReturnType = new ParameterizedTypeReference<ThemeMediaResult>() {};
        return getThemeVideosRequestCreation(itemId, userId, inheritFromParent).bodyToMono(localVarReturnType);
    }

    /**
     * Get theme videos for an item.
     * 
     * <p><b>200</b> - Theme videos returned.
     * <p><b>404</b> - Item not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId The item id.
     * @param userId Optional. Filter by user id, and attach user data.
     * @param inheritFromParent Optional. Determines whether or not parent items should be searched for theme media.
     * @return ResponseEntity&lt;ThemeMediaResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<ThemeMediaResult>> getThemeVideosWithHttpInfo(UUID itemId, UUID userId, Boolean inheritFromParent) throws WebClientResponseException {
        ParameterizedTypeReference<ThemeMediaResult> localVarReturnType = new ParameterizedTypeReference<ThemeMediaResult>() {};
        return getThemeVideosRequestCreation(itemId, userId, inheritFromParent).toEntity(localVarReturnType);
    }

    /**
     * Get theme videos for an item.
     * 
     * <p><b>200</b> - Theme videos returned.
     * <p><b>404</b> - Item not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId The item id.
     * @param userId Optional. Filter by user id, and attach user data.
     * @param inheritFromParent Optional. Determines whether or not parent items should be searched for theme media.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getThemeVideosWithResponseSpec(UUID itemId, UUID userId, Boolean inheritFromParent) throws WebClientResponseException {
        return getThemeVideosRequestCreation(itemId, userId, inheritFromParent);
    }

    /**
     * Reports that new movies have been added by an external source.
     * 
     * <p><b>204</b> - Report success.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param tmdbId The tmdbId.
     * @param imdbId The imdbId.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec postAddedMoviesRequestCreation(String tmdbId, String imdbId) throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "tmdbId", tmdbId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "imdbId", imdbId));
        
        final String[] localVarAccepts = { };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return apiClient.invokeAPI("/Library/Movies/Added", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Reports that new movies have been added by an external source.
     * 
     * <p><b>204</b> - Report success.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param tmdbId The tmdbId.
     * @param imdbId The imdbId.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> postAddedMovies(String tmdbId, String imdbId) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return postAddedMoviesRequestCreation(tmdbId, imdbId).bodyToMono(localVarReturnType);
    }

    /**
     * Reports that new movies have been added by an external source.
     * 
     * <p><b>204</b> - Report success.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param tmdbId The tmdbId.
     * @param imdbId The imdbId.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> postAddedMoviesWithHttpInfo(String tmdbId, String imdbId) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return postAddedMoviesRequestCreation(tmdbId, imdbId).toEntity(localVarReturnType);
    }

    /**
     * Reports that new movies have been added by an external source.
     * 
     * <p><b>204</b> - Report success.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param tmdbId The tmdbId.
     * @param imdbId The imdbId.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec postAddedMoviesWithResponseSpec(String tmdbId, String imdbId) throws WebClientResponseException {
        return postAddedMoviesRequestCreation(tmdbId, imdbId);
    }

    /**
     * Reports that new episodes of a series have been added by an external source.
     * 
     * <p><b>204</b> - Report success.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param tvdbId The tvdbId.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec postAddedSeriesRequestCreation(String tvdbId) throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "tvdbId", tvdbId));
        
        final String[] localVarAccepts = { };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return apiClient.invokeAPI("/Library/Series/Added", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Reports that new episodes of a series have been added by an external source.
     * 
     * <p><b>204</b> - Report success.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param tvdbId The tvdbId.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> postAddedSeries(String tvdbId) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return postAddedSeriesRequestCreation(tvdbId).bodyToMono(localVarReturnType);
    }

    /**
     * Reports that new episodes of a series have been added by an external source.
     * 
     * <p><b>204</b> - Report success.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param tvdbId The tvdbId.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> postAddedSeriesWithHttpInfo(String tvdbId) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return postAddedSeriesRequestCreation(tvdbId).toEntity(localVarReturnType);
    }

    /**
     * Reports that new episodes of a series have been added by an external source.
     * 
     * <p><b>204</b> - Report success.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param tvdbId The tvdbId.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec postAddedSeriesWithResponseSpec(String tvdbId) throws WebClientResponseException {
        return postAddedSeriesRequestCreation(tvdbId);
    }

    /**
     * Reports that new movies have been added by an external source.
     * 
     * <p><b>204</b> - Report success.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param mediaUpdateInfoDto The update paths.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec postUpdatedMediaRequestCreation(MediaUpdateInfoDto mediaUpdateInfoDto) throws WebClientResponseException {
        Object postBody = mediaUpdateInfoDto;
        // verify the required parameter 'mediaUpdateInfoDto' is set
        if (mediaUpdateInfoDto == null) {
            throw new WebClientResponseException("Missing the required parameter 'mediaUpdateInfoDto' when calling postUpdatedMedia", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { 
            "application/json", "text/json", "application/*+json"
        };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return apiClient.invokeAPI("/Library/Media/Updated", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Reports that new movies have been added by an external source.
     * 
     * <p><b>204</b> - Report success.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param mediaUpdateInfoDto The update paths.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> postUpdatedMedia(MediaUpdateInfoDto mediaUpdateInfoDto) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return postUpdatedMediaRequestCreation(mediaUpdateInfoDto).bodyToMono(localVarReturnType);
    }

    /**
     * Reports that new movies have been added by an external source.
     * 
     * <p><b>204</b> - Report success.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param mediaUpdateInfoDto The update paths.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> postUpdatedMediaWithHttpInfo(MediaUpdateInfoDto mediaUpdateInfoDto) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return postUpdatedMediaRequestCreation(mediaUpdateInfoDto).toEntity(localVarReturnType);
    }

    /**
     * Reports that new movies have been added by an external source.
     * 
     * <p><b>204</b> - Report success.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param mediaUpdateInfoDto The update paths.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec postUpdatedMediaWithResponseSpec(MediaUpdateInfoDto mediaUpdateInfoDto) throws WebClientResponseException {
        return postUpdatedMediaRequestCreation(mediaUpdateInfoDto);
    }

    /**
     * Reports that new movies have been added by an external source.
     * 
     * <p><b>204</b> - Report success.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param tmdbId The tmdbId.
     * @param imdbId The imdbId.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec postUpdatedMoviesRequestCreation(String tmdbId, String imdbId) throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "tmdbId", tmdbId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "imdbId", imdbId));
        
        final String[] localVarAccepts = { };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return apiClient.invokeAPI("/Library/Movies/Updated", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Reports that new movies have been added by an external source.
     * 
     * <p><b>204</b> - Report success.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param tmdbId The tmdbId.
     * @param imdbId The imdbId.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> postUpdatedMovies(String tmdbId, String imdbId) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return postUpdatedMoviesRequestCreation(tmdbId, imdbId).bodyToMono(localVarReturnType);
    }

    /**
     * Reports that new movies have been added by an external source.
     * 
     * <p><b>204</b> - Report success.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param tmdbId The tmdbId.
     * @param imdbId The imdbId.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> postUpdatedMoviesWithHttpInfo(String tmdbId, String imdbId) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return postUpdatedMoviesRequestCreation(tmdbId, imdbId).toEntity(localVarReturnType);
    }

    /**
     * Reports that new movies have been added by an external source.
     * 
     * <p><b>204</b> - Report success.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param tmdbId The tmdbId.
     * @param imdbId The imdbId.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec postUpdatedMoviesWithResponseSpec(String tmdbId, String imdbId) throws WebClientResponseException {
        return postUpdatedMoviesRequestCreation(tmdbId, imdbId);
    }

    /**
     * Reports that new episodes of a series have been added by an external source.
     * 
     * <p><b>204</b> - Report success.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param tvdbId The tvdbId.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec postUpdatedSeriesRequestCreation(String tvdbId) throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "tvdbId", tvdbId));
        
        final String[] localVarAccepts = { };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return apiClient.invokeAPI("/Library/Series/Updated", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Reports that new episodes of a series have been added by an external source.
     * 
     * <p><b>204</b> - Report success.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param tvdbId The tvdbId.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> postUpdatedSeries(String tvdbId) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return postUpdatedSeriesRequestCreation(tvdbId).bodyToMono(localVarReturnType);
    }

    /**
     * Reports that new episodes of a series have been added by an external source.
     * 
     * <p><b>204</b> - Report success.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param tvdbId The tvdbId.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> postUpdatedSeriesWithHttpInfo(String tvdbId) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return postUpdatedSeriesRequestCreation(tvdbId).toEntity(localVarReturnType);
    }

    /**
     * Reports that new episodes of a series have been added by an external source.
     * 
     * <p><b>204</b> - Report success.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param tvdbId The tvdbId.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec postUpdatedSeriesWithResponseSpec(String tvdbId) throws WebClientResponseException {
        return postUpdatedSeriesRequestCreation(tvdbId);
    }

    /**
     * Starts a library scan.
     * 
     * <p><b>204</b> - Library scan started.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec refreshLibraryRequestCreation() throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return apiClient.invokeAPI("/Library/Refresh", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Starts a library scan.
     * 
     * <p><b>204</b> - Library scan started.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> refreshLibrary() throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return refreshLibraryRequestCreation().bodyToMono(localVarReturnType);
    }

    /**
     * Starts a library scan.
     * 
     * <p><b>204</b> - Library scan started.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> refreshLibraryWithHttpInfo() throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return refreshLibraryRequestCreation().toEntity(localVarReturnType);
    }

    /**
     * Starts a library scan.
     * 
     * <p><b>204</b> - Library scan started.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec refreshLibraryWithResponseSpec() throws WebClientResponseException {
        return refreshLibraryRequestCreation();
    }
}
