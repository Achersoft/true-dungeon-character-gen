angular.module('main').directive('playStatsMobile', function(){
    return{
        restrict:'E',
        templateUrl:'vtd/mobile/statsMobile-@{TDCC_VERSION}.html'
    };
});