angular.module('main').directive('settingsDesktop', function(){
    return{
        restrict:'E',
        templateUrl:'vtd/desktop/settingsDesktop-@{TDCC_VERSION}.html'
    };
});