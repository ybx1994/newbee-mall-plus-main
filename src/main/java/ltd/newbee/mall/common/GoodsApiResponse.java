package ltd.newbee.mall.common;

import lombok.Data;
import java.util.List;

// 外层响应
@Data
public class GoodsApiResponse {
    private String code;
    private String msg;
    private GoodsDataWrapper data;
}

