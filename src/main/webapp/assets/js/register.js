(function () {

    window.onload = function () {

        let formElement = document.getElementById('register-form');
        let form = new Form(formElement);

        let inputPassword = document.getElementById('input-password');

        form.bindInputValidation('input-password', 'input-password-confirm');
        form.addOnlineValidation('input-username', '/check-username', 'Nome utente già in uso.')

        form.onresponse = function (response){
            window.location.href = '/';
        }

        form.customValidity = function (inputElement) {
            if(inputElement.id === 'input-password-confirm'){
                if(inputElement.value !== inputPassword.value){
                    inputElement.setCustomValidity('La password non coincide.');
                    return false;
                }
            }

            return true;
        }
    }

})();