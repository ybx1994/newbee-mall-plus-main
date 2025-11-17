package ltd.newbee.mall.common;

import lombok.Data;

import java.util.List;

// 数据层
@Data
public class GoodsData {
    private Integer currentPage;
    private Integer pageSize;
    private Long total;
    private List<GoodsDetail> list;
}
