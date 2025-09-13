// UI Enhancements Module
// Handles UI improvements like lazy loading, back to top button, and reading progress
// Author: 

/**
 * Initialize UI Enhancements
 */
function initializeUIEnhancements() {
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

export { initializeUIEnhancements, createBackToTopButton, createReadingProgressBar };