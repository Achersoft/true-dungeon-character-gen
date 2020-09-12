angular.module('main').directive('playNotesDesktop', function(){
    return{
        restrict:'E',
        templateUrl:'vtd/desktop/notesDesktop-@{TDCC_VERSION}.html'
    };
});