package org.openhab.binding.jellyfin.internal.api.version.legacy;

import org.openhab.binding.jellyfin.internal.api.version.ApiClient;

import java.io.File;
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
public class UniversalAudioApi {
    private ApiClient apiClient;

    public UniversalAudioApi() {
        this(new ApiClient());
    }

    @Autowired
    public UniversalAudioApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    
    /**
     * Gets an audio stream.
     * 
     * <p><b>200</b> - Audio stream returned.
     * <p><b>302</b> - Redirected to remote audio stream.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId The item id.
     * @param container Optional. The audio container.
     * @param mediaSourceId The media version id, if playing an alternate version.
     * @param deviceId The device id of the client requesting. Used to stop encoding processes when needed.
     * @param userId Optional. The user id.
     * @param audioCodec Optional. The audio codec to transcode to.
     * @param maxAudioChannels Optional. The maximum number of audio channels.
     * @param transcodingAudioChannels Optional. The number of how many audio channels to transcode to.
     * @param maxStreamingBitrate Optional. The maximum streaming bitrate.
     * @param audioBitRate Optional. Specify an audio bitrate to encode to, e.g. 128000. If omitted this will be left to encoder defaults.
     * @param startTimeTicks Optional. Specify a starting offset, in ticks. 1 tick &#x3D; 10000 ms.
     * @param transcodingContainer Optional. The container to transcode to.
     * @param transcodingProtocol Optional. The transcoding protocol.
     * @param maxAudioSampleRate Optional. The maximum audio sample rate.
     * @param maxAudioBitDepth Optional. The maximum audio bit depth.
     * @param enableRemoteMedia Optional. Whether to enable remote media.
     * @param breakOnNonKeyFrames Optional. Whether to break on non key frames.
     * @param enableRedirection Whether to enable redirection. Defaults to true.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getUniversalAudioStreamRequestCreation(UUID itemId, List<String> container, String mediaSourceId, String deviceId, UUID userId, String audioCodec, Integer maxAudioChannels, Integer transcodingAudioChannels, Integer maxStreamingBitrate, Integer audioBitRate, Long startTimeTicks, String transcodingContainer, String transcodingProtocol, Integer maxAudioSampleRate, Integer maxAudioBitDepth, Boolean enableRemoteMedia, Boolean breakOnNonKeyFrames, Boolean enableRedirection) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException("Missing the required parameter 'itemId' when calling getUniversalAudioStream", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("itemId", itemId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "container", container));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "mediaSourceId", mediaSourceId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "deviceId", deviceId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "userId", userId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "audioCodec", audioCodec));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxAudioChannels", maxAudioChannels));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "transcodingAudioChannels", transcodingAudioChannels));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxStreamingBitrate", maxStreamingBitrate));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "audioBitRate", audioBitRate));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "startTimeTicks", startTimeTicks));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "transcodingContainer", transcodingContainer));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "transcodingProtocol", transcodingProtocol));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxAudioSampleRate", maxAudioSampleRate));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxAudioBitDepth", maxAudioBitDepth));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableRemoteMedia", enableRemoteMedia));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "breakOnNonKeyFrames", breakOnNonKeyFrames));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableRedirection", enableRedirection));
        
        final String[] localVarAccepts = { 
            "audio/*"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return apiClient.invokeAPI("/Audio/{itemId}/universal", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets an audio stream.
     * 
     * <p><b>200</b> - Audio stream returned.
     * <p><b>302</b> - Redirected to remote audio stream.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId The item id.
     * @param container Optional. The audio container.
     * @param mediaSourceId The media version id, if playing an alternate version.
     * @param deviceId The device id of the client requesting. Used to stop encoding processes when needed.
     * @param userId Optional. The user id.
     * @param audioCodec Optional. The audio codec to transcode to.
     * @param maxAudioChannels Optional. The maximum number of audio channels.
     * @param transcodingAudioChannels Optional. The number of how many audio channels to transcode to.
     * @param maxStreamingBitrate Optional. The maximum streaming bitrate.
     * @param audioBitRate Optional. Specify an audio bitrate to encode to, e.g. 128000. If omitted this will be left to encoder defaults.
     * @param startTimeTicks Optional. Specify a starting offset, in ticks. 1 tick &#x3D; 10000 ms.
     * @param transcodingContainer Optional. The container to transcode to.
     * @param transcodingProtocol Optional. The transcoding protocol.
     * @param maxAudioSampleRate Optional. The maximum audio sample rate.
     * @param maxAudioBitDepth Optional. The maximum audio bit depth.
     * @param enableRemoteMedia Optional. Whether to enable remote media.
     * @param breakOnNonKeyFrames Optional. Whether to break on non key frames.
     * @param enableRedirection Whether to enable redirection. Defaults to true.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<File> getUniversalAudioStream(UUID itemId, List<String> container, String mediaSourceId, String deviceId, UUID userId, String audioCodec, Integer maxAudioChannels, Integer transcodingAudioChannels, Integer maxStreamingBitrate, Integer audioBitRate, Long startTimeTicks, String transcodingContainer, String transcodingProtocol, Integer maxAudioSampleRate, Integer maxAudioBitDepth, Boolean enableRemoteMedia, Boolean breakOnNonKeyFrames, Boolean enableRedirection) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return getUniversalAudioStreamRequestCreation(itemId, container, mediaSourceId, deviceId, userId, audioCodec, maxAudioChannels, transcodingAudioChannels, maxStreamingBitrate, audioBitRate, startTimeTicks, transcodingContainer, transcodingProtocol, maxAudioSampleRate, maxAudioBitDepth, enableRemoteMedia, breakOnNonKeyFrames, enableRedirection).bodyToMono(localVarReturnType);
    }

    /**
     * Gets an audio stream.
     * 
     * <p><b>200</b> - Audio stream returned.
     * <p><b>302</b> - Redirected to remote audio stream.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId The item id.
     * @param container Optional. The audio container.
     * @param mediaSourceId The media version id, if playing an alternate version.
     * @param deviceId The device id of the client requesting. Used to stop encoding processes when needed.
     * @param userId Optional. The user id.
     * @param audioCodec Optional. The audio codec to transcode to.
     * @param maxAudioChannels Optional. The maximum number of audio channels.
     * @param transcodingAudioChannels Optional. The number of how many audio channels to transcode to.
     * @param maxStreamingBitrate Optional. The maximum streaming bitrate.
     * @param audioBitRate Optional. Specify an audio bitrate to encode to, e.g. 128000. If omitted this will be left to encoder defaults.
     * @param startTimeTicks Optional. Specify a starting offset, in ticks. 1 tick &#x3D; 10000 ms.
     * @param transcodingContainer Optional. The container to transcode to.
     * @param transcodingProtocol Optional. The transcoding protocol.
     * @param maxAudioSampleRate Optional. The maximum audio sample rate.
     * @param maxAudioBitDepth Optional. The maximum audio bit depth.
     * @param enableRemoteMedia Optional. Whether to enable remote media.
     * @param breakOnNonKeyFrames Optional. Whether to break on non key frames.
     * @param enableRedirection Whether to enable redirection. Defaults to true.
     * @return ResponseEntity&lt;File&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<File>> getUniversalAudioStreamWithHttpInfo(UUID itemId, List<String> container, String mediaSourceId, String deviceId, UUID userId, String audioCodec, Integer maxAudioChannels, Integer transcodingAudioChannels, Integer maxStreamingBitrate, Integer audioBitRate, Long startTimeTicks, String transcodingContainer, String transcodingProtocol, Integer maxAudioSampleRate, Integer maxAudioBitDepth, Boolean enableRemoteMedia, Boolean breakOnNonKeyFrames, Boolean enableRedirection) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return getUniversalAudioStreamRequestCreation(itemId, container, mediaSourceId, deviceId, userId, audioCodec, maxAudioChannels, transcodingAudioChannels, maxStreamingBitrate, audioBitRate, startTimeTicks, transcodingContainer, transcodingProtocol, maxAudioSampleRate, maxAudioBitDepth, enableRemoteMedia, breakOnNonKeyFrames, enableRedirection).toEntity(localVarReturnType);
    }

    /**
     * Gets an audio stream.
     * 
     * <p><b>200</b> - Audio stream returned.
     * <p><b>302</b> - Redirected to remote audio stream.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId The item id.
     * @param container Optional. The audio container.
     * @param mediaSourceId The media version id, if playing an alternate version.
     * @param deviceId The device id of the client requesting. Used to stop encoding processes when needed.
     * @param userId Optional. The user id.
     * @param audioCodec Optional. The audio codec to transcode to.
     * @param maxAudioChannels Optional. The maximum number of audio channels.
     * @param transcodingAudioChannels Optional. The number of how many audio channels to transcode to.
     * @param maxStreamingBitrate Optional. The maximum streaming bitrate.
     * @param audioBitRate Optional. Specify an audio bitrate to encode to, e.g. 128000. If omitted this will be left to encoder defaults.
     * @param startTimeTicks Optional. Specify a starting offset, in ticks. 1 tick &#x3D; 10000 ms.
     * @param transcodingContainer Optional. The container to transcode to.
     * @param transcodingProtocol Optional. The transcoding protocol.
     * @param maxAudioSampleRate Optional. The maximum audio sample rate.
     * @param maxAudioBitDepth Optional. The maximum audio bit depth.
     * @param enableRemoteMedia Optional. Whether to enable remote media.
     * @param breakOnNonKeyFrames Optional. Whether to break on non key frames.
     * @param enableRedirection Whether to enable redirection. Defaults to true.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getUniversalAudioStreamWithResponseSpec(UUID itemId, List<String> container, String mediaSourceId, String deviceId, UUID userId, String audioCodec, Integer maxAudioChannels, Integer transcodingAudioChannels, Integer maxStreamingBitrate, Integer audioBitRate, Long startTimeTicks, String transcodingContainer, String transcodingProtocol, Integer maxAudioSampleRate, Integer maxAudioBitDepth, Boolean enableRemoteMedia, Boolean breakOnNonKeyFrames, Boolean enableRedirection) throws WebClientResponseException {
        return getUniversalAudioStreamRequestCreation(itemId, container, mediaSourceId, deviceId, userId, audioCodec, maxAudioChannels, transcodingAudioChannels, maxStreamingBitrate, audioBitRate, startTimeTicks, transcodingContainer, transcodingProtocol, maxAudioSampleRate, maxAudioBitDepth, enableRemoteMedia, breakOnNonKeyFrames, enableRedirection);
    }

    /**
     * Gets an audio stream.
     * 
     * <p><b>200</b> - Audio stream returned.
     * <p><b>302</b> - Redirected to remote audio stream.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId The item id.
     * @param container Optional. The audio container.
     * @param mediaSourceId The media version id, if playing an alternate version.
     * @param deviceId The device id of the client requesting. Used to stop encoding processes when needed.
     * @param userId Optional. The user id.
     * @param audioCodec Optional. The audio codec to transcode to.
     * @param maxAudioChannels Optional. The maximum number of audio channels.
     * @param transcodingAudioChannels Optional. The number of how many audio channels to transcode to.
     * @param maxStreamingBitrate Optional. The maximum streaming bitrate.
     * @param audioBitRate Optional. Specify an audio bitrate to encode to, e.g. 128000. If omitted this will be left to encoder defaults.
     * @param startTimeTicks Optional. Specify a starting offset, in ticks. 1 tick &#x3D; 10000 ms.
     * @param transcodingContainer Optional. The container to transcode to.
     * @param transcodingProtocol Optional. The transcoding protocol.
     * @param maxAudioSampleRate Optional. The maximum audio sample rate.
     * @param maxAudioBitDepth Optional. The maximum audio bit depth.
     * @param enableRemoteMedia Optional. Whether to enable remote media.
     * @param breakOnNonKeyFrames Optional. Whether to break on non key frames.
     * @param enableRedirection Whether to enable redirection. Defaults to true.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec headUniversalAudioStreamRequestCreation(UUID itemId, List<String> container, String mediaSourceId, String deviceId, UUID userId, String audioCodec, Integer maxAudioChannels, Integer transcodingAudioChannels, Integer maxStreamingBitrate, Integer audioBitRate, Long startTimeTicks, String transcodingContainer, String transcodingProtocol, Integer maxAudioSampleRate, Integer maxAudioBitDepth, Boolean enableRemoteMedia, Boolean breakOnNonKeyFrames, Boolean enableRedirection) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException("Missing the required parameter 'itemId' when calling headUniversalAudioStream", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("itemId", itemId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "container", container));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "mediaSourceId", mediaSourceId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "deviceId", deviceId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "userId", userId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "audioCodec", audioCodec));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxAudioChannels", maxAudioChannels));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "transcodingAudioChannels", transcodingAudioChannels));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxStreamingBitrate", maxStreamingBitrate));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "audioBitRate", audioBitRate));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "startTimeTicks", startTimeTicks));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "transcodingContainer", transcodingContainer));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "transcodingProtocol", transcodingProtocol));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxAudioSampleRate", maxAudioSampleRate));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxAudioBitDepth", maxAudioBitDepth));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableRemoteMedia", enableRemoteMedia));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "breakOnNonKeyFrames", breakOnNonKeyFrames));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableRedirection", enableRedirection));
        
        final String[] localVarAccepts = { 
            "audio/*"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return apiClient.invokeAPI("/Audio/{itemId}/universal", HttpMethod.HEAD, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets an audio stream.
     * 
     * <p><b>200</b> - Audio stream returned.
     * <p><b>302</b> - Redirected to remote audio stream.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId The item id.
     * @param container Optional. The audio container.
     * @param mediaSourceId The media version id, if playing an alternate version.
     * @param deviceId The device id of the client requesting. Used to stop encoding processes when needed.
     * @param userId Optional. The user id.
     * @param audioCodec Optional. The audio codec to transcode to.
     * @param maxAudioChannels Optional. The maximum number of audio channels.
     * @param transcodingAudioChannels Optional. The number of how many audio channels to transcode to.
     * @param maxStreamingBitrate Optional. The maximum streaming bitrate.
     * @param audioBitRate Optional. Specify an audio bitrate to encode to, e.g. 128000. If omitted this will be left to encoder defaults.
     * @param startTimeTicks Optional. Specify a starting offset, in ticks. 1 tick &#x3D; 10000 ms.
     * @param transcodingContainer Optional. The container to transcode to.
     * @param transcodingProtocol Optional. The transcoding protocol.
     * @param maxAudioSampleRate Optional. The maximum audio sample rate.
     * @param maxAudioBitDepth Optional. The maximum audio bit depth.
     * @param enableRemoteMedia Optional. Whether to enable remote media.
     * @param breakOnNonKeyFrames Optional. Whether to break on non key frames.
     * @param enableRedirection Whether to enable redirection. Defaults to true.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<File> headUniversalAudioStream(UUID itemId, List<String> container, String mediaSourceId, String deviceId, UUID userId, String audioCodec, Integer maxAudioChannels, Integer transcodingAudioChannels, Integer maxStreamingBitrate, Integer audioBitRate, Long startTimeTicks, String transcodingContainer, String transcodingProtocol, Integer maxAudioSampleRate, Integer maxAudioBitDepth, Boolean enableRemoteMedia, Boolean breakOnNonKeyFrames, Boolean enableRedirection) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return headUniversalAudioStreamRequestCreation(itemId, container, mediaSourceId, deviceId, userId, audioCodec, maxAudioChannels, transcodingAudioChannels, maxStreamingBitrate, audioBitRate, startTimeTicks, transcodingContainer, transcodingProtocol, maxAudioSampleRate, maxAudioBitDepth, enableRemoteMedia, breakOnNonKeyFrames, enableRedirection).bodyToMono(localVarReturnType);
    }

    /**
     * Gets an audio stream.
     * 
     * <p><b>200</b> - Audio stream returned.
     * <p><b>302</b> - Redirected to remote audio stream.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId The item id.
     * @param container Optional. The audio container.
     * @param mediaSourceId The media version id, if playing an alternate version.
     * @param deviceId The device id of the client requesting. Used to stop encoding processes when needed.
     * @param userId Optional. The user id.
     * @param audioCodec Optional. The audio codec to transcode to.
     * @param maxAudioChannels Optional. The maximum number of audio channels.
     * @param transcodingAudioChannels Optional. The number of how many audio channels to transcode to.
     * @param maxStreamingBitrate Optional. The maximum streaming bitrate.
     * @param audioBitRate Optional. Specify an audio bitrate to encode to, e.g. 128000. If omitted this will be left to encoder defaults.
     * @param startTimeTicks Optional. Specify a starting offset, in ticks. 1 tick &#x3D; 10000 ms.
     * @param transcodingContainer Optional. The container to transcode to.
     * @param transcodingProtocol Optional. The transcoding protocol.
     * @param maxAudioSampleRate Optional. The maximum audio sample rate.
     * @param maxAudioBitDepth Optional. The maximum audio bit depth.
     * @param enableRemoteMedia Optional. Whether to enable remote media.
     * @param breakOnNonKeyFrames Optional. Whether to break on non key frames.
     * @param enableRedirection Whether to enable redirection. Defaults to true.
     * @return ResponseEntity&lt;File&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<File>> headUniversalAudioStreamWithHttpInfo(UUID itemId, List<String> container, String mediaSourceId, String deviceId, UUID userId, String audioCodec, Integer maxAudioChannels, Integer transcodingAudioChannels, Integer maxStreamingBitrate, Integer audioBitRate, Long startTimeTicks, String transcodingContainer, String transcodingProtocol, Integer maxAudioSampleRate, Integer maxAudioBitDepth, Boolean enableRemoteMedia, Boolean breakOnNonKeyFrames, Boolean enableRedirection) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {};
        return headUniversalAudioStreamRequestCreation(itemId, container, mediaSourceId, deviceId, userId, audioCodec, maxAudioChannels, transcodingAudioChannels, maxStreamingBitrate, audioBitRate, startTimeTicks, transcodingContainer, transcodingProtocol, maxAudioSampleRate, maxAudioBitDepth, enableRemoteMedia, breakOnNonKeyFrames, enableRedirection).toEntity(localVarReturnType);
    }

    /**
     * Gets an audio stream.
     * 
     * <p><b>200</b> - Audio stream returned.
     * <p><b>302</b> - Redirected to remote audio stream.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param itemId The item id.
     * @param container Optional. The audio container.
     * @param mediaSourceId The media version id, if playing an alternate version.
     * @param deviceId The device id of the client requesting. Used to stop encoding processes when needed.
     * @param userId Optional. The user id.
     * @param audioCodec Optional. The audio codec to transcode to.
     * @param maxAudioChannels Optional. The maximum number of audio channels.
     * @param transcodingAudioChannels Optional. The number of how many audio channels to transcode to.
     * @param maxStreamingBitrate Optional. The maximum streaming bitrate.
     * @param audioBitRate Optional. Specify an audio bitrate to encode to, e.g. 128000. If omitted this will be left to encoder defaults.
     * @param startTimeTicks Optional. Specify a starting offset, in ticks. 1 tick &#x3D; 10000 ms.
     * @param transcodingContainer Optional. The container to transcode to.
     * @param transcodingProtocol Optional. The transcoding protocol.
     * @param maxAudioSampleRate Optional. The maximum audio sample rate.
     * @param maxAudioBitDepth Optional. The maximum audio bit depth.
     * @param enableRemoteMedia Optional. Whether to enable remote media.
     * @param breakOnNonKeyFrames Optional. Whether to break on non key frames.
     * @param enableRedirection Whether to enable redirection. Defaults to true.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec headUniversalAudioStreamWithResponseSpec(UUID itemId, List<String> container, String mediaSourceId, String deviceId, UUID userId, String audioCodec, Integer maxAudioChannels, Integer transcodingAudioChannels, Integer maxStreamingBitrate, Integer audioBitRate, Long startTimeTicks, String transcodingContainer, String transcodingProtocol, Integer maxAudioSampleRate, Integer maxAudioBitDepth, Boolean enableRemoteMedia, Boolean breakOnNonKeyFrames, Boolean enableRedirection) throws WebClientResponseException {
        return headUniversalAudioStreamRequestCreation(itemId, container, mediaSourceId, deviceId, userId, audioCodec, maxAudioChannels, transcodingAudioChannels, maxStreamingBitrate, audioBitRate, startTimeTicks, transcodingContainer, transcodingProtocol, maxAudioSampleRate, maxAudioBitDepth, enableRemoteMedia, breakOnNonKeyFrames, enableRedirection);
    }
}
