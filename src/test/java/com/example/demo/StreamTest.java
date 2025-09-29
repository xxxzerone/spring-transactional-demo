package com.example.demo;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class StreamTest {

    @Test
    void streamTest() {
        List<Integer> numbers = List.of(1, 2, 3, 4, 5);

        Stream<Integer> numberStream = numbers.stream()
            .filter(num -> {
                log.info("Filtering number: {}", num);
                return num % 2 == 1;
            })
            .map(num -> {
                log.info("Mapping number: {}", num);
                return num * num;
            });

        log.info("Stream created, but not yet processed.");

        log.info("Processing stream collect operation");
        List<Integer> result = numberStream.collect(Collectors.toList());
        log.info("stream collect completed Result: {}", result);
    }
}
