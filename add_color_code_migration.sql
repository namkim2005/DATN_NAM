-- Migration: Add ma_mau column to mau_sac table
-- Thêm cột mã màu vào bảng mau_sac

ALTER TABLE mau_sac 
ADD COLUMN ma_mau VARCHAR(7) NULL;

-- Cập nhật một số màu mặc định (tùy chọn)
UPDATE mau_sac SET ma_mau = '#FF0000' WHERE LOWER(ten) LIKE '%đỏ%' OR LOWER(ten) LIKE '%red%';
UPDATE mau_sac SET ma_mau = '#0000FF' WHERE LOWER(ten) LIKE '%xanh%' OR LOWER(ten) LIKE '%blue%';
UPDATE mau_sac SET ma_mau = '#000000' WHERE LOWER(ten) LIKE '%đen%' OR LOWER(ten) LIKE '%black%';
UPDATE mau_sac SET ma_mau = '#FFFFFF' WHERE LOWER(ten) LIKE '%trắng%' OR LOWER(ten) LIKE '%white%';
UPDATE mau_sac SET ma_mau = '#FFFF00' WHERE LOWER(ten) LIKE '%vàng%' OR LOWER(ten) LIKE '%yellow%';
UPDATE mau_sac SET ma_mau = '#00FF00' WHERE LOWER(ten) LIKE '%xanh lá%' OR LOWER(ten) LIKE '%green%';
UPDATE mau_sac SET ma_mau = '#FFC0CB' WHERE LOWER(ten) LIKE '%hồng%' OR LOWER(ten) LIKE '%pink%';
UPDATE mau_sac SET ma_mau = '#800080' WHERE LOWER(ten) LIKE '%tím%' OR LOWER(ten) LIKE '%purple%';
UPDATE mau_sac SET ma_mau = '#FFA500' WHERE LOWER(ten) LIKE '%cam%' OR LOWER(ten) LIKE '%orange%';
UPDATE mau_sac SET ma_mau = '#A52A2A' WHERE LOWER(ten) LIKE '%nâu%' OR LOWER(ten) LIKE '%brown%';

-- Kiểm tra kết quả
SELECT id, ma, ten, ma_mau FROM mau_sac; 