package org.openhab.binding.jellyfin.internal.api.version.current;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.openhab.binding.jellyfin.internal.api.version.ApiClient;
import org.openhab.binding.jellyfin.internal.api.version.current.model.BaseItemDtoQueryResult;
import org.openhab.binding.jellyfin.internal.api.version.current.model.ChannelFeatures;
import org.openhab.binding.jellyfin.internal.api.version.current.model.ItemFields;
import org.openhab.binding.jellyfin.internal.api.version.current.model.ItemFilter;
import org.openhab.binding.jellyfin.internal.api.version.current.model.ItemSortBy;
import org.openhab.binding.jellyfin.internal.api.version.current.model.SortOrder;
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
public class ChannelsApi {
    private ApiClient apiClient;

    public ChannelsApi() {
        this(new ApiClient());
    }

    @Autowired
    public ChannelsApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Get all channel features.
     * 
     * <p>
     * <b>200</b> - All channel features returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return List&lt;ChannelFeatures&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getAllChannelFeaturesRequestCreation() throws WebClientResponseException {
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

        ParameterizedTypeReference<ChannelFeatures> localVarReturnType = new ParameterizedTypeReference<ChannelFeatures>() {
        };
        return apiClient.invokeAPI("/Channels/Features", HttpMethod.GET, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Get all channel features.
     * 
     * <p>
     * <b>200</b> - All channel features returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return List&lt;ChannelFeatures&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Flux<ChannelFeatures> getAllChannelFeatures() throws WebClientResponseException {
        ParameterizedTypeReference<ChannelFeatures> localVarReturnType = new ParameterizedTypeReference<ChannelFeatures>() {
        };
        return getAllChannelFeaturesRequestCreation().bodyToFlux(localVarReturnType);
    }

    /**
     * Get all channel features.
     * 
     * <p>
     * <b>200</b> - All channel features returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return ResponseEntity&lt;List&lt;ChannelFeatures&gt;&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<List<ChannelFeatures>>> getAllChannelFeaturesWithHttpInfo()
            throws WebClientResponseException {
        ParameterizedTypeReference<ChannelFeatures> localVarReturnType = new ParameterizedTypeReference<ChannelFeatures>() {
        };
        return getAllChannelFeaturesRequestCreation().toEntityList(localVarReturnType);
    }

    /**
     * Get all channel features.
     * 
     * <p>
     * <b>200</b> - All channel features returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getAllChannelFeaturesWithResponseSpec() throws WebClientResponseException {
        return getAllChannelFeaturesRequestCreation();
    }

    /**
     * Get channel features.
     * 
     * <p>
     * <b>200</b> - Channel features returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param channelId Channel id.
     * @return ChannelFeatures
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getChannelFeaturesRequestCreation(UUID channelId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'channelId' is set
        if (channelId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'channelId' when calling getChannelFeatures",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("channelId", channelId);

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

        ParameterizedTypeReference<ChannelFeatures> localVarReturnType = new ParameterizedTypeReference<ChannelFeatures>() {
        };
        return apiClient.invokeAPI("/Channels/{channelId}/Features", HttpMethod.GET, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Get channel features.
     * 
     * <p>
     * <b>200</b> - Channel features returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param channelId Channel id.
     * @return ChannelFeatures
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ChannelFeatures> getChannelFeatures(UUID channelId) throws WebClientResponseException {
        ParameterizedTypeReference<ChannelFeatures> localVarReturnType = new ParameterizedTypeReference<ChannelFeatures>() {
        };
        return getChannelFeaturesRequestCreation(channelId).bodyToMono(localVarReturnType);
    }

    /**
     * Get channel features.
     * 
     * <p>
     * <b>200</b> - Channel features returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param channelId Channel id.
     * @return ResponseEntity&lt;ChannelFeatures&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<ChannelFeatures>> getChannelFeaturesWithHttpInfo(UUID channelId)
            throws WebClientResponseException {
        ParameterizedTypeReference<ChannelFeatures> localVarReturnType = new ParameterizedTypeReference<ChannelFeatures>() {
        };
        return getChannelFeaturesRequestCreation(channelId).toEntity(localVarReturnType);
    }

    /**
     * Get channel features.
     * 
     * <p>
     * <b>200</b> - Channel features returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param channelId Channel id.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getChannelFeaturesWithResponseSpec(UUID channelId) throws WebClientResponseException {
        return getChannelFeaturesRequestCreation(channelId);
    }

    /**
     * Get channel items.
     * 
     * <p>
     * <b>200</b> - Channel items returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param channelId Channel Id.
     * @param folderId Optional. Folder Id.
     * @param userId Optional. User Id.
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results.
     * @param limit Optional. The maximum number of records to return.
     * @param sortOrder Optional. Sort Order - Ascending,Descending.
     * @param filters Optional. Specify additional filters to apply.
     * @param sortBy Optional. Specify one or more sort orders, comma delimited. Options: Album, AlbumArtist, Artist,
     *            Budget, CommunityRating, CriticRating, DateCreated, DatePlayed, PlayCount, PremiereDate,
     *            ProductionYear, SortName, Random, Revenue, Runtime.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getChannelItemsRequestCreation(UUID channelId, UUID folderId, UUID userId, Integer startIndex,
            Integer limit, List<SortOrder> sortOrder, List<ItemFilter> filters, List<ItemSortBy> sortBy,
            List<ItemFields> fields) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'channelId' is set
        if (channelId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'channelId' when calling getChannelItems",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("channelId", channelId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "folderId", folderId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "userId", userId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "startIndex", startIndex));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "limit", limit));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "sortOrder", sortOrder));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "filters", filters));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "sortBy", sortBy));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "fields", fields));

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {
        };
        return apiClient.invokeAPI("/Channels/{channelId}/Items", HttpMethod.GET, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Get channel items.
     * 
     * <p>
     * <b>200</b> - Channel items returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param channelId Channel Id.
     * @param folderId Optional. Folder Id.
     * @param userId Optional. User Id.
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results.
     * @param limit Optional. The maximum number of records to return.
     * @param sortOrder Optional. Sort Order - Ascending,Descending.
     * @param filters Optional. Specify additional filters to apply.
     * @param sortBy Optional. Specify one or more sort orders, comma delimited. Options: Album, AlbumArtist, Artist,
     *            Budget, CommunityRating, CriticRating, DateCreated, DatePlayed, PlayCount, PremiereDate,
     *            ProductionYear, SortName, Random, Revenue, Runtime.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<BaseItemDtoQueryResult> getChannelItems(UUID channelId, UUID folderId, UUID userId, Integer startIndex,
            Integer limit, List<SortOrder> sortOrder, List<ItemFilter> filters, List<ItemSortBy> sortBy,
            List<ItemFields> fields) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {
        };
        return getChannelItemsRequestCreation(channelId, folderId, userId, startIndex, limit, sortOrder, filters,
                sortBy, fields).bodyToMono(localVarReturnType);
    }

    /**
     * Get channel items.
     * 
     * <p>
     * <b>200</b> - Channel items returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param channelId Channel Id.
     * @param folderId Optional. Folder Id.
     * @param userId Optional. User Id.
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results.
     * @param limit Optional. The maximum number of records to return.
     * @param sortOrder Optional. Sort Order - Ascending,Descending.
     * @param filters Optional. Specify additional filters to apply.
     * @param sortBy Optional. Specify one or more sort orders, comma delimited. Options: Album, AlbumArtist, Artist,
     *            Budget, CommunityRating, CriticRating, DateCreated, DatePlayed, PlayCount, PremiereDate,
     *            ProductionYear, SortName, Random, Revenue, Runtime.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @return ResponseEntity&lt;BaseItemDtoQueryResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<BaseItemDtoQueryResult>> getChannelItemsWithHttpInfo(UUID channelId, UUID folderId,
            UUID userId, Integer startIndex, Integer limit, List<SortOrder> sortOrder, List<ItemFilter> filters,
            List<ItemSortBy> sortBy, List<ItemFields> fields) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {
        };
        return getChannelItemsRequestCreation(channelId, folderId, userId, startIndex, limit, sortOrder, filters,
                sortBy, fields).toEntity(localVarReturnType);
    }

    /**
     * Get channel items.
     * 
     * <p>
     * <b>200</b> - Channel items returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param channelId Channel Id.
     * @param folderId Optional. Folder Id.
     * @param userId Optional. User Id.
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results.
     * @param limit Optional. The maximum number of records to return.
     * @param sortOrder Optional. Sort Order - Ascending,Descending.
     * @param filters Optional. Specify additional filters to apply.
     * @param sortBy Optional. Specify one or more sort orders, comma delimited. Options: Album, AlbumArtist, Artist,
     *            Budget, CommunityRating, CriticRating, DateCreated, DatePlayed, PlayCount, PremiereDate,
     *            ProductionYear, SortName, Random, Revenue, Runtime.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getChannelItemsWithResponseSpec(UUID channelId, UUID folderId, UUID userId, Integer startIndex,
            Integer limit, List<SortOrder> sortOrder, List<ItemFilter> filters, List<ItemSortBy> sortBy,
            List<ItemFields> fields) throws WebClientResponseException {
        return getChannelItemsRequestCreation(channelId, folderId, userId, startIndex, limit, sortOrder, filters,
                sortBy, fields);
    }

    /**
     * Gets available channels.
     * 
     * <p>
     * <b>200</b> - Channels returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param userId User Id to filter by. Use System.Guid.Empty to not filter by user.
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results.
     * @param limit Optional. The maximum number of records to return.
     * @param supportsLatestItems Optional. Filter by channels that support getting latest items.
     * @param supportsMediaDeletion Optional. Filter by channels that support media deletion.
     * @param isFavorite Optional. Filter by channels that are favorite.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getChannelsRequestCreation(UUID userId, Integer startIndex, Integer limit,
            Boolean supportsLatestItems, Boolean supportsMediaDeletion, Boolean isFavorite)
            throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "userId", userId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "startIndex", startIndex));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "limit", limit));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "supportsLatestItems", supportsLatestItems));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "supportsMediaDeletion", supportsMediaDeletion));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isFavorite", isFavorite));

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {
        };
        return apiClient.invokeAPI("/Channels", HttpMethod.GET, pathParams, queryParams, postBody, headerParams,
                cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets available channels.
     * 
     * <p>
     * <b>200</b> - Channels returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param userId User Id to filter by. Use System.Guid.Empty to not filter by user.
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results.
     * @param limit Optional. The maximum number of records to return.
     * @param supportsLatestItems Optional. Filter by channels that support getting latest items.
     * @param supportsMediaDeletion Optional. Filter by channels that support media deletion.
     * @param isFavorite Optional. Filter by channels that are favorite.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<BaseItemDtoQueryResult> getChannels(UUID userId, Integer startIndex, Integer limit,
            Boolean supportsLatestItems, Boolean supportsMediaDeletion, Boolean isFavorite)
            throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {
        };
        return getChannelsRequestCreation(userId, startIndex, limit, supportsLatestItems, supportsMediaDeletion,
                isFavorite).bodyToMono(localVarReturnType);
    }

    /**
     * Gets available channels.
     * 
     * <p>
     * <b>200</b> - Channels returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param userId User Id to filter by. Use System.Guid.Empty to not filter by user.
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results.
     * @param limit Optional. The maximum number of records to return.
     * @param supportsLatestItems Optional. Filter by channels that support getting latest items.
     * @param supportsMediaDeletion Optional. Filter by channels that support media deletion.
     * @param isFavorite Optional. Filter by channels that are favorite.
     * @return ResponseEntity&lt;BaseItemDtoQueryResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<BaseItemDtoQueryResult>> getChannelsWithHttpInfo(UUID userId, Integer startIndex,
            Integer limit, Boolean supportsLatestItems, Boolean supportsMediaDeletion, Boolean isFavorite)
            throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {
        };
        return getChannelsRequestCreation(userId, startIndex, limit, supportsLatestItems, supportsMediaDeletion,
                isFavorite).toEntity(localVarReturnType);
    }

    /**
     * Gets available channels.
     * 
     * <p>
     * <b>200</b> - Channels returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param userId User Id to filter by. Use System.Guid.Empty to not filter by user.
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results.
     * @param limit Optional. The maximum number of records to return.
     * @param supportsLatestItems Optional. Filter by channels that support getting latest items.
     * @param supportsMediaDeletion Optional. Filter by channels that support media deletion.
     * @param isFavorite Optional. Filter by channels that are favorite.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getChannelsWithResponseSpec(UUID userId, Integer startIndex, Integer limit,
            Boolean supportsLatestItems, Boolean supportsMediaDeletion, Boolean isFavorite)
            throws WebClientResponseException {
        return getChannelsRequestCreation(userId, startIndex, limit, supportsLatestItems, supportsMediaDeletion,
                isFavorite);
    }

    /**
     * Gets latest channel items.
     * 
     * <p>
     * <b>200</b> - Latest channel items returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param userId Optional. User Id.
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results.
     * @param limit Optional. The maximum number of records to return.
     * @param filters Optional. Specify additional filters to apply.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param channelIds Optional. Specify one or more channel id&#39;s, comma delimited.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getLatestChannelItemsRequestCreation(UUID userId, Integer startIndex, Integer limit,
            List<ItemFilter> filters, List<ItemFields> fields, List<UUID> channelIds)
            throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "userId", userId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "startIndex", startIndex));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "limit", limit));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "filters", filters));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "fields", fields));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "channelIds", channelIds));

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {
        };
        return apiClient.invokeAPI("/Channels/Items/Latest", HttpMethod.GET, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Gets latest channel items.
     * 
     * <p>
     * <b>200</b> - Latest channel items returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param userId Optional. User Id.
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results.
     * @param limit Optional. The maximum number of records to return.
     * @param filters Optional. Specify additional filters to apply.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param channelIds Optional. Specify one or more channel id&#39;s, comma delimited.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<BaseItemDtoQueryResult> getLatestChannelItems(UUID userId, Integer startIndex, Integer limit,
            List<ItemFilter> filters, List<ItemFields> fields, List<UUID> channelIds)
            throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {
        };
        return getLatestChannelItemsRequestCreation(userId, startIndex, limit, filters, fields, channelIds)
                .bodyToMono(localVarReturnType);
    }

    /**
     * Gets latest channel items.
     * 
     * <p>
     * <b>200</b> - Latest channel items returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param userId Optional. User Id.
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results.
     * @param limit Optional. The maximum number of records to return.
     * @param filters Optional. Specify additional filters to apply.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param channelIds Optional. Specify one or more channel id&#39;s, comma delimited.
     * @return ResponseEntity&lt;BaseItemDtoQueryResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<BaseItemDtoQueryResult>> getLatestChannelItemsWithHttpInfo(UUID userId,
            Integer startIndex, Integer limit, List<ItemFilter> filters, List<ItemFields> fields, List<UUID> channelIds)
            throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {
        };
        return getLatestChannelItemsRequestCreation(userId, startIndex, limit, filters, fields, channelIds)
                .toEntity(localVarReturnType);
    }

    /**
     * Gets latest channel items.
     * 
     * <p>
     * <b>200</b> - Latest channel items returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param userId Optional. User Id.
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results.
     * @param limit Optional. The maximum number of records to return.
     * @param filters Optional. Specify additional filters to apply.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param channelIds Optional. Specify one or more channel id&#39;s, comma delimited.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getLatestChannelItemsWithResponseSpec(UUID userId, Integer startIndex, Integer limit,
            List<ItemFilter> filters, List<ItemFields> fields, List<UUID> channelIds)
            throws WebClientResponseException {
        return getLatestChannelItemsRequestCreation(userId, startIndex, limit, filters, fields, channelIds);
    }
}
