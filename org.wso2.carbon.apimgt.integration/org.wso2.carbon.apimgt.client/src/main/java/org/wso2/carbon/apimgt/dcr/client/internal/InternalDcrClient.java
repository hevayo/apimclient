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

package org.wso2.carbon.apimgt.dcr.client.internal;

import feign.Feign;
import feign.auth.BasicAuthRequestInterceptor;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.jaxrs.JAXRSContract;
import org.wso2.carbon.apimgt.client.APIMClientException;
import org.wso2.carbon.apimgt.client.APIMErrorDecoder;
import org.wso2.carbon.apimgt.client.internal.TrustedFeignClient;
import org.wso2.carbon.apimgt.client.configs.ClientProfileConfig;
import org.wso2.carbon.apimgt.client.configs.DCREndpointConfig;
import org.wso2.carbon.apimgt.client.configs.TokenConfig;
import org.wso2.carbon.apimgt.client.configs.TokenEndpointConfig;
import org.wso2.carbon.apimgt.dcr.client.model.ClientProfileDTO;
import org.wso2.carbon.apimgt.dcr.client.model.OAuthApplicationDTO;
import org.wso2.carbon.apimgt.dcr.client.model.TokenDTO;

public class InternalDcrClient {


    public OAuthApplicationDTO createOAuthApplication(DCREndpointConfig dcrConfig) throws APIMClientException {

        DcrClientInterface clientInterface = getFeignClient(dcrConfig.getUrl(), dcrConfig.getUserName(), dcrConfig.getPassword());
        ClientProfileDTO clientProfile = getDcrClinetProfile(dcrConfig);
        return clientInterface.register(clientProfile);
    }

    public TokenDTO getUserToken(TokenEndpointConfig tokenConfig, OAuthApplicationDTO oAuthApplication)
            throws APIMClientException {

        DcrClientInterface clientInterface = getFeignClient(tokenConfig.getUrl(), oAuthApplication.getClientId(), oAuthApplication.getClientSecret());
        TokenConfig tokenInfo = tokenConfig.getTokenInfo();
        return clientInterface.requestToken(tokenInfo.getGrantType(), tokenInfo.getUserName(), tokenInfo.getPassword(), tokenInfo.getScope());
    }

    public TokenDTO renewUserToken(TokenEndpointConfig tokenConfig, OAuthApplicationDTO oAuthApplication,
                                   String refreshToken) throws APIMClientException {

        DcrClientInterface clientInterface = getFeignClient(tokenConfig.getUrl(), oAuthApplication.getClientId(), oAuthApplication.getClientSecret());
        TokenConfig tokenInfo = tokenConfig.getTokenInfo();
        return clientInterface.requestTokenRenew("refresh_token", refreshToken, tokenInfo.getScope());
    }


    private DcrClientInterface getFeignClient(String url, String userName, String password) {
        DcrClientInterface dynamicClientRegistrationService = Feign.builder()
                .client(new TrustedFeignClient())
                .contract(new JAXRSContract())
                .encoder(new GsonEncoder())
                .decoder(new GsonDecoder())
                .errorDecoder(new APIMErrorDecoder())
                .requestInterceptor(new BasicAuthRequestInterceptor(userName, password))
                .target(DcrClientInterface.class, url);
        return dynamicClientRegistrationService;
    }

    private ClientProfileDTO getDcrClinetProfile(DCREndpointConfig dcrConfig) {
        ClientProfileConfig clientConfig = dcrConfig.getClientProfile();
        ClientProfileDTO clientProfile = new ClientProfileDTO();
        clientProfile.setCallbackUrl(clientConfig.getCallbackUrl());
        clientProfile.setClientName(clientConfig.getClientName());
        clientProfile.setGrantType(clientConfig.getGrantType());
        clientProfile.setOwner(clientConfig.getOwner());
        clientProfile.setSaasApp(clientConfig.isSaasApp());
        clientProfile.setTokenScope(clientConfig.getTokenScope());
        return clientProfile;
    }

}
