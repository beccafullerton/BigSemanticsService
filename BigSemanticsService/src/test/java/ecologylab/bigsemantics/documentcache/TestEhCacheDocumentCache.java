package ecologylab.bigsemantics.documentcache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;

import org.junit.Before;
import org.junit.Test;

import ecologylab.bigsemantics.metadata.builtins.Document;
import ecologylab.bigsemantics.metadata.builtins.RichDocument;
import ecologylab.net.ParsedURL;

/**
 * Unit test for EhCacheDocumentCache.
 * 
 * @author quyin
 */
public class TestEhCacheDocumentCache
{

  private EhCacheDocumentCache cache;

  private ParsedURL            key;

  @Before
  public void setUp() throws Exception
  {
    cache = new EhCacheDocumentCache(EhCacheMan.getSingleton());
    key = ParsedURL.getAbsolute("http://example.com/");
  }

  @Test
  public void testContainsKey_empty()
  {
    assertFalse("Empty map does not contain key", cache.containsKey(key));
  }

  @Test
  public void testContainsKey_shouldContain()
  {
    cache.put(key, new RichDocument());

    assertTrue("Map contains previously added key and value", cache.containsKey(key));
  }

  @Test
  public void testGet_empty()
  {
    assertNull(cache.get(key));
  }

  @Test
  public void testPutAndGet()
  {
    Document someDoc = new RichDocument();
    String title = "test title";
    someDoc.setTitle(title);
    cache.put(key, someDoc);

    Document doc = cache.get(key);
    assertNotNull(doc);
    assertEquals("Correct previuosly added item is returned by the map", title, doc.getTitle());
  }

  @Test
  public void testPutIfAbsent_notPresent()
  {
    Document someDoc = new RichDocument();
    String title = "test title";
    someDoc.setTitle(title);
    cache.putIfAbsent(key, someDoc);

    Document doc = cache.get(key);
    assertNotNull(doc);
    assertEquals("The item was correctly added to the map", title, doc.getTitle());
  }

  @Test
  public void testPutIfAbsent_alreadyPresent()
  {
    String title1 = "test title 1";
    Document someDoc1 = new RichDocument();
    someDoc1.setTitle(title1);
    cache.put(key, someDoc1);

    String title2 = "test title 2";
    Document someDoc2 = new RichDocument();
    someDoc2.setTitle(title2);
    Document doc = cache.putIfAbsent(key, someDoc2);
    assertNotNull(doc);
    assertEquals("putIfAbsent() returns the previous value", title1, doc.getTitle());
    doc = cache.get(key);
    assertNotNull(doc);
    assertEquals("The original item was not replaced", title1, doc.getTitle());
  }

  @Test
  public void testReplace_exists()
  {
    String title1 = "test title 1";
    Document someDoc1 = new RichDocument();
    someDoc1.setTitle(title1);
    cache.put(key, someDoc1);

    String title2 = "test title 2";
    Document someDoc2 = new RichDocument();
    someDoc2.setTitle(title2);
    Document doc = cache.replace(key, someDoc2);

    assertNotNull(doc);
    assertEquals("replace(key, newDoc) returns the previous doc", title1, doc.getTitle());
    doc = cache.get(key);
    assertNotNull(doc);
    assertEquals("The original item was replaced", title2, doc.getTitle());
  }

  @Test
  public void testReplace_doesNotExist()
  {
    assertNull(cache.replace(key, new RichDocument()));
  }

  @Test
  public void testRemove()
  {
    String title1 = "test title 1";
    Document someDoc1 = new RichDocument();
    someDoc1.setTitle(title1);
    cache.put(key, someDoc1);

    cache.remove(key);

    assertFalse("The item was removed from the map", cache.containsKey(key));
  }

  @Test
  public void testEvict()
  {
    int n = 10;

    CacheConfiguration config = cache.getEhcache().getCacheConfiguration().clone();
    config.setName("test-evict");
    config.setMaxEntriesLocalHeap(n);
    Ehcache smallCache = new Cache(config);
    cache.getEhcache().getCacheManager().addCache(smallCache);

    for (int i = 0; i < n + n; ++i)
    {
      ParsedURL key = ParsedURL.getAbsolute("http://example.com/page/" + i);
      Document value = new RichDocument();
      value.setTitle("Page " + i);
      // smallCache.putIfAbsent(new Element(key, value)); // this will cause ehcache exception
      smallCache.putIfAbsent(new Element(key.toString(), value));
    }

    assertEquals(n, smallCache.getSize());
  }

}
