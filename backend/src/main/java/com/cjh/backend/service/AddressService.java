package com.cjh.backend.service;

import com.cjh.backend.dto.Address.AddressCreateDto;
import com.cjh.backend.dto.Address.AddressListDto;
import com.cjh.backend.dto.Address.AddressUpdateDto;
import com.cjh.backend.entity.Address;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author 45209
* @description 针对表【address(收货地址表)】的数据库操作Service
* @createDate 2026-01-29 18:58:49
*/
public interface AddressService extends IService<Address> {

    List<AddressListDto> listMyAddresses(Long userId);

    Long createAddress(Long userId, AddressCreateDto req);

    boolean updateAddress(Long userId, AddressUpdateDto req);

    boolean deleteAddress(Long userId, Long addressId);





}
