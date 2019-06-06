angular.module('main').directive('statsDesktop', function(){
    return{
        restrict:'E',
        templateUrl:'character/desktop/statsDesktop-@{TDCC_VERSION}.html'
    };
});