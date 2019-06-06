angular.module('main').directive('notesDesktop', function(){
    return{
        restrict:'E',
        templateUrl:'character/desktop/notesDesktop-@{TDCC_VERSION}.html'
    };
});