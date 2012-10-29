package moa.trident.state.jcs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.jcs.JCS;
import org.apache.jcs.access.exception.CacheException;

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
	
	private JCS m_jcs;
	private String m_key;
	private T m_instance;
	
	public JCSState(String key) throws IOException, CacheException
	{
		JCS.setConfigFilename("/moa/trident/state/jcs/jcs.properties");
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

	public void set(T o) {
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
