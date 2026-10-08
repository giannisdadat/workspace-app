package com.john.workspace_saas.workspace;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MemberResponse add(Authentication auth,
                              @PathVariable Long workspaceId,
                              @Valid @RequestBody AddMemberRequest request) {
        return memberService.addMember((Long) auth.getPrincipal(), workspaceId, request);
    }

    @GetMapping
    public List<MemberResponse> list(Authentication auth, @PathVariable Long workspaceId) {
        return memberService.list((Long) auth.getPrincipal(), workspaceId);
    }
}