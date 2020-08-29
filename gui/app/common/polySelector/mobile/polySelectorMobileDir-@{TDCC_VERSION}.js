angular.module('main').directive('polySelectorMobile',['$uibModal', function($uibModal){
    return {
        restrict:'E',
        scope:{
            characterContext:'=',
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
                    windowClass: 'mobile-modal-dialog',
                    openedClass: 'mobile-modal-content',
                    templateUrl: 'common/polySelector/mobile/polySelectorMobileModalTemplate-@{TDCC_VERSION}.html'
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
        templateUrl:'common/polySelector/mobile/polySelectorMobileTemplate-@{TDCC_VERSION}.html'
    };
}]);