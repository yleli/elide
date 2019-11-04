/*
 * Copyright 2019, Yahoo Inc.
 * Licensed under the Apache License, Version 2.0
 * See LICENSE file in project root for terms.
 */
package com.yahoo.elide.datastores.aggregation.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.yahoo.elide.core.EntityDictionary;
import com.yahoo.elide.datastores.aggregation.annotation.CardinalitySize;
import com.yahoo.elide.datastores.aggregation.example.Country;
import com.yahoo.elide.datastores.aggregation.example.Player;
import com.yahoo.elide.datastores.aggregation.example.PlayerStats;
import com.yahoo.elide.datastores.aggregation.example.SubCountry;
import com.yahoo.elide.datastores.aggregation.example.VideoGame;
import com.yahoo.elide.datastores.aggregation.schema.metric.Max;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collections;

public class SchemaTest {

    private static Schema playerStatsSchema;

    @BeforeAll
    public static void setupEntityDictionary() {
        EntityDictionary entityDictionary = new EntityDictionary(Collections.emptyMap());
        entityDictionary.bindEntity(Country.class);
        entityDictionary.bindEntity(SubCountry.class);
        entityDictionary.bindEntity(VideoGame.class);
        entityDictionary.bindEntity(Player.class);
        entityDictionary.bindEntity(PlayerStats.class);

        playerStatsSchema = new Schema(PlayerStats.class, entityDictionary);
    }

    @Test
    public void testMetricCheck() {
        assertTrue(playerStatsSchema.isMetricField("highScore"));
        assertFalse(playerStatsSchema.isMetricField("country"));
    }

    @Test
    public void testGetDimension() {
        assertEquals(CardinalitySize.SMALL, playerStatsSchema.getDimension("country").getCardinality());
    }

    @Test
    public void testGetMetric() {
        assertEquals(
                "MAX(com_yahoo_elide_datastores_aggregation_example_PlayerStats.highScore)",
                playerStatsSchema.getMetric("highScore").getMetricExpression(Max.class)
        );
    }
}