package com.example.board.auth.credential.controller.dto.validation;

import jakarta.validation.GroupSequence;

@GroupSequence({NotBlankGroup.class, SizeGroup.class, FormatGroup.class})
public interface SignupValidationSequence {
}
