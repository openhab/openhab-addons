package org.openhab.binding.jellyfin.internal.api.version.current;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.openhab.binding.jellyfin.internal.api.version.ApiClient;
import org.openhab.binding.jellyfin.internal.api.version.current.model.BaseItemDto;
import org.openhab.binding.jellyfin.internal.api.version.current.model.BaseItemDtoQueryResult;
import org.openhab.binding.jellyfin.internal.api.version.current.model.BaseItemKind;
import org.openhab.binding.jellyfin.internal.api.version.current.model.ImageType;
import org.openhab.binding.jellyfin.internal.api.version.current.model.ItemFields;
import org.openhab.binding.jellyfin.internal.api.version.current.model.UserItemDataDto;
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

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
     * <p>
     * <b>200</b> - Personal rating removed.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId Item id.
     * @param userId User id.
     * @return UserItemDataDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec deleteUserItemRatingRequestCreation(UUID itemId, UUID userId)
            throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'itemId' when calling deleteUserItemRating",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("itemId", itemId);

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

        ParameterizedTypeReference<UserItemDataDto> localVarReturnType = new ParameterizedTypeReference<UserItemDataDto>() {
        };
        return apiClient.invokeAPI("/UserItems/{itemId}/Rating", HttpMethod.DELETE, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Deletes a user&#39;s saved personal rating for an item.
     * 
     * <p>
     * <b>200</b> - Personal rating removed.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId Item id.
     * @param userId User id.
     * @return UserItemDataDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<UserItemDataDto> deleteUserItemRating(UUID itemId, UUID userId) throws WebClientResponseException {
        ParameterizedTypeReference<UserItemDataDto> localVarReturnType = new ParameterizedTypeReference<UserItemDataDto>() {
        };
        return deleteUserItemRatingRequestCreation(itemId, userId).bodyToMono(localVarReturnType);
    }

    /**
     * Deletes a user&#39;s saved personal rating for an item.
     * 
     * <p>
     * <b>200</b> - Personal rating removed.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId Item id.
     * @param userId User id.
     * @return ResponseEntity&lt;UserItemDataDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<UserItemDataDto>> deleteUserItemRatingWithHttpInfo(UUID itemId, UUID userId)
            throws WebClientResponseException {
        ParameterizedTypeReference<UserItemDataDto> localVarReturnType = new ParameterizedTypeReference<UserItemDataDto>() {
        };
        return deleteUserItemRatingRequestCreation(itemId, userId).toEntity(localVarReturnType);
    }

    /**
     * Deletes a user&#39;s saved personal rating for an item.
     * 
     * <p>
     * <b>200</b> - Personal rating removed.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId Item id.
     * @param userId User id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec deleteUserItemRatingWithResponseSpec(UUID itemId, UUID userId)
            throws WebClientResponseException {
        return deleteUserItemRatingRequestCreation(itemId, userId);
    }

    /**
     * Gets intros to play before the main media item plays.
     * 
     * <p>
     * <b>200</b> - Intros returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId Item id.
     * @param userId User id.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getIntrosRequestCreation(UUID itemId, UUID userId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException("Missing the required parameter 'itemId' when calling getIntros",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("itemId", itemId);

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

        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {
        };
        return apiClient.invokeAPI("/Items/{itemId}/Intros", HttpMethod.GET, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Gets intros to play before the main media item plays.
     * 
     * <p>
     * <b>200</b> - Intros returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId Item id.
     * @param userId User id.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<BaseItemDtoQueryResult> getIntros(UUID itemId, UUID userId) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {
        };
        return getIntrosRequestCreation(itemId, userId).bodyToMono(localVarReturnType);
    }

    /**
     * Gets intros to play before the main media item plays.
     * 
     * <p>
     * <b>200</b> - Intros returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId Item id.
     * @param userId User id.
     * @return ResponseEntity&lt;BaseItemDtoQueryResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<BaseItemDtoQueryResult>> getIntrosWithHttpInfo(UUID itemId, UUID userId)
            throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {
        };
        return getIntrosRequestCreation(itemId, userId).toEntity(localVarReturnType);
    }

    /**
     * Gets intros to play before the main media item plays.
     * 
     * <p>
     * <b>200</b> - Intros returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId Item id.
     * @param userId User id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getIntrosWithResponseSpec(UUID itemId, UUID userId) throws WebClientResponseException {
        return getIntrosRequestCreation(itemId, userId);
    }

    /**
     * Gets an item from a user&#39;s library.
     * 
     * <p>
     * <b>200</b> - Item returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId Item id.
     * @param userId User id.
     * @return BaseItemDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getItemRequestCreation(UUID itemId, UUID userId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException("Missing the required parameter 'itemId' when calling getItem",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("itemId", itemId);

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

        ParameterizedTypeReference<BaseItemDto> localVarReturnType = new ParameterizedTypeReference<BaseItemDto>() {
        };
        return apiClient.invokeAPI("/Items/{itemId}", HttpMethod.GET, pathParams, queryParams, postBody, headerParams,
                cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets an item from a user&#39;s library.
     * 
     * <p>
     * <b>200</b> - Item returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId Item id.
     * @param userId User id.
     * @return BaseItemDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<BaseItemDto> getItem(UUID itemId, UUID userId) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDto> localVarReturnType = new ParameterizedTypeReference<BaseItemDto>() {
        };
        return getItemRequestCreation(itemId, userId).bodyToMono(localVarReturnType);
    }

    /**
     * Gets an item from a user&#39;s library.
     * 
     * <p>
     * <b>200</b> - Item returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId Item id.
     * @param userId User id.
     * @return ResponseEntity&lt;BaseItemDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<BaseItemDto>> getItemWithHttpInfo(UUID itemId, UUID userId)
            throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDto> localVarReturnType = new ParameterizedTypeReference<BaseItemDto>() {
        };
        return getItemRequestCreation(itemId, userId).toEntity(localVarReturnType);
    }

    /**
     * Gets an item from a user&#39;s library.
     * 
     * <p>
     * <b>200</b> - Item returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId Item id.
     * @param userId User id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getItemWithResponseSpec(UUID itemId, UUID userId) throws WebClientResponseException {
        return getItemRequestCreation(itemId, userId);
    }

    /**
     * Gets latest media.
     * 
     * <p>
     * <b>200</b> - Latest media returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param userId User id.
     * @param parentId Specify this to localize the search to a specific item or folder. Omit to use the root.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param includeItemTypes Optional. If specified, results will be filtered based on item type. This allows
     *            multiple, comma delimited.
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
    private ResponseSpec getLatestMediaRequestCreation(UUID userId, UUID parentId, List<ItemFields> fields,
            List<BaseItemKind> includeItemTypes, Boolean isPlayed, Boolean enableImages, Integer imageTypeLimit,
            List<ImageType> enableImageTypes, Boolean enableUserData, Integer limit, Boolean groupItems)
            throws WebClientResponseException {
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
        queryParams.putAll(
                apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)),
                        "includeItemTypes", includeItemTypes));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isPlayed", isPlayed));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableImages", enableImages));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "imageTypeLimit", imageTypeLimit));
        queryParams.putAll(
                apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)),
                        "enableImageTypes", enableImageTypes));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableUserData", enableUserData));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "limit", limit));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "groupItems", groupItems));

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<BaseItemDto> localVarReturnType = new ParameterizedTypeReference<BaseItemDto>() {
        };
        return apiClient.invokeAPI("/Items/Latest", HttpMethod.GET, pathParams, queryParams, postBody, headerParams,
                cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets latest media.
     * 
     * <p>
     * <b>200</b> - Latest media returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param userId User id.
     * @param parentId Specify this to localize the search to a specific item or folder. Omit to use the root.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param includeItemTypes Optional. If specified, results will be filtered based on item type. This allows
     *            multiple, comma delimited.
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
    public Flux<BaseItemDto> getLatestMedia(UUID userId, UUID parentId, List<ItemFields> fields,
            List<BaseItemKind> includeItemTypes, Boolean isPlayed, Boolean enableImages, Integer imageTypeLimit,
            List<ImageType> enableImageTypes, Boolean enableUserData, Integer limit, Boolean groupItems)
            throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDto> localVarReturnType = new ParameterizedTypeReference<BaseItemDto>() {
        };
        return getLatestMediaRequestCreation(userId, parentId, fields, includeItemTypes, isPlayed, enableImages,
                imageTypeLimit, enableImageTypes, enableUserData, limit, groupItems).bodyToFlux(localVarReturnType);
    }

    /**
     * Gets latest media.
     * 
     * <p>
     * <b>200</b> - Latest media returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param userId User id.
     * @param parentId Specify this to localize the search to a specific item or folder. Omit to use the root.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param includeItemTypes Optional. If specified, results will be filtered based on item type. This allows
     *            multiple, comma delimited.
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
    public Mono<ResponseEntity<List<BaseItemDto>>> getLatestMediaWithHttpInfo(UUID userId, UUID parentId,
            List<ItemFields> fields, List<BaseItemKind> includeItemTypes, Boolean isPlayed, Boolean enableImages,
            Integer imageTypeLimit, List<ImageType> enableImageTypes, Boolean enableUserData, Integer limit,
            Boolean groupItems) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDto> localVarReturnType = new ParameterizedTypeReference<BaseItemDto>() {
        };
        return getLatestMediaRequestCreation(userId, parentId, fields, includeItemTypes, isPlayed, enableImages,
                imageTypeLimit, enableImageTypes, enableUserData, limit, groupItems).toEntityList(localVarReturnType);
    }

    /**
     * Gets latest media.
     * 
     * <p>
     * <b>200</b> - Latest media returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param userId User id.
     * @param parentId Specify this to localize the search to a specific item or folder. Omit to use the root.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param includeItemTypes Optional. If specified, results will be filtered based on item type. This allows
     *            multiple, comma delimited.
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
    public ResponseSpec getLatestMediaWithResponseSpec(UUID userId, UUID parentId, List<ItemFields> fields,
            List<BaseItemKind> includeItemTypes, Boolean isPlayed, Boolean enableImages, Integer imageTypeLimit,
            List<ImageType> enableImageTypes, Boolean enableUserData, Integer limit, Boolean groupItems)
            throws WebClientResponseException {
        return getLatestMediaRequestCreation(userId, parentId, fields, includeItemTypes, isPlayed, enableImages,
                imageTypeLimit, enableImageTypes, enableUserData, limit, groupItems);
    }

    /**
     * Gets local trailers for an item.
     * 
     * <p>
     * <b>200</b> - An Microsoft.AspNetCore.Mvc.OkResult containing the item&#39;s local trailers.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId Item id.
     * @param userId User id.
     * @return List&lt;BaseItemDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getLocalTrailersRequestCreation(UUID itemId, UUID userId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'itemId' when calling getLocalTrailers",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("itemId", itemId);

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

        ParameterizedTypeReference<BaseItemDto> localVarReturnType = new ParameterizedTypeReference<BaseItemDto>() {
        };
        return apiClient.invokeAPI("/Items/{itemId}/LocalTrailers", HttpMethod.GET, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Gets local trailers for an item.
     * 
     * <p>
     * <b>200</b> - An Microsoft.AspNetCore.Mvc.OkResult containing the item&#39;s local trailers.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId Item id.
     * @param userId User id.
     * @return List&lt;BaseItemDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Flux<BaseItemDto> getLocalTrailers(UUID itemId, UUID userId) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDto> localVarReturnType = new ParameterizedTypeReference<BaseItemDto>() {
        };
        return getLocalTrailersRequestCreation(itemId, userId).bodyToFlux(localVarReturnType);
    }

    /**
     * Gets local trailers for an item.
     * 
     * <p>
     * <b>200</b> - An Microsoft.AspNetCore.Mvc.OkResult containing the item&#39;s local trailers.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId Item id.
     * @param userId User id.
     * @return ResponseEntity&lt;List&lt;BaseItemDto&gt;&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<List<BaseItemDto>>> getLocalTrailersWithHttpInfo(UUID itemId, UUID userId)
            throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDto> localVarReturnType = new ParameterizedTypeReference<BaseItemDto>() {
        };
        return getLocalTrailersRequestCreation(itemId, userId).toEntityList(localVarReturnType);
    }

    /**
     * Gets local trailers for an item.
     * 
     * <p>
     * <b>200</b> - An Microsoft.AspNetCore.Mvc.OkResult containing the item&#39;s local trailers.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId Item id.
     * @param userId User id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getLocalTrailersWithResponseSpec(UUID itemId, UUID userId) throws WebClientResponseException {
        return getLocalTrailersRequestCreation(itemId, userId);
    }

    /**
     * Gets the root folder from a user&#39;s library.
     * 
     * <p>
     * <b>200</b> - Root folder returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param userId User id.
     * @return BaseItemDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getRootFolderRequestCreation(UUID userId) throws WebClientResponseException {
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

        ParameterizedTypeReference<BaseItemDto> localVarReturnType = new ParameterizedTypeReference<BaseItemDto>() {
        };
        return apiClient.invokeAPI("/Items/Root", HttpMethod.GET, pathParams, queryParams, postBody, headerParams,
                cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets the root folder from a user&#39;s library.
     * 
     * <p>
     * <b>200</b> - Root folder returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param userId User id.
     * @return BaseItemDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<BaseItemDto> getRootFolder(UUID userId) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDto> localVarReturnType = new ParameterizedTypeReference<BaseItemDto>() {
        };
        return getRootFolderRequestCreation(userId).bodyToMono(localVarReturnType);
    }

    /**
     * Gets the root folder from a user&#39;s library.
     * 
     * <p>
     * <b>200</b> - Root folder returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param userId User id.
     * @return ResponseEntity&lt;BaseItemDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<BaseItemDto>> getRootFolderWithHttpInfo(UUID userId) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDto> localVarReturnType = new ParameterizedTypeReference<BaseItemDto>() {
        };
        return getRootFolderRequestCreation(userId).toEntity(localVarReturnType);
    }

    /**
     * Gets the root folder from a user&#39;s library.
     * 
     * <p>
     * <b>200</b> - Root folder returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
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
     * <p>
     * <b>200</b> - Special features returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId Item id.
     * @param userId User id.
     * @return List&lt;BaseItemDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getSpecialFeaturesRequestCreation(UUID itemId, UUID userId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'itemId' when calling getSpecialFeatures",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("itemId", itemId);

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

        ParameterizedTypeReference<BaseItemDto> localVarReturnType = new ParameterizedTypeReference<BaseItemDto>() {
        };
        return apiClient.invokeAPI("/Items/{itemId}/SpecialFeatures", HttpMethod.GET, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Gets special features for an item.
     * 
     * <p>
     * <b>200</b> - Special features returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId Item id.
     * @param userId User id.
     * @return List&lt;BaseItemDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Flux<BaseItemDto> getSpecialFeatures(UUID itemId, UUID userId) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDto> localVarReturnType = new ParameterizedTypeReference<BaseItemDto>() {
        };
        return getSpecialFeaturesRequestCreation(itemId, userId).bodyToFlux(localVarReturnType);
    }

    /**
     * Gets special features for an item.
     * 
     * <p>
     * <b>200</b> - Special features returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId Item id.
     * @param userId User id.
     * @return ResponseEntity&lt;List&lt;BaseItemDto&gt;&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<List<BaseItemDto>>> getSpecialFeaturesWithHttpInfo(UUID itemId, UUID userId)
            throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDto> localVarReturnType = new ParameterizedTypeReference<BaseItemDto>() {
        };
        return getSpecialFeaturesRequestCreation(itemId, userId).toEntityList(localVarReturnType);
    }

    /**
     * Gets special features for an item.
     * 
     * <p>
     * <b>200</b> - Special features returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId Item id.
     * @param userId User id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getSpecialFeaturesWithResponseSpec(UUID itemId, UUID userId) throws WebClientResponseException {
        return getSpecialFeaturesRequestCreation(itemId, userId);
    }

    /**
     * Marks an item as a favorite.
     * 
     * <p>
     * <b>200</b> - Item marked as favorite.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId Item id.
     * @param userId User id.
     * @return UserItemDataDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec markFavoriteItemRequestCreation(UUID itemId, UUID userId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'itemId' when calling markFavoriteItem",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("itemId", itemId);

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

        ParameterizedTypeReference<UserItemDataDto> localVarReturnType = new ParameterizedTypeReference<UserItemDataDto>() {
        };
        return apiClient.invokeAPI("/UserFavoriteItems/{itemId}", HttpMethod.POST, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Marks an item as a favorite.
     * 
     * <p>
     * <b>200</b> - Item marked as favorite.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId Item id.
     * @param userId User id.
     * @return UserItemDataDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<UserItemDataDto> markFavoriteItem(UUID itemId, UUID userId) throws WebClientResponseException {
        ParameterizedTypeReference<UserItemDataDto> localVarReturnType = new ParameterizedTypeReference<UserItemDataDto>() {
        };
        return markFavoriteItemRequestCreation(itemId, userId).bodyToMono(localVarReturnType);
    }

    /**
     * Marks an item as a favorite.
     * 
     * <p>
     * <b>200</b> - Item marked as favorite.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId Item id.
     * @param userId User id.
     * @return ResponseEntity&lt;UserItemDataDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<UserItemDataDto>> markFavoriteItemWithHttpInfo(UUID itemId, UUID userId)
            throws WebClientResponseException {
        ParameterizedTypeReference<UserItemDataDto> localVarReturnType = new ParameterizedTypeReference<UserItemDataDto>() {
        };
        return markFavoriteItemRequestCreation(itemId, userId).toEntity(localVarReturnType);
    }

    /**
     * Marks an item as a favorite.
     * 
     * <p>
     * <b>200</b> - Item marked as favorite.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId Item id.
     * @param userId User id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec markFavoriteItemWithResponseSpec(UUID itemId, UUID userId) throws WebClientResponseException {
        return markFavoriteItemRequestCreation(itemId, userId);
    }

    /**
     * Unmarks item as a favorite.
     * 
     * <p>
     * <b>200</b> - Item unmarked as favorite.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId Item id.
     * @param userId User id.
     * @return UserItemDataDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec unmarkFavoriteItemRequestCreation(UUID itemId, UUID userId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'itemId' when calling unmarkFavoriteItem",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("itemId", itemId);

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

        ParameterizedTypeReference<UserItemDataDto> localVarReturnType = new ParameterizedTypeReference<UserItemDataDto>() {
        };
        return apiClient.invokeAPI("/UserFavoriteItems/{itemId}", HttpMethod.DELETE, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Unmarks item as a favorite.
     * 
     * <p>
     * <b>200</b> - Item unmarked as favorite.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId Item id.
     * @param userId User id.
     * @return UserItemDataDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<UserItemDataDto> unmarkFavoriteItem(UUID itemId, UUID userId) throws WebClientResponseException {
        ParameterizedTypeReference<UserItemDataDto> localVarReturnType = new ParameterizedTypeReference<UserItemDataDto>() {
        };
        return unmarkFavoriteItemRequestCreation(itemId, userId).bodyToMono(localVarReturnType);
    }

    /**
     * Unmarks item as a favorite.
     * 
     * <p>
     * <b>200</b> - Item unmarked as favorite.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId Item id.
     * @param userId User id.
     * @return ResponseEntity&lt;UserItemDataDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<UserItemDataDto>> unmarkFavoriteItemWithHttpInfo(UUID itemId, UUID userId)
            throws WebClientResponseException {
        ParameterizedTypeReference<UserItemDataDto> localVarReturnType = new ParameterizedTypeReference<UserItemDataDto>() {
        };
        return unmarkFavoriteItemRequestCreation(itemId, userId).toEntity(localVarReturnType);
    }

    /**
     * Unmarks item as a favorite.
     * 
     * <p>
     * <b>200</b> - Item unmarked as favorite.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId Item id.
     * @param userId User id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec unmarkFavoriteItemWithResponseSpec(UUID itemId, UUID userId) throws WebClientResponseException {
        return unmarkFavoriteItemRequestCreation(itemId, userId);
    }

    /**
     * Updates a user&#39;s rating for an item.
     * 
     * <p>
     * <b>200</b> - Item rating updated.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId Item id.
     * @param userId User id.
     * @param likes Whether this
     *            M:Jellyfin.Api.Controllers.UserLibraryController.UpdateUserItemRating(System.Nullable{System.Guid},System.Guid,System.Nullable{System.Boolean})
     *            is likes.
     * @return UserItemDataDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec updateUserItemRatingRequestCreation(UUID itemId, UUID userId, Boolean likes)
            throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'itemId' when calling updateUserItemRating",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("itemId", itemId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "userId", userId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "likes", likes));

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<UserItemDataDto> localVarReturnType = new ParameterizedTypeReference<UserItemDataDto>() {
        };
        return apiClient.invokeAPI("/UserItems/{itemId}/Rating", HttpMethod.POST, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Updates a user&#39;s rating for an item.
     * 
     * <p>
     * <b>200</b> - Item rating updated.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId Item id.
     * @param userId User id.
     * @param likes Whether this
     *            M:Jellyfin.Api.Controllers.UserLibraryController.UpdateUserItemRating(System.Nullable{System.Guid},System.Guid,System.Nullable{System.Boolean})
     *            is likes.
     * @return UserItemDataDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<UserItemDataDto> updateUserItemRating(UUID itemId, UUID userId, Boolean likes)
            throws WebClientResponseException {
        ParameterizedTypeReference<UserItemDataDto> localVarReturnType = new ParameterizedTypeReference<UserItemDataDto>() {
        };
        return updateUserItemRatingRequestCreation(itemId, userId, likes).bodyToMono(localVarReturnType);
    }

    /**
     * Updates a user&#39;s rating for an item.
     * 
     * <p>
     * <b>200</b> - Item rating updated.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId Item id.
     * @param userId User id.
     * @param likes Whether this
     *            M:Jellyfin.Api.Controllers.UserLibraryController.UpdateUserItemRating(System.Nullable{System.Guid},System.Guid,System.Nullable{System.Boolean})
     *            is likes.
     * @return ResponseEntity&lt;UserItemDataDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<UserItemDataDto>> updateUserItemRatingWithHttpInfo(UUID itemId, UUID userId,
            Boolean likes) throws WebClientResponseException {
        ParameterizedTypeReference<UserItemDataDto> localVarReturnType = new ParameterizedTypeReference<UserItemDataDto>() {
        };
        return updateUserItemRatingRequestCreation(itemId, userId, likes).toEntity(localVarReturnType);
    }

    /**
     * Updates a user&#39;s rating for an item.
     * 
     * <p>
     * <b>200</b> - Item rating updated.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId Item id.
     * @param userId User id.
     * @param likes Whether this
     *            M:Jellyfin.Api.Controllers.UserLibraryController.UpdateUserItemRating(System.Nullable{System.Guid},System.Guid,System.Nullable{System.Boolean})
     *            is likes.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec updateUserItemRatingWithResponseSpec(UUID itemId, UUID userId, Boolean likes)
            throws WebClientResponseException {
        return updateUserItemRatingRequestCreation(itemId, userId, likes);
    }
}
