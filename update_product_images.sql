-- Script untuk mengupdate URL gambar produk agar sesuai dengan file yang ada
-- Jalankan script ini setelah menjalankan savora_database.sql

USE savora_db;

-- Update image_url untuk produk yang sudah ada dengan URL yang benar
UPDATE products SET image_url = '/images/products/0d14c0af-427a-4c38-b502-058cf568d1ab.png' WHERE id = 1;
UPDATE products SET image_url = '/images/products/4eae39f8-3b02-4c4a-bbf8-2c87712ddc72.png' WHERE id = 2;
UPDATE products SET image_url = '/images/products/7a532ae5-ad77-4597-a42e-820146a1e548.png' WHERE id = 3;
UPDATE products SET image_url = '/images/products/92d96027-35bd-4bab-b6c0-9350291518a1.png' WHERE id = 4;
UPDATE products SET image_url = '/images/products/95735edc-559b-410f-aade-ca49bb556718.png' WHERE id = 5;
UPDATE products SET image_url = '/images/products/56664758-3705-4d42-bdd3-ff02108daf79.png' WHERE id = 6;
UPDATE products SET image_url = '/images/products/c6496077-c395-4bea-9223-387c20c3b258.png' WHERE id = 7;
UPDATE products SET image_url = '/images/products/0d14c0af-427a-4c38-b502-058cf568d1ab.png' WHERE id = 8;
UPDATE products SET image_url = '/images/products/4eae39f8-3b02-4c4a-bbf8-2c87712ddc72.png' WHERE id = 9;
UPDATE products SET image_url = '/images/products/7a532ae5-ad77-4597-a42e-820146a1e548.png' WHERE id = 10;

-- Verifikasi update
SELECT id, name, image_url FROM products ORDER BY id;

-- Jika ada produk lain yang perlu diupdate, gunakan query berikut:
-- UPDATE products SET image_url = '/images/products/{filename}.png' WHERE id = {product_id};