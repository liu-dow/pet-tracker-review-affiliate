// Analytics Module
// Handles analytics tracking functionality
// Author: 

/**
 * Track event in analytics
 * @param {string} category - Event category
 * @param {string} action - Event action
 * @param {string} label - Event label
 */
function trackEvent(category, action, label) {
    if (typeof gtag !== 'undefined') {
        gtag('event', action, {
            event_category: category,
            event_label: label
        });
    }
    
    // Console log for development
    console.log(`Event tracked: ${category} - ${action} - ${label}`);
}

/**
 * Initialize analytics event listeners
 */
function initializeAnalytics() {
    // Track outbound links
    document.addEventListener('click', function(e) {
        const link = e.target.closest('a');
        if (link && link.hostname !== window.location.hostname) {
            trackEvent('Outbound Links', 'Click', link.href);
        }
    });
}

export { trackEvent, initializeAnalytics };