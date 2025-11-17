
package ltd.newbee.mall.service.impl;

import lombok.extern.slf4j.Slf4j;
import ltd.newbee.mall.common.NewBeeMallCategoryLevelEnum;
import ltd.newbee.mall.common.ServiceResultEnum;
import ltd.newbee.mall.controller.vo.NewBeeMallSearchGoodsVO;
import ltd.newbee.mall.dao.GoodsCategoryMapper;
import ltd.newbee.mall.dao.NewBeeMallGoodsMapper;
import ltd.newbee.mall.entity.GoodsCategory;
import ltd.newbee.mall.entity.NewBeeMallGoods;
import ltd.newbee.mall.entity.NewBeeMallGoodsImport;
import ltd.newbee.mall.service.NewBeeMallGoodsService;
import ltd.newbee.mall.util.BeanUtil;
import ltd.newbee.mall.util.PageQueryUtil;
import ltd.newbee.mall.util.PageResult;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class NewBeeMallGoodsServiceImpl  implements NewBeeMallGoodsService {

    @Autowired
    private NewBeeMallGoodsMapper goodsMapper;
    @Autowired
    private GoodsCategoryMapper goodsCategoryMapper;

    @Override
    public PageResult getNewBeeMallGoodsPage(PageQueryUtil pageUtil) {
        List<NewBeeMallGoods> goodsList = goodsMapper.findNewBeeMallGoodsList(pageUtil);
        int total = goodsMapper.getTotalNewBeeMallGoods(pageUtil);
        PageResult pageResult = new PageResult(goodsList, total, pageUtil.getLimit(), pageUtil.getPage());
        return pageResult;
    }

    @Override
    public String saveNewBeeMallGoods(NewBeeMallGoods goods) {
        GoodsCategory goodsCategory = goodsCategoryMapper.selectByPrimaryKey(goods.getGoodsCategoryId());
        // 分类不存在或者不是er级分类，则该参数字段异常
        if (goodsCategory == null || goodsCategory.getCategoryLevel().intValue() != NewBeeMallCategoryLevelEnum.LEVEL_TWO.getLevel()) {
            return ServiceResultEnum.GOODS_CATEGORY_ERROR.getResult();
        }
        if (goodsMapper.selectByCategoryIdAndName(goods.getGoodsId(), goods.getGoodsCategoryId()) != null) {
            return ServiceResultEnum.SAME_GOODS_EXIST.getResult();
        }
        if (goodsMapper.insertSelective(goods) > 0) {
            return ServiceResultEnum.SUCCESS.getResult();
        }
        return ServiceResultEnum.DB_ERROR.getResult();
    }

    @Override
    public void batchSaveNewBeeMallGoods(List<NewBeeMallGoods> newBeeMallGoodsList) {
        if (!CollectionUtils.isEmpty(newBeeMallGoodsList)) {
            goodsMapper.batchInsert(newBeeMallGoodsList);
        }
    }

    @Override
    public String updateNewBeeMallGoods(NewBeeMallGoods goods) {
        GoodsCategory goodsCategory = goodsCategoryMapper.selectByPrimaryKey(goods.getGoodsCategoryId());
        // 分类不存在或者不是三级分类，则该参数字段异常
        if (goodsCategory == null || goodsCategory.getCategoryLevel().intValue() != NewBeeMallCategoryLevelEnum.LEVEL_TWO.getLevel()) {
            return ServiceResultEnum.GOODS_CATEGORY_ERROR.getResult();
        }
        NewBeeMallGoods temp = goodsMapper.selectByPrimaryKey(goods.getGoodsId());
        if (temp == null) {
            return ServiceResultEnum.DATA_NOT_EXIST.getResult();
        }
        NewBeeMallGoods temp2 = goodsMapper.selectByCategoryIdAndName(goods.getGoodsId(), goods.getGoodsCategoryId());
        if (temp2 != null && !temp2.getGoodsId().equals(goods.getGoodsId())) {
            //name和分类id相同且不同id 不能继续修改
            return ServiceResultEnum.SAME_GOODS_EXIST.getResult();
        }
        goods.setUpdateTime(new Date());
        if (goodsMapper.updateByPrimaryKeySelective(goods) > 0) {
            return ServiceResultEnum.SUCCESS.getResult();
        }
        return ServiceResultEnum.DB_ERROR.getResult();
    }

    @Override
    public NewBeeMallGoods getNewBeeMallGoodsById(Long id) {
        return goodsMapper.selectByPrimaryKey(id);
    }
    
    @Override
    public Boolean batchUpdateSellStatus(Long[] ids, int sellStatus) {
        return goodsMapper.batchUpdateSellStatus(ids, sellStatus) > 0;
    }

    @Override
    public PageResult searchNewBeeMallGoods(PageQueryUtil pageUtil) {
        List<NewBeeMallGoods> goodsList = goodsMapper.findNewBeeMallGoodsListBySearch(pageUtil);
        int total = goodsMapper.getTotalNewBeeMallGoodsBySearch(pageUtil);
        List<NewBeeMallSearchGoodsVO> newBeeMallSearchGoodsVOS = new ArrayList<>();
        if (!CollectionUtils.isEmpty(goodsList)) {
            newBeeMallSearchGoodsVOS = BeanUtil.copyList(goodsList, NewBeeMallSearchGoodsVO.class);
            for (NewBeeMallSearchGoodsVO newBeeMallSearchGoodsVO : newBeeMallSearchGoodsVOS) {
                String goodsName = newBeeMallSearchGoodsVO.getGoodsName();
                String goodsIntro = newBeeMallSearchGoodsVO.getGoodsIntro();
                // 字符串过长导致文字超出的问题
                if (goodsName.length() > 28) {
                    goodsName = goodsName.substring(0, 28) + "...";
                    newBeeMallSearchGoodsVO.setGoodsName(goodsName);
                }
                if (goodsIntro.length() > 30) {
                    goodsIntro = goodsIntro.substring(0, 30) + "...";
                    newBeeMallSearchGoodsVO.setGoodsIntro(goodsIntro);
                }
            }
        }
        PageResult pageResult = new PageResult(newBeeMallSearchGoodsVOS, total, pageUtil.getLimit(), pageUtil.getPage());
        return pageResult;
    }

    @Override
    public void insertExportData(ArrayList<NewBeeMallGoodsImport> newBeeMallGoodsArrayList) {
            goodsMapper.insertExportData(newBeeMallGoodsArrayList);
    }

    @Override
    public String ImportData(Workbook workbook) {
        if (workbook == null) {
            return "Excel文件為空";
        }
        
        ArrayList<NewBeeMallGoodsImport> newBeeMallGoodsArrayList = new ArrayList<>();
        Sheet sheet = workbook.getSheetAt(0);
        int rowCount = sheet.getPhysicalNumberOfRows();
        
        if (rowCount <= 1) {
            return "Excel文件中沒有數據行";
        }
        
        log.info("獲取到總行數：{}，開始處理數據", rowCount);
        
        int successCount = 0;
        int errorCount = 0;
        
        for (int r = 1; r < rowCount; ++r) {
            Row row = sheet.getRow(r);
            if (row == null) {
                continue;
            }
            
            try {
                NewBeeMallGoodsImport newBeeMallGoods = new NewBeeMallGoodsImport();
                boolean hasData = false;
                
                for (int c = 0; c <= 16; ++c) {
                    Cell cell = row.getCell(c);
                    String cellStringValue = getCellValueAsString(cell);
                    
                    if (StringUtils.isNotEmpty(cellStringValue)) {
                        hasData = true;
                        setGoodsField(newBeeMallGoods, c, cellStringValue);
                    }
                }
                
                // 只有當行中有數據時才添加到列表中
                if (hasData) {
                    // 設置默認值
                    if (StringUtils.isEmpty(newBeeMallGoods.getGoodsCoverImg())) {
                        newBeeMallGoods.setGoodsCoverImg("http://4z9lip5uwnza.xiaomiqiu.com/upload/20231024_1903229.jpg");
                    }
                    if (StringUtils.isEmpty(newBeeMallGoods.getGoodsCarousel())) {
                        newBeeMallGoods.setGoodsCarousel("/upload/20231024_1903229.jpg");
                    }
                    if (StringUtils.isEmpty(newBeeMallGoods.getGoodsSellStatus())) {
                        newBeeMallGoods.setGoodsSellStatus("1"); // 默認上架
                    }
                    
                    newBeeMallGoodsArrayList.add(newBeeMallGoods);
                    successCount++;
                }
                
            } catch (Exception e) {
                log.error("處理第{}行數據時發生錯誤：{}", r + 1, e.getMessage());
                errorCount++;
            }
        }
        
        if (newBeeMallGoodsArrayList.isEmpty()) {
            return "沒有有效的商品數據";
        }
        
        try {
            log.info("開始批量插入數據，共{}條記錄", newBeeMallGoodsArrayList.size());
            this.goodsMapper.insertExportData(newBeeMallGoodsArrayList);
            log.info("數據插入成功，成功處理{}條記錄，失敗{}條記錄", successCount, errorCount);
            return "SUCCESS";
        } catch (Exception e) {
            log.error("批量插入數據失敗", e);
            return "數據庫插入失敗：" + e.getMessage();
        }
    }
    
    /**
     * 獲取單元格的值作為字符串
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return null;
        }
        
        String cellStringValue = null;
        int cellType = cell.getCellType();
        
        switch (cellType) {
            case Cell.CELL_TYPE_NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    // 處理日期格式
                    cellStringValue = cell.getDateCellValue().toString();
                } else {
                    // 處理數字，避免科學計數法
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == (long) numericValue) {
                        cellStringValue = String.valueOf((long) numericValue);
                    } else {
                        cellStringValue = String.valueOf(numericValue);
                    }
                }
                break;
            case Cell.CELL_TYPE_STRING:
                cellStringValue = cell.getStringCellValue();
                break;
            case Cell.CELL_TYPE_BOOLEAN:
                cellStringValue = String.valueOf(cell.getBooleanCellValue());
                break;
            case Cell.CELL_TYPE_FORMULA:
                try {
                    cellStringValue = cell.getStringCellValue();
                } catch (Exception e) {
                    try {
                        cellStringValue = String.valueOf(cell.getNumericCellValue());
                    } catch (Exception ex) {
                        cellStringValue = "公式錯誤";
                    }
                }
                break;
            default:
                cellStringValue = "";
        }
        
        return cellStringValue != null ? cellStringValue.trim() : null;
    }
    
    /**
     * 設置商品字段值
     */
    private void setGoodsField(NewBeeMallGoodsImport goods, int columnIndex, String value) {
        switch (columnIndex) {
            case 0:
                goods.setGoodsNum(value);
                break;
            case 1:
                goods.setGoodsName(value);
                break;
            case 2:
                goods.setPackagingUnit(value);
                break;
            case 3:
                goods.setSpecifications(value);
                break;
            case 4:
                goods.setSupplyCycle(value);
                break;
            case 5:
                goods.setOriginalPrice(value);
                break;
            case 6:
                goods.setSellingPrice(value);
                break;
            case 7:
                goods.setBrandId(value);
                break;
            case 8:
                goods.setGoodsCategoryId(value);
                break;
            case 9:
                goods.setGoodsIntro(value);
                break;
            case 10:
                goods.setStorageCons(value);
                break;
            case 11:
                goods.setGoodsSellStatus(value);
                break;
            case 12:
                goods.setGoodsDetailContent(value);
                break;
            default:
                // 忽略其他列
                break;
        }
    }
}
