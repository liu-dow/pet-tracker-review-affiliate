// Main JavaScript file for Pet Tracker Review website
document.addEventListener('DOMContentLoaded', function() {
    // Initialize Cookie Manager
    initializeCookieManager();
    
    // Mobile navigation toggle
    const navToggle = document.getElementById('nav-toggle');
    const navMenu = document.getElementById('nav-menu');
    
    if (navToggle && navMenu) {
        navToggle.addEventListener('click', function() {
            navMenu.classList.toggle('active');
            navToggle.classList.toggle('active');
        });
    }
    
    // Smooth scrolling for anchor links
    const anchorLinks = document.querySelectorAll('a[href^="#"]');
    anchorLinks.forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            const target = document.querySelector(this.getAttribute('href'));
            if (target) {
                target.scrollIntoView({
                    behavior: 'smooth',
                    block: 'start'
                });
            }
        });
    });
    
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
                
                // Send to backend API
                fetch('/newsletter/subscribe', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                    },
                    body: 'email=' + encodeURIComponent(email)
                })
                .then(response => response.json())
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
    
    // Lazy loading for images (if any)
    if ('IntersectionObserver' in window) {
        const imageObserver = new IntersectionObserver((entries, observer) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    const img = entry.target;
                    if (img.dataset.src) {
                        img.src = img.dataset.src;
                        img.classList.remove('lazy');
                        observer.unobserve(img);
                    }
                }
            });
        });
        
        const lazyImages = document.querySelectorAll('img[data-src]');
        lazyImages.forEach(img => imageObserver.observe(img));
    }
    
    // Back to top button
    const backToTopButton = createBackToTopButton();
    document.body.appendChild(backToTopButton);
    
    window.addEventListener('scroll', function() {
        if (window.pageYOffset > 300) {
            backToTopButton.style.display = 'block';
        } else {
            backToTopButton.style.display = 'none';
        }
    });
    
    // Reading progress bar for articles
    if (document.querySelector('.article-content') || document.querySelector('.review-content')) {
        createReadingProgressBar();
    }
    
    // Auto-hide header on scroll down, show on scroll up
    let lastScrollTop = 0;
    const header = document.querySelector('.header');
    if (header) {
        window.addEventListener('scroll', function() {
            const scrollTop = window.pageYOffset || document.documentElement.scrollTop;
            
            if (scrollTop > lastScrollTop && scrollTop > 100) {
                // Scrolling down
                header.style.transform = 'translateY(-100%)';
            } else {
                // Scrolling up
                header.style.transform = 'translateY(0)';
            }
            
            lastScrollTop = scrollTop;
        });
    }
});

// Utility Functions
function validateEmail(email) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
}

function showNotification(message, type = 'info') {
    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;
    notification.textContent = message;
    
    // Add notification styles
    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        padding: 1rem 1.5rem;
        border-radius: 6px;
        color: white;
        font-weight: 500;
        z-index: 1000;
        transform: translateX(400px);
        transition: transform 0.3s ease;
    `;
    
    if (type === 'success') {
        notification.style.backgroundColor = '#38a169';
    } else if (type === 'error') {
        notification.style.backgroundColor = '#e53e3e';
    } else {
        notification.style.backgroundColor = '#3182ce';
    }
    
    document.body.appendChild(notification);
    
    // Animate in
    setTimeout(() => {
        notification.style.transform = 'translateX(0)';
    }, 100);
    
    // Animate out and remove
    setTimeout(() => {
        notification.style.transform = 'translateX(400px)';
        setTimeout(() => {
            document.body.removeChild(notification);
        }, 300);
    }, 3000);
}

function createBackToTopButton() {
    const button = document.createElement('button');
    button.innerHTML = '<i class="fas fa-chevron-up"></i>';
    button.className = 'back-to-top';
    button.style.cssText = `
        position: fixed;
        bottom: 20px;
        right: 20px;
        width: 50px;
        height: 50px;
        border: none;
        border-radius: 50%;
        background-color: #3182ce;
        color: white;
        font-size: 1.2rem;
        cursor: pointer;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
        transition: all 0.3s ease;
        display: none;
        z-index: 1000;
    `;
    
    button.addEventListener('mouseenter', function() {
        this.style.backgroundColor = '#2c5aa0';
        this.style.transform = 'translateY(-2px)';
    });
    
    button.addEventListener('mouseleave', function() {
        this.style.backgroundColor = '#3182ce';
        this.style.transform = 'translateY(0)';
    });
    
    button.addEventListener('click', function() {
        window.scrollTo({
            top: 0,
            behavior: 'smooth'
        });
    });
    
    return button;
}

function createReadingProgressBar() {
    const progressBar = document.createElement('div');
    progressBar.className = 'reading-progress';
    progressBar.style.cssText = `
        position: fixed;
        top: 0;
        left: 0;
        width: 0%;
        height: 3px;
        background-color: #3182ce;
        z-index: 1000;
        transition: width 0.1s ease;
    `;
    
    document.body.appendChild(progressBar);
    
    window.addEventListener('scroll', function() {
        const article = document.querySelector('.article-content') || document.querySelector('.review-content');
        if (!article) return;
        
        const articleTop = article.offsetTop;
        const articleHeight = article.offsetHeight;
        const scrollTop = window.pageYOffset;
        const windowHeight = window.innerHeight;
        
        const scrolled = scrollTop - articleTop + windowHeight;
        const progress = Math.min(Math.max(scrolled / articleHeight, 0), 1);
        
        progressBar.style.width = (progress * 100) + '%';
    });
}

// Social sharing functions
function shareOnFacebook(url = window.location.href, title = document.title) {
    const shareUrl = `https://www.facebook.com/sharer/sharer.php?u=${encodeURIComponent(url)}`;
    openShareWindow(shareUrl);
}

function shareOnTwitter(url = window.location.href, title = document.title) {
    const shareUrl = `https://twitter.com/intent/tweet?url=${encodeURIComponent(url)}&text=${encodeURIComponent(title)}`;
    openShareWindow(shareUrl);
}

function shareOnLinkedIn(url = window.location.href) {
    const shareUrl = `https://www.linkedin.com/sharing/share-offsite/?url=${encodeURIComponent(url)}`;
    openShareWindow(shareUrl);
}

function openShareWindow(url) {
    const width = 600;
    const height = 400;
    const left = (screen.width - width) / 2;
    const top = (screen.height - height) / 2;
    
    window.open(
        url,
        'share-window',
        `width=${width},height=${height},left=${left},top=${top},resizable=yes,scrollbars=yes`
    );
}

// Search functionality enhancement
function highlightSearchTerms() {
    const urlParams = new URLSearchParams(window.location.search);
    const searchQuery = urlParams.get('q');
    
    if (searchQuery && searchQuery.length > 2) {
        const regex = new RegExp(`(${escapeRegExp(searchQuery)})`, 'gi');
        const textNodes = getTextNodes(document.body);
        
        textNodes.forEach(node => {
            if (node.textContent.toLowerCase().includes(searchQuery.toLowerCase())) {
                const parent = node.parentNode;
                const html = node.textContent.replace(regex, '<mark>$1</mark>');
                
                if (parent.tagName !== 'SCRIPT' && parent.tagName !== 'STYLE') {
                    const wrapper = document.createElement('span');
                    wrapper.innerHTML = html;
                    parent.replaceChild(wrapper, node);
                }
            }
        });
    }
}

function escapeRegExp(string) {
    return string.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

function getTextNodes(node) {
    const textNodes = [];
    const walker = document.createTreeWalker(
        node,
        NodeFilter.SHOW_TEXT,
        null,
        false
    );
    
    let textNode;
    while (textNode = walker.nextNode()) {
        if (textNode.textContent.trim().length > 0) {
            textNodes.push(textNode);
        }
    }
    
    return textNodes;
}

// Performance optimization: Debounce function
function debounce(func, wait, immediate) {
    let timeout;
    return function executedFunction() {
        const context = this;
        const args = arguments;
        
        const later = function() {
            timeout = null;
            if (!immediate) func.apply(context, args);
        };
        
        const callNow = immediate && !timeout;
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
        
        if (callNow) func.apply(context, args);
    };
}

// Analytics tracking (placeholder for Google Analytics or other tracking)
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

// Track outbound links
document.addEventListener('click', function(e) {
    const link = e.target.closest('a');
    if (link && link.hostname !== window.location.hostname) {
        trackEvent('Outbound Links', 'Click', link.href);
    }
});

// Track share button clicks
document.addEventListener('click', function(e) {
    if (e.target.closest('.share-btn')) {
        const platform = e.target.closest('.share-btn').className.split(' ').find(cls => 
            ['facebook', 'twitter', 'linkedin'].includes(cls)
        );
        trackEvent('Social Share', 'Click', platform);
    }
});

// Initialize highlight search terms on search results page
if (window.location.pathname === '/search') {
    highlightSearchTerms();
}



/**
 * Cookie Manager Implementation
 * Handles cookie consent, preferences, and management
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
