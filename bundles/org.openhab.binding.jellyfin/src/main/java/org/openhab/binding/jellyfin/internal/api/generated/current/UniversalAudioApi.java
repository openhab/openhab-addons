package org.openhab.binding.jellyfin.internal.api.generated.current;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.core.GenericType;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;
import org.openhab.binding.jellyfin.internal.api.generated.ApiException;
import org.openhab.binding.jellyfin.internal.api.generated.ApiResponse;
import org.openhab.binding.jellyfin.internal.api.generated.Configuration;
import org.openhab.binding.jellyfin.internal.api.generated.Pair;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.MediaStreamProtocol;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class UniversalAudioApi {
    private ApiClient apiClient;

    public UniversalAudioApi() {
        this(Configuration.getDefaultApiClient());
    }

    public UniversalAudioApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Get the API client
     *
     * @return API client
     */
    public ApiClient getApiClient() {
        return apiClient;
    }

    /**
     * Set the API client
     *
     * @param apiClient an instance of API client
     */
    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Gets an audio stream.
     * 
     * @param itemId The item id. (required)
     * @param container Optional. The audio container. (optional)
     * @param mediaSourceId The media version id, if playing an alternate version. (optional)
     * @param deviceId The device id of the client requesting. Used to stop encoding processes when needed. (optional)
     * @param userId Optional. The user id. (optional)
     * @param audioCodec Optional. The audio codec to transcode to. (optional)
     * @param maxAudioChannels Optional. The maximum number of audio channels. (optional)
     * @param transcodingAudioChannels Optional. The number of how many audio channels to transcode to. (optional)
     * @param maxStreamingBitrate Optional. The maximum streaming bitrate. (optional)
     * @param audioBitRate Optional. Specify an audio bitrate to encode to, e.g. 128000. If omitted this will be left to
     *            encoder defaults. (optional)
     * @param startTimeTicks Optional. Specify a starting offset, in ticks. 1 tick &#x3D; 10000 ms. (optional)
     * @param transcodingContainer Optional. The container to transcode to. (optional)
     * @param transcodingProtocol Optional. The transcoding protocol. (optional)
     * @param maxAudioSampleRate Optional. The maximum audio sample rate. (optional)
     * @param maxAudioBitDepth Optional. The maximum audio bit depth. (optional)
     * @param enableRemoteMedia Optional. Whether to enable remote media. (optional)
     * @param enableAudioVbrEncoding Optional. Whether to enable Audio Encoding. (optional, default to true)
     * @param breakOnNonKeyFrames Optional. Whether to break on non key frames. (optional, default to false)
     * @param enableRedirection Whether to enable redirection. Defaults to true. (optional, default to true)
     * @return File
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Audio stream returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>302</td>
     *                        <td>Redirected to remote audio stream.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Item not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public File getUniversalAudioStream(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull List<String> container,
            @org.eclipse.jdt.annotation.NonNull String mediaSourceId,
            @org.eclipse.jdt.annotation.NonNull String deviceId, @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull String audioCodec,
            @org.eclipse.jdt.annotation.NonNull Integer maxAudioChannels,
            @org.eclipse.jdt.annotation.NonNull Integer transcodingAudioChannels,
            @org.eclipse.jdt.annotation.NonNull Integer maxStreamingBitrate,
            @org.eclipse.jdt.annotation.NonNull Integer audioBitRate,
            @org.eclipse.jdt.annotation.NonNull Long startTimeTicks,
            @org.eclipse.jdt.annotation.NonNull String transcodingContainer,
            @org.eclipse.jdt.annotation.NonNull MediaStreamProtocol transcodingProtocol,
            @org.eclipse.jdt.annotation.NonNull Integer maxAudioSampleRate,
            @org.eclipse.jdt.annotation.NonNull Integer maxAudioBitDepth,
            @org.eclipse.jdt.annotation.NonNull Boolean enableRemoteMedia,
            @org.eclipse.jdt.annotation.NonNull Boolean enableAudioVbrEncoding,
            @org.eclipse.jdt.annotation.NonNull Boolean breakOnNonKeyFrames,
            @org.eclipse.jdt.annotation.NonNull Boolean enableRedirection) throws ApiException {
        return getUniversalAudioStreamWithHttpInfo(itemId, container, mediaSourceId, deviceId, userId, audioCodec,
                maxAudioChannels, transcodingAudioChannels, maxStreamingBitrate, audioBitRate, startTimeTicks,
                transcodingContainer, transcodingProtocol, maxAudioSampleRate, maxAudioBitDepth, enableRemoteMedia,
                enableAudioVbrEncoding, breakOnNonKeyFrames, enableRedirection).getData();
    }

    /**
     * Gets an audio stream.
     * 
     * @param itemId The item id. (required)
     * @param container Optional. The audio container. (optional)
     * @param mediaSourceId The media version id, if playing an alternate version. (optional)
     * @param deviceId The device id of the client requesting. Used to stop encoding processes when needed. (optional)
     * @param userId Optional. The user id. (optional)
     * @param audioCodec Optional. The audio codec to transcode to. (optional)
     * @param maxAudioChannels Optional. The maximum number of audio channels. (optional)
     * @param transcodingAudioChannels Optional. The number of how many audio channels to transcode to. (optional)
     * @param maxStreamingBitrate Optional. The maximum streaming bitrate. (optional)
     * @param audioBitRate Optional. Specify an audio bitrate to encode to, e.g. 128000. If omitted this will be left to
     *            encoder defaults. (optional)
     * @param startTimeTicks Optional. Specify a starting offset, in ticks. 1 tick &#x3D; 10000 ms. (optional)
     * @param transcodingContainer Optional. The container to transcode to. (optional)
     * @param transcodingProtocol Optional. The transcoding protocol. (optional)
     * @param maxAudioSampleRate Optional. The maximum audio sample rate. (optional)
     * @param maxAudioBitDepth Optional. The maximum audio bit depth. (optional)
     * @param enableRemoteMedia Optional. Whether to enable remote media. (optional)
     * @param enableAudioVbrEncoding Optional. Whether to enable Audio Encoding. (optional, default to true)
     * @param breakOnNonKeyFrames Optional. Whether to break on non key frames. (optional, default to false)
     * @param enableRedirection Whether to enable redirection. Defaults to true. (optional, default to true)
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Audio stream returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>302</td>
     *                        <td>Redirected to remote audio stream.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Item not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<File> getUniversalAudioStreamWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull List<String> container,
            @org.eclipse.jdt.annotation.NonNull String mediaSourceId,
            @org.eclipse.jdt.annotation.NonNull String deviceId, @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull String audioCodec,
            @org.eclipse.jdt.annotation.NonNull Integer maxAudioChannels,
            @org.eclipse.jdt.annotation.NonNull Integer transcodingAudioChannels,
            @org.eclipse.jdt.annotation.NonNull Integer maxStreamingBitrate,
            @org.eclipse.jdt.annotation.NonNull Integer audioBitRate,
            @org.eclipse.jdt.annotation.NonNull Long startTimeTicks,
            @org.eclipse.jdt.annotation.NonNull String transcodingContainer,
            @org.eclipse.jdt.annotation.NonNull MediaStreamProtocol transcodingProtocol,
            @org.eclipse.jdt.annotation.NonNull Integer maxAudioSampleRate,
            @org.eclipse.jdt.annotation.NonNull Integer maxAudioBitDepth,
            @org.eclipse.jdt.annotation.NonNull Boolean enableRemoteMedia,
            @org.eclipse.jdt.annotation.NonNull Boolean enableAudioVbrEncoding,
            @org.eclipse.jdt.annotation.NonNull Boolean breakOnNonKeyFrames,
            @org.eclipse.jdt.annotation.NonNull Boolean enableRedirection) throws ApiException {
        // Check required parameters
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling getUniversalAudioStream");
        }

        // Path parameters
        String localVarPath = "/Audio/{itemId}/universal".replaceAll("\\{itemId}",
                apiClient.escapeString(itemId.toString()));

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("multi", "container", container));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "mediaSourceId", mediaSourceId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "deviceId", deviceId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "userId", userId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "audioCodec", audioCodec));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxAudioChannels", maxAudioChannels));
        localVarQueryParams
                .addAll(apiClient.parameterToPairs("", "transcodingAudioChannels", transcodingAudioChannels));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxStreamingBitrate", maxStreamingBitrate));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "audioBitRate", audioBitRate));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "startTimeTicks", startTimeTicks));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "transcodingContainer", transcodingContainer));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "transcodingProtocol", transcodingProtocol));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxAudioSampleRate", maxAudioSampleRate));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxAudioBitDepth", maxAudioBitDepth));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "enableRemoteMedia", enableRemoteMedia));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "enableAudioVbrEncoding", enableAudioVbrEncoding));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "breakOnNonKeyFrames", breakOnNonKeyFrames));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "enableRedirection", enableRedirection));

        String localVarAccept = apiClient.selectHeaderAccept("audio/*", "application/json",
                "application/json; profile=CamelCase", "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<File> localVarReturnType = new GenericType<File>() {
        };
        return apiClient.invokeAPI("UniversalAudioApi.getUniversalAudioStream", localVarPath, "GET",
                localVarQueryParams, null, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(),
                localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType, false);
    }

    /**
     * Gets an audio stream.
     * 
     * @param itemId The item id. (required)
     * @param container Optional. The audio container. (optional)
     * @param mediaSourceId The media version id, if playing an alternate version. (optional)
     * @param deviceId The device id of the client requesting. Used to stop encoding processes when needed. (optional)
     * @param userId Optional. The user id. (optional)
     * @param audioCodec Optional. The audio codec to transcode to. (optional)
     * @param maxAudioChannels Optional. The maximum number of audio channels. (optional)
     * @param transcodingAudioChannels Optional. The number of how many audio channels to transcode to. (optional)
     * @param maxStreamingBitrate Optional. The maximum streaming bitrate. (optional)
     * @param audioBitRate Optional. Specify an audio bitrate to encode to, e.g. 128000. If omitted this will be left to
     *            encoder defaults. (optional)
     * @param startTimeTicks Optional. Specify a starting offset, in ticks. 1 tick &#x3D; 10000 ms. (optional)
     * @param transcodingContainer Optional. The container to transcode to. (optional)
     * @param transcodingProtocol Optional. The transcoding protocol. (optional)
     * @param maxAudioSampleRate Optional. The maximum audio sample rate. (optional)
     * @param maxAudioBitDepth Optional. The maximum audio bit depth. (optional)
     * @param enableRemoteMedia Optional. Whether to enable remote media. (optional)
     * @param enableAudioVbrEncoding Optional. Whether to enable Audio Encoding. (optional, default to true)
     * @param breakOnNonKeyFrames Optional. Whether to break on non key frames. (optional, default to false)
     * @param enableRedirection Whether to enable redirection. Defaults to true. (optional, default to true)
     * @return File
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Audio stream returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>302</td>
     *                        <td>Redirected to remote audio stream.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Item not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public File headUniversalAudioStream(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull List<String> container,
            @org.eclipse.jdt.annotation.NonNull String mediaSourceId,
            @org.eclipse.jdt.annotation.NonNull String deviceId, @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull String audioCodec,
            @org.eclipse.jdt.annotation.NonNull Integer maxAudioChannels,
            @org.eclipse.jdt.annotation.NonNull Integer transcodingAudioChannels,
            @org.eclipse.jdt.annotation.NonNull Integer maxStreamingBitrate,
            @org.eclipse.jdt.annotation.NonNull Integer audioBitRate,
            @org.eclipse.jdt.annotation.NonNull Long startTimeTicks,
            @org.eclipse.jdt.annotation.NonNull String transcodingContainer,
            @org.eclipse.jdt.annotation.NonNull MediaStreamProtocol transcodingProtocol,
            @org.eclipse.jdt.annotation.NonNull Integer maxAudioSampleRate,
            @org.eclipse.jdt.annotation.NonNull Integer maxAudioBitDepth,
            @org.eclipse.jdt.annotation.NonNull Boolean enableRemoteMedia,
            @org.eclipse.jdt.annotation.NonNull Boolean enableAudioVbrEncoding,
            @org.eclipse.jdt.annotation.NonNull Boolean breakOnNonKeyFrames,
            @org.eclipse.jdt.annotation.NonNull Boolean enableRedirection) throws ApiException {
        return headUniversalAudioStreamWithHttpInfo(itemId, container, mediaSourceId, deviceId, userId, audioCodec,
                maxAudioChannels, transcodingAudioChannels, maxStreamingBitrate, audioBitRate, startTimeTicks,
                transcodingContainer, transcodingProtocol, maxAudioSampleRate, maxAudioBitDepth, enableRemoteMedia,
                enableAudioVbrEncoding, breakOnNonKeyFrames, enableRedirection).getData();
    }

    /**
     * Gets an audio stream.
     * 
     * @param itemId The item id. (required)
     * @param container Optional. The audio container. (optional)
     * @param mediaSourceId The media version id, if playing an alternate version. (optional)
     * @param deviceId The device id of the client requesting. Used to stop encoding processes when needed. (optional)
     * @param userId Optional. The user id. (optional)
     * @param audioCodec Optional. The audio codec to transcode to. (optional)
     * @param maxAudioChannels Optional. The maximum number of audio channels. (optional)
     * @param transcodingAudioChannels Optional. The number of how many audio channels to transcode to. (optional)
     * @param maxStreamingBitrate Optional. The maximum streaming bitrate. (optional)
     * @param audioBitRate Optional. Specify an audio bitrate to encode to, e.g. 128000. If omitted this will be left to
     *            encoder defaults. (optional)
     * @param startTimeTicks Optional. Specify a starting offset, in ticks. 1 tick &#x3D; 10000 ms. (optional)
     * @param transcodingContainer Optional. The container to transcode to. (optional)
     * @param transcodingProtocol Optional. The transcoding protocol. (optional)
     * @param maxAudioSampleRate Optional. The maximum audio sample rate. (optional)
     * @param maxAudioBitDepth Optional. The maximum audio bit depth. (optional)
     * @param enableRemoteMedia Optional. Whether to enable remote media. (optional)
     * @param enableAudioVbrEncoding Optional. Whether to enable Audio Encoding. (optional, default to true)
     * @param breakOnNonKeyFrames Optional. Whether to break on non key frames. (optional, default to false)
     * @param enableRedirection Whether to enable redirection. Defaults to true. (optional, default to true)
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     * @http.response.details
     *                        <table border="1">
     *                        <caption>Response Details</caption>
     *                        <tr>
     *                        <td>Status Code</td>
     *                        <td>Description</td>
     *                        <td>Response Headers</td>
     *                        </tr>
     *                        <tr>
     *                        <td>200</td>
     *                        <td>Audio stream returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>302</td>
     *                        <td>Redirected to remote audio stream.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Item not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>401</td>
     *                        <td>Unauthorized</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>403</td>
     *                        <td>Forbidden</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<File> headUniversalAudioStreamWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull List<String> container,
            @org.eclipse.jdt.annotation.NonNull String mediaSourceId,
            @org.eclipse.jdt.annotation.NonNull String deviceId, @org.eclipse.jdt.annotation.NonNull UUID userId,
            @org.eclipse.jdt.annotation.NonNull String audioCodec,
            @org.eclipse.jdt.annotation.NonNull Integer maxAudioChannels,
            @org.eclipse.jdt.annotation.NonNull Integer transcodingAudioChannels,
            @org.eclipse.jdt.annotation.NonNull Integer maxStreamingBitrate,
            @org.eclipse.jdt.annotation.NonNull Integer audioBitRate,
            @org.eclipse.jdt.annotation.NonNull Long startTimeTicks,
            @org.eclipse.jdt.annotation.NonNull String transcodingContainer,
            @org.eclipse.jdt.annotation.NonNull MediaStreamProtocol transcodingProtocol,
            @org.eclipse.jdt.annotation.NonNull Integer maxAudioSampleRate,
            @org.eclipse.jdt.annotation.NonNull Integer maxAudioBitDepth,
            @org.eclipse.jdt.annotation.NonNull Boolean enableRemoteMedia,
            @org.eclipse.jdt.annotation.NonNull Boolean enableAudioVbrEncoding,
            @org.eclipse.jdt.annotation.NonNull Boolean breakOnNonKeyFrames,
            @org.eclipse.jdt.annotation.NonNull Boolean enableRedirection) throws ApiException {
        // Check required parameters
        if (itemId == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'itemId' when calling headUniversalAudioStream");
        }

        // Path parameters
        String localVarPath = "/Audio/{itemId}/universal".replaceAll("\\{itemId}",
                apiClient.escapeString(itemId.toString()));

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("multi", "container", container));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "mediaSourceId", mediaSourceId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "deviceId", deviceId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "userId", userId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "audioCodec", audioCodec));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxAudioChannels", maxAudioChannels));
        localVarQueryParams
                .addAll(apiClient.parameterToPairs("", "transcodingAudioChannels", transcodingAudioChannels));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxStreamingBitrate", maxStreamingBitrate));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "audioBitRate", audioBitRate));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "startTimeTicks", startTimeTicks));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "transcodingContainer", transcodingContainer));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "transcodingProtocol", transcodingProtocol));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxAudioSampleRate", maxAudioSampleRate));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxAudioBitDepth", maxAudioBitDepth));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "enableRemoteMedia", enableRemoteMedia));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "enableAudioVbrEncoding", enableAudioVbrEncoding));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "breakOnNonKeyFrames", breakOnNonKeyFrames));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "enableRedirection", enableRedirection));

        String localVarAccept = apiClient.selectHeaderAccept("audio/*", "application/json",
                "application/json; profile=CamelCase", "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<File> localVarReturnType = new GenericType<File>() {
        };
        return apiClient.invokeAPI("UniversalAudioApi.headUniversalAudioStream", localVarPath, "HEAD",
                localVarQueryParams, null, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(),
                localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType, false);
    }
}
