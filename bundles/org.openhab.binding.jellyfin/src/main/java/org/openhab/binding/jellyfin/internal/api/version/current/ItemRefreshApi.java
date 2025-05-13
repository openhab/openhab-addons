package org.openhab.binding.jellyfin.internal.api.version.current;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.openhab.binding.jellyfin.internal.api.version.ApiClient;
import org.openhab.binding.jellyfin.internal.api.version.current.model.MetadataRefreshMode;
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
public class ItemRefreshApi {
    private ApiClient apiClient;

    public ItemRefreshApi() {
        this(new ApiClient());
    }

    @Autowired
    public ItemRefreshApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Refreshes metadata for an item.
     * 
     * <p>
     * <b>204</b> - Item metadata refresh queued.
     * <p>
     * <b>404</b> - Item to refresh not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId Item id.
     * @param metadataRefreshMode (Optional) Specifies the metadata refresh mode.
     * @param imageRefreshMode (Optional) Specifies the image refresh mode.
     * @param replaceAllMetadata (Optional) Determines if metadata should be replaced. Only applicable if mode is
     *            FullRefresh.
     * @param replaceAllImages (Optional) Determines if images should be replaced. Only applicable if mode is
     *            FullRefresh.
     * @param regenerateTrickplay (Optional) Determines if trickplay images should be replaced. Only applicable if mode
     *            is FullRefresh.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec refreshItemRequestCreation(UUID itemId, MetadataRefreshMode metadataRefreshMode,
            MetadataRefreshMode imageRefreshMode, Boolean replaceAllMetadata, Boolean replaceAllImages,
            Boolean regenerateTrickplay) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException("Missing the required parameter 'itemId' when calling refreshItem",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("itemId", itemId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "metadataRefreshMode", metadataRefreshMode));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "imageRefreshMode", imageRefreshMode));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "replaceAllMetadata", replaceAllMetadata));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "replaceAllImages", replaceAllImages));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "regenerateTrickplay", regenerateTrickplay));

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return apiClient.invokeAPI("/Items/{itemId}/Refresh", HttpMethod.POST, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Refreshes metadata for an item.
     * 
     * <p>
     * <b>204</b> - Item metadata refresh queued.
     * <p>
     * <b>404</b> - Item to refresh not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId Item id.
     * @param metadataRefreshMode (Optional) Specifies the metadata refresh mode.
     * @param imageRefreshMode (Optional) Specifies the image refresh mode.
     * @param replaceAllMetadata (Optional) Determines if metadata should be replaced. Only applicable if mode is
     *            FullRefresh.
     * @param replaceAllImages (Optional) Determines if images should be replaced. Only applicable if mode is
     *            FullRefresh.
     * @param regenerateTrickplay (Optional) Determines if trickplay images should be replaced. Only applicable if mode
     *            is FullRefresh.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> refreshItem(UUID itemId, MetadataRefreshMode metadataRefreshMode,
            MetadataRefreshMode imageRefreshMode, Boolean replaceAllMetadata, Boolean replaceAllImages,
            Boolean regenerateTrickplay) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return refreshItemRequestCreation(itemId, metadataRefreshMode, imageRefreshMode, replaceAllMetadata,
                replaceAllImages, regenerateTrickplay).bodyToMono(localVarReturnType);
    }

    /**
     * Refreshes metadata for an item.
     * 
     * <p>
     * <b>204</b> - Item metadata refresh queued.
     * <p>
     * <b>404</b> - Item to refresh not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId Item id.
     * @param metadataRefreshMode (Optional) Specifies the metadata refresh mode.
     * @param imageRefreshMode (Optional) Specifies the image refresh mode.
     * @param replaceAllMetadata (Optional) Determines if metadata should be replaced. Only applicable if mode is
     *            FullRefresh.
     * @param replaceAllImages (Optional) Determines if images should be replaced. Only applicable if mode is
     *            FullRefresh.
     * @param regenerateTrickplay (Optional) Determines if trickplay images should be replaced. Only applicable if mode
     *            is FullRefresh.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> refreshItemWithHttpInfo(UUID itemId, MetadataRefreshMode metadataRefreshMode,
            MetadataRefreshMode imageRefreshMode, Boolean replaceAllMetadata, Boolean replaceAllImages,
            Boolean regenerateTrickplay) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return refreshItemRequestCreation(itemId, metadataRefreshMode, imageRefreshMode, replaceAllMetadata,
                replaceAllImages, regenerateTrickplay).toEntity(localVarReturnType);
    }

    /**
     * Refreshes metadata for an item.
     * 
     * <p>
     * <b>204</b> - Item metadata refresh queued.
     * <p>
     * <b>404</b> - Item to refresh not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId Item id.
     * @param metadataRefreshMode (Optional) Specifies the metadata refresh mode.
     * @param imageRefreshMode (Optional) Specifies the image refresh mode.
     * @param replaceAllMetadata (Optional) Determines if metadata should be replaced. Only applicable if mode is
     *            FullRefresh.
     * @param replaceAllImages (Optional) Determines if images should be replaced. Only applicable if mode is
     *            FullRefresh.
     * @param regenerateTrickplay (Optional) Determines if trickplay images should be replaced. Only applicable if mode
     *            is FullRefresh.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec refreshItemWithResponseSpec(UUID itemId, MetadataRefreshMode metadataRefreshMode,
            MetadataRefreshMode imageRefreshMode, Boolean replaceAllMetadata, Boolean replaceAllImages,
            Boolean regenerateTrickplay) throws WebClientResponseException {
        return refreshItemRequestCreation(itemId, metadataRefreshMode, imageRefreshMode, replaceAllMetadata,
                replaceAllImages, regenerateTrickplay);
    }
}
