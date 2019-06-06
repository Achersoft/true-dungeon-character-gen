angular.module('main').directive('statsMobile', function(){
    return{
        restrict:'E',
        templateUrl:'character/mobile/statsMobile-@{TDCC_VERSION}.html'
    };
});