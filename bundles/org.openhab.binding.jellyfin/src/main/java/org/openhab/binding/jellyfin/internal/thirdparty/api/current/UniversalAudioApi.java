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
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.MediaStreamProtocol;

import com.fasterxml.jackson.databind.ObjectMapper;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class UniversalAudioApi {
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

    public UniversalAudioApi() {
        this(Configuration.getDefaultApiClient());
    }

    public UniversalAudioApi(ApiClient apiClient) {
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
     */
    public File getUniversalAudioStream(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.Nullable List<String> container,
            @org.eclipse.jdt.annotation.Nullable String mediaSourceId,
            @org.eclipse.jdt.annotation.Nullable String deviceId, @org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable String audioCodec,
            @org.eclipse.jdt.annotation.Nullable Integer maxAudioChannels,
            @org.eclipse.jdt.annotation.Nullable Integer transcodingAudioChannels,
            @org.eclipse.jdt.annotation.Nullable Integer maxStreamingBitrate,
            @org.eclipse.jdt.annotation.Nullable Integer audioBitRate,
            @org.eclipse.jdt.annotation.Nullable Long startTimeTicks,
            @org.eclipse.jdt.annotation.Nullable String transcodingContainer,
            @org.eclipse.jdt.annotation.Nullable MediaStreamProtocol transcodingProtocol,
            @org.eclipse.jdt.annotation.Nullable Integer maxAudioSampleRate,
            @org.eclipse.jdt.annotation.Nullable Integer maxAudioBitDepth,
            @org.eclipse.jdt.annotation.Nullable Boolean enableRemoteMedia,
            @org.eclipse.jdt.annotation.Nullable Boolean enableAudioVbrEncoding,
            @org.eclipse.jdt.annotation.Nullable Boolean breakOnNonKeyFrames,
            @org.eclipse.jdt.annotation.Nullable Boolean enableRedirection) throws ApiException {
        return getUniversalAudioStream(itemId, container, mediaSourceId, deviceId, userId, audioCodec, maxAudioChannels,
                transcodingAudioChannels, maxStreamingBitrate, audioBitRate, startTimeTicks, transcodingContainer,
                transcodingProtocol, maxAudioSampleRate, maxAudioBitDepth, enableRemoteMedia, enableAudioVbrEncoding,
                breakOnNonKeyFrames, enableRedirection, null);
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
     * @param headers Optional headers to include in the request
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File getUniversalAudioStream(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.Nullable List<String> container,
            @org.eclipse.jdt.annotation.Nullable String mediaSourceId,
            @org.eclipse.jdt.annotation.Nullable String deviceId, @org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable String audioCodec,
            @org.eclipse.jdt.annotation.Nullable Integer maxAudioChannels,
            @org.eclipse.jdt.annotation.Nullable Integer transcodingAudioChannels,
            @org.eclipse.jdt.annotation.Nullable Integer maxStreamingBitrate,
            @org.eclipse.jdt.annotation.Nullable Integer audioBitRate,
            @org.eclipse.jdt.annotation.Nullable Long startTimeTicks,
            @org.eclipse.jdt.annotation.Nullable String transcodingContainer,
            @org.eclipse.jdt.annotation.Nullable MediaStreamProtocol transcodingProtocol,
            @org.eclipse.jdt.annotation.Nullable Integer maxAudioSampleRate,
            @org.eclipse.jdt.annotation.Nullable Integer maxAudioBitDepth,
            @org.eclipse.jdt.annotation.Nullable Boolean enableRemoteMedia,
            @org.eclipse.jdt.annotation.Nullable Boolean enableAudioVbrEncoding,
            @org.eclipse.jdt.annotation.Nullable Boolean breakOnNonKeyFrames,
            @org.eclipse.jdt.annotation.Nullable Boolean enableRedirection, Map<String, String> headers)
            throws ApiException {
        ApiResponse<File> localVarResponse = getUniversalAudioStreamWithHttpInfo(itemId, container, mediaSourceId,
                deviceId, userId, audioCodec, maxAudioChannels, transcodingAudioChannels, maxStreamingBitrate,
                audioBitRate, startTimeTicks, transcodingContainer, transcodingProtocol, maxAudioSampleRate,
                maxAudioBitDepth, enableRemoteMedia, enableAudioVbrEncoding, breakOnNonKeyFrames, enableRedirection,
                headers);
        return localVarResponse.getData();
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
     */
    public ApiResponse<File> getUniversalAudioStreamWithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.Nullable List<String> container,
            @org.eclipse.jdt.annotation.Nullable String mediaSourceId,
            @org.eclipse.jdt.annotation.Nullable String deviceId, @org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable String audioCodec,
            @org.eclipse.jdt.annotation.Nullable Integer maxAudioChannels,
            @org.eclipse.jdt.annotation.Nullable Integer transcodingAudioChannels,
            @org.eclipse.jdt.annotation.Nullable Integer maxStreamingBitrate,
            @org.eclipse.jdt.annotation.Nullable Integer audioBitRate,
            @org.eclipse.jdt.annotation.Nullable Long startTimeTicks,
            @org.eclipse.jdt.annotation.Nullable String transcodingContainer,
            @org.eclipse.jdt.annotation.Nullable MediaStreamProtocol transcodingProtocol,
            @org.eclipse.jdt.annotation.Nullable Integer maxAudioSampleRate,
            @org.eclipse.jdt.annotation.Nullable Integer maxAudioBitDepth,
            @org.eclipse.jdt.annotation.Nullable Boolean enableRemoteMedia,
            @org.eclipse.jdt.annotation.Nullable Boolean enableAudioVbrEncoding,
            @org.eclipse.jdt.annotation.Nullable Boolean breakOnNonKeyFrames,
            @org.eclipse.jdt.annotation.Nullable Boolean enableRedirection) throws ApiException {
        return getUniversalAudioStreamWithHttpInfo(itemId, container, mediaSourceId, deviceId, userId, audioCodec,
                maxAudioChannels, transcodingAudioChannels, maxStreamingBitrate, audioBitRate, startTimeTicks,
                transcodingContainer, transcodingProtocol, maxAudioSampleRate, maxAudioBitDepth, enableRemoteMedia,
                enableAudioVbrEncoding, breakOnNonKeyFrames, enableRedirection, null);
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
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> getUniversalAudioStreamWithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.Nullable List<String> container,
            @org.eclipse.jdt.annotation.Nullable String mediaSourceId,
            @org.eclipse.jdt.annotation.Nullable String deviceId, @org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable String audioCodec,
            @org.eclipse.jdt.annotation.Nullable Integer maxAudioChannels,
            @org.eclipse.jdt.annotation.Nullable Integer transcodingAudioChannels,
            @org.eclipse.jdt.annotation.Nullable Integer maxStreamingBitrate,
            @org.eclipse.jdt.annotation.Nullable Integer audioBitRate,
            @org.eclipse.jdt.annotation.Nullable Long startTimeTicks,
            @org.eclipse.jdt.annotation.Nullable String transcodingContainer,
            @org.eclipse.jdt.annotation.Nullable MediaStreamProtocol transcodingProtocol,
            @org.eclipse.jdt.annotation.Nullable Integer maxAudioSampleRate,
            @org.eclipse.jdt.annotation.Nullable Integer maxAudioBitDepth,
            @org.eclipse.jdt.annotation.Nullable Boolean enableRemoteMedia,
            @org.eclipse.jdt.annotation.Nullable Boolean enableAudioVbrEncoding,
            @org.eclipse.jdt.annotation.Nullable Boolean breakOnNonKeyFrames,
            @org.eclipse.jdt.annotation.Nullable Boolean enableRedirection, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = getUniversalAudioStreamRequestBuilder(itemId, container,
                mediaSourceId, deviceId, userId, audioCodec, maxAudioChannels, transcodingAudioChannels,
                maxStreamingBitrate, audioBitRate, startTimeTicks, transcodingContainer, transcodingProtocol,
                maxAudioSampleRate, maxAudioBitDepth, enableRemoteMedia, enableAudioVbrEncoding, breakOnNonKeyFrames,
                enableRedirection, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("getUniversalAudioStream", localVarResponse);
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

    private HttpRequest.Builder getUniversalAudioStreamRequestBuilder(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.Nullable List<String> container,
            @org.eclipse.jdt.annotation.Nullable String mediaSourceId,
            @org.eclipse.jdt.annotation.Nullable String deviceId, @org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable String audioCodec,
            @org.eclipse.jdt.annotation.Nullable Integer maxAudioChannels,
            @org.eclipse.jdt.annotation.Nullable Integer transcodingAudioChannels,
            @org.eclipse.jdt.annotation.Nullable Integer maxStreamingBitrate,
            @org.eclipse.jdt.annotation.Nullable Integer audioBitRate,
            @org.eclipse.jdt.annotation.Nullable Long startTimeTicks,
            @org.eclipse.jdt.annotation.Nullable String transcodingContainer,
            @org.eclipse.jdt.annotation.Nullable MediaStreamProtocol transcodingProtocol,
            @org.eclipse.jdt.annotation.Nullable Integer maxAudioSampleRate,
            @org.eclipse.jdt.annotation.Nullable Integer maxAudioBitDepth,
            @org.eclipse.jdt.annotation.Nullable Boolean enableRemoteMedia,
            @org.eclipse.jdt.annotation.Nullable Boolean enableAudioVbrEncoding,
            @org.eclipse.jdt.annotation.Nullable Boolean breakOnNonKeyFrames,
            @org.eclipse.jdt.annotation.Nullable Boolean enableRedirection, Map<String, String> headers)
            throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling getUniversalAudioStream");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Audio/{itemId}/universal".replace("{itemId}", ApiClient.urlEncode(itemId.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "container";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "container", container));
        localVarQueryParameterBaseName = "mediaSourceId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("mediaSourceId", mediaSourceId));
        localVarQueryParameterBaseName = "deviceId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("deviceId", deviceId));
        localVarQueryParameterBaseName = "userId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("userId", userId));
        localVarQueryParameterBaseName = "audioCodec";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("audioCodec", audioCodec));
        localVarQueryParameterBaseName = "maxAudioChannels";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxAudioChannels", maxAudioChannels));
        localVarQueryParameterBaseName = "transcodingAudioChannels";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("transcodingAudioChannels", transcodingAudioChannels));
        localVarQueryParameterBaseName = "maxStreamingBitrate";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxStreamingBitrate", maxStreamingBitrate));
        localVarQueryParameterBaseName = "audioBitRate";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("audioBitRate", audioBitRate));
        localVarQueryParameterBaseName = "startTimeTicks";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("startTimeTicks", startTimeTicks));
        localVarQueryParameterBaseName = "transcodingContainer";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("transcodingContainer", transcodingContainer));
        localVarQueryParameterBaseName = "transcodingProtocol";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("transcodingProtocol", transcodingProtocol));
        localVarQueryParameterBaseName = "maxAudioSampleRate";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxAudioSampleRate", maxAudioSampleRate));
        localVarQueryParameterBaseName = "maxAudioBitDepth";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxAudioBitDepth", maxAudioBitDepth));
        localVarQueryParameterBaseName = "enableRemoteMedia";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("enableRemoteMedia", enableRemoteMedia));
        localVarQueryParameterBaseName = "enableAudioVbrEncoding";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("enableAudioVbrEncoding", enableAudioVbrEncoding));
        localVarQueryParameterBaseName = "breakOnNonKeyFrames";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("breakOnNonKeyFrames", breakOnNonKeyFrames));
        localVarQueryParameterBaseName = "enableRedirection";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("enableRedirection", enableRedirection));

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
                "audio/*, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

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
     */
    public File headUniversalAudioStream(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.Nullable List<String> container,
            @org.eclipse.jdt.annotation.Nullable String mediaSourceId,
            @org.eclipse.jdt.annotation.Nullable String deviceId, @org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable String audioCodec,
            @org.eclipse.jdt.annotation.Nullable Integer maxAudioChannels,
            @org.eclipse.jdt.annotation.Nullable Integer transcodingAudioChannels,
            @org.eclipse.jdt.annotation.Nullable Integer maxStreamingBitrate,
            @org.eclipse.jdt.annotation.Nullable Integer audioBitRate,
            @org.eclipse.jdt.annotation.Nullable Long startTimeTicks,
            @org.eclipse.jdt.annotation.Nullable String transcodingContainer,
            @org.eclipse.jdt.annotation.Nullable MediaStreamProtocol transcodingProtocol,
            @org.eclipse.jdt.annotation.Nullable Integer maxAudioSampleRate,
            @org.eclipse.jdt.annotation.Nullable Integer maxAudioBitDepth,
            @org.eclipse.jdt.annotation.Nullable Boolean enableRemoteMedia,
            @org.eclipse.jdt.annotation.Nullable Boolean enableAudioVbrEncoding,
            @org.eclipse.jdt.annotation.Nullable Boolean breakOnNonKeyFrames,
            @org.eclipse.jdt.annotation.Nullable Boolean enableRedirection) throws ApiException {
        return headUniversalAudioStream(itemId, container, mediaSourceId, deviceId, userId, audioCodec,
                maxAudioChannels, transcodingAudioChannels, maxStreamingBitrate, audioBitRate, startTimeTicks,
                transcodingContainer, transcodingProtocol, maxAudioSampleRate, maxAudioBitDepth, enableRemoteMedia,
                enableAudioVbrEncoding, breakOnNonKeyFrames, enableRedirection, null);
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
     * @param headers Optional headers to include in the request
     * @return File
     * @throws ApiException if fails to make API call
     */
    public File headUniversalAudioStream(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.Nullable List<String> container,
            @org.eclipse.jdt.annotation.Nullable String mediaSourceId,
            @org.eclipse.jdt.annotation.Nullable String deviceId, @org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable String audioCodec,
            @org.eclipse.jdt.annotation.Nullable Integer maxAudioChannels,
            @org.eclipse.jdt.annotation.Nullable Integer transcodingAudioChannels,
            @org.eclipse.jdt.annotation.Nullable Integer maxStreamingBitrate,
            @org.eclipse.jdt.annotation.Nullable Integer audioBitRate,
            @org.eclipse.jdt.annotation.Nullable Long startTimeTicks,
            @org.eclipse.jdt.annotation.Nullable String transcodingContainer,
            @org.eclipse.jdt.annotation.Nullable MediaStreamProtocol transcodingProtocol,
            @org.eclipse.jdt.annotation.Nullable Integer maxAudioSampleRate,
            @org.eclipse.jdt.annotation.Nullable Integer maxAudioBitDepth,
            @org.eclipse.jdt.annotation.Nullable Boolean enableRemoteMedia,
            @org.eclipse.jdt.annotation.Nullable Boolean enableAudioVbrEncoding,
            @org.eclipse.jdt.annotation.Nullable Boolean breakOnNonKeyFrames,
            @org.eclipse.jdt.annotation.Nullable Boolean enableRedirection, Map<String, String> headers)
            throws ApiException {
        ApiResponse<File> localVarResponse = headUniversalAudioStreamWithHttpInfo(itemId, container, mediaSourceId,
                deviceId, userId, audioCodec, maxAudioChannels, transcodingAudioChannels, maxStreamingBitrate,
                audioBitRate, startTimeTicks, transcodingContainer, transcodingProtocol, maxAudioSampleRate,
                maxAudioBitDepth, enableRemoteMedia, enableAudioVbrEncoding, breakOnNonKeyFrames, enableRedirection,
                headers);
        return localVarResponse.getData();
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
     */
    public ApiResponse<File> headUniversalAudioStreamWithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.Nullable List<String> container,
            @org.eclipse.jdt.annotation.Nullable String mediaSourceId,
            @org.eclipse.jdt.annotation.Nullable String deviceId, @org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable String audioCodec,
            @org.eclipse.jdt.annotation.Nullable Integer maxAudioChannels,
            @org.eclipse.jdt.annotation.Nullable Integer transcodingAudioChannels,
            @org.eclipse.jdt.annotation.Nullable Integer maxStreamingBitrate,
            @org.eclipse.jdt.annotation.Nullable Integer audioBitRate,
            @org.eclipse.jdt.annotation.Nullable Long startTimeTicks,
            @org.eclipse.jdt.annotation.Nullable String transcodingContainer,
            @org.eclipse.jdt.annotation.Nullable MediaStreamProtocol transcodingProtocol,
            @org.eclipse.jdt.annotation.Nullable Integer maxAudioSampleRate,
            @org.eclipse.jdt.annotation.Nullable Integer maxAudioBitDepth,
            @org.eclipse.jdt.annotation.Nullable Boolean enableRemoteMedia,
            @org.eclipse.jdt.annotation.Nullable Boolean enableAudioVbrEncoding,
            @org.eclipse.jdt.annotation.Nullable Boolean breakOnNonKeyFrames,
            @org.eclipse.jdt.annotation.Nullable Boolean enableRedirection) throws ApiException {
        return headUniversalAudioStreamWithHttpInfo(itemId, container, mediaSourceId, deviceId, userId, audioCodec,
                maxAudioChannels, transcodingAudioChannels, maxStreamingBitrate, audioBitRate, startTimeTicks,
                transcodingContainer, transcodingProtocol, maxAudioSampleRate, maxAudioBitDepth, enableRemoteMedia,
                enableAudioVbrEncoding, breakOnNonKeyFrames, enableRedirection, null);
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
     * @param headers Optional headers to include in the request
     * @return ApiResponse&lt;File&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<File> headUniversalAudioStreamWithHttpInfo(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.Nullable List<String> container,
            @org.eclipse.jdt.annotation.Nullable String mediaSourceId,
            @org.eclipse.jdt.annotation.Nullable String deviceId, @org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable String audioCodec,
            @org.eclipse.jdt.annotation.Nullable Integer maxAudioChannels,
            @org.eclipse.jdt.annotation.Nullable Integer transcodingAudioChannels,
            @org.eclipse.jdt.annotation.Nullable Integer maxStreamingBitrate,
            @org.eclipse.jdt.annotation.Nullable Integer audioBitRate,
            @org.eclipse.jdt.annotation.Nullable Long startTimeTicks,
            @org.eclipse.jdt.annotation.Nullable String transcodingContainer,
            @org.eclipse.jdt.annotation.Nullable MediaStreamProtocol transcodingProtocol,
            @org.eclipse.jdt.annotation.Nullable Integer maxAudioSampleRate,
            @org.eclipse.jdt.annotation.Nullable Integer maxAudioBitDepth,
            @org.eclipse.jdt.annotation.Nullable Boolean enableRemoteMedia,
            @org.eclipse.jdt.annotation.Nullable Boolean enableAudioVbrEncoding,
            @org.eclipse.jdt.annotation.Nullable Boolean breakOnNonKeyFrames,
            @org.eclipse.jdt.annotation.Nullable Boolean enableRedirection, Map<String, String> headers)
            throws ApiException {
        HttpRequest.Builder localVarRequestBuilder = headUniversalAudioStreamRequestBuilder(itemId, container,
                mediaSourceId, deviceId, userId, audioCodec, maxAudioChannels, transcodingAudioChannels,
                maxStreamingBitrate, audioBitRate, startTimeTicks, transcodingContainer, transcodingProtocol,
                maxAudioSampleRate, maxAudioBitDepth, enableRemoteMedia, enableAudioVbrEncoding, breakOnNonKeyFrames,
                enableRedirection, headers);
        try {
            HttpResponse<InputStream> localVarResponse = memberVarHttpClient.send(localVarRequestBuilder.build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            if (memberVarResponseInterceptor != null) {
                memberVarResponseInterceptor.accept(localVarResponse);
            }
            InputStream localVarResponseBody = null;
            try {
                if (localVarResponse.statusCode() / 100 != 2) {
                    throw getApiException("headUniversalAudioStream", localVarResponse);
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

    private HttpRequest.Builder headUniversalAudioStreamRequestBuilder(@org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.Nullable List<String> container,
            @org.eclipse.jdt.annotation.Nullable String mediaSourceId,
            @org.eclipse.jdt.annotation.Nullable String deviceId, @org.eclipse.jdt.annotation.Nullable UUID userId,
            @org.eclipse.jdt.annotation.Nullable String audioCodec,
            @org.eclipse.jdt.annotation.Nullable Integer maxAudioChannels,
            @org.eclipse.jdt.annotation.Nullable Integer transcodingAudioChannels,
            @org.eclipse.jdt.annotation.Nullable Integer maxStreamingBitrate,
            @org.eclipse.jdt.annotation.Nullable Integer audioBitRate,
            @org.eclipse.jdt.annotation.Nullable Long startTimeTicks,
            @org.eclipse.jdt.annotation.Nullable String transcodingContainer,
            @org.eclipse.jdt.annotation.Nullable MediaStreamProtocol transcodingProtocol,
            @org.eclipse.jdt.annotation.Nullable Integer maxAudioSampleRate,
            @org.eclipse.jdt.annotation.Nullable Integer maxAudioBitDepth,
            @org.eclipse.jdt.annotation.Nullable Boolean enableRemoteMedia,
            @org.eclipse.jdt.annotation.Nullable Boolean enableAudioVbrEncoding,
            @org.eclipse.jdt.annotation.Nullable Boolean breakOnNonKeyFrames,
            @org.eclipse.jdt.annotation.Nullable Boolean enableRedirection, Map<String, String> headers)
            throws ApiException {
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'itemId' when calling headUniversalAudioStream");
        }

        HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

        String localVarPath = "/Audio/{itemId}/universal".replace("{itemId}", ApiClient.urlEncode(itemId.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>();
        StringJoiner localVarQueryStringJoiner = new StringJoiner("&");
        String localVarQueryParameterBaseName;
        localVarQueryParameterBaseName = "container";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("multi", "container", container));
        localVarQueryParameterBaseName = "mediaSourceId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("mediaSourceId", mediaSourceId));
        localVarQueryParameterBaseName = "deviceId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("deviceId", deviceId));
        localVarQueryParameterBaseName = "userId";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("userId", userId));
        localVarQueryParameterBaseName = "audioCodec";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("audioCodec", audioCodec));
        localVarQueryParameterBaseName = "maxAudioChannels";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxAudioChannels", maxAudioChannels));
        localVarQueryParameterBaseName = "transcodingAudioChannels";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("transcodingAudioChannels", transcodingAudioChannels));
        localVarQueryParameterBaseName = "maxStreamingBitrate";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxStreamingBitrate", maxStreamingBitrate));
        localVarQueryParameterBaseName = "audioBitRate";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("audioBitRate", audioBitRate));
        localVarQueryParameterBaseName = "startTimeTicks";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("startTimeTicks", startTimeTicks));
        localVarQueryParameterBaseName = "transcodingContainer";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("transcodingContainer", transcodingContainer));
        localVarQueryParameterBaseName = "transcodingProtocol";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("transcodingProtocol", transcodingProtocol));
        localVarQueryParameterBaseName = "maxAudioSampleRate";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxAudioSampleRate", maxAudioSampleRate));
        localVarQueryParameterBaseName = "maxAudioBitDepth";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("maxAudioBitDepth", maxAudioBitDepth));
        localVarQueryParameterBaseName = "enableRemoteMedia";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("enableRemoteMedia", enableRemoteMedia));
        localVarQueryParameterBaseName = "enableAudioVbrEncoding";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("enableAudioVbrEncoding", enableAudioVbrEncoding));
        localVarQueryParameterBaseName = "breakOnNonKeyFrames";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("breakOnNonKeyFrames", breakOnNonKeyFrames));
        localVarQueryParameterBaseName = "enableRedirection";
        localVarQueryParams.addAll(ApiClient.parameterToPairs("enableRedirection", enableRedirection));

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
                "audio/*, application/json, application/json; profile=CamelCase, application/json; profile=PascalCase, text/html");

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
}
