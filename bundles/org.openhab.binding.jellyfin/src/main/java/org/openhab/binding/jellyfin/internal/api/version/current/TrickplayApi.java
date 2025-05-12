package org.openhab.binding.jellyfin.internal.api.version.current;

import org.openhab.binding.jellyfin.internal.api.version.ApiClient;

import java.io.File;
import org.openhab.binding.jellyfin.internal.api.version.current.model.ProblemDetails;
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
public class TrickplayApi {
    private ApiClient apiClient;

    public TrickplayApi() {
        this(new ApiClient());
    }

    @Autowired
    public TrickplayApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    
    /**
     * Gets an image tiles playlist for trickplay.
     * 
     * <p><b>200</b> - Tiles playlist returned.
     * <p><b>404</b> - Not Found
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId The item id.
     * @param width The width of a single tile.
     * @param mediaSourceId The media version id, if using an alternate version.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getTrickplayHlsPlaylistRequestCreation(UUID itemId, Integer width, UUID mediaSourceId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException("Missing the required parameter 'itemId' when calling getTrickplayHlsPlaylist", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'width' is set
        if (width == null) {
            throw new WebClientResponseException("Missing the required parameter 'width' when calling getTrickplayHlsPlaylist", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("itemId", itemId);
        pathParams.put("width", width);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "mediaSourceId", mediaSourceId));
        
        final String[] localVarAccepts = { 
            "application/x-mpegURL", "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return apiClient.invokeAPI("/Videos/{itemId}/Trickplay/{width}/tiles.m3u8", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets an image tiles playlist for trickplay.
     * 
     * <p><b>200</b> - Tiles playlist returned.
     * <p><b>404</b> - Not Found
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId The item id.
     * @param width The width of a single tile.
     * @param mediaSourceId The media version id, if using an alternate version.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<File> getTrickplayHlsPlaylist(UUID itemId, Integer width, UUID mediaSourceId) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return getTrickplayHlsPlaylistRequestCreation(itemId, width, mediaSourceId).bodyToMono(localVarReturnType);
    }

    /**
     * Gets an image tiles playlist for trickplay.
     * 
     * <p><b>200</b> - Tiles playlist returned.
     * <p><b>404</b> - Not Found
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId The item id.
     * @param width The width of a single tile.
     * @param mediaSourceId The media version id, if using an alternate version.
     * @return ResponseEntity&lt;File&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<File>> getTrickplayHlsPlaylistWithHttpInfo(UUID itemId, Integer width, UUID mediaSourceId) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return getTrickplayHlsPlaylistRequestCreation(itemId, width, mediaSourceId).toEntity(localVarReturnType);
    }

    /**
     * Gets an image tiles playlist for trickplay.
     * 
     * <p><b>200</b> - Tiles playlist returned.
     * <p><b>404</b> - Not Found
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId The item id.
     * @param width The width of a single tile.
     * @param mediaSourceId The media version id, if using an alternate version.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getTrickplayHlsPlaylistWithResponseSpec(UUID itemId, Integer width, UUID mediaSourceId) throws WebClientResponseException {
        return getTrickplayHlsPlaylistRequestCreation(itemId, width, mediaSourceId);
    }

    /**
     * Gets a trickplay tile image.
     * 
     * <p><b>200</b> - Tile image not found at specified index.
     * <p><b>404</b> - Not Found
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId The item id.
     * @param width The width of a single tile.
     * @param index The index of the desired tile.
     * @param mediaSourceId The media version id, if using an alternate version.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getTrickplayTileImageRequestCreation(UUID itemId, Integer width, Integer index, UUID mediaSourceId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException("Missing the required parameter 'itemId' when calling getTrickplayTileImage", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'width' is set
        if (width == null) {
            throw new WebClientResponseException("Missing the required parameter 'width' when calling getTrickplayTileImage", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'index' is set
        if (index == null) {
            throw new WebClientResponseException("Missing the required parameter 'index' when calling getTrickplayTileImage", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("itemId", itemId);
        pathParams.put("width", width);
        pathParams.put("index", index);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "mediaSourceId", mediaSourceId));
        
        final String[] localVarAccepts = { 
            "image/*", "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return apiClient.invokeAPI("/Videos/{itemId}/Trickplay/{width}/{index}.jpg", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets a trickplay tile image.
     * 
     * <p><b>200</b> - Tile image not found at specified index.
     * <p><b>404</b> - Not Found
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId The item id.
     * @param width The width of a single tile.
     * @param index The index of the desired tile.
     * @param mediaSourceId The media version id, if using an alternate version.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<File> getTrickplayTileImage(UUID itemId, Integer width, Integer index, UUID mediaSourceId) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return getTrickplayTileImageRequestCreation(itemId, width, index, mediaSourceId).bodyToMono(localVarReturnType);
    }

    /**
     * Gets a trickplay tile image.
     * 
     * <p><b>200</b> - Tile image not found at specified index.
     * <p><b>404</b> - Not Found
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId The item id.
     * @param width The width of a single tile.
     * @param index The index of the desired tile.
     * @param mediaSourceId The media version id, if using an alternate version.
     * @return ResponseEntity&lt;File&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<File>> getTrickplayTileImageWithHttpInfo(UUID itemId, Integer width, Integer index, UUID mediaSourceId) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return getTrickplayTileImageRequestCreation(itemId, width, index, mediaSourceId).toEntity(localVarReturnType);
    }

    /**
     * Gets a trickplay tile image.
     * 
     * <p><b>200</b> - Tile image not found at specified index.
     * <p><b>404</b> - Not Found
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId The item id.
     * @param width The width of a single tile.
     * @param index The index of the desired tile.
     * @param mediaSourceId The media version id, if using an alternate version.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getTrickplayTileImageWithResponseSpec(UUID itemId, Integer width, Integer index, UUID mediaSourceId) throws WebClientResponseException {
        return getTrickplayTileImageRequestCreation(itemId, width, index, mediaSourceId);
    }
}
