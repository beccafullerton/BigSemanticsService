package ecologylab.bigsemantics.logging;

import ecologylab.logging.LogEvent;
import ecologylab.serialization.annotations.simpl_inherit;

/**
 * When a dpool task is queued in the controller.
 * 
 * @author quyin
 */
@simpl_inherit
public class DownloadTaskQueued extends LogEvent
{

  public DownloadTaskQueued()
  {
    super();
  }

}
