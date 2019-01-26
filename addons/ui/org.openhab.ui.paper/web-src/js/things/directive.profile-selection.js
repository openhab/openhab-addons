;
(function() {
    'use strict';

    angular.module('PaperUI.things').directive('profileSelection', ProfileSelection);

    function ProfileSelection() {
        return {
            restrict : 'E',
            scope : {},
            replace : true,
            bindToController : {
                profileModel : '=',
                channel : '=',
                itemType : '='
            },
            controllerAs : '$ctrl',
            templateUrl : 'partials/things/directive.profile-selection.html',
            controller : SelectProfileController
        }
    }

    SelectProfileController.$inject = [ 'profileTypeService' ];

    function SelectProfileController(profileTypeService) {
        var ctrl = this;

        this.profileList;

        this.selectProfile = selectProfile;

        this.$onInit = activate;

        function activate() {
            return profileTypeService.getByChannel({
                channelTypeUID : ctrl.channel.channelTypeUID,
                itemType : ctrl.itemType
            }).$promise.then(function(profiles) {
                ctrl.profileList = profiles.sort(function(a, b) {
                    if (a.uid === 'system:default') {
                        return -1;
                    }
                    if (b.uid === 'system:default') {
                        return 1;
                    }

                    return a.uid < b.uid ? -1 : a.uid > b.uid ? 1 : 0
                })
            });
        }

        function selectProfile(value) {
            if (ctrl.profileModel == undefined) {
                return value == 'system:default';
            }
            return (value == ctrl.profileModel) || (value == 'system:' + ctrl.profileModel);
        }
    }
})();
