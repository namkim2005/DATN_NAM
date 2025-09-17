package com.main.datn_SD113.service;

import com.main.datn_SD113.dto.home.HomeProductDto;

import java.util.List;
import java.util.Map;

public interface HomePageService {
    List<HomeProductDto> getLatestProducts(int limit);
    Map<Integer, String> getCategoryImageMap();
    List<HomeProductDto> getBestSellingProducts(int limit);
} 