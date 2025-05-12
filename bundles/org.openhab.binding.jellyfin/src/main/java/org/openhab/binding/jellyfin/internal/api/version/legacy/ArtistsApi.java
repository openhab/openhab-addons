package org.openhab.binding.jellyfin.internal.api.version.legacy;

import org.openhab.binding.jellyfin.internal.api.version.ApiClient;

import org.openhab.binding.jellyfin.internal.api.version.legacy.model.BaseItemDto;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.BaseItemDtoQueryResult;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.BaseItemKind;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.ImageType;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.ItemFields;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.ItemFilter;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.SortOrder;
import java.util.UUID;

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
public class ArtistsApi {
    private ApiClient apiClient;

    public ArtistsApi() {
        this(new ApiClient());
    }

    @Autowired
    public ArtistsApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    
    /**
     * Gets all album artists from a given item, folder, or the entire library.
     * 
     * <p><b>200</b> - Album artists returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param minCommunityRating Optional filter by minimum community rating.
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the results.
     * @param limit Optional. The maximum number of records to return.
     * @param searchTerm Optional. Search term.
     * @param parentId Specify this to localize the search to a specific item or folder. Omit to use the root.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param excludeItemTypes Optional. If specified, results will be filtered out based on item type. This allows multiple, comma delimited.
     * @param includeItemTypes Optional. If specified, results will be filtered based on item type. This allows multiple, comma delimited.
     * @param filters Optional. Specify additional filters to apply.
     * @param isFavorite Optional filter by items that are marked as favorite, or not.
     * @param mediaTypes Optional filter by MediaType. Allows multiple, comma delimited.
     * @param genres Optional. If specified, results will be filtered based on genre. This allows multiple, pipe delimited.
     * @param genreIds Optional. If specified, results will be filtered based on genre id. This allows multiple, pipe delimited.
     * @param officialRatings Optional. If specified, results will be filtered based on OfficialRating. This allows multiple, pipe delimited.
     * @param tags Optional. If specified, results will be filtered based on tag. This allows multiple, pipe delimited.
     * @param years Optional. If specified, results will be filtered based on production year. This allows multiple, comma delimited.
     * @param enableUserData Optional, include user data.
     * @param imageTypeLimit Optional, the max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @param person Optional. If specified, results will be filtered to include only those containing the specified person.
     * @param personIds Optional. If specified, results will be filtered to include only those containing the specified person ids.
     * @param personTypes Optional. If specified, along with Person, results will be filtered to include only those containing the specified person and PersonType. Allows multiple, comma-delimited.
     * @param studios Optional. If specified, results will be filtered based on studio. This allows multiple, pipe delimited.
     * @param studioIds Optional. If specified, results will be filtered based on studio id. This allows multiple, pipe delimited.
     * @param userId User id.
     * @param nameStartsWithOrGreater Optional filter by items whose name is sorted equally or greater than a given input string.
     * @param nameStartsWith Optional filter by items whose name is sorted equally than a given input string.
     * @param nameLessThan Optional filter by items whose name is equally or lesser than a given input string.
     * @param sortBy Optional. Specify one or more sort orders, comma delimited.
     * @param sortOrder Sort Order - Ascending,Descending.
     * @param enableImages Optional, include image information in output.
     * @param enableTotalRecordCount Total record count.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getAlbumArtistsRequestCreation(Double minCommunityRating, Integer startIndex, Integer limit, String searchTerm, UUID parentId, List<ItemFields> fields, List<BaseItemKind> excludeItemTypes, List<BaseItemKind> includeItemTypes, List<ItemFilter> filters, Boolean isFavorite, List<String> mediaTypes, List<String> genres, List<UUID> genreIds, List<String> officialRatings, List<String> tags, List<Integer> years, Boolean enableUserData, Integer imageTypeLimit, List<ImageType> enableImageTypes, String person, List<UUID> personIds, List<String> personTypes, List<String> studios, List<UUID> studioIds, UUID userId, String nameStartsWithOrGreater, String nameStartsWith, String nameLessThan, List<String> sortBy, List<SortOrder> sortOrder, Boolean enableImages, Boolean enableTotalRecordCount) throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "minCommunityRating", minCommunityRating));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "startIndex", startIndex));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "limit", limit));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "searchTerm", searchTerm));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "parentId", parentId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "fields", fields));
        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "excludeItemTypes", excludeItemTypes));
        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "includeItemTypes", includeItemTypes));
        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "filters", filters));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isFavorite", isFavorite));
        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "mediaTypes", mediaTypes));
        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "genres", genres));
        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "genreIds", genreIds));
        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "officialRatings", officialRatings));
        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "tags", tags));
        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "years", years));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableUserData", enableUserData));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "imageTypeLimit", imageTypeLimit));
        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "enableImageTypes", enableImageTypes));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "person", person));
        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "personIds", personIds));
        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "personTypes", personTypes));
        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "studios", studios));
        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "studioIds", studioIds));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "userId", userId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "nameStartsWithOrGreater", nameStartsWithOrGreater));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "nameStartsWith", nameStartsWith));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "nameLessThan", nameLessThan));
        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "sortBy", sortBy));
        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "sortOrder", sortOrder));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableImages", enableImages));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableTotalRecordCount", enableTotalRecordCount));
        
        final String[] localVarAccepts = { 
            "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {};
        return apiClient.invokeAPI("/Artists/AlbumArtists", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets all album artists from a given item, folder, or the entire library.
     * 
     * <p><b>200</b> - Album artists returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param minCommunityRating Optional filter by minimum community rating.
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the results.
     * @param limit Optional. The maximum number of records to return.
     * @param searchTerm Optional. Search term.
     * @param parentId Specify this to localize the search to a specific item or folder. Omit to use the root.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param excludeItemTypes Optional. If specified, results will be filtered out based on item type. This allows multiple, comma delimited.
     * @param includeItemTypes Optional. If specified, results will be filtered based on item type. This allows multiple, comma delimited.
     * @param filters Optional. Specify additional filters to apply.
     * @param isFavorite Optional filter by items that are marked as favorite, or not.
     * @param mediaTypes Optional filter by MediaType. Allows multiple, comma delimited.
     * @param genres Optional. If specified, results will be filtered based on genre. This allows multiple, pipe delimited.
     * @param genreIds Optional. If specified, results will be filtered based on genre id. This allows multiple, pipe delimited.
     * @param officialRatings Optional. If specified, results will be filtered based on OfficialRating. This allows multiple, pipe delimited.
     * @param tags Optional. If specified, results will be filtered based on tag. This allows multiple, pipe delimited.
     * @param years Optional. If specified, results will be filtered based on production year. This allows multiple, comma delimited.
     * @param enableUserData Optional, include user data.
     * @param imageTypeLimit Optional, the max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @param person Optional. If specified, results will be filtered to include only those containing the specified person.
     * @param personIds Optional. If specified, results will be filtered to include only those containing the specified person ids.
     * @param personTypes Optional. If specified, along with Person, results will be filtered to include only those containing the specified person and PersonType. Allows multiple, comma-delimited.
     * @param studios Optional. If specified, results will be filtered based on studio. This allows multiple, pipe delimited.
     * @param studioIds Optional. If specified, results will be filtered based on studio id. This allows multiple, pipe delimited.
     * @param userId User id.
     * @param nameStartsWithOrGreater Optional filter by items whose name is sorted equally or greater than a given input string.
     * @param nameStartsWith Optional filter by items whose name is sorted equally than a given input string.
     * @param nameLessThan Optional filter by items whose name is equally or lesser than a given input string.
     * @param sortBy Optional. Specify one or more sort orders, comma delimited.
     * @param sortOrder Sort Order - Ascending,Descending.
     * @param enableImages Optional, include image information in output.
     * @param enableTotalRecordCount Total record count.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<BaseItemDtoQueryResult> getAlbumArtists(Double minCommunityRating, Integer startIndex, Integer limit, String searchTerm, UUID parentId, List<ItemFields> fields, List<BaseItemKind> excludeItemTypes, List<BaseItemKind> includeItemTypes, List<ItemFilter> filters, Boolean isFavorite, List<String> mediaTypes, List<String> genres, List<UUID> genreIds, List<String> officialRatings, List<String> tags, List<Integer> years, Boolean enableUserData, Integer imageTypeLimit, List<ImageType> enableImageTypes, String person, List<UUID> personIds, List<String> personTypes, List<String> studios, List<UUID> studioIds, UUID userId, String nameStartsWithOrGreater, String nameStartsWith, String nameLessThan, List<String> sortBy, List<SortOrder> sortOrder, Boolean enableImages, Boolean enableTotalRecordCount) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {};
        return getAlbumArtistsRequestCreation(minCommunityRating, startIndex, limit, searchTerm, parentId, fields, excludeItemTypes, includeItemTypes, filters, isFavorite, mediaTypes, genres, genreIds, officialRatings, tags, years, enableUserData, imageTypeLimit, enableImageTypes, person, personIds, personTypes, studios, studioIds, userId, nameStartsWithOrGreater, nameStartsWith, nameLessThan, sortBy, sortOrder, enableImages, enableTotalRecordCount).bodyToMono(localVarReturnType);
    }

    /**
     * Gets all album artists from a given item, folder, or the entire library.
     * 
     * <p><b>200</b> - Album artists returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param minCommunityRating Optional filter by minimum community rating.
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the results.
     * @param limit Optional. The maximum number of records to return.
     * @param searchTerm Optional. Search term.
     * @param parentId Specify this to localize the search to a specific item or folder. Omit to use the root.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param excludeItemTypes Optional. If specified, results will be filtered out based on item type. This allows multiple, comma delimited.
     * @param includeItemTypes Optional. If specified, results will be filtered based on item type. This allows multiple, comma delimited.
     * @param filters Optional. Specify additional filters to apply.
     * @param isFavorite Optional filter by items that are marked as favorite, or not.
     * @param mediaTypes Optional filter by MediaType. Allows multiple, comma delimited.
     * @param genres Optional. If specified, results will be filtered based on genre. This allows multiple, pipe delimited.
     * @param genreIds Optional. If specified, results will be filtered based on genre id. This allows multiple, pipe delimited.
     * @param officialRatings Optional. If specified, results will be filtered based on OfficialRating. This allows multiple, pipe delimited.
     * @param tags Optional. If specified, results will be filtered based on tag. This allows multiple, pipe delimited.
     * @param years Optional. If specified, results will be filtered based on production year. This allows multiple, comma delimited.
     * @param enableUserData Optional, include user data.
     * @param imageTypeLimit Optional, the max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @param person Optional. If specified, results will be filtered to include only those containing the specified person.
     * @param personIds Optional. If specified, results will be filtered to include only those containing the specified person ids.
     * @param personTypes Optional. If specified, along with Person, results will be filtered to include only those containing the specified person and PersonType. Allows multiple, comma-delimited.
     * @param studios Optional. If specified, results will be filtered based on studio. This allows multiple, pipe delimited.
     * @param studioIds Optional. If specified, results will be filtered based on studio id. This allows multiple, pipe delimited.
     * @param userId User id.
     * @param nameStartsWithOrGreater Optional filter by items whose name is sorted equally or greater than a given input string.
     * @param nameStartsWith Optional filter by items whose name is sorted equally than a given input string.
     * @param nameLessThan Optional filter by items whose name is equally or lesser than a given input string.
     * @param sortBy Optional. Specify one or more sort orders, comma delimited.
     * @param sortOrder Sort Order - Ascending,Descending.
     * @param enableImages Optional, include image information in output.
     * @param enableTotalRecordCount Total record count.
     * @return ResponseEntity&lt;BaseItemDtoQueryResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<BaseItemDtoQueryResult>> getAlbumArtistsWithHttpInfo(Double minCommunityRating, Integer startIndex, Integer limit, String searchTerm, UUID parentId, List<ItemFields> fields, List<BaseItemKind> excludeItemTypes, List<BaseItemKind> includeItemTypes, List<ItemFilter> filters, Boolean isFavorite, List<String> mediaTypes, List<String> genres, List<UUID> genreIds, List<String> officialRatings, List<String> tags, List<Integer> years, Boolean enableUserData, Integer imageTypeLimit, List<ImageType> enableImageTypes, String person, List<UUID> personIds, List<String> personTypes, List<String> studios, List<UUID> studioIds, UUID userId, String nameStartsWithOrGreater, String nameStartsWith, String nameLessThan, List<String> sortBy, List<SortOrder> sortOrder, Boolean enableImages, Boolean enableTotalRecordCount) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {};
        return getAlbumArtistsRequestCreation(minCommunityRating, startIndex, limit, searchTerm, parentId, fields, excludeItemTypes, includeItemTypes, filters, isFavorite, mediaTypes, genres, genreIds, officialRatings, tags, years, enableUserData, imageTypeLimit, enableImageTypes, person, personIds, personTypes, studios, studioIds, userId, nameStartsWithOrGreater, nameStartsWith, nameLessThan, sortBy, sortOrder, enableImages, enableTotalRecordCount).toEntity(localVarReturnType);
    }

    /**
     * Gets all album artists from a given item, folder, or the entire library.
     * 
     * <p><b>200</b> - Album artists returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param minCommunityRating Optional filter by minimum community rating.
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the results.
     * @param limit Optional. The maximum number of records to return.
     * @param searchTerm Optional. Search term.
     * @param parentId Specify this to localize the search to a specific item or folder. Omit to use the root.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param excludeItemTypes Optional. If specified, results will be filtered out based on item type. This allows multiple, comma delimited.
     * @param includeItemTypes Optional. If specified, results will be filtered based on item type. This allows multiple, comma delimited.
     * @param filters Optional. Specify additional filters to apply.
     * @param isFavorite Optional filter by items that are marked as favorite, or not.
     * @param mediaTypes Optional filter by MediaType. Allows multiple, comma delimited.
     * @param genres Optional. If specified, results will be filtered based on genre. This allows multiple, pipe delimited.
     * @param genreIds Optional. If specified, results will be filtered based on genre id. This allows multiple, pipe delimited.
     * @param officialRatings Optional. If specified, results will be filtered based on OfficialRating. This allows multiple, pipe delimited.
     * @param tags Optional. If specified, results will be filtered based on tag. This allows multiple, pipe delimited.
     * @param years Optional. If specified, results will be filtered based on production year. This allows multiple, comma delimited.
     * @param enableUserData Optional, include user data.
     * @param imageTypeLimit Optional, the max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @param person Optional. If specified, results will be filtered to include only those containing the specified person.
     * @param personIds Optional. If specified, results will be filtered to include only those containing the specified person ids.
     * @param personTypes Optional. If specified, along with Person, results will be filtered to include only those containing the specified person and PersonType. Allows multiple, comma-delimited.
     * @param studios Optional. If specified, results will be filtered based on studio. This allows multiple, pipe delimited.
     * @param studioIds Optional. If specified, results will be filtered based on studio id. This allows multiple, pipe delimited.
     * @param userId User id.
     * @param nameStartsWithOrGreater Optional filter by items whose name is sorted equally or greater than a given input string.
     * @param nameStartsWith Optional filter by items whose name is sorted equally than a given input string.
     * @param nameLessThan Optional filter by items whose name is equally or lesser than a given input string.
     * @param sortBy Optional. Specify one or more sort orders, comma delimited.
     * @param sortOrder Sort Order - Ascending,Descending.
     * @param enableImages Optional, include image information in output.
     * @param enableTotalRecordCount Total record count.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getAlbumArtistsWithResponseSpec(Double minCommunityRating, Integer startIndex, Integer limit, String searchTerm, UUID parentId, List<ItemFields> fields, List<BaseItemKind> excludeItemTypes, List<BaseItemKind> includeItemTypes, List<ItemFilter> filters, Boolean isFavorite, List<String> mediaTypes, List<String> genres, List<UUID> genreIds, List<String> officialRatings, List<String> tags, List<Integer> years, Boolean enableUserData, Integer imageTypeLimit, List<ImageType> enableImageTypes, String person, List<UUID> personIds, List<String> personTypes, List<String> studios, List<UUID> studioIds, UUID userId, String nameStartsWithOrGreater, String nameStartsWith, String nameLessThan, List<String> sortBy, List<SortOrder> sortOrder, Boolean enableImages, Boolean enableTotalRecordCount) throws WebClientResponseException {
        return getAlbumArtistsRequestCreation(minCommunityRating, startIndex, limit, searchTerm, parentId, fields, excludeItemTypes, includeItemTypes, filters, isFavorite, mediaTypes, genres, genreIds, officialRatings, tags, years, enableUserData, imageTypeLimit, enableImageTypes, person, personIds, personTypes, studios, studioIds, userId, nameStartsWithOrGreater, nameStartsWith, nameLessThan, sortBy, sortOrder, enableImages, enableTotalRecordCount);
    }

    /**
     * Gets an artist by name.
     * 
     * <p><b>200</b> - Artist returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param name Studio name.
     * @param userId Optional. Filter by user id, and attach user data.
     * @return BaseItemDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getArtistByNameRequestCreation(String name, UUID userId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'name' is set
        if (name == null) {
            throw new WebClientResponseException("Missing the required parameter 'name' when calling getArtistByName", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("name", name);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "userId", userId));
        
        final String[] localVarAccepts = { 
            "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<BaseItemDto> localVarReturnType = new ParameterizedTypeReference<BaseItemDto>() {};
        return apiClient.invokeAPI("/Artists/{name}", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets an artist by name.
     * 
     * <p><b>200</b> - Artist returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param name Studio name.
     * @param userId Optional. Filter by user id, and attach user data.
     * @return BaseItemDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<BaseItemDto> getArtistByName(String name, UUID userId) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDto> localVarReturnType = new ParameterizedTypeReference<BaseItemDto>() {};
        return getArtistByNameRequestCreation(name, userId).bodyToMono(localVarReturnType);
    }

    /**
     * Gets an artist by name.
     * 
     * <p><b>200</b> - Artist returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param name Studio name.
     * @param userId Optional. Filter by user id, and attach user data.
     * @return ResponseEntity&lt;BaseItemDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<BaseItemDto>> getArtistByNameWithHttpInfo(String name, UUID userId) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDto> localVarReturnType = new ParameterizedTypeReference<BaseItemDto>() {};
        return getArtistByNameRequestCreation(name, userId).toEntity(localVarReturnType);
    }

    /**
     * Gets an artist by name.
     * 
     * <p><b>200</b> - Artist returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param name Studio name.
     * @param userId Optional. Filter by user id, and attach user data.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getArtistByNameWithResponseSpec(String name, UUID userId) throws WebClientResponseException {
        return getArtistByNameRequestCreation(name, userId);
    }

    /**
     * Gets all artists from a given item, folder, or the entire library.
     * 
     * <p><b>200</b> - Artists returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param minCommunityRating Optional filter by minimum community rating.
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the results.
     * @param limit Optional. The maximum number of records to return.
     * @param searchTerm Optional. Search term.
     * @param parentId Specify this to localize the search to a specific item or folder. Omit to use the root.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param excludeItemTypes Optional. If specified, results will be filtered out based on item type. This allows multiple, comma delimited.
     * @param includeItemTypes Optional. If specified, results will be filtered based on item type. This allows multiple, comma delimited.
     * @param filters Optional. Specify additional filters to apply.
     * @param isFavorite Optional filter by items that are marked as favorite, or not.
     * @param mediaTypes Optional filter by MediaType. Allows multiple, comma delimited.
     * @param genres Optional. If specified, results will be filtered based on genre. This allows multiple, pipe delimited.
     * @param genreIds Optional. If specified, results will be filtered based on genre id. This allows multiple, pipe delimited.
     * @param officialRatings Optional. If specified, results will be filtered based on OfficialRating. This allows multiple, pipe delimited.
     * @param tags Optional. If specified, results will be filtered based on tag. This allows multiple, pipe delimited.
     * @param years Optional. If specified, results will be filtered based on production year. This allows multiple, comma delimited.
     * @param enableUserData Optional, include user data.
     * @param imageTypeLimit Optional, the max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @param person Optional. If specified, results will be filtered to include only those containing the specified person.
     * @param personIds Optional. If specified, results will be filtered to include only those containing the specified person ids.
     * @param personTypes Optional. If specified, along with Person, results will be filtered to include only those containing the specified person and PersonType. Allows multiple, comma-delimited.
     * @param studios Optional. If specified, results will be filtered based on studio. This allows multiple, pipe delimited.
     * @param studioIds Optional. If specified, results will be filtered based on studio id. This allows multiple, pipe delimited.
     * @param userId User id.
     * @param nameStartsWithOrGreater Optional filter by items whose name is sorted equally or greater than a given input string.
     * @param nameStartsWith Optional filter by items whose name is sorted equally than a given input string.
     * @param nameLessThan Optional filter by items whose name is equally or lesser than a given input string.
     * @param sortBy Optional. Specify one or more sort orders, comma delimited.
     * @param sortOrder Sort Order - Ascending,Descending.
     * @param enableImages Optional, include image information in output.
     * @param enableTotalRecordCount Total record count.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getArtistsRequestCreation(Double minCommunityRating, Integer startIndex, Integer limit, String searchTerm, UUID parentId, List<ItemFields> fields, List<BaseItemKind> excludeItemTypes, List<BaseItemKind> includeItemTypes, List<ItemFilter> filters, Boolean isFavorite, List<String> mediaTypes, List<String> genres, List<UUID> genreIds, List<String> officialRatings, List<String> tags, List<Integer> years, Boolean enableUserData, Integer imageTypeLimit, List<ImageType> enableImageTypes, String person, List<UUID> personIds, List<String> personTypes, List<String> studios, List<UUID> studioIds, UUID userId, String nameStartsWithOrGreater, String nameStartsWith, String nameLessThan, List<String> sortBy, List<SortOrder> sortOrder, Boolean enableImages, Boolean enableTotalRecordCount) throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "minCommunityRating", minCommunityRating));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "startIndex", startIndex));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "limit", limit));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "searchTerm", searchTerm));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "parentId", parentId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "fields", fields));
        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "excludeItemTypes", excludeItemTypes));
        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "includeItemTypes", includeItemTypes));
        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "filters", filters));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isFavorite", isFavorite));
        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "mediaTypes", mediaTypes));
        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "genres", genres));
        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "genreIds", genreIds));
        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "officialRatings", officialRatings));
        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "tags", tags));
        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "years", years));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableUserData", enableUserData));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "imageTypeLimit", imageTypeLimit));
        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "enableImageTypes", enableImageTypes));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "person", person));
        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "personIds", personIds));
        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "personTypes", personTypes));
        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "studios", studios));
        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "studioIds", studioIds));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "userId", userId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "nameStartsWithOrGreater", nameStartsWithOrGreater));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "nameStartsWith", nameStartsWith));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "nameLessThan", nameLessThan));
        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "sortBy", sortBy));
        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "sortOrder", sortOrder));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableImages", enableImages));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableTotalRecordCount", enableTotalRecordCount));
        
        final String[] localVarAccepts = { 
            "application/json", "application/json; profile=CamelCase", "application/json; profile=PascalCase"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {};
        return apiClient.invokeAPI("/Artists", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets all artists from a given item, folder, or the entire library.
     * 
     * <p><b>200</b> - Artists returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param minCommunityRating Optional filter by minimum community rating.
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the results.
     * @param limit Optional. The maximum number of records to return.
     * @param searchTerm Optional. Search term.
     * @param parentId Specify this to localize the search to a specific item or folder. Omit to use the root.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param excludeItemTypes Optional. If specified, results will be filtered out based on item type. This allows multiple, comma delimited.
     * @param includeItemTypes Optional. If specified, results will be filtered based on item type. This allows multiple, comma delimited.
     * @param filters Optional. Specify additional filters to apply.
     * @param isFavorite Optional filter by items that are marked as favorite, or not.
     * @param mediaTypes Optional filter by MediaType. Allows multiple, comma delimited.
     * @param genres Optional. If specified, results will be filtered based on genre. This allows multiple, pipe delimited.
     * @param genreIds Optional. If specified, results will be filtered based on genre id. This allows multiple, pipe delimited.
     * @param officialRatings Optional. If specified, results will be filtered based on OfficialRating. This allows multiple, pipe delimited.
     * @param tags Optional. If specified, results will be filtered based on tag. This allows multiple, pipe delimited.
     * @param years Optional. If specified, results will be filtered based on production year. This allows multiple, comma delimited.
     * @param enableUserData Optional, include user data.
     * @param imageTypeLimit Optional, the max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @param person Optional. If specified, results will be filtered to include only those containing the specified person.
     * @param personIds Optional. If specified, results will be filtered to include only those containing the specified person ids.
     * @param personTypes Optional. If specified, along with Person, results will be filtered to include only those containing the specified person and PersonType. Allows multiple, comma-delimited.
     * @param studios Optional. If specified, results will be filtered based on studio. This allows multiple, pipe delimited.
     * @param studioIds Optional. If specified, results will be filtered based on studio id. This allows multiple, pipe delimited.
     * @param userId User id.
     * @param nameStartsWithOrGreater Optional filter by items whose name is sorted equally or greater than a given input string.
     * @param nameStartsWith Optional filter by items whose name is sorted equally than a given input string.
     * @param nameLessThan Optional filter by items whose name is equally or lesser than a given input string.
     * @param sortBy Optional. Specify one or more sort orders, comma delimited.
     * @param sortOrder Sort Order - Ascending,Descending.
     * @param enableImages Optional, include image information in output.
     * @param enableTotalRecordCount Total record count.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<BaseItemDtoQueryResult> getArtists(Double minCommunityRating, Integer startIndex, Integer limit, String searchTerm, UUID parentId, List<ItemFields> fields, List<BaseItemKind> excludeItemTypes, List<BaseItemKind> includeItemTypes, List<ItemFilter> filters, Boolean isFavorite, List<String> mediaTypes, List<String> genres, List<UUID> genreIds, List<String> officialRatings, List<String> tags, List<Integer> years, Boolean enableUserData, Integer imageTypeLimit, List<ImageType> enableImageTypes, String person, List<UUID> personIds, List<String> personTypes, List<String> studios, List<UUID> studioIds, UUID userId, String nameStartsWithOrGreater, String nameStartsWith, String nameLessThan, List<String> sortBy, List<SortOrder> sortOrder, Boolean enableImages, Boolean enableTotalRecordCount) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {};
        return getArtistsRequestCreation(minCommunityRating, startIndex, limit, searchTerm, parentId, fields, excludeItemTypes, includeItemTypes, filters, isFavorite, mediaTypes, genres, genreIds, officialRatings, tags, years, enableUserData, imageTypeLimit, enableImageTypes, person, personIds, personTypes, studios, studioIds, userId, nameStartsWithOrGreater, nameStartsWith, nameLessThan, sortBy, sortOrder, enableImages, enableTotalRecordCount).bodyToMono(localVarReturnType);
    }

    /**
     * Gets all artists from a given item, folder, or the entire library.
     * 
     * <p><b>200</b> - Artists returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param minCommunityRating Optional filter by minimum community rating.
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the results.
     * @param limit Optional. The maximum number of records to return.
     * @param searchTerm Optional. Search term.
     * @param parentId Specify this to localize the search to a specific item or folder. Omit to use the root.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param excludeItemTypes Optional. If specified, results will be filtered out based on item type. This allows multiple, comma delimited.
     * @param includeItemTypes Optional. If specified, results will be filtered based on item type. This allows multiple, comma delimited.
     * @param filters Optional. Specify additional filters to apply.
     * @param isFavorite Optional filter by items that are marked as favorite, or not.
     * @param mediaTypes Optional filter by MediaType. Allows multiple, comma delimited.
     * @param genres Optional. If specified, results will be filtered based on genre. This allows multiple, pipe delimited.
     * @param genreIds Optional. If specified, results will be filtered based on genre id. This allows multiple, pipe delimited.
     * @param officialRatings Optional. If specified, results will be filtered based on OfficialRating. This allows multiple, pipe delimited.
     * @param tags Optional. If specified, results will be filtered based on tag. This allows multiple, pipe delimited.
     * @param years Optional. If specified, results will be filtered based on production year. This allows multiple, comma delimited.
     * @param enableUserData Optional, include user data.
     * @param imageTypeLimit Optional, the max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @param person Optional. If specified, results will be filtered to include only those containing the specified person.
     * @param personIds Optional. If specified, results will be filtered to include only those containing the specified person ids.
     * @param personTypes Optional. If specified, along with Person, results will be filtered to include only those containing the specified person and PersonType. Allows multiple, comma-delimited.
     * @param studios Optional. If specified, results will be filtered based on studio. This allows multiple, pipe delimited.
     * @param studioIds Optional. If specified, results will be filtered based on studio id. This allows multiple, pipe delimited.
     * @param userId User id.
     * @param nameStartsWithOrGreater Optional filter by items whose name is sorted equally or greater than a given input string.
     * @param nameStartsWith Optional filter by items whose name is sorted equally than a given input string.
     * @param nameLessThan Optional filter by items whose name is equally or lesser than a given input string.
     * @param sortBy Optional. Specify one or more sort orders, comma delimited.
     * @param sortOrder Sort Order - Ascending,Descending.
     * @param enableImages Optional, include image information in output.
     * @param enableTotalRecordCount Total record count.
     * @return ResponseEntity&lt;BaseItemDtoQueryResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<BaseItemDtoQueryResult>> getArtistsWithHttpInfo(Double minCommunityRating, Integer startIndex, Integer limit, String searchTerm, UUID parentId, List<ItemFields> fields, List<BaseItemKind> excludeItemTypes, List<BaseItemKind> includeItemTypes, List<ItemFilter> filters, Boolean isFavorite, List<String> mediaTypes, List<String> genres, List<UUID> genreIds, List<String> officialRatings, List<String> tags, List<Integer> years, Boolean enableUserData, Integer imageTypeLimit, List<ImageType> enableImageTypes, String person, List<UUID> personIds, List<String> personTypes, List<String> studios, List<UUID> studioIds, UUID userId, String nameStartsWithOrGreater, String nameStartsWith, String nameLessThan, List<String> sortBy, List<SortOrder> sortOrder, Boolean enableImages, Boolean enableTotalRecordCount) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {};
        return getArtistsRequestCreation(minCommunityRating, startIndex, limit, searchTerm, parentId, fields, excludeItemTypes, includeItemTypes, filters, isFavorite, mediaTypes, genres, genreIds, officialRatings, tags, years, enableUserData, imageTypeLimit, enableImageTypes, person, personIds, personTypes, studios, studioIds, userId, nameStartsWithOrGreater, nameStartsWith, nameLessThan, sortBy, sortOrder, enableImages, enableTotalRecordCount).toEntity(localVarReturnType);
    }

    /**
     * Gets all artists from a given item, folder, or the entire library.
     * 
     * <p><b>200</b> - Artists returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param minCommunityRating Optional filter by minimum community rating.
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the results.
     * @param limit Optional. The maximum number of records to return.
     * @param searchTerm Optional. Search term.
     * @param parentId Specify this to localize the search to a specific item or folder. Omit to use the root.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param excludeItemTypes Optional. If specified, results will be filtered out based on item type. This allows multiple, comma delimited.
     * @param includeItemTypes Optional. If specified, results will be filtered based on item type. This allows multiple, comma delimited.
     * @param filters Optional. Specify additional filters to apply.
     * @param isFavorite Optional filter by items that are marked as favorite, or not.
     * @param mediaTypes Optional filter by MediaType. Allows multiple, comma delimited.
     * @param genres Optional. If specified, results will be filtered based on genre. This allows multiple, pipe delimited.
     * @param genreIds Optional. If specified, results will be filtered based on genre id. This allows multiple, pipe delimited.
     * @param officialRatings Optional. If specified, results will be filtered based on OfficialRating. This allows multiple, pipe delimited.
     * @param tags Optional. If specified, results will be filtered based on tag. This allows multiple, pipe delimited.
     * @param years Optional. If specified, results will be filtered based on production year. This allows multiple, comma delimited.
     * @param enableUserData Optional, include user data.
     * @param imageTypeLimit Optional, the max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @param person Optional. If specified, results will be filtered to include only those containing the specified person.
     * @param personIds Optional. If specified, results will be filtered to include only those containing the specified person ids.
     * @param personTypes Optional. If specified, along with Person, results will be filtered to include only those containing the specified person and PersonType. Allows multiple, comma-delimited.
     * @param studios Optional. If specified, results will be filtered based on studio. This allows multiple, pipe delimited.
     * @param studioIds Optional. If specified, results will be filtered based on studio id. This allows multiple, pipe delimited.
     * @param userId User id.
     * @param nameStartsWithOrGreater Optional filter by items whose name is sorted equally or greater than a given input string.
     * @param nameStartsWith Optional filter by items whose name is sorted equally than a given input string.
     * @param nameLessThan Optional filter by items whose name is equally or lesser than a given input string.
     * @param sortBy Optional. Specify one or more sort orders, comma delimited.
     * @param sortOrder Sort Order - Ascending,Descending.
     * @param enableImages Optional, include image information in output.
     * @param enableTotalRecordCount Total record count.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getArtistsWithResponseSpec(Double minCommunityRating, Integer startIndex, Integer limit, String searchTerm, UUID parentId, List<ItemFields> fields, List<BaseItemKind> excludeItemTypes, List<BaseItemKind> includeItemTypes, List<ItemFilter> filters, Boolean isFavorite, List<String> mediaTypes, List<String> genres, List<UUID> genreIds, List<String> officialRatings, List<String> tags, List<Integer> years, Boolean enableUserData, Integer imageTypeLimit, List<ImageType> enableImageTypes, String person, List<UUID> personIds, List<String> personTypes, List<String> studios, List<UUID> studioIds, UUID userId, String nameStartsWithOrGreater, String nameStartsWith, String nameLessThan, List<String> sortBy, List<SortOrder> sortOrder, Boolean enableImages, Boolean enableTotalRecordCount) throws WebClientResponseException {
        return getArtistsRequestCreation(minCommunityRating, startIndex, limit, searchTerm, parentId, fields, excludeItemTypes, includeItemTypes, filters, isFavorite, mediaTypes, genres, genreIds, officialRatings, tags, years, enableUserData, imageTypeLimit, enableImageTypes, person, personIds, personTypes, studios, studioIds, userId, nameStartsWithOrGreater, nameStartsWith, nameLessThan, sortBy, sortOrder, enableImages, enableTotalRecordCount);
    }
}
