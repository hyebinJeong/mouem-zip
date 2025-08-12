package org.scoula.oauth.service;

import org.scoula.oauth.domain.DTO.KakaoUserDTO;

public interface KakaoOAuthService {
    String getAccessTokenFromKakao(String code);
    KakaoUserDTO requestKakaoUserInfo(String accessToken);
    String loginWithKakao(String kakaoAccessToken);
    void softDeleteUser(String kakaoId);
    void unlinkByAdminKey(Long kakaoId);
}
