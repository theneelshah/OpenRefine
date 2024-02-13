package com.google.refine.clustering.binning;

import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.google.refine.RefineTest;

public class FunctionalTests extends RefineTest {
    private static Keyer keyer;

    @Override
    @BeforeTest
    public void init() {
        logger = LoggerFactory.getLogger(this.getClass());
    }

    @BeforeMethod
    public void SetUp() {
        keyer = new FingerprintKeyer();
    }

    @AfterMethod
    public void TearDown() {
        keyer = null;
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testInvalidParams() {
        keyer.key("test", (Object[]) new String[] { "foo" });
    }

    // Custom Tests
    @Test
    public void testNonEnglishAlphabets() {
        Assert.assertEquals(keyer.key("Héllô Wôrld"), "hello world");
        // System.out.println(keyer.key("Héllô Wôrld"));
    }

    @Test
    public void testConvertToLowerCase() {
        Assert.assertEquals(keyer.key("HELLO WORLD"), "hello world");
        // System.out.println(keyer.key("HELLO WORLD"));
    }

}
