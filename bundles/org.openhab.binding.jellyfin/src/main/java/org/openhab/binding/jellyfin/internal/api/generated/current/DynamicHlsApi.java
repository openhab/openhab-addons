/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.jellyfin.internal.api.generated.current;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.function.Consumer;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;
import org.openhab.binding.jellyfin.internal.api.generated.ApiException;
import org.openhab.binding.jellyfin.internal.api.generated.ApiResponse;
import org.openhab.binding.jellyfin.internal.api.generated.Configuration;
import org.openhab.binding.jellyfin.internal.api.generated.Pair;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.EncodingContext;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.SubtitleDeliveryMethod;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class DynamicHlsApi {
    private final HttpClient memberVarHttpClient;
    private final ObjectMapper memberVarObjectMapper;
    private final String memberVarBaseUri;
    private final Consumer<HttpRequest.Builder> memberVarInterceptor;
    private final Duration memberVarReadTimeout;
    private final Consumer<HttpResponse<InputStream>> memberVarResponseInterceptor;
    private final Consumer<HttpResponse<String>> memberVarAsyncResponseInterceptor;

    public DynamicHlsApi() {
        this(Configuration.getDefaultApiClient());
    }

    public DynamicHlsApi(ApiClient apiClient) {
        memberVarHttpClient = apiClient.getHttpClient();
        memberVarObjectMapper = apiClient.getObjectMapper();
        memberVarBaseUri = apiClient.getBaseUri();
        memberVarInterceptor = apiClient.getRequestInterceptor();
        memberVarReadTimeout = apiClient.getReadTimeout();
        memberVarResponseInterceptor = apiClient.getResponseInterceptor();
        memberVarAsyncResponseInterceptor = apiClient.getAsyncResponseInterceptor();
    }

    protected ApiException getApiException(String operationId, HttpResponse<InputStream> response) throws IOException {
        String body = response.body() == null ? null : new String(response.body().readAllBytes());
        String message = formatExceptionMessage(operationId, response.statusCode(), body);
        return new ApiException(response.statusCode(), message, response.headers(), body);
    }

    private String formatExceptionMessage(String operationId, int statusCode, String body) {
        if (body == null || body.isEmpty()) {
            body = "[no body]";
        }
        return operationId + " call failed with: " + statusCode + " - " + body;
    }

    /**
     * Gets a video stream using HTTP live streaming.
     * 
     * @param itemId The item id. (required)
     * @param playlistId The playlist id. (required)
     * @param segmentId The segment id. (required)
     * @param container The video container. Possible values are: ts, webm, asf, wmv, ogv, mp4, m4v, mkv, mpeg, mpg,
     *            avi, 3gp, wmv, wtv, m2ts, mov, iso, flv. (required)
     * @param runtimeTicks The position of the requested segment in ticks. (required)
     * @param actualSegmentLengthTicks The length of the requested segment in ticks. (required)
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
     * @param audioCodec Optional. Specify an audio codec to encode to, e.g. mp3. (optional)
     * @param enableAutoStreamCopy Whether or not to allow automatic stream copy if requested values match the original
     *            source. Defaults to true. (optional)
     * @param allowVideoStreamCopy Whether or not to allow copying of the video stream url. (optional)
     * @param allowAudioStreamCopy Whether or not to allow copying of the audio stream url. (optional)
     * @param breakOnNonKeyFrames Optional. Whether to break on non key frames. (optional)
     * @param audioSampleRate Optional. Specify a specific audio sample rate, e.g. 44100. (optional)
     * @param maxAudioBitDepth Optional. The maximum audio bit depth. (optional)
     * @param maxStreamingBitrate Optional. The maximum streaming bitrate. (optional)
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
     * @param videoCodec Optional. Specify a video codec to encode to, e.g. h264. (optional)
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
     */
    public File getHlsAudioSegment(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable String playlistId,
            @org.eclipse.jdt.annotation.Nullable Integer segmentId,
            @org.eclipse.jdt.annotation.Nullable String container,
            @org.eclipse.jdt.annotation.Nullable Long runtimeTicks,
            @org.eclipse.jdt.annotation.Nullable Long actualSegmentLengthTicks,
            @org.eclipse.jdt.annotation.NonNull Boolean _static, @org.eclipse.jdt.annotation.NonNull String params,
            @org.eclipse.jdt.annotation.NonNull String tag, @org.eclipse.jdt.annotation.NonNull String deviceProfileId,
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
            @org.eclipse.jdt.annotation.NonNull Integer maxStreamingBitrate,
            @org.eclipse.jdt.annotation.NonNull Integer audioBitRate,
            @org.eclipse.jdt.annotation.NonNull Integer audioChannels,
            @org.eclipse.jdt.annotation.NonNull Integer maxAudioChannels,
            @org.eclipse.jdt.annotation.NonNull String profile, @org.eclipse.jdt.annotation.NonNull String level,
            @org.eclipse.jdt.annotation.NonNull Float framerate, @org.eclipse.jdt.annotation.NonNull Float maxFramerate,
            @org.eclipse.jdt.annotation.NonNull Boolean copyTimestamps,
            @org.eclipse.jdt.annotation.NonNull Long startTimeTicks, @org.eclipse.jdt.annotation.NonNull Integer width,
            @org.eclipse.jdt.annotation.NonNull Integer height,
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
        ApiResponse<File> localVarResponse = getHlsAudioSegmentWithHttpInfo(itemId, playlistId, segmentId, container,
                runtimeTicks, actualSegmentLengthTicks, _static, params, tag, deviceProfileId, playSessionId,
                segmentContainer, segmentLength, minSegments, mediaSourceId, deviceId, audioCodec, enableAutoStreamCopy,
                allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth,
                maxStreamingBitrate, audioBitRate, audioChannels, maxAudioChannels, profile, level, framerate,
                maxFramerate, copyTimestamps, startTimeTicks, width, height, videoBitRate, subtitleStreamIndex,
                subtitleMethod, maxRefFrames, maxVideoBitDepth, requireAvc, deInterlace, requireNonAnamorphic,
                transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode, videoCodec,
                subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex, context, streamOptions,
                enableAudioVbrEncoding);
        return localVarResponse.getData();
    }

    /**
     * Gets a video stream using HTTP live streaming.
     * 
     * @param itemId The item id. (required)
     * @param playlistId The playlist id. (required)
     * @param segmentId The segment id. (required)
     * @param container The video container. Possible values are: ts, webm, asf, wmv, ogv, mp4, m4v, mkv, mpeg, mpg,
     *            avi, 3gp, wmv, wtv, m2ts, mov, iso, flv. (required)
     * @param runtimeTicks The position of the requested segment in ticks. (required)
     * @param actualSegmentLengthTicks The length of the requested segment in ticks. (required)
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
     * @param audioCodec Optional. Specify an audio codec to encode to, e.g. mp3. (optional)
     * @param enableAutoStreamCopy Whether or not to allow automatic stream copy if requested values match the original
     *            source. Defaults to true. (optional)
     * @param allowVideoStreamCopy Whether or not to allow copying of the video stream url. (optional)
     * @param allowAudioStreamCopy Whether or not to allow copying of the audio stream url. (optional)
     * @param breakOnNonKeyFrames Optional. Whether to break on non key frames. (optional)
     * @param audioSampleRate Optional. Specify a specific audio sample rate, e.g. 44100. (optional)
     * @param maxAudioBitDepth Optional. The maximum audio bit depth. (optional)
     * @param maxStreamingBitrate Optional. The maximum streaming bitrate. (optional)
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
     * @param videoCodec Optional. Specify a video codec to encode to, e.g. h264. (optional)
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
     */
    public ApiResponse<File> getHlsAudioSegmentWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable String playlistId,
            @org.eclipse.jdt.annotation.Nullable Integer segmentId,
            @org.eclipse.jdt.annotation.Nullable String container,
            @org.eclipse.jdt.annotation.Nullable Long runtimeTicks,
            @org.eclipse.jdt.annotation.Nullable Long actualSegmentLengthTicks,
            @org.eclipse.jdt.annotation.NonNull Boolean _static, @org.eclipse.jdt.annotation.NonNull String params,
            @org.eclipse.jdt.annotation.NonNull String tag, @org.eclipse.jdt.annotation.NonNull String deviceProfileId,
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
            @org.eclipse.jdt.annotation.NonNull Integer maxStreamingBitrate,
            @org.eclipse.jdt.annotation.NonNull Integer audioBitRate,
            @org.eclipse.jdt.annotation.NonNull Integer audioChannels,
            @org.eclipse.jdt.annotation.NonNull Integer maxAudioChannels,
            @org.eclipse.jdt.annotation.NonNull String profile, @org.eclipse.jdt.annotation.NonNull String level,
            @org.eclipse.jdt.annotation.NonNull Float framerate, @org.eclipse.jdt.annotation.NonNull Float maxFramerate,
            @org.eclipse.jdt.annotation.NonNull Boolean copyTimestamps,
            @org.eclipse.jdt.annotation.NonNull Long startTimeTicks, @org.eclipse.jdt.annotation.NonNull Integer width,
            @org.eclipse.jdt.annotation.NonNull Integer height,
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
        HttpRequest.Builder localVarRequestBuilder = getHlsAudioSegmentRequestBuilder(itemId, playlistId, segmentId,
                container, runtimeTicks, actualSegmentLengthTicks, _static, params, tag, deviceProfileId, playSessionId,
                segmentContainer, segmentLength, minSegments, mediaSourceId, deviceId, audioCodec, enableAutoStreamCopy,
                allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth,
                maxStreamingBitrate, audioBitRate, audioChannels, maxAudioChannels, profile, level, framerate,
                maxFramerate, copyTimestamps, startTimeTicks, width, height, videoBitRate, subtitleStreamIndex,
                subtitleMethod, maxRefFrames, maxVideoBitDepth, requireAvc, deInterlace, requireNonAnamorphic,
                transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode, videoCodec,
                subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex, context, streamOptions,
                enableAudioVbrEncoding);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getHlsAudioSegment", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody, new TypeReference<File>() {
                                }));
            } finally {
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder getHlsAudioSegmentRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable String playlistId,
            @org.eclipse.jdt.annotation.Nullable Integer segmentId,
            @org.eclipse.jdt.annotation.Nullable String container,
            @org.eclipse.jdt.annotation.Nullable Long runtimeTicks,
            @org.eclipse.jdt.annotation.Nullable Long actualSegmentLengthTicks,
            @org.eclipse.jdt.annotation.NonNull Boolean _static, @org.eclipse.jdt.annotation.NonNull String params,
            @org.eclipse.jdt.annotation.NonNull String tag, @org.eclipse.jdt.annotation.NonNull String deviceProfileId,
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
            @org.eclipse.jdt.annotation.NonNull Integer maxStreamingBitrate,
            @org.eclipse.jdt.annotation.NonNull Integer audioBitRate,
            @org.eclipse.jdt.annotation.NonNull Integer audioChannels,
            @org.eclipse.jdt.annotation.NonNull Integer maxAudioChannels,
            @org.eclipse.jdt.annotation.NonNull String profile, @org.eclipse.jdt.annotation.NonNull String level,
            @org.eclipse.jdt.annotation.NonNull Float framerate, @org.eclipse.jdt.annotation.NonNull Float maxFramerate,
            @org.eclipse.jdt.annotation.NonNull Boolean copyTimestamps,
            @org.eclipse.jdt.annotation.NonNull Long startTimeTicks, @org.eclipse.jdt.annotation.NonNull Integer width,
            @org.eclipse.jdt.annotation.NonNull Integer height,
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
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling getHlsAudioSegment");
        }
        // verify the required parameter 'playlistId' is set
        if (playlistId == null) {
            throw new ApiException(400, "Missing the required parameter 'playlistId' when calling getHlsAudioSegment");
        }
        // verify the required parameter 'segmentId' is set
        if (segmentId == null) {
            throw new ApiException(400, "Missing the required parameter 'segmentId' when calling getHlsAudioSegment");
        }
        // verify the required parameter 'container' is set
        if (container == null) {
            throw new ApiException(400, "Missing the required parameter 'container' when calling getHlsAudioSegment");
        }
        // verify the required parameter 'runtimeTicks' is set
        if (runtimeTicks == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'runtimeTicks' when calling getHlsAudioSegment");
        }
        // verify the required parameter 'actualSegmentLengthTicks' is set
        if (actualSegmentLengthTicks == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'actualSegmentLengthTicks' when calling getHlsAudioSegment");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Audio/{itemId}/hls1/{playlistId}/{segmentId}.{container}"
                .replace("{itemId}", ApiClient.urlEncode(itemId.toString()))
                .replace("{playlistId}", ApiClient.urlEncode(playlistId.toString()))
                .replace("{segmentId}", ApiClient.urlEncode(segmentId.toString()))
                .replace("{container}", ApiClient.urlEncode(container.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "runtimeTicks";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("runtimeTicks", runtimeTicks));
        localVarQueryParameterBaseName = "actualSegmentLengthTicks";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("actualSegmentLengthTicks", actualSegmentLengthTicks));
        localVarQueryParameterBaseName = "static";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("static", _static));
        localVarQueryParameterBaseName = "params";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("params", params));
        localVarQueryParameterBaseName = "tag";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("tag", tag));
        localVarQueryParameterBaseName = "deviceProfileId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("deviceProfileId", deviceProfileId));
        localVarQueryParameterBaseName = "playSessionId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("playSessionId", playSessionId));
        localVarQueryParameterBaseName = "segmentContainer";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("segmentContainer", segmentContainer));
        localVarQueryParameterBaseName = "segmentLength";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("segmentLength", segmentLength));
        localVarQueryParameterBaseName = "minSegments";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("minSegments", minSegments));
        localVarQueryParameterBaseName = "mediaSourceId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("mediaSourceId", mediaSourceId));
        localVarQueryParameterBaseName = "deviceId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("deviceId", deviceId));
        localVarQueryParameterBaseName = "audioCodec";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("audioCodec", audioCodec));
        localVarQueryParameterBaseName = "enableAutoStreamCopy";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("enableAutoStreamCopy", enableAutoStreamCopy));
        localVarQueryParameterBaseName = "allowVideoStreamCopy";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("allowVideoStreamCopy", allowVideoStreamCopy));
        localVarQueryParameterBaseName = "allowAudioStreamCopy";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("allowAudioStreamCopy", allowAudioStreamCopy));
        localVarQueryParameterBaseName = "breakOnNonKeyFrames";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("breakOnNonKeyFrames", breakOnNonKeyFrames));
        localVarQueryParameterBaseName = "audioSampleRate";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("audioSampleRate", audioSampleRate));
        localVarQueryParameterBaseName = "maxAudioBitDepth";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxAudioBitDepth", maxAudioBitDepth));
        localVarQueryParameterBaseName = "maxStreamingBitrate";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxStreamingBitrate", maxStreamingBitrate));
        localVarQueryParameterBaseName = "audioBitRate";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("audioBitRate", audioBitRate));
        localVarQueryParameterBaseName = "audioChannels";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("audioChannels", audioChannels));
        localVarQueryParameterBaseName = "maxAudioChannels";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxAudioChannels", maxAudioChannels));
        localVarQueryParameterBaseName = "profile";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("profile", profile));
        localVarQueryParameterBaseName = "level";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("level", level));
        localVarQueryParameterBaseName = "framerate";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("framerate", framerate));
        localVarQueryParameterBaseName = "maxFramerate";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxFramerate", maxFramerate));
        localVarQueryParameterBaseName = "copyTimestamps";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("copyTimestamps", copyTimestamps));
        localVarQueryParameterBaseName = "startTimeTicks";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("startTimeTicks", startTimeTicks));
        localVarQueryParameterBaseName = "width";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("width", width));
        localVarQueryParameterBaseName = "height";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("height", height));
        localVarQueryParameterBaseName = "videoBitRate";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("videoBitRate", videoBitRate));
        localVarQueryParameterBaseName = "subtitleStreamIndex";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("subtitleStreamIndex", subtitleStreamIndex));
        localVarQueryParameterBaseName = "subtitleMethod";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("subtitleMethod", subtitleMethod));
        localVarQueryParameterBaseName = "maxRefFrames";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxRefFrames", maxRefFrames));
        localVarQueryParameterBaseName = "maxVideoBitDepth";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxVideoBitDepth", maxVideoBitDepth));
        localVarQueryParameterBaseName = "requireAvc";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("requireAvc", requireAvc));
        localVarQueryParameterBaseName = "deInterlace";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("deInterlace", deInterlace));
        localVarQueryParameterBaseName = "requireNonAnamorphic";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("requireNonAnamorphic", requireNonAnamorphic));
        localVarQueryParameterBaseName = "transcodingMaxAudioChannels";
        localVarQueryParams
                .addAll(ApiClient.parameterToPairs("transcodingMaxAudioChannels", transcodingMaxAudioChannels));
        localVarQueryParameterBaseName = "cpuCoreLimit";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("cpuCoreLimit", cpuCoreLimit));
        localVarQueryParameterBaseName = "liveStreamId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("liveStreamId", liveStreamId));
        localVarQueryParameterBaseName = "enableMpegtsM2TsMode";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("enableMpegtsM2TsMode", enableMpegtsM2TsMode));
        localVarQueryParameterBaseName = "videoCodec";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("videoCodec", videoCodec));
        localVarQueryParameterBaseName = "subtitleCodec";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("subtitleCodec", subtitleCodec));
        localVarQueryParameterBaseName = "transcodeReasons";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("transcodeReasons", transcodeReasons));
        localVarQueryParameterBaseName = "audioStreamIndex";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("audioStreamIndex", audioStreamIndex));
        localVarQueryParameterBaseName = "videoStreamIndex";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("videoStreamIndex", videoStreamIndex));
        localVarQueryParameterBaseName = "context";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("context", context));
        localVarQueryParameterBaseName = "streamOptions";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("streamOptions", streamOptions));
        localVarQueryParameterBaseName = "enableAudioVbrEncoding";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("enableAudioVbrEncoding", enableAudioVbrEncoding));

        if (!localVarQueryParams.isEmpty() || localVarQueryStringJoiner.length() != 0) {
            StringJoiner queryJoiner = new StringJoiner("&");
            localVarQueryParams.forEach(p -> queryJoiner.add(p.getName() + '=' + p.getValue()));
            if (localVarQueryStringJoiner.length() != 0) {
                queryJoiner.add(localVarQueryStringJoiner.toString());
            }
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath + '?' + queryJoiner.toString()));
        } else {
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));
        }

        localVarRequestBuilder.header("Accept", "audio/*");

        localVarRequestBuilder.method("GET", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    /**
     * Gets a video stream using HTTP live streaming.
     * 
     * @param itemId The item id. (required)
     * @param playlistId The playlist id. (required)
     * @param segmentId The segment id. (required)
     * @param container The video container. Possible values are: ts, webm, asf, wmv, ogv, mp4, m4v, mkv, mpeg, mpg,
     *            avi, 3gp, wmv, wtv, m2ts, mov, iso, flv. (required)
     * @param runtimeTicks The position of the requested segment in ticks. (required)
     * @param actualSegmentLengthTicks The length of the requested segment in ticks. (required)
     * @param _static Optional. If true, the original file will be streamed statically without any encoding. Use either
     *            no url extension or the original file extension. true/false. (optional)
     * @param params The streaming parameters. (optional)
     * @param tag The tag. (optional)
     * @param deviceProfileId Optional. The dlna device profile id to utilize. (optional)
     * @param playSessionId The play session id. (optional)
     * @param segmentContainer The segment container. (optional)
     * @param segmentLength The desired segment length. (optional)
     * @param minSegments The minimum number of segments. (optional)
     * @param mediaSourceId The media version id, if playing an alternate version. (optional)
     * @param deviceId The device id of the client requesting. Used to stop encoding processes when needed. (optional)
     * @param audioCodec Optional. Specify an audio codec to encode to, e.g. mp3. (optional)
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
     * @param videoCodec Optional. Specify a video codec to encode to, e.g. h264. (optional)
     * @param subtitleCodec Optional. Specify a subtitle codec to encode to. (optional)
     * @param transcodeReasons Optional. The transcoding reason. (optional)
     * @param audioStreamIndex Optional. The index of the audio stream to use. If omitted the first audio stream will be
     *            used. (optional)
     * @param videoStreamIndex Optional. The index of the video stream to use. If omitted the first video stream will be
     *            used. (optional)
     * @param context Optional. The MediaBrowser.Model.Dlna.EncodingContext. (optional)
     * @param streamOptions Optional. The streaming options. (optional)
     * @param enableAudioVbrEncoding Optional. Whether to enable Audio Encoding. (optional, default to true)
     * @param alwaysBurnInSubtitleWhenTranscoding Whether to always burn in subtitles when transcoding. (optional,
     *            default to false)
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getHlsVideoSegment(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable String playlistId,
            @org.eclipse.jdt.annotation.Nullable Integer segmentId,
            @org.eclipse.jdt.annotation.Nullable String container,
            @org.eclipse.jdt.annotation.Nullable Long runtimeTicks,
            @org.eclipse.jdt.annotation.Nullable Long actualSegmentLengthTicks,
            @org.eclipse.jdt.annotation.NonNull Boolean _static, @org.eclipse.jdt.annotation.NonNull String params,
            @org.eclipse.jdt.annotation.NonNull String tag, @org.eclipse.jdt.annotation.NonNull String deviceProfileId,
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
            @org.eclipse.jdt.annotation.NonNull Boolean enableAudioVbrEncoding,
            @org.eclipse.jdt.annotation.NonNull Boolean alwaysBurnInSubtitleWhenTranscoding) throws ApiException {
        ApiResponse<File> localVarResponse = getHlsVideoSegmentWithHttpInfo(itemId, playlistId, segmentId, container,
                runtimeTicks, actualSegmentLengthTicks, _static, params, tag, deviceProfileId, playSessionId,
                segmentContainer, segmentLength, minSegments, mediaSourceId, deviceId, audioCodec, enableAutoStreamCopy,
                allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth,
                audioBitRate, audioChannels, maxAudioChannels, profile, level, framerate, maxFramerate, copyTimestamps,
                startTimeTicks, width, height, maxWidth, maxHeight, videoBitRate, subtitleStreamIndex, subtitleMethod,
                maxRefFrames, maxVideoBitDepth, requireAvc, deInterlace, requireNonAnamorphic,
                transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode, videoCodec,
                subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex, context, streamOptions,
                enableAudioVbrEncoding, alwaysBurnInSubtitleWhenTranscoding);
        return localVarResponse.getData();
    }

    /**
     * Gets a video stream using HTTP live streaming.
     * 
     * @param itemId The item id. (required)
     * @param playlistId The playlist id. (required)
     * @param segmentId The segment id. (required)
     * @param container The video container. Possible values are: ts, webm, asf, wmv, ogv, mp4, m4v, mkv, mpeg, mpg,
     *            avi, 3gp, wmv, wtv, m2ts, mov, iso, flv. (required)
     * @param runtimeTicks The position of the requested segment in ticks. (required)
     * @param actualSegmentLengthTicks The length of the requested segment in ticks. (required)
     * @param _static Optional. If true, the original file will be streamed statically without any encoding. Use either
     *            no url extension or the original file extension. true/false. (optional)
     * @param params The streaming parameters. (optional)
     * @param tag The tag. (optional)
     * @param deviceProfileId Optional. The dlna device profile id to utilize. (optional)
     * @param playSessionId The play session id. (optional)
     * @param segmentContainer The segment container. (optional)
     * @param segmentLength The desired segment length. (optional)
     * @param minSegments The minimum number of segments. (optional)
     * @param mediaSourceId The media version id, if playing an alternate version. (optional)
     * @param deviceId The device id of the client requesting. Used to stop encoding processes when needed. (optional)
     * @param audioCodec Optional. Specify an audio codec to encode to, e.g. mp3. (optional)
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
     * @param videoCodec Optional. Specify a video codec to encode to, e.g. h264. (optional)
     * @param subtitleCodec Optional. Specify a subtitle codec to encode to. (optional)
     * @param transcodeReasons Optional. The transcoding reason. (optional)
     * @param audioStreamIndex Optional. The index of the audio stream to use. If omitted the first audio stream will be
     *            used. (optional)
     * @param videoStreamIndex Optional. The index of the video stream to use. If omitted the first video stream will be
     *            used. (optional)
     * @param context Optional. The MediaBrowser.Model.Dlna.EncodingContext. (optional)
     * @param streamOptions Optional. The streaming options. (optional)
     * @param enableAudioVbrEncoding Optional. Whether to enable Audio Encoding. (optional, default to true)
     * @param alwaysBurnInSubtitleWhenTranscoding Whether to always burn in subtitles when transcoding. (optional,
     *            default to false)
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getHlsVideoSegmentWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable String playlistId,
            @org.eclipse.jdt.annotation.Nullable Integer segmentId,
            @org.eclipse.jdt.annotation.Nullable String container,
            @org.eclipse.jdt.annotation.Nullable Long runtimeTicks,
            @org.eclipse.jdt.annotation.Nullable Long actualSegmentLengthTicks,
            @org.eclipse.jdt.annotation.NonNull Boolean _static, @org.eclipse.jdt.annotation.NonNull String params,
            @org.eclipse.jdt.annotation.NonNull String tag, @org.eclipse.jdt.annotation.NonNull String deviceProfileId,
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
            @org.eclipse.jdt.annotation.NonNull Boolean enableAudioVbrEncoding,
            @org.eclipse.jdt.annotation.NonNull Boolean alwaysBurnInSubtitleWhenTranscoding) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getHlsVideoSegmentRequestBuilder(itemId, playlistId, segmentId,
                container, runtimeTicks, actualSegmentLengthTicks, _static, params, tag, deviceProfileId, playSessionId,
                segmentContainer, segmentLength, minSegments, mediaSourceId, deviceId, audioCodec, enableAutoStreamCopy,
                allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth,
                audioBitRate, audioChannels, maxAudioChannels, profile, level, framerate, maxFramerate, copyTimestamps,
                startTimeTicks, width, height, maxWidth, maxHeight, videoBitRate, subtitleStreamIndex, subtitleMethod,
                maxRefFrames, maxVideoBitDepth, requireAvc, deInterlace, requireNonAnamorphic,
                transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode, videoCodec,
                subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex, context, streamOptions,
                enableAudioVbrEncoding, alwaysBurnInSubtitleWhenTranscoding);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getHlsVideoSegment", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody, new TypeReference<File>() {
                                }));
            } finally {
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder getHlsVideoSegmentRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable String playlistId,
            @org.eclipse.jdt.annotation.Nullable Integer segmentId,
            @org.eclipse.jdt.annotation.Nullable String container,
            @org.eclipse.jdt.annotation.Nullable Long runtimeTicks,
            @org.eclipse.jdt.annotation.Nullable Long actualSegmentLengthTicks,
            @org.eclipse.jdt.annotation.NonNull Boolean _static, @org.eclipse.jdt.annotation.NonNull String params,
            @org.eclipse.jdt.annotation.NonNull String tag, @org.eclipse.jdt.annotation.NonNull String deviceProfileId,
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
            @org.eclipse.jdt.annotation.NonNull Boolean enableAudioVbrEncoding,
            @org.eclipse.jdt.annotation.NonNull Boolean alwaysBurnInSubtitleWhenTranscoding) throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling getHlsVideoSegment");
        }
        // verify the required parameter 'playlistId' is set
        if (playlistId == null) {
            throw new ApiException(400, "Missing the required parameter 'playlistId' when calling getHlsVideoSegment");
        }
        // verify the required parameter 'segmentId' is set
        if (segmentId == null) {
            throw new ApiException(400, "Missing the required parameter 'segmentId' when calling getHlsVideoSegment");
        }
        // verify the required parameter 'container' is set
        if (container == null) {
            throw new ApiException(400, "Missing the required parameter 'container' when calling getHlsVideoSegment");
        }
        // verify the required parameter 'runtimeTicks' is set
        if (runtimeTicks == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'runtimeTicks' when calling getHlsVideoSegment");
        }
        // verify the required parameter 'actualSegmentLengthTicks' is set
        if (actualSegmentLengthTicks == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'actualSegmentLengthTicks' when calling getHlsVideoSegment");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Videos/{itemId}/hls1/{playlistId}/{segmentId}.{container}"
                .replace("{itemId}", ApiClient.urlEncode(itemId.toString()))
                .replace("{playlistId}", ApiClient.urlEncode(playlistId.toString()))
                .replace("{segmentId}", ApiClient.urlEncode(segmentId.toString()))
                .replace("{container}", ApiClient.urlEncode(container.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "runtimeTicks";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("runtimeTicks", runtimeTicks));
        localVarQueryParameterBaseName = "actualSegmentLengthTicks";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("actualSegmentLengthTicks", actualSegmentLengthTicks));
        localVarQueryParameterBaseName = "static";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("static", _static));
        localVarQueryParameterBaseName = "params";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("params", params));
        localVarQueryParameterBaseName = "tag";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("tag", tag));
        localVarQueryParameterBaseName = "deviceProfileId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("deviceProfileId", deviceProfileId));
        localVarQueryParameterBaseName = "playSessionId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("playSessionId", playSessionId));
        localVarQueryParameterBaseName = "segmentContainer";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("segmentContainer", segmentContainer));
        localVarQueryParameterBaseName = "segmentLength";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("segmentLength", segmentLength));
        localVarQueryParameterBaseName = "minSegments";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("minSegments", minSegments));
        localVarQueryParameterBaseName = "mediaSourceId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("mediaSourceId", mediaSourceId));
        localVarQueryParameterBaseName = "deviceId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("deviceId", deviceId));
        localVarQueryParameterBaseName = "audioCodec";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("audioCodec", audioCodec));
        localVarQueryParameterBaseName = "enableAutoStreamCopy";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("enableAutoStreamCopy", enableAutoStreamCopy));
        localVarQueryParameterBaseName = "allowVideoStreamCopy";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("allowVideoStreamCopy", allowVideoStreamCopy));
        localVarQueryParameterBaseName = "allowAudioStreamCopy";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("allowAudioStreamCopy", allowAudioStreamCopy));
        localVarQueryParameterBaseName = "breakOnNonKeyFrames";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("breakOnNonKeyFrames", breakOnNonKeyFrames));
        localVarQueryParameterBaseName = "audioSampleRate";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("audioSampleRate", audioSampleRate));
        localVarQueryParameterBaseName = "maxAudioBitDepth";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxAudioBitDepth", maxAudioBitDepth));
        localVarQueryParameterBaseName = "audioBitRate";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("audioBitRate", audioBitRate));
        localVarQueryParameterBaseName = "audioChannels";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("audioChannels", audioChannels));
        localVarQueryParameterBaseName = "maxAudioChannels";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxAudioChannels", maxAudioChannels));
        localVarQueryParameterBaseName = "profile";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("profile", profile));
        localVarQueryParameterBaseName = "level";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("level", level));
        localVarQueryParameterBaseName = "framerate";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("framerate", framerate));
        localVarQueryParameterBaseName = "maxFramerate";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxFramerate", maxFramerate));
        localVarQueryParameterBaseName = "copyTimestamps";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("copyTimestamps", copyTimestamps));
        localVarQueryParameterBaseName = "startTimeTicks";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("startTimeTicks", startTimeTicks));
        localVarQueryParameterBaseName = "width";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("width", width));
        localVarQueryParameterBaseName = "height";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("height", height));
        localVarQueryParameterBaseName = "maxWidth";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxWidth", maxWidth));
        localVarQueryParameterBaseName = "maxHeight";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxHeight", maxHeight));
        localVarQueryParameterBaseName = "videoBitRate";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("videoBitRate", videoBitRate));
        localVarQueryParameterBaseName = "subtitleStreamIndex";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("subtitleStreamIndex", subtitleStreamIndex));
        localVarQueryParameterBaseName = "subtitleMethod";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("subtitleMethod", subtitleMethod));
        localVarQueryParameterBaseName = "maxRefFrames";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxRefFrames", maxRefFrames));
        localVarQueryParameterBaseName = "maxVideoBitDepth";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxVideoBitDepth", maxVideoBitDepth));
        localVarQueryParameterBaseName = "requireAvc";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("requireAvc", requireAvc));
        localVarQueryParameterBaseName = "deInterlace";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("deInterlace", deInterlace));
        localVarQueryParameterBaseName = "requireNonAnamorphic";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("requireNonAnamorphic", requireNonAnamorphic));
        localVarQueryParameterBaseName = "transcodingMaxAudioChannels";
        localVarQueryParams
                .addAll(ApiClient.parameterToPairs("transcodingMaxAudioChannels", transcodingMaxAudioChannels));
        localVarQueryParameterBaseName = "cpuCoreLimit";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("cpuCoreLimit", cpuCoreLimit));
        localVarQueryParameterBaseName = "liveStreamId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("liveStreamId", liveStreamId));
        localVarQueryParameterBaseName = "enableMpegtsM2TsMode";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("enableMpegtsM2TsMode", enableMpegtsM2TsMode));
        localVarQueryParameterBaseName = "videoCodec";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("videoCodec", videoCodec));
        localVarQueryParameterBaseName = "subtitleCodec";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("subtitleCodec", subtitleCodec));
        localVarQueryParameterBaseName = "transcodeReasons";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("transcodeReasons", transcodeReasons));
        localVarQueryParameterBaseName = "audioStreamIndex";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("audioStreamIndex", audioStreamIndex));
        localVarQueryParameterBaseName = "videoStreamIndex";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("videoStreamIndex", videoStreamIndex));
        localVarQueryParameterBaseName = "context";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("context", context));
        localVarQueryParameterBaseName = "streamOptions";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("streamOptions", streamOptions));
        localVarQueryParameterBaseName = "enableAudioVbrEncoding";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("enableAudioVbrEncoding", enableAudioVbrEncoding));
        localVarQueryParameterBaseName = "alwaysBurnInSubtitleWhenTranscoding";
        localVarQueryParams.addAll(
                ApiClient.parameterToPairs("alwaysBurnInSubtitleWhenTranscoding", alwaysBurnInSubtitleWhenTranscoding));

        if (!localVarQueryParams.isEmpty() || localVarQueryStringJoiner.length() != 0) {
            StringJoiner queryJoiner = new StringJoiner("&");
            localVarQueryParams.forEach(p -> queryJoiner.add(p.getName() + '=' + p.getValue()));
            if (localVarQueryStringJoiner.length() != 0) {
                queryJoiner.add(localVarQueryStringJoiner.toString());
            }
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath + '?' + queryJoiner.toString()));
        } else {
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));
        }

        localVarRequestBuilder.header("Accept", "video/*");

        localVarRequestBuilder.method("GET", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    /**
     * Gets a hls live stream.
     * 
     * @param itemId The item id. (required)
     * @param container The audio container. (optional)
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
     * @param audioCodec Optional. Specify an audio codec to encode to, e.g. mp3. (optional)
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
     * @param videoCodec Optional. Specify a video codec to encode to, e.g. h264. (optional)
     * @param subtitleCodec Optional. Specify a subtitle codec to encode to. (optional)
     * @param transcodeReasons Optional. The transcoding reason. (optional)
     * @param audioStreamIndex Optional. The index of the audio stream to use. If omitted the first audio stream will be
     *            used. (optional)
     * @param videoStreamIndex Optional. The index of the video stream to use. If omitted the first video stream will be
     *            used. (optional)
     * @param context Optional. The MediaBrowser.Model.Dlna.EncodingContext. (optional)
     * @param streamOptions Optional. The streaming options. (optional)
     * @param maxWidth Optional. The max width. (optional)
     * @param maxHeight Optional. The max height. (optional)
     * @param enableSubtitlesInManifest Optional. Whether to enable subtitles in the manifest. (optional)
     * @param enableAudioVbrEncoding Optional. Whether to enable Audio Encoding. (optional, default to true)
     * @param alwaysBurnInSubtitleWhenTranscoding Whether to always burn in subtitles when transcoding. (optional,
     *            default to false)
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getLiveHlsStream(@org.eclipse.jdt.annotation.Nullable UUID itemId,
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
            @org.eclipse.jdt.annotation.NonNull Integer height,
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
            @org.eclipse.jdt.annotation.NonNull Integer maxWidth, @org.eclipse.jdt.annotation.NonNull Integer maxHeight,
            @org.eclipse.jdt.annotation.NonNull Boolean enableSubtitlesInManifest,
            @org.eclipse.jdt.annotation.NonNull Boolean enableAudioVbrEncoding,
            @org.eclipse.jdt.annotation.NonNull Boolean alwaysBurnInSubtitleWhenTranscoding) throws ApiException {
        ApiResponse<File> localVarResponse = getLiveHlsStreamWithHttpInfo(itemId, container, _static, params, tag,
                deviceProfileId, playSessionId, segmentContainer, segmentLength, minSegments, mediaSourceId, deviceId,
                audioCodec, enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames,
                audioSampleRate, maxAudioBitDepth, audioBitRate, audioChannels, maxAudioChannels, profile, level,
                framerate, maxFramerate, copyTimestamps, startTimeTicks, width, height, videoBitRate,
                subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth, requireAvc, deInterlace,
                requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode,
                videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex, context, streamOptions,
                maxWidth, maxHeight, enableSubtitlesInManifest, enableAudioVbrEncoding,
                alwaysBurnInSubtitleWhenTranscoding);
        return localVarResponse.getData();
    }

    /**
     * Gets a hls live stream.
     * 
     * @param itemId The item id. (required)
     * @param container The audio container. (optional)
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
     * @param audioCodec Optional. Specify an audio codec to encode to, e.g. mp3. (optional)
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
     * @param videoCodec Optional. Specify a video codec to encode to, e.g. h264. (optional)
     * @param subtitleCodec Optional. Specify a subtitle codec to encode to. (optional)
     * @param transcodeReasons Optional. The transcoding reason. (optional)
     * @param audioStreamIndex Optional. The index of the audio stream to use. If omitted the first audio stream will be
     *            used. (optional)
     * @param videoStreamIndex Optional. The index of the video stream to use. If omitted the first video stream will be
     *            used. (optional)
     * @param context Optional. The MediaBrowser.Model.Dlna.EncodingContext. (optional)
     * @param streamOptions Optional. The streaming options. (optional)
     * @param maxWidth Optional. The max width. (optional)
     * @param maxHeight Optional. The max height. (optional)
     * @param enableSubtitlesInManifest Optional. Whether to enable subtitles in the manifest. (optional)
     * @param enableAudioVbrEncoding Optional. Whether to enable Audio Encoding. (optional, default to true)
     * @param alwaysBurnInSubtitleWhenTranscoding Whether to always burn in subtitles when transcoding. (optional,
     *            default to false)
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getLiveHlsStreamWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
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
            @org.eclipse.jdt.annotation.NonNull Integer height,
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
            @org.eclipse.jdt.annotation.NonNull Integer maxWidth, @org.eclipse.jdt.annotation.NonNull Integer maxHeight,
            @org.eclipse.jdt.annotation.NonNull Boolean enableSubtitlesInManifest,
            @org.eclipse.jdt.annotation.NonNull Boolean enableAudioVbrEncoding,
            @org.eclipse.jdt.annotation.NonNull Boolean alwaysBurnInSubtitleWhenTranscoding) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getLiveHlsStreamRequestBuilder(itemId, container, _static, params,
                tag, deviceProfileId, playSessionId, segmentContainer, segmentLength, minSegments, mediaSourceId,
                deviceId, audioCodec, enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy,
                breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth, audioBitRate, audioChannels, maxAudioChannels,
                profile, level, framerate, maxFramerate, copyTimestamps, startTimeTicks, width, height, videoBitRate,
                subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth, requireAvc, deInterlace,
                requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode,
                videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex, context, streamOptions,
                maxWidth, maxHeight, enableSubtitlesInManifest, enableAudioVbrEncoding,
                alwaysBurnInSubtitleWhenTranscoding);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getLiveHlsStream", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody, new TypeReference<File>() {
                                }));
            } finally {
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder getLiveHlsStreamRequestBuilder(@org.eclipse.jdt.annotation.Nullable UUID itemId,
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
            @org.eclipse.jdt.annotation.NonNull Integer height,
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
            @org.eclipse.jdt.annotation.NonNull Integer maxWidth, @org.eclipse.jdt.annotation.NonNull Integer maxHeight,
            @org.eclipse.jdt.annotation.NonNull Boolean enableSubtitlesInManifest,
            @org.eclipse.jdt.annotation.NonNull Boolean enableAudioVbrEncoding,
            @org.eclipse.jdt.annotation.NonNull Boolean alwaysBurnInSubtitleWhenTranscoding) throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling getLiveHlsStream");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Videos/{itemId}/live.m3u8".replace("{itemId}", ApiClient.urlEncode(itemId.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "container";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("container", container));
        localVarQueryParameterBaseName = "static";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("static", _static));
        localVarQueryParameterBaseName = "params";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("params", params));
        localVarQueryParameterBaseName = "tag";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("tag", tag));
        localVarQueryParameterBaseName = "deviceProfileId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("deviceProfileId", deviceProfileId));
        localVarQueryParameterBaseName = "playSessionId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("playSessionId", playSessionId));
        localVarQueryParameterBaseName = "segmentContainer";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("segmentContainer", segmentContainer));
        localVarQueryParameterBaseName = "segmentLength";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("segmentLength", segmentLength));
        localVarQueryParameterBaseName = "minSegments";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("minSegments", minSegments));
        localVarQueryParameterBaseName = "mediaSourceId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("mediaSourceId", mediaSourceId));
        localVarQueryParameterBaseName = "deviceId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("deviceId", deviceId));
        localVarQueryParameterBaseName = "audioCodec";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("audioCodec", audioCodec));
        localVarQueryParameterBaseName = "enableAutoStreamCopy";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("enableAutoStreamCopy", enableAutoStreamCopy));
        localVarQueryParameterBaseName = "allowVideoStreamCopy";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("allowVideoStreamCopy", allowVideoStreamCopy));
        localVarQueryParameterBaseName = "allowAudioStreamCopy";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("allowAudioStreamCopy", allowAudioStreamCopy));
        localVarQueryParameterBaseName = "breakOnNonKeyFrames";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("breakOnNonKeyFrames", breakOnNonKeyFrames));
        localVarQueryParameterBaseName = "audioSampleRate";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("audioSampleRate", audioSampleRate));
        localVarQueryParameterBaseName = "maxAudioBitDepth";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxAudioBitDepth", maxAudioBitDepth));
        localVarQueryParameterBaseName = "audioBitRate";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("audioBitRate", audioBitRate));
        localVarQueryParameterBaseName = "audioChannels";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("audioChannels", audioChannels));
        localVarQueryParameterBaseName = "maxAudioChannels";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxAudioChannels", maxAudioChannels));
        localVarQueryParameterBaseName = "profile";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("profile", profile));
        localVarQueryParameterBaseName = "level";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("level", level));
        localVarQueryParameterBaseName = "framerate";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("framerate", framerate));
        localVarQueryParameterBaseName = "maxFramerate";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxFramerate", maxFramerate));
        localVarQueryParameterBaseName = "copyTimestamps";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("copyTimestamps", copyTimestamps));
        localVarQueryParameterBaseName = "startTimeTicks";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("startTimeTicks", startTimeTicks));
        localVarQueryParameterBaseName = "width";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("width", width));
        localVarQueryParameterBaseName = "height";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("height", height));
        localVarQueryParameterBaseName = "videoBitRate";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("videoBitRate", videoBitRate));
        localVarQueryParameterBaseName = "subtitleStreamIndex";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("subtitleStreamIndex", subtitleStreamIndex));
        localVarQueryParameterBaseName = "subtitleMethod";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("subtitleMethod", subtitleMethod));
        localVarQueryParameterBaseName = "maxRefFrames";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxRefFrames", maxRefFrames));
        localVarQueryParameterBaseName = "maxVideoBitDepth";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxVideoBitDepth", maxVideoBitDepth));
        localVarQueryParameterBaseName = "requireAvc";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("requireAvc", requireAvc));
        localVarQueryParameterBaseName = "deInterlace";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("deInterlace", deInterlace));
        localVarQueryParameterBaseName = "requireNonAnamorphic";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("requireNonAnamorphic", requireNonAnamorphic));
        localVarQueryParameterBaseName = "transcodingMaxAudioChannels";
        localVarQueryParams
                .addAll(ApiClient.parameterToPairs("transcodingMaxAudioChannels", transcodingMaxAudioChannels));
        localVarQueryParameterBaseName = "cpuCoreLimit";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("cpuCoreLimit", cpuCoreLimit));
        localVarQueryParameterBaseName = "liveStreamId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("liveStreamId", liveStreamId));
        localVarQueryParameterBaseName = "enableMpegtsM2TsMode";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("enableMpegtsM2TsMode", enableMpegtsM2TsMode));
        localVarQueryParameterBaseName = "videoCodec";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("videoCodec", videoCodec));
        localVarQueryParameterBaseName = "subtitleCodec";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("subtitleCodec", subtitleCodec));
        localVarQueryParameterBaseName = "transcodeReasons";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("transcodeReasons", transcodeReasons));
        localVarQueryParameterBaseName = "audioStreamIndex";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("audioStreamIndex", audioStreamIndex));
        localVarQueryParameterBaseName = "videoStreamIndex";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("videoStreamIndex", videoStreamIndex));
        localVarQueryParameterBaseName = "context";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("context", context));
        localVarQueryParameterBaseName = "streamOptions";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("streamOptions", streamOptions));
        localVarQueryParameterBaseName = "maxWidth";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxWidth", maxWidth));
        localVarQueryParameterBaseName = "maxHeight";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxHeight", maxHeight));
        localVarQueryParameterBaseName = "enableSubtitlesInManifest";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("enableSubtitlesInManifest", enableSubtitlesInManifest));
        localVarQueryParameterBaseName = "enableAudioVbrEncoding";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("enableAudioVbrEncoding", enableAudioVbrEncoding));
        localVarQueryParameterBaseName = "alwaysBurnInSubtitleWhenTranscoding";
        localVarQueryParams.addAll(
                ApiClient.parameterToPairs("alwaysBurnInSubtitleWhenTranscoding", alwaysBurnInSubtitleWhenTranscoding));

        if (!localVarQueryParams.isEmpty() || localVarQueryStringJoiner.length() != 0) {
            StringJoiner queryJoiner = new StringJoiner("&");
            localVarQueryParams.forEach(p -> queryJoiner.add(p.getName() + '=' + p.getValue()));
            if (localVarQueryStringJoiner.length() != 0) {
                queryJoiner.add(localVarQueryStringJoiner.toString());
            }
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath + '?' + queryJoiner.toString()));
        } else {
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));
        }

        localVarRequestBuilder.header("Accept", "application/x-mpegURL");

        localVarRequestBuilder.method("GET", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    /**
     * Gets an audio hls playlist stream.
     * 
     * @param itemId The item id. (required)
     * @param mediaSourceId The media version id, if playing an alternate version. (required)
     * @param _static Optional. If true, the original file will be streamed statically without any encoding. Use either
     *            no url extension or the original file extension. true/false. (optional)
     * @param params The streaming parameters. (optional)
     * @param tag The tag. (optional)
     * @param deviceProfileId Optional. The dlna device profile id to utilize. (optional)
     * @param playSessionId The play session id. (optional)
     * @param segmentContainer The segment container. (optional)
     * @param segmentLength The segment length. (optional)
     * @param minSegments The minimum number of segments. (optional)
     * @param deviceId The device id of the client requesting. Used to stop encoding processes when needed. (optional)
     * @param audioCodec Optional. Specify an audio codec to encode to, e.g. mp3. (optional)
     * @param enableAutoStreamCopy Whether or not to allow automatic stream copy if requested values match the original
     *            source. Defaults to true. (optional)
     * @param allowVideoStreamCopy Whether or not to allow copying of the video stream url. (optional)
     * @param allowAudioStreamCopy Whether or not to allow copying of the audio stream url. (optional)
     * @param breakOnNonKeyFrames Optional. Whether to break on non key frames. (optional)
     * @param audioSampleRate Optional. Specify a specific audio sample rate, e.g. 44100. (optional)
     * @param maxAudioBitDepth Optional. The maximum audio bit depth. (optional)
     * @param maxStreamingBitrate Optional. The maximum streaming bitrate. (optional)
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
     * @param videoCodec Optional. Specify a video codec to encode to, e.g. h264. (optional)
     * @param subtitleCodec Optional. Specify a subtitle codec to encode to. (optional)
     * @param transcodeReasons Optional. The transcoding reason. (optional)
     * @param audioStreamIndex Optional. The index of the audio stream to use. If omitted the first audio stream will be
     *            used. (optional)
     * @param videoStreamIndex Optional. The index of the video stream to use. If omitted the first video stream will be
     *            used. (optional)
     * @param context Optional. The MediaBrowser.Model.Dlna.EncodingContext. (optional)
     * @param streamOptions Optional. The streaming options. (optional)
     * @param enableAdaptiveBitrateStreaming Enable adaptive bitrate streaming. (optional, default to true)
     * @param enableAudioVbrEncoding Optional. Whether to enable Audio Encoding. (optional, default to true)
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getMasterHlsAudioPlaylist(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable String mediaSourceId,
            @org.eclipse.jdt.annotation.NonNull Boolean _static, @org.eclipse.jdt.annotation.NonNull String params,
            @org.eclipse.jdt.annotation.NonNull String tag, @org.eclipse.jdt.annotation.NonNull String deviceProfileId,
            @org.eclipse.jdt.annotation.NonNull String playSessionId,
            @org.eclipse.jdt.annotation.NonNull String segmentContainer,
            @org.eclipse.jdt.annotation.NonNull Integer segmentLength,
            @org.eclipse.jdt.annotation.NonNull Integer minSegments,
            @org.eclipse.jdt.annotation.NonNull String deviceId, @org.eclipse.jdt.annotation.NonNull String audioCodec,
            @org.eclipse.jdt.annotation.NonNull Boolean enableAutoStreamCopy,
            @org.eclipse.jdt.annotation.NonNull Boolean allowVideoStreamCopy,
            @org.eclipse.jdt.annotation.NonNull Boolean allowAudioStreamCopy,
            @org.eclipse.jdt.annotation.NonNull Boolean breakOnNonKeyFrames,
            @org.eclipse.jdt.annotation.NonNull Integer audioSampleRate,
            @org.eclipse.jdt.annotation.NonNull Integer maxAudioBitDepth,
            @org.eclipse.jdt.annotation.NonNull Integer maxStreamingBitrate,
            @org.eclipse.jdt.annotation.NonNull Integer audioBitRate,
            @org.eclipse.jdt.annotation.NonNull Integer audioChannels,
            @org.eclipse.jdt.annotation.NonNull Integer maxAudioChannels,
            @org.eclipse.jdt.annotation.NonNull String profile, @org.eclipse.jdt.annotation.NonNull String level,
            @org.eclipse.jdt.annotation.NonNull Float framerate, @org.eclipse.jdt.annotation.NonNull Float maxFramerate,
            @org.eclipse.jdt.annotation.NonNull Boolean copyTimestamps,
            @org.eclipse.jdt.annotation.NonNull Long startTimeTicks, @org.eclipse.jdt.annotation.NonNull Integer width,
            @org.eclipse.jdt.annotation.NonNull Integer height,
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
            @org.eclipse.jdt.annotation.NonNull Boolean enableAdaptiveBitrateStreaming,
            @org.eclipse.jdt.annotation.NonNull Boolean enableAudioVbrEncoding) throws ApiException {
        ApiResponse<File> localVarResponse = getMasterHlsAudioPlaylistWithHttpInfo(itemId, mediaSourceId, _static,
                params, tag, deviceProfileId, playSessionId, segmentContainer, segmentLength, minSegments, deviceId,
                audioCodec, enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames,
                audioSampleRate, maxAudioBitDepth, maxStreamingBitrate, audioBitRate, audioChannels, maxAudioChannels,
                profile, level, framerate, maxFramerate, copyTimestamps, startTimeTicks, width, height, videoBitRate,
                subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth, requireAvc, deInterlace,
                requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode,
                videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex, context, streamOptions,
                enableAdaptiveBitrateStreaming, enableAudioVbrEncoding);
        return localVarResponse.getData();
    }

    /**
     * Gets an audio hls playlist stream.
     * 
     * @param itemId The item id. (required)
     * @param mediaSourceId The media version id, if playing an alternate version. (required)
     * @param _static Optional. If true, the original file will be streamed statically without any encoding. Use either
     *            no url extension or the original file extension. true/false. (optional)
     * @param params The streaming parameters. (optional)
     * @param tag The tag. (optional)
     * @param deviceProfileId Optional. The dlna device profile id to utilize. (optional)
     * @param playSessionId The play session id. (optional)
     * @param segmentContainer The segment container. (optional)
     * @param segmentLength The segment length. (optional)
     * @param minSegments The minimum number of segments. (optional)
     * @param deviceId The device id of the client requesting. Used to stop encoding processes when needed. (optional)
     * @param audioCodec Optional. Specify an audio codec to encode to, e.g. mp3. (optional)
     * @param enableAutoStreamCopy Whether or not to allow automatic stream copy if requested values match the original
     *            source. Defaults to true. (optional)
     * @param allowVideoStreamCopy Whether or not to allow copying of the video stream url. (optional)
     * @param allowAudioStreamCopy Whether or not to allow copying of the audio stream url. (optional)
     * @param breakOnNonKeyFrames Optional. Whether to break on non key frames. (optional)
     * @param audioSampleRate Optional. Specify a specific audio sample rate, e.g. 44100. (optional)
     * @param maxAudioBitDepth Optional. The maximum audio bit depth. (optional)
     * @param maxStreamingBitrate Optional. The maximum streaming bitrate. (optional)
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
     * @param videoCodec Optional. Specify a video codec to encode to, e.g. h264. (optional)
     * @param subtitleCodec Optional. Specify a subtitle codec to encode to. (optional)
     * @param transcodeReasons Optional. The transcoding reason. (optional)
     * @param audioStreamIndex Optional. The index of the audio stream to use. If omitted the first audio stream will be
     *            used. (optional)
     * @param videoStreamIndex Optional. The index of the video stream to use. If omitted the first video stream will be
     *            used. (optional)
     * @param context Optional. The MediaBrowser.Model.Dlna.EncodingContext. (optional)
     * @param streamOptions Optional. The streaming options. (optional)
     * @param enableAdaptiveBitrateStreaming Enable adaptive bitrate streaming. (optional, default to true)
     * @param enableAudioVbrEncoding Optional. Whether to enable Audio Encoding. (optional, default to true)
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getMasterHlsAudioPlaylistWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable String mediaSourceId,
            @org.eclipse.jdt.annotation.NonNull Boolean _static, @org.eclipse.jdt.annotation.NonNull String params,
            @org.eclipse.jdt.annotation.NonNull String tag, @org.eclipse.jdt.annotation.NonNull String deviceProfileId,
            @org.eclipse.jdt.annotation.NonNull String playSessionId,
            @org.eclipse.jdt.annotation.NonNull String segmentContainer,
            @org.eclipse.jdt.annotation.NonNull Integer segmentLength,
            @org.eclipse.jdt.annotation.NonNull Integer minSegments,
            @org.eclipse.jdt.annotation.NonNull String deviceId, @org.eclipse.jdt.annotation.NonNull String audioCodec,
            @org.eclipse.jdt.annotation.NonNull Boolean enableAutoStreamCopy,
            @org.eclipse.jdt.annotation.NonNull Boolean allowVideoStreamCopy,
            @org.eclipse.jdt.annotation.NonNull Boolean allowAudioStreamCopy,
            @org.eclipse.jdt.annotation.NonNull Boolean breakOnNonKeyFrames,
            @org.eclipse.jdt.annotation.NonNull Integer audioSampleRate,
            @org.eclipse.jdt.annotation.NonNull Integer maxAudioBitDepth,
            @org.eclipse.jdt.annotation.NonNull Integer maxStreamingBitrate,
            @org.eclipse.jdt.annotation.NonNull Integer audioBitRate,
            @org.eclipse.jdt.annotation.NonNull Integer audioChannels,
            @org.eclipse.jdt.annotation.NonNull Integer maxAudioChannels,
            @org.eclipse.jdt.annotation.NonNull String profile, @org.eclipse.jdt.annotation.NonNull String level,
            @org.eclipse.jdt.annotation.NonNull Float framerate, @org.eclipse.jdt.annotation.NonNull Float maxFramerate,
            @org.eclipse.jdt.annotation.NonNull Boolean copyTimestamps,
            @org.eclipse.jdt.annotation.NonNull Long startTimeTicks, @org.eclipse.jdt.annotation.NonNull Integer width,
            @org.eclipse.jdt.annotation.NonNull Integer height,
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
            @org.eclipse.jdt.annotation.NonNull Boolean enableAdaptiveBitrateStreaming,
            @org.eclipse.jdt.annotation.NonNull Boolean enableAudioVbrEncoding) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getMasterHlsAudioPlaylistRequestBuilder(itemId, mediaSourceId,
                _static, params, tag, deviceProfileId, playSessionId, segmentContainer, segmentLength, minSegments,
                deviceId, audioCodec, enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy,
                breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth, maxStreamingBitrate, audioBitRate,
                audioChannels, maxAudioChannels, profile, level, framerate, maxFramerate, copyTimestamps,
                startTimeTicks, width, height, videoBitRate, subtitleStreamIndex, subtitleMethod, maxRefFrames,
                maxVideoBitDepth, requireAvc, deInterlace, requireNonAnamorphic, transcodingMaxAudioChannels,
                cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode, videoCodec, subtitleCodec, transcodeReasons,
                audioStreamIndex, videoStreamIndex, context, streamOptions, enableAdaptiveBitrateStreaming,
                enableAudioVbrEncoding);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getMasterHlsAudioPlaylist", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody, new TypeReference<File>() {
                                }));
            } finally {
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder getMasterHlsAudioPlaylistRequestBuilder(
            @org.eclipse.jdt.annotation.Nullable UUID itemId, @org.eclipse.jdt.annotation.Nullable String mediaSourceId,
            @org.eclipse.jdt.annotation.NonNull Boolean _static, @org.eclipse.jdt.annotation.NonNull String params,
            @org.eclipse.jdt.annotation.NonNull String tag, @org.eclipse.jdt.annotation.NonNull String deviceProfileId,
            @org.eclipse.jdt.annotation.NonNull String playSessionId,
            @org.eclipse.jdt.annotation.NonNull String segmentContainer,
            @org.eclipse.jdt.annotation.NonNull Integer segmentLength,
            @org.eclipse.jdt.annotation.NonNull Integer minSegments,
            @org.eclipse.jdt.annotation.NonNull String deviceId, @org.eclipse.jdt.annotation.NonNull String audioCodec,
            @org.eclipse.jdt.annotation.NonNull Boolean enableAutoStreamCopy,
            @org.eclipse.jdt.annotation.NonNull Boolean allowVideoStreamCopy,
            @org.eclipse.jdt.annotation.NonNull Boolean allowAudioStreamCopy,
            @org.eclipse.jdt.annotation.NonNull Boolean breakOnNonKeyFrames,
            @org.eclipse.jdt.annotation.NonNull Integer audioSampleRate,
            @org.eclipse.jdt.annotation.NonNull Integer maxAudioBitDepth,
            @org.eclipse.jdt.annotation.NonNull Integer maxStreamingBitrate,
            @org.eclipse.jdt.annotation.NonNull Integer audioBitRate,
            @org.eclipse.jdt.annotation.NonNull Integer audioChannels,
            @org.eclipse.jdt.annotation.NonNull Integer maxAudioChannels,
            @org.eclipse.jdt.annotation.NonNull String profile, @org.eclipse.jdt.annotation.NonNull String level,
            @org.eclipse.jdt.annotation.NonNull Float framerate, @org.eclipse.jdt.annotation.NonNull Float maxFramerate,
            @org.eclipse.jdt.annotation.NonNull Boolean copyTimestamps,
            @org.eclipse.jdt.annotation.NonNull Long startTimeTicks, @org.eclipse.jdt.annotation.NonNull Integer width,
            @org.eclipse.jdt.annotation.NonNull Integer height,
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
            @org.eclipse.jdt.annotation.NonNull Boolean enableAdaptiveBitrateStreaming,
            @org.eclipse.jdt.annotation.NonNull Boolean enableAudioVbrEncoding) throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'itemId' when calling getMasterHlsAudioPlaylist");
        }
        // verify the required parameter 'mediaSourceId' is set
        if (mediaSourceId == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'mediaSourceId' when calling getMasterHlsAudioPlaylist");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Audio/{itemId}/master.m3u8".replace("{itemId}", ApiClient.urlEncode(itemId.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "static";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("static", _static));
        localVarQueryParameterBaseName = "params";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("params", params));
        localVarQueryParameterBaseName = "tag";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("tag", tag));
        localVarQueryParameterBaseName = "deviceProfileId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("deviceProfileId", deviceProfileId));
        localVarQueryParameterBaseName = "playSessionId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("playSessionId", playSessionId));
        localVarQueryParameterBaseName = "segmentContainer";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("segmentContainer", segmentContainer));
        localVarQueryParameterBaseName = "segmentLength";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("segmentLength", segmentLength));
        localVarQueryParameterBaseName = "minSegments";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("minSegments", minSegments));
        localVarQueryParameterBaseName = "mediaSourceId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("mediaSourceId", mediaSourceId));
        localVarQueryParameterBaseName = "deviceId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("deviceId", deviceId));
        localVarQueryParameterBaseName = "audioCodec";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("audioCodec", audioCodec));
        localVarQueryParameterBaseName = "enableAutoStreamCopy";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("enableAutoStreamCopy", enableAutoStreamCopy));
        localVarQueryParameterBaseName = "allowVideoStreamCopy";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("allowVideoStreamCopy", allowVideoStreamCopy));
        localVarQueryParameterBaseName = "allowAudioStreamCopy";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("allowAudioStreamCopy", allowAudioStreamCopy));
        localVarQueryParameterBaseName = "breakOnNonKeyFrames";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("breakOnNonKeyFrames", breakOnNonKeyFrames));
        localVarQueryParameterBaseName = "audioSampleRate";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("audioSampleRate", audioSampleRate));
        localVarQueryParameterBaseName = "maxAudioBitDepth";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxAudioBitDepth", maxAudioBitDepth));
        localVarQueryParameterBaseName = "maxStreamingBitrate";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxStreamingBitrate", maxStreamingBitrate));
        localVarQueryParameterBaseName = "audioBitRate";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("audioBitRate", audioBitRate));
        localVarQueryParameterBaseName = "audioChannels";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("audioChannels", audioChannels));
        localVarQueryParameterBaseName = "maxAudioChannels";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxAudioChannels", maxAudioChannels));
        localVarQueryParameterBaseName = "profile";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("profile", profile));
        localVarQueryParameterBaseName = "level";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("level", level));
        localVarQueryParameterBaseName = "framerate";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("framerate", framerate));
        localVarQueryParameterBaseName = "maxFramerate";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxFramerate", maxFramerate));
        localVarQueryParameterBaseName = "copyTimestamps";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("copyTimestamps", copyTimestamps));
        localVarQueryParameterBaseName = "startTimeTicks";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("startTimeTicks", startTimeTicks));
        localVarQueryParameterBaseName = "width";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("width", width));
        localVarQueryParameterBaseName = "height";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("height", height));
        localVarQueryParameterBaseName = "videoBitRate";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("videoBitRate", videoBitRate));
        localVarQueryParameterBaseName = "subtitleStreamIndex";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("subtitleStreamIndex", subtitleStreamIndex));
        localVarQueryParameterBaseName = "subtitleMethod";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("subtitleMethod", subtitleMethod));
        localVarQueryParameterBaseName = "maxRefFrames";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxRefFrames", maxRefFrames));
        localVarQueryParameterBaseName = "maxVideoBitDepth";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxVideoBitDepth", maxVideoBitDepth));
        localVarQueryParameterBaseName = "requireAvc";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("requireAvc", requireAvc));
        localVarQueryParameterBaseName = "deInterlace";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("deInterlace", deInterlace));
        localVarQueryParameterBaseName = "requireNonAnamorphic";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("requireNonAnamorphic", requireNonAnamorphic));
        localVarQueryParameterBaseName = "transcodingMaxAudioChannels";
        localVarQueryParams
                .addAll(ApiClient.parameterToPairs("transcodingMaxAudioChannels", transcodingMaxAudioChannels));
        localVarQueryParameterBaseName = "cpuCoreLimit";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("cpuCoreLimit", cpuCoreLimit));
        localVarQueryParameterBaseName = "liveStreamId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("liveStreamId", liveStreamId));
        localVarQueryParameterBaseName = "enableMpegtsM2TsMode";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("enableMpegtsM2TsMode", enableMpegtsM2TsMode));
        localVarQueryParameterBaseName = "videoCodec";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("videoCodec", videoCodec));
        localVarQueryParameterBaseName = "subtitleCodec";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("subtitleCodec", subtitleCodec));
        localVarQueryParameterBaseName = "transcodeReasons";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("transcodeReasons", transcodeReasons));
        localVarQueryParameterBaseName = "audioStreamIndex";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("audioStreamIndex", audioStreamIndex));
        localVarQueryParameterBaseName = "videoStreamIndex";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("videoStreamIndex", videoStreamIndex));
        localVarQueryParameterBaseName = "context";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("context", context));
        localVarQueryParameterBaseName = "streamOptions";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("streamOptions", streamOptions));
        localVarQueryParameterBaseName = "enableAdaptiveBitrateStreaming";
        localVarQueryParams
                .addAll(ApiClient.parameterToPairs("enableAdaptiveBitrateStreaming", enableAdaptiveBitrateStreaming));
        localVarQueryParameterBaseName = "enableAudioVbrEncoding";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("enableAudioVbrEncoding", enableAudioVbrEncoding));

        if (!localVarQueryParams.isEmpty() || localVarQueryStringJoiner.length() != 0) {
            StringJoiner queryJoiner = new StringJoiner("&");
            localVarQueryParams.forEach(p -> queryJoiner.add(p.getName() + '=' + p.getValue()));
            if (localVarQueryStringJoiner.length() != 0) {
                queryJoiner.add(localVarQueryStringJoiner.toString());
            }
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath + '?' + queryJoiner.toString()));
        } else {
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));
        }

        localVarRequestBuilder.header("Accept", "application/x-mpegURL");

        localVarRequestBuilder.method("GET", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    /**
     * Gets a video hls playlist stream.
     * 
     * @param itemId The item id. (required)
     * @param mediaSourceId The media version id, if playing an alternate version. (required)
     * @param _static Optional. If true, the original file will be streamed statically without any encoding. Use either
     *            no url extension or the original file extension. true/false. (optional)
     * @param params The streaming parameters. (optional)
     * @param tag The tag. (optional)
     * @param deviceProfileId Optional. The dlna device profile id to utilize. (optional)
     * @param playSessionId The play session id. (optional)
     * @param segmentContainer The segment container. (optional)
     * @param segmentLength The segment length. (optional)
     * @param minSegments The minimum number of segments. (optional)
     * @param deviceId The device id of the client requesting. Used to stop encoding processes when needed. (optional)
     * @param audioCodec Optional. Specify an audio codec to encode to, e.g. mp3. (optional)
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
     * @param videoCodec Optional. Specify a video codec to encode to, e.g. h264. (optional)
     * @param subtitleCodec Optional. Specify a subtitle codec to encode to. (optional)
     * @param transcodeReasons Optional. The transcoding reason. (optional)
     * @param audioStreamIndex Optional. The index of the audio stream to use. If omitted the first audio stream will be
     *            used. (optional)
     * @param videoStreamIndex Optional. The index of the video stream to use. If omitted the first video stream will be
     *            used. (optional)
     * @param context Optional. The MediaBrowser.Model.Dlna.EncodingContext. (optional)
     * @param streamOptions Optional. The streaming options. (optional)
     * @param enableAdaptiveBitrateStreaming Enable adaptive bitrate streaming. (optional, default to true)
     * @param enableTrickplay Enable trickplay image playlists being added to master playlist. (optional, default to
     *            true)
     * @param enableAudioVbrEncoding Whether to enable Audio Encoding. (optional, default to true)
     * @param alwaysBurnInSubtitleWhenTranscoding Whether to always burn in subtitles when transcoding. (optional,
     *            default to false)
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getMasterHlsVideoPlaylist(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable String mediaSourceId,
            @org.eclipse.jdt.annotation.NonNull Boolean _static, @org.eclipse.jdt.annotation.NonNull String params,
            @org.eclipse.jdt.annotation.NonNull String tag, @org.eclipse.jdt.annotation.NonNull String deviceProfileId,
            @org.eclipse.jdt.annotation.NonNull String playSessionId,
            @org.eclipse.jdt.annotation.NonNull String segmentContainer,
            @org.eclipse.jdt.annotation.NonNull Integer segmentLength,
            @org.eclipse.jdt.annotation.NonNull Integer minSegments,
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
            @org.eclipse.jdt.annotation.NonNull Boolean enableAdaptiveBitrateStreaming,
            @org.eclipse.jdt.annotation.NonNull Boolean enableTrickplay,
            @org.eclipse.jdt.annotation.NonNull Boolean enableAudioVbrEncoding,
            @org.eclipse.jdt.annotation.NonNull Boolean alwaysBurnInSubtitleWhenTranscoding) throws ApiException {
        ApiResponse<File> localVarResponse = getMasterHlsVideoPlaylistWithHttpInfo(itemId, mediaSourceId, _static,
                params, tag, deviceProfileId, playSessionId, segmentContainer, segmentLength, minSegments, deviceId,
                audioCodec, enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames,
                audioSampleRate, maxAudioBitDepth, audioBitRate, audioChannels, maxAudioChannels, profile, level,
                framerate, maxFramerate, copyTimestamps, startTimeTicks, width, height, maxWidth, maxHeight,
                videoBitRate, subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth, requireAvc,
                deInterlace, requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId,
                enableMpegtsM2TsMode, videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex,
                context, streamOptions, enableAdaptiveBitrateStreaming, enableTrickplay, enableAudioVbrEncoding,
                alwaysBurnInSubtitleWhenTranscoding);
        return localVarResponse.getData();
    }

    /**
     * Gets a video hls playlist stream.
     * 
     * @param itemId The item id. (required)
     * @param mediaSourceId The media version id, if playing an alternate version. (required)
     * @param _static Optional. If true, the original file will be streamed statically without any encoding. Use either
     *            no url extension or the original file extension. true/false. (optional)
     * @param params The streaming parameters. (optional)
     * @param tag The tag. (optional)
     * @param deviceProfileId Optional. The dlna device profile id to utilize. (optional)
     * @param playSessionId The play session id. (optional)
     * @param segmentContainer The segment container. (optional)
     * @param segmentLength The segment length. (optional)
     * @param minSegments The minimum number of segments. (optional)
     * @param deviceId The device id of the client requesting. Used to stop encoding processes when needed. (optional)
     * @param audioCodec Optional. Specify an audio codec to encode to, e.g. mp3. (optional)
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
     * @param videoCodec Optional. Specify a video codec to encode to, e.g. h264. (optional)
     * @param subtitleCodec Optional. Specify a subtitle codec to encode to. (optional)
     * @param transcodeReasons Optional. The transcoding reason. (optional)
     * @param audioStreamIndex Optional. The index of the audio stream to use. If omitted the first audio stream will be
     *            used. (optional)
     * @param videoStreamIndex Optional. The index of the video stream to use. If omitted the first video stream will be
     *            used. (optional)
     * @param context Optional. The MediaBrowser.Model.Dlna.EncodingContext. (optional)
     * @param streamOptions Optional. The streaming options. (optional)
     * @param enableAdaptiveBitrateStreaming Enable adaptive bitrate streaming. (optional, default to true)
     * @param enableTrickplay Enable trickplay image playlists being added to master playlist. (optional, default to
     *            true)
     * @param enableAudioVbrEncoding Whether to enable Audio Encoding. (optional, default to true)
     * @param alwaysBurnInSubtitleWhenTranscoding Whether to always burn in subtitles when transcoding. (optional,
     *            default to false)
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getMasterHlsVideoPlaylistWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable String mediaSourceId,
            @org.eclipse.jdt.annotation.NonNull Boolean _static, @org.eclipse.jdt.annotation.NonNull String params,
            @org.eclipse.jdt.annotation.NonNull String tag, @org.eclipse.jdt.annotation.NonNull String deviceProfileId,
            @org.eclipse.jdt.annotation.NonNull String playSessionId,
            @org.eclipse.jdt.annotation.NonNull String segmentContainer,
            @org.eclipse.jdt.annotation.NonNull Integer segmentLength,
            @org.eclipse.jdt.annotation.NonNull Integer minSegments,
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
            @org.eclipse.jdt.annotation.NonNull Boolean enableAdaptiveBitrateStreaming,
            @org.eclipse.jdt.annotation.NonNull Boolean enableTrickplay,
            @org.eclipse.jdt.annotation.NonNull Boolean enableAudioVbrEncoding,
            @org.eclipse.jdt.annotation.NonNull Boolean alwaysBurnInSubtitleWhenTranscoding) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getMasterHlsVideoPlaylistRequestBuilder(itemId, mediaSourceId,
                _static, params, tag, deviceProfileId, playSessionId, segmentContainer, segmentLength, minSegments,
                deviceId, audioCodec, enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy,
                breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth, audioBitRate, audioChannels, maxAudioChannels,
                profile, level, framerate, maxFramerate, copyTimestamps, startTimeTicks, width, height, maxWidth,
                maxHeight, videoBitRate, subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth,
                requireAvc, deInterlace, requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId,
                enableMpegtsM2TsMode, videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex,
                context, streamOptions, enableAdaptiveBitrateStreaming, enableTrickplay, enableAudioVbrEncoding,
                alwaysBurnInSubtitleWhenTranscoding);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getMasterHlsVideoPlaylist", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody, new TypeReference<File>() {
                                }));
            } finally {
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder getMasterHlsVideoPlaylistRequestBuilder(
            @org.eclipse.jdt.annotation.Nullable UUID itemId, @org.eclipse.jdt.annotation.Nullable String mediaSourceId,
            @org.eclipse.jdt.annotation.NonNull Boolean _static, @org.eclipse.jdt.annotation.NonNull String params,
            @org.eclipse.jdt.annotation.NonNull String tag, @org.eclipse.jdt.annotation.NonNull String deviceProfileId,
            @org.eclipse.jdt.annotation.NonNull String playSessionId,
            @org.eclipse.jdt.annotation.NonNull String segmentContainer,
            @org.eclipse.jdt.annotation.NonNull Integer segmentLength,
            @org.eclipse.jdt.annotation.NonNull Integer minSegments,
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
            @org.eclipse.jdt.annotation.NonNull Boolean enableAdaptiveBitrateStreaming,
            @org.eclipse.jdt.annotation.NonNull Boolean enableTrickplay,
            @org.eclipse.jdt.annotation.NonNull Boolean enableAudioVbrEncoding,
            @org.eclipse.jdt.annotation.NonNull Boolean alwaysBurnInSubtitleWhenTranscoding) throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'itemId' when calling getMasterHlsVideoPlaylist");
        }
        // verify the required parameter 'mediaSourceId' is set
        if (mediaSourceId == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'mediaSourceId' when calling getMasterHlsVideoPlaylist");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Videos/{itemId}/master.m3u8".replace("{itemId}",
                ApiClient.urlEncode(itemId.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "static";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("static", _static));
        localVarQueryParameterBaseName = "params";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("params", params));
        localVarQueryParameterBaseName = "tag";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("tag", tag));
        localVarQueryParameterBaseName = "deviceProfileId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("deviceProfileId", deviceProfileId));
        localVarQueryParameterBaseName = "playSessionId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("playSessionId", playSessionId));
        localVarQueryParameterBaseName = "segmentContainer";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("segmentContainer", segmentContainer));
        localVarQueryParameterBaseName = "segmentLength";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("segmentLength", segmentLength));
        localVarQueryParameterBaseName = "minSegments";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("minSegments", minSegments));
        localVarQueryParameterBaseName = "mediaSourceId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("mediaSourceId", mediaSourceId));
        localVarQueryParameterBaseName = "deviceId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("deviceId", deviceId));
        localVarQueryParameterBaseName = "audioCodec";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("audioCodec", audioCodec));
        localVarQueryParameterBaseName = "enableAutoStreamCopy";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("enableAutoStreamCopy", enableAutoStreamCopy));
        localVarQueryParameterBaseName = "allowVideoStreamCopy";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("allowVideoStreamCopy", allowVideoStreamCopy));
        localVarQueryParameterBaseName = "allowAudioStreamCopy";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("allowAudioStreamCopy", allowAudioStreamCopy));
        localVarQueryParameterBaseName = "breakOnNonKeyFrames";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("breakOnNonKeyFrames", breakOnNonKeyFrames));
        localVarQueryParameterBaseName = "audioSampleRate";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("audioSampleRate", audioSampleRate));
        localVarQueryParameterBaseName = "maxAudioBitDepth";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxAudioBitDepth", maxAudioBitDepth));
        localVarQueryParameterBaseName = "audioBitRate";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("audioBitRate", audioBitRate));
        localVarQueryParameterBaseName = "audioChannels";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("audioChannels", audioChannels));
        localVarQueryParameterBaseName = "maxAudioChannels";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxAudioChannels", maxAudioChannels));
        localVarQueryParameterBaseName = "profile";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("profile", profile));
        localVarQueryParameterBaseName = "level";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("level", level));
        localVarQueryParameterBaseName = "framerate";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("framerate", framerate));
        localVarQueryParameterBaseName = "maxFramerate";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxFramerate", maxFramerate));
        localVarQueryParameterBaseName = "copyTimestamps";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("copyTimestamps", copyTimestamps));
        localVarQueryParameterBaseName = "startTimeTicks";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("startTimeTicks", startTimeTicks));
        localVarQueryParameterBaseName = "width";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("width", width));
        localVarQueryParameterBaseName = "height";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("height", height));
        localVarQueryParameterBaseName = "maxWidth";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxWidth", maxWidth));
        localVarQueryParameterBaseName = "maxHeight";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxHeight", maxHeight));
        localVarQueryParameterBaseName = "videoBitRate";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("videoBitRate", videoBitRate));
        localVarQueryParameterBaseName = "subtitleStreamIndex";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("subtitleStreamIndex", subtitleStreamIndex));
        localVarQueryParameterBaseName = "subtitleMethod";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("subtitleMethod", subtitleMethod));
        localVarQueryParameterBaseName = "maxRefFrames";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxRefFrames", maxRefFrames));
        localVarQueryParameterBaseName = "maxVideoBitDepth";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxVideoBitDepth", maxVideoBitDepth));
        localVarQueryParameterBaseName = "requireAvc";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("requireAvc", requireAvc));
        localVarQueryParameterBaseName = "deInterlace";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("deInterlace", deInterlace));
        localVarQueryParameterBaseName = "requireNonAnamorphic";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("requireNonAnamorphic", requireNonAnamorphic));
        localVarQueryParameterBaseName = "transcodingMaxAudioChannels";
        localVarQueryParams
                .addAll(ApiClient.parameterToPairs("transcodingMaxAudioChannels", transcodingMaxAudioChannels));
        localVarQueryParameterBaseName = "cpuCoreLimit";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("cpuCoreLimit", cpuCoreLimit));
        localVarQueryParameterBaseName = "liveStreamId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("liveStreamId", liveStreamId));
        localVarQueryParameterBaseName = "enableMpegtsM2TsMode";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("enableMpegtsM2TsMode", enableMpegtsM2TsMode));
        localVarQueryParameterBaseName = "videoCodec";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("videoCodec", videoCodec));
        localVarQueryParameterBaseName = "subtitleCodec";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("subtitleCodec", subtitleCodec));
        localVarQueryParameterBaseName = "transcodeReasons";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("transcodeReasons", transcodeReasons));
        localVarQueryParameterBaseName = "audioStreamIndex";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("audioStreamIndex", audioStreamIndex));
        localVarQueryParameterBaseName = "videoStreamIndex";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("videoStreamIndex", videoStreamIndex));
        localVarQueryParameterBaseName = "context";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("context", context));
        localVarQueryParameterBaseName = "streamOptions";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("streamOptions", streamOptions));
        localVarQueryParameterBaseName = "enableAdaptiveBitrateStreaming";
        localVarQueryParams
                .addAll(ApiClient.parameterToPairs("enableAdaptiveBitrateStreaming", enableAdaptiveBitrateStreaming));
        localVarQueryParameterBaseName = "enableTrickplay";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("enableTrickplay", enableTrickplay));
        localVarQueryParameterBaseName = "enableAudioVbrEncoding";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("enableAudioVbrEncoding", enableAudioVbrEncoding));
        localVarQueryParameterBaseName = "alwaysBurnInSubtitleWhenTranscoding";
        localVarQueryParams.addAll(
                ApiClient.parameterToPairs("alwaysBurnInSubtitleWhenTranscoding", alwaysBurnInSubtitleWhenTranscoding));

        if (!localVarQueryParams.isEmpty() || localVarQueryStringJoiner.length() != 0) {
            StringJoiner queryJoiner = new StringJoiner("&");
            localVarQueryParams.forEach(p -> queryJoiner.add(p.getName() + '=' + p.getValue()));
            if (localVarQueryStringJoiner.length() != 0) {
                queryJoiner.add(localVarQueryStringJoiner.toString());
            }
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath + '?' + queryJoiner.toString()));
        } else {
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));
        }

        localVarRequestBuilder.header("Accept", "application/x-mpegURL");

        localVarRequestBuilder.method("GET", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    /**
     * Gets an audio stream using HTTP live streaming.
     * 
     * @param itemId The item id. (required)
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
     * @param audioCodec Optional. Specify an audio codec to encode to, e.g. mp3. (optional)
     * @param enableAutoStreamCopy Whether or not to allow automatic stream copy if requested values match the original
     *            source. Defaults to true. (optional)
     * @param allowVideoStreamCopy Whether or not to allow copying of the video stream url. (optional)
     * @param allowAudioStreamCopy Whether or not to allow copying of the audio stream url. (optional)
     * @param breakOnNonKeyFrames Optional. Whether to break on non key frames. (optional)
     * @param audioSampleRate Optional. Specify a specific audio sample rate, e.g. 44100. (optional)
     * @param maxAudioBitDepth Optional. The maximum audio bit depth. (optional)
     * @param maxStreamingBitrate Optional. The maximum streaming bitrate. (optional)
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
     * @param videoCodec Optional. Specify a video codec to encode to, e.g. h264. (optional)
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
     */
    public File getVariantHlsAudioPlaylist(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull Boolean _static, @org.eclipse.jdt.annotation.NonNull String params,
            @org.eclipse.jdt.annotation.NonNull String tag, @org.eclipse.jdt.annotation.NonNull String deviceProfileId,
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
            @org.eclipse.jdt.annotation.NonNull Integer maxStreamingBitrate,
            @org.eclipse.jdt.annotation.NonNull Integer audioBitRate,
            @org.eclipse.jdt.annotation.NonNull Integer audioChannels,
            @org.eclipse.jdt.annotation.NonNull Integer maxAudioChannels,
            @org.eclipse.jdt.annotation.NonNull String profile, @org.eclipse.jdt.annotation.NonNull String level,
            @org.eclipse.jdt.annotation.NonNull Float framerate, @org.eclipse.jdt.annotation.NonNull Float maxFramerate,
            @org.eclipse.jdt.annotation.NonNull Boolean copyTimestamps,
            @org.eclipse.jdt.annotation.NonNull Long startTimeTicks, @org.eclipse.jdt.annotation.NonNull Integer width,
            @org.eclipse.jdt.annotation.NonNull Integer height,
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
        ApiResponse<File> localVarResponse = getVariantHlsAudioPlaylistWithHttpInfo(itemId, _static, params, tag,
                deviceProfileId, playSessionId, segmentContainer, segmentLength, minSegments, mediaSourceId, deviceId,
                audioCodec, enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames,
                audioSampleRate, maxAudioBitDepth, maxStreamingBitrate, audioBitRate, audioChannels, maxAudioChannels,
                profile, level, framerate, maxFramerate, copyTimestamps, startTimeTicks, width, height, videoBitRate,
                subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth, requireAvc, deInterlace,
                requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode,
                videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex, context, streamOptions,
                enableAudioVbrEncoding);
        return localVarResponse.getData();
    }

    /**
     * Gets an audio stream using HTTP live streaming.
     * 
     * @param itemId The item id. (required)
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
     * @param audioCodec Optional. Specify an audio codec to encode to, e.g. mp3. (optional)
     * @param enableAutoStreamCopy Whether or not to allow automatic stream copy if requested values match the original
     *            source. Defaults to true. (optional)
     * @param allowVideoStreamCopy Whether or not to allow copying of the video stream url. (optional)
     * @param allowAudioStreamCopy Whether or not to allow copying of the audio stream url. (optional)
     * @param breakOnNonKeyFrames Optional. Whether to break on non key frames. (optional)
     * @param audioSampleRate Optional. Specify a specific audio sample rate, e.g. 44100. (optional)
     * @param maxAudioBitDepth Optional. The maximum audio bit depth. (optional)
     * @param maxStreamingBitrate Optional. The maximum streaming bitrate. (optional)
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
     * @param videoCodec Optional. Specify a video codec to encode to, e.g. h264. (optional)
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
     */
    public ApiResponse<File> getVariantHlsAudioPlaylistWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull Boolean _static, @org.eclipse.jdt.annotation.NonNull String params,
            @org.eclipse.jdt.annotation.NonNull String tag, @org.eclipse.jdt.annotation.NonNull String deviceProfileId,
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
            @org.eclipse.jdt.annotation.NonNull Integer maxStreamingBitrate,
            @org.eclipse.jdt.annotation.NonNull Integer audioBitRate,
            @org.eclipse.jdt.annotation.NonNull Integer audioChannels,
            @org.eclipse.jdt.annotation.NonNull Integer maxAudioChannels,
            @org.eclipse.jdt.annotation.NonNull String profile, @org.eclipse.jdt.annotation.NonNull String level,
            @org.eclipse.jdt.annotation.NonNull Float framerate, @org.eclipse.jdt.annotation.NonNull Float maxFramerate,
            @org.eclipse.jdt.annotation.NonNull Boolean copyTimestamps,
            @org.eclipse.jdt.annotation.NonNull Long startTimeTicks, @org.eclipse.jdt.annotation.NonNull Integer width,
            @org.eclipse.jdt.annotation.NonNull Integer height,
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
        HttpRequest.Builder localVarRequestBuilder = getVariantHlsAudioPlaylistRequestBuilder(itemId, _static, params,
                tag, deviceProfileId, playSessionId, segmentContainer, segmentLength, minSegments, mediaSourceId,
                deviceId, audioCodec, enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy,
                breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth, maxStreamingBitrate, audioBitRate,
                audioChannels, maxAudioChannels, profile, level, framerate, maxFramerate, copyTimestamps,
                startTimeTicks, width, height, videoBitRate, subtitleStreamIndex, subtitleMethod, maxRefFrames,
                maxVideoBitDepth, requireAvc, deInterlace, requireNonAnamorphic, transcodingMaxAudioChannels,
                cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode, videoCodec, subtitleCodec, transcodeReasons,
                audioStreamIndex, videoStreamIndex, context, streamOptions, enableAudioVbrEncoding);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getVariantHlsAudioPlaylist", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody, new TypeReference<File>() {
                                }));
            } finally {
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder getVariantHlsAudioPlaylistRequestBuilder(
            @org.eclipse.jdt.annotation.Nullable UUID itemId, @org.eclipse.jdt.annotation.NonNull Boolean _static,
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
            @org.eclipse.jdt.annotation.NonNull Integer maxStreamingBitrate,
            @org.eclipse.jdt.annotation.NonNull Integer audioBitRate,
            @org.eclipse.jdt.annotation.NonNull Integer audioChannels,
            @org.eclipse.jdt.annotation.NonNull Integer maxAudioChannels,
            @org.eclipse.jdt.annotation.NonNull String profile, @org.eclipse.jdt.annotation.NonNull String level,
            @org.eclipse.jdt.annotation.NonNull Float framerate, @org.eclipse.jdt.annotation.NonNull Float maxFramerate,
            @org.eclipse.jdt.annotation.NonNull Boolean copyTimestamps,
            @org.eclipse.jdt.annotation.NonNull Long startTimeTicks, @org.eclipse.jdt.annotation.NonNull Integer width,
            @org.eclipse.jdt.annotation.NonNull Integer height,
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
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'itemId' when calling getVariantHlsAudioPlaylist");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Audio/{itemId}/main.m3u8".replace("{itemId}", ApiClient.urlEncode(itemId.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "static";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("static", _static));
        localVarQueryParameterBaseName = "params";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("params", params));
        localVarQueryParameterBaseName = "tag";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("tag", tag));
        localVarQueryParameterBaseName = "deviceProfileId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("deviceProfileId", deviceProfileId));
        localVarQueryParameterBaseName = "playSessionId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("playSessionId", playSessionId));
        localVarQueryParameterBaseName = "segmentContainer";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("segmentContainer", segmentContainer));
        localVarQueryParameterBaseName = "segmentLength";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("segmentLength", segmentLength));
        localVarQueryParameterBaseName = "minSegments";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("minSegments", minSegments));
        localVarQueryParameterBaseName = "mediaSourceId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("mediaSourceId", mediaSourceId));
        localVarQueryParameterBaseName = "deviceId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("deviceId", deviceId));
        localVarQueryParameterBaseName = "audioCodec";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("audioCodec", audioCodec));
        localVarQueryParameterBaseName = "enableAutoStreamCopy";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("enableAutoStreamCopy", enableAutoStreamCopy));
        localVarQueryParameterBaseName = "allowVideoStreamCopy";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("allowVideoStreamCopy", allowVideoStreamCopy));
        localVarQueryParameterBaseName = "allowAudioStreamCopy";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("allowAudioStreamCopy", allowAudioStreamCopy));
        localVarQueryParameterBaseName = "breakOnNonKeyFrames";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("breakOnNonKeyFrames", breakOnNonKeyFrames));
        localVarQueryParameterBaseName = "audioSampleRate";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("audioSampleRate", audioSampleRate));
        localVarQueryParameterBaseName = "maxAudioBitDepth";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxAudioBitDepth", maxAudioBitDepth));
        localVarQueryParameterBaseName = "maxStreamingBitrate";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxStreamingBitrate", maxStreamingBitrate));
        localVarQueryParameterBaseName = "audioBitRate";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("audioBitRate", audioBitRate));
        localVarQueryParameterBaseName = "audioChannels";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("audioChannels", audioChannels));
        localVarQueryParameterBaseName = "maxAudioChannels";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxAudioChannels", maxAudioChannels));
        localVarQueryParameterBaseName = "profile";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("profile", profile));
        localVarQueryParameterBaseName = "level";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("level", level));
        localVarQueryParameterBaseName = "framerate";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("framerate", framerate));
        localVarQueryParameterBaseName = "maxFramerate";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxFramerate", maxFramerate));
        localVarQueryParameterBaseName = "copyTimestamps";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("copyTimestamps", copyTimestamps));
        localVarQueryParameterBaseName = "startTimeTicks";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("startTimeTicks", startTimeTicks));
        localVarQueryParameterBaseName = "width";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("width", width));
        localVarQueryParameterBaseName = "height";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("height", height));
        localVarQueryParameterBaseName = "videoBitRate";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("videoBitRate", videoBitRate));
        localVarQueryParameterBaseName = "subtitleStreamIndex";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("subtitleStreamIndex", subtitleStreamIndex));
        localVarQueryParameterBaseName = "subtitleMethod";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("subtitleMethod", subtitleMethod));
        localVarQueryParameterBaseName = "maxRefFrames";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxRefFrames", maxRefFrames));
        localVarQueryParameterBaseName = "maxVideoBitDepth";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxVideoBitDepth", maxVideoBitDepth));
        localVarQueryParameterBaseName = "requireAvc";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("requireAvc", requireAvc));
        localVarQueryParameterBaseName = "deInterlace";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("deInterlace", deInterlace));
        localVarQueryParameterBaseName = "requireNonAnamorphic";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("requireNonAnamorphic", requireNonAnamorphic));
        localVarQueryParameterBaseName = "transcodingMaxAudioChannels";
        localVarQueryParams
                .addAll(ApiClient.parameterToPairs("transcodingMaxAudioChannels", transcodingMaxAudioChannels));
        localVarQueryParameterBaseName = "cpuCoreLimit";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("cpuCoreLimit", cpuCoreLimit));
        localVarQueryParameterBaseName = "liveStreamId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("liveStreamId", liveStreamId));
        localVarQueryParameterBaseName = "enableMpegtsM2TsMode";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("enableMpegtsM2TsMode", enableMpegtsM2TsMode));
        localVarQueryParameterBaseName = "videoCodec";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("videoCodec", videoCodec));
        localVarQueryParameterBaseName = "subtitleCodec";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("subtitleCodec", subtitleCodec));
        localVarQueryParameterBaseName = "transcodeReasons";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("transcodeReasons", transcodeReasons));
        localVarQueryParameterBaseName = "audioStreamIndex";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("audioStreamIndex", audioStreamIndex));
        localVarQueryParameterBaseName = "videoStreamIndex";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("videoStreamIndex", videoStreamIndex));
        localVarQueryParameterBaseName = "context";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("context", context));
        localVarQueryParameterBaseName = "streamOptions";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("streamOptions", streamOptions));
        localVarQueryParameterBaseName = "enableAudioVbrEncoding";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("enableAudioVbrEncoding", enableAudioVbrEncoding));

        if (!localVarQueryParams.isEmpty() || localVarQueryStringJoiner.length() != 0) {
            StringJoiner queryJoiner = new StringJoiner("&");
            localVarQueryParams.forEach(p -> queryJoiner.add(p.getName() + '=' + p.getValue()));
            if (localVarQueryStringJoiner.length() != 0) {
                queryJoiner.add(localVarQueryStringJoiner.toString());
            }
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath + '?' + queryJoiner.toString()));
        } else {
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));
        }

        localVarRequestBuilder.header("Accept", "application/x-mpegURL");

        localVarRequestBuilder.method("GET", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    /**
     * Gets a video stream using HTTP live streaming.
     * 
     * @param itemId The item id. (required)
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
     * @param audioCodec Optional. Specify an audio codec to encode to, e.g. mp3. (optional)
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
     * @param videoCodec Optional. Specify a video codec to encode to, e.g. h264. (optional)
     * @param subtitleCodec Optional. Specify a subtitle codec to encode to. (optional)
     * @param transcodeReasons Optional. The transcoding reason. (optional)
     * @param audioStreamIndex Optional. The index of the audio stream to use. If omitted the first audio stream will be
     *            used. (optional)
     * @param videoStreamIndex Optional. The index of the video stream to use. If omitted the first video stream will be
     *            used. (optional)
     * @param context Optional. The MediaBrowser.Model.Dlna.EncodingContext. (optional)
     * @param streamOptions Optional. The streaming options. (optional)
     * @param enableAudioVbrEncoding Optional. Whether to enable Audio Encoding. (optional, default to true)
     * @param alwaysBurnInSubtitleWhenTranscoding Whether to always burn in subtitles when transcoding. (optional,
     *            default to false)
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getVariantHlsVideoPlaylist(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull Boolean _static, @org.eclipse.jdt.annotation.NonNull String params,
            @org.eclipse.jdt.annotation.NonNull String tag, @org.eclipse.jdt.annotation.NonNull String deviceProfileId,
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
            @org.eclipse.jdt.annotation.NonNull Boolean enableAudioVbrEncoding,
            @org.eclipse.jdt.annotation.NonNull Boolean alwaysBurnInSubtitleWhenTranscoding) throws ApiException {
        ApiResponse<File> localVarResponse = getVariantHlsVideoPlaylistWithHttpInfo(itemId, _static, params, tag,
                deviceProfileId, playSessionId, segmentContainer, segmentLength, minSegments, mediaSourceId, deviceId,
                audioCodec, enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames,
                audioSampleRate, maxAudioBitDepth, audioBitRate, audioChannels, maxAudioChannels, profile, level,
                framerate, maxFramerate, copyTimestamps, startTimeTicks, width, height, maxWidth, maxHeight,
                videoBitRate, subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth, requireAvc,
                deInterlace, requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId,
                enableMpegtsM2TsMode, videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex,
                context, streamOptions, enableAudioVbrEncoding, alwaysBurnInSubtitleWhenTranscoding);
        return localVarResponse.getData();
    }

    /**
     * Gets a video stream using HTTP live streaming.
     * 
     * @param itemId The item id. (required)
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
     * @param audioCodec Optional. Specify an audio codec to encode to, e.g. mp3. (optional)
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
     * @param videoCodec Optional. Specify a video codec to encode to, e.g. h264. (optional)
     * @param subtitleCodec Optional. Specify a subtitle codec to encode to. (optional)
     * @param transcodeReasons Optional. The transcoding reason. (optional)
     * @param audioStreamIndex Optional. The index of the audio stream to use. If omitted the first audio stream will be
     *            used. (optional)
     * @param videoStreamIndex Optional. The index of the video stream to use. If omitted the first video stream will be
     *            used. (optional)
     * @param context Optional. The MediaBrowser.Model.Dlna.EncodingContext. (optional)
     * @param streamOptions Optional. The streaming options. (optional)
     * @param enableAudioVbrEncoding Optional. Whether to enable Audio Encoding. (optional, default to true)
     * @param alwaysBurnInSubtitleWhenTranscoding Whether to always burn in subtitles when transcoding. (optional,
     *            default to false)
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getVariantHlsVideoPlaylistWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.NonNull Boolean _static, @org.eclipse.jdt.annotation.NonNull String params,
            @org.eclipse.jdt.annotation.NonNull String tag, @org.eclipse.jdt.annotation.NonNull String deviceProfileId,
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
            @org.eclipse.jdt.annotation.NonNull Boolean enableAudioVbrEncoding,
            @org.eclipse.jdt.annotation.NonNull Boolean alwaysBurnInSubtitleWhenTranscoding) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getVariantHlsVideoPlaylistRequestBuilder(itemId, _static, params,
                tag, deviceProfileId, playSessionId, segmentContainer, segmentLength, minSegments, mediaSourceId,
                deviceId, audioCodec, enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy,
                breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth, audioBitRate, audioChannels, maxAudioChannels,
                profile, level, framerate, maxFramerate, copyTimestamps, startTimeTicks, width, height, maxWidth,
                maxHeight, videoBitRate, subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth,
                requireAvc, deInterlace, requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId,
                enableMpegtsM2TsMode, videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex,
                context, streamOptions, enableAudioVbrEncoding, alwaysBurnInSubtitleWhenTranscoding);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getVariantHlsVideoPlaylist", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody, new TypeReference<File>() {
                                }));
            } finally {
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder getVariantHlsVideoPlaylistRequestBuilder(
            @org.eclipse.jdt.annotation.Nullable UUID itemId, @org.eclipse.jdt.annotation.NonNull Boolean _static,
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
            @org.eclipse.jdt.annotation.NonNull Boolean enableAudioVbrEncoding,
            @org.eclipse.jdt.annotation.NonNull Boolean alwaysBurnInSubtitleWhenTranscoding) throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'itemId' when calling getVariantHlsVideoPlaylist");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Videos/{itemId}/main.m3u8".replace("{itemId}", ApiClient.urlEncode(itemId.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "static";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("static", _static));
        localVarQueryParameterBaseName = "params";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("params", params));
        localVarQueryParameterBaseName = "tag";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("tag", tag));
        localVarQueryParameterBaseName = "deviceProfileId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("deviceProfileId", deviceProfileId));
        localVarQueryParameterBaseName = "playSessionId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("playSessionId", playSessionId));
        localVarQueryParameterBaseName = "segmentContainer";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("segmentContainer", segmentContainer));
        localVarQueryParameterBaseName = "segmentLength";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("segmentLength", segmentLength));
        localVarQueryParameterBaseName = "minSegments";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("minSegments", minSegments));
        localVarQueryParameterBaseName = "mediaSourceId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("mediaSourceId", mediaSourceId));
        localVarQueryParameterBaseName = "deviceId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("deviceId", deviceId));
        localVarQueryParameterBaseName = "audioCodec";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("audioCodec", audioCodec));
        localVarQueryParameterBaseName = "enableAutoStreamCopy";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("enableAutoStreamCopy", enableAutoStreamCopy));
        localVarQueryParameterBaseName = "allowVideoStreamCopy";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("allowVideoStreamCopy", allowVideoStreamCopy));
        localVarQueryParameterBaseName = "allowAudioStreamCopy";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("allowAudioStreamCopy", allowAudioStreamCopy));
        localVarQueryParameterBaseName = "breakOnNonKeyFrames";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("breakOnNonKeyFrames", breakOnNonKeyFrames));
        localVarQueryParameterBaseName = "audioSampleRate";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("audioSampleRate", audioSampleRate));
        localVarQueryParameterBaseName = "maxAudioBitDepth";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxAudioBitDepth", maxAudioBitDepth));
        localVarQueryParameterBaseName = "audioBitRate";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("audioBitRate", audioBitRate));
        localVarQueryParameterBaseName = "audioChannels";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("audioChannels", audioChannels));
        localVarQueryParameterBaseName = "maxAudioChannels";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxAudioChannels", maxAudioChannels));
        localVarQueryParameterBaseName = "profile";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("profile", profile));
        localVarQueryParameterBaseName = "level";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("level", level));
        localVarQueryParameterBaseName = "framerate";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("framerate", framerate));
        localVarQueryParameterBaseName = "maxFramerate";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxFramerate", maxFramerate));
        localVarQueryParameterBaseName = "copyTimestamps";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("copyTimestamps", copyTimestamps));
        localVarQueryParameterBaseName = "startTimeTicks";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("startTimeTicks", startTimeTicks));
        localVarQueryParameterBaseName = "width";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("width", width));
        localVarQueryParameterBaseName = "height";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("height", height));
        localVarQueryParameterBaseName = "maxWidth";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxWidth", maxWidth));
        localVarQueryParameterBaseName = "maxHeight";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxHeight", maxHeight));
        localVarQueryParameterBaseName = "videoBitRate";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("videoBitRate", videoBitRate));
        localVarQueryParameterBaseName = "subtitleStreamIndex";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("subtitleStreamIndex", subtitleStreamIndex));
        localVarQueryParameterBaseName = "subtitleMethod";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("subtitleMethod", subtitleMethod));
        localVarQueryParameterBaseName = "maxRefFrames";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxRefFrames", maxRefFrames));
        localVarQueryParameterBaseName = "maxVideoBitDepth";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxVideoBitDepth", maxVideoBitDepth));
        localVarQueryParameterBaseName = "requireAvc";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("requireAvc", requireAvc));
        localVarQueryParameterBaseName = "deInterlace";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("deInterlace", deInterlace));
        localVarQueryParameterBaseName = "requireNonAnamorphic";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("requireNonAnamorphic", requireNonAnamorphic));
        localVarQueryParameterBaseName = "transcodingMaxAudioChannels";
        localVarQueryParams
                .addAll(ApiClient.parameterToPairs("transcodingMaxAudioChannels", transcodingMaxAudioChannels));
        localVarQueryParameterBaseName = "cpuCoreLimit";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("cpuCoreLimit", cpuCoreLimit));
        localVarQueryParameterBaseName = "liveStreamId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("liveStreamId", liveStreamId));
        localVarQueryParameterBaseName = "enableMpegtsM2TsMode";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("enableMpegtsM2TsMode", enableMpegtsM2TsMode));
        localVarQueryParameterBaseName = "videoCodec";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("videoCodec", videoCodec));
        localVarQueryParameterBaseName = "subtitleCodec";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("subtitleCodec", subtitleCodec));
        localVarQueryParameterBaseName = "transcodeReasons";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("transcodeReasons", transcodeReasons));
        localVarQueryParameterBaseName = "audioStreamIndex";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("audioStreamIndex", audioStreamIndex));
        localVarQueryParameterBaseName = "videoStreamIndex";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("videoStreamIndex", videoStreamIndex));
        localVarQueryParameterBaseName = "context";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("context", context));
        localVarQueryParameterBaseName = "streamOptions";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("streamOptions", streamOptions));
        localVarQueryParameterBaseName = "enableAudioVbrEncoding";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("enableAudioVbrEncoding", enableAudioVbrEncoding));
        localVarQueryParameterBaseName = "alwaysBurnInSubtitleWhenTranscoding";
        localVarQueryParams.addAll(
                ApiClient.parameterToPairs("alwaysBurnInSubtitleWhenTranscoding", alwaysBurnInSubtitleWhenTranscoding));

        if (!localVarQueryParams.isEmpty() || localVarQueryStringJoiner.length() != 0) {
            StringJoiner queryJoiner = new StringJoiner("&");
            localVarQueryParams.forEach(p -> queryJoiner.add(p.getName() + '=' + p.getValue()));
            if (localVarQueryStringJoiner.length() != 0) {
                queryJoiner.add(localVarQueryStringJoiner.toString());
            }
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath + '?' + queryJoiner.toString()));
        } else {
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));
        }

        localVarRequestBuilder.header("Accept", "application/x-mpegURL");

        localVarRequestBuilder.method("GET", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    /**
     * Gets an audio hls playlist stream.
     * 
     * @param itemId The item id. (required)
     * @param mediaSourceId The media version id, if playing an alternate version. (required)
     * @param _static Optional. If true, the original file will be streamed statically without any encoding. Use either
     *            no url extension or the original file extension. true/false. (optional)
     * @param params The streaming parameters. (optional)
     * @param tag The tag. (optional)
     * @param deviceProfileId Optional. The dlna device profile id to utilize. (optional)
     * @param playSessionId The play session id. (optional)
     * @param segmentContainer The segment container. (optional)
     * @param segmentLength The segment length. (optional)
     * @param minSegments The minimum number of segments. (optional)
     * @param deviceId The device id of the client requesting. Used to stop encoding processes when needed. (optional)
     * @param audioCodec Optional. Specify an audio codec to encode to, e.g. mp3. (optional)
     * @param enableAutoStreamCopy Whether or not to allow automatic stream copy if requested values match the original
     *            source. Defaults to true. (optional)
     * @param allowVideoStreamCopy Whether or not to allow copying of the video stream url. (optional)
     * @param allowAudioStreamCopy Whether or not to allow copying of the audio stream url. (optional)
     * @param breakOnNonKeyFrames Optional. Whether to break on non key frames. (optional)
     * @param audioSampleRate Optional. Specify a specific audio sample rate, e.g. 44100. (optional)
     * @param maxAudioBitDepth Optional. The maximum audio bit depth. (optional)
     * @param maxStreamingBitrate Optional. The maximum streaming bitrate. (optional)
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
     * @param videoCodec Optional. Specify a video codec to encode to, e.g. h264. (optional)
     * @param subtitleCodec Optional. Specify a subtitle codec to encode to. (optional)
     * @param transcodeReasons Optional. The transcoding reason. (optional)
     * @param audioStreamIndex Optional. The index of the audio stream to use. If omitted the first audio stream will be
     *            used. (optional)
     * @param videoStreamIndex Optional. The index of the video stream to use. If omitted the first video stream will be
     *            used. (optional)
     * @param context Optional. The MediaBrowser.Model.Dlna.EncodingContext. (optional)
     * @param streamOptions Optional. The streaming options. (optional)
     * @param enableAdaptiveBitrateStreaming Enable adaptive bitrate streaming. (optional, default to true)
     * @param enableAudioVbrEncoding Optional. Whether to enable Audio Encoding. (optional, default to true)
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File headMasterHlsAudioPlaylist(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable String mediaSourceId,
            @org.eclipse.jdt.annotation.NonNull Boolean _static, @org.eclipse.jdt.annotation.NonNull String params,
            @org.eclipse.jdt.annotation.NonNull String tag, @org.eclipse.jdt.annotation.NonNull String deviceProfileId,
            @org.eclipse.jdt.annotation.NonNull String playSessionId,
            @org.eclipse.jdt.annotation.NonNull String segmentContainer,
            @org.eclipse.jdt.annotation.NonNull Integer segmentLength,
            @org.eclipse.jdt.annotation.NonNull Integer minSegments,
            @org.eclipse.jdt.annotation.NonNull String deviceId, @org.eclipse.jdt.annotation.NonNull String audioCodec,
            @org.eclipse.jdt.annotation.NonNull Boolean enableAutoStreamCopy,
            @org.eclipse.jdt.annotation.NonNull Boolean allowVideoStreamCopy,
            @org.eclipse.jdt.annotation.NonNull Boolean allowAudioStreamCopy,
            @org.eclipse.jdt.annotation.NonNull Boolean breakOnNonKeyFrames,
            @org.eclipse.jdt.annotation.NonNull Integer audioSampleRate,
            @org.eclipse.jdt.annotation.NonNull Integer maxAudioBitDepth,
            @org.eclipse.jdt.annotation.NonNull Integer maxStreamingBitrate,
            @org.eclipse.jdt.annotation.NonNull Integer audioBitRate,
            @org.eclipse.jdt.annotation.NonNull Integer audioChannels,
            @org.eclipse.jdt.annotation.NonNull Integer maxAudioChannels,
            @org.eclipse.jdt.annotation.NonNull String profile, @org.eclipse.jdt.annotation.NonNull String level,
            @org.eclipse.jdt.annotation.NonNull Float framerate, @org.eclipse.jdt.annotation.NonNull Float maxFramerate,
            @org.eclipse.jdt.annotation.NonNull Boolean copyTimestamps,
            @org.eclipse.jdt.annotation.NonNull Long startTimeTicks, @org.eclipse.jdt.annotation.NonNull Integer width,
            @org.eclipse.jdt.annotation.NonNull Integer height,
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
            @org.eclipse.jdt.annotation.NonNull Boolean enableAdaptiveBitrateStreaming,
            @org.eclipse.jdt.annotation.NonNull Boolean enableAudioVbrEncoding) throws ApiException {
        ApiResponse<File> localVarResponse = headMasterHlsAudioPlaylistWithHttpInfo(itemId, mediaSourceId, _static,
                params, tag, deviceProfileId, playSessionId, segmentContainer, segmentLength, minSegments, deviceId,
                audioCodec, enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames,
                audioSampleRate, maxAudioBitDepth, maxStreamingBitrate, audioBitRate, audioChannels, maxAudioChannels,
                profile, level, framerate, maxFramerate, copyTimestamps, startTimeTicks, width, height, videoBitRate,
                subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth, requireAvc, deInterlace,
                requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode,
                videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex, context, streamOptions,
                enableAdaptiveBitrateStreaming, enableAudioVbrEncoding);
        return localVarResponse.getData();
    }

    /**
     * Gets an audio hls playlist stream.
     * 
     * @param itemId The item id. (required)
     * @param mediaSourceId The media version id, if playing an alternate version. (required)
     * @param _static Optional. If true, the original file will be streamed statically without any encoding. Use either
     *            no url extension or the original file extension. true/false. (optional)
     * @param params The streaming parameters. (optional)
     * @param tag The tag. (optional)
     * @param deviceProfileId Optional. The dlna device profile id to utilize. (optional)
     * @param playSessionId The play session id. (optional)
     * @param segmentContainer The segment container. (optional)
     * @param segmentLength The segment length. (optional)
     * @param minSegments The minimum number of segments. (optional)
     * @param deviceId The device id of the client requesting. Used to stop encoding processes when needed. (optional)
     * @param audioCodec Optional. Specify an audio codec to encode to, e.g. mp3. (optional)
     * @param enableAutoStreamCopy Whether or not to allow automatic stream copy if requested values match the original
     *            source. Defaults to true. (optional)
     * @param allowVideoStreamCopy Whether or not to allow copying of the video stream url. (optional)
     * @param allowAudioStreamCopy Whether or not to allow copying of the audio stream url. (optional)
     * @param breakOnNonKeyFrames Optional. Whether to break on non key frames. (optional)
     * @param audioSampleRate Optional. Specify a specific audio sample rate, e.g. 44100. (optional)
     * @param maxAudioBitDepth Optional. The maximum audio bit depth. (optional)
     * @param maxStreamingBitrate Optional. The maximum streaming bitrate. (optional)
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
     * @param videoCodec Optional. Specify a video codec to encode to, e.g. h264. (optional)
     * @param subtitleCodec Optional. Specify a subtitle codec to encode to. (optional)
     * @param transcodeReasons Optional. The transcoding reason. (optional)
     * @param audioStreamIndex Optional. The index of the audio stream to use. If omitted the first audio stream will be
     *            used. (optional)
     * @param videoStreamIndex Optional. The index of the video stream to use. If omitted the first video stream will be
     *            used. (optional)
     * @param context Optional. The MediaBrowser.Model.Dlna.EncodingContext. (optional)
     * @param streamOptions Optional. The streaming options. (optional)
     * @param enableAdaptiveBitrateStreaming Enable adaptive bitrate streaming. (optional, default to true)
     * @param enableAudioVbrEncoding Optional. Whether to enable Audio Encoding. (optional, default to true)
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> headMasterHlsAudioPlaylistWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable String mediaSourceId,
            @org.eclipse.jdt.annotation.NonNull Boolean _static, @org.eclipse.jdt.annotation.NonNull String params,
            @org.eclipse.jdt.annotation.NonNull String tag, @org.eclipse.jdt.annotation.NonNull String deviceProfileId,
            @org.eclipse.jdt.annotation.NonNull String playSessionId,
            @org.eclipse.jdt.annotation.NonNull String segmentContainer,
            @org.eclipse.jdt.annotation.NonNull Integer segmentLength,
            @org.eclipse.jdt.annotation.NonNull Integer minSegments,
            @org.eclipse.jdt.annotation.NonNull String deviceId, @org.eclipse.jdt.annotation.NonNull String audioCodec,
            @org.eclipse.jdt.annotation.NonNull Boolean enableAutoStreamCopy,
            @org.eclipse.jdt.annotation.NonNull Boolean allowVideoStreamCopy,
            @org.eclipse.jdt.annotation.NonNull Boolean allowAudioStreamCopy,
            @org.eclipse.jdt.annotation.NonNull Boolean breakOnNonKeyFrames,
            @org.eclipse.jdt.annotation.NonNull Integer audioSampleRate,
            @org.eclipse.jdt.annotation.NonNull Integer maxAudioBitDepth,
            @org.eclipse.jdt.annotation.NonNull Integer maxStreamingBitrate,
            @org.eclipse.jdt.annotation.NonNull Integer audioBitRate,
            @org.eclipse.jdt.annotation.NonNull Integer audioChannels,
            @org.eclipse.jdt.annotation.NonNull Integer maxAudioChannels,
            @org.eclipse.jdt.annotation.NonNull String profile, @org.eclipse.jdt.annotation.NonNull String level,
            @org.eclipse.jdt.annotation.NonNull Float framerate, @org.eclipse.jdt.annotation.NonNull Float maxFramerate,
            @org.eclipse.jdt.annotation.NonNull Boolean copyTimestamps,
            @org.eclipse.jdt.annotation.NonNull Long startTimeTicks, @org.eclipse.jdt.annotation.NonNull Integer width,
            @org.eclipse.jdt.annotation.NonNull Integer height,
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
            @org.eclipse.jdt.annotation.NonNull Boolean enableAdaptiveBitrateStreaming,
            @org.eclipse.jdt.annotation.NonNull Boolean enableAudioVbrEncoding) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = headMasterHlsAudioPlaylistRequestBuilder(itemId, mediaSourceId,
                _static, params, tag, deviceProfileId, playSessionId, segmentContainer, segmentLength, minSegments,
                deviceId, audioCodec, enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy,
                breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth, maxStreamingBitrate, audioBitRate,
                audioChannels, maxAudioChannels, profile, level, framerate, maxFramerate, copyTimestamps,
                startTimeTicks, width, height, videoBitRate, subtitleStreamIndex, subtitleMethod, maxRefFrames,
                maxVideoBitDepth, requireAvc, deInterlace, requireNonAnamorphic, transcodingMaxAudioChannels,
                cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode, videoCodec, subtitleCodec, transcodeReasons,
                audioStreamIndex, videoStreamIndex, context, streamOptions, enableAdaptiveBitrateStreaming,
                enableAudioVbrEncoding);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("headMasterHlsAudioPlaylist", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody, new TypeReference<File>() {
                                }));
            } finally {
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder headMasterHlsAudioPlaylistRequestBuilder(
            @org.eclipse.jdt.annotation.Nullable UUID itemId, @org.eclipse.jdt.annotation.Nullable String mediaSourceId,
            @org.eclipse.jdt.annotation.NonNull Boolean _static, @org.eclipse.jdt.annotation.NonNull String params,
            @org.eclipse.jdt.annotation.NonNull String tag, @org.eclipse.jdt.annotation.NonNull String deviceProfileId,
            @org.eclipse.jdt.annotation.NonNull String playSessionId,
            @org.eclipse.jdt.annotation.NonNull String segmentContainer,
            @org.eclipse.jdt.annotation.NonNull Integer segmentLength,
            @org.eclipse.jdt.annotation.NonNull Integer minSegments,
            @org.eclipse.jdt.annotation.NonNull String deviceId, @org.eclipse.jdt.annotation.NonNull String audioCodec,
            @org.eclipse.jdt.annotation.NonNull Boolean enableAutoStreamCopy,
            @org.eclipse.jdt.annotation.NonNull Boolean allowVideoStreamCopy,
            @org.eclipse.jdt.annotation.NonNull Boolean allowAudioStreamCopy,
            @org.eclipse.jdt.annotation.NonNull Boolean breakOnNonKeyFrames,
            @org.eclipse.jdt.annotation.NonNull Integer audioSampleRate,
            @org.eclipse.jdt.annotation.NonNull Integer maxAudioBitDepth,
            @org.eclipse.jdt.annotation.NonNull Integer maxStreamingBitrate,
            @org.eclipse.jdt.annotation.NonNull Integer audioBitRate,
            @org.eclipse.jdt.annotation.NonNull Integer audioChannels,
            @org.eclipse.jdt.annotation.NonNull Integer maxAudioChannels,
            @org.eclipse.jdt.annotation.NonNull String profile, @org.eclipse.jdt.annotation.NonNull String level,
            @org.eclipse.jdt.annotation.NonNull Float framerate, @org.eclipse.jdt.annotation.NonNull Float maxFramerate,
            @org.eclipse.jdt.annotation.NonNull Boolean copyTimestamps,
            @org.eclipse.jdt.annotation.NonNull Long startTimeTicks, @org.eclipse.jdt.annotation.NonNull Integer width,
            @org.eclipse.jdt.annotation.NonNull Integer height,
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
            @org.eclipse.jdt.annotation.NonNull Boolean enableAdaptiveBitrateStreaming,
            @org.eclipse.jdt.annotation.NonNull Boolean enableAudioVbrEncoding) throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'itemId' when calling headMasterHlsAudioPlaylist");
        }
        // verify the required parameter 'mediaSourceId' is set
        if (mediaSourceId == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'mediaSourceId' when calling headMasterHlsAudioPlaylist");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Audio/{itemId}/master.m3u8".replace("{itemId}", ApiClient.urlEncode(itemId.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "static";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("static", _static));
        localVarQueryParameterBaseName = "params";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("params", params));
        localVarQueryParameterBaseName = "tag";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("tag", tag));
        localVarQueryParameterBaseName = "deviceProfileId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("deviceProfileId", deviceProfileId));
        localVarQueryParameterBaseName = "playSessionId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("playSessionId", playSessionId));
        localVarQueryParameterBaseName = "segmentContainer";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("segmentContainer", segmentContainer));
        localVarQueryParameterBaseName = "segmentLength";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("segmentLength", segmentLength));
        localVarQueryParameterBaseName = "minSegments";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("minSegments", minSegments));
        localVarQueryParameterBaseName = "mediaSourceId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("mediaSourceId", mediaSourceId));
        localVarQueryParameterBaseName = "deviceId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("deviceId", deviceId));
        localVarQueryParameterBaseName = "audioCodec";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("audioCodec", audioCodec));
        localVarQueryParameterBaseName = "enableAutoStreamCopy";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("enableAutoStreamCopy", enableAutoStreamCopy));
        localVarQueryParameterBaseName = "allowVideoStreamCopy";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("allowVideoStreamCopy", allowVideoStreamCopy));
        localVarQueryParameterBaseName = "allowAudioStreamCopy";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("allowAudioStreamCopy", allowAudioStreamCopy));
        localVarQueryParameterBaseName = "breakOnNonKeyFrames";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("breakOnNonKeyFrames", breakOnNonKeyFrames));
        localVarQueryParameterBaseName = "audioSampleRate";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("audioSampleRate", audioSampleRate));
        localVarQueryParameterBaseName = "maxAudioBitDepth";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxAudioBitDepth", maxAudioBitDepth));
        localVarQueryParameterBaseName = "maxStreamingBitrate";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxStreamingBitrate", maxStreamingBitrate));
        localVarQueryParameterBaseName = "audioBitRate";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("audioBitRate", audioBitRate));
        localVarQueryParameterBaseName = "audioChannels";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("audioChannels", audioChannels));
        localVarQueryParameterBaseName = "maxAudioChannels";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxAudioChannels", maxAudioChannels));
        localVarQueryParameterBaseName = "profile";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("profile", profile));
        localVarQueryParameterBaseName = "level";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("level", level));
        localVarQueryParameterBaseName = "framerate";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("framerate", framerate));
        localVarQueryParameterBaseName = "maxFramerate";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxFramerate", maxFramerate));
        localVarQueryParameterBaseName = "copyTimestamps";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("copyTimestamps", copyTimestamps));
        localVarQueryParameterBaseName = "startTimeTicks";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("startTimeTicks", startTimeTicks));
        localVarQueryParameterBaseName = "width";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("width", width));
        localVarQueryParameterBaseName = "height";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("height", height));
        localVarQueryParameterBaseName = "videoBitRate";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("videoBitRate", videoBitRate));
        localVarQueryParameterBaseName = "subtitleStreamIndex";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("subtitleStreamIndex", subtitleStreamIndex));
        localVarQueryParameterBaseName = "subtitleMethod";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("subtitleMethod", subtitleMethod));
        localVarQueryParameterBaseName = "maxRefFrames";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxRefFrames", maxRefFrames));
        localVarQueryParameterBaseName = "maxVideoBitDepth";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxVideoBitDepth", maxVideoBitDepth));
        localVarQueryParameterBaseName = "requireAvc";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("requireAvc", requireAvc));
        localVarQueryParameterBaseName = "deInterlace";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("deInterlace", deInterlace));
        localVarQueryParameterBaseName = "requireNonAnamorphic";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("requireNonAnamorphic", requireNonAnamorphic));
        localVarQueryParameterBaseName = "transcodingMaxAudioChannels";
        localVarQueryParams
                .addAll(ApiClient.parameterToPairs("transcodingMaxAudioChannels", transcodingMaxAudioChannels));
        localVarQueryParameterBaseName = "cpuCoreLimit";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("cpuCoreLimit", cpuCoreLimit));
        localVarQueryParameterBaseName = "liveStreamId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("liveStreamId", liveStreamId));
        localVarQueryParameterBaseName = "enableMpegtsM2TsMode";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("enableMpegtsM2TsMode", enableMpegtsM2TsMode));
        localVarQueryParameterBaseName = "videoCodec";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("videoCodec", videoCodec));
        localVarQueryParameterBaseName = "subtitleCodec";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("subtitleCodec", subtitleCodec));
        localVarQueryParameterBaseName = "transcodeReasons";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("transcodeReasons", transcodeReasons));
        localVarQueryParameterBaseName = "audioStreamIndex";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("audioStreamIndex", audioStreamIndex));
        localVarQueryParameterBaseName = "videoStreamIndex";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("videoStreamIndex", videoStreamIndex));
        localVarQueryParameterBaseName = "context";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("context", context));
        localVarQueryParameterBaseName = "streamOptions";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("streamOptions", streamOptions));
        localVarQueryParameterBaseName = "enableAdaptiveBitrateStreaming";
        localVarQueryParams
                .addAll(ApiClient.parameterToPairs("enableAdaptiveBitrateStreaming", enableAdaptiveBitrateStreaming));
        localVarQueryParameterBaseName = "enableAudioVbrEncoding";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("enableAudioVbrEncoding", enableAudioVbrEncoding));

        if (!localVarQueryParams.isEmpty() || localVarQueryStringJoiner.length() != 0) {
            StringJoiner queryJoiner = new StringJoiner("&");
            localVarQueryParams.forEach(p -> queryJoiner.add(p.getName() + '=' + p.getValue()));
            if (localVarQueryStringJoiner.length() != 0) {
                queryJoiner.add(localVarQueryStringJoiner.toString());
            }
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath + '?' + queryJoiner.toString()));
        } else {
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));
        }

        localVarRequestBuilder.header("Accept", "application/x-mpegURL");

        localVarRequestBuilder.method("HEAD", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    /**
     * Gets a video hls playlist stream.
     * 
     * @param itemId The item id. (required)
     * @param mediaSourceId The media version id, if playing an alternate version. (required)
     * @param _static Optional. If true, the original file will be streamed statically without any encoding. Use either
     *            no url extension or the original file extension. true/false. (optional)
     * @param params The streaming parameters. (optional)
     * @param tag The tag. (optional)
     * @param deviceProfileId Optional. The dlna device profile id to utilize. (optional)
     * @param playSessionId The play session id. (optional)
     * @param segmentContainer The segment container. (optional)
     * @param segmentLength The segment length. (optional)
     * @param minSegments The minimum number of segments. (optional)
     * @param deviceId The device id of the client requesting. Used to stop encoding processes when needed. (optional)
     * @param audioCodec Optional. Specify an audio codec to encode to, e.g. mp3. (optional)
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
     * @param videoCodec Optional. Specify a video codec to encode to, e.g. h264. (optional)
     * @param subtitleCodec Optional. Specify a subtitle codec to encode to. (optional)
     * @param transcodeReasons Optional. The transcoding reason. (optional)
     * @param audioStreamIndex Optional. The index of the audio stream to use. If omitted the first audio stream will be
     *            used. (optional)
     * @param videoStreamIndex Optional. The index of the video stream to use. If omitted the first video stream will be
     *            used. (optional)
     * @param context Optional. The MediaBrowser.Model.Dlna.EncodingContext. (optional)
     * @param streamOptions Optional. The streaming options. (optional)
     * @param enableAdaptiveBitrateStreaming Enable adaptive bitrate streaming. (optional, default to true)
     * @param enableTrickplay Enable trickplay image playlists being added to master playlist. (optional, default to
     *            true)
     * @param enableAudioVbrEncoding Whether to enable Audio Encoding. (optional, default to true)
     * @param alwaysBurnInSubtitleWhenTranscoding Whether to always burn in subtitles when transcoding. (optional,
     *            default to false)
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File headMasterHlsVideoPlaylist(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable String mediaSourceId,
            @org.eclipse.jdt.annotation.NonNull Boolean _static, @org.eclipse.jdt.annotation.NonNull String params,
            @org.eclipse.jdt.annotation.NonNull String tag, @org.eclipse.jdt.annotation.NonNull String deviceProfileId,
            @org.eclipse.jdt.annotation.NonNull String playSessionId,
            @org.eclipse.jdt.annotation.NonNull String segmentContainer,
            @org.eclipse.jdt.annotation.NonNull Integer segmentLength,
            @org.eclipse.jdt.annotation.NonNull Integer minSegments,
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
            @org.eclipse.jdt.annotation.NonNull Boolean enableAdaptiveBitrateStreaming,
            @org.eclipse.jdt.annotation.NonNull Boolean enableTrickplay,
            @org.eclipse.jdt.annotation.NonNull Boolean enableAudioVbrEncoding,
            @org.eclipse.jdt.annotation.NonNull Boolean alwaysBurnInSubtitleWhenTranscoding) throws ApiException {
        ApiResponse<File> localVarResponse = headMasterHlsVideoPlaylistWithHttpInfo(itemId, mediaSourceId, _static,
                params, tag, deviceProfileId, playSessionId, segmentContainer, segmentLength, minSegments, deviceId,
                audioCodec, enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames,
                audioSampleRate, maxAudioBitDepth, audioBitRate, audioChannels, maxAudioChannels, profile, level,
                framerate, maxFramerate, copyTimestamps, startTimeTicks, width, height, maxWidth, maxHeight,
                videoBitRate, subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth, requireAvc,
                deInterlace, requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId,
                enableMpegtsM2TsMode, videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex,
                context, streamOptions, enableAdaptiveBitrateStreaming, enableTrickplay, enableAudioVbrEncoding,
                alwaysBurnInSubtitleWhenTranscoding);
        return localVarResponse.getData();
    }

    /**
     * Gets a video hls playlist stream.
     * 
     * @param itemId The item id. (required)
     * @param mediaSourceId The media version id, if playing an alternate version. (required)
     * @param _static Optional. If true, the original file will be streamed statically without any encoding. Use either
     *            no url extension or the original file extension. true/false. (optional)
     * @param params The streaming parameters. (optional)
     * @param tag The tag. (optional)
     * @param deviceProfileId Optional. The dlna device profile id to utilize. (optional)
     * @param playSessionId The play session id. (optional)
     * @param segmentContainer The segment container. (optional)
     * @param segmentLength The segment length. (optional)
     * @param minSegments The minimum number of segments. (optional)
     * @param deviceId The device id of the client requesting. Used to stop encoding processes when needed. (optional)
     * @param audioCodec Optional. Specify an audio codec to encode to, e.g. mp3. (optional)
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
     * @param videoCodec Optional. Specify a video codec to encode to, e.g. h264. (optional)
     * @param subtitleCodec Optional. Specify a subtitle codec to encode to. (optional)
     * @param transcodeReasons Optional. The transcoding reason. (optional)
     * @param audioStreamIndex Optional. The index of the audio stream to use. If omitted the first audio stream will be
     *            used. (optional)
     * @param videoStreamIndex Optional. The index of the video stream to use. If omitted the first video stream will be
     *            used. (optional)
     * @param context Optional. The MediaBrowser.Model.Dlna.EncodingContext. (optional)
     * @param streamOptions Optional. The streaming options. (optional)
     * @param enableAdaptiveBitrateStreaming Enable adaptive bitrate streaming. (optional, default to true)
     * @param enableTrickplay Enable trickplay image playlists being added to master playlist. (optional, default to
     *            true)
     * @param enableAudioVbrEncoding Whether to enable Audio Encoding. (optional, default to true)
     * @param alwaysBurnInSubtitleWhenTranscoding Whether to always burn in subtitles when transcoding. (optional,
     *            default to false)
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> headMasterHlsVideoPlaylistWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable String mediaSourceId,
            @org.eclipse.jdt.annotation.NonNull Boolean _static, @org.eclipse.jdt.annotation.NonNull String params,
            @org.eclipse.jdt.annotation.NonNull String tag, @org.eclipse.jdt.annotation.NonNull String deviceProfileId,
            @org.eclipse.jdt.annotation.NonNull String playSessionId,
            @org.eclipse.jdt.annotation.NonNull String segmentContainer,
            @org.eclipse.jdt.annotation.NonNull Integer segmentLength,
            @org.eclipse.jdt.annotation.NonNull Integer minSegments,
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
            @org.eclipse.jdt.annotation.NonNull Boolean enableAdaptiveBitrateStreaming,
            @org.eclipse.jdt.annotation.NonNull Boolean enableTrickplay,
            @org.eclipse.jdt.annotation.NonNull Boolean enableAudioVbrEncoding,
            @org.eclipse.jdt.annotation.NonNull Boolean alwaysBurnInSubtitleWhenTranscoding) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = headMasterHlsVideoPlaylistRequestBuilder(itemId, mediaSourceId,
                _static, params, tag, deviceProfileId, playSessionId, segmentContainer, segmentLength, minSegments,
                deviceId, audioCodec, enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy,
                breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth, audioBitRate, audioChannels, maxAudioChannels,
                profile, level, framerate, maxFramerate, copyTimestamps, startTimeTicks, width, height, maxWidth,
                maxHeight, videoBitRate, subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth,
                requireAvc, deInterlace, requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId,
                enableMpegtsM2TsMode, videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex,
                context, streamOptions, enableAdaptiveBitrateStreaming, enableTrickplay, enableAudioVbrEncoding,
                alwaysBurnInSubtitleWhenTranscoding);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("headMasterHlsVideoPlaylist", localVarResponse);
                }
                if (localVarResponse.body() == null) {
                    return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponse.body().readAllBytes());
                localVarResponse.body().close();

                return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                        responseBody.isBlank() ? null
                                : memberVarObjectMapper.readValue(responseBody, new TypeReference<File>() {
                                }));
            } finally {
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder headMasterHlsVideoPlaylistRequestBuilder(
            @org.eclipse.jdt.annotation.Nullable UUID itemId, @org.eclipse.jdt.annotation.Nullable String mediaSourceId,
            @org.eclipse.jdt.annotation.NonNull Boolean _static, @org.eclipse.jdt.annotation.NonNull String params,
            @org.eclipse.jdt.annotation.NonNull String tag, @org.eclipse.jdt.annotation.NonNull String deviceProfileId,
            @org.eclipse.jdt.annotation.NonNull String playSessionId,
            @org.eclipse.jdt.annotation.NonNull String segmentContainer,
            @org.eclipse.jdt.annotation.NonNull Integer segmentLength,
            @org.eclipse.jdt.annotation.NonNull Integer minSegments,
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
            @org.eclipse.jdt.annotation.NonNull Boolean enableAdaptiveBitrateStreaming,
            @org.eclipse.jdt.annotation.NonNull Boolean enableTrickplay,
            @org.eclipse.jdt.annotation.NonNull Boolean enableAudioVbrEncoding,
            @org.eclipse.jdt.annotation.NonNull Boolean alwaysBurnInSubtitleWhenTranscoding) throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'itemId' when calling headMasterHlsVideoPlaylist");
        }
        // verify the required parameter 'mediaSourceId' is set
        if (mediaSourceId == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'mediaSourceId' when calling headMasterHlsVideoPlaylist");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Videos/{itemId}/master.m3u8".replace("{itemId}",
                ApiClient.urlEncode(itemId.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "static";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("static", _static));
        localVarQueryParameterBaseName = "params";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("params", params));
        localVarQueryParameterBaseName = "tag";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("tag", tag));
        localVarQueryParameterBaseName = "deviceProfileId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("deviceProfileId", deviceProfileId));
        localVarQueryParameterBaseName = "playSessionId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("playSessionId", playSessionId));
        localVarQueryParameterBaseName = "segmentContainer";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("segmentContainer", segmentContainer));
        localVarQueryParameterBaseName = "segmentLength";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("segmentLength", segmentLength));
        localVarQueryParameterBaseName = "minSegments";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("minSegments", minSegments));
        localVarQueryParameterBaseName = "mediaSourceId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("mediaSourceId", mediaSourceId));
        localVarQueryParameterBaseName = "deviceId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("deviceId", deviceId));
        localVarQueryParameterBaseName = "audioCodec";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("audioCodec", audioCodec));
        localVarQueryParameterBaseName = "enableAutoStreamCopy";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("enableAutoStreamCopy", enableAutoStreamCopy));
        localVarQueryParameterBaseName = "allowVideoStreamCopy";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("allowVideoStreamCopy", allowVideoStreamCopy));
        localVarQueryParameterBaseName = "allowAudioStreamCopy";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("allowAudioStreamCopy", allowAudioStreamCopy));
        localVarQueryParameterBaseName = "breakOnNonKeyFrames";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("breakOnNonKeyFrames", breakOnNonKeyFrames));
        localVarQueryParameterBaseName = "audioSampleRate";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("audioSampleRate", audioSampleRate));
        localVarQueryParameterBaseName = "maxAudioBitDepth";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxAudioBitDepth", maxAudioBitDepth));
        localVarQueryParameterBaseName = "audioBitRate";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("audioBitRate", audioBitRate));
        localVarQueryParameterBaseName = "audioChannels";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("audioChannels", audioChannels));
        localVarQueryParameterBaseName = "maxAudioChannels";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxAudioChannels", maxAudioChannels));
        localVarQueryParameterBaseName = "profile";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("profile", profile));
        localVarQueryParameterBaseName = "level";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("level", level));
        localVarQueryParameterBaseName = "framerate";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("framerate", framerate));
        localVarQueryParameterBaseName = "maxFramerate";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxFramerate", maxFramerate));
        localVarQueryParameterBaseName = "copyTimestamps";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("copyTimestamps", copyTimestamps));
        localVarQueryParameterBaseName = "startTimeTicks";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("startTimeTicks", startTimeTicks));
        localVarQueryParameterBaseName = "width";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("width", width));
        localVarQueryParameterBaseName = "height";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("height", height));
        localVarQueryParameterBaseName = "maxWidth";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxWidth", maxWidth));
        localVarQueryParameterBaseName = "maxHeight";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxHeight", maxHeight));
        localVarQueryParameterBaseName = "videoBitRate";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("videoBitRate", videoBitRate));
        localVarQueryParameterBaseName = "subtitleStreamIndex";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("subtitleStreamIndex", subtitleStreamIndex));
        localVarQueryParameterBaseName = "subtitleMethod";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("subtitleMethod", subtitleMethod));
        localVarQueryParameterBaseName = "maxRefFrames";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxRefFrames", maxRefFrames));
        localVarQueryParameterBaseName = "maxVideoBitDepth";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxVideoBitDepth", maxVideoBitDepth));
        localVarQueryParameterBaseName = "requireAvc";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("requireAvc", requireAvc));
        localVarQueryParameterBaseName = "deInterlace";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("deInterlace", deInterlace));
        localVarQueryParameterBaseName = "requireNonAnamorphic";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("requireNonAnamorphic", requireNonAnamorphic));
        localVarQueryParameterBaseName = "transcodingMaxAudioChannels";
        localVarQueryParams
                .addAll(ApiClient.parameterToPairs("transcodingMaxAudioChannels", transcodingMaxAudioChannels));
        localVarQueryParameterBaseName = "cpuCoreLimit";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("cpuCoreLimit", cpuCoreLimit));
        localVarQueryParameterBaseName = "liveStreamId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("liveStreamId", liveStreamId));
        localVarQueryParameterBaseName = "enableMpegtsM2TsMode";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("enableMpegtsM2TsMode", enableMpegtsM2TsMode));
        localVarQueryParameterBaseName = "videoCodec";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("videoCodec", videoCodec));
        localVarQueryParameterBaseName = "subtitleCodec";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("subtitleCodec", subtitleCodec));
        localVarQueryParameterBaseName = "transcodeReasons";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("transcodeReasons", transcodeReasons));
        localVarQueryParameterBaseName = "audioStreamIndex";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("audioStreamIndex", audioStreamIndex));
        localVarQueryParameterBaseName = "videoStreamIndex";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("videoStreamIndex", videoStreamIndex));
        localVarQueryParameterBaseName = "context";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("context", context));
        localVarQueryParameterBaseName = "streamOptions";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("streamOptions", streamOptions));
        localVarQueryParameterBaseName = "enableAdaptiveBitrateStreaming";
        localVarQueryParams
                .addAll(ApiClient.parameterToPairs("enableAdaptiveBitrateStreaming", enableAdaptiveBitrateStreaming));
        localVarQueryParameterBaseName = "enableTrickplay";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("enableTrickplay", enableTrickplay));
        localVarQueryParameterBaseName = "enableAudioVbrEncoding";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("enableAudioVbrEncoding", enableAudioVbrEncoding));
        localVarQueryParameterBaseName = "alwaysBurnInSubtitleWhenTranscoding";
        localVarQueryParams.addAll(
                ApiClient.parameterToPairs("alwaysBurnInSubtitleWhenTranscoding", alwaysBurnInSubtitleWhenTranscoding));

        if (!localVarQueryParams.isEmpty() || localVarQueryStringJoiner.length() != 0) {
            StringJoiner queryJoiner = new StringJoiner("&");
            localVarQueryParams.forEach(p -> queryJoiner.add(p.getName() + '=' + p.getValue()));
            if (localVarQueryStringJoiner.length() != 0) {
                queryJoiner.add(localVarQueryStringJoiner.toString());
            }
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath + '?' + queryJoiner.toString()));
        } else {
            localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));
        }

        localVarRequestBuilder.header("Accept", "application/x-mpegURL");

        localVarRequestBuilder.method("HEAD", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }
}
