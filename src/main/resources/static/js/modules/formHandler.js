// Form Handler Module
// Handles form submissions and validations
// Author: 

/**
 * Initialize Form Handlers
 */
function initializeFormHandlers() {
    // Newsletter form submission
    const newsletterForms = document.querySelectorAll('.newsletter-form');
    newsletterForms.forEach(form => {
        form.addEventListener('submit', function(e) {
            e.preventDefault();
            const email = this.querySelector('input[type="email"]').value;
            const submitButton = this.querySelector('button[type="submit"]');
            const originalButtonText = submitButton.textContent;
            
            if (validateEmail(email)) {
                // Show loading state
                submitButton.disabled = true;
                submitButton.textContent = 'Subscribing...';
                
                // Send to backend API using absolute HTTPS URL
                const baseUrl = window.location.origin;
                const fetchUrl = `${baseUrl}/newsletter/subscribe`;
                
                // Create URL-encoded data
                const formData = new URLSearchParams();
                formData.append('email', email);
                
                // Send to backend API
                fetch(fetchUrl, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                    },
                    body: formData
                })
                .then(response => {
                    // Check if the response is ok (status 200-299)
                    if (!response.ok) {
                        throw new Error(`HTTP error! status: ${response.status}`);
                    }
                    // Check content type to ensure it's JSON
                    const contentType = response.headers.get('content-type');
                    if (!contentType || !contentType.includes('application/json')) {
                        throw new Error('Received non-JSON response from server');
                    }
                    return response.json();
                })
                .then(data => {
                    if (data.success) {
                        showNotification(data.message, 'success');
                        this.reset();
                    } else {
                        showNotification(data.message, 'error');
                    }
                })
                .catch(error => {
                    console.error('Newsletter subscription error:', error);
                    showNotification('An error occurred. Please try again later.', 'error');
                })
                .finally(() => {
                    // Restore button state
                    submitButton.disabled = false;
                    submitButton.textContent = originalButtonText;
                });
            } else {
                showNotification('Please enter a valid email address.', 'error');
            }
        });
    });
    
    // Search form enhancement
    const searchForms = document.querySelectorAll('.search-form');
    searchForms.forEach(form => {
        const input = form.querySelector('.search-input');
        if (input) {
            input.addEventListener('keyup', function(e) {
                if (e.key === 'Enter') {
                    form.submit();
                }
            });
        }
    });
}

export { initializeFormHandlers };