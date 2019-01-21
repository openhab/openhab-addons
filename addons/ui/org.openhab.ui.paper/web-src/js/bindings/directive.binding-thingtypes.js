;
(function() {
    'use strict';

    angular.module('PaperUI.bindings').component('bindingThingtypes', {
        bindings : {
            binding : '='
        },
        templateUrl : 'partials/bindings/directive.binding-thingtypes.html',
        controller : BindingThingtypesController
    });

    function BindingThingtypesController() {
        var ctrl = this;
        this.filter = {
            text : ''
        };

        this.clearFilter = clearFilter;
        this.filterItems = filterItems;

        function filterItems(lookupFields, searchText) {
            return function(item) {
                if (searchText && searchText.length > 0) {
                    for (var i = 0; i < lookupFields.length; i++) {
                        if (item[lookupFields[i]] && item[lookupFields[i]].toUpperCase().indexOf(searchText.toUpperCase()) != -1) {
                            return true;
                        }
                    }
                    return false
                }
                return true;
            }
        }

        function clearFilter(event) {
            if (!event || event.keyCode === 27) {
                ctrl.filter.text = '';
            }
        }
    }
})();
