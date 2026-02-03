package com.cjh.backend.controller;

import com.cjh.backend.common.CurrentUser;
import com.cjh.backend.dto.Address.AddressCreateDto;
import com.cjh.backend.dto.Address.AddressListDto;
import com.cjh.backend.dto.Address.AddressUpdateDto;
import com.cjh.backend.service.AddressService;
import com.cjh.backend.utils.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/address")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    /**
     * 获取我的所有收货地址列表
     */
    @GetMapping("/list")
    public Result<List<AddressListDto>> getMyAddressList(@CurrentUser Long userId) {
        log.info("用户 {} 查询收货地址列表", userId);
        try {
            List<AddressListDto> addresses = addressService.listMyAddresses(userId);
            log.info("用户 {} 收货地址查询成功，共 {} 条", userId, addresses.size());
            return Result.success(addresses);
        } catch (Exception e) {
            log.error("用户 {} 查询收货地址异常", userId, e);
            return Result.fail("获取地址列表失败");
        }
    }


    /**
     * 新增收货地址
     */
    @PostMapping
    public Result<Long> createAddress(
            @RequestBody @Valid AddressCreateDto req,
            @CurrentUser Long userId) {
        log.info("用户 {} 新增收货地址：{} {}", userId, req.getReceiverName(), req.getReceiverPhone());
        try {
            Long addressId = addressService.createAddress(userId, req);
            log.info("用户 {} 新增地址成功，addressId = {}", userId, addressId);
            return Result.success(addressId, "新增成功");
        } catch (IllegalArgumentException e) {
            log.warn("用户 {} 新增地址失败：{}", userId, e.getMessage());
            return Result.fail(400, e.getMessage());
        } catch (Exception e) {
            log.error("用户 {} 新增地址异常", userId, e);
            return Result.fail("新增地址失败");
        }
    }


    /**
     * 修改收货地址
     */
    @PutMapping
    public Result<Void> updateAddress(
            @RequestBody AddressUpdateDto req,
            @CurrentUser Long userId) {
        log.info("用户 {} 修改地址：addressId = {}", userId, req.getId());
        try {
            boolean success = addressService.updateAddress(userId, req);
            if (!success) {
                return Result.fail("修改失败，地址不存在或无权限");
            }
            log.info("用户 {} 修改地址成功：addressId = {}", userId, req.getId());
            return Result.success(null, "修改成功");
        } catch (IllegalArgumentException e) {
            log.warn("用户 {} 修改地址失败：{}", userId, e.getMessage());
            return Result.fail(400, e.getMessage());
        } catch (Exception e) {
            log.error("用户 {} 修改地址异常", userId, e);
            return Result.fail("修改地址失败");
        }
    }

    /**
     * 删除指定收货地址（逻辑删除）
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteAddress(
            @PathVariable("id") Long addressId,
            @CurrentUser Long userId) {

        log.info("用户 {} 尝试删除地址：addressId = {}", userId, addressId);

        try {
            boolean success = addressService.deleteAddress(userId, addressId);
            if (!success) {
                return Result.fail("删除失败，地址不存在或无权限");
            }
            log.info("用户 {} 删除地址成功：addressId = {}", userId, addressId);
            return Result.success(null, "删除成功");
        } catch (Exception e) {
            log.error("用户 {} 删除地址异常", userId, e);
            return Result.fail("删除地址失败");
        }
    }


}
