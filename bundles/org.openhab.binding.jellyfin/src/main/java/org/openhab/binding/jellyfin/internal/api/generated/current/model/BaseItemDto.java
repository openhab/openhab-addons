/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.openhab.binding.jellyfin.internal.api.generated.current.model;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.UUID;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * This is strictly used as a data transfer object from the api layer. This holds information about a BaseItem in a
 * format that is convenient for the client.
 */
@JsonPropertyOrder({ BaseItemDto.JSON_PROPERTY_NAME, BaseItemDto.JSON_PROPERTY_ORIGINAL_TITLE,
        BaseItemDto.JSON_PROPERTY_SERVER_ID, BaseItemDto.JSON_PROPERTY_ID, BaseItemDto.JSON_PROPERTY_ETAG,
        BaseItemDto.JSON_PROPERTY_SOURCE_TYPE, BaseItemDto.JSON_PROPERTY_PLAYLIST_ITEM_ID,
        BaseItemDto.JSON_PROPERTY_DATE_CREATED, BaseItemDto.JSON_PROPERTY_DATE_LAST_MEDIA_ADDED,
        BaseItemDto.JSON_PROPERTY_EXTRA_TYPE, BaseItemDto.JSON_PROPERTY_AIRS_BEFORE_SEASON_NUMBER,
        BaseItemDto.JSON_PROPERTY_AIRS_AFTER_SEASON_NUMBER, BaseItemDto.JSON_PROPERTY_AIRS_BEFORE_EPISODE_NUMBER,
        BaseItemDto.JSON_PROPERTY_CAN_DELETE, BaseItemDto.JSON_PROPERTY_CAN_DOWNLOAD,
        BaseItemDto.JSON_PROPERTY_HAS_LYRICS, BaseItemDto.JSON_PROPERTY_HAS_SUBTITLES,
        BaseItemDto.JSON_PROPERTY_PREFERRED_METADATA_LANGUAGE,
        BaseItemDto.JSON_PROPERTY_PREFERRED_METADATA_COUNTRY_CODE, BaseItemDto.JSON_PROPERTY_CONTAINER,
        BaseItemDto.JSON_PROPERTY_SORT_NAME, BaseItemDto.JSON_PROPERTY_FORCED_SORT_NAME,
        BaseItemDto.JSON_PROPERTY_VIDEO3_D_FORMAT, BaseItemDto.JSON_PROPERTY_PREMIERE_DATE,
        BaseItemDto.JSON_PROPERTY_EXTERNAL_URLS, BaseItemDto.JSON_PROPERTY_MEDIA_SOURCES,
        BaseItemDto.JSON_PROPERTY_CRITIC_RATING, BaseItemDto.JSON_PROPERTY_PRODUCTION_LOCATIONS,
        BaseItemDto.JSON_PROPERTY_PATH, BaseItemDto.JSON_PROPERTY_ENABLE_MEDIA_SOURCE_DISPLAY,
        BaseItemDto.JSON_PROPERTY_OFFICIAL_RATING, BaseItemDto.JSON_PROPERTY_CUSTOM_RATING,
        BaseItemDto.JSON_PROPERTY_CHANNEL_ID, BaseItemDto.JSON_PROPERTY_CHANNEL_NAME,
        BaseItemDto.JSON_PROPERTY_OVERVIEW, BaseItemDto.JSON_PROPERTY_TAGLINES, BaseItemDto.JSON_PROPERTY_GENRES,
        BaseItemDto.JSON_PROPERTY_COMMUNITY_RATING, BaseItemDto.JSON_PROPERTY_CUMULATIVE_RUN_TIME_TICKS,
        BaseItemDto.JSON_PROPERTY_RUN_TIME_TICKS, BaseItemDto.JSON_PROPERTY_PLAY_ACCESS,
        BaseItemDto.JSON_PROPERTY_ASPECT_RATIO, BaseItemDto.JSON_PROPERTY_PRODUCTION_YEAR,
        BaseItemDto.JSON_PROPERTY_IS_PLACE_HOLDER, BaseItemDto.JSON_PROPERTY_NUMBER,
        BaseItemDto.JSON_PROPERTY_CHANNEL_NUMBER, BaseItemDto.JSON_PROPERTY_INDEX_NUMBER,
        BaseItemDto.JSON_PROPERTY_INDEX_NUMBER_END, BaseItemDto.JSON_PROPERTY_PARENT_INDEX_NUMBER,
        BaseItemDto.JSON_PROPERTY_REMOTE_TRAILERS, BaseItemDto.JSON_PROPERTY_PROVIDER_IDS,
        BaseItemDto.JSON_PROPERTY_IS_H_D, BaseItemDto.JSON_PROPERTY_IS_FOLDER, BaseItemDto.JSON_PROPERTY_PARENT_ID,
        BaseItemDto.JSON_PROPERTY_TYPE, BaseItemDto.JSON_PROPERTY_PEOPLE, BaseItemDto.JSON_PROPERTY_STUDIOS,
        BaseItemDto.JSON_PROPERTY_GENRE_ITEMS, BaseItemDto.JSON_PROPERTY_PARENT_LOGO_ITEM_ID,
        BaseItemDto.JSON_PROPERTY_PARENT_BACKDROP_ITEM_ID, BaseItemDto.JSON_PROPERTY_PARENT_BACKDROP_IMAGE_TAGS,
        BaseItemDto.JSON_PROPERTY_LOCAL_TRAILER_COUNT, BaseItemDto.JSON_PROPERTY_USER_DATA,
        BaseItemDto.JSON_PROPERTY_RECURSIVE_ITEM_COUNT, BaseItemDto.JSON_PROPERTY_CHILD_COUNT,
        BaseItemDto.JSON_PROPERTY_SERIES_NAME, BaseItemDto.JSON_PROPERTY_SERIES_ID, BaseItemDto.JSON_PROPERTY_SEASON_ID,
        BaseItemDto.JSON_PROPERTY_SPECIAL_FEATURE_COUNT, BaseItemDto.JSON_PROPERTY_DISPLAY_PREFERENCES_ID,
        BaseItemDto.JSON_PROPERTY_STATUS, BaseItemDto.JSON_PROPERTY_AIR_TIME, BaseItemDto.JSON_PROPERTY_AIR_DAYS,
        BaseItemDto.JSON_PROPERTY_TAGS, BaseItemDto.JSON_PROPERTY_PRIMARY_IMAGE_ASPECT_RATIO,
        BaseItemDto.JSON_PROPERTY_ARTISTS, BaseItemDto.JSON_PROPERTY_ARTIST_ITEMS, BaseItemDto.JSON_PROPERTY_ALBUM,
        BaseItemDto.JSON_PROPERTY_COLLECTION_TYPE, BaseItemDto.JSON_PROPERTY_DISPLAY_ORDER,
        BaseItemDto.JSON_PROPERTY_ALBUM_ID, BaseItemDto.JSON_PROPERTY_ALBUM_PRIMARY_IMAGE_TAG,
        BaseItemDto.JSON_PROPERTY_SERIES_PRIMARY_IMAGE_TAG, BaseItemDto.JSON_PROPERTY_ALBUM_ARTIST,
        BaseItemDto.JSON_PROPERTY_ALBUM_ARTISTS, BaseItemDto.JSON_PROPERTY_SEASON_NAME,
        BaseItemDto.JSON_PROPERTY_MEDIA_STREAMS, BaseItemDto.JSON_PROPERTY_VIDEO_TYPE,
        BaseItemDto.JSON_PROPERTY_PART_COUNT, BaseItemDto.JSON_PROPERTY_MEDIA_SOURCE_COUNT,
        BaseItemDto.JSON_PROPERTY_IMAGE_TAGS, BaseItemDto.JSON_PROPERTY_BACKDROP_IMAGE_TAGS,
        BaseItemDto.JSON_PROPERTY_SCREENSHOT_IMAGE_TAGS, BaseItemDto.JSON_PROPERTY_PARENT_LOGO_IMAGE_TAG,
        BaseItemDto.JSON_PROPERTY_PARENT_ART_ITEM_ID, BaseItemDto.JSON_PROPERTY_PARENT_ART_IMAGE_TAG,
        BaseItemDto.JSON_PROPERTY_SERIES_THUMB_IMAGE_TAG, BaseItemDto.JSON_PROPERTY_IMAGE_BLUR_HASHES,
        BaseItemDto.JSON_PROPERTY_SERIES_STUDIO, BaseItemDto.JSON_PROPERTY_PARENT_THUMB_ITEM_ID,
        BaseItemDto.JSON_PROPERTY_PARENT_THUMB_IMAGE_TAG, BaseItemDto.JSON_PROPERTY_PARENT_PRIMARY_IMAGE_ITEM_ID,
        BaseItemDto.JSON_PROPERTY_PARENT_PRIMARY_IMAGE_TAG, BaseItemDto.JSON_PROPERTY_CHAPTERS,
        BaseItemDto.JSON_PROPERTY_TRICKPLAY, BaseItemDto.JSON_PROPERTY_LOCATION_TYPE,
        BaseItemDto.JSON_PROPERTY_ISO_TYPE, BaseItemDto.JSON_PROPERTY_MEDIA_TYPE, BaseItemDto.JSON_PROPERTY_END_DATE,
        BaseItemDto.JSON_PROPERTY_LOCKED_FIELDS, BaseItemDto.JSON_PROPERTY_TRAILER_COUNT,
        BaseItemDto.JSON_PROPERTY_MOVIE_COUNT, BaseItemDto.JSON_PROPERTY_SERIES_COUNT,
        BaseItemDto.JSON_PROPERTY_PROGRAM_COUNT, BaseItemDto.JSON_PROPERTY_EPISODE_COUNT,
        BaseItemDto.JSON_PROPERTY_SONG_COUNT, BaseItemDto.JSON_PROPERTY_ALBUM_COUNT,
        BaseItemDto.JSON_PROPERTY_ARTIST_COUNT, BaseItemDto.JSON_PROPERTY_MUSIC_VIDEO_COUNT,
        BaseItemDto.JSON_PROPERTY_LOCK_DATA, BaseItemDto.JSON_PROPERTY_WIDTH, BaseItemDto.JSON_PROPERTY_HEIGHT,
        BaseItemDto.JSON_PROPERTY_CAMERA_MAKE, BaseItemDto.JSON_PROPERTY_CAMERA_MODEL,
        BaseItemDto.JSON_PROPERTY_SOFTWARE, BaseItemDto.JSON_PROPERTY_EXPOSURE_TIME,
        BaseItemDto.JSON_PROPERTY_FOCAL_LENGTH, BaseItemDto.JSON_PROPERTY_IMAGE_ORIENTATION,
        BaseItemDto.JSON_PROPERTY_APERTURE, BaseItemDto.JSON_PROPERTY_SHUTTER_SPEED, BaseItemDto.JSON_PROPERTY_LATITUDE,
        BaseItemDto.JSON_PROPERTY_LONGITUDE, BaseItemDto.JSON_PROPERTY_ALTITUDE,
        BaseItemDto.JSON_PROPERTY_ISO_SPEED_RATING, BaseItemDto.JSON_PROPERTY_SERIES_TIMER_ID,
        BaseItemDto.JSON_PROPERTY_PROGRAM_ID, BaseItemDto.JSON_PROPERTY_CHANNEL_PRIMARY_IMAGE_TAG,
        BaseItemDto.JSON_PROPERTY_START_DATE, BaseItemDto.JSON_PROPERTY_COMPLETION_PERCENTAGE,
        BaseItemDto.JSON_PROPERTY_IS_REPEAT, BaseItemDto.JSON_PROPERTY_EPISODE_TITLE,
        BaseItemDto.JSON_PROPERTY_CHANNEL_TYPE, BaseItemDto.JSON_PROPERTY_AUDIO, BaseItemDto.JSON_PROPERTY_IS_MOVIE,
        BaseItemDto.JSON_PROPERTY_IS_SPORTS, BaseItemDto.JSON_PROPERTY_IS_SERIES, BaseItemDto.JSON_PROPERTY_IS_LIVE,
        BaseItemDto.JSON_PROPERTY_IS_NEWS, BaseItemDto.JSON_PROPERTY_IS_KIDS, BaseItemDto.JSON_PROPERTY_IS_PREMIERE,
        BaseItemDto.JSON_PROPERTY_TIMER_ID, BaseItemDto.JSON_PROPERTY_NORMALIZATION_GAIN,
        BaseItemDto.JSON_PROPERTY_CURRENT_PROGRAM })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class BaseItemDto {
    public static final String JSON_PROPERTY_NAME = "Name";
    @org.eclipse.jdt.annotation.NonNull
    private String name;

    public static final String JSON_PROPERTY_ORIGINAL_TITLE = "OriginalTitle";
    @org.eclipse.jdt.annotation.NonNull
    private String originalTitle;

    public static final String JSON_PROPERTY_SERVER_ID = "ServerId";
    @org.eclipse.jdt.annotation.NonNull
    private String serverId;

    public static final String JSON_PROPERTY_ID = "Id";
    @org.eclipse.jdt.annotation.NonNull
    private UUID id;

    public static final String JSON_PROPERTY_ETAG = "Etag";
    @org.eclipse.jdt.annotation.NonNull
    private String etag;

    public static final String JSON_PROPERTY_SOURCE_TYPE = "SourceType";
    @org.eclipse.jdt.annotation.NonNull
    private String sourceType;

    public static final String JSON_PROPERTY_PLAYLIST_ITEM_ID = "PlaylistItemId";
    @org.eclipse.jdt.annotation.NonNull
    private String playlistItemId;

    public static final String JSON_PROPERTY_DATE_CREATED = "DateCreated";
    @org.eclipse.jdt.annotation.NonNull
    private OffsetDateTime dateCreated;

    public static final String JSON_PROPERTY_DATE_LAST_MEDIA_ADDED = "DateLastMediaAdded";
    @org.eclipse.jdt.annotation.NonNull
    private OffsetDateTime dateLastMediaAdded;

    public static final String JSON_PROPERTY_EXTRA_TYPE = "ExtraType";
    @org.eclipse.jdt.annotation.NonNull
    private ExtraType extraType;

    public static final String JSON_PROPERTY_AIRS_BEFORE_SEASON_NUMBER = "AirsBeforeSeasonNumber";
    @org.eclipse.jdt.annotation.NonNull
    private Integer airsBeforeSeasonNumber;

    public static final String JSON_PROPERTY_AIRS_AFTER_SEASON_NUMBER = "AirsAfterSeasonNumber";
    @org.eclipse.jdt.annotation.NonNull
    private Integer airsAfterSeasonNumber;

    public static final String JSON_PROPERTY_AIRS_BEFORE_EPISODE_NUMBER = "AirsBeforeEpisodeNumber";
    @org.eclipse.jdt.annotation.NonNull
    private Integer airsBeforeEpisodeNumber;

    public static final String JSON_PROPERTY_CAN_DELETE = "CanDelete";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean canDelete;

    public static final String JSON_PROPERTY_CAN_DOWNLOAD = "CanDownload";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean canDownload;

    public static final String JSON_PROPERTY_HAS_LYRICS = "HasLyrics";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean hasLyrics;

    public static final String JSON_PROPERTY_HAS_SUBTITLES = "HasSubtitles";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean hasSubtitles;

    public static final String JSON_PROPERTY_PREFERRED_METADATA_LANGUAGE = "PreferredMetadataLanguage";
    @org.eclipse.jdt.annotation.NonNull
    private String preferredMetadataLanguage;

    public static final String JSON_PROPERTY_PREFERRED_METADATA_COUNTRY_CODE = "PreferredMetadataCountryCode";
    @org.eclipse.jdt.annotation.NonNull
    private String preferredMetadataCountryCode;

    public static final String JSON_PROPERTY_CONTAINER = "Container";
    @org.eclipse.jdt.annotation.NonNull
    private String container;

    public static final String JSON_PROPERTY_SORT_NAME = "SortName";
    @org.eclipse.jdt.annotation.NonNull
    private String sortName;

    public static final String JSON_PROPERTY_FORCED_SORT_NAME = "ForcedSortName";
    @org.eclipse.jdt.annotation.NonNull
    private String forcedSortName;

    public static final String JSON_PROPERTY_VIDEO3_D_FORMAT = "Video3DFormat";
    @org.eclipse.jdt.annotation.NonNull
    private Video3DFormat video3DFormat;

    public static final String JSON_PROPERTY_PREMIERE_DATE = "PremiereDate";
    @org.eclipse.jdt.annotation.NonNull
    private OffsetDateTime premiereDate;

    public static final String JSON_PROPERTY_EXTERNAL_URLS = "ExternalUrls";
    @org.eclipse.jdt.annotation.NonNull
    private List<ExternalUrl> externalUrls;

    public static final String JSON_PROPERTY_MEDIA_SOURCES = "MediaSources";
    @org.eclipse.jdt.annotation.NonNull
    private List<MediaSourceInfo> mediaSources;

    public static final String JSON_PROPERTY_CRITIC_RATING = "CriticRating";
    @org.eclipse.jdt.annotation.NonNull
    private Float criticRating;

    public static final String JSON_PROPERTY_PRODUCTION_LOCATIONS = "ProductionLocations";
    @org.eclipse.jdt.annotation.NonNull
    private List<String> productionLocations;

    public static final String JSON_PROPERTY_PATH = "Path";
    @org.eclipse.jdt.annotation.NonNull
    private String path;

    public static final String JSON_PROPERTY_ENABLE_MEDIA_SOURCE_DISPLAY = "EnableMediaSourceDisplay";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean enableMediaSourceDisplay;

    public static final String JSON_PROPERTY_OFFICIAL_RATING = "OfficialRating";
    @org.eclipse.jdt.annotation.NonNull
    private String officialRating;

    public static final String JSON_PROPERTY_CUSTOM_RATING = "CustomRating";
    @org.eclipse.jdt.annotation.NonNull
    private String customRating;

    public static final String JSON_PROPERTY_CHANNEL_ID = "ChannelId";
    @org.eclipse.jdt.annotation.NonNull
    private UUID channelId;

    public static final String JSON_PROPERTY_CHANNEL_NAME = "ChannelName";
    @org.eclipse.jdt.annotation.NonNull
    private String channelName;

    public static final String JSON_PROPERTY_OVERVIEW = "Overview";
    @org.eclipse.jdt.annotation.NonNull
    private String overview;

    public static final String JSON_PROPERTY_TAGLINES = "Taglines";
    @org.eclipse.jdt.annotation.NonNull
    private List<String> taglines;

    public static final String JSON_PROPERTY_GENRES = "Genres";
    @org.eclipse.jdt.annotation.NonNull
    private List<String> genres;

    public static final String JSON_PROPERTY_COMMUNITY_RATING = "CommunityRating";
    @org.eclipse.jdt.annotation.NonNull
    private Float communityRating;

    public static final String JSON_PROPERTY_CUMULATIVE_RUN_TIME_TICKS = "CumulativeRunTimeTicks";
    @org.eclipse.jdt.annotation.NonNull
    private Long cumulativeRunTimeTicks;

    public static final String JSON_PROPERTY_RUN_TIME_TICKS = "RunTimeTicks";
    @org.eclipse.jdt.annotation.NonNull
    private Long runTimeTicks;

    public static final String JSON_PROPERTY_PLAY_ACCESS = "PlayAccess";
    @org.eclipse.jdt.annotation.NonNull
    private PlayAccess playAccess;

    public static final String JSON_PROPERTY_ASPECT_RATIO = "AspectRatio";
    @org.eclipse.jdt.annotation.NonNull
    private String aspectRatio;

    public static final String JSON_PROPERTY_PRODUCTION_YEAR = "ProductionYear";
    @org.eclipse.jdt.annotation.NonNull
    private Integer productionYear;

    public static final String JSON_PROPERTY_IS_PLACE_HOLDER = "IsPlaceHolder";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean isPlaceHolder;

    public static final String JSON_PROPERTY_NUMBER = "Number";
    @org.eclipse.jdt.annotation.NonNull
    private String number;

    public static final String JSON_PROPERTY_CHANNEL_NUMBER = "ChannelNumber";
    @org.eclipse.jdt.annotation.NonNull
    private String channelNumber;

    public static final String JSON_PROPERTY_INDEX_NUMBER = "IndexNumber";
    @org.eclipse.jdt.annotation.NonNull
    private Integer indexNumber;

    public static final String JSON_PROPERTY_INDEX_NUMBER_END = "IndexNumberEnd";
    @org.eclipse.jdt.annotation.NonNull
    private Integer indexNumberEnd;

    public static final String JSON_PROPERTY_PARENT_INDEX_NUMBER = "ParentIndexNumber";
    @org.eclipse.jdt.annotation.NonNull
    private Integer parentIndexNumber;

    public static final String JSON_PROPERTY_REMOTE_TRAILERS = "RemoteTrailers";
    @org.eclipse.jdt.annotation.NonNull
    private List<MediaUrl> remoteTrailers;

    public static final String JSON_PROPERTY_PROVIDER_IDS = "ProviderIds";
    @org.eclipse.jdt.annotation.NonNull
    private Map<String, String> providerIds;

    public static final String JSON_PROPERTY_IS_H_D = "IsHD";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean isHD;

    public static final String JSON_PROPERTY_IS_FOLDER = "IsFolder";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean isFolder;

    public static final String JSON_PROPERTY_PARENT_ID = "ParentId";
    @org.eclipse.jdt.annotation.NonNull
    private UUID parentId;

    public static final String JSON_PROPERTY_TYPE = "Type";
    @org.eclipse.jdt.annotation.NonNull
    private BaseItemKind type;

    public static final String JSON_PROPERTY_PEOPLE = "People";
    @org.eclipse.jdt.annotation.NonNull
    private List<BaseItemPerson> people;

    public static final String JSON_PROPERTY_STUDIOS = "Studios";
    @org.eclipse.jdt.annotation.NonNull
    private List<NameGuidPair> studios;

    public static final String JSON_PROPERTY_GENRE_ITEMS = "GenreItems";
    @org.eclipse.jdt.annotation.NonNull
    private List<NameGuidPair> genreItems;

    public static final String JSON_PROPERTY_PARENT_LOGO_ITEM_ID = "ParentLogoItemId";
    @org.eclipse.jdt.annotation.NonNull
    private UUID parentLogoItemId;

    public static final String JSON_PROPERTY_PARENT_BACKDROP_ITEM_ID = "ParentBackdropItemId";
    @org.eclipse.jdt.annotation.NonNull
    private UUID parentBackdropItemId;

    public static final String JSON_PROPERTY_PARENT_BACKDROP_IMAGE_TAGS = "ParentBackdropImageTags";
    @org.eclipse.jdt.annotation.NonNull
    private List<String> parentBackdropImageTags;

    public static final String JSON_PROPERTY_LOCAL_TRAILER_COUNT = "LocalTrailerCount";
    @org.eclipse.jdt.annotation.NonNull
    private Integer localTrailerCount;

    public static final String JSON_PROPERTY_USER_DATA = "UserData";
    @org.eclipse.jdt.annotation.NonNull
    private UserItemDataDto userData;

    public static final String JSON_PROPERTY_RECURSIVE_ITEM_COUNT = "RecursiveItemCount";
    @org.eclipse.jdt.annotation.NonNull
    private Integer recursiveItemCount;

    public static final String JSON_PROPERTY_CHILD_COUNT = "ChildCount";
    @org.eclipse.jdt.annotation.NonNull
    private Integer childCount;

    public static final String JSON_PROPERTY_SERIES_NAME = "SeriesName";
    @org.eclipse.jdt.annotation.NonNull
    private String seriesName;

    public static final String JSON_PROPERTY_SERIES_ID = "SeriesId";
    @org.eclipse.jdt.annotation.NonNull
    private UUID seriesId;

    public static final String JSON_PROPERTY_SEASON_ID = "SeasonId";
    @org.eclipse.jdt.annotation.NonNull
    private UUID seasonId;

    public static final String JSON_PROPERTY_SPECIAL_FEATURE_COUNT = "SpecialFeatureCount";
    @org.eclipse.jdt.annotation.NonNull
    private Integer specialFeatureCount;

    public static final String JSON_PROPERTY_DISPLAY_PREFERENCES_ID = "DisplayPreferencesId";
    @org.eclipse.jdt.annotation.NonNull
    private String displayPreferencesId;

    public static final String JSON_PROPERTY_STATUS = "Status";
    @org.eclipse.jdt.annotation.NonNull
    private String status;

    public static final String JSON_PROPERTY_AIR_TIME = "AirTime";
    @org.eclipse.jdt.annotation.NonNull
    private String airTime;

    public static final String JSON_PROPERTY_AIR_DAYS = "AirDays";
    @org.eclipse.jdt.annotation.NonNull
    private List<DayOfWeek> airDays;

    public static final String JSON_PROPERTY_TAGS = "Tags";
    @org.eclipse.jdt.annotation.NonNull
    private List<String> tags;

    public static final String JSON_PROPERTY_PRIMARY_IMAGE_ASPECT_RATIO = "PrimaryImageAspectRatio";
    @org.eclipse.jdt.annotation.NonNull
    private Double primaryImageAspectRatio;

    public static final String JSON_PROPERTY_ARTISTS = "Artists";
    @org.eclipse.jdt.annotation.NonNull
    private List<String> artists;

    public static final String JSON_PROPERTY_ARTIST_ITEMS = "ArtistItems";
    @org.eclipse.jdt.annotation.NonNull
    private List<NameGuidPair> artistItems;

    public static final String JSON_PROPERTY_ALBUM = "Album";
    @org.eclipse.jdt.annotation.NonNull
    private String album;

    public static final String JSON_PROPERTY_COLLECTION_TYPE = "CollectionType";
    @org.eclipse.jdt.annotation.NonNull
    private CollectionType collectionType;

    public static final String JSON_PROPERTY_DISPLAY_ORDER = "DisplayOrder";
    @org.eclipse.jdt.annotation.NonNull
    private String displayOrder;

    public static final String JSON_PROPERTY_ALBUM_ID = "AlbumId";
    @org.eclipse.jdt.annotation.NonNull
    private UUID albumId;

    public static final String JSON_PROPERTY_ALBUM_PRIMARY_IMAGE_TAG = "AlbumPrimaryImageTag";
    @org.eclipse.jdt.annotation.NonNull
    private String albumPrimaryImageTag;

    public static final String JSON_PROPERTY_SERIES_PRIMARY_IMAGE_TAG = "SeriesPrimaryImageTag";
    @org.eclipse.jdt.annotation.NonNull
    private String seriesPrimaryImageTag;

    public static final String JSON_PROPERTY_ALBUM_ARTIST = "AlbumArtist";
    @org.eclipse.jdt.annotation.NonNull
    private String albumArtist;

    public static final String JSON_PROPERTY_ALBUM_ARTISTS = "AlbumArtists";
    @org.eclipse.jdt.annotation.NonNull
    private List<NameGuidPair> albumArtists;

    public static final String JSON_PROPERTY_SEASON_NAME = "SeasonName";
    @org.eclipse.jdt.annotation.NonNull
    private String seasonName;

    public static final String JSON_PROPERTY_MEDIA_STREAMS = "MediaStreams";
    @org.eclipse.jdt.annotation.NonNull
    private List<MediaStream> mediaStreams;

    public static final String JSON_PROPERTY_VIDEO_TYPE = "VideoType";
    @org.eclipse.jdt.annotation.NonNull
    private VideoType videoType;

    public static final String JSON_PROPERTY_PART_COUNT = "PartCount";
    @org.eclipse.jdt.annotation.NonNull
    private Integer partCount;

    public static final String JSON_PROPERTY_MEDIA_SOURCE_COUNT = "MediaSourceCount";
    @org.eclipse.jdt.annotation.NonNull
    private Integer mediaSourceCount;

    public static final String JSON_PROPERTY_IMAGE_TAGS = "ImageTags";
    @org.eclipse.jdt.annotation.NonNull
    private Map<String, String> imageTags;

    public static final String JSON_PROPERTY_BACKDROP_IMAGE_TAGS = "BackdropImageTags";
    @org.eclipse.jdt.annotation.NonNull
    private List<String> backdropImageTags;

    public static final String JSON_PROPERTY_SCREENSHOT_IMAGE_TAGS = "ScreenshotImageTags";
    @org.eclipse.jdt.annotation.NonNull
    private List<String> screenshotImageTags;

    public static final String JSON_PROPERTY_PARENT_LOGO_IMAGE_TAG = "ParentLogoImageTag";
    @org.eclipse.jdt.annotation.NonNull
    private String parentLogoImageTag;

    public static final String JSON_PROPERTY_PARENT_ART_ITEM_ID = "ParentArtItemId";
    @org.eclipse.jdt.annotation.NonNull
    private UUID parentArtItemId;

    public static final String JSON_PROPERTY_PARENT_ART_IMAGE_TAG = "ParentArtImageTag";
    @org.eclipse.jdt.annotation.NonNull
    private String parentArtImageTag;

    public static final String JSON_PROPERTY_SERIES_THUMB_IMAGE_TAG = "SeriesThumbImageTag";
    @org.eclipse.jdt.annotation.NonNull
    private String seriesThumbImageTag;

    public static final String JSON_PROPERTY_IMAGE_BLUR_HASHES = "ImageBlurHashes";
    @org.eclipse.jdt.annotation.NonNull
    private BaseItemDtoImageBlurHashes imageBlurHashes;

    public static final String JSON_PROPERTY_SERIES_STUDIO = "SeriesStudio";
    @org.eclipse.jdt.annotation.NonNull
    private String seriesStudio;

    public static final String JSON_PROPERTY_PARENT_THUMB_ITEM_ID = "ParentThumbItemId";
    @org.eclipse.jdt.annotation.NonNull
    private UUID parentThumbItemId;

    public static final String JSON_PROPERTY_PARENT_THUMB_IMAGE_TAG = "ParentThumbImageTag";
    @org.eclipse.jdt.annotation.NonNull
    private String parentThumbImageTag;

    public static final String JSON_PROPERTY_PARENT_PRIMARY_IMAGE_ITEM_ID = "ParentPrimaryImageItemId";
    @org.eclipse.jdt.annotation.NonNull
    private UUID parentPrimaryImageItemId;

    public static final String JSON_PROPERTY_PARENT_PRIMARY_IMAGE_TAG = "ParentPrimaryImageTag";
    @org.eclipse.jdt.annotation.NonNull
    private String parentPrimaryImageTag;

    public static final String JSON_PROPERTY_CHAPTERS = "Chapters";
    @org.eclipse.jdt.annotation.NonNull
    private List<ChapterInfo> chapters;

    public static final String JSON_PROPERTY_TRICKPLAY = "Trickplay";
    @org.eclipse.jdt.annotation.NonNull
    private Map<String, Map<String, TrickplayInfoDto>> trickplay;

    public static final String JSON_PROPERTY_LOCATION_TYPE = "LocationType";
    @org.eclipse.jdt.annotation.NonNull
    private LocationType locationType;

    public static final String JSON_PROPERTY_ISO_TYPE = "IsoType";
    @org.eclipse.jdt.annotation.NonNull
    private IsoType isoType;

    public static final String JSON_PROPERTY_MEDIA_TYPE = "MediaType";
    @org.eclipse.jdt.annotation.NonNull
    private MediaType mediaType = MediaType.UNKNOWN;

    public static final String JSON_PROPERTY_END_DATE = "EndDate";
    @org.eclipse.jdt.annotation.NonNull
    private OffsetDateTime endDate;

    public static final String JSON_PROPERTY_LOCKED_FIELDS = "LockedFields";
    @org.eclipse.jdt.annotation.NonNull
    private List<MetadataField> lockedFields;

    public static final String JSON_PROPERTY_TRAILER_COUNT = "TrailerCount";
    @org.eclipse.jdt.annotation.NonNull
    private Integer trailerCount;

    public static final String JSON_PROPERTY_MOVIE_COUNT = "MovieCount";
    @org.eclipse.jdt.annotation.NonNull
    private Integer movieCount;

    public static final String JSON_PROPERTY_SERIES_COUNT = "SeriesCount";
    @org.eclipse.jdt.annotation.NonNull
    private Integer seriesCount;

    public static final String JSON_PROPERTY_PROGRAM_COUNT = "ProgramCount";
    @org.eclipse.jdt.annotation.NonNull
    private Integer programCount;

    public static final String JSON_PROPERTY_EPISODE_COUNT = "EpisodeCount";
    @org.eclipse.jdt.annotation.NonNull
    private Integer episodeCount;

    public static final String JSON_PROPERTY_SONG_COUNT = "SongCount";
    @org.eclipse.jdt.annotation.NonNull
    private Integer songCount;

    public static final String JSON_PROPERTY_ALBUM_COUNT = "AlbumCount";
    @org.eclipse.jdt.annotation.NonNull
    private Integer albumCount;

    public static final String JSON_PROPERTY_ARTIST_COUNT = "ArtistCount";
    @org.eclipse.jdt.annotation.NonNull
    private Integer artistCount;

    public static final String JSON_PROPERTY_MUSIC_VIDEO_COUNT = "MusicVideoCount";
    @org.eclipse.jdt.annotation.NonNull
    private Integer musicVideoCount;

    public static final String JSON_PROPERTY_LOCK_DATA = "LockData";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean lockData;

    public static final String JSON_PROPERTY_WIDTH = "Width";
    @org.eclipse.jdt.annotation.NonNull
    private Integer width;

    public static final String JSON_PROPERTY_HEIGHT = "Height";
    @org.eclipse.jdt.annotation.NonNull
    private Integer height;

    public static final String JSON_PROPERTY_CAMERA_MAKE = "CameraMake";
    @org.eclipse.jdt.annotation.NonNull
    private String cameraMake;

    public static final String JSON_PROPERTY_CAMERA_MODEL = "CameraModel";
    @org.eclipse.jdt.annotation.NonNull
    private String cameraModel;

    public static final String JSON_PROPERTY_SOFTWARE = "Software";
    @org.eclipse.jdt.annotation.NonNull
    private String software;

    public static final String JSON_PROPERTY_EXPOSURE_TIME = "ExposureTime";
    @org.eclipse.jdt.annotation.NonNull
    private Double exposureTime;

    public static final String JSON_PROPERTY_FOCAL_LENGTH = "FocalLength";
    @org.eclipse.jdt.annotation.NonNull
    private Double focalLength;

    public static final String JSON_PROPERTY_IMAGE_ORIENTATION = "ImageOrientation";
    @org.eclipse.jdt.annotation.NonNull
    private ImageOrientation imageOrientation;

    public static final String JSON_PROPERTY_APERTURE = "Aperture";
    @org.eclipse.jdt.annotation.NonNull
    private Double aperture;

    public static final String JSON_PROPERTY_SHUTTER_SPEED = "ShutterSpeed";
    @org.eclipse.jdt.annotation.NonNull
    private Double shutterSpeed;

    public static final String JSON_PROPERTY_LATITUDE = "Latitude";
    @org.eclipse.jdt.annotation.NonNull
    private Double latitude;

    public static final String JSON_PROPERTY_LONGITUDE = "Longitude";
    @org.eclipse.jdt.annotation.NonNull
    private Double longitude;

    public static final String JSON_PROPERTY_ALTITUDE = "Altitude";
    @org.eclipse.jdt.annotation.NonNull
    private Double altitude;

    public static final String JSON_PROPERTY_ISO_SPEED_RATING = "IsoSpeedRating";
    @org.eclipse.jdt.annotation.NonNull
    private Integer isoSpeedRating;

    public static final String JSON_PROPERTY_SERIES_TIMER_ID = "SeriesTimerId";
    @org.eclipse.jdt.annotation.NonNull
    private String seriesTimerId;

    public static final String JSON_PROPERTY_PROGRAM_ID = "ProgramId";
    @org.eclipse.jdt.annotation.NonNull
    private String programId;

    public static final String JSON_PROPERTY_CHANNEL_PRIMARY_IMAGE_TAG = "ChannelPrimaryImageTag";
    @org.eclipse.jdt.annotation.NonNull
    private String channelPrimaryImageTag;

    public static final String JSON_PROPERTY_START_DATE = "StartDate";
    @org.eclipse.jdt.annotation.NonNull
    private OffsetDateTime startDate;

    public static final String JSON_PROPERTY_COMPLETION_PERCENTAGE = "CompletionPercentage";
    @org.eclipse.jdt.annotation.NonNull
    private Double completionPercentage;

    public static final String JSON_PROPERTY_IS_REPEAT = "IsRepeat";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean isRepeat;

    public static final String JSON_PROPERTY_EPISODE_TITLE = "EpisodeTitle";
    @org.eclipse.jdt.annotation.NonNull
    private String episodeTitle;

    public static final String JSON_PROPERTY_CHANNEL_TYPE = "ChannelType";
    @org.eclipse.jdt.annotation.NonNull
    private ChannelType channelType;

    public static final String JSON_PROPERTY_AUDIO = "Audio";
    @org.eclipse.jdt.annotation.NonNull
    private ProgramAudio audio;

    public static final String JSON_PROPERTY_IS_MOVIE = "IsMovie";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean isMovie;

    public static final String JSON_PROPERTY_IS_SPORTS = "IsSports";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean isSports;

    public static final String JSON_PROPERTY_IS_SERIES = "IsSeries";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean isSeries;

    public static final String JSON_PROPERTY_IS_LIVE = "IsLive";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean isLive;

    public static final String JSON_PROPERTY_IS_NEWS = "IsNews";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean isNews;

    public static final String JSON_PROPERTY_IS_KIDS = "IsKids";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean isKids;

    public static final String JSON_PROPERTY_IS_PREMIERE = "IsPremiere";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean isPremiere;

    public static final String JSON_PROPERTY_TIMER_ID = "TimerId";
    @org.eclipse.jdt.annotation.NonNull
    private String timerId;

    public static final String JSON_PROPERTY_NORMALIZATION_GAIN = "NormalizationGain";
    @org.eclipse.jdt.annotation.NonNull
    private Float normalizationGain;

    public static final String JSON_PROPERTY_CURRENT_PROGRAM = "CurrentProgram";
    @org.eclipse.jdt.annotation.NonNull
    private BaseItemDto currentProgram;

    public BaseItemDto() {
    }

    public BaseItemDto name(@org.eclipse.jdt.annotation.NonNull String name) {
        this.name = name;
        return this;
    }

    /**
     * Gets or sets the name.
     * 
     * @return name
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getName() {
        return name;
    }

    @JsonProperty(value = JSON_PROPERTY_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setName(@org.eclipse.jdt.annotation.NonNull String name) {
        this.name = name;
    }

    public BaseItemDto originalTitle(@org.eclipse.jdt.annotation.NonNull String originalTitle) {
        this.originalTitle = originalTitle;
        return this;
    }

    /**
     * Get originalTitle
     * 
     * @return originalTitle
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ORIGINAL_TITLE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getOriginalTitle() {
        return originalTitle;
    }

    @JsonProperty(value = JSON_PROPERTY_ORIGINAL_TITLE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setOriginalTitle(@org.eclipse.jdt.annotation.NonNull String originalTitle) {
        this.originalTitle = originalTitle;
    }

    public BaseItemDto serverId(@org.eclipse.jdt.annotation.NonNull String serverId) {
        this.serverId = serverId;
        return this;
    }

    /**
     * Gets or sets the server identifier.
     * 
     * @return serverId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_SERVER_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getServerId() {
        return serverId;
    }

    @JsonProperty(value = JSON_PROPERTY_SERVER_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setServerId(@org.eclipse.jdt.annotation.NonNull String serverId) {
        this.serverId = serverId;
    }

    public BaseItemDto id(@org.eclipse.jdt.annotation.NonNull UUID id) {
        this.id = id;
        return this;
    }

    /**
     * Gets or sets the id.
     * 
     * @return id
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public UUID getId() {
        return id;
    }

    @JsonProperty(value = JSON_PROPERTY_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setId(@org.eclipse.jdt.annotation.NonNull UUID id) {
        this.id = id;
    }

    public BaseItemDto etag(@org.eclipse.jdt.annotation.NonNull String etag) {
        this.etag = etag;
        return this;
    }

    /**
     * Gets or sets the etag.
     * 
     * @return etag
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ETAG, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getEtag() {
        return etag;
    }

    @JsonProperty(value = JSON_PROPERTY_ETAG, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEtag(@org.eclipse.jdt.annotation.NonNull String etag) {
        this.etag = etag;
    }

    public BaseItemDto sourceType(@org.eclipse.jdt.annotation.NonNull String sourceType) {
        this.sourceType = sourceType;
        return this;
    }

    /**
     * Gets or sets the type of the source.
     * 
     * @return sourceType
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_SOURCE_TYPE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getSourceType() {
        return sourceType;
    }

    @JsonProperty(value = JSON_PROPERTY_SOURCE_TYPE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSourceType(@org.eclipse.jdt.annotation.NonNull String sourceType) {
        this.sourceType = sourceType;
    }

    public BaseItemDto playlistItemId(@org.eclipse.jdt.annotation.NonNull String playlistItemId) {
        this.playlistItemId = playlistItemId;
        return this;
    }

    /**
     * Gets or sets the playlist item identifier.
     * 
     * @return playlistItemId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_PLAYLIST_ITEM_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getPlaylistItemId() {
        return playlistItemId;
    }

    @JsonProperty(value = JSON_PROPERTY_PLAYLIST_ITEM_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPlaylistItemId(@org.eclipse.jdt.annotation.NonNull String playlistItemId) {
        this.playlistItemId = playlistItemId;
    }

    public BaseItemDto dateCreated(@org.eclipse.jdt.annotation.NonNull OffsetDateTime dateCreated) {
        this.dateCreated = dateCreated;
        return this;
    }

    /**
     * Gets or sets the date created.
     * 
     * @return dateCreated
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_DATE_CREATED, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public OffsetDateTime getDateCreated() {
        return dateCreated;
    }

    @JsonProperty(value = JSON_PROPERTY_DATE_CREATED, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDateCreated(@org.eclipse.jdt.annotation.NonNull OffsetDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }

    public BaseItemDto dateLastMediaAdded(@org.eclipse.jdt.annotation.NonNull OffsetDateTime dateLastMediaAdded) {
        this.dateLastMediaAdded = dateLastMediaAdded;
        return this;
    }

    /**
     * Get dateLastMediaAdded
     * 
     * @return dateLastMediaAdded
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_DATE_LAST_MEDIA_ADDED, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public OffsetDateTime getDateLastMediaAdded() {
        return dateLastMediaAdded;
    }

    @JsonProperty(value = JSON_PROPERTY_DATE_LAST_MEDIA_ADDED, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDateLastMediaAdded(@org.eclipse.jdt.annotation.NonNull OffsetDateTime dateLastMediaAdded) {
        this.dateLastMediaAdded = dateLastMediaAdded;
    }

    public BaseItemDto extraType(@org.eclipse.jdt.annotation.NonNull ExtraType extraType) {
        this.extraType = extraType;
        return this;
    }

    /**
     * Get extraType
     * 
     * @return extraType
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_EXTRA_TYPE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public ExtraType getExtraType() {
        return extraType;
    }

    @JsonProperty(value = JSON_PROPERTY_EXTRA_TYPE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setExtraType(@org.eclipse.jdt.annotation.NonNull ExtraType extraType) {
        this.extraType = extraType;
    }

    public BaseItemDto airsBeforeSeasonNumber(@org.eclipse.jdt.annotation.NonNull Integer airsBeforeSeasonNumber) {
        this.airsBeforeSeasonNumber = airsBeforeSeasonNumber;
        return this;
    }

    /**
     * Get airsBeforeSeasonNumber
     * 
     * @return airsBeforeSeasonNumber
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_AIRS_BEFORE_SEASON_NUMBER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getAirsBeforeSeasonNumber() {
        return airsBeforeSeasonNumber;
    }

    @JsonProperty(value = JSON_PROPERTY_AIRS_BEFORE_SEASON_NUMBER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAirsBeforeSeasonNumber(@org.eclipse.jdt.annotation.NonNull Integer airsBeforeSeasonNumber) {
        this.airsBeforeSeasonNumber = airsBeforeSeasonNumber;
    }

    public BaseItemDto airsAfterSeasonNumber(@org.eclipse.jdt.annotation.NonNull Integer airsAfterSeasonNumber) {
        this.airsAfterSeasonNumber = airsAfterSeasonNumber;
        return this;
    }

    /**
     * Get airsAfterSeasonNumber
     * 
     * @return airsAfterSeasonNumber
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_AIRS_AFTER_SEASON_NUMBER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getAirsAfterSeasonNumber() {
        return airsAfterSeasonNumber;
    }

    @JsonProperty(value = JSON_PROPERTY_AIRS_AFTER_SEASON_NUMBER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAirsAfterSeasonNumber(@org.eclipse.jdt.annotation.NonNull Integer airsAfterSeasonNumber) {
        this.airsAfterSeasonNumber = airsAfterSeasonNumber;
    }

    public BaseItemDto airsBeforeEpisodeNumber(@org.eclipse.jdt.annotation.NonNull Integer airsBeforeEpisodeNumber) {
        this.airsBeforeEpisodeNumber = airsBeforeEpisodeNumber;
        return this;
    }

    /**
     * Get airsBeforeEpisodeNumber
     * 
     * @return airsBeforeEpisodeNumber
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_AIRS_BEFORE_EPISODE_NUMBER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getAirsBeforeEpisodeNumber() {
        return airsBeforeEpisodeNumber;
    }

    @JsonProperty(value = JSON_PROPERTY_AIRS_BEFORE_EPISODE_NUMBER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAirsBeforeEpisodeNumber(@org.eclipse.jdt.annotation.NonNull Integer airsBeforeEpisodeNumber) {
        this.airsBeforeEpisodeNumber = airsBeforeEpisodeNumber;
    }

    public BaseItemDto canDelete(@org.eclipse.jdt.annotation.NonNull Boolean canDelete) {
        this.canDelete = canDelete;
        return this;
    }

    /**
     * Get canDelete
     * 
     * @return canDelete
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_CAN_DELETE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getCanDelete() {
        return canDelete;
    }

    @JsonProperty(value = JSON_PROPERTY_CAN_DELETE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCanDelete(@org.eclipse.jdt.annotation.NonNull Boolean canDelete) {
        this.canDelete = canDelete;
    }

    public BaseItemDto canDownload(@org.eclipse.jdt.annotation.NonNull Boolean canDownload) {
        this.canDownload = canDownload;
        return this;
    }

    /**
     * Get canDownload
     * 
     * @return canDownload
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_CAN_DOWNLOAD, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getCanDownload() {
        return canDownload;
    }

    @JsonProperty(value = JSON_PROPERTY_CAN_DOWNLOAD, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCanDownload(@org.eclipse.jdt.annotation.NonNull Boolean canDownload) {
        this.canDownload = canDownload;
    }

    public BaseItemDto hasLyrics(@org.eclipse.jdt.annotation.NonNull Boolean hasLyrics) {
        this.hasLyrics = hasLyrics;
        return this;
    }

    /**
     * Get hasLyrics
     * 
     * @return hasLyrics
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_HAS_LYRICS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getHasLyrics() {
        return hasLyrics;
    }

    @JsonProperty(value = JSON_PROPERTY_HAS_LYRICS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setHasLyrics(@org.eclipse.jdt.annotation.NonNull Boolean hasLyrics) {
        this.hasLyrics = hasLyrics;
    }

    public BaseItemDto hasSubtitles(@org.eclipse.jdt.annotation.NonNull Boolean hasSubtitles) {
        this.hasSubtitles = hasSubtitles;
        return this;
    }

    /**
     * Get hasSubtitles
     * 
     * @return hasSubtitles
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_HAS_SUBTITLES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getHasSubtitles() {
        return hasSubtitles;
    }

    @JsonProperty(value = JSON_PROPERTY_HAS_SUBTITLES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setHasSubtitles(@org.eclipse.jdt.annotation.NonNull Boolean hasSubtitles) {
        this.hasSubtitles = hasSubtitles;
    }

    public BaseItemDto preferredMetadataLanguage(@org.eclipse.jdt.annotation.NonNull String preferredMetadataLanguage) {
        this.preferredMetadataLanguage = preferredMetadataLanguage;
        return this;
    }

    /**
     * Get preferredMetadataLanguage
     * 
     * @return preferredMetadataLanguage
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_PREFERRED_METADATA_LANGUAGE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getPreferredMetadataLanguage() {
        return preferredMetadataLanguage;
    }

    @JsonProperty(value = JSON_PROPERTY_PREFERRED_METADATA_LANGUAGE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPreferredMetadataLanguage(@org.eclipse.jdt.annotation.NonNull String preferredMetadataLanguage) {
        this.preferredMetadataLanguage = preferredMetadataLanguage;
    }

    public BaseItemDto preferredMetadataCountryCode(
            @org.eclipse.jdt.annotation.NonNull String preferredMetadataCountryCode) {
        this.preferredMetadataCountryCode = preferredMetadataCountryCode;
        return this;
    }

    /**
     * Get preferredMetadataCountryCode
     * 
     * @return preferredMetadataCountryCode
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_PREFERRED_METADATA_COUNTRY_CODE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getPreferredMetadataCountryCode() {
        return preferredMetadataCountryCode;
    }

    @JsonProperty(value = JSON_PROPERTY_PREFERRED_METADATA_COUNTRY_CODE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPreferredMetadataCountryCode(
            @org.eclipse.jdt.annotation.NonNull String preferredMetadataCountryCode) {
        this.preferredMetadataCountryCode = preferredMetadataCountryCode;
    }

    public BaseItemDto container(@org.eclipse.jdt.annotation.NonNull String container) {
        this.container = container;
        return this;
    }

    /**
     * Get container
     * 
     * @return container
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_CONTAINER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getContainer() {
        return container;
    }

    @JsonProperty(value = JSON_PROPERTY_CONTAINER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setContainer(@org.eclipse.jdt.annotation.NonNull String container) {
        this.container = container;
    }

    public BaseItemDto sortName(@org.eclipse.jdt.annotation.NonNull String sortName) {
        this.sortName = sortName;
        return this;
    }

    /**
     * Gets or sets the name of the sort.
     * 
     * @return sortName
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_SORT_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getSortName() {
        return sortName;
    }

    @JsonProperty(value = JSON_PROPERTY_SORT_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSortName(@org.eclipse.jdt.annotation.NonNull String sortName) {
        this.sortName = sortName;
    }

    public BaseItemDto forcedSortName(@org.eclipse.jdt.annotation.NonNull String forcedSortName) {
        this.forcedSortName = forcedSortName;
        return this;
    }

    /**
     * Get forcedSortName
     * 
     * @return forcedSortName
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_FORCED_SORT_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getForcedSortName() {
        return forcedSortName;
    }

    @JsonProperty(value = JSON_PROPERTY_FORCED_SORT_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setForcedSortName(@org.eclipse.jdt.annotation.NonNull String forcedSortName) {
        this.forcedSortName = forcedSortName;
    }

    public BaseItemDto video3DFormat(@org.eclipse.jdt.annotation.NonNull Video3DFormat video3DFormat) {
        this.video3DFormat = video3DFormat;
        return this;
    }

    /**
     * Gets or sets the video3 D format.
     * 
     * @return video3DFormat
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_VIDEO3_D_FORMAT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Video3DFormat getVideo3DFormat() {
        return video3DFormat;
    }

    @JsonProperty(value = JSON_PROPERTY_VIDEO3_D_FORMAT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setVideo3DFormat(@org.eclipse.jdt.annotation.NonNull Video3DFormat video3DFormat) {
        this.video3DFormat = video3DFormat;
    }

    public BaseItemDto premiereDate(@org.eclipse.jdt.annotation.NonNull OffsetDateTime premiereDate) {
        this.premiereDate = premiereDate;
        return this;
    }

    /**
     * Gets or sets the premiere date.
     * 
     * @return premiereDate
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_PREMIERE_DATE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public OffsetDateTime getPremiereDate() {
        return premiereDate;
    }

    @JsonProperty(value = JSON_PROPERTY_PREMIERE_DATE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPremiereDate(@org.eclipse.jdt.annotation.NonNull OffsetDateTime premiereDate) {
        this.premiereDate = premiereDate;
    }

    public BaseItemDto externalUrls(@org.eclipse.jdt.annotation.NonNull List<ExternalUrl> externalUrls) {
        this.externalUrls = externalUrls;
        return this;
    }

    public BaseItemDto addExternalUrlsItem(ExternalUrl externalUrlsItem) {
        if (this.externalUrls == null) {
            this.externalUrls = new ArrayList<>();
        }
        this.externalUrls.add(externalUrlsItem);
        return this;
    }

    /**
     * Gets or sets the external urls.
     * 
     * @return externalUrls
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_EXTERNAL_URLS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<ExternalUrl> getExternalUrls() {
        return externalUrls;
    }

    @JsonProperty(value = JSON_PROPERTY_EXTERNAL_URLS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setExternalUrls(@org.eclipse.jdt.annotation.NonNull List<ExternalUrl> externalUrls) {
        this.externalUrls = externalUrls;
    }

    public BaseItemDto mediaSources(@org.eclipse.jdt.annotation.NonNull List<MediaSourceInfo> mediaSources) {
        this.mediaSources = mediaSources;
        return this;
    }

    public BaseItemDto addMediaSourcesItem(MediaSourceInfo mediaSourcesItem) {
        if (this.mediaSources == null) {
            this.mediaSources = new ArrayList<>();
        }
        this.mediaSources.add(mediaSourcesItem);
        return this;
    }

    /**
     * Gets or sets the media versions.
     * 
     * @return mediaSources
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_MEDIA_SOURCES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<MediaSourceInfo> getMediaSources() {
        return mediaSources;
    }

    @JsonProperty(value = JSON_PROPERTY_MEDIA_SOURCES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMediaSources(@org.eclipse.jdt.annotation.NonNull List<MediaSourceInfo> mediaSources) {
        this.mediaSources = mediaSources;
    }

    public BaseItemDto criticRating(@org.eclipse.jdt.annotation.NonNull Float criticRating) {
        this.criticRating = criticRating;
        return this;
    }

    /**
     * Gets or sets the critic rating.
     * 
     * @return criticRating
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_CRITIC_RATING, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Float getCriticRating() {
        return criticRating;
    }

    @JsonProperty(value = JSON_PROPERTY_CRITIC_RATING, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCriticRating(@org.eclipse.jdt.annotation.NonNull Float criticRating) {
        this.criticRating = criticRating;
    }

    public BaseItemDto productionLocations(@org.eclipse.jdt.annotation.NonNull List<String> productionLocations) {
        this.productionLocations = productionLocations;
        return this;
    }

    public BaseItemDto addProductionLocationsItem(String productionLocationsItem) {
        if (this.productionLocations == null) {
            this.productionLocations = new ArrayList<>();
        }
        this.productionLocations.add(productionLocationsItem);
        return this;
    }

    /**
     * Get productionLocations
     * 
     * @return productionLocations
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_PRODUCTION_LOCATIONS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<String> getProductionLocations() {
        return productionLocations;
    }

    @JsonProperty(value = JSON_PROPERTY_PRODUCTION_LOCATIONS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setProductionLocations(@org.eclipse.jdt.annotation.NonNull List<String> productionLocations) {
        this.productionLocations = productionLocations;
    }

    public BaseItemDto path(@org.eclipse.jdt.annotation.NonNull String path) {
        this.path = path;
        return this;
    }

    /**
     * Gets or sets the path.
     * 
     * @return path
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_PATH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getPath() {
        return path;
    }

    @JsonProperty(value = JSON_PROPERTY_PATH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPath(@org.eclipse.jdt.annotation.NonNull String path) {
        this.path = path;
    }

    public BaseItemDto enableMediaSourceDisplay(@org.eclipse.jdt.annotation.NonNull Boolean enableMediaSourceDisplay) {
        this.enableMediaSourceDisplay = enableMediaSourceDisplay;
        return this;
    }

    /**
     * Get enableMediaSourceDisplay
     * 
     * @return enableMediaSourceDisplay
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ENABLE_MEDIA_SOURCE_DISPLAY, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableMediaSourceDisplay() {
        return enableMediaSourceDisplay;
    }

    @JsonProperty(value = JSON_PROPERTY_ENABLE_MEDIA_SOURCE_DISPLAY, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableMediaSourceDisplay(@org.eclipse.jdt.annotation.NonNull Boolean enableMediaSourceDisplay) {
        this.enableMediaSourceDisplay = enableMediaSourceDisplay;
    }

    public BaseItemDto officialRating(@org.eclipse.jdt.annotation.NonNull String officialRating) {
        this.officialRating = officialRating;
        return this;
    }

    /**
     * Gets or sets the official rating.
     * 
     * @return officialRating
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_OFFICIAL_RATING, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getOfficialRating() {
        return officialRating;
    }

    @JsonProperty(value = JSON_PROPERTY_OFFICIAL_RATING, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setOfficialRating(@org.eclipse.jdt.annotation.NonNull String officialRating) {
        this.officialRating = officialRating;
    }

    public BaseItemDto customRating(@org.eclipse.jdt.annotation.NonNull String customRating) {
        this.customRating = customRating;
        return this;
    }

    /**
     * Gets or sets the custom rating.
     * 
     * @return customRating
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_CUSTOM_RATING, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getCustomRating() {
        return customRating;
    }

    @JsonProperty(value = JSON_PROPERTY_CUSTOM_RATING, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCustomRating(@org.eclipse.jdt.annotation.NonNull String customRating) {
        this.customRating = customRating;
    }

    public BaseItemDto channelId(@org.eclipse.jdt.annotation.NonNull UUID channelId) {
        this.channelId = channelId;
        return this;
    }

    /**
     * Gets or sets the channel identifier.
     * 
     * @return channelId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_CHANNEL_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public UUID getChannelId() {
        return channelId;
    }

    @JsonProperty(value = JSON_PROPERTY_CHANNEL_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setChannelId(@org.eclipse.jdt.annotation.NonNull UUID channelId) {
        this.channelId = channelId;
    }

    public BaseItemDto channelName(@org.eclipse.jdt.annotation.NonNull String channelName) {
        this.channelName = channelName;
        return this;
    }

    /**
     * Get channelName
     * 
     * @return channelName
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_CHANNEL_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getChannelName() {
        return channelName;
    }

    @JsonProperty(value = JSON_PROPERTY_CHANNEL_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setChannelName(@org.eclipse.jdt.annotation.NonNull String channelName) {
        this.channelName = channelName;
    }

    public BaseItemDto overview(@org.eclipse.jdt.annotation.NonNull String overview) {
        this.overview = overview;
        return this;
    }

    /**
     * Gets or sets the overview.
     * 
     * @return overview
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_OVERVIEW, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getOverview() {
        return overview;
    }

    @JsonProperty(value = JSON_PROPERTY_OVERVIEW, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setOverview(@org.eclipse.jdt.annotation.NonNull String overview) {
        this.overview = overview;
    }

    public BaseItemDto taglines(@org.eclipse.jdt.annotation.NonNull List<String> taglines) {
        this.taglines = taglines;
        return this;
    }

    public BaseItemDto addTaglinesItem(String taglinesItem) {
        if (this.taglines == null) {
            this.taglines = new ArrayList<>();
        }
        this.taglines.add(taglinesItem);
        return this;
    }

    /**
     * Gets or sets the taglines.
     * 
     * @return taglines
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_TAGLINES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<String> getTaglines() {
        return taglines;
    }

    @JsonProperty(value = JSON_PROPERTY_TAGLINES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTaglines(@org.eclipse.jdt.annotation.NonNull List<String> taglines) {
        this.taglines = taglines;
    }

    public BaseItemDto genres(@org.eclipse.jdt.annotation.NonNull List<String> genres) {
        this.genres = genres;
        return this;
    }

    public BaseItemDto addGenresItem(String genresItem) {
        if (this.genres == null) {
            this.genres = new ArrayList<>();
        }
        this.genres.add(genresItem);
        return this;
    }

    /**
     * Gets or sets the genres.
     * 
     * @return genres
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_GENRES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<String> getGenres() {
        return genres;
    }

    @JsonProperty(value = JSON_PROPERTY_GENRES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setGenres(@org.eclipse.jdt.annotation.NonNull List<String> genres) {
        this.genres = genres;
    }

    public BaseItemDto communityRating(@org.eclipse.jdt.annotation.NonNull Float communityRating) {
        this.communityRating = communityRating;
        return this;
    }

    /**
     * Gets or sets the community rating.
     * 
     * @return communityRating
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_COMMUNITY_RATING, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Float getCommunityRating() {
        return communityRating;
    }

    @JsonProperty(value = JSON_PROPERTY_COMMUNITY_RATING, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCommunityRating(@org.eclipse.jdt.annotation.NonNull Float communityRating) {
        this.communityRating = communityRating;
    }

    public BaseItemDto cumulativeRunTimeTicks(@org.eclipse.jdt.annotation.NonNull Long cumulativeRunTimeTicks) {
        this.cumulativeRunTimeTicks = cumulativeRunTimeTicks;
        return this;
    }

    /**
     * Gets or sets the cumulative run time ticks.
     * 
     * @return cumulativeRunTimeTicks
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_CUMULATIVE_RUN_TIME_TICKS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Long getCumulativeRunTimeTicks() {
        return cumulativeRunTimeTicks;
    }

    @JsonProperty(value = JSON_PROPERTY_CUMULATIVE_RUN_TIME_TICKS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCumulativeRunTimeTicks(@org.eclipse.jdt.annotation.NonNull Long cumulativeRunTimeTicks) {
        this.cumulativeRunTimeTicks = cumulativeRunTimeTicks;
    }

    public BaseItemDto runTimeTicks(@org.eclipse.jdt.annotation.NonNull Long runTimeTicks) {
        this.runTimeTicks = runTimeTicks;
        return this;
    }

    /**
     * Gets or sets the run time ticks.
     * 
     * @return runTimeTicks
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_RUN_TIME_TICKS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Long getRunTimeTicks() {
        return runTimeTicks;
    }

    @JsonProperty(value = JSON_PROPERTY_RUN_TIME_TICKS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRunTimeTicks(@org.eclipse.jdt.annotation.NonNull Long runTimeTicks) {
        this.runTimeTicks = runTimeTicks;
    }

    public BaseItemDto playAccess(@org.eclipse.jdt.annotation.NonNull PlayAccess playAccess) {
        this.playAccess = playAccess;
        return this;
    }

    /**
     * Gets or sets the play access.
     * 
     * @return playAccess
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_PLAY_ACCESS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public PlayAccess getPlayAccess() {
        return playAccess;
    }

    @JsonProperty(value = JSON_PROPERTY_PLAY_ACCESS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPlayAccess(@org.eclipse.jdt.annotation.NonNull PlayAccess playAccess) {
        this.playAccess = playAccess;
    }

    public BaseItemDto aspectRatio(@org.eclipse.jdt.annotation.NonNull String aspectRatio) {
        this.aspectRatio = aspectRatio;
        return this;
    }

    /**
     * Gets or sets the aspect ratio.
     * 
     * @return aspectRatio
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ASPECT_RATIO, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getAspectRatio() {
        return aspectRatio;
    }

    @JsonProperty(value = JSON_PROPERTY_ASPECT_RATIO, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAspectRatio(@org.eclipse.jdt.annotation.NonNull String aspectRatio) {
        this.aspectRatio = aspectRatio;
    }

    public BaseItemDto productionYear(@org.eclipse.jdt.annotation.NonNull Integer productionYear) {
        this.productionYear = productionYear;
        return this;
    }

    /**
     * Gets or sets the production year.
     * 
     * @return productionYear
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_PRODUCTION_YEAR, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getProductionYear() {
        return productionYear;
    }

    @JsonProperty(value = JSON_PROPERTY_PRODUCTION_YEAR, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setProductionYear(@org.eclipse.jdt.annotation.NonNull Integer productionYear) {
        this.productionYear = productionYear;
    }

    public BaseItemDto isPlaceHolder(@org.eclipse.jdt.annotation.NonNull Boolean isPlaceHolder) {
        this.isPlaceHolder = isPlaceHolder;
        return this;
    }

    /**
     * Gets or sets a value indicating whether this instance is place holder.
     * 
     * @return isPlaceHolder
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_IS_PLACE_HOLDER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getIsPlaceHolder() {
        return isPlaceHolder;
    }

    @JsonProperty(value = JSON_PROPERTY_IS_PLACE_HOLDER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsPlaceHolder(@org.eclipse.jdt.annotation.NonNull Boolean isPlaceHolder) {
        this.isPlaceHolder = isPlaceHolder;
    }

    public BaseItemDto number(@org.eclipse.jdt.annotation.NonNull String number) {
        this.number = number;
        return this;
    }

    /**
     * Gets or sets the number.
     * 
     * @return number
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_NUMBER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getNumber() {
        return number;
    }

    @JsonProperty(value = JSON_PROPERTY_NUMBER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setNumber(@org.eclipse.jdt.annotation.NonNull String number) {
        this.number = number;
    }

    public BaseItemDto channelNumber(@org.eclipse.jdt.annotation.NonNull String channelNumber) {
        this.channelNumber = channelNumber;
        return this;
    }

    /**
     * Get channelNumber
     * 
     * @return channelNumber
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_CHANNEL_NUMBER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getChannelNumber() {
        return channelNumber;
    }

    @JsonProperty(value = JSON_PROPERTY_CHANNEL_NUMBER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setChannelNumber(@org.eclipse.jdt.annotation.NonNull String channelNumber) {
        this.channelNumber = channelNumber;
    }

    public BaseItemDto indexNumber(@org.eclipse.jdt.annotation.NonNull Integer indexNumber) {
        this.indexNumber = indexNumber;
        return this;
    }

    /**
     * Gets or sets the index number.
     * 
     * @return indexNumber
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_INDEX_NUMBER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getIndexNumber() {
        return indexNumber;
    }

    @JsonProperty(value = JSON_PROPERTY_INDEX_NUMBER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIndexNumber(@org.eclipse.jdt.annotation.NonNull Integer indexNumber) {
        this.indexNumber = indexNumber;
    }

    public BaseItemDto indexNumberEnd(@org.eclipse.jdt.annotation.NonNull Integer indexNumberEnd) {
        this.indexNumberEnd = indexNumberEnd;
        return this;
    }

    /**
     * Gets or sets the index number end.
     * 
     * @return indexNumberEnd
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_INDEX_NUMBER_END, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getIndexNumberEnd() {
        return indexNumberEnd;
    }

    @JsonProperty(value = JSON_PROPERTY_INDEX_NUMBER_END, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIndexNumberEnd(@org.eclipse.jdt.annotation.NonNull Integer indexNumberEnd) {
        this.indexNumberEnd = indexNumberEnd;
    }

    public BaseItemDto parentIndexNumber(@org.eclipse.jdt.annotation.NonNull Integer parentIndexNumber) {
        this.parentIndexNumber = parentIndexNumber;
        return this;
    }

    /**
     * Gets or sets the parent index number.
     * 
     * @return parentIndexNumber
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_PARENT_INDEX_NUMBER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getParentIndexNumber() {
        return parentIndexNumber;
    }

    @JsonProperty(value = JSON_PROPERTY_PARENT_INDEX_NUMBER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setParentIndexNumber(@org.eclipse.jdt.annotation.NonNull Integer parentIndexNumber) {
        this.parentIndexNumber = parentIndexNumber;
    }

    public BaseItemDto remoteTrailers(@org.eclipse.jdt.annotation.NonNull List<MediaUrl> remoteTrailers) {
        this.remoteTrailers = remoteTrailers;
        return this;
    }

    public BaseItemDto addRemoteTrailersItem(MediaUrl remoteTrailersItem) {
        if (this.remoteTrailers == null) {
            this.remoteTrailers = new ArrayList<>();
        }
        this.remoteTrailers.add(remoteTrailersItem);
        return this;
    }

    /**
     * Gets or sets the trailer urls.
     * 
     * @return remoteTrailers
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_REMOTE_TRAILERS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<MediaUrl> getRemoteTrailers() {
        return remoteTrailers;
    }

    @JsonProperty(value = JSON_PROPERTY_REMOTE_TRAILERS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRemoteTrailers(@org.eclipse.jdt.annotation.NonNull List<MediaUrl> remoteTrailers) {
        this.remoteTrailers = remoteTrailers;
    }

    public BaseItemDto providerIds(@org.eclipse.jdt.annotation.NonNull Map<String, String> providerIds) {
        this.providerIds = providerIds;
        return this;
    }

    public BaseItemDto putProviderIdsItem(String key, String providerIdsItem) {
        if (this.providerIds == null) {
            this.providerIds = new HashMap<>();
        }
        this.providerIds.put(key, providerIdsItem);
        return this;
    }

    /**
     * Gets or sets the provider ids.
     * 
     * @return providerIds
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_PROVIDER_IDS, required = false)
    @JsonInclude(content = JsonInclude.Include.ALWAYS, value = JsonInclude.Include.USE_DEFAULTS)
    public Map<String, String> getProviderIds() {
        return providerIds;
    }

    @JsonProperty(value = JSON_PROPERTY_PROVIDER_IDS, required = false)
    @JsonInclude(content = JsonInclude.Include.ALWAYS, value = JsonInclude.Include.USE_DEFAULTS)
    public void setProviderIds(@org.eclipse.jdt.annotation.NonNull Map<String, String> providerIds) {
        this.providerIds = providerIds;
    }

    public BaseItemDto isHD(@org.eclipse.jdt.annotation.NonNull Boolean isHD) {
        this.isHD = isHD;
        return this;
    }

    /**
     * Gets or sets a value indicating whether this instance is HD.
     * 
     * @return isHD
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_IS_H_D, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getIsHD() {
        return isHD;
    }

    @JsonProperty(value = JSON_PROPERTY_IS_H_D, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsHD(@org.eclipse.jdt.annotation.NonNull Boolean isHD) {
        this.isHD = isHD;
    }

    public BaseItemDto isFolder(@org.eclipse.jdt.annotation.NonNull Boolean isFolder) {
        this.isFolder = isFolder;
        return this;
    }

    /**
     * Gets or sets a value indicating whether this instance is folder.
     * 
     * @return isFolder
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_IS_FOLDER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getIsFolder() {
        return isFolder;
    }

    @JsonProperty(value = JSON_PROPERTY_IS_FOLDER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsFolder(@org.eclipse.jdt.annotation.NonNull Boolean isFolder) {
        this.isFolder = isFolder;
    }

    public BaseItemDto parentId(@org.eclipse.jdt.annotation.NonNull UUID parentId) {
        this.parentId = parentId;
        return this;
    }

    /**
     * Gets or sets the parent id.
     * 
     * @return parentId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_PARENT_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public UUID getParentId() {
        return parentId;
    }

    @JsonProperty(value = JSON_PROPERTY_PARENT_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setParentId(@org.eclipse.jdt.annotation.NonNull UUID parentId) {
        this.parentId = parentId;
    }

    public BaseItemDto type(@org.eclipse.jdt.annotation.NonNull BaseItemKind type) {
        this.type = type;
        return this;
    }

    /**
     * Gets or sets the type.
     * 
     * @return type
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_TYPE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public BaseItemKind getType() {
        return type;
    }

    @JsonProperty(value = JSON_PROPERTY_TYPE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setType(@org.eclipse.jdt.annotation.NonNull BaseItemKind type) {
        this.type = type;
    }

    public BaseItemDto people(@org.eclipse.jdt.annotation.NonNull List<BaseItemPerson> people) {
        this.people = people;
        return this;
    }

    public BaseItemDto addPeopleItem(BaseItemPerson peopleItem) {
        if (this.people == null) {
            this.people = new ArrayList<>();
        }
        this.people.add(peopleItem);
        return this;
    }

    /**
     * Gets or sets the people.
     * 
     * @return people
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_PEOPLE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<BaseItemPerson> getPeople() {
        return people;
    }

    @JsonProperty(value = JSON_PROPERTY_PEOPLE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPeople(@org.eclipse.jdt.annotation.NonNull List<BaseItemPerson> people) {
        this.people = people;
    }

    public BaseItemDto studios(@org.eclipse.jdt.annotation.NonNull List<NameGuidPair> studios) {
        this.studios = studios;
        return this;
    }

    public BaseItemDto addStudiosItem(NameGuidPair studiosItem) {
        if (this.studios == null) {
            this.studios = new ArrayList<>();
        }
        this.studios.add(studiosItem);
        return this;
    }

    /**
     * Gets or sets the studios.
     * 
     * @return studios
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_STUDIOS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<NameGuidPair> getStudios() {
        return studios;
    }

    @JsonProperty(value = JSON_PROPERTY_STUDIOS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setStudios(@org.eclipse.jdt.annotation.NonNull List<NameGuidPair> studios) {
        this.studios = studios;
    }

    public BaseItemDto genreItems(@org.eclipse.jdt.annotation.NonNull List<NameGuidPair> genreItems) {
        this.genreItems = genreItems;
        return this;
    }

    public BaseItemDto addGenreItemsItem(NameGuidPair genreItemsItem) {
        if (this.genreItems == null) {
            this.genreItems = new ArrayList<>();
        }
        this.genreItems.add(genreItemsItem);
        return this;
    }

    /**
     * Get genreItems
     * 
     * @return genreItems
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_GENRE_ITEMS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<NameGuidPair> getGenreItems() {
        return genreItems;
    }

    @JsonProperty(value = JSON_PROPERTY_GENRE_ITEMS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setGenreItems(@org.eclipse.jdt.annotation.NonNull List<NameGuidPair> genreItems) {
        this.genreItems = genreItems;
    }

    public BaseItemDto parentLogoItemId(@org.eclipse.jdt.annotation.NonNull UUID parentLogoItemId) {
        this.parentLogoItemId = parentLogoItemId;
        return this;
    }

    /**
     * Gets or sets whether the item has a logo, this will hold the Id of the Parent that has one.
     * 
     * @return parentLogoItemId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_PARENT_LOGO_ITEM_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public UUID getParentLogoItemId() {
        return parentLogoItemId;
    }

    @JsonProperty(value = JSON_PROPERTY_PARENT_LOGO_ITEM_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setParentLogoItemId(@org.eclipse.jdt.annotation.NonNull UUID parentLogoItemId) {
        this.parentLogoItemId = parentLogoItemId;
    }

    public BaseItemDto parentBackdropItemId(@org.eclipse.jdt.annotation.NonNull UUID parentBackdropItemId) {
        this.parentBackdropItemId = parentBackdropItemId;
        return this;
    }

    /**
     * Gets or sets whether the item has any backdrops, this will hold the Id of the Parent that has one.
     * 
     * @return parentBackdropItemId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_PARENT_BACKDROP_ITEM_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public UUID getParentBackdropItemId() {
        return parentBackdropItemId;
    }

    @JsonProperty(value = JSON_PROPERTY_PARENT_BACKDROP_ITEM_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setParentBackdropItemId(@org.eclipse.jdt.annotation.NonNull UUID parentBackdropItemId) {
        this.parentBackdropItemId = parentBackdropItemId;
    }

    public BaseItemDto parentBackdropImageTags(
            @org.eclipse.jdt.annotation.NonNull List<String> parentBackdropImageTags) {
        this.parentBackdropImageTags = parentBackdropImageTags;
        return this;
    }

    public BaseItemDto addParentBackdropImageTagsItem(String parentBackdropImageTagsItem) {
        if (this.parentBackdropImageTags == null) {
            this.parentBackdropImageTags = new ArrayList<>();
        }
        this.parentBackdropImageTags.add(parentBackdropImageTagsItem);
        return this;
    }

    /**
     * Gets or sets the parent backdrop image tags.
     * 
     * @return parentBackdropImageTags
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_PARENT_BACKDROP_IMAGE_TAGS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<String> getParentBackdropImageTags() {
        return parentBackdropImageTags;
    }

    @JsonProperty(value = JSON_PROPERTY_PARENT_BACKDROP_IMAGE_TAGS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setParentBackdropImageTags(@org.eclipse.jdt.annotation.NonNull List<String> parentBackdropImageTags) {
        this.parentBackdropImageTags = parentBackdropImageTags;
    }

    public BaseItemDto localTrailerCount(@org.eclipse.jdt.annotation.NonNull Integer localTrailerCount) {
        this.localTrailerCount = localTrailerCount;
        return this;
    }

    /**
     * Gets or sets the local trailer count.
     * 
     * @return localTrailerCount
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_LOCAL_TRAILER_COUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getLocalTrailerCount() {
        return localTrailerCount;
    }

    @JsonProperty(value = JSON_PROPERTY_LOCAL_TRAILER_COUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLocalTrailerCount(@org.eclipse.jdt.annotation.NonNull Integer localTrailerCount) {
        this.localTrailerCount = localTrailerCount;
    }

    public BaseItemDto userData(@org.eclipse.jdt.annotation.NonNull UserItemDataDto userData) {
        this.userData = userData;
        return this;
    }

    /**
     * Gets or sets the user data for this item based on the user it&#39;s being requested for.
     * 
     * @return userData
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_USER_DATA, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public UserItemDataDto getUserData() {
        return userData;
    }

    @JsonProperty(value = JSON_PROPERTY_USER_DATA, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUserData(@org.eclipse.jdt.annotation.NonNull UserItemDataDto userData) {
        this.userData = userData;
    }

    public BaseItemDto recursiveItemCount(@org.eclipse.jdt.annotation.NonNull Integer recursiveItemCount) {
        this.recursiveItemCount = recursiveItemCount;
        return this;
    }

    /**
     * Gets or sets the recursive item count.
     * 
     * @return recursiveItemCount
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_RECURSIVE_ITEM_COUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getRecursiveItemCount() {
        return recursiveItemCount;
    }

    @JsonProperty(value = JSON_PROPERTY_RECURSIVE_ITEM_COUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRecursiveItemCount(@org.eclipse.jdt.annotation.NonNull Integer recursiveItemCount) {
        this.recursiveItemCount = recursiveItemCount;
    }

    public BaseItemDto childCount(@org.eclipse.jdt.annotation.NonNull Integer childCount) {
        this.childCount = childCount;
        return this;
    }

    /**
     * Gets or sets the child count.
     * 
     * @return childCount
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_CHILD_COUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getChildCount() {
        return childCount;
    }

    @JsonProperty(value = JSON_PROPERTY_CHILD_COUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setChildCount(@org.eclipse.jdt.annotation.NonNull Integer childCount) {
        this.childCount = childCount;
    }

    public BaseItemDto seriesName(@org.eclipse.jdt.annotation.NonNull String seriesName) {
        this.seriesName = seriesName;
        return this;
    }

    /**
     * Gets or sets the name of the series.
     * 
     * @return seriesName
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_SERIES_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getSeriesName() {
        return seriesName;
    }

    @JsonProperty(value = JSON_PROPERTY_SERIES_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSeriesName(@org.eclipse.jdt.annotation.NonNull String seriesName) {
        this.seriesName = seriesName;
    }

    public BaseItemDto seriesId(@org.eclipse.jdt.annotation.NonNull UUID seriesId) {
        this.seriesId = seriesId;
        return this;
    }

    /**
     * Gets or sets the series id.
     * 
     * @return seriesId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_SERIES_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public UUID getSeriesId() {
        return seriesId;
    }

    @JsonProperty(value = JSON_PROPERTY_SERIES_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSeriesId(@org.eclipse.jdt.annotation.NonNull UUID seriesId) {
        this.seriesId = seriesId;
    }

    public BaseItemDto seasonId(@org.eclipse.jdt.annotation.NonNull UUID seasonId) {
        this.seasonId = seasonId;
        return this;
    }

    /**
     * Gets or sets the season identifier.
     * 
     * @return seasonId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_SEASON_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public UUID getSeasonId() {
        return seasonId;
    }

    @JsonProperty(value = JSON_PROPERTY_SEASON_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSeasonId(@org.eclipse.jdt.annotation.NonNull UUID seasonId) {
        this.seasonId = seasonId;
    }

    public BaseItemDto specialFeatureCount(@org.eclipse.jdt.annotation.NonNull Integer specialFeatureCount) {
        this.specialFeatureCount = specialFeatureCount;
        return this;
    }

    /**
     * Gets or sets the special feature count.
     * 
     * @return specialFeatureCount
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_SPECIAL_FEATURE_COUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getSpecialFeatureCount() {
        return specialFeatureCount;
    }

    @JsonProperty(value = JSON_PROPERTY_SPECIAL_FEATURE_COUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSpecialFeatureCount(@org.eclipse.jdt.annotation.NonNull Integer specialFeatureCount) {
        this.specialFeatureCount = specialFeatureCount;
    }

    public BaseItemDto displayPreferencesId(@org.eclipse.jdt.annotation.NonNull String displayPreferencesId) {
        this.displayPreferencesId = displayPreferencesId;
        return this;
    }

    /**
     * Gets or sets the display preferences id.
     * 
     * @return displayPreferencesId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_DISPLAY_PREFERENCES_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getDisplayPreferencesId() {
        return displayPreferencesId;
    }

    @JsonProperty(value = JSON_PROPERTY_DISPLAY_PREFERENCES_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDisplayPreferencesId(@org.eclipse.jdt.annotation.NonNull String displayPreferencesId) {
        this.displayPreferencesId = displayPreferencesId;
    }

    public BaseItemDto status(@org.eclipse.jdt.annotation.NonNull String status) {
        this.status = status;
        return this;
    }

    /**
     * Gets or sets the status.
     * 
     * @return status
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_STATUS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getStatus() {
        return status;
    }

    @JsonProperty(value = JSON_PROPERTY_STATUS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setStatus(@org.eclipse.jdt.annotation.NonNull String status) {
        this.status = status;
    }

    public BaseItemDto airTime(@org.eclipse.jdt.annotation.NonNull String airTime) {
        this.airTime = airTime;
        return this;
    }

    /**
     * Gets or sets the air time.
     * 
     * @return airTime
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_AIR_TIME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getAirTime() {
        return airTime;
    }

    @JsonProperty(value = JSON_PROPERTY_AIR_TIME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAirTime(@org.eclipse.jdt.annotation.NonNull String airTime) {
        this.airTime = airTime;
    }

    public BaseItemDto airDays(@org.eclipse.jdt.annotation.NonNull List<DayOfWeek> airDays) {
        this.airDays = airDays;
        return this;
    }

    public BaseItemDto addAirDaysItem(DayOfWeek airDaysItem) {
        if (this.airDays == null) {
            this.airDays = new ArrayList<>();
        }
        this.airDays.add(airDaysItem);
        return this;
    }

    /**
     * Gets or sets the air days.
     * 
     * @return airDays
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_AIR_DAYS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<DayOfWeek> getAirDays() {
        return airDays;
    }

    @JsonProperty(value = JSON_PROPERTY_AIR_DAYS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAirDays(@org.eclipse.jdt.annotation.NonNull List<DayOfWeek> airDays) {
        this.airDays = airDays;
    }

    public BaseItemDto tags(@org.eclipse.jdt.annotation.NonNull List<String> tags) {
        this.tags = tags;
        return this;
    }

    public BaseItemDto addTagsItem(String tagsItem) {
        if (this.tags == null) {
            this.tags = new ArrayList<>();
        }
        this.tags.add(tagsItem);
        return this;
    }

    /**
     * Gets or sets the tags.
     * 
     * @return tags
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_TAGS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<String> getTags() {
        return tags;
    }

    @JsonProperty(value = JSON_PROPERTY_TAGS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTags(@org.eclipse.jdt.annotation.NonNull List<String> tags) {
        this.tags = tags;
    }

    public BaseItemDto primaryImageAspectRatio(@org.eclipse.jdt.annotation.NonNull Double primaryImageAspectRatio) {
        this.primaryImageAspectRatio = primaryImageAspectRatio;
        return this;
    }

    /**
     * Gets or sets the primary image aspect ratio, after image enhancements.
     * 
     * @return primaryImageAspectRatio
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_PRIMARY_IMAGE_ASPECT_RATIO, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Double getPrimaryImageAspectRatio() {
        return primaryImageAspectRatio;
    }

    @JsonProperty(value = JSON_PROPERTY_PRIMARY_IMAGE_ASPECT_RATIO, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPrimaryImageAspectRatio(@org.eclipse.jdt.annotation.NonNull Double primaryImageAspectRatio) {
        this.primaryImageAspectRatio = primaryImageAspectRatio;
    }

    public BaseItemDto artists(@org.eclipse.jdt.annotation.NonNull List<String> artists) {
        this.artists = artists;
        return this;
    }

    public BaseItemDto addArtistsItem(String artistsItem) {
        if (this.artists == null) {
            this.artists = new ArrayList<>();
        }
        this.artists.add(artistsItem);
        return this;
    }

    /**
     * Gets or sets the artists.
     * 
     * @return artists
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ARTISTS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<String> getArtists() {
        return artists;
    }

    @JsonProperty(value = JSON_PROPERTY_ARTISTS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setArtists(@org.eclipse.jdt.annotation.NonNull List<String> artists) {
        this.artists = artists;
    }

    public BaseItemDto artistItems(@org.eclipse.jdt.annotation.NonNull List<NameGuidPair> artistItems) {
        this.artistItems = artistItems;
        return this;
    }

    public BaseItemDto addArtistItemsItem(NameGuidPair artistItemsItem) {
        if (this.artistItems == null) {
            this.artistItems = new ArrayList<>();
        }
        this.artistItems.add(artistItemsItem);
        return this;
    }

    /**
     * Gets or sets the artist items.
     * 
     * @return artistItems
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ARTIST_ITEMS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<NameGuidPair> getArtistItems() {
        return artistItems;
    }

    @JsonProperty(value = JSON_PROPERTY_ARTIST_ITEMS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setArtistItems(@org.eclipse.jdt.annotation.NonNull List<NameGuidPair> artistItems) {
        this.artistItems = artistItems;
    }

    public BaseItemDto album(@org.eclipse.jdt.annotation.NonNull String album) {
        this.album = album;
        return this;
    }

    /**
     * Gets or sets the album.
     * 
     * @return album
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ALBUM, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getAlbum() {
        return album;
    }

    @JsonProperty(value = JSON_PROPERTY_ALBUM, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAlbum(@org.eclipse.jdt.annotation.NonNull String album) {
        this.album = album;
    }

    public BaseItemDto collectionType(@org.eclipse.jdt.annotation.NonNull CollectionType collectionType) {
        this.collectionType = collectionType;
        return this;
    }

    /**
     * Gets or sets the type of the collection.
     * 
     * @return collectionType
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_COLLECTION_TYPE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public CollectionType getCollectionType() {
        return collectionType;
    }

    @JsonProperty(value = JSON_PROPERTY_COLLECTION_TYPE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCollectionType(@org.eclipse.jdt.annotation.NonNull CollectionType collectionType) {
        this.collectionType = collectionType;
    }

    public BaseItemDto displayOrder(@org.eclipse.jdt.annotation.NonNull String displayOrder) {
        this.displayOrder = displayOrder;
        return this;
    }

    /**
     * Gets or sets the display order.
     * 
     * @return displayOrder
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_DISPLAY_ORDER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getDisplayOrder() {
        return displayOrder;
    }

    @JsonProperty(value = JSON_PROPERTY_DISPLAY_ORDER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDisplayOrder(@org.eclipse.jdt.annotation.NonNull String displayOrder) {
        this.displayOrder = displayOrder;
    }

    public BaseItemDto albumId(@org.eclipse.jdt.annotation.NonNull UUID albumId) {
        this.albumId = albumId;
        return this;
    }

    /**
     * Gets or sets the album id.
     * 
     * @return albumId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ALBUM_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public UUID getAlbumId() {
        return albumId;
    }

    @JsonProperty(value = JSON_PROPERTY_ALBUM_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAlbumId(@org.eclipse.jdt.annotation.NonNull UUID albumId) {
        this.albumId = albumId;
    }

    public BaseItemDto albumPrimaryImageTag(@org.eclipse.jdt.annotation.NonNull String albumPrimaryImageTag) {
        this.albumPrimaryImageTag = albumPrimaryImageTag;
        return this;
    }

    /**
     * Gets or sets the album image tag.
     * 
     * @return albumPrimaryImageTag
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ALBUM_PRIMARY_IMAGE_TAG, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getAlbumPrimaryImageTag() {
        return albumPrimaryImageTag;
    }

    @JsonProperty(value = JSON_PROPERTY_ALBUM_PRIMARY_IMAGE_TAG, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAlbumPrimaryImageTag(@org.eclipse.jdt.annotation.NonNull String albumPrimaryImageTag) {
        this.albumPrimaryImageTag = albumPrimaryImageTag;
    }

    public BaseItemDto seriesPrimaryImageTag(@org.eclipse.jdt.annotation.NonNull String seriesPrimaryImageTag) {
        this.seriesPrimaryImageTag = seriesPrimaryImageTag;
        return this;
    }

    /**
     * Gets or sets the series primary image tag.
     * 
     * @return seriesPrimaryImageTag
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_SERIES_PRIMARY_IMAGE_TAG, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getSeriesPrimaryImageTag() {
        return seriesPrimaryImageTag;
    }

    @JsonProperty(value = JSON_PROPERTY_SERIES_PRIMARY_IMAGE_TAG, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSeriesPrimaryImageTag(@org.eclipse.jdt.annotation.NonNull String seriesPrimaryImageTag) {
        this.seriesPrimaryImageTag = seriesPrimaryImageTag;
    }

    public BaseItemDto albumArtist(@org.eclipse.jdt.annotation.NonNull String albumArtist) {
        this.albumArtist = albumArtist;
        return this;
    }

    /**
     * Gets or sets the album artist.
     * 
     * @return albumArtist
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ALBUM_ARTIST, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getAlbumArtist() {
        return albumArtist;
    }

    @JsonProperty(value = JSON_PROPERTY_ALBUM_ARTIST, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAlbumArtist(@org.eclipse.jdt.annotation.NonNull String albumArtist) {
        this.albumArtist = albumArtist;
    }

    public BaseItemDto albumArtists(@org.eclipse.jdt.annotation.NonNull List<NameGuidPair> albumArtists) {
        this.albumArtists = albumArtists;
        return this;
    }

    public BaseItemDto addAlbumArtistsItem(NameGuidPair albumArtistsItem) {
        if (this.albumArtists == null) {
            this.albumArtists = new ArrayList<>();
        }
        this.albumArtists.add(albumArtistsItem);
        return this;
    }

    /**
     * Gets or sets the album artists.
     * 
     * @return albumArtists
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ALBUM_ARTISTS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<NameGuidPair> getAlbumArtists() {
        return albumArtists;
    }

    @JsonProperty(value = JSON_PROPERTY_ALBUM_ARTISTS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAlbumArtists(@org.eclipse.jdt.annotation.NonNull List<NameGuidPair> albumArtists) {
        this.albumArtists = albumArtists;
    }

    public BaseItemDto seasonName(@org.eclipse.jdt.annotation.NonNull String seasonName) {
        this.seasonName = seasonName;
        return this;
    }

    /**
     * Gets or sets the name of the season.
     * 
     * @return seasonName
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_SEASON_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getSeasonName() {
        return seasonName;
    }

    @JsonProperty(value = JSON_PROPERTY_SEASON_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSeasonName(@org.eclipse.jdt.annotation.NonNull String seasonName) {
        this.seasonName = seasonName;
    }

    public BaseItemDto mediaStreams(@org.eclipse.jdt.annotation.NonNull List<MediaStream> mediaStreams) {
        this.mediaStreams = mediaStreams;
        return this;
    }

    public BaseItemDto addMediaStreamsItem(MediaStream mediaStreamsItem) {
        if (this.mediaStreams == null) {
            this.mediaStreams = new ArrayList<>();
        }
        this.mediaStreams.add(mediaStreamsItem);
        return this;
    }

    /**
     * Gets or sets the media streams.
     * 
     * @return mediaStreams
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_MEDIA_STREAMS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<MediaStream> getMediaStreams() {
        return mediaStreams;
    }

    @JsonProperty(value = JSON_PROPERTY_MEDIA_STREAMS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMediaStreams(@org.eclipse.jdt.annotation.NonNull List<MediaStream> mediaStreams) {
        this.mediaStreams = mediaStreams;
    }

    public BaseItemDto videoType(@org.eclipse.jdt.annotation.NonNull VideoType videoType) {
        this.videoType = videoType;
        return this;
    }

    /**
     * Gets or sets the type of the video.
     * 
     * @return videoType
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_VIDEO_TYPE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public VideoType getVideoType() {
        return videoType;
    }

    @JsonProperty(value = JSON_PROPERTY_VIDEO_TYPE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setVideoType(@org.eclipse.jdt.annotation.NonNull VideoType videoType) {
        this.videoType = videoType;
    }

    public BaseItemDto partCount(@org.eclipse.jdt.annotation.NonNull Integer partCount) {
        this.partCount = partCount;
        return this;
    }

    /**
     * Gets or sets the part count.
     * 
     * @return partCount
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_PART_COUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getPartCount() {
        return partCount;
    }

    @JsonProperty(value = JSON_PROPERTY_PART_COUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPartCount(@org.eclipse.jdt.annotation.NonNull Integer partCount) {
        this.partCount = partCount;
    }

    public BaseItemDto mediaSourceCount(@org.eclipse.jdt.annotation.NonNull Integer mediaSourceCount) {
        this.mediaSourceCount = mediaSourceCount;
        return this;
    }

    /**
     * Get mediaSourceCount
     * 
     * @return mediaSourceCount
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_MEDIA_SOURCE_COUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getMediaSourceCount() {
        return mediaSourceCount;
    }

    @JsonProperty(value = JSON_PROPERTY_MEDIA_SOURCE_COUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMediaSourceCount(@org.eclipse.jdt.annotation.NonNull Integer mediaSourceCount) {
        this.mediaSourceCount = mediaSourceCount;
    }

    public BaseItemDto imageTags(@org.eclipse.jdt.annotation.NonNull Map<String, String> imageTags) {
        this.imageTags = imageTags;
        return this;
    }

    public BaseItemDto putImageTagsItem(String key, String imageTagsItem) {
        if (this.imageTags == null) {
            this.imageTags = new HashMap<>();
        }
        this.imageTags.put(key, imageTagsItem);
        return this;
    }

    /**
     * Gets or sets the image tags.
     * 
     * @return imageTags
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_IMAGE_TAGS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Map<String, String> getImageTags() {
        return imageTags;
    }

    @JsonProperty(value = JSON_PROPERTY_IMAGE_TAGS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setImageTags(@org.eclipse.jdt.annotation.NonNull Map<String, String> imageTags) {
        this.imageTags = imageTags;
    }

    public BaseItemDto backdropImageTags(@org.eclipse.jdt.annotation.NonNull List<String> backdropImageTags) {
        this.backdropImageTags = backdropImageTags;
        return this;
    }

    public BaseItemDto addBackdropImageTagsItem(String backdropImageTagsItem) {
        if (this.backdropImageTags == null) {
            this.backdropImageTags = new ArrayList<>();
        }
        this.backdropImageTags.add(backdropImageTagsItem);
        return this;
    }

    /**
     * Gets or sets the backdrop image tags.
     * 
     * @return backdropImageTags
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_BACKDROP_IMAGE_TAGS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<String> getBackdropImageTags() {
        return backdropImageTags;
    }

    @JsonProperty(value = JSON_PROPERTY_BACKDROP_IMAGE_TAGS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setBackdropImageTags(@org.eclipse.jdt.annotation.NonNull List<String> backdropImageTags) {
        this.backdropImageTags = backdropImageTags;
    }

    public BaseItemDto screenshotImageTags(@org.eclipse.jdt.annotation.NonNull List<String> screenshotImageTags) {
        this.screenshotImageTags = screenshotImageTags;
        return this;
    }

    public BaseItemDto addScreenshotImageTagsItem(String screenshotImageTagsItem) {
        if (this.screenshotImageTags == null) {
            this.screenshotImageTags = new ArrayList<>();
        }
        this.screenshotImageTags.add(screenshotImageTagsItem);
        return this;
    }

    /**
     * Gets or sets the screenshot image tags.
     * 
     * @return screenshotImageTags
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_SCREENSHOT_IMAGE_TAGS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<String> getScreenshotImageTags() {
        return screenshotImageTags;
    }

    @JsonProperty(value = JSON_PROPERTY_SCREENSHOT_IMAGE_TAGS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setScreenshotImageTags(@org.eclipse.jdt.annotation.NonNull List<String> screenshotImageTags) {
        this.screenshotImageTags = screenshotImageTags;
    }

    public BaseItemDto parentLogoImageTag(@org.eclipse.jdt.annotation.NonNull String parentLogoImageTag) {
        this.parentLogoImageTag = parentLogoImageTag;
        return this;
    }

    /**
     * Gets or sets the parent logo image tag.
     * 
     * @return parentLogoImageTag
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_PARENT_LOGO_IMAGE_TAG, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getParentLogoImageTag() {
        return parentLogoImageTag;
    }

    @JsonProperty(value = JSON_PROPERTY_PARENT_LOGO_IMAGE_TAG, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setParentLogoImageTag(@org.eclipse.jdt.annotation.NonNull String parentLogoImageTag) {
        this.parentLogoImageTag = parentLogoImageTag;
    }

    public BaseItemDto parentArtItemId(@org.eclipse.jdt.annotation.NonNull UUID parentArtItemId) {
        this.parentArtItemId = parentArtItemId;
        return this;
    }

    /**
     * Gets or sets whether the item has fan art, this will hold the Id of the Parent that has one.
     * 
     * @return parentArtItemId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_PARENT_ART_ITEM_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public UUID getParentArtItemId() {
        return parentArtItemId;
    }

    @JsonProperty(value = JSON_PROPERTY_PARENT_ART_ITEM_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setParentArtItemId(@org.eclipse.jdt.annotation.NonNull UUID parentArtItemId) {
        this.parentArtItemId = parentArtItemId;
    }

    public BaseItemDto parentArtImageTag(@org.eclipse.jdt.annotation.NonNull String parentArtImageTag) {
        this.parentArtImageTag = parentArtImageTag;
        return this;
    }

    /**
     * Gets or sets the parent art image tag.
     * 
     * @return parentArtImageTag
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_PARENT_ART_IMAGE_TAG, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getParentArtImageTag() {
        return parentArtImageTag;
    }

    @JsonProperty(value = JSON_PROPERTY_PARENT_ART_IMAGE_TAG, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setParentArtImageTag(@org.eclipse.jdt.annotation.NonNull String parentArtImageTag) {
        this.parentArtImageTag = parentArtImageTag;
    }

    public BaseItemDto seriesThumbImageTag(@org.eclipse.jdt.annotation.NonNull String seriesThumbImageTag) {
        this.seriesThumbImageTag = seriesThumbImageTag;
        return this;
    }

    /**
     * Gets or sets the series thumb image tag.
     * 
     * @return seriesThumbImageTag
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_SERIES_THUMB_IMAGE_TAG, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getSeriesThumbImageTag() {
        return seriesThumbImageTag;
    }

    @JsonProperty(value = JSON_PROPERTY_SERIES_THUMB_IMAGE_TAG, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSeriesThumbImageTag(@org.eclipse.jdt.annotation.NonNull String seriesThumbImageTag) {
        this.seriesThumbImageTag = seriesThumbImageTag;
    }

    public BaseItemDto imageBlurHashes(@org.eclipse.jdt.annotation.NonNull BaseItemDtoImageBlurHashes imageBlurHashes) {
        this.imageBlurHashes = imageBlurHashes;
        return this;
    }

    /**
     * Get imageBlurHashes
     * 
     * @return imageBlurHashes
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_IMAGE_BLUR_HASHES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public BaseItemDtoImageBlurHashes getImageBlurHashes() {
        return imageBlurHashes;
    }

    @JsonProperty(value = JSON_PROPERTY_IMAGE_BLUR_HASHES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setImageBlurHashes(@org.eclipse.jdt.annotation.NonNull BaseItemDtoImageBlurHashes imageBlurHashes) {
        this.imageBlurHashes = imageBlurHashes;
    }

    public BaseItemDto seriesStudio(@org.eclipse.jdt.annotation.NonNull String seriesStudio) {
        this.seriesStudio = seriesStudio;
        return this;
    }

    /**
     * Gets or sets the series studio.
     * 
     * @return seriesStudio
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_SERIES_STUDIO, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getSeriesStudio() {
        return seriesStudio;
    }

    @JsonProperty(value = JSON_PROPERTY_SERIES_STUDIO, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSeriesStudio(@org.eclipse.jdt.annotation.NonNull String seriesStudio) {
        this.seriesStudio = seriesStudio;
    }

    public BaseItemDto parentThumbItemId(@org.eclipse.jdt.annotation.NonNull UUID parentThumbItemId) {
        this.parentThumbItemId = parentThumbItemId;
        return this;
    }

    /**
     * Gets or sets the parent thumb item id.
     * 
     * @return parentThumbItemId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_PARENT_THUMB_ITEM_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public UUID getParentThumbItemId() {
        return parentThumbItemId;
    }

    @JsonProperty(value = JSON_PROPERTY_PARENT_THUMB_ITEM_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setParentThumbItemId(@org.eclipse.jdt.annotation.NonNull UUID parentThumbItemId) {
        this.parentThumbItemId = parentThumbItemId;
    }

    public BaseItemDto parentThumbImageTag(@org.eclipse.jdt.annotation.NonNull String parentThumbImageTag) {
        this.parentThumbImageTag = parentThumbImageTag;
        return this;
    }

    /**
     * Gets or sets the parent thumb image tag.
     * 
     * @return parentThumbImageTag
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_PARENT_THUMB_IMAGE_TAG, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getParentThumbImageTag() {
        return parentThumbImageTag;
    }

    @JsonProperty(value = JSON_PROPERTY_PARENT_THUMB_IMAGE_TAG, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setParentThumbImageTag(@org.eclipse.jdt.annotation.NonNull String parentThumbImageTag) {
        this.parentThumbImageTag = parentThumbImageTag;
    }

    public BaseItemDto parentPrimaryImageItemId(@org.eclipse.jdt.annotation.NonNull UUID parentPrimaryImageItemId) {
        this.parentPrimaryImageItemId = parentPrimaryImageItemId;
        return this;
    }

    /**
     * Gets or sets the parent primary image item identifier.
     * 
     * @return parentPrimaryImageItemId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_PARENT_PRIMARY_IMAGE_ITEM_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public UUID getParentPrimaryImageItemId() {
        return parentPrimaryImageItemId;
    }

    @JsonProperty(value = JSON_PROPERTY_PARENT_PRIMARY_IMAGE_ITEM_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setParentPrimaryImageItemId(@org.eclipse.jdt.annotation.NonNull UUID parentPrimaryImageItemId) {
        this.parentPrimaryImageItemId = parentPrimaryImageItemId;
    }

    public BaseItemDto parentPrimaryImageTag(@org.eclipse.jdt.annotation.NonNull String parentPrimaryImageTag) {
        this.parentPrimaryImageTag = parentPrimaryImageTag;
        return this;
    }

    /**
     * Gets or sets the parent primary image tag.
     * 
     * @return parentPrimaryImageTag
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_PARENT_PRIMARY_IMAGE_TAG, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getParentPrimaryImageTag() {
        return parentPrimaryImageTag;
    }

    @JsonProperty(value = JSON_PROPERTY_PARENT_PRIMARY_IMAGE_TAG, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setParentPrimaryImageTag(@org.eclipse.jdt.annotation.NonNull String parentPrimaryImageTag) {
        this.parentPrimaryImageTag = parentPrimaryImageTag;
    }

    public BaseItemDto chapters(@org.eclipse.jdt.annotation.NonNull List<ChapterInfo> chapters) {
        this.chapters = chapters;
        return this;
    }

    public BaseItemDto addChaptersItem(ChapterInfo chaptersItem) {
        if (this.chapters == null) {
            this.chapters = new ArrayList<>();
        }
        this.chapters.add(chaptersItem);
        return this;
    }

    /**
     * Gets or sets the chapters.
     * 
     * @return chapters
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_CHAPTERS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<ChapterInfo> getChapters() {
        return chapters;
    }

    @JsonProperty(value = JSON_PROPERTY_CHAPTERS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setChapters(@org.eclipse.jdt.annotation.NonNull List<ChapterInfo> chapters) {
        this.chapters = chapters;
    }

    public BaseItemDto trickplay(
            @org.eclipse.jdt.annotation.NonNull Map<String, Map<String, TrickplayInfoDto>> trickplay) {
        this.trickplay = trickplay;
        return this;
    }

    public BaseItemDto putTrickplayItem(String key, Map<String, TrickplayInfoDto> trickplayItem) {
        if (this.trickplay == null) {
            this.trickplay = new HashMap<>();
        }
        this.trickplay.put(key, trickplayItem);
        return this;
    }

    /**
     * Gets or sets the trickplay manifest.
     * 
     * @return trickplay
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_TRICKPLAY, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Map<String, Map<String, TrickplayInfoDto>> getTrickplay() {
        return trickplay;
    }

    @JsonProperty(value = JSON_PROPERTY_TRICKPLAY, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTrickplay(@org.eclipse.jdt.annotation.NonNull Map<String, Map<String, TrickplayInfoDto>> trickplay) {
        this.trickplay = trickplay;
    }

    public BaseItemDto locationType(@org.eclipse.jdt.annotation.NonNull LocationType locationType) {
        this.locationType = locationType;
        return this;
    }

    /**
     * Gets or sets the type of the location.
     * 
     * @return locationType
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_LOCATION_TYPE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public LocationType getLocationType() {
        return locationType;
    }

    @JsonProperty(value = JSON_PROPERTY_LOCATION_TYPE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLocationType(@org.eclipse.jdt.annotation.NonNull LocationType locationType) {
        this.locationType = locationType;
    }

    public BaseItemDto isoType(@org.eclipse.jdt.annotation.NonNull IsoType isoType) {
        this.isoType = isoType;
        return this;
    }

    /**
     * Gets or sets the type of the iso.
     * 
     * @return isoType
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ISO_TYPE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public IsoType getIsoType() {
        return isoType;
    }

    @JsonProperty(value = JSON_PROPERTY_ISO_TYPE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsoType(@org.eclipse.jdt.annotation.NonNull IsoType isoType) {
        this.isoType = isoType;
    }

    public BaseItemDto mediaType(@org.eclipse.jdt.annotation.NonNull MediaType mediaType) {
        this.mediaType = mediaType;
        return this;
    }

    /**
     * Gets or sets the type of the media.
     * 
     * @return mediaType
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_MEDIA_TYPE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public MediaType getMediaType() {
        return mediaType;
    }

    @JsonProperty(value = JSON_PROPERTY_MEDIA_TYPE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMediaType(@org.eclipse.jdt.annotation.NonNull MediaType mediaType) {
        this.mediaType = mediaType;
    }

    public BaseItemDto endDate(@org.eclipse.jdt.annotation.NonNull OffsetDateTime endDate) {
        this.endDate = endDate;
        return this;
    }

    /**
     * Gets or sets the end date.
     * 
     * @return endDate
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_END_DATE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public OffsetDateTime getEndDate() {
        return endDate;
    }

    @JsonProperty(value = JSON_PROPERTY_END_DATE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEndDate(@org.eclipse.jdt.annotation.NonNull OffsetDateTime endDate) {
        this.endDate = endDate;
    }

    public BaseItemDto lockedFields(@org.eclipse.jdt.annotation.NonNull List<MetadataField> lockedFields) {
        this.lockedFields = lockedFields;
        return this;
    }

    public BaseItemDto addLockedFieldsItem(MetadataField lockedFieldsItem) {
        if (this.lockedFields == null) {
            this.lockedFields = new ArrayList<>();
        }
        this.lockedFields.add(lockedFieldsItem);
        return this;
    }

    /**
     * Gets or sets the locked fields.
     * 
     * @return lockedFields
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_LOCKED_FIELDS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<MetadataField> getLockedFields() {
        return lockedFields;
    }

    @JsonProperty(value = JSON_PROPERTY_LOCKED_FIELDS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLockedFields(@org.eclipse.jdt.annotation.NonNull List<MetadataField> lockedFields) {
        this.lockedFields = lockedFields;
    }

    public BaseItemDto trailerCount(@org.eclipse.jdt.annotation.NonNull Integer trailerCount) {
        this.trailerCount = trailerCount;
        return this;
    }

    /**
     * Gets or sets the trailer count.
     * 
     * @return trailerCount
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_TRAILER_COUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getTrailerCount() {
        return trailerCount;
    }

    @JsonProperty(value = JSON_PROPERTY_TRAILER_COUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTrailerCount(@org.eclipse.jdt.annotation.NonNull Integer trailerCount) {
        this.trailerCount = trailerCount;
    }

    public BaseItemDto movieCount(@org.eclipse.jdt.annotation.NonNull Integer movieCount) {
        this.movieCount = movieCount;
        return this;
    }

    /**
     * Gets or sets the movie count.
     * 
     * @return movieCount
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_MOVIE_COUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getMovieCount() {
        return movieCount;
    }

    @JsonProperty(value = JSON_PROPERTY_MOVIE_COUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMovieCount(@org.eclipse.jdt.annotation.NonNull Integer movieCount) {
        this.movieCount = movieCount;
    }

    public BaseItemDto seriesCount(@org.eclipse.jdt.annotation.NonNull Integer seriesCount) {
        this.seriesCount = seriesCount;
        return this;
    }

    /**
     * Gets or sets the series count.
     * 
     * @return seriesCount
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_SERIES_COUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getSeriesCount() {
        return seriesCount;
    }

    @JsonProperty(value = JSON_PROPERTY_SERIES_COUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSeriesCount(@org.eclipse.jdt.annotation.NonNull Integer seriesCount) {
        this.seriesCount = seriesCount;
    }

    public BaseItemDto programCount(@org.eclipse.jdt.annotation.NonNull Integer programCount) {
        this.programCount = programCount;
        return this;
    }

    /**
     * Get programCount
     * 
     * @return programCount
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_PROGRAM_COUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getProgramCount() {
        return programCount;
    }

    @JsonProperty(value = JSON_PROPERTY_PROGRAM_COUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setProgramCount(@org.eclipse.jdt.annotation.NonNull Integer programCount) {
        this.programCount = programCount;
    }

    public BaseItemDto episodeCount(@org.eclipse.jdt.annotation.NonNull Integer episodeCount) {
        this.episodeCount = episodeCount;
        return this;
    }

    /**
     * Gets or sets the episode count.
     * 
     * @return episodeCount
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_EPISODE_COUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getEpisodeCount() {
        return episodeCount;
    }

    @JsonProperty(value = JSON_PROPERTY_EPISODE_COUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEpisodeCount(@org.eclipse.jdt.annotation.NonNull Integer episodeCount) {
        this.episodeCount = episodeCount;
    }

    public BaseItemDto songCount(@org.eclipse.jdt.annotation.NonNull Integer songCount) {
        this.songCount = songCount;
        return this;
    }

    /**
     * Gets or sets the song count.
     * 
     * @return songCount
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_SONG_COUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getSongCount() {
        return songCount;
    }

    @JsonProperty(value = JSON_PROPERTY_SONG_COUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSongCount(@org.eclipse.jdt.annotation.NonNull Integer songCount) {
        this.songCount = songCount;
    }

    public BaseItemDto albumCount(@org.eclipse.jdt.annotation.NonNull Integer albumCount) {
        this.albumCount = albumCount;
        return this;
    }

    /**
     * Gets or sets the album count.
     * 
     * @return albumCount
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ALBUM_COUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getAlbumCount() {
        return albumCount;
    }

    @JsonProperty(value = JSON_PROPERTY_ALBUM_COUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAlbumCount(@org.eclipse.jdt.annotation.NonNull Integer albumCount) {
        this.albumCount = albumCount;
    }

    public BaseItemDto artistCount(@org.eclipse.jdt.annotation.NonNull Integer artistCount) {
        this.artistCount = artistCount;
        return this;
    }

    /**
     * Get artistCount
     * 
     * @return artistCount
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ARTIST_COUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getArtistCount() {
        return artistCount;
    }

    @JsonProperty(value = JSON_PROPERTY_ARTIST_COUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setArtistCount(@org.eclipse.jdt.annotation.NonNull Integer artistCount) {
        this.artistCount = artistCount;
    }

    public BaseItemDto musicVideoCount(@org.eclipse.jdt.annotation.NonNull Integer musicVideoCount) {
        this.musicVideoCount = musicVideoCount;
        return this;
    }

    /**
     * Gets or sets the music video count.
     * 
     * @return musicVideoCount
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_MUSIC_VIDEO_COUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getMusicVideoCount() {
        return musicVideoCount;
    }

    @JsonProperty(value = JSON_PROPERTY_MUSIC_VIDEO_COUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMusicVideoCount(@org.eclipse.jdt.annotation.NonNull Integer musicVideoCount) {
        this.musicVideoCount = musicVideoCount;
    }

    public BaseItemDto lockData(@org.eclipse.jdt.annotation.NonNull Boolean lockData) {
        this.lockData = lockData;
        return this;
    }

    /**
     * Gets or sets a value indicating whether [enable internet providers].
     * 
     * @return lockData
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_LOCK_DATA, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getLockData() {
        return lockData;
    }

    @JsonProperty(value = JSON_PROPERTY_LOCK_DATA, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLockData(@org.eclipse.jdt.annotation.NonNull Boolean lockData) {
        this.lockData = lockData;
    }

    public BaseItemDto width(@org.eclipse.jdt.annotation.NonNull Integer width) {
        this.width = width;
        return this;
    }

    /**
     * Get width
     * 
     * @return width
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_WIDTH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getWidth() {
        return width;
    }

    @JsonProperty(value = JSON_PROPERTY_WIDTH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setWidth(@org.eclipse.jdt.annotation.NonNull Integer width) {
        this.width = width;
    }

    public BaseItemDto height(@org.eclipse.jdt.annotation.NonNull Integer height) {
        this.height = height;
        return this;
    }

    /**
     * Get height
     * 
     * @return height
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_HEIGHT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getHeight() {
        return height;
    }

    @JsonProperty(value = JSON_PROPERTY_HEIGHT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setHeight(@org.eclipse.jdt.annotation.NonNull Integer height) {
        this.height = height;
    }

    public BaseItemDto cameraMake(@org.eclipse.jdt.annotation.NonNull String cameraMake) {
        this.cameraMake = cameraMake;
        return this;
    }

    /**
     * Get cameraMake
     * 
     * @return cameraMake
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_CAMERA_MAKE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getCameraMake() {
        return cameraMake;
    }

    @JsonProperty(value = JSON_PROPERTY_CAMERA_MAKE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCameraMake(@org.eclipse.jdt.annotation.NonNull String cameraMake) {
        this.cameraMake = cameraMake;
    }

    public BaseItemDto cameraModel(@org.eclipse.jdt.annotation.NonNull String cameraModel) {
        this.cameraModel = cameraModel;
        return this;
    }

    /**
     * Get cameraModel
     * 
     * @return cameraModel
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_CAMERA_MODEL, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getCameraModel() {
        return cameraModel;
    }

    @JsonProperty(value = JSON_PROPERTY_CAMERA_MODEL, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCameraModel(@org.eclipse.jdt.annotation.NonNull String cameraModel) {
        this.cameraModel = cameraModel;
    }

    public BaseItemDto software(@org.eclipse.jdt.annotation.NonNull String software) {
        this.software = software;
        return this;
    }

    /**
     * Get software
     * 
     * @return software
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_SOFTWARE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getSoftware() {
        return software;
    }

    @JsonProperty(value = JSON_PROPERTY_SOFTWARE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSoftware(@org.eclipse.jdt.annotation.NonNull String software) {
        this.software = software;
    }

    public BaseItemDto exposureTime(@org.eclipse.jdt.annotation.NonNull Double exposureTime) {
        this.exposureTime = exposureTime;
        return this;
    }

    /**
     * Get exposureTime
     * 
     * @return exposureTime
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_EXPOSURE_TIME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Double getExposureTime() {
        return exposureTime;
    }

    @JsonProperty(value = JSON_PROPERTY_EXPOSURE_TIME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setExposureTime(@org.eclipse.jdt.annotation.NonNull Double exposureTime) {
        this.exposureTime = exposureTime;
    }

    public BaseItemDto focalLength(@org.eclipse.jdt.annotation.NonNull Double focalLength) {
        this.focalLength = focalLength;
        return this;
    }

    /**
     * Get focalLength
     * 
     * @return focalLength
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_FOCAL_LENGTH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Double getFocalLength() {
        return focalLength;
    }

    @JsonProperty(value = JSON_PROPERTY_FOCAL_LENGTH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setFocalLength(@org.eclipse.jdt.annotation.NonNull Double focalLength) {
        this.focalLength = focalLength;
    }

    public BaseItemDto imageOrientation(@org.eclipse.jdt.annotation.NonNull ImageOrientation imageOrientation) {
        this.imageOrientation = imageOrientation;
        return this;
    }

    /**
     * Get imageOrientation
     * 
     * @return imageOrientation
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_IMAGE_ORIENTATION, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public ImageOrientation getImageOrientation() {
        return imageOrientation;
    }

    @JsonProperty(value = JSON_PROPERTY_IMAGE_ORIENTATION, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setImageOrientation(@org.eclipse.jdt.annotation.NonNull ImageOrientation imageOrientation) {
        this.imageOrientation = imageOrientation;
    }

    public BaseItemDto aperture(@org.eclipse.jdt.annotation.NonNull Double aperture) {
        this.aperture = aperture;
        return this;
    }

    /**
     * Get aperture
     * 
     * @return aperture
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_APERTURE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Double getAperture() {
        return aperture;
    }

    @JsonProperty(value = JSON_PROPERTY_APERTURE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAperture(@org.eclipse.jdt.annotation.NonNull Double aperture) {
        this.aperture = aperture;
    }

    public BaseItemDto shutterSpeed(@org.eclipse.jdt.annotation.NonNull Double shutterSpeed) {
        this.shutterSpeed = shutterSpeed;
        return this;
    }

    /**
     * Get shutterSpeed
     * 
     * @return shutterSpeed
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_SHUTTER_SPEED, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Double getShutterSpeed() {
        return shutterSpeed;
    }

    @JsonProperty(value = JSON_PROPERTY_SHUTTER_SPEED, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setShutterSpeed(@org.eclipse.jdt.annotation.NonNull Double shutterSpeed) {
        this.shutterSpeed = shutterSpeed;
    }

    public BaseItemDto latitude(@org.eclipse.jdt.annotation.NonNull Double latitude) {
        this.latitude = latitude;
        return this;
    }

    /**
     * Get latitude
     * 
     * @return latitude
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_LATITUDE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Double getLatitude() {
        return latitude;
    }

    @JsonProperty(value = JSON_PROPERTY_LATITUDE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLatitude(@org.eclipse.jdt.annotation.NonNull Double latitude) {
        this.latitude = latitude;
    }

    public BaseItemDto longitude(@org.eclipse.jdt.annotation.NonNull Double longitude) {
        this.longitude = longitude;
        return this;
    }

    /**
     * Get longitude
     * 
     * @return longitude
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_LONGITUDE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Double getLongitude() {
        return longitude;
    }

    @JsonProperty(value = JSON_PROPERTY_LONGITUDE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLongitude(@org.eclipse.jdt.annotation.NonNull Double longitude) {
        this.longitude = longitude;
    }

    public BaseItemDto altitude(@org.eclipse.jdt.annotation.NonNull Double altitude) {
        this.altitude = altitude;
        return this;
    }

    /**
     * Get altitude
     * 
     * @return altitude
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ALTITUDE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Double getAltitude() {
        return altitude;
    }

    @JsonProperty(value = JSON_PROPERTY_ALTITUDE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAltitude(@org.eclipse.jdt.annotation.NonNull Double altitude) {
        this.altitude = altitude;
    }

    public BaseItemDto isoSpeedRating(@org.eclipse.jdt.annotation.NonNull Integer isoSpeedRating) {
        this.isoSpeedRating = isoSpeedRating;
        return this;
    }

    /**
     * Get isoSpeedRating
     * 
     * @return isoSpeedRating
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ISO_SPEED_RATING, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getIsoSpeedRating() {
        return isoSpeedRating;
    }

    @JsonProperty(value = JSON_PROPERTY_ISO_SPEED_RATING, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsoSpeedRating(@org.eclipse.jdt.annotation.NonNull Integer isoSpeedRating) {
        this.isoSpeedRating = isoSpeedRating;
    }

    public BaseItemDto seriesTimerId(@org.eclipse.jdt.annotation.NonNull String seriesTimerId) {
        this.seriesTimerId = seriesTimerId;
        return this;
    }

    /**
     * Gets or sets the series timer identifier.
     * 
     * @return seriesTimerId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_SERIES_TIMER_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getSeriesTimerId() {
        return seriesTimerId;
    }

    @JsonProperty(value = JSON_PROPERTY_SERIES_TIMER_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSeriesTimerId(@org.eclipse.jdt.annotation.NonNull String seriesTimerId) {
        this.seriesTimerId = seriesTimerId;
    }

    public BaseItemDto programId(@org.eclipse.jdt.annotation.NonNull String programId) {
        this.programId = programId;
        return this;
    }

    /**
     * Gets or sets the program identifier.
     * 
     * @return programId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_PROGRAM_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getProgramId() {
        return programId;
    }

    @JsonProperty(value = JSON_PROPERTY_PROGRAM_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setProgramId(@org.eclipse.jdt.annotation.NonNull String programId) {
        this.programId = programId;
    }

    public BaseItemDto channelPrimaryImageTag(@org.eclipse.jdt.annotation.NonNull String channelPrimaryImageTag) {
        this.channelPrimaryImageTag = channelPrimaryImageTag;
        return this;
    }

    /**
     * Gets or sets the channel primary image tag.
     * 
     * @return channelPrimaryImageTag
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_CHANNEL_PRIMARY_IMAGE_TAG, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getChannelPrimaryImageTag() {
        return channelPrimaryImageTag;
    }

    @JsonProperty(value = JSON_PROPERTY_CHANNEL_PRIMARY_IMAGE_TAG, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setChannelPrimaryImageTag(@org.eclipse.jdt.annotation.NonNull String channelPrimaryImageTag) {
        this.channelPrimaryImageTag = channelPrimaryImageTag;
    }

    public BaseItemDto startDate(@org.eclipse.jdt.annotation.NonNull OffsetDateTime startDate) {
        this.startDate = startDate;
        return this;
    }

    /**
     * Gets or sets the start date of the recording, in UTC.
     * 
     * @return startDate
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_START_DATE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public OffsetDateTime getStartDate() {
        return startDate;
    }

    @JsonProperty(value = JSON_PROPERTY_START_DATE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setStartDate(@org.eclipse.jdt.annotation.NonNull OffsetDateTime startDate) {
        this.startDate = startDate;
    }

    public BaseItemDto completionPercentage(@org.eclipse.jdt.annotation.NonNull Double completionPercentage) {
        this.completionPercentage = completionPercentage;
        return this;
    }

    /**
     * Gets or sets the completion percentage.
     * 
     * @return completionPercentage
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_COMPLETION_PERCENTAGE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Double getCompletionPercentage() {
        return completionPercentage;
    }

    @JsonProperty(value = JSON_PROPERTY_COMPLETION_PERCENTAGE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCompletionPercentage(@org.eclipse.jdt.annotation.NonNull Double completionPercentage) {
        this.completionPercentage = completionPercentage;
    }

    public BaseItemDto isRepeat(@org.eclipse.jdt.annotation.NonNull Boolean isRepeat) {
        this.isRepeat = isRepeat;
        return this;
    }

    /**
     * Gets or sets a value indicating whether this instance is repeat.
     * 
     * @return isRepeat
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_IS_REPEAT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getIsRepeat() {
        return isRepeat;
    }

    @JsonProperty(value = JSON_PROPERTY_IS_REPEAT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsRepeat(@org.eclipse.jdt.annotation.NonNull Boolean isRepeat) {
        this.isRepeat = isRepeat;
    }

    public BaseItemDto episodeTitle(@org.eclipse.jdt.annotation.NonNull String episodeTitle) {
        this.episodeTitle = episodeTitle;
        return this;
    }

    /**
     * Gets or sets the episode title.
     * 
     * @return episodeTitle
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_EPISODE_TITLE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getEpisodeTitle() {
        return episodeTitle;
    }

    @JsonProperty(value = JSON_PROPERTY_EPISODE_TITLE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEpisodeTitle(@org.eclipse.jdt.annotation.NonNull String episodeTitle) {
        this.episodeTitle = episodeTitle;
    }

    public BaseItemDto channelType(@org.eclipse.jdt.annotation.NonNull ChannelType channelType) {
        this.channelType = channelType;
        return this;
    }

    /**
     * Gets or sets the type of the channel.
     * 
     * @return channelType
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_CHANNEL_TYPE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public ChannelType getChannelType() {
        return channelType;
    }

    @JsonProperty(value = JSON_PROPERTY_CHANNEL_TYPE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setChannelType(@org.eclipse.jdt.annotation.NonNull ChannelType channelType) {
        this.channelType = channelType;
    }

    public BaseItemDto audio(@org.eclipse.jdt.annotation.NonNull ProgramAudio audio) {
        this.audio = audio;
        return this;
    }

    /**
     * Gets or sets the audio.
     * 
     * @return audio
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_AUDIO, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public ProgramAudio getAudio() {
        return audio;
    }

    @JsonProperty(value = JSON_PROPERTY_AUDIO, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAudio(@org.eclipse.jdt.annotation.NonNull ProgramAudio audio) {
        this.audio = audio;
    }

    public BaseItemDto isMovie(@org.eclipse.jdt.annotation.NonNull Boolean isMovie) {
        this.isMovie = isMovie;
        return this;
    }

    /**
     * Gets or sets a value indicating whether this instance is movie.
     * 
     * @return isMovie
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_IS_MOVIE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getIsMovie() {
        return isMovie;
    }

    @JsonProperty(value = JSON_PROPERTY_IS_MOVIE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsMovie(@org.eclipse.jdt.annotation.NonNull Boolean isMovie) {
        this.isMovie = isMovie;
    }

    public BaseItemDto isSports(@org.eclipse.jdt.annotation.NonNull Boolean isSports) {
        this.isSports = isSports;
        return this;
    }

    /**
     * Gets or sets a value indicating whether this instance is sports.
     * 
     * @return isSports
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_IS_SPORTS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getIsSports() {
        return isSports;
    }

    @JsonProperty(value = JSON_PROPERTY_IS_SPORTS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsSports(@org.eclipse.jdt.annotation.NonNull Boolean isSports) {
        this.isSports = isSports;
    }

    public BaseItemDto isSeries(@org.eclipse.jdt.annotation.NonNull Boolean isSeries) {
        this.isSeries = isSeries;
        return this;
    }

    /**
     * Gets or sets a value indicating whether this instance is series.
     * 
     * @return isSeries
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_IS_SERIES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getIsSeries() {
        return isSeries;
    }

    @JsonProperty(value = JSON_PROPERTY_IS_SERIES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsSeries(@org.eclipse.jdt.annotation.NonNull Boolean isSeries) {
        this.isSeries = isSeries;
    }

    public BaseItemDto isLive(@org.eclipse.jdt.annotation.NonNull Boolean isLive) {
        this.isLive = isLive;
        return this;
    }

    /**
     * Gets or sets a value indicating whether this instance is live.
     * 
     * @return isLive
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_IS_LIVE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getIsLive() {
        return isLive;
    }

    @JsonProperty(value = JSON_PROPERTY_IS_LIVE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsLive(@org.eclipse.jdt.annotation.NonNull Boolean isLive) {
        this.isLive = isLive;
    }

    public BaseItemDto isNews(@org.eclipse.jdt.annotation.NonNull Boolean isNews) {
        this.isNews = isNews;
        return this;
    }

    /**
     * Gets or sets a value indicating whether this instance is news.
     * 
     * @return isNews
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_IS_NEWS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getIsNews() {
        return isNews;
    }

    @JsonProperty(value = JSON_PROPERTY_IS_NEWS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsNews(@org.eclipse.jdt.annotation.NonNull Boolean isNews) {
        this.isNews = isNews;
    }

    public BaseItemDto isKids(@org.eclipse.jdt.annotation.NonNull Boolean isKids) {
        this.isKids = isKids;
        return this;
    }

    /**
     * Gets or sets a value indicating whether this instance is kids.
     * 
     * @return isKids
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_IS_KIDS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getIsKids() {
        return isKids;
    }

    @JsonProperty(value = JSON_PROPERTY_IS_KIDS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsKids(@org.eclipse.jdt.annotation.NonNull Boolean isKids) {
        this.isKids = isKids;
    }

    public BaseItemDto isPremiere(@org.eclipse.jdt.annotation.NonNull Boolean isPremiere) {
        this.isPremiere = isPremiere;
        return this;
    }

    /**
     * Gets or sets a value indicating whether this instance is premiere.
     * 
     * @return isPremiere
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_IS_PREMIERE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getIsPremiere() {
        return isPremiere;
    }

    @JsonProperty(value = JSON_PROPERTY_IS_PREMIERE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsPremiere(@org.eclipse.jdt.annotation.NonNull Boolean isPremiere) {
        this.isPremiere = isPremiere;
    }

    public BaseItemDto timerId(@org.eclipse.jdt.annotation.NonNull String timerId) {
        this.timerId = timerId;
        return this;
    }

    /**
     * Gets or sets the timer identifier.
     * 
     * @return timerId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_TIMER_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getTimerId() {
        return timerId;
    }

    @JsonProperty(value = JSON_PROPERTY_TIMER_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTimerId(@org.eclipse.jdt.annotation.NonNull String timerId) {
        this.timerId = timerId;
    }

    public BaseItemDto normalizationGain(@org.eclipse.jdt.annotation.NonNull Float normalizationGain) {
        this.normalizationGain = normalizationGain;
        return this;
    }

    /**
     * Gets or sets the gain required for audio normalization.
     * 
     * @return normalizationGain
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_NORMALIZATION_GAIN, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Float getNormalizationGain() {
        return normalizationGain;
    }

    @JsonProperty(value = JSON_PROPERTY_NORMALIZATION_GAIN, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setNormalizationGain(@org.eclipse.jdt.annotation.NonNull Float normalizationGain) {
        this.normalizationGain = normalizationGain;
    }

    public BaseItemDto currentProgram(@org.eclipse.jdt.annotation.NonNull BaseItemDto currentProgram) {
        this.currentProgram = currentProgram;
        return this;
    }

    /**
     * Gets or sets the current program.
     * 
     * @return currentProgram
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_CURRENT_PROGRAM, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public BaseItemDto getCurrentProgram() {
        return currentProgram;
    }

    @JsonProperty(value = JSON_PROPERTY_CURRENT_PROGRAM, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCurrentProgram(@org.eclipse.jdt.annotation.NonNull BaseItemDto currentProgram) {
        this.currentProgram = currentProgram;
    }

    /**
     * Return true if this BaseItemDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BaseItemDto baseItemDto = (BaseItemDto) o;
        return Objects.equals(this.name, baseItemDto.name)
                && Objects.equals(this.originalTitle, baseItemDto.originalTitle)
                && Objects.equals(this.serverId, baseItemDto.serverId) && Objects.equals(this.id, baseItemDto.id)
                && Objects.equals(this.etag, baseItemDto.etag)
                && Objects.equals(this.sourceType, baseItemDto.sourceType)
                && Objects.equals(this.playlistItemId, baseItemDto.playlistItemId)
                && Objects.equals(this.dateCreated, baseItemDto.dateCreated)
                && Objects.equals(this.dateLastMediaAdded, baseItemDto.dateLastMediaAdded)
                && Objects.equals(this.extraType, baseItemDto.extraType)
                && Objects.equals(this.airsBeforeSeasonNumber, baseItemDto.airsBeforeSeasonNumber)
                && Objects.equals(this.airsAfterSeasonNumber, baseItemDto.airsAfterSeasonNumber)
                && Objects.equals(this.airsBeforeEpisodeNumber, baseItemDto.airsBeforeEpisodeNumber)
                && Objects.equals(this.canDelete, baseItemDto.canDelete)
                && Objects.equals(this.canDownload, baseItemDto.canDownload)
                && Objects.equals(this.hasLyrics, baseItemDto.hasLyrics)
                && Objects.equals(this.hasSubtitles, baseItemDto.hasSubtitles)
                && Objects.equals(this.preferredMetadataLanguage, baseItemDto.preferredMetadataLanguage)
                && Objects.equals(this.preferredMetadataCountryCode, baseItemDto.preferredMetadataCountryCode)
                && Objects.equals(this.container, baseItemDto.container)
                && Objects.equals(this.sortName, baseItemDto.sortName)
                && Objects.equals(this.forcedSortName, baseItemDto.forcedSortName)
                && Objects.equals(this.video3DFormat, baseItemDto.video3DFormat)
                && Objects.equals(this.premiereDate, baseItemDto.premiereDate)
                && Objects.equals(this.externalUrls, baseItemDto.externalUrls)
                && Objects.equals(this.mediaSources, baseItemDto.mediaSources)
                && Objects.equals(this.criticRating, baseItemDto.criticRating)
                && Objects.equals(this.productionLocations, baseItemDto.productionLocations)
                && Objects.equals(this.path, baseItemDto.path)
                && Objects.equals(this.enableMediaSourceDisplay, baseItemDto.enableMediaSourceDisplay)
                && Objects.equals(this.officialRating, baseItemDto.officialRating)
                && Objects.equals(this.customRating, baseItemDto.customRating)
                && Objects.equals(this.channelId, baseItemDto.channelId)
                && Objects.equals(this.channelName, baseItemDto.channelName)
                && Objects.equals(this.overview, baseItemDto.overview)
                && Objects.equals(this.taglines, baseItemDto.taglines)
                && Objects.equals(this.genres, baseItemDto.genres)
                && Objects.equals(this.communityRating, baseItemDto.communityRating)
                && Objects.equals(this.cumulativeRunTimeTicks, baseItemDto.cumulativeRunTimeTicks)
                && Objects.equals(this.runTimeTicks, baseItemDto.runTimeTicks)
                && Objects.equals(this.playAccess, baseItemDto.playAccess)
                && Objects.equals(this.aspectRatio, baseItemDto.aspectRatio)
                && Objects.equals(this.productionYear, baseItemDto.productionYear)
                && Objects.equals(this.isPlaceHolder, baseItemDto.isPlaceHolder)
                && Objects.equals(this.number, baseItemDto.number)
                && Objects.equals(this.channelNumber, baseItemDto.channelNumber)
                && Objects.equals(this.indexNumber, baseItemDto.indexNumber)
                && Objects.equals(this.indexNumberEnd, baseItemDto.indexNumberEnd)
                && Objects.equals(this.parentIndexNumber, baseItemDto.parentIndexNumber)
                && Objects.equals(this.remoteTrailers, baseItemDto.remoteTrailers)
                && Objects.equals(this.providerIds, baseItemDto.providerIds)
                && Objects.equals(this.isHD, baseItemDto.isHD) && Objects.equals(this.isFolder, baseItemDto.isFolder)
                && Objects.equals(this.parentId, baseItemDto.parentId) && Objects.equals(this.type, baseItemDto.type)
                && Objects.equals(this.people, baseItemDto.people) && Objects.equals(this.studios, baseItemDto.studios)
                && Objects.equals(this.genreItems, baseItemDto.genreItems)
                && Objects.equals(this.parentLogoItemId, baseItemDto.parentLogoItemId)
                && Objects.equals(this.parentBackdropItemId, baseItemDto.parentBackdropItemId)
                && Objects.equals(this.parentBackdropImageTags, baseItemDto.parentBackdropImageTags)
                && Objects.equals(this.localTrailerCount, baseItemDto.localTrailerCount)
                && Objects.equals(this.userData, baseItemDto.userData)
                && Objects.equals(this.recursiveItemCount, baseItemDto.recursiveItemCount)
                && Objects.equals(this.childCount, baseItemDto.childCount)
                && Objects.equals(this.seriesName, baseItemDto.seriesName)
                && Objects.equals(this.seriesId, baseItemDto.seriesId)
                && Objects.equals(this.seasonId, baseItemDto.seasonId)
                && Objects.equals(this.specialFeatureCount, baseItemDto.specialFeatureCount)
                && Objects.equals(this.displayPreferencesId, baseItemDto.displayPreferencesId)
                && Objects.equals(this.status, baseItemDto.status) && Objects.equals(this.airTime, baseItemDto.airTime)
                && Objects.equals(this.airDays, baseItemDto.airDays) && Objects.equals(this.tags, baseItemDto.tags)
                && Objects.equals(this.primaryImageAspectRatio, baseItemDto.primaryImageAspectRatio)
                && Objects.equals(this.artists, baseItemDto.artists)
                && Objects.equals(this.artistItems, baseItemDto.artistItems)
                && Objects.equals(this.album, baseItemDto.album)
                && Objects.equals(this.collectionType, baseItemDto.collectionType)
                && Objects.equals(this.displayOrder, baseItemDto.displayOrder)
                && Objects.equals(this.albumId, baseItemDto.albumId)
                && Objects.equals(this.albumPrimaryImageTag, baseItemDto.albumPrimaryImageTag)
                && Objects.equals(this.seriesPrimaryImageTag, baseItemDto.seriesPrimaryImageTag)
                && Objects.equals(this.albumArtist, baseItemDto.albumArtist)
                && Objects.equals(this.albumArtists, baseItemDto.albumArtists)
                && Objects.equals(this.seasonName, baseItemDto.seasonName)
                && Objects.equals(this.mediaStreams, baseItemDto.mediaStreams)
                && Objects.equals(this.videoType, baseItemDto.videoType)
                && Objects.equals(this.partCount, baseItemDto.partCount)
                && Objects.equals(this.mediaSourceCount, baseItemDto.mediaSourceCount)
                && Objects.equals(this.imageTags, baseItemDto.imageTags)
                && Objects.equals(this.backdropImageTags, baseItemDto.backdropImageTags)
                && Objects.equals(this.screenshotImageTags, baseItemDto.screenshotImageTags)
                && Objects.equals(this.parentLogoImageTag, baseItemDto.parentLogoImageTag)
                && Objects.equals(this.parentArtItemId, baseItemDto.parentArtItemId)
                && Objects.equals(this.parentArtImageTag, baseItemDto.parentArtImageTag)
                && Objects.equals(this.seriesThumbImageTag, baseItemDto.seriesThumbImageTag)
                && Objects.equals(this.imageBlurHashes, baseItemDto.imageBlurHashes)
                && Objects.equals(this.seriesStudio, baseItemDto.seriesStudio)
                && Objects.equals(this.parentThumbItemId, baseItemDto.parentThumbItemId)
                && Objects.equals(this.parentThumbImageTag, baseItemDto.parentThumbImageTag)
                && Objects.equals(this.parentPrimaryImageItemId, baseItemDto.parentPrimaryImageItemId)
                && Objects.equals(this.parentPrimaryImageTag, baseItemDto.parentPrimaryImageTag)
                && Objects.equals(this.chapters, baseItemDto.chapters)
                && Objects.equals(this.trickplay, baseItemDto.trickplay)
                && Objects.equals(this.locationType, baseItemDto.locationType)
                && Objects.equals(this.isoType, baseItemDto.isoType)
                && Objects.equals(this.mediaType, baseItemDto.mediaType)
                && Objects.equals(this.endDate, baseItemDto.endDate)
                && Objects.equals(this.lockedFields, baseItemDto.lockedFields)
                && Objects.equals(this.trailerCount, baseItemDto.trailerCount)
                && Objects.equals(this.movieCount, baseItemDto.movieCount)
                && Objects.equals(this.seriesCount, baseItemDto.seriesCount)
                && Objects.equals(this.programCount, baseItemDto.programCount)
                && Objects.equals(this.episodeCount, baseItemDto.episodeCount)
                && Objects.equals(this.songCount, baseItemDto.songCount)
                && Objects.equals(this.albumCount, baseItemDto.albumCount)
                && Objects.equals(this.artistCount, baseItemDto.artistCount)
                && Objects.equals(this.musicVideoCount, baseItemDto.musicVideoCount)
                && Objects.equals(this.lockData, baseItemDto.lockData) && Objects.equals(this.width, baseItemDto.width)
                && Objects.equals(this.height, baseItemDto.height)
                && Objects.equals(this.cameraMake, baseItemDto.cameraMake)
                && Objects.equals(this.cameraModel, baseItemDto.cameraModel)
                && Objects.equals(this.software, baseItemDto.software)
                && Objects.equals(this.exposureTime, baseItemDto.exposureTime)
                && Objects.equals(this.focalLength, baseItemDto.focalLength)
                && Objects.equals(this.imageOrientation, baseItemDto.imageOrientation)
                && Objects.equals(this.aperture, baseItemDto.aperture)
                && Objects.equals(this.shutterSpeed, baseItemDto.shutterSpeed)
                && Objects.equals(this.latitude, baseItemDto.latitude)
                && Objects.equals(this.longitude, baseItemDto.longitude)
                && Objects.equals(this.altitude, baseItemDto.altitude)
                && Objects.equals(this.isoSpeedRating, baseItemDto.isoSpeedRating)
                && Objects.equals(this.seriesTimerId, baseItemDto.seriesTimerId)
                && Objects.equals(this.programId, baseItemDto.programId)
                && Objects.equals(this.channelPrimaryImageTag, baseItemDto.channelPrimaryImageTag)
                && Objects.equals(this.startDate, baseItemDto.startDate)
                && Objects.equals(this.completionPercentage, baseItemDto.completionPercentage)
                && Objects.equals(this.isRepeat, baseItemDto.isRepeat)
                && Objects.equals(this.episodeTitle, baseItemDto.episodeTitle)
                && Objects.equals(this.channelType, baseItemDto.channelType)
                && Objects.equals(this.audio, baseItemDto.audio) && Objects.equals(this.isMovie, baseItemDto.isMovie)
                && Objects.equals(this.isSports, baseItemDto.isSports)
                && Objects.equals(this.isSeries, baseItemDto.isSeries)
                && Objects.equals(this.isLive, baseItemDto.isLive) && Objects.equals(this.isNews, baseItemDto.isNews)
                && Objects.equals(this.isKids, baseItemDto.isKids)
                && Objects.equals(this.isPremiere, baseItemDto.isPremiere)
                && Objects.equals(this.timerId, baseItemDto.timerId)
                && Objects.equals(this.normalizationGain, baseItemDto.normalizationGain)
                && Objects.equals(this.currentProgram, baseItemDto.currentProgram);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, originalTitle, serverId, id, etag, sourceType, playlistItemId, dateCreated,
                dateLastMediaAdded, extraType, airsBeforeSeasonNumber, airsAfterSeasonNumber, airsBeforeEpisodeNumber,
                canDelete, canDownload, hasLyrics, hasSubtitles, preferredMetadataLanguage,
                preferredMetadataCountryCode, container, sortName, forcedSortName, video3DFormat, premiereDate,
                externalUrls, mediaSources, criticRating, productionLocations, path, enableMediaSourceDisplay,
                officialRating, customRating, channelId, channelName, overview, taglines, genres, communityRating,
                cumulativeRunTimeTicks, runTimeTicks, playAccess, aspectRatio, productionYear, isPlaceHolder, number,
                channelNumber, indexNumber, indexNumberEnd, parentIndexNumber, remoteTrailers, providerIds, isHD,
                isFolder, parentId, type, people, studios, genreItems, parentLogoItemId, parentBackdropItemId,
                parentBackdropImageTags, localTrailerCount, userData, recursiveItemCount, childCount, seriesName,
                seriesId, seasonId, specialFeatureCount, displayPreferencesId, status, airTime, airDays, tags,
                primaryImageAspectRatio, artists, artistItems, album, collectionType, displayOrder, albumId,
                albumPrimaryImageTag, seriesPrimaryImageTag, albumArtist, albumArtists, seasonName, mediaStreams,
                videoType, partCount, mediaSourceCount, imageTags, backdropImageTags, screenshotImageTags,
                parentLogoImageTag, parentArtItemId, parentArtImageTag, seriesThumbImageTag, imageBlurHashes,
                seriesStudio, parentThumbItemId, parentThumbImageTag, parentPrimaryImageItemId, parentPrimaryImageTag,
                chapters, trickplay, locationType, isoType, mediaType, endDate, lockedFields, trailerCount, movieCount,
                seriesCount, programCount, episodeCount, songCount, albumCount, artistCount, musicVideoCount, lockData,
                width, height, cameraMake, cameraModel, software, exposureTime, focalLength, imageOrientation, aperture,
                shutterSpeed, latitude, longitude, altitude, isoSpeedRating, seriesTimerId, programId,
                channelPrimaryImageTag, startDate, completionPercentage, isRepeat, episodeTitle, channelType, audio,
                isMovie, isSports, isSeries, isLive, isNews, isKids, isPremiere, timerId, normalizationGain,
                currentProgram);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class BaseItemDto {\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    originalTitle: ").append(toIndentedString(originalTitle)).append("\n");
        sb.append("    serverId: ").append(toIndentedString(serverId)).append("\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    etag: ").append(toIndentedString(etag)).append("\n");
        sb.append("    sourceType: ").append(toIndentedString(sourceType)).append("\n");
        sb.append("    playlistItemId: ").append(toIndentedString(playlistItemId)).append("\n");
        sb.append("    dateCreated: ").append(toIndentedString(dateCreated)).append("\n");
        sb.append("    dateLastMediaAdded: ").append(toIndentedString(dateLastMediaAdded)).append("\n");
        sb.append("    extraType: ").append(toIndentedString(extraType)).append("\n");
        sb.append("    airsBeforeSeasonNumber: ").append(toIndentedString(airsBeforeSeasonNumber)).append("\n");
        sb.append("    airsAfterSeasonNumber: ").append(toIndentedString(airsAfterSeasonNumber)).append("\n");
        sb.append("    airsBeforeEpisodeNumber: ").append(toIndentedString(airsBeforeEpisodeNumber)).append("\n");
        sb.append("    canDelete: ").append(toIndentedString(canDelete)).append("\n");
        sb.append("    canDownload: ").append(toIndentedString(canDownload)).append("\n");
        sb.append("    hasLyrics: ").append(toIndentedString(hasLyrics)).append("\n");
        sb.append("    hasSubtitles: ").append(toIndentedString(hasSubtitles)).append("\n");
        sb.append("    preferredMetadataLanguage: ").append(toIndentedString(preferredMetadataLanguage)).append("\n");
        sb.append("    preferredMetadataCountryCode: ").append(toIndentedString(preferredMetadataCountryCode))
                .append("\n");
        sb.append("    container: ").append(toIndentedString(container)).append("\n");
        sb.append("    sortName: ").append(toIndentedString(sortName)).append("\n");
        sb.append("    forcedSortName: ").append(toIndentedString(forcedSortName)).append("\n");
        sb.append("    video3DFormat: ").append(toIndentedString(video3DFormat)).append("\n");
        sb.append("    premiereDate: ").append(toIndentedString(premiereDate)).append("\n");
        sb.append("    externalUrls: ").append(toIndentedString(externalUrls)).append("\n");
        sb.append("    mediaSources: ").append(toIndentedString(mediaSources)).append("\n");
        sb.append("    criticRating: ").append(toIndentedString(criticRating)).append("\n");
        sb.append("    productionLocations: ").append(toIndentedString(productionLocations)).append("\n");
        sb.append("    path: ").append(toIndentedString(path)).append("\n");
        sb.append("    enableMediaSourceDisplay: ").append(toIndentedString(enableMediaSourceDisplay)).append("\n");
        sb.append("    officialRating: ").append(toIndentedString(officialRating)).append("\n");
        sb.append("    customRating: ").append(toIndentedString(customRating)).append("\n");
        sb.append("    channelId: ").append(toIndentedString(channelId)).append("\n");
        sb.append("    channelName: ").append(toIndentedString(channelName)).append("\n");
        sb.append("    overview: ").append(toIndentedString(overview)).append("\n");
        sb.append("    taglines: ").append(toIndentedString(taglines)).append("\n");
        sb.append("    genres: ").append(toIndentedString(genres)).append("\n");
        sb.append("    communityRating: ").append(toIndentedString(communityRating)).append("\n");
        sb.append("    cumulativeRunTimeTicks: ").append(toIndentedString(cumulativeRunTimeTicks)).append("\n");
        sb.append("    runTimeTicks: ").append(toIndentedString(runTimeTicks)).append("\n");
        sb.append("    playAccess: ").append(toIndentedString(playAccess)).append("\n");
        sb.append("    aspectRatio: ").append(toIndentedString(aspectRatio)).append("\n");
        sb.append("    productionYear: ").append(toIndentedString(productionYear)).append("\n");
        sb.append("    isPlaceHolder: ").append(toIndentedString(isPlaceHolder)).append("\n");
        sb.append("    number: ").append(toIndentedString(number)).append("\n");
        sb.append("    channelNumber: ").append(toIndentedString(channelNumber)).append("\n");
        sb.append("    indexNumber: ").append(toIndentedString(indexNumber)).append("\n");
        sb.append("    indexNumberEnd: ").append(toIndentedString(indexNumberEnd)).append("\n");
        sb.append("    parentIndexNumber: ").append(toIndentedString(parentIndexNumber)).append("\n");
        sb.append("    remoteTrailers: ").append(toIndentedString(remoteTrailers)).append("\n");
        sb.append("    providerIds: ").append(toIndentedString(providerIds)).append("\n");
        sb.append("    isHD: ").append(toIndentedString(isHD)).append("\n");
        sb.append("    isFolder: ").append(toIndentedString(isFolder)).append("\n");
        sb.append("    parentId: ").append(toIndentedString(parentId)).append("\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    people: ").append(toIndentedString(people)).append("\n");
        sb.append("    studios: ").append(toIndentedString(studios)).append("\n");
        sb.append("    genreItems: ").append(toIndentedString(genreItems)).append("\n");
        sb.append("    parentLogoItemId: ").append(toIndentedString(parentLogoItemId)).append("\n");
        sb.append("    parentBackdropItemId: ").append(toIndentedString(parentBackdropItemId)).append("\n");
        sb.append("    parentBackdropImageTags: ").append(toIndentedString(parentBackdropImageTags)).append("\n");
        sb.append("    localTrailerCount: ").append(toIndentedString(localTrailerCount)).append("\n");
        sb.append("    userData: ").append(toIndentedString(userData)).append("\n");
        sb.append("    recursiveItemCount: ").append(toIndentedString(recursiveItemCount)).append("\n");
        sb.append("    childCount: ").append(toIndentedString(childCount)).append("\n");
        sb.append("    seriesName: ").append(toIndentedString(seriesName)).append("\n");
        sb.append("    seriesId: ").append(toIndentedString(seriesId)).append("\n");
        sb.append("    seasonId: ").append(toIndentedString(seasonId)).append("\n");
        sb.append("    specialFeatureCount: ").append(toIndentedString(specialFeatureCount)).append("\n");
        sb.append("    displayPreferencesId: ").append(toIndentedString(displayPreferencesId)).append("\n");
        sb.append("    status: ").append(toIndentedString(status)).append("\n");
        sb.append("    airTime: ").append(toIndentedString(airTime)).append("\n");
        sb.append("    airDays: ").append(toIndentedString(airDays)).append("\n");
        sb.append("    tags: ").append(toIndentedString(tags)).append("\n");
        sb.append("    primaryImageAspectRatio: ").append(toIndentedString(primaryImageAspectRatio)).append("\n");
        sb.append("    artists: ").append(toIndentedString(artists)).append("\n");
        sb.append("    artistItems: ").append(toIndentedString(artistItems)).append("\n");
        sb.append("    album: ").append(toIndentedString(album)).append("\n");
        sb.append("    collectionType: ").append(toIndentedString(collectionType)).append("\n");
        sb.append("    displayOrder: ").append(toIndentedString(displayOrder)).append("\n");
        sb.append("    albumId: ").append(toIndentedString(albumId)).append("\n");
        sb.append("    albumPrimaryImageTag: ").append(toIndentedString(albumPrimaryImageTag)).append("\n");
        sb.append("    seriesPrimaryImageTag: ").append(toIndentedString(seriesPrimaryImageTag)).append("\n");
        sb.append("    albumArtist: ").append(toIndentedString(albumArtist)).append("\n");
        sb.append("    albumArtists: ").append(toIndentedString(albumArtists)).append("\n");
        sb.append("    seasonName: ").append(toIndentedString(seasonName)).append("\n");
        sb.append("    mediaStreams: ").append(toIndentedString(mediaStreams)).append("\n");
        sb.append("    videoType: ").append(toIndentedString(videoType)).append("\n");
        sb.append("    partCount: ").append(toIndentedString(partCount)).append("\n");
        sb.append("    mediaSourceCount: ").append(toIndentedString(mediaSourceCount)).append("\n");
        sb.append("    imageTags: ").append(toIndentedString(imageTags)).append("\n");
        sb.append("    backdropImageTags: ").append(toIndentedString(backdropImageTags)).append("\n");
        sb.append("    screenshotImageTags: ").append(toIndentedString(screenshotImageTags)).append("\n");
        sb.append("    parentLogoImageTag: ").append(toIndentedString(parentLogoImageTag)).append("\n");
        sb.append("    parentArtItemId: ").append(toIndentedString(parentArtItemId)).append("\n");
        sb.append("    parentArtImageTag: ").append(toIndentedString(parentArtImageTag)).append("\n");
        sb.append("    seriesThumbImageTag: ").append(toIndentedString(seriesThumbImageTag)).append("\n");
        sb.append("    imageBlurHashes: ").append(toIndentedString(imageBlurHashes)).append("\n");
        sb.append("    seriesStudio: ").append(toIndentedString(seriesStudio)).append("\n");
        sb.append("    parentThumbItemId: ").append(toIndentedString(parentThumbItemId)).append("\n");
        sb.append("    parentThumbImageTag: ").append(toIndentedString(parentThumbImageTag)).append("\n");
        sb.append("    parentPrimaryImageItemId: ").append(toIndentedString(parentPrimaryImageItemId)).append("\n");
        sb.append("    parentPrimaryImageTag: ").append(toIndentedString(parentPrimaryImageTag)).append("\n");
        sb.append("    chapters: ").append(toIndentedString(chapters)).append("\n");
        sb.append("    trickplay: ").append(toIndentedString(trickplay)).append("\n");
        sb.append("    locationType: ").append(toIndentedString(locationType)).append("\n");
        sb.append("    isoType: ").append(toIndentedString(isoType)).append("\n");
        sb.append("    mediaType: ").append(toIndentedString(mediaType)).append("\n");
        sb.append("    endDate: ").append(toIndentedString(endDate)).append("\n");
        sb.append("    lockedFields: ").append(toIndentedString(lockedFields)).append("\n");
        sb.append("    trailerCount: ").append(toIndentedString(trailerCount)).append("\n");
        sb.append("    movieCount: ").append(toIndentedString(movieCount)).append("\n");
        sb.append("    seriesCount: ").append(toIndentedString(seriesCount)).append("\n");
        sb.append("    programCount: ").append(toIndentedString(programCount)).append("\n");
        sb.append("    episodeCount: ").append(toIndentedString(episodeCount)).append("\n");
        sb.append("    songCount: ").append(toIndentedString(songCount)).append("\n");
        sb.append("    albumCount: ").append(toIndentedString(albumCount)).append("\n");
        sb.append("    artistCount: ").append(toIndentedString(artistCount)).append("\n");
        sb.append("    musicVideoCount: ").append(toIndentedString(musicVideoCount)).append("\n");
        sb.append("    lockData: ").append(toIndentedString(lockData)).append("\n");
        sb.append("    width: ").append(toIndentedString(width)).append("\n");
        sb.append("    height: ").append(toIndentedString(height)).append("\n");
        sb.append("    cameraMake: ").append(toIndentedString(cameraMake)).append("\n");
        sb.append("    cameraModel: ").append(toIndentedString(cameraModel)).append("\n");
        sb.append("    software: ").append(toIndentedString(software)).append("\n");
        sb.append("    exposureTime: ").append(toIndentedString(exposureTime)).append("\n");
        sb.append("    focalLength: ").append(toIndentedString(focalLength)).append("\n");
        sb.append("    imageOrientation: ").append(toIndentedString(imageOrientation)).append("\n");
        sb.append("    aperture: ").append(toIndentedString(aperture)).append("\n");
        sb.append("    shutterSpeed: ").append(toIndentedString(shutterSpeed)).append("\n");
        sb.append("    latitude: ").append(toIndentedString(latitude)).append("\n");
        sb.append("    longitude: ").append(toIndentedString(longitude)).append("\n");
        sb.append("    altitude: ").append(toIndentedString(altitude)).append("\n");
        sb.append("    isoSpeedRating: ").append(toIndentedString(isoSpeedRating)).append("\n");
        sb.append("    seriesTimerId: ").append(toIndentedString(seriesTimerId)).append("\n");
        sb.append("    programId: ").append(toIndentedString(programId)).append("\n");
        sb.append("    channelPrimaryImageTag: ").append(toIndentedString(channelPrimaryImageTag)).append("\n");
        sb.append("    startDate: ").append(toIndentedString(startDate)).append("\n");
        sb.append("    completionPercentage: ").append(toIndentedString(completionPercentage)).append("\n");
        sb.append("    isRepeat: ").append(toIndentedString(isRepeat)).append("\n");
        sb.append("    episodeTitle: ").append(toIndentedString(episodeTitle)).append("\n");
        sb.append("    channelType: ").append(toIndentedString(channelType)).append("\n");
        sb.append("    audio: ").append(toIndentedString(audio)).append("\n");
        sb.append("    isMovie: ").append(toIndentedString(isMovie)).append("\n");
        sb.append("    isSports: ").append(toIndentedString(isSports)).append("\n");
        sb.append("    isSeries: ").append(toIndentedString(isSeries)).append("\n");
        sb.append("    isLive: ").append(toIndentedString(isLive)).append("\n");
        sb.append("    isNews: ").append(toIndentedString(isNews)).append("\n");
        sb.append("    isKids: ").append(toIndentedString(isKids)).append("\n");
        sb.append("    isPremiere: ").append(toIndentedString(isPremiere)).append("\n");
        sb.append("    timerId: ").append(toIndentedString(timerId)).append("\n");
        sb.append("    normalizationGain: ").append(toIndentedString(normalizationGain)).append("\n");
        sb.append("    currentProgram: ").append(toIndentedString(currentProgram)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }

    /**
     * Convert the instance into URL query string.
     *
     * @return URL query string
     */
    public String toUrlQueryString() {
        return toUrlQueryString(null);
    }

    /**
     * Convert the instance into URL query string.
     *
     * @param prefix prefix of the query string
     * @return URL query string
     */
    public String toUrlQueryString(String prefix) {
        String suffix = "";
        String containerSuffix = "";
        String containerPrefix = "";
        if (prefix == null) {
            // style=form, explode=true, e.g. /pet?name=cat&type=manx
            prefix = "";
        } else {
            // deepObject style e.g. /pet?id[name]=cat&id[type]=manx
            prefix = prefix + "[";
            suffix = "]";
            containerSuffix = "]";
            containerPrefix = "[";
        }

        StringJoiner joiner = new StringJoiner("&");

        // add `Name` to the URL query string
        if (getName() != null) {
            joiner.add(String.format(Locale.ROOT, "%sName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getName()))));
        }

        // add `OriginalTitle` to the URL query string
        if (getOriginalTitle() != null) {
            joiner.add(String.format(Locale.ROOT, "%sOriginalTitle%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getOriginalTitle()))));
        }

        // add `ServerId` to the URL query string
        if (getServerId() != null) {
            joiner.add(String.format(Locale.ROOT, "%sServerId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getServerId()))));
        }

        // add `Id` to the URL query string
        if (getId() != null) {
            joiner.add(String.format(Locale.ROOT, "%sId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getId()))));
        }

        // add `Etag` to the URL query string
        if (getEtag() != null) {
            joiner.add(String.format(Locale.ROOT, "%sEtag%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEtag()))));
        }

        // add `SourceType` to the URL query string
        if (getSourceType() != null) {
            joiner.add(String.format(Locale.ROOT, "%sSourceType%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSourceType()))));
        }

        // add `PlaylistItemId` to the URL query string
        if (getPlaylistItemId() != null) {
            joiner.add(String.format(Locale.ROOT, "%sPlaylistItemId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPlaylistItemId()))));
        }

        // add `DateCreated` to the URL query string
        if (getDateCreated() != null) {
            joiner.add(String.format(Locale.ROOT, "%sDateCreated%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getDateCreated()))));
        }

        // add `DateLastMediaAdded` to the URL query string
        if (getDateLastMediaAdded() != null) {
            joiner.add(String.format(Locale.ROOT, "%sDateLastMediaAdded%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getDateLastMediaAdded()))));
        }

        // add `ExtraType` to the URL query string
        if (getExtraType() != null) {
            joiner.add(String.format(Locale.ROOT, "%sExtraType%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getExtraType()))));
        }

        // add `AirsBeforeSeasonNumber` to the URL query string
        if (getAirsBeforeSeasonNumber() != null) {
            joiner.add(String.format(Locale.ROOT, "%sAirsBeforeSeasonNumber%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getAirsBeforeSeasonNumber()))));
        }

        // add `AirsAfterSeasonNumber` to the URL query string
        if (getAirsAfterSeasonNumber() != null) {
            joiner.add(String.format(Locale.ROOT, "%sAirsAfterSeasonNumber%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getAirsAfterSeasonNumber()))));
        }

        // add `AirsBeforeEpisodeNumber` to the URL query string
        if (getAirsBeforeEpisodeNumber() != null) {
            joiner.add(String.format(Locale.ROOT, "%sAirsBeforeEpisodeNumber%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getAirsBeforeEpisodeNumber()))));
        }

        // add `CanDelete` to the URL query string
        if (getCanDelete() != null) {
            joiner.add(String.format(Locale.ROOT, "%sCanDelete%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getCanDelete()))));
        }

        // add `CanDownload` to the URL query string
        if (getCanDownload() != null) {
            joiner.add(String.format(Locale.ROOT, "%sCanDownload%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getCanDownload()))));
        }

        // add `HasLyrics` to the URL query string
        if (getHasLyrics() != null) {
            joiner.add(String.format(Locale.ROOT, "%sHasLyrics%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getHasLyrics()))));
        }

        // add `HasSubtitles` to the URL query string
        if (getHasSubtitles() != null) {
            joiner.add(String.format(Locale.ROOT, "%sHasSubtitles%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getHasSubtitles()))));
        }

        // add `PreferredMetadataLanguage` to the URL query string
        if (getPreferredMetadataLanguage() != null) {
            joiner.add(String.format(Locale.ROOT, "%sPreferredMetadataLanguage%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPreferredMetadataLanguage()))));
        }

        // add `PreferredMetadataCountryCode` to the URL query string
        if (getPreferredMetadataCountryCode() != null) {
            joiner.add(String.format(Locale.ROOT, "%sPreferredMetadataCountryCode%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPreferredMetadataCountryCode()))));
        }

        // add `Container` to the URL query string
        if (getContainer() != null) {
            joiner.add(String.format(Locale.ROOT, "%sContainer%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getContainer()))));
        }

        // add `SortName` to the URL query string
        if (getSortName() != null) {
            joiner.add(String.format(Locale.ROOT, "%sSortName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSortName()))));
        }

        // add `ForcedSortName` to the URL query string
        if (getForcedSortName() != null) {
            joiner.add(String.format(Locale.ROOT, "%sForcedSortName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getForcedSortName()))));
        }

        // add `Video3DFormat` to the URL query string
        if (getVideo3DFormat() != null) {
            joiner.add(String.format(Locale.ROOT, "%sVideo3DFormat%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getVideo3DFormat()))));
        }

        // add `PremiereDate` to the URL query string
        if (getPremiereDate() != null) {
            joiner.add(String.format(Locale.ROOT, "%sPremiereDate%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPremiereDate()))));
        }

        // add `ExternalUrls` to the URL query string
        if (getExternalUrls() != null) {
            for (int i = 0; i < getExternalUrls().size(); i++) {
                if (getExternalUrls().get(i) != null) {
                    joiner.add(getExternalUrls().get(i).toUrlQueryString(
                            String.format(Locale.ROOT, "%sExternalUrls%s%s", prefix, suffix, "".equals(suffix) ? ""
                                    : String.format(Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix))));
                }
            }
        }

        // add `MediaSources` to the URL query string
        if (getMediaSources() != null) {
            for (int i = 0; i < getMediaSources().size(); i++) {
                if (getMediaSources().get(i) != null) {
                    joiner.add(getMediaSources().get(i).toUrlQueryString(
                            String.format(Locale.ROOT, "%sMediaSources%s%s", prefix, suffix, "".equals(suffix) ? ""
                                    : String.format(Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix))));
                }
            }
        }

        // add `CriticRating` to the URL query string
        if (getCriticRating() != null) {
            joiner.add(String.format(Locale.ROOT, "%sCriticRating%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getCriticRating()))));
        }

        // add `ProductionLocations` to the URL query string
        if (getProductionLocations() != null) {
            for (int i = 0; i < getProductionLocations().size(); i++) {
                joiner.add(String.format(Locale.ROOT, "%sProductionLocations%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                        ApiClient.urlEncode(ApiClient.valueToString(getProductionLocations().get(i)))));
            }
        }

        // add `Path` to the URL query string
        if (getPath() != null) {
            joiner.add(String.format(Locale.ROOT, "%sPath%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPath()))));
        }

        // add `EnableMediaSourceDisplay` to the URL query string
        if (getEnableMediaSourceDisplay() != null) {
            joiner.add(String.format(Locale.ROOT, "%sEnableMediaSourceDisplay%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableMediaSourceDisplay()))));
        }

        // add `OfficialRating` to the URL query string
        if (getOfficialRating() != null) {
            joiner.add(String.format(Locale.ROOT, "%sOfficialRating%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getOfficialRating()))));
        }

        // add `CustomRating` to the URL query string
        if (getCustomRating() != null) {
            joiner.add(String.format(Locale.ROOT, "%sCustomRating%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getCustomRating()))));
        }

        // add `ChannelId` to the URL query string
        if (getChannelId() != null) {
            joiner.add(String.format(Locale.ROOT, "%sChannelId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getChannelId()))));
        }

        // add `ChannelName` to the URL query string
        if (getChannelName() != null) {
            joiner.add(String.format(Locale.ROOT, "%sChannelName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getChannelName()))));
        }

        // add `Overview` to the URL query string
        if (getOverview() != null) {
            joiner.add(String.format(Locale.ROOT, "%sOverview%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getOverview()))));
        }

        // add `Taglines` to the URL query string
        if (getTaglines() != null) {
            for (int i = 0; i < getTaglines().size(); i++) {
                joiner.add(String.format(Locale.ROOT, "%sTaglines%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                        ApiClient.urlEncode(ApiClient.valueToString(getTaglines().get(i)))));
            }
        }

        // add `Genres` to the URL query string
        if (getGenres() != null) {
            for (int i = 0; i < getGenres().size(); i++) {
                joiner.add(String.format(Locale.ROOT, "%sGenres%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                        ApiClient.urlEncode(ApiClient.valueToString(getGenres().get(i)))));
            }
        }

        // add `CommunityRating` to the URL query string
        if (getCommunityRating() != null) {
            joiner.add(String.format(Locale.ROOT, "%sCommunityRating%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getCommunityRating()))));
        }

        // add `CumulativeRunTimeTicks` to the URL query string
        if (getCumulativeRunTimeTicks() != null) {
            joiner.add(String.format(Locale.ROOT, "%sCumulativeRunTimeTicks%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getCumulativeRunTimeTicks()))));
        }

        // add `RunTimeTicks` to the URL query string
        if (getRunTimeTicks() != null) {
            joiner.add(String.format(Locale.ROOT, "%sRunTimeTicks%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getRunTimeTicks()))));
        }

        // add `PlayAccess` to the URL query string
        if (getPlayAccess() != null) {
            joiner.add(String.format(Locale.ROOT, "%sPlayAccess%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPlayAccess()))));
        }

        // add `AspectRatio` to the URL query string
        if (getAspectRatio() != null) {
            joiner.add(String.format(Locale.ROOT, "%sAspectRatio%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getAspectRatio()))));
        }

        // add `ProductionYear` to the URL query string
        if (getProductionYear() != null) {
            joiner.add(String.format(Locale.ROOT, "%sProductionYear%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getProductionYear()))));
        }

        // add `IsPlaceHolder` to the URL query string
        if (getIsPlaceHolder() != null) {
            joiner.add(String.format(Locale.ROOT, "%sIsPlaceHolder%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIsPlaceHolder()))));
        }

        // add `Number` to the URL query string
        if (getNumber() != null) {
            joiner.add(String.format(Locale.ROOT, "%sNumber%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getNumber()))));
        }

        // add `ChannelNumber` to the URL query string
        if (getChannelNumber() != null) {
            joiner.add(String.format(Locale.ROOT, "%sChannelNumber%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getChannelNumber()))));
        }

        // add `IndexNumber` to the URL query string
        if (getIndexNumber() != null) {
            joiner.add(String.format(Locale.ROOT, "%sIndexNumber%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIndexNumber()))));
        }

        // add `IndexNumberEnd` to the URL query string
        if (getIndexNumberEnd() != null) {
            joiner.add(String.format(Locale.ROOT, "%sIndexNumberEnd%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIndexNumberEnd()))));
        }

        // add `ParentIndexNumber` to the URL query string
        if (getParentIndexNumber() != null) {
            joiner.add(String.format(Locale.ROOT, "%sParentIndexNumber%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getParentIndexNumber()))));
        }

        // add `RemoteTrailers` to the URL query string
        if (getRemoteTrailers() != null) {
            for (int i = 0; i < getRemoteTrailers().size(); i++) {
                if (getRemoteTrailers().get(i) != null) {
                    joiner.add(getRemoteTrailers().get(i).toUrlQueryString(
                            String.format(Locale.ROOT, "%sRemoteTrailers%s%s", prefix, suffix, "".equals(suffix) ? ""
                                    : String.format(Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix))));
                }
            }
        }

        // add `ProviderIds` to the URL query string
        if (getProviderIds() != null) {
            for (String _key : getProviderIds().keySet()) {
                joiner.add(String.format(Locale.ROOT, "%sProviderIds%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(Locale.ROOT, "%s%d%s", containerPrefix, _key, containerSuffix),
                        getProviderIds().get(_key),
                        ApiClient.urlEncode(ApiClient.valueToString(getProviderIds().get(_key)))));
            }
        }

        // add `IsHD` to the URL query string
        if (getIsHD() != null) {
            joiner.add(String.format(Locale.ROOT, "%sIsHD%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIsHD()))));
        }

        // add `IsFolder` to the URL query string
        if (getIsFolder() != null) {
            joiner.add(String.format(Locale.ROOT, "%sIsFolder%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIsFolder()))));
        }

        // add `ParentId` to the URL query string
        if (getParentId() != null) {
            joiner.add(String.format(Locale.ROOT, "%sParentId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getParentId()))));
        }

        // add `Type` to the URL query string
        if (getType() != null) {
            joiner.add(String.format(Locale.ROOT, "%sType%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getType()))));
        }

        // add `People` to the URL query string
        if (getPeople() != null) {
            for (int i = 0; i < getPeople().size(); i++) {
                if (getPeople().get(i) != null) {
                    joiner.add(getPeople().get(i).toUrlQueryString(
                            String.format(Locale.ROOT, "%sPeople%s%s", prefix, suffix, "".equals(suffix) ? ""
                                    : String.format(Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix))));
                }
            }
        }

        // add `Studios` to the URL query string
        if (getStudios() != null) {
            for (int i = 0; i < getStudios().size(); i++) {
                if (getStudios().get(i) != null) {
                    joiner.add(getStudios().get(i).toUrlQueryString(
                            String.format(Locale.ROOT, "%sStudios%s%s", prefix, suffix, "".equals(suffix) ? ""
                                    : String.format(Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix))));
                }
            }
        }

        // add `GenreItems` to the URL query string
        if (getGenreItems() != null) {
            for (int i = 0; i < getGenreItems().size(); i++) {
                if (getGenreItems().get(i) != null) {
                    joiner.add(getGenreItems().get(i).toUrlQueryString(
                            String.format(Locale.ROOT, "%sGenreItems%s%s", prefix, suffix, "".equals(suffix) ? ""
                                    : String.format(Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix))));
                }
            }
        }

        // add `ParentLogoItemId` to the URL query string
        if (getParentLogoItemId() != null) {
            joiner.add(String.format(Locale.ROOT, "%sParentLogoItemId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getParentLogoItemId()))));
        }

        // add `ParentBackdropItemId` to the URL query string
        if (getParentBackdropItemId() != null) {
            joiner.add(String.format(Locale.ROOT, "%sParentBackdropItemId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getParentBackdropItemId()))));
        }

        // add `ParentBackdropImageTags` to the URL query string
        if (getParentBackdropImageTags() != null) {
            for (int i = 0; i < getParentBackdropImageTags().size(); i++) {
                joiner.add(String.format(Locale.ROOT, "%sParentBackdropImageTags%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                        ApiClient.urlEncode(ApiClient.valueToString(getParentBackdropImageTags().get(i)))));
            }
        }

        // add `LocalTrailerCount` to the URL query string
        if (getLocalTrailerCount() != null) {
            joiner.add(String.format(Locale.ROOT, "%sLocalTrailerCount%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getLocalTrailerCount()))));
        }

        // add `UserData` to the URL query string
        if (getUserData() != null) {
            joiner.add(getUserData().toUrlQueryString(prefix + "UserData" + suffix));
        }

        // add `RecursiveItemCount` to the URL query string
        if (getRecursiveItemCount() != null) {
            joiner.add(String.format(Locale.ROOT, "%sRecursiveItemCount%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getRecursiveItemCount()))));
        }

        // add `ChildCount` to the URL query string
        if (getChildCount() != null) {
            joiner.add(String.format(Locale.ROOT, "%sChildCount%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getChildCount()))));
        }

        // add `SeriesName` to the URL query string
        if (getSeriesName() != null) {
            joiner.add(String.format(Locale.ROOT, "%sSeriesName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSeriesName()))));
        }

        // add `SeriesId` to the URL query string
        if (getSeriesId() != null) {
            joiner.add(String.format(Locale.ROOT, "%sSeriesId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSeriesId()))));
        }

        // add `SeasonId` to the URL query string
        if (getSeasonId() != null) {
            joiner.add(String.format(Locale.ROOT, "%sSeasonId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSeasonId()))));
        }

        // add `SpecialFeatureCount` to the URL query string
        if (getSpecialFeatureCount() != null) {
            joiner.add(String.format(Locale.ROOT, "%sSpecialFeatureCount%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSpecialFeatureCount()))));
        }

        // add `DisplayPreferencesId` to the URL query string
        if (getDisplayPreferencesId() != null) {
            joiner.add(String.format(Locale.ROOT, "%sDisplayPreferencesId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getDisplayPreferencesId()))));
        }

        // add `Status` to the URL query string
        if (getStatus() != null) {
            joiner.add(String.format(Locale.ROOT, "%sStatus%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getStatus()))));
        }

        // add `AirTime` to the URL query string
        if (getAirTime() != null) {
            joiner.add(String.format(Locale.ROOT, "%sAirTime%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getAirTime()))));
        }

        // add `AirDays` to the URL query string
        if (getAirDays() != null) {
            for (int i = 0; i < getAirDays().size(); i++) {
                if (getAirDays().get(i) != null) {
                    joiner.add(String.format(Locale.ROOT, "%sAirDays%s%s=%s", prefix, suffix,
                            "".equals(suffix) ? ""
                                    : String.format(Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                            ApiClient.urlEncode(ApiClient.valueToString(getAirDays().get(i)))));
                }
            }
        }

        // add `Tags` to the URL query string
        if (getTags() != null) {
            for (int i = 0; i < getTags().size(); i++) {
                joiner.add(String.format(Locale.ROOT, "%sTags%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                        ApiClient.urlEncode(ApiClient.valueToString(getTags().get(i)))));
            }
        }

        // add `PrimaryImageAspectRatio` to the URL query string
        if (getPrimaryImageAspectRatio() != null) {
            joiner.add(String.format(Locale.ROOT, "%sPrimaryImageAspectRatio%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPrimaryImageAspectRatio()))));
        }

        // add `Artists` to the URL query string
        if (getArtists() != null) {
            for (int i = 0; i < getArtists().size(); i++) {
                joiner.add(String.format(Locale.ROOT, "%sArtists%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                        ApiClient.urlEncode(ApiClient.valueToString(getArtists().get(i)))));
            }
        }

        // add `ArtistItems` to the URL query string
        if (getArtistItems() != null) {
            for (int i = 0; i < getArtistItems().size(); i++) {
                if (getArtistItems().get(i) != null) {
                    joiner.add(getArtistItems().get(i).toUrlQueryString(
                            String.format(Locale.ROOT, "%sArtistItems%s%s", prefix, suffix, "".equals(suffix) ? ""
                                    : String.format(Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix))));
                }
            }
        }

        // add `Album` to the URL query string
        if (getAlbum() != null) {
            joiner.add(String.format(Locale.ROOT, "%sAlbum%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getAlbum()))));
        }

        // add `CollectionType` to the URL query string
        if (getCollectionType() != null) {
            joiner.add(String.format(Locale.ROOT, "%sCollectionType%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getCollectionType()))));
        }

        // add `DisplayOrder` to the URL query string
        if (getDisplayOrder() != null) {
            joiner.add(String.format(Locale.ROOT, "%sDisplayOrder%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getDisplayOrder()))));
        }

        // add `AlbumId` to the URL query string
        if (getAlbumId() != null) {
            joiner.add(String.format(Locale.ROOT, "%sAlbumId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getAlbumId()))));
        }

        // add `AlbumPrimaryImageTag` to the URL query string
        if (getAlbumPrimaryImageTag() != null) {
            joiner.add(String.format(Locale.ROOT, "%sAlbumPrimaryImageTag%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getAlbumPrimaryImageTag()))));
        }

        // add `SeriesPrimaryImageTag` to the URL query string
        if (getSeriesPrimaryImageTag() != null) {
            joiner.add(String.format(Locale.ROOT, "%sSeriesPrimaryImageTag%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSeriesPrimaryImageTag()))));
        }

        // add `AlbumArtist` to the URL query string
        if (getAlbumArtist() != null) {
            joiner.add(String.format(Locale.ROOT, "%sAlbumArtist%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getAlbumArtist()))));
        }

        // add `AlbumArtists` to the URL query string
        if (getAlbumArtists() != null) {
            for (int i = 0; i < getAlbumArtists().size(); i++) {
                if (getAlbumArtists().get(i) != null) {
                    joiner.add(getAlbumArtists().get(i).toUrlQueryString(
                            String.format(Locale.ROOT, "%sAlbumArtists%s%s", prefix, suffix, "".equals(suffix) ? ""
                                    : String.format(Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix))));
                }
            }
        }

        // add `SeasonName` to the URL query string
        if (getSeasonName() != null) {
            joiner.add(String.format(Locale.ROOT, "%sSeasonName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSeasonName()))));
        }

        // add `MediaStreams` to the URL query string
        if (getMediaStreams() != null) {
            for (int i = 0; i < getMediaStreams().size(); i++) {
                if (getMediaStreams().get(i) != null) {
                    joiner.add(getMediaStreams().get(i).toUrlQueryString(
                            String.format(Locale.ROOT, "%sMediaStreams%s%s", prefix, suffix, "".equals(suffix) ? ""
                                    : String.format(Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix))));
                }
            }
        }

        // add `VideoType` to the URL query string
        if (getVideoType() != null) {
            joiner.add(String.format(Locale.ROOT, "%sVideoType%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getVideoType()))));
        }

        // add `PartCount` to the URL query string
        if (getPartCount() != null) {
            joiner.add(String.format(Locale.ROOT, "%sPartCount%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPartCount()))));
        }

        // add `MediaSourceCount` to the URL query string
        if (getMediaSourceCount() != null) {
            joiner.add(String.format(Locale.ROOT, "%sMediaSourceCount%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getMediaSourceCount()))));
        }

        // add `ImageTags` to the URL query string
        if (getImageTags() != null) {
            for (String _key : getImageTags().keySet()) {
                joiner.add(String.format(Locale.ROOT, "%sImageTags%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(Locale.ROOT, "%s%d%s", containerPrefix, _key, containerSuffix),
                        getImageTags().get(_key),
                        ApiClient.urlEncode(ApiClient.valueToString(getImageTags().get(_key)))));
            }
        }

        // add `BackdropImageTags` to the URL query string
        if (getBackdropImageTags() != null) {
            for (int i = 0; i < getBackdropImageTags().size(); i++) {
                joiner.add(String.format(Locale.ROOT, "%sBackdropImageTags%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                        ApiClient.urlEncode(ApiClient.valueToString(getBackdropImageTags().get(i)))));
            }
        }

        // add `ScreenshotImageTags` to the URL query string
        if (getScreenshotImageTags() != null) {
            for (int i = 0; i < getScreenshotImageTags().size(); i++) {
                joiner.add(String.format(Locale.ROOT, "%sScreenshotImageTags%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                        ApiClient.urlEncode(ApiClient.valueToString(getScreenshotImageTags().get(i)))));
            }
        }

        // add `ParentLogoImageTag` to the URL query string
        if (getParentLogoImageTag() != null) {
            joiner.add(String.format(Locale.ROOT, "%sParentLogoImageTag%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getParentLogoImageTag()))));
        }

        // add `ParentArtItemId` to the URL query string
        if (getParentArtItemId() != null) {
            joiner.add(String.format(Locale.ROOT, "%sParentArtItemId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getParentArtItemId()))));
        }

        // add `ParentArtImageTag` to the URL query string
        if (getParentArtImageTag() != null) {
            joiner.add(String.format(Locale.ROOT, "%sParentArtImageTag%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getParentArtImageTag()))));
        }

        // add `SeriesThumbImageTag` to the URL query string
        if (getSeriesThumbImageTag() != null) {
            joiner.add(String.format(Locale.ROOT, "%sSeriesThumbImageTag%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSeriesThumbImageTag()))));
        }

        // add `ImageBlurHashes` to the URL query string
        if (getImageBlurHashes() != null) {
            joiner.add(getImageBlurHashes().toUrlQueryString(prefix + "ImageBlurHashes" + suffix));
        }

        // add `SeriesStudio` to the URL query string
        if (getSeriesStudio() != null) {
            joiner.add(String.format(Locale.ROOT, "%sSeriesStudio%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSeriesStudio()))));
        }

        // add `ParentThumbItemId` to the URL query string
        if (getParentThumbItemId() != null) {
            joiner.add(String.format(Locale.ROOT, "%sParentThumbItemId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getParentThumbItemId()))));
        }

        // add `ParentThumbImageTag` to the URL query string
        if (getParentThumbImageTag() != null) {
            joiner.add(String.format(Locale.ROOT, "%sParentThumbImageTag%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getParentThumbImageTag()))));
        }

        // add `ParentPrimaryImageItemId` to the URL query string
        if (getParentPrimaryImageItemId() != null) {
            joiner.add(String.format(Locale.ROOT, "%sParentPrimaryImageItemId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getParentPrimaryImageItemId()))));
        }

        // add `ParentPrimaryImageTag` to the URL query string
        if (getParentPrimaryImageTag() != null) {
            joiner.add(String.format(Locale.ROOT, "%sParentPrimaryImageTag%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getParentPrimaryImageTag()))));
        }

        // add `Chapters` to the URL query string
        if (getChapters() != null) {
            for (int i = 0; i < getChapters().size(); i++) {
                if (getChapters().get(i) != null) {
                    joiner.add(getChapters().get(i).toUrlQueryString(
                            String.format(Locale.ROOT, "%sChapters%s%s", prefix, suffix, "".equals(suffix) ? ""
                                    : String.format(Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix))));
                }
            }
        }

        // add `Trickplay` to the URL query string
        if (getTrickplay() != null) {
            for (String _key : getTrickplay().keySet()) {
                joiner.add(String.format(Locale.ROOT, "%sTrickplay%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(Locale.ROOT, "%s%d%s", containerPrefix, _key, containerSuffix),
                        getTrickplay().get(_key),
                        ApiClient.urlEncode(ApiClient.valueToString(getTrickplay().get(_key)))));
            }
        }

        // add `LocationType` to the URL query string
        if (getLocationType() != null) {
            joiner.add(String.format(Locale.ROOT, "%sLocationType%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getLocationType()))));
        }

        // add `IsoType` to the URL query string
        if (getIsoType() != null) {
            joiner.add(String.format(Locale.ROOT, "%sIsoType%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIsoType()))));
        }

        // add `MediaType` to the URL query string
        if (getMediaType() != null) {
            joiner.add(String.format(Locale.ROOT, "%sMediaType%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getMediaType()))));
        }

        // add `EndDate` to the URL query string
        if (getEndDate() != null) {
            joiner.add(String.format(Locale.ROOT, "%sEndDate%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEndDate()))));
        }

        // add `LockedFields` to the URL query string
        if (getLockedFields() != null) {
            for (int i = 0; i < getLockedFields().size(); i++) {
                if (getLockedFields().get(i) != null) {
                    joiner.add(String.format(Locale.ROOT, "%sLockedFields%s%s=%s", prefix, suffix,
                            "".equals(suffix) ? ""
                                    : String.format(Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                            ApiClient.urlEncode(ApiClient.valueToString(getLockedFields().get(i)))));
                }
            }
        }

        // add `TrailerCount` to the URL query string
        if (getTrailerCount() != null) {
            joiner.add(String.format(Locale.ROOT, "%sTrailerCount%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getTrailerCount()))));
        }

        // add `MovieCount` to the URL query string
        if (getMovieCount() != null) {
            joiner.add(String.format(Locale.ROOT, "%sMovieCount%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getMovieCount()))));
        }

        // add `SeriesCount` to the URL query string
        if (getSeriesCount() != null) {
            joiner.add(String.format(Locale.ROOT, "%sSeriesCount%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSeriesCount()))));
        }

        // add `ProgramCount` to the URL query string
        if (getProgramCount() != null) {
            joiner.add(String.format(Locale.ROOT, "%sProgramCount%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getProgramCount()))));
        }

        // add `EpisodeCount` to the URL query string
        if (getEpisodeCount() != null) {
            joiner.add(String.format(Locale.ROOT, "%sEpisodeCount%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEpisodeCount()))));
        }

        // add `SongCount` to the URL query string
        if (getSongCount() != null) {
            joiner.add(String.format(Locale.ROOT, "%sSongCount%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSongCount()))));
        }

        // add `AlbumCount` to the URL query string
        if (getAlbumCount() != null) {
            joiner.add(String.format(Locale.ROOT, "%sAlbumCount%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getAlbumCount()))));
        }

        // add `ArtistCount` to the URL query string
        if (getArtistCount() != null) {
            joiner.add(String.format(Locale.ROOT, "%sArtistCount%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getArtistCount()))));
        }

        // add `MusicVideoCount` to the URL query string
        if (getMusicVideoCount() != null) {
            joiner.add(String.format(Locale.ROOT, "%sMusicVideoCount%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getMusicVideoCount()))));
        }

        // add `LockData` to the URL query string
        if (getLockData() != null) {
            joiner.add(String.format(Locale.ROOT, "%sLockData%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getLockData()))));
        }

        // add `Width` to the URL query string
        if (getWidth() != null) {
            joiner.add(String.format(Locale.ROOT, "%sWidth%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getWidth()))));
        }

        // add `Height` to the URL query string
        if (getHeight() != null) {
            joiner.add(String.format(Locale.ROOT, "%sHeight%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getHeight()))));
        }

        // add `CameraMake` to the URL query string
        if (getCameraMake() != null) {
            joiner.add(String.format(Locale.ROOT, "%sCameraMake%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getCameraMake()))));
        }

        // add `CameraModel` to the URL query string
        if (getCameraModel() != null) {
            joiner.add(String.format(Locale.ROOT, "%sCameraModel%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getCameraModel()))));
        }

        // add `Software` to the URL query string
        if (getSoftware() != null) {
            joiner.add(String.format(Locale.ROOT, "%sSoftware%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSoftware()))));
        }

        // add `ExposureTime` to the URL query string
        if (getExposureTime() != null) {
            joiner.add(String.format(Locale.ROOT, "%sExposureTime%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getExposureTime()))));
        }

        // add `FocalLength` to the URL query string
        if (getFocalLength() != null) {
            joiner.add(String.format(Locale.ROOT, "%sFocalLength%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getFocalLength()))));
        }

        // add `ImageOrientation` to the URL query string
        if (getImageOrientation() != null) {
            joiner.add(String.format(Locale.ROOT, "%sImageOrientation%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getImageOrientation()))));
        }

        // add `Aperture` to the URL query string
        if (getAperture() != null) {
            joiner.add(String.format(Locale.ROOT, "%sAperture%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getAperture()))));
        }

        // add `ShutterSpeed` to the URL query string
        if (getShutterSpeed() != null) {
            joiner.add(String.format(Locale.ROOT, "%sShutterSpeed%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getShutterSpeed()))));
        }

        // add `Latitude` to the URL query string
        if (getLatitude() != null) {
            joiner.add(String.format(Locale.ROOT, "%sLatitude%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getLatitude()))));
        }

        // add `Longitude` to the URL query string
        if (getLongitude() != null) {
            joiner.add(String.format(Locale.ROOT, "%sLongitude%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getLongitude()))));
        }

        // add `Altitude` to the URL query string
        if (getAltitude() != null) {
            joiner.add(String.format(Locale.ROOT, "%sAltitude%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getAltitude()))));
        }

        // add `IsoSpeedRating` to the URL query string
        if (getIsoSpeedRating() != null) {
            joiner.add(String.format(Locale.ROOT, "%sIsoSpeedRating%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIsoSpeedRating()))));
        }

        // add `SeriesTimerId` to the URL query string
        if (getSeriesTimerId() != null) {
            joiner.add(String.format(Locale.ROOT, "%sSeriesTimerId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSeriesTimerId()))));
        }

        // add `ProgramId` to the URL query string
        if (getProgramId() != null) {
            joiner.add(String.format(Locale.ROOT, "%sProgramId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getProgramId()))));
        }

        // add `ChannelPrimaryImageTag` to the URL query string
        if (getChannelPrimaryImageTag() != null) {
            joiner.add(String.format(Locale.ROOT, "%sChannelPrimaryImageTag%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getChannelPrimaryImageTag()))));
        }

        // add `StartDate` to the URL query string
        if (getStartDate() != null) {
            joiner.add(String.format(Locale.ROOT, "%sStartDate%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getStartDate()))));
        }

        // add `CompletionPercentage` to the URL query string
        if (getCompletionPercentage() != null) {
            joiner.add(String.format(Locale.ROOT, "%sCompletionPercentage%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getCompletionPercentage()))));
        }

        // add `IsRepeat` to the URL query string
        if (getIsRepeat() != null) {
            joiner.add(String.format(Locale.ROOT, "%sIsRepeat%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIsRepeat()))));
        }

        // add `EpisodeTitle` to the URL query string
        if (getEpisodeTitle() != null) {
            joiner.add(String.format(Locale.ROOT, "%sEpisodeTitle%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEpisodeTitle()))));
        }

        // add `ChannelType` to the URL query string
        if (getChannelType() != null) {
            joiner.add(String.format(Locale.ROOT, "%sChannelType%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getChannelType()))));
        }

        // add `Audio` to the URL query string
        if (getAudio() != null) {
            joiner.add(String.format(Locale.ROOT, "%sAudio%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getAudio()))));
        }

        // add `IsMovie` to the URL query string
        if (getIsMovie() != null) {
            joiner.add(String.format(Locale.ROOT, "%sIsMovie%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIsMovie()))));
        }

        // add `IsSports` to the URL query string
        if (getIsSports() != null) {
            joiner.add(String.format(Locale.ROOT, "%sIsSports%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIsSports()))));
        }

        // add `IsSeries` to the URL query string
        if (getIsSeries() != null) {
            joiner.add(String.format(Locale.ROOT, "%sIsSeries%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIsSeries()))));
        }

        // add `IsLive` to the URL query string
        if (getIsLive() != null) {
            joiner.add(String.format(Locale.ROOT, "%sIsLive%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIsLive()))));
        }

        // add `IsNews` to the URL query string
        if (getIsNews() != null) {
            joiner.add(String.format(Locale.ROOT, "%sIsNews%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIsNews()))));
        }

        // add `IsKids` to the URL query string
        if (getIsKids() != null) {
            joiner.add(String.format(Locale.ROOT, "%sIsKids%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIsKids()))));
        }

        // add `IsPremiere` to the URL query string
        if (getIsPremiere() != null) {
            joiner.add(String.format(Locale.ROOT, "%sIsPremiere%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIsPremiere()))));
        }

        // add `TimerId` to the URL query string
        if (getTimerId() != null) {
            joiner.add(String.format(Locale.ROOT, "%sTimerId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getTimerId()))));
        }

        // add `NormalizationGain` to the URL query string
        if (getNormalizationGain() != null) {
            joiner.add(String.format(Locale.ROOT, "%sNormalizationGain%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getNormalizationGain()))));
        }

        // add `CurrentProgram` to the URL query string
        if (getCurrentProgram() != null) {
            joiner.add(getCurrentProgram().toUrlQueryString(prefix + "CurrentProgram" + suffix));
        }

        return joiner.toString();
    }

    public static class Builder {

        private BaseItemDto instance;

        public Builder() {
            this(new BaseItemDto());
        }

        protected Builder(BaseItemDto instance) {
            this.instance = instance;
        }

        public BaseItemDto.Builder name(String name) {
            this.instance.name = name;
            return this;
        }

        public BaseItemDto.Builder originalTitle(String originalTitle) {
            this.instance.originalTitle = originalTitle;
            return this;
        }

        public BaseItemDto.Builder serverId(String serverId) {
            this.instance.serverId = serverId;
            return this;
        }

        public BaseItemDto.Builder id(UUID id) {
            this.instance.id = id;
            return this;
        }

        public BaseItemDto.Builder etag(String etag) {
            this.instance.etag = etag;
            return this;
        }

        public BaseItemDto.Builder sourceType(String sourceType) {
            this.instance.sourceType = sourceType;
            return this;
        }

        public BaseItemDto.Builder playlistItemId(String playlistItemId) {
            this.instance.playlistItemId = playlistItemId;
            return this;
        }

        public BaseItemDto.Builder dateCreated(OffsetDateTime dateCreated) {
            this.instance.dateCreated = dateCreated;
            return this;
        }

        public BaseItemDto.Builder dateLastMediaAdded(OffsetDateTime dateLastMediaAdded) {
            this.instance.dateLastMediaAdded = dateLastMediaAdded;
            return this;
        }

        public BaseItemDto.Builder extraType(ExtraType extraType) {
            this.instance.extraType = extraType;
            return this;
        }

        public BaseItemDto.Builder airsBeforeSeasonNumber(Integer airsBeforeSeasonNumber) {
            this.instance.airsBeforeSeasonNumber = airsBeforeSeasonNumber;
            return this;
        }

        public BaseItemDto.Builder airsAfterSeasonNumber(Integer airsAfterSeasonNumber) {
            this.instance.airsAfterSeasonNumber = airsAfterSeasonNumber;
            return this;
        }

        public BaseItemDto.Builder airsBeforeEpisodeNumber(Integer airsBeforeEpisodeNumber) {
            this.instance.airsBeforeEpisodeNumber = airsBeforeEpisodeNumber;
            return this;
        }

        public BaseItemDto.Builder canDelete(Boolean canDelete) {
            this.instance.canDelete = canDelete;
            return this;
        }

        public BaseItemDto.Builder canDownload(Boolean canDownload) {
            this.instance.canDownload = canDownload;
            return this;
        }

        public BaseItemDto.Builder hasLyrics(Boolean hasLyrics) {
            this.instance.hasLyrics = hasLyrics;
            return this;
        }

        public BaseItemDto.Builder hasSubtitles(Boolean hasSubtitles) {
            this.instance.hasSubtitles = hasSubtitles;
            return this;
        }

        public BaseItemDto.Builder preferredMetadataLanguage(String preferredMetadataLanguage) {
            this.instance.preferredMetadataLanguage = preferredMetadataLanguage;
            return this;
        }

        public BaseItemDto.Builder preferredMetadataCountryCode(String preferredMetadataCountryCode) {
            this.instance.preferredMetadataCountryCode = preferredMetadataCountryCode;
            return this;
        }

        public BaseItemDto.Builder container(String container) {
            this.instance.container = container;
            return this;
        }

        public BaseItemDto.Builder sortName(String sortName) {
            this.instance.sortName = sortName;
            return this;
        }

        public BaseItemDto.Builder forcedSortName(String forcedSortName) {
            this.instance.forcedSortName = forcedSortName;
            return this;
        }

        public BaseItemDto.Builder video3DFormat(Video3DFormat video3DFormat) {
            this.instance.video3DFormat = video3DFormat;
            return this;
        }

        public BaseItemDto.Builder premiereDate(OffsetDateTime premiereDate) {
            this.instance.premiereDate = premiereDate;
            return this;
        }

        public BaseItemDto.Builder externalUrls(List<ExternalUrl> externalUrls) {
            this.instance.externalUrls = externalUrls;
            return this;
        }

        public BaseItemDto.Builder mediaSources(List<MediaSourceInfo> mediaSources) {
            this.instance.mediaSources = mediaSources;
            return this;
        }

        public BaseItemDto.Builder criticRating(Float criticRating) {
            this.instance.criticRating = criticRating;
            return this;
        }

        public BaseItemDto.Builder productionLocations(List<String> productionLocations) {
            this.instance.productionLocations = productionLocations;
            return this;
        }

        public BaseItemDto.Builder path(String path) {
            this.instance.path = path;
            return this;
        }

        public BaseItemDto.Builder enableMediaSourceDisplay(Boolean enableMediaSourceDisplay) {
            this.instance.enableMediaSourceDisplay = enableMediaSourceDisplay;
            return this;
        }

        public BaseItemDto.Builder officialRating(String officialRating) {
            this.instance.officialRating = officialRating;
            return this;
        }

        public BaseItemDto.Builder customRating(String customRating) {
            this.instance.customRating = customRating;
            return this;
        }

        public BaseItemDto.Builder channelId(UUID channelId) {
            this.instance.channelId = channelId;
            return this;
        }

        public BaseItemDto.Builder channelName(String channelName) {
            this.instance.channelName = channelName;
            return this;
        }

        public BaseItemDto.Builder overview(String overview) {
            this.instance.overview = overview;
            return this;
        }

        public BaseItemDto.Builder taglines(List<String> taglines) {
            this.instance.taglines = taglines;
            return this;
        }

        public BaseItemDto.Builder genres(List<String> genres) {
            this.instance.genres = genres;
            return this;
        }

        public BaseItemDto.Builder communityRating(Float communityRating) {
            this.instance.communityRating = communityRating;
            return this;
        }

        public BaseItemDto.Builder cumulativeRunTimeTicks(Long cumulativeRunTimeTicks) {
            this.instance.cumulativeRunTimeTicks = cumulativeRunTimeTicks;
            return this;
        }

        public BaseItemDto.Builder runTimeTicks(Long runTimeTicks) {
            this.instance.runTimeTicks = runTimeTicks;
            return this;
        }

        public BaseItemDto.Builder playAccess(PlayAccess playAccess) {
            this.instance.playAccess = playAccess;
            return this;
        }

        public BaseItemDto.Builder aspectRatio(String aspectRatio) {
            this.instance.aspectRatio = aspectRatio;
            return this;
        }

        public BaseItemDto.Builder productionYear(Integer productionYear) {
            this.instance.productionYear = productionYear;
            return this;
        }

        public BaseItemDto.Builder isPlaceHolder(Boolean isPlaceHolder) {
            this.instance.isPlaceHolder = isPlaceHolder;
            return this;
        }

        public BaseItemDto.Builder number(String number) {
            this.instance.number = number;
            return this;
        }

        public BaseItemDto.Builder channelNumber(String channelNumber) {
            this.instance.channelNumber = channelNumber;
            return this;
        }

        public BaseItemDto.Builder indexNumber(Integer indexNumber) {
            this.instance.indexNumber = indexNumber;
            return this;
        }

        public BaseItemDto.Builder indexNumberEnd(Integer indexNumberEnd) {
            this.instance.indexNumberEnd = indexNumberEnd;
            return this;
        }

        public BaseItemDto.Builder parentIndexNumber(Integer parentIndexNumber) {
            this.instance.parentIndexNumber = parentIndexNumber;
            return this;
        }

        public BaseItemDto.Builder remoteTrailers(List<MediaUrl> remoteTrailers) {
            this.instance.remoteTrailers = remoteTrailers;
            return this;
        }

        public BaseItemDto.Builder providerIds(Map<String, String> providerIds) {
            this.instance.providerIds = providerIds;
            return this;
        }

        public BaseItemDto.Builder isHD(Boolean isHD) {
            this.instance.isHD = isHD;
            return this;
        }

        public BaseItemDto.Builder isFolder(Boolean isFolder) {
            this.instance.isFolder = isFolder;
            return this;
        }

        public BaseItemDto.Builder parentId(UUID parentId) {
            this.instance.parentId = parentId;
            return this;
        }

        public BaseItemDto.Builder type(BaseItemKind type) {
            this.instance.type = type;
            return this;
        }

        public BaseItemDto.Builder people(List<BaseItemPerson> people) {
            this.instance.people = people;
            return this;
        }

        public BaseItemDto.Builder studios(List<NameGuidPair> studios) {
            this.instance.studios = studios;
            return this;
        }

        public BaseItemDto.Builder genreItems(List<NameGuidPair> genreItems) {
            this.instance.genreItems = genreItems;
            return this;
        }

        public BaseItemDto.Builder parentLogoItemId(UUID parentLogoItemId) {
            this.instance.parentLogoItemId = parentLogoItemId;
            return this;
        }

        public BaseItemDto.Builder parentBackdropItemId(UUID parentBackdropItemId) {
            this.instance.parentBackdropItemId = parentBackdropItemId;
            return this;
        }

        public BaseItemDto.Builder parentBackdropImageTags(List<String> parentBackdropImageTags) {
            this.instance.parentBackdropImageTags = parentBackdropImageTags;
            return this;
        }

        public BaseItemDto.Builder localTrailerCount(Integer localTrailerCount) {
            this.instance.localTrailerCount = localTrailerCount;
            return this;
        }

        public BaseItemDto.Builder userData(UserItemDataDto userData) {
            this.instance.userData = userData;
            return this;
        }

        public BaseItemDto.Builder recursiveItemCount(Integer recursiveItemCount) {
            this.instance.recursiveItemCount = recursiveItemCount;
            return this;
        }

        public BaseItemDto.Builder childCount(Integer childCount) {
            this.instance.childCount = childCount;
            return this;
        }

        public BaseItemDto.Builder seriesName(String seriesName) {
            this.instance.seriesName = seriesName;
            return this;
        }

        public BaseItemDto.Builder seriesId(UUID seriesId) {
            this.instance.seriesId = seriesId;
            return this;
        }

        public BaseItemDto.Builder seasonId(UUID seasonId) {
            this.instance.seasonId = seasonId;
            return this;
        }

        public BaseItemDto.Builder specialFeatureCount(Integer specialFeatureCount) {
            this.instance.specialFeatureCount = specialFeatureCount;
            return this;
        }

        public BaseItemDto.Builder displayPreferencesId(String displayPreferencesId) {
            this.instance.displayPreferencesId = displayPreferencesId;
            return this;
        }

        public BaseItemDto.Builder status(String status) {
            this.instance.status = status;
            return this;
        }

        public BaseItemDto.Builder airTime(String airTime) {
            this.instance.airTime = airTime;
            return this;
        }

        public BaseItemDto.Builder airDays(List<DayOfWeek> airDays) {
            this.instance.airDays = airDays;
            return this;
        }

        public BaseItemDto.Builder tags(List<String> tags) {
            this.instance.tags = tags;
            return this;
        }

        public BaseItemDto.Builder primaryImageAspectRatio(Double primaryImageAspectRatio) {
            this.instance.primaryImageAspectRatio = primaryImageAspectRatio;
            return this;
        }

        public BaseItemDto.Builder artists(List<String> artists) {
            this.instance.artists = artists;
            return this;
        }

        public BaseItemDto.Builder artistItems(List<NameGuidPair> artistItems) {
            this.instance.artistItems = artistItems;
            return this;
        }

        public BaseItemDto.Builder album(String album) {
            this.instance.album = album;
            return this;
        }

        public BaseItemDto.Builder collectionType(CollectionType collectionType) {
            this.instance.collectionType = collectionType;
            return this;
        }

        public BaseItemDto.Builder displayOrder(String displayOrder) {
            this.instance.displayOrder = displayOrder;
            return this;
        }

        public BaseItemDto.Builder albumId(UUID albumId) {
            this.instance.albumId = albumId;
            return this;
        }

        public BaseItemDto.Builder albumPrimaryImageTag(String albumPrimaryImageTag) {
            this.instance.albumPrimaryImageTag = albumPrimaryImageTag;
            return this;
        }

        public BaseItemDto.Builder seriesPrimaryImageTag(String seriesPrimaryImageTag) {
            this.instance.seriesPrimaryImageTag = seriesPrimaryImageTag;
            return this;
        }

        public BaseItemDto.Builder albumArtist(String albumArtist) {
            this.instance.albumArtist = albumArtist;
            return this;
        }

        public BaseItemDto.Builder albumArtists(List<NameGuidPair> albumArtists) {
            this.instance.albumArtists = albumArtists;
            return this;
        }

        public BaseItemDto.Builder seasonName(String seasonName) {
            this.instance.seasonName = seasonName;
            return this;
        }

        public BaseItemDto.Builder mediaStreams(List<MediaStream> mediaStreams) {
            this.instance.mediaStreams = mediaStreams;
            return this;
        }

        public BaseItemDto.Builder videoType(VideoType videoType) {
            this.instance.videoType = videoType;
            return this;
        }

        public BaseItemDto.Builder partCount(Integer partCount) {
            this.instance.partCount = partCount;
            return this;
        }

        public BaseItemDto.Builder mediaSourceCount(Integer mediaSourceCount) {
            this.instance.mediaSourceCount = mediaSourceCount;
            return this;
        }

        public BaseItemDto.Builder imageTags(Map<String, String> imageTags) {
            this.instance.imageTags = imageTags;
            return this;
        }

        public BaseItemDto.Builder backdropImageTags(List<String> backdropImageTags) {
            this.instance.backdropImageTags = backdropImageTags;
            return this;
        }

        public BaseItemDto.Builder screenshotImageTags(List<String> screenshotImageTags) {
            this.instance.screenshotImageTags = screenshotImageTags;
            return this;
        }

        public BaseItemDto.Builder parentLogoImageTag(String parentLogoImageTag) {
            this.instance.parentLogoImageTag = parentLogoImageTag;
            return this;
        }

        public BaseItemDto.Builder parentArtItemId(UUID parentArtItemId) {
            this.instance.parentArtItemId = parentArtItemId;
            return this;
        }

        public BaseItemDto.Builder parentArtImageTag(String parentArtImageTag) {
            this.instance.parentArtImageTag = parentArtImageTag;
            return this;
        }

        public BaseItemDto.Builder seriesThumbImageTag(String seriesThumbImageTag) {
            this.instance.seriesThumbImageTag = seriesThumbImageTag;
            return this;
        }

        public BaseItemDto.Builder imageBlurHashes(BaseItemDtoImageBlurHashes imageBlurHashes) {
            this.instance.imageBlurHashes = imageBlurHashes;
            return this;
        }

        public BaseItemDto.Builder seriesStudio(String seriesStudio) {
            this.instance.seriesStudio = seriesStudio;
            return this;
        }

        public BaseItemDto.Builder parentThumbItemId(UUID parentThumbItemId) {
            this.instance.parentThumbItemId = parentThumbItemId;
            return this;
        }

        public BaseItemDto.Builder parentThumbImageTag(String parentThumbImageTag) {
            this.instance.parentThumbImageTag = parentThumbImageTag;
            return this;
        }

        public BaseItemDto.Builder parentPrimaryImageItemId(UUID parentPrimaryImageItemId) {
            this.instance.parentPrimaryImageItemId = parentPrimaryImageItemId;
            return this;
        }

        public BaseItemDto.Builder parentPrimaryImageTag(String parentPrimaryImageTag) {
            this.instance.parentPrimaryImageTag = parentPrimaryImageTag;
            return this;
        }

        public BaseItemDto.Builder chapters(List<ChapterInfo> chapters) {
            this.instance.chapters = chapters;
            return this;
        }

        public BaseItemDto.Builder trickplay(Map<String, Map<String, TrickplayInfoDto>> trickplay) {
            this.instance.trickplay = trickplay;
            return this;
        }

        public BaseItemDto.Builder locationType(LocationType locationType) {
            this.instance.locationType = locationType;
            return this;
        }

        public BaseItemDto.Builder isoType(IsoType isoType) {
            this.instance.isoType = isoType;
            return this;
        }

        public BaseItemDto.Builder mediaType(MediaType mediaType) {
            this.instance.mediaType = mediaType;
            return this;
        }

        public BaseItemDto.Builder endDate(OffsetDateTime endDate) {
            this.instance.endDate = endDate;
            return this;
        }

        public BaseItemDto.Builder lockedFields(List<MetadataField> lockedFields) {
            this.instance.lockedFields = lockedFields;
            return this;
        }

        public BaseItemDto.Builder trailerCount(Integer trailerCount) {
            this.instance.trailerCount = trailerCount;
            return this;
        }

        public BaseItemDto.Builder movieCount(Integer movieCount) {
            this.instance.movieCount = movieCount;
            return this;
        }

        public BaseItemDto.Builder seriesCount(Integer seriesCount) {
            this.instance.seriesCount = seriesCount;
            return this;
        }

        public BaseItemDto.Builder programCount(Integer programCount) {
            this.instance.programCount = programCount;
            return this;
        }

        public BaseItemDto.Builder episodeCount(Integer episodeCount) {
            this.instance.episodeCount = episodeCount;
            return this;
        }

        public BaseItemDto.Builder songCount(Integer songCount) {
            this.instance.songCount = songCount;
            return this;
        }

        public BaseItemDto.Builder albumCount(Integer albumCount) {
            this.instance.albumCount = albumCount;
            return this;
        }

        public BaseItemDto.Builder artistCount(Integer artistCount) {
            this.instance.artistCount = artistCount;
            return this;
        }

        public BaseItemDto.Builder musicVideoCount(Integer musicVideoCount) {
            this.instance.musicVideoCount = musicVideoCount;
            return this;
        }

        public BaseItemDto.Builder lockData(Boolean lockData) {
            this.instance.lockData = lockData;
            return this;
        }

        public BaseItemDto.Builder width(Integer width) {
            this.instance.width = width;
            return this;
        }

        public BaseItemDto.Builder height(Integer height) {
            this.instance.height = height;
            return this;
        }

        public BaseItemDto.Builder cameraMake(String cameraMake) {
            this.instance.cameraMake = cameraMake;
            return this;
        }

        public BaseItemDto.Builder cameraModel(String cameraModel) {
            this.instance.cameraModel = cameraModel;
            return this;
        }

        public BaseItemDto.Builder software(String software) {
            this.instance.software = software;
            return this;
        }

        public BaseItemDto.Builder exposureTime(Double exposureTime) {
            this.instance.exposureTime = exposureTime;
            return this;
        }

        public BaseItemDto.Builder focalLength(Double focalLength) {
            this.instance.focalLength = focalLength;
            return this;
        }

        public BaseItemDto.Builder imageOrientation(ImageOrientation imageOrientation) {
            this.instance.imageOrientation = imageOrientation;
            return this;
        }

        public BaseItemDto.Builder aperture(Double aperture) {
            this.instance.aperture = aperture;
            return this;
        }

        public BaseItemDto.Builder shutterSpeed(Double shutterSpeed) {
            this.instance.shutterSpeed = shutterSpeed;
            return this;
        }

        public BaseItemDto.Builder latitude(Double latitude) {
            this.instance.latitude = latitude;
            return this;
        }

        public BaseItemDto.Builder longitude(Double longitude) {
            this.instance.longitude = longitude;
            return this;
        }

        public BaseItemDto.Builder altitude(Double altitude) {
            this.instance.altitude = altitude;
            return this;
        }

        public BaseItemDto.Builder isoSpeedRating(Integer isoSpeedRating) {
            this.instance.isoSpeedRating = isoSpeedRating;
            return this;
        }

        public BaseItemDto.Builder seriesTimerId(String seriesTimerId) {
            this.instance.seriesTimerId = seriesTimerId;
            return this;
        }

        public BaseItemDto.Builder programId(String programId) {
            this.instance.programId = programId;
            return this;
        }

        public BaseItemDto.Builder channelPrimaryImageTag(String channelPrimaryImageTag) {
            this.instance.channelPrimaryImageTag = channelPrimaryImageTag;
            return this;
        }

        public BaseItemDto.Builder startDate(OffsetDateTime startDate) {
            this.instance.startDate = startDate;
            return this;
        }

        public BaseItemDto.Builder completionPercentage(Double completionPercentage) {
            this.instance.completionPercentage = completionPercentage;
            return this;
        }

        public BaseItemDto.Builder isRepeat(Boolean isRepeat) {
            this.instance.isRepeat = isRepeat;
            return this;
        }

        public BaseItemDto.Builder episodeTitle(String episodeTitle) {
            this.instance.episodeTitle = episodeTitle;
            return this;
        }

        public BaseItemDto.Builder channelType(ChannelType channelType) {
            this.instance.channelType = channelType;
            return this;
        }

        public BaseItemDto.Builder audio(ProgramAudio audio) {
            this.instance.audio = audio;
            return this;
        }

        public BaseItemDto.Builder isMovie(Boolean isMovie) {
            this.instance.isMovie = isMovie;
            return this;
        }

        public BaseItemDto.Builder isSports(Boolean isSports) {
            this.instance.isSports = isSports;
            return this;
        }

        public BaseItemDto.Builder isSeries(Boolean isSeries) {
            this.instance.isSeries = isSeries;
            return this;
        }

        public BaseItemDto.Builder isLive(Boolean isLive) {
            this.instance.isLive = isLive;
            return this;
        }

        public BaseItemDto.Builder isNews(Boolean isNews) {
            this.instance.isNews = isNews;
            return this;
        }

        public BaseItemDto.Builder isKids(Boolean isKids) {
            this.instance.isKids = isKids;
            return this;
        }

        public BaseItemDto.Builder isPremiere(Boolean isPremiere) {
            this.instance.isPremiere = isPremiere;
            return this;
        }

        public BaseItemDto.Builder timerId(String timerId) {
            this.instance.timerId = timerId;
            return this;
        }

        public BaseItemDto.Builder normalizationGain(Float normalizationGain) {
            this.instance.normalizationGain = normalizationGain;
            return this;
        }

        public BaseItemDto.Builder currentProgram(BaseItemDto currentProgram) {
            this.instance.currentProgram = currentProgram;
            return this;
        }

        /**
         * returns a built BaseItemDto instance.
         *
         * The builder is not reusable.
         */
        public BaseItemDto build() {
            try {
                return this.instance;
            } finally {
                // ensure that this.instance is not reused
                this.instance = null;
            }
        }

        @Override
        public String toString() {
            return getClass() + "=(" + instance + ")";
        }
    }

    /**
     * Create a builder with no initialized field.
     */
    public static BaseItemDto.Builder builder() {
        return new BaseItemDto.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public BaseItemDto.Builder toBuilder() {
        return new BaseItemDto.Builder().name(getName()).originalTitle(getOriginalTitle()).serverId(getServerId())
                .id(getId()).etag(getEtag()).sourceType(getSourceType()).playlistItemId(getPlaylistItemId())
                .dateCreated(getDateCreated()).dateLastMediaAdded(getDateLastMediaAdded()).extraType(getExtraType())
                .airsBeforeSeasonNumber(getAirsBeforeSeasonNumber()).airsAfterSeasonNumber(getAirsAfterSeasonNumber())
                .airsBeforeEpisodeNumber(getAirsBeforeEpisodeNumber()).canDelete(getCanDelete())
                .canDownload(getCanDownload()).hasLyrics(getHasLyrics()).hasSubtitles(getHasSubtitles())
                .preferredMetadataLanguage(getPreferredMetadataLanguage())
                .preferredMetadataCountryCode(getPreferredMetadataCountryCode()).container(getContainer())
                .sortName(getSortName()).forcedSortName(getForcedSortName()).video3DFormat(getVideo3DFormat())
                .premiereDate(getPremiereDate()).externalUrls(getExternalUrls()).mediaSources(getMediaSources())
                .criticRating(getCriticRating()).productionLocations(getProductionLocations()).path(getPath())
                .enableMediaSourceDisplay(getEnableMediaSourceDisplay()).officialRating(getOfficialRating())
                .customRating(getCustomRating()).channelId(getChannelId()).channelName(getChannelName())
                .overview(getOverview()).taglines(getTaglines()).genres(getGenres())
                .communityRating(getCommunityRating()).cumulativeRunTimeTicks(getCumulativeRunTimeTicks())
                .runTimeTicks(getRunTimeTicks()).playAccess(getPlayAccess()).aspectRatio(getAspectRatio())
                .productionYear(getProductionYear()).isPlaceHolder(getIsPlaceHolder()).number(getNumber())
                .channelNumber(getChannelNumber()).indexNumber(getIndexNumber()).indexNumberEnd(getIndexNumberEnd())
                .parentIndexNumber(getParentIndexNumber()).remoteTrailers(getRemoteTrailers())
                .providerIds(getProviderIds()).isHD(getIsHD()).isFolder(getIsFolder()).parentId(getParentId())
                .type(getType()).people(getPeople()).studios(getStudios()).genreItems(getGenreItems())
                .parentLogoItemId(getParentLogoItemId()).parentBackdropItemId(getParentBackdropItemId())
                .parentBackdropImageTags(getParentBackdropImageTags()).localTrailerCount(getLocalTrailerCount())
                .userData(getUserData()).recursiveItemCount(getRecursiveItemCount()).childCount(getChildCount())
                .seriesName(getSeriesName()).seriesId(getSeriesId()).seasonId(getSeasonId())
                .specialFeatureCount(getSpecialFeatureCount()).displayPreferencesId(getDisplayPreferencesId())
                .status(getStatus()).airTime(getAirTime()).airDays(getAirDays()).tags(getTags())
                .primaryImageAspectRatio(getPrimaryImageAspectRatio()).artists(getArtists())
                .artistItems(getArtistItems()).album(getAlbum()).collectionType(getCollectionType())
                .displayOrder(getDisplayOrder()).albumId(getAlbumId()).albumPrimaryImageTag(getAlbumPrimaryImageTag())
                .seriesPrimaryImageTag(getSeriesPrimaryImageTag()).albumArtist(getAlbumArtist())
                .albumArtists(getAlbumArtists()).seasonName(getSeasonName()).mediaStreams(getMediaStreams())
                .videoType(getVideoType()).partCount(getPartCount()).mediaSourceCount(getMediaSourceCount())
                .imageTags(getImageTags()).backdropImageTags(getBackdropImageTags())
                .screenshotImageTags(getScreenshotImageTags()).parentLogoImageTag(getParentLogoImageTag())
                .parentArtItemId(getParentArtItemId()).parentArtImageTag(getParentArtImageTag())
                .seriesThumbImageTag(getSeriesThumbImageTag()).imageBlurHashes(getImageBlurHashes())
                .seriesStudio(getSeriesStudio()).parentThumbItemId(getParentThumbItemId())
                .parentThumbImageTag(getParentThumbImageTag()).parentPrimaryImageItemId(getParentPrimaryImageItemId())
                .parentPrimaryImageTag(getParentPrimaryImageTag()).chapters(getChapters()).trickplay(getTrickplay())
                .locationType(getLocationType()).isoType(getIsoType()).mediaType(getMediaType()).endDate(getEndDate())
                .lockedFields(getLockedFields()).trailerCount(getTrailerCount()).movieCount(getMovieCount())
                .seriesCount(getSeriesCount()).programCount(getProgramCount()).episodeCount(getEpisodeCount())
                .songCount(getSongCount()).albumCount(getAlbumCount()).artistCount(getArtistCount())
                .musicVideoCount(getMusicVideoCount()).lockData(getLockData()).width(getWidth()).height(getHeight())
                .cameraMake(getCameraMake()).cameraModel(getCameraModel()).software(getSoftware())
                .exposureTime(getExposureTime()).focalLength(getFocalLength()).imageOrientation(getImageOrientation())
                .aperture(getAperture()).shutterSpeed(getShutterSpeed()).latitude(getLatitude())
                .longitude(getLongitude()).altitude(getAltitude()).isoSpeedRating(getIsoSpeedRating())
                .seriesTimerId(getSeriesTimerId()).programId(getProgramId())
                .channelPrimaryImageTag(getChannelPrimaryImageTag()).startDate(getStartDate())
                .completionPercentage(getCompletionPercentage()).isRepeat(getIsRepeat()).episodeTitle(getEpisodeTitle())
                .channelType(getChannelType()).audio(getAudio()).isMovie(getIsMovie()).isSports(getIsSports())
                .isSeries(getIsSeries()).isLive(getIsLive()).isNews(getIsNews()).isKids(getIsKids())
                .isPremiere(getIsPremiere()).timerId(getTimerId()).normalizationGain(getNormalizationGain())
                .currentProgram(getCurrentProgram());
    }
}
