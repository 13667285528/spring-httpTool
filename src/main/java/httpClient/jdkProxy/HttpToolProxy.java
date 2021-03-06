package httpClient.jdkProxy;

import httpClient.client.HttpClientManager;
import httpClient.response.HttpResoponseHandler;
import httpClient.factory.reqeustBuilder.HttpReqesutBuilderStaticFactory;
import httpClient.factory.reqeustBuilder.HttpRequestBuilder;
import httpClient.request.HttpRequestConfig;
import httpClient.request.HttpRequestConfigAdapter;
import httpClient.request.HttpRequestConfigParser;
import httpClient.request.HttpRequestCustomConfig;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.Asserts;
import spring.PropertiesResolver;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;


public class HttpToolProxy<T> implements InvocationHandler {

    private Class<T> httpToolInterface;
    // 默认的httpClient
    private HttpClientManager httpClientManager;
    // resolve properties
    private PropertiesResolver propertiesResolver;
    // reponse handle
    private HttpResoponseHandler resoponseHandler;


    public HttpToolProxy(Class<T> httpToolInterface,
                         HttpClientManager httpClientManager,
                         PropertiesResolver propertiesResolver,
                         HttpResoponseHandler resoponseHandler) {

        Asserts.notNull(httpToolInterface, "httpToolInterface");
        Asserts.notNull(httpClientManager, "httpClientManager");
        Asserts.notNull(propertiesResolver, "propertiesResolver");
        Asserts.notNull(resoponseHandler, "resoponseHandler");

        this.httpToolInterface = httpToolInterface;
        this.httpClientManager = httpClientManager;
        this.propertiesResolver = propertiesResolver;
        this.resoponseHandler = resoponseHandler;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        HttpRequestCustomConfig customConfig = HttpRequestConfigParser.parse(httpToolInterface, method, args);
        HttpRequestConfig httpRequestConfig = new HttpRequestConfigAdapter(customConfig, propertiesResolver);
        HttpRequestBuilder requestBuilder = HttpReqesutBuilderStaticFactory.createHttpRequestBuilder(httpRequestConfig);
        HttpRequestBase httpRequest = requestBuilder.build();
        HttpClientContext context = HttpClientContext.create();
        CloseableHttpClient httpClient = httpClientManager.getHttpClient();
        if(httpClient == null) {
            throw new RuntimeException("http client is null");
        }
        HttpResponse httpResponse = null;
        try {
            Long startMillis = System.currentTimeMillis();
            httpResponse = httpClient.execute(httpRequest, context);
            Long endMillis = System.currentTimeMillis();

            System.out.println("total time: " + (endMillis - startMillis) + "ms");

            return resoponseHandler.handle(method, httpRequest, httpResponse);
        } catch (Exception e) {
            System.out.println(e);
            throw e;
        }
    }
}
