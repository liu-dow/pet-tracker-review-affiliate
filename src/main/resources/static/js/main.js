// Main JavaScript Entry Point for Pet Tracker Review website
// This file loads modules as needed based on the current page
// Author: 

document.addEventListener('DOMContentLoaded', function() {
    // Load common utilities used across all pages
    import('./modules/utils.js').then(module => {
        window.validateEmail = module.validateEmail;
        window.showNotification = module.showNotification;
    });
    
    // Load analytics module
    import('./modules/analytics.js').then(module => {
        module.initializeAnalytics();
        window.trackEvent = module.trackEvent;
    });
    
    // Load cookie manager on all pages
    import('./modules/cookieManager.js').then(module => {
        module.initializeCookieManager();
    });
    
    // Load navigation module
    import('./modules/navigation.js').then(module => {
        module.initializeNavigation();
    });
    
    // Conditionally load form handler module
    if (document.querySelectorAll('.newsletter-form').length > 0 || 
        document.querySelectorAll('.search-form').length > 0) {
        import('./modules/formHandler.js').then(module => {
            module.initializeFormHandlers();
        });
    }
    
    // Conditionally load UI enhancements module
    if (document.querySelectorAll('img[data-src]').length > 0 || 
        document.querySelector('.article-content') || 
        document.querySelector('.review-content')) {
        import('./modules/uiEnhancements.js').then(module => {
            module.initializeUIEnhancements();
        });
    }
    
    // Conditionally load social share module
    if (document.querySelectorAll('.share-btn').length > 0) {
        import('./modules/socialShare.js').then(module => {
            module.initializeSocialShare();
            window.shareOnFacebook = module.shareOnFacebook;
            window.shareOnTwitter = module.shareOnTwitter;
            window.shareOnLinkedIn = module.shareOnLinkedIn;
        });
    }
    
    // Conditionally load search enhancements module
    if (window.location.pathname === '/search') {
        import('./modules/searchEnhancements.js').then(module => {
            module.initializeSearchEnhancements();
        });
    }
    
    // Load performance utilities
    import('./modules/performanceUtils.js').then(module => {
        window.debounce = module.debounce;
    });
});