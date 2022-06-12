package com.example.conf;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "demo.dir")
public class Config {
    private String home;

    private String index;

    private String crawler;

    private int maxBlogCount;

    public int getMaxBlogCount() {
        return maxBlogCount;
    }

    public void setMaxBlogCount(int maxBlogCount) {
        this.maxBlogCount = maxBlogCount;
    }

    public String getHome() {
        return home;
    }

    public void setHome(String home) {
        this.home = home;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getCrawler() {
        return crawler;
    }

    public void setCrawler(String crawler) {
        this.crawler = crawler;
    }
}
