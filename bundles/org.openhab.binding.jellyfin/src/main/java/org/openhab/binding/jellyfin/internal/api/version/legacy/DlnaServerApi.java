package org.openhab.binding.jellyfin.internal.api.version.legacy;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
public class DlnaServerApi {
    private ApiClient apiClient;

    public DlnaServerApi() {
        this(new ApiClient());
    }

    @Autowired
    public DlnaServerApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Gets Dlna media receiver registrar xml.
     * 
     * <p>
     * <b>200</b> - Dlna media receiver registrar xml returned.
     * <p>
     * <b>503</b> - DLNA is disabled.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param serverId Server UUID.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getConnectionManagerRequestCreation(String serverId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'serverId' is set
        if (serverId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'serverId' when calling getConnectionManager",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("serverId", serverId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { "text/xml" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return apiClient.invokeAPI("/Dlna/{serverId}/ConnectionManager", HttpMethod.GET, pathParams, queryParams,
                postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType,
                localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets Dlna media receiver registrar xml.
     * 
     * <p>
     * <b>200</b> - Dlna media receiver registrar xml returned.
     * <p>
     * <b>503</b> - DLNA is disabled.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param serverId Server UUID.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<File> getConnectionManager(String serverId) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return getConnectionManagerRequestCreation(serverId).bodyToMono(localVarReturnType);
    }

    /**
     * Gets Dlna media receiver registrar xml.
     * 
     * <p>
     * <b>200</b> - Dlna media receiver registrar xml returned.
     * <p>
     * <b>503</b> - DLNA is disabled.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param serverId Server UUID.
     * @return ResponseEntity&lt;File&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<File>> getConnectionManagerWithHttpInfo(String serverId)
            throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return getConnectionManagerRequestCreation(serverId).toEntity(localVarReturnType);
    }

    /**
     * Gets Dlna media receiver registrar xml.
     * 
     * <p>
     * <b>200</b> - Dlna media receiver registrar xml returned.
     * <p>
     * <b>503</b> - DLNA is disabled.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param serverId Server UUID.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getConnectionManagerWithResponseSpec(String serverId) throws WebClientResponseException {
        return getConnectionManagerRequestCreation(serverId);
    }

    /**
     * Gets Dlna media receiver registrar xml.
     * 
     * <p>
     * <b>200</b> - Dlna media receiver registrar xml returned.
     * <p>
     * <b>503</b> - DLNA is disabled.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param serverId Server UUID.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getConnectionManager2RequestCreation(String serverId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'serverId' is set
        if (serverId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'serverId' when calling getConnectionManager2",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("serverId", serverId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { "text/xml" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return apiClient.invokeAPI("/Dlna/{serverId}/ConnectionManager/ConnectionManager", HttpMethod.GET, pathParams,
                queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType,
                localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets Dlna media receiver registrar xml.
     * 
     * <p>
     * <b>200</b> - Dlna media receiver registrar xml returned.
     * <p>
     * <b>503</b> - DLNA is disabled.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param serverId Server UUID.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<File> getConnectionManager2(String serverId) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return getConnectionManager2RequestCreation(serverId).bodyToMono(localVarReturnType);
    }

    /**
     * Gets Dlna media receiver registrar xml.
     * 
     * <p>
     * <b>200</b> - Dlna media receiver registrar xml returned.
     * <p>
     * <b>503</b> - DLNA is disabled.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param serverId Server UUID.
     * @return ResponseEntity&lt;File&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<File>> getConnectionManager2WithHttpInfo(String serverId)
            throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return getConnectionManager2RequestCreation(serverId).toEntity(localVarReturnType);
    }

    /**
     * Gets Dlna media receiver registrar xml.
     * 
     * <p>
     * <b>200</b> - Dlna media receiver registrar xml returned.
     * <p>
     * <b>503</b> - DLNA is disabled.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param serverId Server UUID.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getConnectionManager2WithResponseSpec(String serverId) throws WebClientResponseException {
        return getConnectionManager2RequestCreation(serverId);
    }

    /**
     * Gets Dlna media receiver registrar xml.
     * 
     * <p>
     * <b>200</b> - Dlna media receiver registrar xml returned.
     * <p>
     * <b>503</b> - DLNA is disabled.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param serverId Server UUID.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getConnectionManager3RequestCreation(String serverId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'serverId' is set
        if (serverId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'serverId' when calling getConnectionManager3",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("serverId", serverId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { "text/xml" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return apiClient.invokeAPI("/Dlna/{serverId}/ConnectionManager/ConnectionManager.xml", HttpMethod.GET,
                pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept,
                localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets Dlna media receiver registrar xml.
     * 
     * <p>
     * <b>200</b> - Dlna media receiver registrar xml returned.
     * <p>
     * <b>503</b> - DLNA is disabled.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param serverId Server UUID.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<File> getConnectionManager3(String serverId) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return getConnectionManager3RequestCreation(serverId).bodyToMono(localVarReturnType);
    }

    /**
     * Gets Dlna media receiver registrar xml.
     * 
     * <p>
     * <b>200</b> - Dlna media receiver registrar xml returned.
     * <p>
     * <b>503</b> - DLNA is disabled.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param serverId Server UUID.
     * @return ResponseEntity&lt;File&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<File>> getConnectionManager3WithHttpInfo(String serverId)
            throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return getConnectionManager3RequestCreation(serverId).toEntity(localVarReturnType);
    }

    /**
     * Gets Dlna media receiver registrar xml.
     * 
     * <p>
     * <b>200</b> - Dlna media receiver registrar xml returned.
     * <p>
     * <b>503</b> - DLNA is disabled.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param serverId Server UUID.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getConnectionManager3WithResponseSpec(String serverId) throws WebClientResponseException {
        return getConnectionManager3RequestCreation(serverId);
    }

    /**
     * Gets Dlna content directory xml.
     * 
     * <p>
     * <b>200</b> - Dlna content directory returned.
     * <p>
     * <b>503</b> - DLNA is disabled.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param serverId Server UUID.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getContentDirectoryRequestCreation(String serverId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'serverId' is set
        if (serverId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'serverId' when calling getContentDirectory",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("serverId", serverId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { "text/xml" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return apiClient.invokeAPI("/Dlna/{serverId}/ContentDirectory", HttpMethod.GET, pathParams, queryParams,
                postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType,
                localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets Dlna content directory xml.
     * 
     * <p>
     * <b>200</b> - Dlna content directory returned.
     * <p>
     * <b>503</b> - DLNA is disabled.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param serverId Server UUID.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<File> getContentDirectory(String serverId) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return getContentDirectoryRequestCreation(serverId).bodyToMono(localVarReturnType);
    }

    /**
     * Gets Dlna content directory xml.
     * 
     * <p>
     * <b>200</b> - Dlna content directory returned.
     * <p>
     * <b>503</b> - DLNA is disabled.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param serverId Server UUID.
     * @return ResponseEntity&lt;File&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<File>> getContentDirectoryWithHttpInfo(String serverId)
            throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return getContentDirectoryRequestCreation(serverId).toEntity(localVarReturnType);
    }

    /**
     * Gets Dlna content directory xml.
     * 
     * <p>
     * <b>200</b> - Dlna content directory returned.
     * <p>
     * <b>503</b> - DLNA is disabled.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param serverId Server UUID.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getContentDirectoryWithResponseSpec(String serverId) throws WebClientResponseException {
        return getContentDirectoryRequestCreation(serverId);
    }

    /**
     * Gets Dlna content directory xml.
     * 
     * <p>
     * <b>200</b> - Dlna content directory returned.
     * <p>
     * <b>503</b> - DLNA is disabled.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param serverId Server UUID.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getContentDirectory2RequestCreation(String serverId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'serverId' is set
        if (serverId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'serverId' when calling getContentDirectory2",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("serverId", serverId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { "text/xml" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return apiClient.invokeAPI("/Dlna/{serverId}/ContentDirectory/ContentDirectory", HttpMethod.GET, pathParams,
                queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType,
                localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets Dlna content directory xml.
     * 
     * <p>
     * <b>200</b> - Dlna content directory returned.
     * <p>
     * <b>503</b> - DLNA is disabled.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param serverId Server UUID.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<File> getContentDirectory2(String serverId) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return getContentDirectory2RequestCreation(serverId).bodyToMono(localVarReturnType);
    }

    /**
     * Gets Dlna content directory xml.
     * 
     * <p>
     * <b>200</b> - Dlna content directory returned.
     * <p>
     * <b>503</b> - DLNA is disabled.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param serverId Server UUID.
     * @return ResponseEntity&lt;File&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<File>> getContentDirectory2WithHttpInfo(String serverId)
            throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return getContentDirectory2RequestCreation(serverId).toEntity(localVarReturnType);
    }

    /**
     * Gets Dlna content directory xml.
     * 
     * <p>
     * <b>200</b> - Dlna content directory returned.
     * <p>
     * <b>503</b> - DLNA is disabled.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param serverId Server UUID.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getContentDirectory2WithResponseSpec(String serverId) throws WebClientResponseException {
        return getContentDirectory2RequestCreation(serverId);
    }

    /**
     * Gets Dlna content directory xml.
     * 
     * <p>
     * <b>200</b> - Dlna content directory returned.
     * <p>
     * <b>503</b> - DLNA is disabled.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param serverId Server UUID.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getContentDirectory3RequestCreation(String serverId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'serverId' is set
        if (serverId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'serverId' when calling getContentDirectory3",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("serverId", serverId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { "text/xml" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return apiClient.invokeAPI("/Dlna/{serverId}/ContentDirectory/ContentDirectory.xml", HttpMethod.GET, pathParams,
                queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType,
                localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets Dlna content directory xml.
     * 
     * <p>
     * <b>200</b> - Dlna content directory returned.
     * <p>
     * <b>503</b> - DLNA is disabled.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param serverId Server UUID.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<File> getContentDirectory3(String serverId) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return getContentDirectory3RequestCreation(serverId).bodyToMono(localVarReturnType);
    }

    /**
     * Gets Dlna content directory xml.
     * 
     * <p>
     * <b>200</b> - Dlna content directory returned.
     * <p>
     * <b>503</b> - DLNA is disabled.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param serverId Server UUID.
     * @return ResponseEntity&lt;File&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<File>> getContentDirectory3WithHttpInfo(String serverId)
            throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return getContentDirectory3RequestCreation(serverId).toEntity(localVarReturnType);
    }

    /**
     * Gets Dlna content directory xml.
     * 
     * <p>
     * <b>200</b> - Dlna content directory returned.
     * <p>
     * <b>503</b> - DLNA is disabled.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param serverId Server UUID.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getContentDirectory3WithResponseSpec(String serverId) throws WebClientResponseException {
        return getContentDirectory3RequestCreation(serverId);
    }

    /**
     * Get Description Xml.
     * 
     * <p>
     * <b>200</b> - Description xml returned.
     * <p>
     * <b>503</b> - DLNA is disabled.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param serverId Server UUID.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getDescriptionXmlRequestCreation(String serverId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'serverId' is set
        if (serverId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'serverId' when calling getDescriptionXml",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("serverId", serverId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { "text/xml" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return apiClient.invokeAPI("/Dlna/{serverId}/description", HttpMethod.GET, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Get Description Xml.
     * 
     * <p>
     * <b>200</b> - Description xml returned.
     * <p>
     * <b>503</b> - DLNA is disabled.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param serverId Server UUID.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<File> getDescriptionXml(String serverId) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return getDescriptionXmlRequestCreation(serverId).bodyToMono(localVarReturnType);
    }

    /**
     * Get Description Xml.
     * 
     * <p>
     * <b>200</b> - Description xml returned.
     * <p>
     * <b>503</b> - DLNA is disabled.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param serverId Server UUID.
     * @return ResponseEntity&lt;File&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<File>> getDescriptionXmlWithHttpInfo(String serverId) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return getDescriptionXmlRequestCreation(serverId).toEntity(localVarReturnType);
    }

    /**
     * Get Description Xml.
     * 
     * <p>
     * <b>200</b> - Description xml returned.
     * <p>
     * <b>503</b> - DLNA is disabled.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param serverId Server UUID.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getDescriptionXmlWithResponseSpec(String serverId) throws WebClientResponseException {
        return getDescriptionXmlRequestCreation(serverId);
    }

    /**
     * Get Description Xml.
     * 
     * <p>
     * <b>200</b> - Description xml returned.
     * <p>
     * <b>503</b> - DLNA is disabled.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param serverId Server UUID.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getDescriptionXml2RequestCreation(String serverId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'serverId' is set
        if (serverId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'serverId' when calling getDescriptionXml2",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("serverId", serverId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { "text/xml" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return apiClient.invokeAPI("/Dlna/{serverId}/description.xml", HttpMethod.GET, pathParams, queryParams,
                postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType,
                localVarAuthNames, localVarReturnType);
    }

    /**
     * Get Description Xml.
     * 
     * <p>
     * <b>200</b> - Description xml returned.
     * <p>
     * <b>503</b> - DLNA is disabled.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param serverId Server UUID.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<File> getDescriptionXml2(String serverId) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return getDescriptionXml2RequestCreation(serverId).bodyToMono(localVarReturnType);
    }

    /**
     * Get Description Xml.
     * 
     * <p>
     * <b>200</b> - Description xml returned.
     * <p>
     * <b>503</b> - DLNA is disabled.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param serverId Server UUID.
     * @return ResponseEntity&lt;File&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<File>> getDescriptionXml2WithHttpInfo(String serverId)
            throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return getDescriptionXml2RequestCreation(serverId).toEntity(localVarReturnType);
    }

    /**
     * Get Description Xml.
     * 
     * <p>
     * <b>200</b> - Description xml returned.
     * <p>
     * <b>503</b> - DLNA is disabled.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param serverId Server UUID.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getDescriptionXml2WithResponseSpec(String serverId) throws WebClientResponseException {
        return getDescriptionXml2RequestCreation(serverId);
    }

    /**
     * Gets a server icon.
     * 
     * <p>
     * <b>200</b> - Request processed.
     * <p>
     * <b>404</b> - Not Found.
     * <p>
     * <b>503</b> - DLNA is disabled.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param fileName The icon filename.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getIconRequestCreation(String fileName) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'fileName' is set
        if (fileName == null) {
            throw new WebClientResponseException("Missing the required parameter 'fileName' when calling getIcon",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("fileName", fileName);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { "image/*", "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return apiClient.invokeAPI("/Dlna/icons/{fileName}", HttpMethod.GET, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Gets a server icon.
     * 
     * <p>
     * <b>200</b> - Request processed.
     * <p>
     * <b>404</b> - Not Found.
     * <p>
     * <b>503</b> - DLNA is disabled.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param fileName The icon filename.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<File> getIcon(String fileName) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return getIconRequestCreation(fileName).bodyToMono(localVarReturnType);
    }

    /**
     * Gets a server icon.
     * 
     * <p>
     * <b>200</b> - Request processed.
     * <p>
     * <b>404</b> - Not Found.
     * <p>
     * <b>503</b> - DLNA is disabled.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param fileName The icon filename.
     * @return ResponseEntity&lt;File&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<File>> getIconWithHttpInfo(String fileName) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return getIconRequestCreation(fileName).toEntity(localVarReturnType);
    }

    /**
     * Gets a server icon.
     * 
     * <p>
     * <b>200</b> - Request processed.
     * <p>
     * <b>404</b> - Not Found.
     * <p>
     * <b>503</b> - DLNA is disabled.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param fileName The icon filename.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getIconWithResponseSpec(String fileName) throws WebClientResponseException {
        return getIconRequestCreation(fileName);
    }

    /**
     * Gets a server icon.
     * 
     * <p>
     * <b>200</b> - Request processed.
     * <p>
     * <b>404</b> - Not Found.
     * <p>
     * <b>503</b> - DLNA is disabled.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param serverId Server UUID.
     * @param fileName The icon filename.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getIconIdRequestCreation(String serverId, String fileName) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'serverId' is set
        if (serverId == null) {
            throw new WebClientResponseException("Missing the required parameter 'serverId' when calling getIconId",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'fileName' is set
        if (fileName == null) {
            throw new WebClientResponseException("Missing the required parameter 'fileName' when calling getIconId",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("serverId", serverId);
        pathParams.put("fileName", fileName);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { "image/*", "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return apiClient.invokeAPI("/Dlna/{serverId}/icons/{fileName}", HttpMethod.GET, pathParams, queryParams,
                postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType,
                localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets a server icon.
     * 
     * <p>
     * <b>200</b> - Request processed.
     * <p>
     * <b>404</b> - Not Found.
     * <p>
     * <b>503</b> - DLNA is disabled.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param serverId Server UUID.
     * @param fileName The icon filename.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<File> getIconId(String serverId, String fileName) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return getIconIdRequestCreation(serverId, fileName).bodyToMono(localVarReturnType);
    }

    /**
     * Gets a server icon.
     * 
     * <p>
     * <b>200</b> - Request processed.
     * <p>
     * <b>404</b> - Not Found.
     * <p>
     * <b>503</b> - DLNA is disabled.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param serverId Server UUID.
     * @param fileName The icon filename.
     * @return ResponseEntity&lt;File&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<File>> getIconIdWithHttpInfo(String serverId, String fileName)
            throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return getIconIdRequestCreation(serverId, fileName).toEntity(localVarReturnType);
    }

    /**
     * Gets a server icon.
     * 
     * <p>
     * <b>200</b> - Request processed.
     * <p>
     * <b>404</b> - Not Found.
     * <p>
     * <b>503</b> - DLNA is disabled.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param serverId Server UUID.
     * @param fileName The icon filename.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getIconIdWithResponseSpec(String serverId, String fileName) throws WebClientResponseException {
        return getIconIdRequestCreation(serverId, fileName);
    }

    /**
     * Gets Dlna media receiver registrar xml.
     * 
     * <p>
     * <b>200</b> - Dlna media receiver registrar xml returned.
     * <p>
     * <b>503</b> - DLNA is disabled.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param serverId Server UUID.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getMediaReceiverRegistrarRequestCreation(String serverId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'serverId' is set
        if (serverId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'serverId' when calling getMediaReceiverRegistrar",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("serverId", serverId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { "text/xml" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return apiClient.invokeAPI("/Dlna/{serverId}/MediaReceiverRegistrar", HttpMethod.GET, pathParams, queryParams,
                postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType,
                localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets Dlna media receiver registrar xml.
     * 
     * <p>
     * <b>200</b> - Dlna media receiver registrar xml returned.
     * <p>
     * <b>503</b> - DLNA is disabled.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param serverId Server UUID.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<File> getMediaReceiverRegistrar(String serverId) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return getMediaReceiverRegistrarRequestCreation(serverId).bodyToMono(localVarReturnType);
    }

    /**
     * Gets Dlna media receiver registrar xml.
     * 
     * <p>
     * <b>200</b> - Dlna media receiver registrar xml returned.
     * <p>
     * <b>503</b> - DLNA is disabled.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param serverId Server UUID.
     * @return ResponseEntity&lt;File&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<File>> getMediaReceiverRegistrarWithHttpInfo(String serverId)
            throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return getMediaReceiverRegistrarRequestCreation(serverId).toEntity(localVarReturnType);
    }

    /**
     * Gets Dlna media receiver registrar xml.
     * 
     * <p>
     * <b>200</b> - Dlna media receiver registrar xml returned.
     * <p>
     * <b>503</b> - DLNA is disabled.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param serverId Server UUID.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getMediaReceiverRegistrarWithResponseSpec(String serverId) throws WebClientResponseException {
        return getMediaReceiverRegistrarRequestCreation(serverId);
    }

    /**
     * Gets Dlna media receiver registrar xml.
     * 
     * <p>
     * <b>200</b> - Dlna media receiver registrar xml returned.
     * <p>
     * <b>503</b> - DLNA is disabled.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param serverId Server UUID.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getMediaReceiverRegistrar2RequestCreation(String serverId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'serverId' is set
        if (serverId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'serverId' when calling getMediaReceiverRegistrar2",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("serverId", serverId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { "text/xml" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return apiClient.invokeAPI("/Dlna/{serverId}/MediaReceiverRegistrar/MediaReceiverRegistrar", HttpMethod.GET,
                pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept,
                localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets Dlna media receiver registrar xml.
     * 
     * <p>
     * <b>200</b> - Dlna media receiver registrar xml returned.
     * <p>
     * <b>503</b> - DLNA is disabled.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param serverId Server UUID.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<File> getMediaReceiverRegistrar2(String serverId) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return getMediaReceiverRegistrar2RequestCreation(serverId).bodyToMono(localVarReturnType);
    }

    /**
     * Gets Dlna media receiver registrar xml.
     * 
     * <p>
     * <b>200</b> - Dlna media receiver registrar xml returned.
     * <p>
     * <b>503</b> - DLNA is disabled.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param serverId Server UUID.
     * @return ResponseEntity&lt;File&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<File>> getMediaReceiverRegistrar2WithHttpInfo(String serverId)
            throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return getMediaReceiverRegistrar2RequestCreation(serverId).toEntity(localVarReturnType);
    }

    /**
     * Gets Dlna media receiver registrar xml.
     * 
     * <p>
     * <b>200</b> - Dlna media receiver registrar xml returned.
     * <p>
     * <b>503</b> - DLNA is disabled.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param serverId Server UUID.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getMediaReceiverRegistrar2WithResponseSpec(String serverId) throws WebClientResponseException {
        return getMediaReceiverRegistrar2RequestCreation(serverId);
    }

    /**
     * Gets Dlna media receiver registrar xml.
     * 
     * <p>
     * <b>200</b> - Dlna media receiver registrar xml returned.
     * <p>
     * <b>503</b> - DLNA is disabled.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param serverId Server UUID.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getMediaReceiverRegistrar3RequestCreation(String serverId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'serverId' is set
        if (serverId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'serverId' when calling getMediaReceiverRegistrar3",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("serverId", serverId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { "text/xml" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return apiClient.invokeAPI("/Dlna/{serverId}/MediaReceiverRegistrar/MediaReceiverRegistrar.xml", HttpMethod.GET,
                pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept,
                localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets Dlna media receiver registrar xml.
     * 
     * <p>
     * <b>200</b> - Dlna media receiver registrar xml returned.
     * <p>
     * <b>503</b> - DLNA is disabled.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param serverId Server UUID.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<File> getMediaReceiverRegistrar3(String serverId) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return getMediaReceiverRegistrar3RequestCreation(serverId).bodyToMono(localVarReturnType);
    }

    /**
     * Gets Dlna media receiver registrar xml.
     * 
     * <p>
     * <b>200</b> - Dlna media receiver registrar xml returned.
     * <p>
     * <b>503</b> - DLNA is disabled.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param serverId Server UUID.
     * @return ResponseEntity&lt;File&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<File>> getMediaReceiverRegistrar3WithHttpInfo(String serverId)
            throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return getMediaReceiverRegistrar3RequestCreation(serverId).toEntity(localVarReturnType);
    }

    /**
     * Gets Dlna media receiver registrar xml.
     * 
     * <p>
     * <b>200</b> - Dlna media receiver registrar xml returned.
     * <p>
     * <b>503</b> - DLNA is disabled.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param serverId Server UUID.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getMediaReceiverRegistrar3WithResponseSpec(String serverId) throws WebClientResponseException {
        return getMediaReceiverRegistrar3RequestCreation(serverId);
    }

    /**
     * Process a connection manager control request.
     * 
     * <p>
     * <b>200</b> - Request processed.
     * <p>
     * <b>503</b> - DLNA is disabled.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param serverId Server UUID.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec processConnectionManagerControlRequestRequestCreation(String serverId)
            throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'serverId' is set
        if (serverId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'serverId' when calling processConnectionManagerControlRequest",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("serverId", serverId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { "text/xml" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return apiClient.invokeAPI("/Dlna/{serverId}/ConnectionManager/Control", HttpMethod.POST, pathParams,
                queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType,
                localVarAuthNames, localVarReturnType);
    }

    /**
     * Process a connection manager control request.
     * 
     * <p>
     * <b>200</b> - Request processed.
     * <p>
     * <b>503</b> - DLNA is disabled.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param serverId Server UUID.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<File> processConnectionManagerControlRequest(String serverId) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return processConnectionManagerControlRequestRequestCreation(serverId).bodyToMono(localVarReturnType);
    }

    /**
     * Process a connection manager control request.
     * 
     * <p>
     * <b>200</b> - Request processed.
     * <p>
     * <b>503</b> - DLNA is disabled.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param serverId Server UUID.
     * @return ResponseEntity&lt;File&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<File>> processConnectionManagerControlRequestWithHttpInfo(String serverId)
            throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return processConnectionManagerControlRequestRequestCreation(serverId).toEntity(localVarReturnType);
    }

    /**
     * Process a connection manager control request.
     * 
     * <p>
     * <b>200</b> - Request processed.
     * <p>
     * <b>503</b> - DLNA is disabled.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param serverId Server UUID.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec processConnectionManagerControlRequestWithResponseSpec(String serverId)
            throws WebClientResponseException {
        return processConnectionManagerControlRequestRequestCreation(serverId);
    }

    /**
     * Process a content directory control request.
     * 
     * <p>
     * <b>200</b> - Request processed.
     * <p>
     * <b>503</b> - DLNA is disabled.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param serverId Server UUID.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec processContentDirectoryControlRequestRequestCreation(String serverId)
            throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'serverId' is set
        if (serverId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'serverId' when calling processContentDirectoryControlRequest",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("serverId", serverId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { "text/xml" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return apiClient.invokeAPI("/Dlna/{serverId}/ContentDirectory/Control", HttpMethod.POST, pathParams,
                queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType,
                localVarAuthNames, localVarReturnType);
    }

    /**
     * Process a content directory control request.
     * 
     * <p>
     * <b>200</b> - Request processed.
     * <p>
     * <b>503</b> - DLNA is disabled.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param serverId Server UUID.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<File> processContentDirectoryControlRequest(String serverId) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return processContentDirectoryControlRequestRequestCreation(serverId).bodyToMono(localVarReturnType);
    }

    /**
     * Process a content directory control request.
     * 
     * <p>
     * <b>200</b> - Request processed.
     * <p>
     * <b>503</b> - DLNA is disabled.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param serverId Server UUID.
     * @return ResponseEntity&lt;File&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<File>> processContentDirectoryControlRequestWithHttpInfo(String serverId)
            throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return processContentDirectoryControlRequestRequestCreation(serverId).toEntity(localVarReturnType);
    }

    /**
     * Process a content directory control request.
     * 
     * <p>
     * <b>200</b> - Request processed.
     * <p>
     * <b>503</b> - DLNA is disabled.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param serverId Server UUID.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec processContentDirectoryControlRequestWithResponseSpec(String serverId)
            throws WebClientResponseException {
        return processContentDirectoryControlRequestRequestCreation(serverId);
    }

    /**
     * Process a media receiver registrar control request.
     * 
     * <p>
     * <b>200</b> - Request processed.
     * <p>
     * <b>503</b> - DLNA is disabled.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param serverId Server UUID.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec processMediaReceiverRegistrarControlRequestRequestCreation(String serverId)
            throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'serverId' is set
        if (serverId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'serverId' when calling processMediaReceiverRegistrarControlRequest",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("serverId", serverId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { "text/xml" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return apiClient.invokeAPI("/Dlna/{serverId}/MediaReceiverRegistrar/Control", HttpMethod.POST, pathParams,
                queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType,
                localVarAuthNames, localVarReturnType);
    }

    /**
     * Process a media receiver registrar control request.
     * 
     * <p>
     * <b>200</b> - Request processed.
     * <p>
     * <b>503</b> - DLNA is disabled.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param serverId Server UUID.
     * @return File
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<File> processMediaReceiverRegistrarControlRequest(String serverId) throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return processMediaReceiverRegistrarControlRequestRequestCreation(serverId).bodyToMono(localVarReturnType);
    }

    /**
     * Process a media receiver registrar control request.
     * 
     * <p>
     * <b>200</b> - Request processed.
     * <p>
     * <b>503</b> - DLNA is disabled.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param serverId Server UUID.
     * @return ResponseEntity&lt;File&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<File>> processMediaReceiverRegistrarControlRequestWithHttpInfo(String serverId)
            throws WebClientResponseException {
        ParameterizedTypeReference<File> localVarReturnType = new ParameterizedTypeReference<File>() {
        };
        return processMediaReceiverRegistrarControlRequestRequestCreation(serverId).toEntity(localVarReturnType);
    }

    /**
     * Process a media receiver registrar control request.
     * 
     * <p>
     * <b>200</b> - Request processed.
     * <p>
     * <b>503</b> - DLNA is disabled.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param serverId Server UUID.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec processMediaReceiverRegistrarControlRequestWithResponseSpec(String serverId)
            throws WebClientResponseException {
        return processMediaReceiverRegistrarControlRequestRequestCreation(serverId);
    }
}
