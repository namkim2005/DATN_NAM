// Global variables
let currentFilters = {
    loaiThu: '',
    danhMuc: '',
    size: '',
    mauSac: '',
    kieuDang: '',
    thuongHieu: '',
    xuatXu: '',
    priceRange: 10000000
};

let filterLabels = {
    loaiThu: 'Loại thú',
    danhMuc: 'Danh mục',
    size: 'Kích cỡ',
    mauSac: 'Màu sắc',
    kieuDang: 'Kiểu dáng',
    thuongHieu: 'Thương hiệu',
    xuatXu: 'Xuất xứ'
};

// Initialize page
document.addEventListener('DOMContentLoaded', function() {
    initializeFilters();
    setupEventListeners();
});

// Initialize filter functionality
function initializeFilters() {
    // Close all select dropdowns when clicking outside
    document.addEventListener('click', function(e) {
        if (!e.target.closest('.custom-select')) {
            closeAllSelects();
        }
    });

    // Setup price range slider
    const priceSlider = document.getElementById('priceRange');
    if (priceSlider) {
        updatePriceDisplay(priceSlider.value);
    }
}

// Setup event listeners
function setupEventListeners() {
    // Price range slider
    const priceSlider = document.getElementById('priceRange');
    if (priceSlider) {
        priceSlider.addEventListener('change', function() {
            currentFilters.priceRange = parseInt(this.value);
            applyFilters();
        });
    }
}

// Toggle select dropdown
function toggleSelect(trigger) {
    const selectContainer = trigger.closest('.custom-select');
    const options = selectContainer.querySelector('.select-options');
    const allSelects = document.querySelectorAll('.custom-select');
    
    // Close other selects
    allSelects.forEach(select => {
        if (select !== selectContainer) {
            select.querySelector('.select-trigger').classList.remove('active');
            select.querySelector('.select-options').classList.remove('show');
        }
    });

    // Toggle current select
    trigger.classList.toggle('active');
    options.classList.toggle('show');
}

// Close all select dropdowns
function closeAllSelects() {
    document.querySelectorAll('.custom-select').forEach(select => {
        select.querySelector('.select-trigger').classList.remove('active');
        select.querySelector('.select-options').classList.remove('show');
    });
}

// Handle option selection
function selectOption(option, filterType) {
    const value = option.dataset.value;
    const text = option.textContent;
    const selectContainer = option.closest('.custom-select');
    const trigger = selectContainer.querySelector('.select-trigger');
    const selectedText = trigger.querySelector('.selected-text');

    // Update display
    selectedText.textContent = text;
    currentFilters[filterType] = value;

    // Update selected state
    selectContainer.querySelectorAll('.select-option').forEach(opt => {
        opt.classList.remove('selected');
    });
    option.classList.add('selected');

    // Close dropdown
    closeAllSelects();

    // Apply filters
    applyFilters();
}

// Update price display
function updatePriceDisplay(value) {
    const maxPriceElement = document.getElementById('maxPrice');
    if (maxPriceElement) {
        const formattedPrice = new Intl.NumberFormat('vi-VN').format(value);
        maxPriceElement.textContent = formattedPrice + 'đ';
    }
}

// Apply all filters
function applyFilters() {
    showLoading();
    updateActiveFilters();
    
    // Build query parameters
    const params = new URLSearchParams();
    Object.keys(currentFilters).forEach(key => {
        if (currentFilters[key] && currentFilters[key] !== '') {
            params.append(key, currentFilters[key]);
        }
    });

    // Make AJAX request
    fetch(`/san-pham/danh-sach/filter?${params.toString()}`, {
        method: 'GET',
        headers: {
            'X-Requested-With': 'XMLHttpRequest'
        }
    })
    .then(response => response.text())
    .then(html => {
        // Update URL without page reload
        const newUrl = window.location.pathname + (params.toString() ? '?' + params.toString() : '');
        window.history.pushState({}, '', newUrl);
        
        // Update product grid
        updateProductGrid(html);
        hideLoading();
    })
    .catch(error => {
        console.error('Error applying filters:', error);
        hideLoading();
    });
}

// Update active filters display
function updateActiveFilters() {
    const activeFiltersContainer = document.getElementById('activeFilters');
    const filterTagsContainer = document.getElementById('filterTags');
    
    // Clear existing tags
    filterTagsContainer.innerHTML = '';
    
    // Add new tags
    let hasActiveFilters = false;
    Object.keys(currentFilters).forEach(key => {
        if (currentFilters[key] && currentFilters[key] !== '' && key !== 'priceRange') {
            hasActiveFilters = true;
            const tag = createFilterTag(key, currentFilters[key]);
            filterTagsContainer.appendChild(tag);
        }
    });

    // Show/hide active filters container
    activeFiltersContainer.style.display = hasActiveFilters ? 'block' : 'none';
}

// Create filter tag element
function createFilterTag(filterType, value) {
    const tag = document.createElement('div');
    tag.className = 'filter-tag';
    
    // Get display text for the value
    const selectContainer = document.querySelector(`[data-filter="${filterType}"]`);
    const option = selectContainer.querySelector(`[data-value="${value}"]`);
    const displayText = option ? option.textContent : value;
    
    tag.innerHTML = `
        <span>${filterLabels[filterType]}: ${displayText}</span>
        <button class="remove-btn" onclick="removeFilter('${filterType}')">
            <iconify-icon icon="mdi:close"></iconify-icon>
        </button>
    `;
    
    return tag;
}

// Remove specific filter
function removeFilter(filterType) {
    currentFilters[filterType] = '';
    
    // Reset select display
    const selectContainer = document.querySelector(`[data-filter="${filterType}"]`);
    const trigger = selectContainer.querySelector('.select-trigger');
    const selectedText = trigger.querySelector('.selected-text');
    selectedText.textContent = `Chọn ${filterLabels[filterType].toLowerCase()}`;
    
    // Remove selected state
    selectContainer.querySelectorAll('.select-option').forEach(opt => {
        opt.classList.remove('selected');
    });
    
    applyFilters();
}

// Clear all filters
function clearAllFilters() {
    currentFilters = {
        loaiThu: '',
        danhMuc: '',
        size: '',
        mauSac: '',
        kieuDang: '',
        thuongHieu: '',
        xuatXu: '',
        priceRange: 10000000
    };

    // Reset all selects
    document.querySelectorAll('.custom-select').forEach(select => {
        const trigger = select.querySelector('.select-trigger');
        const selectedText = trigger.querySelector('.selected-text');
        const filterType = select.dataset.filter;
        selectedText.textContent = `Chọn ${filterLabels[filterType].toLowerCase()}`;
        
        select.querySelectorAll('.select-option').forEach(opt => {
            opt.classList.remove('selected');
        });
    });

    // Reset price slider
    const priceSlider = document.getElementById('priceRange');
    if (priceSlider) {
        priceSlider.value = 10000000;
        updatePriceDisplay(10000000);
    }

    applyFilters();
}

// Update product grid with new data
function updateProductGrid(html) {
    const productGrid = document.getElementById('productGrid');
    const noProducts = document.getElementById('noProducts');
    
    // Check if there are products
    if (html.includes('product-card')) {
        productGrid.innerHTML = html;
        productGrid.style.display = 'grid';
        noProducts.style.display = 'none';
    } else {
        productGrid.style.display = 'none';
        noProducts.style.display = 'block';
    }
}

// Show loading spinner
function showLoading() {
    document.getElementById('loadingSpinner').style.display = 'flex';
}

// Hide loading spinner
function hideLoading() {
    document.getElementById('loadingSpinner').style.display = 'none';
}

// Add to cart function
function addToCart(productId) {
    // Implementation for adding to cart
    console.log('Adding product to cart:', productId);
    // Add your cart logic here
}

// Add to wishlist function
function addToWishlist(productId) {
    // Implementation for adding to wishlist
    console.log('Adding product to wishlist:', productId);
    // Add your wishlist logic here
}

// Setup select option click handlers
document.addEventListener('click', function(e) {
    if (e.target.closest('.select-option')) {
        const option = e.target.closest('.select-option');
        const selectContainer = option.closest('.custom-select');
        const filterType = selectContainer.dataset.filter;
        selectOption(option, filterType);
    }
}); 