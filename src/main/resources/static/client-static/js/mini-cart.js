/**
 * Mini Cart JavaScript for Pure Thymeleaf
 * Handles only user interactions, no API calls
 */

class MiniCart {
    constructor() {
        this.cartItemsContainer = document.getElementById('cartItemsContainer');
        this.cartCount = document.getElementById('cartCount');
        this.cartTotal = document.getElementById('cartTotal');
        this.mobileHeaderCartCount = document.getElementById('mobileHeaderCartCount');
        this.headerCartCount = document.getElementById('headerCartCount');
        
        this.init();
    }

    init() {
        this.setupEventListeners();
        this.setupQuantityControls();
    }

    setupEventListeners() {
        // Listen for cart updates from other parts of the app
        document.addEventListener('cartUpdated', () => {
            // Refresh page to get updated cart data from server
            window.location.reload();
        });

        // Listen for add to cart events (for form submissions)
        document.addEventListener('addToCart', (e) => {
            this.showToast('Đã thêm sản phẩm vào giỏ hàng!', 'success');
            // Page will be reloaded by server-side redirect
        });
    }

    setupQuantityControls() {
        // Setup quantity change handlers
        if (this.cartItemsContainer) {
            this.cartItemsContainer.addEventListener('click', (e) => {
                if (e.target.classList.contains('mini-cart-quantity-btn')) {
                    e.preventDefault();
                    this.handleQuantityChange(e);
                }
                
                if (e.target.classList.contains('mini-cart-item-remove')) {
                    e.preventDefault();
                    this.handleRemoveItem(e);
                }
            });

            // Listen for quantity input changes
            this.cartItemsContainer.addEventListener('change', (e) => {
                if (e.target.classList.contains('mini-cart-quantity-input')) {
                    this.handleQuantityInputChange(e);
                }
            });
        }
    }

    // Method to handle quantity changes (redirects to server-side update)
    handleQuantityChange(e) {
        const button = e.target;
        const action = button.dataset.action;
        const itemContainer = button.closest('.mini-cart-item');
        const itemId = itemContainer.dataset.itemId;
        
        if (action === 'increase') {
            window.location.href = `/gio-hang/cap-nhat/${itemId}?action=increase`;
        } else if (action === 'decrease') {
            window.location.href = `/gio-hang/cap-nhat/${itemId}?action=decrease`;
        }
    }

    // Method to handle quantity input changes
    handleQuantityInputChange(e) {
        const input = e.target;
        const itemId = input.closest('.mini-cart-item').dataset.itemId;
        const newQuantity = input.value;
        
        if (newQuantity > 0) {
            window.location.href = `/gio-hang/cap-nhat/${itemId}?newSoluong=${newQuantity}`;
        }
    }

    // Method to handle item removal
    handleRemoveItem(e) {
        const itemContainer = e.target.closest('.mini-cart-item');
        const itemId = itemContainer.dataset.itemId;
        
        if (confirm('Bạn có chắc muốn xóa sản phẩm này khỏi giỏ hàng?')) {
            window.location.href = `/gio-hang/xoa/${itemId}`;
        }
    }

    // Toast notification method
    showToast(message, type = 'success') {
        // Remove existing toast
        const existingToast = document.querySelector('.mini-cart-toast');
        if (existingToast) {
            existingToast.remove();
        }

        const toast = document.createElement('div');
        toast.className = `mini-cart-toast ${type}`;
        toast.innerHTML = `
            <div class="mini-cart-toast-header">
                <h6 class="mini-cart-toast-title">${type === 'success' ? 'Thành công' : type === 'error' ? 'Lỗi' : 'Cảnh báo'}</h6>
                <button class="mini-cart-toast-close">&times;</button>
            </div>
            <p class="mini-cart-toast-message">${message}</p>
        `;

        document.body.appendChild(toast);

        // Show toast
        setTimeout(() => {
            toast.classList.add('show');
        }, 100);

        // Auto hide after 3 seconds
        setTimeout(() => {
            toast.classList.remove('show');
            setTimeout(() => {
                toast.remove();
            }, 300);
        }, 3000);

        // Close button functionality
        const closeBtn = toast.querySelector('.mini-cart-toast-close');
        closeBtn.addEventListener('click', () => {
            toast.classList.remove('show');
            setTimeout(() => {
                toast.remove();
            }, 300);
        });
    }

    // Public method to show error toast
    showError(message) {
        this.showToast(message, 'error');
    }

    // Public method to refresh page (for external calls)
    refresh() {
        window.location.reload();
    }
}

// Initialize mini cart when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    window.miniCart = new MiniCart();
});

// Export for use in other scripts
if (typeof module !== 'undefined' && module.exports) {
    module.exports = MiniCart;
} 