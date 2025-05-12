package org.openhab.binding.jellyfin.internal.api.version.current;

import org.openhab.binding.jellyfin.internal.api.version.ApiClient;

import org.openhab.binding.jellyfin.internal.api.version.current.model.ActivityLogEntryQueryResult;
import java.time.OffsetDateTime;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.12.0")
public class ActivityLogApi {
    private ApiClient apiClient;

    public ActivityLogApi() {
        this(new ApiClient());
    }

    @Autowired
    public ActivityLogApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    
    /**
     * Gets activity log entries.
     * 
     * <p><b>200</b> - Activity log returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the results.
     * @param limit Optional. The maximum number of records to return.
     * @param minDate Optional. The minimum date. Format &#x3D; ISO.
     * @param hasUserId Optional. Filter log entries if it has user id, or not.
     * @return ActivityLogEntryQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getLogEntriesRequestCreation(Integer startIndex, Integer limit, OffsetDateTime minDate, Boolean hasUserId) throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "startIndex", startIndex));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "limit", limit));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "minDate", minDate));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "hasUserId", hasUserId));
        
        final String[] localVarAccepts = { 
            "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<ActivityLogEntryQueryResult> localVarReturnType = new ParameterizedTypeReference<ActivityLogEntryQueryResult>() {};
        return apiClient.invokeAPI("/System/ActivityLog/Entries", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets activity log entries.
     * 
     * <p><b>200</b> - Activity log returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the results.
     * @param limit Optional. The maximum number of records to return.
     * @param minDate Optional. The minimum date. Format &#x3D; ISO.
     * @param hasUserId Optional. Filter log entries if it has user id, or not.
     * @return ActivityLogEntryQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ActivityLogEntryQueryResult> getLogEntries(Integer startIndex, Integer limit, OffsetDateTime minDate, Boolean hasUserId) throws WebClientResponseException {
        ParameterizedTypeReference<ActivityLogEntryQueryResult> localVarReturnType = new ParameterizedTypeReference<ActivityLogEntryQueryResult>() {};
        return getLogEntriesRequestCreation(startIndex, limit, minDate, hasUserId).bodyToMono(localVarReturnType);
    }

    /**
     * Gets activity log entries.
     * 
     * <p><b>200</b> - Activity log returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the results.
     * @param limit Optional. The maximum number of records to return.
     * @param minDate Optional. The minimum date. Format &#x3D; ISO.
     * @param hasUserId Optional. Filter log entries if it has user id, or not.
     * @return ResponseEntity&lt;ActivityLogEntryQueryResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<ActivityLogEntryQueryResult>> getLogEntriesWithHttpInfo(Integer startIndex, Integer limit, OffsetDateTime minDate, Boolean hasUserId) throws WebClientResponseException {
        ParameterizedTypeReference<ActivityLogEntryQueryResult> localVarReturnType = new ParameterizedTypeReference<ActivityLogEntryQueryResult>() {};
        return getLogEntriesRequestCreation(startIndex, limit, minDate, hasUserId).toEntity(localVarReturnType);
    }

    /**
     * Gets activity log entries.
     * 
     * <p><b>200</b> - Activity log returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the results.
     * @param limit Optional. The maximum number of records to return.
     * @param minDate Optional. The minimum date. Format &#x3D; ISO.
     * @param hasUserId Optional. Filter log entries if it has user id, or not.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getLogEntriesWithResponseSpec(Integer startIndex, Integer limit, OffsetDateTime minDate, Boolean hasUserId) throws WebClientResponseException {
        return getLogEntriesRequestCreation(startIndex, limit, minDate, hasUserId);
    }
}
