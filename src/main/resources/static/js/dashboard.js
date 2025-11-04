// Dashboard JavaScript

document.addEventListener('DOMContentLoaded', function() {
    initializeDashboard();
});

function initializeDashboard() {
    // Initialize charts if Chart.js is available
    if (typeof Chart !== 'undefined') {
        initializeCharts();
    }

    // Initialize tooltips
    initializeTooltips();

    // Initialize data tables
    initializeDataTables();

    // Initialize real-time updates
    initializeRealTimeUpdates();
}

// Charts Initialization
function initializeCharts() {
    // Revenue Chart
    const revenueCtx = document.getElementById('revenueChart');
    if (revenueCtx) {
        new Chart(revenueCtx, {
            type: 'line',
            data: {
                labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun'],
                datasets: [{
                    label: 'Pendapatan',
                    data: [12000000, 19000000, 15000000, 25000000, 22000000, 30000000],
                    borderColor: '#4CAF50',
                    backgroundColor: 'rgba(76, 175, 80, 0.1)',
                    tension: 0.4,
                    fill: true
                }]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: {
                        display: false
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            callback: function(value) {
                                return 'Rp ' + (value / 1000000) + 'M';
                            }
                        }
                    }
                }
            }
        });
    }

    // Orders Chart
    const ordersCtx = document.getElementById('ordersChart');
    if (ordersCtx) {
        new Chart(ordersCtx, {
            type: 'bar',
            data: {
                labels: ['Sen', 'Sel', 'Rab', 'Kam', 'Jum', 'Sab', 'Min'],
                datasets: [{
                    label: 'Pesanan',
                    data: [12, 19, 15, 25, 22, 30, 18],
                    backgroundColor: '#FF9800',
                    borderRadius: 4
                }]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: {
                        display: false
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true
                    }
                }
            }
        });
    }
}

// Tooltips
function initializeTooltips() {
    const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });
}

// Data Tables
function initializeDataTables() {
    // Add sorting and filtering capabilities to tables
    const tables = document.querySelectorAll('.table');
    tables.forEach(table => {
        makeTableInteractive(table);
    });
}

function makeTableInteractive(table) {
    const headers = table.querySelectorAll('th[data-sort]');
    headers.forEach(header => {
        header.style.cursor = 'pointer';
        header.addEventListener('click', function() {
            sortTable(table, this);
        });
    });
}

function sortTable(table, header) {
    const tbody = table.querySelector('tbody');
    const rows = Array.from(tbody.querySelectorAll('tr'));
    const columnIndex = Array.from(header.parentNode.children).indexOf(header);
    const sortDirection = header.dataset.sort === 'asc' ? 'desc' : 'asc';

    // Update sort direction
    header.dataset.sort = sortDirection;

    // Clear other sort indicators
    table.querySelectorAll('th[data-sort]').forEach(th => {
        if (th !== header) th.dataset.sort = '';
    });

    // Sort rows
    rows.sort((a, b) => {
        const aValue = a.children[columnIndex].textContent.trim();
        const bValue = b.children[columnIndex].textContent.trim();

        if (sortDirection === 'asc') {
            return aValue.localeCompare(bValue);
        } else {
            return bValue.localeCompare(aValue);
        }
    });

    // Re-append sorted rows
    rows.forEach(row => tbody.appendChild(row));
}

// Real-time Updates
function initializeRealTimeUpdates() {
    // Simulate real-time updates (in production, use WebSocket or Server-Sent Events)
    setInterval(() => {
        updateStats();
    }, 30000); // Update every 30 seconds
}

function updateStats() {
    // Simulate fetching new data
    fetch('/api/dashboard/stats')
        .then(response => response.json())
        .then(data => {
            updateStatsCards(data);
        })
        .catch(error => {
            console.log('Real-time update failed:', error);
        });
}

function updateStatsCards(data) {
    if (data.totalOrders !== undefined) {
        document.querySelector('.stats-number').textContent = data.totalOrders;
    }
    // Update other stats as needed
}

// Quick Actions
function initializeQuickActions() {
    const quickActionButtons = document.querySelectorAll('.btn.w-100');

    quickActionButtons.forEach(button => {
        button.addEventListener('click', function(e) {
            const action = this.textContent.trim().toLowerCase();

            switch(action) {
                case 'tambah produk':
                    showAddProductModal();
                    break;
                case 'cari produk':
                    window.location.href = '/products';
                    break;
                case 'kelola pesanan':
                    window.location.href = '/orders';
                    break;
                default:
                    console.log('Action:', action);
            }
        });
    });
}

function showAddProductModal() {
    // Create and show add product modal
    const modal = document.createElement('div');
    modal.className = 'modal fade';
    modal.innerHTML = `
        <div class="modal-dialog modal-lg">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title">Tambah Produk Baru</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body">
                    <form id="addProductForm">
                        <div class="row">
                            <div class="col-md-6 mb-3">
                                <label class="form-label">Nama Produk</label>
                                <input type="text" class="form-control" name="name" required>
                            </div>
                            <div class="col-md-6 mb-3">
                                <label class="form-label">Kategori</label>
                                <select class="form-select" name="category" required>
                                    <option value="">Pilih Kategori</option>
                                </select>
                            </div>
                        </div>
                        <div class="mb-3">
                            <label class="form-label">Deskripsi</label>
                            <textarea class="form-control" name="description" rows="3"></textarea>
                        </div>
                        <div class="row">
                            <div class="col-md-6 mb-3">
                                <label class="form-label">Harga</label>
                                <div class="input-group">
                                    <span class="input-group-text">Rp</span>
                                    <input type="number" class="form-control" name="price" required>
                                </div>
                            </div>
                            <div class="col-md-6 mb-3">
                                <label class="form-label">Stok</label>
                                <input type="number" class="form-control" name="stock" required>
                            </div>
                        </div>
                        <div class="mb-3">
                            <label class="form-label">Gambar Produk</label>
                            <input type="file" class="form-control" name="image" accept="image/*">
                        </div>
                    </form>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Batal</button>
                    <button type="button" class="btn btn-primary" onclick="submitAddProduct()">Simpan</button>
                </div>
            </div>
        </div>
    `;

    document.body.appendChild(modal);
    const bsModal = new bootstrap.Modal(modal);
    bsModal.show();

    // Load categories
    loadCategories();
}

function loadCategories() {
    fetch('/api/categories')
        .then(response => response.json())
        .then(categories => {
            const select = document.querySelector('select[name="category"]');
            categories.forEach(category => {
                const option = document.createElement('option');
                option.value = category.id;
                option.textContent = category.name;
                select.appendChild(option);
            });
        });
}

function submitAddProduct() {
    const form = document.getElementById('addProductForm');
    const formData = new FormData(form);

    fetch('/api/products', {
        method: 'POST',
        body: formData
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            window.location.reload();
        } else {
            alert('Gagal menambah produk');
        }
    });
}

// Export functions
window.Dashboard = {
    showAddProductModal,
    submitAddProduct
};
