package org.openhab.binding.jellyfin.internal.api.version.legacy;

import org.openhab.binding.jellyfin.internal.api.version.ApiClient;

import org.openhab.binding.jellyfin.internal.api.version.legacy.model.BaseItemDto;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.BaseItemDtoQueryResult;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.BaseItemKind;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.ImageType;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.ItemFields;
import java.util.UUID;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.UserItemDataDto;

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
public class UserLibraryApi {
    private ApiClient apiClient;

    public UserLibraryApi() {
        this(new ApiClient());
    }

    @Autowired
    public UserLibraryApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    
    /**
     * Deletes a user&#39;s saved personal rating for an item.
     * 
     * <p><b>200</b> - Personal rating removed.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId User id.
     * @param itemId Item id.
     * @return UserItemDataDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec deleteUserItemRatingRequestCreation(UUID userId, UUID itemId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'userId' is set
        if (userId == null) {
            throw new WebClientResponseException("Missing the required parameter 'userId' when calling deleteUserItemRating", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException("Missing the required parameter 'itemId' when calling deleteUserItemRating", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("userId", userId);
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

        ParameterizedTypeReference<UserItemDataDto> localVarReturnType = new ParameterizedTypeReference<UserItemDataDto>() {};
        return apiClient.invokeAPI("/Users/{userId}/Items/{itemId}/Rating", HttpMethod.DELETE, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Deletes a user&#39;s saved personal rating for an item.
     * 
     * <p><b>200</b> - Personal rating removed.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId User id.
     * @param itemId Item id.
     * @return UserItemDataDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<UserItemDataDto> deleteUserItemRating(UUID userId, UUID itemId) throws WebClientResponseException {
        ParameterizedTypeReference<UserItemDataDto> localVarReturnType = new ParameterizedTypeReference<UserItemDataDto>() {};
        return deleteUserItemRatingRequestCreation(userId, itemId).bodyToMono(localVarReturnType);
    }

    /**
     * Deletes a user&#39;s saved personal rating for an item.
     * 
     * <p><b>200</b> - Personal rating removed.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId User id.
     * @param itemId Item id.
     * @return ResponseEntity&lt;UserItemDataDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<UserItemDataDto>> deleteUserItemRatingWithHttpInfo(UUID userId, UUID itemId) throws WebClientResponseException {
        ParameterizedTypeReference<UserItemDataDto> localVarReturnType = new ParameterizedTypeReference<UserItemDataDto>() {};
        return deleteUserItemRatingRequestCreation(userId, itemId).toEntity(localVarReturnType);
    }

    /**
     * Deletes a user&#39;s saved personal rating for an item.
     * 
     * <p><b>200</b> - Personal rating removed.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId User id.
     * @param itemId Item id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec deleteUserItemRatingWithResponseSpec(UUID userId, UUID itemId) throws WebClientResponseException {
        return deleteUserItemRatingRequestCreation(userId, itemId);
    }

    /**
     * Gets intros to play before the main media item plays.
     * 
     * <p><b>200</b> - Intros returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId User id.
     * @param itemId Item id.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getIntrosRequestCreation(UUID userId, UUID itemId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'userId' is set
        if (userId == null) {
            throw new WebClientResponseException("Missing the required parameter 'userId' when calling getIntros", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException("Missing the required parameter 'itemId' when calling getIntros", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("userId", userId);
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
        return apiClient.invokeAPI("/Users/{userId}/Items/{itemId}/Intros", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets intros to play before the main media item plays.
     * 
     * <p><b>200</b> - Intros returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId User id.
     * @param itemId Item id.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<BaseItemDtoQueryResult> getIntros(UUID userId, UUID itemId) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {};
        return getIntrosRequestCreation(userId, itemId).bodyToMono(localVarReturnType);
    }

    /**
     * Gets intros to play before the main media item plays.
     * 
     * <p><b>200</b> - Intros returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId User id.
     * @param itemId Item id.
     * @return ResponseEntity&lt;BaseItemDtoQueryResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<BaseItemDtoQueryResult>> getIntrosWithHttpInfo(UUID userId, UUID itemId) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {};
        return getIntrosRequestCreation(userId, itemId).toEntity(localVarReturnType);
    }

    /**
     * Gets intros to play before the main media item plays.
     * 
     * <p><b>200</b> - Intros returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId User id.
     * @param itemId Item id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getIntrosWithResponseSpec(UUID userId, UUID itemId) throws WebClientResponseException {
        return getIntrosRequestCreation(userId, itemId);
    }

    /**
     * Gets an item from a user&#39;s library.
     * 
     * <p><b>200</b> - Item returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId User id.
     * @param itemId Item id.
     * @return BaseItemDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getItemRequestCreation(UUID userId, UUID itemId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'userId' is set
        if (userId == null) {
            throw new WebClientResponseException("Missing the required parameter 'userId' when calling getItem", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException("Missing the required parameter 'itemId' when calling getItem", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("userId", userId);
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

        ParameterizedTypeReference<BaseItemDto> localVarReturnType = new ParameterizedTypeReference<BaseItemDto>() {};
        return apiClient.invokeAPI("/Users/{userId}/Items/{itemId}", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets an item from a user&#39;s library.
     * 
     * <p><b>200</b> - Item returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId User id.
     * @param itemId Item id.
     * @return BaseItemDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<BaseItemDto> getItem(UUID userId, UUID itemId) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDto> localVarReturnType = new ParameterizedTypeReference<BaseItemDto>() {};
        return getItemRequestCreation(userId, itemId).bodyToMono(localVarReturnType);
    }

    /**
     * Gets an item from a user&#39;s library.
     * 
     * <p><b>200</b> - Item returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId User id.
     * @param itemId Item id.
     * @return ResponseEntity&lt;BaseItemDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<BaseItemDto>> getItemWithHttpInfo(UUID userId, UUID itemId) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDto> localVarReturnType = new ParameterizedTypeReference<BaseItemDto>() {};
        return getItemRequestCreation(userId, itemId).toEntity(localVarReturnType);
    }

    /**
     * Gets an item from a user&#39;s library.
     * 
     * <p><b>200</b> - Item returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId User id.
     * @param itemId Item id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getItemWithResponseSpec(UUID userId, UUID itemId) throws WebClientResponseException {
        return getItemRequestCreation(userId, itemId);
    }

    /**
     * Gets latest media.
     * 
     * <p><b>200</b> - Latest media returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId User id.
     * @param parentId Specify this to localize the search to a specific item or folder. Omit to use the root.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param includeItemTypes Optional. If specified, results will be filtered based on item type. This allows multiple, comma delimited.
     * @param isPlayed Filter by items that are played, or not.
     * @param enableImages Optional. include image information in output.
     * @param imageTypeLimit Optional. the max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @param enableUserData Optional. include user data.
     * @param limit Return item limit.
     * @param groupItems Whether or not to group items into a parent container.
     * @return List&lt;BaseItemDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getLatestMediaRequestCreation(UUID userId, UUID parentId, List<ItemFields> fields, List<BaseItemKind> includeItemTypes, Boolean isPlayed, Boolean enableImages, Integer imageTypeLimit, List<ImageType> enableImageTypes, Boolean enableUserData, Integer limit, Boolean groupItems) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'userId' is set
        if (userId == null) {
            throw new WebClientResponseException("Missing the required parameter 'userId' when calling getLatestMedia", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("userId", userId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "parentId", parentId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "fields", fields));
        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "includeItemTypes", includeItemTypes));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isPlayed", isPlayed));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableImages", enableImages));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "imageTypeLimit", imageTypeLimit));
        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "enableImageTypes", enableImageTypes));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableUserData", enableUserData));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "limit", limit));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "groupItems", groupItems));
        
        final String[] localVarAccepts = { 
            "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<BaseItemDto> localVarReturnType = new ParameterizedTypeReference<BaseItemDto>() {};
        return apiClient.invokeAPI("/Users/{userId}/Items/Latest", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets latest media.
     * 
     * <p><b>200</b> - Latest media returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId User id.
     * @param parentId Specify this to localize the search to a specific item or folder. Omit to use the root.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param includeItemTypes Optional. If specified, results will be filtered based on item type. This allows multiple, comma delimited.
     * @param isPlayed Filter by items that are played, or not.
     * @param enableImages Optional. include image information in output.
     * @param imageTypeLimit Optional. the max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @param enableUserData Optional. include user data.
     * @param limit Return item limit.
     * @param groupItems Whether or not to group items into a parent container.
     * @return List&lt;BaseItemDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Flux<BaseItemDto> getLatestMedia(UUID userId, UUID parentId, List<ItemFields> fields, List<BaseItemKind> includeItemTypes, Boolean isPlayed, Boolean enableImages, Integer imageTypeLimit, List<ImageType> enableImageTypes, Boolean enableUserData, Integer limit, Boolean groupItems) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDto> localVarReturnType = new ParameterizedTypeReference<BaseItemDto>() {};
        return getLatestMediaRequestCreation(userId, parentId, fields, includeItemTypes, isPlayed, enableImages, imageTypeLimit, enableImageTypes, enableUserData, limit, groupItems).bodyToFlux(localVarReturnType);
    }

    /**
     * Gets latest media.
     * 
     * <p><b>200</b> - Latest media returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId User id.
     * @param parentId Specify this to localize the search to a specific item or folder. Omit to use the root.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param includeItemTypes Optional. If specified, results will be filtered based on item type. This allows multiple, comma delimited.
     * @param isPlayed Filter by items that are played, or not.
     * @param enableImages Optional. include image information in output.
     * @param imageTypeLimit Optional. the max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @param enableUserData Optional. include user data.
     * @param limit Return item limit.
     * @param groupItems Whether or not to group items into a parent container.
     * @return ResponseEntity&lt;List&lt;BaseItemDto&gt;&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<List<BaseItemDto>>> getLatestMediaWithHttpInfo(UUID userId, UUID parentId, List<ItemFields> fields, List<BaseItemKind> includeItemTypes, Boolean isPlayed, Boolean enableImages, Integer imageTypeLimit, List<ImageType> enableImageTypes, Boolean enableUserData, Integer limit, Boolean groupItems) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDto> localVarReturnType = new ParameterizedTypeReference<BaseItemDto>() {};
        return getLatestMediaRequestCreation(userId, parentId, fields, includeItemTypes, isPlayed, enableImages, imageTypeLimit, enableImageTypes, enableUserData, limit, groupItems).toEntityList(localVarReturnType);
    }

    /**
     * Gets latest media.
     * 
     * <p><b>200</b> - Latest media returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId User id.
     * @param parentId Specify this to localize the search to a specific item or folder. Omit to use the root.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param includeItemTypes Optional. If specified, results will be filtered based on item type. This allows multiple, comma delimited.
     * @param isPlayed Filter by items that are played, or not.
     * @param enableImages Optional. include image information in output.
     * @param imageTypeLimit Optional. the max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @param enableUserData Optional. include user data.
     * @param limit Return item limit.
     * @param groupItems Whether or not to group items into a parent container.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getLatestMediaWithResponseSpec(UUID userId, UUID parentId, List<ItemFields> fields, List<BaseItemKind> includeItemTypes, Boolean isPlayed, Boolean enableImages, Integer imageTypeLimit, List<ImageType> enableImageTypes, Boolean enableUserData, Integer limit, Boolean groupItems) throws WebClientResponseException {
        return getLatestMediaRequestCreation(userId, parentId, fields, includeItemTypes, isPlayed, enableImages, imageTypeLimit, enableImageTypes, enableUserData, limit, groupItems);
    }

    /**
     * Gets local trailers for an item.
     * 
     * <p><b>200</b> - An Microsoft.AspNetCore.Mvc.OkResult containing the item&#39;s local trailers.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId User id.
     * @param itemId Item id.
     * @return List&lt;BaseItemDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getLocalTrailersRequestCreation(UUID userId, UUID itemId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'userId' is set
        if (userId == null) {
            throw new WebClientResponseException("Missing the required parameter 'userId' when calling getLocalTrailers", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException("Missing the required parameter 'itemId' when calling getLocalTrailers", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("userId", userId);
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

        ParameterizedTypeReference<BaseItemDto> localVarReturnType = new ParameterizedTypeReference<BaseItemDto>() {};
        return apiClient.invokeAPI("/Users/{userId}/Items/{itemId}/LocalTrailers", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets local trailers for an item.
     * 
     * <p><b>200</b> - An Microsoft.AspNetCore.Mvc.OkResult containing the item&#39;s local trailers.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId User id.
     * @param itemId Item id.
     * @return List&lt;BaseItemDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Flux<BaseItemDto> getLocalTrailers(UUID userId, UUID itemId) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDto> localVarReturnType = new ParameterizedTypeReference<BaseItemDto>() {};
        return getLocalTrailersRequestCreation(userId, itemId).bodyToFlux(localVarReturnType);
    }

    /**
     * Gets local trailers for an item.
     * 
     * <p><b>200</b> - An Microsoft.AspNetCore.Mvc.OkResult containing the item&#39;s local trailers.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId User id.
     * @param itemId Item id.
     * @return ResponseEntity&lt;List&lt;BaseItemDto&gt;&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<List<BaseItemDto>>> getLocalTrailersWithHttpInfo(UUID userId, UUID itemId) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDto> localVarReturnType = new ParameterizedTypeReference<BaseItemDto>() {};
        return getLocalTrailersRequestCreation(userId, itemId).toEntityList(localVarReturnType);
    }

    /**
     * Gets local trailers for an item.
     * 
     * <p><b>200</b> - An Microsoft.AspNetCore.Mvc.OkResult containing the item&#39;s local trailers.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId User id.
     * @param itemId Item id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getLocalTrailersWithResponseSpec(UUID userId, UUID itemId) throws WebClientResponseException {
        return getLocalTrailersRequestCreation(userId, itemId);
    }

    /**
     * Gets the root folder from a user&#39;s library.
     * 
     * <p><b>200</b> - Root folder returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId User id.
     * @return BaseItemDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getRootFolderRequestCreation(UUID userId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'userId' is set
        if (userId == null) {
            throw new WebClientResponseException("Missing the required parameter 'userId' when calling getRootFolder", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("userId", userId);

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

        ParameterizedTypeReference<BaseItemDto> localVarReturnType = new ParameterizedTypeReference<BaseItemDto>() {};
        return apiClient.invokeAPI("/Users/{userId}/Items/Root", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets the root folder from a user&#39;s library.
     * 
     * <p><b>200</b> - Root folder returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId User id.
     * @return BaseItemDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<BaseItemDto> getRootFolder(UUID userId) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDto> localVarReturnType = new ParameterizedTypeReference<BaseItemDto>() {};
        return getRootFolderRequestCreation(userId).bodyToMono(localVarReturnType);
    }

    /**
     * Gets the root folder from a user&#39;s library.
     * 
     * <p><b>200</b> - Root folder returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId User id.
     * @return ResponseEntity&lt;BaseItemDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<BaseItemDto>> getRootFolderWithHttpInfo(UUID userId) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDto> localVarReturnType = new ParameterizedTypeReference<BaseItemDto>() {};
        return getRootFolderRequestCreation(userId).toEntity(localVarReturnType);
    }

    /**
     * Gets the root folder from a user&#39;s library.
     * 
     * <p><b>200</b> - Root folder returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId User id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getRootFolderWithResponseSpec(UUID userId) throws WebClientResponseException {
        return getRootFolderRequestCreation(userId);
    }

    /**
     * Gets special features for an item.
     * 
     * <p><b>200</b> - Special features returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId User id.
     * @param itemId Item id.
     * @return List&lt;BaseItemDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getSpecialFeaturesRequestCreation(UUID userId, UUID itemId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'userId' is set
        if (userId == null) {
            throw new WebClientResponseException("Missing the required parameter 'userId' when calling getSpecialFeatures", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException("Missing the required parameter 'itemId' when calling getSpecialFeatures", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("userId", userId);
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

        ParameterizedTypeReference<BaseItemDto> localVarReturnType = new ParameterizedTypeReference<BaseItemDto>() {};
        return apiClient.invokeAPI("/Users/{userId}/Items/{itemId}/SpecialFeatures", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets special features for an item.
     * 
     * <p><b>200</b> - Special features returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId User id.
     * @param itemId Item id.
     * @return List&lt;BaseItemDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Flux<BaseItemDto> getSpecialFeatures(UUID userId, UUID itemId) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDto> localVarReturnType = new ParameterizedTypeReference<BaseItemDto>() {};
        return getSpecialFeaturesRequestCreation(userId, itemId).bodyToFlux(localVarReturnType);
    }

    /**
     * Gets special features for an item.
     * 
     * <p><b>200</b> - Special features returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId User id.
     * @param itemId Item id.
     * @return ResponseEntity&lt;List&lt;BaseItemDto&gt;&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<List<BaseItemDto>>> getSpecialFeaturesWithHttpInfo(UUID userId, UUID itemId) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDto> localVarReturnType = new ParameterizedTypeReference<BaseItemDto>() {};
        return getSpecialFeaturesRequestCreation(userId, itemId).toEntityList(localVarReturnType);
    }

    /**
     * Gets special features for an item.
     * 
     * <p><b>200</b> - Special features returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId User id.
     * @param itemId Item id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getSpecialFeaturesWithResponseSpec(UUID userId, UUID itemId) throws WebClientResponseException {
        return getSpecialFeaturesRequestCreation(userId, itemId);
    }

    /**
     * Marks an item as a favorite.
     * 
     * <p><b>200</b> - Item marked as favorite.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId User id.
     * @param itemId Item id.
     * @return UserItemDataDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec markFavoriteItemRequestCreation(UUID userId, UUID itemId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'userId' is set
        if (userId == null) {
            throw new WebClientResponseException("Missing the required parameter 'userId' when calling markFavoriteItem", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException("Missing the required parameter 'itemId' when calling markFavoriteItem", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("userId", userId);
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

        ParameterizedTypeReference<UserItemDataDto> localVarReturnType = new ParameterizedTypeReference<UserItemDataDto>() {};
        return apiClient.invokeAPI("/Users/{userId}/FavoriteItems/{itemId}", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Marks an item as a favorite.
     * 
     * <p><b>200</b> - Item marked as favorite.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId User id.
     * @param itemId Item id.
     * @return UserItemDataDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<UserItemDataDto> markFavoriteItem(UUID userId, UUID itemId) throws WebClientResponseException {
        ParameterizedTypeReference<UserItemDataDto> localVarReturnType = new ParameterizedTypeReference<UserItemDataDto>() {};
        return markFavoriteItemRequestCreation(userId, itemId).bodyToMono(localVarReturnType);
    }

    /**
     * Marks an item as a favorite.
     * 
     * <p><b>200</b> - Item marked as favorite.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId User id.
     * @param itemId Item id.
     * @return ResponseEntity&lt;UserItemDataDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<UserItemDataDto>> markFavoriteItemWithHttpInfo(UUID userId, UUID itemId) throws WebClientResponseException {
        ParameterizedTypeReference<UserItemDataDto> localVarReturnType = new ParameterizedTypeReference<UserItemDataDto>() {};
        return markFavoriteItemRequestCreation(userId, itemId).toEntity(localVarReturnType);
    }

    /**
     * Marks an item as a favorite.
     * 
     * <p><b>200</b> - Item marked as favorite.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId User id.
     * @param itemId Item id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec markFavoriteItemWithResponseSpec(UUID userId, UUID itemId) throws WebClientResponseException {
        return markFavoriteItemRequestCreation(userId, itemId);
    }

    /**
     * Unmarks item as a favorite.
     * 
     * <p><b>200</b> - Item unmarked as favorite.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId User id.
     * @param itemId Item id.
     * @return UserItemDataDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec unmarkFavoriteItemRequestCreation(UUID userId, UUID itemId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'userId' is set
        if (userId == null) {
            throw new WebClientResponseException("Missing the required parameter 'userId' when calling unmarkFavoriteItem", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException("Missing the required parameter 'itemId' when calling unmarkFavoriteItem", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("userId", userId);
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

        ParameterizedTypeReference<UserItemDataDto> localVarReturnType = new ParameterizedTypeReference<UserItemDataDto>() {};
        return apiClient.invokeAPI("/Users/{userId}/FavoriteItems/{itemId}", HttpMethod.DELETE, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Unmarks item as a favorite.
     * 
     * <p><b>200</b> - Item unmarked as favorite.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId User id.
     * @param itemId Item id.
     * @return UserItemDataDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<UserItemDataDto> unmarkFavoriteItem(UUID userId, UUID itemId) throws WebClientResponseException {
        ParameterizedTypeReference<UserItemDataDto> localVarReturnType = new ParameterizedTypeReference<UserItemDataDto>() {};
        return unmarkFavoriteItemRequestCreation(userId, itemId).bodyToMono(localVarReturnType);
    }

    /**
     * Unmarks item as a favorite.
     * 
     * <p><b>200</b> - Item unmarked as favorite.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId User id.
     * @param itemId Item id.
     * @return ResponseEntity&lt;UserItemDataDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<UserItemDataDto>> unmarkFavoriteItemWithHttpInfo(UUID userId, UUID itemId) throws WebClientResponseException {
        ParameterizedTypeReference<UserItemDataDto> localVarReturnType = new ParameterizedTypeReference<UserItemDataDto>() {};
        return unmarkFavoriteItemRequestCreation(userId, itemId).toEntity(localVarReturnType);
    }

    /**
     * Unmarks item as a favorite.
     * 
     * <p><b>200</b> - Item unmarked as favorite.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId User id.
     * @param itemId Item id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec unmarkFavoriteItemWithResponseSpec(UUID userId, UUID itemId) throws WebClientResponseException {
        return unmarkFavoriteItemRequestCreation(userId, itemId);
    }

    /**
     * Updates a user&#39;s rating for an item.
     * 
     * <p><b>200</b> - Item rating updated.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId User id.
     * @param itemId Item id.
     * @param likes Whether this M:Jellyfin.Api.Controllers.UserLibraryController.UpdateUserItemRating(System.Guid,System.Guid,System.Nullable{System.Boolean}) is likes.
     * @return UserItemDataDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec updateUserItemRatingRequestCreation(UUID userId, UUID itemId, Boolean likes) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'userId' is set
        if (userId == null) {
            throw new WebClientResponseException("Missing the required parameter 'userId' when calling updateUserItemRating", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException("Missing the required parameter 'itemId' when calling updateUserItemRating", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("userId", userId);
        pathParams.put("itemId", itemId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "likes", likes));
        
        final String[] localVarAccepts = { 
            "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<UserItemDataDto> localVarReturnType = new ParameterizedTypeReference<UserItemDataDto>() {};
        return apiClient.invokeAPI("/Users/{userId}/Items/{itemId}/Rating", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Updates a user&#39;s rating for an item.
     * 
     * <p><b>200</b> - Item rating updated.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId User id.
     * @param itemId Item id.
     * @param likes Whether this M:Jellyfin.Api.Controllers.UserLibraryController.UpdateUserItemRating(System.Guid,System.Guid,System.Nullable{System.Boolean}) is likes.
     * @return UserItemDataDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<UserItemDataDto> updateUserItemRating(UUID userId, UUID itemId, Boolean likes) throws WebClientResponseException {
        ParameterizedTypeReference<UserItemDataDto> localVarReturnType = new ParameterizedTypeReference<UserItemDataDto>() {};
        return updateUserItemRatingRequestCreation(userId, itemId, likes).bodyToMono(localVarReturnType);
    }

    /**
     * Updates a user&#39;s rating for an item.
     * 
     * <p><b>200</b> - Item rating updated.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId User id.
     * @param itemId Item id.
     * @param likes Whether this M:Jellyfin.Api.Controllers.UserLibraryController.UpdateUserItemRating(System.Guid,System.Guid,System.Nullable{System.Boolean}) is likes.
     * @return ResponseEntity&lt;UserItemDataDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<UserItemDataDto>> updateUserItemRatingWithHttpInfo(UUID userId, UUID itemId, Boolean likes) throws WebClientResponseException {
        ParameterizedTypeReference<UserItemDataDto> localVarReturnType = new ParameterizedTypeReference<UserItemDataDto>() {};
        return updateUserItemRatingRequestCreation(userId, itemId, likes).toEntity(localVarReturnType);
    }

    /**
     * Updates a user&#39;s rating for an item.
     * 
     * <p><b>200</b> - Item rating updated.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId User id.
     * @param itemId Item id.
     * @param likes Whether this M:Jellyfin.Api.Controllers.UserLibraryController.UpdateUserItemRating(System.Guid,System.Guid,System.Nullable{System.Boolean}) is likes.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec updateUserItemRatingWithResponseSpec(UUID userId, UUID itemId, Boolean likes) throws WebClientResponseException {
        return updateUserItemRatingRequestCreation(userId, itemId, likes);
    }
}
