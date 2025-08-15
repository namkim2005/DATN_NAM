// Product List Page JavaScript

class ProductListManager {
    constructor() {
        this.selectedProducts = new Set();
        this.initializeEventListeners();
    }

    initializeEventListeners() {
        // Search input
        const searchInput = document.getElementById('searchInput');
        if (searchInput) {
            searchInput.addEventListener('input', this.debounce(() => this.filterProducts(), 300));
        }

        // Filter dropdowns
        const filterElements = ['statusFilter', 'categoryFilter', 'brandFilter', 'sortBy'];
        filterElements.forEach(id => {
            const element = document.getElementById(id);
            if (element) {
                element.addEventListener('change', () => this.filterProducts());
            }
        });

        // Product row clicks
        document.addEventListener('click', (e) => {
            const row = e.target.closest('.product-row');
            if (row && !e.target.closest('button, a')) {
                this.toggleProductSelection(row);
            }
        });
    }

    toggleProductSelection(row) {
        const productId = row.getAttribute('data-product-id');
        if (this.selectedProducts.has(productId)) {
            this.selectedProducts.delete(productId);
            row.classList.remove('tw-bg-blue-50', 'tw-border-blue-200');
        } else {
            this.selectedProducts.add(productId);
            row.classList.add('tw-bg-blue-50', 'tw-border-blue-200');
        }
        this.updateSelectedCount();
    }

    updateSelectedCount() {
        const count = this.selectedProducts.size;
        const countElement = document.getElementById('selectedCount');
        const applyBtn = document.getElementById('applyDiscountBtn');
        const hiddenInputs = document.getElementById('selectedProductIds');

        if (countElement) {
            countElement.textContent = `Đã chọn: ${count} sản phẩm`;
        }

        if (applyBtn) {
            if (count > 0) {
                applyBtn.disabled = false;
                applyBtn.classList.remove('tw-opacity-50', 'tw-cursor-not-allowed');
            } else {
                applyBtn.disabled = true;
                applyBtn.classList.add('tw-opacity-50', 'tw-cursor-not-allowed');
            }
        }

        // Update hidden inputs
        if (hiddenInputs) {
            hiddenInputs.innerHTML = '';
            this.selectedProducts.forEach(id => {
                const input = document.createElement('input');
                input.type = 'hidden';
                input.name = 'sanPhamIds';
                input.value = id;
                hiddenInputs.appendChild(input);
            });
        }
    }

    async filterProducts() {
        const params = this.buildFilterParams();
        
        try {
            const response = await fetch(`/admin/san-pham/api/filter?${params.toString()}`);
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            
            const data = await response.json();
            this.updateProductTable(data);
        } catch (error) {
            console.error('Error filtering products:', error);
            this.showError('Có lỗi xảy ra khi lọc sản phẩm');
        }
    }

    buildFilterParams() {
        const params = new URLSearchParams();
        
        const filters = {
            keyword: document.getElementById('searchInput')?.value,
            trangThaiHienThi: document.getElementById('statusFilter')?.value,
            danhMucId: document.getElementById('categoryFilter')?.value,
            thuongHieuId: document.getElementById('brandFilter')?.value
        };

        // Add non-empty filters
        Object.entries(filters).forEach(([key, value]) => {
            if (value && value.trim() !== '') {
                params.append(key, value);
            }
        });

        // Add sorting
        const sortBy = document.getElementById('sortBy')?.value;
        if (sortBy) {
            const [field, order] = sortBy.split(':');
            params.append('sortBy', field);
            params.append('sortOrder', order);
        }

        return params;
    }

    updateProductTable(products) {
        const tbody = document.querySelector('#productTable tbody');
        if (!tbody) return;

        tbody.innerHTML = '';
        
        if (products.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="7" class="tw-px-6 tw-py-8 tw-text-center tw-text-gray-500">
                        <div class="tw-flex tw-flex-col tw-items-center">
                            <svg class="tw-w-12 tw-h-12 tw-text-gray-400 tw-mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4"></path>
                            </svg>
                            <p class="tw-text-lg tw-font-medium">Không tìm thấy sản phẩm nào</p>
                            <p class="tw-text-sm">Thử thay đổi bộ lọc hoặc từ khóa tìm kiếm</p>
                        </div>
                    </td>
                </tr>
            `;
            return;
        }

        products.forEach(product => {
            const row = this.createProductRow(product);
            tbody.appendChild(row);
        });
    }

    createProductRow(product) {
        const row = document.createElement('tr');
        row.className = 'hover:tw-bg-gray-50 tw-transition-colors tw-duration-150 tw-cursor-pointer product-row';
        row.setAttribute('data-product-id', product.id);

        row.innerHTML = `
            <td class="tw-px-6 tw-py-4 tw-whitespace-nowrap">
                <div class="tw-flex-shrink-0 tw-h-16 tw-w-16">
                    <img src="${product.anhChinh || '/images/default-product.jpg'}" 
                         alt="${product.ten}"
                         class="tw-h-16 tw-w-16 tw-rounded-lg tw-object-cover tw-shadow-sm tw-border tw-border-gray-200" 
                         onerror="this.src='/images/default-product.jpg'" />
                </div>
            </td>
            <td class="tw-px-6 tw-py-4">
                <div class="tw-space-y-1">
                    <div class="tw-text-sm tw-font-medium tw-text-gray-900">${product.ma}</div>
                    <div class="tw-text-sm tw-font-semibold tw-text-gray-800">${product.ten}</div>
                    ${product.moTa ? `<div class="tw-text-xs tw-text-gray-500 tw-line-clamp-2 tw-max-w-xs" title="${product.moTa}">${product.moTa}</div>` : ''}
                </div>
            </td>
            <td class="tw-px-6 tw-py-4">
                <div class="tw-space-y-2">
                    <div class="tw-flex tw-flex-wrap tw-gap-1">
                        <span class="tw-inline-flex tw-items-center tw-px-2.5 tw-py-0.5 tw-rounded-full tw-text-xs tw-font-medium tw-bg-blue-100 tw-text-blue-800">${product.danhMuc}</span>
                        <span class="tw-inline-flex tw-items-center tw-px-2.5 tw-py-0.5 tw-rounded-full tw-text-xs tw-font-medium tw-bg-green-100 tw-text-green-800">${product.loaiThu}</span>
                    </div>
                    <div class="tw-text-xs tw-text-gray-600">
                        <span class="tw-font-medium tw-text-gray-700">Chất liệu:</span> ${product.chatLieu}
                    </div>
                    <div class="tw-text-xs tw-text-gray-600">
                        <span class="tw-font-medium tw-text-gray-700">Thương hiệu:</span> ${product.thuongHieu}
                    </div>
                    <div class="tw-text-xs tw-text-gray-600">
                        <span class="tw-font-medium tw-text-gray-700">Xuất xứ:</span> ${product.xuatXu}
                    </div>
                    <div class="tw-text-xs tw-text-gray-600">
                        <span class="tw-font-medium tw-text-gray-700">Kiểu dáng:</span> ${product.kieuDang}
                    </div>
                </div>
            </td>
            <td class="tw-px-6 tw-py-4">
                <div class="tw-space-y-2">
                    <div class="tw-text-sm">
                        <span class="tw-font-medium tw-text-gray-700">Số lượng:</span>
                        <span class="tw-text-lg tw-font-semibold tw-text-blue-600">${product.tongSoLuong}</span>
                    </div>
                    ${this.formatPriceSection(product)}
                </div>
            </td>
            <td class="tw-px-6 tw-py-4">
                <span class="tw-inline-flex tw-items-center tw-px-2.5 tw-py-0.5 tw-rounded-full tw-text-xs tw-font-medium tw-border ${product.trangThaiClass}">${product.trangThaiHienThi}</span>
            </td>
            <td class="tw-px-6 tw-py-4 tw-whitespace-nowrap tw-text-sm tw-text-gray-500">
                <div>${this.formatDate(product.ngayTao)}</div>
                <div>${this.formatTime(product.ngayTao)}</div>
            </td>
            <td class="tw-px-6 tw-py-4 tw-whitespace-nowrap tw-text-sm tw-font-medium">
                <div class="tw-flex tw-space-x-2">
                    <a href="/admin/san-pham/xem/${product.id}" class="tw-inline-flex tw-items-center tw-px-3 tw-py-1.5 tw-bg-blue-100 tw-text-blue-700 tw-rounded-md hover:tw-bg-blue-200 tw-transition-colors tw-duration-150 tw-ease-in-out">
                        <svg class="tw-w-4 tw-h-4 tw-mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path>
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"></path>
                        </svg>
                        Xem
                    </a>
                    <a href="/admin/san-pham/sua/${product.id}" class="tw-inline-flex tw-items-center tw-px-3 tw-py-1.5 tw-bg-yellow-100 tw-text-yellow-700 tw-rounded-md hover:tw-bg-yellow-200 tw-transition-colors tw-duration-150 tw-ease-in-out">
                        <svg class="tw-w-4 tw-h-4 tw-mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"></path>
                        </svg>
                        Sửa
                    </a>
                    <button onclick="productListManager.deleteProduct(event, ${product.id})" class="tw-inline-flex tw-items-center tw-px-3 tw-py-1.5 tw-bg-red-100 tw-text-red-700 tw-rounded-md hover:tw-bg-red-200 tw-transition-colors tw-duration-150 tw-ease-in-out">
                        <svg class="tw-w-4 tw-h-4 tw-mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path>
                        </svg>
                        Xoá
                    </button>
                </div>
            </td>
        `;

        return row;
    }

    formatPriceSection(product) {
        let html = '';
        
        // Giá gốc
        if (product.giaGocMin && product.giaGocMax) {
            html += `
                <div class="tw-text-sm">
                    <span class="tw-font-medium tw-text-gray-700">Giá gốc:</span>
                    <span class="tw-text-gray-600">${this.formatPriceRange(product.giaGocMin, product.giaGocMax)}</span>
                </div>
            `;
        }
        
        // Giá bán
        if (product.giaSauGiamMin && product.giaSauGiamMax) {
            html += `
                <div class="tw-text-sm">
                    <span class="tw-font-medium tw-text-gray-700">Giá bán:</span>
                    <span class="tw-text-lg tw-font-semibold tw-text-red-600">${this.formatPriceRange(product.giaSauGiamMin, product.giaSauGiamMax)}</span>
                </div>
            `;
        }
        
        // Thông tin giảm giá
        if (product.tenDotGiamGia) {
            html += `
                <div class="tw-text-xs tw-text-green-600">
                    <span class="tw-font-medium">Giảm giá:</span> ${product.tenDotGiamGia}
                </div>
            `;
        }
        
        return html;
    }

    formatPriceRange(min, max) {
        const formatPrice = (price) => {
            return new Intl.NumberFormat('vi-VN').format(price);
        };
        
        if (min === max) return formatPrice(min) + 'đ';
        return formatPrice(min) + ' - ' + formatPrice(max) + 'đ';
    }

    formatDate(dateString) {
        return new Date(dateString).toLocaleDateString('vi-VN');
    }

    formatTime(dateString) {
        return new Date(dateString).toLocaleTimeString('vi-VN', {
            hour: '2-digit',
            minute: '2-digit'
        });
    }

    deleteProduct(event, productId) {
        event.stopPropagation();
        if (confirm('Bạn có chắc chắn muốn xóa sản phẩm này?')) {
            window.location.href = `/admin/san-pham/xoa/${productId}`;
        }
    }

    showError(message) {
        // Create error notification
        const notification = document.createElement('div');
        notification.className = 'tw-fixed tw-top-4 tw-right-4 tw-bg-red-100 tw-border-l-4 tw-border-red-400 tw-text-red-700 tw-p-4 tw-rounded tw-shadow-lg tw-z-50';
        notification.innerHTML = `
            <div class="tw-flex tw-items-center">
                <svg class="tw-w-5 tw-h-5 tw-mr-2" fill="currentColor" viewBox="0 0 20 20">
                    <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd"/>
                </svg>
                <span>${message}</span>
            </div>
        `;
        
        document.body.appendChild(notification);
        
        // Remove after 5 seconds
        setTimeout(() => {
            notification.remove();
        }, 5000);
    }

    debounce(func, wait) {
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
}

// Initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    window.productListManager = new ProductListManager();
});

// Global function for delete (for onclick handlers)
function deleteProduct(event, productId) {
    if (window.productListManager) {
        window.productListManager.deleteProduct(event, productId);
    }
} 