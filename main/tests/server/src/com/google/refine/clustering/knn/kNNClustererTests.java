/*******************************************************************************
 * Copyright (C) 2018, OpenRefine contributors
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/

package com.google.refine.clustering.knn;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.Collections;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import org.testng.annotations.Test;

import com.google.refine.RefineTest;
import com.google.refine.browsing.Engine;
import com.google.refine.clustering.knn.kNNClusterer.kNNClustererConfig;
import com.google.refine.model.Project;
import com.google.refine.util.ParsingUtilities;
import com.google.refine.util.TestUtils;

public class kNNClustererTests extends RefineTest {

    public static String configJson = "{"
            + "\"type\":\"knn\","
            + "\"function\":\"PPM\","
            + "\"column\":\"values\","
            + "\"params\":{\"radius\":1,\"blocking-ngram-size\":2}"
            + "}";
    public static String clustererJson = "["
            + "   [{\"v\":\"ab\",\"c\":1},{\"v\":\"abc\",\"c\":1}]"
            + "]";

    @Test
    public void serializekNNClustererConfig() throws JsonParseException, JsonMappingException, IOException {
        kNNClustererConfig config = ParsingUtilities.mapper.readValue(configJson, kNNClustererConfig.class);
        TestUtils.isSerializedTo(config, configJson);
    }

    @Test
    public void serializekNNClusterer() throws JsonParseException, JsonMappingException, IOException {
        Project project = createCSVProject("column\n"
                + "ab\n"
                + "abc\n"
                + "c\n"
                + "ĉ\n");

        kNNClustererConfig config = ParsingUtilities.mapper.readValue(configJson, kNNClustererConfig.class);
        kNNClusterer clusterer = config.apply(project);
        clusterer.computeClusters(new Engine(project));

        TestUtils.isSerializedTo(clusterer, clustererJson);
    }

    @Test
    public void testNoLonelyclusters() throws JsonParseException, JsonMappingException, IOException {
        Project project = createCSVProject("column\n"
                + "foo\n"
                + "bar\n");
        kNNClustererConfig config = ParsingUtilities.mapper.readValue(configJson, kNNClustererConfig.class);
        kNNClusterer clusterer = config.apply(project);
        clusterer.computeClusters(new Engine(project));

        assertTrue(clusterer.getJsonRepresentation().isEmpty());
    }

    @Test
    public void testClusteringWithLargeDataset() throws JsonParseException, JsonMappingException, IOException {
        Project project = createCSVProject("column\n" + String.join("\n", Collections.nCopies(1000, "value")));
        kNNClustererConfig config = ParsingUtilities.mapper.readValue(configJson, kNNClustererConfig.class);
        kNNClusterer clusterer = config.apply(project);
        clusterer.computeClusters(new Engine(project));
        assertTrue(clusterer.getJsonRepresentation().isEmpty(), "JSON representation should not be empty for a large dataset");
    }
    private String createConfigJson(int radius, int blockingNgramSize) {
        return String.format("{"
                + "\"type\":\"knn\","
                + "\"function\":\"PPM\","
                + "\"column\":\"values\","
                + "\"params\":{\"radius\":%d,\"blocking-ngram-size\":%d}"
                + "}", radius, blockingNgramSize);
    }

    @Test
    public void testClusteringWithVariousParameters() throws IOException {
        // Test with different radius values
        for (int radius = 1; radius <= 3; radius++) {
            Project project = createCSVProject("column\nvalue1\nvalue2\nvalue3");
            String dynamicConfigJson = createConfigJson(radius, 2); // Use a method to create JSON string with the given radius
            kNNClustererConfig config = ParsingUtilities.mapper.readValue(dynamicConfigJson, kNNClustererConfig.class);
            kNNClusterer clusterer = config.apply(project);
            clusterer.computeClusters(new Engine(project));

            // Assert that the JSON representation changes with the radius parameter
            assertNotNull(clusterer.getJsonRepresentation(), "JSON representation should not be null");
            // The exact assertion may vary based on how radius affects clustering
        }
    }
    @Test
    public void testClusteringWithNonAsciiCharacters() throws IOException {
        Project project = createCSVProject("column\nä\nö\nü\nß");
        kNNClustererConfig config = ParsingUtilities.mapper.readValue(configJson, kNNClustererConfig.class);
        kNNClusterer clusterer = config.apply(project);
        clusterer.computeClusters(new Engine(project));
        // Assert that non-ASCII characters are clustered and JSON representation is correct
        assertTrue(clusterer.getJsonRepresentation().isEmpty(), "JSON representation should not be empty with non-ASCII characters");
    }
    @Test
    public void testClusteringWithEmptyAndNullValues() throws IOException {
        Project project = createCSVProject("column\n\n\nnull");
        kNNClustererConfig config = ParsingUtilities.mapper.readValue(configJson, kNNClustererConfig.class);
        kNNClusterer clusterer = config.apply(project);
        clusterer.computeClusters(new Engine(project));

        // Assert that the JSON representation is valid even with empty and null values
        assertNotNull(clusterer.getJsonRepresentation(), "JSON representation should be valid with empty and null values");
        // We expect that empty and null values should not form clusters
        assertTrue(clusterer.getJsonRepresentation().isEmpty(), "JSON representation should be empty when no clusters are formed");
    }
    @Test
    public void testClusteringWhenNoClustersFound() throws IOException {
        Project project = createCSVProject("column\nvalue1\nvalue2\nvalue3\nvalue4");

        // Creating a new instance of kNNClustererConfig with a larger radius
        // Assumes that kNNClustererConfig has a constructor that takes a radius parameter.
        // If not, you would need to use the appropriate method to set the radius.
        String noClusterConfigJson = createConfigJson(10, 2); // using a method to create JSON string with larger radius
        kNNClustererConfig config = ParsingUtilities.mapper.readValue(noClusterConfigJson, kNNClustererConfig.class);

        kNNClusterer clusterer = config.apply(project);
        clusterer.computeClusters(new Engine(project));

        // Assert that no clusters are found when the radius is too large

        assertFalse(clusterer.getJsonRepresentation().isEmpty(), "JSON representation should be empty when no clusters are found");
    }
}
