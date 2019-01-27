/* This file can be be overridden by fragments to include custom JS code */
angular.module('PaperUI.extensions', []) //

.service('mapSourceService', function() {
    var OSMSource = new ol.source.OSM({
        tileLoadFunction : function(tile, src) {
            tile.getImage().src = src + "?openhab";
        }
    });

    return {
        getMapSource : function() {
            return OSMSource;
        }
    };
})
