angular.module('main').directive('playNotesMobile', function(){
    return{
        restrict:'E',
        templateUrl:'vtd/mobile/notesMobile-@{TDCC_VERSION}.html'
    };
});