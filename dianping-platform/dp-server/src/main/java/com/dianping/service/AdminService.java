package com.dianping.service;

import com.dianping.dto.AdminLoginDTO;
import com.dianping.vo.AdminLoginVO;

public interface AdminService {

    AdminLoginVO login(AdminLoginDTO adminLoginDTO);
}