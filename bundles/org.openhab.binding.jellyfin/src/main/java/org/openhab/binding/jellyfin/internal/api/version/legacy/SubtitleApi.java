package org.openhab.binding.jellyfin.internal.api.version.legacy;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.openhab.binding.jellyfin.internal.api.version.ApiClient;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.FontFile;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.RemoteSubtitleInfo;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.UploadSubtitleDto;
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

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.12.0")
public class SubtitleApi {
    private ApiClient apiClient;

    public SubtitleApi() {
        this(new ApiClient());
    }

    @Autowired
    public SubtitleApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Deletes an external subtitle file.
     * 
     * <p>
     * <b>204</b> - Subtitle deleted.
     * <p>
     * <b>404</b> - Item not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId The item id.
     * @param index The index of the subtitle file.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec deleteSubtitleRequestCreation(UUID itemId, Integer index) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException("Missing the required parameter 'itemId' when calling deleteSubtitle",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'index' is set
        if (index == null) {
            throw new WebClientResponseException("Missing the required parameter 'index' when calling deleteSubtitle",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("itemId", itemId);
        pathParams.put("index", index);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return apiClient.invokeAPI("/Videos/{itemId}/Subtitles/{index}", HttpMethod.DELETE, pathParams, queryParams,
                postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType,
                localVarAuthNames, localVarReturnType);
    }

    /**
     * Deletes an external subtitle file.
     * 
     * <p>
     * <b>204</b> - Subtitle deleted.
     * <p>
     * <b>404</b> - Item not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId The item id.
     * @param index The index of the subtitle file.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> deleteSubtitle(UUID itemId, Integer index) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return deleteSubtitleRequestCreation(itemId, index).bodyToMono(localVarReturnType);
    }

    /**
     * Deletes an external subtitle file.
     * 
     * <p>
     * <b>204</b> - Subtitle deleted.
     * <p>
     * <b>404</b> - Item not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId The item id.
     * @param index The index of the subtitle file.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> deleteSubtitleWithHttpInfo(UUID itemId, Integer index)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return deleteSubtitleRequestCreation(itemId, index).toEntity(localVarReturnType);
    }

    /**
     * Deletes an external subtitle file.
     * 
     * <p>
     * <b>204</b> - Subtitle deleted.
     * <p>
     * <b>404</b> - Item not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId The item id.
     * @param index The index of the subtitle file.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec deleteSubtitleWithResponseSpec(UUID itemId, Integer index) throws WebClientResponseException {
        return deleteSubtitleRequestCreation(itemId, index);
    }

    /**
     * Downloads a remote subtitle.
     * 
     * <p>
     * <b>204</b> - Subtitle downloaded.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId The item id.
     * @param subtitleId The subtitle id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec downloadRemoteSubtitlesRequestCreation(UUID itemId, String subtitleId)
            throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'itemId' when calling downloadRemoteSubtitles",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'subtitleId' is set
        if (subtitleId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'subtitleId' when calling downloadRemoteSubtitles",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("itemId", itemId);
        pathParams.put("subtitleId", subtitleId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = {};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return apiClient.invokeAPI("/Items/{itemId}/RemoteSearch/Subtitles/{subtitleId}", HttpMethod.POST, pathParams,
                queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType,
                localVarAuthNames, localVarReturnType);
    }

    /**
     * Downloads a remote subtitle.
     * 
     * <p>
     * <b>204</b> - Subtitle downloaded.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId The item id.
     * @param subtitleId The subtitle id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> downloadRemoteSubtitles(UUID itemId, String subtitleId) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return downloadRemoteSubtitlesRequestCreation(itemId, subtitleId).bodyToMono(localVarReturnType);
    }

    /**
     * Downloads a remote subtitle.
     * 
     * <p>
     * <b>204</b> - Subtitle downloaded.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId The item id.
     * @param subtitleId The subtitle id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> downloadRemoteSubtitlesWithHttpInfo(UUID itemId, String subtitleId)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return downloadRemoteSubtitlesRequestCreation(itemId, subtitleId).toEntity(localVarReturnType);
    }

    /**
     * Downloads a remote subtitle.
     * 
     * <p>
     * <b>204</b> - Subtitle downloaded.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId The item id.
     * @param subtitleId The subtitle id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec downloadRemoteSubtitlesWithResponseSpec(UUID itemId, String subtitleId)
            throws WebClientResponseException {
        return downloadRemoteSubtitlesRequestCreation(itemId, subtitleId);
    }

    /**
     * Gets a fallback font file.
     * 
     * <p>
     * <b>200</b> - Fallback font file retrieved.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param name The name of the fallback font file to get.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getFallbackFontRequestCreation(String name) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'name' is set
        if (name == null) {
            throw new WebClientResponseException("Missing the required parameter 'name' when calling getFallbackFont",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("name", name);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { "font/*" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return apiClient.invokeAPI("/FallbackFont/Fonts/{name}", HttpMethod.GET, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Gets a fallback font file.
     * 
     * <p>
     * <b>200</b> - Fallback font file retrieved.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param name The name of the fallback font file to get.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<File> getFallbackFont(String name) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return getFallbackFontRequestCreation(name).bodyToMono(localVarReturnType);
    }

    /**
     * Gets a fallback font file.
     * 
     * <p>
     * <b>200</b> - Fallback font file retrieved.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param name The name of the fallback font file to get.
     * @return ResponseEntity&lt;File&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<File>> getFallbackFontWithHttpInfo(String name) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return getFallbackFontRequestCreation(name).toEntity(localVarReturnType);
    }

    /**
     * Gets a fallback font file.
     * 
     * <p>
     * <b>200</b> - Fallback font file retrieved.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param name The name of the fallback font file to get.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getFallbackFontWithResponseSpec(String name) throws WebClientResponseException {
        return getFallbackFontRequestCreation(name);
    }

    /**
     * Gets a list of available fallback font files.
     * 
     * <p>
     * <b>200</b> - Information retrieved.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return List&lt;FontFile&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getFallbackFontListRequestCreation() throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<FontFile> localVarReturnType = new ParameterizedTypeReference<FontFile>() {
        };
        return apiClient.invokeAPI("/FallbackFont/Fonts", HttpMethod.GET, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Gets a list of available fallback font files.
     * 
     * <p>
     * <b>200</b> - Information retrieved.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return List&lt;FontFile&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Flux<FontFile> getFallbackFontList() throws WebClientResponseException {
        ParameterizedTypeReference<FontFile> localVarReturnType = new ParameterizedTypeReference<FontFile>() {
        };
        return getFallbackFontListRequestCreation().bodyToFlux(localVarReturnType);
    }

    /**
     * Gets a list of available fallback font files.
     * 
     * <p>
     * <b>200</b> - Information retrieved.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return ResponseEntity&lt;List&lt;FontFile&gt;&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<List<FontFile>>> getFallbackFontListWithHttpInfo() throws WebClientResponseException {
        ParameterizedTypeReference<FontFile> localVarReturnType = new ParameterizedTypeReference<FontFile>() {
        };
        return getFallbackFontListRequestCreation().toEntityList(localVarReturnType);
    }

    /**
     * Gets a list of available fallback font files.
     * 
     * <p>
     * <b>200</b> - Information retrieved.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getFallbackFontListWithResponseSpec() throws WebClientResponseException {
        return getFallbackFontListRequestCreation();
    }

    /**
     * Gets the remote subtitles.
     * 
     * <p>
     * <b>200</b> - File returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param id The item id.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getRemoteSubtitlesRequestCreation(String id) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'id' is set
        if (id == null) {
            throw new WebClientResponseException("Missing the required parameter 'id' when calling getRemoteSubtitles",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("id", id);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { "text/*" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return apiClient.invokeAPI("/Providers/Subtitles/Subtitles/{id}", HttpMethod.GET, pathParams, queryParams,
                postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType,
                localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets the remote subtitles.
     * 
     * <p>
     * <b>200</b> - File returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param id The item id.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<File> getRemoteSubtitles(String id) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return getRemoteSubtitlesRequestCreation(id).bodyToMono(localVarReturnType);
    }

    /**
     * Gets the remote subtitles.
     * 
     * <p>
     * <b>200</b> - File returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param id The item id.
     * @return ResponseEntity&lt;File&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<File>> getRemoteSubtitlesWithHttpInfo(String id) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return getRemoteSubtitlesRequestCreation(id).toEntity(localVarReturnType);
    }

    /**
     * Gets the remote subtitles.
     * 
     * <p>
     * <b>200</b> - File returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param id The item id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getRemoteSubtitlesWithResponseSpec(String id) throws WebClientResponseException {
        return getRemoteSubtitlesRequestCreation(id);
    }

    /**
     * Gets subtitles in a specified format.
     * 
     * <p>
     * <b>200</b> - File returned.
     * 
     * @param routeItemId The (route) item id.
     * @param routeMediaSourceId The (route) media source id.
     * @param routeIndex The (route) subtitle stream index.
     * @param routeFormat The (route) format of the returned subtitle.
     * @param itemId The item id.
     * @param mediaSourceId The media source id.
     * @param index The subtitle stream index.
     * @param format The format of the returned subtitle.
     * @param endPositionTicks Optional. The end position of the subtitle in ticks.
     * @param copyTimestamps Optional. Whether to copy the timestamps.
     * @param addVttTimeMap Optional. Whether to add a VTT time map.
     * @param startPositionTicks The start position of the subtitle in ticks.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getSubtitleRequestCreation(UUID routeItemId, String routeMediaSourceId, Integer routeIndex,
            String routeFormat, UUID itemId, String mediaSourceId, Integer index, String format, Long endPositionTicks,
            Boolean copyTimestamps, Boolean addVttTimeMap, Long startPositionTicks) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'routeItemId' is set
        if (routeItemId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'routeItemId' when calling getSubtitle",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'routeMediaSourceId' is set
        if (routeMediaSourceId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'routeMediaSourceId' when calling getSubtitle",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'routeIndex' is set
        if (routeIndex == null) {
            throw new WebClientResponseException("Missing the required parameter 'routeIndex' when calling getSubtitle",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'routeFormat' is set
        if (routeFormat == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'routeFormat' when calling getSubtitle",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("routeItemId", routeItemId);
        pathParams.put("routeMediaSourceId", routeMediaSourceId);
        pathParams.put("routeIndex", routeIndex);
        pathParams.put("routeFormat", routeFormat);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "itemId", itemId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "mediaSourceId", mediaSourceId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "index", index));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "format", format));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "endPositionTicks", endPositionTicks));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "copyTimestamps", copyTimestamps));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "addVttTimeMap", addVttTimeMap));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "startPositionTicks", startPositionTicks));

        final String[] localVarAccepts = { "text/*" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {};

        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return apiClient.invokeAPI(
                "/Videos/{routeItemId}/{routeMediaSourceId}/Subtitles/{routeIndex}/Stream.{routeFormat}",
                HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams,
                localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets subtitles in a specified format.
     * 
     * <p>
     * <b>200</b> - File returned.
     * 
     * @param routeItemId The (route) item id.
     * @param routeMediaSourceId The (route) media source id.
     * @param routeIndex The (route) subtitle stream index.
     * @param routeFormat The (route) format of the returned subtitle.
     * @param itemId The item id.
     * @param mediaSourceId The media source id.
     * @param index The subtitle stream index.
     * @param format The format of the returned subtitle.
     * @param endPositionTicks Optional. The end position of the subtitle in ticks.
     * @param copyTimestamps Optional. Whether to copy the timestamps.
     * @param addVttTimeMap Optional. Whether to add a VTT time map.
     * @param startPositionTicks The start position of the subtitle in ticks.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<File> getSubtitle(UUID routeItemId, String routeMediaSourceId, Integer routeIndex, String routeFormat,
            UUID itemId, String mediaSourceId, Integer index, String format, Long endPositionTicks,
            Boolean copyTimestamps, Boolean addVttTimeMap, Long startPositionTicks) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return getSubtitleRequestCreation(routeItemId, routeMediaSourceId, routeIndex, routeFormat, itemId,
                mediaSourceId, index, format, endPositionTicks, copyTimestamps, addVttTimeMap, startPositionTicks)
                .bodyToMono(localVarReturnType);
    }

    /**
     * Gets subtitles in a specified format.
     * 
     * <p>
     * <b>200</b> - File returned.
     * 
     * @param routeItemId The (route) item id.
     * @param routeMediaSourceId The (route) media source id.
     * @param routeIndex The (route) subtitle stream index.
     * @param routeFormat The (route) format of the returned subtitle.
     * @param itemId The item id.
     * @param mediaSourceId The media source id.
     * @param index The subtitle stream index.
     * @param format The format of the returned subtitle.
     * @param endPositionTicks Optional. The end position of the subtitle in ticks.
     * @param copyTimestamps Optional. Whether to copy the timestamps.
     * @param addVttTimeMap Optional. Whether to add a VTT time map.
     * @param startPositionTicks The start position of the subtitle in ticks.
     * @return ResponseEntity&lt;File&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<File>> getSubtitleWithHttpInfo(UUID routeItemId, String routeMediaSourceId,
            Integer routeIndex, String routeFormat, UUID itemId, String mediaSourceId, Integer index, String format,
            Long endPositionTicks, Boolean copyTimestamps, Boolean addVttTimeMap, Long startPositionTicks)
            throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return getSubtitleRequestCreation(routeItemId, routeMediaSourceId, routeIndex, routeFormat, itemId,
                mediaSourceId, index, format, endPositionTicks, copyTimestamps, addVttTimeMap, startPositionTicks)
                .toEntity(localVarReturnType);
    }

    /**
     * Gets subtitles in a specified format.
     * 
     * <p>
     * <b>200</b> - File returned.
     * 
     * @param routeItemId The (route) item id.
     * @param routeMediaSourceId The (route) media source id.
     * @param routeIndex The (route) subtitle stream index.
     * @param routeFormat The (route) format of the returned subtitle.
     * @param itemId The item id.
     * @param mediaSourceId The media source id.
     * @param index The subtitle stream index.
     * @param format The format of the returned subtitle.
     * @param endPositionTicks Optional. The end position of the subtitle in ticks.
     * @param copyTimestamps Optional. Whether to copy the timestamps.
     * @param addVttTimeMap Optional. Whether to add a VTT time map.
     * @param startPositionTicks The start position of the subtitle in ticks.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getSubtitleWithResponseSpec(UUID routeItemId, String routeMediaSourceId, Integer routeIndex,
            String routeFormat, UUID itemId, String mediaSourceId, Integer index, String format, Long endPositionTicks,
            Boolean copyTimestamps, Boolean addVttTimeMap, Long startPositionTicks) throws WebClientResponseException {
        return getSubtitleRequestCreation(routeItemId, routeMediaSourceId, routeIndex, routeFormat, itemId,
                mediaSourceId, index, format, endPositionTicks, copyTimestamps, addVttTimeMap, startPositionTicks);
    }

    /**
     * Gets an HLS subtitle playlist.
     * 
     * <p>
     * <b>200</b> - Subtitle playlist retrieved.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId The item id.
     * @param index The subtitle stream index.
     * @param mediaSourceId The media source id.
     * @param segmentLength The subtitle segment length.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getSubtitlePlaylistRequestCreation(UUID itemId, Integer index, String mediaSourceId,
            Integer segmentLength) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'itemId' when calling getSubtitlePlaylist",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'index' is set
        if (index == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'index' when calling getSubtitlePlaylist",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'mediaSourceId' is set
        if (mediaSourceId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'mediaSourceId' when calling getSubtitlePlaylist",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'segmentLength' is set
        if (segmentLength == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'segmentLength' when calling getSubtitlePlaylist",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("itemId", itemId);
        pathParams.put("index", index);
        pathParams.put("mediaSourceId", mediaSourceId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "segmentLength", segmentLength));

        final String[] localVarAccepts = { "application/x-mpegURL" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return apiClient.invokeAPI("/Videos/{itemId}/{mediaSourceId}/Subtitles/{index}/subtitles.m3u8", HttpMethod.GET,
                pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept,
                localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets an HLS subtitle playlist.
     * 
     * <p>
     * <b>200</b> - Subtitle playlist retrieved.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId The item id.
     * @param index The subtitle stream index.
     * @param mediaSourceId The media source id.
     * @param segmentLength The subtitle segment length.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<File> getSubtitlePlaylist(UUID itemId, Integer index, String mediaSourceId, Integer segmentLength)
            throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return getSubtitlePlaylistRequestCreation(itemId, index, mediaSourceId, segmentLength)
                .bodyToMono(localVarReturnType);
    }

    /**
     * Gets an HLS subtitle playlist.
     * 
     * <p>
     * <b>200</b> - Subtitle playlist retrieved.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId The item id.
     * @param index The subtitle stream index.
     * @param mediaSourceId The media source id.
     * @param segmentLength The subtitle segment length.
     * @return ResponseEntity&lt;File&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<File>> getSubtitlePlaylistWithHttpInfo(UUID itemId, Integer index, String mediaSourceId,
            Integer segmentLength) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return getSubtitlePlaylistRequestCreation(itemId, index, mediaSourceId, segmentLength)
                .toEntity(localVarReturnType);
    }

    /**
     * Gets an HLS subtitle playlist.
     * 
     * <p>
     * <b>200</b> - Subtitle playlist retrieved.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId The item id.
     * @param index The subtitle stream index.
     * @param mediaSourceId The media source id.
     * @param segmentLength The subtitle segment length.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getSubtitlePlaylistWithResponseSpec(UUID itemId, Integer index, String mediaSourceId,
            Integer segmentLength) throws WebClientResponseException {
        return getSubtitlePlaylistRequestCreation(itemId, index, mediaSourceId, segmentLength);
    }

    /**
     * Gets subtitles in a specified format.
     * 
     * <p>
     * <b>200</b> - File returned.
     * 
     * @param routeItemId The (route) item id.
     * @param routeMediaSourceId The (route) media source id.
     * @param routeIndex The (route) subtitle stream index.
     * @param routeStartPositionTicks The (route) start position of the subtitle in ticks.
     * @param routeFormat The (route) format of the returned subtitle.
     * @param itemId The item id.
     * @param mediaSourceId The media source id.
     * @param index The subtitle stream index.
     * @param startPositionTicks The start position of the subtitle in ticks.
     * @param format The format of the returned subtitle.
     * @param endPositionTicks Optional. The end position of the subtitle in ticks.
     * @param copyTimestamps Optional. Whether to copy the timestamps.
     * @param addVttTimeMap Optional. Whether to add a VTT time map.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getSubtitleWithTicksRequestCreation(UUID routeItemId, String routeMediaSourceId,
            Integer routeIndex, Long routeStartPositionTicks, String routeFormat, UUID itemId, String mediaSourceId,
            Integer index, Long startPositionTicks, String format, Long endPositionTicks, Boolean copyTimestamps,
            Boolean addVttTimeMap) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'routeItemId' is set
        if (routeItemId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'routeItemId' when calling getSubtitleWithTicks",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'routeMediaSourceId' is set
        if (routeMediaSourceId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'routeMediaSourceId' when calling getSubtitleWithTicks",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'routeIndex' is set
        if (routeIndex == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'routeIndex' when calling getSubtitleWithTicks",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'routeStartPositionTicks' is set
        if (routeStartPositionTicks == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'routeStartPositionTicks' when calling getSubtitleWithTicks",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'routeFormat' is set
        if (routeFormat == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'routeFormat' when calling getSubtitleWithTicks",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("routeItemId", routeItemId);
        pathParams.put("routeMediaSourceId", routeMediaSourceId);
        pathParams.put("routeIndex", routeIndex);
        pathParams.put("routeStartPositionTicks", routeStartPositionTicks);
        pathParams.put("routeFormat", routeFormat);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "itemId", itemId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "mediaSourceId", mediaSourceId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "index", index));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "startPositionTicks", startPositionTicks));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "format", format));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "endPositionTicks", endPositionTicks));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "copyTimestamps", copyTimestamps));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "addVttTimeMap", addVttTimeMap));

        final String[] localVarAccepts = { "text/*" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {};

        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return apiClient.invokeAPI(
                "/Videos/{routeItemId}/{routeMediaSourceId}/Subtitles/{routeIndex}/{routeStartPositionTicks}/Stream.{routeFormat}",
                HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams,
                localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets subtitles in a specified format.
     * 
     * <p>
     * <b>200</b> - File returned.
     * 
     * @param routeItemId The (route) item id.
     * @param routeMediaSourceId The (route) media source id.
     * @param routeIndex The (route) subtitle stream index.
     * @param routeStartPositionTicks The (route) start position of the subtitle in ticks.
     * @param routeFormat The (route) format of the returned subtitle.
     * @param itemId The item id.
     * @param mediaSourceId The media source id.
     * @param index The subtitle stream index.
     * @param startPositionTicks The start position of the subtitle in ticks.
     * @param format The format of the returned subtitle.
     * @param endPositionTicks Optional. The end position of the subtitle in ticks.
     * @param copyTimestamps Optional. Whether to copy the timestamps.
     * @param addVttTimeMap Optional. Whether to add a VTT time map.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<File> getSubtitleWithTicks(UUID routeItemId, String routeMediaSourceId, Integer routeIndex,
            Long routeStartPositionTicks, String routeFormat, UUID itemId, String mediaSourceId, Integer index,
            Long startPositionTicks, String format, Long endPositionTicks, Boolean copyTimestamps,
            Boolean addVttTimeMap) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return getSubtitleWithTicksRequestCreation(routeItemId, routeMediaSourceId, routeIndex, routeStartPositionTicks,
                routeFormat, itemId, mediaSourceId, index, startPositionTicks, format, endPositionTicks, copyTimestamps,
                addVttTimeMap).bodyToMono(localVarReturnType);
    }

    /**
     * Gets subtitles in a specified format.
     * 
     * <p>
     * <b>200</b> - File returned.
     * 
     * @param routeItemId The (route) item id.
     * @param routeMediaSourceId The (route) media source id.
     * @param routeIndex The (route) subtitle stream index.
     * @param routeStartPositionTicks The (route) start position of the subtitle in ticks.
     * @param routeFormat The (route) format of the returned subtitle.
     * @param itemId The item id.
     * @param mediaSourceId The media source id.
     * @param index The subtitle stream index.
     * @param startPositionTicks The start position of the subtitle in ticks.
     * @param format The format of the returned subtitle.
     * @param endPositionTicks Optional. The end position of the subtitle in ticks.
     * @param copyTimestamps Optional. Whether to copy the timestamps.
     * @param addVttTimeMap Optional. Whether to add a VTT time map.
     * @return ResponseEntity&lt;File&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<File>> getSubtitleWithTicksWithHttpInfo(UUID routeItemId, String routeMediaSourceId,
            Integer routeIndex, Long routeStartPositionTicks, String routeFormat, UUID itemId, String mediaSourceId,
            Integer index, Long startPositionTicks, String format, Long endPositionTicks, Boolean copyTimestamps,
            Boolean addVttTimeMap) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return getSubtitleWithTicksRequestCreation(routeItemId, routeMediaSourceId, routeIndex, routeStartPositionTicks,
                routeFormat, itemId, mediaSourceId, index, startPositionTicks, format, endPositionTicks, copyTimestamps,
                addVttTimeMap).toEntity(localVarReturnType);
    }

    /**
     * Gets subtitles in a specified format.
     * 
     * <p>
     * <b>200</b> - File returned.
     * 
     * @param routeItemId The (route) item id.
     * @param routeMediaSourceId The (route) media source id.
     * @param routeIndex The (route) subtitle stream index.
     * @param routeStartPositionTicks The (route) start position of the subtitle in ticks.
     * @param routeFormat The (route) format of the returned subtitle.
     * @param itemId The item id.
     * @param mediaSourceId The media source id.
     * @param index The subtitle stream index.
     * @param startPositionTicks The start position of the subtitle in ticks.
     * @param format The format of the returned subtitle.
     * @param endPositionTicks Optional. The end position of the subtitle in ticks.
     * @param copyTimestamps Optional. Whether to copy the timestamps.
     * @param addVttTimeMap Optional. Whether to add a VTT time map.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getSubtitleWithTicksWithResponseSpec(UUID routeItemId, String routeMediaSourceId,
            Integer routeIndex, Long routeStartPositionTicks, String routeFormat, UUID itemId, String mediaSourceId,
            Integer index, Long startPositionTicks, String format, Long endPositionTicks, Boolean copyTimestamps,
            Boolean addVttTimeMap) throws WebClientResponseException {
        return getSubtitleWithTicksRequestCreation(routeItemId, routeMediaSourceId, routeIndex, routeStartPositionTicks,
                routeFormat, itemId, mediaSourceId, index, startPositionTicks, format, endPositionTicks, copyTimestamps,
                addVttTimeMap);
    }

    /**
     * Search remote subtitles.
     * 
     * <p>
     * <b>200</b> - Subtitles retrieved.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId The item id.
     * @param language The language of the subtitles.
     * @param isPerfectMatch Optional. Only show subtitles which are a perfect match.
     * @return List&lt;RemoteSubtitleInfo&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec searchRemoteSubtitlesRequestCreation(UUID itemId, String language, Boolean isPerfectMatch)
            throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'itemId' when calling searchRemoteSubtitles",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'language' is set
        if (language == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'language' when calling searchRemoteSubtitles",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("itemId", itemId);
        pathParams.put("language", language);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isPerfectMatch", isPerfectMatch));

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<RemoteSubtitleInfo> localVarReturnType = new ParameterizedTypeReference<RemoteSubtitleInfo>() {
        };
        return apiClient.invokeAPI("/Items/{itemId}/RemoteSearch/Subtitles/{language}", HttpMethod.GET, pathParams,
                queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType,
                localVarAuthNames, localVarReturnType);
    }

    /**
     * Search remote subtitles.
     * 
     * <p>
     * <b>200</b> - Subtitles retrieved.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId The item id.
     * @param language The language of the subtitles.
     * @param isPerfectMatch Optional. Only show subtitles which are a perfect match.
     * @return List&lt;RemoteSubtitleInfo&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Flux<RemoteSubtitleInfo> searchRemoteSubtitles(UUID itemId, String language, Boolean isPerfectMatch)
            throws WebClientResponseException {
        ParameterizedTypeReference<RemoteSubtitleInfo> localVarReturnType = new ParameterizedTypeReference<RemoteSubtitleInfo>() {
        };
        return searchRemoteSubtitlesRequestCreation(itemId, language, isPerfectMatch).bodyToFlux(localVarReturnType);
    }

    /**
     * Search remote subtitles.
     * 
     * <p>
     * <b>200</b> - Subtitles retrieved.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId The item id.
     * @param language The language of the subtitles.
     * @param isPerfectMatch Optional. Only show subtitles which are a perfect match.
     * @return ResponseEntity&lt;List&lt;RemoteSubtitleInfo&gt;&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<List<RemoteSubtitleInfo>>> searchRemoteSubtitlesWithHttpInfo(UUID itemId,
            String language, Boolean isPerfectMatch) throws WebClientResponseException {
        ParameterizedTypeReference<RemoteSubtitleInfo> localVarReturnType = new ParameterizedTypeReference<RemoteSubtitleInfo>() {
        };
        return searchRemoteSubtitlesRequestCreation(itemId, language, isPerfectMatch).toEntityList(localVarReturnType);
    }

    /**
     * Search remote subtitles.
     * 
     * <p>
     * <b>200</b> - Subtitles retrieved.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId The item id.
     * @param language The language of the subtitles.
     * @param isPerfectMatch Optional. Only show subtitles which are a perfect match.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec searchRemoteSubtitlesWithResponseSpec(UUID itemId, String language, Boolean isPerfectMatch)
            throws WebClientResponseException {
        return searchRemoteSubtitlesRequestCreation(itemId, language, isPerfectMatch);
    }

    /**
     * Upload an external subtitle file.
     * 
     * <p>
     * <b>204</b> - Subtitle uploaded.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId The item the subtitle belongs to.
     * @param uploadSubtitleDto The request body.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec uploadSubtitleRequestCreation(UUID itemId, UploadSubtitleDto uploadSubtitleDto)
            throws WebClientResponseException {
        Object postBody = uploadSubtitleDto;
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException("Missing the required parameter 'itemId' when calling uploadSubtitle",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'uploadSubtitleDto' is set
        if (uploadSubtitleDto == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'uploadSubtitleDto' when calling uploadSubtitle",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("itemId", itemId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = {};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { "application/json", "text/json", "application/*+json" };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return apiClient.invokeAPI("/Videos/{itemId}/Subtitles", HttpMethod.POST, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Upload an external subtitle file.
     * 
     * <p>
     * <b>204</b> - Subtitle uploaded.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId The item the subtitle belongs to.
     * @param uploadSubtitleDto The request body.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> uploadSubtitle(UUID itemId, UploadSubtitleDto uploadSubtitleDto)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return uploadSubtitleRequestCreation(itemId, uploadSubtitleDto).bodyToMono(localVarReturnType);
    }

    /**
     * Upload an external subtitle file.
     * 
     * <p>
     * <b>204</b> - Subtitle uploaded.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId The item the subtitle belongs to.
     * @param uploadSubtitleDto The request body.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> uploadSubtitleWithHttpInfo(UUID itemId, UploadSubtitleDto uploadSubtitleDto)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return uploadSubtitleRequestCreation(itemId, uploadSubtitleDto).toEntity(localVarReturnType);
    }

    /**
     * Upload an external subtitle file.
     * 
     * <p>
     * <b>204</b> - Subtitle uploaded.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId The item the subtitle belongs to.
     * @param uploadSubtitleDto The request body.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec uploadSubtitleWithResponseSpec(UUID itemId, UploadSubtitleDto uploadSubtitleDto)
            throws WebClientResponseException {
        return uploadSubtitleRequestCreation(itemId, uploadSubtitleDto);
    }
}
