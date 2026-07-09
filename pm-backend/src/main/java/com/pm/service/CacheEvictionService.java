package com.pm.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Service
public class CacheEvictionService {
    private static final Logger log = LoggerFactory.getLogger(CacheEvictionService.class);
    private final CacheManager cacheManager;

    public CacheEvictionService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public void evictDashboardCaches() {
        try {
            clearCache("dashboardCache");
            clearCache("leaderDashboardCache");
            clearCache("statisticsCache");
        } catch (Exception e) {
            log.warn("Cache eviction failed (non-critical): {}", e.getMessage());
        }
    }

    private void clearCache(String name) {
        var cache = cacheManager.getCache(name);
        if (cache != null) cache.clear();
    }
}
