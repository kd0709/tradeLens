package com.cjh.backend.exception;

/**
 * 异常错误码常量定义
 */
public class ErrorConstants {

    // ========== 商品相关异常 ==========
    public static final String PRODUCT_NOT_EXIST = "商品不存在";
    public static final String PRODUCT_NOT_AVAILABLE = "商品不可购买";
    public static final String PRODUCT_PUBLISH_FAILED = "商品发布失败";
    public static final String PRODUCT_NO_PERMISSION = "商品不存在或无权操作";
    public static final String PRODUCT_STATUS_INVALID = "非法的商品状态";
    public static final String PRODUCT_CANNOT_MODIFY = "当前状态不允许修改商品信息";
    public static final String PRODUCT_SOLD = "已售商品不可修改状态";
    public static final String PRODUCT_AUDITING = "待审核商品不可修改状态";
    public static final String PRODUCT_CANNOT_DELETE = "上架商品不可删除";
    public static final String PRODUCT_CANNOT_MARK_SOLD = "当前状态不允许标记为已售";
    public static final String PRODUCT_DOWN_FAILED = "商品下架失败";
    public static final String CATEGORY_NOT_EXIST = "分类不存在";
    public static final String CATEGORY_ID_REQUIRED = "分类ID不能为空";

    // ========== 订单相关异常 ==========
    public static final String ORDER_NOT_EXIST = "订单不存在";
    public static final String ORDER_NOT_OWNED = "无权操作该订单";
    public static final String ORDER_STATUS_INVALID = "订单状态异常";
    public static final String ORDER_PAID_BY_OTHERS = "无权支付该订单";
    public static final String ORDER_UNPAID = "订单未支付";
    public static final String ORDER_CANNOT_CANCEL = "当前状态不能取消";
    public static final String CANNOT_BUY_OWN_PRODUCT = "不能购买自己发布的商品";

    // ========== 用户相关异常 ==========
    public static final String USER_NOT_EXIST = "用户不存在";
    public static final String USERNAME_EXIST = "用户名已存在";
    public static final String USERNAME_PASSWORD_ERROR = "用户名或密码错误";
    public static final String USER_FROZEN = "账号已被封禁";
    public static final String PASSWORD_NOT_MATCH = "两次输入的新密码不一致";
    public static final String PASSWORD_OLD_ERROR = "原密码错误";
    public static final String PASSWORD_SAME_AS_OLD = "新密码不能与原密码相同";

    // ========== 购物车相关异常 ==========
    public static final String CART_ITEM_NOT_EXIST = "购物车项不存在";
    public static final String CART_PRODUCT_NOT_AVAILABLE = "购物车中的商品不可用";

    // ========== 评价相关异常 ==========
    public static final String REVIEW_ALREADY_EXIST = "已评价过该订单";
    public static final String REVIEW_NOT_ALLOWED = "只能评价已完成的订单";
    public static final String REVIEW_NOT_EXIST = "评价不存在";

    // ========== 收藏相关异常 ==========
    public static final String FAVORITE_ALREADY_EXIST = "已收藏该商品";
    public static final String FAVORITE_NOT_EXIST = "收藏不存在";

    // ========== 权限相关异常 ==========
    public static final String NO_PERMISSION = "您没有权限执行此操作";
    public static final String ADMIN_PERMISSION_REQUIRED = "需要管理员权限";

    // ========== 系统相关异常 ==========
    public static final String SYSTEM_ERROR = "系统异常，请联系管理员";
}
