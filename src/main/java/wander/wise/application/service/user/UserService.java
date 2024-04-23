package wander.wise.application.service.user;

import wander.wise.application.dto.collection.CollectionWithoutCardsDto;
import wander.wise.application.dto.social.link.SocialLinkDto;
import wander.wise.application.dto.user.UserDto;
import wander.wise.application.dto.user.login.LoginResponseDto;
import wander.wise.application.dto.user.registration.RegisterUserRequestDto;
import wander.wise.application.dto.user.update.*;
import wander.wise.application.model.Collection;
import wander.wise.application.model.User;
import java.util.Set;

public interface UserService {
    UserDto save(RegisterUserRequestDto requestDto);

    LoginResponseDto confirmEmail(String email);

    UserDto updateUserInfo(Long id, String email, UpdateUserInfoRequestDto requestDto);

    UserDto updateUserRoles(Long id, UpdateUserRolesRequestDto requestDto);

    UserDto requestUpdateUserEmail(Long id, String email, UpdateUserEmailRequestDto requestDto);

    LoginResponseDto updateUserEmail(Long id, String email, UpdateUserEmailRequestDto requestDto);

    UserDto requestUpdateUserPassword(Long id, String email);

    LoginResponseDto updateUserPassword(Long id, String email, UpdateUserPasswordRequestDto requestDto);

    UserDto banUser(Long id);

    UserDto unbanUser(Long id);

    void deleteUser(Long id, String email);

    UserDto findById(Long id);

    Set<SocialLinkDto> getUserSocialLinks(Long id);

    Set<CollectionWithoutCardsDto> getUserCollections(Long id, String email);

    void restorePassword(RestorePasswordRequestDto requestDto);

    User findUserAndAuthorize(Long id, String email);
}
