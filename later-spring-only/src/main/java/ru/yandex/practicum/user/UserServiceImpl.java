package ru.yandex.practicum.user;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
class UserServiceImpl implements UserService {
    private final UserRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return repository.findAll()
                .stream()
                .map(User::from)
                .collect(Collectors.toList());
    }

    @Override
    public User saveUser(User user) {
        return repository.save(user);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void updateUsers(){
        // код метода
    }
}