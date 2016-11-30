/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.client.configs.APIMConfig;
import org.wso2.carbon.apimgt.dcr.client.internal.InternalDcrClient;
import org.wso2.carbon.apimgt.dcr.client.model.OAuthApplicationDTO;
import org.wso2.carbon.apimgt.dcr.client.model.TokenDTO;

public class DcrClient {
    private static final Log log = LogFactory.getLog(DcrClient.class);
    private APIMConfig config;
    private OAuthApplicationDTO dcrApplication;
    private TokenDTO dcrToken;
    private InternalDcrClient internalDcrClient;

    /**
     * Initialize APIMDcrClient with a APIMConfig and APIMClient provided as the config
     *
     * @param config - An instance of APIMConfig, see apim-integration.xml
     * @param config - An instance of APIMClient
     * @throws APIMClientException
     */
    public DcrClient(APIMConfig config) throws APIMClientException {
        log.debug("Initializing APIMDcrClient");
        if (config == null) {
            throw new APIMClientException("APIM config should not be null, see apim-ntegration.xml");
        }
        this.config = config;
        internalDcrClient = new InternalDcrClient();
    }

    /**
     * Gets a user token from WSO2-APIM, passing the client-application as an input
     *
     * @return - The access token
     * @throws APIMClientException - If fails to invoke the operation due to wrong credentials or any other issue
     */
    public String getToken() throws APIMClientException {
        log.debug("getToken() invoked");
        if (dcrToken == null) {
            if (dcrApplication == null) {
                log.debug("dcrApplication is null, therefore creating a new one");
                dcrApplication = internalDcrClient.createOAuthApplication(config.getDcrEndpointConfig());
            }
            log.debug("dcrToken is null, therefore creating a new one");
            dcrToken = internalDcrClient.getUserToken(config.getTokenEndpointConfig(), dcrApplication);
        }
        return dcrToken.getAccess_token();
    }

    /**
     * Gets a user token from WSO2-APIM, passing the client-application as an input
     *
     * @return - The access token
     * @throws APIMClientException - If fails to invoke the operation due to wrong credentials or any other issue
     */
    public String getRenewedToken() throws APIMClientException {
        log.debug("getRenewedToken() invoked");
        if (dcrApplication == null) {
            log.debug("dcrApplication is null, therefore creating a new one");
            dcrApplication = internalDcrClient.createOAuthApplication(config.getDcrEndpointConfig());
        }
        dcrToken = internalDcrClient.renewUserToken(config.getTokenEndpointConfig(), dcrApplication, dcrToken.getRefresh_token());
        return dcrToken.getAccess_token();
    }

}
