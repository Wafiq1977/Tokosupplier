# TODO: Perbaikan Fitur Tambah Produk Supplier

## Langkah-langkah yang perlu dilakukan:

1. **Update Model Product.java**
   - Tambahkan field `unit` dengan tipe String
   - Tambahkan anotasi validasi (@NotBlank, @NotNull, @Size, dll.)

2. **Update Database Schema (savora_database.sql)**
   - Tambahkan kolom `unit` VARCHAR(50) ke tabel products

3. **Update ProductController.java**
   - Tambahkan @Valid ke @ModelAttribute Product
   - Tambahkan BindingResult untuk menangani error validasi
   - Jika ada error, return ke form dengan pesan error

4. **Update FileUploadConfig.java**
   - Konfigurasi static resource untuk folder uploads agar gambar dapat diakses

5. **Test Kompilasi**
   - Jalankan `mvn clean compile` untuk memastikan tidak ada error

6. **Test Fitur**
   - Jalankan aplikasi dan test tambah produk
   - Pastikan data tersimpan ke database dengan benar
   - Pastikan gambar terupload dan dapat diakses

## Status Progress:

- [x] Update Model Product.java - SELESAI
- [x] Update Database Schema (savora_database.sql) - SELESAI
- [x] Update ProductController.java - SELESAI
- [x] Update FileUploadConfig.java - SUDAH ADA
- [x] Test Kompilasi - SUDAH DIJALANKAN
- [x] Perbaikan JavaScript untuk input harga - SELESAI
- [x] Perbaikan validasi harga di controller - SELESAI
- [x] Test Fitur - SUDAH DITEST OLEH USER
