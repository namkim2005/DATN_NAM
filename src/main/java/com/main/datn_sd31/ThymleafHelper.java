package com.main.datn_sd31;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.DecimalFormat;

@Component("utils")
public class ThymleafHelper {
    public static String formatCurrency(BigDecimal amount) {
        DecimalFormat df = new DecimalFormat("#,###");
        return df.format(amount);
    }
}
