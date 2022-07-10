package com.oysq.workcalendarms.controller;

import cn.hutool.core.io.IoUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
@RestController
@RequestMapping("/export")
public class ExportController {

    @GetMapping("test")
    public Object test(HttpServletResponse response) throws IOException {


        //用于存储html字符串
        StringBuilder stringHtml = new StringBuilder();
        try{
            //打开文件
            PrintStream printStream = new PrintStream(new FileOutputStream("/Users/oysq/Desktop/test.html"));

            //输入HTML文件内容
            stringHtml.append("<html><head>");
            stringHtml.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">");
            stringHtml.append("<title>测试报告文档</title>");
            stringHtml.append("</head>");
            stringHtml.append("<body>");
            stringHtml.append("<div style='color:red;'>hello</div>");
            stringHtml.append("</body></html>");
            try{
                //将HTML文件内容写入文件中
                printStream.println(stringHtml.toString());
            }catch (Exception e) {

                e.printStackTrace();
            }

        }catch(FileNotFoundException e){
            e.printStackTrace();
        }


        return "success";
    }

    @GetMapping("test2")
    public Object test2(HttpServletResponse response) throws IOException {


        //用于存储html字符串
        StringBuilder stringHtml = new StringBuilder();

        //输入HTML文件内容
        stringHtml.append("<html><head>");
        stringHtml.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">");
        stringHtml.append("<title>测试报告文档</title>");
        stringHtml.append("</head>");
        stringHtml.append("<body>");
        stringHtml.append("<div style='color:red;'>hello</div>");
        stringHtml.append("<table border='1'>");
        stringHtml.append("<tr>");
        stringHtml.append("<td>row 1, cell 1</td>");
        stringHtml.append("<td>row 1, cell 2</td>");
        stringHtml.append("</tr>");
        stringHtml.append("<tr>");
        stringHtml.append("<td>row 2, cell 1</td>");
        stringHtml.append("<td>row 2, cell 2</td>");
        stringHtml.append("</tr>");
        stringHtml.append("</table>");
        stringHtml.append("</body></html>");

        response.setContentType("application/vnd.ms-excel;charset=utf-8");
        response.setHeader("Content-disposition", "attachment; filename=" + URLEncoder.encode("test.html", "utf-8"));

        ServletOutputStream out = response.getOutputStream();
        IoUtil.write(out, true, stringHtml.toString().getBytes(StandardCharsets.UTF_8));
        IoUtil.close(out);

        return "success";
    }

}
