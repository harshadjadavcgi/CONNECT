/*
 * Copyright (c) 2012, United States Government, as represented by the Secretary of Health and Human Services.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above
 *       copyright notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 *     * Neither the name of the United States Government nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE UNITED STATES GOVERNMENT BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package gov.hhs.fha.nhinc.direct;

import static gov.hhs.fha.nhinc.direct.DirectUnitTestUtil.removeSmtpAgentConfig;
import static gov.hhs.fha.nhinc.direct.DirectUnitTestUtil.writeSmtpAgentConfig;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Test {@link DirectMailClient} with Spring Framework Configuration.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/test.direct.appcontext.xml")
public class DirectMailClientSpringTest {
    
    private static final Log LOG = LogFactory.getLog(DirectMailClientSpringTest.class);
    
    @Autowired
    private DirectMailClient intDirectMailClient;

    /**
     * @return DirectMailClient for internal mail server.
     */
    public DirectMailClient getIntDirectMailClient() {
        return intDirectMailClient;
    }

    /**
     * @return DirectMailClient for external mail server.
     */
    public DirectMailClient getExtDirectMailClient() {
        return extDirectMailClient;
    }

    @Autowired
    private DirectMailClient extDirectMailClient;
    
    @Autowired
    private MessageHandler outboundMessageHandler;

    @Autowired
    private MessageHandler inboundMessageHandler;

    @Autowired
    private ApplicationContext applicationContext;
    
    @Autowired 
    private ThreadPoolTaskScheduler scheduler;    

    // these need to be static so we can shut them down in an AfterClass annotation.
    private static ApplicationContext staticContext;
    private static ThreadPoolTaskScheduler staticScheduler;

    /**
     * Set up keystore for test.
     */
    @BeforeClass
    public static void setUpClass() {
        writeSmtpAgentConfig();
    }

    /**
     * Capture the app context and scheduler so we can shut them down later.
     */
    @Before
    public void setUp() {
        staticContext = applicationContext;
        staticScheduler = scheduler;
    }
    
    /**
     * Tear down keystore created in setup. Cleanup the context and scheduler, so they don't interfere with other
     * tests.
     */
    @AfterClass
    public static void tearDownClass() {        
        removeSmtpAgentConfig();

        if (staticScheduler != null) {            
            LOG.debug("shutting down scheduler");
            staticScheduler.shutdown();   
        }
        if (staticContext != null) {
            LOG.debug("closing context");
            ((AbstractApplicationContext) staticContext).close();
        }
    }    
    
    /**
     * Test that we can get an external mail client with spring.
     */
    @Test
    public void canGetExternalMailClient() {
        assertNotNull(extDirectMailClient);
    }
    
    /**
     * Test that we can get an internal mail client with spring.
     */
    @Test
    public void canGetInternalMailClient() {
        assertNotNull(intDirectMailClient);
    }
    
    /**
     * Test that the two mail clients are distinct instances.
     */
    @Test
    public void canDistinguishInternalExternal() {
        assertNotSame(intDirectMailClient, extDirectMailClient);        
    }
    
    /**
     * Test that we can get an outbound message handler from spring.
     */
    @Test
    public void canGetOutboundMessageHandler() {
        assertNotNull(outboundMessageHandler);
    }
    
    /**
     * Test that we can get an inbound message handler from spring.
     */
    @Test
    public void canGetInboundMessageHandler() {
        assertNotNull(inboundMessageHandler);
    }

    /**
     * Test that we can use spring task scheduler to run the polling mail handlers.
     * @throws InterruptedException on failure.
     */
    @Test
    @Ignore
    public void canRunScheduledTaskEveryOneSec() throws InterruptedException {
        Thread.sleep(DirectUnitTestUtil.WAIT_TIME_FOR_MAIL_HANDLER);
        
        int internalInvocations = intDirectMailClient.getHandlerInvocations();
        int externalInvocations = extDirectMailClient.getHandlerInvocations();
        
        assertTrue(internalInvocations >= 2);
        assertTrue(externalInvocations >= 2);
    }    
}
