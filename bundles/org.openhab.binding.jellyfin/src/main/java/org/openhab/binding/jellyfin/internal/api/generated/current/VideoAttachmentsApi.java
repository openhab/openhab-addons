package org.openhab.binding.jellyfin.internal.api.generated.current;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.UUID;

import javax.ws.rs.core.GenericType;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;
import org.openhab.binding.jellyfin.internal.api.generated.ApiException;
import org.openhab.binding.jellyfin.internal.api.generated.ApiResponse;
import org.openhab.binding.jellyfin.internal.api.generated.Configuration;

public class VideoAttachmentsApi {
    private ApiClient apiClient;

    public VideoAttachmentsApi() {
        this(Configuration.getDefaultApiClient());
    }

    public VideoAttachmentsApi(ApiClient apiClient) {
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
     * Get video attachment.
     * 
     * @param videoId Video ID. (required)
     * @param mediaSourceId Media Source ID. (required)
     * @param index Attachment Index. (required)
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
     *                        <td>Attachment retrieved.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Video or attachment not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public File getAttachment(@org.eclipse.jdt.annotation.Nullable UUID videoId,
            @org.eclipse.jdt.annotation.Nullable String mediaSourceId,
            @org.eclipse.jdt.annotation.Nullable Integer index) throws ApiException {
        return getAttachmentWithHttpInfo(videoId, mediaSourceId, index).getData();
    }

    /**
     * Get video attachment.
     * 
     * @param videoId Video ID. (required)
     * @param mediaSourceId Media Source ID. (required)
     * @param index Attachment Index. (required)
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
     *                        <td>Attachment retrieved.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        <tr>
     *                        <td>404</td>
     *                        <td>Video or attachment not found.</td>
     *                        <td>-</td>
     *                        </tr>
     *                        </table>
     */
    public ApiResponse<File> getAttachmentWithHttpInfo(@org.eclipse.jdt.annotation.Nullable UUID videoId,
            @org.eclipse.jdt.annotation.Nullable String mediaSourceId,
            @org.eclipse.jdt.annotation.Nullable Integer index) throws ApiException {
        // Check required parameters
        if (videoId == null) {
            throw new ApiException(400, "Missing the required parameter 'videoId' when calling getAttachment");
        }
        if (mediaSourceId == null) {
            throw new ApiException(400, "Missing the required parameter 'mediaSourceId' when calling getAttachment");
        }
        if (index == null) {
            throw new ApiException(400, "Missing the required parameter 'index' when calling getAttachment");
        }

        // Path parameters
        String localVarPath = "/Videos/{videoId}/{mediaSourceId}/Attachments/{index}"
                .replaceAll("\\{videoId}", apiClient.escapeString(videoId.toString()))
                .replaceAll("\\{mediaSourceId}", apiClient.escapeString(mediaSourceId.toString()))
                .replaceAll("\\{index}", apiClient.escapeString(index.toString()));

        String localVarAccept = apiClient.selectHeaderAccept("application/octet-stream", "application/json",
                "application/json; profile=CamelCase", "application/json; profile=PascalCase");
        String localVarContentType = apiClient.selectHeaderContentType();
        GenericType<File> localVarReturnType = new GenericType<File>() {
        };
        return apiClient.invokeAPI("VideoAttachmentsApi.getAttachment", localVarPath, "GET", new ArrayList<>(), null,
                new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), localVarAccept,
                localVarContentType, null, localVarReturnType, false);
    }
}
