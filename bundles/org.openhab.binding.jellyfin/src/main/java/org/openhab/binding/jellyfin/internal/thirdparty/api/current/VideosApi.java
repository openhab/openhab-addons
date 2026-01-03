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
package org.openhab.binding.jellyfin.internal.thirdparty.api.current;

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

import org.openhab.binding.jellyfin.internal.thirdparty.api.ApiClient;
import org.openhab.binding.jellyfin.internal.thirdparty.api.ApiException;
import org.openhab.binding.jellyfin.internal.thirdparty.api.ApiResponse;
import org.openhab.binding.jellyfin.internal.thirdparty.api.Configuration;
import org.openhab.binding.jellyfin.internal.thirdparty.api.Pair;
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.BaseItemDtoQueryResult;
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.EncodingContext;
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.SubtitleDeliveryMethod;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class VideosApi {
    /**
     * Utility class for extending HttpRequest.Builder functionality.
     */
    private static class HttpRequestBuilderExtensions {
        /**
         * Adds additional headers to the provided HttpRequest.Builder. Useful for adding method/endpoint specific
         * headers.
         *
         * @param builder the HttpRequest.Builder to which headers will be added
         * @param headers a map of header names and values to add; may be null
         * @return the same HttpRequest.Builder instance with the additional headers set
         */
        static HttpRequest.Builder withAdditionalHeaders(HttpRequest.Builder builder, Map<String, String> headers) {
            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    builder.header(entry.getKey(), entry.getValue());
                }
            }
            return builder;
        }
    }

    private final HttpClient memberVarHttpClient;
    private final ObjectMapper memberVarObjectMapper;
    private final String memberVarBaseUri;
    private final Consumer<HttpRequest.Builder> memberVarInterceptor;
    private final Duration memberVarReadTimeout;
    private final Consumer<HttpResponse<InputStream>> memberVarResponseInterceptor;
    private final Consumer<HttpResponse<InputStream>> memberVarAsyncResponseInterceptor;

    public VideosApi() {
        this(Configuration.getDefaultApiClient());
    }

    public VideosApi(ApiClient apiClient) {
        memberVarHttpClient = apiClient.getHttpClient();
        memberVarObjectMapper = apiClient.getObjectMapper();
        memberVarBaseUri = apiClient.getBaseUri();
        memberVarInterceptor = apiClient.getRequestInterceptor();
        memberVarReadTimeout = apiClient.getReadTimeout();
        memberVarResponseInterceptor = apiClient.getResponseInterceptor();
        memberVarAsyncResponseInterceptor = apiClient.getAsyncResponseInterceptor();
    }

    protected ApiException getApiException(String operationId, HttpResponse<InputStream> response) throws IOException {
        InputStream responseBody = ApiClient.getResponseBody(response);
        String body = null;
        try {
            body = responseBody == null ? null : new String(responseBody.readAllBytes());
        } finally {
            if (responseBody != null) {
                responseBody.close();
            }
        }
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
     * Download file from the given response.
     *
     * @param response Response
     * @return File
     * @throws ApiException If fail to read file content from response and write to disk
     */
    public File downloadFileFromResponse(HttpResponse<InputStream> response, InputStream responseBody)
            throws ApiException {
        if (responseBody == null) {
            throw new ApiException(new IOException("Response body is empty"));
        }
        try {
            File file = prepareDownloadFile(response);
            java.nio.file.Files.copy(responseBody, file.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            return file;
        } catch (IOException e) {
            throw new ApiException(e);
        }
    }

    /**
     * <p>
     * Prepare the file for download from the response.
     * </p>
     *
     * @param response a {@link java.net.http.HttpResponse} object.
     * @return a {@link java.io.File} object.
     * @throws java.io.IOException if any.
     */
    private File prepareDownloadFile(HttpResponse<InputStream> response) throws IOException {
        String filename = null;
        java.util.Optional<String> contentDisposition = response.headers().firstValue("Content-Disposition");
        if (contentDisposition.isPresent() && !"".equals(contentDisposition.get())) {
            // Get filename from the Content-Disposition header.
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("filename=['\"]?([^'\"\\s]+)['\"]?");
            java.util.regex.Matcher matcher = pattern.matcher(contentDisposition.get());
            if (matcher.find())
                filename = matcher.group(1);
        }
        File file = null;
        if (filename != null) {
            java.nio.file.Path tempDir = java.nio.file.Files.createTempDirectory("swagger-gen-native");
            java.nio.file.Path filePath = java.nio.file.Files.createFile(tempDir.resolve(filename));
            file = filePath.toFile();
            tempDir.toFile().deleteOnExit(); // best effort cleanup
            file.deleteOnExit(); // best effort cleanup
        } else {
            file = java.nio.file.Files.createTempFile("download-", "").toFile();
            file.deleteOnExit(); // best effort cleanup
        }
        return file;
    }

    /**
     * Removes alternate video sources.
     * 
     * @param itemId The item id. (required)
     * @throws ApiException if fails to make API call
     */
    public void deleteAlternateSources(@org.eclipse.jdt.annotation.NonNull UUID itemId) throws ApiException {
        deleteAlternateSources(itemId, null);
    }

    /**
     * Removes alternate video sources.
     * 
     * @param itemId The item id. (required)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void deleteAlternateSources(@org.eclipse.jdt.annotation.NonNull UUID itemId, Map<String, String> headers)
            throws ApiException {
        deleteAlternateSourcesWithHttpInfo(itemId, headers);
    }

    /**
     * Removes alternate video sources.
     * 
     * @param itemId The item id. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> deleteAlternateSourcesWithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID itemId)
            throws ApiException {
        return deleteAlternateSourcesWithHttpInfo(itemId, null);
    }

    /**
     * Removes alternate video sources.
     * 
     * @param itemId The item id. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> deleteAlternateSourcesWithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = deleteAlternateSourcesRequestBuilder(itemId, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("deleteAlternateSources", localVarResponse);
                }
                localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
                if (localVarResponseBody != null) {
                    localVarResponseBody.readAllBytes();
                }
                return new ApiResponse<>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
            } finally {
                if (localVarResponseBody != null) {
                    localVarResponseBody.close();
                }
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder deleteAlternateSourcesRequestBuilder(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            Map<String, String> headers) throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling deleteAlternateSources");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Videos/{itemId}/AlternateSources".replace("{itemId}",
                ApiClient.urlEncode(itemId.toString()));

        localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

        localVarRequestBuilder.header("Accept",
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

        localVarRequestBuilder.method("DELETE", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        // Add custom headers if provided
        localVarRequestBuilder = HttpRequestBuilderExtensions.withAdditionalHeaders(localVarRequestBuilder, headers);
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    /**
     * Gets additional parts for a video.
     * 
     * @param itemId The item id. (required)
     * @param userId Optional. Filter by user id, and attach user data. (optional)
     * @return BaseItemDtoQueryResult
     * @throws ApiException if fails to make API call
     */
    public BaseItemDtoQueryResult getAdditionalPart(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.Nullable UUID userId) throws ApiException {
        return getAdditionalPart(itemId, userId, null);
    }

    /**
     * Gets additional parts for a video.
     * 
     * @param itemId The item id. (required)
     * @param userId Optional. Filter by user id, and attach user data. (optional)
     * @param headers Optional headers to include in the request
     * @return BaseItemDtoQueryResult
     * @throws ApiException if fails to make API call
     */
    public BaseItemDtoQueryResult getAdditionalPart(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.Nullable UUID userId, Map<String, String> headers) throws ApiException {
        ApiResponse<BaseItemDtoQueryResult> localVarResponse = getAdditionalPartWithHttpInfo(itemId, userId, headers);
        return localVarResponse.getData();
    }

    /**
     * Gets additional parts for a video.
     * 
     * @param itemId The item id. (required)
     * @param userId Optional. Filter by user id, and attach user data. (optional)
     * @return ApiResponse&lt;BaseItemDtoQueryResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<BaseItemDtoQueryResult> getAdditionalPartWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull UUID itemId, @org.eclipse.jdt.annotation.Nullable UUID userId)
            throws ApiException {
        return getAdditionalPartWithHttpInfo(itemId, userId, null);
    }

    /**
     * Gets additional parts for a video.
     * 
     * @param itemId The item id. (required)
     * @param userId Optional. Filter by user id, and attach user data. (optional)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;BaseItemDtoQueryResult&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<BaseItemDtoQueryResult> getAdditionalPartWithHttpInfo(
            @org.eclipse.jdt.annotation.NonNull UUID itemId, @org.eclipse.jdt.annotation.Nullable UUID userId,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getAdditionalPartRequestBuilder(itemId, userId, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getAdditionalPart", localVarResponse);
                }
                localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
                if (localVarResponseBody == null) {
                    return new ApiResponse<BaseItemDtoQueryResult>(localVarResponse.statusCode(),
                            localVarResponse.headers().map(), null);
                }

                String responseBody = new String(localVarResponseBody.readAllBytes());
                BaseItemDtoQueryResult responseValue = responseBody.isBlank() ? null
                        : memberVarObjectMapper.readValue(responseBody, new TypeReference<BaseItemDtoQueryResult>() {
                        });

                return new ApiResponse<BaseItemDtoQueryResult>(localVarResponse.statusCode(),
                        localVarResponse.headers().map(), responseValue);
            } finally {
                if (localVarResponseBody != null) {
                    localVarResponseBody.close();
                }
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder getAdditionalPartRequestBuilder(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.Nullable UUID userId, Map<String, String> headers) throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling getAdditionalPart");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Videos/{itemId}/AdditionalParts".replace("{itemId}",
                ApiClient.urlEncode(itemId.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "userId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("userId", userId));

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

        localVarRequestBuilder.header("Accept",
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

        localVarRequestBuilder.method("GET", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        // Add custom headers if provided
        localVarRequestBuilder = HttpRequestBuilderExtensions.withAdditionalHeaders(localVarRequestBuilder, headers);
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
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
     */
    public File getVideoStream(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.Nullable String container, @org.eclipse.jdt.annotation.Nullable Boolean _static,
            @org.eclipse.jdt.annotation.Nullable String params, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable String deviceProfileId,
            @org.eclipse.jdt.annotation.Nullable String playSessionId,
            @org.eclipse.jdt.annotation.Nullable String segmentContainer,
            @org.eclipse.jdt.annotation.Nullable Integer segmentLength,
            @org.eclipse.jdt.annotation.Nullable Integer minSegments,
            @org.eclipse.jdt.annotation.Nullable String mediaSourceId,
            @org.eclipse.jdt.annotation.Nullable String deviceId,
            @org.eclipse.jdt.annotation.Nullable String audioCodec,
            @org.eclipse.jdt.annotation.Nullable Boolean enableAutoStreamCopy,
            @org.eclipse.jdt.annotation.Nullable Boolean allowVideoStreamCopy,
            @org.eclipse.jdt.annotation.Nullable Boolean allowAudioStreamCopy,
            @org.eclipse.jdt.annotation.Nullable Boolean breakOnNonKeyFrames,
            @org.eclipse.jdt.annotation.Nullable Integer audioSampleRate,
            @org.eclipse.jdt.annotation.Nullable Integer maxAudioBitDepth,
            @org.eclipse.jdt.annotation.Nullable Integer audioBitRate,
            @org.eclipse.jdt.annotation.Nullable Integer audioChannels,
            @org.eclipse.jdt.annotation.Nullable Integer maxAudioChannels,
            @org.eclipse.jdt.annotation.Nullable String profile, @org.eclipse.jdt.annotation.Nullable String level,
            @org.eclipse.jdt.annotation.Nullable Float framerate,
            @org.eclipse.jdt.annotation.Nullable Float maxFramerate,
            @org.eclipse.jdt.annotation.Nullable Boolean copyTimestamps,
            @org.eclipse.jdt.annotation.Nullable Long startTimeTicks,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Integer videoBitRate,
            @org.eclipse.jdt.annotation.Nullable Integer subtitleStreamIndex,
            @org.eclipse.jdt.annotation.Nullable SubtitleDeliveryMethod subtitleMethod,
            @org.eclipse.jdt.annotation.Nullable Integer maxRefFrames,
            @org.eclipse.jdt.annotation.Nullable Integer maxVideoBitDepth,
            @org.eclipse.jdt.annotation.Nullable Boolean requireAvc,
            @org.eclipse.jdt.annotation.Nullable Boolean deInterlace,
            @org.eclipse.jdt.annotation.Nullable Boolean requireNonAnamorphic,
            @org.eclipse.jdt.annotation.Nullable Integer transcodingMaxAudioChannels,
            @org.eclipse.jdt.annotation.Nullable Integer cpuCoreLimit,
            @org.eclipse.jdt.annotation.Nullable String liveStreamId,
            @org.eclipse.jdt.annotation.Nullable Boolean enableMpegtsM2TsMode,
            @org.eclipse.jdt.annotation.Nullable String videoCodec,
            @org.eclipse.jdt.annotation.Nullable String subtitleCodec,
            @org.eclipse.jdt.annotation.Nullable String transcodeReasons,
            @org.eclipse.jdt.annotation.Nullable Integer audioStreamIndex,
            @org.eclipse.jdt.annotation.Nullable Integer videoStreamIndex,
            @org.eclipse.jdt.annotation.Nullable EncodingContext context,
            @org.eclipse.jdt.annotation.Nullable Map<String, String> streamOptions,
            @org.eclipse.jdt.annotation.Nullable Boolean enableAudioVbrEncoding) throws ApiException {
        return getVideoStream(itemId, container, _static, params, tag, deviceProfileId, playSessionId, segmentContainer,
                segmentLength, minSegments, mediaSourceId, deviceId, audioCodec, enableAutoStreamCopy,
                allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth,
                audioBitRate, audioChannels, maxAudioChannels, profile, level, framerate, maxFramerate, copyTimestamps,
                startTimeTicks, width, height, maxWidth, maxHeight, videoBitRate, subtitleStreamIndex, subtitleMethod,
                maxRefFrames, maxVideoBitDepth, requireAvc, deInterlace, requireNonAnamorphic,
                transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode, videoCodec,
                subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex, context, streamOptions,
                enableAudioVbrEncoding, null);
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
     * @param headers Optional headers to include in the request
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getVideoStream(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.Nullable String container, @org.eclipse.jdt.annotation.Nullable Boolean _static,
            @org.eclipse.jdt.annotation.Nullable String params, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable String deviceProfileId,
            @org.eclipse.jdt.annotation.Nullable String playSessionId,
            @org.eclipse.jdt.annotation.Nullable String segmentContainer,
            @org.eclipse.jdt.annotation.Nullable Integer segmentLength,
            @org.eclipse.jdt.annotation.Nullable Integer minSegments,
            @org.eclipse.jdt.annotation.Nullable String mediaSourceId,
            @org.eclipse.jdt.annotation.Nullable String deviceId,
            @org.eclipse.jdt.annotation.Nullable String audioCodec,
            @org.eclipse.jdt.annotation.Nullable Boolean enableAutoStreamCopy,
            @org.eclipse.jdt.annotation.Nullable Boolean allowVideoStreamCopy,
            @org.eclipse.jdt.annotation.Nullable Boolean allowAudioStreamCopy,
            @org.eclipse.jdt.annotation.Nullable Boolean breakOnNonKeyFrames,
            @org.eclipse.jdt.annotation.Nullable Integer audioSampleRate,
            @org.eclipse.jdt.annotation.Nullable Integer maxAudioBitDepth,
            @org.eclipse.jdt.annotation.Nullable Integer audioBitRate,
            @org.eclipse.jdt.annotation.Nullable Integer audioChannels,
            @org.eclipse.jdt.annotation.Nullable Integer maxAudioChannels,
            @org.eclipse.jdt.annotation.Nullable String profile, @org.eclipse.jdt.annotation.Nullable String level,
            @org.eclipse.jdt.annotation.Nullable Float framerate,
            @org.eclipse.jdt.annotation.Nullable Float maxFramerate,
            @org.eclipse.jdt.annotation.Nullable Boolean copyTimestamps,
            @org.eclipse.jdt.annotation.Nullable Long startTimeTicks,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Integer videoBitRate,
            @org.eclipse.jdt.annotation.Nullable Integer subtitleStreamIndex,
            @org.eclipse.jdt.annotation.Nullable SubtitleDeliveryMethod subtitleMethod,
            @org.eclipse.jdt.annotation.Nullable Integer maxRefFrames,
            @org.eclipse.jdt.annotation.Nullable Integer maxVideoBitDepth,
            @org.eclipse.jdt.annotation.Nullable Boolean requireAvc,
            @org.eclipse.jdt.annotation.Nullable Boolean deInterlace,
            @org.eclipse.jdt.annotation.Nullable Boolean requireNonAnamorphic,
            @org.eclipse.jdt.annotation.Nullable Integer transcodingMaxAudioChannels,
            @org.eclipse.jdt.annotation.Nullable Integer cpuCoreLimit,
            @org.eclipse.jdt.annotation.Nullable String liveStreamId,
            @org.eclipse.jdt.annotation.Nullable Boolean enableMpegtsM2TsMode,
            @org.eclipse.jdt.annotation.Nullable String videoCodec,
            @org.eclipse.jdt.annotation.Nullable String subtitleCodec,
            @org.eclipse.jdt.annotation.Nullable String transcodeReasons,
            @org.eclipse.jdt.annotation.Nullable Integer audioStreamIndex,
            @org.eclipse.jdt.annotation.Nullable Integer videoStreamIndex,
            @org.eclipse.jdt.annotation.Nullable EncodingContext context,
            @org.eclipse.jdt.annotation.Nullable Map<String, String> streamOptions,
            @org.eclipse.jdt.annotation.Nullable Boolean enableAudioVbrEncoding, Map<String, String> headers)
            throws ApiException {
        ApiResponse<File> localVarResponse = getVideoStreamWithHttpInfo(itemId, container, _static, params, tag,
                deviceProfileId, playSessionId, segmentContainer, segmentLength, minSegments, mediaSourceId, deviceId,
                audioCodec, enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames,
                audioSampleRate, maxAudioBitDepth, audioBitRate, audioChannels, maxAudioChannels, profile, level,
                framerate, maxFramerate, copyTimestamps, startTimeTicks, width, height, maxWidth, maxHeight,
                videoBitRate, subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth, requireAvc,
                deInterlace, requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId,
                enableMpegtsM2TsMode, videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex,
                context, streamOptions, enableAudioVbrEncoding, headers);
        return localVarResponse.getData();
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
     */
    public ApiResponse<File> getVideoStreamWithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.Nullable String container, @org.eclipse.jdt.annotation.Nullable Boolean _static,
            @org.eclipse.jdt.annotation.Nullable String params, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable String deviceProfileId,
            @org.eclipse.jdt.annotation.Nullable String playSessionId,
            @org.eclipse.jdt.annotation.Nullable String segmentContainer,
            @org.eclipse.jdt.annotation.Nullable Integer segmentLength,
            @org.eclipse.jdt.annotation.Nullable Integer minSegments,
            @org.eclipse.jdt.annotation.Nullable String mediaSourceId,
            @org.eclipse.jdt.annotation.Nullable String deviceId,
            @org.eclipse.jdt.annotation.Nullable String audioCodec,
            @org.eclipse.jdt.annotation.Nullable Boolean enableAutoStreamCopy,
            @org.eclipse.jdt.annotation.Nullable Boolean allowVideoStreamCopy,
            @org.eclipse.jdt.annotation.Nullable Boolean allowAudioStreamCopy,
            @org.eclipse.jdt.annotation.Nullable Boolean breakOnNonKeyFrames,
            @org.eclipse.jdt.annotation.Nullable Integer audioSampleRate,
            @org.eclipse.jdt.annotation.Nullable Integer maxAudioBitDepth,
            @org.eclipse.jdt.annotation.Nullable Integer audioBitRate,
            @org.eclipse.jdt.annotation.Nullable Integer audioChannels,
            @org.eclipse.jdt.annotation.Nullable Integer maxAudioChannels,
            @org.eclipse.jdt.annotation.Nullable String profile, @org.eclipse.jdt.annotation.Nullable String level,
            @org.eclipse.jdt.annotation.Nullable Float framerate,
            @org.eclipse.jdt.annotation.Nullable Float maxFramerate,
            @org.eclipse.jdt.annotation.Nullable Boolean copyTimestamps,
            @org.eclipse.jdt.annotation.Nullable Long startTimeTicks,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Integer videoBitRate,
            @org.eclipse.jdt.annotation.Nullable Integer subtitleStreamIndex,
            @org.eclipse.jdt.annotation.Nullable SubtitleDeliveryMethod subtitleMethod,
            @org.eclipse.jdt.annotation.Nullable Integer maxRefFrames,
            @org.eclipse.jdt.annotation.Nullable Integer maxVideoBitDepth,
            @org.eclipse.jdt.annotation.Nullable Boolean requireAvc,
            @org.eclipse.jdt.annotation.Nullable Boolean deInterlace,
            @org.eclipse.jdt.annotation.Nullable Boolean requireNonAnamorphic,
            @org.eclipse.jdt.annotation.Nullable Integer transcodingMaxAudioChannels,
            @org.eclipse.jdt.annotation.Nullable Integer cpuCoreLimit,
            @org.eclipse.jdt.annotation.Nullable String liveStreamId,
            @org.eclipse.jdt.annotation.Nullable Boolean enableMpegtsM2TsMode,
            @org.eclipse.jdt.annotation.Nullable String videoCodec,
            @org.eclipse.jdt.annotation.Nullable String subtitleCodec,
            @org.eclipse.jdt.annotation.Nullable String transcodeReasons,
            @org.eclipse.jdt.annotation.Nullable Integer audioStreamIndex,
            @org.eclipse.jdt.annotation.Nullable Integer videoStreamIndex,
            @org.eclipse.jdt.annotation.Nullable EncodingContext context,
            @org.eclipse.jdt.annotation.Nullable Map<String, String> streamOptions,
            @org.eclipse.jdt.annotation.Nullable Boolean enableAudioVbrEncoding) throws ApiException {
        return getVideoStreamWithHttpInfo(itemId, container, _static, params, tag, deviceProfileId, playSessionId,
                segmentContainer, segmentLength, minSegments, mediaSourceId, deviceId, audioCodec, enableAutoStreamCopy,
                allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth,
                audioBitRate, audioChannels, maxAudioChannels, profile, level, framerate, maxFramerate, copyTimestamps,
                startTimeTicks, width, height, maxWidth, maxHeight, videoBitRate, subtitleStreamIndex, subtitleMethod,
                maxRefFrames, maxVideoBitDepth, requireAvc, deInterlace, requireNonAnamorphic,
                transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode, videoCodec,
                subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex, context, streamOptions,
                enableAudioVbrEncoding, null);
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
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getVideoStreamWithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.Nullable String container, @org.eclipse.jdt.annotation.Nullable Boolean _static,
            @org.eclipse.jdt.annotation.Nullable String params, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable String deviceProfileId,
            @org.eclipse.jdt.annotation.Nullable String playSessionId,
            @org.eclipse.jdt.annotation.Nullable String segmentContainer,
            @org.eclipse.jdt.annotation.Nullable Integer segmentLength,
            @org.eclipse.jdt.annotation.Nullable Integer minSegments,
            @org.eclipse.jdt.annotation.Nullable String mediaSourceId,
            @org.eclipse.jdt.annotation.Nullable String deviceId,
            @org.eclipse.jdt.annotation.Nullable String audioCodec,
            @org.eclipse.jdt.annotation.Nullable Boolean enableAutoStreamCopy,
            @org.eclipse.jdt.annotation.Nullable Boolean allowVideoStreamCopy,
            @org.eclipse.jdt.annotation.Nullable Boolean allowAudioStreamCopy,
            @org.eclipse.jdt.annotation.Nullable Boolean breakOnNonKeyFrames,
            @org.eclipse.jdt.annotation.Nullable Integer audioSampleRate,
            @org.eclipse.jdt.annotation.Nullable Integer maxAudioBitDepth,
            @org.eclipse.jdt.annotation.Nullable Integer audioBitRate,
            @org.eclipse.jdt.annotation.Nullable Integer audioChannels,
            @org.eclipse.jdt.annotation.Nullable Integer maxAudioChannels,
            @org.eclipse.jdt.annotation.Nullable String profile, @org.eclipse.jdt.annotation.Nullable String level,
            @org.eclipse.jdt.annotation.Nullable Float framerate,
            @org.eclipse.jdt.annotation.Nullable Float maxFramerate,
            @org.eclipse.jdt.annotation.Nullable Boolean copyTimestamps,
            @org.eclipse.jdt.annotation.Nullable Long startTimeTicks,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Integer videoBitRate,
            @org.eclipse.jdt.annotation.Nullable Integer subtitleStreamIndex,
            @org.eclipse.jdt.annotation.Nullable SubtitleDeliveryMethod subtitleMethod,
            @org.eclipse.jdt.annotation.Nullable Integer maxRefFrames,
            @org.eclipse.jdt.annotation.Nullable Integer maxVideoBitDepth,
            @org.eclipse.jdt.annotation.Nullable Boolean requireAvc,
            @org.eclipse.jdt.annotation.Nullable Boolean deInterlace,
            @org.eclipse.jdt.annotation.Nullable Boolean requireNonAnamorphic,
            @org.eclipse.jdt.annotation.Nullable Integer transcodingMaxAudioChannels,
            @org.eclipse.jdt.annotation.Nullable Integer cpuCoreLimit,
            @org.eclipse.jdt.annotation.Nullable String liveStreamId,
            @org.eclipse.jdt.annotation.Nullable Boolean enableMpegtsM2TsMode,
            @org.eclipse.jdt.annotation.Nullable String videoCodec,
            @org.eclipse.jdt.annotation.Nullable String subtitleCodec,
            @org.eclipse.jdt.annotation.Nullable String transcodeReasons,
            @org.eclipse.jdt.annotation.Nullable Integer audioStreamIndex,
            @org.eclipse.jdt.annotation.Nullable Integer videoStreamIndex,
            @org.eclipse.jdt.annotation.Nullable EncodingContext context,
            @org.eclipse.jdt.annotation.Nullable Map<String, String> streamOptions,
            @org.eclipse.jdt.annotation.Nullable Boolean enableAudioVbrEncoding, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getVideoStreamRequestBuilder(itemId, container, _static, params,
                tag, deviceProfileId, playSessionId, segmentContainer, segmentLength, minSegments, mediaSourceId,
                deviceId, audioCodec, enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy,
                breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth, audioBitRate, audioChannels, maxAudioChannels,
                profile, level, framerate, maxFramerate, copyTimestamps, startTimeTicks, width, height, maxWidth,
                maxHeight, videoBitRate, subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth,
                requireAvc, deInterlace, requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId,
                enableMpegtsM2TsMode, videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex,
                context, streamOptions, enableAudioVbrEncoding, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getVideoStream", localVarResponse);
                }
                localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
                if (localVarResponseBody == null) {
                    return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
                }

                // Handle file downloading.
                File responseValue = downloadFileFromResponse(localVarResponse, localVarResponseBody);

                return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                        responseValue);
            } finally {
                if (localVarResponseBody != null) {
                    localVarResponseBody.close();
                }
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder getVideoStreamRequestBuilder(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.Nullable String container, @org.eclipse.jdt.annotation.Nullable Boolean _static,
            @org.eclipse.jdt.annotation.Nullable String params, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable String deviceProfileId,
            @org.eclipse.jdt.annotation.Nullable String playSessionId,
            @org.eclipse.jdt.annotation.Nullable String segmentContainer,
            @org.eclipse.jdt.annotation.Nullable Integer segmentLength,
            @org.eclipse.jdt.annotation.Nullable Integer minSegments,
            @org.eclipse.jdt.annotation.Nullable String mediaSourceId,
            @org.eclipse.jdt.annotation.Nullable String deviceId,
            @org.eclipse.jdt.annotation.Nullable String audioCodec,
            @org.eclipse.jdt.annotation.Nullable Boolean enableAutoStreamCopy,
            @org.eclipse.jdt.annotation.Nullable Boolean allowVideoStreamCopy,
            @org.eclipse.jdt.annotation.Nullable Boolean allowAudioStreamCopy,
            @org.eclipse.jdt.annotation.Nullable Boolean breakOnNonKeyFrames,
            @org.eclipse.jdt.annotation.Nullable Integer audioSampleRate,
            @org.eclipse.jdt.annotation.Nullable Integer maxAudioBitDepth,
            @org.eclipse.jdt.annotation.Nullable Integer audioBitRate,
            @org.eclipse.jdt.annotation.Nullable Integer audioChannels,
            @org.eclipse.jdt.annotation.Nullable Integer maxAudioChannels,
            @org.eclipse.jdt.annotation.Nullable String profile, @org.eclipse.jdt.annotation.Nullable String level,
            @org.eclipse.jdt.annotation.Nullable Float framerate,
            @org.eclipse.jdt.annotation.Nullable Float maxFramerate,
            @org.eclipse.jdt.annotation.Nullable Boolean copyTimestamps,
            @org.eclipse.jdt.annotation.Nullable Long startTimeTicks,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Integer videoBitRate,
            @org.eclipse.jdt.annotation.Nullable Integer subtitleStreamIndex,
            @org.eclipse.jdt.annotation.Nullable SubtitleDeliveryMethod subtitleMethod,
            @org.eclipse.jdt.annotation.Nullable Integer maxRefFrames,
            @org.eclipse.jdt.annotation.Nullable Integer maxVideoBitDepth,
            @org.eclipse.jdt.annotation.Nullable Boolean requireAvc,
            @org.eclipse.jdt.annotation.Nullable Boolean deInterlace,
            @org.eclipse.jdt.annotation.Nullable Boolean requireNonAnamorphic,
            @org.eclipse.jdt.annotation.Nullable Integer transcodingMaxAudioChannels,
            @org.eclipse.jdt.annotation.Nullable Integer cpuCoreLimit,
            @org.eclipse.jdt.annotation.Nullable String liveStreamId,
            @org.eclipse.jdt.annotation.Nullable Boolean enableMpegtsM2TsMode,
            @org.eclipse.jdt.annotation.Nullable String videoCodec,
            @org.eclipse.jdt.annotation.Nullable String subtitleCodec,
            @org.eclipse.jdt.annotation.Nullable String transcodeReasons,
            @org.eclipse.jdt.annotation.Nullable Integer audioStreamIndex,
            @org.eclipse.jdt.annotation.Nullable Integer videoStreamIndex,
            @org.eclipse.jdt.annotation.Nullable EncodingContext context,
            @org.eclipse.jdt.annotation.Nullable Map<String, String> streamOptions,
            @org.eclipse.jdt.annotation.Nullable Boolean enableAudioVbrEncoding, Map<String, String> headers)
            throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling getVideoStream");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Videos/{itemId}/stream".replace("{itemId}", ApiClient.urlEncode(itemId.toString()));

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

        localVarRequestBuilder.header("Accept", "video/*, text/html");

        localVarRequestBuilder.method("GET", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        // Add custom headers if provided
        localVarRequestBuilder = HttpRequestBuilderExtensions.withAdditionalHeaders(localVarRequestBuilder, headers);
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
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
     */
    public File getVideoStreamByContainer(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull String container, @org.eclipse.jdt.annotation.Nullable Boolean _static,
            @org.eclipse.jdt.annotation.Nullable String params, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable String deviceProfileId,
            @org.eclipse.jdt.annotation.Nullable String playSessionId,
            @org.eclipse.jdt.annotation.Nullable String segmentContainer,
            @org.eclipse.jdt.annotation.Nullable Integer segmentLength,
            @org.eclipse.jdt.annotation.Nullable Integer minSegments,
            @org.eclipse.jdt.annotation.Nullable String mediaSourceId,
            @org.eclipse.jdt.annotation.Nullable String deviceId,
            @org.eclipse.jdt.annotation.Nullable String audioCodec,
            @org.eclipse.jdt.annotation.Nullable Boolean enableAutoStreamCopy,
            @org.eclipse.jdt.annotation.Nullable Boolean allowVideoStreamCopy,
            @org.eclipse.jdt.annotation.Nullable Boolean allowAudioStreamCopy,
            @org.eclipse.jdt.annotation.Nullable Boolean breakOnNonKeyFrames,
            @org.eclipse.jdt.annotation.Nullable Integer audioSampleRate,
            @org.eclipse.jdt.annotation.Nullable Integer maxAudioBitDepth,
            @org.eclipse.jdt.annotation.Nullable Integer audioBitRate,
            @org.eclipse.jdt.annotation.Nullable Integer audioChannels,
            @org.eclipse.jdt.annotation.Nullable Integer maxAudioChannels,
            @org.eclipse.jdt.annotation.Nullable String profile, @org.eclipse.jdt.annotation.Nullable String level,
            @org.eclipse.jdt.annotation.Nullable Float framerate,
            @org.eclipse.jdt.annotation.Nullable Float maxFramerate,
            @org.eclipse.jdt.annotation.Nullable Boolean copyTimestamps,
            @org.eclipse.jdt.annotation.Nullable Long startTimeTicks,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Integer videoBitRate,
            @org.eclipse.jdt.annotation.Nullable Integer subtitleStreamIndex,
            @org.eclipse.jdt.annotation.Nullable SubtitleDeliveryMethod subtitleMethod,
            @org.eclipse.jdt.annotation.Nullable Integer maxRefFrames,
            @org.eclipse.jdt.annotation.Nullable Integer maxVideoBitDepth,
            @org.eclipse.jdt.annotation.Nullable Boolean requireAvc,
            @org.eclipse.jdt.annotation.Nullable Boolean deInterlace,
            @org.eclipse.jdt.annotation.Nullable Boolean requireNonAnamorphic,
            @org.eclipse.jdt.annotation.Nullable Integer transcodingMaxAudioChannels,
            @org.eclipse.jdt.annotation.Nullable Integer cpuCoreLimit,
            @org.eclipse.jdt.annotation.Nullable String liveStreamId,
            @org.eclipse.jdt.annotation.Nullable Boolean enableMpegtsM2TsMode,
            @org.eclipse.jdt.annotation.Nullable String videoCodec,
            @org.eclipse.jdt.annotation.Nullable String subtitleCodec,
            @org.eclipse.jdt.annotation.Nullable String transcodeReasons,
            @org.eclipse.jdt.annotation.Nullable Integer audioStreamIndex,
            @org.eclipse.jdt.annotation.Nullable Integer videoStreamIndex,
            @org.eclipse.jdt.annotation.Nullable EncodingContext context,
            @org.eclipse.jdt.annotation.Nullable Map<String, String> streamOptions,
            @org.eclipse.jdt.annotation.Nullable Boolean enableAudioVbrEncoding) throws ApiException {
        return getVideoStreamByContainer(itemId, container, _static, params, tag, deviceProfileId, playSessionId,
                segmentContainer, segmentLength, minSegments, mediaSourceId, deviceId, audioCodec, enableAutoStreamCopy,
                allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth,
                audioBitRate, audioChannels, maxAudioChannels, profile, level, framerate, maxFramerate, copyTimestamps,
                startTimeTicks, width, height, maxWidth, maxHeight, videoBitRate, subtitleStreamIndex, subtitleMethod,
                maxRefFrames, maxVideoBitDepth, requireAvc, deInterlace, requireNonAnamorphic,
                transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode, videoCodec,
                subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex, context, streamOptions,
                enableAudioVbrEncoding, null);
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
     * @param headers Optional headers to include in the request
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getVideoStreamByContainer(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull String container, @org.eclipse.jdt.annotation.Nullable Boolean _static,
            @org.eclipse.jdt.annotation.Nullable String params, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable String deviceProfileId,
            @org.eclipse.jdt.annotation.Nullable String playSessionId,
            @org.eclipse.jdt.annotation.Nullable String segmentContainer,
            @org.eclipse.jdt.annotation.Nullable Integer segmentLength,
            @org.eclipse.jdt.annotation.Nullable Integer minSegments,
            @org.eclipse.jdt.annotation.Nullable String mediaSourceId,
            @org.eclipse.jdt.annotation.Nullable String deviceId,
            @org.eclipse.jdt.annotation.Nullable String audioCodec,
            @org.eclipse.jdt.annotation.Nullable Boolean enableAutoStreamCopy,
            @org.eclipse.jdt.annotation.Nullable Boolean allowVideoStreamCopy,
            @org.eclipse.jdt.annotation.Nullable Boolean allowAudioStreamCopy,
            @org.eclipse.jdt.annotation.Nullable Boolean breakOnNonKeyFrames,
            @org.eclipse.jdt.annotation.Nullable Integer audioSampleRate,
            @org.eclipse.jdt.annotation.Nullable Integer maxAudioBitDepth,
            @org.eclipse.jdt.annotation.Nullable Integer audioBitRate,
            @org.eclipse.jdt.annotation.Nullable Integer audioChannels,
            @org.eclipse.jdt.annotation.Nullable Integer maxAudioChannels,
            @org.eclipse.jdt.annotation.Nullable String profile, @org.eclipse.jdt.annotation.Nullable String level,
            @org.eclipse.jdt.annotation.Nullable Float framerate,
            @org.eclipse.jdt.annotation.Nullable Float maxFramerate,
            @org.eclipse.jdt.annotation.Nullable Boolean copyTimestamps,
            @org.eclipse.jdt.annotation.Nullable Long startTimeTicks,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Integer videoBitRate,
            @org.eclipse.jdt.annotation.Nullable Integer subtitleStreamIndex,
            @org.eclipse.jdt.annotation.Nullable SubtitleDeliveryMethod subtitleMethod,
            @org.eclipse.jdt.annotation.Nullable Integer maxRefFrames,
            @org.eclipse.jdt.annotation.Nullable Integer maxVideoBitDepth,
            @org.eclipse.jdt.annotation.Nullable Boolean requireAvc,
            @org.eclipse.jdt.annotation.Nullable Boolean deInterlace,
            @org.eclipse.jdt.annotation.Nullable Boolean requireNonAnamorphic,
            @org.eclipse.jdt.annotation.Nullable Integer transcodingMaxAudioChannels,
            @org.eclipse.jdt.annotation.Nullable Integer cpuCoreLimit,
            @org.eclipse.jdt.annotation.Nullable String liveStreamId,
            @org.eclipse.jdt.annotation.Nullable Boolean enableMpegtsM2TsMode,
            @org.eclipse.jdt.annotation.Nullable String videoCodec,
            @org.eclipse.jdt.annotation.Nullable String subtitleCodec,
            @org.eclipse.jdt.annotation.Nullable String transcodeReasons,
            @org.eclipse.jdt.annotation.Nullable Integer audioStreamIndex,
            @org.eclipse.jdt.annotation.Nullable Integer videoStreamIndex,
            @org.eclipse.jdt.annotation.Nullable EncodingContext context,
            @org.eclipse.jdt.annotation.Nullable Map<String, String> streamOptions,
            @org.eclipse.jdt.annotation.Nullable Boolean enableAudioVbrEncoding, Map<String, String> headers)
            throws ApiException {
        ApiResponse<File> localVarResponse = getVideoStreamByContainerWithHttpInfo(itemId, container, _static, params,
                tag, deviceProfileId, playSessionId, segmentContainer, segmentLength, minSegments, mediaSourceId,
                deviceId, audioCodec, enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy,
                breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth, audioBitRate, audioChannels, maxAudioChannels,
                profile, level, framerate, maxFramerate, copyTimestamps, startTimeTicks, width, height, maxWidth,
                maxHeight, videoBitRate, subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth,
                requireAvc, deInterlace, requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId,
                enableMpegtsM2TsMode, videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex,
                context, streamOptions, enableAudioVbrEncoding, headers);
        return localVarResponse.getData();
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
     */
    public ApiResponse<File> getVideoStreamByContainerWithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull String container, @org.eclipse.jdt.annotation.Nullable Boolean _static,
            @org.eclipse.jdt.annotation.Nullable String params, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable String deviceProfileId,
            @org.eclipse.jdt.annotation.Nullable String playSessionId,
            @org.eclipse.jdt.annotation.Nullable String segmentContainer,
            @org.eclipse.jdt.annotation.Nullable Integer segmentLength,
            @org.eclipse.jdt.annotation.Nullable Integer minSegments,
            @org.eclipse.jdt.annotation.Nullable String mediaSourceId,
            @org.eclipse.jdt.annotation.Nullable String deviceId,
            @org.eclipse.jdt.annotation.Nullable String audioCodec,
            @org.eclipse.jdt.annotation.Nullable Boolean enableAutoStreamCopy,
            @org.eclipse.jdt.annotation.Nullable Boolean allowVideoStreamCopy,
            @org.eclipse.jdt.annotation.Nullable Boolean allowAudioStreamCopy,
            @org.eclipse.jdt.annotation.Nullable Boolean breakOnNonKeyFrames,
            @org.eclipse.jdt.annotation.Nullable Integer audioSampleRate,
            @org.eclipse.jdt.annotation.Nullable Integer maxAudioBitDepth,
            @org.eclipse.jdt.annotation.Nullable Integer audioBitRate,
            @org.eclipse.jdt.annotation.Nullable Integer audioChannels,
            @org.eclipse.jdt.annotation.Nullable Integer maxAudioChannels,
            @org.eclipse.jdt.annotation.Nullable String profile, @org.eclipse.jdt.annotation.Nullable String level,
            @org.eclipse.jdt.annotation.Nullable Float framerate,
            @org.eclipse.jdt.annotation.Nullable Float maxFramerate,
            @org.eclipse.jdt.annotation.Nullable Boolean copyTimestamps,
            @org.eclipse.jdt.annotation.Nullable Long startTimeTicks,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Integer videoBitRate,
            @org.eclipse.jdt.annotation.Nullable Integer subtitleStreamIndex,
            @org.eclipse.jdt.annotation.Nullable SubtitleDeliveryMethod subtitleMethod,
            @org.eclipse.jdt.annotation.Nullable Integer maxRefFrames,
            @org.eclipse.jdt.annotation.Nullable Integer maxVideoBitDepth,
            @org.eclipse.jdt.annotation.Nullable Boolean requireAvc,
            @org.eclipse.jdt.annotation.Nullable Boolean deInterlace,
            @org.eclipse.jdt.annotation.Nullable Boolean requireNonAnamorphic,
            @org.eclipse.jdt.annotation.Nullable Integer transcodingMaxAudioChannels,
            @org.eclipse.jdt.annotation.Nullable Integer cpuCoreLimit,
            @org.eclipse.jdt.annotation.Nullable String liveStreamId,
            @org.eclipse.jdt.annotation.Nullable Boolean enableMpegtsM2TsMode,
            @org.eclipse.jdt.annotation.Nullable String videoCodec,
            @org.eclipse.jdt.annotation.Nullable String subtitleCodec,
            @org.eclipse.jdt.annotation.Nullable String transcodeReasons,
            @org.eclipse.jdt.annotation.Nullable Integer audioStreamIndex,
            @org.eclipse.jdt.annotation.Nullable Integer videoStreamIndex,
            @org.eclipse.jdt.annotation.Nullable EncodingContext context,
            @org.eclipse.jdt.annotation.Nullable Map<String, String> streamOptions,
            @org.eclipse.jdt.annotation.Nullable Boolean enableAudioVbrEncoding) throws ApiException {
        return getVideoStreamByContainerWithHttpInfo(itemId, container, _static, params, tag, deviceProfileId,
                playSessionId, segmentContainer, segmentLength, minSegments, mediaSourceId, deviceId, audioCodec,
                enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames, audioSampleRate,
                maxAudioBitDepth, audioBitRate, audioChannels, maxAudioChannels, profile, level, framerate,
                maxFramerate, copyTimestamps, startTimeTicks, width, height, maxWidth, maxHeight, videoBitRate,
                subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth, requireAvc, deInterlace,
                requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode,
                videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex, context, streamOptions,
                enableAudioVbrEncoding, null);
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
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getVideoStreamByContainerWithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull String container, @org.eclipse.jdt.annotation.Nullable Boolean _static,
            @org.eclipse.jdt.annotation.Nullable String params, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable String deviceProfileId,
            @org.eclipse.jdt.annotation.Nullable String playSessionId,
            @org.eclipse.jdt.annotation.Nullable String segmentContainer,
            @org.eclipse.jdt.annotation.Nullable Integer segmentLength,
            @org.eclipse.jdt.annotation.Nullable Integer minSegments,
            @org.eclipse.jdt.annotation.Nullable String mediaSourceId,
            @org.eclipse.jdt.annotation.Nullable String deviceId,
            @org.eclipse.jdt.annotation.Nullable String audioCodec,
            @org.eclipse.jdt.annotation.Nullable Boolean enableAutoStreamCopy,
            @org.eclipse.jdt.annotation.Nullable Boolean allowVideoStreamCopy,
            @org.eclipse.jdt.annotation.Nullable Boolean allowAudioStreamCopy,
            @org.eclipse.jdt.annotation.Nullable Boolean breakOnNonKeyFrames,
            @org.eclipse.jdt.annotation.Nullable Integer audioSampleRate,
            @org.eclipse.jdt.annotation.Nullable Integer maxAudioBitDepth,
            @org.eclipse.jdt.annotation.Nullable Integer audioBitRate,
            @org.eclipse.jdt.annotation.Nullable Integer audioChannels,
            @org.eclipse.jdt.annotation.Nullable Integer maxAudioChannels,
            @org.eclipse.jdt.annotation.Nullable String profile, @org.eclipse.jdt.annotation.Nullable String level,
            @org.eclipse.jdt.annotation.Nullable Float framerate,
            @org.eclipse.jdt.annotation.Nullable Float maxFramerate,
            @org.eclipse.jdt.annotation.Nullable Boolean copyTimestamps,
            @org.eclipse.jdt.annotation.Nullable Long startTimeTicks,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Integer videoBitRate,
            @org.eclipse.jdt.annotation.Nullable Integer subtitleStreamIndex,
            @org.eclipse.jdt.annotation.Nullable SubtitleDeliveryMethod subtitleMethod,
            @org.eclipse.jdt.annotation.Nullable Integer maxRefFrames,
            @org.eclipse.jdt.annotation.Nullable Integer maxVideoBitDepth,
            @org.eclipse.jdt.annotation.Nullable Boolean requireAvc,
            @org.eclipse.jdt.annotation.Nullable Boolean deInterlace,
            @org.eclipse.jdt.annotation.Nullable Boolean requireNonAnamorphic,
            @org.eclipse.jdt.annotation.Nullable Integer transcodingMaxAudioChannels,
            @org.eclipse.jdt.annotation.Nullable Integer cpuCoreLimit,
            @org.eclipse.jdt.annotation.Nullable String liveStreamId,
            @org.eclipse.jdt.annotation.Nullable Boolean enableMpegtsM2TsMode,
            @org.eclipse.jdt.annotation.Nullable String videoCodec,
            @org.eclipse.jdt.annotation.Nullable String subtitleCodec,
            @org.eclipse.jdt.annotation.Nullable String transcodeReasons,
            @org.eclipse.jdt.annotation.Nullable Integer audioStreamIndex,
            @org.eclipse.jdt.annotation.Nullable Integer videoStreamIndex,
            @org.eclipse.jdt.annotation.Nullable EncodingContext context,
            @org.eclipse.jdt.annotation.Nullable Map<String, String> streamOptions,
            @org.eclipse.jdt.annotation.Nullable Boolean enableAudioVbrEncoding, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getVideoStreamByContainerRequestBuilder(itemId, container, _static,
                params, tag, deviceProfileId, playSessionId, segmentContainer, segmentLength, minSegments,
                mediaSourceId, deviceId, audioCodec, enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy,
                breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth, audioBitRate, audioChannels, maxAudioChannels,
                profile, level, framerate, maxFramerate, copyTimestamps, startTimeTicks, width, height, maxWidth,
                maxHeight, videoBitRate, subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth,
                requireAvc, deInterlace, requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId,
                enableMpegtsM2TsMode, videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex,
                context, streamOptions, enableAudioVbrEncoding, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getVideoStreamByContainer", localVarResponse);
                }
                localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
                if (localVarResponseBody == null) {
                    return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
                }

                // Handle file downloading.
                File responseValue = downloadFileFromResponse(localVarResponse, localVarResponseBody);

                return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                        responseValue);
            } finally {
                if (localVarResponseBody != null) {
                    localVarResponseBody.close();
                }
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder getVideoStreamByContainerRequestBuilder(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull String container, @org.eclipse.jdt.annotation.Nullable Boolean _static,
            @org.eclipse.jdt.annotation.Nullable String params, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable String deviceProfileId,
            @org.eclipse.jdt.annotation.Nullable String playSessionId,
            @org.eclipse.jdt.annotation.Nullable String segmentContainer,
            @org.eclipse.jdt.annotation.Nullable Integer segmentLength,
            @org.eclipse.jdt.annotation.Nullable Integer minSegments,
            @org.eclipse.jdt.annotation.Nullable String mediaSourceId,
            @org.eclipse.jdt.annotation.Nullable String deviceId,
            @org.eclipse.jdt.annotation.Nullable String audioCodec,
            @org.eclipse.jdt.annotation.Nullable Boolean enableAutoStreamCopy,
            @org.eclipse.jdt.annotation.Nullable Boolean allowVideoStreamCopy,
            @org.eclipse.jdt.annotation.Nullable Boolean allowAudioStreamCopy,
            @org.eclipse.jdt.annotation.Nullable Boolean breakOnNonKeyFrames,
            @org.eclipse.jdt.annotation.Nullable Integer audioSampleRate,
            @org.eclipse.jdt.annotation.Nullable Integer maxAudioBitDepth,
            @org.eclipse.jdt.annotation.Nullable Integer audioBitRate,
            @org.eclipse.jdt.annotation.Nullable Integer audioChannels,
            @org.eclipse.jdt.annotation.Nullable Integer maxAudioChannels,
            @org.eclipse.jdt.annotation.Nullable String profile, @org.eclipse.jdt.annotation.Nullable String level,
            @org.eclipse.jdt.annotation.Nullable Float framerate,
            @org.eclipse.jdt.annotation.Nullable Float maxFramerate,
            @org.eclipse.jdt.annotation.Nullable Boolean copyTimestamps,
            @org.eclipse.jdt.annotation.Nullable Long startTimeTicks,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Integer videoBitRate,
            @org.eclipse.jdt.annotation.Nullable Integer subtitleStreamIndex,
            @org.eclipse.jdt.annotation.Nullable SubtitleDeliveryMethod subtitleMethod,
            @org.eclipse.jdt.annotation.Nullable Integer maxRefFrames,
            @org.eclipse.jdt.annotation.Nullable Integer maxVideoBitDepth,
            @org.eclipse.jdt.annotation.Nullable Boolean requireAvc,
            @org.eclipse.jdt.annotation.Nullable Boolean deInterlace,
            @org.eclipse.jdt.annotation.Nullable Boolean requireNonAnamorphic,
            @org.eclipse.jdt.annotation.Nullable Integer transcodingMaxAudioChannels,
            @org.eclipse.jdt.annotation.Nullable Integer cpuCoreLimit,
            @org.eclipse.jdt.annotation.Nullable String liveStreamId,
            @org.eclipse.jdt.annotation.Nullable Boolean enableMpegtsM2TsMode,
            @org.eclipse.jdt.annotation.Nullable String videoCodec,
            @org.eclipse.jdt.annotation.Nullable String subtitleCodec,
            @org.eclipse.jdt.annotation.Nullable String transcodeReasons,
            @org.eclipse.jdt.annotation.Nullable Integer audioStreamIndex,
            @org.eclipse.jdt.annotation.Nullable Integer videoStreamIndex,
            @org.eclipse.jdt.annotation.Nullable EncodingContext context,
            @org.eclipse.jdt.annotation.Nullable Map<String, String> streamOptions,
            @org.eclipse.jdt.annotation.Nullable Boolean enableAudioVbrEncoding, Map<String, String> headers)
            throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'itemId' when calling getVideoStreamByContainer");
        }
        // verify the required parameter 'container' is set
        if (container == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'container' when calling getVideoStreamByContainer");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Videos/{itemId}/stream.{container}"
                .replace("{itemId}", ApiClient.urlEncode(itemId.toString()))
                .replace("{container}", ApiClient.urlEncode(container.toString()));

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

        localVarRequestBuilder.header("Accept", "video/*, text/html");

        localVarRequestBuilder.method("GET", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        // Add custom headers if provided
        localVarRequestBuilder = HttpRequestBuilderExtensions.withAdditionalHeaders(localVarRequestBuilder, headers);
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
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
     */
    public File headVideoStream(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.Nullable String container, @org.eclipse.jdt.annotation.Nullable Boolean _static,
            @org.eclipse.jdt.annotation.Nullable String params, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable String deviceProfileId,
            @org.eclipse.jdt.annotation.Nullable String playSessionId,
            @org.eclipse.jdt.annotation.Nullable String segmentContainer,
            @org.eclipse.jdt.annotation.Nullable Integer segmentLength,
            @org.eclipse.jdt.annotation.Nullable Integer minSegments,
            @org.eclipse.jdt.annotation.Nullable String mediaSourceId,
            @org.eclipse.jdt.annotation.Nullable String deviceId,
            @org.eclipse.jdt.annotation.Nullable String audioCodec,
            @org.eclipse.jdt.annotation.Nullable Boolean enableAutoStreamCopy,
            @org.eclipse.jdt.annotation.Nullable Boolean allowVideoStreamCopy,
            @org.eclipse.jdt.annotation.Nullable Boolean allowAudioStreamCopy,
            @org.eclipse.jdt.annotation.Nullable Boolean breakOnNonKeyFrames,
            @org.eclipse.jdt.annotation.Nullable Integer audioSampleRate,
            @org.eclipse.jdt.annotation.Nullable Integer maxAudioBitDepth,
            @org.eclipse.jdt.annotation.Nullable Integer audioBitRate,
            @org.eclipse.jdt.annotation.Nullable Integer audioChannels,
            @org.eclipse.jdt.annotation.Nullable Integer maxAudioChannels,
            @org.eclipse.jdt.annotation.Nullable String profile, @org.eclipse.jdt.annotation.Nullable String level,
            @org.eclipse.jdt.annotation.Nullable Float framerate,
            @org.eclipse.jdt.annotation.Nullable Float maxFramerate,
            @org.eclipse.jdt.annotation.Nullable Boolean copyTimestamps,
            @org.eclipse.jdt.annotation.Nullable Long startTimeTicks,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Integer videoBitRate,
            @org.eclipse.jdt.annotation.Nullable Integer subtitleStreamIndex,
            @org.eclipse.jdt.annotation.Nullable SubtitleDeliveryMethod subtitleMethod,
            @org.eclipse.jdt.annotation.Nullable Integer maxRefFrames,
            @org.eclipse.jdt.annotation.Nullable Integer maxVideoBitDepth,
            @org.eclipse.jdt.annotation.Nullable Boolean requireAvc,
            @org.eclipse.jdt.annotation.Nullable Boolean deInterlace,
            @org.eclipse.jdt.annotation.Nullable Boolean requireNonAnamorphic,
            @org.eclipse.jdt.annotation.Nullable Integer transcodingMaxAudioChannels,
            @org.eclipse.jdt.annotation.Nullable Integer cpuCoreLimit,
            @org.eclipse.jdt.annotation.Nullable String liveStreamId,
            @org.eclipse.jdt.annotation.Nullable Boolean enableMpegtsM2TsMode,
            @org.eclipse.jdt.annotation.Nullable String videoCodec,
            @org.eclipse.jdt.annotation.Nullable String subtitleCodec,
            @org.eclipse.jdt.annotation.Nullable String transcodeReasons,
            @org.eclipse.jdt.annotation.Nullable Integer audioStreamIndex,
            @org.eclipse.jdt.annotation.Nullable Integer videoStreamIndex,
            @org.eclipse.jdt.annotation.Nullable EncodingContext context,
            @org.eclipse.jdt.annotation.Nullable Map<String, String> streamOptions,
            @org.eclipse.jdt.annotation.Nullable Boolean enableAudioVbrEncoding) throws ApiException {
        return headVideoStream(itemId, container, _static, params, tag, deviceProfileId, playSessionId,
                segmentContainer, segmentLength, minSegments, mediaSourceId, deviceId, audioCodec, enableAutoStreamCopy,
                allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth,
                audioBitRate, audioChannels, maxAudioChannels, profile, level, framerate, maxFramerate, copyTimestamps,
                startTimeTicks, width, height, maxWidth, maxHeight, videoBitRate, subtitleStreamIndex, subtitleMethod,
                maxRefFrames, maxVideoBitDepth, requireAvc, deInterlace, requireNonAnamorphic,
                transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode, videoCodec,
                subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex, context, streamOptions,
                enableAudioVbrEncoding, null);
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
     * @param headers Optional headers to include in the request
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File headVideoStream(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.Nullable String container, @org.eclipse.jdt.annotation.Nullable Boolean _static,
            @org.eclipse.jdt.annotation.Nullable String params, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable String deviceProfileId,
            @org.eclipse.jdt.annotation.Nullable String playSessionId,
            @org.eclipse.jdt.annotation.Nullable String segmentContainer,
            @org.eclipse.jdt.annotation.Nullable Integer segmentLength,
            @org.eclipse.jdt.annotation.Nullable Integer minSegments,
            @org.eclipse.jdt.annotation.Nullable String mediaSourceId,
            @org.eclipse.jdt.annotation.Nullable String deviceId,
            @org.eclipse.jdt.annotation.Nullable String audioCodec,
            @org.eclipse.jdt.annotation.Nullable Boolean enableAutoStreamCopy,
            @org.eclipse.jdt.annotation.Nullable Boolean allowVideoStreamCopy,
            @org.eclipse.jdt.annotation.Nullable Boolean allowAudioStreamCopy,
            @org.eclipse.jdt.annotation.Nullable Boolean breakOnNonKeyFrames,
            @org.eclipse.jdt.annotation.Nullable Integer audioSampleRate,
            @org.eclipse.jdt.annotation.Nullable Integer maxAudioBitDepth,
            @org.eclipse.jdt.annotation.Nullable Integer audioBitRate,
            @org.eclipse.jdt.annotation.Nullable Integer audioChannels,
            @org.eclipse.jdt.annotation.Nullable Integer maxAudioChannels,
            @org.eclipse.jdt.annotation.Nullable String profile, @org.eclipse.jdt.annotation.Nullable String level,
            @org.eclipse.jdt.annotation.Nullable Float framerate,
            @org.eclipse.jdt.annotation.Nullable Float maxFramerate,
            @org.eclipse.jdt.annotation.Nullable Boolean copyTimestamps,
            @org.eclipse.jdt.annotation.Nullable Long startTimeTicks,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Integer videoBitRate,
            @org.eclipse.jdt.annotation.Nullable Integer subtitleStreamIndex,
            @org.eclipse.jdt.annotation.Nullable SubtitleDeliveryMethod subtitleMethod,
            @org.eclipse.jdt.annotation.Nullable Integer maxRefFrames,
            @org.eclipse.jdt.annotation.Nullable Integer maxVideoBitDepth,
            @org.eclipse.jdt.annotation.Nullable Boolean requireAvc,
            @org.eclipse.jdt.annotation.Nullable Boolean deInterlace,
            @org.eclipse.jdt.annotation.Nullable Boolean requireNonAnamorphic,
            @org.eclipse.jdt.annotation.Nullable Integer transcodingMaxAudioChannels,
            @org.eclipse.jdt.annotation.Nullable Integer cpuCoreLimit,
            @org.eclipse.jdt.annotation.Nullable String liveStreamId,
            @org.eclipse.jdt.annotation.Nullable Boolean enableMpegtsM2TsMode,
            @org.eclipse.jdt.annotation.Nullable String videoCodec,
            @org.eclipse.jdt.annotation.Nullable String subtitleCodec,
            @org.eclipse.jdt.annotation.Nullable String transcodeReasons,
            @org.eclipse.jdt.annotation.Nullable Integer audioStreamIndex,
            @org.eclipse.jdt.annotation.Nullable Integer videoStreamIndex,
            @org.eclipse.jdt.annotation.Nullable EncodingContext context,
            @org.eclipse.jdt.annotation.Nullable Map<String, String> streamOptions,
            @org.eclipse.jdt.annotation.Nullable Boolean enableAudioVbrEncoding, Map<String, String> headers)
            throws ApiException {
        ApiResponse<File> localVarResponse = headVideoStreamWithHttpInfo(itemId, container, _static, params, tag,
                deviceProfileId, playSessionId, segmentContainer, segmentLength, minSegments, mediaSourceId, deviceId,
                audioCodec, enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames,
                audioSampleRate, maxAudioBitDepth, audioBitRate, audioChannels, maxAudioChannels, profile, level,
                framerate, maxFramerate, copyTimestamps, startTimeTicks, width, height, maxWidth, maxHeight,
                videoBitRate, subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth, requireAvc,
                deInterlace, requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId,
                enableMpegtsM2TsMode, videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex,
                context, streamOptions, enableAudioVbrEncoding, headers);
        return localVarResponse.getData();
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
     */
    public ApiResponse<File> headVideoStreamWithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.Nullable String container, @org.eclipse.jdt.annotation.Nullable Boolean _static,
            @org.eclipse.jdt.annotation.Nullable String params, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable String deviceProfileId,
            @org.eclipse.jdt.annotation.Nullable String playSessionId,
            @org.eclipse.jdt.annotation.Nullable String segmentContainer,
            @org.eclipse.jdt.annotation.Nullable Integer segmentLength,
            @org.eclipse.jdt.annotation.Nullable Integer minSegments,
            @org.eclipse.jdt.annotation.Nullable String mediaSourceId,
            @org.eclipse.jdt.annotation.Nullable String deviceId,
            @org.eclipse.jdt.annotation.Nullable String audioCodec,
            @org.eclipse.jdt.annotation.Nullable Boolean enableAutoStreamCopy,
            @org.eclipse.jdt.annotation.Nullable Boolean allowVideoStreamCopy,
            @org.eclipse.jdt.annotation.Nullable Boolean allowAudioStreamCopy,
            @org.eclipse.jdt.annotation.Nullable Boolean breakOnNonKeyFrames,
            @org.eclipse.jdt.annotation.Nullable Integer audioSampleRate,
            @org.eclipse.jdt.annotation.Nullable Integer maxAudioBitDepth,
            @org.eclipse.jdt.annotation.Nullable Integer audioBitRate,
            @org.eclipse.jdt.annotation.Nullable Integer audioChannels,
            @org.eclipse.jdt.annotation.Nullable Integer maxAudioChannels,
            @org.eclipse.jdt.annotation.Nullable String profile, @org.eclipse.jdt.annotation.Nullable String level,
            @org.eclipse.jdt.annotation.Nullable Float framerate,
            @org.eclipse.jdt.annotation.Nullable Float maxFramerate,
            @org.eclipse.jdt.annotation.Nullable Boolean copyTimestamps,
            @org.eclipse.jdt.annotation.Nullable Long startTimeTicks,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Integer videoBitRate,
            @org.eclipse.jdt.annotation.Nullable Integer subtitleStreamIndex,
            @org.eclipse.jdt.annotation.Nullable SubtitleDeliveryMethod subtitleMethod,
            @org.eclipse.jdt.annotation.Nullable Integer maxRefFrames,
            @org.eclipse.jdt.annotation.Nullable Integer maxVideoBitDepth,
            @org.eclipse.jdt.annotation.Nullable Boolean requireAvc,
            @org.eclipse.jdt.annotation.Nullable Boolean deInterlace,
            @org.eclipse.jdt.annotation.Nullable Boolean requireNonAnamorphic,
            @org.eclipse.jdt.annotation.Nullable Integer transcodingMaxAudioChannels,
            @org.eclipse.jdt.annotation.Nullable Integer cpuCoreLimit,
            @org.eclipse.jdt.annotation.Nullable String liveStreamId,
            @org.eclipse.jdt.annotation.Nullable Boolean enableMpegtsM2TsMode,
            @org.eclipse.jdt.annotation.Nullable String videoCodec,
            @org.eclipse.jdt.annotation.Nullable String subtitleCodec,
            @org.eclipse.jdt.annotation.Nullable String transcodeReasons,
            @org.eclipse.jdt.annotation.Nullable Integer audioStreamIndex,
            @org.eclipse.jdt.annotation.Nullable Integer videoStreamIndex,
            @org.eclipse.jdt.annotation.Nullable EncodingContext context,
            @org.eclipse.jdt.annotation.Nullable Map<String, String> streamOptions,
            @org.eclipse.jdt.annotation.Nullable Boolean enableAudioVbrEncoding) throws ApiException {
        return headVideoStreamWithHttpInfo(itemId, container, _static, params, tag, deviceProfileId, playSessionId,
                segmentContainer, segmentLength, minSegments, mediaSourceId, deviceId, audioCodec, enableAutoStreamCopy,
                allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth,
                audioBitRate, audioChannels, maxAudioChannels, profile, level, framerate, maxFramerate, copyTimestamps,
                startTimeTicks, width, height, maxWidth, maxHeight, videoBitRate, subtitleStreamIndex, subtitleMethod,
                maxRefFrames, maxVideoBitDepth, requireAvc, deInterlace, requireNonAnamorphic,
                transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode, videoCodec,
                subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex, context, streamOptions,
                enableAudioVbrEncoding, null);
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
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> headVideoStreamWithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.Nullable String container, @org.eclipse.jdt.annotation.Nullable Boolean _static,
            @org.eclipse.jdt.annotation.Nullable String params, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable String deviceProfileId,
            @org.eclipse.jdt.annotation.Nullable String playSessionId,
            @org.eclipse.jdt.annotation.Nullable String segmentContainer,
            @org.eclipse.jdt.annotation.Nullable Integer segmentLength,
            @org.eclipse.jdt.annotation.Nullable Integer minSegments,
            @org.eclipse.jdt.annotation.Nullable String mediaSourceId,
            @org.eclipse.jdt.annotation.Nullable String deviceId,
            @org.eclipse.jdt.annotation.Nullable String audioCodec,
            @org.eclipse.jdt.annotation.Nullable Boolean enableAutoStreamCopy,
            @org.eclipse.jdt.annotation.Nullable Boolean allowVideoStreamCopy,
            @org.eclipse.jdt.annotation.Nullable Boolean allowAudioStreamCopy,
            @org.eclipse.jdt.annotation.Nullable Boolean breakOnNonKeyFrames,
            @org.eclipse.jdt.annotation.Nullable Integer audioSampleRate,
            @org.eclipse.jdt.annotation.Nullable Integer maxAudioBitDepth,
            @org.eclipse.jdt.annotation.Nullable Integer audioBitRate,
            @org.eclipse.jdt.annotation.Nullable Integer audioChannels,
            @org.eclipse.jdt.annotation.Nullable Integer maxAudioChannels,
            @org.eclipse.jdt.annotation.Nullable String profile, @org.eclipse.jdt.annotation.Nullable String level,
            @org.eclipse.jdt.annotation.Nullable Float framerate,
            @org.eclipse.jdt.annotation.Nullable Float maxFramerate,
            @org.eclipse.jdt.annotation.Nullable Boolean copyTimestamps,
            @org.eclipse.jdt.annotation.Nullable Long startTimeTicks,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Integer videoBitRate,
            @org.eclipse.jdt.annotation.Nullable Integer subtitleStreamIndex,
            @org.eclipse.jdt.annotation.Nullable SubtitleDeliveryMethod subtitleMethod,
            @org.eclipse.jdt.annotation.Nullable Integer maxRefFrames,
            @org.eclipse.jdt.annotation.Nullable Integer maxVideoBitDepth,
            @org.eclipse.jdt.annotation.Nullable Boolean requireAvc,
            @org.eclipse.jdt.annotation.Nullable Boolean deInterlace,
            @org.eclipse.jdt.annotation.Nullable Boolean requireNonAnamorphic,
            @org.eclipse.jdt.annotation.Nullable Integer transcodingMaxAudioChannels,
            @org.eclipse.jdt.annotation.Nullable Integer cpuCoreLimit,
            @org.eclipse.jdt.annotation.Nullable String liveStreamId,
            @org.eclipse.jdt.annotation.Nullable Boolean enableMpegtsM2TsMode,
            @org.eclipse.jdt.annotation.Nullable String videoCodec,
            @org.eclipse.jdt.annotation.Nullable String subtitleCodec,
            @org.eclipse.jdt.annotation.Nullable String transcodeReasons,
            @org.eclipse.jdt.annotation.Nullable Integer audioStreamIndex,
            @org.eclipse.jdt.annotation.Nullable Integer videoStreamIndex,
            @org.eclipse.jdt.annotation.Nullable EncodingContext context,
            @org.eclipse.jdt.annotation.Nullable Map<String, String> streamOptions,
            @org.eclipse.jdt.annotation.Nullable Boolean enableAudioVbrEncoding, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = headVideoStreamRequestBuilder(itemId, container, _static, params,
                tag, deviceProfileId, playSessionId, segmentContainer, segmentLength, minSegments, mediaSourceId,
                deviceId, audioCodec, enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy,
                breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth, audioBitRate, audioChannels, maxAudioChannels,
                profile, level, framerate, maxFramerate, copyTimestamps, startTimeTicks, width, height, maxWidth,
                maxHeight, videoBitRate, subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth,
                requireAvc, deInterlace, requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId,
                enableMpegtsM2TsMode, videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex,
                context, streamOptions, enableAudioVbrEncoding, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("headVideoStream", localVarResponse);
                }
                localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
                if (localVarResponseBody == null) {
                    return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
                }

                // Handle file downloading.
                File responseValue = downloadFileFromResponse(localVarResponse, localVarResponseBody);

                return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                        responseValue);
            } finally {
                if (localVarResponseBody != null) {
                    localVarResponseBody.close();
                }
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder headVideoStreamRequestBuilder(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.Nullable String container, @org.eclipse.jdt.annotation.Nullable Boolean _static,
            @org.eclipse.jdt.annotation.Nullable String params, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable String deviceProfileId,
            @org.eclipse.jdt.annotation.Nullable String playSessionId,
            @org.eclipse.jdt.annotation.Nullable String segmentContainer,
            @org.eclipse.jdt.annotation.Nullable Integer segmentLength,
            @org.eclipse.jdt.annotation.Nullable Integer minSegments,
            @org.eclipse.jdt.annotation.Nullable String mediaSourceId,
            @org.eclipse.jdt.annotation.Nullable String deviceId,
            @org.eclipse.jdt.annotation.Nullable String audioCodec,
            @org.eclipse.jdt.annotation.Nullable Boolean enableAutoStreamCopy,
            @org.eclipse.jdt.annotation.Nullable Boolean allowVideoStreamCopy,
            @org.eclipse.jdt.annotation.Nullable Boolean allowAudioStreamCopy,
            @org.eclipse.jdt.annotation.Nullable Boolean breakOnNonKeyFrames,
            @org.eclipse.jdt.annotation.Nullable Integer audioSampleRate,
            @org.eclipse.jdt.annotation.Nullable Integer maxAudioBitDepth,
            @org.eclipse.jdt.annotation.Nullable Integer audioBitRate,
            @org.eclipse.jdt.annotation.Nullable Integer audioChannels,
            @org.eclipse.jdt.annotation.Nullable Integer maxAudioChannels,
            @org.eclipse.jdt.annotation.Nullable String profile, @org.eclipse.jdt.annotation.Nullable String level,
            @org.eclipse.jdt.annotation.Nullable Float framerate,
            @org.eclipse.jdt.annotation.Nullable Float maxFramerate,
            @org.eclipse.jdt.annotation.Nullable Boolean copyTimestamps,
            @org.eclipse.jdt.annotation.Nullable Long startTimeTicks,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Integer videoBitRate,
            @org.eclipse.jdt.annotation.Nullable Integer subtitleStreamIndex,
            @org.eclipse.jdt.annotation.Nullable SubtitleDeliveryMethod subtitleMethod,
            @org.eclipse.jdt.annotation.Nullable Integer maxRefFrames,
            @org.eclipse.jdt.annotation.Nullable Integer maxVideoBitDepth,
            @org.eclipse.jdt.annotation.Nullable Boolean requireAvc,
            @org.eclipse.jdt.annotation.Nullable Boolean deInterlace,
            @org.eclipse.jdt.annotation.Nullable Boolean requireNonAnamorphic,
            @org.eclipse.jdt.annotation.Nullable Integer transcodingMaxAudioChannels,
            @org.eclipse.jdt.annotation.Nullable Integer cpuCoreLimit,
            @org.eclipse.jdt.annotation.Nullable String liveStreamId,
            @org.eclipse.jdt.annotation.Nullable Boolean enableMpegtsM2TsMode,
            @org.eclipse.jdt.annotation.Nullable String videoCodec,
            @org.eclipse.jdt.annotation.Nullable String subtitleCodec,
            @org.eclipse.jdt.annotation.Nullable String transcodeReasons,
            @org.eclipse.jdt.annotation.Nullable Integer audioStreamIndex,
            @org.eclipse.jdt.annotation.Nullable Integer videoStreamIndex,
            @org.eclipse.jdt.annotation.Nullable EncodingContext context,
            @org.eclipse.jdt.annotation.Nullable Map<String, String> streamOptions,
            @org.eclipse.jdt.annotation.Nullable Boolean enableAudioVbrEncoding, Map<String, String> headers)
            throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling headVideoStream");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Videos/{itemId}/stream".replace("{itemId}", ApiClient.urlEncode(itemId.toString()));

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

        localVarRequestBuilder.header("Accept", "video/*, text/html");

        localVarRequestBuilder.method("HEAD", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        // Add custom headers if provided
        localVarRequestBuilder = HttpRequestBuilderExtensions.withAdditionalHeaders(localVarRequestBuilder, headers);
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
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
     */
    public File headVideoStreamByContainer(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull String container, @org.eclipse.jdt.annotation.Nullable Boolean _static,
            @org.eclipse.jdt.annotation.Nullable String params, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable String deviceProfileId,
            @org.eclipse.jdt.annotation.Nullable String playSessionId,
            @org.eclipse.jdt.annotation.Nullable String segmentContainer,
            @org.eclipse.jdt.annotation.Nullable Integer segmentLength,
            @org.eclipse.jdt.annotation.Nullable Integer minSegments,
            @org.eclipse.jdt.annotation.Nullable String mediaSourceId,
            @org.eclipse.jdt.annotation.Nullable String deviceId,
            @org.eclipse.jdt.annotation.Nullable String audioCodec,
            @org.eclipse.jdt.annotation.Nullable Boolean enableAutoStreamCopy,
            @org.eclipse.jdt.annotation.Nullable Boolean allowVideoStreamCopy,
            @org.eclipse.jdt.annotation.Nullable Boolean allowAudioStreamCopy,
            @org.eclipse.jdt.annotation.Nullable Boolean breakOnNonKeyFrames,
            @org.eclipse.jdt.annotation.Nullable Integer audioSampleRate,
            @org.eclipse.jdt.annotation.Nullable Integer maxAudioBitDepth,
            @org.eclipse.jdt.annotation.Nullable Integer audioBitRate,
            @org.eclipse.jdt.annotation.Nullable Integer audioChannels,
            @org.eclipse.jdt.annotation.Nullable Integer maxAudioChannels,
            @org.eclipse.jdt.annotation.Nullable String profile, @org.eclipse.jdt.annotation.Nullable String level,
            @org.eclipse.jdt.annotation.Nullable Float framerate,
            @org.eclipse.jdt.annotation.Nullable Float maxFramerate,
            @org.eclipse.jdt.annotation.Nullable Boolean copyTimestamps,
            @org.eclipse.jdt.annotation.Nullable Long startTimeTicks,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Integer videoBitRate,
            @org.eclipse.jdt.annotation.Nullable Integer subtitleStreamIndex,
            @org.eclipse.jdt.annotation.Nullable SubtitleDeliveryMethod subtitleMethod,
            @org.eclipse.jdt.annotation.Nullable Integer maxRefFrames,
            @org.eclipse.jdt.annotation.Nullable Integer maxVideoBitDepth,
            @org.eclipse.jdt.annotation.Nullable Boolean requireAvc,
            @org.eclipse.jdt.annotation.Nullable Boolean deInterlace,
            @org.eclipse.jdt.annotation.Nullable Boolean requireNonAnamorphic,
            @org.eclipse.jdt.annotation.Nullable Integer transcodingMaxAudioChannels,
            @org.eclipse.jdt.annotation.Nullable Integer cpuCoreLimit,
            @org.eclipse.jdt.annotation.Nullable String liveStreamId,
            @org.eclipse.jdt.annotation.Nullable Boolean enableMpegtsM2TsMode,
            @org.eclipse.jdt.annotation.Nullable String videoCodec,
            @org.eclipse.jdt.annotation.Nullable String subtitleCodec,
            @org.eclipse.jdt.annotation.Nullable String transcodeReasons,
            @org.eclipse.jdt.annotation.Nullable Integer audioStreamIndex,
            @org.eclipse.jdt.annotation.Nullable Integer videoStreamIndex,
            @org.eclipse.jdt.annotation.Nullable EncodingContext context,
            @org.eclipse.jdt.annotation.Nullable Map<String, String> streamOptions,
            @org.eclipse.jdt.annotation.Nullable Boolean enableAudioVbrEncoding) throws ApiException {
        return headVideoStreamByContainer(itemId, container, _static, params, tag, deviceProfileId, playSessionId,
                segmentContainer, segmentLength, minSegments, mediaSourceId, deviceId, audioCodec, enableAutoStreamCopy,
                allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth,
                audioBitRate, audioChannels, maxAudioChannels, profile, level, framerate, maxFramerate, copyTimestamps,
                startTimeTicks, width, height, maxWidth, maxHeight, videoBitRate, subtitleStreamIndex, subtitleMethod,
                maxRefFrames, maxVideoBitDepth, requireAvc, deInterlace, requireNonAnamorphic,
                transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode, videoCodec,
                subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex, context, streamOptions,
                enableAudioVbrEncoding, null);
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
     * @param headers Optional headers to include in the request
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File headVideoStreamByContainer(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull String container, @org.eclipse.jdt.annotation.Nullable Boolean _static,
            @org.eclipse.jdt.annotation.Nullable String params, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable String deviceProfileId,
            @org.eclipse.jdt.annotation.Nullable String playSessionId,
            @org.eclipse.jdt.annotation.Nullable String segmentContainer,
            @org.eclipse.jdt.annotation.Nullable Integer segmentLength,
            @org.eclipse.jdt.annotation.Nullable Integer minSegments,
            @org.eclipse.jdt.annotation.Nullable String mediaSourceId,
            @org.eclipse.jdt.annotation.Nullable String deviceId,
            @org.eclipse.jdt.annotation.Nullable String audioCodec,
            @org.eclipse.jdt.annotation.Nullable Boolean enableAutoStreamCopy,
            @org.eclipse.jdt.annotation.Nullable Boolean allowVideoStreamCopy,
            @org.eclipse.jdt.annotation.Nullable Boolean allowAudioStreamCopy,
            @org.eclipse.jdt.annotation.Nullable Boolean breakOnNonKeyFrames,
            @org.eclipse.jdt.annotation.Nullable Integer audioSampleRate,
            @org.eclipse.jdt.annotation.Nullable Integer maxAudioBitDepth,
            @org.eclipse.jdt.annotation.Nullable Integer audioBitRate,
            @org.eclipse.jdt.annotation.Nullable Integer audioChannels,
            @org.eclipse.jdt.annotation.Nullable Integer maxAudioChannels,
            @org.eclipse.jdt.annotation.Nullable String profile, @org.eclipse.jdt.annotation.Nullable String level,
            @org.eclipse.jdt.annotation.Nullable Float framerate,
            @org.eclipse.jdt.annotation.Nullable Float maxFramerate,
            @org.eclipse.jdt.annotation.Nullable Boolean copyTimestamps,
            @org.eclipse.jdt.annotation.Nullable Long startTimeTicks,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Integer videoBitRate,
            @org.eclipse.jdt.annotation.Nullable Integer subtitleStreamIndex,
            @org.eclipse.jdt.annotation.Nullable SubtitleDeliveryMethod subtitleMethod,
            @org.eclipse.jdt.annotation.Nullable Integer maxRefFrames,
            @org.eclipse.jdt.annotation.Nullable Integer maxVideoBitDepth,
            @org.eclipse.jdt.annotation.Nullable Boolean requireAvc,
            @org.eclipse.jdt.annotation.Nullable Boolean deInterlace,
            @org.eclipse.jdt.annotation.Nullable Boolean requireNonAnamorphic,
            @org.eclipse.jdt.annotation.Nullable Integer transcodingMaxAudioChannels,
            @org.eclipse.jdt.annotation.Nullable Integer cpuCoreLimit,
            @org.eclipse.jdt.annotation.Nullable String liveStreamId,
            @org.eclipse.jdt.annotation.Nullable Boolean enableMpegtsM2TsMode,
            @org.eclipse.jdt.annotation.Nullable String videoCodec,
            @org.eclipse.jdt.annotation.Nullable String subtitleCodec,
            @org.eclipse.jdt.annotation.Nullable String transcodeReasons,
            @org.eclipse.jdt.annotation.Nullable Integer audioStreamIndex,
            @org.eclipse.jdt.annotation.Nullable Integer videoStreamIndex,
            @org.eclipse.jdt.annotation.Nullable EncodingContext context,
            @org.eclipse.jdt.annotation.Nullable Map<String, String> streamOptions,
            @org.eclipse.jdt.annotation.Nullable Boolean enableAudioVbrEncoding, Map<String, String> headers)
            throws ApiException {
        ApiResponse<File> localVarResponse = headVideoStreamByContainerWithHttpInfo(itemId, container, _static, params,
                tag, deviceProfileId, playSessionId, segmentContainer, segmentLength, minSegments, mediaSourceId,
                deviceId, audioCodec, enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy,
                breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth, audioBitRate, audioChannels, maxAudioChannels,
                profile, level, framerate, maxFramerate, copyTimestamps, startTimeTicks, width, height, maxWidth,
                maxHeight, videoBitRate, subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth,
                requireAvc, deInterlace, requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId,
                enableMpegtsM2TsMode, videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex,
                context, streamOptions, enableAudioVbrEncoding, headers);
        return localVarResponse.getData();
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
     */
    public ApiResponse<File> headVideoStreamByContainerWithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull String container, @org.eclipse.jdt.annotation.Nullable Boolean _static,
            @org.eclipse.jdt.annotation.Nullable String params, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable String deviceProfileId,
            @org.eclipse.jdt.annotation.Nullable String playSessionId,
            @org.eclipse.jdt.annotation.Nullable String segmentContainer,
            @org.eclipse.jdt.annotation.Nullable Integer segmentLength,
            @org.eclipse.jdt.annotation.Nullable Integer minSegments,
            @org.eclipse.jdt.annotation.Nullable String mediaSourceId,
            @org.eclipse.jdt.annotation.Nullable String deviceId,
            @org.eclipse.jdt.annotation.Nullable String audioCodec,
            @org.eclipse.jdt.annotation.Nullable Boolean enableAutoStreamCopy,
            @org.eclipse.jdt.annotation.Nullable Boolean allowVideoStreamCopy,
            @org.eclipse.jdt.annotation.Nullable Boolean allowAudioStreamCopy,
            @org.eclipse.jdt.annotation.Nullable Boolean breakOnNonKeyFrames,
            @org.eclipse.jdt.annotation.Nullable Integer audioSampleRate,
            @org.eclipse.jdt.annotation.Nullable Integer maxAudioBitDepth,
            @org.eclipse.jdt.annotation.Nullable Integer audioBitRate,
            @org.eclipse.jdt.annotation.Nullable Integer audioChannels,
            @org.eclipse.jdt.annotation.Nullable Integer maxAudioChannels,
            @org.eclipse.jdt.annotation.Nullable String profile, @org.eclipse.jdt.annotation.Nullable String level,
            @org.eclipse.jdt.annotation.Nullable Float framerate,
            @org.eclipse.jdt.annotation.Nullable Float maxFramerate,
            @org.eclipse.jdt.annotation.Nullable Boolean copyTimestamps,
            @org.eclipse.jdt.annotation.Nullable Long startTimeTicks,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Integer videoBitRate,
            @org.eclipse.jdt.annotation.Nullable Integer subtitleStreamIndex,
            @org.eclipse.jdt.annotation.Nullable SubtitleDeliveryMethod subtitleMethod,
            @org.eclipse.jdt.annotation.Nullable Integer maxRefFrames,
            @org.eclipse.jdt.annotation.Nullable Integer maxVideoBitDepth,
            @org.eclipse.jdt.annotation.Nullable Boolean requireAvc,
            @org.eclipse.jdt.annotation.Nullable Boolean deInterlace,
            @org.eclipse.jdt.annotation.Nullable Boolean requireNonAnamorphic,
            @org.eclipse.jdt.annotation.Nullable Integer transcodingMaxAudioChannels,
            @org.eclipse.jdt.annotation.Nullable Integer cpuCoreLimit,
            @org.eclipse.jdt.annotation.Nullable String liveStreamId,
            @org.eclipse.jdt.annotation.Nullable Boolean enableMpegtsM2TsMode,
            @org.eclipse.jdt.annotation.Nullable String videoCodec,
            @org.eclipse.jdt.annotation.Nullable String subtitleCodec,
            @org.eclipse.jdt.annotation.Nullable String transcodeReasons,
            @org.eclipse.jdt.annotation.Nullable Integer audioStreamIndex,
            @org.eclipse.jdt.annotation.Nullable Integer videoStreamIndex,
            @org.eclipse.jdt.annotation.Nullable EncodingContext context,
            @org.eclipse.jdt.annotation.Nullable Map<String, String> streamOptions,
            @org.eclipse.jdt.annotation.Nullable Boolean enableAudioVbrEncoding) throws ApiException {
        return headVideoStreamByContainerWithHttpInfo(itemId, container, _static, params, tag, deviceProfileId,
                playSessionId, segmentContainer, segmentLength, minSegments, mediaSourceId, deviceId, audioCodec,
                enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy, breakOnNonKeyFrames, audioSampleRate,
                maxAudioBitDepth, audioBitRate, audioChannels, maxAudioChannels, profile, level, framerate,
                maxFramerate, copyTimestamps, startTimeTicks, width, height, maxWidth, maxHeight, videoBitRate,
                subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth, requireAvc, deInterlace,
                requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId, enableMpegtsM2TsMode,
                videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex, context, streamOptions,
                enableAudioVbrEncoding, null);
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
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> headVideoStreamByContainerWithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull String container, @org.eclipse.jdt.annotation.Nullable Boolean _static,
            @org.eclipse.jdt.annotation.Nullable String params, @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable String deviceProfileId,
            @org.eclipse.jdt.annotation.Nullable String playSessionId,
            @org.eclipse.jdt.annotation.Nullable String segmentContainer,
            @org.eclipse.jdt.annotation.Nullable Integer segmentLength,
            @org.eclipse.jdt.annotation.Nullable Integer minSegments,
            @org.eclipse.jdt.annotation.Nullable String mediaSourceId,
            @org.eclipse.jdt.annotation.Nullable String deviceId,
            @org.eclipse.jdt.annotation.Nullable String audioCodec,
            @org.eclipse.jdt.annotation.Nullable Boolean enableAutoStreamCopy,
            @org.eclipse.jdt.annotation.Nullable Boolean allowVideoStreamCopy,
            @org.eclipse.jdt.annotation.Nullable Boolean allowAudioStreamCopy,
            @org.eclipse.jdt.annotation.Nullable Boolean breakOnNonKeyFrames,
            @org.eclipse.jdt.annotation.Nullable Integer audioSampleRate,
            @org.eclipse.jdt.annotation.Nullable Integer maxAudioBitDepth,
            @org.eclipse.jdt.annotation.Nullable Integer audioBitRate,
            @org.eclipse.jdt.annotation.Nullable Integer audioChannels,
            @org.eclipse.jdt.annotation.Nullable Integer maxAudioChannels,
            @org.eclipse.jdt.annotation.Nullable String profile, @org.eclipse.jdt.annotation.Nullable String level,
            @org.eclipse.jdt.annotation.Nullable Float framerate,
            @org.eclipse.jdt.annotation.Nullable Float maxFramerate,
            @org.eclipse.jdt.annotation.Nullable Boolean copyTimestamps,
            @org.eclipse.jdt.annotation.Nullable Long startTimeTicks,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Integer videoBitRate,
            @org.eclipse.jdt.annotation.Nullable Integer subtitleStreamIndex,
            @org.eclipse.jdt.annotation.Nullable SubtitleDeliveryMethod subtitleMethod,
            @org.eclipse.jdt.annotation.Nullable Integer maxRefFrames,
            @org.eclipse.jdt.annotation.Nullable Integer maxVideoBitDepth,
            @org.eclipse.jdt.annotation.Nullable Boolean requireAvc,
            @org.eclipse.jdt.annotation.Nullable Boolean deInterlace,
            @org.eclipse.jdt.annotation.Nullable Boolean requireNonAnamorphic,
            @org.eclipse.jdt.annotation.Nullable Integer transcodingMaxAudioChannels,
            @org.eclipse.jdt.annotation.Nullable Integer cpuCoreLimit,
            @org.eclipse.jdt.annotation.Nullable String liveStreamId,
            @org.eclipse.jdt.annotation.Nullable Boolean enableMpegtsM2TsMode,
            @org.eclipse.jdt.annotation.Nullable String videoCodec,
            @org.eclipse.jdt.annotation.Nullable String subtitleCodec,
            @org.eclipse.jdt.annotation.Nullable String transcodeReasons,
            @org.eclipse.jdt.annotation.Nullable Integer audioStreamIndex,
            @org.eclipse.jdt.annotation.Nullable Integer videoStreamIndex,
            @org.eclipse.jdt.annotation.Nullable EncodingContext context,
            @org.eclipse.jdt.annotation.Nullable Map<String, String> streamOptions,
            @org.eclipse.jdt.annotation.Nullable Boolean enableAudioVbrEncoding, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = headVideoStreamByContainerRequestBuilder(itemId, container,
                _static, params, tag, deviceProfileId, playSessionId, segmentContainer, segmentLength, minSegments,
                mediaSourceId, deviceId, audioCodec, enableAutoStreamCopy, allowVideoStreamCopy, allowAudioStreamCopy,
                breakOnNonKeyFrames, audioSampleRate, maxAudioBitDepth, audioBitRate, audioChannels, maxAudioChannels,
                profile, level, framerate, maxFramerate, copyTimestamps, startTimeTicks, width, height, maxWidth,
                maxHeight, videoBitRate, subtitleStreamIndex, subtitleMethod, maxRefFrames, maxVideoBitDepth,
                requireAvc, deInterlace, requireNonAnamorphic, transcodingMaxAudioChannels, cpuCoreLimit, liveStreamId,
                enableMpegtsM2TsMode, videoCodec, subtitleCodec, transcodeReasons, audioStreamIndex, videoStreamIndex,
                context, streamOptions, enableAudioVbrEncoding, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("headVideoStreamByContainer", localVarResponse);
                }
                localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
                if (localVarResponseBody == null) {
                    return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
                }

                // Handle file downloading.
                File responseValue = downloadFileFromResponse(localVarResponse, localVarResponseBody);

                return new ApiResponse<File>(localVarResponse.statusCode(), localVarResponse.headers().map(),
                        responseValue);
            } finally {
                if (localVarResponseBody != null) {
                    localVarResponseBody.close();
                }
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder headVideoStreamByContainerRequestBuilder(
            @org.eclipse.jdt.annotation.NonNull UUID itemId, @org.eclipse.jdt.annotation.NonNull String container,
            @org.eclipse.jdt.annotation.Nullable Boolean _static, @org.eclipse.jdt.annotation.Nullable String params,
            @org.eclipse.jdt.annotation.Nullable String tag,
            @org.eclipse.jdt.annotation.Nullable String deviceProfileId,
            @org.eclipse.jdt.annotation.Nullable String playSessionId,
            @org.eclipse.jdt.annotation.Nullable String segmentContainer,
            @org.eclipse.jdt.annotation.Nullable Integer segmentLength,
            @org.eclipse.jdt.annotation.Nullable Integer minSegments,
            @org.eclipse.jdt.annotation.Nullable String mediaSourceId,
            @org.eclipse.jdt.annotation.Nullable String deviceId,
            @org.eclipse.jdt.annotation.Nullable String audioCodec,
            @org.eclipse.jdt.annotation.Nullable Boolean enableAutoStreamCopy,
            @org.eclipse.jdt.annotation.Nullable Boolean allowVideoStreamCopy,
            @org.eclipse.jdt.annotation.Nullable Boolean allowAudioStreamCopy,
            @org.eclipse.jdt.annotation.Nullable Boolean breakOnNonKeyFrames,
            @org.eclipse.jdt.annotation.Nullable Integer audioSampleRate,
            @org.eclipse.jdt.annotation.Nullable Integer maxAudioBitDepth,
            @org.eclipse.jdt.annotation.Nullable Integer audioBitRate,
            @org.eclipse.jdt.annotation.Nullable Integer audioChannels,
            @org.eclipse.jdt.annotation.Nullable Integer maxAudioChannels,
            @org.eclipse.jdt.annotation.Nullable String profile, @org.eclipse.jdt.annotation.Nullable String level,
            @org.eclipse.jdt.annotation.Nullable Float framerate,
            @org.eclipse.jdt.annotation.Nullable Float maxFramerate,
            @org.eclipse.jdt.annotation.Nullable Boolean copyTimestamps,
            @org.eclipse.jdt.annotation.Nullable Long startTimeTicks,
            @org.eclipse.jdt.annotation.Nullable Integer width, @org.eclipse.jdt.annotation.Nullable Integer height,
            @org.eclipse.jdt.annotation.Nullable Integer maxWidth,
            @org.eclipse.jdt.annotation.Nullable Integer maxHeight,
            @org.eclipse.jdt.annotation.Nullable Integer videoBitRate,
            @org.eclipse.jdt.annotation.Nullable Integer subtitleStreamIndex,
            @org.eclipse.jdt.annotation.Nullable SubtitleDeliveryMethod subtitleMethod,
            @org.eclipse.jdt.annotation.Nullable Integer maxRefFrames,
            @org.eclipse.jdt.annotation.Nullable Integer maxVideoBitDepth,
            @org.eclipse.jdt.annotation.Nullable Boolean requireAvc,
            @org.eclipse.jdt.annotation.Nullable Boolean deInterlace,
            @org.eclipse.jdt.annotation.Nullable Boolean requireNonAnamorphic,
            @org.eclipse.jdt.annotation.Nullable Integer transcodingMaxAudioChannels,
            @org.eclipse.jdt.annotation.Nullable Integer cpuCoreLimit,
            @org.eclipse.jdt.annotation.Nullable String liveStreamId,
            @org.eclipse.jdt.annotation.Nullable Boolean enableMpegtsM2TsMode,
            @org.eclipse.jdt.annotation.Nullable String videoCodec,
            @org.eclipse.jdt.annotation.Nullable String subtitleCodec,
            @org.eclipse.jdt.annotation.Nullable String transcodeReasons,
            @org.eclipse.jdt.annotation.Nullable Integer audioStreamIndex,
            @org.eclipse.jdt.annotation.Nullable Integer videoStreamIndex,
            @org.eclipse.jdt.annotation.Nullable EncodingContext context,
            @org.eclipse.jdt.annotation.Nullable Map<String, String> streamOptions,
            @org.eclipse.jdt.annotation.Nullable Boolean enableAudioVbrEncoding, Map<String, String> headers)
            throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'itemId' when calling headVideoStreamByContainer");
        }
        // verify the required parameter 'container' is set
        if (container == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'container' when calling headVideoStreamByContainer");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Videos/{itemId}/stream.{container}"
                .replace("{itemId}", ApiClient.urlEncode(itemId.toString()))
                .replace("{container}", ApiClient.urlEncode(container.toString()));

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

        localVarRequestBuilder.header("Accept", "video/*, text/html");

        localVarRequestBuilder.method("HEAD", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        // Add custom headers if provided
        localVarRequestBuilder = HttpRequestBuilderExtensions.withAdditionalHeaders(localVarRequestBuilder, headers);
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }

    /**
     * Merges videos into a single record.
     * 
     * @param ids Item id list. This allows multiple, comma delimited. (required)
     * @throws ApiException if fails to make API call
     */
    public void mergeVersions(@org.eclipse.jdt.annotation.NonNull List<UUID> ids) throws ApiException {
        mergeVersions(ids, null);
    }

    /**
     * Merges videos into a single record.
     * 
     * @param ids Item id list. This allows multiple, comma delimited. (required)
     * @param headers Optional headers to include in the request
     * @throws ApiException if fails to make API call
     */
    public void mergeVersions(@org.eclipse.jdt.annotation.NonNull List<UUID> ids, Map<String, String> headers)
            throws ApiException {
        mergeVersionsWithHttpInfo(ids, headers);
    }

    /**
     * Merges videos into a single record.
     * 
     * @param ids Item id list. This allows multiple, comma delimited. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> mergeVersionsWithHttpInfo(@org.eclipse.jdt.annotation.NonNull List<UUID> ids)
            throws ApiException {
        return mergeVersionsWithHttpInfo(ids, null);
    }

    /**
     * Merges videos into a single record.
     * 
     * @param ids Item id list. This allows multiple, comma delimited. (required)
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Void> mergeVersionsWithHttpInfo(@org.eclipse.jdt.annotation.NonNull List<UUID> ids,
            Map<String, String> headers) throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = mergeVersionsRequestBuilder(ids, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("mergeVersions", localVarResponse);
                }
                localVarResponseBody = ApiClient.getResponseBody(localVarResponse);
                if (localVarResponseBody != null) {
                    localVarResponseBody.readAllBytes();
                }
                return new ApiResponse<>(localVarResponse.statusCode(), localVarResponse.headers().map(), null);
            } finally {
                if (localVarResponseBody != null) {
                    localVarResponseBody.close();
                }
            }
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e);
        }
    }

    private HttpRequest.Builder mergeVersionsRequestBuilder(@org.eclipse.jdt.annotation.NonNull List<UUID> ids,
            Map<String, String> headers) throws ApiException {
        // verify the required parameter 'ids' is set
        if (ids == null) {
            throw new ApiException(400, "Missing the required parameter 'ids' when calling mergeVersions");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Videos/MergeVersions";

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "ids";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "ids", ids));

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

        localVarRequestBuilder.header("Accept",
                "application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

        localVarRequestBuilder.method("POST", HttpRequest.BodyPublishers.noBody());
        if (memberVarReadTimeout != null) {
            localVarRequestBuilder.timeout(memberVarReadTimeout);
        }
        // Add custom headers if provided
        localVarRequestBuilder = HttpRequestBuilderExtensions.withAdditionalHeaders(localVarRequestBuilder, headers);
        if (memberVarInterceptor != null) {
            memberVarInterceptor.accept(localVarRequestBuilder);
        }
        return localVarRequestBuilder;
    }
}
