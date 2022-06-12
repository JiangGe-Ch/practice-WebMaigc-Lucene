package com.example.model;

import java.lang.reflect.Field;
import java.util.List;

public class Blog {
    /**
     * 博客文章唯一id
     */
    private String id;
    /**
     * 博客内容标题
     */
    private String title;

    /**
     * 文章发表日期
     */
    private long postDate;

    /**
     * 文章内容
     */
    private String content;

    /**
     * 文章标签
     */
    private List<String> tags;

    /**
     * 阅读数
     */
    private int readNum;

    /**
     * 评论数
     */
    private int commentNum;

    /**
     * 页面url
     */
    private String url;

    /**
     * 作者
     */
    private String author;
    /**
     * 推荐数
     */
    private int recommendCount=-1;
    /**
     * 反对数
     */
    private int oppositionCount=-1;

    private int setValCount=0;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        this.setValCount++;
    }

    public long getPostDate() {
        return postDate;
    }

    public void setPostDate(long postDate) {
        this.postDate = postDate;
        this.setValCount++;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
        this.setValCount++;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
        this.setValCount++;
    }

    public int getReadNum() {
        return readNum;
    }

    public void setReadNum(int readNum) {
        this.readNum = readNum;
        this.setValCount++;
    }

    public int getCommentNum() {
        return commentNum;
    }

    public void setCommentNum(int commentNum) {
        this.commentNum = commentNum;
        this.setValCount++;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
        this.setValCount++;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
        this.setValCount++;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
        this.setValCount++;
    }

    public int getRecommendCount() {
        return recommendCount;
    }

    public void setRecommendCount(int recommendCount) {
        this.recommendCount = recommendCount;
        this.setValCount++;
    }

    public int getOppositionCount() {
        return oppositionCount;
    }

    public void setOppositionCount(int oppositionCount) {
        this.oppositionCount = oppositionCount;
        this.setValCount++;
    }

    public Boolean isComplete(){
        if(this.setValCount==11){
            return true;
        }else {
            return false;
        }
    }

    @Override
    public String toString() {
        StringBuilder str=new StringBuilder("[");
        for(Field field:Blog.class.getDeclaredFields()){
            String fieldName=field.getName();
            str.append(field.getName());
            str.append("=");
            try {
                if(fieldName.equals("content")){
                    str.append("content 省略");
                }else {
                    str.append(field.get(Blog.this));
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            str.append("，");
        }
        String strs=str.substring(0, str.length()-1);
        strs=strs+"]...";
        return strs;
    }
}
