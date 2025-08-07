package org.openhab.binding.jellyfin.internal.api.generated.current;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;
import org.openhab.binding.jellyfin.internal.api.generated.ApiException;
import org.openhab.binding.jellyfin.internal.api.generated.ApiResponse;
import org.openhab.binding.jellyfin.internal.api.generated.Configuration;
import org.openhab.binding.jellyfin.internal.api.generated.Pair;

import jakarta.ws.rs.core.GenericType;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class HlsSegmentApi {
    private ApiClient apiClient;

    public HlsSegmentApi() {
        this(Configuration.getDefaultApiClient());
    }

    public HlsSegmentApi(ApiClient apiClient) {
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
     * Gets the specified audio segment for an audio item.
     * 
     * @param itemId The item id. (required)
     * @param segmentId The segment id. (required)
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
     *                        <td>Hls audio segment returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public File getHlsAudioSegmentLegacyAac(@org.eclipse.jdt.annotation.Nullable String itemId,
            @org.eclipse.jdt.annotation.Nullable String segmentId) throws ApiException {
        return getHlsAudioSegmentLegacyAacWithHttpInfo(itemId, segmentId).getData();
    }

    /**
     * Gets the specified audio segment for an audio item.
     * 
     * @param itemId The item id. (required)
     * @param segmentId The segment id. (required)
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
     *                        <td>Hls audio segment returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<File> getHlsAudioSegmentLegacyAacWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String itemId,
            @org.eclipse.jdt.annotation.Nullable String segmentId) throws ApiException {
        // Check required parameters
        if (itemId == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'itemId' when calling getHlsAudioSegmentLegacyAac");
        }
        if (segmentId == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'segmentId' when calling getHlsAudioSegmentLegacyAac");
        }

        // Path parameters
        String localVarPath = "/Audio/{itemId}/hls/{segmentId}/stream.aac"
                .replaceAll("\\{itemId}", apiClient.escapeString(itemId.toString()))
                .replaceAll("\\{segmentId}", apiClient.escapeString(segmentId.toString()));

        String localVarAccept = apiClient.selectHeaderAccept("audio/*");
        String localVarContentType = apiClient.selectHeaderContentType();
        GenericType<File> localVarReturnType = new GenericType<File>() {
        };
        return apiClient.invokeAPI("HlsSegmentApi.getHlsAudioSegmentLegacyAac", localVarPath, "GET", new ArrayList<>(),
                null, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, null, localVarReturnType, false);
    }

    /**
     * Gets the specified audio segment for an audio item.
     * 
     * @param itemId The item id. (required)
     * @param segmentId The segment id. (required)
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
     *                        <td>Hls audio segment returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public File getHlsAudioSegmentLegacyMp3(@org.eclipse.jdt.annotation.Nullable String itemId,
            @org.eclipse.jdt.annotation.Nullable String segmentId) throws ApiException {
        return getHlsAudioSegmentLegacyMp3WithHttpInfo(itemId, segmentId).getData();
    }

    /**
     * Gets the specified audio segment for an audio item.
     * 
     * @param itemId The item id. (required)
     * @param segmentId The segment id. (required)
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
     *                        <td>Hls audio segment returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<File> getHlsAudioSegmentLegacyMp3WithHttpInfo(@org.eclipse.jdt.annotation.Nullable String itemId,
            @org.eclipse.jdt.annotation.Nullable String segmentId) throws ApiException {
        // Check required parameters
        if (itemId == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'itemId' when calling getHlsAudioSegmentLegacyMp3");
        }
        if (segmentId == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'segmentId' when calling getHlsAudioSegmentLegacyMp3");
        }

        // Path parameters
        String localVarPath = "/Audio/{itemId}/hls/{segmentId}/stream.mp3"
                .replaceAll("\\{itemId}", apiClient.escapeString(itemId.toString()))
                .replaceAll("\\{segmentId}", apiClient.escapeString(segmentId.toString()));

        String localVarAccept = apiClient.selectHeaderAccept("audio/*");
        String localVarContentType = apiClient.selectHeaderContentType();
        GenericType<File> localVarReturnType = new GenericType<File>() {
        };
        return apiClient.invokeAPI("HlsSegmentApi.getHlsAudioSegmentLegacyMp3", localVarPath, "GET", new ArrayList<>(),
                null, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, null, localVarReturnType, false);
    }

    /**
     * Gets a hls video playlist.
     * 
     * @param itemId The video id. (required)
     * @param playlistId The playlist id. (required)
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
     *                        <td>Hls video playlist returned.</td>
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
    public File getHlsPlaylistLegacy(@org.eclipse.jdt.annotation.Nullable String itemId,
            @org.eclipse.jdt.annotation.Nullable String playlistId) throws ApiException {
        return getHlsPlaylistLegacyWithHttpInfo(itemId, playlistId).getData();
    }

    /**
     * Gets a hls video playlist.
     * 
     * @param itemId The video id. (required)
     * @param playlistId The playlist id. (required)
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
     *                        <td>Hls video playlist returned.</td>
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
    public ApiResponse<File> getHlsPlaylistLegacyWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String itemId,
            @org.eclipse.jdt.annotation.Nullable String playlistId) throws ApiException {
        // Check required parameters
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling getHlsPlaylistLegacy");
        }
        if (playlistId == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'playlistId' when calling getHlsPlaylistLegacy");
        }

        // Path parameters
        String localVarPath = "/Videos/{itemId}/hls/{playlistId}/stream.m3u8"
                .replaceAll("\\{itemId}", apiClient.escapeString(itemId.toString()))
                .replaceAll("\\{playlistId}", apiClient.escapeString(playlistId.toString()));

        String localVarAccept = apiClient.selectHeaderAccept("application/x-mpegURL");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<File> localVarReturnType = new GenericType<File>() {
        };
        return apiClient.invokeAPI("HlsSegmentApi.getHlsPlaylistLegacy", localVarPath, "GET", new ArrayList<>(), null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, localVarReturnType, false);
    }

    /**
     * Gets a hls video segment.
     * 
     * @param itemId The item id. (required)
     * @param playlistId The playlist id. (required)
     * @param segmentId The segment id. (required)
     * @param segmentContainer The segment container. (required)
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
     *                        <td>Hls video segment returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Hls segment not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public File getHlsVideoSegmentLegacy(@org.eclipse.jdt.annotation.Nullable String itemId,
            @org.eclipse.jdt.annotation.Nullable String playlistId,
            @org.eclipse.jdt.annotation.Nullable String segmentId,
            @org.eclipse.jdt.annotation.Nullable String segmentContainer) throws ApiException {
        return getHlsVideoSegmentLegacyWithHttpInfo(itemId, playlistId, segmentId, segmentContainer).getData();
    }

    /**
     * Gets a hls video segment.
     * 
     * @param itemId The item id. (required)
     * @param playlistId The playlist id. (required)
     * @param segmentId The segment id. (required)
     * @param segmentContainer The segment container. (required)
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
     *                        <td>Hls video segment returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Hls segment not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<File> getHlsVideoSegmentLegacyWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String itemId,
            @org.eclipse.jdt.annotation.Nullable String playlistId,
            @org.eclipse.jdt.annotation.Nullable String segmentId,
            @org.eclipse.jdt.annotation.Nullable String segmentContainer) throws ApiException {
        // Check required parameters
        if (itemId == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'itemId' when calling getHlsVideoSegmentLegacy");
        }
        if (playlistId == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'playlistId' when calling getHlsVideoSegmentLegacy");
        }
        if (segmentId == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'segmentId' when calling getHlsVideoSegmentLegacy");
        }
        if (segmentContainer == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'segmentContainer' when calling getHlsVideoSegmentLegacy");
        }

        // Path parameters
        String localVarPath = "/Videos/{itemId}/hls/{playlistId}/{segmentId}.{segmentContainer}"
                .replaceAll("\\{itemId}", apiClient.escapeString(itemId.toString()))
                .replaceAll("\\{playlistId}", apiClient.escapeString(playlistId.toString()))
                .replaceAll("\\{segmentId}", apiClient.escapeString(segmentId.toString()))
                .replaceAll("\\{segmentContainer}", apiClient.escapeString(segmentContainer.toString()));

        String localVarAccept = apiClient.selectHeaderAccept("video/*", "application/json",
                "application/json; profile=CamelCase", "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        GenericType<File> localVarReturnType = new GenericType<File>() {
        };
        return apiClient.invokeAPI("HlsSegmentApi.getHlsVideoSegmentLegacy", localVarPath, "GET", new ArrayList<>(),
                null, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, null, localVarReturnType, false);
    }

    /**
     * Stops an active encoding.
     * 
     * @param deviceId The device id of the client requesting. Used to stop encoding processes when needed. (required)
     * @param playSessionId The play session id. (required)
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
     *                        <td>Encoding stopped successfully.</td>
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
    public void stopEncodingProcess(@org.eclipse.jdt.annotation.Nullable String deviceId,
            @org.eclipse.jdt.annotation.Nullable String playSessionId) throws ApiException {
        stopEncodingProcessWithHttpInfo(deviceId, playSessionId);
    }

    /**
     * Stops an active encoding.
     * 
     * @param deviceId The device id of the client requesting. Used to stop encoding processes when needed. (required)
     * @param playSessionId The play session id. (required)
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
     *                        <td>Encoding stopped successfully.</td>
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
    public ApiResponse<Void> stopEncodingProcessWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String deviceId,
            @org.eclipse.jdt.annotation.Nullable String playSessionId) throws ApiException {
        // Check required parameters
        if (deviceId == null) {
            throw new ApiException(400, "Missing the required parameter 'deviceId' when calling stopEncodingProcess");
        }
        if (playSessionId == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'playSessionId' when calling stopEncodingProcess");
        }

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "deviceId", deviceId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "playSessionId", playSessionId));

        String localVarAccept = apiClient.selectHeaderAccept();
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        return apiClient.invokeAPI("HlsSegmentApi.stopEncodingProcess", "/Videos/ActiveEncodings", "DELETE",
                localVarQueryParams, null, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(),
                localVarAccept, localVarContentType, localVarAuthNames, null, false);
    }
}
