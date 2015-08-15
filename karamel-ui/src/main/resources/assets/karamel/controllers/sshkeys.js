/**
 * Created by babbarshaer on 2014-11-25.
 */
// Controller for different providers.

angular.module("karamel-main.module")
    .controller('SshKeysController', ['$log', '$scope', '$modalInstance', 'KaramelCoreRestServices', function($log, $scope, $modalInstance, KaramelCoreRestServices) {

        function initKeys(scope) {
          scope.sshKeyPair = {
            pubKeyPath: null,
            privKeyPath: null,
            passphrase: null
          };
          KaramelCoreRestServices.loadSshKeys()
              .success(function(data) {
                $log.info("ssh data is:" + data.publicKeyPath + "," + data.privateKeyPath);
                scope.sshKeyPair.pubKeyPath = data.publicKeyPath;
                scope.sshKeyPair.privKeyPath = data.privateKeyPath;
                scope.sshKeyPair.passphrase = data.passphrase;
              })
              .error(function(data) {
                $log.warn("No SSh keys is available");
              });

          scope.states = {
            valid: "valid",
            invalid: "invalid",
            initial: "initial"
          };

        }

        $scope.close = function() {
          $modalInstance.close();
        };


        $scope.submitKeys = function() {
          if (this.sshKeyForm.$valid) {
            $modalInstance.close();
          }
        };

        $scope.generateKeys = function() {
          KaramelCoreRestServices.generateSshKeys()
              .success(function(data) {
                $log.info("ssh data is:" + data.publicKeyPath + "," + data.privateKeyPath);
                $scope.sshKeyPair.pubKeyPath = data.publicKeyPath;
                $scope.sshKeyPair.privKeyPath = data.privateKeyPath;
              })
              .error(function(data) {
                $log.warn("Couldn't generate ssh-keys");
              });
        };


        initKeys($scope);
      }]);