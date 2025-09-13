// Cookie Manager Implementation
// Handles cookie consent, preferences, and management
// Author: 

/**
 * Initialize Cookie Manager
 */
function initializeCookieManager() {
    const cookieBanner = document.getElementById('cookie-banner');
    const cookieModal = document.getElementById('cookie-settings-modal');
    const cookieSettingsLink = document.getElementById('cookie-settings-link');
    
    // Check if user has already made a choice
    const cookieConsent = getCookie('cookie-consent');
    if (!cookieConsent) {
        showCookieBanner();
    } else {
        showCookieSettingsLink();
        applyCookieSettings(JSON.parse(cookieConsent));
    }
    
    // Event listeners for cookie banner
    document.getElementById('accept-all-cookies')?.addEventListener('click', acceptAllCookies);
    document.getElementById('reject-all-cookies')?.addEventListener('click', rejectAllCookies);
    document.getElementById('manage-cookies')?.addEventListener('click', openCookieSettings);
    
    // Event listeners for cookie modal
    document.getElementById('close-cookie-modal')?.addEventListener('click', closeCookieSettings);
    document.getElementById('save-cookie-preferences')?.addEventListener('click', saveCookiePreferences);
    document.getElementById('accept-all-modal')?.addEventListener('click', acceptAllFromModal);
    
    // Event listener for floating cookie settings button
    document.getElementById('open-cookie-settings')?.addEventListener('click', openCookieSettings);
    
    // Close modal when clicking outside
    cookieModal?.addEventListener('click', function(e) {
        if (e.target === cookieModal) {
            closeCookieSettings();
        }
    });
}

function showCookieBanner() {
    const banner = document.getElementById('cookie-banner');
    if (banner) {
        banner.style.display = 'block';
        // Add slide-in animation
        setTimeout(() => {
            banner.classList.add('cookie-banner-visible');
        }, 100);
    }
}

function hideCookieBanner() {
    const banner = document.getElementById('cookie-banner');
    if (banner) {
        banner.classList.remove('cookie-banner-visible');
        setTimeout(() => {
            banner.style.display = 'none';
        }, 300);
    }
}

function showCookieSettingsLink() {
    const link = document.getElementById('cookie-settings-link');
    if (link) {
        link.style.display = 'block';
    }
}

function acceptAllCookies() {
    const preferences = {
        necessary: true,
        analytics: true,  // Always true for GA4
        marketing: true,
        preferences: true,
        timestamp: Date.now()
    };
    
    setCookie('cookie-consent', JSON.stringify(preferences), 365);
    applyCookieSettings(preferences);
    hideCookieBanner();
    showCookieSettingsLink();
    
    showNotification('All cookies accepted', 'success');
    trackEvent('Cookie Consent', 'Accept All', 'Banner');
}

function rejectAllCookies() {
    const preferences = {
        necessary: true,  // Always true as these are required
        analytics: true,  // Always true for GA4 - required
        marketing: false,
        preferences: false,
        timestamp: Date.now()
    };
    
    setCookie('cookie-consent', JSON.stringify(preferences), 365);
    applyCookieSettings(preferences);
    hideCookieBanner();
    showCookieSettingsLink();
    
    showNotification('Marketing and preference cookies rejected. Analytics cookies are required for GA4.', 'info');
    trackEvent('Cookie Consent', 'Reject Optional', 'Banner');
}

function openCookieSettings() {
    const modal = document.getElementById('cookie-settings-modal');
    if (modal) {
        modal.style.display = 'flex';
        loadCurrentPreferences();
        // Add fade-in animation
        setTimeout(() => {
            modal.classList.add('cookie-modal-visible');
        }, 10);
    }
    hideCookieBanner();
}

function closeCookieSettings() {
    const modal = document.getElementById('cookie-settings-modal');
    if (modal) {
        modal.classList.remove('cookie-modal-visible');
        setTimeout(() => {
            modal.style.display = 'none';
        }, 300);
    }
}

function loadCurrentPreferences() {
    const cookieConsent = getCookie('cookie-consent');
    if (cookieConsent) {
        const preferences = JSON.parse(cookieConsent);
        
        document.getElementById('necessary-cookies').checked = preferences.necessary || true;
        document.getElementById('analytics-cookies').checked = true; // Always true for GA4
        document.getElementById('marketing-cookies').checked = preferences.marketing || false;
        document.getElementById('preferences-cookies').checked = preferences.preferences || false;
        
        // Disable analytics checkbox since GA4 is required
        document.getElementById('analytics-cookies').disabled = true;
    } else {
        // Set default values for new users
        document.getElementById('necessary-cookies').checked = true;
        document.getElementById('analytics-cookies').checked = true;
        document.getElementById('analytics-cookies').disabled = true;
        document.getElementById('marketing-cookies').checked = false;
        document.getElementById('preferences-cookies').checked = false;
    }
}

function saveCookiePreferences() {
    const preferences = {
        necessary: document.getElementById('necessary-cookies').checked,
        analytics: true, // Always true for GA4
        marketing: document.getElementById('marketing-cookies').checked,
        preferences: document.getElementById('preferences-cookies').checked,
        timestamp: Date.now()
    };
    
    setCookie('cookie-consent', JSON.stringify(preferences), 365);
    applyCookieSettings(preferences);
    closeCookieSettings();
    showCookieSettingsLink();
    
    showNotification('Cookie preferences saved', 'success');
    trackEvent('Cookie Consent', 'Custom Preferences', 'Modal');
}

function acceptAllFromModal() {
    // Check all checkboxes except necessary and analytics (which are always true)
    document.getElementById('marketing-cookies').checked = true;
    document.getElementById('preferences-cookies').checked = true;
    
    saveCookiePreferences();
}

function applyCookieSettings(preferences) {
    // Apply analytics cookies (Google Analytics, etc.)
    if (preferences.analytics) {
        loadAnalyticsScripts();
    } else {
        disableAnalytics();
    }
    
    // Apply marketing cookies (advertising, remarketing, etc.)
    if (preferences.marketing) {
        loadMarketingScripts();
    } else {
        disableMarketing();
    }
    
    // Apply preference cookies (theme, language, etc.)
    if (preferences.preferences) {
        enablePreferenceCookies();
    } else {
        disablePreferenceCookies();
    }
    
    // Store settings in localStorage for immediate access
    localStorage.setItem('cookie-preferences', JSON.stringify(preferences));
}

function loadAnalyticsScripts() {
    // Enable Google Analytics 4 (GA4) - already loaded in head
    if (typeof gtag !== 'undefined') {
        gtag('consent', 'update', {
            'analytics_storage': 'granted'
        });
        
        // Track initial page view after consent
        gtag('event', 'page_view', {
            page_title: document.title,
            page_location: window.location.href,
            page_path: window.location.pathname,
            consent_granted: true
        });
        
        // Track cookie consent event
        gtag('event', 'cookie_consent_granted', {
            event_category: 'Cookie Consent',
            event_label: 'Analytics Enabled'
        });
    }
    
    console.log('GA4 Analytics cookies enabled and consent updated');
}

function disableAnalytics() {
    // Note: GA4 is required, but we can disable some features
    if (typeof gtag !== 'undefined') {
        gtag('consent', 'update', {
            'analytics_storage': 'granted', // Keep granted since GA4 is required
            'ad_storage': 'denied'
        });
    }
    
    console.log('GA4 Analytics remain enabled (required), but ad features disabled');
}

function loadMarketingScripts() {
    // Example: Load marketing/advertising scripts
    // Facebook Pixel, Google Ads, etc.
    console.log('Marketing cookies enabled');
    
    // Enable Google Analytics advertising features
    if (typeof gtag !== 'undefined') {
        gtag('consent', 'update', {
            'ad_storage': 'granted',
            'ad_user_data': 'granted',
            'ad_personalization': 'granted'
        });
    }
}

function disableMarketing() {
    // Disable marketing cookies
    if (typeof gtag !== 'undefined') {
        gtag('consent', 'update', {
            'ad_storage': 'denied',
            'ad_user_data': 'denied',
            'ad_personalization': 'denied'
        });
    }
    
    console.log('Marketing cookies disabled');
}

function enablePreferenceCookies() {
    // Enable preference cookies for user settings
    console.log('Preference cookies enabled');
}

function disablePreferenceCookies() {
    // Disable preference cookies
    console.log('Preference cookies disabled');
}

// Cookie utility functions
function setCookie(name, value, days) {
    const expires = new Date();
    expires.setTime(expires.getTime() + (days * 24 * 60 * 60 * 1000));
    document.cookie = `${name}=${value};expires=${expires.toUTCString()};path=/;SameSite=Lax`;
}

function getCookie(name) {
    const nameEQ = name + '=';
    const ca = document.cookie.split(';');
    for (let i = 0; i < ca.length; i++) {
        let c = ca[i];
        while (c.charAt(0) === ' ') c = c.substring(1, c.length);
        if (c.indexOf(nameEQ) === 0) return c.substring(nameEQ.length, c.length);
    }
    return null;
}

function deleteCookie(name) {
    document.cookie = `${name}=;expires=Thu, 01 Jan 1970 00:00:00 UTC;path=/;`;
}

// Export cookie preferences for use in analytics
function getCookiePreferences() {
    const preferences = localStorage.getItem('cookie-preferences');
    return preferences ? JSON.parse(preferences) : null;
}

// Check if specific cookie category is allowed
function isCookieAllowed(category) {
    const preferences = getCookiePreferences();
    return preferences ? preferences[category] : false;
}

// Reset all cookie preferences
function resetCookiePreferences() {
    deleteCookie('cookie-consent');
    localStorage.removeItem('cookie-preferences');
    
    // Hide settings link and show banner again
    const settingsLink = document.getElementById('cookie-settings-link');
    if (settingsLink) {
        settingsLink.style.display = 'none';
    }
    
    showCookieBanner();
    showNotification('Cookie settings have been reset', 'info');
}

// Make some functions globally available
window.cookieManager = {
    getCookiePreferences,
    isCookieAllowed,
    resetCookiePreferences,
    openCookieSettings
};

// Export functions for modular usage
export {
    initializeCookieManager,
    showCookieBanner,
    hideCookieBanner,
    showCookieSettingsLink,
    acceptAllCookies,
    rejectAllCookies,
    openCookieSettings,
    closeCookieSettings,
    loadCurrentPreferences,
    saveCookiePreferences,
    acceptAllFromModal,
    applyCookieSettings,
    loadAnalyticsScripts,
    disableAnalytics,
    loadMarketingScripts,
    disableMarketing,
    enablePreferenceCookies,
    disablePreferenceCookies,
    setCookie,
    getCookie,
    deleteCookie,
    getCookiePreferences,
    isCookieAllowed,
    resetCookiePreferences
};