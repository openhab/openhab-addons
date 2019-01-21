;
(function() {
    'use strict';

    angular.module('PaperUI.items').directive('metadata', MetaData);

    function MetaData() {
        return {
            restrict : 'E',
            scope : {},
            bindToController : {
                configDescription : '=',
                metadata : '='
            },
            controllerAs : '$ctrl',
            templateUrl : 'partials/items/directive.metadata-details.html',
            controller : MetadataDetailsController
        }
    }

    MetadataDetailsController.$inject = [ '$scope', '$mdDialog', 'metadataService', 'configDescriptionService' ];

    function MetadataDetailsController($scope, $mdDialog, metadataService, configDescriptionService) {
        var ctrl = this;
        this.namespace = metadataService.URI2Namespace(this.configDescription.uri);
        this.mainParameter = this.configDescription.parameters[0];
        this.configParameterDescription = undefined;

        this.hasOptions = hasOptions;
        this.hasParameters = hasParameters;
        this.editParameters = editParameters;

        this.$onInit = activate;

        function activate() {
            // watch changes to the main metadata value and load the corresponding
            // config description for its configuration:
            $scope.$watch(function watchMetadataValue(scope) {
                return ctrl.metadata ? ctrl.metadata.value : undefined;
            }, function handleMetadataValueChange(newValue, oldValue) {
                if (!ctrl.metadata.value || ctrl.metadata.value === '') {
                    return;
                }

                var value = ctrl.metadata.value.replace(/\s/g, '_');

                return configDescriptionService.getByUri({
                    uri : 'metadata:' + ctrl.namespace + ":" + value
                }).$promise.then(function success(configDescription) {
                    ctrl.configParameterDescription = configDescription;
                    // once a valid config description is found for the new metadata value,
                    // we clear the configuration:
                    if (oldValue && oldValue != newValue) {
                        ctrl.metadata.config = {};
                    }
                }, function error() {
                    ctrl.configParameterDescription = undefined;
                });
            });
        }

        function hasOptions() {
            return ctrl.mainParameter.options && ctrl.mainParameter.options.length > 0;
        }

        function hasParameters() {
            return ctrl.configParameterDescription != undefined;
        }

        function editParameters(event) {
            $mdDialog.show({
                controller : 'ConfigurableMetadataDialogController',
                controllerAs : '$ctrl',
                templateUrl : 'partials/items/dialog.configure-metadata.html',
                targetEvent : event,
                hasBackdrop : true,
                locals : {
                    metadata : ctrl.metadata,
                    configDescription : ctrl.configParameterDescription
                }
            });
        }

    }

})();