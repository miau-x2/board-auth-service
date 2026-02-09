package com.example.board.auth.credential.controller.dto.request;


import com.example.board.auth.credential.controller.dto.validation.FormatGroup;
import com.example.board.auth.credential.controller.dto.validation.NotBlankGroup;
import com.example.board.auth.credential.controller.dto.validation.SizeGroup;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Locale;

public record MemberSignupRequest(
        @NotBlank(message = "아이디를 입력해주세요.", groups = NotBlankGroup.class)
        @Size(min = 5, max = 20, message = "아이디는 5~20자입니다.", groups = SizeGroup.class)
        @Pattern(
                regexp = "^(?=.*[a-z])[a-z0-9]+$",
                message = "아이디는 영문 소문자와 숫자만 가능하며 영문은 필수입니다.",
                groups = FormatGroup.class
        )
        String username,

        @NotBlank(message = "비밀번호를 입력해주세요.", groups = NotBlankGroup.class)
        @Size(min = 8, max = 20, message = "비밀번호는 8~20자입니다.", groups = SizeGroup.class)
        @Pattern(
                regexp = "^(?=\\S+$)(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()])[A-Za-z\\d!@#$%^&*()]+$",
                message = "비밀번호는 영문, 숫자, 특수문자('!', '@', '#', '$', '%', '^', '&', '*', '(', ')')를 각각 1개 이상 포함해야 하며 공백은 사용할 수 없습니다.",
                groups = FormatGroup.class
        )
        String password,

        @NotBlank(message = "이메일을 입력해주세요.", groups = NotBlankGroup.class)
        @Email(message = "이메일 형식이 올바르지 않습니다.", groups = FormatGroup.class)
        String email,

        @NotBlank(message = "닉네임을 입력해주세요.", groups = NotBlankGroup.class)
        @Size(min = 2, max = 20, message = "닉네임은 2~20자입니다.", groups = SizeGroup.class)
        @Pattern(
                regexp = "^[a-z0-9가-힣]+$",
                message = "닉네임은 한글, 영문 소문자, 숫자만 사용할 수 있습니다.",
                groups = FormatGroup.class
        )
        String nickname
)
{
    public MemberSignupRequest {
        username = username == null ? null : username.strip();
        email = email == null ? null : email.strip().toLowerCase(Locale.ROOT);
        nickname = nickname == null ? null : nickname.strip();
    }

    public static MemberSignupRequest empty() {
        return new MemberSignupRequest(null, null, null, null);
    }
}