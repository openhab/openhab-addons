package org.openhab.binding.jellyfin.internal.api.version.current;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.openhab.binding.jellyfin.internal.api.version.ApiClient;
import org.openhab.binding.jellyfin.internal.api.version.current.model.BaseItemDto;
import org.openhab.binding.jellyfin.internal.api.version.current.model.MetadataEditorInfo;
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
public class ItemUpdateApi {
    private ApiClient apiClient;

    public ItemUpdateApi() {
        this(new ApiClient());
    }

    @Autowired
    public ItemUpdateApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Gets metadata editor info for an item.
     * 
     * <p>
     * <b>200</b> - Item metadata editor returned.
     * <p>
     * <b>404</b> - Item not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId The item id.
     * @return MetadataEditorInfo
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getMetadataEditorInfoRequestCreation(UUID itemId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'itemId' when calling getMetadataEditorInfo",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("itemId", itemId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<MetadataEditorInfo> localVarReturnType = new ParameterizedTypeReference<MetadataEditorInfo>() {
        };
        return apiClient.invokeAPI("/Items/{itemId}/MetadataEditor", HttpMethod.GET, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Gets metadata editor info for an item.
     * 
     * <p>
     * <b>200</b> - Item metadata editor returned.
     * <p>
     * <b>404</b> - Item not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId The item id.
     * @return MetadataEditorInfo
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<MetadataEditorInfo> getMetadataEditorInfo(UUID itemId) throws WebClientResponseException {
        ParameterizedTypeReference<MetadataEditorInfo> localVarReturnType = new ParameterizedTypeReference<MetadataEditorInfo>() {
        };
        return getMetadataEditorInfoRequestCreation(itemId).bodyToMono(localVarReturnType);
    }

    /**
     * Gets metadata editor info for an item.
     * 
     * <p>
     * <b>200</b> - Item metadata editor returned.
     * <p>
     * <b>404</b> - Item not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId The item id.
     * @return ResponseEntity&lt;MetadataEditorInfo&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<MetadataEditorInfo>> getMetadataEditorInfoWithHttpInfo(UUID itemId)
            throws WebClientResponseException {
        ParameterizedTypeReference<MetadataEditorInfo> localVarReturnType = new ParameterizedTypeReference<MetadataEditorInfo>() {
        };
        return getMetadataEditorInfoRequestCreation(itemId).toEntity(localVarReturnType);
    }

    /**
     * Gets metadata editor info for an item.
     * 
     * <p>
     * <b>200</b> - Item metadata editor returned.
     * <p>
     * <b>404</b> - Item not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId The item id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getMetadataEditorInfoWithResponseSpec(UUID itemId) throws WebClientResponseException {
        return getMetadataEditorInfoRequestCreation(itemId);
    }

    /**
     * Updates an item.
     * 
     * <p>
     * <b>204</b> - Item updated.
     * <p>
     * <b>404</b> - Item not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId The item id.
     * @param baseItemDto The new item properties.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec updateItemRequestCreation(UUID itemId, BaseItemDto baseItemDto)
            throws WebClientResponseException {
        Object postBody = baseItemDto;
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException("Missing the required parameter 'itemId' when calling updateItem",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'baseItemDto' is set
        if (baseItemDto == null) {
            throw new WebClientResponseException("Missing the required parameter 'baseItemDto' when calling updateItem",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("itemId", itemId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { "application/json", "text/json", "application/*+json" };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return apiClient.invokeAPI("/Items/{itemId}", HttpMethod.POST, pathParams, queryParams, postBody, headerParams,
                cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Updates an item.
     * 
     * <p>
     * <b>204</b> - Item updated.
     * <p>
     * <b>404</b> - Item not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId The item id.
     * @param baseItemDto The new item properties.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> updateItem(UUID itemId, BaseItemDto baseItemDto) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return updateItemRequestCreation(itemId, baseItemDto).bodyToMono(localVarReturnType);
    }

    /**
     * Updates an item.
     * 
     * <p>
     * <b>204</b> - Item updated.
     * <p>
     * <b>404</b> - Item not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId The item id.
     * @param baseItemDto The new item properties.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> updateItemWithHttpInfo(UUID itemId, BaseItemDto baseItemDto)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return updateItemRequestCreation(itemId, baseItemDto).toEntity(localVarReturnType);
    }

    /**
     * Updates an item.
     * 
     * <p>
     * <b>204</b> - Item updated.
     * <p>
     * <b>404</b> - Item not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId The item id.
     * @param baseItemDto The new item properties.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec updateItemWithResponseSpec(UUID itemId, BaseItemDto baseItemDto)
            throws WebClientResponseException {
        return updateItemRequestCreation(itemId, baseItemDto);
    }

    /**
     * Updates an item&#39;s content type.
     * 
     * <p>
     * <b>204</b> - Item content type updated.
     * <p>
     * <b>404</b> - Item not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId The item id.
     * @param contentType The content type of the item.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec updateItemContentTypeRequestCreation(UUID itemId, String contentType)
            throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'itemId' when calling updateItemContentType",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("itemId", itemId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "contentType", contentType));

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return apiClient.invokeAPI("/Items/{itemId}/ContentType", HttpMethod.POST, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Updates an item&#39;s content type.
     * 
     * <p>
     * <b>204</b> - Item content type updated.
     * <p>
     * <b>404</b> - Item not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId The item id.
     * @param contentType The content type of the item.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> updateItemContentType(UUID itemId, String contentType) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return updateItemContentTypeRequestCreation(itemId, contentType).bodyToMono(localVarReturnType);
    }

    /**
     * Updates an item&#39;s content type.
     * 
     * <p>
     * <b>204</b> - Item content type updated.
     * <p>
     * <b>404</b> - Item not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId The item id.
     * @param contentType The content type of the item.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> updateItemContentTypeWithHttpInfo(UUID itemId, String contentType)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return updateItemContentTypeRequestCreation(itemId, contentType).toEntity(localVarReturnType);
    }

    /**
     * Updates an item&#39;s content type.
     * 
     * <p>
     * <b>204</b> - Item content type updated.
     * <p>
     * <b>404</b> - Item not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId The item id.
     * @param contentType The content type of the item.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec updateItemContentTypeWithResponseSpec(UUID itemId, String contentType)
            throws WebClientResponseException {
        return updateItemContentTypeRequestCreation(itemId, contentType);
    }
}
