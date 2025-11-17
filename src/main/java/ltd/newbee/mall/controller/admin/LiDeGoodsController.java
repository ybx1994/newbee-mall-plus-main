package ltd.newbee.mall.controller.admin;

import com.alibaba.excel.EasyExcel;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import ltd.newbee.mall.common.*;
import ltd.newbee.mall.entity.GoodsExcelDTO;
import okhttp3.*;
import okhttp3.RequestBody;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author bx* @link https://github.com/newbee-ltd
 */
@Controller
@RequestMapping("/lide")
@Slf4j
public class LiDeGoodsController {



    // 接口地址
    private static final String API_URL = "https://www.lddfshop.com/w/goods/list";
    // ObjectMapper用于JSON解析
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    // OkHttp客户端
    private static final OkHttpClient OK_HTTP_CLIENT = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();


    @GetMapping("/goods")
    public String goodsPage(HttpServletRequest request) {
        request.setAttribute("path", "lide_goods");
        return "admin/lide_goods";
    }


    /**
     * 导出数据 exportGoods理德东方现货数据
     */
    @GetMapping("/goods/liDeExportGoods")
    @ResponseBody
    public void liDeExportGoods(HttpServletResponse response) {
        try {
            // 1. 分页获取所有商品数据
            String deliveryTime = "现货";
            List<GoodsExcelDTO> allGoods = getAllGoodsData(deliveryTime);
            
            // 2. 设置响应头，实现文件下载
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            // 这里URLEncoder.encode可以防止中文乱码
            String fileName = URLEncoder.encode("理德东方商品数据.xlsx", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName);
            
            // 3. 直接写入响应输出流
            EasyExcel.write(response.getOutputStream(), GoodsExcelDTO.class)
                    .sheet("商品列表")
                    .doWrite(allGoods);
            
            System.out.println("Excel导出完成！共导出：" + allGoods.size() + "条数据");
        } catch (Exception e) {
            log.error("数据导出失败！", e);
            throw new RuntimeException("数据导出失败: " + e.getMessage());
        }
    }

    /**
     * 导出数据 exportGoods理德东方所有数据
     */
    @GetMapping("/goods/liDeExportGoodsAll")
    @ResponseBody
    public void liDeExportGoodsAll(HttpServletResponse response) {
        try {
            // 1. 分页获取所有商品数据
            String deliveryTime = "";
            List<GoodsExcelDTO> allGoods = getAllGoodsData(deliveryTime);

            // 2. 设置响应头，实现文件下载
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            // 这里URLEncoder.encode可以防止中文乱码
            String fileName = URLEncoder.encode("理德东方商品数据.xlsx", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName);

            // 3. 直接写入响应输出流
            EasyExcel.write(response.getOutputStream(), GoodsExcelDTO.class)
                    .sheet("商品列表")
                    .doWrite(allGoods);

            System.out.println("Excel导出完成！共导出：" + allGoods.size() + "条数据");
        } catch (Exception e) {
            log.error("数据导出失败！", e);
            throw new RuntimeException("数据导出失败: " + e.getMessage());
        }
    }

    /**
     * 分页获取所有商品数据
     */
    private static List<GoodsExcelDTO> getAllGoodsData(String deliveryTime) throws Exception {
        // 首先检查网络连接
        try {
            InetAddress.getByName("www.lddfshop.com");
        } catch (UnknownHostException e) {
            throw new RuntimeException("无法连接到 www.lddfshop.com，请检查网络连接或DNS配置");
        }
        
        List<GoodsExcelDTO> allGoods = new ArrayList<>();
        int currentPage = 0;
        int pageSize = 50;
        int total = Integer.MAX_VALUE; // 初始值设为最大值确保首次进入循环

        while (allGoods.size() < total) {
            // 构建请求参数
            String requestBody = buildRequestBody(currentPage, pageSize,deliveryTime);

            // 发送POST请求
            Request request = new Request.Builder()
                    .url(API_URL)
                    .addHeader("accept", "application/json, text/javascript, */*; q=0.01")
                    .addHeader("content-type", "application/json")
                    .addHeader("cookie", "search-history=[{\"id\":0,\"search\":\"\"}]; SESSION=NTU2YmYwYmEtYTE0Ni00MTAyLTgwMDctOWE1MWZlNzdkMDU2")
                    .addHeader("origin", "https://www.lddfshop.com")
                    .addHeader("referer", "https://www.lddfshop.com/w/goods/search?q=")
                    .addHeader("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36")
                    .addHeader("x-requested-with", "XMLHttpRequest")
                    .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), requestBody))
                    .build();

            // 执行请求并解析响应
            try (Response response = OK_HTTP_CLIENT.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new RuntimeException("第" + currentPage + "页数据请求失败，响应码：" + response.code());
                }

                // 确保 response.body() 不为 null
                if (response.body() == null) {
                    throw new RuntimeException("第" + currentPage + "页响应体为空");
                }

                // 解析JSON响应
                String responseBody = response.body().string();
                GoodsApiResponse apiResponse = OBJECT_MAPPER.readValue(responseBody, GoodsApiResponse.class);

                if (!"0".equals(apiResponse.getCode()) || apiResponse.getData() == null) {
                    throw new RuntimeException("第" + currentPage + "页数据解析失败，响应信息：" + apiResponse.getMsg());
                }

                // 更新总条数和当前页数据
                total = Math.toIntExact(apiResponse.getData().getTotal());
                List<GoodsDetail> currentPageGoods = apiResponse.getData().getList();

                // 转换为ExcelDTO并添加到总列表
                if (currentPageGoods != null) {
                    for (GoodsDetail detail : currentPageGoods) {
                        GoodsExcelDTO dto = new GoodsExcelDTO();
                        dto.setBrandName(detail.getBrandName());
                        dto.setStockNo(detail.getStockNo());
                        dto.setGoodsName(detail.getGoodsName());
                        dto.setSpecVals(detail.getSpecVals());
                        dto.setDeliveryTime(detail.getDeliveryTime());
                        dto.setRetailPrice(detail.getRetailPrice());
                        dto.setDiscountPrice(detail.getDiscountPrice());
                        allGoods.add(dto);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("获取第" + currentPage + "页数据时发生错误: " + e.getMessage());
            }

            System.out.println("已获取第" + currentPage + "页数据，累计获取：" + allGoods.size() + "条");
            currentPage++;

            // 添加延迟避免请求过于频繁
            Thread.sleep(1000);

            // 防止无限循环的安全检查
            if (currentPage > (total / pageSize) + 2) {
                break;
            }
        }

        return allGoods;
    }


    /**
     * 构建请求体（支持分页参数）
     */
    private static String buildRequestBody(int currentPage, int pageSize, String deliveryTime) throws Exception {
        // 构造请求参数对象
        RequestParam param = new RequestParam();
        FormData formData = new FormData();
        formData.setDeliveryTime(deliveryTime); // 固定筛选现货
        param.setFormData(formData);

        PageData pageData = new PageData();
        pageData.setPageSize(pageSize);
        pageData.setCurrentPage(currentPage);
        param.setPageData(pageData);

        Opt opt = new Opt();
        opt.setPager(".paging");
        param.setOpt(opt);

        // 转换为JSON字符串
        return OBJECT_MAPPER.writeValueAsString(param);
    }

    @Data
    static class RequestParam {
        private FormData formData;
        private PageData pageData;
        private Opt opt;
    }

    @Data
    static class FormData {
        private String goodsName = "";
        private String stockCode = "";
        private String specVals = "";
        private String cas = "";
        private String brandName = "";
        private String shopName = "";
        private String priceGt = "";
        private String priceLt = "";
        private String classifyId = "";
        private String q = "";
        private String deliveryTime;
        private List<String> goodsAttrs = new ArrayList<>();
    }

    @Data
    static class PageData {
        private String sortName = "";
        private String sortOrder = "";
        private int pageSize;
        private int currentPage;
    }

    @Data
    static class Opt {
        private String pager;
    }
}