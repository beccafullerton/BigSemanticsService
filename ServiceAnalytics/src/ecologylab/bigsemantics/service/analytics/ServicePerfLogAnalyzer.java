/**
 * 
 */
package ecologylab.bigsemantics.service.analytics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import ecologylab.bigsemantics.service.logging.ServiceLogRecord;
import ecologylab.generic.Debug;
import ecologylab.net.ParsedURL;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.formatenums.StringFormat;

/**
 * ServicePerfLogAnalyzer accepts performance logs generated by BigSemanticsService and calculates
 * performance metrics using them. begin/end timestamps are used to constrain the analysis window.
 * 
 * @author ajit
 * 
 */

public class ServicePerfLogAnalyzer extends Debug
{
	Date												fromTime, toTime; // analysis window specified w timestamps

	ArrayList<File>							logFiles;				// service perf log files

	ArrayList<ServiceLogRecord>	logRecords;			// log records deserialized from from log files

	final int										STATUS_OK	= 200;	// HTTP response code

	public ServicePerfLogAnalyzer(ArrayList<File> logFiles, Date fromTime, Date toTime)
	{
		this.logFiles = logFiles;
		this.fromTime = fromTime;
		this.toTime = toTime;
		this.logRecords = new ArrayList<ServiceLogRecord>();
	}

	private void readPerfLogs() throws IOException
	{
		SimplTypesScope tscope = SimplTypesScope.get("perf-log-analysis", ServiceLogRecord.class);
		if (logFiles != null)
		{
			for (File logFile : logFiles)
			{
				BufferedReader reader = null;
				try
				{
					reader = new BufferedReader(new FileReader(logFile));
					String rec;
					while ((rec = reader.readLine()) != null)
					{
						try
						{
						  int logBeginPos = rec.indexOf('{');
						  if (logBeginPos < 0)
						  {
						    continue;
						  }
							rec = rec.substring(logBeginPos); // discard log4j header
							ServiceLogRecord logRecord = (ServiceLogRecord) tscope.deserialize(rec,
									StringFormat.JSON);
							if (logRecord != null)
							{
								if (fromTime != null && (logRecord.getBeginTime().compareTo(fromTime) < 0))
									continue;
								if (toTime != null && (logRecord.getBeginTime().compareTo(toTime) > 0))
									continue;
							}
							logRecords.add(logRecord);
						}
						catch (SIMPLTranslationException e)
						{
							debug("Couldn't deserialize " + rec);
							e.printStackTrace();
						}
					}
				}
				catch (FileNotFoundException e)
				{
					debug("Shouldn't have occurred. Already checked if exists.");
					e.printStackTrace();
				}
				finally
				{
					if (reader != null)
						reader.close();
				}
			}
		}
	}

	private void getPerformanceMetrics() throws IOException
	{
		readPerfLogs();

		if (logRecords != null)
		{
			float avgLatency = 0, avgLatencyWoQueuedWait = 0, nSuccessful = 0;
			float avgTimeInDownloading = 0, avgTimeInExtraction = 0, avgTimeInSerialization = 0;
			float avgLatencyS = 0, avgLatencyWoQueuedWaitS = 0, avgTimeInDownloadingS = 0, avgTimeInExtractionS = 0, avgTimeInSerializationS = 0;

			for (ServiceLogRecord logRecord : logRecords)
			{
				float latency = logRecord.getMsTotal();
				avgLatency += latency;

				ArrayList<Long> queuePeekIntervals = logRecord.getQueuePeekIntervals();
				if (queuePeekIntervals.size() > 0)
					avgLatencyWoQueuedWait += (latency - queuePeekIntervals
							.get(queuePeekIntervals.size() - 1));
				else
					avgLatencyWoQueuedWait += latency;

				avgTimeInDownloading += logRecord.getmSecInHtmlDownload();
				avgTimeInExtraction += logRecord.getmSecInExtraction();
				avgTimeInSerialization += logRecord.getmSecInSerialization();
				
				if (logRecord.getResponseCode() == STATUS_OK)
				{
					nSuccessful++;
					avgLatencyS += latency;
					
					if (queuePeekIntervals.size() > 0)
						avgLatencyWoQueuedWaitS += (latency - queuePeekIntervals
								.get(queuePeekIntervals.size() - 1));
					else
						avgLatencyWoQueuedWaitS += latency;
					
					avgTimeInDownloadingS += logRecord.getmSecInHtmlDownload();
					avgTimeInExtractionS += logRecord.getmSecInExtraction();
					avgTimeInSerializationS += logRecord.getmSecInSerialization();					
				}
			}

			int size = logRecords.size();
			
			debug("================= Service Metrics =================\n");
			debug("From : " + fromTime + "  To : " + toTime);
			debug("Time span (ms): " + (toTime.getTime() - fromTime.getTime()) + "\n");
			debug("No. of Requests : " + size);
			debug("Success Rate (%) : " + ((nSuccessful / size) * 100));
			debug("Throughput (successful requests / second) : "
					+ (nSuccessful * 1000 / (toTime.getTime() - fromTime.getTime())) + "\n");
			
			debug("Average Latency (ms): " + (avgLatency / size));
			debug("Average Latency (exluding download queue waiting period) (ms): "
					+ (avgLatencyWoQueuedWait / size) + "\n\n");
			
			debug("-------------- Component-wise Summary --------------\n");
			debug("Average Downloading time (ms) : " + (avgTimeInDownloading / size) + "\n");
			debug("Average Extraction time (ms) :" + (avgTimeInExtraction / size) + "\n");
			debug("Average Serialization time (ms) :" + (avgTimeInSerialization / size) + "\n\n");
			
			debug("********* Successful Requests **********\n");
			debug("Average Latency (ms): " + (avgLatencyS / nSuccessful));
			debug("Average Latency (exluding download queue waiting period) (ms): "
					+ (avgLatencyWoQueuedWaitS / nSuccessful) + "\n\n");
			
			debug("-------------- Component-wise Summary --------------\n");
			debug("Average Downloading time (ms) : " + (avgTimeInDownloadingS / nSuccessful) + "\n");
			debug("Average Extraction time (ms) :" + (avgTimeInExtractionS / nSuccessful) + "\n");
			debug("Average Serialization time (ms) :" + (avgTimeInSerializationS / nSuccessful) + "\n\n");
			debug("****************************************\n");
			
			debug("===================================================\n");
		}
	}
	
	private void getExtractionTimes() throws IOException
	{
	  readPerfLogs();
	  
	  if (logRecords != null)
	  {
	    List<ServiceLogRecord> sorted = new ArrayList<ServiceLogRecord>();

	    for (ServiceLogRecord logRecord : logRecords)
	    {
	      if (logRecord != null)
	      {
  	      ParsedURL purl = logRecord.getDocumentUrl();
  	      if (purl != null)
  	      {
  	        sorted.add(logRecord);
  	      }
	      }
	    }
	    
      Collections.sort(sorted,
                       new Comparator<ServiceLogRecord>()
                       {
                         @Override
                         public int compare(ServiceLogRecord r1, ServiceLogRecord r2)
                         {
                           long x = r1.getmSecInExtraction() - r2.getmSecInExtraction();
                           return (x>0)?(-1):((x==0)?0:1);
                         }
                       });

	    for (ServiceLogRecord logRecord : sorted)
	    {
	      System.out.format("%s\t%s\n", logRecord.getDocumentUrl(), logRecord.getmSecInExtraction());
	    }
	  }
	}

	public static void main(String[] args) throws IOException
	{
		String usage = "Usage: java ServicePerfLogAnalyzer [OPTIONS] <log-file-1> <log-file-2> ... <log-file-N>\n\n"
				+ "OPTIONS: \n\n"
				+ "--from <yyyy-MM-dd/HH:mm:ss>\n\n"
				+ "--to <yyyy-MM-dd/HH:mm:ss>\n\n"
				+ "--tz <timezone-name>\n" + "e.g. CST, PDT, GMT etc.\n\n";

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'/'HH:mm:ss");

		Date fromTime = null;
		Date toTime = null;
		ArrayList<File> logFiles = new ArrayList<File>();

		if (args.length == 0)
		{
			System.out.println(usage);
			return;
		}

		int i = 0;
		while (i < args.length)
		{
			String item = args[i++];
			try
			{
				if (item.equals("--from") && i < args.length)
				{
					fromTime = sdf.parse(args[i++]);
				}
				else if (item.equals("--to") && i < args.length)
				{
					toTime = sdf.parse(args[i++]);
				}
				else if (item.equals("--tz"))
				{
					// TODO
				}
				else
				{
					File logFile = new File(item);
					if (logFile.exists())
						logFiles.add(logFile);
					else
					{
						System.out.println("File <" + item + "> not found.");
						return;
					}
				}
			}
			catch (ParseException e)
			{
				System.out.println("Date format is not correct.");
				System.out.println(usage);
				e.printStackTrace();
				return;
			}
		}

		if (fromTime != null && toTime != null && fromTime.compareTo(toTime) > 0)
		{
			System.out.println("'from' timestamp > 'to' timestamp");
			return;
		}

		ServicePerfLogAnalyzer s1 = new ServicePerfLogAnalyzer(logFiles, fromTime, toTime);
		s1.getExtractionTimes();
		//s1.getPerformanceMetrics();
	}
}
