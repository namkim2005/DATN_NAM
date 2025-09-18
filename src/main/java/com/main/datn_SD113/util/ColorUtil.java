package com.main.datn_SD113.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class ColorUtil {
    
    // Regex pattern cho hex color
    private static final Pattern HEX_PATTERN = Pattern.compile("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$");
    
    // Màu mặc định khi không xác định được
    private static final String DEFAULT_COLOR = "#9CA3AF"; // gray-400
    
    // Map màu Vietnamese -> Hex code
    private static final Map<String, String> COLOR_NAME_MAP = new HashMap<String, String>() {{
        // Màu cơ bản
        put("đỏ", "#DC2626");           // red-600
        put("red", "#DC2626");
        put("xanh", "#2563EB");         // blue-600
        put("blue", "#2563EB");
        put("xanh lam", "#2563EB");
        put("xanh dương", "#2563EB");
        put("xanh lá", "#16A34A");      // green-600
        put("green", "#16A34A");
        put("xanh lá cây", "#16A34A");
        put("vàng", "#EAB308");         // yellow-500
        put("yellow", "#EAB308");
        put("cam", "#EA580C");          // orange-600
        put("orange", "#EA580C");
        put("tím", "#9333EA");          // purple-600
        put("purple", "#9333EA");
        put("violet", "#9333EA");
        put("hồng", "#EC4899");         // pink-500
        put("pink", "#EC4899");
        put("nâu", "#A16207");          // amber-700
        put("brown", "#A16207");
        put("đen", "#000000");          // black
        put("black", "#000000");
        put("trắng", "#FFFFFF");        // white
        put("white", "#FFFFFF");
        put("xám", "#6B7280");          // gray-500
        put("gray", "#6B7280");
        put("grey", "#6B7280");
        
        // Màu nâng cao
        put("be", "#F5F5DC");           // beige
        put("beige", "#F5F5DC");
        put("kem", "#FFF8DC");          // cornsilk
        put("cream", "#FFF8DC");
        put("navy", "#1E3A8A");         // blue-800
        put("xanh navy", "#1E3A8A");
        put("maroon", "#7F1D1D");       // red-900
        put("đỏ đô", "#7F1D1D");
        put("olive", "#65A30D");        // lime-600
        put("xanh ô liu", "#65A30D");
        put("khaki", "#F0E68C");
        put("turquoise", "#06B6D4");    // cyan-500
        put("xanh ngọc", "#06B6D4");
        put("indigo", "#4F46E5");       // indigo-600
        put("magenta", "#D946EF");      // fuchsia-500
    }};
    
    /**
     * Validate hex color code
     */
    public static boolean isValidHexColor(String color) {
        return color != null && HEX_PATTERN.matcher(color.trim()).matches();
    }
    
    /**
     * Get color hex code từ tên màu hoặc mã màu
     */
    public static String getColorHex(String colorInput) {
        if (colorInput == null || colorInput.trim().isEmpty()) {
            return DEFAULT_COLOR;
        }
        
        String color = colorInput.trim().toLowerCase();
        
        // Nếu đã là hex code thì validate và return
        if (color.startsWith("#")) {
            return isValidHexColor(color) ? color.toUpperCase() : DEFAULT_COLOR;
        }
        
        // Tìm trong map theo tên
        String hexColor = COLOR_NAME_MAP.get(color);
        if (hexColor != null) {
            return hexColor;
        }
        
        // Tìm partial match
        for (Map.Entry<String, String> entry : COLOR_NAME_MAP.entrySet()) {
            if (color.contains(entry.getKey()) || entry.getKey().contains(color)) {
                return entry.getValue();
            }
        }
        
        return DEFAULT_COLOR;
    }
    
    /**
     * Check if color is light (để xác định text color)
     */
    public static boolean isLightColor(String hex) {
        if (!isValidHexColor(hex)) {
            return false;
        }
        
        hex = hex.replace("#", "");
        int r = Integer.valueOf(hex.substring(0, 2), 16);
        int g = Integer.valueOf(hex.substring(2, 4), 16);
        int b = Integer.valueOf(hex.substring(4, 6), 16);
        
        // Calculate luminance
        double luminance = (0.299 * r + 0.587 * g + 0.114 * b) / 255;
        return luminance > 0.5;
    }
    
    /**
     * Get contrast text color (black or white)
     */
    public static String getContrastTextColor(String backgroundColor) {
        return isLightColor(backgroundColor) ? "#000000" : "#FFFFFF";
    }
} 