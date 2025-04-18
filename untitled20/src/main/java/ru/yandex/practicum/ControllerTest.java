package ru.yandex.practicum;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
@Slf4j
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class ControllerTest {
    @GetMapping
    public ResponseEntity<List<User>> getAllFilms() {
        log.info("Request");
        return ResponseEntity.ok().body(new FakeRepository().findAll());
    }
}