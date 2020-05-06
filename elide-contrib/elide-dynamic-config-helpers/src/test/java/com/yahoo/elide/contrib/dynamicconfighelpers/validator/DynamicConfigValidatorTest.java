/*
 * Copyright 2020, Yahoo Inc.
 * Licensed under the Apache License, Version 2.0
 * See LICENSE file in project root for terms.
 */
package com.yahoo.elide.contrib.dynamicconfighelpers.validator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class DynamicConfigValidatorTest {

    @Test
    public void testNoArgumnents() {
        Exception e = assertThrows(IllegalStateException.class, () -> DynamicConfigValidator.main(null));
        assertEquals("No Arguments provided!", e.getMessage());
    }

    @Test
    public void testOneEmptyArgumnents() {
        Exception e = assertThrows(IllegalStateException.class, () -> DynamicConfigValidator.main(new String[] { "" }));
        assertEquals("Expecting One non-empty argument only!", e.getMessage());
    }

    @Test
    public void testMultipleArgumnents() {
        Exception e = assertThrows(IllegalStateException.class,
                () -> DynamicConfigValidator.main(new String[] { "", "" }));
        assertEquals("Expecting One non-empty argument only!", e.getMessage());
    }

    @Test
    public void testMissingConfigDir() {
        Exception e = assertThrows(IllegalStateException.class,
                () -> DynamicConfigValidator.main(new String[] { "src/test/resources/validator/missing" }));
        assertEquals("Model Configs Directory doesn't exists", e.getMessage());
    }

    @Test
    public void testValidConfigDir() {
        assertDoesNotThrow(() -> DynamicConfigValidator.main(new String[] { "src/test/resources/validator/valid" }));
    }

    @Test
    public void testMissingVariableConfig() {
        assertDoesNotThrow(
                () -> DynamicConfigValidator.main(new String[] { "src/test/resources/validator/missing_variable" }));
    }

    @Test
    public void testMissingSecurityConfig() {
        assertDoesNotThrow(
                () -> DynamicConfigValidator.main(new String[] { "src/test/resources/validator/missing_security" }));
    }

    @Test
    public void testMissingTableDir() {
        Exception e = assertThrows(IllegalStateException.class,
                () -> DynamicConfigValidator.main(new String[] { "src/test/resources/validator/missing_table_dir" }));
        assertTrue(e.getMessage().startsWith("Table Configs Directory doesn't exists at location"));
    }

    @Test
    public void testMissingTableConfig() {
        Exception e = assertThrows(IllegalStateException.class,
                () -> DynamicConfigValidator.main(new String[] { "src/test/resources/validator/missing_table" }));
        assertTrue(e.getMessage().startsWith("No Table Configs found at location"));
    }

    @Test
    public void testBadVariableConfig() {
        Exception e = assertThrows(IllegalStateException.class,
                () -> DynamicConfigValidator.main(new String[] { "src/test/resources/validator/bad_variable" }));
        assertTrue(e.getMessage().startsWith("Error while parsing variable config at location"));
    }

    @Test
    public void testBadSecurityConfig() {
        Exception e = assertThrows(IllegalStateException.class,
                () -> DynamicConfigValidator.main(new String[] { "src/test/resources/validator/bad_security" }));
        assertTrue(e.getMessage().startsWith("Error while parsing security config at location"));
    }

    @Test
    public void testBadSecurityRoleConfig() {
        Exception e = assertThrows(IllegalStateException.class,
                () -> DynamicConfigValidator.main(new String[] { "src/test/resources/validator/bad_security_role" }));
        assertEquals(e.getMessage(), "ROLE provided in security config contain one of these words: [,]");
    }

    @Test
    public void testBadTableConfigJoinType() {
        Exception e = assertThrows(IllegalStateException.class,
                () -> DynamicConfigValidator.main(new String[] { "src/test/resources/validator/bad_table_join_type" }));
        assertTrue(e.getMessage().startsWith("Error while parsing table config at location"));
    }

    @Test
    public void testBadTableConfigSQL() {
        Exception e = assertThrows(IllegalStateException.class,
                () -> DynamicConfigValidator.main(new String[] { "src/test/resources/validator/bad_table_sql" }));
        assertEquals(e.getMessage(),
                "SQL provided in table config contain one of these words: [DROP , TRUNCATE , DELETE , INSERT , UPDATE ]");
    }

    @Test
    public void testUndefinedVariable() {
        Exception e = assertThrows(IllegalStateException.class,
                () -> DynamicConfigValidator.main(new String[] { "src/test/resources/validator/undefined_handlebar" }));
        assertEquals(e.getMessage(),
                "foobar is used as a variable in either table or security config files but is not defined in variables config file.");
    }
}
