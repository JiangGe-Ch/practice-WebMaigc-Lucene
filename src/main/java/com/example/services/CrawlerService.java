package com.example.services;

import com.example.conf.Config;
import com.example.crawler.CnBlogCrawler;
import com.example.pipeline.LucenePipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.pipeline.JsonFilePipeline;

@Component
public class CrawlerService {

    private static final Logger log= LoggerFactory.getLogger(CrawlerService.class);

    public CrawlerService(@Autowired Config config, @Autowired LuceneService lucenePipelineService){
        log.info("初始化CrawlerService...");
        Site site=Site
                .me()
                .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.5005.63 Safari/537.36")
                .setSleepTime(500)
                .setRetryTimes(3);
        Spider cnBlogSpider=Spider.create(new CnBlogCrawler(site, config));
        cnBlogSpider.addUrl("https://www.cnblogs.com/tencent-cloud-native/default.html?page=1");
        cnBlogSpider.addPipeline(new LucenePipeline(lucenePipelineService));
        cnBlogSpider.addPipeline(new JsonFilePipeline(config.getCrawler()));
        cnBlogSpider.thread(1);
        cnBlogSpider.runAsync();
    }
}
