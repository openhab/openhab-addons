;
(function() {
    'use strict';

    angular.module('PaperUI.items').factory('metadataService', MetadataService);

    function MetadataService() {
        return {
            URI2Namespace : URI2Namespace
        }
    }

    function URI2Namespace(uri) {
        var segments = uri.split(':');
        if (segments.length > 1) {
            return segments[1];
        }

        return undefined;
    }

})();