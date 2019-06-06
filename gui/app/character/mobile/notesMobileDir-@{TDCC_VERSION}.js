angular.module('main').directive('notesMobile', function(){
    return{
        restrict:'E',
        templateUrl:'character/mobile/notesMobile-@{TDCC_VERSION}.html'
    };
});