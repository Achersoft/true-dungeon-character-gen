angular.module('main').directive('rollerMobile', function(){
    return{
        restrict:'E',
        templateUrl:'vtd/mobile/rollerMobile-@{TDCC_VERSION}.html'
    };
});