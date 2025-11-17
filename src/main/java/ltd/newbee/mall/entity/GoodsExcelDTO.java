
package ltd.newbee.mall.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class GoodsExcelDTO {
    @ExcelProperty("品牌名称")
    private String brandName;

    @ExcelProperty("库存编号")
    private String stockNo;

    @ExcelProperty("商品名称")
    private String goodsName;

    @ExcelProperty("规格")
    private String specVals;

    @ExcelProperty("发货时间")
    private String deliveryTime;

    @ExcelProperty("零售价")
    private BigDecimal retailPrice;

    @ExcelProperty("折扣价")
    private BigDecimal discountPrice;
}