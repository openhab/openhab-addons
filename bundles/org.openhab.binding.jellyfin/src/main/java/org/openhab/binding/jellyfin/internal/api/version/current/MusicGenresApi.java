package org.openhab.binding.jellyfin.internal.api.version.current;

import org.openhab.binding.jellyfin.internal.api.version.ApiClient;

import org.openhab.binding.jellyfin.internal.api.version.current.model.BaseItemDto;
import org.openhab.binding.jellyfin.internal.api.version.current.model.BaseItemDtoQueryResult;
import org.openhab.binding.jellyfin.internal.api.version.current.model.BaseItemKind;
import org.openhab.binding.jellyfin.internal.api.version.current.model.ImageType;
import org.openhab.binding.jellyfin.internal.api.version.current.model.ItemFields;
import org.openhab.binding.jellyfin.internal.api.version.current.model.ItemSortBy;
import org.openhab.binding.jellyfin.internal.api.version.current.model.SortOrder;
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
public class MusicGenresApi {
    private ApiClient apiClient;

    public MusicGenresApi() {
        this(new ApiClient());
    }

    @Autowired
    public MusicGenresApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    
    /**
     * Gets a music genre, by name.
     * 
     * <p><b>200</b> - Success
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param genreName The genre name.
     * @param userId Optional. Filter by user id, and attach user data.
     * @return BaseItemDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getMusicGenreRequestCreation(String genreName, UUID userId) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'genreName' is set
        if (genreName == null) {
            throw new WebClientResponseException("Missing the required parameter 'genreName' when calling getMusicGenre", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("genreName", genreName);

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
        return apiClient.invokeAPI("/MusicGenres/{genreName}", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets a music genre, by name.
     * 
     * <p><b>200</b> - Success
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param genreName The genre name.
     * @param userId Optional. Filter by user id, and attach user data.
     * @return BaseItemDto
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<BaseItemDto> getMusicGenre(String genreName, UUID userId) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDto> localVarReturnType = new ParameterizedTypeReference<BaseItemDto>() {};
        return getMusicGenreRequestCreation(genreName, userId).bodyToMono(localVarReturnType);
    }

    /**
     * Gets a music genre, by name.
     * 
     * <p><b>200</b> - Success
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param genreName The genre name.
     * @param userId Optional. Filter by user id, and attach user data.
     * @return ResponseEntity&lt;BaseItemDto&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<BaseItemDto>> getMusicGenreWithHttpInfo(String genreName, UUID userId) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDto> localVarReturnType = new ParameterizedTypeReference<BaseItemDto>() {};
        return getMusicGenreRequestCreation(genreName, userId).toEntity(localVarReturnType);
    }

    /**
     * Gets a music genre, by name.
     * 
     * <p><b>200</b> - Success
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param genreName The genre name.
     * @param userId Optional. Filter by user id, and attach user data.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getMusicGenreWithResponseSpec(String genreName, UUID userId) throws WebClientResponseException {
        return getMusicGenreRequestCreation(genreName, userId);
    }

    /**
     * Gets all music genres from a given item, folder, or the entire library.
     * 
     * <p><b>200</b> - Music genres returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the results.
     * @param limit Optional. The maximum number of records to return.
     * @param searchTerm The search term.
     * @param parentId Specify this to localize the search to a specific item or folder. Omit to use the root.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param excludeItemTypes Optional. If specified, results will be filtered out based on item type. This allows multiple, comma delimited.
     * @param includeItemTypes Optional. If specified, results will be filtered in based on item type. This allows multiple, comma delimited.
     * @param isFavorite Optional filter by items that are marked as favorite, or not.
     * @param imageTypeLimit Optional, the max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @param userId User id.
     * @param nameStartsWithOrGreater Optional filter by items whose name is sorted equally or greater than a given input string.
     * @param nameStartsWith Optional filter by items whose name is sorted equally than a given input string.
     * @param nameLessThan Optional filter by items whose name is equally or lesser than a given input string.
     * @param sortBy Optional. Specify one or more sort orders, comma delimited.
     * @param sortOrder Sort Order - Ascending,Descending.
     * @param enableImages Optional, include image information in output.
     * @param enableTotalRecordCount Optional. Include total record count.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     * @deprecated
     */
    @Deprecated
    private ResponseSpec getMusicGenresRequestCreation(Integer startIndex, Integer limit, String searchTerm, UUID parentId, List<ItemFields> fields, List<BaseItemKind> excludeItemTypes, List<BaseItemKind> includeItemTypes, Boolean isFavorite, Integer imageTypeLimit, List<ImageType> enableImageTypes, UUID userId, String nameStartsWithOrGreater, String nameStartsWith, String nameLessThan, List<ItemSortBy> sortBy, List<SortOrder> sortOrder, Boolean enableImages, Boolean enableTotalRecordCount) throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "startIndex", startIndex));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "limit", limit));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "searchTerm", searchTerm));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "parentId", parentId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "fields", fields));
        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "excludeItemTypes", excludeItemTypes));
        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "includeItemTypes", includeItemTypes));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isFavorite", isFavorite));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "imageTypeLimit", imageTypeLimit));
        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "enableImageTypes", enableImageTypes));
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
        return apiClient.invokeAPI("/MusicGenres", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets all music genres from a given item, folder, or the entire library.
     * 
     * <p><b>200</b> - Music genres returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the results.
     * @param limit Optional. The maximum number of records to return.
     * @param searchTerm The search term.
     * @param parentId Specify this to localize the search to a specific item or folder. Omit to use the root.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param excludeItemTypes Optional. If specified, results will be filtered out based on item type. This allows multiple, comma delimited.
     * @param includeItemTypes Optional. If specified, results will be filtered in based on item type. This allows multiple, comma delimited.
     * @param isFavorite Optional filter by items that are marked as favorite, or not.
     * @param imageTypeLimit Optional, the max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @param userId User id.
     * @param nameStartsWithOrGreater Optional filter by items whose name is sorted equally or greater than a given input string.
     * @param nameStartsWith Optional filter by items whose name is sorted equally than a given input string.
     * @param nameLessThan Optional filter by items whose name is equally or lesser than a given input string.
     * @param sortBy Optional. Specify one or more sort orders, comma delimited.
     * @param sortOrder Sort Order - Ascending,Descending.
     * @param enableImages Optional, include image information in output.
     * @param enableTotalRecordCount Optional. Include total record count.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<BaseItemDtoQueryResult> getMusicGenres(Integer startIndex, Integer limit, String searchTerm, UUID parentId, List<ItemFields> fields, List<BaseItemKind> excludeItemTypes, List<BaseItemKind> includeItemTypes, Boolean isFavorite, Integer imageTypeLimit, List<ImageType> enableImageTypes, UUID userId, String nameStartsWithOrGreater, String nameStartsWith, String nameLessThan, List<ItemSortBy> sortBy, List<SortOrder> sortOrder, Boolean enableImages, Boolean enableTotalRecordCount) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {};
        return getMusicGenresRequestCreation(startIndex, limit, searchTerm, parentId, fields, excludeItemTypes, includeItemTypes, isFavorite, imageTypeLimit, enableImageTypes, userId, nameStartsWithOrGreater, nameStartsWith, nameLessThan, sortBy, sortOrder, enableImages, enableTotalRecordCount).bodyToMono(localVarReturnType);
    }

    /**
     * Gets all music genres from a given item, folder, or the entire library.
     * 
     * <p><b>200</b> - Music genres returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the results.
     * @param limit Optional. The maximum number of records to return.
     * @param searchTerm The search term.
     * @param parentId Specify this to localize the search to a specific item or folder. Omit to use the root.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param excludeItemTypes Optional. If specified, results will be filtered out based on item type. This allows multiple, comma delimited.
     * @param includeItemTypes Optional. If specified, results will be filtered in based on item type. This allows multiple, comma delimited.
     * @param isFavorite Optional filter by items that are marked as favorite, or not.
     * @param imageTypeLimit Optional, the max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @param userId User id.
     * @param nameStartsWithOrGreater Optional filter by items whose name is sorted equally or greater than a given input string.
     * @param nameStartsWith Optional filter by items whose name is sorted equally than a given input string.
     * @param nameLessThan Optional filter by items whose name is equally or lesser than a given input string.
     * @param sortBy Optional. Specify one or more sort orders, comma delimited.
     * @param sortOrder Sort Order - Ascending,Descending.
     * @param enableImages Optional, include image information in output.
     * @param enableTotalRecordCount Optional. Include total record count.
     * @return ResponseEntity&lt;BaseItemDtoQueryResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<BaseItemDtoQueryResult>> getMusicGenresWithHttpInfo(Integer startIndex, Integer limit, String searchTerm, UUID parentId, List<ItemFields> fields, List<BaseItemKind> excludeItemTypes, List<BaseItemKind> includeItemTypes, Boolean isFavorite, Integer imageTypeLimit, List<ImageType> enableImageTypes, UUID userId, String nameStartsWithOrGreater, String nameStartsWith, String nameLessThan, List<ItemSortBy> sortBy, List<SortOrder> sortOrder, Boolean enableImages, Boolean enableTotalRecordCount) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {};
        return getMusicGenresRequestCreation(startIndex, limit, searchTerm, parentId, fields, excludeItemTypes, includeItemTypes, isFavorite, imageTypeLimit, enableImageTypes, userId, nameStartsWithOrGreater, nameStartsWith, nameLessThan, sortBy, sortOrder, enableImages, enableTotalRecordCount).toEntity(localVarReturnType);
    }

    /**
     * Gets all music genres from a given item, folder, or the entire library.
     * 
     * <p><b>200</b> - Music genres returned.
     * <p><b>401</b> - Unauthorized
     * <p><b>403</b> - Forbidden
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the results.
     * @param limit Optional. The maximum number of records to return.
     * @param searchTerm The search term.
     * @param parentId Specify this to localize the search to a specific item or folder. Omit to use the root.
     * @param fields Optional. Specify additional fields of information to return in the output.
     * @param excludeItemTypes Optional. If specified, results will be filtered out based on item type. This allows multiple, comma delimited.
     * @param includeItemTypes Optional. If specified, results will be filtered in based on item type. This allows multiple, comma delimited.
     * @param isFavorite Optional filter by items that are marked as favorite, or not.
     * @param imageTypeLimit Optional, the max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @param userId User id.
     * @param nameStartsWithOrGreater Optional filter by items whose name is sorted equally or greater than a given input string.
     * @param nameStartsWith Optional filter by items whose name is sorted equally than a given input string.
     * @param nameLessThan Optional filter by items whose name is equally or lesser than a given input string.
     * @param sortBy Optional. Specify one or more sort orders, comma delimited.
     * @param sortOrder Sort Order - Ascending,Descending.
     * @param enableImages Optional, include image information in output.
     * @param enableTotalRecordCount Optional. Include total record count.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getMusicGenresWithResponseSpec(Integer startIndex, Integer limit, String searchTerm, UUID parentId, List<ItemFields> fields, List<BaseItemKind> excludeItemTypes, List<BaseItemKind> includeItemTypes, Boolean isFavorite, Integer imageTypeLimit, List<ImageType> enableImageTypes, UUID userId, String nameStartsWithOrGreater, String nameStartsWith, String nameLessThan, List<ItemSortBy> sortBy, List<SortOrder> sortOrder, Boolean enableImages, Boolean enableTotalRecordCount) throws WebClientResponseException {
        return getMusicGenresRequestCreation(startIndex, limit, searchTerm, parentId, fields, excludeItemTypes, includeItemTypes, isFavorite, imageTypeLimit, enableImageTypes, userId, nameStartsWithOrGreater, nameStartsWith, nameLessThan, sortBy, sortOrder, enableImages, enableTotalRecordCount);
    }
}
