;
(function() {
    'use strict';

    angular.module('PaperUI.items').directive('metadataList', MetaDataList);

    function MetaDataList() {
        return {
            restrict : 'E',
            scope : {},
            bindToController : {
                item : '='
            },
            controllerAs : '$ctrl',
            templateUrl : 'partials/items/directive.metadata-list.html',
            controller : MetaDataListController
        }
    }

    MetaDataListController.$inject = [ 'configDescriptionService', 'metadataService' ];

    function MetaDataListController(configDescriptionService, metadataService) {
        var ctrl = this;
        this.metadataConfigDescriptions;
        this.URI2Namespace = metadataService.URI2Namespace

        this.$onInit = activate;

        function activate() {
            return configDescriptionService.getAll({
                scheme : 'metadata'
            }, function(metadataConfigDescriptions) {
                ctrl.metadataConfigDescriptions = metadataConfigDescriptions;

                if (!ctrl.item.metadata) {
                    ctrl.item.metadata = {};
                }

                ctrl.metadataConfigDescriptions.forEach(function(metadataConfigDescription) {
                    var namespace = ctrl.URI2Namespace(metadataConfigDescription.uri);
                    if (!ctrl.item.metadata[namespace]) {
                        ctrl.item.metadata[namespace] = {
                            value : undefined,
                            config : {}
                        }
                    }
                })

            }).$promise;
        }
    }

})();