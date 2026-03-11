package com.cjh.backend.utils;

import com.alibaba.excel.EasyExcel;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
public class ExcelUtils {

    /**
     * 通用导出 Excel 方法
     *
     * @param response  HttpServletResponse
     * @param fileName  导出的文件名 (不含后缀)
     * @param sheetName sheet名称
     * @param data      导出的数据列表
     * @param clazz     映射的 DTO 类
     */
    public static <T> void export(HttpServletResponse response, String fileName, String sheetName, List<T> data, Class<T> clazz) {
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + encodedFileName + ".xlsx");

            EasyExcel.write(response.getOutputStream(), clazz)
                    .sheet(sheetName)
                    .doWrite(data);
        } catch (Exception e) {
            log.error("导出Excel失败, fileName: {}", fileName, e);
            response.reset();
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            try {
                response.getWriter().println("{\"code\":500, \"message\":\"导出失败\"}");
            } catch (Exception ex) {
                log.error("返回错误信息失败", ex);
            }
        }
    }
}