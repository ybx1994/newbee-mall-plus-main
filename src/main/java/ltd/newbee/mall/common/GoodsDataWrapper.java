package ltd.newbee.mall.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

// 新增中间层封装类
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoodsDataWrapper {
    private Integer currentPage;
    private Integer pageSize;
    private Long total;
    private List<GoodsDetail> list;
    private Object data;  // 内部可能存在的额外 data 字段
}