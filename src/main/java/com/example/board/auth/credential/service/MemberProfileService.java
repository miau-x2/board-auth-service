package com.example.board.auth.credential.service;

import com.example.board.auth.client.member.MemberProfileCreateRequest;
import com.example.board.auth.credential.service.result.CreateProfileResult;
import com.example.board.auth.credential.service.result.DeleteProfileResult;

public interface MemberProfileService {
    CreateProfileResult createProfile(Long id, MemberProfileCreateRequest request);
    DeleteProfileResult deleteProfile(Long id);
}
