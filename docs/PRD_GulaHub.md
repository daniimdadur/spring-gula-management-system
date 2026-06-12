# PRODUCT REQUIREMENTS DOCUMENT (PRD)
## Gula Management System (GulaHub)
---

### 1. Informasi Dokumen
| Item | Keterangan |
|---|---|
| **Nama Produk** | Gula Management System (GulaHub) |
| **Versi** | 1.0 (Monolith Version) |
| **Tipe Aplikasi** | Web Application |
| **Arsitektur** | Monolithic (Modular Monolith) |
| **Backend** | Spring Boot |
| **Frontend** | Svelte |
| **Database** | MySQL (Single Shared Database) |
| **Event Bus** | Spring Application Events (In-Memory / Async Event Bus) |
| **Cache** | Redis |
| **Target Pengguna** | Pemilik UMKM (Owner), Admin Gudang, Admin Penjualan |

---

### 2. Latar Belakang
UMKM yang bergerak dalam produksi dan penjualan gula kelapa serta gula aren sering mengalami permasalahan dalam pengelolaan stok, pencatatan produksi, dan monitoring penjualan.
Proses yang masih dilakukan secara manual dapat menyebabkan:
* Kesalahan perhitungan stok.
* Sulit mengetahui jumlah barang yang tersedia.
* Sulit memantau pesanan pelanggan.
* Tidak adanya notifikasi ketika stok hampir habis.
* Sulit membuat laporan penjualan dan produksi.

Untuk mengatasi permasalahan tersebut, akan dibangun sebuah sistem berbasis web yang dapat mengelola seluruh proses bisnis mulai dari produksi hingga penjualan dalam satu kesatuan sistem aplikasi monolith.

---

### 3. Tujuan Produk
Tujuan utama sistem adalah:
1. Mempermudah pengelolaan stok barang.
2. Mempermudah pencatatan hasil produksi.
3. Mempermudah pengelolaan pesanan pelanggan.
4. Menyediakan dashboard monitoring bisnis secara real-time.
5. Mengimplementasikan arsitektur Event-Driven internal menggunakan Spring Application Events.
6. Menjadi studi kasus nyata pembelajaran pengembangan aplikasi Modular Monolith dengan Spring Boot.

---

### 4. Ruang Lingkup
Sistem mencakup:

**Included (Masuk dalam Cakupan):**
* Manajemen Produk
* Manajemen Inventori (Stok)
* Manajemen Produksi
* Manajemen Pesanan (Order)
* Manajemen Pembayaran
* Manajemen Pelanggan
* Dashboard Monitoring
* Notifikasi Stok Menipis (Internal & Email/WA log)

**Excluded (Versi Selanjutnya):**
* Integrasi Payment Gateway otomatis (dilakukan manual confirmation pada versi ini)
* Integrasi Marketplace
* Integrasi Ekspedisi / Kurir
* Mobile Application
* Multi Warehouse (Multi Gudang)

---

### 5. User Roles (Peran Pengguna)

#### 5.1 Owner (Pemilik)
Hak akses:
* Melihat dashboard ringkasan bisnis
* Mengelola master data produk (Create, Update, Delete)
* Mengelola inventori (Penyesuaian stok / Adjust stock)
* Melihat laporan produksi dan penjualan
* Melihat seluruh transaksi dan riwayat pembayaran

#### 5.2 Admin Gudang
Hak akses:
* Input stok masuk (Stock In) secara manual
* Input stok keluar (Stock Out) secara manual
* Mencatat hasil produksi baru
* Melihat daftar stok barang dan riwayat pergerakan stok

#### 5.3 Admin Penjualan
Hak akses:
* Membuat pesanan (Order) baru
* Mengubah status pesanan
* Mengonfirmasi pembayaran pesanan
* Mengelola data master pelanggan (Customer)

---

### 6. Modul Sistem

#### 6.1 Product Management (Modul Produk)
* **Deskripsi:** Modul untuk mengelola katalog produk yang dijual.
* **Data Produk:**
  * `id` : UUID (Primary Key)
  * `code` : String (Unique) - Kode produk (contoh: P001)
  * `name` : String - Nama produk
  * `category` : String - Kategori produk (Gula Aren / Gula Kelapa)
  * `description` : Text - Deskripsi detail produk
  * `weight` : Decimal - Berat produk (kg)
  * `price` : Decimal - Harga jual produk
  * `imageUrl` : String - URL foto produk
  * `status` : Boolean - Status aktif/nonaktif
* **Fitur:**
  * *Create Product:* Owner menambahkan produk baru.
  * *Update Product:* Owner mengubah informasi data produk.
  * *Delete Product:* Owner melakukan penonaktifan produk (Soft Delete).
  * *Product Listing:* Menampilkan seluruh produk (dengan pagination & pencarian).
  * *Product Detail:* Menampilkan detail spesifik dari satu produk.

#### 6.2 Inventory Management (Modul Inventori)
* **Deskripsi:** Mengelola jumlah stok barang yang tersedia dan mencatat histori pergerakan stok.
* **Data Inventory:**
  * `id` : UUID (Primary Key)
  * `productId` : UUID (Foreign Key ke Product)
  * `currentStock` : Integer - Stok saat ini
  * `minimumStock` : Integer - Stok minimum untuk memicu peringatan
  * `updatedAt` : Timestamp
* **Fitur:**
  * *Stock In:* Penambahan stok (manual atau otomatis dari hasil produksi).
  * *Stock Out:* Pengurangan stok (manual akibat rusak/hilang, atau otomatis akibat penjualan terkonfirmasi).
  * *Stock Adjustment:* Penyesuaian stok setelah proses audit fisik / stock opname.
  * *Stock Movement History:* Riwayat lengkap seluruh pergerakan stok masuk dan keluar.
* **Business Rules:**
  * Stok tidak boleh bernilai negatif.
  * Setiap perubahan stok wajib tercatat di riwayat pergerakan stok.
  * Sistem harus memicu notifikasi internal jika stok berada di bawah `minimumStock`.

#### 6.3 Production Management (Modul Produksi)
* **Deskripsi:** Mengelola proses pencatatan hasil produksi gula kelapa dan gula aren.
* **Data Production:**
  * `id` : UUID (Primary Key)
  * `productId` : UUID (Foreign Key ke Product)
  * `quantity` : Integer - Jumlah yang diproduksi
  * `productionDate` : Date - Tanggal produksi
  * `notes` : Text - Catatan produksi (misal: Batch pagi, nira dari kebun A)
* **Fitur:**
  * *Create Production:* Mencatat hasil produksi baru.
  * *Production History:* Melihat histori catatan produksi (filter berdasarkan tanggal/produk).
* **Business Rules:**
  * Setelah data produksi berhasil disimpan, sistem akan secara otomatis memicu event internal `ProductionCompletedEvent`.
  * Modul Inventori akan menangkap event tersebut dan menambah stok produk terkait secara otomatis.

#### 6.4 Order Management (Modul Pesanan)
* **Deskripsi:** Mengelola transaksi pesanan penjualan produk.
* **Data Order:**
  * `id` : UUID (Primary Key)
  * `customerId` : UUID (Foreign Key ke Customer)
  * `orderDate` : Timestamp - Waktu pemesanan
  * `totalAmount` : Decimal - Total nilai pesanan
  * `status` : Enum (`CREATED`, `PAID`, `PACKED`, `SHIPPED`, `COMPLETED`, `CANCELLED`)
* **Fitur:**
  * *Create Order:* Membuat pesanan baru untuk pelanggan.
  * *Update Status:* Memperbarui status pesanan seiring proses pengiriman/penyelesaian.
  * *View Orders:* Menampilkan daftar pesanan dengan filter status.
  * *Order Detail:* Menampilkan detail barang yang dipesan (Order Items) beserta statusnya.

#### 6.5 Customer Management (Modul Pelanggan)
* **Deskripsi:** Mengelola data master pelanggan untuk keperluan penjualan.
* **Data Customer:**
  * `id` : UUID (Primary Key)
  * `name` : String - Nama pelanggan
  * `phoneNumber` : String - Nomor telepon / WhatsApp
  * `address` : Text - Alamat pengiriman
  * `createdAt` : Timestamp
* **Fitur:**
  * *Create Customer:* Menambah data pelanggan baru.
  * *Update Customer:* Mengubah informasi data pelanggan.
  * *Customer List:* Menampilkan daftar pelanggan dengan fitur pencarian.

#### 6.6 Payment Management (Modul Pembayaran)
* **Deskripsi:** Mengelola status pembayaran dari setiap pesanan.
* **Data Payment:**
  * `id` : UUID (Primary Key)
  * `orderId` : UUID (Foreign Key ke Order)
  * `amount` : Decimal - Jumlah uang yang dibayarkan
  * `paymentMethod` : Enum (`CASH`, `BANK_TRANSFER`, `OTHER`)
  * `status` : Enum (`PENDING`, `SUCCESS`, `FAILED`)
  * `paymentDate` : Timestamp
* **Fitur:**
  * *Confirm Payment:* Mengonfirmasi status pembayaran (mengubah status menjadi `SUCCESS` atau `FAILED`).
  * *Payment History:* Melihat riwayat pembayaran masuk.
* **Business Rules:**
  * Ketika pembayaran berhasil dikonfirmasi (`SUCCESS`), sistem mempublikasikan internal event `PaymentSuccessEvent`.
  * Modul Inventori akan menangkap event ini untuk secara otomatis memotong stok barang sesuai jumlah pesanan.

#### 6.7 Notification Management (Modul Notifikasi)
* **Deskripsi:** Mengelola pengiriman notifikasi internal sistem (log notifikasi).
* **Jenis Notifikasi:**
  * *Low Stock:* Memberi tahu Owner/Admin Gudang ketika stok produk tertentu di bawah batas minimum (Contoh: "Stok Gula Aren 250gr tersisa 8 pcs").
  * *Order Created:* Memberi tahu Admin Penjualan bahwa pesanan baru berhasil dibuat.
  * *Payment Success:* Memberi tahu Pelanggan/Owner bahwa pembayaran telah sukses diterima.

#### 6.8 Dashboard
* **Deskripsi:** Menampilkan ringkasan visual kondisi performa bisnis secara terpusat untuk Owner.
* **Widget Dashboard:**
  * *Sales Summary:* Total Penjualan Hari Ini dan Bulan Ini.
  * *Inventory Summary:* Total jenis produk dan total kuantitas stok yang tersedia.
  * *Low Stock Products:* Daftar produk dengan stok kritis (di bawah minimum).
  * *Top Selling Products:* Produk terlaris berdasarkan kuantitas penjualan.
  * *Recent Orders:* Daftar transaksi pesanan terbaru.

---

### 7. Event-Driven Monolith Architecture (Spring Events)
Meskipun berupa aplikasi Monolith, sistem ini menggunakan pendekatan **Event-Driven** internal untuk menjaga pemisahan tanggung jawab (decoupling) antar modul backend:

1. **OrderCreatedEvent**
   * **Publisher:** Order Module (saat order dibuat)
   * **Listener:** Notification Module (mencatat log notifikasi order baru)

2. **PaymentSuccessEvent**
   * **Publisher:** Payment Module (saat pembayaran dikonfirmasi sukses)
   * **Listener:** 
     * Inventory Module (memotong stok barang)
     * Notification Module (mencatat log notifikasi sukses bayar)

3. **ProductionCompletedEvent**
   * **Publisher:** Production Module (saat produksi selesai dicatat)
   * **Listener:** Inventory Module (menambah stok barang masuk)

4. **InventoryUpdatedEvent**
   * **Publisher:** Inventory Module (setiap perubahan stok)
   * **Listener:** Dashboard Module (menghapus/meng-evict cache dashboard agar data ter-refresh)

5. **LowStockEvent**
   * **Publisher:** Inventory Module (saat stok di bawah batas minimum)
   * **Listener:** Notification Module (mengirim log peringatan stok menipis)

---

### 8. Strategi Caching (Redis)
Untuk mempercepat performa baca dan mengurangi beban query database MySQL:
* **Product Cache:** Caching daftar katalog produk. Key: `products`. TTL: 10 menit. Di-evict saat ada modifikasi produk.
* **Product Detail Cache:** Caching detail produk tertentu. Key: `product:{id}`. TTL: 10 menit. Di-evict saat produk terkait diupdate/dihapus.
* **Dashboard Cache:** Caching data ringkasan dashboard. Key: `dashboard_summary`. TTL: 5 menit. Di-evict otomatis saat menerima event `InventoryUpdatedEvent` atau `PaymentSuccessEvent`.

---

### 9. Non-Functional Requirements (NFR)
* **Performance:**
  * API response time rata-rata di bawah 200 ms (lebih cepat karena tidak ada network latency antar-service).
  * Halaman Dashboard terload di bawah 1 detik menggunakan Redis Cache.
* **Maintainability & Decoupling:**
  * Menerapkan struktur **Modular Monolith** di mana setiap modul fungsional (Product, Inventory, Order, dll.) dikelompokkan dalam package terpisah.
  * Komunikasi antar modul menggunakan Spring Events, menghindari dependency melingkar (circular dependency).
* **Scalability:**
  * Aplikasi dideploy sebagai single unit (JAR) dan dapat diskalakan secara horizontal di balik Load Balancer, dengan session dan cache terpusat di Redis.
* **Security:**
  * JWT Authentication terpusat pada Spring Security filter chain di backend monolith.
  * Role-Based Access Control (RBAC) diterapkan pada level Controller/API endpoint.
  * Password disimpan menggunakan BCrypt hashing.

---

### 10. Teknologi Utama
* **Frontend:** Svelte, Vite
* **Backend Framework:** Spring Boot 3.x, Spring Security
* **Database:** MySQL 8.0 (Single Database)
* **Caching & Session:** Redis 7.x
* **Event Broker:** Spring Application Events (In-Process Event Bus)
* **Authentication:** JWT (JSON Web Token)
* **Containerization:** Docker & Docker Compose (Single compose file untuk monolith app, MySQL, dan Redis)

---

### 11. Indikator Keberhasilan (Success Metrics)
* **Operasional:**
  * Akurasi stok tercatat 100% sinkron antara fisik dan sistem.
  * Riwayat inventori dapat dilacak secara kronologis tanpa terputus.
* **Teknis:**
  * Alur bisnis asinkron menggunakan Spring Application Events berjalan lancar tanpa kehilangan data.
  * Penggunaan Redis Cache berhasil mengurangi beban query database untuk halaman katalog dan dashboard minimal 50%.
  * Struktur kode Modular Monolith terjaga tanpa ada circular dependency antar package modul.

---
*GulaHub Management System v1.0 - Product Requirements Document (PRD)*
