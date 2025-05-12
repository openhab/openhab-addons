package org.openhab.binding.jellyfin.internal.api.version.legacy;

import org.openhab.binding.jellyfin.internal.api.version.ApiClient;

import org.openhab.binding.jellyfin.internal.api.version.legacy.model.AdminNotificationDto;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.NameIdPair;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.NotificationResultDto;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.NotificationTypeInfo;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.NotificationsSummaryDto;

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
public class NotificationsApi {
    private ApiClient apiClient;

    public NotificationsApi() {
        this(new ApiClient());
    }

    @Autowired
    public NotificationsApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    
    /**
     * Sends a notification to all admins.
     * 
     * <p><b>204</b> - Notification sent.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param adminNotificationDto The notification request.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec createAdminNotificationRequestCreation(AdminNotificationDto adminNotificationDto) throws WebClientResponseException {
        Object postBody = adminNotificationDto;
        // verify the required parameter 'adminNotificationDto' is set
        if (adminNotificationDto == null) {
            throw new WebClientResponseException("Missing the required parameter 'adminNotificationDto' when calling createAdminNotification", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { 
            "application/json", "text/json", "application/*+json"
        };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return apiClient.invokeAPI("/Notifications/Admin", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Sends a notification to all admins.
     * 
     * <p><b>204</b> - Notification sent.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param adminNotificationDto The notification request.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> createAdminNotification(AdminNotificationDto adminNotificationDto) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return createAdminNotificationRequestCreation(adminNotificationDto).bodyToMono(localVarReturnType);
    }

    /**
     * Sends a notification to all admins.
     * 
     * <p><b>204</b> - Notification sent.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param adminNotificationDto The notification request.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> createAdminNotificationWithHttpInfo(AdminNotificationDto adminNotificationDto) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return createAdminNotificationRequestCreation(adminNotificationDto).toEntity(localVarReturnType);
    }

    /**
     * Sends a notification to all admins.
     * 
     * <p><b>204</b> - Notification sent.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param adminNotificationDto The notification request.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec createAdminNotificationWithResponseSpec(AdminNotificationDto adminNotificationDto) throws WebClientResponseException {
        return createAdminNotificationRequestCreation(adminNotificationDto);
    }

    /**
     * Gets notification services.
     * 
     * <p><b>200</b> - All notification services returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @return List&lt;NameIdPair&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getNotificationServicesRequestCreation() throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<NameIdPair> localVarReturnType = new ParameterizedTypeReference<NameIdPair>() {};
        return apiClient.invokeAPI("/Notifications/Services", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets notification services.
     * 
     * <p><b>200</b> - All notification services returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @return List&lt;NameIdPair&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Flux<NameIdPair> getNotificationServices() throws WebClientResponseException {
        ParameterizedTypeReference<NameIdPair> localVarReturnType = new ParameterizedTypeReference<NameIdPair>() {};
        return getNotificationServicesRequestCreation().bodyToFlux(localVarReturnType);
    }

    /**
     * Gets notification services.
     * 
     * <p><b>200</b> - All notification services returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @return ResponseEntity&lt;List&lt;NameIdPair&gt;&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<List<NameIdPair>>> getNotificationServicesWithHttpInfo() throws WebClientResponseException {
        ParameterizedTypeReference<NameIdPair> localVarReturnType = new ParameterizedTypeReference<NameIdPair>() {};
        return getNotificationServicesRequestCreation().toEntityList(localVarReturnType);
    }

    /**
     * Gets notification services.
     * 
     * <p><b>200</b> - All notification services returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getNotificationServicesWithResponseSpec() throws WebClientResponseException {
        return getNotificationServicesRequestCreation();
    }

    /**
     * Gets notification types.
     * 
     * <p><b>200</b> - All notification types returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @return List&lt;NotificationTypeInfo&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getNotificationTypesRequestCreation() throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<NotificationTypeInfo> localVarReturnType = new ParameterizedTypeReference<NotificationTypeInfo>() {};
        return apiClient.invokeAPI("/Notifications/Types", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets notification types.
     * 
     * <p><b>200</b> - All notification types returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @return List&lt;NotificationTypeInfo&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Flux<NotificationTypeInfo> getNotificationTypes() throws WebClientResponseException {
        ParameterizedTypeReference<NotificationTypeInfo> localVarReturnType = new ParameterizedTypeReference<NotificationTypeInfo>() {};
        return getNotificationTypesRequestCreation().bodyToFlux(localVarReturnType);
    }

    /**
     * Gets notification types.
     * 
     * <p><b>200</b> - All notification types returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @return ResponseEntity&lt;List&lt;NotificationTypeInfo&gt;&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<List<NotificationTypeInfo>>> getNotificationTypesWithHttpInfo() throws WebClientResponseException {
        ParameterizedTypeReference<NotificationTypeInfo> localVarReturnType = new ParameterizedTypeReference<NotificationTypeInfo>() {};
        return getNotificationTypesRequestCreation().toEntityList(localVarReturnType);
    }

    /**
     * Gets notification types.
     * 
     * <p><b>200</b> - All notification types returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getNotificationTypesWithResponseSpec() throws WebClientResponseException {
        return getNotificationTypesRequestCreation();
    }

    /**
     * Gets a user&#39;s notifications.
     * 
     * <p><b>200</b> - Notifications returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId The userId parameter
     * @return NotificationResultDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getNotificationsRequestCreation(String userId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'userId' is set
        if (userId == null) {
            throw new WebClientResponseException("Missing the required parameter 'userId' when calling getNotifications", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("userId", userId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<NotificationResultDto> localVarReturnType = new ParameterizedTypeReference<NotificationResultDto>() {};
        return apiClient.invokeAPI("/Notifications/{userId}", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets a user&#39;s notifications.
     * 
     * <p><b>200</b> - Notifications returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId The userId parameter
     * @return NotificationResultDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<NotificationResultDto> getNotifications(String userId) throws WebClientResponseException {
        ParameterizedTypeReference<NotificationResultDto> localVarReturnType = new ParameterizedTypeReference<NotificationResultDto>() {};
        return getNotificationsRequestCreation(userId).bodyToMono(localVarReturnType);
    }

    /**
     * Gets a user&#39;s notifications.
     * 
     * <p><b>200</b> - Notifications returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId The userId parameter
     * @return ResponseEntity&lt;NotificationResultDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<NotificationResultDto>> getNotificationsWithHttpInfo(String userId) throws WebClientResponseException {
        ParameterizedTypeReference<NotificationResultDto> localVarReturnType = new ParameterizedTypeReference<NotificationResultDto>() {};
        return getNotificationsRequestCreation(userId).toEntity(localVarReturnType);
    }

    /**
     * Gets a user&#39;s notifications.
     * 
     * <p><b>200</b> - Notifications returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId The userId parameter
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getNotificationsWithResponseSpec(String userId) throws WebClientResponseException {
        return getNotificationsRequestCreation(userId);
    }

    /**
     * Gets a user&#39;s notification summary.
     * 
     * <p><b>200</b> - Summary of user&#39;s notifications returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId The userId parameter
     * @return NotificationsSummaryDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getNotificationsSummaryRequestCreation(String userId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'userId' is set
        if (userId == null) {
            throw new WebClientResponseException("Missing the required parameter 'userId' when calling getNotificationsSummary", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("userId", userId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<NotificationsSummaryDto> localVarReturnType = new ParameterizedTypeReference<NotificationsSummaryDto>() {};
        return apiClient.invokeAPI("/Notifications/{userId}/Summary", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets a user&#39;s notification summary.
     * 
     * <p><b>200</b> - Summary of user&#39;s notifications returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId The userId parameter
     * @return NotificationsSummaryDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<NotificationsSummaryDto> getNotificationsSummary(String userId) throws WebClientResponseException {
        ParameterizedTypeReference<NotificationsSummaryDto> localVarReturnType = new ParameterizedTypeReference<NotificationsSummaryDto>() {};
        return getNotificationsSummaryRequestCreation(userId).bodyToMono(localVarReturnType);
    }

    /**
     * Gets a user&#39;s notification summary.
     * 
     * <p><b>200</b> - Summary of user&#39;s notifications returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId The userId parameter
     * @return ResponseEntity&lt;NotificationsSummaryDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<NotificationsSummaryDto>> getNotificationsSummaryWithHttpInfo(String userId) throws WebClientResponseException {
        ParameterizedTypeReference<NotificationsSummaryDto> localVarReturnType = new ParameterizedTypeReference<NotificationsSummaryDto>() {};
        return getNotificationsSummaryRequestCreation(userId).toEntity(localVarReturnType);
    }

    /**
     * Gets a user&#39;s notification summary.
     * 
     * <p><b>200</b> - Summary of user&#39;s notifications returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId The userId parameter
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getNotificationsSummaryWithResponseSpec(String userId) throws WebClientResponseException {
        return getNotificationsSummaryRequestCreation(userId);
    }

    /**
     * Sets notifications as read.
     * 
     * <p><b>204</b> - Notifications set as read.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId The userId parameter
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec setReadRequestCreation(String userId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'userId' is set
        if (userId == null) {
            throw new WebClientResponseException("Missing the required parameter 'userId' when calling setRead", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("userId", userId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return apiClient.invokeAPI("/Notifications/{userId}/Read", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Sets notifications as read.
     * 
     * <p><b>204</b> - Notifications set as read.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId The userId parameter
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> setRead(String userId) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return setReadRequestCreation(userId).bodyToMono(localVarReturnType);
    }

    /**
     * Sets notifications as read.
     * 
     * <p><b>204</b> - Notifications set as read.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId The userId parameter
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> setReadWithHttpInfo(String userId) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return setReadRequestCreation(userId).toEntity(localVarReturnType);
    }

    /**
     * Sets notifications as read.
     * 
     * <p><b>204</b> - Notifications set as read.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId The userId parameter
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec setReadWithResponseSpec(String userId) throws WebClientResponseException {
        return setReadRequestCreation(userId);
    }

    /**
     * Sets notifications as unread.
     * 
     * <p><b>204</b> - Notifications set as unread.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId The userId parameter
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec setUnreadRequestCreation(String userId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'userId' is set
        if (userId == null) {
            throw new WebClientResponseException("Missing the required parameter 'userId' when calling setUnread", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("userId", userId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return apiClient.invokeAPI("/Notifications/{userId}/Unread", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Sets notifications as unread.
     * 
     * <p><b>204</b> - Notifications set as unread.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId The userId parameter
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> setUnread(String userId) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return setUnreadRequestCreation(userId).bodyToMono(localVarReturnType);
    }

    /**
     * Sets notifications as unread.
     * 
     * <p><b>204</b> - Notifications set as unread.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId The userId parameter
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> setUnreadWithHttpInfo(String userId) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        return setUnreadRequestCreation(userId).toEntity(localVarReturnType);
    }

    /**
     * Sets notifications as unread.
     * 
     * <p><b>204</b> - Notifications set as unread.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param userId The userId parameter
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec setUnreadWithResponseSpec(String userId) throws WebClientResponseException {
        return setUnreadRequestCreation(userId);
    }
}
