package com.cjh.backend.service;

import org.springframework.web.multipart.MultipartFile;

public interface CommonService {

    String uploadFile(MultipartFile file, Long userId);
}