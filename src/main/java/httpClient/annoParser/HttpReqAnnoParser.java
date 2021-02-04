package httpClient.annoParser;

import httpClient.annotation.HttpReq;
import httpClient.HttpRequestConfig;

import java.lang.annotation.Annotation;

public class HttpReqAnnoParser implements HttpToolAnnoParser{

    @Override
    public void parse(Annotation annotation, HttpRequestConfig httpRequestConfig) {
        HttpReq httpReq = (HttpReq)annotation;
        httpRequestConfig.setUrl(httpReq.url());
    }
}
