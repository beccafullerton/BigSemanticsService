/**
 * 
 */
package ecologylab.semantic.service.dbinterface;

import java.util.List;

import org.hibernate.Session;

import ecologylab.net.ParsedURL;
import ecologylab.semantics.collecting.DownloadStatus;
import ecologylab.semantics.compiler.orm.MetadataORMFacade;
import ecologylab.semantics.dbinterface.IDocumentCache;
import ecologylab.semantics.metadata.builtins.Document;
import ecologylab.semantics.metadata.builtins.DocumentClosure;
import ecologylab.serialization.TranslationContext;
import ecologylab.serialization.TranslationContextPool;

/**
 * implements IDBDocumentprovider for database retrieving and storing documents
 * 
 * @author ajit
 *
 */

public class DBDocumentCache implements IDocumentCache
{
	private static Session						hibernateSession;

	private static MetadataORMFacade	ormFacade;

	static
	{
		ormFacade = MetadataORMFacade.defaultSingleton();
		hibernateSession = ormFacade.newSession();
	}

	@Override
	public Document retrieveDocument(DocumentClosure closure)
	{
		ParsedURL url = closure.location();
		synchronized (ormFacade)
		{
			List<Document> documents = ormFacade.lookupDocumentByLocation(hibernateSession, url);
			for (Document doc : documents)
			{
				if (doc.getDownloadStatus() == DownloadStatus.DOWNLOAD_DONE)
				{
					TranslationContext translationContext = TranslationContextPool.get().acquire();
					ormFacade.materialize(doc, translationContext, null);
					return doc;
				}
			}
		}
		return null;
	}

	public void storeDocument(Document document)
	{
		final Document newDocument = document;

		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				synchronized (ormFacade)
				{
					ormFacade.recursivelySave(hibernateSession, newDocument);
				}
			}
		}).start();
	}

	@Override
	public void removeDocument(ParsedURL url)
	{
		// TODO Auto-generated method stub
	}
}
