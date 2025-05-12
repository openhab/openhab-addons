package org.openhab.binding.jellyfin.internal.api.version.legacy;

import org.openhab.binding.jellyfin.internal.api.version.ApiClient;

import java.io.File;
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
public class HlsSegmentApi {
    private ApiClient apiClient;

    public HlsSegmentApi() {
        this(new ApiClient());
    }

    @Autowired
    public HlsSegmentApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    
    /**
     * Gets the specified audio segment for an audio item.
     * 
     * <p><b>200</b> - Hls audio segment returned.
     * @param itemId The item id.
     * @param segmentId The segment id.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getHlsAudioSegmentLegacyAacRequestCreation(String itemId, String segmentId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException("Missing the required parameter 'itemId' when calling getHlsAudioSegmentLegacyAac", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'segmentId' is set
        if (segmentId == null) {
            throw new WebClientResponseException("Missing the required parameter 'segmentId' when calling getHlsAudioSegmentLegacyAac", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("itemId", itemId);
        pathParams.put("segmentId", segmentId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "audio/*"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return apiClient.invokeAPI("/Audio/{itemId}/hls/{segmentId}/stream.aac", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets the specified audio segment for an audio item.
     * 
     * <p><b>200</b> - Hls audio segment returned.
     * @param itemId The item id.
     * @param segmentId The segment id.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<File> getHlsAudioSegmentLegacyAac(String itemId, String segmentId) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return getHlsAudioSegmentLegacyAacRequestCreation(itemId, segmentId).bodyToMono(localVarReturnType);
    }

    /**
     * Gets the specified audio segment for an audio item.
     * 
     * <p><b>200</b> - Hls audio segment returned.
     * @param itemId The item id.
     * @param segmentId The segment id.
     * @return ResponseEntity&lt;File&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<File>> getHlsAudioSegmentLegacyAacWithHttpInfo(String itemId, String segmentId) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return getHlsAudioSegmentLegacyAacRequestCreation(itemId, segmentId).toEntity(localVarReturnType);
    }

    /**
     * Gets the specified audio segment for an audio item.
     * 
     * <p><b>200</b> - Hls audio segment returned.
     * @param itemId The item id.
     * @param segmentId The segment id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getHlsAudioSegmentLegacyAacWithResponseSpec(String itemId, String segmentId) throws WebClientResponseException {
        return getHlsAudioSegmentLegacyAacRequestCreation(itemId, segmentId);
    }

    /**
     * Gets the specified audio segment for an audio item.
     * 
     * <p><b>200</b> - Hls audio segment returned.
     * @param itemId The item id.
     * @param segmentId The segment id.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getHlsAudioSegmentLegacyMp3RequestCreation(String itemId, String segmentId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException("Missing the required parameter 'itemId' when calling getHlsAudioSegmentLegacyMp3", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'segmentId' is set
        if (segmentId == null) {
            throw new WebClientResponseException("Missing the required parameter 'segmentId' when calling getHlsAudioSegmentLegacyMp3", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("itemId", itemId);
        pathParams.put("segmentId", segmentId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "audio/*"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return apiClient.invokeAPI("/Audio/{itemId}/hls/{segmentId}/stream.mp3", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets the specified audio segment for an audio item.
     * 
     * <p><b>200</b> - Hls audio segment returned.
     * @param itemId The item id.
     * @param segmentId The segment id.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<File> getHlsAudioSegmentLegacyMp3(String itemId, String segmentId) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return getHlsAudioSegmentLegacyMp3RequestCreation(itemId, segmentId).bodyToMono(localVarReturnType);
    }

    /**
     * Gets the specified audio segment for an audio item.
     * 
     * <p><b>200</b> - Hls audio segment returned.
     * @param itemId The item id.
     * @param segmentId The segment id.
     * @return ResponseEntity&lt;File&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<File>> getHlsAudioSegmentLegacyMp3WithHttpInfo(String itemId, String segmentId) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return getHlsAudioSegmentLegacyMp3RequestCreation(itemId, segmentId).toEntity(localVarReturnType);
    }

    /**
     * Gets the specified audio segment for an audio item.
     * 
     * <p><b>200</b> - Hls audio segment returned.
     * @param itemId The item id.
     * @param segmentId The segment id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getHlsAudioSegmentLegacyMp3WithResponseSpec(String itemId, String segmentId) throws WebClientResponseException {
        return getHlsAudioSegmentLegacyMp3RequestCreation(itemId, segmentId);
    }

    /**
     * Gets a hls video playlist.
     * 
     * <p><b>200</b> - Hls video playlist returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId The video id.
     * @param playlistId The playlist id.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getHlsPlaylistLegacyRequestCreation(String itemId, String playlistId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException("Missing the required parameter 'itemId' when calling getHlsPlaylistLegacy", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'playlistId' is set
        if (playlistId == null) {
            throw new WebClientResponseException("Missing the required parameter 'playlistId' when calling getHlsPlaylistLegacy", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("itemId", itemId);
        pathParams.put("playlistId", playlistId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "application/x-mpegURL"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return apiClient.invokeAPI("/Videos/{itemId}/hls/{playlistId}/stream.m3u8", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets a hls video playlist.
     * 
     * <p><b>200</b> - Hls video playlist returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId The video id.
     * @param playlistId The playlist id.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<File> getHlsPlaylistLegacy(String itemId, String playlistId) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return getHlsPlaylistLegacyRequestCreation(itemId, playlistId).bodyToMono(localVarReturnType);
    }

    /**
     * Gets a hls video playlist.
     * 
     * <p><b>200</b> - Hls video playlist returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId The video id.
     * @param playlistId The playlist id.
     * @return ResponseEntity&lt;File&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<File>> getHlsPlaylistLegacyWithHttpInfo(String itemId, String playlistId) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return getHlsPlaylistLegacyRequestCreation(itemId, playlistId).toEntity(localVarReturnType);
    }

    /**
     * Gets a hls video playlist.
     * 
     * <p><b>200</b> - Hls video playlist returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId The video id.
     * @param playlistId The playlist id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getHlsPlaylistLegacyWithResponseSpec(String itemId, String playlistId) throws WebClientResponseException {
        return getHlsPlaylistLegacyRequestCreation(itemId, playlistId);
    }

    /**
     * Gets a hls video segment.
     * 
     * <p><b>200</b> - Hls video segment returned.
     * <p><b>404</b> - Hls segment not found.
     * @param itemId The item id.
     * @param playlistId The playlist id.
     * @param segmentId The segment id.
     * @param segmentContainer The segment container.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getHlsVideoSegmentLegacyRequestCreation(String itemId, String playlistId, String segmentId, String segmentContainer) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException("Missing the required parameter 'itemId' when calling getHlsVideoSegmentLegacy", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'playlistId' is set
        if (playlistId == null) {
            throw new WebClientResponseException("Missing the required parameter 'playlistId' when calling getHlsVideoSegmentLegacy", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'segmentId' is set
        if (segmentId == null) {
            throw new WebClientResponseException("Missing the required parameter 'segmentId' when calling getHlsVideoSegmentLegacy", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'segmentContainer' is set
        if (segmentContainer == null) {
            throw new WebClientResponseException("Missing the required parameter 'segmentContainer' when calling getHlsVideoSegmentLegacy", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("itemId", itemId);
        pathParams.put("playlistId", playlistId);
        pathParams.put("segmentId", segmentId);
        pathParams.put("segmentContainer", segmentContainer);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "video/*", "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return apiClient.invokeAPI("/Videos/{itemId}/hls/{playlistId}/{segmentId}.{segmentContainer}", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets a hls video segment.
     * 
     * <p><b>200</b> - Hls video segment returned.
     * <p><b>404</b> - Hls segment not found.
     * @param itemId The item id.
     * @param playlistId The playlist id.
     * @param segmentId The segment id.
     * @param segmentContainer The segment container.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<File> getHlsVideoSegmentLegacy(String itemId, String playlistId, String segmentId, String segmentContainer) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return getHlsVideoSegmentLegacyRequestCreation(itemId, playlistId, segmentId, segmentContainer).bodyToMono(localVarReturnType);
    }

    /**
     * Gets a hls video segment.
     * 
     * <p><b>200</b> - Hls video segment returned.
     * <p><b>404</b> - Hls segment not found.
     * @param itemId The item id.
     * @param playlistId The playlist id.
     * @param segmentId The segment id.
     * @param segmentContainer The segment container.
     * @return ResponseEntity&lt;File&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<File>> getHlsVideoSegmentLegacyWithHttpInfo(String itemId, String playlistId, String segmentId, String segmentContainer) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return getHlsVideoSegmentLegacyRequestCreation(itemId, playlistId, segmentId, segmentContainer).toEntity(localVarReturnType);
    }

    /**
     * Gets a hls video segment.
     * 
     * <p><b>200</b> - Hls video segment returned.
     * <p><b>404</b> - Hls segment not found.
     * @param itemId The item id.
     * @param playlistId The playlist id.
     * @param segmentId The segment id.
     * @param segmentContainer The segment container.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getHlsVideoSegmentLegacyWithResponseSpec(String itemId, String playlistId, String segmentId, String segmentContainer) throws WebClientResponseException {
        return getHlsVideoSegmentLegacyRequestCreation(itemId, playlistId, segmentId, segmentContainer);
    }

    /**
     * Stops an active encoding.
     * 
     * <p><b>204</b> - Encoding stopped successfully.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param deviceId The device id of the client requesting. Used to stop encoding processes when needed.
     * @param playSessionId The play session id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec stopEncodingProcessRequestCreation(String deviceId, String playSessionId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'deviceId' is set
        if (deviceId == null) {
            throw new WebClientResponseException("Missing the required parameter 'deviceId' when calling stopEncodingProcess", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'playSessionId' is set
        if (playSessionId == null) {
            throw new WebClientResponseException("Missing the required parameter 'playSessionId' when calling stopEncodingProcess", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "deviceId", deviceId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "playSessionId", playSessionId));
        
        final String[] localVarAccepts = { };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return apiClient.invokeAPI("/Videos/ActiveEncodings", HttpMethod.DELETE, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Stops an active encoding.
     * 
     * <p><b>204</b> - Encoding stopped successfully.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param deviceId The device id of the client requesting. Used to stop encoding processes when needed.
     * @param playSessionId The play session id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> stopEncodingProcess(String deviceId, String playSessionId) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return stopEncodingProcessRequestCreation(deviceId, playSessionId).bodyToMono(localVarReturnType);
    }

    /**
     * Stops an active encoding.
     * 
     * <p><b>204</b> - Encoding stopped successfully.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param deviceId The device id of the client requesting. Used to stop encoding processes when needed.
     * @param playSessionId The play session id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> stopEncodingProcessWithHttpInfo(String deviceId, String playSessionId) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return stopEncodingProcessRequestCreation(deviceId, playSessionId).toEntity(localVarReturnType);
    }

    /**
     * Stops an active encoding.
     * 
     * <p><b>204</b> - Encoding stopped successfully.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param deviceId The device id of the client requesting. Used to stop encoding processes when needed.
     * @param playSessionId The play session id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec stopEncodingProcessWithResponseSpec(String deviceId, String playSessionId) throws WebClientResponseException {
        return stopEncodingProcessRequestCreation(deviceId, playSessionId);
    }
}
