package moa.trident.state.jcs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.jcs.JCS;
import org.apache.jcs.access.exception.CacheException;
import org.apache.jcs.engine.control.CompositeCacheManager;

import backtype.storm.tuple.Values;

import storm.trident.state.State;
import storm.trident.state.StateFactory;
import storm.trident.state.ValueUpdater;
import storm.trident.state.map.IBackingMap;
import storm.trident.state.map.MapState;
import storm.trident.state.map.SnapshottableMap;
import storm.trident.state.snapshot.Snapshottable;

/** 
 * An Apache JCS (http://commons.apache.org/jcs/) backed trident state implementation. 
 * @author bsp
 *
 * @param <T> - type to cache.
 */
public class JCSState <T> implements Snapshottable<T>{
	
	private static boolean CONFIGURED = false;
	private JCS m_jcs;
	private String m_key;
	private T m_instance;
	
	private int getPid() throws Throwable
	{
		java.lang.management.RuntimeMXBean runtime = java.lang.management.ManagementFactory.getRuntimeMXBean();
		java.lang.reflect.Field jvm = runtime.getClass().getDeclaredField("jvm");
		jvm.setAccessible(true);
		sun.management.VMManagement mgmt = (sun.management.VMManagement) jvm.get(runtime);
		java.lang.reflect.Method pid_method = mgmt.getClass().getDeclaredMethod("getProcessId");
		pid_method.setAccessible(true);
		int pid = (Integer) pid_method.invoke(mgmt);
		return pid;
	}
	
	public JCSState(String key) throws IOException, CacheException
	{
		if (!JCSState.CONFIGURED)
		{
			int port = 31110;
			try // hack, to use unique port number for each cache instance
			{
				int pid = getPid();
				pid = pid % 1000;
				port = port + pid;
			}
			catch (Throwable t) { throw new IOException(t); }

			java.util.Properties prp = new java.util.Properties();
			prp.load(getClass().getResourceAsStream("/moa/trident/state/jcs/jcs.properties"));
			prp.setProperty("jcs.auxiliary.LTCP.attributes.TcpListenerPort", port + "");
			CompositeCacheManager.getUnconfiguredInstance().configure(prp);
			JCSState.CONFIGURED= true;
			
		}
		m_jcs = JCS.getInstance("sharedCache");
		m_key = key;
	}
	
	public static StateFactory create(String key) {
		return new Factory(key);
	}

	public static class Factory implements StateFactory {
		private String m_key;
		public Factory(String key) {
			m_key = key;
		}
		public State makeState(Map conf, int partitionIndex, int numPartitions) {
			try {
				return new JCSState<Object>(m_key);
			} catch (IOException e) {
				throw new RuntimeException(e);
			} catch (CacheException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public T get() {
		return (T) m_jcs.get(m_key);
	}

	public T update(ValueUpdater updater) {
		m_instance = (T) updater.update(m_instance);
		return m_instance;
	}

	public synchronized void set(T o) {
		try {
			m_jcs.put( m_key, o);
		} catch (CacheException e) {
			throw new RuntimeException(e);
		}
	}

	public void beginCommit(Long txid) {
		m_instance = get(); 
	}

	public void commit(Long txid) {
		set(m_instance);
	}

}
