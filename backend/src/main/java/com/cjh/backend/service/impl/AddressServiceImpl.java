package com.cjh.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjh.backend.dto.AddressCreateDto;
import com.cjh.backend.dto.AddressListDto;
import com.cjh.backend.dto.AddressUpdateDto;
import com.cjh.backend.entity.Address;
import com.cjh.backend.service.AddressService;
import com.cjh.backend.mapper.AddressMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
* @author 45209
* @description 针对表【address(收货地址表)】的数据库操作Service实现
* @createDate 2026-01-29 18:58:49
*/
@Service
@RequiredArgsConstructor
public class AddressServiceImpl extends ServiceImpl<AddressMapper, Address>
    implements AddressService{

    private final AddressMapper addressMapper;


    @Override
    public List<AddressListDto> listMyAddresses(Long userId) {
        return addressMapper.listByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createAddress(Long userId, AddressCreateDto req) {
        // 1. 如果设置为默认地址，先把该用户其他默认地址取消
        if (req.getIsDefault() != null && req.getIsDefault() == 1) {
            // 简单更新所有默认地址为 0
            addressMapper.cancelDefaultByUserId(userId);
        }
        // 2. 构建实体
        Address address = new Address();
        BeanUtils.copyProperties(req, address);
        address.setUserId(userId);
        // 如果前端没传 isDefault，默认 0
        address.setIsDefault(req.getIsDefault() != null ? req.getIsDefault() : 0);
        // 3. 插入
        int rows = addressMapper.insertAddress(address);
        if (rows == 0) {
            throw new IllegalStateException("新增地址失败");
        }
        return address.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateAddress(Long userId, AddressUpdateDto req) {
        // 1. 构建更新实体
        Address address = new Address();
        BeanUtils.copyProperties(req, address);
        address.setUserId(userId);  // 强制设置 userId 用于 WHERE 条件

        // 2. 如果设置为默认地址，先取消其他默认
        if (req.getIsDefault() != null && req.getIsDefault() == 1) {
            addressMapper.cancelDefaultByUserId(userId);
        }

        // 3. 执行动态更新
        int rows = addressMapper.updateAddressSelective(address);
        return rows > 0;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteAddress(Long userId, Long addressId) {
        int rows = addressMapper.deleteByIdAndUserId(addressId, userId);
        return rows > 0;
    }









}




