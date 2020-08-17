angular.module('main').directive('playSettingsMobile', function(){
    return{
        restrict:'E',
        templateUrl:'vtd/mobile/settingsMobile-@{TDCC_VERSION}.html'
    };
});