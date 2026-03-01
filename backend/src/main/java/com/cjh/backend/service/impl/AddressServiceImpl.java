package com.cjh.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjh.backend.dto.Address.AddressCreateDto;
import com.cjh.backend.dto.Address.AddressListDto;
import com.cjh.backend.dto.Address.AddressUpdateDto;
import com.cjh.backend.entity.Address;
import com.cjh.backend.service.AddressService;
import com.cjh.backend.mapper.AddressMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
        if (req.getIsDefault() != null && req.getIsDefault() == 1) {
            addressMapper.cancelDefaultByUserId(userId);
        }
        Address address = new Address();
        BeanUtils.copyProperties(req, address);
        address.setUserId(userId);
        address.setIsDefault(req.getIsDefault() != null ? req.getIsDefault() : 0);
        int rows = addressMapper.insertAddress(address);
        if (rows == 0) {
            throw new IllegalStateException("新增地址失败");
        }
        return address.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateAddress(Long userId, AddressUpdateDto req) {
        Address address = new Address();
        BeanUtils.copyProperties(req, address);
        address.setUserId(userId);

        if (req.getIsDefault() != null && req.getIsDefault() == 1) {
            addressMapper.cancelDefaultByUserId(userId);
        }

        int rows = addressMapper.updateAddressSelective(address);
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteAddress(Long userId, Long addressId) {
        return addressMapper.deleteByIdAndUserId(addressId, userId) > 0;
    }
}