package org.openhab.binding.jellyfin.internal.api.version.legacy;

import org.openhab.binding.jellyfin.internal.api.version.ApiClient;

import java.io.File;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.ImageByNameInfo;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.ProblemDetails;

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
public class ImageByNameApi {
    private ApiClient apiClient;

    public ImageByNameApi() {
        this(new ApiClient());
    }

    @Autowired
    public ImageByNameApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    
    /**
     * Get General Image.
     * 
     * <p><b>200</b> - Image stream retrieved.
     * <p><b>404</b> - Image not found.
     * @param name The name of the image.
     * @param type Image Type (primary, backdrop, logo, etc).
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getGeneralImageRequestCreation(String name, String type) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'name' is set
        if (name == null) {
            throw new WebClientResponseException("Missing the required parameter 'name' when calling getGeneralImage", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'type' is set
        if (type == null) {
            throw new WebClientResponseException("Missing the required parameter 'type' when calling getGeneralImage", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("name", name);
        pathParams.put("type", type);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "image/*", "application/octet-stream", "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return apiClient.invokeAPI("/Images/General/{name}/{type}", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get General Image.
     * 
     * <p><b>200</b> - Image stream retrieved.
     * <p><b>404</b> - Image not found.
     * @param name The name of the image.
     * @param type Image Type (primary, backdrop, logo, etc).
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<File> getGeneralImage(String name, String type) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return getGeneralImageRequestCreation(name, type).bodyToMono(localVarReturnType);
    }

    /**
     * Get General Image.
     * 
     * <p><b>200</b> - Image stream retrieved.
     * <p><b>404</b> - Image not found.
     * @param name The name of the image.
     * @param type Image Type (primary, backdrop, logo, etc).
     * @return ResponseEntity&lt;File&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<File>> getGeneralImageWithHttpInfo(String name, String type) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return getGeneralImageRequestCreation(name, type).toEntity(localVarReturnType);
    }

    /**
     * Get General Image.
     * 
     * <p><b>200</b> - Image stream retrieved.
     * <p><b>404</b> - Image not found.
     * @param name The name of the image.
     * @param type Image Type (primary, backdrop, logo, etc).
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getGeneralImageWithResponseSpec(String name, String type) throws WebClientResponseException {
        return getGeneralImageRequestCreation(name, type);
    }

    /**
     * Get all general images.
     * 
     * <p><b>200</b> - Retrieved list of images.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @return List&lt;ImageByNameInfo&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getGeneralImagesRequestCreation() throws WebClientResponseException {
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

        ParameterizedTypeReference<ImageByNameInfo> localVarReturnType = new ParameterizedTypeReference<ImageByNameInfo>() {};
        return apiClient.invokeAPI("/Images/General", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get all general images.
     * 
     * <p><b>200</b> - Retrieved list of images.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @return List&lt;ImageByNameInfo&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Flux<ImageByNameInfo> getGeneralImages() throws WebClientResponseException {
        ParameterizedTypeReference<ImageByNameInfo> localVarReturnType = new ParameterizedTypeReference<ImageByNameInfo>() {};
        return getGeneralImagesRequestCreation().bodyToFlux(localVarReturnType);
    }

    /**
     * Get all general images.
     * 
     * <p><b>200</b> - Retrieved list of images.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @return ResponseEntity&lt;List&lt;ImageByNameInfo&gt;&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<List<ImageByNameInfo>>> getGeneralImagesWithHttpInfo() throws WebClientResponseException {
        ParameterizedTypeReference<ImageByNameInfo> localVarReturnType = new ParameterizedTypeReference<ImageByNameInfo>() {};
        return getGeneralImagesRequestCreation().toEntityList(localVarReturnType);
    }

    /**
     * Get all general images.
     * 
     * <p><b>200</b> - Retrieved list of images.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getGeneralImagesWithResponseSpec() throws WebClientResponseException {
        return getGeneralImagesRequestCreation();
    }

    /**
     * Get media info image.
     * 
     * <p><b>200</b> - Image stream retrieved.
     * <p><b>404</b> - Image not found.
     * @param theme The theme to get the image from.
     * @param name The name of the image.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getMediaInfoImageRequestCreation(String theme, String name) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'theme' is set
        if (theme == null) {
            throw new WebClientResponseException("Missing the required parameter 'theme' when calling getMediaInfoImage", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'name' is set
        if (name == null) {
            throw new WebClientResponseException("Missing the required parameter 'name' when calling getMediaInfoImage", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("theme", theme);
        pathParams.put("name", name);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "image/*", "application/octet-stream", "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return apiClient.invokeAPI("/Images/MediaInfo/{theme}/{name}", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get media info image.
     * 
     * <p><b>200</b> - Image stream retrieved.
     * <p><b>404</b> - Image not found.
     * @param theme The theme to get the image from.
     * @param name The name of the image.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<File> getMediaInfoImage(String theme, String name) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return getMediaInfoImageRequestCreation(theme, name).bodyToMono(localVarReturnType);
    }

    /**
     * Get media info image.
     * 
     * <p><b>200</b> - Image stream retrieved.
     * <p><b>404</b> - Image not found.
     * @param theme The theme to get the image from.
     * @param name The name of the image.
     * @return ResponseEntity&lt;File&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<File>> getMediaInfoImageWithHttpInfo(String theme, String name) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return getMediaInfoImageRequestCreation(theme, name).toEntity(localVarReturnType);
    }

    /**
     * Get media info image.
     * 
     * <p><b>200</b> - Image stream retrieved.
     * <p><b>404</b> - Image not found.
     * @param theme The theme to get the image from.
     * @param name The name of the image.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getMediaInfoImageWithResponseSpec(String theme, String name) throws WebClientResponseException {
        return getMediaInfoImageRequestCreation(theme, name);
    }

    /**
     * Get all media info images.
     * 
     * <p><b>200</b> - Image list retrieved.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @return List&lt;ImageByNameInfo&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getMediaInfoImagesRequestCreation() throws WebClientResponseException {
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

        ParameterizedTypeReference<ImageByNameInfo> localVarReturnType = new ParameterizedTypeReference<ImageByNameInfo>() {};
        return apiClient.invokeAPI("/Images/MediaInfo", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get all media info images.
     * 
     * <p><b>200</b> - Image list retrieved.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @return List&lt;ImageByNameInfo&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Flux<ImageByNameInfo> getMediaInfoImages() throws WebClientResponseException {
        ParameterizedTypeReference<ImageByNameInfo> localVarReturnType = new ParameterizedTypeReference<ImageByNameInfo>() {};
        return getMediaInfoImagesRequestCreation().bodyToFlux(localVarReturnType);
    }

    /**
     * Get all media info images.
     * 
     * <p><b>200</b> - Image list retrieved.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @return ResponseEntity&lt;List&lt;ImageByNameInfo&gt;&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<List<ImageByNameInfo>>> getMediaInfoImagesWithHttpInfo() throws WebClientResponseException {
        ParameterizedTypeReference<ImageByNameInfo> localVarReturnType = new ParameterizedTypeReference<ImageByNameInfo>() {};
        return getMediaInfoImagesRequestCreation().toEntityList(localVarReturnType);
    }

    /**
     * Get all media info images.
     * 
     * <p><b>200</b> - Image list retrieved.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getMediaInfoImagesWithResponseSpec() throws WebClientResponseException {
        return getMediaInfoImagesRequestCreation();
    }

    /**
     * Get rating image.
     * 
     * <p><b>200</b> - Image stream retrieved.
     * <p><b>404</b> - Image not found.
     * @param theme The theme to get the image from.
     * @param name The name of the image.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getRatingImageRequestCreation(String theme, String name) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'theme' is set
        if (theme == null) {
            throw new WebClientResponseException("Missing the required parameter 'theme' when calling getRatingImage", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'name' is set
        if (name == null) {
            throw new WebClientResponseException("Missing the required parameter 'name' when calling getRatingImage", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("theme", theme);
        pathParams.put("name", name);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "image/*", "application/octet-stream", "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return apiClient.invokeAPI("/Images/Ratings/{theme}/{name}", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get rating image.
     * 
     * <p><b>200</b> - Image stream retrieved.
     * <p><b>404</b> - Image not found.
     * @param theme The theme to get the image from.
     * @param name The name of the image.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<File> getRatingImage(String theme, String name) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return getRatingImageRequestCreation(theme, name).bodyToMono(localVarReturnType);
    }

    /**
     * Get rating image.
     * 
     * <p><b>200</b> - Image stream retrieved.
     * <p><b>404</b> - Image not found.
     * @param theme The theme to get the image from.
     * @param name The name of the image.
     * @return ResponseEntity&lt;File&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<File>> getRatingImageWithHttpInfo(String theme, String name) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return getRatingImageRequestCreation(theme, name).toEntity(localVarReturnType);
    }

    /**
     * Get rating image.
     * 
     * <p><b>200</b> - Image stream retrieved.
     * <p><b>404</b> - Image not found.
     * @param theme The theme to get the image from.
     * @param name The name of the image.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getRatingImageWithResponseSpec(String theme, String name) throws WebClientResponseException {
        return getRatingImageRequestCreation(theme, name);
    }

    /**
     * Get all general images.
     * 
     * <p><b>200</b> - Retrieved list of images.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @return List&lt;ImageByNameInfo&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getRatingImagesRequestCreation() throws WebClientResponseException {
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

        ParameterizedTypeReference<ImageByNameInfo> localVarReturnType = new ParameterizedTypeReference<ImageByNameInfo>() {};
        return apiClient.invokeAPI("/Images/Ratings", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get all general images.
     * 
     * <p><b>200</b> - Retrieved list of images.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @return List&lt;ImageByNameInfo&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Flux<ImageByNameInfo> getRatingImages() throws WebClientResponseException {
        ParameterizedTypeReference<ImageByNameInfo> localVarReturnType = new ParameterizedTypeReference<ImageByNameInfo>() {};
        return getRatingImagesRequestCreation().bodyToFlux(localVarReturnType);
    }

    /**
     * Get all general images.
     * 
     * <p><b>200</b> - Retrieved list of images.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @return ResponseEntity&lt;List&lt;ImageByNameInfo&gt;&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<List<ImageByNameInfo>>> getRatingImagesWithHttpInfo() throws WebClientResponseException {
        ParameterizedTypeReference<ImageByNameInfo> localVarReturnType = new ParameterizedTypeReference<ImageByNameInfo>() {};
        return getRatingImagesRequestCreation().toEntityList(localVarReturnType);
    }

    /**
     * Get all general images.
     * 
     * <p><b>200</b> - Retrieved list of images.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getRatingImagesWithResponseSpec() throws WebClientResponseException {
        return getRatingImagesRequestCreation();
    }
}
