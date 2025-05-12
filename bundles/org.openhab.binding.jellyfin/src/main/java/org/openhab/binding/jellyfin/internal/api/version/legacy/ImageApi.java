package org.openhab.binding.jellyfin.internal.api.version.legacy;

import org.openhab.binding.jellyfin.internal.api.version.ApiClient;

import java.io.File;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.ImageFormat;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.ImageInfo;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.ImageType;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.ProblemDetails;
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
public class ImageApi {
    private ApiClient apiClient;

    public ImageApi() {
        this(new ApiClient());
    }

    @Autowired
    public ImageApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    
    /**
     * Delete a custom splashscreen.
     * 
     * <p><b>204</b> - Successfully deleted the custom splashscreen.
     * <p><b>403</b> - User does not have permission to delete splashscreen..
     * <p><b>401</b> - Unauthorized
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec deleteCustomSplashscreenRequestCreation() throws WebClientResponseException {
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
        return apiClient.invokeAPI("/Branding/Splashscreen", HttpMethod.DELETE, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Delete a custom splashscreen.
     * 
     * <p><b>204</b> - Successfully deleted the custom splashscreen.
     * <p><b>403</b> - User does not have permission to delete splashscreen..
     * <p><b>401</b> - Unauthorized
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> deleteCustomSplashscreen() throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return deleteCustomSplashscreenRequestCreation().bodyToMono(localVarReturnType);
    }

    /**
     * Delete a custom splashscreen.
     * 
     * <p><b>204</b> - Successfully deleted the custom splashscreen.
     * <p><b>403</b> - User does not have permission to delete splashscreen..
     * <p><b>401</b> - Unauthorized
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> deleteCustomSplashscreenWithHttpInfo() throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return deleteCustomSplashscreenRequestCreation().toEntity(localVarReturnType);
    }

    /**
     * Delete a custom splashscreen.
     * 
     * <p><b>204</b> - Successfully deleted the custom splashscreen.
     * <p><b>403</b> - User does not have permission to delete splashscreen..
     * <p><b>401</b> - Unauthorized
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec deleteCustomSplashscreenWithResponseSpec() throws WebClientResponseException {
        return deleteCustomSplashscreenRequestCreation();
    }

    /**
     * Delete an item&#39;s image.
     * 
     * <p><b>204</b> - Image deleted.
     * <p><b>404</b> - Item not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId Item id.
     * @param imageType Image type.
     * @param imageIndex The image index.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec deleteItemImageRequestCreation(UUID itemId, ImageType imageType, Integer imageIndex) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException("Missing the required parameter 'itemId' when calling deleteItemImage", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'imageType' is set
        if (imageType == null) {
            throw new WebClientResponseException("Missing the required parameter 'imageType' when calling deleteItemImage", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("itemId", itemId);
        pathParams.put("imageType", imageType);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "imageIndex", imageIndex));
        
        final String[] localVarAccepts = { 
            "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return apiClient.invokeAPI("/Items/{itemId}/Images/{imageType}", HttpMethod.DELETE, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Delete an item&#39;s image.
     * 
     * <p><b>204</b> - Image deleted.
     * <p><b>404</b> - Item not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId Item id.
     * @param imageType Image type.
     * @param imageIndex The image index.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> deleteItemImage(UUID itemId, ImageType imageType, Integer imageIndex) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return deleteItemImageRequestCreation(itemId, imageType, imageIndex).bodyToMono(localVarReturnType);
    }

    /**
     * Delete an item&#39;s image.
     * 
     * <p><b>204</b> - Image deleted.
     * <p><b>404</b> - Item not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId Item id.
     * @param imageType Image type.
     * @param imageIndex The image index.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> deleteItemImageWithHttpInfo(UUID itemId, ImageType imageType, Integer imageIndex) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return deleteItemImageRequestCreation(itemId, imageType, imageIndex).toEntity(localVarReturnType);
    }

    /**
     * Delete an item&#39;s image.
     * 
     * <p><b>204</b> - Image deleted.
     * <p><b>404</b> - Item not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId Item id.
     * @param imageType Image type.
     * @param imageIndex The image index.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec deleteItemImageWithResponseSpec(UUID itemId, ImageType imageType, Integer imageIndex) throws WebClientResponseException {
        return deleteItemImageRequestCreation(itemId, imageType, imageIndex);
    }

    /**
     * Delete an item&#39;s image.
     * 
     * <p><b>204</b> - Image deleted.
     * <p><b>404</b> - Item not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId Item id.
     * @param imageType Image type.
     * @param imageIndex The image index.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec deleteItemImageByIndexRequestCreation(UUID itemId, ImageType imageType, Integer imageIndex) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException("Missing the required parameter 'itemId' when calling deleteItemImageByIndex", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'imageType' is set
        if (imageType == null) {
            throw new WebClientResponseException("Missing the required parameter 'imageType' when calling deleteItemImageByIndex", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'imageIndex' is set
        if (imageIndex == null) {
            throw new WebClientResponseException("Missing the required parameter 'imageIndex' when calling deleteItemImageByIndex", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("itemId", itemId);
        pathParams.put("imageType", imageType);
        pathParams.put("imageIndex", imageIndex);

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
        return apiClient.invokeAPI("/Items/{itemId}/Images/{imageType}/{imageIndex}", HttpMethod.DELETE, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Delete an item&#39;s image.
     * 
     * <p><b>204</b> - Image deleted.
     * <p><b>404</b> - Item not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId Item id.
     * @param imageType Image type.
     * @param imageIndex The image index.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> deleteItemImageByIndex(UUID itemId, ImageType imageType, Integer imageIndex) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return deleteItemImageByIndexRequestCreation(itemId, imageType, imageIndex).bodyToMono(localVarReturnType);
    }

    /**
     * Delete an item&#39;s image.
     * 
     * <p><b>204</b> - Image deleted.
     * <p><b>404</b> - Item not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId Item id.
     * @param imageType Image type.
     * @param imageIndex The image index.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> deleteItemImageByIndexWithHttpInfo(UUID itemId, ImageType imageType, Integer imageIndex) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return deleteItemImageByIndexRequestCreation(itemId, imageType, imageIndex).toEntity(localVarReturnType);
    }

    /**
     * Delete an item&#39;s image.
     * 
     * <p><b>204</b> - Image deleted.
     * <p><b>404</b> - Item not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId Item id.
     * @param imageType Image type.
     * @param imageIndex The image index.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec deleteItemImageByIndexWithResponseSpec(UUID itemId, ImageType imageType, Integer imageIndex) throws WebClientResponseException {
        return deleteItemImageByIndexRequestCreation(itemId, imageType, imageIndex);
    }

    /**
     * Delete the user&#39;s image.
     * 
     * <p><b>204</b> - Image deleted.
     * <p><b>403</b> - User does not have permission to delete the image.
     * <p><b>401</b> - Unauthorized
     * @param userId User Id.
     * @param imageType (Unused) Image type.
     * @param index (Unused) Image index.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec deleteUserImageRequestCreation(UUID userId, ImageType imageType, Integer index) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'userId' is set
        if (userId == null) {
            throw new WebClientResponseException("Missing the required parameter 'userId' when calling deleteUserImage", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'imageType' is set
        if (imageType == null) {
            throw new WebClientResponseException("Missing the required parameter 'imageType' when calling deleteUserImage", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("userId", userId);
        pathParams.put("imageType", imageType);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "index", index));
        
        final String[] localVarAccepts = { 
            "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return apiClient.invokeAPI("/Users/{userId}/Images/{imageType}", HttpMethod.DELETE, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Delete the user&#39;s image.
     * 
     * <p><b>204</b> - Image deleted.
     * <p><b>403</b> - User does not have permission to delete the image.
     * <p><b>401</b> - Unauthorized
     * @param userId User Id.
     * @param imageType (Unused) Image type.
     * @param index (Unused) Image index.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> deleteUserImage(UUID userId, ImageType imageType, Integer index) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return deleteUserImageRequestCreation(userId, imageType, index).bodyToMono(localVarReturnType);
    }

    /**
     * Delete the user&#39;s image.
     * 
     * <p><b>204</b> - Image deleted.
     * <p><b>403</b> - User does not have permission to delete the image.
     * <p><b>401</b> - Unauthorized
     * @param userId User Id.
     * @param imageType (Unused) Image type.
     * @param index (Unused) Image index.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> deleteUserImageWithHttpInfo(UUID userId, ImageType imageType, Integer index) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return deleteUserImageRequestCreation(userId, imageType, index).toEntity(localVarReturnType);
    }

    /**
     * Delete the user&#39;s image.
     * 
     * <p><b>204</b> - Image deleted.
     * <p><b>403</b> - User does not have permission to delete the image.
     * <p><b>401</b> - Unauthorized
     * @param userId User Id.
     * @param imageType (Unused) Image type.
     * @param index (Unused) Image index.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec deleteUserImageWithResponseSpec(UUID userId, ImageType imageType, Integer index) throws WebClientResponseException {
        return deleteUserImageRequestCreation(userId, imageType, index);
    }

    /**
     * Delete the user&#39;s image.
     * 
     * <p><b>204</b> - Image deleted.
     * <p><b>403</b> - User does not have permission to delete the image.
     * <p><b>401</b> - Unauthorized
     * @param userId User Id.
     * @param imageType (Unused) Image type.
     * @param index (Unused) Image index.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec deleteUserImageByIndexRequestCreation(UUID userId, ImageType imageType, Integer index) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'userId' is set
        if (userId == null) {
            throw new WebClientResponseException("Missing the required parameter 'userId' when calling deleteUserImageByIndex", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'imageType' is set
        if (imageType == null) {
            throw new WebClientResponseException("Missing the required parameter 'imageType' when calling deleteUserImageByIndex", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'index' is set
        if (index == null) {
            throw new WebClientResponseException("Missing the required parameter 'index' when calling deleteUserImageByIndex", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("userId", userId);
        pathParams.put("imageType", imageType);
        pathParams.put("index", index);

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
        return apiClient.invokeAPI("/Users/{userId}/Images/{imageType}/{index}", HttpMethod.DELETE, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Delete the user&#39;s image.
     * 
     * <p><b>204</b> - Image deleted.
     * <p><b>403</b> - User does not have permission to delete the image.
     * <p><b>401</b> - Unauthorized
     * @param userId User Id.
     * @param imageType (Unused) Image type.
     * @param index (Unused) Image index.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> deleteUserImageByIndex(UUID userId, ImageType imageType, Integer index) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return deleteUserImageByIndexRequestCreation(userId, imageType, index).bodyToMono(localVarReturnType);
    }

    /**
     * Delete the user&#39;s image.
     * 
     * <p><b>204</b> - Image deleted.
     * <p><b>403</b> - User does not have permission to delete the image.
     * <p><b>401</b> - Unauthorized
     * @param userId User Id.
     * @param imageType (Unused) Image type.
     * @param index (Unused) Image index.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> deleteUserImageByIndexWithHttpInfo(UUID userId, ImageType imageType, Integer index) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return deleteUserImageByIndexRequestCreation(userId, imageType, index).toEntity(localVarReturnType);
    }

    /**
     * Delete the user&#39;s image.
     * 
     * <p><b>204</b> - Image deleted.
     * <p><b>403</b> - User does not have permission to delete the image.
     * <p><b>401</b> - Unauthorized
     * @param userId User Id.
     * @param imageType (Unused) Image type.
     * @param index (Unused) Image index.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec deleteUserImageByIndexWithResponseSpec(UUID userId, ImageType imageType, Integer index) throws WebClientResponseException {
        return deleteUserImageByIndexRequestCreation(userId, imageType, index);
    }

    /**
     * Get artist image by name.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param name Artist name.
     * @param imageType Image type.
     * @param imageIndex Image index.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getArtistImageRequestCreation(String name, ImageType imageType, Integer imageIndex, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'name' is set
        if (name == null) {
            throw new WebClientResponseException("Missing the required parameter 'name' when calling getArtistImage", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'imageType' is set
        if (imageType == null) {
            throw new WebClientResponseException("Missing the required parameter 'imageType' when calling getArtistImage", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'imageIndex' is set
        if (imageIndex == null) {
            throw new WebClientResponseException("Missing the required parameter 'imageIndex' when calling getArtistImage", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("name", name);
        pathParams.put("imageType", imageType);
        pathParams.put("imageIndex", imageIndex);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "tag", tag));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "format", format));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxWidth", maxWidth));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxHeight", maxHeight));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "percentPlayed", percentPlayed));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "unplayedCount", unplayedCount));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "width", width));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "height", height));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "quality", quality));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "fillWidth", fillWidth));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "fillHeight", fillHeight));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "cropWhitespace", cropWhitespace));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "addPlayedIndicator", addPlayedIndicator));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "blur", blur));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "backgroundColor", backgroundColor));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "foregroundLayer", foregroundLayer));
        
        final String[] localVarAccepts = { 
            "image/*", "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return apiClient.invokeAPI("/Artists/{name}/Images/{imageType}/{imageIndex}", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get artist image by name.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param name Artist name.
     * @param imageType Image type.
     * @param imageIndex Image index.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<File> getArtistImage(String name, ImageType imageType, Integer imageIndex, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return getArtistImageRequestCreation(name, imageType, imageIndex, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer).bodyToMono(localVarReturnType);
    }

    /**
     * Get artist image by name.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param name Artist name.
     * @param imageType Image type.
     * @param imageIndex Image index.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @return ResponseEntity&lt;File&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<File>> getArtistImageWithHttpInfo(String name, ImageType imageType, Integer imageIndex, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return getArtistImageRequestCreation(name, imageType, imageIndex, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer).toEntity(localVarReturnType);
    }

    /**
     * Get artist image by name.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param name Artist name.
     * @param imageType Image type.
     * @param imageIndex Image index.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getArtistImageWithResponseSpec(String name, ImageType imageType, Integer imageIndex, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer) throws WebClientResponseException {
        return getArtistImageRequestCreation(name, imageType, imageIndex, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer);
    }

    /**
     * Get genre image by name.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param name Genre name.
     * @param imageType Image type.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @param imageIndex Image index.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getGenreImageRequestCreation(String name, ImageType imageType, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer, Integer imageIndex) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'name' is set
        if (name == null) {
            throw new WebClientResponseException("Missing the required parameter 'name' when calling getGenreImage", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'imageType' is set
        if (imageType == null) {
            throw new WebClientResponseException("Missing the required parameter 'imageType' when calling getGenreImage", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("name", name);
        pathParams.put("imageType", imageType);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "tag", tag));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "format", format));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxWidth", maxWidth));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxHeight", maxHeight));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "percentPlayed", percentPlayed));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "unplayedCount", unplayedCount));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "width", width));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "height", height));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "quality", quality));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "fillWidth", fillWidth));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "fillHeight", fillHeight));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "cropWhitespace", cropWhitespace));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "addPlayedIndicator", addPlayedIndicator));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "blur", blur));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "backgroundColor", backgroundColor));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "foregroundLayer", foregroundLayer));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "imageIndex", imageIndex));
        
        final String[] localVarAccepts = { 
            "image/*", "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return apiClient.invokeAPI("/Genres/{name}/Images/{imageType}", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get genre image by name.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param name Genre name.
     * @param imageType Image type.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @param imageIndex Image index.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<File> getGenreImage(String name, ImageType imageType, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer, Integer imageIndex) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return getGenreImageRequestCreation(name, imageType, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer, imageIndex).bodyToMono(localVarReturnType);
    }

    /**
     * Get genre image by name.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param name Genre name.
     * @param imageType Image type.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @param imageIndex Image index.
     * @return ResponseEntity&lt;File&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<File>> getGenreImageWithHttpInfo(String name, ImageType imageType, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer, Integer imageIndex) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return getGenreImageRequestCreation(name, imageType, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer, imageIndex).toEntity(localVarReturnType);
    }

    /**
     * Get genre image by name.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param name Genre name.
     * @param imageType Image type.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @param imageIndex Image index.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getGenreImageWithResponseSpec(String name, ImageType imageType, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer, Integer imageIndex) throws WebClientResponseException {
        return getGenreImageRequestCreation(name, imageType, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer, imageIndex);
    }

    /**
     * Get genre image by name.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param name Genre name.
     * @param imageType Image type.
     * @param imageIndex Image index.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getGenreImageByIndexRequestCreation(String name, ImageType imageType, Integer imageIndex, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'name' is set
        if (name == null) {
            throw new WebClientResponseException("Missing the required parameter 'name' when calling getGenreImageByIndex", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'imageType' is set
        if (imageType == null) {
            throw new WebClientResponseException("Missing the required parameter 'imageType' when calling getGenreImageByIndex", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'imageIndex' is set
        if (imageIndex == null) {
            throw new WebClientResponseException("Missing the required parameter 'imageIndex' when calling getGenreImageByIndex", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("name", name);
        pathParams.put("imageType", imageType);
        pathParams.put("imageIndex", imageIndex);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "tag", tag));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "format", format));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxWidth", maxWidth));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxHeight", maxHeight));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "percentPlayed", percentPlayed));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "unplayedCount", unplayedCount));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "width", width));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "height", height));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "quality", quality));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "fillWidth", fillWidth));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "fillHeight", fillHeight));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "cropWhitespace", cropWhitespace));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "addPlayedIndicator", addPlayedIndicator));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "blur", blur));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "backgroundColor", backgroundColor));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "foregroundLayer", foregroundLayer));
        
        final String[] localVarAccepts = { 
            "image/*", "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return apiClient.invokeAPI("/Genres/{name}/Images/{imageType}/{imageIndex}", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get genre image by name.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param name Genre name.
     * @param imageType Image type.
     * @param imageIndex Image index.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<File> getGenreImageByIndex(String name, ImageType imageType, Integer imageIndex, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return getGenreImageByIndexRequestCreation(name, imageType, imageIndex, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer).bodyToMono(localVarReturnType);
    }

    /**
     * Get genre image by name.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param name Genre name.
     * @param imageType Image type.
     * @param imageIndex Image index.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @return ResponseEntity&lt;File&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<File>> getGenreImageByIndexWithHttpInfo(String name, ImageType imageType, Integer imageIndex, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return getGenreImageByIndexRequestCreation(name, imageType, imageIndex, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer).toEntity(localVarReturnType);
    }

    /**
     * Get genre image by name.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param name Genre name.
     * @param imageType Image type.
     * @param imageIndex Image index.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getGenreImageByIndexWithResponseSpec(String name, ImageType imageType, Integer imageIndex, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer) throws WebClientResponseException {
        return getGenreImageByIndexRequestCreation(name, imageType, imageIndex, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer);
    }

    /**
     * Gets the item&#39;s image.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param itemId Item id.
     * @param imageType Image type.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param format Optional. The MediaBrowser.Model.Drawing.ImageFormat of the returned image.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @param imageIndex Image index.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getItemImageRequestCreation(UUID itemId, ImageType imageType, Integer maxWidth, Integer maxHeight, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, String tag, Boolean cropWhitespace, ImageFormat format, Boolean addPlayedIndicator, Double percentPlayed, Integer unplayedCount, Integer blur, String backgroundColor, String foregroundLayer, Integer imageIndex) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException("Missing the required parameter 'itemId' when calling getItemImage", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'imageType' is set
        if (imageType == null) {
            throw new WebClientResponseException("Missing the required parameter 'imageType' when calling getItemImage", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("itemId", itemId);
        pathParams.put("imageType", imageType);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxWidth", maxWidth));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxHeight", maxHeight));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "width", width));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "height", height));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "quality", quality));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "fillWidth", fillWidth));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "fillHeight", fillHeight));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "tag", tag));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "cropWhitespace", cropWhitespace));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "format", format));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "addPlayedIndicator", addPlayedIndicator));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "percentPlayed", percentPlayed));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "unplayedCount", unplayedCount));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "blur", blur));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "backgroundColor", backgroundColor));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "foregroundLayer", foregroundLayer));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "imageIndex", imageIndex));
        
        final String[] localVarAccepts = { 
            "image/*", "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return apiClient.invokeAPI("/Items/{itemId}/Images/{imageType}", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets the item&#39;s image.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param itemId Item id.
     * @param imageType Image type.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param format Optional. The MediaBrowser.Model.Drawing.ImageFormat of the returned image.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @param imageIndex Image index.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<File> getItemImage(UUID itemId, ImageType imageType, Integer maxWidth, Integer maxHeight, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, String tag, Boolean cropWhitespace, ImageFormat format, Boolean addPlayedIndicator, Double percentPlayed, Integer unplayedCount, Integer blur, String backgroundColor, String foregroundLayer, Integer imageIndex) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return getItemImageRequestCreation(itemId, imageType, maxWidth, maxHeight, width, height, quality, fillWidth, fillHeight, tag, cropWhitespace, format, addPlayedIndicator, percentPlayed, unplayedCount, blur, backgroundColor, foregroundLayer, imageIndex).bodyToMono(localVarReturnType);
    }

    /**
     * Gets the item&#39;s image.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param itemId Item id.
     * @param imageType Image type.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param format Optional. The MediaBrowser.Model.Drawing.ImageFormat of the returned image.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @param imageIndex Image index.
     * @return ResponseEntity&lt;File&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<File>> getItemImageWithHttpInfo(UUID itemId, ImageType imageType, Integer maxWidth, Integer maxHeight, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, String tag, Boolean cropWhitespace, ImageFormat format, Boolean addPlayedIndicator, Double percentPlayed, Integer unplayedCount, Integer blur, String backgroundColor, String foregroundLayer, Integer imageIndex) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return getItemImageRequestCreation(itemId, imageType, maxWidth, maxHeight, width, height, quality, fillWidth, fillHeight, tag, cropWhitespace, format, addPlayedIndicator, percentPlayed, unplayedCount, blur, backgroundColor, foregroundLayer, imageIndex).toEntity(localVarReturnType);
    }

    /**
     * Gets the item&#39;s image.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param itemId Item id.
     * @param imageType Image type.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param format Optional. The MediaBrowser.Model.Drawing.ImageFormat of the returned image.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @param imageIndex Image index.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getItemImageWithResponseSpec(UUID itemId, ImageType imageType, Integer maxWidth, Integer maxHeight, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, String tag, Boolean cropWhitespace, ImageFormat format, Boolean addPlayedIndicator, Double percentPlayed, Integer unplayedCount, Integer blur, String backgroundColor, String foregroundLayer, Integer imageIndex) throws WebClientResponseException {
        return getItemImageRequestCreation(itemId, imageType, maxWidth, maxHeight, width, height, quality, fillWidth, fillHeight, tag, cropWhitespace, format, addPlayedIndicator, percentPlayed, unplayedCount, blur, backgroundColor, foregroundLayer, imageIndex);
    }

    /**
     * Gets the item&#39;s image.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param itemId Item id.
     * @param imageType Image type.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param imageIndex Image index.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getItemImage2RequestCreation(UUID itemId, ImageType imageType, Integer maxWidth, Integer maxHeight, String tag, ImageFormat format, Double percentPlayed, Integer unplayedCount, Integer imageIndex, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException("Missing the required parameter 'itemId' when calling getItemImage2", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'imageType' is set
        if (imageType == null) {
            throw new WebClientResponseException("Missing the required parameter 'imageType' when calling getItemImage2", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'maxWidth' is set
        if (maxWidth == null) {
            throw new WebClientResponseException("Missing the required parameter 'maxWidth' when calling getItemImage2", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'maxHeight' is set
        if (maxHeight == null) {
            throw new WebClientResponseException("Missing the required parameter 'maxHeight' when calling getItemImage2", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'tag' is set
        if (tag == null) {
            throw new WebClientResponseException("Missing the required parameter 'tag' when calling getItemImage2", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'format' is set
        if (format == null) {
            throw new WebClientResponseException("Missing the required parameter 'format' when calling getItemImage2", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'percentPlayed' is set
        if (percentPlayed == null) {
            throw new WebClientResponseException("Missing the required parameter 'percentPlayed' when calling getItemImage2", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'unplayedCount' is set
        if (unplayedCount == null) {
            throw new WebClientResponseException("Missing the required parameter 'unplayedCount' when calling getItemImage2", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'imageIndex' is set
        if (imageIndex == null) {
            throw new WebClientResponseException("Missing the required parameter 'imageIndex' when calling getItemImage2", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("itemId", itemId);
        pathParams.put("imageType", imageType);
        pathParams.put("maxWidth", maxWidth);
        pathParams.put("maxHeight", maxHeight);
        pathParams.put("tag", tag);
        pathParams.put("format", format);
        pathParams.put("percentPlayed", percentPlayed);
        pathParams.put("unplayedCount", unplayedCount);
        pathParams.put("imageIndex", imageIndex);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "width", width));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "height", height));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "quality", quality));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "fillWidth", fillWidth));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "fillHeight", fillHeight));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "cropWhitespace", cropWhitespace));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "addPlayedIndicator", addPlayedIndicator));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "blur", blur));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "backgroundColor", backgroundColor));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "foregroundLayer", foregroundLayer));
        
        final String[] localVarAccepts = { 
            "image/*", "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return apiClient.invokeAPI("/Items/{itemId}/Images/{imageType}/{imageIndex}/{tag}/{format}/{maxWidth}/{maxHeight}/{percentPlayed}/{unplayedCount}", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets the item&#39;s image.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param itemId Item id.
     * @param imageType Image type.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param imageIndex Image index.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<File> getItemImage2(UUID itemId, ImageType imageType, Integer maxWidth, Integer maxHeight, String tag, ImageFormat format, Double percentPlayed, Integer unplayedCount, Integer imageIndex, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return getItemImage2RequestCreation(itemId, imageType, maxWidth, maxHeight, tag, format, percentPlayed, unplayedCount, imageIndex, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer).bodyToMono(localVarReturnType);
    }

    /**
     * Gets the item&#39;s image.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param itemId Item id.
     * @param imageType Image type.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param imageIndex Image index.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @return ResponseEntity&lt;File&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<File>> getItemImage2WithHttpInfo(UUID itemId, ImageType imageType, Integer maxWidth, Integer maxHeight, String tag, ImageFormat format, Double percentPlayed, Integer unplayedCount, Integer imageIndex, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return getItemImage2RequestCreation(itemId, imageType, maxWidth, maxHeight, tag, format, percentPlayed, unplayedCount, imageIndex, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer).toEntity(localVarReturnType);
    }

    /**
     * Gets the item&#39;s image.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param itemId Item id.
     * @param imageType Image type.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param imageIndex Image index.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getItemImage2WithResponseSpec(UUID itemId, ImageType imageType, Integer maxWidth, Integer maxHeight, String tag, ImageFormat format, Double percentPlayed, Integer unplayedCount, Integer imageIndex, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer) throws WebClientResponseException {
        return getItemImage2RequestCreation(itemId, imageType, maxWidth, maxHeight, tag, format, percentPlayed, unplayedCount, imageIndex, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer);
    }

    /**
     * Gets the item&#39;s image.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param itemId Item id.
     * @param imageType Image type.
     * @param imageIndex Image index.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param format Optional. The MediaBrowser.Model.Drawing.ImageFormat of the returned image.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getItemImageByIndexRequestCreation(UUID itemId, ImageType imageType, Integer imageIndex, Integer maxWidth, Integer maxHeight, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, String tag, Boolean cropWhitespace, ImageFormat format, Boolean addPlayedIndicator, Double percentPlayed, Integer unplayedCount, Integer blur, String backgroundColor, String foregroundLayer) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException("Missing the required parameter 'itemId' when calling getItemImageByIndex", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'imageType' is set
        if (imageType == null) {
            throw new WebClientResponseException("Missing the required parameter 'imageType' when calling getItemImageByIndex", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'imageIndex' is set
        if (imageIndex == null) {
            throw new WebClientResponseException("Missing the required parameter 'imageIndex' when calling getItemImageByIndex", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("itemId", itemId);
        pathParams.put("imageType", imageType);
        pathParams.put("imageIndex", imageIndex);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxWidth", maxWidth));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxHeight", maxHeight));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "width", width));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "height", height));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "quality", quality));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "fillWidth", fillWidth));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "fillHeight", fillHeight));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "tag", tag));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "cropWhitespace", cropWhitespace));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "format", format));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "addPlayedIndicator", addPlayedIndicator));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "percentPlayed", percentPlayed));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "unplayedCount", unplayedCount));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "blur", blur));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "backgroundColor", backgroundColor));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "foregroundLayer", foregroundLayer));
        
        final String[] localVarAccepts = { 
            "image/*", "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return apiClient.invokeAPI("/Items/{itemId}/Images/{imageType}/{imageIndex}", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets the item&#39;s image.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param itemId Item id.
     * @param imageType Image type.
     * @param imageIndex Image index.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param format Optional. The MediaBrowser.Model.Drawing.ImageFormat of the returned image.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<File> getItemImageByIndex(UUID itemId, ImageType imageType, Integer imageIndex, Integer maxWidth, Integer maxHeight, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, String tag, Boolean cropWhitespace, ImageFormat format, Boolean addPlayedIndicator, Double percentPlayed, Integer unplayedCount, Integer blur, String backgroundColor, String foregroundLayer) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return getItemImageByIndexRequestCreation(itemId, imageType, imageIndex, maxWidth, maxHeight, width, height, quality, fillWidth, fillHeight, tag, cropWhitespace, format, addPlayedIndicator, percentPlayed, unplayedCount, blur, backgroundColor, foregroundLayer).bodyToMono(localVarReturnType);
    }

    /**
     * Gets the item&#39;s image.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param itemId Item id.
     * @param imageType Image type.
     * @param imageIndex Image index.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param format Optional. The MediaBrowser.Model.Drawing.ImageFormat of the returned image.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @return ResponseEntity&lt;File&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<File>> getItemImageByIndexWithHttpInfo(UUID itemId, ImageType imageType, Integer imageIndex, Integer maxWidth, Integer maxHeight, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, String tag, Boolean cropWhitespace, ImageFormat format, Boolean addPlayedIndicator, Double percentPlayed, Integer unplayedCount, Integer blur, String backgroundColor, String foregroundLayer) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return getItemImageByIndexRequestCreation(itemId, imageType, imageIndex, maxWidth, maxHeight, width, height, quality, fillWidth, fillHeight, tag, cropWhitespace, format, addPlayedIndicator, percentPlayed, unplayedCount, blur, backgroundColor, foregroundLayer).toEntity(localVarReturnType);
    }

    /**
     * Gets the item&#39;s image.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param itemId Item id.
     * @param imageType Image type.
     * @param imageIndex Image index.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param format Optional. The MediaBrowser.Model.Drawing.ImageFormat of the returned image.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getItemImageByIndexWithResponseSpec(UUID itemId, ImageType imageType, Integer imageIndex, Integer maxWidth, Integer maxHeight, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, String tag, Boolean cropWhitespace, ImageFormat format, Boolean addPlayedIndicator, Double percentPlayed, Integer unplayedCount, Integer blur, String backgroundColor, String foregroundLayer) throws WebClientResponseException {
        return getItemImageByIndexRequestCreation(itemId, imageType, imageIndex, maxWidth, maxHeight, width, height, quality, fillWidth, fillHeight, tag, cropWhitespace, format, addPlayedIndicator, percentPlayed, unplayedCount, blur, backgroundColor, foregroundLayer);
    }

    /**
     * Get item image infos.
     * 
     * <p><b>200</b> - Item images returned.
     * <p><b>404</b> - Item not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId Item id.
     * @return List&lt;ImageInfo&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getItemImageInfosRequestCreation(UUID itemId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException("Missing the required parameter 'itemId' when calling getItemImageInfos", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
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

        ParameterizedTypeReference<ImageInfo> localVarReturnType = new ParameterizedTypeReference<ImageInfo>() {};
        return apiClient.invokeAPI("/Items/{itemId}/Images", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get item image infos.
     * 
     * <p><b>200</b> - Item images returned.
     * <p><b>404</b> - Item not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId Item id.
     * @return List&lt;ImageInfo&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Flux<ImageInfo> getItemImageInfos(UUID itemId) throws WebClientResponseException {
        ParameterizedTypeReference<ImageInfo> localVarReturnType = new ParameterizedTypeReference<ImageInfo>() {};
        return getItemImageInfosRequestCreation(itemId).bodyToFlux(localVarReturnType);
    }

    /**
     * Get item image infos.
     * 
     * <p><b>200</b> - Item images returned.
     * <p><b>404</b> - Item not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId Item id.
     * @return ResponseEntity&lt;List&lt;ImageInfo&gt;&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<List<ImageInfo>>> getItemImageInfosWithHttpInfo(UUID itemId) throws WebClientResponseException {
        ParameterizedTypeReference<ImageInfo> localVarReturnType = new ParameterizedTypeReference<ImageInfo>() {};
        return getItemImageInfosRequestCreation(itemId).toEntityList(localVarReturnType);
    }

    /**
     * Get item image infos.
     * 
     * <p><b>200</b> - Item images returned.
     * <p><b>404</b> - Item not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId Item id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getItemImageInfosWithResponseSpec(UUID itemId) throws WebClientResponseException {
        return getItemImageInfosRequestCreation(itemId);
    }

    /**
     * Get music genre image by name.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param name Music genre name.
     * @param imageType Image type.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @param imageIndex Image index.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getMusicGenreImageRequestCreation(String name, ImageType imageType, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer, Integer imageIndex) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'name' is set
        if (name == null) {
            throw new WebClientResponseException("Missing the required parameter 'name' when calling getMusicGenreImage", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'imageType' is set
        if (imageType == null) {
            throw new WebClientResponseException("Missing the required parameter 'imageType' when calling getMusicGenreImage", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("name", name);
        pathParams.put("imageType", imageType);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "tag", tag));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "format", format));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxWidth", maxWidth));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxHeight", maxHeight));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "percentPlayed", percentPlayed));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "unplayedCount", unplayedCount));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "width", width));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "height", height));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "quality", quality));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "fillWidth", fillWidth));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "fillHeight", fillHeight));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "cropWhitespace", cropWhitespace));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "addPlayedIndicator", addPlayedIndicator));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "blur", blur));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "backgroundColor", backgroundColor));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "foregroundLayer", foregroundLayer));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "imageIndex", imageIndex));
        
        final String[] localVarAccepts = { 
            "image/*", "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return apiClient.invokeAPI("/MusicGenres/{name}/Images/{imageType}", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get music genre image by name.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param name Music genre name.
     * @param imageType Image type.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @param imageIndex Image index.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<File> getMusicGenreImage(String name, ImageType imageType, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer, Integer imageIndex) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return getMusicGenreImageRequestCreation(name, imageType, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer, imageIndex).bodyToMono(localVarReturnType);
    }

    /**
     * Get music genre image by name.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param name Music genre name.
     * @param imageType Image type.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @param imageIndex Image index.
     * @return ResponseEntity&lt;File&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<File>> getMusicGenreImageWithHttpInfo(String name, ImageType imageType, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer, Integer imageIndex) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return getMusicGenreImageRequestCreation(name, imageType, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer, imageIndex).toEntity(localVarReturnType);
    }

    /**
     * Get music genre image by name.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param name Music genre name.
     * @param imageType Image type.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @param imageIndex Image index.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getMusicGenreImageWithResponseSpec(String name, ImageType imageType, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer, Integer imageIndex) throws WebClientResponseException {
        return getMusicGenreImageRequestCreation(name, imageType, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer, imageIndex);
    }

    /**
     * Get music genre image by name.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param name Music genre name.
     * @param imageType Image type.
     * @param imageIndex Image index.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getMusicGenreImageByIndexRequestCreation(String name, ImageType imageType, Integer imageIndex, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'name' is set
        if (name == null) {
            throw new WebClientResponseException("Missing the required parameter 'name' when calling getMusicGenreImageByIndex", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'imageType' is set
        if (imageType == null) {
            throw new WebClientResponseException("Missing the required parameter 'imageType' when calling getMusicGenreImageByIndex", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'imageIndex' is set
        if (imageIndex == null) {
            throw new WebClientResponseException("Missing the required parameter 'imageIndex' when calling getMusicGenreImageByIndex", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("name", name);
        pathParams.put("imageType", imageType);
        pathParams.put("imageIndex", imageIndex);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "tag", tag));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "format", format));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxWidth", maxWidth));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxHeight", maxHeight));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "percentPlayed", percentPlayed));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "unplayedCount", unplayedCount));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "width", width));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "height", height));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "quality", quality));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "fillWidth", fillWidth));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "fillHeight", fillHeight));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "cropWhitespace", cropWhitespace));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "addPlayedIndicator", addPlayedIndicator));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "blur", blur));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "backgroundColor", backgroundColor));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "foregroundLayer", foregroundLayer));
        
        final String[] localVarAccepts = { 
            "image/*", "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return apiClient.invokeAPI("/MusicGenres/{name}/Images/{imageType}/{imageIndex}", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get music genre image by name.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param name Music genre name.
     * @param imageType Image type.
     * @param imageIndex Image index.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<File> getMusicGenreImageByIndex(String name, ImageType imageType, Integer imageIndex, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return getMusicGenreImageByIndexRequestCreation(name, imageType, imageIndex, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer).bodyToMono(localVarReturnType);
    }

    /**
     * Get music genre image by name.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param name Music genre name.
     * @param imageType Image type.
     * @param imageIndex Image index.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @return ResponseEntity&lt;File&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<File>> getMusicGenreImageByIndexWithHttpInfo(String name, ImageType imageType, Integer imageIndex, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return getMusicGenreImageByIndexRequestCreation(name, imageType, imageIndex, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer).toEntity(localVarReturnType);
    }

    /**
     * Get music genre image by name.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param name Music genre name.
     * @param imageType Image type.
     * @param imageIndex Image index.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getMusicGenreImageByIndexWithResponseSpec(String name, ImageType imageType, Integer imageIndex, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer) throws WebClientResponseException {
        return getMusicGenreImageByIndexRequestCreation(name, imageType, imageIndex, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer);
    }

    /**
     * Get person image by name.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param name Person name.
     * @param imageType Image type.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @param imageIndex Image index.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getPersonImageRequestCreation(String name, ImageType imageType, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer, Integer imageIndex) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'name' is set
        if (name == null) {
            throw new WebClientResponseException("Missing the required parameter 'name' when calling getPersonImage", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'imageType' is set
        if (imageType == null) {
            throw new WebClientResponseException("Missing the required parameter 'imageType' when calling getPersonImage", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("name", name);
        pathParams.put("imageType", imageType);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "tag", tag));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "format", format));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxWidth", maxWidth));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxHeight", maxHeight));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "percentPlayed", percentPlayed));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "unplayedCount", unplayedCount));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "width", width));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "height", height));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "quality", quality));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "fillWidth", fillWidth));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "fillHeight", fillHeight));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "cropWhitespace", cropWhitespace));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "addPlayedIndicator", addPlayedIndicator));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "blur", blur));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "backgroundColor", backgroundColor));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "foregroundLayer", foregroundLayer));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "imageIndex", imageIndex));
        
        final String[] localVarAccepts = { 
            "image/*", "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return apiClient.invokeAPI("/Persons/{name}/Images/{imageType}", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get person image by name.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param name Person name.
     * @param imageType Image type.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @param imageIndex Image index.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<File> getPersonImage(String name, ImageType imageType, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer, Integer imageIndex) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return getPersonImageRequestCreation(name, imageType, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer, imageIndex).bodyToMono(localVarReturnType);
    }

    /**
     * Get person image by name.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param name Person name.
     * @param imageType Image type.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @param imageIndex Image index.
     * @return ResponseEntity&lt;File&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<File>> getPersonImageWithHttpInfo(String name, ImageType imageType, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer, Integer imageIndex) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return getPersonImageRequestCreation(name, imageType, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer, imageIndex).toEntity(localVarReturnType);
    }

    /**
     * Get person image by name.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param name Person name.
     * @param imageType Image type.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @param imageIndex Image index.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getPersonImageWithResponseSpec(String name, ImageType imageType, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer, Integer imageIndex) throws WebClientResponseException {
        return getPersonImageRequestCreation(name, imageType, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer, imageIndex);
    }

    /**
     * Get person image by name.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param name Person name.
     * @param imageType Image type.
     * @param imageIndex Image index.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getPersonImageByIndexRequestCreation(String name, ImageType imageType, Integer imageIndex, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'name' is set
        if (name == null) {
            throw new WebClientResponseException("Missing the required parameter 'name' when calling getPersonImageByIndex", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'imageType' is set
        if (imageType == null) {
            throw new WebClientResponseException("Missing the required parameter 'imageType' when calling getPersonImageByIndex", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'imageIndex' is set
        if (imageIndex == null) {
            throw new WebClientResponseException("Missing the required parameter 'imageIndex' when calling getPersonImageByIndex", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("name", name);
        pathParams.put("imageType", imageType);
        pathParams.put("imageIndex", imageIndex);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "tag", tag));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "format", format));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxWidth", maxWidth));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxHeight", maxHeight));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "percentPlayed", percentPlayed));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "unplayedCount", unplayedCount));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "width", width));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "height", height));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "quality", quality));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "fillWidth", fillWidth));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "fillHeight", fillHeight));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "cropWhitespace", cropWhitespace));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "addPlayedIndicator", addPlayedIndicator));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "blur", blur));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "backgroundColor", backgroundColor));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "foregroundLayer", foregroundLayer));
        
        final String[] localVarAccepts = { 
            "image/*", "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return apiClient.invokeAPI("/Persons/{name}/Images/{imageType}/{imageIndex}", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get person image by name.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param name Person name.
     * @param imageType Image type.
     * @param imageIndex Image index.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<File> getPersonImageByIndex(String name, ImageType imageType, Integer imageIndex, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return getPersonImageByIndexRequestCreation(name, imageType, imageIndex, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer).bodyToMono(localVarReturnType);
    }

    /**
     * Get person image by name.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param name Person name.
     * @param imageType Image type.
     * @param imageIndex Image index.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @return ResponseEntity&lt;File&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<File>> getPersonImageByIndexWithHttpInfo(String name, ImageType imageType, Integer imageIndex, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return getPersonImageByIndexRequestCreation(name, imageType, imageIndex, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer).toEntity(localVarReturnType);
    }

    /**
     * Get person image by name.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param name Person name.
     * @param imageType Image type.
     * @param imageIndex Image index.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getPersonImageByIndexWithResponseSpec(String name, ImageType imageType, Integer imageIndex, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer) throws WebClientResponseException {
        return getPersonImageByIndexRequestCreation(name, imageType, imageIndex, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer);
    }

    /**
     * Generates or gets the splashscreen.
     * 
     * <p><b>200</b> - Splashscreen returned successfully.
     * @param tag Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param blur Blur image.
     * @param backgroundColor Apply a background color for transparent images.
     * @param foregroundLayer Apply a foreground layer on top of the image.
     * @param quality Quality setting, from 0-100.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getSplashscreenRequestCreation(String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Integer width, Integer height, Integer fillWidth, Integer fillHeight, Integer blur, String backgroundColor, String foregroundLayer, Integer quality) throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "tag", tag));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "format", format));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxWidth", maxWidth));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxHeight", maxHeight));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "width", width));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "height", height));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "fillWidth", fillWidth));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "fillHeight", fillHeight));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "blur", blur));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "backgroundColor", backgroundColor));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "foregroundLayer", foregroundLayer));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "quality", quality));
        
        final String[] localVarAccepts = { 
            "image/*"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return apiClient.invokeAPI("/Branding/Splashscreen", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Generates or gets the splashscreen.
     * 
     * <p><b>200</b> - Splashscreen returned successfully.
     * @param tag Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param blur Blur image.
     * @param backgroundColor Apply a background color for transparent images.
     * @param foregroundLayer Apply a foreground layer on top of the image.
     * @param quality Quality setting, from 0-100.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<File> getSplashscreen(String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Integer width, Integer height, Integer fillWidth, Integer fillHeight, Integer blur, String backgroundColor, String foregroundLayer, Integer quality) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return getSplashscreenRequestCreation(tag, format, maxWidth, maxHeight, width, height, fillWidth, fillHeight, blur, backgroundColor, foregroundLayer, quality).bodyToMono(localVarReturnType);
    }

    /**
     * Generates or gets the splashscreen.
     * 
     * <p><b>200</b> - Splashscreen returned successfully.
     * @param tag Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param blur Blur image.
     * @param backgroundColor Apply a background color for transparent images.
     * @param foregroundLayer Apply a foreground layer on top of the image.
     * @param quality Quality setting, from 0-100.
     * @return ResponseEntity&lt;File&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<File>> getSplashscreenWithHttpInfo(String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Integer width, Integer height, Integer fillWidth, Integer fillHeight, Integer blur, String backgroundColor, String foregroundLayer, Integer quality) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return getSplashscreenRequestCreation(tag, format, maxWidth, maxHeight, width, height, fillWidth, fillHeight, blur, backgroundColor, foregroundLayer, quality).toEntity(localVarReturnType);
    }

    /**
     * Generates or gets the splashscreen.
     * 
     * <p><b>200</b> - Splashscreen returned successfully.
     * @param tag Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param blur Blur image.
     * @param backgroundColor Apply a background color for transparent images.
     * @param foregroundLayer Apply a foreground layer on top of the image.
     * @param quality Quality setting, from 0-100.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getSplashscreenWithResponseSpec(String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Integer width, Integer height, Integer fillWidth, Integer fillHeight, Integer blur, String backgroundColor, String foregroundLayer, Integer quality) throws WebClientResponseException {
        return getSplashscreenRequestCreation(tag, format, maxWidth, maxHeight, width, height, fillWidth, fillHeight, blur, backgroundColor, foregroundLayer, quality);
    }

    /**
     * Get studio image by name.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param name Studio name.
     * @param imageType Image type.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @param imageIndex Image index.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getStudioImageRequestCreation(String name, ImageType imageType, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer, Integer imageIndex) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'name' is set
        if (name == null) {
            throw new WebClientResponseException("Missing the required parameter 'name' when calling getStudioImage", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'imageType' is set
        if (imageType == null) {
            throw new WebClientResponseException("Missing the required parameter 'imageType' when calling getStudioImage", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("name", name);
        pathParams.put("imageType", imageType);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "tag", tag));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "format", format));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxWidth", maxWidth));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxHeight", maxHeight));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "percentPlayed", percentPlayed));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "unplayedCount", unplayedCount));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "width", width));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "height", height));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "quality", quality));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "fillWidth", fillWidth));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "fillHeight", fillHeight));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "cropWhitespace", cropWhitespace));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "addPlayedIndicator", addPlayedIndicator));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "blur", blur));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "backgroundColor", backgroundColor));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "foregroundLayer", foregroundLayer));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "imageIndex", imageIndex));
        
        final String[] localVarAccepts = { 
            "image/*", "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return apiClient.invokeAPI("/Studios/{name}/Images/{imageType}", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get studio image by name.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param name Studio name.
     * @param imageType Image type.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @param imageIndex Image index.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<File> getStudioImage(String name, ImageType imageType, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer, Integer imageIndex) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return getStudioImageRequestCreation(name, imageType, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer, imageIndex).bodyToMono(localVarReturnType);
    }

    /**
     * Get studio image by name.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param name Studio name.
     * @param imageType Image type.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @param imageIndex Image index.
     * @return ResponseEntity&lt;File&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<File>> getStudioImageWithHttpInfo(String name, ImageType imageType, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer, Integer imageIndex) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return getStudioImageRequestCreation(name, imageType, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer, imageIndex).toEntity(localVarReturnType);
    }

    /**
     * Get studio image by name.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param name Studio name.
     * @param imageType Image type.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @param imageIndex Image index.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getStudioImageWithResponseSpec(String name, ImageType imageType, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer, Integer imageIndex) throws WebClientResponseException {
        return getStudioImageRequestCreation(name, imageType, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer, imageIndex);
    }

    /**
     * Get studio image by name.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param name Studio name.
     * @param imageType Image type.
     * @param imageIndex Image index.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getStudioImageByIndexRequestCreation(String name, ImageType imageType, Integer imageIndex, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'name' is set
        if (name == null) {
            throw new WebClientResponseException("Missing the required parameter 'name' when calling getStudioImageByIndex", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'imageType' is set
        if (imageType == null) {
            throw new WebClientResponseException("Missing the required parameter 'imageType' when calling getStudioImageByIndex", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'imageIndex' is set
        if (imageIndex == null) {
            throw new WebClientResponseException("Missing the required parameter 'imageIndex' when calling getStudioImageByIndex", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("name", name);
        pathParams.put("imageType", imageType);
        pathParams.put("imageIndex", imageIndex);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "tag", tag));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "format", format));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxWidth", maxWidth));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxHeight", maxHeight));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "percentPlayed", percentPlayed));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "unplayedCount", unplayedCount));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "width", width));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "height", height));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "quality", quality));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "fillWidth", fillWidth));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "fillHeight", fillHeight));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "cropWhitespace", cropWhitespace));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "addPlayedIndicator", addPlayedIndicator));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "blur", blur));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "backgroundColor", backgroundColor));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "foregroundLayer", foregroundLayer));
        
        final String[] localVarAccepts = { 
            "image/*", "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return apiClient.invokeAPI("/Studios/{name}/Images/{imageType}/{imageIndex}", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get studio image by name.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param name Studio name.
     * @param imageType Image type.
     * @param imageIndex Image index.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<File> getStudioImageByIndex(String name, ImageType imageType, Integer imageIndex, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return getStudioImageByIndexRequestCreation(name, imageType, imageIndex, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer).bodyToMono(localVarReturnType);
    }

    /**
     * Get studio image by name.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param name Studio name.
     * @param imageType Image type.
     * @param imageIndex Image index.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @return ResponseEntity&lt;File&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<File>> getStudioImageByIndexWithHttpInfo(String name, ImageType imageType, Integer imageIndex, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return getStudioImageByIndexRequestCreation(name, imageType, imageIndex, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer).toEntity(localVarReturnType);
    }

    /**
     * Get studio image by name.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param name Studio name.
     * @param imageType Image type.
     * @param imageIndex Image index.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getStudioImageByIndexWithResponseSpec(String name, ImageType imageType, Integer imageIndex, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer) throws WebClientResponseException {
        return getStudioImageByIndexRequestCreation(name, imageType, imageIndex, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer);
    }

    /**
     * Get user profile image.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param userId User id.
     * @param imageType Image type.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @param imageIndex Image index.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getUserImageRequestCreation(UUID userId, ImageType imageType, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer, Integer imageIndex) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'userId' is set
        if (userId == null) {
            throw new WebClientResponseException("Missing the required parameter 'userId' when calling getUserImage", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'imageType' is set
        if (imageType == null) {
            throw new WebClientResponseException("Missing the required parameter 'imageType' when calling getUserImage", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("userId", userId);
        pathParams.put("imageType", imageType);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "tag", tag));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "format", format));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxWidth", maxWidth));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxHeight", maxHeight));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "percentPlayed", percentPlayed));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "unplayedCount", unplayedCount));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "width", width));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "height", height));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "quality", quality));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "fillWidth", fillWidth));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "fillHeight", fillHeight));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "cropWhitespace", cropWhitespace));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "addPlayedIndicator", addPlayedIndicator));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "blur", blur));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "backgroundColor", backgroundColor));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "foregroundLayer", foregroundLayer));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "imageIndex", imageIndex));
        
        final String[] localVarAccepts = { 
            "image/*", "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return apiClient.invokeAPI("/Users/{userId}/Images/{imageType}", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get user profile image.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param userId User id.
     * @param imageType Image type.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @param imageIndex Image index.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<File> getUserImage(UUID userId, ImageType imageType, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer, Integer imageIndex) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return getUserImageRequestCreation(userId, imageType, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer, imageIndex).bodyToMono(localVarReturnType);
    }

    /**
     * Get user profile image.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param userId User id.
     * @param imageType Image type.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @param imageIndex Image index.
     * @return ResponseEntity&lt;File&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<File>> getUserImageWithHttpInfo(UUID userId, ImageType imageType, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer, Integer imageIndex) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return getUserImageRequestCreation(userId, imageType, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer, imageIndex).toEntity(localVarReturnType);
    }

    /**
     * Get user profile image.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param userId User id.
     * @param imageType Image type.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @param imageIndex Image index.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getUserImageWithResponseSpec(UUID userId, ImageType imageType, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer, Integer imageIndex) throws WebClientResponseException {
        return getUserImageRequestCreation(userId, imageType, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer, imageIndex);
    }

    /**
     * Get user profile image.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param userId User id.
     * @param imageType Image type.
     * @param imageIndex Image index.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getUserImageByIndexRequestCreation(UUID userId, ImageType imageType, Integer imageIndex, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'userId' is set
        if (userId == null) {
            throw new WebClientResponseException("Missing the required parameter 'userId' when calling getUserImageByIndex", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'imageType' is set
        if (imageType == null) {
            throw new WebClientResponseException("Missing the required parameter 'imageType' when calling getUserImageByIndex", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'imageIndex' is set
        if (imageIndex == null) {
            throw new WebClientResponseException("Missing the required parameter 'imageIndex' when calling getUserImageByIndex", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("userId", userId);
        pathParams.put("imageType", imageType);
        pathParams.put("imageIndex", imageIndex);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "tag", tag));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "format", format));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxWidth", maxWidth));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxHeight", maxHeight));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "percentPlayed", percentPlayed));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "unplayedCount", unplayedCount));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "width", width));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "height", height));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "quality", quality));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "fillWidth", fillWidth));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "fillHeight", fillHeight));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "cropWhitespace", cropWhitespace));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "addPlayedIndicator", addPlayedIndicator));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "blur", blur));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "backgroundColor", backgroundColor));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "foregroundLayer", foregroundLayer));
        
        final String[] localVarAccepts = { 
            "image/*", "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return apiClient.invokeAPI("/Users/{userId}/Images/{imageType}/{imageIndex}", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get user profile image.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param userId User id.
     * @param imageType Image type.
     * @param imageIndex Image index.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<File> getUserImageByIndex(UUID userId, ImageType imageType, Integer imageIndex, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return getUserImageByIndexRequestCreation(userId, imageType, imageIndex, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer).bodyToMono(localVarReturnType);
    }

    /**
     * Get user profile image.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param userId User id.
     * @param imageType Image type.
     * @param imageIndex Image index.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @return ResponseEntity&lt;File&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<File>> getUserImageByIndexWithHttpInfo(UUID userId, ImageType imageType, Integer imageIndex, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return getUserImageByIndexRequestCreation(userId, imageType, imageIndex, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer).toEntity(localVarReturnType);
    }

    /**
     * Get user profile image.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param userId User id.
     * @param imageType Image type.
     * @param imageIndex Image index.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getUserImageByIndexWithResponseSpec(UUID userId, ImageType imageType, Integer imageIndex, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer) throws WebClientResponseException {
        return getUserImageByIndexRequestCreation(userId, imageType, imageIndex, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer);
    }

    /**
     * Get artist image by name.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param name Artist name.
     * @param imageType Image type.
     * @param imageIndex Image index.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec headArtistImageRequestCreation(String name, ImageType imageType, Integer imageIndex, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'name' is set
        if (name == null) {
            throw new WebClientResponseException("Missing the required parameter 'name' when calling headArtistImage", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'imageType' is set
        if (imageType == null) {
            throw new WebClientResponseException("Missing the required parameter 'imageType' when calling headArtistImage", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'imageIndex' is set
        if (imageIndex == null) {
            throw new WebClientResponseException("Missing the required parameter 'imageIndex' when calling headArtistImage", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("name", name);
        pathParams.put("imageType", imageType);
        pathParams.put("imageIndex", imageIndex);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "tag", tag));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "format", format));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxWidth", maxWidth));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxHeight", maxHeight));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "percentPlayed", percentPlayed));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "unplayedCount", unplayedCount));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "width", width));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "height", height));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "quality", quality));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "fillWidth", fillWidth));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "fillHeight", fillHeight));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "cropWhitespace", cropWhitespace));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "addPlayedIndicator", addPlayedIndicator));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "blur", blur));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "backgroundColor", backgroundColor));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "foregroundLayer", foregroundLayer));
        
        final String[] localVarAccepts = { 
            "image/*", "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return apiClient.invokeAPI("/Artists/{name}/Images/{imageType}/{imageIndex}", HttpMethod.HEAD, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get artist image by name.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param name Artist name.
     * @param imageType Image type.
     * @param imageIndex Image index.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<File> headArtistImage(String name, ImageType imageType, Integer imageIndex, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return headArtistImageRequestCreation(name, imageType, imageIndex, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer).bodyToMono(localVarReturnType);
    }

    /**
     * Get artist image by name.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param name Artist name.
     * @param imageType Image type.
     * @param imageIndex Image index.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @return ResponseEntity&lt;File&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<File>> headArtistImageWithHttpInfo(String name, ImageType imageType, Integer imageIndex, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return headArtistImageRequestCreation(name, imageType, imageIndex, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer).toEntity(localVarReturnType);
    }

    /**
     * Get artist image by name.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param name Artist name.
     * @param imageType Image type.
     * @param imageIndex Image index.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec headArtistImageWithResponseSpec(String name, ImageType imageType, Integer imageIndex, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer) throws WebClientResponseException {
        return headArtistImageRequestCreation(name, imageType, imageIndex, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer);
    }

    /**
     * Get genre image by name.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param name Genre name.
     * @param imageType Image type.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @param imageIndex Image index.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec headGenreImageRequestCreation(String name, ImageType imageType, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer, Integer imageIndex) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'name' is set
        if (name == null) {
            throw new WebClientResponseException("Missing the required parameter 'name' when calling headGenreImage", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'imageType' is set
        if (imageType == null) {
            throw new WebClientResponseException("Missing the required parameter 'imageType' when calling headGenreImage", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("name", name);
        pathParams.put("imageType", imageType);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "tag", tag));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "format", format));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxWidth", maxWidth));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxHeight", maxHeight));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "percentPlayed", percentPlayed));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "unplayedCount", unplayedCount));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "width", width));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "height", height));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "quality", quality));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "fillWidth", fillWidth));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "fillHeight", fillHeight));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "cropWhitespace", cropWhitespace));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "addPlayedIndicator", addPlayedIndicator));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "blur", blur));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "backgroundColor", backgroundColor));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "foregroundLayer", foregroundLayer));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "imageIndex", imageIndex));
        
        final String[] localVarAccepts = { 
            "image/*", "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return apiClient.invokeAPI("/Genres/{name}/Images/{imageType}", HttpMethod.HEAD, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get genre image by name.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param name Genre name.
     * @param imageType Image type.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @param imageIndex Image index.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<File> headGenreImage(String name, ImageType imageType, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer, Integer imageIndex) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return headGenreImageRequestCreation(name, imageType, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer, imageIndex).bodyToMono(localVarReturnType);
    }

    /**
     * Get genre image by name.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param name Genre name.
     * @param imageType Image type.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @param imageIndex Image index.
     * @return ResponseEntity&lt;File&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<File>> headGenreImageWithHttpInfo(String name, ImageType imageType, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer, Integer imageIndex) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return headGenreImageRequestCreation(name, imageType, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer, imageIndex).toEntity(localVarReturnType);
    }

    /**
     * Get genre image by name.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param name Genre name.
     * @param imageType Image type.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @param imageIndex Image index.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec headGenreImageWithResponseSpec(String name, ImageType imageType, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer, Integer imageIndex) throws WebClientResponseException {
        return headGenreImageRequestCreation(name, imageType, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer, imageIndex);
    }

    /**
     * Get genre image by name.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param name Genre name.
     * @param imageType Image type.
     * @param imageIndex Image index.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec headGenreImageByIndexRequestCreation(String name, ImageType imageType, Integer imageIndex, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'name' is set
        if (name == null) {
            throw new WebClientResponseException("Missing the required parameter 'name' when calling headGenreImageByIndex", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'imageType' is set
        if (imageType == null) {
            throw new WebClientResponseException("Missing the required parameter 'imageType' when calling headGenreImageByIndex", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'imageIndex' is set
        if (imageIndex == null) {
            throw new WebClientResponseException("Missing the required parameter 'imageIndex' when calling headGenreImageByIndex", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("name", name);
        pathParams.put("imageType", imageType);
        pathParams.put("imageIndex", imageIndex);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "tag", tag));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "format", format));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxWidth", maxWidth));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxHeight", maxHeight));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "percentPlayed", percentPlayed));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "unplayedCount", unplayedCount));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "width", width));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "height", height));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "quality", quality));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "fillWidth", fillWidth));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "fillHeight", fillHeight));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "cropWhitespace", cropWhitespace));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "addPlayedIndicator", addPlayedIndicator));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "blur", blur));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "backgroundColor", backgroundColor));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "foregroundLayer", foregroundLayer));
        
        final String[] localVarAccepts = { 
            "image/*", "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return apiClient.invokeAPI("/Genres/{name}/Images/{imageType}/{imageIndex}", HttpMethod.HEAD, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get genre image by name.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param name Genre name.
     * @param imageType Image type.
     * @param imageIndex Image index.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<File> headGenreImageByIndex(String name, ImageType imageType, Integer imageIndex, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return headGenreImageByIndexRequestCreation(name, imageType, imageIndex, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer).bodyToMono(localVarReturnType);
    }

    /**
     * Get genre image by name.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param name Genre name.
     * @param imageType Image type.
     * @param imageIndex Image index.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @return ResponseEntity&lt;File&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<File>> headGenreImageByIndexWithHttpInfo(String name, ImageType imageType, Integer imageIndex, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return headGenreImageByIndexRequestCreation(name, imageType, imageIndex, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer).toEntity(localVarReturnType);
    }

    /**
     * Get genre image by name.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param name Genre name.
     * @param imageType Image type.
     * @param imageIndex Image index.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec headGenreImageByIndexWithResponseSpec(String name, ImageType imageType, Integer imageIndex, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer) throws WebClientResponseException {
        return headGenreImageByIndexRequestCreation(name, imageType, imageIndex, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer);
    }

    /**
     * Gets the item&#39;s image.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param itemId Item id.
     * @param imageType Image type.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param format Optional. The MediaBrowser.Model.Drawing.ImageFormat of the returned image.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @param imageIndex Image index.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec headItemImageRequestCreation(UUID itemId, ImageType imageType, Integer maxWidth, Integer maxHeight, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, String tag, Boolean cropWhitespace, ImageFormat format, Boolean addPlayedIndicator, Double percentPlayed, Integer unplayedCount, Integer blur, String backgroundColor, String foregroundLayer, Integer imageIndex) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException("Missing the required parameter 'itemId' when calling headItemImage", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'imageType' is set
        if (imageType == null) {
            throw new WebClientResponseException("Missing the required parameter 'imageType' when calling headItemImage", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("itemId", itemId);
        pathParams.put("imageType", imageType);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxWidth", maxWidth));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxHeight", maxHeight));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "width", width));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "height", height));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "quality", quality));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "fillWidth", fillWidth));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "fillHeight", fillHeight));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "tag", tag));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "cropWhitespace", cropWhitespace));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "format", format));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "addPlayedIndicator", addPlayedIndicator));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "percentPlayed", percentPlayed));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "unplayedCount", unplayedCount));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "blur", blur));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "backgroundColor", backgroundColor));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "foregroundLayer", foregroundLayer));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "imageIndex", imageIndex));
        
        final String[] localVarAccepts = { 
            "image/*", "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return apiClient.invokeAPI("/Items/{itemId}/Images/{imageType}", HttpMethod.HEAD, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets the item&#39;s image.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param itemId Item id.
     * @param imageType Image type.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param format Optional. The MediaBrowser.Model.Drawing.ImageFormat of the returned image.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @param imageIndex Image index.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<File> headItemImage(UUID itemId, ImageType imageType, Integer maxWidth, Integer maxHeight, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, String tag, Boolean cropWhitespace, ImageFormat format, Boolean addPlayedIndicator, Double percentPlayed, Integer unplayedCount, Integer blur, String backgroundColor, String foregroundLayer, Integer imageIndex) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return headItemImageRequestCreation(itemId, imageType, maxWidth, maxHeight, width, height, quality, fillWidth, fillHeight, tag, cropWhitespace, format, addPlayedIndicator, percentPlayed, unplayedCount, blur, backgroundColor, foregroundLayer, imageIndex).bodyToMono(localVarReturnType);
    }

    /**
     * Gets the item&#39;s image.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param itemId Item id.
     * @param imageType Image type.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param format Optional. The MediaBrowser.Model.Drawing.ImageFormat of the returned image.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @param imageIndex Image index.
     * @return ResponseEntity&lt;File&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<File>> headItemImageWithHttpInfo(UUID itemId, ImageType imageType, Integer maxWidth, Integer maxHeight, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, String tag, Boolean cropWhitespace, ImageFormat format, Boolean addPlayedIndicator, Double percentPlayed, Integer unplayedCount, Integer blur, String backgroundColor, String foregroundLayer, Integer imageIndex) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return headItemImageRequestCreation(itemId, imageType, maxWidth, maxHeight, width, height, quality, fillWidth, fillHeight, tag, cropWhitespace, format, addPlayedIndicator, percentPlayed, unplayedCount, blur, backgroundColor, foregroundLayer, imageIndex).toEntity(localVarReturnType);
    }

    /**
     * Gets the item&#39;s image.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param itemId Item id.
     * @param imageType Image type.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param format Optional. The MediaBrowser.Model.Drawing.ImageFormat of the returned image.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @param imageIndex Image index.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec headItemImageWithResponseSpec(UUID itemId, ImageType imageType, Integer maxWidth, Integer maxHeight, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, String tag, Boolean cropWhitespace, ImageFormat format, Boolean addPlayedIndicator, Double percentPlayed, Integer unplayedCount, Integer blur, String backgroundColor, String foregroundLayer, Integer imageIndex) throws WebClientResponseException {
        return headItemImageRequestCreation(itemId, imageType, maxWidth, maxHeight, width, height, quality, fillWidth, fillHeight, tag, cropWhitespace, format, addPlayedIndicator, percentPlayed, unplayedCount, blur, backgroundColor, foregroundLayer, imageIndex);
    }

    /**
     * Gets the item&#39;s image.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param itemId Item id.
     * @param imageType Image type.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param imageIndex Image index.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec headItemImage2RequestCreation(UUID itemId, ImageType imageType, Integer maxWidth, Integer maxHeight, String tag, ImageFormat format, Double percentPlayed, Integer unplayedCount, Integer imageIndex, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException("Missing the required parameter 'itemId' when calling headItemImage2", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'imageType' is set
        if (imageType == null) {
            throw new WebClientResponseException("Missing the required parameter 'imageType' when calling headItemImage2", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'maxWidth' is set
        if (maxWidth == null) {
            throw new WebClientResponseException("Missing the required parameter 'maxWidth' when calling headItemImage2", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'maxHeight' is set
        if (maxHeight == null) {
            throw new WebClientResponseException("Missing the required parameter 'maxHeight' when calling headItemImage2", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'tag' is set
        if (tag == null) {
            throw new WebClientResponseException("Missing the required parameter 'tag' when calling headItemImage2", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'format' is set
        if (format == null) {
            throw new WebClientResponseException("Missing the required parameter 'format' when calling headItemImage2", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'percentPlayed' is set
        if (percentPlayed == null) {
            throw new WebClientResponseException("Missing the required parameter 'percentPlayed' when calling headItemImage2", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'unplayedCount' is set
        if (unplayedCount == null) {
            throw new WebClientResponseException("Missing the required parameter 'unplayedCount' when calling headItemImage2", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'imageIndex' is set
        if (imageIndex == null) {
            throw new WebClientResponseException("Missing the required parameter 'imageIndex' when calling headItemImage2", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("itemId", itemId);
        pathParams.put("imageType", imageType);
        pathParams.put("maxWidth", maxWidth);
        pathParams.put("maxHeight", maxHeight);
        pathParams.put("tag", tag);
        pathParams.put("format", format);
        pathParams.put("percentPlayed", percentPlayed);
        pathParams.put("unplayedCount", unplayedCount);
        pathParams.put("imageIndex", imageIndex);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "width", width));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "height", height));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "quality", quality));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "fillWidth", fillWidth));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "fillHeight", fillHeight));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "cropWhitespace", cropWhitespace));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "addPlayedIndicator", addPlayedIndicator));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "blur", blur));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "backgroundColor", backgroundColor));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "foregroundLayer", foregroundLayer));
        
        final String[] localVarAccepts = { 
            "image/*", "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return apiClient.invokeAPI("/Items/{itemId}/Images/{imageType}/{imageIndex}/{tag}/{format}/{maxWidth}/{maxHeight}/{percentPlayed}/{unplayedCount}", HttpMethod.HEAD, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets the item&#39;s image.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param itemId Item id.
     * @param imageType Image type.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param imageIndex Image index.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<File> headItemImage2(UUID itemId, ImageType imageType, Integer maxWidth, Integer maxHeight, String tag, ImageFormat format, Double percentPlayed, Integer unplayedCount, Integer imageIndex, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return headItemImage2RequestCreation(itemId, imageType, maxWidth, maxHeight, tag, format, percentPlayed, unplayedCount, imageIndex, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer).bodyToMono(localVarReturnType);
    }

    /**
     * Gets the item&#39;s image.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param itemId Item id.
     * @param imageType Image type.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param imageIndex Image index.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @return ResponseEntity&lt;File&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<File>> headItemImage2WithHttpInfo(UUID itemId, ImageType imageType, Integer maxWidth, Integer maxHeight, String tag, ImageFormat format, Double percentPlayed, Integer unplayedCount, Integer imageIndex, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return headItemImage2RequestCreation(itemId, imageType, maxWidth, maxHeight, tag, format, percentPlayed, unplayedCount, imageIndex, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer).toEntity(localVarReturnType);
    }

    /**
     * Gets the item&#39;s image.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param itemId Item id.
     * @param imageType Image type.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param imageIndex Image index.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec headItemImage2WithResponseSpec(UUID itemId, ImageType imageType, Integer maxWidth, Integer maxHeight, String tag, ImageFormat format, Double percentPlayed, Integer unplayedCount, Integer imageIndex, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer) throws WebClientResponseException {
        return headItemImage2RequestCreation(itemId, imageType, maxWidth, maxHeight, tag, format, percentPlayed, unplayedCount, imageIndex, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer);
    }

    /**
     * Gets the item&#39;s image.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param itemId Item id.
     * @param imageType Image type.
     * @param imageIndex Image index.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param format Optional. The MediaBrowser.Model.Drawing.ImageFormat of the returned image.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec headItemImageByIndexRequestCreation(UUID itemId, ImageType imageType, Integer imageIndex, Integer maxWidth, Integer maxHeight, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, String tag, Boolean cropWhitespace, ImageFormat format, Boolean addPlayedIndicator, Double percentPlayed, Integer unplayedCount, Integer blur, String backgroundColor, String foregroundLayer) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException("Missing the required parameter 'itemId' when calling headItemImageByIndex", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'imageType' is set
        if (imageType == null) {
            throw new WebClientResponseException("Missing the required parameter 'imageType' when calling headItemImageByIndex", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'imageIndex' is set
        if (imageIndex == null) {
            throw new WebClientResponseException("Missing the required parameter 'imageIndex' when calling headItemImageByIndex", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("itemId", itemId);
        pathParams.put("imageType", imageType);
        pathParams.put("imageIndex", imageIndex);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxWidth", maxWidth));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxHeight", maxHeight));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "width", width));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "height", height));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "quality", quality));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "fillWidth", fillWidth));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "fillHeight", fillHeight));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "tag", tag));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "cropWhitespace", cropWhitespace));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "format", format));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "addPlayedIndicator", addPlayedIndicator));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "percentPlayed", percentPlayed));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "unplayedCount", unplayedCount));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "blur", blur));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "backgroundColor", backgroundColor));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "foregroundLayer", foregroundLayer));
        
        final String[] localVarAccepts = { 
            "image/*", "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return apiClient.invokeAPI("/Items/{itemId}/Images/{imageType}/{imageIndex}", HttpMethod.HEAD, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets the item&#39;s image.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param itemId Item id.
     * @param imageType Image type.
     * @param imageIndex Image index.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param format Optional. The MediaBrowser.Model.Drawing.ImageFormat of the returned image.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<File> headItemImageByIndex(UUID itemId, ImageType imageType, Integer imageIndex, Integer maxWidth, Integer maxHeight, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, String tag, Boolean cropWhitespace, ImageFormat format, Boolean addPlayedIndicator, Double percentPlayed, Integer unplayedCount, Integer blur, String backgroundColor, String foregroundLayer) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return headItemImageByIndexRequestCreation(itemId, imageType, imageIndex, maxWidth, maxHeight, width, height, quality, fillWidth, fillHeight, tag, cropWhitespace, format, addPlayedIndicator, percentPlayed, unplayedCount, blur, backgroundColor, foregroundLayer).bodyToMono(localVarReturnType);
    }

    /**
     * Gets the item&#39;s image.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param itemId Item id.
     * @param imageType Image type.
     * @param imageIndex Image index.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param format Optional. The MediaBrowser.Model.Drawing.ImageFormat of the returned image.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @return ResponseEntity&lt;File&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<File>> headItemImageByIndexWithHttpInfo(UUID itemId, ImageType imageType, Integer imageIndex, Integer maxWidth, Integer maxHeight, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, String tag, Boolean cropWhitespace, ImageFormat format, Boolean addPlayedIndicator, Double percentPlayed, Integer unplayedCount, Integer blur, String backgroundColor, String foregroundLayer) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return headItemImageByIndexRequestCreation(itemId, imageType, imageIndex, maxWidth, maxHeight, width, height, quality, fillWidth, fillHeight, tag, cropWhitespace, format, addPlayedIndicator, percentPlayed, unplayedCount, blur, backgroundColor, foregroundLayer).toEntity(localVarReturnType);
    }

    /**
     * Gets the item&#39;s image.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param itemId Item id.
     * @param imageType Image type.
     * @param imageIndex Image index.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param format Optional. The MediaBrowser.Model.Drawing.ImageFormat of the returned image.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec headItemImageByIndexWithResponseSpec(UUID itemId, ImageType imageType, Integer imageIndex, Integer maxWidth, Integer maxHeight, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, String tag, Boolean cropWhitespace, ImageFormat format, Boolean addPlayedIndicator, Double percentPlayed, Integer unplayedCount, Integer blur, String backgroundColor, String foregroundLayer) throws WebClientResponseException {
        return headItemImageByIndexRequestCreation(itemId, imageType, imageIndex, maxWidth, maxHeight, width, height, quality, fillWidth, fillHeight, tag, cropWhitespace, format, addPlayedIndicator, percentPlayed, unplayedCount, blur, backgroundColor, foregroundLayer);
    }

    /**
     * Get music genre image by name.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param name Music genre name.
     * @param imageType Image type.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @param imageIndex Image index.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec headMusicGenreImageRequestCreation(String name, ImageType imageType, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer, Integer imageIndex) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'name' is set
        if (name == null) {
            throw new WebClientResponseException("Missing the required parameter 'name' when calling headMusicGenreImage", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'imageType' is set
        if (imageType == null) {
            throw new WebClientResponseException("Missing the required parameter 'imageType' when calling headMusicGenreImage", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("name", name);
        pathParams.put("imageType", imageType);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "tag", tag));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "format", format));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxWidth", maxWidth));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxHeight", maxHeight));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "percentPlayed", percentPlayed));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "unplayedCount", unplayedCount));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "width", width));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "height", height));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "quality", quality));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "fillWidth", fillWidth));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "fillHeight", fillHeight));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "cropWhitespace", cropWhitespace));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "addPlayedIndicator", addPlayedIndicator));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "blur", blur));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "backgroundColor", backgroundColor));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "foregroundLayer", foregroundLayer));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "imageIndex", imageIndex));
        
        final String[] localVarAccepts = { 
            "image/*", "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return apiClient.invokeAPI("/MusicGenres/{name}/Images/{imageType}", HttpMethod.HEAD, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get music genre image by name.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param name Music genre name.
     * @param imageType Image type.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @param imageIndex Image index.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<File> headMusicGenreImage(String name, ImageType imageType, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer, Integer imageIndex) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return headMusicGenreImageRequestCreation(name, imageType, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer, imageIndex).bodyToMono(localVarReturnType);
    }

    /**
     * Get music genre image by name.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param name Music genre name.
     * @param imageType Image type.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @param imageIndex Image index.
     * @return ResponseEntity&lt;File&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<File>> headMusicGenreImageWithHttpInfo(String name, ImageType imageType, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer, Integer imageIndex) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return headMusicGenreImageRequestCreation(name, imageType, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer, imageIndex).toEntity(localVarReturnType);
    }

    /**
     * Get music genre image by name.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param name Music genre name.
     * @param imageType Image type.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @param imageIndex Image index.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec headMusicGenreImageWithResponseSpec(String name, ImageType imageType, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer, Integer imageIndex) throws WebClientResponseException {
        return headMusicGenreImageRequestCreation(name, imageType, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer, imageIndex);
    }

    /**
     * Get music genre image by name.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param name Music genre name.
     * @param imageType Image type.
     * @param imageIndex Image index.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec headMusicGenreImageByIndexRequestCreation(String name, ImageType imageType, Integer imageIndex, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'name' is set
        if (name == null) {
            throw new WebClientResponseException("Missing the required parameter 'name' when calling headMusicGenreImageByIndex", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'imageType' is set
        if (imageType == null) {
            throw new WebClientResponseException("Missing the required parameter 'imageType' when calling headMusicGenreImageByIndex", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'imageIndex' is set
        if (imageIndex == null) {
            throw new WebClientResponseException("Missing the required parameter 'imageIndex' when calling headMusicGenreImageByIndex", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("name", name);
        pathParams.put("imageType", imageType);
        pathParams.put("imageIndex", imageIndex);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "tag", tag));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "format", format));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxWidth", maxWidth));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxHeight", maxHeight));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "percentPlayed", percentPlayed));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "unplayedCount", unplayedCount));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "width", width));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "height", height));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "quality", quality));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "fillWidth", fillWidth));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "fillHeight", fillHeight));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "cropWhitespace", cropWhitespace));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "addPlayedIndicator", addPlayedIndicator));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "blur", blur));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "backgroundColor", backgroundColor));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "foregroundLayer", foregroundLayer));
        
        final String[] localVarAccepts = { 
            "image/*", "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return apiClient.invokeAPI("/MusicGenres/{name}/Images/{imageType}/{imageIndex}", HttpMethod.HEAD, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get music genre image by name.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param name Music genre name.
     * @param imageType Image type.
     * @param imageIndex Image index.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<File> headMusicGenreImageByIndex(String name, ImageType imageType, Integer imageIndex, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return headMusicGenreImageByIndexRequestCreation(name, imageType, imageIndex, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer).bodyToMono(localVarReturnType);
    }

    /**
     * Get music genre image by name.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param name Music genre name.
     * @param imageType Image type.
     * @param imageIndex Image index.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @return ResponseEntity&lt;File&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<File>> headMusicGenreImageByIndexWithHttpInfo(String name, ImageType imageType, Integer imageIndex, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return headMusicGenreImageByIndexRequestCreation(name, imageType, imageIndex, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer).toEntity(localVarReturnType);
    }

    /**
     * Get music genre image by name.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param name Music genre name.
     * @param imageType Image type.
     * @param imageIndex Image index.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec headMusicGenreImageByIndexWithResponseSpec(String name, ImageType imageType, Integer imageIndex, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer) throws WebClientResponseException {
        return headMusicGenreImageByIndexRequestCreation(name, imageType, imageIndex, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer);
    }

    /**
     * Get person image by name.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param name Person name.
     * @param imageType Image type.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @param imageIndex Image index.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec headPersonImageRequestCreation(String name, ImageType imageType, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer, Integer imageIndex) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'name' is set
        if (name == null) {
            throw new WebClientResponseException("Missing the required parameter 'name' when calling headPersonImage", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'imageType' is set
        if (imageType == null) {
            throw new WebClientResponseException("Missing the required parameter 'imageType' when calling headPersonImage", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("name", name);
        pathParams.put("imageType", imageType);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "tag", tag));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "format", format));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxWidth", maxWidth));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxHeight", maxHeight));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "percentPlayed", percentPlayed));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "unplayedCount", unplayedCount));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "width", width));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "height", height));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "quality", quality));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "fillWidth", fillWidth));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "fillHeight", fillHeight));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "cropWhitespace", cropWhitespace));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "addPlayedIndicator", addPlayedIndicator));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "blur", blur));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "backgroundColor", backgroundColor));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "foregroundLayer", foregroundLayer));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "imageIndex", imageIndex));
        
        final String[] localVarAccepts = { 
            "image/*", "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return apiClient.invokeAPI("/Persons/{name}/Images/{imageType}", HttpMethod.HEAD, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get person image by name.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param name Person name.
     * @param imageType Image type.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @param imageIndex Image index.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<File> headPersonImage(String name, ImageType imageType, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer, Integer imageIndex) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return headPersonImageRequestCreation(name, imageType, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer, imageIndex).bodyToMono(localVarReturnType);
    }

    /**
     * Get person image by name.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param name Person name.
     * @param imageType Image type.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @param imageIndex Image index.
     * @return ResponseEntity&lt;File&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<File>> headPersonImageWithHttpInfo(String name, ImageType imageType, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer, Integer imageIndex) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return headPersonImageRequestCreation(name, imageType, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer, imageIndex).toEntity(localVarReturnType);
    }

    /**
     * Get person image by name.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param name Person name.
     * @param imageType Image type.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @param imageIndex Image index.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec headPersonImageWithResponseSpec(String name, ImageType imageType, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer, Integer imageIndex) throws WebClientResponseException {
        return headPersonImageRequestCreation(name, imageType, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer, imageIndex);
    }

    /**
     * Get person image by name.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param name Person name.
     * @param imageType Image type.
     * @param imageIndex Image index.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec headPersonImageByIndexRequestCreation(String name, ImageType imageType, Integer imageIndex, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'name' is set
        if (name == null) {
            throw new WebClientResponseException("Missing the required parameter 'name' when calling headPersonImageByIndex", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'imageType' is set
        if (imageType == null) {
            throw new WebClientResponseException("Missing the required parameter 'imageType' when calling headPersonImageByIndex", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'imageIndex' is set
        if (imageIndex == null) {
            throw new WebClientResponseException("Missing the required parameter 'imageIndex' when calling headPersonImageByIndex", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("name", name);
        pathParams.put("imageType", imageType);
        pathParams.put("imageIndex", imageIndex);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "tag", tag));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "format", format));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxWidth", maxWidth));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxHeight", maxHeight));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "percentPlayed", percentPlayed));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "unplayedCount", unplayedCount));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "width", width));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "height", height));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "quality", quality));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "fillWidth", fillWidth));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "fillHeight", fillHeight));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "cropWhitespace", cropWhitespace));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "addPlayedIndicator", addPlayedIndicator));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "blur", blur));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "backgroundColor", backgroundColor));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "foregroundLayer", foregroundLayer));
        
        final String[] localVarAccepts = { 
            "image/*", "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return apiClient.invokeAPI("/Persons/{name}/Images/{imageType}/{imageIndex}", HttpMethod.HEAD, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get person image by name.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param name Person name.
     * @param imageType Image type.
     * @param imageIndex Image index.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<File> headPersonImageByIndex(String name, ImageType imageType, Integer imageIndex, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return headPersonImageByIndexRequestCreation(name, imageType, imageIndex, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer).bodyToMono(localVarReturnType);
    }

    /**
     * Get person image by name.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param name Person name.
     * @param imageType Image type.
     * @param imageIndex Image index.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @return ResponseEntity&lt;File&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<File>> headPersonImageByIndexWithHttpInfo(String name, ImageType imageType, Integer imageIndex, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return headPersonImageByIndexRequestCreation(name, imageType, imageIndex, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer).toEntity(localVarReturnType);
    }

    /**
     * Get person image by name.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param name Person name.
     * @param imageType Image type.
     * @param imageIndex Image index.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec headPersonImageByIndexWithResponseSpec(String name, ImageType imageType, Integer imageIndex, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer) throws WebClientResponseException {
        return headPersonImageByIndexRequestCreation(name, imageType, imageIndex, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer);
    }

    /**
     * Get studio image by name.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param name Studio name.
     * @param imageType Image type.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @param imageIndex Image index.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec headStudioImageRequestCreation(String name, ImageType imageType, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer, Integer imageIndex) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'name' is set
        if (name == null) {
            throw new WebClientResponseException("Missing the required parameter 'name' when calling headStudioImage", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'imageType' is set
        if (imageType == null) {
            throw new WebClientResponseException("Missing the required parameter 'imageType' when calling headStudioImage", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("name", name);
        pathParams.put("imageType", imageType);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "tag", tag));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "format", format));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxWidth", maxWidth));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxHeight", maxHeight));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "percentPlayed", percentPlayed));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "unplayedCount", unplayedCount));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "width", width));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "height", height));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "quality", quality));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "fillWidth", fillWidth));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "fillHeight", fillHeight));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "cropWhitespace", cropWhitespace));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "addPlayedIndicator", addPlayedIndicator));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "blur", blur));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "backgroundColor", backgroundColor));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "foregroundLayer", foregroundLayer));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "imageIndex", imageIndex));
        
        final String[] localVarAccepts = { 
            "image/*", "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return apiClient.invokeAPI("/Studios/{name}/Images/{imageType}", HttpMethod.HEAD, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get studio image by name.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param name Studio name.
     * @param imageType Image type.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @param imageIndex Image index.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<File> headStudioImage(String name, ImageType imageType, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer, Integer imageIndex) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return headStudioImageRequestCreation(name, imageType, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer, imageIndex).bodyToMono(localVarReturnType);
    }

    /**
     * Get studio image by name.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param name Studio name.
     * @param imageType Image type.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @param imageIndex Image index.
     * @return ResponseEntity&lt;File&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<File>> headStudioImageWithHttpInfo(String name, ImageType imageType, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer, Integer imageIndex) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return headStudioImageRequestCreation(name, imageType, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer, imageIndex).toEntity(localVarReturnType);
    }

    /**
     * Get studio image by name.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param name Studio name.
     * @param imageType Image type.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @param imageIndex Image index.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec headStudioImageWithResponseSpec(String name, ImageType imageType, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer, Integer imageIndex) throws WebClientResponseException {
        return headStudioImageRequestCreation(name, imageType, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer, imageIndex);
    }

    /**
     * Get studio image by name.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param name Studio name.
     * @param imageType Image type.
     * @param imageIndex Image index.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec headStudioImageByIndexRequestCreation(String name, ImageType imageType, Integer imageIndex, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'name' is set
        if (name == null) {
            throw new WebClientResponseException("Missing the required parameter 'name' when calling headStudioImageByIndex", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'imageType' is set
        if (imageType == null) {
            throw new WebClientResponseException("Missing the required parameter 'imageType' when calling headStudioImageByIndex", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'imageIndex' is set
        if (imageIndex == null) {
            throw new WebClientResponseException("Missing the required parameter 'imageIndex' when calling headStudioImageByIndex", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("name", name);
        pathParams.put("imageType", imageType);
        pathParams.put("imageIndex", imageIndex);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "tag", tag));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "format", format));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxWidth", maxWidth));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxHeight", maxHeight));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "percentPlayed", percentPlayed));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "unplayedCount", unplayedCount));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "width", width));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "height", height));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "quality", quality));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "fillWidth", fillWidth));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "fillHeight", fillHeight));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "cropWhitespace", cropWhitespace));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "addPlayedIndicator", addPlayedIndicator));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "blur", blur));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "backgroundColor", backgroundColor));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "foregroundLayer", foregroundLayer));
        
        final String[] localVarAccepts = { 
            "image/*", "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return apiClient.invokeAPI("/Studios/{name}/Images/{imageType}/{imageIndex}", HttpMethod.HEAD, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get studio image by name.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param name Studio name.
     * @param imageType Image type.
     * @param imageIndex Image index.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<File> headStudioImageByIndex(String name, ImageType imageType, Integer imageIndex, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return headStudioImageByIndexRequestCreation(name, imageType, imageIndex, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer).bodyToMono(localVarReturnType);
    }

    /**
     * Get studio image by name.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param name Studio name.
     * @param imageType Image type.
     * @param imageIndex Image index.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @return ResponseEntity&lt;File&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<File>> headStudioImageByIndexWithHttpInfo(String name, ImageType imageType, Integer imageIndex, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return headStudioImageByIndexRequestCreation(name, imageType, imageIndex, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer).toEntity(localVarReturnType);
    }

    /**
     * Get studio image by name.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param name Studio name.
     * @param imageType Image type.
     * @param imageIndex Image index.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec headStudioImageByIndexWithResponseSpec(String name, ImageType imageType, Integer imageIndex, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer) throws WebClientResponseException {
        return headStudioImageByIndexRequestCreation(name, imageType, imageIndex, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer);
    }

    /**
     * Get user profile image.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param userId User id.
     * @param imageType Image type.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @param imageIndex Image index.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec headUserImageRequestCreation(UUID userId, ImageType imageType, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer, Integer imageIndex) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'userId' is set
        if (userId == null) {
            throw new WebClientResponseException("Missing the required parameter 'userId' when calling headUserImage", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'imageType' is set
        if (imageType == null) {
            throw new WebClientResponseException("Missing the required parameter 'imageType' when calling headUserImage", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("userId", userId);
        pathParams.put("imageType", imageType);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "tag", tag));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "format", format));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxWidth", maxWidth));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxHeight", maxHeight));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "percentPlayed", percentPlayed));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "unplayedCount", unplayedCount));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "width", width));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "height", height));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "quality", quality));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "fillWidth", fillWidth));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "fillHeight", fillHeight));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "cropWhitespace", cropWhitespace));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "addPlayedIndicator", addPlayedIndicator));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "blur", blur));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "backgroundColor", backgroundColor));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "foregroundLayer", foregroundLayer));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "imageIndex", imageIndex));
        
        final String[] localVarAccepts = { 
            "image/*", "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return apiClient.invokeAPI("/Users/{userId}/Images/{imageType}", HttpMethod.HEAD, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get user profile image.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param userId User id.
     * @param imageType Image type.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @param imageIndex Image index.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<File> headUserImage(UUID userId, ImageType imageType, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer, Integer imageIndex) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return headUserImageRequestCreation(userId, imageType, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer, imageIndex).bodyToMono(localVarReturnType);
    }

    /**
     * Get user profile image.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param userId User id.
     * @param imageType Image type.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @param imageIndex Image index.
     * @return ResponseEntity&lt;File&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<File>> headUserImageWithHttpInfo(UUID userId, ImageType imageType, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer, Integer imageIndex) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return headUserImageRequestCreation(userId, imageType, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer, imageIndex).toEntity(localVarReturnType);
    }

    /**
     * Get user profile image.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param userId User id.
     * @param imageType Image type.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @param imageIndex Image index.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec headUserImageWithResponseSpec(UUID userId, ImageType imageType, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer, Integer imageIndex) throws WebClientResponseException {
        return headUserImageRequestCreation(userId, imageType, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer, imageIndex);
    }

    /**
     * Get user profile image.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param userId User id.
     * @param imageType Image type.
     * @param imageIndex Image index.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec headUserImageByIndexRequestCreation(UUID userId, ImageType imageType, Integer imageIndex, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'userId' is set
        if (userId == null) {
            throw new WebClientResponseException("Missing the required parameter 'userId' when calling headUserImageByIndex", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'imageType' is set
        if (imageType == null) {
            throw new WebClientResponseException("Missing the required parameter 'imageType' when calling headUserImageByIndex", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'imageIndex' is set
        if (imageIndex == null) {
            throw new WebClientResponseException("Missing the required parameter 'imageIndex' when calling headUserImageByIndex", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("userId", userId);
        pathParams.put("imageType", imageType);
        pathParams.put("imageIndex", imageIndex);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "tag", tag));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "format", format));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxWidth", maxWidth));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxHeight", maxHeight));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "percentPlayed", percentPlayed));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "unplayedCount", unplayedCount));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "width", width));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "height", height));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "quality", quality));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "fillWidth", fillWidth));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "fillHeight", fillHeight));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "cropWhitespace", cropWhitespace));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "addPlayedIndicator", addPlayedIndicator));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "blur", blur));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "backgroundColor", backgroundColor));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "foregroundLayer", foregroundLayer));
        
        final String[] localVarAccepts = { 
            "image/*", "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return apiClient.invokeAPI("/Users/{userId}/Images/{imageType}/{imageIndex}", HttpMethod.HEAD, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get user profile image.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param userId User id.
     * @param imageType Image type.
     * @param imageIndex Image index.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<File> headUserImageByIndex(UUID userId, ImageType imageType, Integer imageIndex, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return headUserImageByIndexRequestCreation(userId, imageType, imageIndex, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer).bodyToMono(localVarReturnType);
    }

    /**
     * Get user profile image.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param userId User id.
     * @param imageType Image type.
     * @param imageIndex Image index.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @return ResponseEntity&lt;File&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<File>> headUserImageByIndexWithHttpInfo(UUID userId, ImageType imageType, Integer imageIndex, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return headUserImageByIndexRequestCreation(userId, imageType, imageIndex, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer).toEntity(localVarReturnType);
    }

    /**
     * Get user profile image.
     * 
     * <p><b>200</b> - Image stream returned.
     * <p><b>404</b> - Item not found.
     * @param userId User id.
     * @param imageType Image type.
     * @param imageIndex Image index.
     * @param tag Optional. Supply the cache tag from the item object to receive strong caching headers.
     * @param format Determines the output format of the image - original,gif,jpg,png.
     * @param maxWidth The maximum image width to return.
     * @param maxHeight The maximum image height to return.
     * @param percentPlayed Optional. Percent to render for the percent played overlay.
     * @param unplayedCount Optional. Unplayed count overlay to render.
     * @param width The fixed image width to return.
     * @param height The fixed image height to return.
     * @param quality Optional. Quality setting, from 0-100. Defaults to 90 and should suffice in most cases.
     * @param fillWidth Width of box to fill.
     * @param fillHeight Height of box to fill.
     * @param cropWhitespace Optional. Specify if whitespace should be cropped out of the image. True/False. If unspecified, whitespace will be cropped from logos and clear art.
     * @param addPlayedIndicator Optional. Add a played indicator.
     * @param blur Optional. Blur image.
     * @param backgroundColor Optional. Apply a background color for transparent images.
     * @param foregroundLayer Optional. Apply a foreground layer on top of the image.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec headUserImageByIndexWithResponseSpec(UUID userId, ImageType imageType, Integer imageIndex, String tag, ImageFormat format, Integer maxWidth, Integer maxHeight, Double percentPlayed, Integer unplayedCount, Integer width, Integer height, Integer quality, Integer fillWidth, Integer fillHeight, Boolean cropWhitespace, Boolean addPlayedIndicator, Integer blur, String backgroundColor, String foregroundLayer) throws WebClientResponseException {
        return headUserImageByIndexRequestCreation(userId, imageType, imageIndex, tag, format, maxWidth, maxHeight, percentPlayed, unplayedCount, width, height, quality, fillWidth, fillHeight, cropWhitespace, addPlayedIndicator, blur, backgroundColor, foregroundLayer);
    }

    /**
     * Sets the user image.
     * 
     * <p><b>204</b> - Image updated.
     * <p><b>403</b> - User does not have permission to delete the image.
     * <p><b>401</b> - Unauthorized
     * @param userId User Id.
     * @param imageType (Unused) Image type.
     * @param index (Unused) Image index.
     * @param body The body parameter
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec postUserImageRequestCreation(UUID userId, ImageType imageType, Integer index, File body) throws WebClientResponseException {
        Object postBody = body;
        // verify the required parameter 'userId' is set
        if (userId == null) {
            throw new WebClientResponseException("Missing the required parameter 'userId' when calling postUserImage", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'imageType' is set
        if (imageType == null) {
            throw new WebClientResponseException("Missing the required parameter 'imageType' when calling postUserImage", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("userId", userId);
        pathParams.put("imageType", imageType);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "index", index));
        
        final String[] localVarAccepts = { 
            "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { 
            "image/*"
        };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return apiClient.invokeAPI("/Users/{userId}/Images/{imageType}", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Sets the user image.
     * 
     * <p><b>204</b> - Image updated.
     * <p><b>403</b> - User does not have permission to delete the image.
     * <p><b>401</b> - Unauthorized
     * @param userId User Id.
     * @param imageType (Unused) Image type.
     * @param index (Unused) Image index.
     * @param body The body parameter
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> postUserImage(UUID userId, ImageType imageType, Integer index, File body) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return postUserImageRequestCreation(userId, imageType, index, body).bodyToMono(localVarReturnType);
    }

    /**
     * Sets the user image.
     * 
     * <p><b>204</b> - Image updated.
     * <p><b>403</b> - User does not have permission to delete the image.
     * <p><b>401</b> - Unauthorized
     * @param userId User Id.
     * @param imageType (Unused) Image type.
     * @param index (Unused) Image index.
     * @param body The body parameter
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> postUserImageWithHttpInfo(UUID userId, ImageType imageType, Integer index, File body) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return postUserImageRequestCreation(userId, imageType, index, body).toEntity(localVarReturnType);
    }

    /**
     * Sets the user image.
     * 
     * <p><b>204</b> - Image updated.
     * <p><b>403</b> - User does not have permission to delete the image.
     * <p><b>401</b> - Unauthorized
     * @param userId User Id.
     * @param imageType (Unused) Image type.
     * @param index (Unused) Image index.
     * @param body The body parameter
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec postUserImageWithResponseSpec(UUID userId, ImageType imageType, Integer index, File body) throws WebClientResponseException {
        return postUserImageRequestCreation(userId, imageType, index, body);
    }

    /**
     * Sets the user image.
     * 
     * <p><b>204</b> - Image updated.
     * <p><b>403</b> - User does not have permission to delete the image.
     * <p><b>401</b> - Unauthorized
     * @param userId User Id.
     * @param imageType (Unused) Image type.
     * @param index (Unused) Image index.
     * @param body The body parameter
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec postUserImageByIndexRequestCreation(UUID userId, ImageType imageType, Integer index, File body) throws WebClientResponseException {
        Object postBody = body;
        // verify the required parameter 'userId' is set
        if (userId == null) {
            throw new WebClientResponseException("Missing the required parameter 'userId' when calling postUserImageByIndex", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'imageType' is set
        if (imageType == null) {
            throw new WebClientResponseException("Missing the required parameter 'imageType' when calling postUserImageByIndex", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'index' is set
        if (index == null) {
            throw new WebClientResponseException("Missing the required parameter 'index' when calling postUserImageByIndex", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("userId", userId);
        pathParams.put("imageType", imageType);
        pathParams.put("index", index);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { 
            "image/*"
        };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return apiClient.invokeAPI("/Users/{userId}/Images/{imageType}/{index}", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Sets the user image.
     * 
     * <p><b>204</b> - Image updated.
     * <p><b>403</b> - User does not have permission to delete the image.
     * <p><b>401</b> - Unauthorized
     * @param userId User Id.
     * @param imageType (Unused) Image type.
     * @param index (Unused) Image index.
     * @param body The body parameter
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> postUserImageByIndex(UUID userId, ImageType imageType, Integer index, File body) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return postUserImageByIndexRequestCreation(userId, imageType, index, body).bodyToMono(localVarReturnType);
    }

    /**
     * Sets the user image.
     * 
     * <p><b>204</b> - Image updated.
     * <p><b>403</b> - User does not have permission to delete the image.
     * <p><b>401</b> - Unauthorized
     * @param userId User Id.
     * @param imageType (Unused) Image type.
     * @param index (Unused) Image index.
     * @param body The body parameter
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> postUserImageByIndexWithHttpInfo(UUID userId, ImageType imageType, Integer index, File body) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return postUserImageByIndexRequestCreation(userId, imageType, index, body).toEntity(localVarReturnType);
    }

    /**
     * Sets the user image.
     * 
     * <p><b>204</b> - Image updated.
     * <p><b>403</b> - User does not have permission to delete the image.
     * <p><b>401</b> - Unauthorized
     * @param userId User Id.
     * @param imageType (Unused) Image type.
     * @param index (Unused) Image index.
     * @param body The body parameter
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec postUserImageByIndexWithResponseSpec(UUID userId, ImageType imageType, Integer index, File body) throws WebClientResponseException {
        return postUserImageByIndexRequestCreation(userId, imageType, index, body);
    }

    /**
     * Set item image.
     * 
     * <p><b>204</b> - Image saved.
     * <p><b>404</b> - Item not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId Item id.
     * @param imageType Image type.
     * @param body The body parameter
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec setItemImageRequestCreation(UUID itemId, ImageType imageType, File body) throws WebClientResponseException {
        Object postBody = body;
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException("Missing the required parameter 'itemId' when calling setItemImage", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'imageType' is set
        if (imageType == null) {
            throw new WebClientResponseException("Missing the required parameter 'imageType' when calling setItemImage", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("itemId", itemId);
        pathParams.put("imageType", imageType);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { 
            "image/*"
        };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return apiClient.invokeAPI("/Items/{itemId}/Images/{imageType}", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Set item image.
     * 
     * <p><b>204</b> - Image saved.
     * <p><b>404</b> - Item not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId Item id.
     * @param imageType Image type.
     * @param body The body parameter
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> setItemImage(UUID itemId, ImageType imageType, File body) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return setItemImageRequestCreation(itemId, imageType, body).bodyToMono(localVarReturnType);
    }

    /**
     * Set item image.
     * 
     * <p><b>204</b> - Image saved.
     * <p><b>404</b> - Item not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId Item id.
     * @param imageType Image type.
     * @param body The body parameter
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> setItemImageWithHttpInfo(UUID itemId, ImageType imageType, File body) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return setItemImageRequestCreation(itemId, imageType, body).toEntity(localVarReturnType);
    }

    /**
     * Set item image.
     * 
     * <p><b>204</b> - Image saved.
     * <p><b>404</b> - Item not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId Item id.
     * @param imageType Image type.
     * @param body The body parameter
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec setItemImageWithResponseSpec(UUID itemId, ImageType imageType, File body) throws WebClientResponseException {
        return setItemImageRequestCreation(itemId, imageType, body);
    }

    /**
     * Set item image.
     * 
     * <p><b>204</b> - Image saved.
     * <p><b>404</b> - Item not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId Item id.
     * @param imageType Image type.
     * @param imageIndex (Unused) Image index.
     * @param body The body parameter
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec setItemImageByIndexRequestCreation(UUID itemId, ImageType imageType, Integer imageIndex, File body) throws WebClientResponseException {
        Object postBody = body;
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException("Missing the required parameter 'itemId' when calling setItemImageByIndex", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'imageType' is set
        if (imageType == null) {
            throw new WebClientResponseException("Missing the required parameter 'imageType' when calling setItemImageByIndex", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'imageIndex' is set
        if (imageIndex == null) {
            throw new WebClientResponseException("Missing the required parameter 'imageIndex' when calling setItemImageByIndex", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("itemId", itemId);
        pathParams.put("imageType", imageType);
        pathParams.put("imageIndex", imageIndex);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { 
            "image/*"
        };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return apiClient.invokeAPI("/Items/{itemId}/Images/{imageType}/{imageIndex}", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Set item image.
     * 
     * <p><b>204</b> - Image saved.
     * <p><b>404</b> - Item not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId Item id.
     * @param imageType Image type.
     * @param imageIndex (Unused) Image index.
     * @param body The body parameter
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> setItemImageByIndex(UUID itemId, ImageType imageType, Integer imageIndex, File body) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return setItemImageByIndexRequestCreation(itemId, imageType, imageIndex, body).bodyToMono(localVarReturnType);
    }

    /**
     * Set item image.
     * 
     * <p><b>204</b> - Image saved.
     * <p><b>404</b> - Item not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId Item id.
     * @param imageType Image type.
     * @param imageIndex (Unused) Image index.
     * @param body The body parameter
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> setItemImageByIndexWithHttpInfo(UUID itemId, ImageType imageType, Integer imageIndex, File body) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return setItemImageByIndexRequestCreation(itemId, imageType, imageIndex, body).toEntity(localVarReturnType);
    }

    /**
     * Set item image.
     * 
     * <p><b>204</b> - Image saved.
     * <p><b>404</b> - Item not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId Item id.
     * @param imageType Image type.
     * @param imageIndex (Unused) Image index.
     * @param body The body parameter
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec setItemImageByIndexWithResponseSpec(UUID itemId, ImageType imageType, Integer imageIndex, File body) throws WebClientResponseException {
        return setItemImageByIndexRequestCreation(itemId, imageType, imageIndex, body);
    }

    /**
     * Updates the index for an item image.
     * 
     * <p><b>204</b> - Image index updated.
     * <p><b>404</b> - Item not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId Item id.
     * @param imageType Image type.
     * @param imageIndex Old image index.
     * @param newIndex New image index.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec updateItemImageIndexRequestCreation(UUID itemId, ImageType imageType, Integer imageIndex, Integer newIndex) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException("Missing the required parameter 'itemId' when calling updateItemImageIndex", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'imageType' is set
        if (imageType == null) {
            throw new WebClientResponseException("Missing the required parameter 'imageType' when calling updateItemImageIndex", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'imageIndex' is set
        if (imageIndex == null) {
            throw new WebClientResponseException("Missing the required parameter 'imageIndex' when calling updateItemImageIndex", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'newIndex' is set
        if (newIndex == null) {
            throw new WebClientResponseException("Missing the required parameter 'newIndex' when calling updateItemImageIndex", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("itemId", itemId);
        pathParams.put("imageType", imageType);
        pathParams.put("imageIndex", imageIndex);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "newIndex", newIndex));
        
        final String[] localVarAccepts = { 
            "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return apiClient.invokeAPI("/Items/{itemId}/Images/{imageType}/{imageIndex}/Index", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Updates the index for an item image.
     * 
     * <p><b>204</b> - Image index updated.
     * <p><b>404</b> - Item not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId Item id.
     * @param imageType Image type.
     * @param imageIndex Old image index.
     * @param newIndex New image index.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> updateItemImageIndex(UUID itemId, ImageType imageType, Integer imageIndex, Integer newIndex) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return updateItemImageIndexRequestCreation(itemId, imageType, imageIndex, newIndex).bodyToMono(localVarReturnType);
    }

    /**
     * Updates the index for an item image.
     * 
     * <p><b>204</b> - Image index updated.
     * <p><b>404</b> - Item not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId Item id.
     * @param imageType Image type.
     * @param imageIndex Old image index.
     * @param newIndex New image index.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> updateItemImageIndexWithHttpInfo(UUID itemId, ImageType imageType, Integer imageIndex, Integer newIndex) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return updateItemImageIndexRequestCreation(itemId, imageType, imageIndex, newIndex).toEntity(localVarReturnType);
    }

    /**
     * Updates the index for an item image.
     * 
     * <p><b>204</b> - Image index updated.
     * <p><b>404</b> - Item not found.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId Item id.
     * @param imageType Image type.
     * @param imageIndex Old image index.
     * @param newIndex New image index.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec updateItemImageIndexWithResponseSpec(UUID itemId, ImageType imageType, Integer imageIndex, Integer newIndex) throws WebClientResponseException {
        return updateItemImageIndexRequestCreation(itemId, imageType, imageIndex, newIndex);
    }

    /**
     * Uploads a custom splashscreen.  The body is expected to the image contents base64 encoded.
     * 
     * <p><b>204</b> - Successfully uploaded new splashscreen.
     * <p><b>400</b> - Error reading MimeType from uploaded image.
     * <p><b>403</b> - User does not have permission to upload splashscreen..
     * <p><b>401</b> - Unauthorized
     * @param body The body parameter
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec uploadCustomSplashscreenRequestCreation(File body) throws WebClientResponseException {
        Object postBody = body;
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
            "image/*"
        };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return apiClient.invokeAPI("/Branding/Splashscreen", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Uploads a custom splashscreen.  The body is expected to the image contents base64 encoded.
     * 
     * <p><b>204</b> - Successfully uploaded new splashscreen.
     * <p><b>400</b> - Error reading MimeType from uploaded image.
     * <p><b>403</b> - User does not have permission to upload splashscreen..
     * <p><b>401</b> - Unauthorized
     * @param body The body parameter
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> uploadCustomSplashscreen(File body) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return uploadCustomSplashscreenRequestCreation(body).bodyToMono(localVarReturnType);
    }

    /**
     * Uploads a custom splashscreen.  The body is expected to the image contents base64 encoded.
     * 
     * <p><b>204</b> - Successfully uploaded new splashscreen.
     * <p><b>400</b> - Error reading MimeType from uploaded image.
     * <p><b>403</b> - User does not have permission to upload splashscreen..
     * <p><b>401</b> - Unauthorized
     * @param body The body parameter
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> uploadCustomSplashscreenWithHttpInfo(File body) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return uploadCustomSplashscreenRequestCreation(body).toEntity(localVarReturnType);
    }

    /**
     * Uploads a custom splashscreen.  The body is expected to the image contents base64 encoded.
     * 
     * <p><b>204</b> - Successfully uploaded new splashscreen.
     * <p><b>400</b> - Error reading MimeType from uploaded image.
     * <p><b>403</b> - User does not have permission to upload splashscreen..
     * <p><b>401</b> - Unauthorized
     * @param body The body parameter
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec uploadCustomSplashscreenWithResponseSpec(File body) throws WebClientResponseException {
        return uploadCustomSplashscreenRequestCreation(body);
    }
}
