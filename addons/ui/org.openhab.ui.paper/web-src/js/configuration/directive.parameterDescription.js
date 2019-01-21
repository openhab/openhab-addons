;
(function() {
    'use strict';

    angular.module('PaperUI.directive.parameterDescription', []).component('parameterDescription', {
        bindings : {
            description : '='
        },
        templateUrl : 'partials/configuration/directive.parameterDescription.html',
        controller : function() {
            var ctrl = this;
            this.isShowMore = false;

            this.showMore = function(sm, $event) {
                $event.stopImmediatePropagation();
                ctrl.isShowMore = sm;
            }
        }
    });

})();
