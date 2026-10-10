// FoodFrenzy Main Client Script

document.addEventListener('DOMContentLoaded', function () {
    // 1. Auto-dismiss flash alerts after 5 seconds
    const alerts = document.querySelectorAll('.alert-dismissible');
    alerts.forEach(function (alert) {
        setTimeout(function () {
            const bsAlert = bootstrap.Alert.getOrCreateInstance(alert);
            if (bsAlert) {
                bsAlert.close();
            }
        }, 5000);
    });

    // 2. Demo credentials auto-filler for Login Page
    window.fillCredentials = function (email, password) {
        const emailInput = document.getElementById('email');
        const passwordInput = document.getElementById('password');
        if (emailInput && passwordInput) {
            emailInput.value = email;
            passwordInput.value = password;
            // Visual feedback
            emailInput.classList.add('is-valid');
            passwordInput.classList.add('is-valid');
            setTimeout(() => {
                emailInput.classList.remove('is-valid');
                passwordInput.classList.remove('is-valid');
            }, 1000);
        }
    };

    // 3. Confirm dialog helper
    const confirmButtons = document.querySelectorAll('[data-confirm]');
    confirmButtons.forEach(button => {
        button.addEventListener('click', function (e) {
            const message = this.getAttribute('data-confirm') || 'Are you sure you want to proceed?';
            if (!confirm(message)) {
                e.preventDefault();
            }
        });
    });

    // 4. Quantity increment/decrement buttons
    const qtyInputs = document.querySelectorAll('.qty-input');
    qtyInputs.forEach(input => {
        const btnMinus = input.parentElement.querySelector('.btn-minus');
        const btnPlus = input.parentElement.querySelector('.btn-plus');
        const max = parseInt(input.getAttribute('max')) || 99;
        const min = parseInt(input.getAttribute('min')) || 1;

        if (btnMinus) {
            btnMinus.addEventListener('click', () => {
                let val = parseInt(input.value) || min;
                if (val > min) {
                    input.value = val - 1;
                    input.dispatchEvent(new Event('change'));
                }
            });
        }
        if (btnPlus) {
            btnPlus.addEventListener('click', () => {
                let val = parseInt(input.value) || min;
                if (val < max) {
                    input.value = val + 1;
                    input.dispatchEvent(new Event('change'));
                }
            });
        }
    });
});
