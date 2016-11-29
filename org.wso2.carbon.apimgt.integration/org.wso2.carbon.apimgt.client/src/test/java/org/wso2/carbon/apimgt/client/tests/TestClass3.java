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

package org.wso2.carbon.apimgt.client.tests;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.client.PublisherClient;
import org.wso2.carbon.apimgt.client.StoreClient;
import org.wso2.carbon.apimgt.client.common.APIMClientException;
import org.wso2.carbon.apimgt.client.common.APIMConfigReader;
import org.wso2.carbon.apimgt.client.common.configs.APIMConfig;
import org.wso2.carbon.apimgt.publisher.client.model.API;
import org.wso2.carbon.apimgt.publisher.client.model.APIInfo;
import org.wso2.carbon.apimgt.publisher.client.model.APIList;
import org.wso2.carbon.apimgt.store.client.model.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;


public class TestClass3 {

    public static void main(String[] args) {


        System.out.println("Testing org.wso2.carbon.apimgt.apim.integration");
        System.out.println("Reading the config file 'apim-integration.xml'");

        APIMConfig apimConfig = null;
        try {
            apimConfig = APIMConfigReader.getAPIMConfig("src/test/java/apim-integration.xml");

            System.out.println("Config file red sucessfully and apimConfig.getDcrEndpointConfig().getClientProfile().getClientName() = "
                    + apimConfig.getDcrEndpointConfig().getClientProfile().getClientName());

            String fileString = new String(Files.readAllBytes(Paths.get("src/test/java/api6.json")));
            //createNewTestApi(fileString, apimConfig);
            searchStoreAPIsAndApps(apimConfig);

        } catch (APIManagementException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void createNewTestApi(String fileString, APIMConfig apimConfig) {
        PublisherClient apiClient = new PublisherClient(apimConfig);


        API api = new API();
        api.setTags(Arrays.asList("apple"));
        api.setName("Ausi3808");
        api.setContext("/Ausi3808");
        api.setVersion("1.0.1");
        api.setProvider("admin");
        api.setApiDefinition(fileString);
        api.setIsDefaultVersion(false);
        api.setTransport(Arrays.asList("http", "https"));
        api.setTiers(Arrays.asList("Unlimited"));
        api.setVisibility(API.VisibilityEnum.PUBLIC);
        api.setEndpointConfig(
                "{\"production_endpoints\":{\"url\":\"https://localhost:9443/am/sample/pizzashack/v1/api/\",\"config\":null}, \"endpoint_type\":\"http\" }");
        api.setGatewayEnvironments("Production");


        try {

            APIList list = apiClient.getAvailableApis("", 100, 0);
            List<APIInfo> apiLIst = list.getList();
            APIInfo existingAPI = getExistingApi(api, apiLIst);
            API createdAPI = null;
            if (existingAPI != null) {
                System.out.println("API found " + existingAPI.getName());
                createdAPI = apiClient.getApi(existingAPI.getId());
            } else {
                createdAPI = apiClient.createApi(api);
                String id = createdAPI.getId();
                apiClient.changeApiLifecycle("Publish", createdAPI.getId());
            }
        } catch (APIMClientException ee) {

        }
    }

    private static void searchStoreAPIsAndApps(APIMConfig apimConfig) {
        try {
            StoreClient storeClient = new StoreClient(apimConfig);
            org.wso2.carbon.apimgt.store.client.model.APIList availableStoreApis =
                    storeClient.getAvailableStoreApis(100, 0, "carbon.super", "");
            for (org.wso2.carbon.apimgt.store.client.model.APIInfo api : availableStoreApis.getList()) {
                System.out.println("Store API name " + api.getName());
            }

            ApplicationList availableApplications = null;
            try {
                availableApplications = storeClient.getAvailableApplications("", "", 100, 0);
            } catch (APIMClientException e) {
                e.printStackTrace();
            }
            for (ApplicationInfo app : availableApplications.getList()) {
                System.out.println("Store Application name " + app.getName());
            }

            String appName = "AusiApp3901";
            Application requestApp = new Application();
            requestApp.setName(appName);
            requestApp.setThrottlingTier("Unlimited");
            Application resultApp = null;
            ApplicationInfo existingApp = getExistingApp(requestApp, availableApplications.getList());
            if (existingApp != null) {
                System.out.println("Store app found therefore not creating " + requestApp.getName());
                resultApp = storeClient.getApplicationDetails(existingApp.getApplicationId());
            } else {
                resultApp = storeClient.createApplication(requestApp);
            }

            ApplicationKey productionKey = getProductionKeyIfExists(resultApp);
            if (productionKey != null) {
                System.out.println(
                        "A PRODCUTION applicationKey is already exists therefore not cretating keys, applicationKey.getConsumerKey and secret "
                                + productionKey.getConsumerKey() + "  " + productionKey.getConsumerSecret());
            } else {
                ApplicationKeyGenerateRequest keygenRequest = new ApplicationKeyGenerateRequest();
                keygenRequest.setKeyType(ApplicationKeyGenerateRequest.KeyTypeEnum.PRODUCTION);
                keygenRequest.setValidityTime("3600");
                keygenRequest.setAccessAllowDomains(Arrays.asList("ALL"));
                ApplicationKey applicationKey = storeClient.generateKeysForApplication(resultApp.getApplicationId(), keygenRequest);
                System.out.println("API applicationKey gen OK, applicationKey.getConsumerKey and secret "
                        + applicationKey.getConsumerKey() + "  " + applicationKey.getConsumerSecret());
                System.out.println("API applicationKey generation successfull applicationKey.getToken().toString() = "
                        + applicationKey.getToken().toString());
            }

            //5th api object
            org.wso2.carbon.apimgt.store.client.model.APIInfo resultApiObject = availableStoreApis.getList().get(5);
            Subscription subscription = new Subscription();
            subscription.setTier("Unlimited");
            subscription.setApplicationId(resultApp.getApplicationId());
            subscription.setApiIdentifier(resultApiObject.getId());

            SubscriptionList existingSubscriptions = storeClient.getSusbscriptions(resultApiObject.getId(), resultApp.getApplicationId(), "", 0, 100);
            Subscription availableSubscription = getExistingSubscription(existingSubscriptions, subscription);
            if (availableSubscription != null) {
                System.out.println("API subscription already exists for apiID " + resultApiObject.getId()
                        + " and appID " + resultApp.getApplicationId());
                System.out.println("API subscription already exists availableSubscription.getSubscriptionId() = "
                        + availableSubscription.getSubscriptionId());
            } else {
                Subscription subscriptionResult = storeClient.addNewSubscription(subscription);
                System.out.println("API getSubscriptionId successfull subscriptionResult.getSubscriptionId() = "
                        + subscriptionResult.getSubscriptionId());
            }
        } catch (APIMClientException e) {

        }

    }


    private static APIInfo getExistingApi(API apiDTO, List<APIInfo> apiLIst) {
        for (APIInfo api : apiLIst) {
            if (api.getContext().equals(apiDTO.getContext())) {
                return api;
            }
        }
        return null;
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


}
