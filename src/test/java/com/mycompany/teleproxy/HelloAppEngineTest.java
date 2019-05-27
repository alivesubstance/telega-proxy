/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.teleproxy;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 *
 * @author adast
 */
public class HelloAppEngineTest {

    @Test
    public void testKeyFound() {
        String key = TeleProxyServlet.findBotKey("/bot754871252:AAGNFauixcVTvMMSnQnbQzfcNTh5LnQIw-0/getupdates");
        assertEquals("754871252:AAGNFauixcVTvMMSnQnbQzfcNTh5LnQIw-0", key);
    }

    @Test
    public void testKeyNotFound() {
        String key = TeleProxyServlet.findBotKey("/adsasdbot754871252:AAGNFauixcVTvMMSnQnbQzfcNTh5LnQIw-0/getupdates");
        assertNull(key);
    }
    
}
