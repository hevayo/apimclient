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

package org.wso2.carbon.apimgt.client.helpers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.client.DcrClient;
import org.wso2.carbon.apimgt.client.StoreClient;
import org.wso2.carbon.apimgt.client.common.APIMClientException;
import org.wso2.carbon.apimgt.client.common.configs.APIMConfig;
import org.wso2.carbon.apimgt.store.client.model.*;

import java.util.List;


public class StoreClientHelper {
    private static final Log log = LogFactory.getLog(StoreClientHelper.class);
    private StoreClient internalStoreClient;
    private APIMConfig config;
    private DcrClient dcrClient;

    /**
     * Initialize APIMIntegration client with a APIMConfig provided as the config
     *
     * @param config - An instance of APIMConfig, see apim-integration.xml
     * @throws APIManagementException
     */
    public StoreClientHelper(APIMConfig config) throws APIManagementException {
        if (config == null) {
            throw new APIManagementException("APIM config should not be null, see apim-ntegration.xml");
        }
        this.config = config;
        this.internalStoreClient = new StoreClient(config);
        this.dcrClient = new DcrClient(config);
    }

    private static ApplicationInfo getExistingApp(Application application, List<ApplicationInfo> appList) {
        for (ApplicationInfo app : appList) {
            if (application.getName().equals(app.getName())) {
                return app;
            }
        }
        return null;
    }

    private static ApplicationKey getProductionKeyIfExists(Application apimApp) {
        if (apimApp.getKeys().isEmpty()) {
            return null;
        } else {
            List<ApplicationKey> appKeys = apimApp.getKeys();
            for (ApplicationKey key : appKeys) {
                if (key.getKeyType().equals(ApplicationKey.KeyTypeEnum.PRODUCTION)) {
                    return key;
                }
            }
            return null;
        }
    }

    private static Subscription getExistingSubscription(SubscriptionList existingSubscriptions, Subscription subscription) {
        for (Subscription apiSubscription : existingSubscriptions.getList()) {
            if (apiSubscription.getApplicationId().equals(subscription.getApplicationId())) {
                return apiSubscription;
            }
        }
        return null;
    }

    //TODO to be removed once apim fix is avaialable
    public void setDcrClient(DcrClient dcrClient) {
        this.dcrClient = dcrClient;
    }

    /**
     * Get a list of of available APIs (from WSO2-APIM) qualifying under a given search query.
     *
     * @param searchQuery - You can search in attributes by using an ":" modifier. Eg. "tag:wso2" will match an API if the tag of the API is "wso2".
     * @return - An instance of SubscriptionListDTO, which contains the list of apis
     * @throws APIMClientException - If fails to invoke the operation due to wrong credentials or any other issue
     */
    public APIList searchStoreAPIs(String tenant, String searchQuery) throws APIMClientException {
        APIList result = null;
        try {
            result = internalStoreClient.getAvailableStoreApis(100, 0, tenant, searchQuery);
        } catch (APIMClientException e) {
            if (e.getResponseStatus() == 401) {
                dcrClient.getRenewedToken();
                result = internalStoreClient.getAvailableStoreApis(100, 0, tenant, searchQuery);
            } else {
                throw e;
            }
        }
        return result;
    }

    /**
     * Creates a application in WSO2-APIM, which then can be used to subscribe api's
     *
     * @param requestApp - An instance of APIMApplicationDTO, which carries the information of application creation request
     * @return - An instance of APIMApplicationDTO, which contains the information of the application created.
     * @throws APIMClientException - If fails to invoke the operation due to wrong credentials or any other issue
     */
    public Application createAPIMApplicationIfNotExists(Application requestApp) throws APIMClientException {

        Application resultApp = null;
        ApplicationList appList = searchAPIMApplications();
        log.info("API application listing successfull appList.getApplicationId() = " + appList.getCount());
        List<ApplicationInfo> apimAppList = appList.getList();
        ApplicationInfo existingApp = getExistingApp(requestApp, apimAppList);
        if (existingApp != null) {
            System.out.println("Store app found therefore not creating " + requestApp.getName());
            resultApp = getAPIMApplicationDetails(existingApp.getApplicationId());
        } else {
            resultApp = createAPIMApplication(requestApp);
        }
        return resultApp;
    }

    /**
     * Subscirbe an API to an Application providing the id of the API and the application.
     *
     * @param subs - An instance of SubscriptionDTO which contains the API id and the Application id.
     * @return - An instance of SubscriptionDTO, which contains the information of the subscription details.
     * @throws APIMClientException - If fails to invoke the operation due to wrong credentials or any other issue
     */
    public Subscription subscribeAPItoAppIfNotExists(Subscription subs) throws APIMClientException {
        Subscription result = null;
        SubscriptionList existingSubscriptions = getExistingSubscriptions(subs.getApiIdentifier(), subs.getApplicationId());
        Subscription availableSubscription = getExistingSubscription(existingSubscriptions, subs);
        if (availableSubscription != null) {
            result = availableSubscription;
            log.info("API subscription already exists for apiID " + subs.getApiIdentifier() + " and appID " + subs.getApplicationId());
            log.info("API subscription already exists availableSubscription.getSubscriptionId() = " + availableSubscription.getSubscriptionId());
        } else {
            result = subscribeAPItoApp(subs);
            System.out.println("API getSubscriptionId successfull subscriptionResult.getSubscriptionId() = " + result.getSubscriptionId());
        }
        return result;
    }

    /**
     * Generates keys (consumerKey, consumerSecret, accessToken) for an Application specified by the applicationId.
     *
     * @param keygenRequest - An instance of ApplicationKeyGenRequestDTO which contains information of key generation request.
     * @return - An instance of ApplicationKeyDTO, which contains the information of keys(consumerKey, consumerSecret, accessToken).
     * @throws APIMClientException - If fails to invoke the operation due to wrong credentials or any other issue
     */
    public ApplicationKey generateKeysforAppIfNotExists(ApplicationKeyGenerateRequest keygenRequest, Application apimApp) throws APIMClientException {
        ApplicationKey applicationKey = getProductionKeyIfExists(apimApp);
        if (applicationKey != null) {
            System.out.println("A PRODCUTION applicationKey is already exists therefore not cretating keys, applicationKey.getConsumerKey and secret " + applicationKey.getConsumerKey() + "  " + applicationKey.getConsumerSecret());
        } else {
            applicationKey = generateKeysforApp(keygenRequest, apimApp.getApplicationId());
            System.out.println("API applicationKey gen OK, applicationKey.getConsumerKey and secret " + applicationKey.getConsumerKey() + "  " + applicationKey.getConsumerSecret());
            System.out.println("API applicationKey generation successfull applicationKey.getToken().toString() = " + applicationKey.getToken().toString());
        }
        return applicationKey;
    }

    /**
     * Searches available applications in WSO2-APIM, which then can be used to subscribe api's
     *
     * @return - An instance of APIMApplicationListDTO, which contains a list of APIMApplicationDTO objects corresponds to existing apllications.
     * @throws APIMClientException - If fails to invoke the operation due to wrong credentials or any other issue
     */
    private ApplicationList searchAPIMApplications() throws APIMClientException {

        ApplicationList result = null;
        try {
            result = internalStoreClient.getAvailableApplications("", "", 100, 0);
        } catch (APIMClientException e) {
            if (e.getResponseStatus() == 401) {
                dcrClient.getRenewedToken();
                result = internalStoreClient.getAvailableApplications("", "", 100, 0);
            } else {
                throw e;
            }
        }
        return result;
    }

    /**
     * Gets the details of APIM application (specified by the appId) in WSO2-APIM.
     *
     * @param appId - The unique identifier of the application
     * @return - An instance of APIMApplicationDTO, which contains the details of application corresponds to appId.
     * @throws APIMClientException - If fails to invoke the operation due to wrong credentials or any other issue
     */
    private Application getAPIMApplicationDetails(String appId) throws APIMClientException {

        Application resultApp = null;
        try {
            resultApp = internalStoreClient.getApplicationDetails(appId);
        } catch (APIMClientException e) {
            if (e.getResponseStatus() == 401) {
                dcrClient.getRenewedToken();
                resultApp = internalStoreClient.getApplicationDetails(appId);
            } else {
                throw e;
            }
        }
        return resultApp;
    }

    /**
     * Creates a application in WSO2-APIM, which then can be used to subscribe api's
     *
     * @param requestApp - An instance of APIMApplicationDTO, which carries the information of application creation request
     * @return - An instance of APIMApplicationDTO, which contains the information of the application created.
     * @throws APIMClientException - If fails to invoke the operation due to wrong credentials or any other issue
     */
    private Application createAPIMApplication(Application requestApp) throws APIMClientException {

        Application result = null;
        try {
            result = internalStoreClient.createApplication(requestApp);
        } catch (APIMClientException e) {
            if (e.getResponseStatus() == 401) {
                dcrClient.getRenewedToken();
                result = internalStoreClient.createApplication(requestApp);
            } else {
                throw e;
            }
        }
        return result;
    }

    /**
     * Gets existing subscriptions for a api (specified by apiId).
     *
     * @return - An instance of SubscriptionListDTO, which contains the list of subscriptions of the requested api.
     * @throws APIMClientException - If fails to invoke the operation due to wrong credentials or any other issue
     */
    private SubscriptionList getExistingSubscriptions(String apiId, String applicationId) throws APIMClientException {
        SubscriptionList result = null;
        try {
            result = internalStoreClient.getSusbscriptions(apiId, applicationId, "", 0, 100);
        } catch (APIMClientException e) {
            if (e.getResponseStatus() == 401) {
                dcrClient.getRenewedToken();
                result = internalStoreClient.getSusbscriptions(apiId, applicationId, "", 0, 100);
            } else {
                throw e;
            }
        }
        return result;
    }

    /**
     * Subscirbe an API to an Application providing the id of the API and the application.
     *
     * @param subscriptionRequest - An instance of SubscriptionDTO which contains the API id and the Application id.
     * @return - An instance of SubscriptionDTO, which contains the information of the subscription details.
     * @throws APIMClientException - If fails to invoke the operation due to wrong credentials or any other issue
     */
    private Subscription subscribeAPItoApp(Subscription subscriptionRequest) throws APIMClientException {
        Subscription result = null;
        try {
            result = internalStoreClient.addNewSubscription(subscriptionRequest);
        } catch (APIMClientException e) {
            if (e.getResponseStatus() == 401) {
                dcrClient.getRenewedToken();
                result = internalStoreClient.addNewSubscription(subscriptionRequest);
            } else {
                throw e;
            }
        }
        return result;
    }

    /**
     * Generates keys (consumerKey, consumerSecret, accessToken) for an Application specified by the applicationId.
     *
     * @param keygenRequest - An instance of ApplicationKeyGenRequestDTO which contains information of key generation request.
     * @param applicationId - Unique Id of the APIM application (which needs to generate the keys)
     * @return - An instance of ApplicationKeyDTO, which contains the information of keys(consumerKey, consumerSecret, accessToken).
     * @throws APIMClientException - If fails to invoke the operation due to wrong credentials or any other issue
     */
    private ApplicationKey generateKeysforApp(ApplicationKeyGenerateRequest keygenRequest, String applicationId) throws APIMClientException {
        ApplicationKey result = null;
        try {
            result = internalStoreClient.generateKeysForApplication(applicationId, keygenRequest);
        } catch (APIMClientException e) {
            if (e.getResponseStatus() == 401) {
                dcrClient.getRenewedToken();
                result = internalStoreClient.generateKeysForApplication(applicationId, keygenRequest);
            } else {
                throw e;
            }
        }
        return result;
    }


}
