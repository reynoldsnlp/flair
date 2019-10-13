package com.integrationtests.raft;

import com.flair.server.raft.Weka;

import org.junit.Before;
import org.junit.experimental.categories.Category;

import type.IntegrationTest;

@Category(IntegrationTest.class)

public class WekaIntegrationTest 
{
    private Weka weka;
    private String model;
    private String featureData; 

    //@Before
    public void setUp() 
    {
        featureData = "13.578947368421053,6.1976744186046515,0.4728682170542636,0.0,0.0,3081347,479479.4516129032,45739.0,0.49224806201550386,0.007751937984496124,0.1434108527131783,0.1937984496124031,0.007751937984496124,0.03875968992248062,0.007751937984496124,0.10852713178294573,1.0";
        model = "model.arff";
        weka = new Weka(model);
    }
}