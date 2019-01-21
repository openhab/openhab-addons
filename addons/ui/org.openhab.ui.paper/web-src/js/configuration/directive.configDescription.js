;
(function() {
    'use strict';

    angular.module('PaperUI.directive.configDescription', []).component('configDescription', {
        bindings : {
            configuration : '=',
            parameters : '=',
            expertMode : '=?',
            configArray : '=?',
            form : '=?'
        },
        templateUrl : 'partials/configuration/directive.configDescription.html',
        controller : ConfigDescriptionController
    });

    function ConfigDescriptionController() {
        var ctrl = this;

        this.getName = getName;

        function getName(parameter, option) {
            if (!option) {
                return undefined;
            }
            return option.name ? option.name : parameter.context == 'thing' ? option.UID : parameter.context == 'channel' ? option.id : undefined;
        }
    }

})()
