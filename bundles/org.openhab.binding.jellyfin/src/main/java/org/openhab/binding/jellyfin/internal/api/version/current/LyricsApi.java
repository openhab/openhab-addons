package org.openhab.binding.jellyfin.internal.api.version.current;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.openhab.binding.jellyfin.internal.api.version.ApiClient;
import org.openhab.binding.jellyfin.internal.api.version.current.model.LyricDto;
import org.openhab.binding.jellyfin.internal.api.version.current.model.RemoteLyricInfoDto;
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
public class LyricsApi {
    private ApiClient apiClient;

    public LyricsApi() {
        this(new ApiClient());
    }

    @Autowired
    public LyricsApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Deletes an external lyric file.
     * 
     * <p>
     * <b>204</b> - Lyric deleted.
     * <p>
     * <b>404</b> - Item not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId The item id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec deleteLyricsRequestCreation(UUID itemId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException("Missing the required parameter 'itemId' when calling deleteLyrics",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("itemId", itemId);

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
        return apiClient.invokeAPI("/Audio/{itemId}/Lyrics", HttpMethod.DELETE, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Deletes an external lyric file.
     * 
     * <p>
     * <b>204</b> - Lyric deleted.
     * <p>
     * <b>404</b> - Item not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId The item id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> deleteLyrics(UUID itemId) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return deleteLyricsRequestCreation(itemId).bodyToMono(localVarReturnType);
    }

    /**
     * Deletes an external lyric file.
     * 
     * <p>
     * <b>204</b> - Lyric deleted.
     * <p>
     * <b>404</b> - Item not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId The item id.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> deleteLyricsWithHttpInfo(UUID itemId) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return deleteLyricsRequestCreation(itemId).toEntity(localVarReturnType);
    }

    /**
     * Deletes an external lyric file.
     * 
     * <p>
     * <b>204</b> - Lyric deleted.
     * <p>
     * <b>404</b> - Item not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId The item id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec deleteLyricsWithResponseSpec(UUID itemId) throws WebClientResponseException {
        return deleteLyricsRequestCreation(itemId);
    }

    /**
     * Downloads a remote lyric.
     * 
     * <p>
     * <b>200</b> - Lyric downloaded.
     * <p>
     * <b>404</b> - Item not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId The item id.
     * @param lyricId The lyric id.
     * @return LyricDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec downloadRemoteLyricsRequestCreation(UUID itemId, String lyricId)
            throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'itemId' when calling downloadRemoteLyrics",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'lyricId' is set
        if (lyricId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'lyricId' when calling downloadRemoteLyrics",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("itemId", itemId);
        pathParams.put("lyricId", lyricId);

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

        ParameterizedTypeReference<LyricDto> localVarReturnType = new ParameterizedTypeReference<LyricDto>() {
        };
        return apiClient.invokeAPI("/Audio/{itemId}/RemoteSearch/Lyrics/{lyricId}", HttpMethod.POST, pathParams,
                queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType,
                localVarAuthNames, localVarReturnType);
    }

    /**
     * Downloads a remote lyric.
     * 
     * <p>
     * <b>200</b> - Lyric downloaded.
     * <p>
     * <b>404</b> - Item not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId The item id.
     * @param lyricId The lyric id.
     * @return LyricDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<LyricDto> downloadRemoteLyrics(UUID itemId, String lyricId) throws WebClientResponseException {
        ParameterizedTypeReference<LyricDto> localVarReturnType = new ParameterizedTypeReference<LyricDto>() {
        };
        return downloadRemoteLyricsRequestCreation(itemId, lyricId).bodyToMono(localVarReturnType);
    }

    /**
     * Downloads a remote lyric.
     * 
     * <p>
     * <b>200</b> - Lyric downloaded.
     * <p>
     * <b>404</b> - Item not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId The item id.
     * @param lyricId The lyric id.
     * @return ResponseEntity&lt;LyricDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<LyricDto>> downloadRemoteLyricsWithHttpInfo(UUID itemId, String lyricId)
            throws WebClientResponseException {
        ParameterizedTypeReference<LyricDto> localVarReturnType = new ParameterizedTypeReference<LyricDto>() {
        };
        return downloadRemoteLyricsRequestCreation(itemId, lyricId).toEntity(localVarReturnType);
    }

    /**
     * Downloads a remote lyric.
     * 
     * <p>
     * <b>200</b> - Lyric downloaded.
     * <p>
     * <b>404</b> - Item not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId The item id.
     * @param lyricId The lyric id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec downloadRemoteLyricsWithResponseSpec(UUID itemId, String lyricId)
            throws WebClientResponseException {
        return downloadRemoteLyricsRequestCreation(itemId, lyricId);
    }

    /**
     * Gets an item&#39;s lyrics.
     * 
     * <p>
     * <b>200</b> - Lyrics returned.
     * <p>
     * <b>404</b> - Something went wrong. No Lyrics will be returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId Item id.
     * @return LyricDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getLyricsRequestCreation(UUID itemId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException("Missing the required parameter 'itemId' when calling getLyrics",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("itemId", itemId);

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

        ParameterizedTypeReference<LyricDto> localVarReturnType = new ParameterizedTypeReference<LyricDto>() {
        };
        return apiClient.invokeAPI("/Audio/{itemId}/Lyrics", HttpMethod.GET, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Gets an item&#39;s lyrics.
     * 
     * <p>
     * <b>200</b> - Lyrics returned.
     * <p>
     * <b>404</b> - Something went wrong. No Lyrics will be returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId Item id.
     * @return LyricDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<LyricDto> getLyrics(UUID itemId) throws WebClientResponseException {
        ParameterizedTypeReference<LyricDto> localVarReturnType = new ParameterizedTypeReference<LyricDto>() {
        };
        return getLyricsRequestCreation(itemId).bodyToMono(localVarReturnType);
    }

    /**
     * Gets an item&#39;s lyrics.
     * 
     * <p>
     * <b>200</b> - Lyrics returned.
     * <p>
     * <b>404</b> - Something went wrong. No Lyrics will be returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId Item id.
     * @return ResponseEntity&lt;LyricDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<LyricDto>> getLyricsWithHttpInfo(UUID itemId) throws WebClientResponseException {
        ParameterizedTypeReference<LyricDto> localVarReturnType = new ParameterizedTypeReference<LyricDto>() {
        };
        return getLyricsRequestCreation(itemId).toEntity(localVarReturnType);
    }

    /**
     * Gets an item&#39;s lyrics.
     * 
     * <p>
     * <b>200</b> - Lyrics returned.
     * <p>
     * <b>404</b> - Something went wrong. No Lyrics will be returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId Item id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getLyricsWithResponseSpec(UUID itemId) throws WebClientResponseException {
        return getLyricsRequestCreation(itemId);
    }

    /**
     * Gets the remote lyrics.
     * 
     * <p>
     * <b>200</b> - File returned.
     * <p>
     * <b>404</b> - Lyric not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param lyricId The remote provider item id.
     * @return LyricDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getRemoteLyricsRequestCreation(String lyricId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'lyricId' is set
        if (lyricId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'lyricId' when calling getRemoteLyrics",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("lyricId", lyricId);

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

        ParameterizedTypeReference<LyricDto> localVarReturnType = new ParameterizedTypeReference<LyricDto>() {
        };
        return apiClient.invokeAPI("/Providers/Lyrics/{lyricId}", HttpMethod.GET, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Gets the remote lyrics.
     * 
     * <p>
     * <b>200</b> - File returned.
     * <p>
     * <b>404</b> - Lyric not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param lyricId The remote provider item id.
     * @return LyricDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<LyricDto> getRemoteLyrics(String lyricId) throws WebClientResponseException {
        ParameterizedTypeReference<LyricDto> localVarReturnType = new ParameterizedTypeReference<LyricDto>() {
        };
        return getRemoteLyricsRequestCreation(lyricId).bodyToMono(localVarReturnType);
    }

    /**
     * Gets the remote lyrics.
     * 
     * <p>
     * <b>200</b> - File returned.
     * <p>
     * <b>404</b> - Lyric not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param lyricId The remote provider item id.
     * @return ResponseEntity&lt;LyricDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<LyricDto>> getRemoteLyricsWithHttpInfo(String lyricId)
            throws WebClientResponseException {
        ParameterizedTypeReference<LyricDto> localVarReturnType = new ParameterizedTypeReference<LyricDto>() {
        };
        return getRemoteLyricsRequestCreation(lyricId).toEntity(localVarReturnType);
    }

    /**
     * Gets the remote lyrics.
     * 
     * <p>
     * <b>200</b> - File returned.
     * <p>
     * <b>404</b> - Lyric not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param lyricId The remote provider item id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getRemoteLyricsWithResponseSpec(String lyricId) throws WebClientResponseException {
        return getRemoteLyricsRequestCreation(lyricId);
    }

    /**
     * Search remote lyrics.
     * 
     * <p>
     * <b>200</b> - Lyrics retrieved.
     * <p>
     * <b>404</b> - Item not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId The item id.
     * @return List&lt;RemoteLyricInfoDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec searchRemoteLyricsRequestCreation(UUID itemId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'itemId' when calling searchRemoteLyrics",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("itemId", itemId);

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

        ParameterizedTypeReference<RemoteLyricInfoDto> localVarReturnType = new ParameterizedTypeReference<RemoteLyricInfoDto>() {
        };
        return apiClient.invokeAPI("/Audio/{itemId}/RemoteSearch/Lyrics", HttpMethod.GET, pathParams, queryParams,
                postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType,
                localVarAuthNames, localVarReturnType);
    }

    /**
     * Search remote lyrics.
     * 
     * <p>
     * <b>200</b> - Lyrics retrieved.
     * <p>
     * <b>404</b> - Item not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId The item id.
     * @return List&lt;RemoteLyricInfoDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Flux<RemoteLyricInfoDto> searchRemoteLyrics(UUID itemId) throws WebClientResponseException {
        ParameterizedTypeReference<RemoteLyricInfoDto> localVarReturnType = new ParameterizedTypeReference<RemoteLyricInfoDto>() {
        };
        return searchRemoteLyricsRequestCreation(itemId).bodyToFlux(localVarReturnType);
    }

    /**
     * Search remote lyrics.
     * 
     * <p>
     * <b>200</b> - Lyrics retrieved.
     * <p>
     * <b>404</b> - Item not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId The item id.
     * @return ResponseEntity&lt;List&lt;RemoteLyricInfoDto&gt;&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<List<RemoteLyricInfoDto>>> searchRemoteLyricsWithHttpInfo(UUID itemId)
            throws WebClientResponseException {
        ParameterizedTypeReference<RemoteLyricInfoDto> localVarReturnType = new ParameterizedTypeReference<RemoteLyricInfoDto>() {
        };
        return searchRemoteLyricsRequestCreation(itemId).toEntityList(localVarReturnType);
    }

    /**
     * Search remote lyrics.
     * 
     * <p>
     * <b>200</b> - Lyrics retrieved.
     * <p>
     * <b>404</b> - Item not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId The item id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec searchRemoteLyricsWithResponseSpec(UUID itemId) throws WebClientResponseException {
        return searchRemoteLyricsRequestCreation(itemId);
    }

    /**
     * Upload an external lyric file.
     * 
     * <p>
     * <b>200</b> - Lyrics uploaded.
     * <p>
     * <b>400</b> - Error processing upload.
     * <p>
     * <b>404</b> - Item not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId The item the lyric belongs to.
     * @param fileName Name of the file being uploaded.
     * @param body The body parameter
     * @return LyricDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec uploadLyricsRequestCreation(UUID itemId, String fileName, File body)
            throws WebClientResponseException {
        Object postBody = body;
        // verify the required parameter 'itemId' is set
        if (itemId == null) {
            throw new WebClientResponseException("Missing the required parameter 'itemId' when calling uploadLyrics",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'fileName' is set
        if (fileName == null) {
            throw new WebClientResponseException("Missing the required parameter 'fileName' when calling uploadLyrics",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("itemId", itemId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "fileName", fileName));

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { "text/plain" };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<LyricDto> localVarReturnType = new ParameterizedTypeReference<LyricDto>() {
        };
        return apiClient.invokeAPI("/Audio/{itemId}/Lyrics", HttpMethod.POST, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Upload an external lyric file.
     * 
     * <p>
     * <b>200</b> - Lyrics uploaded.
     * <p>
     * <b>400</b> - Error processing upload.
     * <p>
     * <b>404</b> - Item not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId The item the lyric belongs to.
     * @param fileName Name of the file being uploaded.
     * @param body The body parameter
     * @return LyricDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<LyricDto> uploadLyrics(UUID itemId, String fileName, File body) throws WebClientResponseException {
        ParameterizedTypeReference<LyricDto> localVarReturnType = new ParameterizedTypeReference<LyricDto>() {
        };
        return uploadLyricsRequestCreation(itemId, fileName, body).bodyToMono(localVarReturnType);
    }

    /**
     * Upload an external lyric file.
     * 
     * <p>
     * <b>200</b> - Lyrics uploaded.
     * <p>
     * <b>400</b> - Error processing upload.
     * <p>
     * <b>404</b> - Item not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId The item the lyric belongs to.
     * @param fileName Name of the file being uploaded.
     * @param body The body parameter
     * @return ResponseEntity&lt;LyricDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<LyricDto>> uploadLyricsWithHttpInfo(UUID itemId, String fileName, File body)
            throws WebClientResponseException {
        ParameterizedTypeReference<LyricDto> localVarReturnType = new ParameterizedTypeReference<LyricDto>() {
        };
        return uploadLyricsRequestCreation(itemId, fileName, body).toEntity(localVarReturnType);
    }

    /**
     * Upload an external lyric file.
     * 
     * <p>
     * <b>200</b> - Lyrics uploaded.
     * <p>
     * <b>400</b> - Error processing upload.
     * <p>
     * <b>404</b> - Item not found.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param itemId The item the lyric belongs to.
     * @param fileName Name of the file being uploaded.
     * @param body The body parameter
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec uploadLyricsWithResponseSpec(UUID itemId, String fileName, File body)
            throws WebClientResponseException {
        return uploadLyricsRequestCreation(itemId, fileName, body);
    }
}
