package com.example.services;

import com.example.conf.Config;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class LuceneService implements DisposableBean {
    private static final Logger log= LoggerFactory.getLogger(LuceneService.class);

    private StandardAnalyzer analyzer;

    private IndexWriter writer;

    public LuceneService(@Autowired Config config){
        log.info("初始化LuceneService...");
        analyzer=new StandardAnalyzer();
        File dir=new File(config.getIndex());
        Directory index=null;
        IndexWriterConfig writeConfig=null;
        try {
            index= FSDirectory.open(dir.toPath());
            writeConfig=new IndexWriterConfig(analyzer);
            writer=new IndexWriter(index, writeConfig);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    public Boolean addDocument(String fid, String blogId, Document doc){
        log.info("updateDocument, fid=["+fid+"], blogId=["+blogId+"], writer=["+writer.toString()+"]...");

        try{
            writer.updateDocument(new Term(fid, blogId), doc);
            writer.commit();
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 检索标题中包含关键字kw的文档
     * @param kw     关键字
     * @param pageNo    页号
     * @param pageSize  页大小
     * @return  resualMap  Map    key="results"——结果文档的List；key="total"——结果总数的List（该List仅一个元素）
     * @throws Exception
     */
    public Map<String, List> queryByTitleKw(String kw, int pageNo, int pageSize) throws Exception{
        log.info("queryByTitleKw，Title Kw=["+kw+"]， pageNo=["+pageNo+"]， pageSize="+pageSize+"]...");
        IndexReader reader=DirectoryReader.open(writer);
        IndexSearcher searcher=new IndexSearcher(reader);

        QueryParser parser=new QueryParser("TITLE", analyzer);
        Query query=parser.parse(kw);

//        PhraseQuery.Builder builder=new PhraseQuery.Builder();
//        builder.add(new Term("TITLE", "腾"));
//        builder.add(new Term("TITLE", "讯"));
//        PhraseQuery query=builder.build();

        TopDocs docs=searcher.search(query, pageNo*pageSize);
        ScoreDoc[] hits=docs.scoreDocs;
        Map<String, List> resultMap=new HashMap<>();
        List<Document> results=new ArrayList<>();
        List<Integer> totalList=new ArrayList<>();
        totalList.add(hits.length);
        resultMap.put("results", results);
        resultMap.put("total", totalList);
        int startNO=(pageNo-1)*pageSize;        //根据分页参数返回分页结果
        ScoreDoc doc;
        if(hits.length==0){
            return resultMap;
        }
        try{
            doc=hits[startNO];
        }catch (ArrayIndexOutOfBoundsException e){
            int length=hits.length;
            int maxPageNo;
            if(length%pageSize!=0){
                maxPageNo=length/pageSize+1;
            }else {
                maxPageNo=length/pageSize;
            }
            reader.close();
            throw new Exception("页数超出结果范围，共查询到记录["+length+"]条，当前pageSize["+pageSize+"]，最大pageNo["+maxPageNo+"]");
        }
        for(int i=startNO;i<startNO+pageSize;i++){
            try{
                doc=hits[i];
                results.add(searcher.doc(hits[i].doc));
            }catch (ArrayIndexOutOfBoundsException e){
                return resultMap;
            }
        }
        reader.close();
        return resultMap;
    }

    /**
     * 查询已经建立索引的文档数量
     * @return
     * @throws Exception
     */
    public String queryTotalIndex() throws Exception {
        log.info("queryTotalIndex...");
        IndexReader reader=DirectoryReader.open(writer);
        String count=String.valueOf(reader.getDocCount("ID"));
        reader.close();
        return count;
    }

    /**
     * 检索发表时间范围内的blog
     * @param startTimeStamp    开始时间（包含）
     * @param endTimeStamp      结束时间（包含）
     * @param pageNo            页号
     * @param pageSize          页面大小
     * @return
     * @throws Exception
     */
    public Map<String, List> queryTimeRange(long startTimeStamp, long endTimeStamp, int pageNo, int pageSize) throws Exception {
        log.info("queryTimeRange， startTimeStamp=["+startTimeStamp+"]， endTimeStamp=["+endTimeStamp+"]...");
        IndexReader reader=DirectoryReader.open(writer);
        IndexSearcher searcher=new IndexSearcher(reader);
        Query query= LongPoint.newRangeQuery("POSTDATEINDEX", startTimeStamp, endTimeStamp);
        TopDocs docs=searcher.search(query, pageNo*pageSize);
        ScoreDoc[] hits=docs.scoreDocs;
        Map<String, List> resultMap=new HashMap<>();
        List<Document> results=new ArrayList<>();
        List<Integer> totalList=new ArrayList<>();
        totalList.add(hits.length);
        resultMap.put("results", results);
        resultMap.put("total", totalList);
        int startNO=(pageNo-1)*pageSize;        //根据分页参数返回分页结果
        ScoreDoc doc;
        if(hits.length==0){
            return resultMap;
        }
        try{
            doc=hits[startNO];
        }catch (ArrayIndexOutOfBoundsException e){
            int length=hits.length;
            int maxPageNo;
            if(length%pageSize!=0){
                maxPageNo=length/pageSize+1;
            }else {
                maxPageNo=length/pageSize;
            }
            reader.close();
            throw new Exception("页数超出结果范围，共查询到记录["+length+"]条，当前pageSize["+pageSize+"]，最大pageNo["+maxPageNo+"]");
        }
        for(int i=startNO;i<startNO+pageSize;i++){
            try{
                doc=hits[i];
                results.add(searcher.doc(hits[i].doc));
            }catch (ArrayIndexOutOfBoundsException e){
                return resultMap;
            }
        }
        reader.close();
        return resultMap;
    }

    /**
     * 布尔组合检索
     * 检索TITLE中包含titleKwList中字符且CONTENT中包含contentKwList中字符的文档
     * @param titleKwList
     * @param contentKwList
     * @param pageNo
     * @param pageSize
     * @return
     * @throws Exception
     */
    public Map<String, List> queryTitleAndContent(List<String> titleKwList, List<String> contentKwList, int pageNo, int pageSize) throws Exception {
        log.info("queryTitleAndContent， titleKwList=["+titleKwList+"]， contentKwList=["+contentKwList+"]， pageNo="+pageNo+"]， pageSize=["+pageSize+"]...");
        IndexReader reader=DirectoryReader.open(writer);
        IndexSearcher searcher=new IndexSearcher(reader);
        BooleanQuery.Builder booleanBuilder=new BooleanQuery.Builder();

        PhraseQuery.Builder titleBuilder=new PhraseQuery.Builder();
        for(String s:titleKwList){
            titleBuilder.add(new Term("TITLE", s));
        }
        PhraseQuery titleQuery= titleBuilder.build();

        PhraseQuery.Builder contentBuilder=new PhraseQuery.Builder();
        for(String s:contentKwList){
            contentBuilder.add(new Term("CONTENT", s));
        }
        PhraseQuery contentQuery=contentBuilder.build();

        booleanBuilder.add(titleQuery, BooleanClause.Occur.MUST);
        booleanBuilder.add(contentQuery, BooleanClause.Occur.MUST);
        BooleanQuery query=booleanBuilder.build();
        TopDocs docs=searcher.search(query, pageNo*pageSize);
        ScoreDoc[] hits=docs.scoreDocs;
        Map<String, List> resultMap=new HashMap<>();
        List<Document> results=new ArrayList<>();
        List<Integer> totalList=new ArrayList<>();
        totalList.add(hits.length);
        resultMap.put("results", results);
        resultMap.put("total", totalList);
        int startNO=(pageNo-1)*pageSize;        //根据分页参数返回分页结果
        ScoreDoc doc;
        if(hits.length==0){
            return resultMap;
        }
        try{
            doc=hits[startNO];
        }catch (ArrayIndexOutOfBoundsException e){
            int length=hits.length;
            int maxPageNo;
            if(length%pageSize!=0){
                maxPageNo=length/pageSize+1;
            }else {
                maxPageNo=length/pageSize;
            }
            reader.close();
            throw new Exception("页数超出结果范围，共查询到记录["+length+"]条，当前pageSize["+pageSize+"]，最大pageNo["+maxPageNo+"]");
        }
        for(int i=startNO;i<startNO+pageSize;i++){
            try{
                doc=hits[i];
                results.add(searcher.doc(hits[i].doc));
            }catch (ArrayIndexOutOfBoundsException e){
                return resultMap;
            }
        }
        reader.close();
        return resultMap;
    }

    /**
     * 布尔组合检索方法重载
     * 检索标题中包含titleKw且内容中包含contentKw的文档
     * 通过QueryParser实现的上一方法
     * @param titleKw
     * @param contentKw
     * @param pageNo
     * @param pageSize
     * @return
     * @throws Exception
     */
    public Map<String, List> queryTitleAndContent(String titleKw, String contentKw, int pageNo, int pageSize) throws Exception {
        log.info("queryTitleAndContent(BuParser)， titleKw=["+titleKw+"]， contentKw=["+contentKw+"]< pageNo=["+pageNo+"]， pageSize=["+pageSize+"]...");
        IndexReader reader=DirectoryReader.open(writer);
        IndexSearcher searcher=new IndexSearcher(reader);
        BooleanQuery.Builder booleanBuilder=new BooleanQuery.Builder();

        QueryParser titleParser=new QueryParser("TITLE", analyzer);
        Query titleQuery=titleParser.parse(titleKw);
        QueryParser contentParser=new QueryParser("CONTENT", analyzer);
        Query contentQuery=contentParser.parse(contentKw);

        booleanBuilder.add(titleQuery, BooleanClause.Occur.MUST);
        booleanBuilder.add(contentQuery, BooleanClause.Occur.MUST);
        BooleanQuery query=booleanBuilder.build();
        TopDocs docs=searcher.search(query, pageNo*pageSize);
        ScoreDoc[] hits=docs.scoreDocs;
        Map<String, List> resultMap=new HashMap<>();
        List<Document> results=new ArrayList<>();
        List<Integer> totalList=new ArrayList<>();
        totalList.add(hits.length);
        resultMap.put("results", results);
        resultMap.put("total", totalList);
        int startNO=(pageNo-1)*pageSize;        //根据分页参数返回分页结果
        ScoreDoc doc;
        if(hits.length==0){
            return resultMap;
        }
        try{
            doc=hits[startNO];
        }catch (ArrayIndexOutOfBoundsException e){
            int length=hits.length;
            int maxPageNo;
            if(length%pageSize!=0){
                maxPageNo=length/pageSize+1;
            }else {
                maxPageNo=length/pageSize;
            }
            reader.close();
            throw new Exception("页数超出结果范围，共查询到记录["+length+"]条，当前pageSize["+pageSize+"]，最大pageNo["+maxPageNo+"]");
        }
        for(int i=startNO;i<startNO+pageSize;i++){
            try{
                doc=hits[i];
                results.add(searcher.doc(hits[i].doc));
            }catch (ArrayIndexOutOfBoundsException e){
                return resultMap;
            }
        }
        reader.close();
        return resultMap;
    }

    @Override
    public void destroy() throws Exception {
        log.info("destroy...");
        if(this.writer==null){
            return;
        }
        try {
            log.info("索引关闭");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            log.info("尝试关闭索引失败");
        }
    }
}
