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
import org.wso2.carbon.apimgt.publisher.client.api.*;
import org.wso2.carbon.apimgt.publisher.client.invoker.ApiClient;
import org.wso2.carbon.apimgt.publisher.client.model.*;

import java.io.File;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.List;


public class PublisherClient {

    private static final org.apache.commons.logging.Log log = LogFactory.getLog(PublisherClient.class);

    //private String basePath = "https://localhost:9443/api/am/publisher/v0.10";
    private APIMConfig config = null;
    private DcrClient dcr = null;

    private String accept = "application/json";
    private String contentType = "application/json";
    private String ifMatch = null;
    private String ifNoneMatch = null;
    private String ifModifiedSince = null;
    private String ifUnmodifiedSince = null;

    private APIsApi api = null;
    private APIDocumentApi document = null;
    private ApplicationsApi application = null;
    private EnvironmentsApi environments = null;
    private SubscriptionsApi subscriptions = null;
    private TiersApi tiers = null;


    /**
     * PublisherClient constructor - Initialize a PublisherClient instance
     *
     * @param config - APIMConfig object
     */
    public PublisherClient(APIMConfig config) {
        this.config = config;
        try {
            dcr = new DcrClient(config);
        } catch (APIManagementException e) {
            log.error("Error initializing DCR client");
        }

        ApiClient apiClient = getApiClient();
        api = apiClient.buildClient(APIsApi.class);

        ApiClient docClient = getApiClient();
        document = docClient.buildClient(APIDocumentApi.class);

        ApiClient appClient = getApiClient();
        application = appClient.buildClient(ApplicationsApi.class);

        ApiClient envClient = getApiClient();
        environments = envClient.buildClient(EnvironmentsApi.class);

        ApiClient subscriptionClient = getApiClient();
        subscriptions = subscriptionClient.buildClient(SubscriptionsApi.class);

        ApiClient tiersClient = getApiClient();
        tiers = tiersClient.buildClient(TiersApi.class);
    }

    private ApiClient getApiClient() {
        Feign.Builder feignBuilder = getFeignBuilder();
        return getApiClient(feignBuilder);
    }

    private ApiClient getApiClient(Feign.Builder feignBuilder) {
        ApiClient client = new ApiClient();
        client.setBasePath(this.config.getPublisherEndpointConfig().getUrl());
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
     * Delete an existing API
     */
    public void deleteApi(String apiId) throws APIMClientException {
        try {
            api.apisApiIdDelete(apiId, ifMatch, ifUnmodifiedSince);
        } catch (UndeclaredThrowableException ute) {
            throw (APIMClientException) ute.getCause();
        }
    }

    /**
     * Get details of an API
     */
    public API getApi(String apiId) throws APIMClientException {
        API response = null;
        try {
            response = api.apisApiIdGet(apiId, accept, ifNoneMatch, ifModifiedSince);
        } catch (UndeclaredThrowableException ute) {
            throw (APIMClientException) ute.getCause();
        }
        return response;
    }

    /**
     * Update an existing API
     */
    public API updateApi(String apiId, API body) throws APIMClientException {
        API response = null;
        try {
            response = api.apisApiIdPut(apiId, body, contentType, ifMatch, ifUnmodifiedSince);
        } catch (UndeclaredThrowableException ute) {
            throw (APIMClientException) ute.getCause();
        }
        return response;
    }

    /**
     * Get the swagger of an API
     */
    public void getApiSwaggerDefinition(String apiId) throws APIMClientException {
        try {
            api.apisApiIdSwaggerGet(apiId, accept, ifNoneMatch, ifModifiedSince);
        } catch (UndeclaredThrowableException ute) {
            throw (APIMClientException) ute.getCause();
        }
    }

    /**
     * Update an existing swagger definition of an API
     */
    public void updateApiSwaggerDefinition(String apiId, String apiDefinition) throws APIMClientException {
        String contentType = "multipart/form-data";
        try {
            api.apisApiIdSwaggerPut(apiId, apiDefinition, contentType, ifMatch, ifUnmodifiedSince);
        } catch (UndeclaredThrowableException ute) {
            throw (APIMClientException) ute.getCause();
        }


    }

    /**
     * Downloads a thumbnail image of an API
     */
    public void getApiThumbnailImage(String apiId) throws APIMClientException {
        try {
            api.apisApiIdThumbnailGet(apiId, accept, ifNoneMatch, ifModifiedSince);
        } catch (UndeclaredThrowableException ute) {
            throw (APIMClientException) ute.getCause();
        }
    }

    /**
     * Upload a thumbnail image to an API.
     */
    public FileInfo updateApiThumbnail(String apiId, File file) throws APIMClientException {
        FileInfo response = null;
        try {
            response = api.apisApiIdThumbnailPost(apiId, file, contentType, ifMatch, ifUnmodifiedSince);
        } catch (UndeclaredThrowableException ute) {
            throw (APIMClientException) ute.getCause();
        }
        return response;
    }

    /**
     * Change the lifecycle of an API.
     */
    public void changeApiLifecycle(String action, String apiId) throws APIMClientException {
        String lifecycleChecklist = "";
        try {
            api.apisChangeLifecyclePost(action, apiId, lifecycleChecklist, ifMatch, ifUnmodifiedSince);
        } catch (UndeclaredThrowableException ute) {
            throw (APIMClientException) ute.getCause();
        }
    }

    /**
     * Create a new API by copying an existing API.
     */
    public void copyApi(String newVersion, String apiId) throws APIMClientException {
        try {
            api.apisCopyApiPost(newVersion, apiId);
        } catch (UndeclaredThrowableException ute) {
            throw (APIMClientException) ute.getCause();
        }
    }

    /**
     * Get a list of available APIs qualifying under a given search condition.
     */
    public APIList getAvailableApis(String query, int limit, int offset) throws APIMClientException {
        APIList response = null;
        try {
            response = api.apisGet(limit, offset, query, accept, ifNoneMatch);
        } catch (UndeclaredThrowableException ute) {
            throw (APIMClientException) ute.getCause();
        }
        return response;
    }

    /**
     * Create a new API.
     */
    public API createApi(API body) throws APIMClientException {
        API response = null;
        try {
            response = api.apisPost(body, contentType);
        } catch (UndeclaredThrowableException ute) {
            throw (APIMClientException) ute.getCause();
        }
        return response;
    }

    /**
     * Downloads a FILE type document/get the inline content or source url of a certain document.
     */
    public void getFileTypeDocument(String apiId, String documentId) throws APIMClientException {
        try {
            document.apisApiIdDocumentsDocumentIdContentGet(apiId, documentId, accept, ifNoneMatch, ifModifiedSince);
        } catch (UndeclaredThrowableException ute) {
            throw (APIMClientException) ute.getCause();
        }
    }

    /**
     * Upload a file to a document or add inline content to the document.  Document&#39;s source type should be **FILE** in order to upload a file to
     * the document using **file** parameter. Document&#39;s source type should be **INLINE** in order to add inline content to the document using
     * **inlineContent** parameter.  Only one of **file** or **inlineContent** can be specified at one time.
     */
    public Document uploadDocument(String apiId, String documentId, File file, String inlineContent) throws APIMClientException {
        Document response = null;
        try {
            response = document.apisApiIdDocumentsDocumentIdContentPost(apiId, documentId, contentType, file, inlineContent, ifMatch, ifUnmodifiedSince);
        } catch (UndeclaredThrowableException ute) {
            throw (APIMClientException) ute.getCause();
        }
        return response;
    }

    /**
     * Delete a document of an API.
     */
    public void deleteDocument(String apiId, String documentId) throws APIMClientException {
        try {
            document.apisApiIdDocumentsDocumentIdDelete(apiId, documentId, ifMatch, ifUnmodifiedSince);
        } catch (UndeclaredThrowableException ute) {
            throw (APIMClientException) ute.getCause();
        }
    }

    /**
     * Get a particular document associated with an API.
     */
    public Document getDocument(String apiId, String documentId) throws APIMClientException {
        Document response = null;
        try {
            response = document.apisApiIdDocumentsDocumentIdGet(apiId, documentId, accept, ifNoneMatch, ifModifiedSince);
        } catch (UndeclaredThrowableException ute) {
            throw (APIMClientException) ute.getCause();
        }
        return response;
    }

    /**
     * Update document details.
     */
    public Document updateDocument(String apiId, String documentId, Document body) throws APIMClientException {
        Document response = null;
        try {
            response = document.apisApiIdDocumentsDocumentIdPut(apiId, documentId, body, contentType, ifMatch, ifUnmodifiedSince);
        } catch (UndeclaredThrowableException ute) {
            throw (APIMClientException) ute.getCause();
        }
        return response;
    }

    /**
     * Get a list of documents belonging to an API.
     */
    public DocumentList getDocumentsList(String apiId, int limit, int offset) throws APIMClientException {
        DocumentList response = null;
        try {
            response = document.apisApiIdDocumentsGet(apiId, limit, offset, accept, ifNoneMatch);
        } catch (UndeclaredThrowableException ute) {
            throw (APIMClientException) ute.getCause();
        }
        return response;
    }

    /**
     * Add a new document to an API.
     */
    public Document addNewDocument(String apiId, Document body) throws APIMClientException {
        Document response = null;
        try {
            response = document.apisApiIdDocumentsPost(apiId, body, contentType);
        } catch (UndeclaredThrowableException ute) {
            throw (APIMClientException) ute.getCause();
        }
        return response;
    }

    /**
     * Get application details.
     */
    public Application getApplicationDetails(String applicationId) throws APIMClientException {
        Application response = null;
        try {
            response = application.applicationsApplicationIdGet(applicationId, accept, ifNoneMatch, ifModifiedSince);
        } catch (UndeclaredThrowableException ute) {
            throw (APIMClientException) ute.getCause();
        }
        return response;
    }

    /**
     * Get a list of gateway environments configured previously.
     */
    public EnvironmentList getGatewayEnvironments(String apiId) throws APIMClientException {
        EnvironmentList response = null;
        try {
            response = environments.environmentsGet(apiId);
        } catch (UndeclaredThrowableException ute) {
            throw (APIMClientException) ute.getCause();
        }
        return response;
    }


    /**
     * Block a subscription.
     */
    public void blockSubscription(String subscriptionId, String blockState) throws APIMClientException {
        try {
            subscriptions.subscriptionsBlockSubscriptionPost(subscriptionId, blockState, ifMatch, ifUnmodifiedSince);
        } catch (UndeclaredThrowableException ute) {
            throw (APIMClientException) ute.getCause();
        }
    }

    /**
     * Get subscription list. The API Identifier and corresponding Application Identifier the subscriptions of which are
     * to be returned are passed as parameters.
     */
    public SubscriptionList getSubscriptionsList(String apiId, int limit, int offset) throws APIMClientException {
        SubscriptionList response = null;
        try {
            response = subscriptions.subscriptionsGet(apiId, limit, offset, accept, ifNoneMatch);
        } catch (UndeclaredThrowableException ute) {
            throw (APIMClientException) ute.getCause();
        }
        return response;
    }

    /**
     * Get subscription details.
     */
    public Subscription getSubscriptionDetails(String subscriptionId, String accept) throws APIMClientException {
        Subscription response = null;
        try {
            response = subscriptions.subscriptionsSubscriptionIdGet(subscriptionId, accept, ifNoneMatch, ifModifiedSince);
        } catch (UndeclaredThrowableException ute) {
            throw (APIMClientException) ute.getCause();
        }
        return response;
    }

    /**
     * Unblock a Subscription.
     */
    public void unblockSubscription(String subscriptionId) throws APIMClientException {
        try {
            subscriptions.subscriptionsUnblockSubscriptionPost(subscriptionId, ifMatch, ifUnmodifiedSince);
        } catch (UndeclaredThrowableException ute) {
            throw (APIMClientException) ute.getCause();
        }
    }


    /**
     * Get available tiers.
     */
    public TierList getAvailableTiers(String tierLevel, int limit, int offset) throws APIMClientException {
        TierList response = null;
        try {
            response = tiers.tiersTierLevelGet(tierLevel, limit, offset, accept, ifNoneMatch);
        } catch (UndeclaredThrowableException ute) {
            throw (APIMClientException) ute.getCause();
        }
        return response;

    }

    /**
     * Add a new Tier.
     */
    public Tier addNewTier(Tier body, String tierLevel) throws APIMClientException {
        Tier response = null;
        try {
            response = tiers.tiersTierLevelPost(body, tierLevel, contentType);
        } catch (UndeclaredThrowableException ute) {
            throw (APIMClientException) ute.getCause();
        }
        return response;
    }

    /**
     * Delete a Tier.
     */
    public void deleteTier(String tierName, String tierLevel) throws APIMClientException {
        try {
            tiers.tiersTierLevelTierNameDelete(tierName, tierLevel, ifMatch, ifUnmodifiedSince);
        } catch (UndeclaredThrowableException ute) {
            throw (APIMClientException) ute.getCause();
        }
    }

    /**
     * Get a Tier.
     */
    public Tier getTier(String tierName, String tierLevel) throws APIMClientException {
        Tier response = null;
        try {
            response = tiers.tiersTierLevelTierNameGet(tierName, tierLevel, accept, ifNoneMatch, ifModifiedSince);
        } catch (UndeclaredThrowableException ute) {
            throw (APIMClientException) ute.getCause();
        }
        return response;
    }

    /**
     * Update a Tier.
     */
    public Tier updateTier(String tierName, Tier body, String tierLevel) throws APIMClientException {
        Tier response = null;
        try {
            response = tiers.tiersTierLevelTierNamePut(tierName, body, tierLevel, contentType, ifMatch, ifUnmodifiedSince);
        } catch (UndeclaredThrowableException ute) {
            throw (APIMClientException) ute.getCause();
        }
        return response;
    }

    /**
     * Update Tier Permission.
     */
    public List<Tier> updateTierPermission(String tierName, String tierLevel, TierPermission permissions) throws APIMClientException {
        List<Tier> response = null;
        try {
            response = tiers.tiersUpdatePermissionPost(tierName, tierLevel, ifMatch, ifUnmodifiedSince, permissions);
        } catch (UndeclaredThrowableException ute) {
            throw (APIMClientException) ute.getCause();
        }
        return response;
    }

}
