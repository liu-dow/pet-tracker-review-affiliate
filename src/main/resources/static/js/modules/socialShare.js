// Social Share Module
// Handles social media sharing functionality
// Author: 

/**
 * Share on Facebook
 * @param {string} url - URL to share
 * @param {string} title - Title to share
 */
function shareOnFacebook(url = window.location.href, title = document.title) {
    const shareUrl = `https://www.facebook.com/sharer/sharer.php?u=${encodeURIComponent(url)}`;
    openShareWindow(shareUrl);
}

/**
 * Share on Twitter
 * @param {string} url - URL to share
 * @param {string} title - Title to share
 */
function shareOnTwitter(url = window.location.href, title = document.title) {
    const shareUrl = `https://twitter.com/intent/tweet?url=${encodeURIComponent(url)}&text=${encodeURIComponent(title)}`;
    openShareWindow(shareUrl);
}

/**
 * Share on LinkedIn
 * @param {string} url - URL to share
 */
function shareOnLinkedIn(url = window.location.href) {
    const shareUrl = `https://www.linkedin.com/sharing/share-offsite/?url=${encodeURIComponent(url)}`;
    openShareWindow(shareUrl);
}

/**
 * Open share window with specified URL
 * @param {string} url - URL to open in share window
 */
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

/**
 * Initialize Social Share Event Listeners
 */
function initializeSocialShare() {
    // Track share button clicks
    document.addEventListener('click', function(e) {
        if (e.target.closest('.share-btn')) {
            const platform = e.target.closest('.share-btn').className.split(' ').find(cls => 
                ['facebook', 'twitter', 'linkedin'].includes(cls)
            );
            trackEvent('Social Share', 'Click', platform);
        }
    });
}

export { shareOnFacebook, shareOnLinkedIn, shareOnTwitter, openShareWindow, initializeSocialShare };