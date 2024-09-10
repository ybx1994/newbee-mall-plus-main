
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
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
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
    public void ImportData(Workbook workbook) {
        ArrayList<NewBeeMallGoodsImport> newBeeMallGoodsArrayList = new ArrayList<>();
        // 2.读取页脚sheet
        Sheet sheet = workbook.getSheetAt(0);
        int rowCount = sheet.getPhysicalNumberOfRows();//获取总行数
        log.info("获取到总行数～～～～～～开始拼装数据～～～：{}",rowCount);
        // 3.循环读取某一行
        for (int r = 1; r < rowCount; r++) {
            Row row = sheet.getRow(r);
            if (null == row) {
                continue;
            }
            NewBeeMallGoodsImport newBeeMallGoods = new NewBeeMallGoodsImport();
            for (int c = 1; c <= 16; c++) {
                Cell cell = row.getCell(c);
                String cellStringValue = null;
                if (null != cell) {
                    int cellType = cell.getCellType();
                    switch (cellType) {
                        case Cell.CELL_TYPE_STRING: // 文本
                            cellStringValue = cell.getStringCellValue();
                            break;
                        case Cell.CELL_TYPE_NUMERIC: // 数字、日期
                            if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                                // 日期型
//                                    cellStringValue = fmt.format(cell.getDateCellValue());
                            } else {
                                // 数字
                                cellStringValue = String.valueOf(cell.getNumericCellValue());
                            }
                            break;
                        case Cell.CELL_TYPE_BOOLEAN: // 布尔型
                            cellStringValue = String.valueOf(cell.getBooleanCellValue());
                            break;
                        case Cell.CELL_TYPE_BLANK: // 空白
                            cellStringValue = cell.getStringCellValue();
                            break;
                        case Cell.CELL_TYPE_ERROR: // 错误
                            cellStringValue = "错误";
                            break;
                        case Cell.CELL_TYPE_FORMULA: // 公式
                            cellStringValue = "错误";
                            break;
                        default:
                            cellStringValue = "错误";
                    }
                    cellStringValue = cellStringValue.trim();
                }
                switch (c) {
                    //货号
                    case 1:
                        if (StringUtils.isNotEmpty(cellStringValue)) {
                            newBeeMallGoods.setGoodsNum(cellStringValue);
                        }
                        break;
                    //名称
                    case 2:
                        if (StringUtils.isNotEmpty(cellStringValue)) {
                            newBeeMallGoods.setGoodsName(cellStringValue);
                        }
                        break;
                    //包装单位
                    case 3:
                        if (StringUtils.isNotEmpty(cellStringValue)) {
                            newBeeMallGoods.setPackagingUnit(cellStringValue);
                        }
                        break;
                    // 规格
                    case 4:
                        if (StringUtils.isNotEmpty(cellStringValue)) {
                            newBeeMallGoods.setSpecifications(cellStringValue);
                        }
                        break;
                        // 供货周期
                    case 5:
                        if (StringUtils.isNotEmpty(cellStringValue)) {
                            newBeeMallGoods.setSupplyCycle(cellStringValue);
                        }
                        break;
                        //目录价
                    case 6:
                        if (StringUtils.isNotEmpty(cellStringValue)) {
                            newBeeMallGoods.setOriginalPrice(cellStringValue);
                        }
                        break;
                        //实际价
                    case 7:
                        if (StringUtils.isNotEmpty(cellStringValue)) {
                            newBeeMallGoods.setSellingPrice(cellStringValue);
                        }
                        break;
                        //品牌id
                    case 8:
                        if (StringUtils.isNotEmpty(cellStringValue)) {
                            newBeeMallGoods.setBrandId(cellStringValue);
                        }
                        break;
                        //分类id
                    case 9:
                        if (StringUtils.isNotEmpty(cellStringValue)) {
                            newBeeMallGoods.setGoodsCategoryId(cellStringValue);
                        }
                        break;
                        //简介
                    case 10:
                        if (StringUtils.isNotEmpty(cellStringValue)) {
                            newBeeMallGoods.setGoodsIntro(cellStringValue);
                        }
                        break;
                        //存储条件
                    case 11:
                        if (StringUtils.isNotEmpty(cellStringValue)) {
                            newBeeMallGoods.setStorageCons(cellStringValue);
                        }
                        //状态
                    case 12:
                        if (StringUtils.isNotEmpty(cellStringValue)) {
                            newBeeMallGoods.setGoodsSellStatus(cellStringValue);
                        }
                        break;
                        //详情
                    case 13:
                        if (StringUtils.isNotEmpty(cellStringValue)) {
                            newBeeMallGoods.setGoodsDetailContent(cellStringValue);
                        }
                        break;
                    default:
                        log.error("oooooo~~~/goods/export~~~导入失败啦～～～～报错啦");
                }
            }
            newBeeMallGoodsArrayList.add(newBeeMallGoods);
        }
        log.info("拼装数据完成～～～～～执行入库start");
        goodsMapper.insertExportData(newBeeMallGoodsArrayList);
        log.info("拼装数据完成～～～～～执行入库end");
    }
}
