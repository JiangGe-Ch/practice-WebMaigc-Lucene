package com.example.crawler;

import com.example.conf.Config;
import com.example.model.Blog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Html;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CnBlogCrawler implements PageProcessor {

    private static final Logger log= LoggerFactory.getLogger(CnBlogCrawler.class);

    private Site site;

    private final SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm");

    private Map<String, Blog> blogMap=new HashMap<>();

    private static int blogCount=0;

    private static int maxBlogCount;

    private static int pageCount=1;


    public CnBlogCrawler(Site site, Config config){
        log.info("初始化CnBlogCrawler...");
        this.site=site;
        this.maxBlogCount=config.getMaxBlogCount();
    }

    @Override
    public void process(Page page) {
        String url=page.getRequest().getUrl();
        log.info("获取到url["+url+"]...");
        //目录页
        if(url.startsWith("https://www.cnblogs.com/tencent-cloud-native/default.html?page=")){
            log.info("解析目录页...");
            List<String> items=page.getHtml().xpath("//div[@class='forFlow']/div/div[@class='postTitle']/a/@href").all();
            log.info("解析到文章内容页地址"+items.size()+"条...");
            page.addTargetRequests(items);
            blogCount+= items.size();
            if(blogCount<maxBlogCount){
                pageCount++;
                page.addTargetRequest("https://www.cnblogs.com/tencent-cloud-native/default.html?page="+pageCount);
                log.info("blogCount=["+blogCount+"]， addTargetRequest["+"https://www.cnblogs.com/tencent-cloud-native/default.html?page="+pageCount+"]...");
            }
            page.setSkip(true);
        }else if(url.startsWith("https://www.cnblogs.com/tencent-cloud-native/p")){
            log.info("解析到文章内容页，地址["+url+"]...");
            Html html=page.getHtml();
            String id=url.substring(url.indexOf("/p/")+3, url.indexOf(".html"));
            String title=html.xpath("//div[@class='forFlow']//h1[@class='postTitle']/a/span/text()").get();
            log.info("xpath title=["+title+"]...");
            String postDate=html.xpath("//div[@class='forFlow']//div[@class='postDesc']/span[@id='post-date']/text()").get();
            String content=html.xpath("//div[@class='forFlow']//div[@class='postBody']/div[@id='cnblogs_post_body']/tidyText()").get();
            int readNum=Integer.parseInt(html.xpath("//div[@class='forFlow']//div[@class='postDesc']/span[@id='post_view_count']/text()").get());
            int commentNum=Integer.parseInt(html.xpath("//div[@class='forFlow']//div[@class='postDesc']/span[@id='post_comment_count']/text()").get());
            String author=html.xpath("//div[@id='blogTitle']/div[@class='title']/a/text()").get();

            Blog blog;
            if(this.blogMap.containsKey(id)){
                blog=this.blogMap.get(id);
            }else {
                blog=new Blog();
            }

            blog.setId(id);
            blog.setTitle(title);
            try {
                blog.setPostDate(sdf.parse(postDate).getTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            blog.setContent(content);
            blog.setReadNum(readNum);
            blog.setCommentNum(commentNum);
            blog.setUrl(url);
            blog.setAuthor(author);

            //添加动态数据（推荐数和反对数）的url
            String recAndOppoRequestUrl="https://www.cnblogs.com/tencent-cloud-native/ajax/BlogPostInfo.aspx?blogId=625070&postId="+id+"&blogUserGuid=234ee4ac-f280-4e99-6724-08d7f668918e&_="+System.currentTimeMillis();
            page.addTargetRequest(recAndOppoRequestUrl);

            //添加动态数据（标签）的url
            String tagsRequestUrl="https://www.cnblogs.com/tencent-cloud-native/ajax/CategoriesTags.aspx?blogId=625070&postId="+id+"&_="+System.currentTimeMillis();
            page.addTargetRequest(tagsRequestUrl);
            if(blog.isComplete()){
                this.blogMap.remove(id);
                page.putField("BLOG_INFO", blog);
            }else {
                this.blogMap.put(id, blog);
                page.setSkip(true);
            }

        }else if(url.startsWith("https://www.cnblogs.com/tencent-cloud-native/ajax/BlogPostInfo.aspx")){
            log.info("解析到动态数据（推荐数、反对数）请求地址["+url+"]...");
            Html html=page.getHtml();
            String postId=url.substring(url.indexOf("postId=")+7, url.indexOf("&blogUserGuid"));
            Blog blog=this.blogMap.get(postId);
            log.info("对应postid=["+postId+"]...");
            int recommend=Integer.parseInt(html.xpath("//div[@id='div_digg']/div[@class='diggit']/span/text()").get());
            int opposit=Integer.parseInt(html.xpath("//div[@id='div_digg']/div[@class='buryit']/span/text()").get());
            blog.setRecommendCount(recommend);
            blog.setOppositionCount(opposit);
            log.info("blog 构建(推荐数/反对数)完成，blog："+this.blogMap.get(postId));
            if(blog.isComplete()){
                this.blogMap.remove(postId);
                page.putField("BLOG_INFO", blog);
            }else {
                page.setSkip(true);
            }
        } else if(url.startsWith("https://www.cnblogs.com/tencent-cloud-native/ajax/CategoriesTags.aspx")){
            log.info("解析到动态数据（标签）请求地址["+url+"]...");
            Html html=page.getHtml();
            //https://www.cnblogs.com/tencent-cloud-native/ajax/CategoriesTags.aspx?blogId=625070&postId=15751705&_=1654591012813
            String postId=url.substring(url.indexOf("postId")+7, url.indexOf("&_"));
            Blog blog=this.blogMap.get(postId);
            List<String> tags=html.xpath("//div/a/text()").all();
            log.info("获取到blogPostid["+postId+"] 的标签["+tags+"]...");
            blog.setTags(tags);
            log.info("blog tags 构建完成，blog：["+blog+"]...");
            if(blog.isComplete()){
                this.blogMap.remove(postId);
                page.putField("BLOG_INFO", blog);
            }else {
                page.setSkip(true);
            }
        }
        else {
            log.info("无法识别的url["+url+"]...");
            page.setSkip(true);
        }
    }

    @Override
    public Site getSite() {
        return this.site;
    }
}
