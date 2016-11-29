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

package org.wso2.carbon.apimgt.client.common.configs;

import javax.xml.bind.annotation.XmlElement;

public class ClientProfileConfig {

    private String clientName;
    private String callbackUrl;
    private String tokenScope;
    private String owner;
    private String grantType;
    private String saasApp;

    @XmlElement(name = "clientName", required = true)
    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    @XmlElement(name = "callbackUrl")
    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    @XmlElement(name = "tokenScope")
    public String getTokenScope() {
        return tokenScope;
    }

    public void setTokenScope(String tokenScope) {
        this.tokenScope = tokenScope;
    }

    @XmlElement(name = "owner")
    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    @XmlElement(name = "grantType", required = true)
    public String getGrantType() {
        return grantType;
    }

    public void setGrantType(String grantTypem) {
        this.grantType = grantTypem;
    }

    @XmlElement(name = "saasApp")
    public String isSaasApp() {
        return saasApp;
    }

    public void setSaasApp(String saasApp) {
        this.saasApp = saasApp;
    }

}
