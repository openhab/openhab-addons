package org.openhab.binding.jellyfin.internal.api.version.legacy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.openhab.binding.jellyfin.internal.api.version.ApiClient;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.ImageProviderInfo;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.ImageType;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.RemoteImageResult;
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
public class RemoteImageApi {
    private ApiClient apiClient;

    public RemoteImageApi() {
        this(new ApiClient());
    }

    @Autowired
    public RemoteImageApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Downloads a remote image for an item.
     * 
     * <p>
     * <b>204</b> - Remote image downloaded.
     * <p>
     * <b>404</b> - Remote image not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId Item Id.
     * @param type The image type.
     * @param imageUrl The image url.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec downloadRemoteImageRequestCreation(UUID itemId, ImageType type, String imageUrl)
            throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'itemId' when calling downloadRemoteImage",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'type' is set
        if (type == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'type' when calling downloadRemoteImage",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("itemId", itemId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "type", type));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "imageUrl", imageUrl));

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return apiClient.invokeAPI("/Items/{itemId}/RemoteImages/Download", HttpMethod.POST, pathParams, queryParams,
                postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType,
                localVarAuthNames, localVarReturnType);
    }

    /**
     * Downloads a remote image for an item.
     * 
     * <p>
     * <b>204</b> - Remote image downloaded.
     * <p>
     * <b>404</b> - Remote image not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId Item Id.
     * @param type The image type.
     * @param imageUrl The image url.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> downloadRemoteImage(UUID itemId, ImageType type, String imageUrl)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return downloadRemoteImageRequestCreation(itemId, type, imageUrl).bodyToMono(localVarReturnType);
    }

    /**
     * Downloads a remote image for an item.
     * 
     * <p>
     * <b>204</b> - Remote image downloaded.
     * <p>
     * <b>404</b> - Remote image not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId Item Id.
     * @param type The image type.
     * @param imageUrl The image url.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> downloadRemoteImageWithHttpInfo(UUID itemId, ImageType type, String imageUrl)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return downloadRemoteImageRequestCreation(itemId, type, imageUrl).toEntity(localVarReturnType);
    }

    /**
     * Downloads a remote image for an item.
     * 
     * <p>
     * <b>204</b> - Remote image downloaded.
     * <p>
     * <b>404</b> - Remote image not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId Item Id.
     * @param type The image type.
     * @param imageUrl The image url.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec downloadRemoteImageWithResponseSpec(UUID itemId, ImageType type, String imageUrl)
            throws WebClientResponseException {
        return downloadRemoteImageRequestCreation(itemId, type, imageUrl);
    }

    /**
     * Gets available remote image providers for an item.
     * 
     * <p>
     * <b>200</b> - Returned remote image providers.
     * <p>
     * <b>404</b> - Item not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId Item Id.
     * @return List&lt;ImageProviderInfo&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getRemoteImageProvidersRequestCreation(UUID itemId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'itemId' when calling getRemoteImageProviders",
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

        ParameterizedTypeReference<ImageProviderInfo> localVarReturnType = new ParameterizedTypeReference<ImageProviderInfo>() {
        };
        return apiClient.invokeAPI("/Items/{itemId}/RemoteImages/Providers", HttpMethod.GET, pathParams, queryParams,
                postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType,
                localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets available remote image providers for an item.
     * 
     * <p>
     * <b>200</b> - Returned remote image providers.
     * <p>
     * <b>404</b> - Item not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId Item Id.
     * @return List&lt;ImageProviderInfo&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Flux<ImageProviderInfo> getRemoteImageProviders(UUID itemId) throws WebClientResponseException {
        ParameterizedTypeReference<ImageProviderInfo> localVarReturnType = new ParameterizedTypeReference<ImageProviderInfo>() {
        };
        return getRemoteImageProvidersRequestCreation(itemId).bodyToFlux(localVarReturnType);
    }

    /**
     * Gets available remote image providers for an item.
     * 
     * <p>
     * <b>200</b> - Returned remote image providers.
     * <p>
     * <b>404</b> - Item not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId Item Id.
     * @return ResponseEntity&lt;List&lt;ImageProviderInfo&gt;&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<List<ImageProviderInfo>>> getRemoteImageProvidersWithHttpInfo(UUID itemId)
            throws WebClientResponseException {
        ParameterizedTypeReference<ImageProviderInfo> localVarReturnType = new ParameterizedTypeReference<ImageProviderInfo>() {
        };
        return getRemoteImageProvidersRequestCreation(itemId).toEntityList(localVarReturnType);
    }

    /**
     * Gets available remote image providers for an item.
     * 
     * <p>
     * <b>200</b> - Returned remote image providers.
     * <p>
     * <b>404</b> - Item not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId Item Id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getRemoteImageProvidersWithResponseSpec(UUID itemId) throws WebClientResponseException {
        return getRemoteImageProvidersRequestCreation(itemId);
    }

    /**
     * Gets available remote images for an item.
     * 
     * <p>
     * <b>200</b> - Remote Images returned.
     * <p>
     * <b>404</b> - Item not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId Item Id.
     * @param type The image type.
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results.
     * @param limit Optional. The maximum number of records to return.
     * @param providerName Optional. The image provider to use.
     * @param includeAllLanguages Optional. Include all languages.
     * @return RemoteImageResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getRemoteImagesRequestCreation(UUID itemId, ImageType type, Integer startIndex, Integer limit,
            String providerName, Boolean includeAllLanguages) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException("Missing the required parameter 'itemId' when calling getRemoteImages",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("itemId", itemId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "type", type));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "startIndex", startIndex));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "limit", limit));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "providerName", providerName));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "includeAllLanguages", includeAllLanguages));

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<RemoteImageResult> localVarReturnType = new ParameterizedTypeReference<RemoteImageResult>() {
        };
        return apiClient.invokeAPI("/Items/{itemId}/RemoteImages", HttpMethod.GET, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Gets available remote images for an item.
     * 
     * <p>
     * <b>200</b> - Remote Images returned.
     * <p>
     * <b>404</b> - Item not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId Item Id.
     * @param type The image type.
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results.
     * @param limit Optional. The maximum number of records to return.
     * @param providerName Optional. The image provider to use.
     * @param includeAllLanguages Optional. Include all languages.
     * @return RemoteImageResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<RemoteImageResult> getRemoteImages(UUID itemId, ImageType type, Integer startIndex, Integer limit,
            String providerName, Boolean includeAllLanguages) throws WebClientResponseException {
        ParameterizedTypeReference<RemoteImageResult> localVarReturnType = new ParameterizedTypeReference<RemoteImageResult>() {
        };
        return getRemoteImagesRequestCreation(itemId, type, startIndex, limit, providerName, includeAllLanguages)
                .bodyToMono(localVarReturnType);
    }

    /**
     * Gets available remote images for an item.
     * 
     * <p>
     * <b>200</b> - Remote Images returned.
     * <p>
     * <b>404</b> - Item not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId Item Id.
     * @param type The image type.
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results.
     * @param limit Optional. The maximum number of records to return.
     * @param providerName Optional. The image provider to use.
     * @param includeAllLanguages Optional. Include all languages.
     * @return ResponseEntity&lt;RemoteImageResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<RemoteImageResult>> getRemoteImagesWithHttpInfo(UUID itemId, ImageType type,
            Integer startIndex, Integer limit, String providerName, Boolean includeAllLanguages)
            throws WebClientResponseException {
        ParameterizedTypeReference<RemoteImageResult> localVarReturnType = new ParameterizedTypeReference<RemoteImageResult>() {
        };
        return getRemoteImagesRequestCreation(itemId, type, startIndex, limit, providerName, includeAllLanguages)
                .toEntity(localVarReturnType);
    }

    /**
     * Gets available remote images for an item.
     * 
     * <p>
     * <b>200</b> - Remote Images returned.
     * <p>
     * <b>404</b> - Item not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId Item Id.
     * @param type The image type.
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results.
     * @param limit Optional. The maximum number of records to return.
     * @param providerName Optional. The image provider to use.
     * @param includeAllLanguages Optional. Include all languages.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getRemoteImagesWithResponseSpec(UUID itemId, ImageType type, Integer startIndex, Integer limit,
            String providerName, Boolean includeAllLanguages) throws WebClientResponseException {
        return getRemoteImagesRequestCreation(itemId, type, startIndex, limit, providerName, includeAllLanguages);
    }
}
