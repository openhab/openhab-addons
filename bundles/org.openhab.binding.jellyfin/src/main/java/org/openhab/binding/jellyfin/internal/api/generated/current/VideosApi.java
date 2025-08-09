package org.openhab.binding.jellyfin.internal.api.generated.current;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.core.GenericType;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;
import org.openhab.binding.jellyfin.internal.api.generated.ApiException;
import org.openhab.binding.jellyfin.internal.api.generated.ApiResponse;
import org.openhab.binding.jellyfin.internal.api.generated.Configuration;
import org.openhab.binding.jellyfin.internal.api.generated.Pair;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.BaseItemDtoQueryResult;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.EncodingContext;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.SubtitleDeliveryMethod;

public class VideosApi {
    private ApiClient apiClient;

    public VideosApi() {
        this(Configuration.getDefaultApiClient());
    }

    public VideosApi(ApiClient apiClient) {
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
     * Removes alternate video sources.
     * 
     * @param itemId The item id. (required)
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
     *                        <td>204</td>
     *                        <td>Alternate sources deleted.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Video not found.</td>
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
    public void deleteAlternateSources(@org.eclipse.jdt.annotation.Nullable UUID itemId) throws ApiException {
        deleteAlternateSourcesWithHttpInfo(itemId);
    }

    /**
     * Removes alternate video sources.
     * 
     * @param itemId The item id. (required)
     * @return ApiResponse&lt;Void&gt;
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
     *                        <td>204</td>
     *                        <td>Alternate sources deleted.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Video not found.</td>
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
    public ApiResponse<Void> deleteAlternateSourcesWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId)
            throws ApiException {
        // Check required parameters
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling deleteAlternateSources");
        }

        // Path parameters
        String localVarPath = "/Videos/{itemId}/AlternateSources".replaceAll("\\{itemId}",
                apiClient.escapeString(itemId.toString()));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        return apiClient.invokeAPI("VideosApi.deleteAlternateSources", localVarPath, "DELETE", new ArrayList<>(), null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, null, false);
    }

    /**
     * Gets additional parts for a video.
     * 
     * @param itemId The item id. (required)
     * @param userId Optional. Filter by user id, and attach user data. (optional)
     * @return BaseItemDtoQueryResult
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
     *                        <td>Additional parts returned.</td>
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
    public BaseItemDtoQueryResult getAdditionalPart(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull UUID userId) throws ApiException {
        return getAdditionalPartWithHttpInfo(itemId, userId).getData();
    }

    /**
     * Gets additional parts for a video.
     * 
     * @param itemId The item id. (required)
     * @param userId Optional. Filter by user id, and attach user data. (optional)
     * @return ApiResponse&lt;BaseItemDtoQueryResult&gt;
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
     *                        <td>Additional parts returned.</td>
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
    public ApiResponse<BaseItemDtoQueryResult> getAdditionalPartWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable UUID itemId, @org.eclipse.jdt.annotation.NonNull UUID userId)
            throws ApiException {
        // Check required parameters
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling getAdditionalPart");
        }

        // Path parameters
        String localVarPath = "/Videos/{itemId}/AdditionalParts".replaceAll("\\{itemId}",
                apiClient.escapeString(itemId.toString()));

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "userId", userId));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<BaseItemDtoQueryResult> localVarReturnType = new GenericType<BaseItemDtoQueryResult>() {
        };
        return apiClient.invokeAPI("VideosApi.getAdditionalPart", localVarPath, "GET", localVarQueryParams, null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, localVarReturnType, false);
    }

    /**
     * Gets a video stream.
     * 
     * @param itemId The item id. (required)
     * @param container The video container. Possible values are: ts, webm, asf, wmv, ogv, mp4, m4v, mkv, mpeg, mpg,
     *            avi, 3gp, wmv, wtv, m2ts, mov, iso, flv. (optional)
     * @param _static Optional. If true, the original file will be streamed statically without any encoding. Use either
     *            no url extension or the original file extension. true/false. (optional)
     * @param params The streaming parameters. (optional)
     * @param tag The tag. (optional)
     * @param deviceProfileId Optional. The dlna device profile id to utilize. (optional)
     * @param playSessionId The play session id. (optional)
     * @param segmentContainer The segment container. (optional)
     * @param segmentLength The segment length. (optional)
     * @param minSegments The minimum number of segments. (optional)
     * @param mediaSourceId The media version id, if playing an alternate version. (optional)
     * @param deviceId The device id of the client requesting. Used to stop encoding processes when needed. (optional)
     * @param audioCodec Optional. Specify an audio codec to encode to, e.g. mp3. If omitted the server will auto-select
     *            using the url&#39;s extension. (optional)
     * @param enableAutoStreamCopy Whether or not to allow automatic stream copy if requested values match the original
     *            source. Defaults to true. (optional)
     * @param allowVideoStreamCopy Whether or not to allow copying of the video stream url. (optional)
     * @param allowAudioStreamCopy Whether or not to allow copying of the audio stream url. (optional)
     * @param breakOnNonKeyFrames Optional. Whether to break on non key frames. (optional)
     * @param audioSampleRate Optional. Specify a specific audio sample rate, e.g. 44100. (optional)
     * @param maxAudioBitDepth Optional. The maximum audio bit depth. (optional)
     * @param audioBitRate Optional. Specify an audio bitrate to encode to, e.g. 128000. If omitted this will be left to
     *            encoder defaults. (optional)
     * @param audioChannels Optional. Specify a specific number of audio channels to encode to, e.g. 2. (optional)
     * @param maxAudioChannels Optional. Specify a maximum number of audio channels to encode to, e.g. 2. (optional)
     * @param profile Optional. Specify a specific an encoder profile (varies by encoder), e.g. main, baseline, high.
     *            (optional)
     * @param level Optional. Specify a level for the encoder profile (varies by encoder), e.g. 3, 3.1. (optional)
     * @param framerate Optional. A specific video framerate to encode to, e.g. 23.976. Generally this should be omitted
     *            unless the device has specific requirements. (optional)
     * @param maxFramerate Optional. A specific maximum video framerate to encode to, e.g. 23.976. Generally this should
     *            be omitted unless the device has specific requirements. (optional)
     * @param copyTimestamps Whether or not to copy timestamps when transcoding with an offset. Defaults to false.
     *            (optional)
     * @param startTimeTicks Optional. Specify a starting offset, in ticks. 1 tick &#x3D; 10000 ms. (optional)
     * @param width Optional. The fixed horizontal resolution of the encoded video. (optional)
     * @param height Optional. The fixed vertical resolution of the encoded video. (optional)
     * @param maxWidth Optional. The maximum horizontal resolution of the encoded video. (optional)
     * @param maxHeight Optional. The maximum vertical resolution of the encoded video. (optional)
     * @param videoBitRate Optional. Specify a video bitrate to encode to, e.g. 500000. If omitted this will be left to
     *            encoder defaults. (optional)
     * @param subtitleStreamIndex Optional. The index of the subtitle stream to use. If omitted no subtitles will be
     *            used. (optional)
     * @param subtitleMethod Optional. Specify the subtitle delivery method. (optional)
     * @param maxRefFrames Optional. (optional)
     * @param maxVideoBitDepth Optional. The maximum video bit depth. (optional)
     * @param requireAvc Optional. Whether to require avc. (optional)
     * @param deInterlace Optional. Whether to deinterlace the video. (optional)
     * @param requireNonAnamorphic Optional. Whether to require a non anamorphic stream. (optional)
     * @param transcodingMaxAudioChannels Optional. The maximum number of audio channels to transcode. (optional)
     * @param cpuCoreLimit Optional. The limit of how many cpu cores to use. (optional)
     * @param liveStreamId The live stream id. (optional)
     * @param enableMpegtsM2TsMode Optional. Whether to enable the MpegtsM2Ts mode. (optional)
     * @param videoCodec Optional. Specify a video codec to encode to, e.g. h264. If omitted the server will auto-select
     *            using the url&#39;s extension. (optional)
     * @param subtitleCodec Optional. Specify a subtitle codec to encode to. (optional)
     * @param transcodeReasons Optional. The transcoding reason. (optional)
     * @param audioStreamIndex Optional. The index of the audio stream to use. If omitted the first audio stream will be
     *            used. (optional)
     * @param videoStreamIndex Optional. The index of the video stream to use. If omitted the first video stream will be
     *            used. (optional)
     * @param context Optional. The MediaBrowser.Model.Dlna.EncodingContext. (optional)
     * @param streamOptions Optional. The streaming options. (optional)
     * @param enableAudioVbrEncoding Optional. Whether to enable Audio Encoding. (optional, default to true)
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
     *                        <td>Video stream returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public File getVideoStream(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull String container, @org.eclipse.jdt.annotation.NonNull Boolean _static,
            @org.eclipse.jdt.annotation.NonNull String params, @org.eclipse.jdt.annotation.NonNull String tag,
            @org.eclipse.jdt.annotation.NonNull String deviceProfileId,
            @org.eclipse.jdt.annotation.NonNull String playSessionId,
            @org.eclipse.jdt.annotation.NonNull String segmentContainer,
            @org.eclipse.jdt.annotation.NonNull Integer segmentLength,
            @org.eclipse.jdt.annotation.NonNull Integer minSegments,
            @org.eclipse.jdt.annotation.NonNull String mediaSourceId,
            @org.eclipse.jdt.annotation.NonNull String deviceId, @org.eclipse.jdt.annotation.NonNull String audioCodec,
            @org.eclipse.jdt.annotation.NonNull Boolean enableAutoStreamCopy,
            @org.eclipse.jdt.annotation.NonNull Boolean allowVideoStreamCopy,
            @org.eclipse.jdt.annotation.NonNull Boolean allowAudioStreamCopy,
            @org.eclipse.jdt.annotation.NonNull Boolean breakOnNonKeyFrames,
            @org.eclipse.jdt.annotation.NonNull Integer audioSampleRate,
            @org.eclipse.jdt.annotation.NonNull Integer maxAudioBitDepth,
            @org.eclipse.jdt.annotation.NonNull Integer audioBitRate,
            @org.eclipse.jdt.annotation.NonNull Integer audioChannels,
            @org.eclipse.jdt.annotation.NonNull Integer maxAudioChannels,
            @org.eclipse.jdt.annotation.NonNull String profile, @org.eclipse.jdt.annotation.NonNull String level,
            @org.eclipse.jdt.annotation.NonNull Float framerate, @org.eclipse.jdt.annotation.NonNull Float maxFramerate,
            @org.eclipse.jdt.annotation.NonNull Boolean copyTimestamps,
            @org.eclipse.jdt.annotation.NonNull Long startTimeTicks, @org.eclipse.jdt.annotation.NonNull Integer width,
            @org.eclipse.jdt.annotation.NonNull Integer height, @org.eclipse.jdt.annotation.NonNull Integer maxWidth,
            @org.eclipse.jdt.annotation.NonNull Integer maxHeight,
            @org.eclipse.jdt.annotation.NonNull Integer videoBitRate,
            @org.eclipse.jdt.annotation.NonNull Integer subtitleStreamIndex,
            @org.eclipse.jdt.annotation.NonNull SubtitleDeliveryMethod subtitleMethod,
            @org.eclipse.jdt.annotation.NonNull Integer maxRefFrames,
            @org.eclipse.jdt.annotation.NonNull Integer maxVideoBitDepth,
            @org.eclipse.jdt.annotation.NonNull Boolean requireAvc,
            @org.eclipse.jdt.annotation.NonNull Boolean deInterlace,
            @org.eclipse.jdt.annotation.NonNull Boolean requireNonAnamorphic,
            @org.eclipse.jdt.annotation.NonNull Integer transcodingMaxAudioChannels,
            @org.eclipse.jdt.annotation.NonNull Integer cpuCoreLimit,
            @org.eclipse.jdt.annotation.NonNull String liveStreamId,
            @org.eclipse.jdt.annotation.NonNull Boolean enableMpegtsM2TsMode,
            @org.eclipse.jdt.annotation.NonNull String videoCodec,
            @org.eclipse.jdt.annotation.NonNull String subtitleCodec,
            @org.eclipse.jdt.annotation.NonNull String transcodeReasons,
            @org.eclipse.jdt.annotation.NonNull Integer audioStreamIndex,
            @org.eclipse.jdt.annotation.NonNull Integer videoStreamIndex,
            @org.eclipse.jdt.annotation.NonNull EncodingContext context,
            @org.eclipse.jdt.annotation.NonNull Map<String, String> streamOptions,
            @org.eclipse.jdt.annotation.NonNull Boolean enableAudioVbrEncoding) throws ApiException {
        return getVideoStreamWithHttpInfo(itemId, container, _static, params, tag, deviceProfileId, playSessionId,
                segmentContainer, segmentLength, minSegments, mediaSourceId, deviceId, audioCodec, enableAutoStreamCopy,
                allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth,
                audioBitRate, audioChannels, maxAudioChannels, profile, level, framerate, maxFramerate, copyTimestamps,
                startTimeTicks, width, height, maxWidth, maxHeight, videoBitRate, subtitleStreamIndex, subtitleMethod,
                maxRefFrames, maxVideoBitDepth, requireAvc, deInterlace, requireNonAnamorphic,
                transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode, videoCodec,
                subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex, context, streamOptions,
                enableAudioVbrEncoding).getData();
    }

    /**
     * Gets a video stream.
     * 
     * @param itemId The item id. (required)
     * @param container The video container. Possible values are: ts, webm, asf, wmv, ogv, mp4, m4v, mkv, mpeg, mpg,
     *            avi, 3gp, wmv, wtv, m2ts, mov, iso, flv. (optional)
     * @param _static Optional. If true, the original file will be streamed statically without any encoding. Use either
     *            no url extension or the original file extension. true/false. (optional)
     * @param params The streaming parameters. (optional)
     * @param tag The tag. (optional)
     * @param deviceProfileId Optional. The dlna device profile id to utilize. (optional)
     * @param playSessionId The play session id. (optional)
     * @param segmentContainer The segment container. (optional)
     * @param segmentLength The segment length. (optional)
     * @param minSegments The minimum number of segments. (optional)
     * @param mediaSourceId The media version id, if playing an alternate version. (optional)
     * @param deviceId The device id of the client requesting. Used to stop encoding processes when needed. (optional)
     * @param audioCodec Optional. Specify an audio codec to encode to, e.g. mp3. If omitted the server will auto-select
     *            using the url&#39;s extension. (optional)
     * @param enableAutoStreamCopy Whether or not to allow automatic stream copy if requested values match the original
     *            source. Defaults to true. (optional)
     * @param allowVideoStreamCopy Whether or not to allow copying of the video stream url. (optional)
     * @param allowAudioStreamCopy Whether or not to allow copying of the audio stream url. (optional)
     * @param breakOnNonKeyFrames Optional. Whether to break on non key frames. (optional)
     * @param audioSampleRate Optional. Specify a specific audio sample rate, e.g. 44100. (optional)
     * @param maxAudioBitDepth Optional. The maximum audio bit depth. (optional)
     * @param audioBitRate Optional. Specify an audio bitrate to encode to, e.g. 128000. If omitted this will be left to
     *            encoder defaults. (optional)
     * @param audioChannels Optional. Specify a specific number of audio channels to encode to, e.g. 2. (optional)
     * @param maxAudioChannels Optional. Specify a maximum number of audio channels to encode to, e.g. 2. (optional)
     * @param profile Optional. Specify a specific an encoder profile (varies by encoder), e.g. main, baseline, high.
     *            (optional)
     * @param level Optional. Specify a level for the encoder profile (varies by encoder), e.g. 3, 3.1. (optional)
     * @param framerate Optional. A specific video framerate to encode to, e.g. 23.976. Generally this should be omitted
     *            unless the device has specific requirements. (optional)
     * @param maxFramerate Optional. A specific maximum video framerate to encode to, e.g. 23.976. Generally this should
     *            be omitted unless the device has specific requirements. (optional)
     * @param copyTimestamps Whether or not to copy timestamps when transcoding with an offset. Defaults to false.
     *            (optional)
     * @param startTimeTicks Optional. Specify a starting offset, in ticks. 1 tick &#x3D; 10000 ms. (optional)
     * @param width Optional. The fixed horizontal resolution of the encoded video. (optional)
     * @param height Optional. The fixed vertical resolution of the encoded video. (optional)
     * @param maxWidth Optional. The maximum horizontal resolution of the encoded video. (optional)
     * @param maxHeight Optional. The maximum vertical resolution of the encoded video. (optional)
     * @param videoBitRate Optional. Specify a video bitrate to encode to, e.g. 500000. If omitted this will be left to
     *            encoder defaults. (optional)
     * @param subtitleStreamIndex Optional. The index of the subtitle stream to use. If omitted no subtitles will be
     *            used. (optional)
     * @param subtitleMethod Optional. Specify the subtitle delivery method. (optional)
     * @param maxRefFrames Optional. (optional)
     * @param maxVideoBitDepth Optional. The maximum video bit depth. (optional)
     * @param requireAvc Optional. Whether to require avc. (optional)
     * @param deInterlace Optional. Whether to deinterlace the video. (optional)
     * @param requireNonAnamorphic Optional. Whether to require a non anamorphic stream. (optional)
     * @param transcodingMaxAudioChannels Optional. The maximum number of audio channels to transcode. (optional)
     * @param cpuCoreLimit Optional. The limit of how many cpu cores to use. (optional)
     * @param liveStreamId The live stream id. (optional)
     * @param enableMpegtsM2TsMode Optional. Whether to enable the MpegtsM2Ts mode. (optional)
     * @param videoCodec Optional. Specify a video codec to encode to, e.g. h264. If omitted the server will auto-select
     *            using the url&#39;s extension. (optional)
     * @param subtitleCodec Optional. Specify a subtitle codec to encode to. (optional)
     * @param transcodeReasons Optional. The transcoding reason. (optional)
     * @param audioStreamIndex Optional. The index of the audio stream to use. If omitted the first audio stream will be
     *            used. (optional)
     * @param videoStreamIndex Optional. The index of the video stream to use. If omitted the first video stream will be
     *            used. (optional)
     * @param context Optional. The MediaBrowser.Model.Dlna.EncodingContext. (optional)
     * @param streamOptions Optional. The streaming options. (optional)
     * @param enableAudioVbrEncoding Optional. Whether to enable Audio Encoding. (optional, default to true)
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
     *                        <td>Video stream returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<File> getVideoStreamWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull String container, @org.eclipse.jdt.annotation.NonNull Boolean _static,
            @org.eclipse.jdt.annotation.NonNull String params, @org.eclipse.jdt.annotation.NonNull String tag,
            @org.eclipse.jdt.annotation.NonNull String deviceProfileId,
            @org.eclipse.jdt.annotation.NonNull String playSessionId,
            @org.eclipse.jdt.annotation.NonNull String segmentContainer,
            @org.eclipse.jdt.annotation.NonNull Integer segmentLength,
            @org.eclipse.jdt.annotation.NonNull Integer minSegments,
            @org.eclipse.jdt.annotation.NonNull String mediaSourceId,
            @org.eclipse.jdt.annotation.NonNull String deviceId, @org.eclipse.jdt.annotation.NonNull String audioCodec,
            @org.eclipse.jdt.annotation.NonNull Boolean enableAutoStreamCopy,
            @org.eclipse.jdt.annotation.NonNull Boolean allowVideoStreamCopy,
            @org.eclipse.jdt.annotation.NonNull Boolean allowAudioStreamCopy,
            @org.eclipse.jdt.annotation.NonNull Boolean breakOnNonKeyFrames,
            @org.eclipse.jdt.annotation.NonNull Integer audioSampleRate,
            @org.eclipse.jdt.annotation.NonNull Integer maxAudioBitDepth,
            @org.eclipse.jdt.annotation.NonNull Integer audioBitRate,
            @org.eclipse.jdt.annotation.NonNull Integer audioChannels,
            @org.eclipse.jdt.annotation.NonNull Integer maxAudioChannels,
            @org.eclipse.jdt.annotation.NonNull String profile, @org.eclipse.jdt.annotation.NonNull String level,
            @org.eclipse.jdt.annotation.NonNull Float framerate, @org.eclipse.jdt.annotation.NonNull Float maxFramerate,
            @org.eclipse.jdt.annotation.NonNull Boolean copyTimestamps,
            @org.eclipse.jdt.annotation.NonNull Long startTimeTicks, @org.eclipse.jdt.annotation.NonNull Integer width,
            @org.eclipse.jdt.annotation.NonNull Integer height, @org.eclipse.jdt.annotation.NonNull Integer maxWidth,
            @org.eclipse.jdt.annotation.NonNull Integer maxHeight,
            @org.eclipse.jdt.annotation.NonNull Integer videoBitRate,
            @org.eclipse.jdt.annotation.NonNull Integer subtitleStreamIndex,
            @org.eclipse.jdt.annotation.NonNull SubtitleDeliveryMethod subtitleMethod,
            @org.eclipse.jdt.annotation.NonNull Integer maxRefFrames,
            @org.eclipse.jdt.annotation.NonNull Integer maxVideoBitDepth,
            @org.eclipse.jdt.annotation.NonNull Boolean requireAvc,
            @org.eclipse.jdt.annotation.NonNull Boolean deInterlace,
            @org.eclipse.jdt.annotation.NonNull Boolean requireNonAnamorphic,
            @org.eclipse.jdt.annotation.NonNull Integer transcodingMaxAudioChannels,
            @org.eclipse.jdt.annotation.NonNull Integer cpuCoreLimit,
            @org.eclipse.jdt.annotation.NonNull String liveStreamId,
            @org.eclipse.jdt.annotation.NonNull Boolean enableMpegtsM2TsMode,
            @org.eclipse.jdt.annotation.NonNull String videoCodec,
            @org.eclipse.jdt.annotation.NonNull String subtitleCodec,
            @org.eclipse.jdt.annotation.NonNull String transcodeReasons,
            @org.eclipse.jdt.annotation.NonNull Integer audioStreamIndex,
            @org.eclipse.jdt.annotation.NonNull Integer videoStreamIndex,
            @org.eclipse.jdt.annotation.NonNull EncodingContext context,
            @org.eclipse.jdt.annotation.NonNull Map<String, String> streamOptions,
            @org.eclipse.jdt.annotation.NonNull Boolean enableAudioVbrEncoding) throws ApiException {
        // Check required parameters
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling getVideoStream");
        }

        // Path parameters
        String localVarPath = "/Videos/{itemId}/stream".replaceAll("\\{itemId}",
                apiClient.escapeString(itemId.toString()));

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "container", container));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "static", _static));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "params", params));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "tag", tag));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "deviceProfileId", deviceProfileId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "playSessionId", playSessionId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "segmentContainer", segmentContainer));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "segmentLength", segmentLength));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "minSegments", minSegments));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "mediaSourceId", mediaSourceId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "deviceId", deviceId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "audioCodec", audioCodec));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "enableAutoStreamCopy", enableAutoStreamCopy));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "allowVideoStreamCopy", allowVideoStreamCopy));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "allowAudioStreamCopy", allowAudioStreamCopy));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "breakOnNonKeyFrames", breakOnNonKeyFrames));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "audioSampleRate", audioSampleRate));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxAudioBitDepth", maxAudioBitDepth));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "audioBitRate", audioBitRate));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "audioChannels", audioChannels));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxAudioChannels", maxAudioChannels));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "profile", profile));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "level", level));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "framerate", framerate));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxFramerate", maxFramerate));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "copyTimestamps", copyTimestamps));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "startTimeTicks", startTimeTicks));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "width", width));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "height", height));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxWidth", maxWidth));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxHeight", maxHeight));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "videoBitRate", videoBitRate));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "subtitleStreamIndex", subtitleStreamIndex));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "subtitleMethod", subtitleMethod));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxRefFrames", maxRefFrames));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxVideoBitDepth", maxVideoBitDepth));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "requireAvc", requireAvc));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "deInterlace", deInterlace));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "requireNonAnamorphic", requireNonAnamorphic));
        localVarQueryParams
                .addAll(apiClient.parameterToPairs("", "transcodingMaxAudioChannels", transcodingMaxAudioChannels));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "cpuCoreLimit", cpuCoreLimit));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "liveStreamId", liveStreamId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "enableMpegtsM2TsMode", enableMpegtsM2TsMode));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "videoCodec", videoCodec));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "subtitleCodec", subtitleCodec));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "transcodeReasons", transcodeReasons));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "audioStreamIndex", audioStreamIndex));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "videoStreamIndex", videoStreamIndex));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "context", context));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "streamOptions", streamOptions));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "enableAudioVbrEncoding", enableAudioVbrEncoding));

        String localVarAccept = apiClient.selectHeaderAccept("video/*");
        String localVarContentType = apiClient.selectHeaderContentType();
        GenericType<File> localVarReturnType = new GenericType<File>() {
        };
        return apiClient.invokeAPI("VideosApi.getVideoStream", localVarPath, "GET", localVarQueryParams, null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, null, localVarReturnType, false);
    }

    /**
     * Gets a video stream.
     * 
     * @param itemId The item id. (required)
     * @param container The video container. Possible values are: ts, webm, asf, wmv, ogv, mp4, m4v, mkv, mpeg, mpg,
     *            avi, 3gp, wmv, wtv, m2ts, mov, iso, flv. (required)
     * @param _static Optional. If true, the original file will be streamed statically without any encoding. Use either
     *            no url extension or the original file extension. true/false. (optional)
     * @param params The streaming parameters. (optional)
     * @param tag The tag. (optional)
     * @param deviceProfileId Optional. The dlna device profile id to utilize. (optional)
     * @param playSessionId The play session id. (optional)
     * @param segmentContainer The segment container. (optional)
     * @param segmentLength The segment length. (optional)
     * @param minSegments The minimum number of segments. (optional)
     * @param mediaSourceId The media version id, if playing an alternate version. (optional)
     * @param deviceId The device id of the client requesting. Used to stop encoding processes when needed. (optional)
     * @param audioCodec Optional. Specify an audio codec to encode to, e.g. mp3. If omitted the server will auto-select
     *            using the url&#39;s extension. (optional)
     * @param enableAutoStreamCopy Whether or not to allow automatic stream copy if requested values match the original
     *            source. Defaults to true. (optional)
     * @param allowVideoStreamCopy Whether or not to allow copying of the video stream url. (optional)
     * @param allowAudioStreamCopy Whether or not to allow copying of the audio stream url. (optional)
     * @param breakOnNonKeyFrames Optional. Whether to break on non key frames. (optional)
     * @param audioSampleRate Optional. Specify a specific audio sample rate, e.g. 44100. (optional)
     * @param maxAudioBitDepth Optional. The maximum audio bit depth. (optional)
     * @param audioBitRate Optional. Specify an audio bitrate to encode to, e.g. 128000. If omitted this will be left to
     *            encoder defaults. (optional)
     * @param audioChannels Optional. Specify a specific number of audio channels to encode to, e.g. 2. (optional)
     * @param maxAudioChannels Optional. Specify a maximum number of audio channels to encode to, e.g. 2. (optional)
     * @param profile Optional. Specify a specific an encoder profile (varies by encoder), e.g. main, baseline, high.
     *            (optional)
     * @param level Optional. Specify a level for the encoder profile (varies by encoder), e.g. 3, 3.1. (optional)
     * @param framerate Optional. A specific video framerate to encode to, e.g. 23.976. Generally this should be omitted
     *            unless the device has specific requirements. (optional)
     * @param maxFramerate Optional. A specific maximum video framerate to encode to, e.g. 23.976. Generally this should
     *            be omitted unless the device has specific requirements. (optional)
     * @param copyTimestamps Whether or not to copy timestamps when transcoding with an offset. Defaults to false.
     *            (optional)
     * @param startTimeTicks Optional. Specify a starting offset, in ticks. 1 tick &#x3D; 10000 ms. (optional)
     * @param width Optional. The fixed horizontal resolution of the encoded video. (optional)
     * @param height Optional. The fixed vertical resolution of the encoded video. (optional)
     * @param maxWidth Optional. The maximum horizontal resolution of the encoded video. (optional)
     * @param maxHeight Optional. The maximum vertical resolution of the encoded video. (optional)
     * @param videoBitRate Optional. Specify a video bitrate to encode to, e.g. 500000. If omitted this will be left to
     *            encoder defaults. (optional)
     * @param subtitleStreamIndex Optional. The index of the subtitle stream to use. If omitted no subtitles will be
     *            used. (optional)
     * @param subtitleMethod Optional. Specify the subtitle delivery method. (optional)
     * @param maxRefFrames Optional. (optional)
     * @param maxVideoBitDepth Optional. The maximum video bit depth. (optional)
     * @param requireAvc Optional. Whether to require avc. (optional)
     * @param deInterlace Optional. Whether to deinterlace the video. (optional)
     * @param requireNonAnamorphic Optional. Whether to require a non anamorphic stream. (optional)
     * @param transcodingMaxAudioChannels Optional. The maximum number of audio channels to transcode. (optional)
     * @param cpuCoreLimit Optional. The limit of how many cpu cores to use. (optional)
     * @param liveStreamId The live stream id. (optional)
     * @param enableMpegtsM2TsMode Optional. Whether to enable the MpegtsM2Ts mode. (optional)
     * @param videoCodec Optional. Specify a video codec to encode to, e.g. h264. If omitted the server will auto-select
     *            using the url&#39;s extension. (optional)
     * @param subtitleCodec Optional. Specify a subtitle codec to encode to. (optional)
     * @param transcodeReasons Optional. The transcoding reason. (optional)
     * @param audioStreamIndex Optional. The index of the audio stream to use. If omitted the first audio stream will be
     *            used. (optional)
     * @param videoStreamIndex Optional. The index of the video stream to use. If omitted the first video stream will be
     *            used. (optional)
     * @param context Optional. The MediaBrowser.Model.Dlna.EncodingContext. (optional)
     * @param streamOptions Optional. The streaming options. (optional)
     * @param enableAudioVbrEncoding Optional. Whether to enable Audio Encoding. (optional, default to true)
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
     *                        <td>Video stream returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public File getVideoStreamByContainer(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable String container, @org.eclipse.jdt.annotation.NonNull Boolean _static,
            @org.eclipse.jdt.annotation.NonNull String params, @org.eclipse.jdt.annotation.NonNull String tag,
            @org.eclipse.jdt.annotation.NonNull String deviceProfileId,
            @org.eclipse.jdt.annotation.NonNull String playSessionId,
            @org.eclipse.jdt.annotation.NonNull String segmentContainer,
            @org.eclipse.jdt.annotation.NonNull Integer segmentLength,
            @org.eclipse.jdt.annotation.NonNull Integer minSegments,
            @org.eclipse.jdt.annotation.NonNull String mediaSourceId,
            @org.eclipse.jdt.annotation.NonNull String deviceId, @org.eclipse.jdt.annotation.NonNull String audioCodec,
            @org.eclipse.jdt.annotation.NonNull Boolean enableAutoStreamCopy,
            @org.eclipse.jdt.annotation.NonNull Boolean allowVideoStreamCopy,
            @org.eclipse.jdt.annotation.NonNull Boolean allowAudioStreamCopy,
            @org.eclipse.jdt.annotation.NonNull Boolean breakOnNonKeyFrames,
            @org.eclipse.jdt.annotation.NonNull Integer audioSampleRate,
            @org.eclipse.jdt.annotation.NonNull Integer maxAudioBitDepth,
            @org.eclipse.jdt.annotation.NonNull Integer audioBitRate,
            @org.eclipse.jdt.annotation.NonNull Integer audioChannels,
            @org.eclipse.jdt.annotation.NonNull Integer maxAudioChannels,
            @org.eclipse.jdt.annotation.NonNull String profile, @org.eclipse.jdt.annotation.NonNull String level,
            @org.eclipse.jdt.annotation.NonNull Float framerate, @org.eclipse.jdt.annotation.NonNull Float maxFramerate,
            @org.eclipse.jdt.annotation.NonNull Boolean copyTimestamps,
            @org.eclipse.jdt.annotation.NonNull Long startTimeTicks, @org.eclipse.jdt.annotation.NonNull Integer width,
            @org.eclipse.jdt.annotation.NonNull Integer height, @org.eclipse.jdt.annotation.NonNull Integer maxWidth,
            @org.eclipse.jdt.annotation.NonNull Integer maxHeight,
            @org.eclipse.jdt.annotation.NonNull Integer videoBitRate,
            @org.eclipse.jdt.annotation.NonNull Integer subtitleStreamIndex,
            @org.eclipse.jdt.annotation.NonNull SubtitleDeliveryMethod subtitleMethod,
            @org.eclipse.jdt.annotation.NonNull Integer maxRefFrames,
            @org.eclipse.jdt.annotation.NonNull Integer maxVideoBitDepth,
            @org.eclipse.jdt.annotation.NonNull Boolean requireAvc,
            @org.eclipse.jdt.annotation.NonNull Boolean deInterlace,
            @org.eclipse.jdt.annotation.NonNull Boolean requireNonAnamorphic,
            @org.eclipse.jdt.annotation.NonNull Integer transcodingMaxAudioChannels,
            @org.eclipse.jdt.annotation.NonNull Integer cpuCoreLimit,
            @org.eclipse.jdt.annotation.NonNull String liveStreamId,
            @org.eclipse.jdt.annotation.NonNull Boolean enableMpegtsM2TsMode,
            @org.eclipse.jdt.annotation.NonNull String videoCodec,
            @org.eclipse.jdt.annotation.NonNull String subtitleCodec,
            @org.eclipse.jdt.annotation.NonNull String transcodeReasons,
            @org.eclipse.jdt.annotation.NonNull Integer audioStreamIndex,
            @org.eclipse.jdt.annotation.NonNull Integer videoStreamIndex,
            @org.eclipse.jdt.annotation.NonNull EncodingContext context,
            @org.eclipse.jdt.annotation.NonNull Map<String, String> streamOptions,
            @org.eclipse.jdt.annotation.NonNull Boolean enableAudioVbrEncoding) throws ApiException {
        return getVideoStreamByContainerWithHttpInfo(itemId, container, _static, params, tag, deviceProfileId,
                playSessionId, segmentContainer, segmentLength, minSegments, mediaSourceId, deviceId, audioCodec,
                enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames, audioSampleRate,
                maxAudioBitDepth, audioBitRate, audioChannels, maxAudioChannels, profile, level, framerate,
                maxFramerate, copyTimestamps, startTimeTicks, width, height, maxWidth, maxHeight, videoBitRate,
                subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth, requireAvc, deInterlace,
                requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode,
                videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex, context, streamOptions,
                enableAudioVbrEncoding).getData();
    }

    /**
     * Gets a video stream.
     * 
     * @param itemId The item id. (required)
     * @param container The video container. Possible values are: ts, webm, asf, wmv, ogv, mp4, m4v, mkv, mpeg, mpg,
     *            avi, 3gp, wmv, wtv, m2ts, mov, iso, flv. (required)
     * @param _static Optional. If true, the original file will be streamed statically without any encoding. Use either
     *            no url extension or the original file extension. true/false. (optional)
     * @param params The streaming parameters. (optional)
     * @param tag The tag. (optional)
     * @param deviceProfileId Optional. The dlna device profile id to utilize. (optional)
     * @param playSessionId The play session id. (optional)
     * @param segmentContainer The segment container. (optional)
     * @param segmentLength The segment length. (optional)
     * @param minSegments The minimum number of segments. (optional)
     * @param mediaSourceId The media version id, if playing an alternate version. (optional)
     * @param deviceId The device id of the client requesting. Used to stop encoding processes when needed. (optional)
     * @param audioCodec Optional. Specify an audio codec to encode to, e.g. mp3. If omitted the server will auto-select
     *            using the url&#39;s extension. (optional)
     * @param enableAutoStreamCopy Whether or not to allow automatic stream copy if requested values match the original
     *            source. Defaults to true. (optional)
     * @param allowVideoStreamCopy Whether or not to allow copying of the video stream url. (optional)
     * @param allowAudioStreamCopy Whether or not to allow copying of the audio stream url. (optional)
     * @param breakOnNonKeyFrames Optional. Whether to break on non key frames. (optional)
     * @param audioSampleRate Optional. Specify a specific audio sample rate, e.g. 44100. (optional)
     * @param maxAudioBitDepth Optional. The maximum audio bit depth. (optional)
     * @param audioBitRate Optional. Specify an audio bitrate to encode to, e.g. 128000. If omitted this will be left to
     *            encoder defaults. (optional)
     * @param audioChannels Optional. Specify a specific number of audio channels to encode to, e.g. 2. (optional)
     * @param maxAudioChannels Optional. Specify a maximum number of audio channels to encode to, e.g. 2. (optional)
     * @param profile Optional. Specify a specific an encoder profile (varies by encoder), e.g. main, baseline, high.
     *            (optional)
     * @param level Optional. Specify a level for the encoder profile (varies by encoder), e.g. 3, 3.1. (optional)
     * @param framerate Optional. A specific video framerate to encode to, e.g. 23.976. Generally this should be omitted
     *            unless the device has specific requirements. (optional)
     * @param maxFramerate Optional. A specific maximum video framerate to encode to, e.g. 23.976. Generally this should
     *            be omitted unless the device has specific requirements. (optional)
     * @param copyTimestamps Whether or not to copy timestamps when transcoding with an offset. Defaults to false.
     *            (optional)
     * @param startTimeTicks Optional. Specify a starting offset, in ticks. 1 tick &#x3D; 10000 ms. (optional)
     * @param width Optional. The fixed horizontal resolution of the encoded video. (optional)
     * @param height Optional. The fixed vertical resolution of the encoded video. (optional)
     * @param maxWidth Optional. The maximum horizontal resolution of the encoded video. (optional)
     * @param maxHeight Optional. The maximum vertical resolution of the encoded video. (optional)
     * @param videoBitRate Optional. Specify a video bitrate to encode to, e.g. 500000. If omitted this will be left to
     *            encoder defaults. (optional)
     * @param subtitleStreamIndex Optional. The index of the subtitle stream to use. If omitted no subtitles will be
     *            used. (optional)
     * @param subtitleMethod Optional. Specify the subtitle delivery method. (optional)
     * @param maxRefFrames Optional. (optional)
     * @param maxVideoBitDepth Optional. The maximum video bit depth. (optional)
     * @param requireAvc Optional. Whether to require avc. (optional)
     * @param deInterlace Optional. Whether to deinterlace the video. (optional)
     * @param requireNonAnamorphic Optional. Whether to require a non anamorphic stream. (optional)
     * @param transcodingMaxAudioChannels Optional. The maximum number of audio channels to transcode. (optional)
     * @param cpuCoreLimit Optional. The limit of how many cpu cores to use. (optional)
     * @param liveStreamId The live stream id. (optional)
     * @param enableMpegtsM2TsMode Optional. Whether to enable the MpegtsM2Ts mode. (optional)
     * @param videoCodec Optional. Specify a video codec to encode to, e.g. h264. If omitted the server will auto-select
     *            using the url&#39;s extension. (optional)
     * @param subtitleCodec Optional. Specify a subtitle codec to encode to. (optional)
     * @param transcodeReasons Optional. The transcoding reason. (optional)
     * @param audioStreamIndex Optional. The index of the audio stream to use. If omitted the first audio stream will be
     *            used. (optional)
     * @param videoStreamIndex Optional. The index of the video stream to use. If omitted the first video stream will be
     *            used. (optional)
     * @param context Optional. The MediaBrowser.Model.Dlna.EncodingContext. (optional)
     * @param streamOptions Optional. The streaming options. (optional)
     * @param enableAudioVbrEncoding Optional. Whether to enable Audio Encoding. (optional, default to true)
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
     *                        <td>Video stream returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<File> getVideoStreamByContainerWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable String container, @org.eclipse.jdt.annotation.NonNull Boolean _static,
            @org.eclipse.jdt.annotation.NonNull String params, @org.eclipse.jdt.annotation.NonNull String tag,
            @org.eclipse.jdt.annotation.NonNull String deviceProfileId,
            @org.eclipse.jdt.annotation.NonNull String playSessionId,
            @org.eclipse.jdt.annotation.NonNull String segmentContainer,
            @org.eclipse.jdt.annotation.NonNull Integer segmentLength,
            @org.eclipse.jdt.annotation.NonNull Integer minSegments,
            @org.eclipse.jdt.annotation.NonNull String mediaSourceId,
            @org.eclipse.jdt.annotation.NonNull String deviceId, @org.eclipse.jdt.annotation.NonNull String audioCodec,
            @org.eclipse.jdt.annotation.NonNull Boolean enableAutoStreamCopy,
            @org.eclipse.jdt.annotation.NonNull Boolean allowVideoStreamCopy,
            @org.eclipse.jdt.annotation.NonNull Boolean allowAudioStreamCopy,
            @org.eclipse.jdt.annotation.NonNull Boolean breakOnNonKeyFrames,
            @org.eclipse.jdt.annotation.NonNull Integer audioSampleRate,
            @org.eclipse.jdt.annotation.NonNull Integer maxAudioBitDepth,
            @org.eclipse.jdt.annotation.NonNull Integer audioBitRate,
            @org.eclipse.jdt.annotation.NonNull Integer audioChannels,
            @org.eclipse.jdt.annotation.NonNull Integer maxAudioChannels,
            @org.eclipse.jdt.annotation.NonNull String profile, @org.eclipse.jdt.annotation.NonNull String level,
            @org.eclipse.jdt.annotation.NonNull Float framerate, @org.eclipse.jdt.annotation.NonNull Float maxFramerate,
            @org.eclipse.jdt.annotation.NonNull Boolean copyTimestamps,
            @org.eclipse.jdt.annotation.NonNull Long startTimeTicks, @org.eclipse.jdt.annotation.NonNull Integer width,
            @org.eclipse.jdt.annotation.NonNull Integer height, @org.eclipse.jdt.annotation.NonNull Integer maxWidth,
            @org.eclipse.jdt.annotation.NonNull Integer maxHeight,
            @org.eclipse.jdt.annotation.NonNull Integer videoBitRate,
            @org.eclipse.jdt.annotation.NonNull Integer subtitleStreamIndex,
            @org.eclipse.jdt.annotation.NonNull SubtitleDeliveryMethod subtitleMethod,
            @org.eclipse.jdt.annotation.NonNull Integer maxRefFrames,
            @org.eclipse.jdt.annotation.NonNull Integer maxVideoBitDepth,
            @org.eclipse.jdt.annotation.NonNull Boolean requireAvc,
            @org.eclipse.jdt.annotation.NonNull Boolean deInterlace,
            @org.eclipse.jdt.annotation.NonNull Boolean requireNonAnamorphic,
            @org.eclipse.jdt.annotation.NonNull Integer transcodingMaxAudioChannels,
            @org.eclipse.jdt.annotation.NonNull Integer cpuCoreLimit,
            @org.eclipse.jdt.annotation.NonNull String liveStreamId,
            @org.eclipse.jdt.annotation.NonNull Boolean enableMpegtsM2TsMode,
            @org.eclipse.jdt.annotation.NonNull String videoCodec,
            @org.eclipse.jdt.annotation.NonNull String subtitleCodec,
            @org.eclipse.jdt.annotation.NonNull String transcodeReasons,
            @org.eclipse.jdt.annotation.NonNull Integer audioStreamIndex,
            @org.eclipse.jdt.annotation.NonNull Integer videoStreamIndex,
            @org.eclipse.jdt.annotation.NonNull EncodingContext context,
            @org.eclipse.jdt.annotation.NonNull Map<String, String> streamOptions,
            @org.eclipse.jdt.annotation.NonNull Boolean enableAudioVbrEncoding) throws ApiException {
        // Check required parameters
        if (itemId == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'itemId' when calling getVideoStreamByContainer");
        }
        if (container == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'container' when calling getVideoStreamByContainer");
        }

        // Path parameters
        String localVarPath = "/Videos/{itemId}/stream.{container}"
                .replaceAll("\\{itemId}", apiClient.escapeString(itemId.toString()))
                .replaceAll("\\{container}", apiClient.escapeString(container.toString()));

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "static", _static));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "params", params));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "tag", tag));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "deviceProfileId", deviceProfileId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "playSessionId", playSessionId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "segmentContainer", segmentContainer));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "segmentLength", segmentLength));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "minSegments", minSegments));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "mediaSourceId", mediaSourceId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "deviceId", deviceId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "audioCodec", audioCodec));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "enableAutoStreamCopy", enableAutoStreamCopy));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "allowVideoStreamCopy", allowVideoStreamCopy));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "allowAudioStreamCopy", allowAudioStreamCopy));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "breakOnNonKeyFrames", breakOnNonKeyFrames));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "audioSampleRate", audioSampleRate));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxAudioBitDepth", maxAudioBitDepth));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "audioBitRate", audioBitRate));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "audioChannels", audioChannels));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxAudioChannels", maxAudioChannels));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "profile", profile));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "level", level));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "framerate", framerate));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxFramerate", maxFramerate));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "copyTimestamps", copyTimestamps));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "startTimeTicks", startTimeTicks));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "width", width));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "height", height));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxWidth", maxWidth));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxHeight", maxHeight));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "videoBitRate", videoBitRate));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "subtitleStreamIndex", subtitleStreamIndex));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "subtitleMethod", subtitleMethod));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxRefFrames", maxRefFrames));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxVideoBitDepth", maxVideoBitDepth));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "requireAvc", requireAvc));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "deInterlace", deInterlace));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "requireNonAnamorphic", requireNonAnamorphic));
        localVarQueryParams
                .addAll(apiClient.parameterToPairs("", "transcodingMaxAudioChannels", transcodingMaxAudioChannels));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "cpuCoreLimit", cpuCoreLimit));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "liveStreamId", liveStreamId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "enableMpegtsM2TsMode", enableMpegtsM2TsMode));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "videoCodec", videoCodec));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "subtitleCodec", subtitleCodec));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "transcodeReasons", transcodeReasons));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "audioStreamIndex", audioStreamIndex));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "videoStreamIndex", videoStreamIndex));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "context", context));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "streamOptions", streamOptions));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "enableAudioVbrEncoding", enableAudioVbrEncoding));

        String localVarAccept = apiClient.selectHeaderAccept("video/*");
        String localVarContentType = apiClient.selectHeaderContentType();
        GenericType<File> localVarReturnType = new GenericType<File>() {
        };
        return apiClient.invokeAPI("VideosApi.getVideoStreamByContainer", localVarPath, "GET", localVarQueryParams,
                null, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, null, localVarReturnType, false);
    }

    /**
     * Gets a video stream.
     * 
     * @param itemId The item id. (required)
     * @param container The video container. Possible values are: ts, webm, asf, wmv, ogv, mp4, m4v, mkv, mpeg, mpg,
     *            avi, 3gp, wmv, wtv, m2ts, mov, iso, flv. (optional)
     * @param _static Optional. If true, the original file will be streamed statically without any encoding. Use either
     *            no url extension or the original file extension. true/false. (optional)
     * @param params The streaming parameters. (optional)
     * @param tag The tag. (optional)
     * @param deviceProfileId Optional. The dlna device profile id to utilize. (optional)
     * @param playSessionId The play session id. (optional)
     * @param segmentContainer The segment container. (optional)
     * @param segmentLength The segment length. (optional)
     * @param minSegments The minimum number of segments. (optional)
     * @param mediaSourceId The media version id, if playing an alternate version. (optional)
     * @param deviceId The device id of the client requesting. Used to stop encoding processes when needed. (optional)
     * @param audioCodec Optional. Specify an audio codec to encode to, e.g. mp3. If omitted the server will auto-select
     *            using the url&#39;s extension. (optional)
     * @param enableAutoStreamCopy Whether or not to allow automatic stream copy if requested values match the original
     *            source. Defaults to true. (optional)
     * @param allowVideoStreamCopy Whether or not to allow copying of the video stream url. (optional)
     * @param allowAudioStreamCopy Whether or not to allow copying of the audio stream url. (optional)
     * @param breakOnNonKeyFrames Optional. Whether to break on non key frames. (optional)
     * @param audioSampleRate Optional. Specify a specific audio sample rate, e.g. 44100. (optional)
     * @param maxAudioBitDepth Optional. The maximum audio bit depth. (optional)
     * @param audioBitRate Optional. Specify an audio bitrate to encode to, e.g. 128000. If omitted this will be left to
     *            encoder defaults. (optional)
     * @param audioChannels Optional. Specify a specific number of audio channels to encode to, e.g. 2. (optional)
     * @param maxAudioChannels Optional. Specify a maximum number of audio channels to encode to, e.g. 2. (optional)
     * @param profile Optional. Specify a specific an encoder profile (varies by encoder), e.g. main, baseline, high.
     *            (optional)
     * @param level Optional. Specify a level for the encoder profile (varies by encoder), e.g. 3, 3.1. (optional)
     * @param framerate Optional. A specific video framerate to encode to, e.g. 23.976. Generally this should be omitted
     *            unless the device has specific requirements. (optional)
     * @param maxFramerate Optional. A specific maximum video framerate to encode to, e.g. 23.976. Generally this should
     *            be omitted unless the device has specific requirements. (optional)
     * @param copyTimestamps Whether or not to copy timestamps when transcoding with an offset. Defaults to false.
     *            (optional)
     * @param startTimeTicks Optional. Specify a starting offset, in ticks. 1 tick &#x3D; 10000 ms. (optional)
     * @param width Optional. The fixed horizontal resolution of the encoded video. (optional)
     * @param height Optional. The fixed vertical resolution of the encoded video. (optional)
     * @param maxWidth Optional. The maximum horizontal resolution of the encoded video. (optional)
     * @param maxHeight Optional. The maximum vertical resolution of the encoded video. (optional)
     * @param videoBitRate Optional. Specify a video bitrate to encode to, e.g. 500000. If omitted this will be left to
     *            encoder defaults. (optional)
     * @param subtitleStreamIndex Optional. The index of the subtitle stream to use. If omitted no subtitles will be
     *            used. (optional)
     * @param subtitleMethod Optional. Specify the subtitle delivery method. (optional)
     * @param maxRefFrames Optional. (optional)
     * @param maxVideoBitDepth Optional. The maximum video bit depth. (optional)
     * @param requireAvc Optional. Whether to require avc. (optional)
     * @param deInterlace Optional. Whether to deinterlace the video. (optional)
     * @param requireNonAnamorphic Optional. Whether to require a non anamorphic stream. (optional)
     * @param transcodingMaxAudioChannels Optional. The maximum number of audio channels to transcode. (optional)
     * @param cpuCoreLimit Optional. The limit of how many cpu cores to use. (optional)
     * @param liveStreamId The live stream id. (optional)
     * @param enableMpegtsM2TsMode Optional. Whether to enable the MpegtsM2Ts mode. (optional)
     * @param videoCodec Optional. Specify a video codec to encode to, e.g. h264. If omitted the server will auto-select
     *            using the url&#39;s extension. (optional)
     * @param subtitleCodec Optional. Specify a subtitle codec to encode to. (optional)
     * @param transcodeReasons Optional. The transcoding reason. (optional)
     * @param audioStreamIndex Optional. The index of the audio stream to use. If omitted the first audio stream will be
     *            used. (optional)
     * @param videoStreamIndex Optional. The index of the video stream to use. If omitted the first video stream will be
     *            used. (optional)
     * @param context Optional. The MediaBrowser.Model.Dlna.EncodingContext. (optional)
     * @param streamOptions Optional. The streaming options. (optional)
     * @param enableAudioVbrEncoding Optional. Whether to enable Audio Encoding. (optional, default to true)
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
     *                        <td>Video stream returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public File headVideoStream(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull String container, @org.eclipse.jdt.annotation.NonNull Boolean _static,
            @org.eclipse.jdt.annotation.NonNull String params, @org.eclipse.jdt.annotation.NonNull String tag,
            @org.eclipse.jdt.annotation.NonNull String deviceProfileId,
            @org.eclipse.jdt.annotation.NonNull String playSessionId,
            @org.eclipse.jdt.annotation.NonNull String segmentContainer,
            @org.eclipse.jdt.annotation.NonNull Integer segmentLength,
            @org.eclipse.jdt.annotation.NonNull Integer minSegments,
            @org.eclipse.jdt.annotation.NonNull String mediaSourceId,
            @org.eclipse.jdt.annotation.NonNull String deviceId, @org.eclipse.jdt.annotation.NonNull String audioCodec,
            @org.eclipse.jdt.annotation.NonNull Boolean enableAutoStreamCopy,
            @org.eclipse.jdt.annotation.NonNull Boolean allowVideoStreamCopy,
            @org.eclipse.jdt.annotation.NonNull Boolean allowAudioStreamCopy,
            @org.eclipse.jdt.annotation.NonNull Boolean breakOnNonKeyFrames,
            @org.eclipse.jdt.annotation.NonNull Integer audioSampleRate,
            @org.eclipse.jdt.annotation.NonNull Integer maxAudioBitDepth,
            @org.eclipse.jdt.annotation.NonNull Integer audioBitRate,
            @org.eclipse.jdt.annotation.NonNull Integer audioChannels,
            @org.eclipse.jdt.annotation.NonNull Integer maxAudioChannels,
            @org.eclipse.jdt.annotation.NonNull String profile, @org.eclipse.jdt.annotation.NonNull String level,
            @org.eclipse.jdt.annotation.NonNull Float framerate, @org.eclipse.jdt.annotation.NonNull Float maxFramerate,
            @org.eclipse.jdt.annotation.NonNull Boolean copyTimestamps,
            @org.eclipse.jdt.annotation.NonNull Long startTimeTicks, @org.eclipse.jdt.annotation.NonNull Integer width,
            @org.eclipse.jdt.annotation.NonNull Integer height, @org.eclipse.jdt.annotation.NonNull Integer maxWidth,
            @org.eclipse.jdt.annotation.NonNull Integer maxHeight,
            @org.eclipse.jdt.annotation.NonNull Integer videoBitRate,
            @org.eclipse.jdt.annotation.NonNull Integer subtitleStreamIndex,
            @org.eclipse.jdt.annotation.NonNull SubtitleDeliveryMethod subtitleMethod,
            @org.eclipse.jdt.annotation.NonNull Integer maxRefFrames,
            @org.eclipse.jdt.annotation.NonNull Integer maxVideoBitDepth,
            @org.eclipse.jdt.annotation.NonNull Boolean requireAvc,
            @org.eclipse.jdt.annotation.NonNull Boolean deInterlace,
            @org.eclipse.jdt.annotation.NonNull Boolean requireNonAnamorphic,
            @org.eclipse.jdt.annotation.NonNull Integer transcodingMaxAudioChannels,
            @org.eclipse.jdt.annotation.NonNull Integer cpuCoreLimit,
            @org.eclipse.jdt.annotation.NonNull String liveStreamId,
            @org.eclipse.jdt.annotation.NonNull Boolean enableMpegtsM2TsMode,
            @org.eclipse.jdt.annotation.NonNull String videoCodec,
            @org.eclipse.jdt.annotation.NonNull String subtitleCodec,
            @org.eclipse.jdt.annotation.NonNull String transcodeReasons,
            @org.eclipse.jdt.annotation.NonNull Integer audioStreamIndex,
            @org.eclipse.jdt.annotation.NonNull Integer videoStreamIndex,
            @org.eclipse.jdt.annotation.NonNull EncodingContext context,
            @org.eclipse.jdt.annotation.NonNull Map<String, String> streamOptions,
            @org.eclipse.jdt.annotation.NonNull Boolean enableAudioVbrEncoding) throws ApiException {
        return headVideoStreamWithHttpInfo(itemId, container, _static, params, tag, deviceProfileId, playSessionId,
                segmentContainer, segmentLength, minSegments, mediaSourceId, deviceId, audioCodec, enableAutoStreamCopy,
                allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth,
                audioBitRate, audioChannels, maxAudioChannels, profile, level, framerate, maxFramerate, copyTimestamps,
                startTimeTicks, width, height, maxWidth, maxHeight, videoBitRate, subtitleStreamIndex, subtitleMethod,
                maxRefFrames, maxVideoBitDepth, requireAvc, deInterlace, requireNonAnamorphic,
                transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode, videoCodec,
                subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex, context, streamOptions,
                enableAudioVbrEncoding).getData();
    }

    /**
     * Gets a video stream.
     * 
     * @param itemId The item id. (required)
     * @param container The video container. Possible values are: ts, webm, asf, wmv, ogv, mp4, m4v, mkv, mpeg, mpg,
     *            avi, 3gp, wmv, wtv, m2ts, mov, iso, flv. (optional)
     * @param _static Optional. If true, the original file will be streamed statically without any encoding. Use either
     *            no url extension or the original file extension. true/false. (optional)
     * @param params The streaming parameters. (optional)
     * @param tag The tag. (optional)
     * @param deviceProfileId Optional. The dlna device profile id to utilize. (optional)
     * @param playSessionId The play session id. (optional)
     * @param segmentContainer The segment container. (optional)
     * @param segmentLength The segment length. (optional)
     * @param minSegments The minimum number of segments. (optional)
     * @param mediaSourceId The media version id, if playing an alternate version. (optional)
     * @param deviceId The device id of the client requesting. Used to stop encoding processes when needed. (optional)
     * @param audioCodec Optional. Specify an audio codec to encode to, e.g. mp3. If omitted the server will auto-select
     *            using the url&#39;s extension. (optional)
     * @param enableAutoStreamCopy Whether or not to allow automatic stream copy if requested values match the original
     *            source. Defaults to true. (optional)
     * @param allowVideoStreamCopy Whether or not to allow copying of the video stream url. (optional)
     * @param allowAudioStreamCopy Whether or not to allow copying of the audio stream url. (optional)
     * @param breakOnNonKeyFrames Optional. Whether to break on non key frames. (optional)
     * @param audioSampleRate Optional. Specify a specific audio sample rate, e.g. 44100. (optional)
     * @param maxAudioBitDepth Optional. The maximum audio bit depth. (optional)
     * @param audioBitRate Optional. Specify an audio bitrate to encode to, e.g. 128000. If omitted this will be left to
     *            encoder defaults. (optional)
     * @param audioChannels Optional. Specify a specific number of audio channels to encode to, e.g. 2. (optional)
     * @param maxAudioChannels Optional. Specify a maximum number of audio channels to encode to, e.g. 2. (optional)
     * @param profile Optional. Specify a specific an encoder profile (varies by encoder), e.g. main, baseline, high.
     *            (optional)
     * @param level Optional. Specify a level for the encoder profile (varies by encoder), e.g. 3, 3.1. (optional)
     * @param framerate Optional. A specific video framerate to encode to, e.g. 23.976. Generally this should be omitted
     *            unless the device has specific requirements. (optional)
     * @param maxFramerate Optional. A specific maximum video framerate to encode to, e.g. 23.976. Generally this should
     *            be omitted unless the device has specific requirements. (optional)
     * @param copyTimestamps Whether or not to copy timestamps when transcoding with an offset. Defaults to false.
     *            (optional)
     * @param startTimeTicks Optional. Specify a starting offset, in ticks. 1 tick &#x3D; 10000 ms. (optional)
     * @param width Optional. The fixed horizontal resolution of the encoded video. (optional)
     * @param height Optional. The fixed vertical resolution of the encoded video. (optional)
     * @param maxWidth Optional. The maximum horizontal resolution of the encoded video. (optional)
     * @param maxHeight Optional. The maximum vertical resolution of the encoded video. (optional)
     * @param videoBitRate Optional. Specify a video bitrate to encode to, e.g. 500000. If omitted this will be left to
     *            encoder defaults. (optional)
     * @param subtitleStreamIndex Optional. The index of the subtitle stream to use. If omitted no subtitles will be
     *            used. (optional)
     * @param subtitleMethod Optional. Specify the subtitle delivery method. (optional)
     * @param maxRefFrames Optional. (optional)
     * @param maxVideoBitDepth Optional. The maximum video bit depth. (optional)
     * @param requireAvc Optional. Whether to require avc. (optional)
     * @param deInterlace Optional. Whether to deinterlace the video. (optional)
     * @param requireNonAnamorphic Optional. Whether to require a non anamorphic stream. (optional)
     * @param transcodingMaxAudioChannels Optional. The maximum number of audio channels to transcode. (optional)
     * @param cpuCoreLimit Optional. The limit of how many cpu cores to use. (optional)
     * @param liveStreamId The live stream id. (optional)
     * @param enableMpegtsM2TsMode Optional. Whether to enable the MpegtsM2Ts mode. (optional)
     * @param videoCodec Optional. Specify a video codec to encode to, e.g. h264. If omitted the server will auto-select
     *            using the url&#39;s extension. (optional)
     * @param subtitleCodec Optional. Specify a subtitle codec to encode to. (optional)
     * @param transcodeReasons Optional. The transcoding reason. (optional)
     * @param audioStreamIndex Optional. The index of the audio stream to use. If omitted the first audio stream will be
     *            used. (optional)
     * @param videoStreamIndex Optional. The index of the video stream to use. If omitted the first video stream will be
     *            used. (optional)
     * @param context Optional. The MediaBrowser.Model.Dlna.EncodingContext. (optional)
     * @param streamOptions Optional. The streaming options. (optional)
     * @param enableAudioVbrEncoding Optional. Whether to enable Audio Encoding. (optional, default to true)
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
     *                        <td>Video stream returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<File> headVideoStreamWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull String container, @org.eclipse.jdt.annotation.NonNull Boolean _static,
            @org.eclipse.jdt.annotation.NonNull String params, @org.eclipse.jdt.annotation.NonNull String tag,
            @org.eclipse.jdt.annotation.NonNull String deviceProfileId,
            @org.eclipse.jdt.annotation.NonNull String playSessionId,
            @org.eclipse.jdt.annotation.NonNull String segmentContainer,
            @org.eclipse.jdt.annotation.NonNull Integer segmentLength,
            @org.eclipse.jdt.annotation.NonNull Integer minSegments,
            @org.eclipse.jdt.annotation.NonNull String mediaSourceId,
            @org.eclipse.jdt.annotation.NonNull String deviceId, @org.eclipse.jdt.annotation.NonNull String audioCodec,
            @org.eclipse.jdt.annotation.NonNull Boolean enableAutoStreamCopy,
            @org.eclipse.jdt.annotation.NonNull Boolean allowVideoStreamCopy,
            @org.eclipse.jdt.annotation.NonNull Boolean allowAudioStreamCopy,
            @org.eclipse.jdt.annotation.NonNull Boolean breakOnNonKeyFrames,
            @org.eclipse.jdt.annotation.NonNull Integer audioSampleRate,
            @org.eclipse.jdt.annotation.NonNull Integer maxAudioBitDepth,
            @org.eclipse.jdt.annotation.NonNull Integer audioBitRate,
            @org.eclipse.jdt.annotation.NonNull Integer audioChannels,
            @org.eclipse.jdt.annotation.NonNull Integer maxAudioChannels,
            @org.eclipse.jdt.annotation.NonNull String profile, @org.eclipse.jdt.annotation.NonNull String level,
            @org.eclipse.jdt.annotation.NonNull Float framerate, @org.eclipse.jdt.annotation.NonNull Float maxFramerate,
            @org.eclipse.jdt.annotation.NonNull Boolean copyTimestamps,
            @org.eclipse.jdt.annotation.NonNull Long startTimeTicks, @org.eclipse.jdt.annotation.NonNull Integer width,
            @org.eclipse.jdt.annotation.NonNull Integer height, @org.eclipse.jdt.annotation.NonNull Integer maxWidth,
            @org.eclipse.jdt.annotation.NonNull Integer maxHeight,
            @org.eclipse.jdt.annotation.NonNull Integer videoBitRate,
            @org.eclipse.jdt.annotation.NonNull Integer subtitleStreamIndex,
            @org.eclipse.jdt.annotation.NonNull SubtitleDeliveryMethod subtitleMethod,
            @org.eclipse.jdt.annotation.NonNull Integer maxRefFrames,
            @org.eclipse.jdt.annotation.NonNull Integer maxVideoBitDepth,
            @org.eclipse.jdt.annotation.NonNull Boolean requireAvc,
            @org.eclipse.jdt.annotation.NonNull Boolean deInterlace,
            @org.eclipse.jdt.annotation.NonNull Boolean requireNonAnamorphic,
            @org.eclipse.jdt.annotation.NonNull Integer transcodingMaxAudioChannels,
            @org.eclipse.jdt.annotation.NonNull Integer cpuCoreLimit,
            @org.eclipse.jdt.annotation.NonNull String liveStreamId,
            @org.eclipse.jdt.annotation.NonNull Boolean enableMpegtsM2TsMode,
            @org.eclipse.jdt.annotation.NonNull String videoCodec,
            @org.eclipse.jdt.annotation.NonNull String subtitleCodec,
            @org.eclipse.jdt.annotation.NonNull String transcodeReasons,
            @org.eclipse.jdt.annotation.NonNull Integer audioStreamIndex,
            @org.eclipse.jdt.annotation.NonNull Integer videoStreamIndex,
            @org.eclipse.jdt.annotation.NonNull EncodingContext context,
            @org.eclipse.jdt.annotation.NonNull Map<String, String> streamOptions,
            @org.eclipse.jdt.annotation.NonNull Boolean enableAudioVbrEncoding) throws ApiException {
        // Check required parameters
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling headVideoStream");
        }

        // Path parameters
        String localVarPath = "/Videos/{itemId}/stream".replaceAll("\\{itemId}",
                apiClient.escapeString(itemId.toString()));

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "container", container));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "static", _static));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "params", params));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "tag", tag));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "deviceProfileId", deviceProfileId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "playSessionId", playSessionId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "segmentContainer", segmentContainer));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "segmentLength", segmentLength));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "minSegments", minSegments));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "mediaSourceId", mediaSourceId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "deviceId", deviceId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "audioCodec", audioCodec));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "enableAutoStreamCopy", enableAutoStreamCopy));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "allowVideoStreamCopy", allowVideoStreamCopy));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "allowAudioStreamCopy", allowAudioStreamCopy));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "breakOnNonKeyFrames", breakOnNonKeyFrames));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "audioSampleRate", audioSampleRate));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxAudioBitDepth", maxAudioBitDepth));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "audioBitRate", audioBitRate));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "audioChannels", audioChannels));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxAudioChannels", maxAudioChannels));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "profile", profile));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "level", level));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "framerate", framerate));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxFramerate", maxFramerate));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "copyTimestamps", copyTimestamps));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "startTimeTicks", startTimeTicks));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "width", width));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "height", height));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxWidth", maxWidth));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxHeight", maxHeight));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "videoBitRate", videoBitRate));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "subtitleStreamIndex", subtitleStreamIndex));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "subtitleMethod", subtitleMethod));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxRefFrames", maxRefFrames));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxVideoBitDepth", maxVideoBitDepth));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "requireAvc", requireAvc));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "deInterlace", deInterlace));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "requireNonAnamorphic", requireNonAnamorphic));
        localVarQueryParams
                .addAll(apiClient.parameterToPairs("", "transcodingMaxAudioChannels", transcodingMaxAudioChannels));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "cpuCoreLimit", cpuCoreLimit));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "liveStreamId", liveStreamId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "enableMpegtsM2TsMode", enableMpegtsM2TsMode));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "videoCodec", videoCodec));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "subtitleCodec", subtitleCodec));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "transcodeReasons", transcodeReasons));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "audioStreamIndex", audioStreamIndex));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "videoStreamIndex", videoStreamIndex));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "context", context));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "streamOptions", streamOptions));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "enableAudioVbrEncoding", enableAudioVbrEncoding));

        String localVarAccept = apiClient.selectHeaderAccept("video/*");
        String localVarContentType = apiClient.selectHeaderContentType();
        GenericType<File> localVarReturnType = new GenericType<File>() {
        };
        return apiClient.invokeAPI("VideosApi.headVideoStream", localVarPath, "HEAD", localVarQueryParams, null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, null, localVarReturnType, false);
    }

    /**
     * Gets a video stream.
     * 
     * @param itemId The item id. (required)
     * @param container The video container. Possible values are: ts, webm, asf, wmv, ogv, mp4, m4v, mkv, mpeg, mpg,
     *            avi, 3gp, wmv, wtv, m2ts, mov, iso, flv. (required)
     * @param _static Optional. If true, the original file will be streamed statically without any encoding. Use either
     *            no url extension or the original file extension. true/false. (optional)
     * @param params The streaming parameters. (optional)
     * @param tag The tag. (optional)
     * @param deviceProfileId Optional. The dlna device profile id to utilize. (optional)
     * @param playSessionId The play session id. (optional)
     * @param segmentContainer The segment container. (optional)
     * @param segmentLength The segment length. (optional)
     * @param minSegments The minimum number of segments. (optional)
     * @param mediaSourceId The media version id, if playing an alternate version. (optional)
     * @param deviceId The device id of the client requesting. Used to stop encoding processes when needed. (optional)
     * @param audioCodec Optional. Specify an audio codec to encode to, e.g. mp3. If omitted the server will auto-select
     *            using the url&#39;s extension. (optional)
     * @param enableAutoStreamCopy Whether or not to allow automatic stream copy if requested values match the original
     *            source. Defaults to true. (optional)
     * @param allowVideoStreamCopy Whether or not to allow copying of the video stream url. (optional)
     * @param allowAudioStreamCopy Whether or not to allow copying of the audio stream url. (optional)
     * @param breakOnNonKeyFrames Optional. Whether to break on non key frames. (optional)
     * @param audioSampleRate Optional. Specify a specific audio sample rate, e.g. 44100. (optional)
     * @param maxAudioBitDepth Optional. The maximum audio bit depth. (optional)
     * @param audioBitRate Optional. Specify an audio bitrate to encode to, e.g. 128000. If omitted this will be left to
     *            encoder defaults. (optional)
     * @param audioChannels Optional. Specify a specific number of audio channels to encode to, e.g. 2. (optional)
     * @param maxAudioChannels Optional. Specify a maximum number of audio channels to encode to, e.g. 2. (optional)
     * @param profile Optional. Specify a specific an encoder profile (varies by encoder), e.g. main, baseline, high.
     *            (optional)
     * @param level Optional. Specify a level for the encoder profile (varies by encoder), e.g. 3, 3.1. (optional)
     * @param framerate Optional. A specific video framerate to encode to, e.g. 23.976. Generally this should be omitted
     *            unless the device has specific requirements. (optional)
     * @param maxFramerate Optional. A specific maximum video framerate to encode to, e.g. 23.976. Generally this should
     *            be omitted unless the device has specific requirements. (optional)
     * @param copyTimestamps Whether or not to copy timestamps when transcoding with an offset. Defaults to false.
     *            (optional)
     * @param startTimeTicks Optional. Specify a starting offset, in ticks. 1 tick &#x3D; 10000 ms. (optional)
     * @param width Optional. The fixed horizontal resolution of the encoded video. (optional)
     * @param height Optional. The fixed vertical resolution of the encoded video. (optional)
     * @param maxWidth Optional. The maximum horizontal resolution of the encoded video. (optional)
     * @param maxHeight Optional. The maximum vertical resolution of the encoded video. (optional)
     * @param videoBitRate Optional. Specify a video bitrate to encode to, e.g. 500000. If omitted this will be left to
     *            encoder defaults. (optional)
     * @param subtitleStreamIndex Optional. The index of the subtitle stream to use. If omitted no subtitles will be
     *            used. (optional)
     * @param subtitleMethod Optional. Specify the subtitle delivery method. (optional)
     * @param maxRefFrames Optional. (optional)
     * @param maxVideoBitDepth Optional. The maximum video bit depth. (optional)
     * @param requireAvc Optional. Whether to require avc. (optional)
     * @param deInterlace Optional. Whether to deinterlace the video. (optional)
     * @param requireNonAnamorphic Optional. Whether to require a non anamorphic stream. (optional)
     * @param transcodingMaxAudioChannels Optional. The maximum number of audio channels to transcode. (optional)
     * @param cpuCoreLimit Optional. The limit of how many cpu cores to use. (optional)
     * @param liveStreamId The live stream id. (optional)
     * @param enableMpegtsM2TsMode Optional. Whether to enable the MpegtsM2Ts mode. (optional)
     * @param videoCodec Optional. Specify a video codec to encode to, e.g. h264. If omitted the server will auto-select
     *            using the url&#39;s extension. (optional)
     * @param subtitleCodec Optional. Specify a subtitle codec to encode to. (optional)
     * @param transcodeReasons Optional. The transcoding reason. (optional)
     * @param audioStreamIndex Optional. The index of the audio stream to use. If omitted the first audio stream will be
     *            used. (optional)
     * @param videoStreamIndex Optional. The index of the video stream to use. If omitted the first video stream will be
     *            used. (optional)
     * @param context Optional. The MediaBrowser.Model.Dlna.EncodingContext. (optional)
     * @param streamOptions Optional. The streaming options. (optional)
     * @param enableAudioVbrEncoding Optional. Whether to enable Audio Encoding. (optional, default to true)
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
     *                        <td>Video stream returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public File headVideoStreamByContainer(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable String container, @org.eclipse.jdt.annotation.NonNull Boolean _static,
            @org.eclipse.jdt.annotation.NonNull String params, @org.eclipse.jdt.annotation.NonNull String tag,
            @org.eclipse.jdt.annotation.NonNull String deviceProfileId,
            @org.eclipse.jdt.annotation.NonNull String playSessionId,
            @org.eclipse.jdt.annotation.NonNull String segmentContainer,
            @org.eclipse.jdt.annotation.NonNull Integer segmentLength,
            @org.eclipse.jdt.annotation.NonNull Integer minSegments,
            @org.eclipse.jdt.annotation.NonNull String mediaSourceId,
            @org.eclipse.jdt.annotation.NonNull String deviceId, @org.eclipse.jdt.annotation.NonNull String audioCodec,
            @org.eclipse.jdt.annotation.NonNull Boolean enableAutoStreamCopy,
            @org.eclipse.jdt.annotation.NonNull Boolean allowVideoStreamCopy,
            @org.eclipse.jdt.annotation.NonNull Boolean allowAudioStreamCopy,
            @org.eclipse.jdt.annotation.NonNull Boolean breakOnNonKeyFrames,
            @org.eclipse.jdt.annotation.NonNull Integer audioSampleRate,
            @org.eclipse.jdt.annotation.NonNull Integer maxAudioBitDepth,
            @org.eclipse.jdt.annotation.NonNull Integer audioBitRate,
            @org.eclipse.jdt.annotation.NonNull Integer audioChannels,
            @org.eclipse.jdt.annotation.NonNull Integer maxAudioChannels,
            @org.eclipse.jdt.annotation.NonNull String profile, @org.eclipse.jdt.annotation.NonNull String level,
            @org.eclipse.jdt.annotation.NonNull Float framerate, @org.eclipse.jdt.annotation.NonNull Float maxFramerate,
            @org.eclipse.jdt.annotation.NonNull Boolean copyTimestamps,
            @org.eclipse.jdt.annotation.NonNull Long startTimeTicks, @org.eclipse.jdt.annotation.NonNull Integer width,
            @org.eclipse.jdt.annotation.NonNull Integer height, @org.eclipse.jdt.annotation.NonNull Integer maxWidth,
            @org.eclipse.jdt.annotation.NonNull Integer maxHeight,
            @org.eclipse.jdt.annotation.NonNull Integer videoBitRate,
            @org.eclipse.jdt.annotation.NonNull Integer subtitleStreamIndex,
            @org.eclipse.jdt.annotation.NonNull SubtitleDeliveryMethod subtitleMethod,
            @org.eclipse.jdt.annotation.NonNull Integer maxRefFrames,
            @org.eclipse.jdt.annotation.NonNull Integer maxVideoBitDepth,
            @org.eclipse.jdt.annotation.NonNull Boolean requireAvc,
            @org.eclipse.jdt.annotation.NonNull Boolean deInterlace,
            @org.eclipse.jdt.annotation.NonNull Boolean requireNonAnamorphic,
            @org.eclipse.jdt.annotation.NonNull Integer transcodingMaxAudioChannels,
            @org.eclipse.jdt.annotation.NonNull Integer cpuCoreLimit,
            @org.eclipse.jdt.annotation.NonNull String liveStreamId,
            @org.eclipse.jdt.annotation.NonNull Boolean enableMpegtsM2TsMode,
            @org.eclipse.jdt.annotation.NonNull String videoCodec,
            @org.eclipse.jdt.annotation.NonNull String subtitleCodec,
            @org.eclipse.jdt.annotation.NonNull String transcodeReasons,
            @org.eclipse.jdt.annotation.NonNull Integer audioStreamIndex,
            @org.eclipse.jdt.annotation.NonNull Integer videoStreamIndex,
            @org.eclipse.jdt.annotation.NonNull EncodingContext context,
            @org.eclipse.jdt.annotation.NonNull Map<String, String> streamOptions,
            @org.eclipse.jdt.annotation.NonNull Boolean enableAudioVbrEncoding) throws ApiException {
        return headVideoStreamByContainerWithHttpInfo(itemId, container, _static, params, tag, deviceProfileId,
                playSessionId, segmentContainer, segmentLength, minSegments, mediaSourceId, deviceId, audioCodec,
                enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames, audioSampleRate,
                maxAudioBitDepth, audioBitRate, audioChannels, maxAudioChannels, profile, level, framerate,
                maxFramerate, copyTimestamps, startTimeTicks, width, height, maxWidth, maxHeight, videoBitRate,
                subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth, requireAvc, deInterlace,
                requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode,
                videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex, context, streamOptions,
                enableAudioVbrEncoding).getData();
    }

    /**
     * Gets a video stream.
     * 
     * @param itemId The item id. (required)
     * @param container The video container. Possible values are: ts, webm, asf, wmv, ogv, mp4, m4v, mkv, mpeg, mpg,
     *            avi, 3gp, wmv, wtv, m2ts, mov, iso, flv. (required)
     * @param _static Optional. If true, the original file will be streamed statically without any encoding. Use either
     *            no url extension or the original file extension. true/false. (optional)
     * @param params The streaming parameters. (optional)
     * @param tag The tag. (optional)
     * @param deviceProfileId Optional. The dlna device profile id to utilize. (optional)
     * @param playSessionId The play session id. (optional)
     * @param segmentContainer The segment container. (optional)
     * @param segmentLength The segment length. (optional)
     * @param minSegments The minimum number of segments. (optional)
     * @param mediaSourceId The media version id, if playing an alternate version. (optional)
     * @param deviceId The device id of the client requesting. Used to stop encoding processes when needed. (optional)
     * @param audioCodec Optional. Specify an audio codec to encode to, e.g. mp3. If omitted the server will auto-select
     *            using the url&#39;s extension. (optional)
     * @param enableAutoStreamCopy Whether or not to allow automatic stream copy if requested values match the original
     *            source. Defaults to true. (optional)
     * @param allowVideoStreamCopy Whether or not to allow copying of the video stream url. (optional)
     * @param allowAudioStreamCopy Whether or not to allow copying of the audio stream url. (optional)
     * @param breakOnNonKeyFrames Optional. Whether to break on non key frames. (optional)
     * @param audioSampleRate Optional. Specify a specific audio sample rate, e.g. 44100. (optional)
     * @param maxAudioBitDepth Optional. The maximum audio bit depth. (optional)
     * @param audioBitRate Optional. Specify an audio bitrate to encode to, e.g. 128000. If omitted this will be left to
     *            encoder defaults. (optional)
     * @param audioChannels Optional. Specify a specific number of audio channels to encode to, e.g. 2. (optional)
     * @param maxAudioChannels Optional. Specify a maximum number of audio channels to encode to, e.g. 2. (optional)
     * @param profile Optional. Specify a specific an encoder profile (varies by encoder), e.g. main, baseline, high.
     *            (optional)
     * @param level Optional. Specify a level for the encoder profile (varies by encoder), e.g. 3, 3.1. (optional)
     * @param framerate Optional. A specific video framerate to encode to, e.g. 23.976. Generally this should be omitted
     *            unless the device has specific requirements. (optional)
     * @param maxFramerate Optional. A specific maximum video framerate to encode to, e.g. 23.976. Generally this should
     *            be omitted unless the device has specific requirements. (optional)
     * @param copyTimestamps Whether or not to copy timestamps when transcoding with an offset. Defaults to false.
     *            (optional)
     * @param startTimeTicks Optional. Specify a starting offset, in ticks. 1 tick &#x3D; 10000 ms. (optional)
     * @param width Optional. The fixed horizontal resolution of the encoded video. (optional)
     * @param height Optional. The fixed vertical resolution of the encoded video. (optional)
     * @param maxWidth Optional. The maximum horizontal resolution of the encoded video. (optional)
     * @param maxHeight Optional. The maximum vertical resolution of the encoded video. (optional)
     * @param videoBitRate Optional. Specify a video bitrate to encode to, e.g. 500000. If omitted this will be left to
     *            encoder defaults. (optional)
     * @param subtitleStreamIndex Optional. The index of the subtitle stream to use. If omitted no subtitles will be
     *            used. (optional)
     * @param subtitleMethod Optional. Specify the subtitle delivery method. (optional)
     * @param maxRefFrames Optional. (optional)
     * @param maxVideoBitDepth Optional. The maximum video bit depth. (optional)
     * @param requireAvc Optional. Whether to require avc. (optional)
     * @param deInterlace Optional. Whether to deinterlace the video. (optional)
     * @param requireNonAnamorphic Optional. Whether to require a non anamorphic stream. (optional)
     * @param transcodingMaxAudioChannels Optional. The maximum number of audio channels to transcode. (optional)
     * @param cpuCoreLimit Optional. The limit of how many cpu cores to use. (optional)
     * @param liveStreamId The live stream id. (optional)
     * @param enableMpegtsM2TsMode Optional. Whether to enable the MpegtsM2Ts mode. (optional)
     * @param videoCodec Optional. Specify a video codec to encode to, e.g. h264. If omitted the server will auto-select
     *            using the url&#39;s extension. (optional)
     * @param subtitleCodec Optional. Specify a subtitle codec to encode to. (optional)
     * @param transcodeReasons Optional. The transcoding reason. (optional)
     * @param audioStreamIndex Optional. The index of the audio stream to use. If omitted the first audio stream will be
     *            used. (optional)
     * @param videoStreamIndex Optional. The index of the video stream to use. If omitted the first video stream will be
     *            used. (optional)
     * @param context Optional. The MediaBrowser.Model.Dlna.EncodingContext. (optional)
     * @param streamOptions Optional. The streaming options. (optional)
     * @param enableAudioVbrEncoding Optional. Whether to enable Audio Encoding. (optional, default to true)
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
     *                        <td>Video stream returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<File> headVideoStreamByContainerWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable String container, @org.eclipse.jdt.annotation.NonNull Boolean _static,
            @org.eclipse.jdt.annotation.NonNull String params, @org.eclipse.jdt.annotation.NonNull String tag,
            @org.eclipse.jdt.annotation.NonNull String deviceProfileId,
            @org.eclipse.jdt.annotation.NonNull String playSessionId,
            @org.eclipse.jdt.annotation.NonNull String segmentContainer,
            @org.eclipse.jdt.annotation.NonNull Integer segmentLength,
            @org.eclipse.jdt.annotation.NonNull Integer minSegments,
            @org.eclipse.jdt.annotation.NonNull String mediaSourceId,
            @org.eclipse.jdt.annotation.NonNull String deviceId, @org.eclipse.jdt.annotation.NonNull String audioCodec,
            @org.eclipse.jdt.annotation.NonNull Boolean enableAutoStreamCopy,
            @org.eclipse.jdt.annotation.NonNull Boolean allowVideoStreamCopy,
            @org.eclipse.jdt.annotation.NonNull Boolean allowAudioStreamCopy,
            @org.eclipse.jdt.annotation.NonNull Boolean breakOnNonKeyFrames,
            @org.eclipse.jdt.annotation.NonNull Integer audioSampleRate,
            @org.eclipse.jdt.annotation.NonNull Integer maxAudioBitDepth,
            @org.eclipse.jdt.annotation.NonNull Integer audioBitRate,
            @org.eclipse.jdt.annotation.NonNull Integer audioChannels,
            @org.eclipse.jdt.annotation.NonNull Integer maxAudioChannels,
            @org.eclipse.jdt.annotation.NonNull String profile, @org.eclipse.jdt.annotation.NonNull String level,
            @org.eclipse.jdt.annotation.NonNull Float framerate, @org.eclipse.jdt.annotation.NonNull Float maxFramerate,
            @org.eclipse.jdt.annotation.NonNull Boolean copyTimestamps,
            @org.eclipse.jdt.annotation.NonNull Long startTimeTicks, @org.eclipse.jdt.annotation.NonNull Integer width,
            @org.eclipse.jdt.annotation.NonNull Integer height, @org.eclipse.jdt.annotation.NonNull Integer maxWidth,
            @org.eclipse.jdt.annotation.NonNull Integer maxHeight,
            @org.eclipse.jdt.annotation.NonNull Integer videoBitRate,
            @org.eclipse.jdt.annotation.NonNull Integer subtitleStreamIndex,
            @org.eclipse.jdt.annotation.NonNull SubtitleDeliveryMethod subtitleMethod,
            @org.eclipse.jdt.annotation.NonNull Integer maxRefFrames,
            @org.eclipse.jdt.annotation.NonNull Integer maxVideoBitDepth,
            @org.eclipse.jdt.annotation.NonNull Boolean requireAvc,
            @org.eclipse.jdt.annotation.NonNull Boolean deInterlace,
            @org.eclipse.jdt.annotation.NonNull Boolean requireNonAnamorphic,
            @org.eclipse.jdt.annotation.NonNull Integer transcodingMaxAudioChannels,
            @org.eclipse.jdt.annotation.NonNull Integer cpuCoreLimit,
            @org.eclipse.jdt.annotation.NonNull String liveStreamId,
            @org.eclipse.jdt.annotation.NonNull Boolean enableMpegtsM2TsMode,
            @org.eclipse.jdt.annotation.NonNull String videoCodec,
            @org.eclipse.jdt.annotation.NonNull String subtitleCodec,
            @org.eclipse.jdt.annotation.NonNull String transcodeReasons,
            @org.eclipse.jdt.annotation.NonNull Integer audioStreamIndex,
            @org.eclipse.jdt.annotation.NonNull Integer videoStreamIndex,
            @org.eclipse.jdt.annotation.NonNull EncodingContext context,
            @org.eclipse.jdt.annotation.NonNull Map<String, String> streamOptions,
            @org.eclipse.jdt.annotation.NonNull Boolean enableAudioVbrEncoding) throws ApiException {
        // Check required parameters
        if (itemId == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'itemId' when calling headVideoStreamByContainer");
        }
        if (container == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'container' when calling headVideoStreamByContainer");
        }

        // Path parameters
        String localVarPath = "/Videos/{itemId}/stream.{container}"
                .replaceAll("\\{itemId}", apiClient.escapeString(itemId.toString()))
                .replaceAll("\\{container}", apiClient.escapeString(container.toString()));

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "static", _static));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "params", params));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "tag", tag));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "deviceProfileId", deviceProfileId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "playSessionId", playSessionId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "segmentContainer", segmentContainer));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "segmentLength", segmentLength));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "minSegments", minSegments));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "mediaSourceId", mediaSourceId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "deviceId", deviceId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "audioCodec", audioCodec));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "enableAutoStreamCopy", enableAutoStreamCopy));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "allowVideoStreamCopy", allowVideoStreamCopy));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "allowAudioStreamCopy", allowAudioStreamCopy));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "breakOnNonKeyFrames", breakOnNonKeyFrames));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "audioSampleRate", audioSampleRate));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxAudioBitDepth", maxAudioBitDepth));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "audioBitRate", audioBitRate));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "audioChannels", audioChannels));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxAudioChannels", maxAudioChannels));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "profile", profile));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "level", level));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "framerate", framerate));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxFramerate", maxFramerate));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "copyTimestamps", copyTimestamps));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "startTimeTicks", startTimeTicks));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "width", width));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "height", height));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxWidth", maxWidth));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxHeight", maxHeight));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "videoBitRate", videoBitRate));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "subtitleStreamIndex", subtitleStreamIndex));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "subtitleMethod", subtitleMethod));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxRefFrames", maxRefFrames));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxVideoBitDepth", maxVideoBitDepth));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "requireAvc", requireAvc));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "deInterlace", deInterlace));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "requireNonAnamorphic", requireNonAnamorphic));
        localVarQueryParams
                .addAll(apiClient.parameterToPairs("", "transcodingMaxAudioChannels", transcodingMaxAudioChannels));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "cpuCoreLimit", cpuCoreLimit));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "liveStreamId", liveStreamId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "enableMpegtsM2TsMode", enableMpegtsM2TsMode));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "videoCodec", videoCodec));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "subtitleCodec", subtitleCodec));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "transcodeReasons", transcodeReasons));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "audioStreamIndex", audioStreamIndex));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "videoStreamIndex", videoStreamIndex));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "context", context));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "streamOptions", streamOptions));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "enableAudioVbrEncoding", enableAudioVbrEncoding));

        String localVarAccept = apiClient.selectHeaderAccept("video/*");
        String localVarContentType = apiClient.selectHeaderContentType();
        GenericType<File> localVarReturnType = new GenericType<File>() {
        };
        return apiClient.invokeAPI("VideosApi.headVideoStreamByContainer", localVarPath, "HEAD", localVarQueryParams,
                null, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, null, localVarReturnType, false);
    }

    /**
     * Merges videos into a single record.
     * 
     * @param ids Item id list. This allows multiple, comma delimited. (required)
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
     *                        <td>204</td>
     *                        <td>Videos merged.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>400</td>
     *                        <td>Supply at least 2 video ids.</td>
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
    public void mergeVersions(@org.eclipse.jdt.annotation.Nullable List<UUID> ids) throws ApiException {
        mergeVersionsWithHttpInfo(ids);
    }

    /**
     * Merges videos into a single record.
     * 
     * @param ids Item id list. This allows multiple, comma delimited. (required)
     * @return ApiResponse&lt;Void&gt;
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
     *                        <td>204</td>
     *                        <td>Videos merged.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>400</td>
     *                        <td>Supply at least 2 video ids.</td>
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
    public ApiResponse<Void> mergeVersionsWithHttpInfo(@org.eclipse.jdt.annotation.Nullable List<UUID> ids)
            throws ApiException {
        // Check required parameters
        if (ids == null) {
            throw new ApiException(400, "Missing the required parameter 'ids' when calling mergeVersions");
        }

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("multi", "ids", ids));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        return apiClient.invokeAPI("VideosApi.mergeVersions", "/Videos/MergeVersions", "POST", localVarQueryParams,
                null, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, null, false);
    }
}
