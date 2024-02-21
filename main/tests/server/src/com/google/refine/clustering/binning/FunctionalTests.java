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
    }

    @Test
    public void testConvertToLowerCase() {
        Assert.assertEquals(keyer.key("HELLO WORLD"), "hello world");
    }

    @Test
    public void testFixWhiteSpaces() {
        Assert.assertEquals(keyer.key("He        llo"), "he llo");
    }

    @Test
    public void testNGramTest() {
        keyer = new NGramFingerprintKeyer();
        Assert.assertEquals(keyer.key("banana", 3), "anabannan");
    }
    @Test
    public void testIgnorePunctuation() {
        Assert.assertEquals(keyer.key("Hello, How are you?!"), "are hello how you");
    }

    @Test
    public void testSortAlphabetically() {
        Assert.assertEquals(keyer.key("xyz abc pqr"), "abc pqr xyz");
}
}
