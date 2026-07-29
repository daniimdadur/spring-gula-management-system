package com.guvaren.gms.master.auth.service;

import com.guvaren.gms.master.auth.dto.req.AuthenticationReq;
import com.guvaren.gms.master.auth.dto.req.RegistrationReq;
import com.guvaren.gms.master.auth.dto.res.AuthenticationResult;
import com.guvaren.gms.master.auth.dto.res.TokenRes;

public interface AuthService {
    AuthenticationResult register(RegistrationReq req);
    AuthenticationResult login(AuthenticationReq req);
    AuthenticationResult loginAndLogoutForAllDevices(AuthenticationReq req);
    TokenRes getNewAccessToken(String refreshToken);
    String logoutAllDevices(String refreshToken);
    String logoutThisDevice(String refreshToken);
}
