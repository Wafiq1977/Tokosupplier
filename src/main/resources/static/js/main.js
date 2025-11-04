// SAVORA - Main JavaScript File

document.addEventListener('DOMContentLoaded', function() {
    // Initialize all components
    initializeAnimations();
    initializeSearch();
    initializeProductCards();
    initializeForms();
    initializeScrollEffects();
});

// Animation initialization
function initializeAnimations() {
    // Add fade-in animation to elements
    const observerOptions = {
        threshold: 0.1,
        rootMargin: '0px 0px -50px 0px'
    };

    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add('fade-in-up');
            }
        });
    }, observerOptions);

    // Observe elements for animation
    document.querySelectorAll('.category-card, .product-card, .feature-card, .stats-card').forEach(card => {
        observer.observe(card);
    });
}

// Search functionality
function initializeSearch() {
    const searchForm = document.querySelector('form[action="/"]');
    if (searchForm) {
        searchForm.addEventListener('submit', function(e) {
            const searchInput = this.querySelector('input[name="search"]');
            if (searchInput && searchInput.value.trim() === '') {
                e.preventDefault();
                showNotification('Silakan masukkan kata kunci pencarian', 'warning');
                searchInput.focus();
            }
        });
    }
}

// Product card interactions
function initializeProductCards() {
    // Add to cart functionality
    document.querySelectorAll('.btn-primary').forEach(btn => {
        if (btn.textContent.includes('Lihat Detail') || btn.textContent.includes('Beli')) {
            btn.addEventListener('click', function(e) {
                e.preventDefault();
                const productCard = this.closest('.product-card');
                const productName = productCard.querySelector('.product-title').textContent;

                // Add loading state
                this.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i>Memproses...';
                this.disabled = true;

                // Simulate API call
                setTimeout(() => {
                    showNotification(`${productName} berhasil ditambahkan ke keranjang!`, 'success');
                    this.innerHTML = '<i class="fas fa-check me-2"></i>Ditambahkan';
                    this.classList.remove('btn-primary');
                    this.classList.add('btn-success');

                    // Update cart count
                    updateCartCount(1);
                }, 1000);
            });
        }
    });

    // Favorite functionality
    document.querySelectorAll('.fa-heart').forEach(heart => {
        heart.addEventListener('click', function() {
            this.classList.toggle('text-danger');
            const isFavorited = this.classList.contains('text-danger');

            if (isFavorited) {
                showNotification('Produk ditambahkan ke favorit', 'success');
            } else {
                showNotification('Produk dihapus dari favorit', 'info');
            }
        });
    });
}

// Form validation and enhancement
function initializeForms() {
    // Contact form validation
    const contactForm = document.querySelector('form[action="/contact"]');
    if (contactForm) {
        contactForm.addEventListener('submit', function(e) {
            const requiredFields = this.querySelectorAll('input[required], textarea[required], select[required]');
            let isValid = true;

            requiredFields.forEach(field => {
                if (!field.value.trim()) {
                    field.classList.add('is-invalid');
                    isValid = false;
                } else {
                    field.classList.remove('is-invalid');
                }
            });

            if (!isValid) {
                e.preventDefault();
                showNotification('Mohon lengkapi semua field yang diperlukan', 'danger');
            }
        });
    }

    // Real-time validation
    document.querySelectorAll('input, textarea, select').forEach(field => {
        field.addEventListener('blur', function() {
            if (this.hasAttribute('required') && !this.value.trim()) {
                this.classList.add('is-invalid');
            } else {
                this.classList.remove('is-invalid');
            }
        });
    });
}

// Scroll effects
function initializeScrollEffects() {
    let lastScrollTop = 0;
    const navbar = document.querySelector('.navbar');

    window.addEventListener('scroll', function() {
        const scrollTop = window.pageYOffset || document.documentElement.scrollTop;

        // Navbar hide/show on scroll
        if (scrollTop > lastScrollTop && scrollTop > 100) {
            navbar.style.transform = 'translateY(-100%)';
        } else {
            navbar.style.transform = 'translateY(0)';
        }

        lastScrollTop = scrollTop;

        // Parallax effect for hero section
        const heroSection = document.querySelector('.hero-section');
        if (heroSection) {
            const scrolled = scrollTop * 0.5;
            heroSection.style.backgroundPosition = `center ${scrolled}px`;
        }
    });
}

// Notification system
function showNotification(message, type = 'info') {
    // Remove existing notifications
    document.querySelectorAll('.notification-toast').forEach(toast => toast.remove());

    // Create notification element
    const notification = document.createElement('div');
    notification.className = `notification-toast alert alert-${type} position-fixed`;
    notification.style.cssText = `
        top: 20px;
        right: 20px;
        z-index: 9999;
        min-width: 300px;
        box-shadow: 0 4px 12px rgba(0,0,0,0.15);
        animation: slideInRight 0.3s ease-out;
    `;

    const iconMap = {
        success: 'check-circle',
        danger: 'exclamation-triangle',
        warning: 'exclamation-circle',
        info: 'info-circle'
    };

    notification.innerHTML = `
        <i class="fas fa-${iconMap[type]} me-2"></i>
        ${message}
        <button type="button" class="btn-close" onclick="this.parentElement.remove()"></button>
    `;

    document.body.appendChild(notification);

    // Auto remove after 5 seconds
    setTimeout(() => {
        if (notification.parentElement) {
            notification.style.animation = 'slideOutRight 0.3s ease-in';
            setTimeout(() => notification.remove(), 300);
        }
    }, 5000);
}

// Cart count update
function updateCartCount(change) {
    let cartCount = parseInt(localStorage.getItem('cartCount') || '0');
    cartCount += change;
    localStorage.setItem('cartCount', cartCount);

    const cartBadge = document.querySelector('.cart-badge');
    if (cartBadge) {
        cartBadge.textContent = cartCount;
        cartBadge.style.display = cartCount > 0 ? 'inline' : 'none';
    }
}

// Initialize cart count on page load
function initializeCartCount() {
    const cartCount = parseInt(localStorage.getItem('cartCount') || '0');
    const cartBadge = document.querySelector('.cart-badge');
    if (cartBadge) {
        cartBadge.textContent = cartCount;
        cartBadge.style.display = cartCount > 0 ? 'inline' : 'none';
    }
}

// Product image lazy loading
function initializeLazyLoading() {
    const images = document.querySelectorAll('img[data-src]');

    const imageObserver = new IntersectionObserver((entries, observer) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                const img = entry.target;
                img.src = img.dataset.src;
                img.classList.remove('loading');
                observer.unobserve(img);
            }
        });
    });

    images.forEach(img => imageObserver.observe(img));
}

// Smooth scrolling for anchor links
document.querySelectorAll('a[href^="#"]').forEach(anchor => {
    anchor.addEventListener('click', function (e) {
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

// Loading states for buttons
function setLoadingState(button, loading = true) {
    if (loading) {
        button.disabled = true;
        button.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i>Loading...';
    } else {
        button.disabled = false;
        button.innerHTML = button.dataset.originalText || 'Submit';
    }
}

// Form data persistence
function saveFormData(form) {
    const formData = new FormData(form);
    const data = {};

    for (let [key, value] of formData.entries()) {
        data[key] = value;
    }

    localStorage.setItem(`form_${form.id}`, JSON.stringify(data));
}

function loadFormData(form) {
    const savedData = localStorage.getItem(`form_${form.id}`);
    if (savedData) {
        const data = JSON.parse(savedData);
        Object.keys(data).forEach(key => {
            const field = form.querySelector(`[name="${key}"]`);
            if (field) {
                field.value = data[key];
            }
        });
    }
}

// Initialize form data persistence
document.querySelectorAll('form[id]').forEach(form => {
    loadFormData(form);

    form.addEventListener('input', () => saveFormData(form));
});

// Utility functions
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

// Search suggestions (if needed)
function initializeSearchSuggestions() {
    const searchInput = document.querySelector('input[name="search"]');
    if (searchInput) {
        let suggestionsContainer = null;

        searchInput.addEventListener('input', debounce(function() {
            const query = this.value.trim();
            if (query.length < 2) {
                hideSuggestions();
                return;
            }

            // Mock suggestions - replace with actual API call
            const suggestions = [
                'Bahan Baku Makanan',
                'Bahan Baku Minuman',
                'Packaging',
                'Bumbu Dapur',
                'Rempah-rempah'
            ].filter(item => item.toLowerCase().includes(query.toLowerCase()));

            showSuggestions(suggestions);
        }, 300));

        function showSuggestions(suggestions) {
            hideSuggestions();

            if (suggestions.length === 0) return;

            suggestionsContainer = document.createElement('div');
            suggestionsContainer.className = 'search-suggestions position-absolute bg-white border rounded shadow-sm';
            suggestionsContainer.style.cssText = `
                top: 100%;
                left: 0;
                right: 0;
                z-index: 1000;
                max-height: 200px;
                overflow-y: auto;
            `;

            suggestions.forEach(suggestion => {
                const item = document.createElement('div');
                item.className = 'p-2 hover-bg-light cursor-pointer';
                item.textContent = suggestion;
                item.addEventListener('click', () => {
                    searchInput.value = suggestion;
                    hideSuggestions();
                    searchInput.form.submit();
                });
                suggestionsContainer.appendChild(item);
            });

            searchInput.parentElement.style.position = 'relative';
            searchInput.parentElement.appendChild(suggestionsContainer);
        }

        function hideSuggestions() {
            if (suggestionsContainer) {
                suggestionsContainer.remove();
                suggestionsContainer = null;
            }
        }

        document.addEventListener('click', function(e) {
            if (!searchInput.contains(e.target)) {
                hideSuggestions();
            }
        });
    }
}

// Initialize all components when DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    initializeAnimations();
    initializeSearch();
    initializeProductCards();
    initializeForms();
    initializeScrollEffects();
    initializeCartCount();
    initializeLazyLoading();
    initializeSearchSuggestions();
});

// Export functions for global use
window.SAVORA = {
    showNotification,
    setLoadingState,
    updateCartCount
};
