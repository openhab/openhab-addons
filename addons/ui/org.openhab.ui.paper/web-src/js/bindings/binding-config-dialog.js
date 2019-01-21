;
(function() {
    'use strict';

    angular.module('PaperUI.bindings') //
    .controller('ConfigureBindingDialogController', ConfigureBindingDialogController);

    ConfigureBindingDialogController.$inject = [ '$scope', '$mdDialog', 'bindingRepository', 'bindingService', 'configService', 'configDescriptionService', 'toastService', 'binding' ];

    function ConfigureBindingDialogController($scope, $mdDialog, bindingRepository, bindingService, configService, configDescriptionService, toastService, binding) {
        $scope.binding = binding;
        $scope.parameters = [];
        $scope.configuration = {};
        $scope.configArray = [];
        $scope.newConfig = false;
        $scope.expertMode = false;

        $scope.addParameter = addParameter;
        $scope.save = save;
        $scope.close = close;

        activate();

        function activate() {
            if (binding.configDescriptionURI) {
                configDescriptionService.getByUri({
                    uri : binding.configDescriptionURI
                }).$promise.then(function success(configDescription) {
                    if (configDescription) {
                        $scope.parameters = configService.getRenderingModel(configDescription.parameters, configDescription.parameterGroups);
                    }
                }).then(function() {
                    createConfiguration(binding);
                });
            } else {
                createConfiguration(binding);
            }
        }

        $scope.$watch('expertMode', function() {
            if ($scope.expertMode) {
                $scope.configArray = configService.getConfigAsArray($scope.configuration, $scope.parameters);
            } else {
                $scope.configuration = configService.getConfigAsObject($scope.configArray, $scope.parameters);
            }
        });

        function createConfiguration(binding) {
            if (binding) {
                bindingService.getConfigById({
                    id : binding.id
                }).$promise.then(function(config) {
                    $scope.configuration = configService.convertValues(config);
                    $scope.configuration = configService.setConfigDefaults($scope.configuration, $scope.parameters);
                    $scope.configArray = configService.getConfigAsArray($scope.configuration, $scope.parameters);

                }, function(failed) {
                    $scope.configuration = {};
                    $scope.configArray = [];
                });
            } else {
                $scope.newConfig = true;
                $scope.expertMode = true;
                $scope.configuration = {
                    '' : ''
                };
                $scope.configArray = [];
            }
        }

        function close() {
            $mdDialog.hide();
        }

        function addParameter() {
            $scope.configArray.push({
                name : '',
                value : undefined
            });
        }

        function save() {
            if ($scope.expertMode) {
                $scope.configuration = configService.getConfigAsObject($scope.configArray, $scope.parameters);
            }
            var configuration = configService.setConfigDefaults($scope.configuration, $scope.parameters, true);
            bindingService.updateConfig({
                id : binding.id
            }, configuration, function() {
                $mdDialog.hide();
                toastService.showDefaultToast('Binding config updated.');
            });
        }
    }
})();
