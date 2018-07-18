package ru.krista.sonar.plugins.tools;

import java.io.IOException;
import java.util.Date;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import ru.krista.sonar.plugins.components.ConfigTools;
import ru.krista.sonar.plugins.exceptions.HttpManagerException;
import ru.krista.sonar.plugins.model.ResponseInfo;

/**
 * Менеджер обращений к kallithea-api по http.
 */
public class HttpManager implements AutoCloseable {

    private static final Logger LOGGER = Loggers.get(HttpManager.class);
    private static final Integer RESPONSE_OK_CODE = 200;

    private final CloseableHttpClient httpClient;

    public HttpManager(ConfigTools configTools) {
        Integer connectionTimeout = configTools.getConnectionTimeout();
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(connectionTimeout)
                .setConnectionRequestTimeout(connectionTimeout)
                .setSocketTimeout(connectionTimeout).build();
        httpClient = HttpClients.custom().setDefaultRequestConfig(config).build();
    }

    /**
     * Выполнить закачку обновления.
     *
     * @return ответ сервера
     */
    public ResponseInfo select(String url) {
        if (httpClient == null) {
            throw new HttpManagerException("httpClient not setup");
        }
        HttpGet httpGet = new HttpGet(url);
        return send(httpGet);
    }

    /**
     * Отправить сообщение и обработать ответ.
     *
     * @param requestBase сообщение
     *
     * @return ответ
     */
    private ResponseInfo send(HttpRequestBase requestBase) {
        byte[] buffer;
        String statusCode;
        try {
            CloseableHttpResponse response = httpClient.execute(requestBase);
            statusCode = String.valueOf(response.getStatusLine().getStatusCode());
            checkResponse(response, true);
            buffer = EntityUtils.toByteArray(response.getEntity());
        } catch (Exception e) {
            LOGGER.error("Request from {} error: {}", requestBase.getURI().toString(), e);
            throw new HttpManagerException(e.getMessage());
        }
        return new ResponseInfo(statusCode, buffer);
    }

    public void send(String url, String body) {
        try {
            HttpPost httpPost = new HttpPost(url);
            httpPost.setEntity(new StringEntity(body, ConfigTools.UTF_8));
            LOGGER.info("Start send comment time {}", DateFormatUtils.ISO_TIME_NO_T_FORMAT.format(new Date()));
            CloseableHttpResponse response = httpClient.execute(httpPost);
            LOGGER.info("Finish send comment time {}", DateFormatUtils.ISO_TIME_NO_T_FORMAT.format(new Date()));
            checkResponse(response, false);
        } catch (Exception e) {
            LOGGER.info("Error send comment time {}", DateFormatUtils.ISO_TIME_NO_T_FORMAT.format(new Date()));
            LOGGER.error("Send comment {} error: {}", url, e);
            throw new HttpManagerException(e.getMessage());
        }
    }

    private static void checkResponse(HttpResponse response, boolean needCheckContent) {
        try {
            if (needCheckContent && response.getEntity().getContent() == null) {
                throw new HttpManagerException("Empty response");
            }
            if (response.getStatusLine().getStatusCode() != RESPONSE_OK_CODE) {
                throw new HttpManagerException(String.format("Error response - %d : %s",
                        response.getStatusLine().getStatusCode(), EntityUtils.toString(response.getEntity())));
            }
        } catch (IOException e) {
            throw new HttpManagerException("Error check response", e);
        }
    }

    @Override
    public void close() throws Exception {
        try {
            httpClient.close();
        } catch (Exception e) {
            LOGGER.error("Error close httpClient: {}", e);
        }
    }
}
