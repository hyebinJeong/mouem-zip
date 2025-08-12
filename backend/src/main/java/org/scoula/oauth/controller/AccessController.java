package org.scoula.oauth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/check-access")
public class AccessController {


    // 회원 전용
    @GetMapping({
            "/safety-check",
            "/safety-check/{id}",
            "/agreement",
            "/my",
            "/contract-list",
            "/report-list",
            "/final-report",
            "/referencecontracts",
            "/referencecontracts/**"
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> checkUserAccess() {
        return ResponseEntity.ok("AUTHORIZED");
    }

    // 관리자 전용
    @GetMapping({
            "/category",
            "/category/**"  // add, edit/:id, term/**, special/** 모두 커버
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> checkAdminAccess() {
        return ResponseEntity.ok("AUTHORIZED");
    }
}
