package org.openhab.binding.jellyfin.internal.api.version.legacy;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.openhab.binding.jellyfin.internal.api.version.ApiClient;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.BaseItemDtoQueryResult;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.ImageType;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.ItemFields;
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
public class TvShowsApi {
    private ApiClient apiClient;

    public TvShowsApi() {
        this(new ApiClient());
    }

    @Autowired
    public TvShowsApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Gets episodes for a tv season.
     * 
     * <p>
     * <b>200</b> - Success
     * <p>
     * <b>404</b> - Not Found
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param seriesId The series id.
     * @param userId The user id.
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple,
     *            comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions,
     *            MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue,
     *            SortName, Studios, Taglines, TrailerUrls.
     * @param season Optional filter by season number.
     * @param seasonId Optional. Filter by season id.
     * @param isMissing Optional. Filter by items that are missing episodes or not.
     * @param adjacentTo Optional. Return items that are siblings of a supplied item.
     * @param startItemId Optional. Skip through the list until a given item is found.
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results.
     * @param limit Optional. The maximum number of records to return.
     * @param enableImages Optional, include image information in output.
     * @param imageTypeLimit Optional, the max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @param enableUserData Optional. Include user data.
     * @param sortBy Optional. Specify one or more sort orders, comma delimited. Options: Album, AlbumArtist, Artist,
     *            Budget, CommunityRating, CriticRating, DateCreated, DatePlayed, PlayCount, PremiereDate,
     *            ProductionYear, SortName, Random, Revenue, Runtime.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getEpisodesRequestCreation(UUID seriesId, UUID userId, List<ItemFields> fields, Integer season,
            UUID seasonId, Boolean isMissing, String adjacentTo, UUID startItemId, Integer startIndex, Integer limit,
            Boolean enableImages, Integer imageTypeLimit, List<ImageType> enableImageTypes, Boolean enableUserData,
            String sortBy) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'seriesId' is set
        if (seriesId == null) {
            throw new WebClientResponseException("Missing the required parameter 'seriesId' when calling getEpisodes",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("seriesId", seriesId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "userId", userId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "fields", fields));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "season", season));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "seasonId", seasonId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isMissing", isMissing));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "adjacentTo", adjacentTo));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "startItemId", startItemId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "startIndex", startIndex));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "limit", limit));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableImages", enableImages));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "imageTypeLimit", imageTypeLimit));
        queryParams.putAll(
                apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)),
                        "enableImageTypes", enableImageTypes));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableUserData", enableUserData));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "sortBy", sortBy));

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {
        };
        return apiClient.invokeAPI("/Shows/{seriesId}/Episodes", HttpMethod.GET, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Gets episodes for a tv season.
     * 
     * <p>
     * <b>200</b> - Success
     * <p>
     * <b>404</b> - Not Found
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param seriesId The series id.
     * @param userId The user id.
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple,
     *            comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions,
     *            MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue,
     *            SortName, Studios, Taglines, TrailerUrls.
     * @param season Optional filter by season number.
     * @param seasonId Optional. Filter by season id.
     * @param isMissing Optional. Filter by items that are missing episodes or not.
     * @param adjacentTo Optional. Return items that are siblings of a supplied item.
     * @param startItemId Optional. Skip through the list until a given item is found.
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results.
     * @param limit Optional. The maximum number of records to return.
     * @param enableImages Optional, include image information in output.
     * @param imageTypeLimit Optional, the max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @param enableUserData Optional. Include user data.
     * @param sortBy Optional. Specify one or more sort orders, comma delimited. Options: Album, AlbumArtist, Artist,
     *            Budget, CommunityRating, CriticRating, DateCreated, DatePlayed, PlayCount, PremiereDate,
     *            ProductionYear, SortName, Random, Revenue, Runtime.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<BaseItemDtoQueryResult> getEpisodes(UUID seriesId, UUID userId, List<ItemFields> fields, Integer season,
            UUID seasonId, Boolean isMissing, String adjacentTo, UUID startItemId, Integer startIndex, Integer limit,
            Boolean enableImages, Integer imageTypeLimit, List<ImageType> enableImageTypes, Boolean enableUserData,
            String sortBy) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {
        };
        return getEpisodesRequestCreation(seriesId, userId, fields, season, seasonId, isMissing, adjacentTo,
                startItemId, startIndex, limit, enableImages, imageTypeLimit, enableImageTypes, enableUserData, sortBy)
                .bodyToMono(localVarReturnType);
    }

    /**
     * Gets episodes for a tv season.
     * 
     * <p>
     * <b>200</b> - Success
     * <p>
     * <b>404</b> - Not Found
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param seriesId The series id.
     * @param userId The user id.
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple,
     *            comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions,
     *            MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue,
     *            SortName, Studios, Taglines, TrailerUrls.
     * @param season Optional filter by season number.
     * @param seasonId Optional. Filter by season id.
     * @param isMissing Optional. Filter by items that are missing episodes or not.
     * @param adjacentTo Optional. Return items that are siblings of a supplied item.
     * @param startItemId Optional. Skip through the list until a given item is found.
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results.
     * @param limit Optional. The maximum number of records to return.
     * @param enableImages Optional, include image information in output.
     * @param imageTypeLimit Optional, the max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @param enableUserData Optional. Include user data.
     * @param sortBy Optional. Specify one or more sort orders, comma delimited. Options: Album, AlbumArtist, Artist,
     *            Budget, CommunityRating, CriticRating, DateCreated, DatePlayed, PlayCount, PremiereDate,
     *            ProductionYear, SortName, Random, Revenue, Runtime.
     * @return ResponseEntity&lt;BaseItemDtoQueryResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<BaseItemDtoQueryResult>> getEpisodesWithHttpInfo(UUID seriesId, UUID userId,
            List<ItemFields> fields, Integer season, UUID seasonId, Boolean isMissing, String adjacentTo,
            UUID startItemId, Integer startIndex, Integer limit, Boolean enableImages, Integer imageTypeLimit,
            List<ImageType> enableImageTypes, Boolean enableUserData, String sortBy) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {
        };
        return getEpisodesRequestCreation(seriesId, userId, fields, season, seasonId, isMissing, adjacentTo,
                startItemId, startIndex, limit, enableImages, imageTypeLimit, enableImageTypes, enableUserData, sortBy)
                .toEntity(localVarReturnType);
    }

    /**
     * Gets episodes for a tv season.
     * 
     * <p>
     * <b>200</b> - Success
     * <p>
     * <b>404</b> - Not Found
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param seriesId The series id.
     * @param userId The user id.
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple,
     *            comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions,
     *            MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue,
     *            SortName, Studios, Taglines, TrailerUrls.
     * @param season Optional filter by season number.
     * @param seasonId Optional. Filter by season id.
     * @param isMissing Optional. Filter by items that are missing episodes or not.
     * @param adjacentTo Optional. Return items that are siblings of a supplied item.
     * @param startItemId Optional. Skip through the list until a given item is found.
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results.
     * @param limit Optional. The maximum number of records to return.
     * @param enableImages Optional, include image information in output.
     * @param imageTypeLimit Optional, the max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @param enableUserData Optional. Include user data.
     * @param sortBy Optional. Specify one or more sort orders, comma delimited. Options: Album, AlbumArtist, Artist,
     *            Budget, CommunityRating, CriticRating, DateCreated, DatePlayed, PlayCount, PremiereDate,
     *            ProductionYear, SortName, Random, Revenue, Runtime.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getEpisodesWithResponseSpec(UUID seriesId, UUID userId, List<ItemFields> fields, Integer season,
            UUID seasonId, Boolean isMissing, String adjacentTo, UUID startItemId, Integer startIndex, Integer limit,
            Boolean enableImages, Integer imageTypeLimit, List<ImageType> enableImageTypes, Boolean enableUserData,
            String sortBy) throws WebClientResponseException {
        return getEpisodesRequestCreation(seriesId, userId, fields, season, seasonId, isMissing, adjacentTo,
                startItemId, startIndex, limit, enableImages, imageTypeLimit, enableImageTypes, enableUserData, sortBy);
    }

    /**
     * Gets a list of next up episodes.
     * 
     * <p>
     * <b>200</b> - Success
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param userId The user id of the user to get the next up episodes for.
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results.
     * @param limit Optional. The maximum number of records to return.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param seriesId Optional. Filter by series id.
     * @param parentId Optional. Specify this to localize the search to a specific item or folder. Omit to use the root.
     * @param enableImages Optional. Include image information in output.
     * @param imageTypeLimit Optional. The max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @param enableUserData Optional. Include user data.
     * @param nextUpDateCutoff Optional. Starting date of shows to show in Next Up section.
     * @param enableTotalRecordCount Whether to enable the total records count. Defaults to true.
     * @param disableFirstEpisode Whether to disable sending the first episode in a series as next up.
     * @param enableRewatching Whether to include watched episode in next up results.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getNextUpRequestCreation(UUID userId, Integer startIndex, Integer limit,
            List<ItemFields> fields, String seriesId, UUID parentId, Boolean enableImages, Integer imageTypeLimit,
            List<ImageType> enableImageTypes, Boolean enableUserData, OffsetDateTime nextUpDateCutoff,
            Boolean enableTotalRecordCount, Boolean disableFirstEpisode, Boolean enableRewatching)
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
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "fields", fields));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "seriesId", seriesId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "parentId", parentId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableImages", enableImages));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "imageTypeLimit", imageTypeLimit));
        queryParams.putAll(
                apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)),
                        "enableImageTypes", enableImageTypes));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableUserData", enableUserData));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "nextUpDateCutoff", nextUpDateCutoff));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableTotalRecordCount", enableTotalRecordCount));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "disableFirstEpisode", disableFirstEpisode));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableRewatching", enableRewatching));

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {
        };
        return apiClient.invokeAPI("/Shows/NextUp", HttpMethod.GET, pathParams, queryParams, postBody, headerParams,
                cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets a list of next up episodes.
     * 
     * <p>
     * <b>200</b> - Success
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param userId The user id of the user to get the next up episodes for.
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results.
     * @param limit Optional. The maximum number of records to return.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param seriesId Optional. Filter by series id.
     * @param parentId Optional. Specify this to localize the search to a specific item or folder. Omit to use the root.
     * @param enableImages Optional. Include image information in output.
     * @param imageTypeLimit Optional. The max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @param enableUserData Optional. Include user data.
     * @param nextUpDateCutoff Optional. Starting date of shows to show in Next Up section.
     * @param enableTotalRecordCount Whether to enable the total records count. Defaults to true.
     * @param disableFirstEpisode Whether to disable sending the first episode in a series as next up.
     * @param enableRewatching Whether to include watched episode in next up results.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<BaseItemDtoQueryResult> getNextUp(UUID userId, Integer startIndex, Integer limit,
            List<ItemFields> fields, String seriesId, UUID parentId, Boolean enableImages, Integer imageTypeLimit,
            List<ImageType> enableImageTypes, Boolean enableUserData, OffsetDateTime nextUpDateCutoff,
            Boolean enableTotalRecordCount, Boolean disableFirstEpisode, Boolean enableRewatching)
            throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {
        };
        return getNextUpRequestCreation(userId, startIndex, limit, fields, seriesId, parentId, enableImages,
                imageTypeLimit, enableImageTypes, enableUserData, nextUpDateCutoff, enableTotalRecordCount,
                disableFirstEpisode, enableRewatching).bodyToMono(localVarReturnType);
    }

    /**
     * Gets a list of next up episodes.
     * 
     * <p>
     * <b>200</b> - Success
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param userId The user id of the user to get the next up episodes for.
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results.
     * @param limit Optional. The maximum number of records to return.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param seriesId Optional. Filter by series id.
     * @param parentId Optional. Specify this to localize the search to a specific item or folder. Omit to use the root.
     * @param enableImages Optional. Include image information in output.
     * @param imageTypeLimit Optional. The max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @param enableUserData Optional. Include user data.
     * @param nextUpDateCutoff Optional. Starting date of shows to show in Next Up section.
     * @param enableTotalRecordCount Whether to enable the total records count. Defaults to true.
     * @param disableFirstEpisode Whether to disable sending the first episode in a series as next up.
     * @param enableRewatching Whether to include watched episode in next up results.
     * @return ResponseEntity&lt;BaseItemDtoQueryResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<BaseItemDtoQueryResult>> getNextUpWithHttpInfo(UUID userId, Integer startIndex,
            Integer limit, List<ItemFields> fields, String seriesId, UUID parentId, Boolean enableImages,
            Integer imageTypeLimit, List<ImageType> enableImageTypes, Boolean enableUserData,
            OffsetDateTime nextUpDateCutoff, Boolean enableTotalRecordCount, Boolean disableFirstEpisode,
            Boolean enableRewatching) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {
        };
        return getNextUpRequestCreation(userId, startIndex, limit, fields, seriesId, parentId, enableImages,
                imageTypeLimit, enableImageTypes, enableUserData, nextUpDateCutoff, enableTotalRecordCount,
                disableFirstEpisode, enableRewatching).toEntity(localVarReturnType);
    }

    /**
     * Gets a list of next up episodes.
     * 
     * <p>
     * <b>200</b> - Success
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param userId The user id of the user to get the next up episodes for.
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results.
     * @param limit Optional. The maximum number of records to return.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param seriesId Optional. Filter by series id.
     * @param parentId Optional. Specify this to localize the search to a specific item or folder. Omit to use the root.
     * @param enableImages Optional. Include image information in output.
     * @param imageTypeLimit Optional. The max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @param enableUserData Optional. Include user data.
     * @param nextUpDateCutoff Optional. Starting date of shows to show in Next Up section.
     * @param enableTotalRecordCount Whether to enable the total records count. Defaults to true.
     * @param disableFirstEpisode Whether to disable sending the first episode in a series as next up.
     * @param enableRewatching Whether to include watched episode in next up results.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getNextUpWithResponseSpec(UUID userId, Integer startIndex, Integer limit,
            List<ItemFields> fields, String seriesId, UUID parentId, Boolean enableImages, Integer imageTypeLimit,
            List<ImageType> enableImageTypes, Boolean enableUserData, OffsetDateTime nextUpDateCutoff,
            Boolean enableTotalRecordCount, Boolean disableFirstEpisode, Boolean enableRewatching)
            throws WebClientResponseException {
        return getNextUpRequestCreation(userId, startIndex, limit, fields, seriesId, parentId, enableImages,
                imageTypeLimit, enableImageTypes, enableUserData, nextUpDateCutoff, enableTotalRecordCount,
                disableFirstEpisode, enableRewatching);
    }

    /**
     * Gets seasons for a tv series.
     * 
     * <p>
     * <b>200</b> - Success
     * <p>
     * <b>404</b> - Not Found
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param seriesId The series id.
     * @param userId The user id.
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple,
     *            comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions,
     *            MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue,
     *            SortName, Studios, Taglines, TrailerUrls.
     * @param isSpecialSeason Optional. Filter by special season.
     * @param isMissing Optional. Filter by items that are missing episodes or not.
     * @param adjacentTo Optional. Return items that are siblings of a supplied item.
     * @param enableImages Optional. Include image information in output.
     * @param imageTypeLimit Optional. The max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @param enableUserData Optional. Include user data.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getSeasonsRequestCreation(UUID seriesId, UUID userId, List<ItemFields> fields,
            Boolean isSpecialSeason, Boolean isMissing, String adjacentTo, Boolean enableImages, Integer imageTypeLimit,
            List<ImageType> enableImageTypes, Boolean enableUserData) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'seriesId' is set
        if (seriesId == null) {
            throw new WebClientResponseException("Missing the required parameter 'seriesId' when calling getSeasons",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("seriesId", seriesId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "userId", userId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "fields", fields));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isSpecialSeason", isSpecialSeason));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isMissing", isMissing));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "adjacentTo", adjacentTo));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableImages", enableImages));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "imageTypeLimit", imageTypeLimit));
        queryParams.putAll(
                apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)),
                        "enableImageTypes", enableImageTypes));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableUserData", enableUserData));

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {
        };
        return apiClient.invokeAPI("/Shows/{seriesId}/Seasons", HttpMethod.GET, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Gets seasons for a tv series.
     * 
     * <p>
     * <b>200</b> - Success
     * <p>
     * <b>404</b> - Not Found
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param seriesId The series id.
     * @param userId The user id.
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple,
     *            comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions,
     *            MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue,
     *            SortName, Studios, Taglines, TrailerUrls.
     * @param isSpecialSeason Optional. Filter by special season.
     * @param isMissing Optional. Filter by items that are missing episodes or not.
     * @param adjacentTo Optional. Return items that are siblings of a supplied item.
     * @param enableImages Optional. Include image information in output.
     * @param imageTypeLimit Optional. The max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @param enableUserData Optional. Include user data.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<BaseItemDtoQueryResult> getSeasons(UUID seriesId, UUID userId, List<ItemFields> fields,
            Boolean isSpecialSeason, Boolean isMissing, String adjacentTo, Boolean enableImages, Integer imageTypeLimit,
            List<ImageType> enableImageTypes, Boolean enableUserData) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {
        };
        return getSeasonsRequestCreation(seriesId, userId, fields, isSpecialSeason, isMissing, adjacentTo, enableImages,
                imageTypeLimit, enableImageTypes, enableUserData).bodyToMono(localVarReturnType);
    }

    /**
     * Gets seasons for a tv series.
     * 
     * <p>
     * <b>200</b> - Success
     * <p>
     * <b>404</b> - Not Found
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param seriesId The series id.
     * @param userId The user id.
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple,
     *            comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions,
     *            MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue,
     *            SortName, Studios, Taglines, TrailerUrls.
     * @param isSpecialSeason Optional. Filter by special season.
     * @param isMissing Optional. Filter by items that are missing episodes or not.
     * @param adjacentTo Optional. Return items that are siblings of a supplied item.
     * @param enableImages Optional. Include image information in output.
     * @param imageTypeLimit Optional. The max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @param enableUserData Optional. Include user data.
     * @return ResponseEntity&lt;BaseItemDtoQueryResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<BaseItemDtoQueryResult>> getSeasonsWithHttpInfo(UUID seriesId, UUID userId,
            List<ItemFields> fields, Boolean isSpecialSeason, Boolean isMissing, String adjacentTo,
            Boolean enableImages, Integer imageTypeLimit, List<ImageType> enableImageTypes, Boolean enableUserData)
            throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {
        };
        return getSeasonsRequestCreation(seriesId, userId, fields, isSpecialSeason, isMissing, adjacentTo, enableImages,
                imageTypeLimit, enableImageTypes, enableUserData).toEntity(localVarReturnType);
    }

    /**
     * Gets seasons for a tv series.
     * 
     * <p>
     * <b>200</b> - Success
     * <p>
     * <b>404</b> - Not Found
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param seriesId The series id.
     * @param userId The user id.
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple,
     *            comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions,
     *            MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue,
     *            SortName, Studios, Taglines, TrailerUrls.
     * @param isSpecialSeason Optional. Filter by special season.
     * @param isMissing Optional. Filter by items that are missing episodes or not.
     * @param adjacentTo Optional. Return items that are siblings of a supplied item.
     * @param enableImages Optional. Include image information in output.
     * @param imageTypeLimit Optional. The max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @param enableUserData Optional. Include user data.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getSeasonsWithResponseSpec(UUID seriesId, UUID userId, List<ItemFields> fields,
            Boolean isSpecialSeason, Boolean isMissing, String adjacentTo, Boolean enableImages, Integer imageTypeLimit,
            List<ImageType> enableImageTypes, Boolean enableUserData) throws WebClientResponseException {
        return getSeasonsRequestCreation(seriesId, userId, fields, isSpecialSeason, isMissing, adjacentTo, enableImages,
                imageTypeLimit, enableImageTypes, enableUserData);
    }

    /**
     * Gets a list of upcoming episodes.
     * 
     * <p>
     * <b>200</b> - Success
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param userId The user id of the user to get the upcoming episodes for.
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results.
     * @param limit Optional. The maximum number of records to return.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param parentId Optional. Specify this to localize the search to a specific item or folder. Omit to use the root.
     * @param enableImages Optional. Include image information in output.
     * @param imageTypeLimit Optional. The max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @param enableUserData Optional. Include user data.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getUpcomingEpisodesRequestCreation(UUID userId, Integer startIndex, Integer limit,
            List<ItemFields> fields, UUID parentId, Boolean enableImages, Integer imageTypeLimit,
            List<ImageType> enableImageTypes, Boolean enableUserData) throws WebClientResponseException {
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
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "fields", fields));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "parentId", parentId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableImages", enableImages));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "imageTypeLimit", imageTypeLimit));
        queryParams.putAll(
                apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)),
                        "enableImageTypes", enableImageTypes));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableUserData", enableUserData));

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {
        };
        return apiClient.invokeAPI("/Shows/Upcoming", HttpMethod.GET, pathParams, queryParams, postBody, headerParams,
                cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets a list of upcoming episodes.
     * 
     * <p>
     * <b>200</b> - Success
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param userId The user id of the user to get the upcoming episodes for.
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results.
     * @param limit Optional. The maximum number of records to return.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param parentId Optional. Specify this to localize the search to a specific item or folder. Omit to use the root.
     * @param enableImages Optional. Include image information in output.
     * @param imageTypeLimit Optional. The max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @param enableUserData Optional. Include user data.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<BaseItemDtoQueryResult> getUpcomingEpisodes(UUID userId, Integer startIndex, Integer limit,
            List<ItemFields> fields, UUID parentId, Boolean enableImages, Integer imageTypeLimit,
            List<ImageType> enableImageTypes, Boolean enableUserData) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {
        };
        return getUpcomingEpisodesRequestCreation(userId, startIndex, limit, fields, parentId, enableImages,
                imageTypeLimit, enableImageTypes, enableUserData).bodyToMono(localVarReturnType);
    }

    /**
     * Gets a list of upcoming episodes.
     * 
     * <p>
     * <b>200</b> - Success
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param userId The user id of the user to get the upcoming episodes for.
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results.
     * @param limit Optional. The maximum number of records to return.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param parentId Optional. Specify this to localize the search to a specific item or folder. Omit to use the root.
     * @param enableImages Optional. Include image information in output.
     * @param imageTypeLimit Optional. The max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @param enableUserData Optional. Include user data.
     * @return ResponseEntity&lt;BaseItemDtoQueryResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<BaseItemDtoQueryResult>> getUpcomingEpisodesWithHttpInfo(UUID userId, Integer startIndex,
            Integer limit, List<ItemFields> fields, UUID parentId, Boolean enableImages, Integer imageTypeLimit,
            List<ImageType> enableImageTypes, Boolean enableUserData) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {
        };
        return getUpcomingEpisodesRequestCreation(userId, startIndex, limit, fields, parentId, enableImages,
                imageTypeLimit, enableImageTypes, enableUserData).toEntity(localVarReturnType);
    }

    /**
     * Gets a list of upcoming episodes.
     * 
     * <p>
     * <b>200</b> - Success
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param userId The user id of the user to get the upcoming episodes for.
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results.
     * @param limit Optional. The maximum number of records to return.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param parentId Optional. Specify this to localize the search to a specific item or folder. Omit to use the root.
     * @param enableImages Optional. Include image information in output.
     * @param imageTypeLimit Optional. The max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @param enableUserData Optional. Include user data.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getUpcomingEpisodesWithResponseSpec(UUID userId, Integer startIndex, Integer limit,
            List<ItemFields> fields, UUID parentId, Boolean enableImages, Integer imageTypeLimit,
            List<ImageType> enableImageTypes, Boolean enableUserData) throws WebClientResponseException {
        return getUpcomingEpisodesRequestCreation(userId, startIndex, limit, fields, parentId, enableImages,
                imageTypeLimit, enableImageTypes, enableUserData);
    }
}
