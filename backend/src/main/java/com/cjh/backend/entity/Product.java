package com.cjh.backend.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.Data;

/**
 * 商品表
 * @TableName product
 */
@TableName(value ="product")
@Data
public class Product {
    /**
     * 商品ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 发布者ID
     */
    private Long userId;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 商品标题
     */
    private String title;

    /**
     * 商品描述
     */
    private String description;

    /**
     * 商品价格
     */
    private BigDecimal price;

    /**
     * 商品数量
     */
    private Integer quantity;

    /**
     * 新旧程度：1全新 2几乎全新 3轻微使用 4明显使用
     */
    private Integer conditionLevel;

    /**
     * 是否可议价：1是 0否
     */
    private Integer negotiable;

    /**
     * 状态：1待审核 2上架 3下架 4已售
     */
    private Integer productStatus;

    /**
     * 逻辑删除：0否 1是
     */

    @TableField(value ="is_delete")
    private Integer isDeleted;

    /**
     * 浏览量
     */
    private Integer viewCount;

    /**
     * 创建时间
     */
    @TableField(value ="create_time",fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value ="update_time",fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        Product other = (Product) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getCategoryId() == null ? other.getCategoryId() == null : this.getCategoryId().equals(other.getCategoryId()))
            && (this.getTitle() == null ? other.getTitle() == null : this.getTitle().equals(other.getTitle()))
            && (this.getDescription() == null ? other.getDescription() == null : this.getDescription().equals(other.getDescription()))
            && (this.getPrice() == null ? other.getPrice() == null : this.getPrice().equals(other.getPrice()))
            && (this.getQuantity() == null ? other.getQuantity() == null : this.getQuantity().equals(other.getQuantity()))
            && (this.getConditionLevel() == null ? other.getConditionLevel() == null : this.getConditionLevel().equals(other.getConditionLevel()))
            && (this.getNegotiable() == null ? other.getNegotiable() == null : this.getNegotiable().equals(other.getNegotiable()))
            && (this.getProductStatus() == null ? other.getProductStatus() == null : this.getProductStatus().equals(other.getProductStatus()))
            && (this.getIsDeleted() == null ? other.getIsDeleted() == null : this.getIsDeleted().equals(other.getIsDeleted()))
            && (this.getViewCount() == null ? other.getViewCount() == null : this.getViewCount().equals(other.getViewCount()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getCategoryId() == null) ? 0 : getCategoryId().hashCode());
        result = prime * result + ((getTitle() == null) ? 0 : getTitle().hashCode());
        result = prime * result + ((getDescription() == null) ? 0 : getDescription().hashCode());
        result = prime * result + ((getPrice() == null) ? 0 : getPrice().hashCode());
        result = prime * result + ((getQuantity() == null) ? 0 : getQuantity().hashCode());
        result = prime * result + ((getConditionLevel() == null) ? 0 : getConditionLevel().hashCode());
        result = prime * result + ((getNegotiable() == null) ? 0 : getNegotiable().hashCode());
        result = prime * result + ((getProductStatus() == null) ? 0 : getProductStatus().hashCode());
        result = prime * result + ((getIsDeleted() == null) ? 0 : getIsDeleted().hashCode());
        result = prime * result + ((getViewCount() == null) ? 0 : getViewCount().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", userId=").append(userId);
        sb.append(", categoryId=").append(categoryId);
        sb.append(", title=").append(title);
        sb.append(", description=").append(description);
        sb.append(", price=").append(price);
        sb.append(", quantity=").append(quantity);
        sb.append(", conditionLevel=").append(conditionLevel);
        sb.append(", negotiable=").append(negotiable);
        sb.append(", productStatus=").append(productStatus);
        sb.append(", isDeleted=").append(isDeleted);
        sb.append(", viewCount=").append(viewCount);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateTime=").append(updateTime);
        sb.append("]");
        return sb.toString();
    }
}