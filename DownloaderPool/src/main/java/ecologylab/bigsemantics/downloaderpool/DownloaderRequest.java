package ecologylab.bigsemantics.downloaderpool;

import java.util.ArrayList;
import java.util.List;

import ecologylab.net.ParsedURL;
import ecologylab.serialization.annotations.simpl_collection;
import ecologylab.serialization.annotations.simpl_scalar;

/**
 * 
 * @author quyin
 *
 */
public class DownloaderRequest
{
  
  /**
   * Blacklisted domains.
   */
  @simpl_collection("domain")
  private List<String> blacklist;

  @simpl_scalar
  private int          maxTaskCount;
  
  public DownloaderRequest()
  {
    super();
  }

  public List<String> getBlacklist()
  {
    return blacklist;
  }

  public void setBlacklist(List<String> blacklist)
  {
    this.blacklist = blacklist;
  }

  public int getMaxTaskCount()
  {
    return maxTaskCount;
  }

  public void setMaxTaskCount(int maxTaskCount)
  {
    this.maxTaskCount = maxTaskCount;
  }
  
  protected List<String> blacklist()
  {
    if(blacklist == null)
      blacklist = new ArrayList<String>();
    return blacklist;
  }
  
  public void addToBlacklist(String domain)
  {
    if (domain != null && domain.length() > 0)
      this.blacklist().add(domain);
  }
  
  public boolean accept(ParsedURL purl)
  {
    if (purl == null)
      return false;
    if (blacklist != null)
    {
      for (String domain : blacklist)
      {
        if (purl.domain().equals(domain))
          return false;
      }
    }
    return true;
  }

}