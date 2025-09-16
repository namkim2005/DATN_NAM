// Wishlist functionality for Yuki Dresses
class WishlistManager {
    constructor() {
        this.init();
    }

    init() {
        // Add event listeners for wishlist buttons
        document.addEventListener('click', (e) => {
            if (e.target.closest('.wishlist-btn') || e.target.closest('[data-wishlist-toggle]')) {
                e.preventDefault();
                const button = e.target.closest('.wishlist-btn') || e.target.closest('[data-wishlist-toggle]');
                const productId = button.dataset.id || button.dataset.productId;
                if (productId) {
                    this.toggleWishlist(productId, button);
                }
            }
        });
    }

    async toggleWishlist(productId, buttonElement = null) {
        try {
            const response = await fetch('/yeu-thich/toggle', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                    [document.querySelector('meta[name="_csrf_header"]').content]: 
                        document.querySelector('meta[name="_csrf"]').content
                },
                body: 'productId=' + productId
            });

            if (response.status === 401) {
                this.showNotification('Vui lòng đăng nhập để sử dụng tính năng yêu thích', 'warning');
                return;
            }

            const data = await response.json();

            if (data.success) {
                // Update UI based on action
                this.updateWishlistUI(productId, data.status === 'added', buttonElement);
                this.showNotification(data.message, 'success');
            } else {
                this.showNotification(data.error || 'Có lỗi xảy ra', 'error');
            }
        } catch (error) {
            console.error('Wishlist error:', error);
            this.showNotification('Có lỗi xảy ra khi cập nhật danh sách yêu thích', 'error');
        }
    }

    updateWishlistUI(productId, isLiked, buttonElement = null) {
        // Update all wishlist buttons for this product
        const buttons = document.querySelectorAll(`[data-id="${productId}"], [data-product-id="${productId}"]`);
        
        buttons.forEach(button => {
            const icon = button.querySelector('i');
            if (icon) {
                if (isLiked) {
                    // Add to wishlist
                    icon.classList.remove('far', 'text-gray-400');
                    icon.classList.add('fas', 'text-red-500');
                    button.classList.add('text-red-500');
                    button.classList.remove('text-gray-400');
                } else {
                    // Remove from wishlist
                    icon.classList.remove('fas', 'text-red-500');
                    icon.classList.add('far', 'text-gray-400');
                    button.classList.add('text-gray-400');
                    button.classList.remove('text-red-500');
                }
            }
        });
    }

    removeFromWishlist(productId, cardElement = null) {
        if (confirm('Bạn có chắc muốn xóa sản phẩm này khỏi danh sách yêu thích?')) {
            this.toggleWishlist(productId).then(() => {
                if (cardElement) {
                    // Animate removal
                    cardElement.style.transform = 'scale(0)';
                    cardElement.style.opacity = '0';
                    
                    setTimeout(() => {
                        cardElement.remove();
                        
                        // Check if wishlist page is empty
                        const remainingCards = document.querySelectorAll('.product-card');
                        if (remainingCards.length === 0 && window.location.pathname.includes('yeu-thich')) {
                            location.reload();
                        }
                    }, 300);
                }
            });
        }
    }

    showNotification(message, type = 'info') {
        // Remove existing notifications
        const existingNotifications = document.querySelectorAll('.wishlist-notification');
        existingNotifications.forEach(n => n.remove());

        // Create new notification
        const notification = document.createElement('div');
        notification.className = `alert alert-${this.getBootstrapAlertClass(type)} alert-dismissible fade show position-fixed wishlist-notification`;
        notification.style.cssText = 'top: 20px; right: 20px; z-index: 1050; min-width: 300px; max-width: 400px;';
        notification.innerHTML = `
            <div class="d-flex align-items-center">
                <i class="fas fa-${this.getNotificationIcon(type)} me-2"></i>
                <span>${message}</span>
                <button type="button" class="btn-close ms-auto" data-bs-dismiss="alert"></button>
            </div>
        `;
        
        document.body.appendChild(notification);
        
        // Auto remove after 3 seconds
        setTimeout(() => {
            if (notification.parentNode) {
                notification.remove();
            }
        }, 3000);
    }

    getBootstrapAlertClass(type) {
        switch (type) {
            case 'success': return 'success';
            case 'error': return 'danger';
            case 'warning': return 'warning';
            default: return 'info';
        }
    }

    getNotificationIcon(type) {
        switch (type) {
            case 'success': return 'check-circle';
            case 'error': return 'exclamation-circle';
            case 'warning': return 'exclamation-triangle';
            default: return 'info-circle';
        }
    }
}

// Initialize wishlist manager when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    window.wishlistManager = new WishlistManager();
});

// Global functions for backward compatibility
function toggleWishlist(productId, buttonElement = null) {
    if (window.wishlistManager) {
        return window.wishlistManager.toggleWishlist(productId, buttonElement);
    }
}

function removeFromWishlist(element) {
    if (window.wishlistManager) {
        const productId = element.dataset.productId;
        const productCard = element.closest('.product-card');
        return window.wishlistManager.removeFromWishlist(productId, productCard);
    }
}

function viewProduct(productId) {
    window.location.href = '/san-pham/chi-tiet/' + productId;
}
