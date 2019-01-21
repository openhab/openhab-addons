;
(function() {
    'use strict';

    angular.module('PaperUI.control').component('itemStateDropdown', {
        bindings : {
            item : '=',
            onChange : '&'
        },
        templateUrl : 'partials/control/component.control.stateDropdown.html'
    });
})()
