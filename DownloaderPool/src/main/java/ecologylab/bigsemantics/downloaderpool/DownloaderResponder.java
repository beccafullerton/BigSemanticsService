package ecologylab.bigsemantics.downloaderpool;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ecologylab.bigsemantics.downloaderpool.DownloaderResult.State;
import ecologylab.generic.Continuation;

/**
 * This object sends the downloading result to the controller. An object of this class corresponds
 * to a downloading task (represented by a Task object, and associated with a Page object).
 * 
 * @author quyin
 */
public class DownloaderResponder implements Continuation<Page>
{

  static Logger  logger = LoggerFactory.getLogger(DownloaderResponder.class);

  /**
   * The URL used to report downloaded pages to the controller.
   */
  private String controllerReportUrl;

  /**
   * The task that we need to respond to.
   */
  private Task   associatedTask;

  Downloader     downloader;

  public DownloaderResponder(String controllerReportUrl, Task associatedTask)
  {
    this.controllerReportUrl = controllerReportUrl;
    this.associatedTask = associatedTask;
  }

  @Override
  public void callback(Page page)
  {
    DownloaderResult result = page.getResult();
    if (result == null)
    {
      logger.error("WEIRD! Null result for callback Page: " + page);
      return;
    }
    logger.info("Downloading result for Task[{}][{}]: {}: {}",
                associatedTask.getId(),
                associatedTask.getPurl(),
                result.getHttpRespCode(),
                result.getHttpRespMsg());

    result.setState(State.OK);
    searchPatternInContent(result, associatedTask.getFailRegex(), State.ERR_CONTENT);
    searchPatternInContent(result, associatedTask.getBanRegex(), State.ERR_BANNED);

    Map<String, String> params = new HashMap<String, String>();
    params.put("wid", downloader.getName());
    params.put("tid", associatedTask.getId());
    params.put("state", result.getState().toString());
    params.put("code", String.valueOf(result.getHttpRespCode()));
    params.put("msg", result.getHttpRespMsg());
    Utils.putNonEmpty(params, "mime", result.getMimeType());
    Utils.putNonEmpty(params, "charset", result.getCharset());
    Utils.putNonEmpty(params, "descr", result.getContentDescription());
    String content = result.getContent();
    int contentLen = content == null ? 0 : content.length();
    logger.info("form params except content[len={}]: {}", contentLen, params);
    Utils.putNonEmpty(params, "content", content);
    HttpPost post = Utils.generatePostRequest(controllerReportUrl, params);

    try
    {
      HttpClient client = downloader.clientFactory.get();
      BasicResponse postResult = client.execute(post, new BasicResponseHandler());
      if (postResult.getHttpRespCode() == HttpStatus.SC_OK)
      {
        logger.info("Downloading result reported.");
      }
      else
      {
        logger.error("Error reporting downloading result: {}: {}",
                     postResult.getHttpRespCode(),
                     postResult.getHttpRespMsg());
      }
    }
    catch (ClientProtocolException e)
    {
      logger.error("Protocol exception when reporting to "
                   + controllerReportUrl
                   + " with "
                   + associatedTask, e);
    }
    catch (IOException e)
    {
      logger.error("I/O exception when reporting to "
                   + controllerReportUrl
                   + " with "
                   + associatedTask, e);
    }
  }

  /**
   * Search pattern from the content of the given downloading result.
   * 
   * @param result
   *          The downloading result.
   * @param regex
   *          The pattern to search for.
   * @param newState
   *          The result will be set to this state if the pattern is found in the content of the
   *          result.
   * @return true if the pattern has been found in the content of the result, otherwise false.
   */
  private boolean searchPatternInContent(DownloaderResult result, String regex, State newState)
  {
    String content = result.getContent();
    if (content != null && content.length() > 0 && regex != null && regex.length() > 0)
    {
      Pattern p = Pattern.compile(regex);
      Matcher m = p.matcher(content);
      if (m.find())
      {
        result.setState(newState);
        return true;
      }
    }
    return false;
  }

}
