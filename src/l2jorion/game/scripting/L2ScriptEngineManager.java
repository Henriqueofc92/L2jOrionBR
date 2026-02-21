package l2jorion.game.scripting;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import l2jorion.Config;

public final class L2ScriptEngineManager
{
	private static final Logger _log = Logger.getLogger(L2ScriptEngineManager.class.getName());
	
	public static final File SCRIPT_FOLDER = new File(Config.DATAPACK_ROOT.getAbsolutePath(), "data/scripts");
	
	public static L2ScriptEngineManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private final Map<String, ScriptEngine> _nameEngines = new HashMap<>();
	private final Map<String, ScriptEngine> _extEngines = new HashMap<>();
	private final List<ScriptManager<?>> _scriptManagers = new LinkedList<>();
	private final ThreadLocal<File> _currentLoadingScript = new ThreadLocal<>();
	
	private static final boolean VERBOSE_LOADING = Config.SCRIPT_DEBUG;
	
	private static final boolean ATTEMPT_COMPILATION = Config.SCRIPT_ALLOW_COMPILATION;
	
	private static final boolean PURGE_ERROR_LOG = Config.SCRIPT_ERROR_LOG;
	
	protected L2ScriptEngineManager()
	{
		ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
		List<ScriptEngineFactory> factories = scriptEngineManager.getEngineFactories();
		
		for (ScriptEngineFactory factory : factories)
		{
			try
			{
				ScriptEngine engine = factory.getScriptEngine();
				boolean reg = false;
				for (String name : factory.getNames())
				{
					ScriptEngine existentEngine = _nameEngines.get(name);
					
					if (existentEngine != null)
					{
						double engineVer = Double.parseDouble(factory.getEngineVersion());
						double existentEngVer = Double.parseDouble(existentEngine.getFactory().getEngineVersion());
						
						if (engineVer <= existentEngVer)
						{
							continue;
						}
					}
					
					reg = false;
					_nameEngines.put(name, engine);
				}
				
				if (reg)
				{
					_log.info("Script Engine: " + factory.getEngineName() + " " + factory.getEngineVersion() + " - Language: " + factory.getLanguageName() + " - Language Version: " + factory.getLanguageVersion());
				}
				
				for (String ext : factory.getExtensions())
				{
					if (!ext.equals("java") || factory.getLanguageName().equals("java"))
					{
						_extEngines.put(ext, engine);
					}
				}
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, "Failed initializing factory: " + e.getMessage(), e);
			}
		}

	}
	
	private ScriptEngine getEngineByName(String name)
	{
		return _nameEngines.get(name);
	}
	
	private ScriptEngine getEngineByExtension(String ext)
	{
		return _extEngines.get(ext);
	}
	
	public void executeScriptList(File list) throws IOException
	{
		if (Config.ALT_DEV_NO_QUESTS)
		{
			return;
		}
		
		final List<File> priorityScripts = new LinkedList<>();
		final List<File> regularScripts = new LinkedList<>();
		
		if (list.isFile())
		{
			try (FileInputStream fis = new FileInputStream(list);
				InputStreamReader isr = new InputStreamReader(fis);
				LineNumberReader lnr = new LineNumberReader(isr))
			{
				String line;
				while ((line = lnr.readLine()) != null)
				{
					String[] parts = line.trim().split("#");
					
					if ((parts.length > 0) && !parts[0].isEmpty() && (parts[0].charAt(0) != '#'))
					{
						line = parts[0];
						
						if (line.endsWith("/**"))
						{
							line = line.substring(0, line.length() - 3);
							File file = new File(SCRIPT_FOLDER, line);
							if (file.isDirectory())
							{
								splitScriptsByPriority(file, true, 32, priorityScripts, regularScripts);
							}
						}
						else if (line.endsWith("/*"))
						{
							line = line.substring(0, line.length() - 2);
							File file = new File(SCRIPT_FOLDER, line);
							if (file.isDirectory())
							{
								splitScriptsByPriority(file, false, 0, priorityScripts, regularScripts);
							}
						}
						else
						{
							File file = new File(SCRIPT_FOLDER, line);
							if (file.isFile())
							{
								boolean isParallel = false;
								String name = file.getName();
								if (Character.isDigit(name.charAt(0)) && !name.contains("Saga"))
								{
									isParallel = true;
								}
								else if (name.startsWith("__init__"))
								{
									String parentName = file.getParentFile().getName();
									if (parentName != null && !parentName.isEmpty() && Character.isDigit(parentName.charAt(0)) && !parentName.contains("Saga")) // Proteção para pastas de Saga
									{
										isParallel = true;
									}
								}
								
								if (isParallel)
								{
									regularScripts.add(file);
								}
								else
								{
									priorityScripts.add(file);
								}
							}
						}
					}
				}
			}
			_log.info("Loading " + priorityScripts.size() + " priority scripts (Libs/AI/Sagas)...");
			for (File file : priorityScripts)
			{
				try
				{
					executeScript(file);
				}
				catch (Exception e)
				{
					reportScriptFileError(file, (e instanceof ScriptException) ? (ScriptException) e : new ScriptException(e));
				}
			}
			_log.info("Loading " + regularScripts.size() + " regular quests using parallel execution...");
			
			regularScripts.parallelStream().forEach(file ->
			{
				try
				{
					executeScript(file);
				}
				catch (ScriptException e)
				{
					reportScriptFileError(file, e);
				}
				catch (Exception e)
				{
					_log.log(Level.WARNING, "Error executing script: " + file.getName(), e);
				}
			});
			
			_log.info("All scripts loaded.");
		}
		else
		{
			throw new IllegalArgumentException("Argument must be an file containing a list of scripts to be loaded");
		}
	}
	
	private void splitScriptsByPriority(File dir, boolean recurseDown, int maxDepth, List<File> priorityList, List<File> parallelList)
	{
		splitScriptsRecursive(dir, recurseDown, maxDepth, 0, priorityList, parallelList);
	}
	
	private void splitScriptsRecursive(File dir, boolean recurseDown, int maxDepth, int currentDepth, List<File> priorityList, List<File> parallelList)
	{
		if (dir.isDirectory())
		{
			final File[] files = dir.listFiles();
			if (files == null)
			{
				return;
			}
			
			for (File file : files)
			{
				if (file.isDirectory() && recurseDown && (maxDepth > currentDepth))
				{
					splitScriptsRecursive(file, recurseDown, maxDepth, currentDepth + 1, priorityList, parallelList);
				}
				else if (file.isFile())
				{
					String name = file.getName();
					if (name.contains(".") && getEngineByExtension(name.substring(name.lastIndexOf('.') + 1)) != null)
					{
						boolean isParallel = false;
						if (Character.isDigit(name.charAt(0)) && !name.contains("Saga"))
						{
							isParallel = true;
						}
						else if (name.startsWith("__init__"))
						{
							String parentName = file.getParentFile().getName();
							if (parentName != null && !parentName.isEmpty() && Character.isDigit(parentName.charAt(0)) && !parentName.contains("Saga"))
							{
								isParallel = true;
							}
						}
						
						if (isParallel)
						{
							parallelList.add(file);
						}
						else
						{
							priorityList.add(file);
						}
					}
				}
			}
		}
	}
	
	private void collectAllScriptsInDirectory(File dir, boolean recurseDown, int maxDepth, List<File> collector)
	{
		collectAllScriptsInDirectoryRecursive(dir, recurseDown, maxDepth, 0, collector);
	}
	
	private void collectAllScriptsInDirectoryRecursive(File dir, boolean recurseDown, int maxDepth, int currentDepth, List<File> collector)
	{
		if (dir.isDirectory())
		{
			final File[] files = dir.listFiles();
			if (files == null)
			{
				return;
			}
			
			for (File file : files)
			{
				if (file.isDirectory() && recurseDown && (maxDepth > currentDepth))
				{
					collectAllScriptsInDirectoryRecursive(file, recurseDown, maxDepth, currentDepth + 1, collector);
				}
				else if (file.isFile())
				{
					String name = file.getName();
					if (name.contains(".") && getEngineByExtension(name.substring(name.lastIndexOf('.') + 1)) != null)
					{
						collector.add(file);
					}
				}
			}
		}
	}
	
	public void executeScript(File file) throws ScriptException
	{
		String name = file.getName();
		int lastIndex = name.lastIndexOf('.');
		String extension;
		if (lastIndex != -1)
		{
			extension = name.substring(lastIndex + 1);
		}
		else
		{
			throw new ScriptException("Script file (" + name + ") doesnt has an extension that identifies the ScriptEngine to be used.");
		}
		
		ScriptEngine engine = getEngineByExtension(extension);
		if (engine == null)
		{
			throw new ScriptException("No engine registered for extension (" + extension + ")");
		}
		executeScript(engine, file);
	}
	
	public void executeScript(String engineName, File file) throws ScriptException
	{
		ScriptEngine engine = getEngineByName(engineName);
		if (engine == null)
		{
			throw new ScriptException("No engine registered with name (" + engineName + ")");
		}
		executeScript(engine, file);
	}
	
	public void executeScript(ScriptEngine engine, File file) throws ScriptException
	{
		if (VERBOSE_LOADING)
		{
			_log.info("Loading Script: " + file.getAbsolutePath());
		}
		
		if (PURGE_ERROR_LOG)
		{
			String name = file.getAbsolutePath() + ".error.log";
			File errorLog = new File(name);
			if (errorLog.isFile())
			{
				errorLog.delete();
			}
		}
		
		final String relativeName = file.getAbsolutePath().substring(SCRIPT_FOLDER.getAbsolutePath().length() + 1).replace('\\', '/');
		try (FileInputStream fis = new FileInputStream(file);
			InputStreamReader isr = new InputStreamReader(fis);
			BufferedReader reader = new BufferedReader(isr))
		{
			if ((engine instanceof Compilable) && ATTEMPT_COMPILATION)
			{
				ScriptContext context = new SimpleScriptContext();
				context.setAttribute("mainClass", getClassForFile(file).replace('/', '.').replace('\\', '.'), ScriptContext.ENGINE_SCOPE);
				context.setAttribute(ScriptEngine.FILENAME, relativeName, ScriptContext.ENGINE_SCOPE);
				context.setAttribute("classpath", SCRIPT_FOLDER.getAbsolutePath(), ScriptContext.ENGINE_SCOPE);
				context.setAttribute("sourcepath", SCRIPT_FOLDER.getAbsolutePath(), ScriptContext.ENGINE_SCOPE);
				
				setCurrentLoadingScript(file);
				ScriptContext ctx = engine.getContext();
				try
				{
					engine.setContext(context);
					Compilable eng = (Compilable) engine;
					CompiledScript cs = eng.compile(reader);
					cs.eval(context);
				}
				finally
				{
					engine.setContext(ctx);
					setCurrentLoadingScript(null);
					context.removeAttribute(ScriptEngine.FILENAME, ScriptContext.ENGINE_SCOPE);
					context.removeAttribute("mainClass", ScriptContext.ENGINE_SCOPE);
				}
			}
			else
			{
				ScriptContext context = new SimpleScriptContext();
				context.setAttribute("mainClass", getClassForFile(file).replace('/', '.').replace('\\', '.'), ScriptContext.ENGINE_SCOPE);
				context.setAttribute(ScriptEngine.FILENAME, relativeName, ScriptContext.ENGINE_SCOPE);
				context.setAttribute("classpath", SCRIPT_FOLDER.getAbsolutePath(), ScriptContext.ENGINE_SCOPE);
				context.setAttribute("sourcepath", SCRIPT_FOLDER.getAbsolutePath(), ScriptContext.ENGINE_SCOPE);
				setCurrentLoadingScript(file);
				try
				{
					engine.eval(reader, context);
				}
				finally
				{
					setCurrentLoadingScript(null);
					engine.getContext().removeAttribute(ScriptEngine.FILENAME, ScriptContext.ENGINE_SCOPE);
					engine.getContext().removeAttribute("mainClass", ScriptContext.ENGINE_SCOPE);
				}
			}
		}
		catch (IOException e)
		{
			_log.log(Level.WARNING, "Error executing script!", e);
		}
	}
	
	public static String getClassForFile(File script)
	{
		String path = script.getAbsolutePath();
		String scpPath = SCRIPT_FOLDER.getAbsolutePath();
		if (path.startsWith(scpPath))
		{
			int idx = path.lastIndexOf('.');
			return path.substring(scpPath.length() + 1, idx);
		}
		return null;
	}
	
	public ScriptContext getScriptContext(ScriptEngine engine)
	{
		return engine.getContext();
	}
	
	public ScriptContext getScriptContext(String engineName)
	{
		ScriptEngine engine = getEngineByName(engineName);
		if (engine == null)
		{
			throw new IllegalStateException("No engine registered with name (" + engineName + ")");
		}
		return getScriptContext(engine);
	}
	
	public Object eval(ScriptEngine engine, String script, ScriptContext context) throws ScriptException
	{
		if ((engine instanceof Compilable) && ATTEMPT_COMPILATION)
		{
			Compilable eng = (Compilable) engine;
			CompiledScript cs = eng.compile(script);
			return context != null ? cs.eval(context) : cs.eval();
		}
		return context != null ? engine.eval(script, context) : engine.eval(script);
	}
	
	public Object eval(String engineName, String script) throws ScriptException
	{
		return eval(engineName, script, null);
	}
	
	public Object eval(String engineName, String script, ScriptContext context) throws ScriptException
	{
		ScriptEngine engine = getEngineByName(engineName);
		if (engine == null)
		{
			throw new ScriptException("No engine registered with name (" + engineName + ")");
		}
		return eval(engine, script, context);
	}
	
	public Object eval(ScriptEngine engine, String script) throws ScriptException
	{
		return eval(engine, script, null);
	}
	
	public void reportScriptFileError(File script, ScriptException e)
	{
		String dir = script.getParent();
		String name = script.getName() + ".error.log";
		if (dir != null)
		{
			final File file = new File(dir + "/" + name);
			try (FileOutputStream fos = new FileOutputStream(file))
			{
				String errorHeader = "Error on: " + file.getCanonicalPath() + Config.EOL + "Line: " + e.getLineNumber() + " - Column: " + e.getColumnNumber() + Config.EOL + Config.EOL;
				fos.write(errorHeader.getBytes());
				fos.write(e.getMessage().getBytes());
				_log.warning("Failed executing script: " + script.getAbsolutePath() + ". See " + file.getName() + " for details.");
			}
			catch (IOException ioe)
			{
				_log.log(Level.WARNING, "Failed executing script: " + script.getAbsolutePath() + Config.EOL + e.getMessage() + "Additionally failed when trying to write an error report on script directory. Reason: " + ioe.getMessage(), ioe);
			}
		}
		else
		{
			_log.log(Level.WARNING, "Failed executing script: " + script.getAbsolutePath() + Config.EOL + e.getMessage() + "Additionally failed when trying to write an error report on script directory.", e);
		}
	}
	
	public void executeAllScriptsInDirectory(File dir)
	{
		executeAllScriptsInDirectory(dir, false, 0);
	}
	
	public void executeAllScriptsInDirectory(File dir, boolean recurseDown, int maxDepth)
	{
		final List<File> scriptFiles = new LinkedList<>();
		
		collectAllScriptsInDirectory(dir, recurseDown, maxDepth, scriptFiles);
		
		if (!scriptFiles.isEmpty())
		{
			_log.info("Loading " + scriptFiles.size() + " scripts from " + dir.getName() + " using parallel execution...");
			
			scriptFiles.parallelStream().forEach(file ->
			{
				try
				{
					executeScript(file);
				}
				catch (ScriptException e)
				{
					reportScriptFileError(file, e);
				}
				catch (Exception e)
				{
					_log.log(Level.WARNING, "Error executing script: " + file.getName(), e);
				}
			});
		}
		else
		{
			_log.warning("No scripts found in directory: " + dir.getAbsolutePath());
		}
	}
	
	public void registerScriptManager(ScriptManager<?> manager)
	{
		_scriptManagers.add(manager);
	}
	
	public void removeScriptManager(ScriptManager<?> manager)
	{
		_scriptManagers.remove(manager);
	}
	
	public List<ScriptManager<?>> getScriptManagers()
	{
		return _scriptManagers;
		
	}
	
	protected void setCurrentLoadingScript(File currentLoadingScript)
	{
		_currentLoadingScript.set(currentLoadingScript);
	}
	
	public File getCurrentLoadingScript()
	{
		return _currentLoadingScript.get();
	}
	
	private static class SingletonHolder
	{
		protected static final L2ScriptEngineManager _instance = new L2ScriptEngineManager();
	}
}