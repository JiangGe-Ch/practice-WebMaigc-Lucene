package com.example.pipeline;

import com.example.model.Blog;
import com.example.services.LuceneService;
import org.apache.lucene.document.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

public class LucenePipeline implements Pipeline {
    private static final Logger log= LoggerFactory.getLogger(LucenePipeline.class);

    private LuceneService lucenePipelineService;

    public LucenePipeline(LuceneService lucenePipelineService){
        log.info("初始化LucenePipeline...");
        this.lucenePipelineService=lucenePipelineService;
    }

    @Override
    public void process(ResultItems resultItems, Task task) {
        log.info("pipeline  ResultItems=["+resultItems+"], Task=["+task+"]...");

//        private String id;
//        private String title;
//        private long postDate;
//        private String content;
//        private List<String> tags;
//        private int readNum;
//        private int commentNum;
//        private String url;
//        private String author;
//        private int recommendCount;
//        private int oppositionCount;

        Document doc=new Document();
        Blog blog=resultItems.get("BLOG_INFO");
        doc.add(new TextField("TITLE", blog.getTitle(), Field.Store.YES));
        doc.add(new StringField("ID", blog.getId(), Field.Store.YES));
        doc.add(new LongPoint("POSTDATEINDEX", blog.getPostDate()));
        doc.add(new StoredField("POSTDATE", blog.getPostDate()));        //LongPoint字段默认不存储，添加StoredField以存储
        doc.add(new TextField("CONTENT", blog.getContent(), Field.Store.YES));
        doc.add(new StringField("TAGS", blog.getTags().toString(), Field.Store.YES));
        doc.add(new LongPoint("READNUM", blog.getReadNum()));
        doc.add(new LongPoint("COMMENTNUM", blog.getCommentNum()));
        doc.add(new StringField("URL", blog.getUrl(), Field.Store.YES));
        doc.add(new StringField("AUTHOR", blog.getAuthor(), Field.Store.YES));
        doc.add(new LongPoint("RECOMMENDCOUNT", blog.getCommentNum()));
        doc.add(new LongPoint("OPPOSITIONCOUNT", blog.getOppositionCount()));


        String id=blog.getId();
        if(!lucenePipelineService.addDocument("ID", id, doc)){
            log.info("添加blog到索引失败...");
        }
    }
}
