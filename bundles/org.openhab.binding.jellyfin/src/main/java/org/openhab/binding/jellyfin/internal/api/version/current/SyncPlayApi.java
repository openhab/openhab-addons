package org.openhab.binding.jellyfin.internal.api.version.current;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openhab.binding.jellyfin.internal.api.version.ApiClient;
import org.openhab.binding.jellyfin.internal.api.version.current.model.BufferRequestDto;
import org.openhab.binding.jellyfin.internal.api.version.current.model.GroupInfoDto;
import org.openhab.binding.jellyfin.internal.api.version.current.model.IgnoreWaitRequestDto;
import org.openhab.binding.jellyfin.internal.api.version.current.model.JoinGroupRequestDto;
import org.openhab.binding.jellyfin.internal.api.version.current.model.MovePlaylistItemRequestDto;
import org.openhab.binding.jellyfin.internal.api.version.current.model.NewGroupRequestDto;
import org.openhab.binding.jellyfin.internal.api.version.current.model.NextItemRequestDto;
import org.openhab.binding.jellyfin.internal.api.version.current.model.PingRequestDto;
import org.openhab.binding.jellyfin.internal.api.version.current.model.PlayRequestDto;
import org.openhab.binding.jellyfin.internal.api.version.current.model.PreviousItemRequestDto;
import org.openhab.binding.jellyfin.internal.api.version.current.model.QueueRequestDto;
import org.openhab.binding.jellyfin.internal.api.version.current.model.ReadyRequestDto;
import org.openhab.binding.jellyfin.internal.api.version.current.model.RemoveFromPlaylistRequestDto;
import org.openhab.binding.jellyfin.internal.api.version.current.model.SeekRequestDto;
import org.openhab.binding.jellyfin.internal.api.version.current.model.SetPlaylistItemRequestDto;
import org.openhab.binding.jellyfin.internal.api.version.current.model.SetRepeatModeRequestDto;
import org.openhab.binding.jellyfin.internal.api.version.current.model.SetShuffleModeRequestDto;
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
public class SyncPlayApi {
    private ApiClient apiClient;

    public SyncPlayApi() {
        this(new ApiClient());
    }

    @Autowired
    public SyncPlayApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Notify SyncPlay group that member is buffering.
     * 
     * <p>
     * <b>204</b> - Group state update sent to all group members.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param bufferRequestDto The player status.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec syncPlayBufferingRequestCreation(BufferRequestDto bufferRequestDto)
            throws WebClientResponseException {
        Object postBody = bufferRequestDto;
        // verify the required parameter 'bufferRequestDto' is set
        if (bufferRequestDto == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'bufferRequestDto' when calling syncPlayBuffering",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

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
        return apiClient.invokeAPI("/SyncPlay/Buffering", HttpMethod.POST, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Notify SyncPlay group that member is buffering.
     * 
     * <p>
     * <b>204</b> - Group state update sent to all group members.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param bufferRequestDto The player status.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> syncPlayBuffering(BufferRequestDto bufferRequestDto) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return syncPlayBufferingRequestCreation(bufferRequestDto).bodyToMono(localVarReturnType);
    }

    /**
     * Notify SyncPlay group that member is buffering.
     * 
     * <p>
     * <b>204</b> - Group state update sent to all group members.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param bufferRequestDto The player status.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> syncPlayBufferingWithHttpInfo(BufferRequestDto bufferRequestDto)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return syncPlayBufferingRequestCreation(bufferRequestDto).toEntity(localVarReturnType);
    }

    /**
     * Notify SyncPlay group that member is buffering.
     * 
     * <p>
     * <b>204</b> - Group state update sent to all group members.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param bufferRequestDto The player status.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec syncPlayBufferingWithResponseSpec(BufferRequestDto bufferRequestDto)
            throws WebClientResponseException {
        return syncPlayBufferingRequestCreation(bufferRequestDto);
    }

    /**
     * Create a new SyncPlay group.
     * 
     * <p>
     * <b>204</b> - New group created.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param newGroupRequestDto The settings of the new group.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec syncPlayCreateGroupRequestCreation(NewGroupRequestDto newGroupRequestDto)
            throws WebClientResponseException {
        Object postBody = newGroupRequestDto;
        // verify the required parameter 'newGroupRequestDto' is set
        if (newGroupRequestDto == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'newGroupRequestDto' when calling syncPlayCreateGroup",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

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
        return apiClient.invokeAPI("/SyncPlay/New", HttpMethod.POST, pathParams, queryParams, postBody, headerParams,
                cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Create a new SyncPlay group.
     * 
     * <p>
     * <b>204</b> - New group created.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param newGroupRequestDto The settings of the new group.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> syncPlayCreateGroup(NewGroupRequestDto newGroupRequestDto) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return syncPlayCreateGroupRequestCreation(newGroupRequestDto).bodyToMono(localVarReturnType);
    }

    /**
     * Create a new SyncPlay group.
     * 
     * <p>
     * <b>204</b> - New group created.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param newGroupRequestDto The settings of the new group.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> syncPlayCreateGroupWithHttpInfo(NewGroupRequestDto newGroupRequestDto)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return syncPlayCreateGroupRequestCreation(newGroupRequestDto).toEntity(localVarReturnType);
    }

    /**
     * Create a new SyncPlay group.
     * 
     * <p>
     * <b>204</b> - New group created.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param newGroupRequestDto The settings of the new group.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec syncPlayCreateGroupWithResponseSpec(NewGroupRequestDto newGroupRequestDto)
            throws WebClientResponseException {
        return syncPlayCreateGroupRequestCreation(newGroupRequestDto);
    }

    /**
     * Gets all SyncPlay groups.
     * 
     * <p>
     * <b>200</b> - Groups returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return List&lt;GroupInfoDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec syncPlayGetGroupsRequestCreation() throws WebClientResponseException {
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

        ParameterizedTypeReference<GroupInfoDto> localVarReturnType = new ParameterizedTypeReference<GroupInfoDto>() {
        };
        return apiClient.invokeAPI("/SyncPlay/List", HttpMethod.GET, pathParams, queryParams, postBody, headerParams,
                cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets all SyncPlay groups.
     * 
     * <p>
     * <b>200</b> - Groups returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return List&lt;GroupInfoDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Flux<GroupInfoDto> syncPlayGetGroups() throws WebClientResponseException {
        ParameterizedTypeReference<GroupInfoDto> localVarReturnType = new ParameterizedTypeReference<GroupInfoDto>() {
        };
        return syncPlayGetGroupsRequestCreation().bodyToFlux(localVarReturnType);
    }

    /**
     * Gets all SyncPlay groups.
     * 
     * <p>
     * <b>200</b> - Groups returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return ResponseEntity&lt;List&lt;GroupInfoDto&gt;&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<List<GroupInfoDto>>> syncPlayGetGroupsWithHttpInfo() throws WebClientResponseException {
        ParameterizedTypeReference<GroupInfoDto> localVarReturnType = new ParameterizedTypeReference<GroupInfoDto>() {
        };
        return syncPlayGetGroupsRequestCreation().toEntityList(localVarReturnType);
    }

    /**
     * Gets all SyncPlay groups.
     * 
     * <p>
     * <b>200</b> - Groups returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec syncPlayGetGroupsWithResponseSpec() throws WebClientResponseException {
        return syncPlayGetGroupsRequestCreation();
    }

    /**
     * Join an existing SyncPlay group.
     * 
     * <p>
     * <b>204</b> - Group join successful.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param joinGroupRequestDto The group to join.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec syncPlayJoinGroupRequestCreation(JoinGroupRequestDto joinGroupRequestDto)
            throws WebClientResponseException {
        Object postBody = joinGroupRequestDto;
        // verify the required parameter 'joinGroupRequestDto' is set
        if (joinGroupRequestDto == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'joinGroupRequestDto' when calling syncPlayJoinGroup",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

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
        return apiClient.invokeAPI("/SyncPlay/Join", HttpMethod.POST, pathParams, queryParams, postBody, headerParams,
                cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Join an existing SyncPlay group.
     * 
     * <p>
     * <b>204</b> - Group join successful.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param joinGroupRequestDto The group to join.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> syncPlayJoinGroup(JoinGroupRequestDto joinGroupRequestDto) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return syncPlayJoinGroupRequestCreation(joinGroupRequestDto).bodyToMono(localVarReturnType);
    }

    /**
     * Join an existing SyncPlay group.
     * 
     * <p>
     * <b>204</b> - Group join successful.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param joinGroupRequestDto The group to join.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> syncPlayJoinGroupWithHttpInfo(JoinGroupRequestDto joinGroupRequestDto)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return syncPlayJoinGroupRequestCreation(joinGroupRequestDto).toEntity(localVarReturnType);
    }

    /**
     * Join an existing SyncPlay group.
     * 
     * <p>
     * <b>204</b> - Group join successful.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param joinGroupRequestDto The group to join.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec syncPlayJoinGroupWithResponseSpec(JoinGroupRequestDto joinGroupRequestDto)
            throws WebClientResponseException {
        return syncPlayJoinGroupRequestCreation(joinGroupRequestDto);
    }

    /**
     * Leave the joined SyncPlay group.
     * 
     * <p>
     * <b>204</b> - Group leave successful.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec syncPlayLeaveGroupRequestCreation() throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

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
        return apiClient.invokeAPI("/SyncPlay/Leave", HttpMethod.POST, pathParams, queryParams, postBody, headerParams,
                cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Leave the joined SyncPlay group.
     * 
     * <p>
     * <b>204</b> - Group leave successful.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> syncPlayLeaveGroup() throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return syncPlayLeaveGroupRequestCreation().bodyToMono(localVarReturnType);
    }

    /**
     * Leave the joined SyncPlay group.
     * 
     * <p>
     * <b>204</b> - Group leave successful.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> syncPlayLeaveGroupWithHttpInfo() throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return syncPlayLeaveGroupRequestCreation().toEntity(localVarReturnType);
    }

    /**
     * Leave the joined SyncPlay group.
     * 
     * <p>
     * <b>204</b> - Group leave successful.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec syncPlayLeaveGroupWithResponseSpec() throws WebClientResponseException {
        return syncPlayLeaveGroupRequestCreation();
    }

    /**
     * Request to move an item in the playlist in SyncPlay group.
     * 
     * <p>
     * <b>204</b> - Queue update sent to all group members.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param movePlaylistItemRequestDto The new position for the item.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec syncPlayMovePlaylistItemRequestCreation(MovePlaylistItemRequestDto movePlaylistItemRequestDto)
            throws WebClientResponseException {
        Object postBody = movePlaylistItemRequestDto;
        // verify the required parameter 'movePlaylistItemRequestDto' is set
        if (movePlaylistItemRequestDto == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'movePlaylistItemRequestDto' when calling syncPlayMovePlaylistItem",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

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
        return apiClient.invokeAPI("/SyncPlay/MovePlaylistItem", HttpMethod.POST, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Request to move an item in the playlist in SyncPlay group.
     * 
     * <p>
     * <b>204</b> - Queue update sent to all group members.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param movePlaylistItemRequestDto The new position for the item.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> syncPlayMovePlaylistItem(MovePlaylistItemRequestDto movePlaylistItemRequestDto)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return syncPlayMovePlaylistItemRequestCreation(movePlaylistItemRequestDto).bodyToMono(localVarReturnType);
    }

    /**
     * Request to move an item in the playlist in SyncPlay group.
     * 
     * <p>
     * <b>204</b> - Queue update sent to all group members.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param movePlaylistItemRequestDto The new position for the item.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> syncPlayMovePlaylistItemWithHttpInfo(
            MovePlaylistItemRequestDto movePlaylistItemRequestDto) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return syncPlayMovePlaylistItemRequestCreation(movePlaylistItemRequestDto).toEntity(localVarReturnType);
    }

    /**
     * Request to move an item in the playlist in SyncPlay group.
     * 
     * <p>
     * <b>204</b> - Queue update sent to all group members.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param movePlaylistItemRequestDto The new position for the item.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec syncPlayMovePlaylistItemWithResponseSpec(MovePlaylistItemRequestDto movePlaylistItemRequestDto)
            throws WebClientResponseException {
        return syncPlayMovePlaylistItemRequestCreation(movePlaylistItemRequestDto);
    }

    /**
     * Request next item in SyncPlay group.
     * 
     * <p>
     * <b>204</b> - Next item update sent to all group members.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param nextItemRequestDto The current item information.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec syncPlayNextItemRequestCreation(NextItemRequestDto nextItemRequestDto)
            throws WebClientResponseException {
        Object postBody = nextItemRequestDto;
        // verify the required parameter 'nextItemRequestDto' is set
        if (nextItemRequestDto == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'nextItemRequestDto' when calling syncPlayNextItem",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

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
        return apiClient.invokeAPI("/SyncPlay/NextItem", HttpMethod.POST, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Request next item in SyncPlay group.
     * 
     * <p>
     * <b>204</b> - Next item update sent to all group members.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param nextItemRequestDto The current item information.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> syncPlayNextItem(NextItemRequestDto nextItemRequestDto) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return syncPlayNextItemRequestCreation(nextItemRequestDto).bodyToMono(localVarReturnType);
    }

    /**
     * Request next item in SyncPlay group.
     * 
     * <p>
     * <b>204</b> - Next item update sent to all group members.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param nextItemRequestDto The current item information.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> syncPlayNextItemWithHttpInfo(NextItemRequestDto nextItemRequestDto)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return syncPlayNextItemRequestCreation(nextItemRequestDto).toEntity(localVarReturnType);
    }

    /**
     * Request next item in SyncPlay group.
     * 
     * <p>
     * <b>204</b> - Next item update sent to all group members.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param nextItemRequestDto The current item information.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec syncPlayNextItemWithResponseSpec(NextItemRequestDto nextItemRequestDto)
            throws WebClientResponseException {
        return syncPlayNextItemRequestCreation(nextItemRequestDto);
    }

    /**
     * Request pause in SyncPlay group.
     * 
     * <p>
     * <b>204</b> - Pause update sent to all group members.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec syncPlayPauseRequestCreation() throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

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
        return apiClient.invokeAPI("/SyncPlay/Pause", HttpMethod.POST, pathParams, queryParams, postBody, headerParams,
                cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Request pause in SyncPlay group.
     * 
     * <p>
     * <b>204</b> - Pause update sent to all group members.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> syncPlayPause() throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return syncPlayPauseRequestCreation().bodyToMono(localVarReturnType);
    }

    /**
     * Request pause in SyncPlay group.
     * 
     * <p>
     * <b>204</b> - Pause update sent to all group members.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> syncPlayPauseWithHttpInfo() throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return syncPlayPauseRequestCreation().toEntity(localVarReturnType);
    }

    /**
     * Request pause in SyncPlay group.
     * 
     * <p>
     * <b>204</b> - Pause update sent to all group members.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec syncPlayPauseWithResponseSpec() throws WebClientResponseException {
        return syncPlayPauseRequestCreation();
    }

    /**
     * Update session ping.
     * 
     * <p>
     * <b>204</b> - Ping updated.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param pingRequestDto The new ping.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec syncPlayPingRequestCreation(PingRequestDto pingRequestDto) throws WebClientResponseException {
        Object postBody = pingRequestDto;
        // verify the required parameter 'pingRequestDto' is set
        if (pingRequestDto == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'pingRequestDto' when calling syncPlayPing",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

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
        return apiClient.invokeAPI("/SyncPlay/Ping", HttpMethod.POST, pathParams, queryParams, postBody, headerParams,
                cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Update session ping.
     * 
     * <p>
     * <b>204</b> - Ping updated.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param pingRequestDto The new ping.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> syncPlayPing(PingRequestDto pingRequestDto) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return syncPlayPingRequestCreation(pingRequestDto).bodyToMono(localVarReturnType);
    }

    /**
     * Update session ping.
     * 
     * <p>
     * <b>204</b> - Ping updated.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param pingRequestDto The new ping.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> syncPlayPingWithHttpInfo(PingRequestDto pingRequestDto)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return syncPlayPingRequestCreation(pingRequestDto).toEntity(localVarReturnType);
    }

    /**
     * Update session ping.
     * 
     * <p>
     * <b>204</b> - Ping updated.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param pingRequestDto The new ping.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec syncPlayPingWithResponseSpec(PingRequestDto pingRequestDto) throws WebClientResponseException {
        return syncPlayPingRequestCreation(pingRequestDto);
    }

    /**
     * Request previous item in SyncPlay group.
     * 
     * <p>
     * <b>204</b> - Previous item update sent to all group members.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param previousItemRequestDto The current item information.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec syncPlayPreviousItemRequestCreation(PreviousItemRequestDto previousItemRequestDto)
            throws WebClientResponseException {
        Object postBody = previousItemRequestDto;
        // verify the required parameter 'previousItemRequestDto' is set
        if (previousItemRequestDto == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'previousItemRequestDto' when calling syncPlayPreviousItem",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

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
        return apiClient.invokeAPI("/SyncPlay/PreviousItem", HttpMethod.POST, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Request previous item in SyncPlay group.
     * 
     * <p>
     * <b>204</b> - Previous item update sent to all group members.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param previousItemRequestDto The current item information.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> syncPlayPreviousItem(PreviousItemRequestDto previousItemRequestDto)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return syncPlayPreviousItemRequestCreation(previousItemRequestDto).bodyToMono(localVarReturnType);
    }

    /**
     * Request previous item in SyncPlay group.
     * 
     * <p>
     * <b>204</b> - Previous item update sent to all group members.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param previousItemRequestDto The current item information.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> syncPlayPreviousItemWithHttpInfo(PreviousItemRequestDto previousItemRequestDto)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return syncPlayPreviousItemRequestCreation(previousItemRequestDto).toEntity(localVarReturnType);
    }

    /**
     * Request previous item in SyncPlay group.
     * 
     * <p>
     * <b>204</b> - Previous item update sent to all group members.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param previousItemRequestDto The current item information.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec syncPlayPreviousItemWithResponseSpec(PreviousItemRequestDto previousItemRequestDto)
            throws WebClientResponseException {
        return syncPlayPreviousItemRequestCreation(previousItemRequestDto);
    }

    /**
     * Request to queue items to the playlist of a SyncPlay group.
     * 
     * <p>
     * <b>204</b> - Queue update sent to all group members.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param queueRequestDto The items to add.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec syncPlayQueueRequestCreation(QueueRequestDto queueRequestDto)
            throws WebClientResponseException {
        Object postBody = queueRequestDto;
        // verify the required parameter 'queueRequestDto' is set
        if (queueRequestDto == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'queueRequestDto' when calling syncPlayQueue",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

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
        return apiClient.invokeAPI("/SyncPlay/Queue", HttpMethod.POST, pathParams, queryParams, postBody, headerParams,
                cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Request to queue items to the playlist of a SyncPlay group.
     * 
     * <p>
     * <b>204</b> - Queue update sent to all group members.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param queueRequestDto The items to add.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> syncPlayQueue(QueueRequestDto queueRequestDto) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return syncPlayQueueRequestCreation(queueRequestDto).bodyToMono(localVarReturnType);
    }

    /**
     * Request to queue items to the playlist of a SyncPlay group.
     * 
     * <p>
     * <b>204</b> - Queue update sent to all group members.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param queueRequestDto The items to add.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> syncPlayQueueWithHttpInfo(QueueRequestDto queueRequestDto)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return syncPlayQueueRequestCreation(queueRequestDto).toEntity(localVarReturnType);
    }

    /**
     * Request to queue items to the playlist of a SyncPlay group.
     * 
     * <p>
     * <b>204</b> - Queue update sent to all group members.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param queueRequestDto The items to add.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec syncPlayQueueWithResponseSpec(QueueRequestDto queueRequestDto)
            throws WebClientResponseException {
        return syncPlayQueueRequestCreation(queueRequestDto);
    }

    /**
     * Notify SyncPlay group that member is ready for playback.
     * 
     * <p>
     * <b>204</b> - Group state update sent to all group members.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param readyRequestDto The player status.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec syncPlayReadyRequestCreation(ReadyRequestDto readyRequestDto)
            throws WebClientResponseException {
        Object postBody = readyRequestDto;
        // verify the required parameter 'readyRequestDto' is set
        if (readyRequestDto == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'readyRequestDto' when calling syncPlayReady",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

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
        return apiClient.invokeAPI("/SyncPlay/Ready", HttpMethod.POST, pathParams, queryParams, postBody, headerParams,
                cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Notify SyncPlay group that member is ready for playback.
     * 
     * <p>
     * <b>204</b> - Group state update sent to all group members.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param readyRequestDto The player status.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> syncPlayReady(ReadyRequestDto readyRequestDto) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return syncPlayReadyRequestCreation(readyRequestDto).bodyToMono(localVarReturnType);
    }

    /**
     * Notify SyncPlay group that member is ready for playback.
     * 
     * <p>
     * <b>204</b> - Group state update sent to all group members.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param readyRequestDto The player status.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> syncPlayReadyWithHttpInfo(ReadyRequestDto readyRequestDto)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return syncPlayReadyRequestCreation(readyRequestDto).toEntity(localVarReturnType);
    }

    /**
     * Notify SyncPlay group that member is ready for playback.
     * 
     * <p>
     * <b>204</b> - Group state update sent to all group members.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param readyRequestDto The player status.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec syncPlayReadyWithResponseSpec(ReadyRequestDto readyRequestDto)
            throws WebClientResponseException {
        return syncPlayReadyRequestCreation(readyRequestDto);
    }

    /**
     * Request to remove items from the playlist in SyncPlay group.
     * 
     * <p>
     * <b>204</b> - Queue update sent to all group members.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param removeFromPlaylistRequestDto The items to remove.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec syncPlayRemoveFromPlaylistRequestCreation(
            RemoveFromPlaylistRequestDto removeFromPlaylistRequestDto) throws WebClientResponseException {
        Object postBody = removeFromPlaylistRequestDto;
        // verify the required parameter 'removeFromPlaylistRequestDto' is set
        if (removeFromPlaylistRequestDto == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'removeFromPlaylistRequestDto' when calling syncPlayRemoveFromPlaylist",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

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
        return apiClient.invokeAPI("/SyncPlay/RemoveFromPlaylist", HttpMethod.POST, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Request to remove items from the playlist in SyncPlay group.
     * 
     * <p>
     * <b>204</b> - Queue update sent to all group members.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param removeFromPlaylistRequestDto The items to remove.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> syncPlayRemoveFromPlaylist(RemoveFromPlaylistRequestDto removeFromPlaylistRequestDto)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return syncPlayRemoveFromPlaylistRequestCreation(removeFromPlaylistRequestDto).bodyToMono(localVarReturnType);
    }

    /**
     * Request to remove items from the playlist in SyncPlay group.
     * 
     * <p>
     * <b>204</b> - Queue update sent to all group members.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param removeFromPlaylistRequestDto The items to remove.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> syncPlayRemoveFromPlaylistWithHttpInfo(
            RemoveFromPlaylistRequestDto removeFromPlaylistRequestDto) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return syncPlayRemoveFromPlaylistRequestCreation(removeFromPlaylistRequestDto).toEntity(localVarReturnType);
    }

    /**
     * Request to remove items from the playlist in SyncPlay group.
     * 
     * <p>
     * <b>204</b> - Queue update sent to all group members.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param removeFromPlaylistRequestDto The items to remove.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec syncPlayRemoveFromPlaylistWithResponseSpec(
            RemoveFromPlaylistRequestDto removeFromPlaylistRequestDto) throws WebClientResponseException {
        return syncPlayRemoveFromPlaylistRequestCreation(removeFromPlaylistRequestDto);
    }

    /**
     * Request seek in SyncPlay group.
     * 
     * <p>
     * <b>204</b> - Seek update sent to all group members.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param seekRequestDto The new playback position.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec syncPlaySeekRequestCreation(SeekRequestDto seekRequestDto) throws WebClientResponseException {
        Object postBody = seekRequestDto;
        // verify the required parameter 'seekRequestDto' is set
        if (seekRequestDto == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'seekRequestDto' when calling syncPlaySeek",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

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
        return apiClient.invokeAPI("/SyncPlay/Seek", HttpMethod.POST, pathParams, queryParams, postBody, headerParams,
                cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Request seek in SyncPlay group.
     * 
     * <p>
     * <b>204</b> - Seek update sent to all group members.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param seekRequestDto The new playback position.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> syncPlaySeek(SeekRequestDto seekRequestDto) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return syncPlaySeekRequestCreation(seekRequestDto).bodyToMono(localVarReturnType);
    }

    /**
     * Request seek in SyncPlay group.
     * 
     * <p>
     * <b>204</b> - Seek update sent to all group members.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param seekRequestDto The new playback position.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> syncPlaySeekWithHttpInfo(SeekRequestDto seekRequestDto)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return syncPlaySeekRequestCreation(seekRequestDto).toEntity(localVarReturnType);
    }

    /**
     * Request seek in SyncPlay group.
     * 
     * <p>
     * <b>204</b> - Seek update sent to all group members.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param seekRequestDto The new playback position.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec syncPlaySeekWithResponseSpec(SeekRequestDto seekRequestDto) throws WebClientResponseException {
        return syncPlaySeekRequestCreation(seekRequestDto);
    }

    /**
     * Request SyncPlay group to ignore member during group-wait.
     * 
     * <p>
     * <b>204</b> - Member state updated.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param ignoreWaitRequestDto The settings to set.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec syncPlaySetIgnoreWaitRequestCreation(IgnoreWaitRequestDto ignoreWaitRequestDto)
            throws WebClientResponseException {
        Object postBody = ignoreWaitRequestDto;
        // verify the required parameter 'ignoreWaitRequestDto' is set
        if (ignoreWaitRequestDto == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'ignoreWaitRequestDto' when calling syncPlaySetIgnoreWait",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

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
        return apiClient.invokeAPI("/SyncPlay/SetIgnoreWait", HttpMethod.POST, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Request SyncPlay group to ignore member during group-wait.
     * 
     * <p>
     * <b>204</b> - Member state updated.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param ignoreWaitRequestDto The settings to set.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> syncPlaySetIgnoreWait(IgnoreWaitRequestDto ignoreWaitRequestDto)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return syncPlaySetIgnoreWaitRequestCreation(ignoreWaitRequestDto).bodyToMono(localVarReturnType);
    }

    /**
     * Request SyncPlay group to ignore member during group-wait.
     * 
     * <p>
     * <b>204</b> - Member state updated.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param ignoreWaitRequestDto The settings to set.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> syncPlaySetIgnoreWaitWithHttpInfo(IgnoreWaitRequestDto ignoreWaitRequestDto)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return syncPlaySetIgnoreWaitRequestCreation(ignoreWaitRequestDto).toEntity(localVarReturnType);
    }

    /**
     * Request SyncPlay group to ignore member during group-wait.
     * 
     * <p>
     * <b>204</b> - Member state updated.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param ignoreWaitRequestDto The settings to set.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec syncPlaySetIgnoreWaitWithResponseSpec(IgnoreWaitRequestDto ignoreWaitRequestDto)
            throws WebClientResponseException {
        return syncPlaySetIgnoreWaitRequestCreation(ignoreWaitRequestDto);
    }

    /**
     * Request to set new playlist in SyncPlay group.
     * 
     * <p>
     * <b>204</b> - Queue update sent to all group members.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param playRequestDto The new playlist to play in the group.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec syncPlaySetNewQueueRequestCreation(PlayRequestDto playRequestDto)
            throws WebClientResponseException {
        Object postBody = playRequestDto;
        // verify the required parameter 'playRequestDto' is set
        if (playRequestDto == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'playRequestDto' when calling syncPlaySetNewQueue",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

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
        return apiClient.invokeAPI("/SyncPlay/SetNewQueue", HttpMethod.POST, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Request to set new playlist in SyncPlay group.
     * 
     * <p>
     * <b>204</b> - Queue update sent to all group members.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param playRequestDto The new playlist to play in the group.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> syncPlaySetNewQueue(PlayRequestDto playRequestDto) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return syncPlaySetNewQueueRequestCreation(playRequestDto).bodyToMono(localVarReturnType);
    }

    /**
     * Request to set new playlist in SyncPlay group.
     * 
     * <p>
     * <b>204</b> - Queue update sent to all group members.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param playRequestDto The new playlist to play in the group.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> syncPlaySetNewQueueWithHttpInfo(PlayRequestDto playRequestDto)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return syncPlaySetNewQueueRequestCreation(playRequestDto).toEntity(localVarReturnType);
    }

    /**
     * Request to set new playlist in SyncPlay group.
     * 
     * <p>
     * <b>204</b> - Queue update sent to all group members.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param playRequestDto The new playlist to play in the group.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec syncPlaySetNewQueueWithResponseSpec(PlayRequestDto playRequestDto)
            throws WebClientResponseException {
        return syncPlaySetNewQueueRequestCreation(playRequestDto);
    }

    /**
     * Request to change playlist item in SyncPlay group.
     * 
     * <p>
     * <b>204</b> - Queue update sent to all group members.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param setPlaylistItemRequestDto The new item to play.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec syncPlaySetPlaylistItemRequestCreation(SetPlaylistItemRequestDto setPlaylistItemRequestDto)
            throws WebClientResponseException {
        Object postBody = setPlaylistItemRequestDto;
        // verify the required parameter 'setPlaylistItemRequestDto' is set
        if (setPlaylistItemRequestDto == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'setPlaylistItemRequestDto' when calling syncPlaySetPlaylistItem",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

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
        return apiClient.invokeAPI("/SyncPlay/SetPlaylistItem", HttpMethod.POST, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Request to change playlist item in SyncPlay group.
     * 
     * <p>
     * <b>204</b> - Queue update sent to all group members.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param setPlaylistItemRequestDto The new item to play.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> syncPlaySetPlaylistItem(SetPlaylistItemRequestDto setPlaylistItemRequestDto)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return syncPlaySetPlaylistItemRequestCreation(setPlaylistItemRequestDto).bodyToMono(localVarReturnType);
    }

    /**
     * Request to change playlist item in SyncPlay group.
     * 
     * <p>
     * <b>204</b> - Queue update sent to all group members.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param setPlaylistItemRequestDto The new item to play.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> syncPlaySetPlaylistItemWithHttpInfo(
            SetPlaylistItemRequestDto setPlaylistItemRequestDto) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return syncPlaySetPlaylistItemRequestCreation(setPlaylistItemRequestDto).toEntity(localVarReturnType);
    }

    /**
     * Request to change playlist item in SyncPlay group.
     * 
     * <p>
     * <b>204</b> - Queue update sent to all group members.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param setPlaylistItemRequestDto The new item to play.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec syncPlaySetPlaylistItemWithResponseSpec(SetPlaylistItemRequestDto setPlaylistItemRequestDto)
            throws WebClientResponseException {
        return syncPlaySetPlaylistItemRequestCreation(setPlaylistItemRequestDto);
    }

    /**
     * Request to set repeat mode in SyncPlay group.
     * 
     * <p>
     * <b>204</b> - Play queue update sent to all group members.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param setRepeatModeRequestDto The new repeat mode.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec syncPlaySetRepeatModeRequestCreation(SetRepeatModeRequestDto setRepeatModeRequestDto)
            throws WebClientResponseException {
        Object postBody = setRepeatModeRequestDto;
        // verify the required parameter 'setRepeatModeRequestDto' is set
        if (setRepeatModeRequestDto == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'setRepeatModeRequestDto' when calling syncPlaySetRepeatMode",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

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
        return apiClient.invokeAPI("/SyncPlay/SetRepeatMode", HttpMethod.POST, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Request to set repeat mode in SyncPlay group.
     * 
     * <p>
     * <b>204</b> - Play queue update sent to all group members.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param setRepeatModeRequestDto The new repeat mode.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> syncPlaySetRepeatMode(SetRepeatModeRequestDto setRepeatModeRequestDto)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return syncPlaySetRepeatModeRequestCreation(setRepeatModeRequestDto).bodyToMono(localVarReturnType);
    }

    /**
     * Request to set repeat mode in SyncPlay group.
     * 
     * <p>
     * <b>204</b> - Play queue update sent to all group members.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param setRepeatModeRequestDto The new repeat mode.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> syncPlaySetRepeatModeWithHttpInfo(SetRepeatModeRequestDto setRepeatModeRequestDto)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return syncPlaySetRepeatModeRequestCreation(setRepeatModeRequestDto).toEntity(localVarReturnType);
    }

    /**
     * Request to set repeat mode in SyncPlay group.
     * 
     * <p>
     * <b>204</b> - Play queue update sent to all group members.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param setRepeatModeRequestDto The new repeat mode.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec syncPlaySetRepeatModeWithResponseSpec(SetRepeatModeRequestDto setRepeatModeRequestDto)
            throws WebClientResponseException {
        return syncPlaySetRepeatModeRequestCreation(setRepeatModeRequestDto);
    }

    /**
     * Request to set shuffle mode in SyncPlay group.
     * 
     * <p>
     * <b>204</b> - Play queue update sent to all group members.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param setShuffleModeRequestDto The new shuffle mode.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec syncPlaySetShuffleModeRequestCreation(SetShuffleModeRequestDto setShuffleModeRequestDto)
            throws WebClientResponseException {
        Object postBody = setShuffleModeRequestDto;
        // verify the required parameter 'setShuffleModeRequestDto' is set
        if (setShuffleModeRequestDto == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'setShuffleModeRequestDto' when calling syncPlaySetShuffleMode",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

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
        return apiClient.invokeAPI("/SyncPlay/SetShuffleMode", HttpMethod.POST, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Request to set shuffle mode in SyncPlay group.
     * 
     * <p>
     * <b>204</b> - Play queue update sent to all group members.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param setShuffleModeRequestDto The new shuffle mode.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> syncPlaySetShuffleMode(SetShuffleModeRequestDto setShuffleModeRequestDto)
            throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return syncPlaySetShuffleModeRequestCreation(setShuffleModeRequestDto).bodyToMono(localVarReturnType);
    }

    /**
     * Request to set shuffle mode in SyncPlay group.
     * 
     * <p>
     * <b>204</b> - Play queue update sent to all group members.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param setShuffleModeRequestDto The new shuffle mode.
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> syncPlaySetShuffleModeWithHttpInfo(
            SetShuffleModeRequestDto setShuffleModeRequestDto) throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return syncPlaySetShuffleModeRequestCreation(setShuffleModeRequestDto).toEntity(localVarReturnType);
    }

    /**
     * Request to set shuffle mode in SyncPlay group.
     * 
     * <p>
     * <b>204</b> - Play queue update sent to all group members.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param setShuffleModeRequestDto The new shuffle mode.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec syncPlaySetShuffleModeWithResponseSpec(SetShuffleModeRequestDto setShuffleModeRequestDto)
            throws WebClientResponseException {
        return syncPlaySetShuffleModeRequestCreation(setShuffleModeRequestDto);
    }

    /**
     * Request stop in SyncPlay group.
     * 
     * <p>
     * <b>204</b> - Stop update sent to all group members.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec syncPlayStopRequestCreation() throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

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
        return apiClient.invokeAPI("/SyncPlay/Stop", HttpMethod.POST, pathParams, queryParams, postBody, headerParams,
                cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Request stop in SyncPlay group.
     * 
     * <p>
     * <b>204</b> - Stop update sent to all group members.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> syncPlayStop() throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return syncPlayStopRequestCreation().bodyToMono(localVarReturnType);
    }

    /**
     * Request stop in SyncPlay group.
     * 
     * <p>
     * <b>204</b> - Stop update sent to all group members.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> syncPlayStopWithHttpInfo() throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return syncPlayStopRequestCreation().toEntity(localVarReturnType);
    }

    /**
     * Request stop in SyncPlay group.
     * 
     * <p>
     * <b>204</b> - Stop update sent to all group members.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec syncPlayStopWithResponseSpec() throws WebClientResponseException {
        return syncPlayStopRequestCreation();
    }

    /**
     * Request unpause in SyncPlay group.
     * 
     * <p>
     * <b>204</b> - Unpause update sent to all group members.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec syncPlayUnpauseRequestCreation() throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

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
        return apiClient.invokeAPI("/SyncPlay/Unpause", HttpMethod.POST, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Request unpause in SyncPlay group.
     * 
     * <p>
     * <b>204</b> - Unpause update sent to all group members.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<Void> syncPlayUnpause() throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return syncPlayUnpauseRequestCreation().bodyToMono(localVarReturnType);
    }

    /**
     * Request unpause in SyncPlay group.
     * 
     * <p>
     * <b>204</b> - Unpause update sent to all group members.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<Void>> syncPlayUnpauseWithHttpInfo() throws WebClientResponseException {
        ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        return syncPlayUnpauseRequestCreation().toEntity(localVarReturnType);
    }

    /**
     * Request unpause in SyncPlay group.
     * 
     * <p>
     * <b>204</b> - Unpause update sent to all group members.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec syncPlayUnpauseWithResponseSpec() throws WebClientResponseException {
        return syncPlayUnpauseRequestCreation();
    }
}
