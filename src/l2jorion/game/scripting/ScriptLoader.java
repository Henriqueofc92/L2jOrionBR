package l2jorion.game.scripting;

import java.io.File;
import java.lang.reflect.Method;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import l2jorion.Config;

public final class ScriptLoader
{
	private static final Logger LOG = Logger.getLogger(ScriptLoader.class.getName());
	private static final String BASE_PACKAGE = "l2jorion.game.data.";
	
	public static void loadAllScripts()
	{
		final File xml = new File(Config.DATAPACK_ROOT, "data/xml/scripts.xml");
		if (!xml.exists())
		{
			LOG.severe("ScriptLoader: scripts.xml not found at " + xml.getAbsolutePath());
			return;
		}
		
		int loaded = 0;
		int failed = 0;
		
		try
		{
			final Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xml);
			final NodeList nodes = doc.getElementsByTagName("script");
			
			for (int i = 0; i < nodes.getLength(); i++)
			{
				final Node node = nodes.item(i);
				final NamedNodeMap attrs = node.getAttributes();
				if (attrs == null)
				{
					continue;
				}
				
				final Node pathAttr = attrs.getNamedItem("path");
				if (pathAttr == null)
				{
					continue;
				}
				
				final String path = pathAttr.getNodeValue();
				final String className = BASE_PACKAGE + path;
				
				try
				{
					final Class<?> clazz = Class.forName(className);
					final Method main = clazz.getMethod("main", String[].class);
					main.invoke(null, (Object) new String[0]);
					loaded++;
				}
				catch (Exception e)
				{
					LOG.warning("ScriptLoader: Failed to load " + className + " - " + e.getMessage());
					failed++;
				}
			}
		}
		catch (Exception e)
		{
			LOG.severe("ScriptLoader: Error parsing scripts.xml - " + e.getMessage());
		}
		
		LOG.info("ScriptLoader: Loaded " + loaded + " scripts" + (failed > 0 ? " (" + failed + " failed)" : ""));
	}
}
