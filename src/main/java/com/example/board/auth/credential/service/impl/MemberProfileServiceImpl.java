package com.example.board.auth.credential.service.impl;

import com.example.board.auth.client.member.CreateProfileClientResult;
import com.example.board.auth.client.member.DeleteProfileClientResult;
import com.example.board.auth.client.member.MemberProfileCreateRequest;
import com.example.board.auth.client.member.MemberProfileFeignAdapter;
import com.example.board.auth.credential.service.MemberProfileService;
import com.example.board.auth.credential.service.result.CreateProfileResult;
import com.example.board.auth.credential.service.result.DeleteProfileResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberProfileServiceImpl implements MemberProfileService {
    private final MemberProfileFeignAdapter memberProfileFeignAdapter;

    @Override
    public CreateProfileResult createProfile(Long id, MemberProfileCreateRequest request) {
        return switch (memberProfileFeignAdapter.createProfile(id, request)) {
            case CreateProfileClientResult.Success _ -> new CreateProfileResult.Success();
            case CreateProfileClientResult.HandleDuplicate _ -> new CreateProfileResult.HandleDuplicate();
            case CreateProfileClientResult.NicknameDuplicate _ -> new CreateProfileResult.NicknameDuplicate();
            case CreateProfileClientResult.UnexpectedConflictError _ -> new CreateProfileResult.UnexpectedConflictError();
            case CreateProfileClientResult.UnexpectedValidationError _ -> new CreateProfileResult.UnexpectedValidationError();
            case CreateProfileClientResult.DownstreamServiceError _ -> new CreateProfileResult.DownstreamServiceError();
        };
    }

    @Override
    public DeleteProfileResult deleteProfile(Long id) {
        return switch (memberProfileFeignAdapter.deleteProfile(id)) {
            case DeleteProfileClientResult.Success _ -> new DeleteProfileResult.Success();
            case DeleteProfileClientResult.DownstreamServiceError _ -> new DeleteProfileResult.DownstreamServiceError();
        };
    }
}
