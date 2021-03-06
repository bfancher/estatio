/*
 *
 *  Copyright 2012-2014 Eurocommercial Properties NV
 *
 *
 *  Licensed under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.estatio.integtests;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import javax.inject.Inject;

import org.apache.log4j.Level;
import org.apache.log4j.PropertyConfigurator;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.core.integtestsupport.IntegrationTestAbstract2;
import org.apache.isis.core.integtestsupport.IsisSystemForTest;
import org.apache.isis.core.integtestsupport.scenarios.ScenarioExecutionForIntegration;
import org.apache.isis.core.security.authentication.AuthenticationRequestNameOnly;

import org.isisaddons.module.command.dom.BackgroundCommandServiceJdoRepository;

import org.estatio.app.EstatioAppManifest;
import org.estatio.integtests.fakes.EstatioIntegTestFakeServicesModule;

/**
 * Base class for integration tests.
 */
public abstract class EstatioIntegrationTest extends IntegrationTestAbstract2 {

    private static final Logger LOG = LoggerFactory.getLogger(EstatioIntegrationTest.class);

    @BeforeClass
    public static void initClass() {
        PropertyConfigurator.configure("logging-integtest.properties");

        LOG.info("Starting tests");

        IsisSystemForTest isft = IsisSystemForTest.getElseNull();
        if (isft == null) {
            isft = new IsisSystemForTest.Builder()
                    .withLoggingAt(Level.WARN)
                    .with(new AuthenticationRequestNameOnly("estatio-admin"))
                    .with(new EstatioAppManifest(
                            Collections.emptyList(),
                            null,
                            Arrays.asList(EstatioIntegTestFakeServicesModule.class)
                    ) {
                        @Override
                        public Map<String, String> getConfigurationProperties() {
                            Map<String, String> props = super.getConfigurationProperties();
                            Util.withIsisIntegTestProperties(props);
                            Util.withJavaxJdoRunInMemoryProperties(props);
                            Util.withDataNucleusProperties(props);
                            return props;
                        }
                    })
                    .build()
                    .setUpSystem();
            IsisSystemForTest.set(isft);
        }

        // instantiating will install onto ThreadLocal
        new ScenarioExecutionForIntegration();
    }

    @Inject
    protected RunBackgroundCommandsService runBackgroundCommandsService;

    @Inject
    protected BackgroundCommandServiceJdoRepository backgroundCommandRepository;

    @Inject
    protected TransactionService transactionService;


}

