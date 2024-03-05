/*

Copyright 2010, Google Inc.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:

    * Redistributions of source code must retain the above copyright
notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above
copyright notice, this list of conditions and the following disclaimer
in the documentation and/or other materials provided with the
distribution.
    * Neither the name of Google Inc. nor the names of its
contributors may be used to endorse or promote products derived from
this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,           
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY           
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*/

package com.google.refine.operations.cell;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.Properties;

import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.google.refine.RefineTest;
import com.google.refine.model.Row;
import com.google.refine.history.HistoryEntry;
import com.google.refine.model.AbstractOperation;
import com.google.refine.model.Column;
import com.google.refine.model.ColumnModel;
import com.google.refine.model.Project;
import com.google.refine.operations.OperationRegistry;
import com.google.refine.process.Process;

public class JoinMultiValuedCellsTests extends RefineTest {

    Project project;

    @Override
    @BeforeTest
    public void init() {
        logger = LoggerFactory.getLogger(this.getClass());
    }

    @BeforeSuite
    public void registerOperation() {
        OperationRegistry.registerOperation(getCoreModule(), "multivalued-cell-join",
                MultiValuedCellJoinOperation.class);
    }

    @BeforeMethod
    public void createProject() {
        project = createCSVProject(
                "Key,Value\n"
                        + "Record_1,one\n"
                        + ",two\n"
                        + ",three\n"
                        + ",four\n");
    }

    /*
     * Test to demonstrate the intended behaviour of the function
     */

    @Test
    public void testJoinMultiValuedCells() throws Exception {
        AbstractOperation op = new MultiValuedCellJoinOperation(
                "Value",
                "Key",
                ",");
        Process process = op.createProcess(project, new Properties());
        process.performImmediate();

        int keyCol = project.columnModel.getColumnByName("Key").getCellIndex();
        int valueCol = project.columnModel.getColumnByName("Value").getCellIndex();

        Assert.assertEquals(project.rows.get(0).getCellValue(keyCol), "Record_1");
        Assert.assertEquals(project.rows.get(0).getCellValue(valueCol), "one,two,three,four");
    }

    @Test
    public void testJoinMultiValuedCellsMultipleSpaces() throws Exception {
        AbstractOperation op = new MultiValuedCellJoinOperation(
                "Value",
                "Key",
                ",     ,");
        Process process = op.createProcess(project, new Properties());
        process.performImmediate();

        int keyCol = project.columnModel.getColumnByName("Key").getCellIndex();
        int valueCol = project.columnModel.getColumnByName("Value").getCellIndex();

        Assert.assertEquals(project.rows.get(0).getCellValue(keyCol), "Record_1");
        Assert.assertEquals(project.rows.get(0).getCellValue(valueCol), "one,     ,two,     ,three,     ,four");
    }

    @Test
    public void testConstructorOverload() {
        MultiValuedCellJoinOperation operation = new MultiValuedCellJoinOperation("Value", "Key", ",", null);

        assertNotNull(operation);
    }

    @Test
    public void testProjectField() {
        // Setup
        Project project = new Project();
        MultiValuedCellJoinOperation operation = new MultiValuedCellJoinOperation("Value", "Key", ",", project);

        // Exercise
        Project storedProject = operation.getProject();

        // Verify
        assertEquals(storedProject, project);
    }

    @Test
    public void testExtractedMethod() {

        MultiValuedCellJoinOperation operation = new MultiValuedCellJoinOperation("Value", "Key", ",", null);
        Project project = new Project();

        List<Row> newRows = operation.generateNewRows(project, 0, 1);

        assertEquals(newRows.size(), 0); // Assuming the logic would not add any rows without valid data
    }

    @Test
    public void testCreateHistoryEntry() throws Exception {
        Project project = new Project();
        ColumnModel columnModel = project.columnModel;
        Column valueColumn = new Column(0, "Value");
        Column keyColumn = new Column(1, "Key");

        columnModel.addColumn(0, valueColumn, true);
        columnModel.addColumn(1, keyColumn, true);

        MultiValuedCellJoinOperation operation = new MultiValuedCellJoinOperation("Value", "Key", ",", project);

        HistoryEntry historyEntry = operation.createHistoryEntry(1L);

        assertNotNull(historyEntry);
    }
}
