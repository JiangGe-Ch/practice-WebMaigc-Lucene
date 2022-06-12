package com.example.controller;

import com.example.model.Blog;
import com.example.model.QueryResponse;
import com.example.services.LuceneService;
import org.apache.lucene.document.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/query")
public class QueryController {

    private static final Logger log= LoggerFactory.getLogger(QueryController.class);

    private LuceneService luceneService;

    public QueryController(@Autowired LuceneService luceneService){
        log.info("初始化QueryController...");
        this.luceneService=luceneService;
    }

    /**
     * 面向前端的接口
     * 检索文档标题中有关键词kw的文档
     * @param kw
     * @param pageNo
     * @param pageSize
     * @return
     */
    @GetMapping(value="/TitleKw", produces = "application/json;charset=UTF-8")
    public QueryResponse<List<Map<String, String>>> queryByTitleKw(@RequestParam(name = "kw") String kw,
                                                              @RequestParam(name = "pageNo", defaultValue = "1") int pageNo,
                                                              @RequestParam(name = "pageSize", defaultValue = "10") int pageSize){
        log.info("queryByTitleKw...");
        try{
            Map<String, List> resultMap=luceneService.queryByTitleKw(kw, pageNo, pageSize);
            List<Document> docs=resultMap.get("results");
            List<Map<String, String>> results=new ArrayList<>();
            for(Document doc:docs){
                Map<String, String> record=new HashMap<>();
                record.put("ID", doc.get("ID"));
                record.put("TITLE", doc.get("TITLE"));
                String timeStampStr=doc.get("POSTDATE");
                record.put("POSTTIMESTAMP", timeStampStr);
                record.put("POSTDATE", sdf.format(new Date(Long.parseLong(timeStampStr))));
//                record.put("TIMESTAMP", doc.get("POSTDATE"));       //TODO 返回的时间戳为 null
                results.add(record);
            }
            List<Integer> totalList=resultMap.get("total");
            return QueryResponse.genSucc("检索成功", totalList.get(0), pageNo, pageSize, results);
        }catch (Exception e){
            e.printStackTrace();
            return QueryResponse.genErr(e.getMessage());
        }
    }

    /**
     *面向前端的接口
     * 检索已经建立索引的文档数
     * @return
     */
    @GetMapping(value = "/TotalCount", produces = "application/json;charset=UTF-8")
    public Map<String, String> queryTotalCount(){
        log.info("queryTotalCount...");
        Map<String, String> result=new HashMap<>();
        try{
            String totalCount=luceneService.queryTotalIndex();
            result.put("success", "true");
            result.put("msg", "检索成功");
            result.put("TotalCount", totalCount);
        }catch (Exception e){
            e.printStackTrace();
            result.put("success", "false");
            result.put("msg", e.getMessage());
            return result;
        }
        return result;
    }

    private final SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm");

    /**
     * 面向前端的接口
     * 检索发表时间范围内的博客
     * @param startTime 开始时间，格式 yyyy-MM-dd HH:mm
     * @param endTime   结束时间，格式同上
     * @param pageNo
     * @param pageSize
     * @return
     */
    @GetMapping(value = "/timeRange", produces = "application/json;charset=UTF-8")
    public QueryResponse<List<Map<String, String>>> queryTimeRange(@RequestParam (name = "startTime") String startTime,
                                                                   @RequestParam (name = "endTime") String endTime,
                                                                   @RequestParam (name = "pageNo", defaultValue = "1") int pageNo,
                                                                   @RequestParam (name = "pageSize", defaultValue = "10") int pageSize) {
        log.info("queryTimeRange...");
       long startTimeStamp, endTimeStamp;
        try {
            startTimeStamp=sdf.parse(startTime).getTime();
            endTimeStamp=sdf.parse(endTime).getTime();
            Map<String, List> resultMap=luceneService.queryTimeRange(startTimeStamp, endTimeStamp, pageNo, pageSize);
            List<Document> docs=resultMap.get("results");
            List<Map<String, String>> results=new ArrayList<>();
            for(Document doc:docs){
                Map<String, String> record=new HashMap<>();
                record.put("TITLE", doc.get("TITLE"));
                record.put("ID", doc.get("ID"));
                String timeStampStr=doc.get("POSTDATE");
                record.put("POSTTIMESTAMP", timeStampStr);
                record.put("POSTDATE", sdf.format(new Date(Long.parseLong(timeStampStr))));
                results.add(record);
            }
            List<Integer> totalList=resultMap.get("total");
            return QueryResponse.genSucc("检索成功", totalList.get(0), pageNo, pageSize, results);
        }catch (Exception e){
            e.printStackTrace();
            return QueryResponse.genErr(e.getMessage());
        }
    }

    @GetMapping(value = "/TitleAndContent", produces = "application/json;charset=UTF-8")
    public QueryResponse<List<Map<String, String>>> queryTitleAndContent(@RequestParam(name = "titleKw") String titleKw,
                                                              @RequestParam(name  ="contentKw") String contentKw,
                                                              @RequestParam(name = "pageNo", defaultValue = "1") int pageNo,
                                                              @RequestParam(name = "pageSize", defaultValue = "10") int pageSize){
        log.info("queryTitleAndContent...");
        List<String> titleKwList=new ArrayList<>();
        char[] titleKws=titleKw.toCharArray();
        for(char c:titleKws){
            titleKwList.add(String.valueOf(c));
        }
        List<String> contentKwList=new ArrayList<>();
        char[] contentKws=contentKw.toCharArray();
        for(char c:contentKws){
            contentKwList.add(String.valueOf(c));
        }
        try{
            Map<String, List> resultMap=luceneService.queryTitleAndContent(titleKwList, contentKwList, pageNo, pageSize);
            List<Document> docs=resultMap.get("results");
            List<Map<String, String>> results=new ArrayList<>();
            for(Document doc:docs){
                Map<String, String> record=new HashMap<>();
                record.put("ID", doc.get("ID"));
                record.put("TITLE", doc.get("TITLE"));
                String timeStampStr=doc.get("POSTDATE");
                record.put("POSTTIMESTAMP", timeStampStr);
                record.put("POSTDATE", sdf.format(new Date(Long.parseLong(timeStampStr))));
//                record.put("TIMESTAMP", doc.get("POSTDATE"));       //TODO 返回的时间戳为 null
                results.add(record);
            }
            List<Integer> totalList=resultMap.get("total");
            return QueryResponse.genSucc("检索成功", totalList.get(0), pageNo, pageSize, results);
        }catch (Exception e){
            e.printStackTrace();
            return QueryResponse.genErr(e.getMessage());
        }
    }

    @GetMapping(value = "/TitleAndContentByQueryParser", produces = "application/json;charset=UTF-8")
    public QueryResponse<List<Map<String, String>>> queryTitleAndContentByQueryParser(@RequestParam(name = "titleKw") String titleKw,
                                                                         @RequestParam(name  ="contentKw") String contentKw,
                                                                         @RequestParam(name = "pageNo", defaultValue = "1") int pageNo,
                                                                         @RequestParam(name = "pageSize", defaultValue = "10") int pageSize){
        log.info("queryTitleAndContentByQueryParser...");
        try{
            Map<String, List> resultMap=luceneService.queryTitleAndContent(titleKw, contentKw, pageNo, pageSize);
            List<Document> docs=resultMap.get("results");
            List<Map<String, String>> results=new ArrayList<>();
            for(Document doc:docs){
                Map<String, String> record=new HashMap<>();
                record.put("ID", doc.get("ID"));
                record.put("TITLE", doc.get("TITLE"));
                String timeStampStr=doc.get("POSTDATE");
                record.put("POSTTIMESTAMP", timeStampStr);
                record.put("POSTDATE", sdf.format(new Date(Long.parseLong(timeStampStr))));
//                record.put("TIMESTAMP", doc.get("POSTDATE"));       //TODO 返回的时间戳为 null
                results.add(record);
            }
            List<Integer> totalList=resultMap.get("total");
            return QueryResponse.genSucc("检索成功", totalList.get(0), pageNo, pageSize, results);
        }catch (Exception e){
            e.printStackTrace();
            return QueryResponse.genErr(e.getMessage());
        }
    }

    @RequestMapping("/test")
    public Blog test(){
        Blog blog=new Blog();
        blog.setAuthor("JiangMingyu");
        return blog;
    }
}
