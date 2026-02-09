package com.minhtetthar.post_now.service;

import com.minhtetthar.post_now.dto.user.UserCreateDto;
import com.minhtetthar.post_now.dto.user.UserDto;
import com.minhtetthar.post_now.dto.user.UserUpdateDto;
import com.minhtetthar.post_now.entity.User;
import com.minhtetthar.post_now.mapper.UserMapper;
import com.minhtetthar.post_now.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Cacheable(value = "users", key = "#username")
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    public UserDto getCurrentUser(String username) {
        User user = loadUserByUsername(username);
        return userMapper.toDto(user);
    }

    @Cacheable(value = "users", key = "#id")
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return userMapper.toDto(user);
    }

    public UserDto getUserByUsername(String username) {
        User user = loadUserByUsername(username);
        return userMapper.toDto(user);
    }

    public Page<UserDto> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(userMapper::toDto);
    }

    public Page<UserDto> searchUsers(String searchTerm, Pageable pageable) {
        return userRepository.findBySearchTerm(searchTerm, pageable)
                .map(userMapper::toDto);
    }

    @Transactional
    public UserDto createUser(UserCreateDto createDto) {
        if (userRepository.existsByUsername(createDto.getUsername())) {
            throw new RuntimeException("Username already exists: " + createDto.getUsername());
        }
        if (userRepository.existsByEmail(createDto.getEmail())) {
            throw new RuntimeException("Email already exists: " + createDto.getEmail());
        }

        User user = userMapper.toEntity(createDto);
        user.setPassword(passwordEncoder.encode(createDto.getPassword()));
        user.setRole(User.Role.USER);
        user = userRepository.save(user);
        return userMapper.toDto(user);
    }

    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public UserDto updateUser(String username, UserUpdateDto updateDto) {
        User user = loadUserByUsername(username);

        if (updateDto.getEmail() != null && !updateDto.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(updateDto.getEmail())) {
                throw new RuntimeException("Email already exists: " + updateDto.getEmail());
            }
        }

        userMapper.updateEntity(user, updateDto);
        user = userRepository.save(user);
        return userMapper.toDto(user);
    }

    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public void deleteUser(String username) {
        User user = loadUserByUsername(username);
        user.setEnabled(false);
        userRepository.save(user);
    }
}