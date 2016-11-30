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

import org.wso2.carbon.apimgt.client.APIMClientException;
import org.wso2.carbon.apimgt.dcr.client.model.ClientProfileDTO;
import org.wso2.carbon.apimgt.dcr.client.model.OAuthApplicationDTO;
import org.wso2.carbon.apimgt.dcr.client.model.TokenDTO;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/")
public interface DcrClientInterface {

    // DCR APIs
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public OAuthApplicationDTO register(ClientProfileDTO registrationProfile) throws APIMClientException;

    // Token APIs
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public TokenDTO requestToken(@QueryParam("grant_type") String grant, @QueryParam("username") String username, @QueryParam("password") String password,
                                 @QueryParam("scope") String scope) throws APIMClientException;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public TokenDTO requestTokenRenew(@QueryParam("grant_type") String grant, @QueryParam("refresh_token") String refreshToken, @QueryParam("scope") String scope)
            throws APIMClientException;
}
