
package ltd.newbee.mall.common;

/**
* @author bx* @link https://github.com/newbee-ltd
 * @apiNote 常量配置
 */
public class Constants {

    public final static int INDEX_CAROUSEL_NUMBER = 5;//首页轮播图数量(可根据自身需求修改)

    public final static int INDEX_CATEGORY_NUMBER = 10;//首页一级分类的最大数量

    public final static int SEARCH_CATEGORY_NUMBER = 8;//搜索页一级分类的最大数量

    public final static int INDEX_GOODS_HOT_NUMBER = 4;//首页热卖商品数量
    public final static int INDEX_GOODS_NEW_NUMBER = 5;//首页新品数量
    public final static int INDEX_GOODS_RECOMMOND_NUMBER = 10;//首页推荐商品数量

    public final static int SHOPPING_CART_ITEM_TOTAL_NUMBER = 13;//购物车中商品的最大数量(可根据自身需求修改)

    public final static int SHOPPING_CART_ITEM_LIMIT_NUMBER = 5;//购物车中单个商品的最大购买数量(可根据自身需求修改)

    public final static String MALL_VERIFY_CODE_KEY = "mallVerifyCode";//验证码key

    public final static String MALL_USER_SESSION_KEY = "newBeeMallUser";//session中user的key

    public final static int GOODS_SEARCH_PAGE_LIMIT = 10;//搜索分页的默认条数(每页10条)

    public final static int ORDER_SEARCH_PAGE_LIMIT = 3;//我的订单列表分页的默认条数(每页3条)

    public final static int SELL_STATUS_UP = 0;//商品上架状态
    public final static int SELL_STATUS_DOWN = 1;//商品下架状态

    /**
     * 字符编码
     */
    public static final String UTF_ENCODING = "UTF-8";

    /**
     * 秒杀下单盐值
     */
    public static final String SECKILL_ORDER_SALT = "asd";

    /**
     * 秒杀商品库存缓存
     */
    public static final String SECKILL_GOODS_STOCK_KEY = "seckill_goods_stock:";

    /**
     * 秒杀商品缓存
     */
    public static final String SECKILL_KEY = "seckill:";
    /**
     * 秒杀商品详情页面缓存
     */
    public static final String SECKILL_GOODS_DETAIL = "seckill_goods_detail:";
    /**
     * 秒杀商品列表页面缓存
     */
    public static final String SECKILL_GOODS_LIST = "seckill_goods_list";

    /**
     * 秒杀成功的用户set缓存
     */
    public static final String SECKILL_SUCCESS_USER_ID = "seckill_success_user_id:";

}
