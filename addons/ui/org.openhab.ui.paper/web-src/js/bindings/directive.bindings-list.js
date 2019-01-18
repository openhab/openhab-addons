;
(function() {
    'use strict';

    angular.module('PaperUI.bindings').component('bindingsList', {
        templateUrl : 'partials/bindings/directive.bindings-list.html',
        controller : BindingsListController
    });

    BindingsListController.$inject = [ '$location', 'extensionService', '$mdDialog', 'bindingRepository', 'titleService' ];

    function BindingsListController($location, extensionService, $mdDialog, bindingRepository, titleService) {
        var ctrl = this;
        this.bindings = [];
        this.extensionServiceAvailable = false;

        this.navigateTo = navigateTo;
        this.configure = configure;
        this.isConfigurable = isConfigurable;
        this.refresh = activate;

        extensionService.isAvailable(function(available) {
            ctrl.extensionServiceAvailable = available;
        })

        titleService.setTitle('Configuration');
        titleService.setSubtitles([ 'Bindings' ]);

        activate();

        function activate() {
            return bindingRepository.getAll(true).then(function(bindings) {
                ctrl.bindings = bindings;
                return ctrl.bindings;
            });
        }

        function navigateTo(path) {
            $location.path(path);
        }

        function configure(binding, event) {
            event.stopPropagation();
            $mdDialog.show({
                controller : 'ConfigureBindingDialogController',
                templateUrl : 'partials/bindings/dialog.configurebinding.html',
                targetEvent : event,
                hasBackdrop : true,
                locals : {
                    binding : binding
                }
            });
        }

        function isConfigurable(binding) {
            return binding.configDescriptionURI ? true : false;
        }
    }

})();