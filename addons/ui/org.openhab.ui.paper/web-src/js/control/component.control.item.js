;
(function() {
    'use strict';

    angular.module('PaperUI.control').component('itemControl', {
        bindings : {
            itemName : '<'
        },
        templateUrl : 'partials/control/component.control.item.html',
        controller : ItemControlController

    });

    ItemControlController.$inject = [ 'itemRepository', 'util' ];

    function ItemControlController(itemRepository, util) {
        var ctrl = this;
        this.item;

        this.$onInit = activate;

        function activate() {
            getItem();
        }

        function getItem() {
            itemRepository.getOne(function condition(element) {
                return element.name == ctrl.itemName;
            }, function callback(item) {
                item.stateText = util.getItemStateText(item);
                item.readOnly = isReadOnly(item);
                ctrl.item = item;
            });
        }

        function isReadOnly(item) {
            return item.stateDescription ? item.stateDescription.readOnly : false;
        }
    }

})()
