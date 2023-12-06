package com.study.snsbackoffice.user.service;

import com.study.snsbackoffice.common.constant.ExceptionType;
import com.study.snsbackoffice.common.exception.GlobalCustomException;
import com.study.snsbackoffice.user.dto.*;
import com.study.snsbackoffice.common.entity.RefreshToken;
import com.study.snsbackoffice.user.entity.User;
import com.study.snsbackoffice.user.entity.UserRoleEnum;
import com.study.snsbackoffice.user.repository.RefreshTokenRepository;
import com.study.snsbackoffice.user.repository.UserRepository;
import com.study.snsbackoffice.user.util.UserValidUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserValidUtil userValidUtil;

    public UserResponseDto getInfo(User user) {
        return new UserResponseDto(user);
    }
    public SignupResponseDto signup(SignupRequestDto requestDto, BindingResult bindingResult) {
        // Validation 예외처리
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        if(fieldErrors.size() > 0) {
            for (FieldError fieldError : bindingResult.getFieldErrors()) {
                throw new IllegalArgumentException(fieldError.getField() + " 필드 : " + fieldError.getDefaultMessage());
            }
        }

        User user = userValidUtil.getValidNewUserByRequestDto(requestDto);
        user.setRole(UserRoleEnum.USER);

        // 사용자 등록
        userRepository.save(user);
        return new SignupResponseDto(user);
    }

    public DescriptionResponseDto addDescriptionUser(User user, DescriptionRequestDto requestDto) {
        user.descUpdate(requestDto.getDesc());
        userRepository.save(user);
        return new DescriptionResponseDto(requestDto.getDesc());
    }

    public UserUpdateResponseDto updateUser(User user, UserRequestDto requestDto) {
        // email 중복확인
        Optional<User> checkEmail = userRepository.findByEmailAndIdNot(requestDto.getEmail(), user.getId());
        if (checkEmail.isPresent()) {
            throw new GlobalCustomException(ExceptionType.DUPLICATE_EMAIL);
        }

        user.update(requestDto);
        userRepository.save(user);
        return new UserUpdateResponseDto(requestDto);
    }

    public String logout(User user) {
        RefreshToken token = refreshTokenRepository.findByUsername(user.getUsername());
        refreshTokenRepository.delete(token);
        return user.getUsername();
    }

    public UserResponseDto updatePassword(User user, PasswordRequestDto requestDto) {
        if (!userValidUtil.matchesPassword(user.getPassword(), requestDto.getPreviousPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치 하지 않습니다.");
        }

        user.updatePassword(userValidUtil.encodePassword(requestDto.getPassword()));
        userRepository.save(user);
        return new UserResponseDto(user);
    }

}
