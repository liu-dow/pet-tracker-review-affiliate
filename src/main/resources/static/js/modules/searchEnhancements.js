// Search Enhancements Module
// Handles search functionality improvements
// Author: 

/**
 * Highlight search terms in content
 */
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

/**
 * Escape special characters in regex
 * @param {string} string - String to escape
 * @returns {string} Escaped string
 */
function escapeRegExp(string) {
    return string.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

/**
 * Get all text nodes in an element
 * @param {Element} node - Element to search
 * @returns {Array} Array of text nodes
 */
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

/**
 * Initialize search enhancements
 */
function initializeSearchEnhancements() {
    // Initialize highlight search terms on search results page
    if (window.location.pathname === '/search') {
        highlightSearchTerms();
    }
}

export { highlightSearchTerms, escapeRegExp, getTextNodes, initializeSearchEnhancements };