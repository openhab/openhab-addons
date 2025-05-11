package org.openhab.binding.jellyfin.internal.api.version.legacy;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.openhab.binding.jellyfin.internal.api.version.ApiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import reactor.core.publisher.Mono;

@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.12.0")
public class VideoAttachmentsApi {
    private ApiClient apiClient;

    public VideoAttachmentsApi() {
        this(new ApiClient());
    }

    @Autowired
    public VideoAttachmentsApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Get video attachment.
     * 
     * <p>
     * <b>200</b> - Attachment retrieved.
     * <p>
     * <b>404</b> - Video or attachment not found.
     * 
     * @param videoId Video ID.
     * @param mediaSourceId Media Source ID.
     * @param index Attachment Index.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getAttachmentRequestCreation(UUID videoId, String mediaSourceId, Integer index)
            throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'videoId' is set
        if (videoId == null) {
            throw new WebClientResponseException("Missing the required parameter 'videoId' when calling getAttachment",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'mediaSourceId' is set
        if (mediaSourceId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'mediaSourceId' when calling getAttachment",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'index' is set
        if (index == null) {
            throw new WebClientResponseException("Missing the required parameter 'index' when calling getAttachment",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("videoId", videoId);
        pathParams.put("mediaSourceId", mediaSourceId);
        pathParams.put("index", index);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { "application/octet-stream", "application/json",
                "application/json; profile=CamelCase", "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {};

        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return apiClient.invokeAPI("/Videos/{videoId}/{mediaSourceId}/Attachments/{index}", HttpMethod.GET, pathParams,
                queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType,
                localVarAuthNames, localVarReturnType);
    }

    /**
     * Get video attachment.
     * 
     * <p>
     * <b>200</b> - Attachment retrieved.
     * <p>
     * <b>404</b> - Video or attachment not found.
     * 
     * @param videoId Video ID.
     * @param mediaSourceId Media Source ID.
     * @param index Attachment Index.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<File> getAttachment(UUID videoId, String mediaSourceId, Integer index)
            throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return getAttachmentRequestCreation(videoId, mediaSourceId, index).bodyToMono(localVarReturnType);
    }

    /**
     * Get video attachment.
     * 
     * <p>
     * <b>200</b> - Attachment retrieved.
     * <p>
     * <b>404</b> - Video or attachment not found.
     * 
     * @param videoId Video ID.
     * @param mediaSourceId Media Source ID.
     * @param index Attachment Index.
     * @return ResponseEntity&lt;File&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<File>> getAttachmentWithHttpInfo(UUID videoId, String mediaSourceId, Integer index)
            throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return getAttachmentRequestCreation(videoId, mediaSourceId, index).toEntity(localVarReturnType);
    }

    /**
     * Get video attachment.
     * 
     * <p>
     * <b>200</b> - Attachment retrieved.
     * <p>
     * <b>404</b> - Video or attachment not found.
     * 
     * @param videoId Video ID.
     * @param mediaSourceId Media Source ID.
     * @param index Attachment Index.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getAttachmentWithResponseSpec(UUID videoId, String mediaSourceId, Integer index)
            throws WebClientResponseException {
        return getAttachmentRequestCreation(videoId, mediaSourceId, index);
    }
}
