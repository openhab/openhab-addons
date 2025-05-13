package org.openhab.binding.jellyfin.internal.api.version.legacy;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.openhab.binding.jellyfin.internal.api.version.ApiClient;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.BaseItemDtoQueryResult;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.BaseItemKind;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.ImageType;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.ItemFields;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.ItemFilter;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.LocationType;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.SeriesStatus;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.SortOrder;
import org.openhab.binding.jellyfin.internal.api.version.legacy.model.VideoType;
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
public class ItemsApi {
    private ApiClient apiClient;

    public ItemsApi() {
        this(new ApiClient());
    }

    @Autowired
    public ItemsApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Gets items based on a query.
     * 
     * <p>
     * <b>200</b> - Success
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param userId The user id supplied as query parameter.
     * @param maxOfficialRating Optional filter by maximum official rating (PG, PG-13, TV-MA, etc).
     * @param hasThemeSong Optional filter by items with theme songs.
     * @param hasThemeVideo Optional filter by items with theme videos.
     * @param hasSubtitles Optional filter by items with subtitles.
     * @param hasSpecialFeature Optional filter by items with special features.
     * @param hasTrailer Optional filter by items with trailers.
     * @param adjacentTo Optional. Return items that are siblings of a supplied item.
     * @param parentIndexNumber Optional filter by parent index number.
     * @param hasParentalRating Optional filter by items that have or do not have a parental rating.
     * @param isHd Optional filter by items that are HD or not.
     * @param is4K Optional filter by items that are 4K or not.
     * @param locationTypes Optional. If specified, results will be filtered based on LocationType. This allows
     *            multiple, comma delimited.
     * @param excludeLocationTypes Optional. If specified, results will be filtered based on the LocationType. This
     *            allows multiple, comma delimited.
     * @param isMissing Optional filter by items that are missing episodes or not.
     * @param isUnaired Optional filter by items that are unaired episodes or not.
     * @param minCommunityRating Optional filter by minimum community rating.
     * @param minCriticRating Optional filter by minimum critic rating.
     * @param minPremiereDate Optional. The minimum premiere date. Format &#x3D; ISO.
     * @param minDateLastSaved Optional. The minimum last saved date. Format &#x3D; ISO.
     * @param minDateLastSavedForUser Optional. The minimum last saved date for the current user. Format &#x3D; ISO.
     * @param maxPremiereDate Optional. The maximum premiere date. Format &#x3D; ISO.
     * @param hasOverview Optional filter by items that have an overview or not.
     * @param hasImdbId Optional filter by items that have an imdb id or not.
     * @param hasTmdbId Optional filter by items that have a tmdb id or not.
     * @param hasTvdbId Optional filter by items that have a tvdb id or not.
     * @param isMovie Optional filter for live tv movies.
     * @param isSeries Optional filter for live tv series.
     * @param isNews Optional filter for live tv news.
     * @param isKids Optional filter for live tv kids.
     * @param isSports Optional filter for live tv sports.
     * @param excludeItemIds Optional. If specified, results will be filtered by excluding item ids. This allows
     *            multiple, comma delimited.
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results.
     * @param limit Optional. The maximum number of records to return.
     * @param recursive When searching within folders, this determines whether or not the search will be recursive.
     *            true/false.
     * @param searchTerm Optional. Filter based on a search term.
     * @param sortOrder Sort Order - Ascending,Descending.
     * @param parentId Specify this to localize the search to a specific item or folder. Omit to use the root.
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple,
     *            comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions,
     *            MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue,
     *            SortName, Studios, Taglines.
     * @param excludeItemTypes Optional. If specified, results will be filtered based on item type. This allows
     *            multiple, comma delimited.
     * @param includeItemTypes Optional. If specified, results will be filtered based on the item type. This allows
     *            multiple, comma delimited.
     * @param filters Optional. Specify additional filters to apply. This allows multiple, comma delimited. Options:
     *            IsFolder, IsNotFolder, IsUnplayed, IsPlayed, IsFavorite, IsResumable, Likes, Dislikes.
     * @param isFavorite Optional filter by items that are marked as favorite, or not.
     * @param mediaTypes Optional filter by MediaType. Allows multiple, comma delimited.
     * @param imageTypes Optional. If specified, results will be filtered based on those containing image types. This
     *            allows multiple, comma delimited.
     * @param sortBy Optional. Specify one or more sort orders, comma delimited. Options: Album, AlbumArtist, Artist,
     *            Budget, CommunityRating, CriticRating, DateCreated, DatePlayed, PlayCount, PremiereDate,
     *            ProductionYear, SortName, Random, Revenue, Runtime.
     * @param isPlayed Optional filter by items that are played, or not.
     * @param genres Optional. If specified, results will be filtered based on genre. This allows multiple, pipe
     *            delimited.
     * @param officialRatings Optional. If specified, results will be filtered based on OfficialRating. This allows
     *            multiple, pipe delimited.
     * @param tags Optional. If specified, results will be filtered based on tag. This allows multiple, pipe delimited.
     * @param years Optional. If specified, results will be filtered based on production year. This allows multiple,
     *            comma delimited.
     * @param enableUserData Optional, include user data.
     * @param imageTypeLimit Optional, the max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @param person Optional. If specified, results will be filtered to include only those containing the specified
     *            person.
     * @param personIds Optional. If specified, results will be filtered to include only those containing the specified
     *            person id.
     * @param personTypes Optional. If specified, along with Person, results will be filtered to include only those
     *            containing the specified person and PersonType. Allows multiple, comma-delimited.
     * @param studios Optional. If specified, results will be filtered based on studio. This allows multiple, pipe
     *            delimited.
     * @param artists Optional. If specified, results will be filtered based on artists. This allows multiple, pipe
     *            delimited.
     * @param excludeArtistIds Optional. If specified, results will be filtered based on artist id. This allows
     *            multiple, pipe delimited.
     * @param artistIds Optional. If specified, results will be filtered to include only those containing the specified
     *            artist id.
     * @param albumArtistIds Optional. If specified, results will be filtered to include only those containing the
     *            specified album artist id.
     * @param contributingArtistIds Optional. If specified, results will be filtered to include only those containing
     *            the specified contributing artist id.
     * @param albums Optional. If specified, results will be filtered based on album. This allows multiple, pipe
     *            delimited.
     * @param albumIds Optional. If specified, results will be filtered based on album id. This allows multiple, pipe
     *            delimited.
     * @param ids Optional. If specific items are needed, specify a list of item id&#39;s to retrieve. This allows
     *            multiple, comma delimited.
     * @param videoTypes Optional filter by VideoType (videofile, dvd, bluray, iso). Allows multiple, comma delimited.
     * @param minOfficialRating Optional filter by minimum official rating (PG, PG-13, TV-MA, etc).
     * @param isLocked Optional filter by items that are locked.
     * @param isPlaceHolder Optional filter by items that are placeholders.
     * @param hasOfficialRating Optional filter by items that have official ratings.
     * @param collapseBoxSetItems Whether or not to hide items behind their boxsets.
     * @param minWidth Optional. Filter by the minimum width of the item.
     * @param minHeight Optional. Filter by the minimum height of the item.
     * @param maxWidth Optional. Filter by the maximum width of the item.
     * @param maxHeight Optional. Filter by the maximum height of the item.
     * @param is3D Optional filter by items that are 3D, or not.
     * @param seriesStatus Optional filter by Series Status. Allows multiple, comma delimited.
     * @param nameStartsWithOrGreater Optional filter by items whose name is sorted equally or greater than a given
     *            input string.
     * @param nameStartsWith Optional filter by items whose name is sorted equally than a given input string.
     * @param nameLessThan Optional filter by items whose name is equally or lesser than a given input string.
     * @param studioIds Optional. If specified, results will be filtered based on studio id. This allows multiple, pipe
     *            delimited.
     * @param genreIds Optional. If specified, results will be filtered based on genre id. This allows multiple, pipe
     *            delimited.
     * @param enableTotalRecordCount Optional. Enable the total record count.
     * @param enableImages Optional, include image information in output.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getItemsRequestCreation(UUID userId, String maxOfficialRating, Boolean hasThemeSong,
            Boolean hasThemeVideo, Boolean hasSubtitles, Boolean hasSpecialFeature, Boolean hasTrailer,
            String adjacentTo, Integer parentIndexNumber, Boolean hasParentalRating, Boolean isHd, Boolean is4K,
            List<LocationType> locationTypes, List<LocationType> excludeLocationTypes, Boolean isMissing,
            Boolean isUnaired, Double minCommunityRating, Double minCriticRating, OffsetDateTime minPremiereDate,
            OffsetDateTime minDateLastSaved, OffsetDateTime minDateLastSavedForUser, OffsetDateTime maxPremiereDate,
            Boolean hasOverview, Boolean hasImdbId, Boolean hasTmdbId, Boolean hasTvdbId, Boolean isMovie,
            Boolean isSeries, Boolean isNews, Boolean isKids, Boolean isSports, List<UUID> excludeItemIds,
            Integer startIndex, Integer limit, Boolean recursive, String searchTerm, List<SortOrder> sortOrder,
            UUID parentId, List<ItemFields> fields, List<BaseItemKind> excludeItemTypes,
            List<BaseItemKind> includeItemTypes, List<ItemFilter> filters, Boolean isFavorite, List<String> mediaTypes,
            List<ImageType> imageTypes, List<String> sortBy, Boolean isPlayed, List<String> genres,
            List<String> officialRatings, List<String> tags, List<Integer> years, Boolean enableUserData,
            Integer imageTypeLimit, List<ImageType> enableImageTypes, String person, List<UUID> personIds,
            List<String> personTypes, List<String> studios, List<String> artists, List<UUID> excludeArtistIds,
            List<UUID> artistIds, List<UUID> albumArtistIds, List<UUID> contributingArtistIds, List<String> albums,
            List<UUID> albumIds, List<UUID> ids, List<VideoType> videoTypes, String minOfficialRating, Boolean isLocked,
            Boolean isPlaceHolder, Boolean hasOfficialRating, Boolean collapseBoxSetItems, Integer minWidth,
            Integer minHeight, Integer maxWidth, Integer maxHeight, Boolean is3D, List<SeriesStatus> seriesStatus,
            String nameStartsWithOrGreater, String nameStartsWith, String nameLessThan, List<UUID> studioIds,
            List<UUID> genreIds, Boolean enableTotalRecordCount, Boolean enableImages)
            throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "userId", userId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxOfficialRating", maxOfficialRating));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "hasThemeSong", hasThemeSong));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "hasThemeVideo", hasThemeVideo));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "hasSubtitles", hasSubtitles));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "hasSpecialFeature", hasSpecialFeature));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "hasTrailer", hasTrailer));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "adjacentTo", adjacentTo));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "parentIndexNumber", parentIndexNumber));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "hasParentalRating", hasParentalRating));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isHd", isHd));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "is4K", is4K));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "locationTypes", locationTypes));
        queryParams.putAll(
                apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)),
                        "excludeLocationTypes", excludeLocationTypes));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isMissing", isMissing));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isUnaired", isUnaired));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "minCommunityRating", minCommunityRating));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "minCriticRating", minCriticRating));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "minPremiereDate", minPremiereDate));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "minDateLastSaved", minDateLastSaved));
        queryParams
                .putAll(apiClient.parameterToMultiValueMap(null, "minDateLastSavedForUser", minDateLastSavedForUser));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxPremiereDate", maxPremiereDate));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "hasOverview", hasOverview));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "hasImdbId", hasImdbId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "hasTmdbId", hasTmdbId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "hasTvdbId", hasTvdbId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isMovie", isMovie));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isSeries", isSeries));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isNews", isNews));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isKids", isKids));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isSports", isSports));
        queryParams.putAll(
                apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)),
                        "excludeItemIds", excludeItemIds));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "startIndex", startIndex));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "limit", limit));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "recursive", recursive));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "searchTerm", searchTerm));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "sortOrder", sortOrder));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "parentId", parentId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "fields", fields));
        queryParams.putAll(
                apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)),
                        "excludeItemTypes", excludeItemTypes));
        queryParams.putAll(
                apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)),
                        "includeItemTypes", includeItemTypes));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "filters", filters));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isFavorite", isFavorite));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "mediaTypes", mediaTypes));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "imageTypes", imageTypes));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "sortBy", sortBy));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isPlayed", isPlayed));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "genres", genres));
        queryParams.putAll(
                apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)),
                        "officialRatings", officialRatings));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "tags", tags));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "years", years));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableUserData", enableUserData));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "imageTypeLimit", imageTypeLimit));
        queryParams.putAll(
                apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)),
                        "enableImageTypes", enableImageTypes));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "person", person));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "personIds", personIds));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "personTypes", personTypes));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "studios", studios));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "artists", artists));
        queryParams.putAll(
                apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)),
                        "excludeArtistIds", excludeArtistIds));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "artistIds", artistIds));
        queryParams.putAll(
                apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)),
                        "albumArtistIds", albumArtistIds));
        queryParams.putAll(
                apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)),
                        "contributingArtistIds", contributingArtistIds));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "albums", albums));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "albumIds", albumIds));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "ids", ids));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "videoTypes", videoTypes));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "minOfficialRating", minOfficialRating));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isLocked", isLocked));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isPlaceHolder", isPlaceHolder));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "hasOfficialRating", hasOfficialRating));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "collapseBoxSetItems", collapseBoxSetItems));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "minWidth", minWidth));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "minHeight", minHeight));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxWidth", maxWidth));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxHeight", maxHeight));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "is3D", is3D));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "seriesStatus", seriesStatus));
        queryParams
                .putAll(apiClient.parameterToMultiValueMap(null, "nameStartsWithOrGreater", nameStartsWithOrGreater));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "nameStartsWith", nameStartsWith));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "nameLessThan", nameLessThan));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "studioIds", studioIds));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "genreIds", genreIds));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableTotalRecordCount", enableTotalRecordCount));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableImages", enableImages));

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {
        };
        return apiClient.invokeAPI("/Items", HttpMethod.GET, pathParams, queryParams, postBody, headerParams,
                cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Gets items based on a query.
     * 
     * <p>
     * <b>200</b> - Success
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param userId The user id supplied as query parameter.
     * @param maxOfficialRating Optional filter by maximum official rating (PG, PG-13, TV-MA, etc).
     * @param hasThemeSong Optional filter by items with theme songs.
     * @param hasThemeVideo Optional filter by items with theme videos.
     * @param hasSubtitles Optional filter by items with subtitles.
     * @param hasSpecialFeature Optional filter by items with special features.
     * @param hasTrailer Optional filter by items with trailers.
     * @param adjacentTo Optional. Return items that are siblings of a supplied item.
     * @param parentIndexNumber Optional filter by parent index number.
     * @param hasParentalRating Optional filter by items that have or do not have a parental rating.
     * @param isHd Optional filter by items that are HD or not.
     * @param is4K Optional filter by items that are 4K or not.
     * @param locationTypes Optional. If specified, results will be filtered based on LocationType. This allows
     *            multiple, comma delimited.
     * @param excludeLocationTypes Optional. If specified, results will be filtered based on the LocationType. This
     *            allows multiple, comma delimited.
     * @param isMissing Optional filter by items that are missing episodes or not.
     * @param isUnaired Optional filter by items that are unaired episodes or not.
     * @param minCommunityRating Optional filter by minimum community rating.
     * @param minCriticRating Optional filter by minimum critic rating.
     * @param minPremiereDate Optional. The minimum premiere date. Format &#x3D; ISO.
     * @param minDateLastSaved Optional. The minimum last saved date. Format &#x3D; ISO.
     * @param minDateLastSavedForUser Optional. The minimum last saved date for the current user. Format &#x3D; ISO.
     * @param maxPremiereDate Optional. The maximum premiere date. Format &#x3D; ISO.
     * @param hasOverview Optional filter by items that have an overview or not.
     * @param hasImdbId Optional filter by items that have an imdb id or not.
     * @param hasTmdbId Optional filter by items that have a tmdb id or not.
     * @param hasTvdbId Optional filter by items that have a tvdb id or not.
     * @param isMovie Optional filter for live tv movies.
     * @param isSeries Optional filter for live tv series.
     * @param isNews Optional filter for live tv news.
     * @param isKids Optional filter for live tv kids.
     * @param isSports Optional filter for live tv sports.
     * @param excludeItemIds Optional. If specified, results will be filtered by excluding item ids. This allows
     *            multiple, comma delimited.
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results.
     * @param limit Optional. The maximum number of records to return.
     * @param recursive When searching within folders, this determines whether or not the search will be recursive.
     *            true/false.
     * @param searchTerm Optional. Filter based on a search term.
     * @param sortOrder Sort Order - Ascending,Descending.
     * @param parentId Specify this to localize the search to a specific item or folder. Omit to use the root.
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple,
     *            comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions,
     *            MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue,
     *            SortName, Studios, Taglines.
     * @param excludeItemTypes Optional. If specified, results will be filtered based on item type. This allows
     *            multiple, comma delimited.
     * @param includeItemTypes Optional. If specified, results will be filtered based on the item type. This allows
     *            multiple, comma delimited.
     * @param filters Optional. Specify additional filters to apply. This allows multiple, comma delimited. Options:
     *            IsFolder, IsNotFolder, IsUnplayed, IsPlayed, IsFavorite, IsResumable, Likes, Dislikes.
     * @param isFavorite Optional filter by items that are marked as favorite, or not.
     * @param mediaTypes Optional filter by MediaType. Allows multiple, comma delimited.
     * @param imageTypes Optional. If specified, results will be filtered based on those containing image types. This
     *            allows multiple, comma delimited.
     * @param sortBy Optional. Specify one or more sort orders, comma delimited. Options: Album, AlbumArtist, Artist,
     *            Budget, CommunityRating, CriticRating, DateCreated, DatePlayed, PlayCount, PremiereDate,
     *            ProductionYear, SortName, Random, Revenue, Runtime.
     * @param isPlayed Optional filter by items that are played, or not.
     * @param genres Optional. If specified, results will be filtered based on genre. This allows multiple, pipe
     *            delimited.
     * @param officialRatings Optional. If specified, results will be filtered based on OfficialRating. This allows
     *            multiple, pipe delimited.
     * @param tags Optional. If specified, results will be filtered based on tag. This allows multiple, pipe delimited.
     * @param years Optional. If specified, results will be filtered based on production year. This allows multiple,
     *            comma delimited.
     * @param enableUserData Optional, include user data.
     * @param imageTypeLimit Optional, the max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @param person Optional. If specified, results will be filtered to include only those containing the specified
     *            person.
     * @param personIds Optional. If specified, results will be filtered to include only those containing the specified
     *            person id.
     * @param personTypes Optional. If specified, along with Person, results will be filtered to include only those
     *            containing the specified person and PersonType. Allows multiple, comma-delimited.
     * @param studios Optional. If specified, results will be filtered based on studio. This allows multiple, pipe
     *            delimited.
     * @param artists Optional. If specified, results will be filtered based on artists. This allows multiple, pipe
     *            delimited.
     * @param excludeArtistIds Optional. If specified, results will be filtered based on artist id. This allows
     *            multiple, pipe delimited.
     * @param artistIds Optional. If specified, results will be filtered to include only those containing the specified
     *            artist id.
     * @param albumArtistIds Optional. If specified, results will be filtered to include only those containing the
     *            specified album artist id.
     * @param contributingArtistIds Optional. If specified, results will be filtered to include only those containing
     *            the specified contributing artist id.
     * @param albums Optional. If specified, results will be filtered based on album. This allows multiple, pipe
     *            delimited.
     * @param albumIds Optional. If specified, results will be filtered based on album id. This allows multiple, pipe
     *            delimited.
     * @param ids Optional. If specific items are needed, specify a list of item id&#39;s to retrieve. This allows
     *            multiple, comma delimited.
     * @param videoTypes Optional filter by VideoType (videofile, dvd, bluray, iso). Allows multiple, comma delimited.
     * @param minOfficialRating Optional filter by minimum official rating (PG, PG-13, TV-MA, etc).
     * @param isLocked Optional filter by items that are locked.
     * @param isPlaceHolder Optional filter by items that are placeholders.
     * @param hasOfficialRating Optional filter by items that have official ratings.
     * @param collapseBoxSetItems Whether or not to hide items behind their boxsets.
     * @param minWidth Optional. Filter by the minimum width of the item.
     * @param minHeight Optional. Filter by the minimum height of the item.
     * @param maxWidth Optional. Filter by the maximum width of the item.
     * @param maxHeight Optional. Filter by the maximum height of the item.
     * @param is3D Optional filter by items that are 3D, or not.
     * @param seriesStatus Optional filter by Series Status. Allows multiple, comma delimited.
     * @param nameStartsWithOrGreater Optional filter by items whose name is sorted equally or greater than a given
     *            input string.
     * @param nameStartsWith Optional filter by items whose name is sorted equally than a given input string.
     * @param nameLessThan Optional filter by items whose name is equally or lesser than a given input string.
     * @param studioIds Optional. If specified, results will be filtered based on studio id. This allows multiple, pipe
     *            delimited.
     * @param genreIds Optional. If specified, results will be filtered based on genre id. This allows multiple, pipe
     *            delimited.
     * @param enableTotalRecordCount Optional. Enable the total record count.
     * @param enableImages Optional, include image information in output.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<BaseItemDtoQueryResult> getItems(UUID userId, String maxOfficialRating, Boolean hasThemeSong,
            Boolean hasThemeVideo, Boolean hasSubtitles, Boolean hasSpecialFeature, Boolean hasTrailer,
            String adjacentTo, Integer parentIndexNumber, Boolean hasParentalRating, Boolean isHd, Boolean is4K,
            List<LocationType> locationTypes, List<LocationType> excludeLocationTypes, Boolean isMissing,
            Boolean isUnaired, Double minCommunityRating, Double minCriticRating, OffsetDateTime minPremiereDate,
            OffsetDateTime minDateLastSaved, OffsetDateTime minDateLastSavedForUser, OffsetDateTime maxPremiereDate,
            Boolean hasOverview, Boolean hasImdbId, Boolean hasTmdbId, Boolean hasTvdbId, Boolean isMovie,
            Boolean isSeries, Boolean isNews, Boolean isKids, Boolean isSports, List<UUID> excludeItemIds,
            Integer startIndex, Integer limit, Boolean recursive, String searchTerm, List<SortOrder> sortOrder,
            UUID parentId, List<ItemFields> fields, List<BaseItemKind> excludeItemTypes,
            List<BaseItemKind> includeItemTypes, List<ItemFilter> filters, Boolean isFavorite, List<String> mediaTypes,
            List<ImageType> imageTypes, List<String> sortBy, Boolean isPlayed, List<String> genres,
            List<String> officialRatings, List<String> tags, List<Integer> years, Boolean enableUserData,
            Integer imageTypeLimit, List<ImageType> enableImageTypes, String person, List<UUID> personIds,
            List<String> personTypes, List<String> studios, List<String> artists, List<UUID> excludeArtistIds,
            List<UUID> artistIds, List<UUID> albumArtistIds, List<UUID> contributingArtistIds, List<String> albums,
            List<UUID> albumIds, List<UUID> ids, List<VideoType> videoTypes, String minOfficialRating, Boolean isLocked,
            Boolean isPlaceHolder, Boolean hasOfficialRating, Boolean collapseBoxSetItems, Integer minWidth,
            Integer minHeight, Integer maxWidth, Integer maxHeight, Boolean is3D, List<SeriesStatus> seriesStatus,
            String nameStartsWithOrGreater, String nameStartsWith, String nameLessThan, List<UUID> studioIds,
            List<UUID> genreIds, Boolean enableTotalRecordCount, Boolean enableImages)
            throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {
        };
        return getItemsRequestCreation(userId, maxOfficialRating, hasThemeSong, hasThemeVideo, hasSubtitles,
                hasSpecialFeature, hasTrailer, adjacentTo, parentIndexNumber, hasParentalRating, isHd, is4K,
                locationTypes, excludeLocationTypes, isMissing, isUnaired, minCommunityRating, minCriticRating,
                minPremiereDate, minDateLastSaved, minDateLastSavedForUser, maxPremiereDate, hasOverview, hasImdbId,
                hasTmdbId, hasTvdbId, isMovie, isSeries, isNews, isKids, isSports, excludeItemIds, startIndex, limit,
                recursive, searchTerm, sortOrder, parentId, fields, excludeItemTypes, includeItemTypes, filters,
                isFavorite, mediaTypes, imageTypes, sortBy, isPlayed, genres, officialRatings, tags, years,
                enableUserData, imageTypeLimit, enableImageTypes, person, personIds, personTypes, studios, artists,
                excludeArtistIds, artistIds, albumArtistIds, contributingArtistIds, albums, albumIds, ids, videoTypes,
                minOfficialRating, isLocked, isPlaceHolder, hasOfficialRating, collapseBoxSetItems, minWidth, minHeight,
                maxWidth, maxHeight, is3D, seriesStatus, nameStartsWithOrGreater, nameStartsWith, nameLessThan,
                studioIds, genreIds, enableTotalRecordCount, enableImages).bodyToMono(localVarReturnType);
    }

    /**
     * Gets items based on a query.
     * 
     * <p>
     * <b>200</b> - Success
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param userId The user id supplied as query parameter.
     * @param maxOfficialRating Optional filter by maximum official rating (PG, PG-13, TV-MA, etc).
     * @param hasThemeSong Optional filter by items with theme songs.
     * @param hasThemeVideo Optional filter by items with theme videos.
     * @param hasSubtitles Optional filter by items with subtitles.
     * @param hasSpecialFeature Optional filter by items with special features.
     * @param hasTrailer Optional filter by items with trailers.
     * @param adjacentTo Optional. Return items that are siblings of a supplied item.
     * @param parentIndexNumber Optional filter by parent index number.
     * @param hasParentalRating Optional filter by items that have or do not have a parental rating.
     * @param isHd Optional filter by items that are HD or not.
     * @param is4K Optional filter by items that are 4K or not.
     * @param locationTypes Optional. If specified, results will be filtered based on LocationType. This allows
     *            multiple, comma delimited.
     * @param excludeLocationTypes Optional. If specified, results will be filtered based on the LocationType. This
     *            allows multiple, comma delimited.
     * @param isMissing Optional filter by items that are missing episodes or not.
     * @param isUnaired Optional filter by items that are unaired episodes or not.
     * @param minCommunityRating Optional filter by minimum community rating.
     * @param minCriticRating Optional filter by minimum critic rating.
     * @param minPremiereDate Optional. The minimum premiere date. Format &#x3D; ISO.
     * @param minDateLastSaved Optional. The minimum last saved date. Format &#x3D; ISO.
     * @param minDateLastSavedForUser Optional. The minimum last saved date for the current user. Format &#x3D; ISO.
     * @param maxPremiereDate Optional. The maximum premiere date. Format &#x3D; ISO.
     * @param hasOverview Optional filter by items that have an overview or not.
     * @param hasImdbId Optional filter by items that have an imdb id or not.
     * @param hasTmdbId Optional filter by items that have a tmdb id or not.
     * @param hasTvdbId Optional filter by items that have a tvdb id or not.
     * @param isMovie Optional filter for live tv movies.
     * @param isSeries Optional filter for live tv series.
     * @param isNews Optional filter for live tv news.
     * @param isKids Optional filter for live tv kids.
     * @param isSports Optional filter for live tv sports.
     * @param excludeItemIds Optional. If specified, results will be filtered by excluding item ids. This allows
     *            multiple, comma delimited.
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results.
     * @param limit Optional. The maximum number of records to return.
     * @param recursive When searching within folders, this determines whether or not the search will be recursive.
     *            true/false.
     * @param searchTerm Optional. Filter based on a search term.
     * @param sortOrder Sort Order - Ascending,Descending.
     * @param parentId Specify this to localize the search to a specific item or folder. Omit to use the root.
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple,
     *            comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions,
     *            MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue,
     *            SortName, Studios, Taglines.
     * @param excludeItemTypes Optional. If specified, results will be filtered based on item type. This allows
     *            multiple, comma delimited.
     * @param includeItemTypes Optional. If specified, results will be filtered based on the item type. This allows
     *            multiple, comma delimited.
     * @param filters Optional. Specify additional filters to apply. This allows multiple, comma delimited. Options:
     *            IsFolder, IsNotFolder, IsUnplayed, IsPlayed, IsFavorite, IsResumable, Likes, Dislikes.
     * @param isFavorite Optional filter by items that are marked as favorite, or not.
     * @param mediaTypes Optional filter by MediaType. Allows multiple, comma delimited.
     * @param imageTypes Optional. If specified, results will be filtered based on those containing image types. This
     *            allows multiple, comma delimited.
     * @param sortBy Optional. Specify one or more sort orders, comma delimited. Options: Album, AlbumArtist, Artist,
     *            Budget, CommunityRating, CriticRating, DateCreated, DatePlayed, PlayCount, PremiereDate,
     *            ProductionYear, SortName, Random, Revenue, Runtime.
     * @param isPlayed Optional filter by items that are played, or not.
     * @param genres Optional. If specified, results will be filtered based on genre. This allows multiple, pipe
     *            delimited.
     * @param officialRatings Optional. If specified, results will be filtered based on OfficialRating. This allows
     *            multiple, pipe delimited.
     * @param tags Optional. If specified, results will be filtered based on tag. This allows multiple, pipe delimited.
     * @param years Optional. If specified, results will be filtered based on production year. This allows multiple,
     *            comma delimited.
     * @param enableUserData Optional, include user data.
     * @param imageTypeLimit Optional, the max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @param person Optional. If specified, results will be filtered to include only those containing the specified
     *            person.
     * @param personIds Optional. If specified, results will be filtered to include only those containing the specified
     *            person id.
     * @param personTypes Optional. If specified, along with Person, results will be filtered to include only those
     *            containing the specified person and PersonType. Allows multiple, comma-delimited.
     * @param studios Optional. If specified, results will be filtered based on studio. This allows multiple, pipe
     *            delimited.
     * @param artists Optional. If specified, results will be filtered based on artists. This allows multiple, pipe
     *            delimited.
     * @param excludeArtistIds Optional. If specified, results will be filtered based on artist id. This allows
     *            multiple, pipe delimited.
     * @param artistIds Optional. If specified, results will be filtered to include only those containing the specified
     *            artist id.
     * @param albumArtistIds Optional. If specified, results will be filtered to include only those containing the
     *            specified album artist id.
     * @param contributingArtistIds Optional. If specified, results will be filtered to include only those containing
     *            the specified contributing artist id.
     * @param albums Optional. If specified, results will be filtered based on album. This allows multiple, pipe
     *            delimited.
     * @param albumIds Optional. If specified, results will be filtered based on album id. This allows multiple, pipe
     *            delimited.
     * @param ids Optional. If specific items are needed, specify a list of item id&#39;s to retrieve. This allows
     *            multiple, comma delimited.
     * @param videoTypes Optional filter by VideoType (videofile, dvd, bluray, iso). Allows multiple, comma delimited.
     * @param minOfficialRating Optional filter by minimum official rating (PG, PG-13, TV-MA, etc).
     * @param isLocked Optional filter by items that are locked.
     * @param isPlaceHolder Optional filter by items that are placeholders.
     * @param hasOfficialRating Optional filter by items that have official ratings.
     * @param collapseBoxSetItems Whether or not to hide items behind their boxsets.
     * @param minWidth Optional. Filter by the minimum width of the item.
     * @param minHeight Optional. Filter by the minimum height of the item.
     * @param maxWidth Optional. Filter by the maximum width of the item.
     * @param maxHeight Optional. Filter by the maximum height of the item.
     * @param is3D Optional filter by items that are 3D, or not.
     * @param seriesStatus Optional filter by Series Status. Allows multiple, comma delimited.
     * @param nameStartsWithOrGreater Optional filter by items whose name is sorted equally or greater than a given
     *            input string.
     * @param nameStartsWith Optional filter by items whose name is sorted equally than a given input string.
     * @param nameLessThan Optional filter by items whose name is equally or lesser than a given input string.
     * @param studioIds Optional. If specified, results will be filtered based on studio id. This allows multiple, pipe
     *            delimited.
     * @param genreIds Optional. If specified, results will be filtered based on genre id. This allows multiple, pipe
     *            delimited.
     * @param enableTotalRecordCount Optional. Enable the total record count.
     * @param enableImages Optional, include image information in output.
     * @return ResponseEntity&lt;BaseItemDtoQueryResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<BaseItemDtoQueryResult>> getItemsWithHttpInfo(UUID userId, String maxOfficialRating,
            Boolean hasThemeSong, Boolean hasThemeVideo, Boolean hasSubtitles, Boolean hasSpecialFeature,
            Boolean hasTrailer, String adjacentTo, Integer parentIndexNumber, Boolean hasParentalRating, Boolean isHd,
            Boolean is4K, List<LocationType> locationTypes, List<LocationType> excludeLocationTypes, Boolean isMissing,
            Boolean isUnaired, Double minCommunityRating, Double minCriticRating, OffsetDateTime minPremiereDate,
            OffsetDateTime minDateLastSaved, OffsetDateTime minDateLastSavedForUser, OffsetDateTime maxPremiereDate,
            Boolean hasOverview, Boolean hasImdbId, Boolean hasTmdbId, Boolean hasTvdbId, Boolean isMovie,
            Boolean isSeries, Boolean isNews, Boolean isKids, Boolean isSports, List<UUID> excludeItemIds,
            Integer startIndex, Integer limit, Boolean recursive, String searchTerm, List<SortOrder> sortOrder,
            UUID parentId, List<ItemFields> fields, List<BaseItemKind> excludeItemTypes,
            List<BaseItemKind> includeItemTypes, List<ItemFilter> filters, Boolean isFavorite, List<String> mediaTypes,
            List<ImageType> imageTypes, List<String> sortBy, Boolean isPlayed, List<String> genres,
            List<String> officialRatings, List<String> tags, List<Integer> years, Boolean enableUserData,
            Integer imageTypeLimit, List<ImageType> enableImageTypes, String person, List<UUID> personIds,
            List<String> personTypes, List<String> studios, List<String> artists, List<UUID> excludeArtistIds,
            List<UUID> artistIds, List<UUID> albumArtistIds, List<UUID> contributingArtistIds, List<String> albums,
            List<UUID> albumIds, List<UUID> ids, List<VideoType> videoTypes, String minOfficialRating, Boolean isLocked,
            Boolean isPlaceHolder, Boolean hasOfficialRating, Boolean collapseBoxSetItems, Integer minWidth,
            Integer minHeight, Integer maxWidth, Integer maxHeight, Boolean is3D, List<SeriesStatus> seriesStatus,
            String nameStartsWithOrGreater, String nameStartsWith, String nameLessThan, List<UUID> studioIds,
            List<UUID> genreIds, Boolean enableTotalRecordCount, Boolean enableImages)
            throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {
        };
        return getItemsRequestCreation(userId, maxOfficialRating, hasThemeSong, hasThemeVideo, hasSubtitles,
                hasSpecialFeature, hasTrailer, adjacentTo, parentIndexNumber, hasParentalRating, isHd, is4K,
                locationTypes, excludeLocationTypes, isMissing, isUnaired, minCommunityRating, minCriticRating,
                minPremiereDate, minDateLastSaved, minDateLastSavedForUser, maxPremiereDate, hasOverview, hasImdbId,
                hasTmdbId, hasTvdbId, isMovie, isSeries, isNews, isKids, isSports, excludeItemIds, startIndex, limit,
                recursive, searchTerm, sortOrder, parentId, fields, excludeItemTypes, includeItemTypes, filters,
                isFavorite, mediaTypes, imageTypes, sortBy, isPlayed, genres, officialRatings, tags, years,
                enableUserData, imageTypeLimit, enableImageTypes, person, personIds, personTypes, studios, artists,
                excludeArtistIds, artistIds, albumArtistIds, contributingArtistIds, albums, albumIds, ids, videoTypes,
                minOfficialRating, isLocked, isPlaceHolder, hasOfficialRating, collapseBoxSetItems, minWidth, minHeight,
                maxWidth, maxHeight, is3D, seriesStatus, nameStartsWithOrGreater, nameStartsWith, nameLessThan,
                studioIds, genreIds, enableTotalRecordCount, enableImages).toEntity(localVarReturnType);
    }

    /**
     * Gets items based on a query.
     * 
     * <p>
     * <b>200</b> - Success
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param userId The user id supplied as query parameter.
     * @param maxOfficialRating Optional filter by maximum official rating (PG, PG-13, TV-MA, etc).
     * @param hasThemeSong Optional filter by items with theme songs.
     * @param hasThemeVideo Optional filter by items with theme videos.
     * @param hasSubtitles Optional filter by items with subtitles.
     * @param hasSpecialFeature Optional filter by items with special features.
     * @param hasTrailer Optional filter by items with trailers.
     * @param adjacentTo Optional. Return items that are siblings of a supplied item.
     * @param parentIndexNumber Optional filter by parent index number.
     * @param hasParentalRating Optional filter by items that have or do not have a parental rating.
     * @param isHd Optional filter by items that are HD or not.
     * @param is4K Optional filter by items that are 4K or not.
     * @param locationTypes Optional. If specified, results will be filtered based on LocationType. This allows
     *            multiple, comma delimited.
     * @param excludeLocationTypes Optional. If specified, results will be filtered based on the LocationType. This
     *            allows multiple, comma delimited.
     * @param isMissing Optional filter by items that are missing episodes or not.
     * @param isUnaired Optional filter by items that are unaired episodes or not.
     * @param minCommunityRating Optional filter by minimum community rating.
     * @param minCriticRating Optional filter by minimum critic rating.
     * @param minPremiereDate Optional. The minimum premiere date. Format &#x3D; ISO.
     * @param minDateLastSaved Optional. The minimum last saved date. Format &#x3D; ISO.
     * @param minDateLastSavedForUser Optional. The minimum last saved date for the current user. Format &#x3D; ISO.
     * @param maxPremiereDate Optional. The maximum premiere date. Format &#x3D; ISO.
     * @param hasOverview Optional filter by items that have an overview or not.
     * @param hasImdbId Optional filter by items that have an imdb id or not.
     * @param hasTmdbId Optional filter by items that have a tmdb id or not.
     * @param hasTvdbId Optional filter by items that have a tvdb id or not.
     * @param isMovie Optional filter for live tv movies.
     * @param isSeries Optional filter for live tv series.
     * @param isNews Optional filter for live tv news.
     * @param isKids Optional filter for live tv kids.
     * @param isSports Optional filter for live tv sports.
     * @param excludeItemIds Optional. If specified, results will be filtered by excluding item ids. This allows
     *            multiple, comma delimited.
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results.
     * @param limit Optional. The maximum number of records to return.
     * @param recursive When searching within folders, this determines whether or not the search will be recursive.
     *            true/false.
     * @param searchTerm Optional. Filter based on a search term.
     * @param sortOrder Sort Order - Ascending,Descending.
     * @param parentId Specify this to localize the search to a specific item or folder. Omit to use the root.
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple,
     *            comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions,
     *            MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue,
     *            SortName, Studios, Taglines.
     * @param excludeItemTypes Optional. If specified, results will be filtered based on item type. This allows
     *            multiple, comma delimited.
     * @param includeItemTypes Optional. If specified, results will be filtered based on the item type. This allows
     *            multiple, comma delimited.
     * @param filters Optional. Specify additional filters to apply. This allows multiple, comma delimited. Options:
     *            IsFolder, IsNotFolder, IsUnplayed, IsPlayed, IsFavorite, IsResumable, Likes, Dislikes.
     * @param isFavorite Optional filter by items that are marked as favorite, or not.
     * @param mediaTypes Optional filter by MediaType. Allows multiple, comma delimited.
     * @param imageTypes Optional. If specified, results will be filtered based on those containing image types. This
     *            allows multiple, comma delimited.
     * @param sortBy Optional. Specify one or more sort orders, comma delimited. Options: Album, AlbumArtist, Artist,
     *            Budget, CommunityRating, CriticRating, DateCreated, DatePlayed, PlayCount, PremiereDate,
     *            ProductionYear, SortName, Random, Revenue, Runtime.
     * @param isPlayed Optional filter by items that are played, or not.
     * @param genres Optional. If specified, results will be filtered based on genre. This allows multiple, pipe
     *            delimited.
     * @param officialRatings Optional. If specified, results will be filtered based on OfficialRating. This allows
     *            multiple, pipe delimited.
     * @param tags Optional. If specified, results will be filtered based on tag. This allows multiple, pipe delimited.
     * @param years Optional. If specified, results will be filtered based on production year. This allows multiple,
     *            comma delimited.
     * @param enableUserData Optional, include user data.
     * @param imageTypeLimit Optional, the max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @param person Optional. If specified, results will be filtered to include only those containing the specified
     *            person.
     * @param personIds Optional. If specified, results will be filtered to include only those containing the specified
     *            person id.
     * @param personTypes Optional. If specified, along with Person, results will be filtered to include only those
     *            containing the specified person and PersonType. Allows multiple, comma-delimited.
     * @param studios Optional. If specified, results will be filtered based on studio. This allows multiple, pipe
     *            delimited.
     * @param artists Optional. If specified, results will be filtered based on artists. This allows multiple, pipe
     *            delimited.
     * @param excludeArtistIds Optional. If specified, results will be filtered based on artist id. This allows
     *            multiple, pipe delimited.
     * @param artistIds Optional. If specified, results will be filtered to include only those containing the specified
     *            artist id.
     * @param albumArtistIds Optional. If specified, results will be filtered to include only those containing the
     *            specified album artist id.
     * @param contributingArtistIds Optional. If specified, results will be filtered to include only those containing
     *            the specified contributing artist id.
     * @param albums Optional. If specified, results will be filtered based on album. This allows multiple, pipe
     *            delimited.
     * @param albumIds Optional. If specified, results will be filtered based on album id. This allows multiple, pipe
     *            delimited.
     * @param ids Optional. If specific items are needed, specify a list of item id&#39;s to retrieve. This allows
     *            multiple, comma delimited.
     * @param videoTypes Optional filter by VideoType (videofile, dvd, bluray, iso). Allows multiple, comma delimited.
     * @param minOfficialRating Optional filter by minimum official rating (PG, PG-13, TV-MA, etc).
     * @param isLocked Optional filter by items that are locked.
     * @param isPlaceHolder Optional filter by items that are placeholders.
     * @param hasOfficialRating Optional filter by items that have official ratings.
     * @param collapseBoxSetItems Whether or not to hide items behind their boxsets.
     * @param minWidth Optional. Filter by the minimum width of the item.
     * @param minHeight Optional. Filter by the minimum height of the item.
     * @param maxWidth Optional. Filter by the maximum width of the item.
     * @param maxHeight Optional. Filter by the maximum height of the item.
     * @param is3D Optional filter by items that are 3D, or not.
     * @param seriesStatus Optional filter by Series Status. Allows multiple, comma delimited.
     * @param nameStartsWithOrGreater Optional filter by items whose name is sorted equally or greater than a given
     *            input string.
     * @param nameStartsWith Optional filter by items whose name is sorted equally than a given input string.
     * @param nameLessThan Optional filter by items whose name is equally or lesser than a given input string.
     * @param studioIds Optional. If specified, results will be filtered based on studio id. This allows multiple, pipe
     *            delimited.
     * @param genreIds Optional. If specified, results will be filtered based on genre id. This allows multiple, pipe
     *            delimited.
     * @param enableTotalRecordCount Optional. Enable the total record count.
     * @param enableImages Optional, include image information in output.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getItemsWithResponseSpec(UUID userId, String maxOfficialRating, Boolean hasThemeSong,
            Boolean hasThemeVideo, Boolean hasSubtitles, Boolean hasSpecialFeature, Boolean hasTrailer,
            String adjacentTo, Integer parentIndexNumber, Boolean hasParentalRating, Boolean isHd, Boolean is4K,
            List<LocationType> locationTypes, List<LocationType> excludeLocationTypes, Boolean isMissing,
            Boolean isUnaired, Double minCommunityRating, Double minCriticRating, OffsetDateTime minPremiereDate,
            OffsetDateTime minDateLastSaved, OffsetDateTime minDateLastSavedForUser, OffsetDateTime maxPremiereDate,
            Boolean hasOverview, Boolean hasImdbId, Boolean hasTmdbId, Boolean hasTvdbId, Boolean isMovie,
            Boolean isSeries, Boolean isNews, Boolean isKids, Boolean isSports, List<UUID> excludeItemIds,
            Integer startIndex, Integer limit, Boolean recursive, String searchTerm, List<SortOrder> sortOrder,
            UUID parentId, List<ItemFields> fields, List<BaseItemKind> excludeItemTypes,
            List<BaseItemKind> includeItemTypes, List<ItemFilter> filters, Boolean isFavorite, List<String> mediaTypes,
            List<ImageType> imageTypes, List<String> sortBy, Boolean isPlayed, List<String> genres,
            List<String> officialRatings, List<String> tags, List<Integer> years, Boolean enableUserData,
            Integer imageTypeLimit, List<ImageType> enableImageTypes, String person, List<UUID> personIds,
            List<String> personTypes, List<String> studios, List<String> artists, List<UUID> excludeArtistIds,
            List<UUID> artistIds, List<UUID> albumArtistIds, List<UUID> contributingArtistIds, List<String> albums,
            List<UUID> albumIds, List<UUID> ids, List<VideoType> videoTypes, String minOfficialRating, Boolean isLocked,
            Boolean isPlaceHolder, Boolean hasOfficialRating, Boolean collapseBoxSetItems, Integer minWidth,
            Integer minHeight, Integer maxWidth, Integer maxHeight, Boolean is3D, List<SeriesStatus> seriesStatus,
            String nameStartsWithOrGreater, String nameStartsWith, String nameLessThan, List<UUID> studioIds,
            List<UUID> genreIds, Boolean enableTotalRecordCount, Boolean enableImages)
            throws WebClientResponseException {
        return getItemsRequestCreation(userId, maxOfficialRating, hasThemeSong, hasThemeVideo, hasSubtitles,
                hasSpecialFeature, hasTrailer, adjacentTo, parentIndexNumber, hasParentalRating, isHd, is4K,
                locationTypes, excludeLocationTypes, isMissing, isUnaired, minCommunityRating, minCriticRating,
                minPremiereDate, minDateLastSaved, minDateLastSavedForUser, maxPremiereDate, hasOverview, hasImdbId,
                hasTmdbId, hasTvdbId, isMovie, isSeries, isNews, isKids, isSports, excludeItemIds, startIndex, limit,
                recursive, searchTerm, sortOrder, parentId, fields, excludeItemTypes, includeItemTypes, filters,
                isFavorite, mediaTypes, imageTypes, sortBy, isPlayed, genres, officialRatings, tags, years,
                enableUserData, imageTypeLimit, enableImageTypes, person, personIds, personTypes, studios, artists,
                excludeArtistIds, artistIds, albumArtistIds, contributingArtistIds, albums, albumIds, ids, videoTypes,
                minOfficialRating, isLocked, isPlaceHolder, hasOfficialRating, collapseBoxSetItems, minWidth, minHeight,
                maxWidth, maxHeight, is3D, seriesStatus, nameStartsWithOrGreater, nameStartsWith, nameLessThan,
                studioIds, genreIds, enableTotalRecordCount, enableImages);
    }

    /**
     * Gets items based on a query.
     * 
     * <p>
     * <b>200</b> - Success
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param userId The user id supplied as query parameter.
     * @param maxOfficialRating Optional filter by maximum official rating (PG, PG-13, TV-MA, etc).
     * @param hasThemeSong Optional filter by items with theme songs.
     * @param hasThemeVideo Optional filter by items with theme videos.
     * @param hasSubtitles Optional filter by items with subtitles.
     * @param hasSpecialFeature Optional filter by items with special features.
     * @param hasTrailer Optional filter by items with trailers.
     * @param adjacentTo Optional. Return items that are siblings of a supplied item.
     * @param parentIndexNumber Optional filter by parent index number.
     * @param hasParentalRating Optional filter by items that have or do not have a parental rating.
     * @param isHd Optional filter by items that are HD or not.
     * @param is4K Optional filter by items that are 4K or not.
     * @param locationTypes Optional. If specified, results will be filtered based on LocationType. This allows
     *            multiple, comma delimited.
     * @param excludeLocationTypes Optional. If specified, results will be filtered based on the LocationType. This
     *            allows multiple, comma delimited.
     * @param isMissing Optional filter by items that are missing episodes or not.
     * @param isUnaired Optional filter by items that are unaired episodes or not.
     * @param minCommunityRating Optional filter by minimum community rating.
     * @param minCriticRating Optional filter by minimum critic rating.
     * @param minPremiereDate Optional. The minimum premiere date. Format &#x3D; ISO.
     * @param minDateLastSaved Optional. The minimum last saved date. Format &#x3D; ISO.
     * @param minDateLastSavedForUser Optional. The minimum last saved date for the current user. Format &#x3D; ISO.
     * @param maxPremiereDate Optional. The maximum premiere date. Format &#x3D; ISO.
     * @param hasOverview Optional filter by items that have an overview or not.
     * @param hasImdbId Optional filter by items that have an imdb id or not.
     * @param hasTmdbId Optional filter by items that have a tmdb id or not.
     * @param hasTvdbId Optional filter by items that have a tvdb id or not.
     * @param isMovie Optional filter for live tv movies.
     * @param isSeries Optional filter for live tv series.
     * @param isNews Optional filter for live tv news.
     * @param isKids Optional filter for live tv kids.
     * @param isSports Optional filter for live tv sports.
     * @param excludeItemIds Optional. If specified, results will be filtered by excluding item ids. This allows
     *            multiple, comma delimited.
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results.
     * @param limit Optional. The maximum number of records to return.
     * @param recursive When searching within folders, this determines whether or not the search will be recursive.
     *            true/false.
     * @param searchTerm Optional. Filter based on a search term.
     * @param sortOrder Sort Order - Ascending,Descending.
     * @param parentId Specify this to localize the search to a specific item or folder. Omit to use the root.
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple,
     *            comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions,
     *            MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue,
     *            SortName, Studios, Taglines.
     * @param excludeItemTypes Optional. If specified, results will be filtered based on item type. This allows
     *            multiple, comma delimited.
     * @param includeItemTypes Optional. If specified, results will be filtered based on the item type. This allows
     *            multiple, comma delimited.
     * @param filters Optional. Specify additional filters to apply. This allows multiple, comma delimited. Options:
     *            IsFolder, IsNotFolder, IsUnplayed, IsPlayed, IsFavorite, IsResumable, Likes, Dislikes.
     * @param isFavorite Optional filter by items that are marked as favorite, or not.
     * @param mediaTypes Optional filter by MediaType. Allows multiple, comma delimited.
     * @param imageTypes Optional. If specified, results will be filtered based on those containing image types. This
     *            allows multiple, comma delimited.
     * @param sortBy Optional. Specify one or more sort orders, comma delimited. Options: Album, AlbumArtist, Artist,
     *            Budget, CommunityRating, CriticRating, DateCreated, DatePlayed, PlayCount, PremiereDate,
     *            ProductionYear, SortName, Random, Revenue, Runtime.
     * @param isPlayed Optional filter by items that are played, or not.
     * @param genres Optional. If specified, results will be filtered based on genre. This allows multiple, pipe
     *            delimited.
     * @param officialRatings Optional. If specified, results will be filtered based on OfficialRating. This allows
     *            multiple, pipe delimited.
     * @param tags Optional. If specified, results will be filtered based on tag. This allows multiple, pipe delimited.
     * @param years Optional. If specified, results will be filtered based on production year. This allows multiple,
     *            comma delimited.
     * @param enableUserData Optional, include user data.
     * @param imageTypeLimit Optional, the max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @param person Optional. If specified, results will be filtered to include only those containing the specified
     *            person.
     * @param personIds Optional. If specified, results will be filtered to include only those containing the specified
     *            person id.
     * @param personTypes Optional. If specified, along with Person, results will be filtered to include only those
     *            containing the specified person and PersonType. Allows multiple, comma-delimited.
     * @param studios Optional. If specified, results will be filtered based on studio. This allows multiple, pipe
     *            delimited.
     * @param artists Optional. If specified, results will be filtered based on artists. This allows multiple, pipe
     *            delimited.
     * @param excludeArtistIds Optional. If specified, results will be filtered based on artist id. This allows
     *            multiple, pipe delimited.
     * @param artistIds Optional. If specified, results will be filtered to include only those containing the specified
     *            artist id.
     * @param albumArtistIds Optional. If specified, results will be filtered to include only those containing the
     *            specified album artist id.
     * @param contributingArtistIds Optional. If specified, results will be filtered to include only those containing
     *            the specified contributing artist id.
     * @param albums Optional. If specified, results will be filtered based on album. This allows multiple, pipe
     *            delimited.
     * @param albumIds Optional. If specified, results will be filtered based on album id. This allows multiple, pipe
     *            delimited.
     * @param ids Optional. If specific items are needed, specify a list of item id&#39;s to retrieve. This allows
     *            multiple, comma delimited.
     * @param videoTypes Optional filter by VideoType (videofile, dvd, bluray, iso). Allows multiple, comma delimited.
     * @param minOfficialRating Optional filter by minimum official rating (PG, PG-13, TV-MA, etc).
     * @param isLocked Optional filter by items that are locked.
     * @param isPlaceHolder Optional filter by items that are placeholders.
     * @param hasOfficialRating Optional filter by items that have official ratings.
     * @param collapseBoxSetItems Whether or not to hide items behind their boxsets.
     * @param minWidth Optional. Filter by the minimum width of the item.
     * @param minHeight Optional. Filter by the minimum height of the item.
     * @param maxWidth Optional. Filter by the maximum width of the item.
     * @param maxHeight Optional. Filter by the maximum height of the item.
     * @param is3D Optional filter by items that are 3D, or not.
     * @param seriesStatus Optional filter by Series Status. Allows multiple, comma delimited.
     * @param nameStartsWithOrGreater Optional filter by items whose name is sorted equally or greater than a given
     *            input string.
     * @param nameStartsWith Optional filter by items whose name is sorted equally than a given input string.
     * @param nameLessThan Optional filter by items whose name is equally or lesser than a given input string.
     * @param studioIds Optional. If specified, results will be filtered based on studio id. This allows multiple, pipe
     *            delimited.
     * @param genreIds Optional. If specified, results will be filtered based on genre id. This allows multiple, pipe
     *            delimited.
     * @param enableTotalRecordCount Optional. Enable the total record count.
     * @param enableImages Optional, include image information in output.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getItemsByUserIdRequestCreation(UUID userId, String maxOfficialRating, Boolean hasThemeSong,
            Boolean hasThemeVideo, Boolean hasSubtitles, Boolean hasSpecialFeature, Boolean hasTrailer,
            String adjacentTo, Integer parentIndexNumber, Boolean hasParentalRating, Boolean isHd, Boolean is4K,
            List<LocationType> locationTypes, List<LocationType> excludeLocationTypes, Boolean isMissing,
            Boolean isUnaired, Double minCommunityRating, Double minCriticRating, OffsetDateTime minPremiereDate,
            OffsetDateTime minDateLastSaved, OffsetDateTime minDateLastSavedForUser, OffsetDateTime maxPremiereDate,
            Boolean hasOverview, Boolean hasImdbId, Boolean hasTmdbId, Boolean hasTvdbId, Boolean isMovie,
            Boolean isSeries, Boolean isNews, Boolean isKids, Boolean isSports, List<UUID> excludeItemIds,
            Integer startIndex, Integer limit, Boolean recursive, String searchTerm, List<SortOrder> sortOrder,
            UUID parentId, List<ItemFields> fields, List<BaseItemKind> excludeItemTypes,
            List<BaseItemKind> includeItemTypes, List<ItemFilter> filters, Boolean isFavorite, List<String> mediaTypes,
            List<ImageType> imageTypes, List<String> sortBy, Boolean isPlayed, List<String> genres,
            List<String> officialRatings, List<String> tags, List<Integer> years, Boolean enableUserData,
            Integer imageTypeLimit, List<ImageType> enableImageTypes, String person, List<UUID> personIds,
            List<String> personTypes, List<String> studios, List<String> artists, List<UUID> excludeArtistIds,
            List<UUID> artistIds, List<UUID> albumArtistIds, List<UUID> contributingArtistIds, List<String> albums,
            List<UUID> albumIds, List<UUID> ids, List<VideoType> videoTypes, String minOfficialRating, Boolean isLocked,
            Boolean isPlaceHolder, Boolean hasOfficialRating, Boolean collapseBoxSetItems, Integer minWidth,
            Integer minHeight, Integer maxWidth, Integer maxHeight, Boolean is3D, List<SeriesStatus> seriesStatus,
            String nameStartsWithOrGreater, String nameStartsWith, String nameLessThan, List<UUID> studioIds,
            List<UUID> genreIds, Boolean enableTotalRecordCount, Boolean enableImages)
            throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'userId' is set
        if (userId == null) {
            throw new WebClientResponseException(
                    "Missing the required parameter 'userId' when calling getItemsByUserId",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("userId", userId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxOfficialRating", maxOfficialRating));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "hasThemeSong", hasThemeSong));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "hasThemeVideo", hasThemeVideo));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "hasSubtitles", hasSubtitles));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "hasSpecialFeature", hasSpecialFeature));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "hasTrailer", hasTrailer));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "adjacentTo", adjacentTo));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "parentIndexNumber", parentIndexNumber));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "hasParentalRating", hasParentalRating));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isHd", isHd));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "is4K", is4K));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "locationTypes", locationTypes));
        queryParams.putAll(
                apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)),
                        "excludeLocationTypes", excludeLocationTypes));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isMissing", isMissing));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isUnaired", isUnaired));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "minCommunityRating", minCommunityRating));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "minCriticRating", minCriticRating));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "minPremiereDate", minPremiereDate));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "minDateLastSaved", minDateLastSaved));
        queryParams
                .putAll(apiClient.parameterToMultiValueMap(null, "minDateLastSavedForUser", minDateLastSavedForUser));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxPremiereDate", maxPremiereDate));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "hasOverview", hasOverview));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "hasImdbId", hasImdbId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "hasTmdbId", hasTmdbId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "hasTvdbId", hasTvdbId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isMovie", isMovie));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isSeries", isSeries));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isNews", isNews));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isKids", isKids));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isSports", isSports));
        queryParams.putAll(
                apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)),
                        "excludeItemIds", excludeItemIds));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "startIndex", startIndex));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "limit", limit));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "recursive", recursive));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "searchTerm", searchTerm));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "sortOrder", sortOrder));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "parentId", parentId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "fields", fields));
        queryParams.putAll(
                apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)),
                        "excludeItemTypes", excludeItemTypes));
        queryParams.putAll(
                apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)),
                        "includeItemTypes", includeItemTypes));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "filters", filters));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isFavorite", isFavorite));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "mediaTypes", mediaTypes));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "imageTypes", imageTypes));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "sortBy", sortBy));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isPlayed", isPlayed));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "genres", genres));
        queryParams.putAll(
                apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)),
                        "officialRatings", officialRatings));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "tags", tags));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "years", years));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableUserData", enableUserData));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "imageTypeLimit", imageTypeLimit));
        queryParams.putAll(
                apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)),
                        "enableImageTypes", enableImageTypes));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "person", person));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "personIds", personIds));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "personTypes", personTypes));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "studios", studios));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "artists", artists));
        queryParams.putAll(
                apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)),
                        "excludeArtistIds", excludeArtistIds));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "artistIds", artistIds));
        queryParams.putAll(
                apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)),
                        "albumArtistIds", albumArtistIds));
        queryParams.putAll(
                apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)),
                        "contributingArtistIds", contributingArtistIds));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "albums", albums));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "albumIds", albumIds));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "ids", ids));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "videoTypes", videoTypes));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "minOfficialRating", minOfficialRating));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isLocked", isLocked));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "isPlaceHolder", isPlaceHolder));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "hasOfficialRating", hasOfficialRating));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "collapseBoxSetItems", collapseBoxSetItems));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "minWidth", minWidth));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "minHeight", minHeight));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxWidth", maxWidth));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "maxHeight", maxHeight));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "is3D", is3D));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "seriesStatus", seriesStatus));
        queryParams
                .putAll(apiClient.parameterToMultiValueMap(null, "nameStartsWithOrGreater", nameStartsWithOrGreater));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "nameStartsWith", nameStartsWith));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "nameLessThan", nameLessThan));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "studioIds", studioIds));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "genreIds", genreIds));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableTotalRecordCount", enableTotalRecordCount));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableImages", enableImages));

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {
        };
        return apiClient.invokeAPI("/Users/{userId}/Items", HttpMethod.GET, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Gets items based on a query.
     * 
     * <p>
     * <b>200</b> - Success
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param userId The user id supplied as query parameter.
     * @param maxOfficialRating Optional filter by maximum official rating (PG, PG-13, TV-MA, etc).
     * @param hasThemeSong Optional filter by items with theme songs.
     * @param hasThemeVideo Optional filter by items with theme videos.
     * @param hasSubtitles Optional filter by items with subtitles.
     * @param hasSpecialFeature Optional filter by items with special features.
     * @param hasTrailer Optional filter by items with trailers.
     * @param adjacentTo Optional. Return items that are siblings of a supplied item.
     * @param parentIndexNumber Optional filter by parent index number.
     * @param hasParentalRating Optional filter by items that have or do not have a parental rating.
     * @param isHd Optional filter by items that are HD or not.
     * @param is4K Optional filter by items that are 4K or not.
     * @param locationTypes Optional. If specified, results will be filtered based on LocationType. This allows
     *            multiple, comma delimited.
     * @param excludeLocationTypes Optional. If specified, results will be filtered based on the LocationType. This
     *            allows multiple, comma delimited.
     * @param isMissing Optional filter by items that are missing episodes or not.
     * @param isUnaired Optional filter by items that are unaired episodes or not.
     * @param minCommunityRating Optional filter by minimum community rating.
     * @param minCriticRating Optional filter by minimum critic rating.
     * @param minPremiereDate Optional. The minimum premiere date. Format &#x3D; ISO.
     * @param minDateLastSaved Optional. The minimum last saved date. Format &#x3D; ISO.
     * @param minDateLastSavedForUser Optional. The minimum last saved date for the current user. Format &#x3D; ISO.
     * @param maxPremiereDate Optional. The maximum premiere date. Format &#x3D; ISO.
     * @param hasOverview Optional filter by items that have an overview or not.
     * @param hasImdbId Optional filter by items that have an imdb id or not.
     * @param hasTmdbId Optional filter by items that have a tmdb id or not.
     * @param hasTvdbId Optional filter by items that have a tvdb id or not.
     * @param isMovie Optional filter for live tv movies.
     * @param isSeries Optional filter for live tv series.
     * @param isNews Optional filter for live tv news.
     * @param isKids Optional filter for live tv kids.
     * @param isSports Optional filter for live tv sports.
     * @param excludeItemIds Optional. If specified, results will be filtered by excluding item ids. This allows
     *            multiple, comma delimited.
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results.
     * @param limit Optional. The maximum number of records to return.
     * @param recursive When searching within folders, this determines whether or not the search will be recursive.
     *            true/false.
     * @param searchTerm Optional. Filter based on a search term.
     * @param sortOrder Sort Order - Ascending,Descending.
     * @param parentId Specify this to localize the search to a specific item or folder. Omit to use the root.
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple,
     *            comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions,
     *            MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue,
     *            SortName, Studios, Taglines.
     * @param excludeItemTypes Optional. If specified, results will be filtered based on item type. This allows
     *            multiple, comma delimited.
     * @param includeItemTypes Optional. If specified, results will be filtered based on the item type. This allows
     *            multiple, comma delimited.
     * @param filters Optional. Specify additional filters to apply. This allows multiple, comma delimited. Options:
     *            IsFolder, IsNotFolder, IsUnplayed, IsPlayed, IsFavorite, IsResumable, Likes, Dislikes.
     * @param isFavorite Optional filter by items that are marked as favorite, or not.
     * @param mediaTypes Optional filter by MediaType. Allows multiple, comma delimited.
     * @param imageTypes Optional. If specified, results will be filtered based on those containing image types. This
     *            allows multiple, comma delimited.
     * @param sortBy Optional. Specify one or more sort orders, comma delimited. Options: Album, AlbumArtist, Artist,
     *            Budget, CommunityRating, CriticRating, DateCreated, DatePlayed, PlayCount, PremiereDate,
     *            ProductionYear, SortName, Random, Revenue, Runtime.
     * @param isPlayed Optional filter by items that are played, or not.
     * @param genres Optional. If specified, results will be filtered based on genre. This allows multiple, pipe
     *            delimited.
     * @param officialRatings Optional. If specified, results will be filtered based on OfficialRating. This allows
     *            multiple, pipe delimited.
     * @param tags Optional. If specified, results will be filtered based on tag. This allows multiple, pipe delimited.
     * @param years Optional. If specified, results will be filtered based on production year. This allows multiple,
     *            comma delimited.
     * @param enableUserData Optional, include user data.
     * @param imageTypeLimit Optional, the max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @param person Optional. If specified, results will be filtered to include only those containing the specified
     *            person.
     * @param personIds Optional. If specified, results will be filtered to include only those containing the specified
     *            person id.
     * @param personTypes Optional. If specified, along with Person, results will be filtered to include only those
     *            containing the specified person and PersonType. Allows multiple, comma-delimited.
     * @param studios Optional. If specified, results will be filtered based on studio. This allows multiple, pipe
     *            delimited.
     * @param artists Optional. If specified, results will be filtered based on artists. This allows multiple, pipe
     *            delimited.
     * @param excludeArtistIds Optional. If specified, results will be filtered based on artist id. This allows
     *            multiple, pipe delimited.
     * @param artistIds Optional. If specified, results will be filtered to include only those containing the specified
     *            artist id.
     * @param albumArtistIds Optional. If specified, results will be filtered to include only those containing the
     *            specified album artist id.
     * @param contributingArtistIds Optional. If specified, results will be filtered to include only those containing
     *            the specified contributing artist id.
     * @param albums Optional. If specified, results will be filtered based on album. This allows multiple, pipe
     *            delimited.
     * @param albumIds Optional. If specified, results will be filtered based on album id. This allows multiple, pipe
     *            delimited.
     * @param ids Optional. If specific items are needed, specify a list of item id&#39;s to retrieve. This allows
     *            multiple, comma delimited.
     * @param videoTypes Optional filter by VideoType (videofile, dvd, bluray, iso). Allows multiple, comma delimited.
     * @param minOfficialRating Optional filter by minimum official rating (PG, PG-13, TV-MA, etc).
     * @param isLocked Optional filter by items that are locked.
     * @param isPlaceHolder Optional filter by items that are placeholders.
     * @param hasOfficialRating Optional filter by items that have official ratings.
     * @param collapseBoxSetItems Whether or not to hide items behind their boxsets.
     * @param minWidth Optional. Filter by the minimum width of the item.
     * @param minHeight Optional. Filter by the minimum height of the item.
     * @param maxWidth Optional. Filter by the maximum width of the item.
     * @param maxHeight Optional. Filter by the maximum height of the item.
     * @param is3D Optional filter by items that are 3D, or not.
     * @param seriesStatus Optional filter by Series Status. Allows multiple, comma delimited.
     * @param nameStartsWithOrGreater Optional filter by items whose name is sorted equally or greater than a given
     *            input string.
     * @param nameStartsWith Optional filter by items whose name is sorted equally than a given input string.
     * @param nameLessThan Optional filter by items whose name is equally or lesser than a given input string.
     * @param studioIds Optional. If specified, results will be filtered based on studio id. This allows multiple, pipe
     *            delimited.
     * @param genreIds Optional. If specified, results will be filtered based on genre id. This allows multiple, pipe
     *            delimited.
     * @param enableTotalRecordCount Optional. Enable the total record count.
     * @param enableImages Optional, include image information in output.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<BaseItemDtoQueryResult> getItemsByUserId(UUID userId, String maxOfficialRating, Boolean hasThemeSong,
            Boolean hasThemeVideo, Boolean hasSubtitles, Boolean hasSpecialFeature, Boolean hasTrailer,
            String adjacentTo, Integer parentIndexNumber, Boolean hasParentalRating, Boolean isHd, Boolean is4K,
            List<LocationType> locationTypes, List<LocationType> excludeLocationTypes, Boolean isMissing,
            Boolean isUnaired, Double minCommunityRating, Double minCriticRating, OffsetDateTime minPremiereDate,
            OffsetDateTime minDateLastSaved, OffsetDateTime minDateLastSavedForUser, OffsetDateTime maxPremiereDate,
            Boolean hasOverview, Boolean hasImdbId, Boolean hasTmdbId, Boolean hasTvdbId, Boolean isMovie,
            Boolean isSeries, Boolean isNews, Boolean isKids, Boolean isSports, List<UUID> excludeItemIds,
            Integer startIndex, Integer limit, Boolean recursive, String searchTerm, List<SortOrder> sortOrder,
            UUID parentId, List<ItemFields> fields, List<BaseItemKind> excludeItemTypes,
            List<BaseItemKind> includeItemTypes, List<ItemFilter> filters, Boolean isFavorite, List<String> mediaTypes,
            List<ImageType> imageTypes, List<String> sortBy, Boolean isPlayed, List<String> genres,
            List<String> officialRatings, List<String> tags, List<Integer> years, Boolean enableUserData,
            Integer imageTypeLimit, List<ImageType> enableImageTypes, String person, List<UUID> personIds,
            List<String> personTypes, List<String> studios, List<String> artists, List<UUID> excludeArtistIds,
            List<UUID> artistIds, List<UUID> albumArtistIds, List<UUID> contributingArtistIds, List<String> albums,
            List<UUID> albumIds, List<UUID> ids, List<VideoType> videoTypes, String minOfficialRating, Boolean isLocked,
            Boolean isPlaceHolder, Boolean hasOfficialRating, Boolean collapseBoxSetItems, Integer minWidth,
            Integer minHeight, Integer maxWidth, Integer maxHeight, Boolean is3D, List<SeriesStatus> seriesStatus,
            String nameStartsWithOrGreater, String nameStartsWith, String nameLessThan, List<UUID> studioIds,
            List<UUID> genreIds, Boolean enableTotalRecordCount, Boolean enableImages)
            throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {
        };
        return getItemsByUserIdRequestCreation(userId, maxOfficialRating, hasThemeSong, hasThemeVideo, hasSubtitles,
                hasSpecialFeature, hasTrailer, adjacentTo, parentIndexNumber, hasParentalRating, isHd, is4K,
                locationTypes, excludeLocationTypes, isMissing, isUnaired, minCommunityRating, minCriticRating,
                minPremiereDate, minDateLastSaved, minDateLastSavedForUser, maxPremiereDate, hasOverview, hasImdbId,
                hasTmdbId, hasTvdbId, isMovie, isSeries, isNews, isKids, isSports, excludeItemIds, startIndex, limit,
                recursive, searchTerm, sortOrder, parentId, fields, excludeItemTypes, includeItemTypes, filters,
                isFavorite, mediaTypes, imageTypes, sortBy, isPlayed, genres, officialRatings, tags, years,
                enableUserData, imageTypeLimit, enableImageTypes, person, personIds, personTypes, studios, artists,
                excludeArtistIds, artistIds, albumArtistIds, contributingArtistIds, albums, albumIds, ids, videoTypes,
                minOfficialRating, isLocked, isPlaceHolder, hasOfficialRating, collapseBoxSetItems, minWidth, minHeight,
                maxWidth, maxHeight, is3D, seriesStatus, nameStartsWithOrGreater, nameStartsWith, nameLessThan,
                studioIds, genreIds, enableTotalRecordCount, enableImages).bodyToMono(localVarReturnType);
    }

    /**
     * Gets items based on a query.
     * 
     * <p>
     * <b>200</b> - Success
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param userId The user id supplied as query parameter.
     * @param maxOfficialRating Optional filter by maximum official rating (PG, PG-13, TV-MA, etc).
     * @param hasThemeSong Optional filter by items with theme songs.
     * @param hasThemeVideo Optional filter by items with theme videos.
     * @param hasSubtitles Optional filter by items with subtitles.
     * @param hasSpecialFeature Optional filter by items with special features.
     * @param hasTrailer Optional filter by items with trailers.
     * @param adjacentTo Optional. Return items that are siblings of a supplied item.
     * @param parentIndexNumber Optional filter by parent index number.
     * @param hasParentalRating Optional filter by items that have or do not have a parental rating.
     * @param isHd Optional filter by items that are HD or not.
     * @param is4K Optional filter by items that are 4K or not.
     * @param locationTypes Optional. If specified, results will be filtered based on LocationType. This allows
     *            multiple, comma delimited.
     * @param excludeLocationTypes Optional. If specified, results will be filtered based on the LocationType. This
     *            allows multiple, comma delimited.
     * @param isMissing Optional filter by items that are missing episodes or not.
     * @param isUnaired Optional filter by items that are unaired episodes or not.
     * @param minCommunityRating Optional filter by minimum community rating.
     * @param minCriticRating Optional filter by minimum critic rating.
     * @param minPremiereDate Optional. The minimum premiere date. Format &#x3D; ISO.
     * @param minDateLastSaved Optional. The minimum last saved date. Format &#x3D; ISO.
     * @param minDateLastSavedForUser Optional. The minimum last saved date for the current user. Format &#x3D; ISO.
     * @param maxPremiereDate Optional. The maximum premiere date. Format &#x3D; ISO.
     * @param hasOverview Optional filter by items that have an overview or not.
     * @param hasImdbId Optional filter by items that have an imdb id or not.
     * @param hasTmdbId Optional filter by items that have a tmdb id or not.
     * @param hasTvdbId Optional filter by items that have a tvdb id or not.
     * @param isMovie Optional filter for live tv movies.
     * @param isSeries Optional filter for live tv series.
     * @param isNews Optional filter for live tv news.
     * @param isKids Optional filter for live tv kids.
     * @param isSports Optional filter for live tv sports.
     * @param excludeItemIds Optional. If specified, results will be filtered by excluding item ids. This allows
     *            multiple, comma delimited.
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results.
     * @param limit Optional. The maximum number of records to return.
     * @param recursive When searching within folders, this determines whether or not the search will be recursive.
     *            true/false.
     * @param searchTerm Optional. Filter based on a search term.
     * @param sortOrder Sort Order - Ascending,Descending.
     * @param parentId Specify this to localize the search to a specific item or folder. Omit to use the root.
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple,
     *            comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions,
     *            MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue,
     *            SortName, Studios, Taglines.
     * @param excludeItemTypes Optional. If specified, results will be filtered based on item type. This allows
     *            multiple, comma delimited.
     * @param includeItemTypes Optional. If specified, results will be filtered based on the item type. This allows
     *            multiple, comma delimited.
     * @param filters Optional. Specify additional filters to apply. This allows multiple, comma delimited. Options:
     *            IsFolder, IsNotFolder, IsUnplayed, IsPlayed, IsFavorite, IsResumable, Likes, Dislikes.
     * @param isFavorite Optional filter by items that are marked as favorite, or not.
     * @param mediaTypes Optional filter by MediaType. Allows multiple, comma delimited.
     * @param imageTypes Optional. If specified, results will be filtered based on those containing image types. This
     *            allows multiple, comma delimited.
     * @param sortBy Optional. Specify one or more sort orders, comma delimited. Options: Album, AlbumArtist, Artist,
     *            Budget, CommunityRating, CriticRating, DateCreated, DatePlayed, PlayCount, PremiereDate,
     *            ProductionYear, SortName, Random, Revenue, Runtime.
     * @param isPlayed Optional filter by items that are played, or not.
     * @param genres Optional. If specified, results will be filtered based on genre. This allows multiple, pipe
     *            delimited.
     * @param officialRatings Optional. If specified, results will be filtered based on OfficialRating. This allows
     *            multiple, pipe delimited.
     * @param tags Optional. If specified, results will be filtered based on tag. This allows multiple, pipe delimited.
     * @param years Optional. If specified, results will be filtered based on production year. This allows multiple,
     *            comma delimited.
     * @param enableUserData Optional, include user data.
     * @param imageTypeLimit Optional, the max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @param person Optional. If specified, results will be filtered to include only those containing the specified
     *            person.
     * @param personIds Optional. If specified, results will be filtered to include only those containing the specified
     *            person id.
     * @param personTypes Optional. If specified, along with Person, results will be filtered to include only those
     *            containing the specified person and PersonType. Allows multiple, comma-delimited.
     * @param studios Optional. If specified, results will be filtered based on studio. This allows multiple, pipe
     *            delimited.
     * @param artists Optional. If specified, results will be filtered based on artists. This allows multiple, pipe
     *            delimited.
     * @param excludeArtistIds Optional. If specified, results will be filtered based on artist id. This allows
     *            multiple, pipe delimited.
     * @param artistIds Optional. If specified, results will be filtered to include only those containing the specified
     *            artist id.
     * @param albumArtistIds Optional. If specified, results will be filtered to include only those containing the
     *            specified album artist id.
     * @param contributingArtistIds Optional. If specified, results will be filtered to include only those containing
     *            the specified contributing artist id.
     * @param albums Optional. If specified, results will be filtered based on album. This allows multiple, pipe
     *            delimited.
     * @param albumIds Optional. If specified, results will be filtered based on album id. This allows multiple, pipe
     *            delimited.
     * @param ids Optional. If specific items are needed, specify a list of item id&#39;s to retrieve. This allows
     *            multiple, comma delimited.
     * @param videoTypes Optional filter by VideoType (videofile, dvd, bluray, iso). Allows multiple, comma delimited.
     * @param minOfficialRating Optional filter by minimum official rating (PG, PG-13, TV-MA, etc).
     * @param isLocked Optional filter by items that are locked.
     * @param isPlaceHolder Optional filter by items that are placeholders.
     * @param hasOfficialRating Optional filter by items that have official ratings.
     * @param collapseBoxSetItems Whether or not to hide items behind their boxsets.
     * @param minWidth Optional. Filter by the minimum width of the item.
     * @param minHeight Optional. Filter by the minimum height of the item.
     * @param maxWidth Optional. Filter by the maximum width of the item.
     * @param maxHeight Optional. Filter by the maximum height of the item.
     * @param is3D Optional filter by items that are 3D, or not.
     * @param seriesStatus Optional filter by Series Status. Allows multiple, comma delimited.
     * @param nameStartsWithOrGreater Optional filter by items whose name is sorted equally or greater than a given
     *            input string.
     * @param nameStartsWith Optional filter by items whose name is sorted equally than a given input string.
     * @param nameLessThan Optional filter by items whose name is equally or lesser than a given input string.
     * @param studioIds Optional. If specified, results will be filtered based on studio id. This allows multiple, pipe
     *            delimited.
     * @param genreIds Optional. If specified, results will be filtered based on genre id. This allows multiple, pipe
     *            delimited.
     * @param enableTotalRecordCount Optional. Enable the total record count.
     * @param enableImages Optional, include image information in output.
     * @return ResponseEntity&lt;BaseItemDtoQueryResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<BaseItemDtoQueryResult>> getItemsByUserIdWithHttpInfo(UUID userId,
            String maxOfficialRating, Boolean hasThemeSong, Boolean hasThemeVideo, Boolean hasSubtitles,
            Boolean hasSpecialFeature, Boolean hasTrailer, String adjacentTo, Integer parentIndexNumber,
            Boolean hasParentalRating, Boolean isHd, Boolean is4K, List<LocationType> locationTypes,
            List<LocationType> excludeLocationTypes, Boolean isMissing, Boolean isUnaired, Double minCommunityRating,
            Double minCriticRating, OffsetDateTime minPremiereDate, OffsetDateTime minDateLastSaved,
            OffsetDateTime minDateLastSavedForUser, OffsetDateTime maxPremiereDate, Boolean hasOverview,
            Boolean hasImdbId, Boolean hasTmdbId, Boolean hasTvdbId, Boolean isMovie, Boolean isSeries, Boolean isNews,
            Boolean isKids, Boolean isSports, List<UUID> excludeItemIds, Integer startIndex, Integer limit,
            Boolean recursive, String searchTerm, List<SortOrder> sortOrder, UUID parentId, List<ItemFields> fields,
            List<BaseItemKind> excludeItemTypes, List<BaseItemKind> includeItemTypes, List<ItemFilter> filters,
            Boolean isFavorite, List<String> mediaTypes, List<ImageType> imageTypes, List<String> sortBy,
            Boolean isPlayed, List<String> genres, List<String> officialRatings, List<String> tags, List<Integer> years,
            Boolean enableUserData, Integer imageTypeLimit, List<ImageType> enableImageTypes, String person,
            List<UUID> personIds, List<String> personTypes, List<String> studios, List<String> artists,
            List<UUID> excludeArtistIds, List<UUID> artistIds, List<UUID> albumArtistIds,
            List<UUID> contributingArtistIds, List<String> albums, List<UUID> albumIds, List<UUID> ids,
            List<VideoType> videoTypes, String minOfficialRating, Boolean isLocked, Boolean isPlaceHolder,
            Boolean hasOfficialRating, Boolean collapseBoxSetItems, Integer minWidth, Integer minHeight,
            Integer maxWidth, Integer maxHeight, Boolean is3D, List<SeriesStatus> seriesStatus,
            String nameStartsWithOrGreater, String nameStartsWith, String nameLessThan, List<UUID> studioIds,
            List<UUID> genreIds, Boolean enableTotalRecordCount, Boolean enableImages)
            throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {
        };
        return getItemsByUserIdRequestCreation(userId, maxOfficialRating, hasThemeSong, hasThemeVideo, hasSubtitles,
                hasSpecialFeature, hasTrailer, adjacentTo, parentIndexNumber, hasParentalRating, isHd, is4K,
                locationTypes, excludeLocationTypes, isMissing, isUnaired, minCommunityRating, minCriticRating,
                minPremiereDate, minDateLastSaved, minDateLastSavedForUser, maxPremiereDate, hasOverview, hasImdbId,
                hasTmdbId, hasTvdbId, isMovie, isSeries, isNews, isKids, isSports, excludeItemIds, startIndex, limit,
                recursive, searchTerm, sortOrder, parentId, fields, excludeItemTypes, includeItemTypes, filters,
                isFavorite, mediaTypes, imageTypes, sortBy, isPlayed, genres, officialRatings, tags, years,
                enableUserData, imageTypeLimit, enableImageTypes, person, personIds, personTypes, studios, artists,
                excludeArtistIds, artistIds, albumArtistIds, contributingArtistIds, albums, albumIds, ids, videoTypes,
                minOfficialRating, isLocked, isPlaceHolder, hasOfficialRating, collapseBoxSetItems, minWidth, minHeight,
                maxWidth, maxHeight, is3D, seriesStatus, nameStartsWithOrGreater, nameStartsWith, nameLessThan,
                studioIds, genreIds, enableTotalRecordCount, enableImages).toEntity(localVarReturnType);
    }

    /**
     * Gets items based on a query.
     * 
     * <p>
     * <b>200</b> - Success
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param userId The user id supplied as query parameter.
     * @param maxOfficialRating Optional filter by maximum official rating (PG, PG-13, TV-MA, etc).
     * @param hasThemeSong Optional filter by items with theme songs.
     * @param hasThemeVideo Optional filter by items with theme videos.
     * @param hasSubtitles Optional filter by items with subtitles.
     * @param hasSpecialFeature Optional filter by items with special features.
     * @param hasTrailer Optional filter by items with trailers.
     * @param adjacentTo Optional. Return items that are siblings of a supplied item.
     * @param parentIndexNumber Optional filter by parent index number.
     * @param hasParentalRating Optional filter by items that have or do not have a parental rating.
     * @param isHd Optional filter by items that are HD or not.
     * @param is4K Optional filter by items that are 4K or not.
     * @param locationTypes Optional. If specified, results will be filtered based on LocationType. This allows
     *            multiple, comma delimited.
     * @param excludeLocationTypes Optional. If specified, results will be filtered based on the LocationType. This
     *            allows multiple, comma delimited.
     * @param isMissing Optional filter by items that are missing episodes or not.
     * @param isUnaired Optional filter by items that are unaired episodes or not.
     * @param minCommunityRating Optional filter by minimum community rating.
     * @param minCriticRating Optional filter by minimum critic rating.
     * @param minPremiereDate Optional. The minimum premiere date. Format &#x3D; ISO.
     * @param minDateLastSaved Optional. The minimum last saved date. Format &#x3D; ISO.
     * @param minDateLastSavedForUser Optional. The minimum last saved date for the current user. Format &#x3D; ISO.
     * @param maxPremiereDate Optional. The maximum premiere date. Format &#x3D; ISO.
     * @param hasOverview Optional filter by items that have an overview or not.
     * @param hasImdbId Optional filter by items that have an imdb id or not.
     * @param hasTmdbId Optional filter by items that have a tmdb id or not.
     * @param hasTvdbId Optional filter by items that have a tvdb id or not.
     * @param isMovie Optional filter for live tv movies.
     * @param isSeries Optional filter for live tv series.
     * @param isNews Optional filter for live tv news.
     * @param isKids Optional filter for live tv kids.
     * @param isSports Optional filter for live tv sports.
     * @param excludeItemIds Optional. If specified, results will be filtered by excluding item ids. This allows
     *            multiple, comma delimited.
     * @param startIndex Optional. The record index to start at. All items with a lower index will be dropped from the
     *            results.
     * @param limit Optional. The maximum number of records to return.
     * @param recursive When searching within folders, this determines whether or not the search will be recursive.
     *            true/false.
     * @param searchTerm Optional. Filter based on a search term.
     * @param sortOrder Sort Order - Ascending,Descending.
     * @param parentId Specify this to localize the search to a specific item or folder. Omit to use the root.
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple,
     *            comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions,
     *            MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue,
     *            SortName, Studios, Taglines.
     * @param excludeItemTypes Optional. If specified, results will be filtered based on item type. This allows
     *            multiple, comma delimited.
     * @param includeItemTypes Optional. If specified, results will be filtered based on the item type. This allows
     *            multiple, comma delimited.
     * @param filters Optional. Specify additional filters to apply. This allows multiple, comma delimited. Options:
     *            IsFolder, IsNotFolder, IsUnplayed, IsPlayed, IsFavorite, IsResumable, Likes, Dislikes.
     * @param isFavorite Optional filter by items that are marked as favorite, or not.
     * @param mediaTypes Optional filter by MediaType. Allows multiple, comma delimited.
     * @param imageTypes Optional. If specified, results will be filtered based on those containing image types. This
     *            allows multiple, comma delimited.
     * @param sortBy Optional. Specify one or more sort orders, comma delimited. Options: Album, AlbumArtist, Artist,
     *            Budget, CommunityRating, CriticRating, DateCreated, DatePlayed, PlayCount, PremiereDate,
     *            ProductionYear, SortName, Random, Revenue, Runtime.
     * @param isPlayed Optional filter by items that are played, or not.
     * @param genres Optional. If specified, results will be filtered based on genre. This allows multiple, pipe
     *            delimited.
     * @param officialRatings Optional. If specified, results will be filtered based on OfficialRating. This allows
     *            multiple, pipe delimited.
     * @param tags Optional. If specified, results will be filtered based on tag. This allows multiple, pipe delimited.
     * @param years Optional. If specified, results will be filtered based on production year. This allows multiple,
     *            comma delimited.
     * @param enableUserData Optional, include user data.
     * @param imageTypeLimit Optional, the max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @param person Optional. If specified, results will be filtered to include only those containing the specified
     *            person.
     * @param personIds Optional. If specified, results will be filtered to include only those containing the specified
     *            person id.
     * @param personTypes Optional. If specified, along with Person, results will be filtered to include only those
     *            containing the specified person and PersonType. Allows multiple, comma-delimited.
     * @param studios Optional. If specified, results will be filtered based on studio. This allows multiple, pipe
     *            delimited.
     * @param artists Optional. If specified, results will be filtered based on artists. This allows multiple, pipe
     *            delimited.
     * @param excludeArtistIds Optional. If specified, results will be filtered based on artist id. This allows
     *            multiple, pipe delimited.
     * @param artistIds Optional. If specified, results will be filtered to include only those containing the specified
     *            artist id.
     * @param albumArtistIds Optional. If specified, results will be filtered to include only those containing the
     *            specified album artist id.
     * @param contributingArtistIds Optional. If specified, results will be filtered to include only those containing
     *            the specified contributing artist id.
     * @param albums Optional. If specified, results will be filtered based on album. This allows multiple, pipe
     *            delimited.
     * @param albumIds Optional. If specified, results will be filtered based on album id. This allows multiple, pipe
     *            delimited.
     * @param ids Optional. If specific items are needed, specify a list of item id&#39;s to retrieve. This allows
     *            multiple, comma delimited.
     * @param videoTypes Optional filter by VideoType (videofile, dvd, bluray, iso). Allows multiple, comma delimited.
     * @param minOfficialRating Optional filter by minimum official rating (PG, PG-13, TV-MA, etc).
     * @param isLocked Optional filter by items that are locked.
     * @param isPlaceHolder Optional filter by items that are placeholders.
     * @param hasOfficialRating Optional filter by items that have official ratings.
     * @param collapseBoxSetItems Whether or not to hide items behind their boxsets.
     * @param minWidth Optional. Filter by the minimum width of the item.
     * @param minHeight Optional. Filter by the minimum height of the item.
     * @param maxWidth Optional. Filter by the maximum width of the item.
     * @param maxHeight Optional. Filter by the maximum height of the item.
     * @param is3D Optional filter by items that are 3D, or not.
     * @param seriesStatus Optional filter by Series Status. Allows multiple, comma delimited.
     * @param nameStartsWithOrGreater Optional filter by items whose name is sorted equally or greater than a given
     *            input string.
     * @param nameStartsWith Optional filter by items whose name is sorted equally than a given input string.
     * @param nameLessThan Optional filter by items whose name is equally or lesser than a given input string.
     * @param studioIds Optional. If specified, results will be filtered based on studio id. This allows multiple, pipe
     *            delimited.
     * @param genreIds Optional. If specified, results will be filtered based on genre id. This allows multiple, pipe
     *            delimited.
     * @param enableTotalRecordCount Optional. Enable the total record count.
     * @param enableImages Optional, include image information in output.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getItemsByUserIdWithResponseSpec(UUID userId, String maxOfficialRating, Boolean hasThemeSong,
            Boolean hasThemeVideo, Boolean hasSubtitles, Boolean hasSpecialFeature, Boolean hasTrailer,
            String adjacentTo, Integer parentIndexNumber, Boolean hasParentalRating, Boolean isHd, Boolean is4K,
            List<LocationType> locationTypes, List<LocationType> excludeLocationTypes, Boolean isMissing,
            Boolean isUnaired, Double minCommunityRating, Double minCriticRating, OffsetDateTime minPremiereDate,
            OffsetDateTime minDateLastSaved, OffsetDateTime minDateLastSavedForUser, OffsetDateTime maxPremiereDate,
            Boolean hasOverview, Boolean hasImdbId, Boolean hasTmdbId, Boolean hasTvdbId, Boolean isMovie,
            Boolean isSeries, Boolean isNews, Boolean isKids, Boolean isSports, List<UUID> excludeItemIds,
            Integer startIndex, Integer limit, Boolean recursive, String searchTerm, List<SortOrder> sortOrder,
            UUID parentId, List<ItemFields> fields, List<BaseItemKind> excludeItemTypes,
            List<BaseItemKind> includeItemTypes, List<ItemFilter> filters, Boolean isFavorite, List<String> mediaTypes,
            List<ImageType> imageTypes, List<String> sortBy, Boolean isPlayed, List<String> genres,
            List<String> officialRatings, List<String> tags, List<Integer> years, Boolean enableUserData,
            Integer imageTypeLimit, List<ImageType> enableImageTypes, String person, List<UUID> personIds,
            List<String> personTypes, List<String> studios, List<String> artists, List<UUID> excludeArtistIds,
            List<UUID> artistIds, List<UUID> albumArtistIds, List<UUID> contributingArtistIds, List<String> albums,
            List<UUID> albumIds, List<UUID> ids, List<VideoType> videoTypes, String minOfficialRating, Boolean isLocked,
            Boolean isPlaceHolder, Boolean hasOfficialRating, Boolean collapseBoxSetItems, Integer minWidth,
            Integer minHeight, Integer maxWidth, Integer maxHeight, Boolean is3D, List<SeriesStatus> seriesStatus,
            String nameStartsWithOrGreater, String nameStartsWith, String nameLessThan, List<UUID> studioIds,
            List<UUID> genreIds, Boolean enableTotalRecordCount, Boolean enableImages)
            throws WebClientResponseException {
        return getItemsByUserIdRequestCreation(userId, maxOfficialRating, hasThemeSong, hasThemeVideo, hasSubtitles,
                hasSpecialFeature, hasTrailer, adjacentTo, parentIndexNumber, hasParentalRating, isHd, is4K,
                locationTypes, excludeLocationTypes, isMissing, isUnaired, minCommunityRating, minCriticRating,
                minPremiereDate, minDateLastSaved, minDateLastSavedForUser, maxPremiereDate, hasOverview, hasImdbId,
                hasTmdbId, hasTvdbId, isMovie, isSeries, isNews, isKids, isSports, excludeItemIds, startIndex, limit,
                recursive, searchTerm, sortOrder, parentId, fields, excludeItemTypes, includeItemTypes, filters,
                isFavorite, mediaTypes, imageTypes, sortBy, isPlayed, genres, officialRatings, tags, years,
                enableUserData, imageTypeLimit, enableImageTypes, person, personIds, personTypes, studios, artists,
                excludeArtistIds, artistIds, albumArtistIds, contributingArtistIds, albums, albumIds, ids, videoTypes,
                minOfficialRating, isLocked, isPlaceHolder, hasOfficialRating, collapseBoxSetItems, minWidth, minHeight,
                maxWidth, maxHeight, is3D, seriesStatus, nameStartsWithOrGreater, nameStartsWith, nameLessThan,
                studioIds, genreIds, enableTotalRecordCount, enableImages);
    }

    /**
     * Gets items based on a query.
     * 
     * <p>
     * <b>200</b> - Items returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param userId The user id.
     * @param startIndex The start index.
     * @param limit The item limit.
     * @param searchTerm The search term.
     * @param parentId Specify this to localize the search to a specific item or folder. Omit to use the root.
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple,
     *            comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions,
     *            MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue,
     *            SortName, Studios, Taglines.
     * @param mediaTypes Optional. Filter by MediaType. Allows multiple, comma delimited.
     * @param enableUserData Optional. Include user data.
     * @param imageTypeLimit Optional. The max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @param excludeItemTypes Optional. If specified, results will be filtered based on item type. This allows
     *            multiple, comma delimited.
     * @param includeItemTypes Optional. If specified, results will be filtered based on the item type. This allows
     *            multiple, comma delimited.
     * @param enableTotalRecordCount Optional. Enable the total record count.
     * @param enableImages Optional. Include image information in output.
     * @param excludeActiveSessions Optional. Whether to exclude the currently active sessions.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getResumeItemsRequestCreation(UUID userId, Integer startIndex, Integer limit,
            String searchTerm, UUID parentId, List<ItemFields> fields, List<String> mediaTypes, Boolean enableUserData,
            Integer imageTypeLimit, List<ImageType> enableImageTypes, List<BaseItemKind> excludeItemTypes,
            List<BaseItemKind> includeItemTypes, Boolean enableTotalRecordCount, Boolean enableImages,
            Boolean excludeActiveSessions) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'userId' is set
        if (userId == null) {
            throw new WebClientResponseException("Missing the required parameter 'userId' when calling getResumeItems",
                    HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("userId", userId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "startIndex", startIndex));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "limit", limit));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "searchTerm", searchTerm));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "parentId", parentId));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "fields", fields));
        queryParams.putAll(apiClient.parameterToMultiValueMap(
                ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)), "mediaTypes", mediaTypes));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableUserData", enableUserData));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "imageTypeLimit", imageTypeLimit));
        queryParams.putAll(
                apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)),
                        "enableImageTypes", enableImageTypes));
        queryParams.putAll(
                apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)),
                        "excludeItemTypes", excludeItemTypes));
        queryParams.putAll(
                apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase(Locale.ROOT)),
                        "includeItemTypes", includeItemTypes));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableTotalRecordCount", enableTotalRecordCount));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "enableImages", enableImages));
        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "excludeActiveSessions", excludeActiveSessions));

        final String[] localVarAccepts = { "application/json", "application/json; profile=CamelCase",
                "application/json; profile=PascalCase" };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "CustomAuthentication" };

        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {
        };
        return apiClient.invokeAPI("/Users/{userId}/Items/Resume", HttpMethod.GET, pathParams, queryParams, postBody,
                headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames,
                localVarReturnType);
    }

    /**
     * Gets items based on a query.
     * 
     * <p>
     * <b>200</b> - Items returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param userId The user id.
     * @param startIndex The start index.
     * @param limit The item limit.
     * @param searchTerm The search term.
     * @param parentId Specify this to localize the search to a specific item or folder. Omit to use the root.
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple,
     *            comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions,
     *            MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue,
     *            SortName, Studios, Taglines.
     * @param mediaTypes Optional. Filter by MediaType. Allows multiple, comma delimited.
     * @param enableUserData Optional. Include user data.
     * @param imageTypeLimit Optional. The max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @param excludeItemTypes Optional. If specified, results will be filtered based on item type. This allows
     *            multiple, comma delimited.
     * @param includeItemTypes Optional. If specified, results will be filtered based on the item type. This allows
     *            multiple, comma delimited.
     * @param enableTotalRecordCount Optional. Enable the total record count.
     * @param enableImages Optional. Include image information in output.
     * @param excludeActiveSessions Optional. Whether to exclude the currently active sessions.
     * @return BaseItemDtoQueryResult
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<BaseItemDtoQueryResult> getResumeItems(UUID userId, Integer startIndex, Integer limit,
            String searchTerm, UUID parentId, List<ItemFields> fields, List<String> mediaTypes, Boolean enableUserData,
            Integer imageTypeLimit, List<ImageType> enableImageTypes, List<BaseItemKind> excludeItemTypes,
            List<BaseItemKind> includeItemTypes, Boolean enableTotalRecordCount, Boolean enableImages,
            Boolean excludeActiveSessions) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {
        };
        return getResumeItemsRequestCreation(userId, startIndex, limit, searchTerm, parentId, fields, mediaTypes,
                enableUserData, imageTypeLimit, enableImageTypes, excludeItemTypes, includeItemTypes,
                enableTotalRecordCount, enableImages, excludeActiveSessions).bodyToMono(localVarReturnType);
    }

    /**
     * Gets items based on a query.
     * 
     * <p>
     * <b>200</b> - Items returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param userId The user id.
     * @param startIndex The start index.
     * @param limit The item limit.
     * @param searchTerm The search term.
     * @param parentId Specify this to localize the search to a specific item or folder. Omit to use the root.
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple,
     *            comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions,
     *            MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue,
     *            SortName, Studios, Taglines.
     * @param mediaTypes Optional. Filter by MediaType. Allows multiple, comma delimited.
     * @param enableUserData Optional. Include user data.
     * @param imageTypeLimit Optional. The max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @param excludeItemTypes Optional. If specified, results will be filtered based on item type. This allows
     *            multiple, comma delimited.
     * @param includeItemTypes Optional. If specified, results will be filtered based on the item type. This allows
     *            multiple, comma delimited.
     * @param enableTotalRecordCount Optional. Enable the total record count.
     * @param enableImages Optional. Include image information in output.
     * @param excludeActiveSessions Optional. Whether to exclude the currently active sessions.
     * @return ResponseEntity&lt;BaseItemDtoQueryResult&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<BaseItemDtoQueryResult>> getResumeItemsWithHttpInfo(UUID userId, Integer startIndex,
            Integer limit, String searchTerm, UUID parentId, List<ItemFields> fields, List<String> mediaTypes,
            Boolean enableUserData, Integer imageTypeLimit, List<ImageType> enableImageTypes,
            List<BaseItemKind> excludeItemTypes, List<BaseItemKind> includeItemTypes, Boolean enableTotalRecordCount,
            Boolean enableImages, Boolean excludeActiveSessions) throws WebClientResponseException {
        ParameterizedTypeReference<BaseItemDtoQueryResult> localVarReturnType = new ParameterizedTypeReference<BaseItemDtoQueryResult>() {
        };
        return getResumeItemsRequestCreation(userId, startIndex, limit, searchTerm, parentId, fields, mediaTypes,
                enableUserData, imageTypeLimit, enableImageTypes, excludeItemTypes, includeItemTypes,
                enableTotalRecordCount, enableImages, excludeActiveSessions).toEntity(localVarReturnType);
    }

    /**
     * Gets items based on a query.
     * 
     * <p>
     * <b>200</b> - Items returned.
     * <p>
     * <b>401</b> - Unauthorized
     * <p>
     * <b>403</b> - Forbidden
     * 
     * @param userId The user id.
     * @param startIndex The start index.
     * @param limit The item limit.
     * @param searchTerm The search term.
     * @param parentId Specify this to localize the search to a specific item or folder. Omit to use the root.
     * @param fields Optional. Specify additional fields of information to return in the output. This allows multiple,
     *            comma delimited. Options: Budget, Chapters, DateCreated, Genres, HomePageUrl, IndexOptions,
     *            MediaStreams, Overview, ParentId, Path, People, ProviderIds, PrimaryImageAspectRatio, Revenue,
     *            SortName, Studios, Taglines.
     * @param mediaTypes Optional. Filter by MediaType. Allows multiple, comma delimited.
     * @param enableUserData Optional. Include user data.
     * @param imageTypeLimit Optional. The max number of images to return, per image type.
     * @param enableImageTypes Optional. The image types to include in the output.
     * @param excludeItemTypes Optional. If specified, results will be filtered based on item type. This allows
     *            multiple, comma delimited.
     * @param includeItemTypes Optional. If specified, results will be filtered based on the item type. This allows
     *            multiple, comma delimited.
     * @param enableTotalRecordCount Optional. Enable the total record count.
     * @param enableImages Optional. Include image information in output.
     * @param excludeActiveSessions Optional. Whether to exclude the currently active sessions.
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getResumeItemsWithResponseSpec(UUID userId, Integer startIndex, Integer limit,
            String searchTerm, UUID parentId, List<ItemFields> fields, List<String> mediaTypes, Boolean enableUserData,
            Integer imageTypeLimit, List<ImageType> enableImageTypes, List<BaseItemKind> excludeItemTypes,
            List<BaseItemKind> includeItemTypes, Boolean enableTotalRecordCount, Boolean enableImages,
            Boolean excludeActiveSessions) throws WebClientResponseException {
        return getResumeItemsRequestCreation(userId, startIndex, limit, searchTerm, parentId, fields, mediaTypes,
                enableUserData, imageTypeLimit, enableImageTypes, excludeItemTypes, includeItemTypes,
                enableTotalRecordCount, enableImages, excludeActiveSessions);
    }
}
