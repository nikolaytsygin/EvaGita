package com.eva.evagita.validation;

import com.eva.evagita.model.Tag;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TagValidationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        validatorFactory.close();
    }

    @Test
    void validTagName_shouldPassValidation() {
        Tag tag = new Tag();
        tag.setName("backend");

        Set<ConstraintViolation<Tag>> violations =
                validator.validate(tag);

        assertTrue(violations.isEmpty());
    }

    @Test
    void emptyTagName_shouldFailValidation() {
        Tag tag = new Tag();
        tag.setName("");

        Set<ConstraintViolation<Tag>> violations =
                validator.validate(tag);

        assertEquals(1, violations.size());
        assertEquals(
                "Tag name must not be blank",
                violations.iterator().next().getMessage()
        );
    }

    @Test
    void whitespaceTagName_shouldFailValidation() {
        Tag tag = new Tag();
        tag.setName("   ");

        Set<ConstraintViolation<Tag>> violations =
                validator.validate(tag);

        assertEquals(1, violations.size());
        assertEquals(
                "Tag name must not be blank",
                violations.iterator().next().getMessage()
        );
    }

    @Test
    void nullTagName_shouldFailValidation() {
        Tag tag = new Tag();
        tag.setName(null);

        Set<ConstraintViolation<Tag>> violations =
                validator.validate(tag);

        assertEquals(1, violations.size());
        assertEquals(
                "Tag name must not be blank",
                violations.iterator().next().getMessage()
        );
    }
}
