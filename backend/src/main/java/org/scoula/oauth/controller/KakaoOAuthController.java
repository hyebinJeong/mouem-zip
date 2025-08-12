package org.scoula.oauth.controller;

import lombok.RequiredArgsConstructor;
import org.scoula.oauth.domain.DTO.KakaoUserDTO;
import org.scoula.oauth.service.KakaoOAuthService;
import org.scoula.security.util.JwtProcessor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/oauth/kakao")
@RequiredArgsConstructor
public class KakaoOAuthController {

    private final KakaoOAuthService kakaoOAuthService;
    private final JwtProcessor jwtProcessor;

    // 카카오 로그인 요청 -> access_token -> jwt 변환
    @PostMapping("/login")
    public ResponseEntity<?> kakaoLogin(@RequestBody Map<String, String> body) {
        String code = body.get("access_token"); // 실제로는 인가코드임
        //  인가코드로 access_token 발급
        String accessToken = kakaoOAuthService.getAccessTokenFromKakao(code);
        // access_token을 넘겨서 전체 로그인 처리 (userInfo + DB 저장 + JWT 발급)
        String jwt = kakaoOAuthService.loginWithKakao(accessToken);
        return ResponseEntity.ok(Map.of(
                "jwt", jwt,
                "kakaoAccessToken", accessToken
        ));
    }

    @PostMapping("/unlink")
    public ResponseEntity<Void> unlink(@RequestHeader("Authorization") String authorizationHeader) {
        String jwt = authorizationHeader.replace("Bearer ", "");
        String kakaoId = jwtProcessor.getKakaoId(jwt);


        kakaoOAuthService.unlinkByAdminKey(Long.parseLong(kakaoId));
        kakaoOAuthService.softDeleteUser(kakaoId); // state=false 등

        return ResponseEntity.ok().build();
    }
}