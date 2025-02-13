package wander.wise.application.service.user;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import wander.wise.application.dto.collection.CollectionWithoutCardsDto;
import wander.wise.application.dto.comment.CommentDto;
import wander.wise.application.dto.social.link.SocialLinkDto;
import wander.wise.application.dto.user.UserDto;
import wander.wise.application.dto.user.login.LoginRequestDto;
import wander.wise.application.dto.user.login.LoginResponseDto;
import wander.wise.application.dto.user.registration.RegisterUserRequestDto;
import wander.wise.application.dto.user.update.RestorePasswordRequestDto;
import wander.wise.application.dto.user.update.UpdateUserEmailRequestDto;
import wander.wise.application.dto.user.update.UpdateUserInfoRequestDto;
import wander.wise.application.dto.user.update.UpdateUserPasswordRequestDto;
import wander.wise.application.dto.user.update.UpdateUserRolesRequestDto;
import wander.wise.application.exception.custom.AuthorizationException;
import wander.wise.application.exception.custom.RegistrationException;
import wander.wise.application.mapper.CollectionMapper;
import wander.wise.application.mapper.CommentMapper;
import wander.wise.application.mapper.SocialLinkMapper;
import wander.wise.application.mapper.UserMapper;
import wander.wise.application.model.Collection;
import wander.wise.application.model.Role;
import wander.wise.application.model.User;
import wander.wise.application.repository.collection.CollectionRepository;
import wander.wise.application.repository.comment.CommentRepository;
import wander.wise.application.repository.user.pseudonym.PseudonymRepository;
import wander.wise.application.repository.user.UserRepository;
import wander.wise.application.security.AuthenticationService;
import wander.wise.application.security.JwtUtil;
import wander.wise.application.service.api.email.EmailService;
import wander.wise.application.service.api.storage.StorageService;

import static wander.wise.application.constants.GlobalConstants.DIVIDER;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private static final long ROOT_ID = 1L;
    private static final long ADMIN_ID = 2L;
    private static final long USER_ID = 3L;
    private static final String EMAIL_CONFIRM_SUBJECT = "Email confirmation";
    private static final String RESTORE_PASSWORD_SUBJECT = "New password";
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder encoder;
    private final Random random = new Random();
    private final EmailService emailService;
    private final JwtUtil jwtUtil;
    private final AuthenticationService authenticationService;
    private final SocialLinkMapper socialLinkMapper;
    private final CollectionRepository collectionRepository;
    private final CollectionMapper collectionMapper;
    private final StorageService storageService;
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final PseudonymRepository pseudonymRepository;

    @Override
    @Transactional
    public UserDto save(RegisterUserRequestDto requestDto) {
        if (userRepository.existsByEmail(requestDto.email())) {
            throw new RegistrationException("Account with this "
                    + "email address already exists");
        }
        User savedUser = userRepository.save(createAndInitializeUser(requestDto));
        createAndSaveDefaultCollections(savedUser);
        UserDto userDto = userMapper.toDto(savedUser);
        userDto = userDto.setEmailConfirmCode(sendEmailConfirmCode(savedUser.getEmail()));
        return userDto;
    }

    @Override
    @Transactional
    public LoginResponseDto confirmEmail(String email) {
        User updatedUser = findUserEntityByEmail(email);
        updatedUser.setBanned(false);
        UserDto savedUser = userMapper.toDto(userRepository.save(updatedUser));
        String token = jwtUtil.generateToken(updatedUser.getEmail());
        return new LoginResponseDto(savedUser, token);
    }

    @Override
    public UserDto findById(Long id) {
        return userMapper.toDto(userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Can't find user by id: " + id)));
    }

    @Override
    public Set<SocialLinkDto> getUserSocialLinks(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Can't find user by id: " + id))
                .getSocialLinks()
                .stream()
                .map(socialLinkMapper::toDto)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<CollectionWithoutCardsDto> getUserCollections(Long id, String email) {
        User user = findUserAndAuthorize(id, email);
        Set<Collection> collections = getCollectionsAndInitializeImageLinks(user);
        return collections.stream()
                .map(collectionMapper::toCollectionWithoutCardsDto)
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional
    public Set<CommentDto> getUserComments(Long id, String email) {
        User user = findUserAndAuthorize(id, email);
        Set<CommentDto> userComments = commentRepository.getAllByUserEmail(email)
                .stream()
                .map(commentMapper::toDto)
                .collect(Collectors.toSet());
        return userComments;
    }

    @Override
    @Transactional
    public void restorePassword(RestorePasswordRequestDto requestDto) {
        User updatedUser = findUserEntityByEmail(requestDto.email());
        String newPassword = ("" + UUID.randomUUID()).replaceAll("-", "");
        updatedUser.setPassword(encoder.encode(newPassword));
        userRepository.save(updatedUser);
        emailService.sendEmail(
                requestDto.email(),
                RESTORE_PASSWORD_SUBJECT,
                newPassword);
    }

    @Override
    @Transactional
    public UserDto updateUserInfo(Long id, String email, UpdateUserInfoRequestDto requestDto) {
        User updatedUser = findUserAndAuthorize(id, email);
        if (userRepository.existsByPseudonym(requestDto.pseudonym())) {
            throw new RegistrationException("Such user already exists");
        } else {
            return userMapper.toDto(userRepository.save(userMapper
                    .updateUserFromDto(updatedUser, requestDto)));
        }
    }

    @Override
    @Transactional
    public UserDto updateUserImage(Long id, String email, MultipartFile userImage) {
        User updatedUser = findUserAndAuthorize(id, email);
        if (updatedUser.getProfileImage() != null) {
            storageService
                    .deleteFile(updatedUser
                    .getProfileImage()
                    .substring(updatedUser
                            .getProfileImage()
                            .lastIndexOf("/") + 1));
        }
        if (userImage.getSize() == 0) {
            updatedUser.setProfileImage(null);
        } else {
            updatedUser.setProfileImage(storageService.uploadFile(userImage));
        }
        return userMapper.toDto(userRepository.save(updatedUser));
    }

    @Override
    @Transactional
    public UserDto updateUserRoles(Long id, UpdateUserRolesRequestDto requestDto) {
        User updatedUser = findUserEntityById(id);
        Set<Role> newRoles = requestDto
                .roleIds()
                .stream()
                .map(Role::new)
                .collect(Collectors.toSet());
        updatedUser.setRoles(newRoles);
        return userMapper.toDto(userRepository.save(updatedUser));
    }

    @Override
    public UserDto requestUpdateUserEmail(Long id, String email,
                                          UpdateUserEmailRequestDto requestDto) {
        User updatedUser = findUserAndAuthorize(id, email);
        String confirmCode = sendEmailConfirmCode(requestDto.email());
        UserDto updatedUserDto = userMapper.toDto(updatedUser);
        updatedUserDto = updatedUserDto.setEmailConfirmCode(confirmCode);
        return updatedUserDto;
    }

    @Override
    @Transactional
    public LoginResponseDto updateUserEmail(Long id, String email,
                                            UpdateUserEmailRequestDto requestDto) {
        User updatedUser = findUserAndAuthorize(id, email);
        updatedUser.setEmail(requestDto.email());
        UserDto savedUser = userMapper.toDto(userRepository.save(updatedUser));
        String token = jwtUtil.generateToken(updatedUser.getEmail());
        return new LoginResponseDto(savedUser, token);
    }

    @Override
    @Transactional
    public LoginResponseDto updateUserPassword(Long id, String email,
                                               UpdateUserPasswordRequestDto requestDto) {
        LoginRequestDto checkLogin = new LoginRequestDto(email, requestDto.oldPassword());
        authenticationService.authenticate(checkLogin);
        User updatedUser = findUserAndAuthorize(id, email);
        updatedUser.setPassword(encoder.encode(requestDto.password()));
        userRepository.save(updatedUser);
        LoginRequestDto loginRequestDto = new LoginRequestDto(email, requestDto.password());
        return authenticationService.authenticate(loginRequestDto);
    }

    @Override
    @Transactional
    public void deleteUser(Long id, String email) {
        User updatedUser = findUserAndAuthorize(id, email);
        userRepository.deleteById(updatedUser.getId());
    }

    @Override
    @Transactional
    public UserDto banUser(Long id) {
        User updatedUser = findUserEntityById(id);
        updatedUser.setBanned(true);
        return userMapper.toDto(userRepository.save(updatedUser));
    }

    @Override
    @Transactional
    public UserDto unbanUser(Long id) {
        User updatedUser = findUserEntityById(id);
        updatedUser.setBanned(false);
        return userMapper.toDto(userRepository.save(updatedUser));
    }

    @Override
    public User findUserAndAuthorize(Long id, String email) {
        User user = findUserEntityById(id);
        if (!user.getEmail().equals(email)) {
            throw new AuthorizationException("Access denied.");
        }
        return user;
    }

    @Override
    public User findUserEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Can't find user by id: " + id));
    }

    @Override
    public User findUserEntityByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Can't find user by email: " + email));
    }

    private User createAndInitializeUser(RegisterUserRequestDto requestDto) {
        User newUser = userMapper.toModel(requestDto);
        if (userRepository.count() == 0) {
            newUser.setRoles(Set.of(
                    new Role(ROOT_ID),
                    new Role(ADMIN_ID),
                    new Role(USER_ID)));
        } else {
            newUser.setRoles(Set.of(new Role(USER_ID)));
        }
        newUser.setPassword(encoder.encode(newUser.getPassword()));
        newUser.setPseudonym(generatePseudonym());
        return newUser;
    }

    private void createAndSaveDefaultCollections(User savedUser) {
        Collection likedCards = createCollection("Liked cards", savedUser);
        Collection createdCards = createCollection("Created cards", savedUser);
        Collection savedCards = createCollection("Saved cards", savedUser);
        collectionRepository.saveAll(List.of(likedCards, createdCards, savedCards));
    }

    private String generateEmailConfirmCode() {
        return "" + random.nextInt(1000, 9999);
    }

    private String sendEmailConfirmCode(String email) {
        String emailConfirmCode = generateEmailConfirmCode();
        emailService.sendEmail(
                email,
                EMAIL_CONFIRM_SUBJECT,
                emailConfirmCode);
        return emailConfirmCode;
    }

    private String generatePseudonym() {
        StringBuilder pseudonym = new StringBuilder();
        pseudonym
                .append(pseudonymRepository.getAdjective())
                .append(pseudonymRepository.getColor())
                .append(pseudonymRepository.getAnimal())
                .append(random.nextInt(1, 10000));
        if (userRepository.existsByPseudonym(pseudonym.toString())) {
            return generatePseudonym();
        }
        return pseudonym.toString();
    }

    private static Set<Collection> getCollectionsAndInitializeImageLinks(User user) {
        Set<Collection> collections = user.getCollections();
        collections.forEach(collection -> {
            if (collection.getImageLink() == null && !collection.getCards().isEmpty()) {
                String[] imageLinks = collection.getCards()
                        .stream()
                        .toList()
                        .get(0)
                        .getImageLinks()
                        .split(DIVIDER);
                collection.setImageLink(imageLinks[0]);
            }
        });
        return collections;
    }

    private static Collection createCollection(String name, User user) {
        Collection newCollection = new Collection();
        newCollection.setName(name);
        newCollection.setUser(user);
        return newCollection;
    }
}
