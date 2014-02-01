package ecologylab.bigsemantics.service.logging;

import java.util.Date;

import ecologylab.bigsemantics.metadata.output.DocumentLogRecord;
import ecologylab.net.ParsedURL;
import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_scalar;

@simpl_inherit
public class ServiceLogRecord extends DocumentLogRecord
{

  @simpl_scalar
  private String    requesterIp;

  @simpl_scalar
  private ParsedURL requestUrl;

  @simpl_scalar
  private Date      beginTime;

  @simpl_scalar
  private long      msTotal;

  @simpl_scalar
  private int       responseCode;

  @simpl_scalar
  private long      msPageCacheLookup;

  public Date getBeginTime()
  {
    return beginTime;
  }

  public void setBeginTime(Date beginTime)
  {
    this.beginTime = beginTime;
  }

  public long getMsTotal()
  {
    return msTotal;
  }

  public void setMsTotal(long msTotal)
  {
    this.msTotal = msTotal;
  }

  public String getRequesterIp()
  {
    return requesterIp;
  }

  public void setRequesterIp(String requesterIp)
  {
    this.requesterIp = requesterIp;
  }

  public ParsedURL getRequestUrl()
  {
    return requestUrl;
  }

  public void setRequestUrl(ParsedURL requestUrl)
  {
    this.requestUrl = requestUrl;
  }

  public int getResponseCode()
  {
    return responseCode;
  }

  public void setResponseCode(int responseCode)
  {
    this.responseCode = responseCode;
  }

  public long getMsPageCacheLookup()
  {
    return msPageCacheLookup;
  }

  public void setMsPageCacheLookup(long msPageCacheLookup)
  {
    this.msPageCacheLookup = msPageCacheLookup;
  }

  @Override
  public String toString()
  {
    return String.format("%s[%s]",
                         ServiceLogRecord.class.getSimpleName(),
                         requestUrl == null ? "NullLocation" : requestUrl.toString());
  }

  static final public ServiceLogRecord DUMMY;

  static
  {
    DUMMY = new ServiceLogRecord();
  }

}
