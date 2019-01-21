/* This file can be be overridden by fragments to include custom JS code */
angular.module('PaperUI.extensions', []) //

// default implementation of the map source service which does return "undefined"
// to disable map display in pure ESH framework. Replace "undefined" by
// "new ol.source.OSM()" to temporarily activate OpenStreetMap source.
.service('mapSourceService', function() {
    return {
        getMapSource : function() {
            return undefined
        }
    }
})
