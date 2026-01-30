package com.cjh.backend.mapper;

import com.cjh.backend.dto.Comment.CommentListDto;
import com.cjh.backend.entity.Comment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @author 45209
* @description 针对表【comment(商品评价表)】的数据库操作Mapper
* @createDate 2026-01-29 18:58:49
* @Entity com.cjh.backend.entity.Comment
*/

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    /**
     * 检查该订单是否已评价（防止重复评价）
     */
    @Select("SELECT id FROM tradelens.comment WHERE order_id = #{orderId}")
    Long selectIdByOrderId(@Param("orderId") Long orderId);


    /**
     * 查询商品的评价列表（联表查询用户信息）
     * → 对应 XML 中的 select id="selectCommentsByProductId"
     */
    List<CommentListDto> selectCommentsByProductId(@Param("productId") Long productId);

}




