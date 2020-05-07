/*
 * Copyright 2020, Yahoo Inc.
 * Licensed under the Apache License, Version 2.0
 * See LICENSE file in project root for terms.
 */
package com.yahoo.elide.contrib.dynamicconfighelpers.validator;

import com.yahoo.elide.contrib.dynamicconfighelpers.DynamicConfigHelpers;
import com.yahoo.elide.contrib.dynamicconfighelpers.model.ElideSecurityConfig;
import com.yahoo.elide.contrib.dynamicconfighelpers.model.ElideTableConfig;
import com.yahoo.elide.contrib.dynamicconfighelpers.model.Table;

import org.apache.commons.io.FileUtils;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Data
public class DynamicConfigValidator {

    private static ElideTableConfig elideTableConfig;
    private static ElideSecurityConfig elideSecurityConfig;
    private static Map<String, Object> variables;
    private static final String[] SQL_DISALLOWED_WORDS = new String[] { "DROP ", "TRUNCATE ", "DELETE ", "INSERT ",
            "UPDATE " };
    private static final String[] ROLE_NAME_DISALLOWED_WORDS = new String[] { "," };

    public static void main(String[] args) {

        if (args == null || args.length == 0) {
            usage();
            throw new IllegalStateException("No Arguments provided!");
        }

        if (args.length > 1 || DynamicConfigHelpers.isNullOrEmpty(args[0])) {
            usage();
            throw new IllegalStateException("Expecting One non-empty argument only!");
        }

        File file = new File(args[0]);
        String absoluteBasePath = DynamicConfigHelpers.formatFilePath(file.getAbsolutePath());
        log.info("Absolute Path for Model Configs Directory: " + absoluteBasePath);

        if (!file.isDirectory()) {
            usage();
            throw new IllegalStateException("Model Configs Directory doesn't exists");
        }

        readVariableConfig(absoluteBasePath);
        if (readSecurityConfig(absoluteBasePath)) {
            validateRoleInSecurityConfig(elideSecurityConfig);
        }
        if (readTableConfig(absoluteBasePath)) {
            validateSqlInTableConfig(elideTableConfig);
        }

        log.info("Configs Validation Passed!");
    }

    private static boolean readVariableConfig(String absoluteBasePath) {
        boolean isVariableConfig = exists(absoluteBasePath + DynamicConfigHelpers.VARIABLE_CONFIG_PATH);
        try {
            variables = isVariableConfig ? DynamicConfigHelpers.getVaribalesPojo(absoluteBasePath)
                    : Collections.<String, Object>emptyMap();
        } catch (Exception e) {
            throw new IllegalStateException("Error while parsing variable config at location: " + absoluteBasePath, e);
        }
        return isVariableConfig;
    }

    private static boolean readSecurityConfig(String absoluteBasePath) {
        boolean isSecurityConfig = exists(absoluteBasePath + DynamicConfigHelpers.SECURITY_CONFIG_PATH);
        if (isSecurityConfig) {
            String securityConfigContent = DynamicConfigHelpers
                    .readConfigFile(new File(absoluteBasePath + DynamicConfigHelpers.SECURITY_CONFIG_PATH));
            validateConfigForMissingVariables(securityConfigContent, variables);
            try {
                elideSecurityConfig = DynamicConfigHelpers.getElideSecurityPojo(absoluteBasePath, variables);
            } catch (Exception e) {
                throw new IllegalStateException("Error while parsing security config at location: " + absoluteBasePath,
                        e);
            }
        }
        return isSecurityConfig;
    }

    private static boolean readTableConfig(String absoluteBasePath) {
        boolean isTableConfig = exists(absoluteBasePath + DynamicConfigHelpers.TABLE_CONFIG_PATH);
        if (isTableConfig) {
            Collection<File> tableConfigs = FileUtils.listFiles(
                    new File(absoluteBasePath + DynamicConfigHelpers.TABLE_CONFIG_PATH), new String[] { "hjson" },
                    false);
            if (tableConfigs.isEmpty()) {
                usage();
                throw new IllegalStateException("No Table Configs found at location: " + absoluteBasePath
                        + DynamicConfigHelpers.TABLE_CONFIG_PATH);
            }
            for (File tableConfig : tableConfigs) {
                String tableConfigContent = DynamicConfigHelpers.readConfigFile(tableConfig);
                validateConfigForMissingVariables(tableConfigContent, variables);
            }
            try {
                elideTableConfig = DynamicConfigHelpers.getElideTablePojo(absoluteBasePath, variables);
            } catch (Exception e) {
                throw new IllegalStateException("Error while parsing table config at location: " + absoluteBasePath, e);
            }
        } else {
            usage();
            throw new IllegalStateException("Table Configs Directory doesn't exists at location: " + absoluteBasePath
                    + DynamicConfigHelpers.TABLE_CONFIG_PATH);
        }
        return isTableConfig;
    }

    private static boolean exists(String filePath) {
        return new File(filePath).exists();
    }

    private static void validateConfigForMissingVariables(String config, Map<String, Object> variables) {
        Pattern regex = Pattern.compile("<%(.*?)%>");
        Matcher regexMatcher = regex.matcher(config);
        while (regexMatcher.find()) {
            String str = regexMatcher.group(1).trim();
            if (!variables.containsKey(str)) {
                throw new IllegalStateException(str + " is used as a variable in either table or security config files "
                        + "but is not defined in variables config file.");
            }
        }
    }

    private static boolean validateSqlInTableConfig(ElideTableConfig elideTableConfig) {
        for (Table table : elideTableConfig.getTables()) {
            if (containsDisallowedWords(table.getSql(), SQL_DISALLOWED_WORDS)) {
                throw new IllegalStateException("SQL provided in table config contain one of these words: "
                        + Arrays.toString(SQL_DISALLOWED_WORDS));
            }
        }
        return true;
    }

    private static boolean validateRoleInSecurityConfig(ElideSecurityConfig elideSecurityConfig) {
        for (String role : elideSecurityConfig.getRoles()) {
            if (containsDisallowedWords(role, ROLE_NAME_DISALLOWED_WORDS)) {
                throw new IllegalStateException("ROLE provided in security config contain one of these words: "
                        + Arrays.toString(ROLE_NAME_DISALLOWED_WORDS));
            }
        }
        return true;
    }

    private static boolean containsDisallowedWords(String str, String[] keywords) {
        return Arrays.stream(keywords).parallel().anyMatch(str.toUpperCase(Locale.ENGLISH)::contains);
    }

    private static void usage() {
        log.info("Usage: java -cp <Jar File Name>"
                + " com.yahoo.elide.contrib.dynamicconfighelpers.validator.DynamicConfigValidator"
                + " <Path for Model Configs Directory>\n" + "Expected Directory Structure:\n"
                + "./security.hjson(optional)\n" + "./variables.hjson(optional)\n" + "./tables/\n"
                + "./tables/table1.hjson\n" + "./tables/table2.hjson\n" + "./tables/tableN.hjson\n");
    }
}
