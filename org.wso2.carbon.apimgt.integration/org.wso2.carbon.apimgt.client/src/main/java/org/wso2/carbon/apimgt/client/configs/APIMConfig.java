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

package org.wso2.carbon.apimgt.client.configs;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "APIMConfiguration")
public class APIMConfig {

    DCREndpointConfig dcrEndpointConfig;
    TokenEndpointConfig tokenEndpointConfig;
    PublisherEndpointConfig publisherEndpoint;
    StoreEndpointConfig storeEndpoint;

    @XmlElement(name = "DCREndpoint", required = true)
    public DCREndpointConfig getDcrEndpointConfig() {
        return dcrEndpointConfig;
    }

    public void setDcrEndpointConfig(DCREndpointConfig dcrEndpointConfig) {
        this.dcrEndpointConfig = dcrEndpointConfig;
    }

    @XmlElement(name = "TokenEndpoint", required = true)
    public TokenEndpointConfig getTokenEndpointConfig() {
        return tokenEndpointConfig;
    }

    public void setTokenEndpointConfig(TokenEndpointConfig tokenEndpointConfig) {
        this.tokenEndpointConfig = tokenEndpointConfig;
    }

    @XmlElement(name = "PublisherEndpoint", required = true)
    public PublisherEndpointConfig getPublisherEndpointConfig() {
        return publisherEndpoint;
    }

    public void setPublisherEndpointConfig(PublisherEndpointConfig publisherEndpoint) {
        this.publisherEndpoint = publisherEndpoint;
    }

    @XmlElement(name = "StoreEndpoint", required = true)
    public StoreEndpointConfig getStoreEndpointConfig() {
        return storeEndpoint;
    }

    public void setStoreEndpointConfig(StoreEndpointConfig storeEndpoint) {
        this.storeEndpoint = storeEndpoint;
    }

}
