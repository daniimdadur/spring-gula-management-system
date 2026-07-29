package com.guvaren.gms.master.auth.service;

import com.guvaren.gms.master.auth.dto.req.RolesReq;
import com.guvaren.gms.master.auth.dto.res.UserRes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {
    List<UserRes> get();
    Page<UserRes> get(Pageable pageable);
    String updateNewRoles(RolesReq req, String id);
}
