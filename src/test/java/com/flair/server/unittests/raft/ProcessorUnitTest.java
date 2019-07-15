package com.flair.server.unittests.raft;

import com.flair.server.raft.Processor;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import type.UnitTest;

@Category(UnitTest.class)
public class ProcessorUnitTest
{
    private Processor processor;
    private String goodText;

    @Before
    public void setUp()
    {
        processor = null;
        goodText = "هذه جملة اختبار للمعالج العربي. سيقوم المعالج الطوافة بمعالجة هذه الجملة.";
    }
    @Test 
    public void testNullConstruction()
    {
       new Processor((String) null);
    }
    @Test 
    public void testEmptyConstruction()
    {
       new Processor("");
    }
    @Test
    public void testShortConstruction()
    {
        new Processor("a");
    }
    @Test
    public void testBadConstruction()
    {
        new Processor("!(*!^@(^)(#{][]|}{;';::,.,./   ( ͡° ͜ʖ ͡°)");
    }
    @Test
    public void testNormalConstruction()
    {
        new Processor(goodText);
    }
    /* @Test
    public void testGetTagContents()
    {

    } */

}