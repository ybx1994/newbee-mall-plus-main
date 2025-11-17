package ltd.newbee.mall.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;

// 商品详情（仅保留需要的字段）
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoodsDetail {
    private Long id;
    private String goodsNo;
    private String uuid;
    private String goodsName;
    private String nameEn;
    private String stockNo;
    private Integer classifyId;
    private String brandName;
    private String subitemCode;
    private BigDecimal retailPrice;
    private BigDecimal discountPrice;
    private String specVals;
    private String deliveryTime;
}
