
package ltd.newbee.mall.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Data
public class NewBeeMallGoods {
    private Long goodsId;

    private String goodsName;
    //商品简介
    private String goodsIntro;

    private Long goodsCategoryId;
    //商品主图
    private String goodsCoverImg;
    //商品轮播图
    private String goodsCarousel;
    //商品价格
    private Integer originalPrice;
    //商品实际售价
    private Integer sellingPrice;
    //商品库存数量
    private Integer stockNum;

    //供货周期
    private Integer supplyCycle;

    //货号
    private String goodsNum;
// 存储条件
    private String storageCons;
    //包装单位
    private String packagingUnit;
    //包装单位名称
    private String packagingUnitName;
    //规格
    private String specifications;
    //品牌
    private Integer brandId;
    //    品牌名称
    private Integer brandName;
    //商品标签
    private String tag;
    //商品上架状态 0-下架 1-上架
    private Byte goodsSellStatus;

    private Integer createUser;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    private Integer updateUser;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
    //商品详情
    private String goodsDetailContent;

}