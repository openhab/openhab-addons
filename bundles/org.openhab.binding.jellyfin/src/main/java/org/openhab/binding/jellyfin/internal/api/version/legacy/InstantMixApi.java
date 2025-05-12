package org.openhab.binding.jellyfin.internal.api.version.legacy;

import org.openhab.binding.jellyfin.internal.api.version.ApiClient;

import org.openhab.binding.jellyfin.internal.api.version.legacy.model.BaseItemDtoQueryResult;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.ImageType;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.ItemFields;
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
public class InstantMixApi {
    private ApiClient apiClient;

    public InstantMixApi() {
        this(new ApiClient());
    }

    @Autowired
    public InstantMixApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    
    /**
     * Creates an instant playlist based on a given album.
     * 
     * <p><b>200</b> - Instant playlist returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param id The item id.
     * @param userId Optional. Filter by user id, and attach user data.
     * @param limit Optional. The maximum number of records to return.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param enableImages Optional. Include image information in output.
     * @param enableUserData Optional. Include user data.
     * @param imageTypeLimit Optional. The max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getInstantMixFromAlbumRequestCreation(UUID id, UUID userId, Integer limit, List<ItemFields> fields, Boolean enableImages, Boolean enableUserData, Integer imageTypeLimit, List<ImageType> enableImageTypes) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'id' is set
        if (id == null) {
            throw new WebClientResponseException("Missing the required parameter 'id' when calling getInstantMixFromAlbum", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("id", id);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "userId", userId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "limit", limit));
        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "fields", fields));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableImages", enableImages));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableUserData", enableUserData));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "imageTypeLimit", imageTypeLimit));
        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "enableImageTypes", enableImageTypes));
        
        final String[] localVarAccepts = { 
            "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {};
        return apiClient.invokeAPI("/Albums/{id}/InstantMix", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Creates an instant playlist based on a given album.
     * 
     * <p><b>200</b> - Instant playlist returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param id The item id.
     * @param userId Optional. Filter by user id, and attach user data.
     * @param limit Optional. The maximum number of records to return.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param enableImages Optional. Include image information in output.
     * @param enableUserData Optional. Include user data.
     * @param imageTypeLimit Optional. The max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<BaseItemDtoQueryResult> getInstantMixFromAlbum(UUID id, UUID userId, Integer limit, List<ItemFields> fields, Boolean enableImages, Boolean enableUserData, Integer imageTypeLimit, List<ImageType> enableImageTypes) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {};
        return getInstantMixFromAlbumRequestCreation(id, userId, limit, fields, enableImages, enableUserData, imageTypeLimit, enableImageTypes).bodyToMono(localVarReturnType);
    }

    /**
     * Creates an instant playlist based on a given album.
     * 
     * <p><b>200</b> - Instant playlist returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param id The item id.
     * @param userId Optional. Filter by user id, and attach user data.
     * @param limit Optional. The maximum number of records to return.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param enableImages Optional. Include image information in output.
     * @param enableUserData Optional. Include user data.
     * @param imageTypeLimit Optional. The max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @return ResponseEntity&lt;BaseItemDtoQueryResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<BaseItemDtoQueryResult>> getInstantMixFromAlbumWithHttpInfo(UUID id, UUID userId, Integer limit, List<ItemFields> fields, Boolean enableImages, Boolean enableUserData, Integer imageTypeLimit, List<ImageType> enableImageTypes) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {};
        return getInstantMixFromAlbumRequestCreation(id, userId, limit, fields, enableImages, enableUserData, imageTypeLimit, enableImageTypes).toEntity(localVarReturnType);
    }

    /**
     * Creates an instant playlist based on a given album.
     * 
     * <p><b>200</b> - Instant playlist returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param id The item id.
     * @param userId Optional. Filter by user id, and attach user data.
     * @param limit Optional. The maximum number of records to return.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param enableImages Optional. Include image information in output.
     * @param enableUserData Optional. Include user data.
     * @param imageTypeLimit Optional. The max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getInstantMixFromAlbumWithResponseSpec(UUID id, UUID userId, Integer limit, List<ItemFields> fields, Boolean enableImages, Boolean enableUserData, Integer imageTypeLimit, List<ImageType> enableImageTypes) throws WebClientResponseException {
        return getInstantMixFromAlbumRequestCreation(id, userId, limit, fields, enableImages, enableUserData, imageTypeLimit, enableImageTypes);
    }

    /**
     * Creates an instant playlist based on a given artist.
     * 
     * <p><b>200</b> - Instant playlist returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param id The item id.
     * @param userId Optional. Filter by user id, and attach user data.
     * @param limit Optional. The maximum number of records to return.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param enableImages Optional. Include image information in output.
     * @param enableUserData Optional. Include user data.
     * @param imageTypeLimit Optional. The max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getInstantMixFromArtistsRequestCreation(UUID id, UUID userId, Integer limit, List<ItemFields> fields, Boolean enableImages, Boolean enableUserData, Integer imageTypeLimit, List<ImageType> enableImageTypes) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'id' is set
        if (id == null) {
            throw new WebClientResponseException("Missing the required parameter 'id' when calling getInstantMixFromArtists", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("id", id);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "userId", userId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "limit", limit));
        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "fields", fields));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableImages", enableImages));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableUserData", enableUserData));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "imageTypeLimit", imageTypeLimit));
        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "enableImageTypes", enableImageTypes));
        
        final String[] localVarAccepts = { 
            "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {};
        return apiClient.invokeAPI("/Artists/{id}/InstantMix", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Creates an instant playlist based on a given artist.
     * 
     * <p><b>200</b> - Instant playlist returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param id The item id.
     * @param userId Optional. Filter by user id, and attach user data.
     * @param limit Optional. The maximum number of records to return.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param enableImages Optional. Include image information in output.
     * @param enableUserData Optional. Include user data.
     * @param imageTypeLimit Optional. The max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<BaseItemDtoQueryResult> getInstantMixFromArtists(UUID id, UUID userId, Integer limit, List<ItemFields> fields, Boolean enableImages, Boolean enableUserData, Integer imageTypeLimit, List<ImageType> enableImageTypes) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {};
        return getInstantMixFromArtistsRequestCreation(id, userId, limit, fields, enableImages, enableUserData, imageTypeLimit, enableImageTypes).bodyToMono(localVarReturnType);
    }

    /**
     * Creates an instant playlist based on a given artist.
     * 
     * <p><b>200</b> - Instant playlist returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param id The item id.
     * @param userId Optional. Filter by user id, and attach user data.
     * @param limit Optional. The maximum number of records to return.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param enableImages Optional. Include image information in output.
     * @param enableUserData Optional. Include user data.
     * @param imageTypeLimit Optional. The max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @return ResponseEntity&lt;BaseItemDtoQueryResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<BaseItemDtoQueryResult>> getInstantMixFromArtistsWithHttpInfo(UUID id, UUID userId, Integer limit, List<ItemFields> fields, Boolean enableImages, Boolean enableUserData, Integer imageTypeLimit, List<ImageType> enableImageTypes) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {};
        return getInstantMixFromArtistsRequestCreation(id, userId, limit, fields, enableImages, enableUserData, imageTypeLimit, enableImageTypes).toEntity(localVarReturnType);
    }

    /**
     * Creates an instant playlist based on a given artist.
     * 
     * <p><b>200</b> - Instant playlist returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param id The item id.
     * @param userId Optional. Filter by user id, and attach user data.
     * @param limit Optional. The maximum number of records to return.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param enableImages Optional. Include image information in output.
     * @param enableUserData Optional. Include user data.
     * @param imageTypeLimit Optional. The max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getInstantMixFromArtistsWithResponseSpec(UUID id, UUID userId, Integer limit, List<ItemFields> fields, Boolean enableImages, Boolean enableUserData, Integer imageTypeLimit, List<ImageType> enableImageTypes) throws WebClientResponseException {
        return getInstantMixFromArtistsRequestCreation(id, userId, limit, fields, enableImages, enableUserData, imageTypeLimit, enableImageTypes);
    }

    /**
     * Creates an instant playlist based on a given artist.
     * 
     * <p><b>200</b> - Instant playlist returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param id The item id.
     * @param userId Optional. Filter by user id, and attach user data.
     * @param limit Optional. The maximum number of records to return.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param enableImages Optional. Include image information in output.
     * @param enableUserData Optional. Include user data.
     * @param imageTypeLimit Optional. The max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     * @deprecated
     */
    @Deprecated
    private ResponseSpec getInstantMixFromArtists2RequestCreation(UUID id, UUID userId, Integer limit, List<ItemFields> fields, Boolean enableImages, Boolean enableUserData, Integer imageTypeLimit, List<ImageType> enableImageTypes) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'id' is set
        if (id == null) {
            throw new WebClientResponseException("Missing the required parameter 'id' when calling getInstantMixFromArtists2", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "id", id));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "userId", userId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "limit", limit));
        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "fields", fields));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableImages", enableImages));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableUserData", enableUserData));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "imageTypeLimit", imageTypeLimit));
        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "enableImageTypes", enableImageTypes));
        
        final String[] localVarAccepts = { 
            "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {};
        return apiClient.invokeAPI("/Artists/InstantMix", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Creates an instant playlist based on a given artist.
     * 
     * <p><b>200</b> - Instant playlist returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param id The item id.
     * @param userId Optional. Filter by user id, and attach user data.
     * @param limit Optional. The maximum number of records to return.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param enableImages Optional. Include image information in output.
     * @param enableUserData Optional. Include user data.
     * @param imageTypeLimit Optional. The max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<BaseItemDtoQueryResult> getInstantMixFromArtists2(UUID id, UUID userId, Integer limit, List<ItemFields> fields, Boolean enableImages, Boolean enableUserData, Integer imageTypeLimit, List<ImageType> enableImageTypes) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {};
        return getInstantMixFromArtists2RequestCreation(id, userId, limit, fields, enableImages, enableUserData, imageTypeLimit, enableImageTypes).bodyToMono(localVarReturnType);
    }

    /**
     * Creates an instant playlist based on a given artist.
     * 
     * <p><b>200</b> - Instant playlist returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param id The item id.
     * @param userId Optional. Filter by user id, and attach user data.
     * @param limit Optional. The maximum number of records to return.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param enableImages Optional. Include image information in output.
     * @param enableUserData Optional. Include user data.
     * @param imageTypeLimit Optional. The max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @return ResponseEntity&lt;BaseItemDtoQueryResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<BaseItemDtoQueryResult>> getInstantMixFromArtists2WithHttpInfo(UUID id, UUID userId, Integer limit, List<ItemFields> fields, Boolean enableImages, Boolean enableUserData, Integer imageTypeLimit, List<ImageType> enableImageTypes) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {};
        return getInstantMixFromArtists2RequestCreation(id, userId, limit, fields, enableImages, enableUserData, imageTypeLimit, enableImageTypes).toEntity(localVarReturnType);
    }

    /**
     * Creates an instant playlist based on a given artist.
     * 
     * <p><b>200</b> - Instant playlist returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param id The item id.
     * @param userId Optional. Filter by user id, and attach user data.
     * @param limit Optional. The maximum number of records to return.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param enableImages Optional. Include image information in output.
     * @param enableUserData Optional. Include user data.
     * @param imageTypeLimit Optional. The max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getInstantMixFromArtists2WithResponseSpec(UUID id, UUID userId, Integer limit, List<ItemFields> fields, Boolean enableImages, Boolean enableUserData, Integer imageTypeLimit, List<ImageType> enableImageTypes) throws WebClientResponseException {
        return getInstantMixFromArtists2RequestCreation(id, userId, limit, fields, enableImages, enableUserData, imageTypeLimit, enableImageTypes);
    }

    /**
     * Creates an instant playlist based on a given item.
     * 
     * <p><b>200</b> - Instant playlist returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param id The item id.
     * @param userId Optional. Filter by user id, and attach user data.
     * @param limit Optional. The maximum number of records to return.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param enableImages Optional. Include image information in output.
     * @param enableUserData Optional. Include user data.
     * @param imageTypeLimit Optional. The max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getInstantMixFromItemRequestCreation(UUID id, UUID userId, Integer limit, List<ItemFields> fields, Boolean enableImages, Boolean enableUserData, Integer imageTypeLimit, List<ImageType> enableImageTypes) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'id' is set
        if (id == null) {
            throw new WebClientResponseException("Missing the required parameter 'id' when calling getInstantMixFromItem", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("id", id);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "userId", userId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "limit", limit));
        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "fields", fields));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableImages", enableImages));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableUserData", enableUserData));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "imageTypeLimit", imageTypeLimit));
        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "enableImageTypes", enableImageTypes));
        
        final String[] localVarAccepts = { 
            "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {};
        return apiClient.invokeAPI("/Items/{id}/InstantMix", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Creates an instant playlist based on a given item.
     * 
     * <p><b>200</b> - Instant playlist returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param id The item id.
     * @param userId Optional. Filter by user id, and attach user data.
     * @param limit Optional. The maximum number of records to return.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param enableImages Optional. Include image information in output.
     * @param enableUserData Optional. Include user data.
     * @param imageTypeLimit Optional. The max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<BaseItemDtoQueryResult> getInstantMixFromItem(UUID id, UUID userId, Integer limit, List<ItemFields> fields, Boolean enableImages, Boolean enableUserData, Integer imageTypeLimit, List<ImageType> enableImageTypes) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {};
        return getInstantMixFromItemRequestCreation(id, userId, limit, fields, enableImages, enableUserData, imageTypeLimit, enableImageTypes).bodyToMono(localVarReturnType);
    }

    /**
     * Creates an instant playlist based on a given item.
     * 
     * <p><b>200</b> - Instant playlist returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param id The item id.
     * @param userId Optional. Filter by user id, and attach user data.
     * @param limit Optional. The maximum number of records to return.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param enableImages Optional. Include image information in output.
     * @param enableUserData Optional. Include user data.
     * @param imageTypeLimit Optional. The max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @return ResponseEntity&lt;BaseItemDtoQueryResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<BaseItemDtoQueryResult>> getInstantMixFromItemWithHttpInfo(UUID id, UUID userId, Integer limit, List<ItemFields> fields, Boolean enableImages, Boolean enableUserData, Integer imageTypeLimit, List<ImageType> enableImageTypes) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {};
        return getInstantMixFromItemRequestCreation(id, userId, limit, fields, enableImages, enableUserData, imageTypeLimit, enableImageTypes).toEntity(localVarReturnType);
    }

    /**
     * Creates an instant playlist based on a given item.
     * 
     * <p><b>200</b> - Instant playlist returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param id The item id.
     * @param userId Optional. Filter by user id, and attach user data.
     * @param limit Optional. The maximum number of records to return.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param enableImages Optional. Include image information in output.
     * @param enableUserData Optional. Include user data.
     * @param imageTypeLimit Optional. The max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getInstantMixFromItemWithResponseSpec(UUID id, UUID userId, Integer limit, List<ItemFields> fields, Boolean enableImages, Boolean enableUserData, Integer imageTypeLimit, List<ImageType> enableImageTypes) throws WebClientResponseException {
        return getInstantMixFromItemRequestCreation(id, userId, limit, fields, enableImages, enableUserData, imageTypeLimit, enableImageTypes);
    }

    /**
     * Creates an instant playlist based on a given genre.
     * 
     * <p><b>200</b> - Instant playlist returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param id The item id.
     * @param userId Optional. Filter by user id, and attach user data.
     * @param limit Optional. The maximum number of records to return.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param enableImages Optional. Include image information in output.
     * @param enableUserData Optional. Include user data.
     * @param imageTypeLimit Optional. The max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getInstantMixFromMusicGenreByIdRequestCreation(UUID id, UUID userId, Integer limit, List<ItemFields> fields, Boolean enableImages, Boolean enableUserData, Integer imageTypeLimit, List<ImageType> enableImageTypes) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'id' is set
        if (id == null) {
            throw new WebClientResponseException("Missing the required parameter 'id' when calling getInstantMixFromMusicGenreById", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "id", id));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "userId", userId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "limit", limit));
        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "fields", fields));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableImages", enableImages));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableUserData", enableUserData));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "imageTypeLimit", imageTypeLimit));
        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "enableImageTypes", enableImageTypes));
        
        final String[] localVarAccepts = { 
            "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {};
        return apiClient.invokeAPI("/MusicGenres/InstantMix", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Creates an instant playlist based on a given genre.
     * 
     * <p><b>200</b> - Instant playlist returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param id The item id.
     * @param userId Optional. Filter by user id, and attach user data.
     * @param limit Optional. The maximum number of records to return.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param enableImages Optional. Include image information in output.
     * @param enableUserData Optional. Include user data.
     * @param imageTypeLimit Optional. The max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<BaseItemDtoQueryResult> getInstantMixFromMusicGenreById(UUID id, UUID userId, Integer limit, List<ItemFields> fields, Boolean enableImages, Boolean enableUserData, Integer imageTypeLimit, List<ImageType> enableImageTypes) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {};
        return getInstantMixFromMusicGenreByIdRequestCreation(id, userId, limit, fields, enableImages, enableUserData, imageTypeLimit, enableImageTypes).bodyToMono(localVarReturnType);
    }

    /**
     * Creates an instant playlist based on a given genre.
     * 
     * <p><b>200</b> - Instant playlist returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param id The item id.
     * @param userId Optional. Filter by user id, and attach user data.
     * @param limit Optional. The maximum number of records to return.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param enableImages Optional. Include image information in output.
     * @param enableUserData Optional. Include user data.
     * @param imageTypeLimit Optional. The max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @return ResponseEntity&lt;BaseItemDtoQueryResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<BaseItemDtoQueryResult>> getInstantMixFromMusicGenreByIdWithHttpInfo(UUID id, UUID userId, Integer limit, List<ItemFields> fields, Boolean enableImages, Boolean enableUserData, Integer imageTypeLimit, List<ImageType> enableImageTypes) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {};
        return getInstantMixFromMusicGenreByIdRequestCreation(id, userId, limit, fields, enableImages, enableUserData, imageTypeLimit, enableImageTypes).toEntity(localVarReturnType);
    }

    /**
     * Creates an instant playlist based on a given genre.
     * 
     * <p><b>200</b> - Instant playlist returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param id The item id.
     * @param userId Optional. Filter by user id, and attach user data.
     * @param limit Optional. The maximum number of records to return.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param enableImages Optional. Include image information in output.
     * @param enableUserData Optional. Include user data.
     * @param imageTypeLimit Optional. The max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getInstantMixFromMusicGenreByIdWithResponseSpec(UUID id, UUID userId, Integer limit, List<ItemFields> fields, Boolean enableImages, Boolean enableUserData, Integer imageTypeLimit, List<ImageType> enableImageTypes) throws WebClientResponseException {
        return getInstantMixFromMusicGenreByIdRequestCreation(id, userId, limit, fields, enableImages, enableUserData, imageTypeLimit, enableImageTypes);
    }

    /**
     * Creates an instant playlist based on a given genre.
     * 
     * <p><b>200</b> - Instant playlist returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param name The genre name.
     * @param userId Optional. Filter by user id, and attach user data.
     * @param limit Optional. The maximum number of records to return.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param enableImages Optional. Include image information in output.
     * @param enableUserData Optional. Include user data.
     * @param imageTypeLimit Optional. The max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getInstantMixFromMusicGenreByNameRequestCreation(String name, UUID userId, Integer limit, List<ItemFields> fields, Boolean enableImages, Boolean enableUserData, Integer imageTypeLimit, List<ImageType> enableImageTypes) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'name' is set
        if (name == null) {
            throw new WebClientResponseException("Missing the required parameter 'name' when calling getInstantMixFromMusicGenreByName", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("name", name);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "userId", userId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "limit", limit));
        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "fields", fields));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableImages", enableImages));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableUserData", enableUserData));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "imageTypeLimit", imageTypeLimit));
        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "enableImageTypes", enableImageTypes));
        
        final String[] localVarAccepts = { 
            "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {};
        return apiClient.invokeAPI("/MusicGenres/{name}/InstantMix", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Creates an instant playlist based on a given genre.
     * 
     * <p><b>200</b> - Instant playlist returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param name The genre name.
     * @param userId Optional. Filter by user id, and attach user data.
     * @param limit Optional. The maximum number of records to return.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param enableImages Optional. Include image information in output.
     * @param enableUserData Optional. Include user data.
     * @param imageTypeLimit Optional. The max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<BaseItemDtoQueryResult> getInstantMixFromMusicGenreByName(String name, UUID userId, Integer limit, List<ItemFields> fields, Boolean enableImages, Boolean enableUserData, Integer imageTypeLimit, List<ImageType> enableImageTypes) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {};
        return getInstantMixFromMusicGenreByNameRequestCreation(name, userId, limit, fields, enableImages, enableUserData, imageTypeLimit, enableImageTypes).bodyToMono(localVarReturnType);
    }

    /**
     * Creates an instant playlist based on a given genre.
     * 
     * <p><b>200</b> - Instant playlist returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param name The genre name.
     * @param userId Optional. Filter by user id, and attach user data.
     * @param limit Optional. The maximum number of records to return.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param enableImages Optional. Include image information in output.
     * @param enableUserData Optional. Include user data.
     * @param imageTypeLimit Optional. The max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @return ResponseEntity&lt;BaseItemDtoQueryResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<BaseItemDtoQueryResult>> getInstantMixFromMusicGenreByNameWithHttpInfo(String name, UUID userId, Integer limit, List<ItemFields> fields, Boolean enableImages, Boolean enableUserData, Integer imageTypeLimit, List<ImageType> enableImageTypes) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {};
        return getInstantMixFromMusicGenreByNameRequestCreation(name, userId, limit, fields, enableImages, enableUserData, imageTypeLimit, enableImageTypes).toEntity(localVarReturnType);
    }

    /**
     * Creates an instant playlist based on a given genre.
     * 
     * <p><b>200</b> - Instant playlist returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param name The genre name.
     * @param userId Optional. Filter by user id, and attach user data.
     * @param limit Optional. The maximum number of records to return.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param enableImages Optional. Include image information in output.
     * @param enableUserData Optional. Include user data.
     * @param imageTypeLimit Optional. The max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getInstantMixFromMusicGenreByNameWithResponseSpec(String name, UUID userId, Integer limit, List<ItemFields> fields, Boolean enableImages, Boolean enableUserData, Integer imageTypeLimit, List<ImageType> enableImageTypes) throws WebClientResponseException {
        return getInstantMixFromMusicGenreByNameRequestCreation(name, userId, limit, fields, enableImages, enableUserData, imageTypeLimit, enableImageTypes);
    }

    /**
     * Creates an instant playlist based on a given playlist.
     * 
     * <p><b>200</b> - Instant playlist returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param id The item id.
     * @param userId Optional. Filter by user id, and attach user data.
     * @param limit Optional. The maximum number of records to return.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param enableImages Optional. Include image information in output.
     * @param enableUserData Optional. Include user data.
     * @param imageTypeLimit Optional. The max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getInstantMixFromPlaylistRequestCreation(UUID id, UUID userId, Integer limit, List<ItemFields> fields, Boolean enableImages, Boolean enableUserData, Integer imageTypeLimit, List<ImageType> enableImageTypes) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'id' is set
        if (id == null) {
            throw new WebClientResponseException("Missing the required parameter 'id' when calling getInstantMixFromPlaylist", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("id", id);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "userId", userId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "limit", limit));
        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "fields", fields));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableImages", enableImages));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableUserData", enableUserData));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "imageTypeLimit", imageTypeLimit));
        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "enableImageTypes", enableImageTypes));
        
        final String[] localVarAccepts = { 
            "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {};
        return apiClient.invokeAPI("/Playlists/{id}/InstantMix", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Creates an instant playlist based on a given playlist.
     * 
     * <p><b>200</b> - Instant playlist returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param id The item id.
     * @param userId Optional. Filter by user id, and attach user data.
     * @param limit Optional. The maximum number of records to return.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param enableImages Optional. Include image information in output.
     * @param enableUserData Optional. Include user data.
     * @param imageTypeLimit Optional. The max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<BaseItemDtoQueryResult> getInstantMixFromPlaylist(UUID id, UUID userId, Integer limit, List<ItemFields> fields, Boolean enableImages, Boolean enableUserData, Integer imageTypeLimit, List<ImageType> enableImageTypes) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {};
        return getInstantMixFromPlaylistRequestCreation(id, userId, limit, fields, enableImages, enableUserData, imageTypeLimit, enableImageTypes).bodyToMono(localVarReturnType);
    }

    /**
     * Creates an instant playlist based on a given playlist.
     * 
     * <p><b>200</b> - Instant playlist returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param id The item id.
     * @param userId Optional. Filter by user id, and attach user data.
     * @param limit Optional. The maximum number of records to return.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param enableImages Optional. Include image information in output.
     * @param enableUserData Optional. Include user data.
     * @param imageTypeLimit Optional. The max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @return ResponseEntity&lt;BaseItemDtoQueryResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<BaseItemDtoQueryResult>> getInstantMixFromPlaylistWithHttpInfo(UUID id, UUID userId, Integer limit, List<ItemFields> fields, Boolean enableImages, Boolean enableUserData, Integer imageTypeLimit, List<ImageType> enableImageTypes) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {};
        return getInstantMixFromPlaylistRequestCreation(id, userId, limit, fields, enableImages, enableUserData, imageTypeLimit, enableImageTypes).toEntity(localVarReturnType);
    }

    /**
     * Creates an instant playlist based on a given playlist.
     * 
     * <p><b>200</b> - Instant playlist returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param id The item id.
     * @param userId Optional. Filter by user id, and attach user data.
     * @param limit Optional. The maximum number of records to return.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param enableImages Optional. Include image information in output.
     * @param enableUserData Optional. Include user data.
     * @param imageTypeLimit Optional. The max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getInstantMixFromPlaylistWithResponseSpec(UUID id, UUID userId, Integer limit, List<ItemFields> fields, Boolean enableImages, Boolean enableUserData, Integer imageTypeLimit, List<ImageType> enableImageTypes) throws WebClientResponseException {
        return getInstantMixFromPlaylistRequestCreation(id, userId, limit, fields, enableImages, enableUserData, imageTypeLimit, enableImageTypes);
    }

    /**
     * Creates an instant playlist based on a given song.
     * 
     * <p><b>200</b> - Instant playlist returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param id The item id.
     * @param userId Optional. Filter by user id, and attach user data.
     * @param limit Optional. The maximum number of records to return.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param enableImages Optional. Include image information in output.
     * @param enableUserData Optional. Include user data.
     * @param imageTypeLimit Optional. The max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getInstantMixFromSongRequestCreation(UUID id, UUID userId, Integer limit, List<ItemFields> fields, Boolean enableImages, Boolean enableUserData, Integer imageTypeLimit, List<ImageType> enableImageTypes) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'id' is set
        if (id == null) {
            throw new WebClientResponseException("Missing the required parameter 'id' when calling getInstantMixFromSong", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("id", id);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "userId", userId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "limit", limit));
        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "fields", fields));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableImages", enableImages));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableUserData", enableUserData));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "imageTypeLimit", imageTypeLimit));
        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "enableImageTypes", enableImageTypes));
        
        final String[] localVarAccepts = { 
            "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {};
        return apiClient.invokeAPI("/Songs/{id}/InstantMix", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Creates an instant playlist based on a given song.
     * 
     * <p><b>200</b> - Instant playlist returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param id The item id.
     * @param userId Optional. Filter by user id, and attach user data.
     * @param limit Optional. The maximum number of records to return.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param enableImages Optional. Include image information in output.
     * @param enableUserData Optional. Include user data.
     * @param imageTypeLimit Optional. The max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<BaseItemDtoQueryResult> getInstantMixFromSong(UUID id, UUID userId, Integer limit, List<ItemFields> fields, Boolean enableImages, Boolean enableUserData, Integer imageTypeLimit, List<ImageType> enableImageTypes) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {};
        return getInstantMixFromSongRequestCreation(id, userId, limit, fields, enableImages, enableUserData, imageTypeLimit, enableImageTypes).bodyToMono(localVarReturnType);
    }

    /**
     * Creates an instant playlist based on a given song.
     * 
     * <p><b>200</b> - Instant playlist returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param id The item id.
     * @param userId Optional. Filter by user id, and attach user data.
     * @param limit Optional. The maximum number of records to return.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param enableImages Optional. Include image information in output.
     * @param enableUserData Optional. Include user data.
     * @param imageTypeLimit Optional. The max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @return ResponseEntity&lt;BaseItemDtoQueryResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<BaseItemDtoQueryResult>> getInstantMixFromSongWithHttpInfo(UUID id, UUID userId, Integer limit, List<ItemFields> fields, Boolean enableImages, Boolean enableUserData, Integer imageTypeLimit, List<ImageType> enableImageTypes) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {};
        return getInstantMixFromSongRequestCreation(id, userId, limit, fields, enableImages, enableUserData, imageTypeLimit, enableImageTypes).toEntity(localVarReturnType);
    }

    /**
     * Creates an instant playlist based on a given song.
     * 
     * <p><b>200</b> - Instant playlist returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param id The item id.
     * @param userId Optional. Filter by user id, and attach user data.
     * @param limit Optional. The maximum number of records to return.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param enableImages Optional. Include image information in output.
     * @param enableUserData Optional. Include user data.
     * @param imageTypeLimit Optional. The max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getInstantMixFromSongWithResponseSpec(UUID id, UUID userId, Integer limit, List<ItemFields> fields, Boolean enableImages, Boolean enableUserData, Integer imageTypeLimit, List<ImageType> enableImageTypes) throws WebClientResponseException {
        return getInstantMixFromSongRequestCreation(id, userId, limit, fields, enableImages, enableUserData, imageTypeLimit, enableImageTypes);
    }
}
