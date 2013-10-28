package ecologylab.bigsemantics.httpclient;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.protocol.RequestAcceptEncoding;
import org.apache.http.client.protocol.ResponseContentEncoding;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.CoreProtocolPNames;

/**
 * A factory for HttpClient objects.
 * 
 * @author quyin
 */
public class HttpClientFactory
{

  private PoolingClientConnectionManager connectionManager;

  private Map<String, AbstractHttpClient> clients;

  public HttpClientFactory()
  {
    connectionManager = new PoolingClientConnectionManager();
    connectionManager.setDefaultMaxPerRoute(20);
    connectionManager.setMaxTotal(200);
    clients = new HashMap<String, AbstractHttpClient>();
  }
  
  public AbstractHttpClient get()
  {
    return get("");
  }
  
  public synchronized AbstractHttpClient get(String userAgent)
  {
    if (!clients.containsKey(userAgent))
    {
      // From Apache HttpClient doc: "HttpClient is fully thread-safe when used
      // with a thread-safe connection manager." The
      // PoolingClientConnectionManager we are using is such as thread-safe one.
      AbstractHttpClient client = new RedirectHttpClient(connectionManager);
      prepareHttpClient(client);
      if (userAgent.length() > 0)
      {
        client.getParams().setParameter(CoreProtocolPNames.USER_AGENT, userAgent);
      }
      clients.put(userAgent, client);
    }
    return clients.get(userAgent);
  }
  
  private void prepareHttpClient(AbstractHttpClient client)
  {
    client.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS, true);
    client.addRequestInterceptor(new RequestAcceptEncoding());
    client.addResponseInterceptor(new ResponseContentEncoding());
    client.setRedirectStrategy(new BasicRedirectStrategy());
  }

}
