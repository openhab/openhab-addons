package org.openhab.binding.jellyfin.internal.api.generated.current;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;
import org.openhab.binding.jellyfin.internal.api.generated.ApiException;
import org.openhab.binding.jellyfin.internal.api.generated.ApiResponse;
import org.openhab.binding.jellyfin.internal.api.generated.Configuration;
import org.openhab.binding.jellyfin.internal.api.generated.Pair;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.FontFile;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.RemoteSubtitleInfo;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.UploadSubtitleDto;

import jakarta.ws.rs.core.GenericType;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class SubtitleApi {
    private ApiClient apiClient;

    public SubtitleApi() {
        this(Configuration.getDefaultApiClient());
    }

    public SubtitleApi(ApiClient apiClient) {
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
     * Deletes an external subtitle file.
     * 
     * @param itemId The item id. (required)
     * @param index The index of the subtitle file. (required)
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
     *                        <td>Subtitle deleted.</td>
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
    public void deleteSubtitle(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable Integer index) throws ApiException {
        deleteSubtitleWithHttpInfo(itemId, index);
    }

    /**
     * Deletes an external subtitle file.
     * 
     * @param itemId The item id. (required)
     * @param index The index of the subtitle file. (required)
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
     *                        <td>Subtitle deleted.</td>
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
    public ApiResponse<Void> deleteSubtitleWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable Integer index) throws ApiException {
        // Check required parameters
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling deleteSubtitle");
        }
        if (index == null) {
            throw new ApiException(400, "Missing the required parameter 'index' when calling deleteSubtitle");
        }

        // Path parameters
        String localVarPath = "/Videos/{itemId}/Subtitles/{index}"
                .replaceAll("\\{itemId}", apiClient.escapeString(itemId.toString()))
                .replaceAll("\\{index}", apiClient.escapeString(index.toString()));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        return apiClient.invokeAPI("SubtitleApi.deleteSubtitle", localVarPath, "DELETE", new ArrayList<>(), null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, null, false);
    }

    /**
     * Downloads a remote subtitle.
     * 
     * @param itemId The item id. (required)
     * @param subtitleId The subtitle id. (required)
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
     *                        <td>Subtitle downloaded.</td>
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
    public void downloadRemoteSubtitles(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable String subtitleId) throws ApiException {
        downloadRemoteSubtitlesWithHttpInfo(itemId, subtitleId);
    }

    /**
     * Downloads a remote subtitle.
     * 
     * @param itemId The item id. (required)
     * @param subtitleId The subtitle id. (required)
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
     *                        <td>Subtitle downloaded.</td>
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
    public ApiResponse<Void> downloadRemoteSubtitlesWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable String subtitleId) throws ApiException {
        // Check required parameters
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling downloadRemoteSubtitles");
        }
        if (subtitleId == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'subtitleId' when calling downloadRemoteSubtitles");
        }

        // Path parameters
        String localVarPath = "/Items/{itemId}/RemoteSearch/Subtitles/{subtitleId}"
                .replaceAll("\\{itemId}", apiClient.escapeString(itemId.toString()))
                .replaceAll("\\{subtitleId}", apiClient.escapeString(subtitleId.toString()));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        return apiClient.invokeAPI("SubtitleApi.downloadRemoteSubtitles", localVarPath, "POST", new ArrayList<>(), null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, null, false);
    }

    /**
     * Gets a fallback font file.
     * 
     * @param name The name of the fallback font file to get. (required)
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
     *                        <td>Fallback font file retrieved.</td>
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
    public File getFallbackFont(@org.eclipse.jdt.annotation.Nullable String name) throws ApiException {
        return getFallbackFontWithHttpInfo(name).getData();
    }

    /**
     * Gets a fallback font file.
     * 
     * @param name The name of the fallback font file to get. (required)
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
     *                        <td>Fallback font file retrieved.</td>
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
    public ApiResponse<File> getFallbackFontWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String name)
            throws ApiException {
        // Check required parameters
        if (name == null) {
            throw new ApiException(400, "Missing the required parameter 'name' when calling getFallbackFont");
        }

        // Path parameters
        String localVarPath = "/FallbackFont/Fonts/{name}".replaceAll("\\{name}",
                apiClient.escapeString(name.toString()));

        String localVarAccept = apiClient.selectHeaderAccept("font/*");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<File> localVarReturnType = new GenericType<File>() {
        };
        return apiClient.invokeAPI("SubtitleApi.getFallbackFont", localVarPath, "GET", new ArrayList<>(), null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, localVarReturnType, false);
    }

    /**
     * Gets a list of available fallback font files.
     * 
     * @return List&lt;FontFile&gt;
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
     *                        <td>Information retrieved.</td>
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
    public List<FontFile> getFallbackFontList() throws ApiException {
        return getFallbackFontListWithHttpInfo().getData();
    }

    /**
     * Gets a list of available fallback font files.
     * 
     * @return ApiResponse&lt;List&lt;FontFile&gt;&gt;
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
     *                        <td>Information retrieved.</td>
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
    public ApiResponse<List<FontFile>> getFallbackFontListWithHttpInfo() throws ApiException {
        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<List<FontFile>> localVarReturnType = new GenericType<List<FontFile>>() {
        };
        return apiClient.invokeAPI("SubtitleApi.getFallbackFontList", "/FallbackFont/Fonts", "GET", new ArrayList<>(),
                null, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, localVarReturnType, false);
    }

    /**
     * Gets the remote subtitles.
     * 
     * @param subtitleId The item id. (required)
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
     *                        <td>File returned.</td>
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
    public File getRemoteSubtitles(@org.eclipse.jdt.annotation.Nullable String subtitleId) throws ApiException {
        return getRemoteSubtitlesWithHttpInfo(subtitleId).getData();
    }

    /**
     * Gets the remote subtitles.
     * 
     * @param subtitleId The item id. (required)
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
     *                        <td>File returned.</td>
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
    public ApiResponse<File> getRemoteSubtitlesWithHttpInfo(@org.eclipse.jdt.annotation.Nullable String subtitleId)
            throws ApiException {
        // Check required parameters
        if (subtitleId == null) {
            throw new ApiException(400, "Missing the required parameter 'subtitleId' when calling getRemoteSubtitles");
        }

        // Path parameters
        String localVarPath = "/Providers/Subtitles/Subtitles/{subtitleId}".replaceAll("\\{subtitleId}",
                apiClient.escapeString(subtitleId.toString()));

        String localVarAccept = apiClient.selectHeaderAccept("text/*");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<File> localVarReturnType = new GenericType<File>() {
        };
        return apiClient.invokeAPI("SubtitleApi.getRemoteSubtitles", localVarPath, "GET", new ArrayList<>(), null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, localVarReturnType, false);
    }

    /**
     * Gets subtitles in a specified format.
     * 
     * @param routeItemId The (route) item id. (required)
     * @param routeMediaSourceId The (route) media source id. (required)
     * @param routeIndex The (route) subtitle stream index. (required)
     * @param routeFormat The (route) format of the returned subtitle. (required)
     * @param itemId The item id. (optional)
     * @param mediaSourceId The media source id. (optional)
     * @param index The subtitle stream index. (optional)
     * @param format The format of the returned subtitle. (optional)
     * @param endPositionTicks Optional. The end position of the subtitle in ticks. (optional)
     * @param copyTimestamps Optional. Whether to copy the timestamps. (optional, default to false)
     * @param addVttTimeMap Optional. Whether to add a VTT time map. (optional, default to false)
     * @param startPositionTicks The start position of the subtitle in ticks. (optional, default to 0)
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
     *                        <td>File returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public File getSubtitle(@org.eclipse.jdt.annotation.Nullable UUID routeItemId,
            @org.eclipse.jdt.annotation.Nullable String routeMediaSourceId,
            @org.eclipse.jdt.annotation.Nullable Integer routeIndex,
            @org.eclipse.jdt.annotation.Nullable String routeFormat, @org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull String mediaSourceId, @org.eclipse.jdt.annotation.NonNull Integer index,
            @org.eclipse.jdt.annotation.NonNull String format,
            @org.eclipse.jdt.annotation.NonNull Long endPositionTicks,
            @org.eclipse.jdt.annotation.NonNull Boolean copyTimestamps,
            @org.eclipse.jdt.annotation.NonNull Boolean addVttTimeMap,
            @org.eclipse.jdt.annotation.NonNull Long startPositionTicks) throws ApiException {
        return getSubtitleWithHttpInfo(routeItemId, routeMediaSourceId, routeIndex, routeFormat, itemId, mediaSourceId,
                index, format, endPositionTicks, copyTimestamps, addVttTimeMap, startPositionTicks).getData();
    }

    /**
     * Gets subtitles in a specified format.
     * 
     * @param routeItemId The (route) item id. (required)
     * @param routeMediaSourceId The (route) media source id. (required)
     * @param routeIndex The (route) subtitle stream index. (required)
     * @param routeFormat The (route) format of the returned subtitle. (required)
     * @param itemId The item id. (optional)
     * @param mediaSourceId The media source id. (optional)
     * @param index The subtitle stream index. (optional)
     * @param format The format of the returned subtitle. (optional)
     * @param endPositionTicks Optional. The end position of the subtitle in ticks. (optional)
     * @param copyTimestamps Optional. Whether to copy the timestamps. (optional, default to false)
     * @param addVttTimeMap Optional. Whether to add a VTT time map. (optional, default to false)
     * @param startPositionTicks The start position of the subtitle in ticks. (optional, default to 0)
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
     *                        <td>File returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<File> getSubtitleWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID routeItemId,
            @org.eclipse.jdt.annotation.Nullable String routeMediaSourceId,
            @org.eclipse.jdt.annotation.Nullable Integer routeIndex,
            @org.eclipse.jdt.annotation.Nullable String routeFormat, @org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull String mediaSourceId, @org.eclipse.jdt.annotation.NonNull Integer index,
            @org.eclipse.jdt.annotation.NonNull String format,
            @org.eclipse.jdt.annotation.NonNull Long endPositionTicks,
            @org.eclipse.jdt.annotation.NonNull Boolean copyTimestamps,
            @org.eclipse.jdt.annotation.NonNull Boolean addVttTimeMap,
            @org.eclipse.jdt.annotation.NonNull Long startPositionTicks) throws ApiException {
        // Check required parameters
        if (routeItemId == null) {
            throw new ApiException(400, "Missing the required parameter 'routeItemId' when calling getSubtitle");
        }
        if (routeMediaSourceId == null) {
            throw new ApiException(400, "Missing the required parameter 'routeMediaSourceId' when calling getSubtitle");
        }
        if (routeIndex == null) {
            throw new ApiException(400, "Missing the required parameter 'routeIndex' when calling getSubtitle");
        }
        if (routeFormat == null) {
            throw new ApiException(400, "Missing the required parameter 'routeFormat' when calling getSubtitle");
        }

        // Path parameters
        String localVarPath = "/Videos/{routeItemId}/{routeMediaSourceId}/Subtitles/{routeIndex}/Stream.{routeFormat}"
                .replaceAll("\\{routeItemId}", apiClient.escapeString(routeItemId.toString()))
                .replaceAll("\\{routeMediaSourceId}", apiClient.escapeString(routeMediaSourceId.toString()))
                .replaceAll("\\{routeIndex}", apiClient.escapeString(routeIndex.toString()))
                .replaceAll("\\{routeFormat}", apiClient.escapeString(routeFormat.toString()));

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "itemId", itemId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "mediaSourceId", mediaSourceId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "index", index));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "format", format));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "endPositionTicks", endPositionTicks));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "copyTimestamps", copyTimestamps));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "addVttTimeMap", addVttTimeMap));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "startPositionTicks", startPositionTicks));

        String localVarAccept = apiClient.selectHeaderAccept("text/*");
        String localVarContentType = apiClient.selectHeaderContentType();
        GenericType<File> localVarReturnType = new GenericType<File>() {
        };
        return apiClient.invokeAPI("SubtitleApi.getSubtitle", localVarPath, "GET", localVarQueryParams, null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, null, localVarReturnType, false);
    }

    /**
     * Gets an HLS subtitle playlist.
     * 
     * @param itemId The item id. (required)
     * @param index The subtitle stream index. (required)
     * @param mediaSourceId The media source id. (required)
     * @param segmentLength The subtitle segment length. (required)
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
     *                        <td>Subtitle playlist retrieved.</td>
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
    public File getSubtitlePlaylist(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable Integer index,
            @org.eclipse.jdt.annotation.Nullable String mediaSourceId,
            @org.eclipse.jdt.annotation.Nullable Integer segmentLength) throws ApiException {
        return getSubtitlePlaylistWithHttpInfo(itemId, index, mediaSourceId, segmentLength).getData();
    }

    /**
     * Gets an HLS subtitle playlist.
     * 
     * @param itemId The item id. (required)
     * @param index The subtitle stream index. (required)
     * @param mediaSourceId The media source id. (required)
     * @param segmentLength The subtitle segment length. (required)
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
     *                        <td>Subtitle playlist retrieved.</td>
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
    public ApiResponse<File> getSubtitlePlaylistWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable Integer index,
            @org.eclipse.jdt.annotation.Nullable String mediaSourceId,
            @org.eclipse.jdt.annotation.Nullable Integer segmentLength) throws ApiException {
        // Check required parameters
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling getSubtitlePlaylist");
        }
        if (index == null) {
            throw new ApiException(400, "Missing the required parameter 'index' when calling getSubtitlePlaylist");
        }
        if (mediaSourceId == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'mediaSourceId' when calling getSubtitlePlaylist");
        }
        if (segmentLength == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'segmentLength' when calling getSubtitlePlaylist");
        }

        // Path parameters
        String localVarPath = "/Videos/{itemId}/{mediaSourceId}/Subtitles/{index}/subtitles.m3u8"
                .replaceAll("\\{itemId}", apiClient.escapeString(itemId.toString()))
                .replaceAll("\\{index}", apiClient.escapeString(index.toString()))
                .replaceAll("\\{mediaSourceId}", apiClient.escapeString(mediaSourceId.toString()));

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(
                apiClient.parameterToPairs("", "segmentLength", segmentLength));

        String localVarAccept = apiClient.selectHeaderAccept("application/x-mpegURL", "application/json",
                "application/json; profile=CamelCase", "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<File> localVarReturnType = new GenericType<File>() {
        };
        return apiClient.invokeAPI("SubtitleApi.getSubtitlePlaylist", localVarPath, "GET", localVarQueryParams, null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, localVarReturnType, false);
    }

    /**
     * Gets subtitles in a specified format.
     * 
     * @param routeItemId The (route) item id. (required)
     * @param routeMediaSourceId The (route) media source id. (required)
     * @param routeIndex The (route) subtitle stream index. (required)
     * @param routeStartPositionTicks The (route) start position of the subtitle in ticks. (required)
     * @param routeFormat The (route) format of the returned subtitle. (required)
     * @param itemId The item id. (optional)
     * @param mediaSourceId The media source id. (optional)
     * @param index The subtitle stream index. (optional)
     * @param startPositionTicks The start position of the subtitle in ticks. (optional)
     * @param format The format of the returned subtitle. (optional)
     * @param endPositionTicks Optional. The end position of the subtitle in ticks. (optional)
     * @param copyTimestamps Optional. Whether to copy the timestamps. (optional, default to false)
     * @param addVttTimeMap Optional. Whether to add a VTT time map. (optional, default to false)
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
     *                        <td>File returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public File getSubtitleWithTicks(@org.eclipse.jdt.annotation.Nullable UUID routeItemId,
            @org.eclipse.jdt.annotation.Nullable String routeMediaSourceId,
            @org.eclipse.jdt.annotation.Nullable Integer routeIndex,
            @org.eclipse.jdt.annotation.Nullable Long routeStartPositionTicks,
            @org.eclipse.jdt.annotation.Nullable String routeFormat, @org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull String mediaSourceId, @org.eclipse.jdt.annotation.NonNull Integer index,
            @org.eclipse.jdt.annotation.NonNull Long startPositionTicks,
            @org.eclipse.jdt.annotation.NonNull String format,
            @org.eclipse.jdt.annotation.NonNull Long endPositionTicks,
            @org.eclipse.jdt.annotation.NonNull Boolean copyTimestamps,
            @org.eclipse.jdt.annotation.NonNull Boolean addVttTimeMap) throws ApiException {
        return getSubtitleWithTicksWithHttpInfo(routeItemId, routeMediaSourceId, routeIndex, routeStartPositionTicks,
                routeFormat, itemId, mediaSourceId, index, startPositionTicks, format, endPositionTicks, copyTimestamps,
                addVttTimeMap).getData();
    }

    /**
     * Gets subtitles in a specified format.
     * 
     * @param routeItemId The (route) item id. (required)
     * @param routeMediaSourceId The (route) media source id. (required)
     * @param routeIndex The (route) subtitle stream index. (required)
     * @param routeStartPositionTicks The (route) start position of the subtitle in ticks. (required)
     * @param routeFormat The (route) format of the returned subtitle. (required)
     * @param itemId The item id. (optional)
     * @param mediaSourceId The media source id. (optional)
     * @param index The subtitle stream index. (optional)
     * @param startPositionTicks The start position of the subtitle in ticks. (optional)
     * @param format The format of the returned subtitle. (optional)
     * @param endPositionTicks Optional. The end position of the subtitle in ticks. (optional)
     * @param copyTimestamps Optional. Whether to copy the timestamps. (optional, default to false)
     * @param addVttTimeMap Optional. Whether to add a VTT time map. (optional, default to false)
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
     *                        <td>File returned.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<File> getSubtitleWithTicksWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID routeItemId,
            @org.eclipse.jdt.annotation.Nullable String routeMediaSourceId,
            @org.eclipse.jdt.annotation.Nullable Integer routeIndex,
            @org.eclipse.jdt.annotation.Nullable Long routeStartPositionTicks,
            @org.eclipse.jdt.annotation.Nullable String routeFormat, @org.eclipse.jdt.annotation.NonNull UUID itemId,
            @org.eclipse.jdt.annotation.NonNull String mediaSourceId, @org.eclipse.jdt.annotation.NonNull Integer index,
            @org.eclipse.jdt.annotation.NonNull Long startPositionTicks,
            @org.eclipse.jdt.annotation.NonNull String format,
            @org.eclipse.jdt.annotation.NonNull Long endPositionTicks,
            @org.eclipse.jdt.annotation.NonNull Boolean copyTimestamps,
            @org.eclipse.jdt.annotation.NonNull Boolean addVttTimeMap) throws ApiException {
        // Check required parameters
        if (routeItemId == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'routeItemId' when calling getSubtitleWithTicks");
        }
        if (routeMediaSourceId == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'routeMediaSourceId' when calling getSubtitleWithTicks");
        }
        if (routeIndex == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'routeIndex' when calling getSubtitleWithTicks");
        }
        if (routeStartPositionTicks == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'routeStartPositionTicks' when calling getSubtitleWithTicks");
        }
        if (routeFormat == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'routeFormat' when calling getSubtitleWithTicks");
        }

        // Path parameters
        String localVarPath = "/Videos/{routeItemId}/{routeMediaSourceId}/Subtitles/{routeIndex}/{routeStartPositionTicks}/Stream.{routeFormat}"
                .replaceAll("\\{routeItemId}", apiClient.escapeString(routeItemId.toString()))
                .replaceAll("\\{routeMediaSourceId}", apiClient.escapeString(routeMediaSourceId.toString()))
                .replaceAll("\\{routeIndex}", apiClient.escapeString(routeIndex.toString()))
                .replaceAll("\\{routeStartPositionTicks}", apiClient.escapeString(routeStartPositionTicks.toString()))
                .replaceAll("\\{routeFormat}", apiClient.escapeString(routeFormat.toString()));

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(apiClient.parameterToPairs("", "itemId", itemId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "mediaSourceId", mediaSourceId));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "index", index));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "startPositionTicks", startPositionTicks));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "format", format));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "endPositionTicks", endPositionTicks));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "copyTimestamps", copyTimestamps));
        localVarQueryParams.addAll(apiClient.parameterToPairs("", "addVttTimeMap", addVttTimeMap));

        String localVarAccept = apiClient.selectHeaderAccept("text/*");
        String localVarContentType = apiClient.selectHeaderContentType();
        GenericType<File> localVarReturnType = new GenericType<File>() {
        };
        return apiClient.invokeAPI("SubtitleApi.getSubtitleWithTicks", localVarPath, "GET", localVarQueryParams, null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, null, localVarReturnType, false);
    }

    /**
     * Search remote subtitles.
     * 
     * @param itemId The item id. (required)
     * @param language The language of the subtitles. (required)
     * @param isPerfectMatch Optional. Only show subtitles which are a perfect match. (optional)
     * @return List&lt;RemoteSubtitleInfo&gt;
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
     *                        <td>Subtitles retrieved.</td>
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
    public List<RemoteSubtitleInfo> searchRemoteSubtitles(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable String language,
            @org.eclipse.jdt.annotation.NonNull Boolean isPerfectMatch) throws ApiException {
        return searchRemoteSubtitlesWithHttpInfo(itemId, language, isPerfectMatch).getData();
    }

    /**
     * Search remote subtitles.
     * 
     * @param itemId The item id. (required)
     * @param language The language of the subtitles. (required)
     * @param isPerfectMatch Optional. Only show subtitles which are a perfect match. (optional)
     * @return ApiResponse&lt;List&lt;RemoteSubtitleInfo&gt;&gt;
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
     *                        <td>Subtitles retrieved.</td>
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
    public ApiResponse<List<RemoteSubtitleInfo>> searchRemoteSubtitlesWithHttpInfo(
            @org.eclipse.jdt.annotation.Nullable UUID itemId, @org.eclipse.jdt.annotation.Nullable String language,
            @org.eclipse.jdt.annotation.NonNull Boolean isPerfectMatch) throws ApiException {
        // Check required parameters
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling searchRemoteSubtitles");
        }
        if (language == null) {
            throw new ApiException(400, "Missing the required parameter 'language' when calling searchRemoteSubtitles");
        }

        // Path parameters
        String localVarPath = "/Items/{itemId}/RemoteSearch/Subtitles/{language}"
                .replaceAll("\\{itemId}", apiClient.escapeString(itemId.toString()))
                .replaceAll("\\{language}", apiClient.escapeString(language.toString()));

        // Query parameters
        List<Pair> localVarQueryParams = new ArrayList<>(
                apiClient.parameterToPairs("", "isPerfectMatch", isPerfectMatch));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        GenericType<List<RemoteSubtitleInfo>> localVarReturnType = new GenericType<List<RemoteSubtitleInfo>>() {
        };
        return apiClient.invokeAPI("SubtitleApi.searchRemoteSubtitles", localVarPath, "GET", localVarQueryParams, null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, localVarReturnType, false);
    }

    /**
     * Upload an external subtitle file.
     * 
     * @param itemId The item the subtitle belongs to. (required)
     * @param uploadSubtitleDto The request body. (required)
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
     *                        <td>Subtitle uploaded.</td>
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
    public void uploadSubtitle(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable UploadSubtitleDto uploadSubtitleDto) throws ApiException {
        uploadSubtitleWithHttpInfo(itemId, uploadSubtitleDto);
    }

    /**
     * Upload an external subtitle file.
     * 
     * @param itemId The item the subtitle belongs to. (required)
     * @param uploadSubtitleDto The request body. (required)
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
     *                        <td>Subtitle uploaded.</td>
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
    public ApiResponse<Void> uploadSubtitleWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID itemId,
            @org.eclipse.jdt.annotation.Nullable UploadSubtitleDto uploadSubtitleDto) throws ApiException {
        // Check required parameters
        if (itemId == null) {
            throw new ApiException(400, "Missing the required parameter 'itemId' when calling uploadSubtitle");
        }
        if (uploadSubtitleDto == null) {
            throw new ApiException(400,
                    "Missing the required parameter 'uploadSubtitleDto' when calling uploadSubtitle");
        }

        // Path parameters
        String localVarPath = "/Videos/{itemId}/Subtitles".replaceAll("\\{itemId}",
                apiClient.escapeString(itemId.toString()));

        String localVarAccept = apiClient.selectHeaderAccept("application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType("application/json", "text/json",
                "application/*+json");
        String[] localVarAuthNames = new String[] { "CustomAuthentication" };
        return apiClient.invokeAPI("SubtitleApi.uploadSubtitle", localVarPath, "POST", new ArrayList<>(),
                uploadSubtitleDto, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, localVarAuthNames, null, false);
    }
}
