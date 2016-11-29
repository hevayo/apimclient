package org.wso2.carbon.apimgt.client;


import feign.Feign;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.client.common.APIMClientException;
import org.wso2.carbon.apimgt.client.common.APIMErrorDecoder;
import org.wso2.carbon.apimgt.client.common.IntegratedAuthBearerRequestInterceptor;
import org.wso2.carbon.apimgt.client.common.TrustedFeignClient;
import org.wso2.carbon.apimgt.client.common.configs.APIMConfig;
import org.wso2.carbon.apimgt.store.client.api.*;
import org.wso2.carbon.apimgt.store.client.invoker.ApiClient;
import org.wso2.carbon.apimgt.store.client.model.*;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.List;


public class StoreClient {

    private static final org.apache.commons.logging.Log log = LogFactory.getLog(StoreClient.class);
    String contentType = "application/json";
    //private String basePath = "https://localhost:9443/api/am/store/v0.10";
    private String accept = "application/json";
    private APIMConfig config;
    private String ifMatch = null;
    private String ifNoneMatch = null;
    private String ifModifiedSince = null;
    private String ifUnmodifiedSince = null;
    private DcrClient dcr = null;
    private ApisAPIApi apis = null;
    private APIindividualApi individualApi = null;
    private ApplicationCollectionApi applications = null;
    private ApplicationindividualApi individualApplication = null;
    private SubscriptionCollectionApi subscriptions = null;
    private SubscriptionindividualApi individualSubscription = null;
    private TierindividualApi individualTier = null;
    private TagCollectionApi tags = null;
    private TierCollectionApi tiers = null;


    public StoreClient(APIMConfig config) {
        this.config = config;
        try {
            dcr = new DcrClient(config);
        } catch (APIManagementException e) {
            log.error("Error initializing DCR client");
        }

        Feign.Builder builder = Feign.builder().client(new TrustedFeignClient())
                .encoder(new GsonEncoder())
                .decoder(new GsonDecoder())
                .errorDecoder(new APIMErrorDecoder());
        ApiClient apiClient = getApiClient(builder);
        apis = apiClient.buildClient(ApisAPIApi.class);

        ApiClient apiIndividualClient = getApiClient();
        individualApi = apiIndividualClient.buildClient(APIindividualApi.class);

        ApiClient applicationsClient = getApiClient();
        applications = applicationsClient.buildClient(ApplicationCollectionApi.class);

        ApiClient singleAppClient = getApiClient();
        individualApplication = singleAppClient.buildClient(ApplicationindividualApi.class);

        ApiClient subscriptionsClient = getApiClient();
        subscriptions = subscriptionsClient.buildClient(SubscriptionCollectionApi.class);

        ApiClient singleSubscriptionsClient = getApiClient();
        individualSubscription = singleSubscriptionsClient.buildClient(SubscriptionindividualApi.class);

        ApiClient tagsClient = getApiClient();
        tags = tagsClient.buildClient(TagCollectionApi.class);

        ApiClient tiersClient = getApiClient();
        tiers = tiersClient.buildClient(TierCollectionApi.class);

        ApiClient individualTierClient = getApiClient();
        individualTier = individualTierClient.buildClient(TierindividualApi.class);

    }

    private ApiClient getApiClient() {
        Feign.Builder feignBuilder = getFeignBuilder();
        ApiClient client = getApiClient(feignBuilder);
        return client;
    }

    private ApiClient getApiClient(Feign.Builder feignBuilder) {
        ApiClient client = new ApiClient();
        client.setBasePath(this.config.getStoreEndpointConfig().getUrl());
        client.setFeignBuilder(feignBuilder);
        return client;
    }

    private Feign.Builder getFeignBuilder() {
        return Feign.builder().client(new TrustedFeignClient())
                .encoder(new GsonEncoder())
                .decoder(new GsonDecoder())
                .errorDecoder(new APIMErrorDecoder())
                .requestInterceptor(new IntegratedAuthBearerRequestInterceptor(dcr));
    }

    /**
     * Get a list of available APIs qualifying under a given search condition.
     */
    public APIList getAvailableStoreApis(int limit, int offset, String xWSO2Tenant, String query) throws APIMClientException {
        APIList response = null;
        try {
            response = apis.apisGet(limit, offset, xWSO2Tenant, query, accept, ifNoneMatch);
        } catch (UndeclaredThrowableException e) {
            throw (APIMClientException) e.getCause();
        }
        return response;
    }


    /**
     * Downloads a FILE type document/get the inline content or source url of a certain document.
     */
    public void getFileTypeDocument(String apiId, String documentId, String xWSO2Tenant) throws APIMClientException {
        try {
            individualApi.apisApiIdDocumentsDocumentIdContentGet(apiId, documentId, xWSO2Tenant, accept, ifNoneMatch, ifModifiedSince);
        } catch (UndeclaredThrowableException e) {
            throw (APIMClientException) e.getCause();
        }
    }

    /**
     * Get a particular document associated with an API.
     */
    public Document getApiDocument(String apiId, String documentId, String xWSO2Tenant) throws APIMClientException {
        Document response = null;
        try {
            response = individualApi.apisApiIdDocumentsDocumentIdGet(apiId, documentId, xWSO2Tenant, accept, ifNoneMatch, ifModifiedSince);
        } catch (UndeclaredThrowableException e) {
            throw (APIMClientException) e.getCause();
        }
        return response;
    }

    /**
     * Get a list of documents belonging to an API.
     */
    public DocumentList getApiDocuments(String apiId, int limit, int offset, String xWSO2Tenant) throws APIMClientException {
        DocumentList response = null;
        try {
            response = individualApi.apisApiIdDocumentsGet(apiId, limit, offset, xWSO2Tenant, accept, ifNoneMatch);
        } catch (UndeclaredThrowableException e) {
            throw (APIMClientException) e.getCause();
        }
        return response;
    }

    /**
     * Get Details of API
     */
    public API getApiDetails(String apiId, String xWSO2Tenant) throws APIMClientException {
        API response = null;
        try {
            response = individualApi.apisApiIdGet(apiId, accept, ifNoneMatch, ifModifiedSince, xWSO2Tenant);
        } catch (UndeclaredThrowableException e) {
            throw (APIMClientException) e.getCause();
        }
        return response;
    }

    /**
     * Get the swagger of an API
     */
    public void getApiSwagger(String apiId, String xWSO2Tenant) throws APIMClientException {
        try {
            individualApi.apisApiIdSwaggerGet(apiId, accept, ifNoneMatch, ifModifiedSince, xWSO2Tenant);
        } catch (UndeclaredThrowableException e) {
            throw (APIMClientException) e.getCause();
        }
    }

    /**
     * Get the thumbnail image
     */
    public void getApiThumbnail(String apiId) throws APIMClientException {
        try {
            individualApi.apisApiIdThumbnailGet(apiId, accept, ifNoneMatch, ifModifiedSince);
        } catch (UndeclaredThrowableException e) {
            throw (APIMClientException) e.getCause();
        }
    }


    /**
     * Get a list of applications
     */
    public ApplicationList getAvailableApplications(String groupId, String query, int limit, int offset) throws APIMClientException {
        ApplicationList response = null;
        try {
            response = applications.applicationsGet(groupId, query, limit, offset, accept, ifNoneMatch);
        } catch (UndeclaredThrowableException e) {
            throw (APIMClientException) e.getCause();
        }
        return response;
    }

    /**
     * Remove an application
     */
    public void removeApplication(String applicationId) throws APIMClientException {
        try {
            individualApplication.applicationsApplicationIdDelete(applicationId, ifMatch, ifUnmodifiedSince);
        } catch (UndeclaredThrowableException e) {
            throw (APIMClientException) e.getCause();
        }
    }

    /**
     * Get application details
     */
    public Application getApplicationDetails(String applicationId) throws APIMClientException {
        Application response = null;
        try {
            response = individualApplication.applicationsApplicationIdGet(applicationId, accept, ifNoneMatch, ifModifiedSince);
        } catch (UndeclaredThrowableException e) {
            throw (APIMClientException) e.getCause();
        }
        return response;
    }

    /**
     * Update application details
     */
    public Application updateApplication(String applicationId, Application body) throws APIMClientException {
        Application response = null;
        try {
            response = individualApplication.applicationsApplicationIdPut(applicationId, body, contentType, ifMatch, ifUnmodifiedSince);
        } catch (UndeclaredThrowableException e) {
            throw (APIMClientException) e.getCause();
        }
        return response;
    }

    /**
     * Generate keys for application
     */
    public ApplicationKey generateKeysForApplication(String applicationId, ApplicationKeyGenerateRequest body) throws APIMClientException {
        ApplicationKey response = null;
        try {
            response = individualApplication.applicationsGenerateKeysPost(applicationId, body, contentType, ifMatch, ifUnmodifiedSince);
        } catch (UndeclaredThrowableException e) {
            throw (APIMClientException) e.getCause();
        }
        return response;
    }

    /**
     * Create a new application.
     */
    public Application createApplication(Application body) throws APIMClientException {
        Application response = null;
        try {
            response = individualApplication.applicationsPost(body, contentType);
        } catch (UndeclaredThrowableException e) {
            throw (APIMClientException) e.getCause();
        }
        return response;
    }

    /**
     * Get subscription list. The API Identifier or Application Identifier the subscriptions of which are to be returned are passed as parameters.
     */
    public SubscriptionList getSusbscriptions(String apiId, String applicationId, String groupId, int offset, int limit) throws APIMClientException {
        SubscriptionList response = null;
        try {
            response = subscriptions.subscriptionsGet(apiId, applicationId, groupId, offset, limit, accept, ifNoneMatch);
        } catch (UndeclaredThrowableException e) {
            throw (APIMClientException) e.getCause();
        }
        return response;
    }

    /**
     * Add a new subscription
     */
    public Subscription addNewSubscription(Subscription body) throws APIMClientException {
        Subscription response = null;
        try {
            response = individualSubscription.subscriptionsPost(body, contentType);
        } catch (UndeclaredThrowableException e) {
            throw (APIMClientException) e.getCause();
        }
        return response;
    }

    /**
     * Remove subscription
     */
    public void removeSusbcription(String subscriptionId) throws APIMClientException {
        try {
            individualSubscription.subscriptionsSubscriptionIdDelete(subscriptionId, ifMatch, ifUnmodifiedSince);
        } catch (UndeclaredThrowableException e) {
            throw (APIMClientException) e.getCause();
        }
    }

    /**
     * Get subscription details
     */
    public Subscription getSubscriptionDetails(String subscriptionId) throws APIMClientException {
        Subscription response = null;
        try {
            response = individualSubscription.subscriptionsSubscriptionIdGet(subscriptionId, accept, ifNoneMatch, ifModifiedSince);
        } catch (UndeclaredThrowableException e) {
            throw (APIMClientException) e.getCause();
        }
        return response;
    }

    /**
     * Get a list of tags that are already added to APIs
     */
    public TagList getTags(int offset, int limit, String xWSO2Tenant) throws APIMClientException {
        TagList response = null;
        try {
            response = tags.tagsGet(limit, offset, xWSO2Tenant, accept, ifNoneMatch);
        } catch (UndeclaredThrowableException e) {
            throw (APIMClientException) e.getCause();
        }
        return response;
    }

    /**
     * Get available tiers
     */
    public List<TierList> getTiers(String tierLevel, int offset, int limit, String xWSO2Tenant) throws APIMClientException {
        List<TierList> response = null;
        try {
            response = tiers.tiersTierLevelGet(tierLevel, limit, offset, xWSO2Tenant, accept, ifNoneMatch);
        } catch (UndeclaredThrowableException e) {
            throw (APIMClientException) e.getCause();
        }
        return response;
    }

    /**
     * Get tier details
     */
    public Tier getTierDetails(String tierName, String tierLevel, String xWSO2Tenant) throws APIMClientException {
        Tier response = null;
        try {
            response = individualTier.tiersTierLevelTierNameGet(tierName, tierLevel, xWSO2Tenant, accept, ifNoneMatch, ifModifiedSince);
        } catch (UndeclaredThrowableException e) {
            throw (APIMClientException) e.getCause();
        }
        return response;
    }

}
