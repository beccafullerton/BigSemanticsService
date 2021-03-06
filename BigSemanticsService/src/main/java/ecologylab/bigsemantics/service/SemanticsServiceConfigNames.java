package ecologylab.bigsemantics.service;

/**
 * Config names.
 * 
 * @author quyin
 */
public interface SemanticsServiceConfigNames
{

  static final String PORT                      = "service.port";

  static final String MAX_THREADS               = "service.threads.max";

  static final String MIN_THREADS               = "service.threads.min";

  static final String NUM_ACCEPTORS             = "service.acceptors";

  static final String NUM_SELECTORS             = "service.selectors";

  static final String STATIC_DIR                = "service.static_dir";

  static final String SECURE_PORT               = "service.secure_port";

  static final String KEYSTORE_PATH             = "service.keystore_path";

  static final String KEYSTORE_PASSWORD         = "service.keystore_password";

  static final String KEYMANAGER_PASSWORD       = "service.keymanager_password";

  static final String PERSISTENT_CACHE_CLASS    = "service.persistent_cache.class";

  static final String PERSISTENT_CACHE_DIR      = "service.persistent_cache.dir";
  
  static final String WIKI_URL                  = "service.wiki_url";

  static final String COUCHDB_URL               = "service.persistent_cache.couchdb.url";

  static final String LOG_CACHE_SIZE            = "service.log_cache.size";

  static final String ADMIN_PORT                = "service.admin.port";

  static final String DPOOL_RUN_BUILTIN_SERVICE = "service.dpool.run_builtin_service";

  static final String DPOOL_HOST                = "service.dpool.host";

  static final String DPOOL_PORT                = "service.dpool.port";
  
  static final String POST_STARTUP_MESSAGE      = "service.post_startup_message";

}
