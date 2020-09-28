angular.module('main').directive('damageTakerDesktop',['VtdSvc', '$uibModal', 'RESOURCES', function(vtdSvc, $uibModal, RESOURCES){
    return {
        restrict:'E',
        scope:{
            model:'=',
            editable: '=',
            partyId: '=',
            characterClass: '@',
            addCharacter: '&?',
            removeCharacter: '&?'
        },
        link: function(scope) {
            scope.userAccounts = {};
            
            scope.openModal = function() {
                scope.modalInstance = $uibModal.open({
                    ariaLabelledBy: 'modal-title',
                    ariaDescribedBy: 'modal-body',
                    bindToController: true,
                    scope: scope,
                    windowClass: 'desktop-vtd-dialog',
                    openedClass: 'desktop-modal-content',
                    templateUrl: 'common/vtdDamageTaker/desktop/damageTakerDesktopModalTemplate-@{TDCC_VERSION}.html'
                });
            };
            
            scope.removeCharacterFromParty = function(characterClass) {
                if(characterClass !== null) {
                    scope.removeCharacter()(scope.partyId, characterClass); 
                }
            };
            
            scope.closeModal = function() {
                scope.modalInstance.close();
            };
        },
        templateUrl:'common/vtdDamageTaker/desktop/damageTakerDesktopTemplate-@{TDCC_VERSION}.html'
    };
}]);