angular.module('main').directive('polySelectorDesktop',['$uibModal', function($uibModal){
    return {
        restrict:'E',
        scope:{
            available:'=',
            active:'=',
            label: '@',
            elementId: '@',
            setPoly: '&?'
        },
        link: function(scope) {
            scope.modalInstance = null;
            
            scope.openModal = function() {
                scope.modalInstance = $uibModal.open({
                    ariaLabelledBy: 'modal-title',
                    ariaDescribedBy: 'modal-body',
                    bindToController: true,
                    scope: scope,
                    windowClass: 'desktop-vtd-dialog',
                    openedClass: 'desktop-modal-content',
                    templateUrl: 'common/polySelector/desktop/polySelectorDesktopModalTemplate-@{TDCC_VERSION}.html'
                }); 
            };
            
            scope.selectPoly = function(poly) {
                scope.setPoly()(poly.id); 
                scope.modalInstance.close();
            }; 
            
            scope.closeModal = function() {
                scope.modalInstance.close();
            };
        },
        templateUrl:'common/polySelector/desktop/polySelectorDesktopTemplate-@{TDCC_VERSION}.html'
    };
}]);