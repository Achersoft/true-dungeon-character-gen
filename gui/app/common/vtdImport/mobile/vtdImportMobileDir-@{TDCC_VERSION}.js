angular.module('main').directive('vtdImportMobile',['PartySvc', '$uibModal', 'AuthorizationState', 'RESOURCES', function(partySvc, $uibModal, AuthorizationState, RESOURCES){
    return {
        restrict:'E',
        scope:{
            addCharacter: '&?'
        },
        link: function(scope) {
            scope.userAccounts = {};
            scope.userCharacters = {};
            scope.user = AuthorizationState.getLoggedInUserId();
            scope.character = null;
            scope.modalInstance = null;
            
            scope.openModal = function() {
                partySvc.getSelectableCharacters(scope.user, "ALL").then(function(result) {
                    scope.userAccounts = result.data.userAccounts;
                    scope.userCharacters = result.data.characters;

                    scope.modalInstance = $uibModal.open({
                        ariaLabelledBy: 'modal-title',
                        ariaDescribedBy: 'modal-body',
                        bindToController: true,
                        scope: scope,
                        windowClass: 'mobile-modal-dialog',
                        openedClass: 'mobile-modal-content',
                        templateUrl: 'common/vtdImport/mobile/vtdImportMobileModalTemplate-@{TDCC_VERSION}.html'
                    });
                });
               
            };
            
            scope.updateCharacters = function(user) {
                partySvc.getSelectableCharacters(user, "ALL").then(function(result) {
                    scope.userAccounts = result.data.userAccounts;
                    scope.userCharacters = result.data.characters;
                    scope.character = null;
                });
            };

            scope.addCharacterToVtd = function(character) {
                if(character !== null) {
                    scope.addCharacter()(character); 
                    scope.modalInstance.close();
                }
            };

            scope.closeModal = function() {
                scope.modalInstance.close();
            };
        },
        templateUrl:'common/vtdImport/mobile/vtdImportMobileTemplate-@{TDCC_VERSION}.html'
    };
}]);