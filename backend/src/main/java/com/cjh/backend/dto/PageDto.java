package com.cjh.backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class PageDto<T> {
    private List<T> list;
    private Long total;
    private Integer page;
    private Integer size;
}