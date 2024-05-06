package com.gmail.merikbest2015.service.util;

import com.gmail.merikbest2015.exception.ApiRequestException;
import com.gmail.merikbest2015.exception.InputFieldException;
import com.gmail.merikbest2015.model.User;
import com.gmail.merikbest2015.repository.BlockUserRepository;
import com.gmail.merikbest2015.repository.FollowerUserRepository;
import com.gmail.merikbest2015.repository.MuteUserRepository;
import com.gmail.merikbest2015.repository.UserRepository;
import com.gmail.merikbest2015.repository.projection.SameFollower;
import com.gmail.merikbest2015.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import static com.gmail.merikbest2015.constants.ErrorMessage.*;
import static com.gmail.merikbest2015.constants.PathConstants.AUTH_USER_ID_HEADER;

@Component
@RequiredArgsConstructor
public class UserServiceHelper {

   // @Lazy
    //private final AuthenticationService authenticationService;
    private final UserRepository userRepository;
    private final FollowerUserRepository followerUserRepository;
    private final BlockUserRepository blockUserRepository;
    private final MuteUserRepository muteUserRepository;

    public void processInputErrors(BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new InputFieldException(bindingResult);
        }
    }

    public Long getAuthenticatedUserId() {
        return getUserId();
    }
    
    private Long getUserId() {
        RequestAttributes attribs = RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) attribs).getRequest();
        return Long.parseLong(request.getHeader(AUTH_USER_ID_HEADER));
    }

    
    public void validateUserProfile(Long userId) {
        checkIsUserExist(userId);
        Long authUserId = getAuthenticatedUserId();

        if (!userId.equals(authUserId)) {
            checkIsUserBlocked(userId);
            checkIsUserHavePrivateProfile(userId);
        }
    }

    public void checkIsUserExistOrMyProfileBlocked(Long userId) {
        checkIsUserExist(userId);
        checkIsUserBlocked(userId);
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiRequestException(String.format(USER_ID_NOT_FOUND, userId), HttpStatus.NOT_FOUND));
    }

    public void checkIsUserExist(Long userId) {
        boolean userExist = userRepository.isUserExist(userId);

        if (!userExist) {
            throw new ApiRequestException(String.format(USER_ID_NOT_FOUND, userId), HttpStatus.NOT_FOUND);
        }
    }

    public void checkIsUserBlocked(User user, User authUser) {
        if (blockUserRepository.isUserBlocked(user, authUser)) {
            throw new ApiRequestException(USER_PROFILE_BLOCKED, HttpStatus.BAD_REQUEST);
        }
    }

    public void checkIsUserBlocked(Long userId) {
        Long authUserId = getAuthenticatedUserId();

        if (blockUserRepository.isUserBlocked(userId, authUserId)) {
            throw new ApiRequestException(USER_PROFILE_BLOCKED, HttpStatus.BAD_REQUEST);
        }
    }

    public void checkIsUserHavePrivateProfile(Long userId) {
        Long authUserId = getAuthenticatedUserId();

        if (!userRepository.isUserHavePrivateProfile(userId, authUserId)) {
            throw new ApiRequestException(USER_NOT_FOUND, HttpStatus.NOT_FOUND);
        }
    }

    public boolean isUserFollowByOtherUser(Long userId) {
        Long authUserId =getAuthenticatedUserId();
        return followerUserRepository.isUserFollowByOtherUser(authUserId, userId);
    }

    public boolean isUserHavePrivateProfile(Long userId) {
        Long authUserId = getAuthenticatedUserId();
        return !userRepository.isUserHavePrivateProfile(userId, authUserId);
    }

    public boolean isUserBlockedByMyProfile(Long userId) {
        Long authUserId = getAuthenticatedUserId();
        return blockUserRepository.isUserBlocked(authUserId, userId);
    }

    public boolean isUserMutedByMyProfile(Long userId) {
        Long authUserId = getAuthenticatedUserId();
        return muteUserRepository.isUserMuted(authUserId, userId);
    }

    public boolean isMyProfileBlockedByUser(Long userId) {
        Long authUserId = getAuthenticatedUserId();
        return blockUserRepository.isUserBlocked(userId, authUserId);
    }

    public boolean isMyProfileWaitingForApprove(Long userId) {
        Long authUserId = getAuthenticatedUserId();
        return userRepository.isMyProfileWaitingForApprove(userId, authUserId);
    }

    public boolean isMyProfileSubscribed(Long userId) {
        Long authUserId = getAuthenticatedUserId();
        return userRepository.isMyProfileSubscribed(userId, authUserId);
    }

    public List<SameFollower> getSameFollowers(Long userId) {
        Long authUserId = getAuthenticatedUserId();
        return followerUserRepository.getSameFollowers(userId, authUserId, SameFollower.class);
    }
}
