package org.openhab.binding.jellyfin.internal.api.version.legacy;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.openhab.binding.jellyfin.internal.api.version.ApiClient;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.CollectionCreationResult;
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

import reactor.core.publisher.Mono;

@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.12.0")
public class CollectionApi {
    private ApiClient apiClient;

    public CollectionApi() {
        this(new ApiClient());
    }

    @Autowired
    public CollectionApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Adds items to a collection.
     * 
     * <p>
     * <b>204</b> - Items added to collection.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param collectionId The collection id.
     * @param ids Item ids, comma delimited.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec addToCollectionRequestCreation(UUID collectionId, List<UUID> ids)
            throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'collectionId' is set
        if (collectionId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'collectionId' when calling addToCollection",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'ids' is set
        if (ids == null) {
            throw new WebClientResponseException("Missing the required parameter 'ids' when calling addToCollection",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("collectionId", collectionId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "ids", ids));

        final String[] localVarAccepts = {};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return apiClient.invokeAPI("/Collections/{collectionId}/Items", HttpMethod.POST, pathParams, queryParams,
                postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType,
                localVarAuthNames, localVarReturnType);
    }

    /**
     * Adds items to a collection.
     * 
     * <p>
     * <b>204</b> - Items added to collection.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param collectionId The collection id.
     * @param ids Item ids, comma delimited.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> addToCollection(UUID collectionId, List<UUID> ids) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return addToCollectionRequestCreation(collectionId, ids).bodyToMono(localVarReturnType);
    }

    /**
     * Adds items to a collection.
     * 
     * <p>
     * <b>204</b> - Items added to collection.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param collectionId The collection id.
     * @param ids Item ids, comma delimited.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> addToCollectionWithHttpInfo(UUID collectionId, List<UUID> ids)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return addToCollectionRequestCreation(collectionId, ids).toEntity(localVarReturnType);
    }

    /**
     * Adds items to a collection.
     * 
     * <p>
     * <b>204</b> - Items added to collection.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param collectionId The collection id.
     * @param ids Item ids, comma delimited.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec addToCollectionWithResponseSpec(UUID collectionId, List<UUID> ids)
            throws WebClientResponseException {
        return addToCollectionRequestCreation(collectionId, ids);
    }

    /**
     * Creates a new collection.
     * 
     * <p>
     * <b>200</b> - Collection created.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param name The name of the collection.
     * @param ids Item Ids to add to the collection.
     * @param parentId Optional. Create the collection within a specific folder.
     * @param isLocked Whether or not to lock the new collection.
     * @return CollectionCreationResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec createCollectionRequestCreation(String name, List<String> ids, UUID parentId, Boolean isLocked)
            throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "name", name));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "ids", ids));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "parentId", parentId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isLocked", isLocked));

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<CollectionCreationResult> localVarReturnType = new ParameterizedTypeReference<CollectionCreationResult>() {
        };
        return apiClient.invokeAPI("/Collections", HttpMethod.POST, pathParams, queryParams, postBody, headerParams,
                cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Creates a new collection.
     * 
     * <p>
     * <b>200</b> - Collection created.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param name The name of the collection.
     * @param ids Item Ids to add to the collection.
     * @param parentId Optional. Create the collection within a specific folder.
     * @param isLocked Whether or not to lock the new collection.
     * @return CollectionCreationResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<CollectionCreationResult> createCollection(String name, List<String> ids, UUID parentId,
            Boolean isLocked) throws WebClientResponseException {
        ParameterizedTypeReference<CollectionCreationResult> localVarReturnType = new ParameterizedTypeReference<CollectionCreationResult>() {
        };
        return createCollectionRequestCreation(name, ids, parentId, isLocked).bodyToMono(localVarReturnType);
    }

    /**
     * Creates a new collection.
     * 
     * <p>
     * <b>200</b> - Collection created.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param name The name of the collection.
     * @param ids Item Ids to add to the collection.
     * @param parentId Optional. Create the collection within a specific folder.
     * @param isLocked Whether or not to lock the new collection.
     * @return ResponseEntity&lt;CollectionCreationResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<CollectionCreationResult>> createCollectionWithHttpInfo(String name, List<String> ids,
            UUID parentId, Boolean isLocked) throws WebClientResponseException {
        ParameterizedTypeReference<CollectionCreationResult> localVarReturnType = new ParameterizedTypeReference<CollectionCreationResult>() {
        };
        return createCollectionRequestCreation(name, ids, parentId, isLocked).toEntity(localVarReturnType);
    }

    /**
     * Creates a new collection.
     * 
     * <p>
     * <b>200</b> - Collection created.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param name The name of the collection.
     * @param ids Item Ids to add to the collection.
     * @param parentId Optional. Create the collection within a specific folder.
     * @param isLocked Whether or not to lock the new collection.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec createCollectionWithResponseSpec(String name, List<String> ids, UUID parentId, Boolean isLocked)
            throws WebClientResponseException {
        return createCollectionRequestCreation(name, ids, parentId, isLocked);
    }

    /**
     * Removes items from a collection.
     * 
     * <p>
     * <b>204</b> - Items removed from collection.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param collectionId The collection id.
     * @param ids Item ids, comma delimited.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec removeFromCollectionRequestCreation(UUID collectionId, List<UUID> ids)
            throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'collectionId' is set
        if (collectionId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'collectionId' when calling removeFromCollection",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'ids' is set
        if (ids == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'ids' when calling removeFromCollection",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("collectionId", collectionId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "ids", ids));

        final String[] localVarAccepts = {};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return apiClient.invokeAPI("/Collections/{collectionId}/Items", HttpMethod.DELETE, pathParams, queryParams,
                postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType,
                localVarAuthNames, localVarReturnType);
    }

    /**
     * Removes items from a collection.
     * 
     * <p>
     * <b>204</b> - Items removed from collection.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param collectionId The collection id.
     * @param ids Item ids, comma delimited.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> removeFromCollection(UUID collectionId, List<UUID> ids) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return removeFromCollectionRequestCreation(collectionId, ids).bodyToMono(localVarReturnType);
    }

    /**
     * Removes items from a collection.
     * 
     * <p>
     * <b>204</b> - Items removed from collection.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param collectionId The collection id.
     * @param ids Item ids, comma delimited.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> removeFromCollectionWithHttpInfo(UUID collectionId, List<UUID> ids)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return removeFromCollectionRequestCreation(collectionId, ids).toEntity(localVarReturnType);
    }

    /**
     * Removes items from a collection.
     * 
     * <p>
     * <b>204</b> - Items removed from collection.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param collectionId The collection id.
     * @param ids Item ids, comma delimited.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec removeFromCollectionWithResponseSpec(UUID collectionId, List<UUID> ids)
            throws WebClientResponseException {
        return removeFromCollectionRequestCreation(collectionId, ids);
    }
}
