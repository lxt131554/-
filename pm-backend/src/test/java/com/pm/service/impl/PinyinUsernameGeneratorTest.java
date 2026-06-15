package com.pm.service.impl;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PinyinUsernameGeneratorTest {

    @Test
    void generatesLowercasePinyinUsernameFromChineseName() {
        String username = PinyinUsernameGenerator.generate("彭成", name -> false);

        assertEquals("pengcheng", username);
    }

    @Test
    void appendsNumberWhenGeneratedUsernameAlreadyExists() {
        Set<String> existing = new HashSet<>();
        existing.add("pengcheng");
        existing.add("pengcheng2");

        String username = PinyinUsernameGenerator.generate("彭成", existing::contains);

        assertEquals("pengcheng3", username);
    }

    @Test
    void keepsLettersAndDigitsForMixedNames() {
        String username = PinyinUsernameGenerator.generate("OA负责人A1", name -> false);

        assertEquals("oafuzerena1", username);
    }
}
