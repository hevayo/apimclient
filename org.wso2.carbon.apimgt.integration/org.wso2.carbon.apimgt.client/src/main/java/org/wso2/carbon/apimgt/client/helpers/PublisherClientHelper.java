package org.wso2.carbon.apimgt.client.helpers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.client.DcrClient;
import org.wso2.carbon.apimgt.client.PublisherClient;
import org.wso2.carbon.apimgt.client.common.APIMClientException;
import org.wso2.carbon.apimgt.client.common.configs.APIMConfig;
import org.wso2.carbon.apimgt.publisher.client.model.API;
import org.wso2.carbon.apimgt.publisher.client.model.APIInfo;
import org.wso2.carbon.apimgt.publisher.client.model.APIList;

import java.util.List;

public class PublisherClientHelper {
    private static final Log log = LogFactory.getLog(PublisherClientHelper.class);
    private PublisherClient internalPublisherClient;
    private APIMConfig config;
    private DcrClient dcrClient;

    /**
     * Initialize APIMIntegration client with a APIMConfig provided as the config
     *
     * @param config - An instance of APIMConfig, see apim-integration.xml
     * @throws APIManagementException
     */
    public PublisherClientHelper(APIMConfig config) throws APIManagementException {
        if (config == null) {
            throw new APIManagementException("APIM config should not be null, see apim-ntegration.xml");
        }
        this.config = config;
        this.internalPublisherClient = new PublisherClient(config);
        this.dcrClient = new DcrClient(config);
    }

    //TODO to be removed once apim fix is avaialbe
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
    public APIList searchPublisherAPIs(String searchQuery) throws APIMClientException {

        APIList publisherApis = null;
        try {
            publisherApis = internalPublisherClient.getAvailableApis("", 100, 0);
        } catch (APIMClientException e) {
            if (e.getResponseStatus() == 401) {
                dcrClient.getRenewedToken();
                publisherApis = internalPublisherClient.getAvailableApis("", 100, 0);
            } else {
                throw e;
            }
        }
        return publisherApis;
    }

    /**
     * Creates a API in WSO2-APIM
     *
     * @param apiDTO - Instance of APIDTO, which carries the information of api creation request
     * @return - Instance of APIDTO, which contains the information of created API
     * @throws APIMClientException - If fails to invoke the operation due to wrong credentials or any other issue
     */
    public API createAndPublishAPIIfNotExists(API apiDTO) throws APIMClientException {

        APIList publisherApiList = searchPublisherAPIs("");
        List<APIInfo> apiLIst = publisherApiList.getList();
        API existingAPI = getExistingApi(apiDTO, apiLIst);
        if (existingAPI != null) {
            System.out.println("API " + existingAPI.getName() + " apready exists, therefore not creating");
            if ("PUBLISHED".equals(existingAPI.getStatus())) {
                System.out.println("API " + existingAPI.getName() + " apready in PUBLISHED state, therefore not publishing");
            } else {
                boolean publishResult = publishAPI(existingAPI.getId());
                System.out.println("API publish result " + publishResult);
            }
            return existingAPI;
        } else {
            API createdAPI = createAPI(apiDTO);
            System.out.println("API creation successfull createdAPI.getId() = " + createdAPI.getId());
            boolean result = publishAPI(createdAPI.getId());
            System.out.println("API publish done result = " + result);
            return createdAPI;
        }
    }


    /**
     * Creates a API in WSO2-APIM
     *
     * @param apiDTO - Instance of APIDTO, which carries the information of api creation request
     * @return - Instance of APIDTO, which contains the information of created API
     * @throws APIMClientException - If fails to invoke the operation due to wrong credentials or any other issue
     */
    private API createAPI(API apiDTO) throws APIMClientException {

        API publisherApi = null;
        try {
            publisherApi = internalPublisherClient.createApi(apiDTO);
        } catch (APIMClientException e) {
            if (e.getResponseStatus() == 401) {
                dcrClient.getRenewedToken();
                publisherApi = internalPublisherClient.createApi(apiDTO);
            } else {
                throw e;
            }
        }
        return publisherApi;
    }

    /**
     * Publishes a created api (specified by the apiID) to APIM
     *
     * @param apiID - The unique ID which represents the API in WSO2-APIM
     * @return - True if publishing is successful, false otherwise
     * @throws APIMClientException - If fails to invoke the operation due to wrong credentials or any other issue
     */
    private boolean publishAPI(String apiID) throws APIMClientException {

        boolean result = false;
        try {
            internalPublisherClient.changeApiLifecycle("Publish", apiID);
            result = true;
        } catch (APIMClientException e) {
            if (e.getResponseStatus() == 401) {
                dcrClient.getRenewedToken();
                internalPublisherClient.changeApiLifecycle("Publish", apiID);
                result = true;
            } else {
                throw e;
            }
        }
        return result;
    }


    private APIInfo getExistingApiInfo(API apiDTO, List<APIInfo> apiLIst) {
        for (APIInfo api : apiLIst) {
            if (api.getName().equals(apiDTO.getName())) {
                return api;
            }
        }
        return null;
    }


    private API getExistingApi(API apiDTO, List<APIInfo> apiLIst) throws APIMClientException {
        APIInfo apiInfo = getExistingApiInfo(apiDTO, apiLIst);
        API api = null;
        if (apiInfo != null) {
            try {
                api = internalPublisherClient.getApi(apiInfo.getId());
            } catch (APIMClientException e) {
                if (e.getResponseStatus() == 401) {
                    dcrClient.getRenewedToken();
                    api = internalPublisherClient.getApi(apiInfo.getId());
                } else {
                    throw e;
                }
            }
        }
        return api;
    }
}
