angular.module('main').directive('buffMobile', function(){
    return{
        restrict:'E',
        templateUrl:'vtd/mobile/buffMobile-@{TDCC_VERSION}.html'
    };
});