package com.cjh.backend.mapper;

import com.cjh.backend.dto.AddressListDto;
import com.cjh.backend.entity.Address;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
* @author 45209
* @description 针对表【address(收货地址表)】的数据库操作Mapper
* @createDate 2026-01-29 18:58:49
* @Entity com.cjh.backend.entity.Address
*/

@Mapper
public interface AddressMapper extends BaseMapper<Address> {

    /**
     * 查询用户的所有收货地址（默认地址排在最前）
     */
    @Select("""
        SELECT 
            id,
            receiver_name AS receiverName,
            receiver_phone AS receiverPhone,
            province,
            city,
            district,
            detail_address AS detailAddress,
            is_default AS isDefault,
            create_time AS createTime
        FROM tradelens.address
        WHERE user_id = #{userId}
          AND is_deleted = 0
        ORDER BY is_default DESC, create_time DESC
    """)
    List<AddressListDto> listByUserId(@Param("userId") Long userId);



    // com.cjh.backend.mapper.AddressMapper.java

    /**
     * 新增一条地址记录
     */
    int insertAddress(Address address);


    @Update("UPDATE tradelens.address SET is_default = 0 WHERE user_id = #{userId} AND is_default = 1 AND is_deleted = 0")
    int cancelDefaultByUserId(@Param("userId") Long userId);

    /**
     * 动态更新地址信息（只更新非空字段）
     */
    int updateAddressSelective(Address address);

    /**
     * 逻辑删除地址（设置 is_deleted = 1）
     */
    int deleteByIdAndUserId(
            @Param("id") Long id,
            @Param("userId") Long userId
    );

}




