;
(function() {
    'use strict';

    angular.module('PaperUI.bindings').component('bindingDetail', {
        templateUrl : 'partials/bindings/directive.binding-details.html',
        controller : BindingDetailsController
    });

    BindingDetailsController.$inject = [ '$routeParams', '$location', '$mdExpansionPanel', '$mdDialog', 'thingTypeRepository', 'bindingRepository', 'titleService' ];

    function BindingDetailsController($routeParams, $location, $mdExpansionPanel, $mdDialog, thingTypeRepository, bindingRepository, titleService) {
        var ctrl = this;
        this.binding;

        this.isConfigurable = isConfigurable;
        this.configure = configure;
        this.navigateTo = navigateTo;

        this.$onInit = activate;

        function activate() {
            titleService.setTitle('Configuration');

            var bindingId = $routeParams.bindingId;

            bindingRepository.getOne(function(binding) {
                return binding.id === bindingId;
            }, function(binding) {
                titleService.setSubtitles([ 'Bindings', binding.name ]);

                ctrl.binding = binding;
                ctrl.binding.thingTypes = [];
                thingTypeRepository.getAll(function(thingTypes) {
                    angular.forEach(thingTypes, function(thingType) {
                        if (thingType.UID.split(':')[0] === binding.id) {
                            ctrl.binding.thingTypes.push(thingType);
                        }
                    });
                });
            });
        }

        function navigateTo(path) {
            $location.path(path);
        }

        function configure(event) {
            $mdDialog.show({
                controller : 'ConfigureBindingDialogController',
                templateUrl : 'partials/bindings/dialog.configurebinding.html',
                targetEvent : event,
                hasBackdrop : true,
                locals : {
                    binding : ctrl.binding
                }
            });
        }

        function isConfigurable() {
            return ctrl.binding.configDescriptionURI ? true : false;
        }
    }
})();
